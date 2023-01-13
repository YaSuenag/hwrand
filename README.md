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

# ビルド方法

```bash
$ mvn package
```

# 利用方法

* [SecureRandom#getInstance(String)](http://docs.oracle.com/javase/jp/8/docs/api/java/security/SecureRandom.html#getInstance-java.lang.String-) にそれぞれの引数を与えてインスタンスを取得してください。
 * RDRAND（NIST SP 800-90A）
   * X86RdRand
 * RDSEED（NIST SP 800-90B/C）
   * X86RdSeed
* 具体的な利用方法については、ソース中の [test/random/Test.java](test/random/Test.java) もご覧ください。
* `UUID::randomUUID` の `SecureRandom` に適用する場合は [test/uuid](test/uuid) をご覧ください

# 実行前の準備

`com.yasuenag.hwrand.x86.HWRandX86Provider` を `java.security` の `security.provider.<番号>` に設定します。  
OracleJDK 8u66付属の `java.security` に対する変更例として `dist/java.security.patch` をソースに含めています。

# 実行

* 普通にJavaプログラムを実行します。
* クラスパスに `hwrand.jar` を追加します。
* `libhwrandx86.so` の含まれるディレクトリが `LD_LIBRARY_PATH` に通っていない場合、 `-Djava.library.path` でディレクトリを指定します。
* もし `$JAVA_HOME/jre/lib/security/java.security` 以外にHWRandの設定を組み込んだ場合、そのセキュリティ設定ファイルを `-Djava.security.properties` で指定します。

# 注意

* HWRandは、実行されるプロセッサがRDRAND、RDSEEDをサポートするか判断し、未サポートのCPUの上では当該機能が利用できません。
 * RDRAND（NIST SP 800-90A）
   * Ivy Bridge以降
 * RDSEED（NIST SP 800-90B/C）
   * Broadwell以降
* 実装は [SecureRandomSpi](http://docs.oracle.com/javase/jp/8/docs/api/java/security/SecureRandomSpi.html) に対して行っていますが、engineGenerateSeed()とengineNextBytes()は同じ挙動（同じソースからの乱数取得）となります。また、engineSetSeed()は何も行いません（空実装です）。

# License

GNU General Public License v2
