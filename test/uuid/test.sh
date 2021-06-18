#!/bin/sh

DISTDIR=../../dist
export CLASSPATH=.:$DISTDIR/hwrand.jar
SECURITY_OPTS=-Djava.security.properties=java.security

$JAVA_HOME/bin/javac Test.java
$JAVA_HOME/bin/java -Djava.library.path=$DISTDIR $SECURITY_OPTS -XX:StartFlightRecording=filename=uuid.jfr Test
