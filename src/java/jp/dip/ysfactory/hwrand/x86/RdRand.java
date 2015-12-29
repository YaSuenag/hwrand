package jp.dip.ysfactory.hwrand.x86;

import java.security.*;


public class RdRand extends SecureRandomSpi{

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    byte[] result = new byte[numBytes];
    engineNextBytes(result);
    return result;
  }

  @Override
  protected native void engineNextBytes(byte[] bytes);

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

}

