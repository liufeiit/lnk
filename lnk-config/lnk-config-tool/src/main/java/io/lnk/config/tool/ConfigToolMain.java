package io.lnk.config.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * 配置工具主程序类。
 */
public class ConfigToolMain {
    public static final String CURRENT_VER = "1.0.0";

    private static final String USAGE = "config-tool [options] env-name";

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("d", "dir", true, "specify the config dir.");
        options.addOption("o", "overwrite", false, "overwrite current env dir.");
        options.addOption("v", "version", false, "print the version.");
        options.addOption("h", "help", false, "print the help information.");
        options.addOption("p", "partition", true, "specify the app parition.");
        options.addOption("n", "ns", true, "specify the ns dir or zip file.");
        return options;
    }

    public static void main(String[] args) {
        Options helpOptions = getOptions();
        Options options = getOptions();
        options.addOption("D", true, "");

        CommandLineParser parser = new PosixParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, helpOptions);
            return;
        }

        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, helpOptions);
            return;
        }

        if (line.hasOption("v")) {
            System.out.printf("config-tool version=[" + CURRENT_VER + "].\n");
            return;
        }

        if (line.hasOption("n")) {
            String nsFile = line.getOptionValue("n", null);
            if (nsFile == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, helpOptions);
                return;
            }

            if (nsFile.startsWith("/") == false) {
                String dir = line.getOptionValue("D", null);
                if (dir != null) {
                    nsFile = dir + "/" + nsFile;
                }
            }

            NsCopyUtil.deploy(nsFile);
            return;
        }

        String configDir = line.getOptionValue("d", null);
        if (configDir == null) {
            configDir = line.getOptionValue("D", null);
            if (configDir != null) {
                configDir = configDir + "/config";
            }
        }

        boolean overwrite = false;
        if (line.hasOption("o")) {
            overwrite = true;
        }

        String partition = line.getOptionValue("p", null);

        if (line.getArgs().length < 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, helpOptions);
            return;
        }

        String env = line.getArgs()[0];
        ConfigUtil.deploy(configDir, env, partition, overwrite);
    }

}
