HWRand
===================
HWRandはIntelプロセッサのRDRAND、RDSEED命令を用いた乱数生成を行うためのSecureRandom実装です。
RDRANDはNIST SP 800-90A、RDSEEDはNIST SP 800-90BとCに対応しているようです。
[The Difference Between RDRAND and RDSEED](https://software.intel.com/en-us/blogs/2012/11/17/the-difference-between-rdrand-and-rdseed)

NIST SP 800-90A～Cは [JEP 273: DRBG-Based SecureRandom Implementations](http://openjdk.java.net/jeps/273) として、JDK 9で実装が追加される予定ですが、HWRandを利用することで、Linux x86/x86_64環境ではJDK 8以前でもこれらの乱数が利用可能となります。

# 対応環境

* JDK 20
* Linux x86_64
    * 動作チェックはUbuntu 22.04 (WSL2) x86_64で実施

# ビルド方法

`JAVA_HOME` 環境変数を設定の上、 `mvn package` します。

HWRand は [ffmasm](https://github.com/YaSuenag/ffmasm) に依存しており、Maven で [GitHub Packages](https://github.com/YaSuenag/ffmasm/packages/) からダウンロードします。そのためには settings.xml で PAT を設定する必要があります。 [こちら](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) を参考に、あらかじめ Maven の設定を行ってください。

# 利用方法

* [SecureRandom#getInstance(String)](http://docs.oracle.com/javase/jp/8/docs/api/java/security/SecureRandom.html#getInstance-java.lang.String-) にそれぞれの引数を与えてインスタンスを取得してください。
 * RDRAND（NIST SP 800-90A）
   * X86RdRand
 * RDSEED（NIST SP 800-90B/C）
   * X86RdSeed
* 具体的な利用方法については [test](test) もご覧ください。
* `UUID::randomUUID` の `SecureRandom` に適用する場合は [test/src/main/java/com/yasuenag/hwrand/test/random/Uuid.java](test/src/main/java/com/yasuenag/hwrand/test/random/Uuid.java) をご覧ください

# 実行前の準備

`com.yasuenag.hwrand.x86.HWRandX86Provider` を `java.security` の `security.provider.<番号>` に設定します。  
OracleJDK 8u66付属の `java.security` に対する変更例として `dist/java.security.patch` をソースに含めています。

# 実行

* 普通にJavaプログラムを実行します。
* モジュールパスに `hwrand-<バージョン>.jar` と、HWRand が依存する `ffmasm-<バージョン>.jar` を設定します。
* JDK 20 のプレビュー機能を使用しているため、 `--enable-preview` も付与します。
* ネイティブアクセスの警告メッセージを消したい場合は `--enable-native-access=com.yasuenag.ffmasm` も付与します。
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
