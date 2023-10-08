package com.yasuenag.hwrand.x86;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;


public class RdSeed extends SecureRandomSpi{

  private MethodHandle fillWithRDSEED;

  public RdSeed(){
    fillWithRDSEED = HWRandX86Provider.fillWithRDSEED();
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    try{
      var mem = HWRandX86Provider.getArena().allocate(numBytes);
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
      var mem = HWRandX86Provider.getArena().allocate(bytes.length);
      fillWithRDSEED.invoke(mem, bytes.length);
      MemorySegment.copy(mem, ValueLayout.JAVA_BYTE, 0, bytes, 0, bytes.length);
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

