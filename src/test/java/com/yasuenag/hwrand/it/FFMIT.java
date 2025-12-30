package com.yasuenag.hwrand.it;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
public class FFMIT extends TestBase{

  @Test
  @EnabledForJreRange(min = JRE.JAVA_22)
  public void testFFMRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);

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
  @EnabledForJreRange(min = JRE.JAVA_22)
  public void testFFMRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);

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

  @ParameterizedTest
  @ValueSource(strings = {"FFMX86RdRand", "FFMX86RdSeed"})
  @EnabledForJreRange(max = JRE.JAVA_21)
  public void testDisable(String algo){
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionInGetInstanceTest(algo));
  }

}
