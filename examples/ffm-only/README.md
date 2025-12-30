JVMCI 無効化
===

`-Dhwrand.disableJVMCI=true` によって JVMCI サポートを無効化することで JDK 25 以降でも JVMCI 関連オプションを設定することなく HWRand を使えるようにします。

Windows での実行も考慮し、ここで使用できるアルゴリズムは `FFMX86RdRand` と `FFMX86RdSeed` の 2 つです。

# 必要なもの

* JDK 25 以降
* Maven
* HWRand
    * **maven installされている必要あり**
    * [pom.xml](pom.xml)で依存設定しています

# 動かし方

```
mvn package
mvn exec:exec
```
