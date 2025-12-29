HWRandベンチマーク
===

NativePRNG、DRBG、X86RdRand、X86RdSeed、FFMX86RdRand、FFMX86RdSeed、JVMCIX86RdRand、JVMCIX86RdSeed のパフォーマンスをJMHで測定します。シングルスレッド性能と100スレッド同時実行の2パターンを測定します。

# 必要なもの

* JDK 25
* Maven
* HWRand
    * **maven installされている必要あり**
    * [pom.xml](pom.xml)で依存設定しています

# 動かし方

```
mvn package
mvn exec:exec
```

# 注意

* -Xmxで8GB設定しています
* すべての起動オプションは [RandomBenchmark.java](src/main/java/com/yasuenag/hwrand/benchmark/RandomBenchmark.java) の `@Fork` 内の `jvmArgsAppend` をご確認ください
