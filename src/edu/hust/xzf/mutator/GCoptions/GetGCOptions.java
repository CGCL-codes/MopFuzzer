package edu.hust.xzf.mutator.GCoptions;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class GetGCOptions {

    static Map<String, GCOptions> gcOptionsMap;

    static String[] gc_set = {"g1gc", "shenandoahgc", "zgc"};

    public static String GetGCOptionFromFile(String file_name, int gc_index, String jdk_name_and_version) {
        GCOptions gcOptions = null;

        // check to ensure only read json file once
        if (!gcOptionsMap.containsKey(jdk_name_and_version)) {
            // if gcOptions is null, read options from json file
            try {
                JsonReader reader = new JsonReader(new FileReader(file_name));
                Gson gson = new Gson();
                gcOptions = gson.fromJson(reader, GCOptions.class);
                gcOptionsMap.put(jdk_name_and_version + gc_set[gc_index], gcOptions);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // generate gc sequence
            assert gcOptions != null;
            for (GCOption option : gcOptions.options) {
                option.output_set = new ArrayList<>();

                // deal with different types of options
                if (option.type.equals("bool")) {
                    option.output_set.add("-XX:+" + option.name);
                    option.output_set.add("-XX:-" + option.name);
                } else if (option.type.equals("ccstr")) {
                    for (String value : option.possible_value) {
                        option.output_set.add("-XX:" + option.name + "=" + value);
                    }
                } else {
                    if (option.type.equals("double")) {
                        double max_value = Double.parseDouble(option.max_value);
                        double min_value = Double.parseDouble(option.min_value);
                        double rand_value = Math.random() * (max_value - min_value) + min_value;
                        option.output_set.add("-XX:" + option.name + "=" + rand_value);
                    } else {
                        long max_value = Long.parseLong(option.max_value);
                        long min_value = Long.parseLong(option.min_value);
                        long rand_value = (long) (Math.random() * (max_value - min_value) + min_value);
                        option.output_set.add("-XX:" + option.name + "=" + rand_value);
                    }
                    // adding max_value and min_value may conflict with some automatically set ergonomic parameters
                    option.output_set.add("-XX:" + option.name + "=" + option.max_value);
                    option.output_set.add("-XX:" + option.name + "=" + option.min_value);
                    if (!option.max_value.equals(option.default_value)
                            && !option.min_value.equals(option.default_value)) {
                        option.output_set.add("-XX:" + option.name + "=" + option.default_value);
                    }
                }
            }
        } else {
            gcOptions = gcOptionsMap.get(jdk_name_and_version + gc_set[gc_index]);
        }

        StringBuilder options_seq = new StringBuilder();
        // 将数组转换为列表
        int size = gcOptions.size < gcOptions.options.length ? gcOptions.size : gcOptions.options.length;
        List<GCOption> gcOptionsList = new ArrayList<>(Arrays.asList(gcOptions.options));
        // 随机打乱列表
        Collections.shuffle(gcOptionsList);

        // 选择前n个元素作为子集
        List<GCOption> subset = gcOptionsList.subList(0, size);

        for (GCOption option : subset) {
            // skip ConcGCThreads option for ZGC and Shenandoah
            if (gc_index == 2 && option.name.equals("ConcGCThreads")) continue;
            if (gc_index == 1 && option.name.equals("ConcGCThreads")) continue;

            // resolve precedes and less_than situation
            if (option.precedes != null) {
                if (option.less_than != null) {
                    // get value of less_than from current_value
                    for (GCOption may_precedes : gcOptions.options) {
                        if (may_precedes.name.equals(option.less_than)) {
                            // System.out.println("less_than: " + may_precedes.current_value);
                            // get the value after the equal sign of current_value and convert it to int
                            long less_than_value = Long.parseLong(may_precedes.current_value.split("=")[1]);
                            long min_value = Long.parseLong(option.min_value);
                            long rand_value = (long) (Math.random() * (less_than_value - min_value) + min_value);

                            option.output_set.clear();

                            if (rand_value > Long.parseLong(option.max_value)
                                    || rand_value < Long.parseLong(option.min_value)
                                    || min_value == less_than_value) {
                                option.output_set.add(" ");
                            } else {
                                option.output_set.add("-XX:" + option.name + "=" + rand_value);
                            }
                            break;
                        }
                    }
                } else if (option.greater_than != null) {
                    // get value of greater_than from current_value
                    for (GCOption may_precedes : gcOptions.options) {
                        if (may_precedes.name.equals(option.greater_than)) {
//                            System.out.println("greater_than: " + may_precedes.current_value);
                            // get the value after the equal sign of current_value and convert it to int
                            long greater_than_value = Long.parseLong(
                                    may_precedes.current_value.split("=")[1]);
                            long min_value = Long.parseLong(option.min_value);
                            long max_value = Long.parseLong(option.max_value);
                            // long rand_value = (long) (Math.random() * (less_than_value - min_value) + min_value);
                            long rand_value = (long) (Math.random() * (max_value - greater_than_value) + greater_than_value);

                            option.output_set.clear();

                            if (rand_value > Long.parseLong(option.max_value) ||
                                    rand_value < Long.parseLong(option.min_value) ||
                                    min_value == greater_than_value) {
                                option.output_set.add(" ");
                            } else {
                                option.output_set.add("-XX:" + option.name + "=" + rand_value);
                            }
                            break;
                        }
                    }
                }
            }

            // get random option
            int randomIndex = (int) (Math.random() * option.output_set.size());
            option.current_value = option.output_set.get(randomIndex);
            options_seq.append(option.output_set.get(randomIndex)).append(" ");
        }
        return options_seq.toString();
    }

    public static String GetRandomGCOptions(String jdk_name, int jdk_version) {
        String currentDir = System.getProperty("user.dir");
        String options_file_root = currentDir + "/resourses/gcOptions/";
        String options_file_name = options_file_root + jdk_name + jdk_version + "/";

        String gc_name = "";
        String options_seq = "";
        int gc_index;

        // init gcOptionsMap
        if (gcOptionsMap == null) {
            gcOptionsMap = new HashMap<>();
        }

        // randomly select a gc
        if (jdk_name.equals("bishengjdk")) {
            gc_name = "g1gc";
            gc_index = 0;
//            options_seq += "-XX:+UseG1GC ";
            // set specific gc options next
            options_seq += GetGCOptionFromFile(options_file_name + gc_name + ".json", gc_index, jdk_name + jdk_version);
        } else if (jdk_name.equals("openjdk")) {
            gc_index = (int) (Math.random() * gc_set.length);
            // gc_index = 2;
            gc_name = gc_set[gc_index];
            if (gc_index == 0) {
                options_seq += "-XX:+UseG1GC ";
            } else if (gc_index == 1) {
                options_seq += "-XX:+UseShenandoahGC ";
            } else if (gc_index == 2) {
                options_seq += "-XX:+UseZGC ";
            }
            // set shared options first
            options_seq += GetGCOptionFromFile(options_file_name + "shared.json", gc_index, jdk_name + jdk_version);
            // set specific gc options next
            options_seq += GetGCOptionFromFile(options_file_name + gc_name + ".json", gc_index, jdk_name + jdk_version);
        }
        return options_seq;
    }

    public static void main(String[] args) {
        // System.out.println(GetRandomGCOptions("bishengjdk", "8"));
        // System.out.println(GetRandomGCOptions("openjdk", "23"));

        int fail_count = 0;
        int total_try = 10;
        for (int i = 0; i < total_try; ++i) {
            // String openjdk_options = GetRandomGCOptions("openjdk", "23");
//             String command = "/data/xzf/jdk23u/build/linux-x86_64-server-fastdebug/jdk/bin/java " + openjdk_options + " -version";

            String bishengjdk_options = GetRandomGCOptions("bishengjdk", 8);
            String command = "/home/user/tools/bisheng-jdk1.8.0_402/bin/java " + bishengjdk_options + " -version";

            System.out.println(command);
            // run command
            try {
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                System.out.println("###");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("###");

                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                System.out.println("!!!");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("!!!");

                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    System.out.println("Success!");
                } else {
                    System.out.println("Failed!");
                    fail_count++;
                    break;
                    // 错误处理
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Fail count: " + fail_count);
        System.out.println("ratio: " + (double) fail_count / total_try);
    }
}