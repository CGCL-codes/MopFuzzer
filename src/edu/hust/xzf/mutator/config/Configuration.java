package edu.hust.xzf.mutator.config;

public class Configuration {

    //    public static final String TEMP_FILES_PATH = ".temp/";

    public static final int LinuxTimeout = 1000;
    public static final long SHELL_RUN_TIMEOUT = 300L;
    public static final long TEST_SHELL_RUN_TIMEOUT = LinuxTimeout + 15;
    public static final String TEMP_FILES_PATH = "mutants/";
    public final String osName = System.getProperty("os.name");
    public String projectPath;
    public String javaFilePath;

    //    public static boolean disableRandomOptions;
    public static final int MaxHitMutatorTimes = 5;
    public String targetCase;
    public String JDKRoot;

    public static boolean useRandomGCOptions;
    public static boolean useRandomJITOptions;

    public static int lineNumber;

    public String classPathSplitter;

//    public static int JDK_level;

    public static boolean isWin;

    //    private final String add_export = "--add-exports java.base/sun.nio.cs=ALL-UNNAMED "
//            + "--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED "
//            + "--add-exports java.base/jdk.internal.access=ALL-UNNAMED "
//            + "--add-exports java.base/jdk.internal.reflect=ALL-UNNAMED "
//            + "--add-exports java.base/jdk.internal.util=ALL-UNNAMED "
//            + "--add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED ";

    public static boolean isRegression;
    public String jtregTestsRoot;
    public String jtregTestsRoot_bk;

    public String jtreg;
    public static String javaFuzzerRoot;
    public static boolean enableProfileGuidance;
    public static int maxIter;
    public static boolean usejTreg;

    public JDK[] jdks;

    public Configuration() {

    }

    public void settingAll() {
        // D:/repository/jdk11u/test/hotspot/jtreg
        isRegression = this.targetCase.contains("compiler.") || this.targetCase.contains("History.");

        this.javaFilePath = projectPath + "/" + targetCase.replace(".", "/") + ".java";
        isWin = osName.toLowerCase().contains("windows");
        // jdk root = D:/repository/jdk11u/build/windows-x86_64-normal-server-fastdebug/images/jdk/bin/
        if (isWin) {
            this.classPathSplitter = ";";
        } else {
            this.classPathSplitter = ":";
        }
        javaFuzzerRoot = "benchmarks/JavaFuzzer/";

        String[] jdk = JDKRoot.split(",");
        jdks = new JDK[jdk.length];
        for (int i = 0; i < jdk.length; i++) {
            jdks[i] = new JDK(jdk[i], this, i);
        }

//        if (JDK_level == 11) {
//            additionalOptions += " -XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -Xcomp -ea " +
//                    "-XX:+AssertRangeCheckElimination -XX:+StressLoopInvariantCodeMotion -XX:+StressRangeCheckElimination -XX:+AlwaysIncrementalInline " +
//                    "-XX:+StressLinearScan -XX:+BailoutToInterpreterForThrows -XX:+PostLoopMultiversioning -XX:+StressArrayCopyMacroNode " +
//                    "-XX:+AlwaysIncrementalInline -XX:+AlwaysSafeConstructors -XX:+TieredCompilation " +
//                    "-XX:+AggressiveUnboxing -XX:+BailoutToInterpreterForThrows -XX:+SuperWordLoopUnrollAnalysis";
//        } else if (JDK_level == 17) {
//            additionalOptions += " -XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -Xcomp -ea " +
//                    "-XX:+AssertRangeCheckElimination -XX:+StressLoopInvariantCodeMotion -XX:+StressRangeCheckElimination -XX:+AlwaysIncrementalInline " +
//                    "-XX:+StressLinearScan -XX:+BailoutToInterpreterForThrows -XX:+PostLoopMultiversioning -XX:+StressArrayCopyMacroNode " +
//                    "-XX:+EnableVectorAggressiveReboxing -XX:+EnableVectorReboxing -XX:+AlwaysIncrementalInline " +
//                    "-XX:+EnableVectorSupport -XX:+ExpandSubTypeCheckAtParseTime -XX:+IncrementalInlineForceCleanup " +
//                    "-XX:+StressMethodHandleLinkerInlining -XX:+TieredCompilation -XX:+SuperWordLoopUnrollAnalysis " +
//                    "-XX:+AggressiveUnboxing -XX:+BailoutToInterpreterForThrows ";
//        } else if (JDK_level == 22) {
//            additionalOptions += " -XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -Xcomp -ea " +
//                    "-XX:+AssertRangeCheckElimination -XX:+StressLoopInvariantCodeMotion -XX:+StressRangeCheckElimination -XX:+AlwaysIncrementalInline " +
//                    "-XX:+StressLinearScan -XX:+BailoutToInterpreterForThrows -XX:+PostLoopMultiversioning -XX:+StressArrayCopyMacroNode " +
//                    "-XX:+EnableVectorAggressiveReboxing -XX:+EnableVectorReboxing -XX:+AlwaysIncrementalInline " +
//                    "-XX:+EnableVectorSupport -XX:+ExpandSubTypeCheckAtParseTime -XX:+IncrementalInlineForceCleanup " +
//                    "-XX:+StressMethodHandleLinkerInlining -XX:+TieredCompilation -XX:+SuperWordLoopUnrollAnalysis " +
//                    "-XX:+AggressiveUnboxing -XX:+BailoutToInterpreterForThrows ";
//        }
    }
}
