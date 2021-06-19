package com.yasuenag.hwrand.x86;

import java.security.*;
import java.util.*;


public class HWRandX86Provider extends Provider{

  // These fields will be written from native code
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
    // This c'tor has been deprecated since JDK 9.
    super("HWRandX86", 0.2d,
          "Wrapper for RDRAND and RDSEED instructions in x86 processors.");

    // This code should support JDK 6 or later.
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ThreadSafe", "true");

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

