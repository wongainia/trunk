package cn.emoney.acg.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import cn.emoney.acg.ACGApplication;

public class DeviceInfoUtil {
    public String mModel = "";
    public String mProduct = "";
    public String mDevice = "";
    public String mBoard = "";
    public String mBrand = "";
    public String mSDKVer = "";

    private static DeviceInfoUtil mInstance = null;

    public static DeviceInfoUtil getInstance() {
        if (mInstance == null) {
            mInstance = new DeviceInfoUtil();
        }

        return mInstance;
    }

    DeviceInfoUtil() {
        mModel = android.os.Build.MODEL;
        mProduct = android.os.Build.PRODUCT;
        mDevice = android.os.Build.DEVICE;
        mBoard = android.os.Build.BOARD;
        mBrand = android.os.Build.BRAND;
        mSDKVer = String.valueOf(android.os.Build.VERSION.SDK_INT);
    }

    /**
     * 获取手机固件版本号(整型)
     * 
     * @return
     */
    public static int getPhoneSdkVersion() {
        return Build.VERSION.SDK_INT;
    }


    /**
     * 获取手机固件版本号(字符串)
     * 
     * @return
     */
    public static String getPhoneSDKVersionChar() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 手机型号
     * 
     * @return
     */
    public static String getHandsetType() {
        return android.os.Build.MODEL;
    }


    /**
     * 平台
     * 
     * @return
     */
    public static String getPlatForm() {
        return "mobile.anroid";// android
    }

    /**
     * 版本号内容(字符串)
     * 
     * @return
     */
    public static String getVersionChars() {
        PackageManager packageManager = ACGApplication.getInstance().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(ACGApplication.getInstance().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (packageInfo == null) {
            return "1.0";
        }
        String versionStr = packageInfo.versionName;

        packageInfo = null;
        packageManager = null;
        if (versionStr == null) {
            versionStr = "1.0";
        }
        return versionStr;
    }

    /**
     * 版本号(整型)
     * 
     * @return
     */
    public static int getVersionInt() {
        PackageManager packageManager = ACGApplication.getInstance().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(ACGApplication.getInstance().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (packageInfo == null) {
            return 1;
        }

        int versionCode = packageInfo.versionCode;

        packageInfo = null;
        packageManager = null;

        return versionCode;
    }
}
