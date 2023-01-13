# HWRandベンチマーク

NativePRNGNonBlocking、DRBG、X86RdRand、X86RdSeedのパフォーマンスをJMHで測定します。シングルスレッド性能と100スレッド同時実行の2パターンを測定します。

## 必要なもの

* JDK 16
* Maven
* HWRand
    * **maven installされている必要あり**
    * [pom.xml](pom.xml)で依存設定しています

## 動かし方

```
$ java -jar target/hwrand-benchmark-1.0.0.jar
```

## 注意

* Linuxのみで動きます
* -Xmxで8GB設定しています
* ベンチマーク内部でEpsilon GCを設定しています
