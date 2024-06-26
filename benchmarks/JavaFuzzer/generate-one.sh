#!/bin/bash
set -u
R=$1
JDK_PATH=$2
TIME_OUT=30
while true; do
  rm -f *.java Fuzzer* *.out
  ruby -I$R/rb $R/rb/Fuzzer.rb -f $R/rb/config.yml > Test.java
  cp ../FuzzerUtils.class .
  $JDK_PATH/bin/javac -J-Xmx512m -J-XX:ActiveProcessorCount=1 --release 8 Test.java

  timeout $TIME_OUT $JDK_PATH/bin/java Test > /dev/null
  EXIT_CODE=$?
  if [ $EXIT_CODE -eq 0 ]; then
        exit;
  fi
done

