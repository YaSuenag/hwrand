import java.security.*;


public class Test{

  private static void processRandom(String algorithm)
                                            throws NoSuchAlgorithmException{
    SecureRandom random = SecureRandom.getInstance(algorithm);

    byte[] randBytes = new byte[10];
    random.nextBytes(randBytes);

    for(byte b : randBytes){
      System.out.printf("%02x ", b);
    }

    System.out.println();
  }

  public static void main(String[] args) throws Exception{
    processRandom("X86RdRand");
    processRandom("X86RdSeed");
  }

}

