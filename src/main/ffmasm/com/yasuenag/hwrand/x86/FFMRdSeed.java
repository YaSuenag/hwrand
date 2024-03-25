package com.yasuenag.hwrand.x86;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;

import com.yasuenag.hwrand.x86.internal.FFMHelper;


public class FFMRdSeed extends SecureRandomSpi{

  private MethodHandle fillWithRDSEED;

  public FFMRdSeed(){
    fillWithRDSEED = FFMHelper.getFillWithRDSEED();
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    try(var arena = Arena.ofConfined()){
      var mem = arena.allocate(numBytes);
      fillWithRDSEED.invoke(mem, numBytes);
      return mem.toArray(ValueLayout.JAVA_BYTE);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    try{
      fillWithRDSEED.invoke(MemorySegment.ofArray(bytes), bytes.length);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

}
