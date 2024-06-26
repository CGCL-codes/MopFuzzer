package edu.hust.xzf.mutator.utils;

import edu.hust.xzf.mutator.config.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

public class ShellUtils {

    public static String shellRun(List<String> asList, String buggyProject, int type) throws IOException {
        String fileName;
        String cmd;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            Process process = Runtime.getRuntime().exec(StringUtils.join(asList, " "));
            return ShellUtils.getShellOut(process, type, "logs/compile_log.log");
        } else {
            fileName = Configuration.TEMP_FILES_PATH + buggyProject + ".sh";
            cmd = "bash " + fileName;
        }
        File batFile = new File(fileName);
        if (!batFile.exists()) {
            if (!batFile.getParentFile().exists()) {
                batFile.getParentFile().mkdirs();
            }
            boolean result = batFile.createNewFile();
            if (!result) {
                throw new IOException("Cannot Create bat file:" + fileName);
            }
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(batFile);
            for (String arg : asList) {
                outputStream.write(arg.getBytes());
            }
            outputStream.close();
        } catch (IOException e) {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        batFile.deleteOnExit();

        String results = ShellUtils.getShellOut(Runtime.getRuntime().exec(cmd), type, "logs/compile_log.log");
        batFile.delete();
        return results;
    }

    public static String genCompileCmd(List<String> asList, String buggyProject) throws IOException {
        String fileName;
        String cmd;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            fileName = Configuration.TEMP_FILES_PATH + buggyProject + "/cmd.bat";
            cmd = Configuration.TEMP_FILES_PATH + "/" + buggyProject + "/cmd.bat";
        } else {
            fileName = Configuration.TEMP_FILES_PATH + "/" + buggyProject + "/cmd.sh";
            cmd = "bash " + fileName;
        }
        File batFile = new File(fileName);
        if (!batFile.exists()) {
            if (!batFile.getParentFile().exists()) {
                batFile.getParentFile().mkdirs();
            }
            boolean result = batFile.createNewFile();
            if (!result) {
                throw new IOException("Cannot Create bat file:" + fileName);
            }
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(batFile);
            for (String arg : asList) {
                outputStream.write(arg.getBytes());
            }
            outputStream.close();
        } catch (IOException e) {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        batFile.deleteOnExit();

        return cmd;
    }

    public static String getShellOut(Process process, int type, String logFile) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<String> future = service.submit(new ReadShellProcess(process, logFile));
        String returnString;
        try {
            if (type == 2)
                returnString = future.get(Configuration.TEST_SHELL_RUN_TIMEOUT, TimeUnit.SECONDS);
            else
                returnString = future.get(Configuration.SHELL_RUN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            future.cancel(true);
//            e.printStackTrace();
            shutdownProcess(service, process);
            return "";
        } finally {
            shutdownProcess(service, process);
        }
        return returnString;
    }

    private static void shutdownProcess(ExecutorService service, Process process) {
        service.shutdownNow();
        try {
            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        process.destroy();
    }
}

class ReadShellProcess implements Callable<String> {
    public Process process;
    public String logFile;

    public ReadShellProcess(Process p, String logFile) {
        this.process = p;
        this.logFile = logFile;
    }

    public synchronized String call() {
        StringBuilder sb = new StringBuilder();
        BufferedInputStream in = null;
        BufferedReader br = null;
        try {
            String s;
            in = new BufferedInputStream(process.getInputStream());
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            while ((s = br.readLine()) != null && !s.isEmpty()) {
                if (sb.length() < 1000000) {
                    if (Thread.interrupted()) {
                        return sb.toString();
                    }
                    sb.append(System.getProperty("line.separator"));
                    sb.append(s);
                }
            }
            in = new BufferedInputStream(process.getErrorStream());
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            while ((s = br.readLine()) != null && !s.isEmpty()) {
                if (Thread.interrupted()) {
                    return sb.toString();
                }
                if (sb.length() < 1000000) {
                    sb.append(System.getProperty("line.separator"));
                    sb.append(s);
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            process.destroy();
        }
        FileHelper.outputToFile(logFile, sb, true);
        return sb.toString();
    }
}