#!/bin/sh

DISTDIR=../../dist
export CLASSPATH=.:$DISTDIR/hwrand.jar

# Please comment out if you want to measure performance of default SecureRandom.
SECURITY_OPTS=-Djava.security.properties=java.security

$JAVA_HOME/bin/javac Test.java
$JAVA_HOME/bin/java -Djava.library.path=$DISTDIR $SECURITY_OPTS -Xms8g -Xmx8g -XX:StartFlightRecording=filename=uuid.jfr Test
