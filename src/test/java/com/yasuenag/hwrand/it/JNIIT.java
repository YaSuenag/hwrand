package com.yasuenag.hwrand.it;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import com.yasuenag.hwrand.x86.HWRandX86Provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;


public class JNIIT extends TestBase{

  public static class ExceptionAtAddProvider implements Executable{
    @Override
    public void execute() throws Throwable{
      Security.addProvider(new HWRandX86Provider());
    }
  }

  @Test
  @EnabledOnOs(value = {OS.LINUX}, architectures = {"amd64"})
  public void testRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);

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
  @EnabledOnOs(value = {OS.LINUX}, architectures = {"amd64"})
  public void testRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);

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
  public void testOnWindows(){
    Assertions.assertThrows(RuntimeException.class, new ExceptionAtAddProvider());
  }

}
