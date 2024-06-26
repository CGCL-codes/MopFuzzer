package edu.hust.xzf.mutator.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileDeltaRegex {
    private static final Map<String, String> logRegDict = new HashMap<String, String>();

    static {
        logRegDict.put("Loop Unrolling", "Unroll [0-9]+");
        logRegDict.put("Loop Peeling", "(Partial)?Peel\\s{2}");
        logRegDict.put("Loop Unswitching", "Loop unswitching orig: [0-9]+ @ [0-9]+  new:");
        logRegDict.put("Split If", "^SplitIf$");
        logRegDict.put("Conditional Expression Elimination", "[0-9]+\\. CEE in B[0-9]+ (B[0-9]+ B[0-9]+)");
        logRegDict.put("Inline", "inline(\\s\\(hot\\))?$");
        logRegDict.put("Uncommon Trap", "Uncommon trap");
        logRegDict.put("Escape Analysis", "JavaObject (UnknownEscape|NoEscape|GlobalEscape|ArgEscape)\\((UnknownEscape|NoEscape|GlobalEscape|ArgEscape)\\)");
        logRegDict.put("Eliminate Locks", "(\\+\\+\\+\\+ Eliminated: [0-9]+ (Lock|Unlock)|unique_lock)");
        logRegDict.put("Locks Coarsening", "=== (Coarsened [0-9]+ unlocks and [0-9]+ locks|unbalanced coarsened locks)");
        logRegDict.put("Conditional Constant Propagation", "CCP: ([0-9]+)");
        logRegDict.put("Eliminate Autobox", "\\+\\+\\+\\+ Eliminated: [0-9]+ (Allocate|AllocateArray)*");
        logRegDict.put("Block Elimination", "(replaced If and IfOp|merged B[0-9]+)");
        logRegDict.put("Block Merge", "\\*\\*\\*\\*\\*\\*\\*\\* try_merge for block B[0-9]+ successful");
        logRegDict.put("Canonicalization", "^canonicalized to:$");
        logRegDict.put("Null Check Elimination", "Done with null check elimination for method");
        logRegDict.put("Range Check Elimination", "Range check for instruction [0-9]+ eliminated");
        logRegDict.put("Optimize Ptr Compare", "\\+\\+\\+\\+ Replaced: [0-9]+");
        logRegDict.put("Parallel IV", "Parallel IV: [0-9]+");
    }

    private static final Object[] keySet = logRegDict.keySet().toArray();


    public static double getProfileDeltaDivMagnitude(int[] parent, int[] child) throws IOException {
        int ret = 0;

        double deltaSum = 0.0;
        double Magnitude = 0.0;
        for (int i = 0; i < parent.length; i++) {
            deltaSum += Math.pow(child[i] - parent[i], 2);
            Magnitude += Math.pow(child[i], 2);
        }
        return Math.sqrt(deltaSum) / Math.sqrt(Magnitude);
    }

    public static double getProfileDelta(int[] vp, int[] vc) throws IOException {
        if (vc.length != vp.length)
            throw new RuntimeException("crash");
        double deltaSum = 0.0;
        for (int i = 0; i < vp.length; i++) {
            deltaSum += Math.pow(vc[i] - vp[i], 2);
        }
        return Math.sqrt(deltaSum);
    }


    public static int getProfileDelta(String vmLog1Path, String vmLog2Path) throws IOException {
        int[] vmLog1Vector = getProfileVector(vmLog1Path);
        int[] vmLog2Vector = getProfileVector(vmLog2Path);
        int ret = 0;

        for (int i = 0; i < vmLog1Vector.length; i++) {
            ret += vmLog1Vector[i] != vmLog2Vector[i] ? 1 : 0;
        }
        return ret;
    }

    public static int[] getProfileVector(String vmLogPath) throws IOException {
        int[] profileVector = new int[logRegDict.size()];

        FileInputStream inputStream = new FileInputStream(vmLogPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            if (isUselessLine(str)) continue;
            for (int i = 0; i < logRegDict.size(); i++) {

                String key = (String) keySet[i];
                String regex = logRegDict.get(key);

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(str);

                if (matcher.find()) {
                    if (key.equals("Conditional Constant Propagation")) {
                        profileVector[i] += Integer.parseInt(matcher.group(1));
                    } else {
                        profileVector[i] += 1;
                    }
                    break;
                }
            }
        }

        inputStream.close();
        bufferedReader.close();

        return profileVector;
    }

    private static boolean isUselessLine(String line) {
        return line.startsWith("<") || line.contains("&lt;=") || line.startsWith("simplified phi function") || line.startsWith("creating phi-function");
    }

    public static void main(String[] args) throws IOException {

        int[] v0, v50, temp;
        Map<Integer, Double> map = new HashMap<>();

        String dir = "mutants/TestBooleanVect_11_28_17_47_45";

        String v0P = dir + "/0/vm.log";
        String v50P = dir + "/50/vm.log";
        v0 = getProfileVector(v0P);
        v50 = getProfileVector(v50P);
        for (File file : new File(dir).listFiles()) {
            if (file.isFile())
                continue;
            String vm = file.getAbsolutePath() + "/vm.log";
            System.out.println(vm);
            temp = getProfileVector(vm);

            if (!file.getName().equals("0")) {
                double div = getProfileDeltaDivMagnitude(v0, temp);
                map.put(Integer.parseInt(file.getName()), div);
            }
        }

        // 使用TreeMap对HashMap进行排序
        Map<Integer, Double> sortedMap = new TreeMap<>(map);

        // 打印排序后的map
        for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        for (int i = 0; i < logRegDict.size(); i++) {

            String key = (String) keySet[i];
            System.out.println(key + " " + v0[i] + " " + v50[i]);
        }
    }

}


//        for (int i = 0; i < keySet.length; i++) {
//            System.out.println(keySet[i]);
//        }
//
//        int[] v1 = getProfileVector("./resources/60/vm.log");
//        int[] v2 = getProfileVector("./resources/59/vm.log");
//        System.out.println(Arrays.toString(v1));
//        System.out.println(Arrays.toString(v2));
//        System.out.println(getProfileDelta("./resources/60/vm.log", "./resources/59/vm.log"));
