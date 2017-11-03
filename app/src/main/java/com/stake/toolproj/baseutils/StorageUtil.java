package com.stake.toolproj.baseutils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused")
public class StorageUtil {

    public static boolean isMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getAppSdRootPath(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            packageName = ApplicationStatus.getApplicationContext().getPackageName();
        }

        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + packageName + File.separator;
    }

    /**
     * 获取SD卡路径
     * <p>先用shell，shell失败再普通方法获取，一般是/storage/emulated/0/</p>
     *
     * @return SD卡路径
     */
    public static String getSDCardPath() {
        if (!isMounted()) return null;
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();
        BufferedReader bufferedReader = null;
        try {
            Process p = run.exec(cmd);
            bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));
            String lineStr;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.contains("sdcard") && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray.length >= 5) {
                        return strArray[1].replace("/.android_secure", "") + File.separator;
                    }
                }
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    /**
     * 获取SD卡剩余空间
     *
     * @return SD卡剩余空间(format size for show)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getFreeSpace() {
        return byte2FitMemorySize(getExactFreeSpace());
    }

    /**
     * 获取SD卡剩余空间
     *
     * @return SD卡剩余空间(byte)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static Long getExactFreeSpace() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return -1L;
        }

        if (!isMounted()) {
            return -1L;
        }

        StatFs stat = new StatFs(getSDCardPath());
        long blockSize, availableBlocks;
        availableBlocks = stat.getAvailableBlocksLong();
        blockSize = stat.getBlockSizeLong();
        return availableBlocks * blockSize;
    }

    /**
     * 字节数转合适内存大小
     * <p>保留2位小数</p>
     *
     * @param byteNum 字节数
     * @return 合适内存大小
     */
    @SuppressLint("DefaultLocale")
    public static String byte2FitMemorySize(long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum < Unit.KB) {
            return String.format("%.2fB", (double) byteNum + 0.0005);
        } else if (byteNum < Unit.MB) {
            return String.format("%.2fK", (double) byteNum / Unit.KB + 0.0005);
        } else if (byteNum < Unit.GB) {
            return String.format("%.2fM", (double) byteNum / Unit.MB + 0.0005);
        } else {
            return String.format("%.2fG", (double) byteNum / Unit.GB + 0.0005);
        }
    }

    @IntDef({Unit.KB, Unit.MB, Unit.GB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Unit {
        int KB = 1024;
        int MB = 1024 * 1024;
        int GB = 1024 * 1024 * 1024;
    }
}
