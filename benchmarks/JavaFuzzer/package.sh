#!/bin/bash
set -u

OUTDIR_JDK_X=tests-jdkX/
OUTDIR_JDK_11=tests-jdk11/
OUTDIR_JDK_8=tests-jdk8/

rm -f *.sfs
mksquashfs $OUTDIR_JDK_X  fuzzer-tests-jdkX.sfs  -comp xz
mksquashfs $OUTDIR_JDK_11 fuzzer-tests-jdk11.sfs -comp xz
mksquashfs $OUTDIR_JDK_8  fuzzer-tests-jdk8.sfs  -comp xz
