package edu.hust.xzf.mutator;

import edu.hust.xzf.mutator.config.Configuration;
import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {

    private static final String PROJECT_PATH = "project_path"; // source code root path. e.g. /home/kui/Desktop/buggyProject
    private static final String TARGET_CASE = "target_case"; // target java file. e.g. a.b.c denotes a/b/c.java
    private static final String JDK = "jdk"; // the jdk directory, path to bin/
    private static final String JDK_level = "jdk_level";
    private static final String LINE_NUMBER = "line_number";

    private static final String MAX_ITER = "max_iter";

    private static final String USE_RANDOM_JIT_OPTIONS = "use_jit_options";

    private static final String USE_JTREG = "is_use_jtreg";

    private static final String ENABLE_PROFILE_GUIDE = "enable_profile_guide";

    private static final String USE_RANDOM_GC_OPTIONS = "use_gc_options";


    protected final Options options = new Options();

    public Main() {
        initializeCommandLineOptions();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run(args);
    }

    private void run(String[] args) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLine cmd = null;

        // We need proper parameters
        final HelpFormatter formatter = new HelpFormatter();
        if (args.length == 0) {
            hf.printHelp("help", options, true);
            System.exit(0);
        }
        try {
            CommandLineParser parser = new PosixParser();
            try {
                cmd = parser.parse(options, args);
                cmd.getArgs();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            // Do we need to display the user manual?
            if (cmd.hasOption("?") || cmd.hasOption("help")) {
                formatter.printHelp("[OPTIONS]", options);
                return;
            }
            Configuration config = new Configuration();
            parseCommandOptions(cmd, config);
            config.settingAll();

            Scheduler scheduler = new Scheduler(config);
            scheduler.run();
        } catch (Exception e) {
            System.err.printf("The analysis has failed. Error message: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }


    private void initializeCommandLineOptions() {
        options.addOption("?", "help", false, "Print this help message");
        options.addOption(PROJECT_PATH, true, "source code root path. e.g., /home/kui/Desktop/buggyProject. Necessary.");
        options.addOption(TARGET_CASE, true, "target java file. e.g., a.b.c denotes a/b/c.java. Necessary.");
        options.addOption(JDK, true, "the jdk directory, path to bin/. At least one JDK. We will check whether" +
                " the outputs of these JDKs are consistant. Necessary.");
        options.addOption(LINE_NUMBER, true, "line number of target file to be mutated. e.g., 10. Default is null.");
        options.addOption(MAX_ITER, true, "max iteration times.. e.g., 100. Default is 10");
//        options.addOption(RANDOM_OPTION, true, "disable random options? e.g., true. Default is false");
        options.addOption(USE_JTREG, true, "use Jtreg? e.g., true. Default is false");
        options.addOption(USE_RANDOM_GC_OPTIONS, true, "use random GC options? e.g., true. Default is false");
        options.addOption(USE_RANDOM_JIT_OPTIONS, true, "use random JIT options? e.g., true. Default is false");
        options.addOption(ENABLE_PROFILE_GUIDE, true, "enable profile guidance? We recommand false" +
                " for faster testing. Default is false.");
    }

    protected void parseCommandOptions(CommandLine cmd, Configuration config) throws IOException {
        String project_path = cmd.getOptionValue(PROJECT_PATH);
        if (project_path != null) {
            config.projectPath = project_path;
        } else throw new IOException("project_path is null");

        String target_case = cmd.getOptionValue(TARGET_CASE);
        if (target_case != null) {
            config.targetCase = target_case;
        } else throw new IOException("target_case is null");

        String jdk = cmd.getOptionValue(JDK);
        if (jdk != null) {
            config.JDKRoot = jdk;
        } else throw new IOException("jdk is null");

        String line_number = cmd.getOptionValue(LINE_NUMBER);
        if (line_number != null) {
            Configuration.lineNumber = Integer.parseInt(line_number);
        } else Configuration.lineNumber = -1;

        String max_iter = cmd.getOptionValue(MAX_ITER);
        if (max_iter != null) {
            Configuration.maxIter = Integer.parseInt(max_iter);
        } else Configuration.maxIter = 50;

//        String random_option = cmd.getOptionValue(RANDOM_OPTION);
//        if (random_option != null) {
//            Configuration.disableRandomOptions = Boolean.parseBoolean(random_option);
//        } else Configuration.disableRandomOptions = false;

        String enable_profile_guidance = cmd.getOptionValue(ENABLE_PROFILE_GUIDE);
        if (enable_profile_guidance != null) {
            Configuration.enableProfileGuidance = Boolean.parseBoolean(enable_profile_guidance);
        } else Configuration.enableProfileGuidance = false;

        String useJtreg = cmd.getOptionValue(USE_JTREG);
        if (useJtreg != null) {
            Configuration.usejTreg = Boolean.parseBoolean(useJtreg);
        } else Configuration.usejTreg = false;

        String useGCOption = cmd.getOptionValue(USE_RANDOM_GC_OPTIONS);
        if (useGCOption != null) {
            Configuration.useRandomGCOptions = Boolean.parseBoolean(useGCOption);
        } else Configuration.useRandomGCOptions = false;

        String useJITOption = cmd.getOptionValue(USE_RANDOM_JIT_OPTIONS);
        if (useJITOption != null) {
            Configuration.useRandomJITOptions = Boolean.parseBoolean(useJITOption);
        } else Configuration.useRandomJITOptions = false;

    }

}
