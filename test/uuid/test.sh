#!/bin/sh

FILE=$0
JAR=$1

if [ "x$JAR" = 'x' ]; then
  echo "Usage: $0 /path/to/hwrand.jar"
  exit 1
fi

# Please comment out if you want to measure performance of default SecureRandom.
SECURITY_OPTS=-Djava.security.properties=java.security

$JAVA_HOME/bin/javac -cp $JAR Test.java
$JAVA_HOME/bin/java -cp .:$JAR -Djava.library.path=$DISTDIR $SECURITY_OPTS -Xms8g -Xmx8g -XX:StartFlightRecording=filename=uuid.jfr Test
