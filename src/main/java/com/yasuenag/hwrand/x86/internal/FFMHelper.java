package com.yasuenag.hwrand.x86.internal;

import java.security.Provider;;
import java.util.Map;


public class FFMHelper{

  public static boolean ffmSupported(){
    return false;
  }

  public FFMHelper(Provider provider, Map<String, String> attrs){
    // Do nothing
  }

  public static boolean isRDRANDAvailable(){
    throw new UnsupportedOperationException("FFM is not supported on this Java version");
  }

  public static boolean isRDSEEDAvailable(){
    throw new UnsupportedOperationException("FFM is not supported on this Java version");
  }

  public Provider.Service getX86RdRand(){
    throw new UnsupportedOperationException("FFM is not supported on this Java version");
  }

  public Provider.Service getX86RdSeed(){
    throw new UnsupportedOperationException("FFM is not supported on this Java version");
  }

}
