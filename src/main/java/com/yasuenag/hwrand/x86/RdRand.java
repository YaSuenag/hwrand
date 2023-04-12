package com.yasuenag.hwrand.x86;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;


public class RdRand extends SecureRandomSpi{

  private MethodHandle fillWithRDRAND;

  public RdRand(){
    fillWithRDRAND = HWRandX86Provider.fillWithRDRAND();
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    try{
      var mem = HWRandX86Provider.getAllocator().allocate(numBytes);
      fillWithRDRAND.invoke(mem, numBytes);
      return mem.toArray(ValueLayout.JAVA_BYTE);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    try{
      var mem = HWRandX86Provider.getAllocator().allocate(bytes.length);
      fillWithRDRAND.invoke(mem, bytes.length);
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

