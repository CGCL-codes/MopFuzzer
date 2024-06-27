# MopFuzzer

MopFuzzer is a tool for automated testing of the JDK, utilizing various optimization evoking mutators and profile data-based guidance to rigorously test JVM JIT compilers by maximizing optimization interactions.
Our artifact includes the source code of MopFuzzer, the mutation seed files required during the execution of MopFuzzer, and a README file describing the installation process and the bugs discovered by the tool.

## The Designed Optimization Evoking Mutators

In this work, we designed 13 Optimization Evoking Mutators, which aim to trigger primarily the following optimization
behaviors respectively: LoopUnrolling, LockElimination, LockCoarsening, Inlining, DeReflection, LoopPeeling,
LoopUnswitching, Deoptimization, AutoboxElimination, RedundantStoreElimination, AlgebraicSimplification, EscapeAnalysis,
DeadCodeElimination.

The remaining mutators can be found at the **[Supplementary Material](MopFuzzer/SupplementaryMaterial.pdf)** of the paper.

## Usage of MopFuzzer

### Step 1: Prerequisites

MopFuzzer needs the debug build of JVM, so users should download the source code of JVM and set the debug flag. Here we
take the OpenJDK (mainline) as an example.

```
# git clone https://github.com/openjdk/jdk.git
# cd jdk
# bash configure --enable-debug
# make images JOBS=40
```

### Step 2: Run the tool

To use MopFuzzer, users should specify the target test case, the path of the debug build of JVM (at least one),
We will check whether the outputs of these JDKs are consistant (if perform differential testing). The following is
Parameters of running MopFuzzer.

```
usage: help [-jdk <arg>] [-project_path <arg>] [-target_case <arg>] [-line_number <arg>] [-is_use_jtreg <arg>] [-max_iter <arg>] [-?] [-enable_profile_guide <arg>]

 -?,--help                     Print this help message
 --project_path <arg>          source code root path. e.g., /home/user/benchmark/jtreg17. Necessary.
 --target_case <arg>           target java file. e.g., a.b.c denotes a/b/c.java. Necessary.
 --use_gc_options              use gc options? e.g., true. Default is false.
 --use_jit_options             use JIT options? e.g., true. Default is false.
 --enable_profile_guide <arg>  enable profile guidance? 
                               Recommend false for faster testing. Default is
                               false.
 --is_use_jtreg <arg>          use Jtreg? e.g., true. Default is false
 --jdk <arg>                   the jdk directory, path to bin/. At least one JDK. Necessary
                               We will check whether the outputs of these JDKs are consistent. 
 --line_number <arg>           line number of target file to be mutated. e.g., 10. Default is null.
 --max_iter <arg>              max iteration times.. e.g., 100. Default is 50
```

**Example Command:**

```bash
# differential testing of two target jdks
path/to/java17/bin/java -jar MopFuzzer.jar --project_path benchmarks/JavaFuzzer/tests/  --target_case Test0001 --jdk path/to/targetJDK1/bin/,path/to/targetJDK2/bin/ --enable_profile_guide true

# Testing a single jdk using jtreg seeds
path/to/java17/bin/java -jar MopFuzzer.jar --project_path benchmarks/jtreg17/ --target_case compiler.codegen.TestBooleanVect --jdk path/to/targetJDK/bin/ --enable_profile_guide true
```

## Show Cases of Detected Bugs

<details>[log.txt](..%2Ffuzz%2Fmutants%2FTest0001_02_02_23_02_36%2Flog.txt)
<summary><b>JDK-8322743</b></summary>

[JDK-8322743: assert(held_monitor_count() == jni_monitor_count())](https://bugs.openjdk.org/browse/JDK-8322743)

```java
synchronized void write(char[] data, int offset, int length) throws IOException {
    while (--length >= 0) {
        synchronized (new Test8267042()) {
            int var1 = 0;
            while (var1 < 100000) {
                if (var1 == 50000) {
                    getZeroOnStack(offset);
                }
                var1++;
            }
        }
        // original code
        // getZeroOnStack(offset);
        write(data[offset++]);
    }
}
```

</details>


The cause of this bug lies in improper operations during the Just-In-Time (JIT) compiler's escape analysis and On-Stack
Replacement (OSR) transformation.

This bug involves optimization processes including escape analysis and lock optimization. Specifically, JVM incorrectly
identifies certain objects as non-escaping during escape analysis, leading to incorrect handling of these objects' lock
states in the On-Stack Replacement (OSR) transformation, which triggers a crash.

<details>
<summary><b>JDK-8324174</b></summary>

[JDK-8324174: assert(m->is_entered(current)) failed: invariant](https://bugs.openjdk.org/browse/JDK-8324174)

```java
public class Test {

    public static final int CHUNK = 10000;
    public static ArrayList<Object> arr = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            synchronized (Test.class) {
                synchronized (new Test()) {
                    synchronized (Test.class) {
                        arr.add(new byte[CHUNK]);
                    }
                }
            }
        }
    }
}

```

</details>


The cause of this bug is improper handling of nested synchronized locks during the deoptimization process. The bug
involves optimization processes related to deoptimization and the maintenance of synchronized locks.

JVM attempts to eliminate nested locks and locks on non-escaping allocations in the C2 compiler optimization behavior,
but during deoptimization, it handles the unlocking and relocking sequence improperly (starting from the inner frame
instead of the outer frame), leading to a crash during the deoptimization process.




<details>
<summary><b>OpenJ9 Issue #18756</b></summary>

[Issue #18756 · eclipse-openj9/openj9 (github.com)](https://github.com/eclipse-openj9/openj9/issues/18756)

```java
public class Test {
    public static final int N = 400;

    public static long instanceCount = 3L;
    public static int[] iArrFld = new int[N];

    public void mainTest(String[] strArr1) {
        for (int i0 = 0; i0 < N; i0++) {
            Test.iArrFld[i0] = -1;
        }

        for (int i14 = 343; i14 > 21; --i14) {
            Test.instanceCount += (i14 ^ Test.instanceCount);
        }

        for (int i1 = 0; i1 < N; ++i1) {
            for (int var6 = 0; var6 < 10000; ++var6) {
                if (var6 == 4) {
                }
            }

            int[] tmp = new int[N];
            for (int i2 = 0; i2 < N; i2++) {
                tmp[i2] = 9;
            }
            Test.iArrFld = tmp;

            for (int i18 = 3; i18 < 63; i18++) {
                Test.iArrFld[i18 - 1] <<= (int) Test.instanceCount;
            }
        }

        long sum = 0;
        for (int i : Test.iArrFld) {
            sum += i;
        }
        System.out.println("sum: " + sum);
    }

    public static void main(String[] strArr) {
        Test _instance = new Test();
        _instance.mainTest(strArr);
    }
}
```

</details>



The cause of this bug is the improper handling of shift operation codes by the JVM during automatic SIMD (Single
Instruction, Multiple Data) optimization, leading to behavior inconsistent with Java semantics. This bug involves the
optimization process of automatic SIMD optimization. Specifically, the JVM improperly handled the VSHL (Vector Shift
Left) operation code during the execution of automatic SIMD optimization.


## Profile Flags

 JVM Flags and the Corresponding Regular Expression Rules for Profiling Optimization Behaviors

| **JVM Flags**               | **Optimization Behavior Types**              | **Regular Rules**                                           |
|-----------------------------|----------------------------------------------|-------------------------------------------------------------|
| TraceLoopOpts               | B1: Loop Unrolling                           | `Unroll [0-9]+`                                             |
|                             | B2: Loop Peeling                             | `(Partial)?Peel\s{2}`                                       |
|                             | B3: Parallel Induction Variables             | `Parallel IV: [0-9]+`                                       |
|                             | B4: Split if                                 | `^SplitIf$`                                                |
| TraceLoopUnswitching        | B5: Loop Unswitching                         | `Loop unswitching orig: [0-9]+ @ [0-9]+  new:`               |
| PrintCEE                    | B6: Conditional Expression Elimination       | `[0-9]+\. CEE in B[0-9]+ (B[0-9]+ B[0-9]+)`                |
| PrintInlining               | B7: Function Inlining                        | `inline(\s\(hot\))?$`                                       |
| TraceDeoptimization         | B8: Deoptimization                           | `Uncommon trap`                                             |
| PrintEscapeAnalysis         | B9: Escape Analysis                          | `(UnknownEscape\|NoEscape\|GlobalEscape\|ArgEscape)`           |
| PrintEliminateLocks         | B10: Eliminate Locks                         | `(Eliminated: [0-9]+ (Lock\|Unlock)\|unique_lock)`            |
|                             | B11: Locks Coarsening                        | `(Coarsened [0-9]+ unlocks\|unbalanced coarsened)`           |
| PrintOptoStatistics         | B12: Conditional Constant Propagation        | `CCP: [0-9]+`                                               |
| PrintEliminateAllocations   | B13: Eliminate Autobox                       | `Eliminated: [0-9]+ (Allocate\|AllocateArray)*`              |
| PrintBlockElimination       | B14: Block Elimination                       | `(replaced If and IfOp\|merged B[0-9]+)`                     |
| PrintPhiFunctions           | B15: simplify Phi Function                   | `try_merge for block B[0-9]+ successful`                    |
| PrintCanonicalization       | B16: Canonicalization                        | `^canonicalized to:$`                                      |
| PrintNullCheckElimination   | B17: Null Check Elimination                  | `Done with null check elimination for method`               |
| TraceRangeCheckElimination  | B18: Range Check Elimination                 | `Range check for instruction [0-9]+ eliminated`             |
| PrintOptimizePtrCompare     | B19: Optimize Ptr Compare                    | `\+\+\+\+ Replaced: [0-9]+`                               |


## Confirmed Bugs

Since unconfirmed bugs cannot be shown in Java Bug System(JBS), we only show the bugs that are confirmed by developers.

### OpenJDK Bugs

| Bug ID      | Affected Versions         | Link                                        |
| ----------- | ------------------------- | ------------------------------------------- |
| JDK-8312741 | 11                        | https://bugs.openjdk.org/browse/JDK-8312741 |
| JDK-8312438 | 11                        | https://bugs.openjdk.org/browse/JDK-8312438 |
| JDK-8322743 | 8, 11, 17, 20, 21, 22, 23 | https://bugs.openjdk.org/browse/JDK-8322743 |
| JDK-8324174 | 8, 11, 17, 21, 22, 23     | https://bugs.openjdk.org/browse/JDK-8324174 |
| JDK-8312744 | 22                        | https://bugs.openjdk.org/browse/JDK-8312744 |
| JDK-8312748 | 22                        | https://bugs.openjdk.org/browse/JDK-8312748 |
| JDK-8315916 | 17, 20, 21, 22            | https://bugs.openjdk.org/browse/JDK-8315916 |
| JDK-8324969 | 17, 21, 23                | https://bugs.openjdk.org/browse/JDK-8324969 |
| JDK-8325030 | 11, 17, 21, 23            | https://bugs.openjdk.org/browse/JDK-8325030 |
| JDK-8324739 | 15, 17, 21, 23            | https://bugs.openjdk.org/browse/JDK-8324739 |
| JDK-8329757 | 17, 21, 22, 23            | https://bugs.openjdk.org/browse/JDK-8329757 |
| JDK-8313992 | 17                        | https://bugs.openjdk.org/browse/JDK-8313992 |
| JDK-8329534 | 23                        | https://bugs.openjdk.org/browse/JDK-8329534 |
| JDK-8323507 | 22                        | https://bugs.openjdk.org/browse/JDK-8323507 |
| JDK-8327868 | 15, 17, 21, 22, 23        | https://bugs.openjdk.org/browse/JDK-8327868 |
| JDK-8324892 | 8                         | https://bugs.openjdk.org/browse/JDK-8324892 |
| JDK-8324853 | 8                         | https://bugs.openjdk.org/browse/JDK-8324853 |
| JDK-8325523 | 8                         | https://bugs.openjdk.org/browse/JDK-8325523 |
| JDK-8324339 | 8                         | https://bugs.openjdk.org/browse/JDK-8324339 |
| JDK-8316862 | 8                         | https://bugs.openjdk.org/browse/JDK-8316862 |
| JDK-8318291 | 8                         | https://bugs.openjdk.org/browse/JDK-8318291 |
| JDK-8318886 | 8, 11                     | https://bugs.openjdk.org/browse/JDK-8318886 |
| JDK-8316863 | 8                         | https://bugs.openjdk.org/browse/JDK-8316863 |
| JDK-8317504 | 8                         | https://bugs.openjdk.org/browse/JDK-8317504 |
| JDK-8317506 | 8                         | https://bugs.openjdk.org/browse/JDK-8317506 |
| JDK-8316864 | 8                         | https://bugs.openjdk.org/browse/JDK-8316864 |
| JDK-8316950 | 8                         | https://bugs.openjdk.org/browse/JDK-8316950 |
| JDK-8326992 | 15, 17, 19                | https://bugs.openjdk.org/browse/JDK-8326992 |
| JDK-8317299 | 17, 20, 22, 23            | https://bugs.openjdk.org/browse/JDK-8317299 |
| JDK-8329536 | 23                        | https://bugs.openjdk.org/browse/JDK-8329536 |
| JDK-8329535 | 17, 23                    | https://bugs.openjdk.org/browse/JDK-8329535 |
| JDK-8316865 | 8                         | https://bugs.openjdk.org/browse/JDK-8316865 |
| JDK-8316866 | 8, 11                     | https://bugs.openjdk.org/browse/JDK-8316866 |
| JDK-8323686 | 11                        | https://bugs.openjdk.org/browse/JDK-8323686 |
| JDK-8316952 | 8                         | https://bugs.openjdk.org/browse/JDK-8316952 |
| JDK-8317301 | 8                         | https://bugs.openjdk.org/browse/JDK-8317301 |
| JDK-8316939 | 8                         | https://bugs.openjdk.org/browse/JDK-8316939 |
| JDK-8329984 | 15, 17, 21, 23            | https://bugs.openjdk.org/browse/JDK-8329984 |
| JDK-8317576 | 8                         | https://bugs.openjdk.org/browse/JDK-8317576 |
| JDK-8317816 | 8, 11                     | https://bugs.openjdk.org/browse/JDK-8317816 |
| JDK-8316937 | 8                         | https://bugs.openjdk.org/browse/JDK-8316937 |
| JDK-8316949 | 8                         | https://bugs.openjdk.org/browse/JDK-8316949 |
| JDK-8317236 | 8                         | https://bugs.openjdk.org/browse/JDK-8317236 |
| JDK-8317346 | 8                         | https://bugs.openjdk.org/browse/JDK-8317346 |
| JDK-8324801 | 8                         | https://bugs.openjdk.org/browse/JDK-8324801 |

### OpenJ9 Bugs

| Issue ID     | Link                                                  |
| ------------ | ----------------------------------------------------- |
| Issue #18756 | https://github.com/eclipse-openj9/openj9/issues/18756 |
| Issue #18765 | https://github.com/eclipse-openj9/openj9/issues/18765 |
| Issue #18777 | https://github.com/eclipse-openj9/openj9/issues/18777 |
| Issue #18919 | https://github.com/eclipse-openj9/openj9/issues/18919 |
| Issue #18860 | https://github.com/eclipse-openj9/openj9/issues/18860 |
| Issue #18836 | https://github.com/eclipse-openj9/openj9/issues/18836 |
| Issue #18834 | https://github.com/eclipse-openj9/openj9/issues/18834 |
| Issue #18955 | https://github.com/eclipse-openj9/openj9/issues/18955 |
| Issue #19220 | https://github.com/eclipse-openj9/openj9/issues/19220 |
| Issue #19079 | https://github.com/eclipse-openj9/openj9/issues/19079 |
| Issue #19012 | https://github.com/eclipse-openj9/openj9/issues/19012 |
| Issue #19013 | https://github.com/eclipse-openj9/openj9/issues/19013 |
| Issue #19218 | https://github.com/eclipse-openj9/openj9/issues/19218 |
| Issue #19080 | https://github.com/eclipse-openj9/openj9/issues/19080 |
