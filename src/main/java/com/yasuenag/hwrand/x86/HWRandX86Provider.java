package com.yasuenag.hwrand.x86;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.security.Provider;
import java.util.Map;
import java.util.HashMap;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;


public class HWRandX86Provider extends Provider{

  private static final CodeSegment codeSegment;

  // These fields will be written from native code
  private static final boolean supportedRDRAND;
  private static final boolean supportedRDSEED;

  private static MethodHandle cpuid;
  private static MethodHandle fillWithRDRAND;
  private static MethodHandle fillWithRDSEED;

  private static void assembleCodes() throws UnsupportedPlatformException{
    /* CPUID */
    var cpuidDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.JAVA_INT, // 1st argument (EAX)
                  ValueLayout.JAVA_INT, // 2nd argument (ECX)
                  ValueLayout.ADDRESS   // 3rd argument (result)
                );
    cpuid = AMD64AsmBuilder.create(codeSegment, cpuidDesc)
   /* push %rbp         */ .push(Register.RBP)
   /* mov %rsp, %rbp    */ .movRM(Register.RSP, Register.RBP, OptionalInt.empty())
   /* push %rbx         */ .push(Register.RBX)
   /* mov %rdi, %rax    */ .movRM(Register.RDI, Register.RAX, OptionalInt.empty())
   /* mov %rsi, %rcx    */ .movRM(Register.RSI, Register.RCX, OptionalInt.empty())
   /* mov %rdx, %r8     */ .movRM(Register.RDX, Register.R8, OptionalInt.empty())
   /* cpuid             */ .cpuid()
   /* mov %eax, (%r8)   */ .movRM(Register.EAX, Register.R8, OptionalInt.of(0))
   /* mov %ebx, 4(%r8)  */ .movRM(Register.EBX, Register.R8, OptionalInt.of(4))
   /* mov %ecx, 8(%r8)  */ .movRM(Register.ECX, Register.R8, OptionalInt.of(8))
   /* mov %ecx, 12(%r8) */ .movRM(Register.EDX, Register.R8, OptionalInt.of(12))
   /* pop %rbx          */ .pop(Register.RBX, OptionalInt.empty())
   /* leave             */ .leave()
   /* ret               */ .ret()
                           .build();

    /* RDRAND */
    var rdrandDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.ADDRESS, // 1st argument (mem)
                  ValueLayout.JAVA_INT // 2nd argument (length)
                );
    fillWithRDRAND = AMD64AsmBuilder.create(codeSegment, rdrandDesc)
           /*   push %rbp        */ .push(Register.RBP)
           /*   mov %rsp, %rbp   */ .movRM(Register.RSP, Register.RBP, OptionalInt.empty())
           /* .align 16          */ .alignTo16BytesWithNOP()
           /* bulk:              */ .label("bulk")
           /*   cmp $8, %rsi     */ .cmp(Register.RSI, 8, OptionalInt.empty())
           /*   jl last_call     */ .jl("last_call")
           /* retry:             */ .label("retry")
           /*   rdrand %rax      */ .rdrand(Register.RAX)
           /*   jae retry        */ .jae("retry")
           /*   mov %rax, (%rdi) */ .movRM(Register.RAX, Register.RDI, OptionalInt.of(0))
           /*   add $8, %rdi     */ .add(Register.RDI, 8, OptionalInt.empty())
           /*   sub $8, %rsi     */ .sub(Register.RSI, 8, OptionalInt.empty())
           /*   jne bulk         */ .jne("bulk")
           /*   je exit          */ .je("exit")
           /* last_call:         */ .label("last_call")
           /*   rdrand %rax      */ .rdrand(Register.RAX)
           /*   jae last_call    */ .jae("last_call")
           /* .align 16          */ .alignTo16BytesWithNOP()
           /* proc1byte:         */ .label("proc1byte")
           /*   mov %al, (%rdi)  */ .movRM(Register.AL, Register.RDI, OptionalInt.of(0))
           /*   add $1, %rdi     */ .add(Register.RDI, 1, OptionalInt.empty())
           /*   shl $1, %rax     */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
           /*   sub $1, %rsi     */ .sub(Register.RSI, 1, OptionalInt.empty())
           /*   jne proc1byte    */ .jne("proc1byte")
           /* exit:              */ .label("exit")
           /*   leave            */ .leave()
           /*   ret              */ .ret()
                                    .build();

    /* RDSEED */
    var rdseedDesc = FunctionDescriptor.ofVoid(
                  ValueLayout.ADDRESS, // 1st argument (mem)
                  ValueLayout.JAVA_INT // 2nd argument (length)
                );
    fillWithRDSEED = AMD64AsmBuilder.create(codeSegment, rdseedDesc)
           /*   push %rbp        */ .push(Register.RBP)
           /*   mov %rsp, %rbp   */ .movRM(Register.RSP, Register.RBP, OptionalInt.empty())
           /* .align 16          */ .alignTo16BytesWithNOP()
           /* bulk:              */ .label("bulk")
           /*   cmp $8, %rsi     */ .cmp(Register.RSI, 8, OptionalInt.empty())
           /*   jl last_call     */ .jl("last_call")
           /* retry:             */ .label("retry")
           /*   rdseed %rax      */ .rdseed(Register.RAX)
           /*   jae retry        */ .jae("retry")
           /*   mov %rax, (%rdi) */ .movRM(Register.RAX, Register.RDI, OptionalInt.of(0))
           /*   add $8, %rdi     */ .add(Register.RDI, 8, OptionalInt.empty())
           /*   sub $8, %rsi     */ .sub(Register.RSI, 8, OptionalInt.empty())
           /*   jne bulk         */ .jne("bulk")
           /*   je exit          */ .je("exit")
           /* last_call:         */ .label("last_call")
           /*   rdseed %rax      */ .rdseed(Register.RAX)
           /*   jae last_call    */ .jae("last_call")
           /* .align 16          */ .alignTo16BytesWithNOP()
           /* proc1byte:         */ .label("proc1byte")
           /*   mov %ah, (%rdi)  */ .movRM(Register.AH, Register.RDI, OptionalInt.of(0))
           /*   add $1, %rdi     */ .add(Register.RDI, 1, OptionalInt.empty())
           /*   shl $1, %rax     */ .shl(Register.RAX, (byte)1, OptionalInt.empty())
           /*   sub $1, %rsi     */ .sub(Register.RSI, 1, OptionalInt.empty())
           /*   jne proc1byte    */ .jne("proc1byte")
           /* exit:              */ .label("exit")
           /*   leave            */ .leave()
           /*   ret              */ .ret()
                                    .build();
  }

  static {
    try{
      codeSegment = new CodeSegment();
      Cleaner.create()
             .register(codeSegment, () -> {
               try(codeSegment){
               }
               catch(Exception e){
                 throw new RuntimeException(e);
               }
             });
      assembleCodes();

      var allocator = SegmentAllocator.implicitAllocator();
      var mem = allocator.allocateArray(ValueLayout.JAVA_INT, 4);

      /* RDRAND check */
      cpuid.invoke(1, 0, mem);
      supportedRDRAND = ((mem.getAtIndex(ValueLayout.JAVA_INT, 2) >>> 30) & 1) == 1;

      /* RDSEED check */
      cpuid.invoke(7, 0, mem);
      supportedRDSEED = ((mem.getAtIndex(ValueLayout.JAVA_INT, 1) >>> 18) & 1) == 1;
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static boolean isSupportedRDRAND(){
    return supportedRDRAND;
  }

  public static boolean isSupportedRDSEED(){
    return supportedRDSEED;
  }

  static MethodHandle cpuid(){
    return cpuid;
  }

  static MethodHandle fillWithRDRAND(){
    return fillWithRDRAND;
  }

  static MethodHandle fillWithRDSEED(){
    return fillWithRDSEED;
  }

  public HWRandX86Provider(){
    // This c'tor has been deprecated since JDK 9.
    super("HWRandX86", 0.2d,
          "Wrapper for RDRAND and RDSEED instructions in x86 processors.");

    // This code should support JDK 6 or later.
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ThreadSafe", "true");
    attrs.put("ImplementedIn", "Hardware");

    if(isSupportedRDRAND()){
      putService(new Provider.Service(this, "SecureRandom", "X86RdRand",
                                      RdRand.class.getName(), null, attrs));
    }

    if(isSupportedRDSEED()){
      putService(new Provider.Service(this, "SecureRandom", "X86RdSeed",
                                      RdSeed.class.getName(), null, attrs));
    }

  }

}

