package com.yasuenag.hwrand.x86;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;


public class RdRand extends SecureRandomSpi{

  private MethodHandle fillWithRDRAND;

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    byte[] result = new byte[numBytes];
    engineNextBytes(result);
    return result;
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    if(fillWithRDRAND == null){
      fillWithRDRAND = HWRandX86Provider.fillWithRDRAND();
    }
    try{
      var allocator = SegmentAllocator.nativeAllocator(SegmentScope.auto());
      var mem = allocator.allocateArray(ValueLayout.JAVA_BYTE, bytes.length);
      fillWithRDRAND.invoke(mem, bytes.length);
      for(int i = 0; i < bytes.length; i++){
        bytes[i] = mem.get(ValueLayout.JAVA_BYTE, i);
      }
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

