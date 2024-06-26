#!/bin/bash
set -u

# Max time to run the test for
TIMEOUT=30

# Parallelism
PARALLEL=8

echo "Running tests with $*"

R=`pwd`

find -iname Test.class -printf "%h\n" | sort | xargs -n 1 -P $PARALLEL -I MYPATH sh -c "cd MYPATH; $R/run-one.sh $*"
