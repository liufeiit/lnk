package io.lnk.config.ctx.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月5日 上午11:12:33
 */
public class ZipFileTool {
    public static void zipFiles(File tgtFile, File[] files, String filePath, String password, boolean includeRootFolder) {
        try {
            ZipFile zipFile = new ZipFile(tgtFile);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            if (filePath != null) {
                parameters.setIncludeRootFolder(true);
                parameters.setRootFolderInZip(filePath);
            } else {
                parameters.setIncludeRootFolder(includeRootFolder);
            }

            if (password != null) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                parameters.setPassword(password);
            }

            ArrayList<File> filesToAdd = new ArrayList<File>();
            ArrayList<File> foldersToAdd = new ArrayList<File>();
            for (File file : files) {
                if (file.isFile()) {
                    filesToAdd.add(file);
                } else {
                    foldersToAdd.add(file);
                }
            }

            if (filesToAdd.isEmpty() == false) {
                zipFile.addFiles(filesToAdd, parameters);
            }

            if (foldersToAdd.isEmpty() == false) {
                for (File file : foldersToAdd) {
                    zipFile.addFolder(file, parameters);
                }
            }
        } catch (ZipException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void zipFiles(File tgtFile, File[] files) {
        zipFiles(tgtFile, files, null, null, true);
    }

    public static void zipFile(File tgtFile, File file) {
        zipFiles(tgtFile, new File[] {file}, null, null, true);
    }

    public static File zipFile(File file) {
        File tgtFile = new File(FileUtils.changeFileNameExtension(file.getPath(), "zip"));
        zipFile(tgtFile, file);
        return tgtFile;
    }

    public static File zipFile(File file, String password) {
        File tgtFile = new File(FileUtils.changeFileNameExtension(file.getPath(), "zip"));
        zipFiles(tgtFile, new File[] {file}, null, password, true);
        return tgtFile;
    }

    @SuppressWarnings("unchecked")
    public static File[] unzipFile(File srcFile, String destPath, String password, String fileName) {
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            if (password != null) {
                zipFile.setPassword(password);
            }

            if (destPath == null) {
                destPath = srcFile.getParent();
            }

            if (fileName == null) {
                zipFile.extractAll(destPath);
            } else {
                zipFile.extractFile(fileName, destPath);
            }

            List<FileHeader> fileNameLists = zipFile.getFileHeaders();
            File[] files = new File[fileNameLists.size()];
            for (int i = 0; i < files.length; i++) {
                files[i] = new File(destPath + "/" + fileNameLists.get(i).getFileName());
            }

            return files;
        } catch (ZipException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static File[] unzipFile(File srcFile, String password) {
        return unzipFile(srcFile, null, password, null);
    }

    public static File[] unzipFile(File srcFile) {
        return unzipFile(srcFile, null, null, null);
    }
}
