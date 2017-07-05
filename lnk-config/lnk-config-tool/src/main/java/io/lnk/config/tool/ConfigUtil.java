package io.lnk.config.tool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.lnk.config.ctx.config.ConfigHome;
import io.lnk.config.ctx.utils.DateUtils;
import io.lnk.config.ctx.utils.FileUtils;

/**
 * 配置工具类。
 */
public class ConfigUtil {
    private static Pattern pattern = Pattern.compile("^([^=]*)=(.*)$");

    private static File createOrGetDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return dir;
            } else {
                throw new RuntimeException("It isn't a directory, path=[" + path + "].");
            }
        }

        dir.mkdirs();

        return dir;
    }

    private static void copyOrMergeProps(File propsFile, File targetPropsFile) {
        if (targetPropsFile.exists() == false) {
            FileUtils.copyFile(propsFile, targetPropsFile);
            return;
        }

        if (targetPropsFile.isDirectory()) {
            throw new RuntimeException("Target props file is directory, path=[" + targetPropsFile.getAbsolutePath() + "].");
        }

        Properties props = new Properties();
        FileUtils.loadProperties(props, propsFile);
        Properties targetProps = new Properties();
        FileUtils.loadProperties(targetProps, targetPropsFile);

        Map<String, String> overrideProps = new HashMap<String, String>();
        for (Entry<Object, Object> entry : targetProps.entrySet()) {
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value.lastIndexOf("#fix") >= 0) {
                // 覆盖参数，不能复制目标
                overrideProps.put(name, value);
            }
        }

        FileUtils.FileReadHandler src = FileUtils.openForRead(propsFile);
        FileUtils.FileWriteHandler tgt = FileUtils.openForWrite(targetPropsFile);

        try {
            while (true) {
                String line = FileUtils.readLine(src);
                if (line == null) {
                    break;
                }

                Matcher matcher = pattern.matcher(line);
                boolean matchFound = matcher.find();
                if (matchFound) {
                    String propName = matcher.group(1).trim();
                    if (overrideProps.containsKey(propName)) {
                        FileUtils.printf(tgt, "%s=%s\n", propName, overrideProps.get(propName));
                        System.out.printf("keep the property=[%s] value=[%s] in file=[%s].\n", propName, overrideProps.get(propName), targetPropsFile.getPath());
                        continue;
                    }
                }

                FileUtils.printf(tgt, "%s\n", line);
            }
        } finally {
            FileUtils.close(src);
            FileUtils.close(tgt);
        }
    }

    private static void deployProps(File configDir, String partition, String targetPath) {
        File[] files = configDir.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".props")) {
                copyOrMergeProps(file, new File(targetPath + "/" + file.getName()));
            } else {
                FileUtils.copyFile(file, new File(targetPath + "/" + file.getName()));
            }
        }
    }

    public static void deploy(String configDirPath, String env, String partition, boolean overwrite) {
        if (configDirPath == null) {
            configDirPath = "config";
        }
        File configDir = new File(configDirPath);
        if (configDir.isDirectory() == false) {
            throw new RuntimeException("Not found the config directory.");
        }

        File systemIdDir = null;
        for (File f : configDir.listFiles()) {
            if (f.isDirectory() && !f.isHidden()) {
                systemIdDir = f;
                break;
            }
        }

        if (systemIdDir == null) {
            throw new RuntimeException("Not found the systemId directory.");
        }

        String systemId = systemIdDir.getName();
        File envDir = new File(systemIdDir.getPath() + "/" + env);

        if (envDir.isDirectory() == false) {
            throw new RuntimeException("Not found the envDir=[config/" + systemId + "/" + env + "]");
        }

        String configHome = ConfigHome.getDir();
        configHome = configHome + "/" + systemId;
        createOrGetDir(configHome);

        File envFile = new File(configHome + "/env");
        FileUtils.printf(envFile, "%s\n", env);

        File targetEnvDir = new File(configHome + "/" + env);
        if (targetEnvDir.exists() && overwrite) {
            // 完全覆盖方式，备份当前目录
            File bakEnvDir = new File(configHome + "/" + env + "." + DateUtils.format("yyyyMMddHHmmss", new java.util.Date()));
            targetEnvDir.renameTo(bakEnvDir);
            System.out.printf("move the old env dir=[%s] to [%s].\n", targetEnvDir.getPath(), bakEnvDir.getName());
        }

        targetEnvDir = createOrGetDir(configHome + "/" + env);
        deployProps(envDir, partition, targetEnvDir.getPath());

        System.out.printf("env deployed.\n");
    }
}
