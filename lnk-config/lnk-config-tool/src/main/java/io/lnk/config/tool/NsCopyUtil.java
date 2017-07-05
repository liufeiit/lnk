package io.lnk.config.tool;

import java.io.File;
import java.io.IOException;

import io.lnk.config.ctx.config.ConfigHome;
import io.lnk.config.ctx.utils.ZipFileTool;

/**
 * 名字服务复制工具类。
 */
public class NsCopyUtil {
    private static void copyFileOrDir(File f, String deployPath) {
        File tempZipFile = null;

        try {
            tempZipFile = File.createTempFile(".NsCopy", "zip");
            tempZipFile.delete();
            ZipFileTool.zipFiles(tempZipFile, new File[] {f}, null, null, false);

            ZipFileTool.unzipFile(tempZipFile, deployPath, null, null);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (tempZipFile != null) {
                tempZipFile.delete();
            }
        }
    }

    public static void deploy(String nsFile) {
        File f = new File(nsFile);
        String deployPath = ConfigHome.getNsDir();
        File deployDir = new File(deployPath);
        if (deployDir.exists() == false) {
            deployDir.mkdirs();
        }

        if (f.isFile()) {
            if (f.getName().endsWith(".zip") || f.getName().endsWith(".jar")) {
                // zip 文件
                ZipFileTool.unzipFile(f, deployPath, null, null);
                File metaInfoFile = new File(deployPath + "/META-INF");
                if (metaInfoFile.exists()) {
                    // 删除jar文件的META-INF目录
                    File manifestFile = new File(deployPath + "/META-INF/MANIFEST.MF");
                    manifestFile.delete();
                    metaInfoFile.delete();
                }
            } else {
                copyFileOrDir(f, deployPath);
            }
        } else {
            copyFileOrDir(f, deployPath);
        }
    }
}
