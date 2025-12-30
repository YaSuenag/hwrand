package com.yasuenag.hwrand.example.ffmonly;

import java.security.*;

import com.yasuenag.hwrand.x86.*;


public class Main{

  private static void processRandom(String algorithm)
                                            throws NoSuchAlgorithmException{
    SecureRandom random = SecureRandom.getInstance(algorithm);

    byte[] randBytes = new byte[10];
    random.nextBytes(randBytes);

    System.out.print(algorithm + ": ");
    for(byte b : randBytes){
      System.out.printf("%02x ", b);
    }

    System.out.println();
  }

  public static void main(String[] args) throws Exception{
    Security.addProvider(new HWRandX86Provider());
    processRandom("FFMX86RdRand");
    processRandom("FFMX86RdSeed");
  }

}

