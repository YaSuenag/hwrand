package com.yasuenag.hwrand.x86;

import java.io.*;
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

  static{
    String os = System.getProperty("os.name");
    int bits = Integer.getInteger("sun.arch.data.model");
    if(!os.equals("Linux") || (bits != 64)){
      throw new RuntimeException("HWRand supports Linux x86_64 only");
    }

    InputStream resource = null;
    FileOutputStream lib = null;
    try{
      File f = File.createTempFile("libhwrandx86-", ".so");
      f.deleteOnExit();

      resource = HWRandX86Provider.class.getResourceAsStream("/native/libhwrandx86.so");
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
  }

  public HWRandX86Provider(){
    // This c'tor has been deprecated since JDK 9.
    super("HWRandX86", 0.2d,
          "Wrapper for RDRAND and RDSEED instructions in x86 processors.");

    // This code should support JDK 6 or later.
    Map<String, String> attrs = new HashMap<String, String>();
    attrs.put("ThreadSafe", "true");
    attrs.put("ImplementedIn", "Hardware");

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

