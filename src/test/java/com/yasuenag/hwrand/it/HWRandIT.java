package com.yasuenag.hwrand.it;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yasuenag.hwrand.x86.HWRandX86Provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;


public class HWRandIT{

  public static class ExceptionTest implements Executable{
    private final String algo;

    public ExceptionTest(String algo){
      this.algo = algo;
    }

    @Override
    public void execute() throws Throwable{
      SecureRandom.getInstance(algo);
    }
  }

  private static boolean hasRDRAND = false;
  private static boolean hasRDSEED = false;

  @BeforeAll
  @EnabledOnOs(OS.LINUX)
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
    }
    finally{
      if(reader != null){
        reader.close();
      }
    }

    hasRDRAND = cpuFeatures.contains("rdrand");
    hasRDSEED = cpuFeatures.contains("rdseed");
  }

  @BeforeAll
  @EnabledOnOs(OS.WINDOWS)
  @EnabledForJreRange(min = JRE.JAVA_22)
  public static void prepareForWindows() throws IOException{
    // Modern CPU supports both RDRAND and RDSEED, thus we assume
    // they are enabled on test platform.
    hasRDRAND = true;
    hasRDSEED = true;
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX}, architectures = {"amd64"})
  public void testRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("X86RdRand");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void testJNIRDRANDOnWindows(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("X86RdRand"));
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX}, architectures = {"amd64"})
  public void testRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("X86RdSeed");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void testJNIRDSEEDOnWindows(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("X86RdSeed"));
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
  @EnabledForJreRange(min = JRE.JAVA_22)
  public void testFFMRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("FFMX86RdRand");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledForJreRange(max = JRE.JAVA_21)
  public void testDisableFFMRDRAND(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("FFMX86RdRand"));
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
  @EnabledForJreRange(min = JRE.JAVA_22)
  public void testFFMRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("FFMX86RdSeed");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledForJreRange(max = JRE.JAVA_21)
  public void testDisableFFMRDSEED(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("FFMX86RdSeed"));
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
  @EnabledForJreRange(min = JRE.JAVA_25)
  public void testJVMCIRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("JVMCIX86RdRand");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledForJreRange(max = JRE.JAVA_24)
  public void testDisableJVMCIRDRAND(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("JVMCIX86RdRand"));
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
  @EnabledForJreRange(min = JRE.JAVA_25)
  public void testJVMCIRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);
    Security.addProvider(new HWRandX86Provider());

    SecureRandom random = null;
    try{
      random = SecureRandom.getInstance("JVMCIX86RdSeed");
    }
    catch(NoSuchAlgorithmException e){
      Assertions.fail(e);
    }
    byte[] rand1 = new byte[16];
    byte[] rand2 = new byte[16];
    random.nextBytes(rand1);
    random.nextBytes(rand2);

    Assertions.assertFalse(Arrays.equals(rand1, rand2));
  }

  @Test
  @EnabledForJreRange(max = JRE.JAVA_24)
  public void testDisableJVMCIRDSEED(){
    Security.addProvider(new HWRandX86Provider());
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionTest("JVMCIX86RdSeed"));
  }

}
