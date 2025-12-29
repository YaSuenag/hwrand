package com.yasuenag.hwrand.x86;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;
import java.security.Provider;
import java.util.Map;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.UnsupportedPlatformException;

import com.yasuenag.hwrand.x86.internal.AsmUtil;


public class FFMRdRand extends SecureRandomSpi{

  private static final MethodHandle rdrand;

  static{
    try{
      var desc = FunctionDescriptor.ofVoid(
          ValueLayout.ADDRESS, // 1st argument (mem)
          ValueLayout.JAVA_INT // 2nd argument (length)
      );
      var builder = new AsmBuilder.AMD64(AsmUtil.getCodeSegment(), desc);
      AsmUtil.createRDRANDBody(builder, AsmUtil.ArgRegisters.get(false), 0);
      rdrand = builder.ret().build(Linker.Option.critical(true));
    }
    catch(UnsupportedPlatformException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    var result = new byte[numBytes];
    engineNextBytes(result);
    return result;
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    try{
      rdrand.invokeExact(MemorySegment.ofArray(bytes), bytes.length);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

  public Provider.Service getService(Provider provider, Map<String, String> attrs){
    return new Provider.Service(provider, "SecureRandom", "FFMX86RdRand",
                                this.getClass().getName(), null, attrs);
  }

}
