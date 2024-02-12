package com.yasuenag.hwrand.x86.internal;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.Provider;
import java.util.Map;

import com.yasuenag.hwrand.x86.RdRand;
import com.yasuenag.hwrand.x86.RdSeed;


public class JNIHelper{

  // These fields will be written from native code
  private static boolean supportedRDRAND;
  private static boolean supportedRDSEED;

  public static boolean isRDRANDAvailable(){
    return supportedRDRAND;
  }

  public static boolean isRDSEEDAvailable(){
    return supportedRDSEED;
  }

  private static native void checkCPUFeatures();

  private static boolean initialized = false;

  private static void closeSilently(Closeable res){
    try{
      if(res != null){
        res.close();
      }
    }
    catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  private static void init(){
    String os = System.getProperty("os.name");
    String arch = System.getProperty("os.arch");
    if(!os.equals("Linux") || !arch.equals("amd64")){
      throw new RuntimeException("HWRand (JNI) supports Linux x86_64 only");
    }

    InputStream resource = null;
    FileOutputStream lib = null;
    try{
      File f = File.createTempFile("libhwrandx86-", ".so");
      f.deleteOnExit();

      resource = JNIHelper.class.getResourceAsStream("/native/libhwrandx86.so");
      lib = new FileOutputStream(f);

      byte[] bytes = new byte[1024 * 10]; // 10KB
      int nRead;
      while((nRead = resource.read(bytes)) != -1){
        lib.write(bytes, 0, nRead);
      }

      System.load(f.getAbsolutePath());
    }
    catch(IOException e){
      throw new RuntimeException(e);
    }
    finally{
      closeSilently(resource);
      closeSilently(lib);
    }

    checkCPUFeatures();
    initialized = true;
  }

  private final Provider provider;

  private final Map<String, String> attrs;

  public JNIHelper(Provider provider, Map<String, String> attrs){
    if(!initialized){
      init();
    }
    this.provider = provider;
    this.attrs = attrs;
  }

  public Provider.Service getX86RdRand(){
    return new Provider.Service(provider, "SecureRandom", "X86RdRand",
                                RdRand.class.getName(), null, attrs);
  }

  public Provider.Service getX86RdSeed(){
    return new Provider.Service(provider, "SecureRandom", "X86RdSeed",
                                RdSeed.class.getName(), null, attrs);
  }

}
