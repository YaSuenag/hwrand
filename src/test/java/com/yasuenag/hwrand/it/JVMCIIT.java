package com.yasuenag.hwrand.it;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@EnabledOnOs(value = {OS.LINUX, OS.WINDOWS}, architectures = {"amd64"})
public class JVMCIIT extends TestBase{

  @Test
  @EnabledForJreRange(min = JRE.JAVA_25)
  @DisabledIfSystemProperty(named = "hwrand.disableJVMCI", matches = "true")
  public void testJVMCIRDRAND(){
    Assumptions.assumeTrue(hasRDRAND);

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
  @EnabledForJreRange(min = JRE.JAVA_25)
  @DisabledIfSystemProperty(named = "hwrand.disableJVMCI", matches = "true")
  public void testJVMCIRDSEED(){
    Assumptions.assumeTrue(hasRDSEED);

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

  @ParameterizedTest
  @ValueSource(strings = {"JVMCIX86RdRand", "JVMCIX86RdSeed"})
  @EnabledForJreRange(max = JRE.JAVA_24)
  public void testDisable(String algo){
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionInGetInstanceTest(algo));
  }

  @ParameterizedTest
  @ValueSource(strings = {"JVMCIX86RdRand", "JVMCIX86RdSeed"})
  @EnabledIfSystemProperty(named = "hwrand.disableJVMCI", matches = "true")
  public void testDisableByProp(String algo){
    Assertions.assertThrows(NoSuchAlgorithmException.class, new ExceptionInGetInstanceTest(algo));
  }

}
