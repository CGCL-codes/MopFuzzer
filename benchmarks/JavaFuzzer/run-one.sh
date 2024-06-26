#!/bin/bash
set -u

# Max time to run the test for
TIMEOUT=180

# Tries to perform, to catch intermittently failing tests
TRIES=3

zcat golden.out.gz > golden.out

for TRY in `seq -w 1 ${TRIES}`; do
  timeout $TIMEOUT $* Test > test.out
  if [ $? -eq 0 ]; then
    cmp golden.out test.out
    if [ $? -eq 0 ]; then
      echo "$PWD ($TRY): Passed"
    else
      echo "$PWD ($TRY): Failed"
      exit 1
    fi
  else
    echo "$PWD ($TRY): Timeout"
    exit 1
  fi
done

rm *.out
