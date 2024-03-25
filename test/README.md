HWRand テスト
===

X86RdRand、X86RdSeed、FFMX86RdRand、FFMX86RdSeed 経由で取得する `SecureRandom` の値を出力します。

# 必要なもの

* JDK 22
* Maven
* HWRand
    * **maven installされている必要あり**
    * [pom.xml](pom.xml)で依存設定しています

# 動かし方

```
mvn package
mvn exec:java
```
