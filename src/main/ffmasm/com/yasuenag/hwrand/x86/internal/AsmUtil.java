package com.yasuenag.hwrand.x86.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;


public class AsmUtil{

  private static final CodeSegment codeSegment;

  // These fields will be written from native code
  private static final boolean supportedRDRAND;
  private static final boolean supportedRDSEED;

  // Collection of registers for arguments
  public static enum ArgRegisters{
    LinuxFFM(Register.RDI, Register.RSI, Register.RDX),
    WindowsFFM(Register.RCX, Register.RDX, Register.R8),
    LinuxJVMCI(Register.RDX, Register.RCX, Register.R8),
    WindowsJVMCI(Register.R8, Register.R9, Register.RDI);

    private final Register arg1;
    private final Register arg2;
    private final Register arg3;

    private ArgRegisters(Register arg1, Register arg2, Register arg3){
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
    }

    public Register arg1(){
      return arg1;
    }

    public Register arg2(){
      return arg2;
    }

    public Register arg3(){
      return arg3;
    }

    public static ArgRegisters get(boolean isJVMCI) throws UnsupportedPlatformException{
      String osArch = System.getProperty("os.arch");
      if(!osArch.equals("amd64")){
        throw new UnsupportedPlatformException("Unsupported architecture: " + osArch);
      }

      String osName = System.getProperty("os.name");
      if(osName.equals("Linux")){
        return isJVMCI ? LinuxJVMCI : LinuxFFM;
      }
      else if(osName.startsWith("Windows")){
        return isJVMCI ? WindowsJVMCI : WindowsFFM;
      }
      else{
        throw new UnsupportedPlatformException("Unsupported OS: " + osName);
      }
    }

  }

  private static MethodHandle createCPUID() throws UnsupportedPlatformException{
    var argRegs = ArgRegisters.get(false);
    var cpuidDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.JAVA_INT, // 1st argument (EAX)
                  ValueLayout.JAVA_INT, // 2nd argument (ECX)
                  ValueLayout.ADDRESS   // 3rd argument (result)
                );
    return new AsmBuilder.AMD64(codeSegment, cpuidDesc)
/* push %rbp          */ .push(Register.RBP)
/* mov %rsp, %rbp     */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
/* push %rbx          */ .push(Register.RBX)
/* mov <arg1>, %rax   */ .movMR(argRegs.arg1(), Register.RAX, OptionalInt.empty())
/* mov <arg2>, %rcx   */ .movMR(argRegs.arg2(), Register.RCX, OptionalInt.empty())
/* mov <arg3>, %r11   */ .movMR(argRegs.arg3(), Register.R11, OptionalInt.empty())
/* cpuid              */ .cpuid()
/* mov %eax, (%r11)   */ .movMR(Register.EAX, Register.R11, OptionalInt.of(0))
/* mov %ebx, 4(%r11)  */ .movMR(Register.EBX, Register.R11, OptionalInt.of(4))
/* mov %ecx, 8(%r11)  */ .movMR(Register.ECX, Register.R11, OptionalInt.of(8))
/* mov %edx, 12(%r11) */ .movMR(Register.EDX, Register.R11, OptionalInt.of(12))
/* pop %rbx           */ .pop(Register.RBX, OptionalInt.empty())
/* leave              */ .leave()
/* ret                */ .ret()
                         .build(Linker.Option.critical(true));
  }

  static{
    String osArch = System.getProperty("os.arch");
    if(!osArch.equals("amd64")){
      throw new RuntimeException("Unsupported architecture: " + osArch);
    }

    try(var arena = Arena.ofConfined()){
      codeSegment = new CodeSegment();
      var action = new CodeSegment.CleanerAction(codeSegment);
      Cleaner.create()
             .register(AsmUtil.class, action);

      var cpuid = createCPUID();
      var mem = arena.allocate(ValueLayout.JAVA_INT, 4);

      /* RDRAND */
      cpuid.invoke(1, 0, mem);
      supportedRDRAND = ((mem.getAtIndex(ValueLayout.JAVA_INT, 2) >>> 30) & 1) == 1;

      /* RDSEED */
      cpuid.invoke(7, 0, mem);
      supportedRDSEED = ((mem.getAtIndex(ValueLayout.JAVA_INT, 1) >>> 18) & 1) == 1;
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static void createRDRANDBody(AMD64AsmBuilder<?> builder, ArgRegisters argRegs, int arrayOffset){
    if(arrayOffset > 0){
      builder.add(argRegs.arg1(), arrayOffset, OptionalInt.empty())
             .movImm(argRegs.arg2(), arrayOffset);
    }

    builder
/* .align 16            */ .alignTo16BytesWithNOP()
/* bulk:                */ .label("bulk")
/*   cmp $8, <arg2>     */ .cmp(argRegs.arg2(), 8, OptionalInt.empty())
/*   jl last_call       */ .jl("last_call")
/* retry:               */ .label("retry")
/*   rdrand %rax        */ .rdrand(Register.RAX)
/*   jae retry          */ .jae("retry")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, argRegs.arg1(), OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(argRegs.arg1(), 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(argRegs.arg2(), 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   je exit            */ .je("exit")
/* last_call:           */ .label("last_call")
/*   rdrand %rax        */ .rdrand(Register.RAX)
/*   jae last_call      */ .jae("last_call")
/* .align 16            */ .alignTo16BytesWithNOP()
/* proc1byte:           */ .label("proc1byte")
/*   mov %al, (<arg1>)  */ .movMR(Register.AL, argRegs.arg1(), OptionalInt.of(0))
/*   add $1, <arg1>     */ .add(argRegs.arg1(), 1, OptionalInt.empty())
/*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
/*   sub $1, <arg2>     */ .sub(argRegs.arg2(), 1, OptionalInt.empty())
/*   jne proc1byte      */ .jne("proc1byte")
/* exit:                */ .label("exit");
  }

  public static void createRDSEEDBody(AMD64AsmBuilder<?> builder, ArgRegisters argRegs, int arrayOffset){
    if(arrayOffset > 0){
      builder.add(argRegs.arg1(), arrayOffset, OptionalInt.empty())
             .movImm(argRegs.arg2(), arrayOffset);
    }

    builder
/* .align 16            */ .alignTo16BytesWithNOP()
/* bulk:                */ .label("bulk")
/*   cmp $8, <arg2>     */ .cmp(argRegs.arg2(), 8, OptionalInt.empty())
/*   jl last_call       */ .jl("last_call")
/* retry:               */ .label("retry")
/*   rdseed %rax        */ .rdseed(Register.RAX)
/*   jae retry          */ .jae("retry")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, argRegs.arg1(), OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(argRegs.arg1(), 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(argRegs.arg2(), 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   je exit            */ .je("exit")
/* last_call:           */ .label("last_call")
/*   rdseed %rax        */ .rdseed(Register.RAX)
/*   jae last_call      */ .jae("last_call")
/* .align 16            */ .alignTo16BytesWithNOP()
/* proc1byte:           */ .label("proc1byte")
/*   mov %al, (<arg1>)  */ .movMR(Register.AL, argRegs.arg1(), OptionalInt.of(0))
/*   add $1, <arg1>     */ .add(argRegs.arg1(), 1, OptionalInt.empty())
/*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
/*   sub $1, <arg2>     */ .sub(argRegs.arg2(), 1, OptionalInt.empty())
/*   jne proc1byte      */ .jne("proc1byte")
/* exit:                */ .label("exit");
  }

  public static CodeSegment getCodeSegment(){
    return codeSegment;
  }

  public static boolean isRDRANDAvailable(){
    return supportedRDRAND;
  }

  public static boolean isRDSEEDAvailable(){
    return supportedRDSEED;
  }

}
