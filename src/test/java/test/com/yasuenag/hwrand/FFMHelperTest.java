package test.com.yasuenag.hwrand;

import com.yasuenag.hwrand.x86.internal.FFMHelper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;


public class FFMHelperTest{

  @Test
  @EnabledForJreRange(max = JRE.JAVA_21)
  public void testFFMUnsupported(){
    Assertions.assertFalse(FFMHelper.ffmSupported());
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_22)
  public void testFFMSupported(){
    Assertions.assertTrue(FFMHelper.ffmSupported());
  }

}
