import java.util.*;
import java.util.concurrent.*;

public class Test implements Callable<Set<UUID>>{

  private final static int NUM_OF_SETS    = 100_000;
  private final static int NUM_OF_THREADS = 100;

  @Override
  public Set<UUID> call(){
    Set<UUID> uuids = new HashSet<>();

    for(int i = 0; i < NUM_OF_SETS; i++){
      uuids.add(UUID.randomUUID());
    }

    return uuids;
  }

  public static void main(String[] args) throws Exception{
    List<Callable<Set<UUID>>> tasks = new ArrayList<>();
    for(int i = 0; i < NUM_OF_THREADS; i++){
      tasks.add(new Test());
    }

    var uuids = new HashSet<UUID>();
    ExecutorService tp = Executors.newFixedThreadPool(tasks.size());
    long start = System.currentTimeMillis();
    for(var future : tp.invokeAll(tasks)){
      uuids.addAll(future.get());
    }
    long end = System.currentTimeMillis();

    int expectedResults = NUM_OF_SETS * NUM_OF_THREADS;
    if(uuids.size() == expectedResults){
      System.out.println("Test OK!");
    }
    else{
      throw new RuntimeException("Expected: " + expectedResults + ", actual: " + uuids.size());
    }

    tp.shutdownNow();
    System.out.println("Elapsed Time (ms): " + Long.toString(end - start));
  }

}

