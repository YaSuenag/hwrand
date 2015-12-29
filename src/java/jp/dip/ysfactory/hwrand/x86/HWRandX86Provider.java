package jp.dip.ysfactory.hwrand.x86;

import java.security.*;


public class HWRandX86Provider extends Provider{

  private static boolean supportedRDRAND;

  private static boolean supportedRDSEED;

  public static boolean isSupportedRDRAND(){
    return supportedRDRAND;
  }

  public static boolean isSupportedRDSEED(){
    return supportedRDSEED;
  }

  private static native void checkCPUFeatures();

  static{
    System.loadLibrary("hwrandx86");
    checkCPUFeatures();
  }

  public HWRandX86Provider(){
    super("HWRandX86", 0.1d,
              "Wrapper for RDRAND and RDSEED instructions in x86 processors.");

    if(isSupportedRDRAND()){
      put("SecureRandom.X86RdRand", "jp.dip.ysfactory.hwrand.x86.RdRand");
    }

    if(isSupportedRDSEED()){
      put("SecureRandom.X86RdRand", "jp.dip.ysfactory.hwrand.x86.RdSeed");
    }

  }

}

