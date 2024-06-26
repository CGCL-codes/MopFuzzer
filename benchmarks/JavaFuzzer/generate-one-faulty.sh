#!/bin/bash
set -u

# Run with the most aggressive compilation options to catch compiler errors early.
TEST_OPTS="-Xmx512m -XX:+UnlockDiagnosticVMOptions -XX:+StressLCM -XX:+StressGCM -XX:+StressCCP -XX:+StressIGVN -Xcomp -XX:CompileOnly=Test"

# Maximum test running time to be considered stable.
# Note it covers for generated tests that never finish, and also for tests that would
# run longer in some unusual JVM mode (for example, with lots of verification).
# Time is in seconds.
C2_TIMEOUT=5

# Target that C1 is about 10x slower: unusual exceptions, like Div/0 exceptions
# make the generated code slower than C2.
C1_TIMEOUT=5

# C2 Stress options are random, makes sense to try the same test a few times
C2_TRIES=10

R=$1

while true; do
  rm -f *.java Fuzzer* *.out
  ruby -I$R/rb $R/rb/Fuzzer.rb -f $R/rb/config.yml > Test.java
  cp ../FuzzerUtils.class .
  javac -J-Xmx512m -J-XX:ActiveProcessorCount=1 --release 8 Test.java

  # Trial balloon: does it timeout in C2?
  for T in `seq 1 $C2_TRIES`; do
    timeout $C2_TIMEOUT java $TEST_OPTS -XX:-TieredCompilation Test > /dev/null
    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
      # Passes... slide to another step
      echo -n "."
    elif [ $EXIT_CODE -eq 124 ]; then
      # Timeout... do another test
      echo -n "O"
      break;
    elif [ $EXIT_CODE -eq 3 ]; then
      # Out of memory... do another test
      echo -n "M"
      break;
    else
      # Some other error, finally. Exit!
      echo -n "!"
      exit;
    fi
  done

  # Trial balloon: does it timeout in C1?
  timeout $C1_TIMEOUT java $TEST_OPTS -XX:TieredStopAtLevel=1 Test > /dev/null
  EXIT_CODE=$?
  if [ $EXIT_CODE -eq 0 ]; then
    # Passes... do another test
    echo -n "."
  elif [ $EXIT_CODE -eq 124 ]; then
    # Timeout... do another test
    echo -n "o"
  elif [ $EXIT_CODE -eq 3 ]; then
    # Out of memory... do another test
    echo -n "m"
  else
    # Some other error, finally. Break out!
    echo -n "!"
    exit;
  fi
done
