package cn.emoney.acg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Environment;
import cn.emoney.acg.ACGApplication;

/**
 * @ClassName: FileUtils
 * @Description:文件操作工具
 * @author xiechengfa
 * @date 2015年11月30日 上午11:06:07
 *
 */
public class FileUtils {
    /**
     * 创建文件目录(文件的父文件夹)
     * 
     * @param filePath
     */
    public static void createFileDir(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            parentFile = null;
        }
        file = null;
    }

    /**
     * 删除文件（不含文件夹）
     * 
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName) {
        try {
            if (fileName == null) {
                return false;
            }

            File f = new File(fileName);
            if (f == null || !f.exists()) {
                return false;
            }

            if (f.isDirectory()) {
                return false;
            }

            return f.delete();
        } catch (Exception e) {
            // Log.d(FILE_TAG, e.getMessage());
            return false;
        }
    }

    // 扩展可获取目录大小
    public static long getFileSize(File file) {
        // 判断文件是否存在
        if (file.exists()) {
            // 如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                for (File f : children)
                    size += getFileSize(f);
                return size;
            } else {// 如果是文件则直接返回其大小,以“兆”为单位
                long size = file.length();
                return size;
            }
        } else {
            return 0;
        }
    }

    public static String getFileSizeString(File file) {
        long lszie = getFileSize(file);

        String sSize = DataUtils.formatFileSize(lszie);
        return sSize;
    }

    // 尝试获取tf卡路径,使用系统方法
    public static String getTFCardPath() {

        String sdcard_path = "";
        String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (sd_default.endsWith("/")) {
            sd_default = sd_default.substring(0, sd_default.length() - 1);
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                } else if (line.contains("fuse") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtil.easylog("sky", "tf卡:" + sdcard_path);

        return sdcard_path;
    }

    /**
     * 获取SDCard的路径
     * 
     * @return
     */
    public static String getStoragePath() {
        // Environment.MEDIA_MOUNTED // sd卡在手机上正常使用状态
        // Environment.MEDIA_UNMOUNTED // 用户手工到手机设置中卸载sd卡之后的状态
        // Environment.MEDIA_REMOVED // 用户手动卸载，然后将sd卡从手机取出之后的状态
        // Environment.MEDIA_BAD_REMOVAL // 用户未到手机设置中手动卸载sd卡，直接拨出之后的状态
        // Environment.MEDIA_SHARED // 手机直接连接到电脑作为u盘使用之后的状态
        // Environment.MEDIA_CHECKINGS // 手机正在扫描sd卡过程中的状态
        File file = null;
        String exStorageState = Environment.getExternalStorageState();
        if (exStorageState != null && Environment.MEDIA_SHARED.equals(exStorageState)) {
            // 有SD卡，手机直接连接到电脑作为u盘使用之后的状态
            return null;
        }

        if (exStorageState != null && Environment.MEDIA_MOUNTED.equals(exStorageState)) {
            // 是否存在外存储器(优先判断)
            file = Environment.getExternalStorageDirectory();
        } else {
            // 如果外存储器不存在，则判断内存储器
            file = ACGApplication.getInstance().getFilesDir();
        }

        if (file != null) {
            return file.getPath() + "/";
        } else {
            return null;
        }
    }

    /**
     * 检查SDCard是否存在
     * 
     * @return
     */
    public static boolean isExistsStorage() {
        return getStoragePath() != null ? true : false;
    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
