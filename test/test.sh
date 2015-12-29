#!/bin/sh

DISTDIR=../dist
$JAVA_HOME/bin/javac Test.java
$JAVA_HOME/bin/java -cp .:$DISTDIR/hwrand.jar \
                    -Djava.library.path=$DISTDIR \
                    -Djava.security.properties==$DISTDIR/java.security Test
