import java.util.*;

public class Test implements Runnable{

  public void run(){
    for(int i = 0; i < 1_000_000; i++){
      UUID.randomUUID();
    }
  }

  public static void main(String[] args) throws Exception{
    for(int i = 0; i < 100; i++){
      (new Thread(new Test())).start();
    }
  }

}

