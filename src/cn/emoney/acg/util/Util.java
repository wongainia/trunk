package cn.emoney.acg.util;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class Util {
    /**
     * 获取mac地址
     * 
     * @param context
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 检查是否全是数字
     * 
     * @param str
     * @return
     */
    public static boolean IsAllNum(String str) {
        int nLength = 0;
        if (str != null) {
            nLength = str.length();
        }
        if (nLength == 0 || str == null) {
            return false;
        }
        for (int i = 0; i < nLength; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9')
                return false;
        }
        return true;
    }

    /**
     * 股票代码补足7位
     */
    public static String FormatStockCode(int code) {
        String sCode = String.valueOf(code);
        return FormatStockCode(sCode);
    }

    /**
     * 股票代码补足7位
     */
    public static String FormatStockCode(String code) {
        try {
            String sCode = code;
            int dLen = 7 - code.length();
            for (int i = 0; i < dLen; i++) {
                sCode = "0" + sCode;
            }
            return sCode;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 返回整数部分和小数部分
     */
    public static String[] FormatPriceZS(String price, int goodsId) {
        if (TextUtils.isEmpty(price)) {
            return new String[] {"", ""};
        }
        return FormatPrice(price, goodsId).split("\\.");
    }

    /**
     * 用股票id判断是否要保存3位小数
     * 
     * @param dwGoodsID
     * @return
     */
    private static boolean isSHB(int dwGoodsID) {
        return (dwGoodsID / 100000 == 9 || dwGoodsID / 100000 == 2 || dwGoodsID / 100000 == 5 || dwGoodsID / 100000 == 11 || dwGoodsID / 10000 == 103 || dwGoodsID / 1000000 == 5);
    }

    /**
     * 格式化价钱 保留三位小数或者两位小数 比如传入58636 则返回58.636 这里的goodsid是没有处理过的goodsid
     */
    public static String FormatPrice(String price, int goodsId) {
        if (TextUtils.isEmpty(price)) {
            return "";
        }
        try {
            double b = Double.parseDouble(price) / 1000;
            return FormatPrice(b, goodsId);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 格式化价钱 保留三位小数或者两位小数
     */
    public static String FormatPrice(double price, int goodsId) {
        try {
            // 保留3位
            if (isSHB(goodsId)) {
                DecimalFormat df = new DecimalFormat();
                String pattern = "###############0.000";
                df.applyPattern(pattern);
                return df.format(formatPrice(price, 3));
            } else {
                DecimalFormat df = new DecimalFormat();
                String pattern = "###############0.00";
                df.applyPattern(pattern);
                return df.format(formatPrice(price, 2));
            }
        } catch (Exception e) {
            return price + "";
        }
    }

    /**
     * 四舍五入
     * 
     * @param price
     * @param type 保留小数点位数
     * @return
     */
    private static double formatPrice(double price, int type) {
        return new java.math.BigDecimal(Double.toString(price)).setScale(type, java.math.BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 百分数 保留两位小数
     * 
     * @return
     */
    public static String FormatPercent(String percent) {
        if (TextUtils.isEmpty(percent)) {
            return "";
        }
        try {
            double b = Double.parseDouble(percent) / 100;
            return FormatPercent(b);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 百分数 保留两位小数
     * 
     * @return
     */
    public static String FormatPercent(double percent) {
        try {
            DecimalFormat df = new DecimalFormat();
            String pattern = "#0.00";
            df.applyPattern(pattern);
            return df.format(formatPrice(percent, 2)) + "%";
        } catch (Exception e) {
            return percent + "%";
        }
    }

    /**
     * 格式化净流
     * 
     * @param hValue
     * @return
     */
    public static String jl2String(int hValue) {

        String str = "";
        String fh = "";

        if (hValue < 0) {
            hValue = -hValue;
            fh = "-";
        } else {
            fh = "";
        }

        if (hValue < 100000) {
            return fh + str + hValue;
        } else if (hValue < 1000000) // 100万
        {
            hValue = (hValue + 50) / 100;
            str = String.format("%d.%02d", hValue / 100, hValue % 100);
            return fh + str + "万";
        } else if (hValue < 10000000) // 1000万
        {
            hValue = (hValue + 500) / 1000;
            str = String.format("%d.%d", hValue / 10, hValue % 10);
            return fh + str + "万";
        } else if (hValue < 100000000) // 1亿
        {
            hValue = (hValue + 5000) / 10000;
            str = String.format("%d", hValue);
            return fh + str + "万";
        } else if (hValue < 1000000000) // 10亿
        {
            hValue = (hValue + 50000) / 100000;
            str = String.format("%d.%03d", hValue / 1000, hValue % 1000);
            return fh + str + "亿";
        } else if (hValue < 10000000000l) // 100亿
        {
            hValue = (hValue + 500000) / 1000000;
            str = String.format("%d.%02d", hValue / 100, hValue % 100);
            return fh + str + "亿";
        } else if (hValue < 100000000000l) // 1000亿
        {
            hValue = (hValue + 5000000) / 10000000;
            str = String.format("%d.%d", hValue / 10, hValue % 10);
            return fh + str + "亿";
        } else {
            hValue = (hValue + 50000000) / 100000000;
            str = String.format("%d", hValue);
            return fh + str + "亿";
        }
    }

    /**
     * 资金净流的格式化 好股抄过来的
     * 
     * @param nValue
     * @param nNum
     * @return
     */
    public static String Int2String(int nValue, int nNum) {
        String str = String.valueOf(nValue);
        int n = str.length();
        for (int i = n; i < nNum; i++)
            str = '0' + str;
        return str;
    }

    /**
     * 资金净流的格式化 好股抄过来的
     * 
     * @param lValue
     * @param nFont
     * @return
     */
    public static String Long2String(long lValue, int nFont) {
        String str;
        if (lValue <= 0) {
            lValue = -lValue;
            str = "-";
        } else
            str = "";
        if (lValue < 10000) // 1万
        {
            if (lValue != 0)
                str += String.valueOf(lValue);
            else
                str = String.valueOf(lValue);
        } else if (lValue < 100000) // 10万
        {
            lValue = (lValue + 5) / 10;
            str += String.valueOf(lValue / 1000) + '.' + Int2String((int) (lValue % 1000), 3);
            /*
             * if (nFont == 2) str += '万'; else
             */
            str += '万';
        } else if (lValue < 1000000) // 100万
        {
            lValue = (lValue + 50) / 100;
            str += String.valueOf(lValue / 100) + '.' + Int2String((int) (lValue % 100), 2);
            /*
             * if (nFont == 2) str += '万'; else
             */
            str += '万';
        } else if (lValue < 10000000) // 1000万
        {
            lValue = (lValue + 500) / 1000;
            str += String.valueOf(lValue / 10) + '.' + Int2String((int) (lValue % 10), 1);
            /*
             * if (nFont == 2) str += '万'; else
             */
            str += '万';
        } else if (lValue < 100000000) // 1亿
        {
            lValue = (lValue + 5000) / 10000;
            str += String.valueOf(lValue);
            /*
             * if (nFont == 2) str += '万'; else
             */
            str += '万';
        } else if (lValue < 1000000000) // 10亿
        {
            lValue = (lValue + 50000) / 100000;
            str += String.valueOf(lValue / 1000) + '.' + Int2String((int) (lValue % 1000), 3);
            // lValue = (lValue + 50000) / 1000000;
            // str += String.valueOf(lValue / 100) + '.'
            // + Int2String((int) (lValue % 100), 2);
            /*
             * if (nFont == 2) str += '亿'; else
             */
            str += '亿';
        } else if (lValue < 10000000000l) // 100亿
        {
            lValue = (lValue + 500000) / 1000000;
            str += String.valueOf(lValue / 100) + '.' + Int2String((int) (lValue % 100), 2);
            /*
             * if (nFont == 2) str += '亿'; else
             */
            str += '亿';
        } else if (lValue < 100000000000l) // 1000亿
        {
            lValue = (lValue + 5000000) / 10000000;
            str += String.valueOf(lValue / 10) + '.' + Int2String((int) (lValue % 10), 1);
            /*
             * if (nFont == 2) str += '亿'; else
             */
            str += '亿';
        } else {
            lValue = (lValue + 50000000) / 100000000;
            str += String.valueOf(lValue);
            /*
             * if (nFont == 2) str += '亿'; else
             */
            str += '亿';
        }
        return str;
    }

    /**
     * 获取资源文件里的dimension
     * 
     * @param resId
     * @return
     */
    public static int getResourcesDimension(int resId) {
        return ACGApplication.getInstance().getResources().getDimensionPixelSize(resId);
    }

    /**
     * 获取资源文件里的color
     * 
     * @param resId
     * @return
     */
    public static int getResourcesColor(int resId) {
        return ACGApplication.getInstance().getResources().getColor(resId);
    }

    /**
     * 获取资源文件里的字符串
     * 
     * @param resId
     * @return
     */
    public static String getResourcesString(int resId) {
        return ACGApplication.getInstance().getResources().getString(resId);
    }

    /**
     * 获取资源文件里的字符串数组
     * 
     * @param resId
     * @return
     */
    public static String[] getResourcesArrString(int resId) {
        return ACGApplication.getInstance().getResources().getStringArray(resId);
    }

    /**
     * 获取资源文件里的integer
     * 
     * @param resId
     * @return
     */
    public static int getResourcesInteger(int resId) {
        return ACGApplication.getInstance().getResources().getInteger(resId);
    }

    /**
     * 通过用户Id获取头像文件名
     * 
     * @param userId
     * @return
     */
    public static String getHeadIconName(String userId, String headId) {
        if (userId == null || userId.trim().length() <= 0) {
            return userId;
        }

        return DataUtils.convertToInt(userId) / 500 + "/" + headId + "." + DataModule.FORMAT_PNG;
    }


    /**
     * 通过Url获得文件名
     */
    public static String getFileNameOfUrl(String url) {
        String fileName = "";

        if (url == null || url.trim().length() <= 0) {
            return fileName;
        }

        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf(".");
        if (end <= 0) {
            end = url.length();
        }

        if (start > 0 && end > 0 && end > start) {
            fileName = url.substring(start, end);
        }

        return fileName;
    }

    /**
     * 判断字符串是否为数字
     * 
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().length() <= 0) {
            return false;
        }

        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 初始化webView的设置
     * 
     * @param webView
     */
    public static void initWebSetting(final WebView webView) {
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setHorizontalScrollbarOverlay(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setInitialScale(100);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置自动适应屏幕大小
        webView.getSettings().setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= 7) {
            webView.getSettings().setLoadWithOverviewMode(true);
        }

        webView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Field defaultScale = WebView.class.getDeclaredField("mDefaultScale");
                        defaultScale.setAccessible(true);
                        // WebViewSettingUtil.getInitScaleValue(VideoNavigationActivity.this,
                        // false )/100.0f 是我的程序的一个方法，可以用float 的scale替代
                        defaultScale.setFloat(webView, 1);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取meta的数值
     * 
     * @param context
     * @param metaKey
     * @return
     */
    public static String getMetaValue(String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (metaKey == null) {
            return null;
        }

        try {

            ApplicationInfo ai = ACGApplication.getInstance().getPackageManager().getApplicationInfo(ACGApplication.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
        }

        return apiKey;
    }

    /**
     * 加载头像
     * 
     * @param headView
     * @param userId
     * @param headId
     */
    public static void loadHeadIcon(ImageView headView, String userId, String headId) {
        System.out.println("**************userId:" + userId + ",headId:" + headId);
        // 是数字
        int headIdInt = DataUtils.convertToInt(headId);
        if (headIdInt > 0 && headIdInt < 1000) {
            // 系统头像
            imageLoader(headView, DataModule.HEAD_ICON_SYSTEM_PRE_URL + "/" + headId + "." + DataModule.FORMAT_PNG);
        } else {
            if (headIdInt > 0) {
                // 上传上传的头像
                imageLoader(headView, DataModule.HEAD_ICON_PRE_URL + Util.getHeadIconName(userId, headId));
            } else {
                headView.setImageResource(R.drawable.img_head_icon_default);
            }
        }
    }

    // 异步下载图片
    private static void imageLoader(ImageView headView, String url) {
        // System.out.println("***********url:" + url);
        ImageListener listener = ImageLoader.getImageListener(headView, R.drawable.img_head_icon_default, R.drawable.img_head_icon_default);
        ImageLoader imageLoader = VolleyHelper.getInstance(ACGApplication.getInstance()).getImageLoader();
        imageLoader.get(url, listener);
    }


    /**
     * 发送广播事件
     * 
     * @param action
     */
    public static void sendBroadcast(Intent intent) {
        ACGApplication.getInstance().sendBroadcast(intent);
    }


    /**
     * 获取内容里的全部股票代码(整型值)
     * 
     * @param content
     * @return
     */
    public static String getReleativeStockList(String content) {
        String res = "";
        if (content != null && !content.equals("")) {
            Pattern p = Pattern.compile("\\d{6}");
            Matcher m = p.matcher(content);
            boolean bFind = m.find();
            while (bFind) {
                res += formateGoodsId(m.group());
                bFind = m.find();
                if (bFind) {
                    res += ",";
                }
            }
        }

        // if (!TextUtils.isEmpty(res)) {
        res = "[" + res + "]";
        // }

        return res;
    }

    private static int formateGoodsId(String code) {
        if (!code.startsWith("6")) {
            return DataUtils.convertToInt(1 + code);
        } else {
            return DataUtils.convertToInt(code);
        }
    }

    // private static String formateStockCode(String code) {
    // if (!code.startsWith("6")) {
    // return Util.FormatStockCode("1" + code);
    // } else {
    // return Util.FormatStockCode(code);
    // }
    // }
}
