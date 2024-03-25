package com.yasuenag.hwrand.x86;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

import com.yasuenag.hwrand.x86.internal.FFMHelper;
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
    FFMHelper ffm = new FFMHelper(this, attrs);

    if(FFMHelper.isRDRANDAvailable()){
      putService(ffm.getX86RdRand());
    }
    if(FFMHelper.isRDSEEDAvailable()){
      putService(ffm.getX86RdSeed());
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

    try{
      registerJNI(attrs);
    }
    catch(RuntimeException e){
      // We can ignore RuntimeException thrown by JNIHelper::init
      //  if JDK supports FFM because we can fallback to FFM.
      // Otherwise rethrow exception.
      if(!FFMHelper.ffmSupported()){
        throw e;
      }
    }

    if(FFMHelper.ffmSupported()){
      registerFFM(attrs);
    }
  }

}

