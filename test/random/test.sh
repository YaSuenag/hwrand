#!/bin/sh

FILE=$0
JAR=$1

if [ "x$JAR" = 'x' ]; then
  echo "Usage: $0 /path/to/hwrand.jar"
  exit 1
fi

$JAVA_HOME/bin/javac -cp $JAR Test.java
$JAVA_HOME/bin/java -cp .:$JAR Test
