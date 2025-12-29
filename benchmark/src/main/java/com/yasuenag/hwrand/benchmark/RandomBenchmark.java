package com.yasuenag.hwrand.benchmark;

import java.security.*;
import java.util.concurrent.*;

import com.yasuenag.hwrand.x86.*;
import org.openjdk.jmh.annotations.*;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgsAppend = {
                     "-Xms8g",
                     "-Xmx8g",
                     "-XX:+AlwaysPreTouch",
                     "-XX:+UnlockExperimentalVMOptions",
                     "-XX:+EnableJVMCI",
                     "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
                     "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
                     "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
                     "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
                     "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED",
                     "--enable-native-access=ALL-UNNAMED"
                 })
@Warmup(iterations = 1, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class RandomBenchmark{

  private SecureRandom rand;

  // WARNING: NativePRNGBlocking might be hang if entropy is exhausted.
  @Param({/* "NativePRNGBlocking", */
          "NativePRNG", "DRBG",  // built-in
          "X86RdRand", "X86RdSeed",  // JNI
          "FFMX86RdRand", "FFMX86RdSeed",  // FFM
          "JVMCIX86RdRand", "JVMCIX86RdSeed"  // JVMCI
        })
  private String algo;

  static{
    Security.addProvider(new HWRandX86Provider());
  }

  @Setup
  public void setup(){
    try{
      rand = SecureRandom.getInstance(algo);
    }
    catch(NoSuchAlgorithmException e){
      throw new RuntimeException(e);
    }
  }

  @State(Scope.Thread)
  public static class RandomHolder{

    @Param("16") // 128bit
    private int bytes;

    public byte[] randomVal;

    @Setup
    public void setup(){
      randomVal = new byte[bytes];
    }
  }

  @Benchmark
  @Threads(1)
  public byte[] fillRandom(RandomHolder holder){
    rand.nextBytes(holder.randomVal);
    return holder.randomVal;
  }

  @Benchmark
  @Threads(100)
  public byte[] fillRandomInMT(RandomHolder holder){
    rand.nextBytes(holder.randomVal);
    return holder.randomVal;
  }

}
