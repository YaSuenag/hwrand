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
import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
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

  private static void assembleCodes() throws UnsupportedPlatformException{
    String osArch = System.getProperty("os.arch");
    if(!osArch.equals("amd64")){
      throw new UnsupportedPlatformException("Unsupported architecture: " + osArch);
    }

    Register arg1, arg2, arg3;
    String osName = System.getProperty("os.name");
    if(osName.equals("Linux")){
      arg1 = Register.RDI;
      arg2 = Register.RSI;
      arg3 = Register.RDX;
    }
    else if(osName.startsWith("Windows")){
      arg1 = Register.RCX;
      arg2 = Register.RDX;
      arg3 = Register.R8;
    }
    else{
      throw new UnsupportedPlatformException("Unsupported OS: " + osName);
    }

    /* CPUID */
    var cpuidDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.JAVA_INT, // 1st argument (EAX)
                  ValueLayout.JAVA_INT, // 2nd argument (ECX)
                  ValueLayout.ADDRESS   // 3rd argument (result)
                );
    cpuid = AMD64AsmBuilder.create(AMD64AsmBuilder.class, codeSegment, cpuidDesc)
  /* push %rbp          */ .push(Register.RBP)
  /* mov %rsp, %rbp     */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
  /* push %rbx          */ .push(Register.RBX)
  /* mov <arg1>, %rax   */ .movMR(arg1, Register.RAX, OptionalInt.empty())
  /* mov <arg2>, %rcx   */ .movMR(arg2, Register.RCX, OptionalInt.empty())
  /* mov <arg3>, %r11   */ .movMR(arg3, Register.R11, OptionalInt.empty())
  /* cpuid              */ .cpuid()
  /* mov %eax, (%r11)   */ .movMR(Register.EAX, Register.R11, OptionalInt.of(0))
  /* mov %ebx, 4(%r11)  */ .movMR(Register.EBX, Register.R11, OptionalInt.of(4))
  /* mov %ecx, 8(%r11)  */ .movMR(Register.ECX, Register.R11, OptionalInt.of(8))
  /* mov %edx, 12(%r11) */ .movMR(Register.EDX, Register.R11, OptionalInt.of(12))
  /* pop %rbx           */ .pop(Register.RBX, OptionalInt.empty())
  /* leave              */ .leave()
  /* ret                */ .ret()
                           .build(Linker.Option.critical(true));

    /* RDRAND */
    var rdrandDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.ADDRESS, // 1st argument (mem)
                  ValueLayout.JAVA_INT // 2nd argument (length)
                );
    fillWithRDRAND = AMD64AsmBuilder.create(AMD64AsmBuilder.class, codeSegment, rdrandDesc)
         /* bulk:                */ .label("bulk")
         /*   cmp $8, <arg2>     */ .cmp(arg2, 8, OptionalInt.empty())
         /*   jl last_call       */ .jl("last_call")
         /* retry:               */ .label("retry")
         /*   rdrand %rax        */ .rdrand(Register.RAX)
         /*   jae retry          */ .jae("retry")
         /*   mov %rax, (<arg1>) */ .movMR(Register.RAX, arg1, OptionalInt.of(0))
         /*   add $8, <arg1>     */ .add(arg1, 8, OptionalInt.empty())
         /*   sub $8, <arg2>     */ .sub(arg2, 8, OptionalInt.empty())
         /*   jne bulk           */ .jne("bulk")
         /*   je exit            */ .je("exit")
         /* last_call:           */ .label("last_call")
         /*   rdrand %rax        */ .rdrand(Register.RAX)
         /*   jae last_call      */ .jae("last_call")
         /* .align 16            */ .alignTo16BytesWithNOP()
         /* proc1byte:           */ .label("proc1byte")
         /*   mov %al, (<arg1>)  */ .movMR(Register.AL, arg1, OptionalInt.of(0))
         /*   add $1, <arg1>     */ .add(arg1, 1, OptionalInt.empty())
         /*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
         /*   sub $1, <arg2>     */ .sub(arg2, 1, OptionalInt.empty())
         /*   jne proc1byte      */ .jne("proc1byte")
         /* exit:                */ .label("exit")
         /*   ret                */ .ret()
                                    .build(Linker.Option.critical(true));

    /* RDSEED */
    var rdseedDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.ADDRESS, // 1st argument (mem)
                  ValueLayout.JAVA_INT // 2nd argument (length)
                );
    fillWithRDSEED = AMD64AsmBuilder.create(AMD64AsmBuilder.class, codeSegment, rdseedDesc)
         /* bulk:                */ .label("bulk")
         /*   cmp $8, <arg2>     */ .cmp(arg2, 8, OptionalInt.empty())
         /*   jl last_call       */ .jl("last_call")
         /* retry:               */ .label("retry")
         /*   rdseed %rax        */ .rdseed(Register.RAX)
         /*   jae retry          */ .jae("retry")
         /*   mov %rax, (<arg1>) */ .movMR(Register.RAX, arg1, OptionalInt.of(0))
         /*   add $8, <arg1>     */ .add(arg1, 8, OptionalInt.empty())
         /*   sub $8, <arg2>     */ .sub(arg2, 8, OptionalInt.empty())
         /*   jne bulk           */ .jne("bulk")
         /*   je exit            */ .je("exit")
         /* last_call:           */ .label("last_call")
         /*   rdseed %rax        */ .rdseed(Register.RAX)
         /*   jae last_call      */ .jae("last_call")
         /* .align 16            */ .alignTo16BytesWithNOP()
         /* proc1byte:           */ .label("proc1byte")
         /*   mov %al, (<arg1>)  */ .movMR(Register.AL, arg1, OptionalInt.of(0))
         /*   add $1, <arg1>     */ .add(arg1, 1, OptionalInt.empty())
         /*   shl $1, %rax       */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
         /*   sub $1, <arg2>     */ .sub(arg2, 1, OptionalInt.empty())
         /*   jne proc1byte      */ .jne("proc1byte")
         /* exit:                */ .label("exit")
         /*   ret                */ .ret()
                                    .build(Linker.Option.critical(true));
  }

  private static void init() throws Throwable{
    // Set "initialized" true at first for prohibiting to initialize later
    // even if any exception is thrown.
    initialized = true;

    codeSegment = new CodeSegment();
    var action = new CodeSegment.CleanerAction(codeSegment);
    Cleaner.create()
           .register(FFMHelper.class, action);

    assembleCodes();

    try(var arena = Arena.ofConfined()){
      var mem = arena.allocate(ValueLayout.JAVA_INT, 4);

      /* RDRAND check */
      cpuid.invoke(1, 0, mem);
      supportedRDRAND = ((mem.getAtIndex(ValueLayout.JAVA_INT, 2) >>> 30) & 1) == 1;

      /* RDSEED check */
      cpuid.invoke(7, 0, mem);
      supportedRDSEED = ((mem.getAtIndex(ValueLayout.JAVA_INT, 1) >>> 18) & 1) == 1;
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
