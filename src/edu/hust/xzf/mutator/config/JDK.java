package edu.hust.xzf.mutator.config;

import edu.hust.xzf.mutator.GCoptions.GetGCOptions;
import edu.hust.xzf.mutator.utils.ShellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static edu.hust.xzf.mutator.Scheduler.runCmd;
import static edu.hust.xzf.mutator.config.Configuration.isWin;

public class JDK {

    public final String profileFlags = " -XX:+TraceLoopOpts -XX:+PrintCEE -XX:+PrintInlining -XX:+TraceDeoptimization -XX:+PrintEscapeAnalysis"
            +
            " -XX:+PrintEliminateLocks -XX:+PrintOptoStatistics -XX:+PrintEliminateAllocations -XX:+PrintBlockElimination -XX:+PrintPhiFunctions"
            +
            " -XX:+PrintCanonicalization -XX:+PrintNullCheckElimination -XX:+TraceRangeCheckElimination -XX:+PrintOptimizePtrCompare ";
    static String javac;

    boolean isFirstJDK = false;
    String java;
    private static final Logger log = LoggerFactory.getLogger(JDK.class);
    String xcomp;
    String classPathSplitter;
    public boolean isOpenJDK = false;
    public Configuration config;
    public boolean isBiSheng = false;
    public boolean isJ9 = false;
    public boolean isGraal = false;
    String art;
    // String test_jdk;
    String testLibRoot;
    public int version;

    public JDK(String jdkRoot, Configuration config, int jdkIndex) {
        this.config = config;
        isFirstJDK = jdkIndex == 0;
        if (isWin) {
            this.java = jdkRoot + "/java.exe ";
            if (jdkIndex == 0)
                JDK.javac = jdkRoot + "/javac.exe ";
            classPathSplitter = ";";
        } else {
            this.java = jdkRoot + "/java ";
            if (jdkIndex == 0)
                JDK.javac = jdkRoot + "/javac ";
            classPathSplitter = ":";
        }

        testLibRoot = "JtregTestLib";

        // test_jdk = " -D\"test.jdk=" + jdkRoot.substring(0, jdkRoot.indexOf("bin")) +
        // "\" ";

        String lowerCase = jdkRoot.toLowerCase();

        isGraal = lowerCase.contains("graalvm");

        isOpenJDK = !lowerCase.contains("openj9") && !isGraal;

        isJ9 = lowerCase.contains("openj9");

        isBiSheng = lowerCase.contains("bisheng");

        String jdkRepo = jdkRoot;
        if (jdkRoot.contains("build"))
            jdkRepo = jdkRoot.substring(0, jdkRoot.indexOf("build"));

        if (jdkRepo.contains("1.8")) {
            version = 8;
        } else if (jdkRepo.contains("11")) {
            version = 11;
        } else if (jdkRepo.contains("17")) {
            version = 17;
        } else if (jdkRepo.contains("21")) {
            version = 21;
        } else if (jdkRepo.contains("23")) {
            version = 23;
        } else if (jdkRepo.contains("8")) {
            version = 8;
        } else {
            throw new RuntimeException("Unsupported JDK version: " + jdkRepo);
        }
    }

    public static String getJavac() {
        if (isWin) {
            return javac + "-source 8 -target 8 -Xlint:none ";
        } else {
            return javac + " -source 8 -target 8 -Xlint:none ";
        }
    }

    public String getJITJava(String compileOnly, String tmpDir, String iter0Path, String trueTestCase) {
        String cp = iter0Path == null ? (Configuration.isRegression ? getTestLibRoot() : Configuration.javaFuzzerRoot)
                : iter0Path;
        String addtionOption = (config.useRandomJITOptions ? getRandomOption() : "")
                + (config.useRandomGCOptions ? getRandomGCOptions() : "");

        if (isOpenJDK || isBiSheng) {
            String option = String.format(" -XX:+LogVMOutput -XX:LogFile=%s/vm.log -XX:-DisplayVMOutput "
                    + "%s", tmpDir, profileFlags);
            String logvm = config.enableProfileGuidance ? (compileOnly + option) : "";
            return java + " -Xcomp " + logvm + addtionOption + " -cp \"" + tmpDir + classPathSplitter + cp + "\" "
                    + trueTestCase;
        } else if (isJ9) {
            // todo should use -Xjit:count=0 for openj9?
            // return java + " -Xmx1G -cp \"" + tmpDir + classPathSplitter + cp + "\" ";
            return java + " -Xshareclasses:none -Xmx1G -Xjit:count=0 -cp \"" + tmpDir + classPathSplitter + cp + "\" "
                    + trueTestCase;
        } else
            return java + " -cp \"" + tmpDir + classPathSplitter + cp + "\" " + trueTestCase;
    }

    public String getDefaultJava(String compileOnly, String tmpDir, String iter0Path, String trueTestCase) {
        String cp = iter0Path == null ? (Configuration.isRegression ? getTestLibRoot() : Configuration.javaFuzzerRoot)
                : iter0Path;
        String addtionOption = (config.useRandomJITOptions ? getRandomOption() : "")
                + (config.useRandomGCOptions ? getRandomGCOptions() : "");

        if (isOpenJDK || isBiSheng) {
            String option = String.format(" -XX:+LogVMOutput -XX:LogFile=%s/vm.log -XX:-DisplayVMOutput "
                    + "%s", tmpDir, profileFlags);
            String logvm = config.enableProfileGuidance ? (compileOnly + option) : "";
            return java + " " + logvm + addtionOption + " -cp \"" + tmpDir + classPathSplitter + cp + "\" "
                    + trueTestCase;
        } else if (isJ9) {
            return java + " -Xshareclasses:none -Xmx1G -cp \"" + tmpDir + classPathSplitter + cp + "\" " + trueTestCase;
        } else
            return java + " -cp \"" + tmpDir + classPathSplitter + cp + "\" " + trueTestCase;
    }

    public String getRandomOption() {
        if (isOpenJDK) {
            String prefix = "-XX:+IgnoreUnrecognizedVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions ";
            switch (new Random().nextInt(2)) {
                case 0:
                    return prefix + genRandomSubOpions();
                case 1:
                    return prefix + genRandomSubOpions();
            }
        } else if (isJ9) {
            switch (new Random().nextInt(2)) {
                case 0:
                    return " -Xjit:optLevel=veryhot ";
                case 1:
                    return "";
            }
        }
        return "";
    }

    private String genRandomSubOpions() {
        String ops = "";
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            int index = r.nextInt(Option.values().length);
            ops += makeOption(String.valueOf(Option.values()[index]), r.nextInt(2));
        }
        return ops;
    }

    private String makeOption(String option, int i) {
        if (option.isEmpty()) {
            return "";
        }
        int[] result = Option.valueOf(option).getRange();
        if (result[2] != 1) {
            if (i == 1)
                return " -XX:" + option + "=" + result[2] + " ";
            else
                return " -XX:" + option + "=" + result[1] + " ";
        } else {
            if (i == 1)
                return " -XX:+" + option + " ";
            else
                return " -XX:-" + option + " ";
        }
    }

    public String getRandomGCOptions() {
        if (!Configuration.useRandomGCOptions)
            return "";
        if (isOpenJDK && version == 23) {
            return GetGCOptions.GetRandomGCOptions("openjdk", version);
        } else if (isBiSheng && version == 8)
            return GetGCOptions.GetRandomGCOptions("bishengjdk", version);
        else if (isJ9) {
            return GetGCOptions.GetRandomGCOptions("openj9", version);
        } else
            return "";
    }

    public String getTestLibRoot() {
        return testLibRoot;
    }
}
