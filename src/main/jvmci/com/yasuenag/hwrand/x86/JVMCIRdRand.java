package com.yasuenag.hwrand.x86;

import java.security.SecureRandomSpi;
import java.security.Provider;
import java.util.Map;

import sun.misc.Unsafe;

import com.yasuenag.ffmasmtools.jvmci.amd64.JVMCIAMD64AsmBuilder;

import com.yasuenag.hwrand.x86.internal.AsmUtil;


public class JVMCIRdRand extends SecureRandomSpi{

  static{
    try{
      var builder = new JVMCIAMD64AsmBuilder();
      builder.emitPrologue();
      AsmUtil.createRDRANDBody(builder, AsmUtil.ArgRegisters.get(true), Unsafe.ARRAY_INT_BASE_OFFSET);
      builder.emitEpilogue();

      var targetMethod = JVMCIRdRand.class.getDeclaredMethod("engineNextBytes", byte[].class);
      builder.install(targetMethod, 16);
    }
    catch(Exception e){
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
    throw new IllegalStateException("This method should be installed by JVMCI");
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

  public Provider.Service getService(Provider provider, Map<String, String> attrs){
    return new Provider.Service(provider, "SecureRandom", "JVMCIX86RdRand",
                                this.getClass().getName(), null, attrs);
  }

}
