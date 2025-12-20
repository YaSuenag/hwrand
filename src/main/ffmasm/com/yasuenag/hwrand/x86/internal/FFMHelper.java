package com.yasuenag.hwrand.x86.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.security.Provider;
import java.util.Map;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.hwrand.x86.FFMRdRand;
import com.yasuenag.hwrand.x86.FFMRdSeed;


public class FFMHelper{

  private static CodeSegment codeSegment;

  private static boolean initialized = false;

  // These fields will be written from native code
  private static boolean supportedRDRAND = false;
  private static boolean supportedRDSEED = false;

  private static MethodHandle cpuid;
  private static MethodHandle fillWithRDRAND;
  private static MethodHandle fillWithRDSEED;

  public static record ArgRegisters(Register arg1, Register arg2, Register arg3){};

  protected static ArgRegisters argRegisters;

  private static void createCPUID() throws UnsupportedPlatformException{
    var cpuidDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.JAVA_INT, // 1st argument (EAX)
                  ValueLayout.JAVA_INT, // 2nd argument (ECX)
                  ValueLayout.ADDRESS   // 3rd argument (result)
                );
    cpuid = new AsmBuilder.AMD64(codeSegment, cpuidDesc)
 /* push %rbp          */ .push(Register.RBP)
 /* mov %rsp, %rbp     */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
 /* push %rbx          */ .push(Register.RBX)
 /* mov <arg1>, %rax   */ .movMR(argRegisters.arg1(), Register.RAX, OptionalInt.empty())
 /* mov <arg2>, %rcx   */ .movMR(argRegisters.arg2(), Register.RCX, OptionalInt.empty())
 /* mov <arg3>, %r11   */ .movMR(argRegisters.arg3(), Register.R11, OptionalInt.empty())
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

  protected static void createRDRANDBody(AsmBuilder.AMD64 builder){
    builder
/* bulk:                */ .label("bulk")
/*   cmp $8, <arg2>     */ .cmp(argRegisters.arg2(), 8, OptionalInt.empty())
/*   jl last_call       */ .jl("last_call")
/* retry:               */ .label("retry")
/*   rdrand %rax        */ .rdrand(Register.RAX)
/*   jae retry          */ .jae("retry")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, argRegisters.arg1(), OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(argRegisters.arg1(), 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(argRegisters.arg2(), 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   je exit            */ .je("exit")
/* last_call:           */ .label("last_call")
/*   rdrand %rax        */ .rdrand(Register.RAX)
/*   jae last_call      */ .jae("last_call")
/* .align 16            */ .alignTo16BytesWithNOP()
/* proc1byte:           */ .label("proc1byte")
/*   mov %al, (<arg1>)  */ .movMR(Register.AL, argRegisters.arg1(), OptionalInt.of(0))
/*   add $1, <arg1>     */ .add(argRegisters.arg1(), 1, OptionalInt.empty())
/*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
/*   sub $1, <arg2>     */ .sub(argRegisters.arg2(), 1, OptionalInt.empty())
/*   jne proc1byte      */ .jne("proc1byte")
/* exit:                */ .label("exit");
  }

  protected static void createRDSEEDBody(AsmBuilder.AMD64 builder){
    builder
/* bulk:                */ .label("bulk")
/*   cmp $8, <arg2>     */ .cmp(argRegisters.arg2(), 8, OptionalInt.empty())
/*   jl last_call       */ .jl("last_call")
/* retry:               */ .label("retry")
/*   rdseed %rax        */ .rdseed(Register.RAX)
/*   jae retry          */ .jae("retry")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, argRegisters.arg1(), OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(argRegisters.arg1(), 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(argRegisters.arg2(), 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   je exit            */ .je("exit")
/* last_call:           */ .label("last_call")
/*   rdseed %rax        */ .rdseed(Register.RAX)
/*   jae last_call      */ .jae("last_call")
/* .align 16            */ .alignTo16BytesWithNOP()
/* proc1byte:           */ .label("proc1byte")
/*   mov %al, (<arg1>)  */ .movMR(Register.AL, argRegisters.arg1(), OptionalInt.of(0))
/*   add $1, <arg1>     */ .add(argRegisters.arg1(), 1, OptionalInt.empty())
/*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
/*   sub $1, <arg2>     */ .sub(argRegisters.arg2(), 1, OptionalInt.empty())
/*   jne proc1byte      */ .jne("proc1byte")
/* exit:                */ .label("exit");
  }

  private static void init() throws Throwable{
    // Set "initialized" true at first for prohibiting to initialize later
    // even if any exception is thrown.
    initialized = true;

    String osArch = System.getProperty("os.arch");
    if(!osArch.equals("amd64")){
      throw new UnsupportedPlatformException("Unsupported architecture: " + osArch);
    }

    String osName = System.getProperty("os.name");
    if(osName.equals("Linux")){
      argRegisters = new ArgRegisters(Register.RDI, Register.RSI, Register.RDX);
    }
    else if(osName.startsWith("Windows")){
      argRegisters = new ArgRegisters(Register.RCX, Register.RDX, Register.R8);
    }
    else{
      throw new UnsupportedPlatformException("Unsupported OS: " + osName);
    }

    codeSegment = new CodeSegment();
    var action = new CodeSegment.CleanerAction(codeSegment);
    Cleaner.create()
           .register(FFMHelper.class, action);

    createCPUID();

    try(var arena = Arena.ofConfined()){
      var mem = arena.allocate(ValueLayout.JAVA_INT, 4);

      /* RDRAND */
      cpuid.invoke(1, 0, mem);
      supportedRDRAND = ((mem.getAtIndex(ValueLayout.JAVA_INT, 2) >>> 30) & 1) == 1;
      if(supportedRDRAND){
        var rdrandDesc = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // 1st argument (mem)
            ValueLayout.JAVA_INT // 2nd argument (length)
        );
        var rdrandBuilder = new AsmBuilder.AMD64(codeSegment, rdrandDesc);
        createRDRANDBody(rdrandBuilder);
        fillWithRDRAND = rdrandBuilder.ret()
                                      .build(Linker.Option.critical(true));
      }

      /* RDSEED */
      cpuid.invoke(7, 0, mem);
      supportedRDSEED = ((mem.getAtIndex(ValueLayout.JAVA_INT, 1) >>> 18) & 1) == 1;
      if(supportedRDSEED){
        var rdseedDesc = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // 1st argument (mem)
            ValueLayout.JAVA_INT // 2nd argument (length)
        );
        var rdseedBuilder = new AsmBuilder.AMD64(codeSegment, rdseedDesc);
        createRDSEEDBody(rdseedBuilder);
        fillWithRDSEED = rdseedBuilder.ret()
                                      .build(Linker.Option.critical(true));
      }
    }
  }

  private final Provider provider;
  private final Map<String, String> attrs;

  public FFMHelper(Provider provider, Map<String, String> attrs){
    if(!initialized){
      try{
        init();
      }
      catch(Throwable t){
        throw new RuntimeException(t);
      }
    }

    this.provider = provider;
    this.attrs = attrs;
  }

  public static boolean ffmSupported(){
    return true;
  }

  public static boolean isRDRANDAvailable(){
    return supportedRDRAND;
  }

  public static boolean isRDSEEDAvailable(){
    return supportedRDSEED;
  }

  public Provider.Service getX86RdRand(){
    return new Provider.Service(provider, "SecureRandom", "FFMX86RdRand",
                                FFMRdRand.class.getName(), null, attrs);
  }

  public Provider.Service getX86RdSeed(){
    return new Provider.Service(provider, "SecureRandom", "FFMX86RdSeed",
                                FFMRdSeed.class.getName(), null, attrs);
  }

  public static MethodHandle getFillWithRDRAND(){
    return fillWithRDRAND;
  }

  public static MethodHandle getFillWithRDSEED(){
    return fillWithRDSEED;
  }

}
