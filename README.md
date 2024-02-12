HWRand
![CodeQL](../../workflows/CodeQL/badge.svg)
===================
HWRandはIntelプロセッサのRDRAND、RDSEED命令を用いた乱数生成を行うためのSecureRandom実装です。
RDRANDはNIST SP 800-90A、RDSEEDはNIST SP 800-90BとCに対応しているようです。
[The Difference Between RDRAND and RDSEED](https://software.intel.com/en-us/blogs/2012/11/17/the-difference-between-rdrand-and-rdseed)

NIST SP 800-90A～Cは [JEP 273: DRBG-Based SecureRandom Implementations](http://openjdk.java.net/jeps/273) として、JDK 9で実装が追加される予定ですが、HWRandを利用することで、Linux x86/x86_64環境ではJDK 8以前でもこれらの乱数が利用可能となります。

# 対応環境

* JDK 6以降
* Linux x86_64
   * 動作チェックはUbuntu 20.04 (WSL2) x86_64で実施
   * ビルドにはJDK、GCC、GNU Make、GNU Assembler、Maven必須

JDK 22 以降の場合は Foreign Function & Memory API を用いた、RDRAND / RDSEED 各命令のダイレクト呼び出しをサポートします。これは Linux だけでなく Windows でも動作します。

# ビルド方法

```
JAVA_HOME=/path/to/jdk8 mvn compile test
JAVA_HOME=/path/to/jdk22 mvn package
```

# 利用方法

* [SecureRandom#getInstance(String)](http://docs.oracle.com/javase/jp/8/docs/api/java/security/SecureRandom.html#getInstance-java.lang.String-) にそれぞれの引数を与えてインスタンスを取得してください。
    * RDRAND（NIST SP 800-90A）
        * X86RdRand
        * FFMX86RdRand
    * RDSEED（NIST SP 800-90B/C）
        * X86RdSeed
        * FFMX86RdSeed
* 具体的な利用方法については [テストコード](test/src/main/java/com/yasuenag/hwrand/test/random/Test.java) もご覧ください。

# 注意

* HWRandは、実行されるプロセッサがRDRAND、RDSEEDをサポートするか判断し、未サポートのCPUの上では当該機能が利用できません。
    * RDRAND（NIST SP 800-90A）
        * Ivy Bridge以降
    * RDSEED（NIST SP 800-90B/C）
        * Broadwell以降
* 実装は [SecureRandomSpi](http://docs.oracle.com/javase/jp/8/docs/api/java/security/SecureRandomSpi.html) に対して行っていますが、engineGenerateSeed()とengineNextBytes()は同じ挙動（同じソースからの乱数取得）となります。また、engineSetSeed()は何も行いません（空実装です）。
* クラッシュ等、アプリケーションが異常終了した場合は一時ディレクトリに生成されるネイティブライブラリ（ `libhwrandx86-*.so` ）が残存する場合があります（デフォルトでは `/tmp` 配下）。

# License

GNU General Public License v2
