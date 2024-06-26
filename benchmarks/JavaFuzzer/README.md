# Java* Fuzzer test generator

Java* Fuzzer test generator is a random Java tests generator. It is derived from Java* Fuzzer for Android* (https://github.com/android-art-intel/Fuzzer), adapted for desktop/server Java environment on Linux and extended to cover more Java syntax features (class inheritance, complex loop patterns, improved exception throwing patterns, etc). The tool compares the result of execution in JIT mode with interpreter mode or reference Java VM that allows to detect crashes, hangs and incorrect calculations. The main idea of the tool is to generate hundreds of thousands small random tests and to cover various cases using pre-defined test generator heuristics and provide a strong testing for Java VM compiler and runtime.

This is significantly reduced version of the original fuzzing infrastructure. The goal for this project is to generate the static bundle of verifiable tests that can be deployed anywhere without having Ruby installed, base Java executed to provide baseline results, etc. Example generated bundles are here:
  https://builds.shipilev.net/fuzzer/

## Authors
- Mohammad R. Haghighat (Intel Corporation)
- Dmitry Khukhro (Intel Corporation)
- Andrey Yakovlev (Intel Corporation)

### Authors of 2017-2018 modifications by Azul Systems
- Nina Rinskaya (Azul Systems)
- Ivan Popov (Azul Systems)

### Authors of 2019 modifications by Red Hat
- Aleksey Shipilev (Red Hat)
