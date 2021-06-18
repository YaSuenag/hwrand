#!/bin/sh

DISTDIR=../..//dist
export CLASSPATH=.:$DISTDIR/hwrand.jar

$JAVA_HOME/bin/javac Test.java
$JAVA_HOME/bin/java -Djava.library.path=$DISTDIR Test
