package com.yasuenag.hwrand.x86;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

import com.yasuenag.hwrand.x86.internal.AsmUtil;
import com.yasuenag.hwrand.x86.internal.JNIHelper;


public class HWRandX86Provider extends Provider{

  private void registerJNI(Map<String, String> attrs){
    JNIHelper jni = new JNIHelper(this, attrs);

    if(JNIHelper.isRDRANDAvailable()){
      putService(jni.getX86RdRand());
    }
    if(JNIHelper.isRDSEEDAvailable()){
      putService(jni.getX86RdSeed());
    }
  }

  private void registerFFM(Map<String, String> attrs){
    if(AsmUtil.isRDRANDAvailable()){
      FFMRdRand rdrand = new FFMRdRand();
      putService(rdrand.getService(this, attrs));
    }
    if(AsmUtil.isRDSEEDAvailable()){
      FFMRdSeed rdseed = new FFMRdSeed();
      putService(rdseed.getService(this, attrs));
    }
  }

  private void registerJVMCI(Map<String, String> attrs){
    if(AsmUtil.isRDRANDAvailable()){
      JVMCIRdRand rdrand = new JVMCIRdRand();
      putService(rdrand.getService(this, attrs));
    }
    if(AsmUtil.isRDSEEDAvailable()){
      JVMCIRdSeed rdseed = new JVMCIRdSeed();
      putService(rdseed.getService(this, attrs));
    }
  }

  public HWRandX86Provider(){
    // This c'tor has been deprecated since JDK 9.
    super("HWRandX86", 0.2d,
          "Wrapper for RDRAND and RDSEED instructions in x86 processors.");

    // This code should support JDK 6 or later.
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ThreadSafe", "true");
    attrs.put("ImplementedIn", "Hardware");

    //var jdkFeatureNumber = Runtime.getRuntime().feature();
    float jdkFeatureNumber = Float.parseFloat(System.getProperty("java.specification.version"));
    try{
      registerJNI(attrs);
    }
    catch(RuntimeException e){
      // We can ignore RuntimeException thrown by JNIHelper::init
      // if JDK supports FFM because we can fallback to FFM.
      // Otherwise rethrow exception.
      if(jdkFeatureNumber < 22){
        throw e;
      }
    }

    if(jdkFeatureNumber >= 22){
      registerFFM(attrs);
    }
    if(jdkFeatureNumber >= 25){
      registerJVMCI(attrs);
    }
  }

}

