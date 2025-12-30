package com.yasuenag.hwrand.it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yasuenag.hwrand.x86.HWRandX86Provider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;


public class TestBase{

  public static class ExceptionInGetInstanceTest implements Executable{
    private final String algo;

    public ExceptionInGetInstanceTest(String algo){
      this.algo = algo;
    }

    @Override
    public void execute() throws Throwable{
      SecureRandom.getInstance(algo);
    }
  }

  protected static boolean hasRDRAND = false;
  protected static boolean hasRDSEED = false;

  @BeforeAll
  public static void prepare() throws IOException{
    List<String> cpuFeatures = Collections.emptyList();
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
      String line;
      while((line = reader.readLine()) != null){
        if(line.startsWith("flags")){
          int flagsIdx = line.indexOf(":");
          String flags = line.substring(flagsIdx + 2);
          cpuFeatures = Arrays.asList(flags.split(" "));
        }
      }

      hasRDRAND = cpuFeatures.contains("rdrand");
      hasRDSEED = cpuFeatures.contains("rdseed");
    }
    catch(FileNotFoundException e){
      // Modern CPU supports both RDRAND and RDSEED, thus we assume
      // they are enabled on test platform.
      hasRDRAND = true;
      hasRDSEED = true;
    }
    finally{
      if(reader != null){
        reader.close();
      }
    }

    try{
      Security.addProvider(new HWRandX86Provider());
    }
    catch(RuntimeException e){
      // ignore: it might happen on Windows with JDK 8
    }

  }

}
