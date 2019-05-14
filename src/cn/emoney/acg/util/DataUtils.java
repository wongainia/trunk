package cn.emoney.acg.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.text.TextUtils;

/**
 * 数据处理工具类
 * 
 * @ClassName: DataUtils
 * @Description:
 * @author xiechengfa
 * @date 2015年11月30日 上午10:37:48
 */
public class DataUtils {
    public static final DecimalFormat mDecimalFormat = new DecimalFormat("0");
    public static final DecimalFormat mDecimalFormat1 = new DecimalFormat("0.0");
    public static final DecimalFormat mDecimalFormat2 = new DecimalFormat("0.00");
    public static final DecimalFormat mDecimalFormat3 = new DecimalFormat("0.000");
    public static final DecimalFormat mDecimalFormat4 = new DecimalFormat("0.0000");
    public static final DecimalFormat mAmountFormat = new DecimalFormat("0");
    public static final DecimalFormat mZDFFormat = new DecimalFormat("0.00");
    public static final DecimalFormat mPriceFormat = new DecimalFormat("0.00");
    public static final DecimalFormat mHSLFormat = new DecimalFormat("0.00");
    public static final DecimalFormat mZDFormat = new DecimalFormat("0.00");
    public static final DecimalFormat mFloat2Percent = new DecimalFormat("0.00");
    public static final DecimalFormat mFloat2JL = new DecimalFormat("0.00");

    public static final DecimalFormat mDecimalFormat1_max = new DecimalFormat("0.#");
    public static final DecimalFormat mDecimalFormat2_max = new DecimalFormat("0.##");

    public DataUtils() {
        // TODO Auto-generated constructor stub
    }

    public static String formatFloat2Percent(float val) {
        val = val * 100;
        return mZDFFormat.format(val) + "%";
    }

    /**
     * 将百分比数字转化为小数
     * */
    public static String formatPercent2Float(String percent) {
        double value = convertToDouble(percent);
        value = value / 100;
        return mDecimalFormat4.format(value);
    }

    // 100646或者70412 转换成 10:06 或者 07:04 int
    public static String formatTimeH_M(int orgiTime) {
        String t_sTime = String.valueOf(orgiTime);
        return formatTimeH_M(t_sTime);
    }

    // 100646或者70412 转换成 10:06 或者 07:04 string
    public static String formatTimeH_M(String orgTimeStr) {
        String fixTimeString = "";
        String t_str = formatTimeH_M_S(orgTimeStr);
        if (!t_str.equals("")) {
            fixTimeString = t_str.substring(0, 5);
        }

        return fixTimeString;
    }

    // 100646或者70412 转换成 10:06:46 或者 07:04:12
    public static String formatTimeH_M_S(String orgTimeStr) {
        String fixTimeString = "";
        if (orgTimeStr != null) {
            int nlen = orgTimeStr.length();
            if (nlen == 4) {
                fixTimeString = "00:" + orgTimeStr.substring(0, 2) + ":" + orgTimeStr.substring(2, 4);
            } else if (nlen == 5) {
                fixTimeString = "0" + orgTimeStr.substring(0, 1) + ":" + orgTimeStr.substring(1, 3) + ":" + orgTimeStr.substring(3, 5);
            } else if (nlen == 6) {
                fixTimeString = orgTimeStr.substring(0, 2) + ":" + orgTimeStr.substring(2, 4) + ":" + orgTimeStr.substring(4, 6);
            }

        }
        return fixTimeString;
    }

    // 1507111022 15年07月11日11时22分 -> 15/07/11/10:22
    public static String formatDateY_M_D_HHmm(String orgDate, String separator) {
        String fixDate = "";
        String t_separator = "/";
        if (separator != null && !separator.equals("")) {
            t_separator = separator;
        }

        int nlen = orgDate.length();
        if (nlen >= 10) {
            String tYear = orgDate.substring(0, 2);
            String tMonth = orgDate.substring(2, 4);
            if (tMonth.startsWith("0")) {
                tMonth = tMonth.replace("0", "");
            }

            String tDay = orgDate.substring(4, 6);
            if (tDay.startsWith("0")) {
                tDay = tDay.replace("0", "");
            }

            String tHour = orgDate.substring(6, 8);

            String tMinute = orgDate.substring(8, 10);

            fixDate = tYear + t_separator + tMonth + t_separator + tDay + t_separator + tHour + ":" + tMinute;
        }
        return fixDate;
    }

    // 20140601 转化为 2014/6/1
    public static String formatDateY_M_D(String orgDate, String separator) {
        String fixDate = "";
        String t_separator = "/";
        if (separator != null && !separator.equals("")) {
            t_separator = separator;
        }

        int nlen = orgDate.length();
        if (nlen >= 8) {
            String tYear = orgDate.substring(0, 4);

            String tMonth = orgDate.substring(4, 6);
            if (tMonth.startsWith("0")) {
                tMonth = tMonth.replace("0", "");
            }

            String tDay = orgDate.substring(6, 8);
            if (tDay.startsWith("0")) {
                tDay = tDay.replace("0", "");
            }

            fixDate = tYear + t_separator + tMonth + t_separator + tDay;
        }

        return fixDate;
    }

    public static String formatDateM_D(String orgDate, String separator) {
        String fixDate = "";
        String t_separator = "/";
        if (separator != null && !separator.equals("")) {
            t_separator = separator;
        }

        int nlen = orgDate.length();
        if (nlen >= 8) {
            String tMonth = orgDate.substring(4, 6);
            if (tMonth.startsWith("0")) {
                tMonth = tMonth.replace("0", "");
            }

            String tDay = orgDate.substring(6, 8);
            if (tDay.startsWith("0")) {
                tDay = tDay.replace("0", "");
            }

            fixDate = tMonth + t_separator + tDay;
        }

        return fixDate;
    }

    // 20140601 转化为2014/6
    public static String formatDateY_M(String orgDate, String separator) {
        String fixDate = "";
        String t_separator = "/";
        if (separator != null && !separator.equals("")) {
            t_separator = separator;
        }

        String t = formatDateY_M_D(orgDate, t_separator);
        if (!t.equals("")) {
            int end = t.indexOf(t_separator, 5);
            if (end > 0) {
                fixDate = t.substring(0, end);
            }

        }
        return fixDate;
    }

    public static String formatPercentLevel(double val) {
        String flag = "";
        if (val < 0) {
            flag = "—";
        }
        val = Math.abs(val) * 100;
        if (0 <= val && val < 10000) {
            return flag + mDecimalFormat2.format(val) + "%";
        } else if (val >= 10000 && val < 100000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return flag + mDecimalFormat2.format(newVal) + "万%";
        } else if (val >= 100000000) {
            double newVal = val;
            newVal = newVal / 100000000;
            return flag + mDecimalFormat2.format(newVal) + "亿%";
        } else {
            return null;
        }
    }

    public static String formatNumLevel(DecimalFormat deFormat, double val) {
        String flag = "";
        if (val < 0) {
            flag = "—";
        }
        val = Math.abs(val);
        if (0 <= val && val < 10000) {
            return flag + deFormat.format(val);
        } else if (val >= 10000 && val < 100000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return flag + deFormat.format(newVal) + "万";
        } else if (val >= 100000000) {
            double newVal = val;
            newVal = newVal / 100000000;
            return flag + deFormat.format(newVal) + "亿";
        } else {
            return null;
        }
    }

    public static String formatNumLevel(double val) {
        return formatNumLevel(mDecimalFormat2, val);
    }

    // 总手
    public static String formatVolume(String val) {
        return formatVolume(convertToLong(val));
    }

    public static String formatVolume(long val) {
        if (val < 100000) {
            return mDecimalFormat.format(val);
        } else if (val >= 100000 && val < 1000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return mDecimalFormat2.format(newVal) + "万";
        } else if (val >= 1000000 && val < 10000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return mDecimalFormat1.format(newVal) + "万";
        } else if (val >= 10000000 && val < 100000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return mDecimalFormat.format(newVal) + "万";
        } else {
            double newVal = val;
            newVal = newVal / 100000000;
            return mDecimalFormat2.format(newVal) + "亿";
        }
    }

    // 市值
    public static String formatSZ(String val) {
        // return formatAmount(convertToLong(val));
        return formatAmount(convertToLong(val));

    }

    // 金额
    public static String formatAmount(String val) {
        return formatAmount(convertToLong(val) / 1000);
    }

    public static String formatAmount(long val) {
        if (val < 0) {
            val = Math.abs(val);
            if (val < 100000) {
                return "-" + mDecimalFormat.format(val);
            } else if (val >= 100000 && val < 1000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return "-" + mDecimalFormat2.format(newVal) + "万";
            } else if (val >= 1000000 && val < 10000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return "-" + mDecimalFormat1.format(newVal) + "万";
            } else if (val >= 10000000 && val < 100000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return "-" + mDecimalFormat.format(newVal) + "万";
            } else if (val >= 100000000 && val < 1000000000) {
                double newVal = val;
                newVal = newVal / 100000000;
                return "-" + mDecimalFormat2.format(newVal) + "亿";
            } else if (val >= 1000000000 && val < 10000000000L) {
                double newVal = val;
                newVal = newVal / 100000000;
                return "-" + mDecimalFormat1.format(newVal) + "亿";
            } else {
                double newVal = val;
                newVal = newVal / 100000000;
                return "-" + mDecimalFormat.format(newVal) + "亿";
            }
        } else {
            if (val < 100000) {
                return mDecimalFormat.format(val);
            } else if (val >= 100000 && val < 1000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return mDecimalFormat2.format(newVal) + "万";
            } else if (val >= 1000000 && val < 10000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return mDecimalFormat1.format(newVal) + "万";
            } else if (val >= 10000000 && val < 100000000) {
                double newVal = val;
                newVal = newVal / 10000;
                return mDecimalFormat.format(newVal) + "万";
            } else if (val >= 100000000 && val < 1000000000) {
                double newVal = val;
                newVal = newVal / 100000000;
                return mDecimalFormat2.format(newVal) + "亿";
            } else if (val >= 1000000000 && val < 10000000000L) {
                double newVal = val;
                newVal = newVal / 100000000;
                return mDecimalFormat1.format(newVal) + "亿";
            } else {
                double newVal = val;
                newVal = newVal / 100000000;
                return mDecimalFormat.format(newVal) + "亿";
            }
        }
    }


    public static String formatZD(String val, DecimalFormat df) {
        return formatZD(DataUtils.convertToFloat(val), df);

    }

    public static String formatZD(float val, DecimalFormat df) {
        String flag = "";
        if (val < 0) {
            flag = "-";
            val = -val;
        }
        DecimalFormat t_df = mDecimalFormat2;
        if (df != null) {
            t_df = df;
        }

        if (val < 10000) {
            return flag + t_df.format(val);
        } else if (val >= 100000 && val < 100000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return flag + mDecimalFormat.format(newVal) + "万";
        } else {
            double newVal = val;
            newVal = newVal / 100000000;
            return flag + t_df.format(newVal) + "亿";
        }
    }

    public static String formatJL(String val, DecimalFormat df) {
        return formatJL(DataUtils.convertToLong(val), df);

    }

    /**
     * 格式化量比
     * */
    public static String formatLb(String lb) {
        if (!TextUtils.isEmpty(lb)) {
            // 1. 转换为数值型
            double value = convertToDouble(lb);

            // 2. 除以100
            value = value / 100;

            // 3. 保留两位小数
            return mZDFFormat.format(value);
        }

        return lb;
    }

    public static String formatJL(long val, DecimalFormat df) {
        String flag = "";
        if (val < 0) {
            flag = "-";
            val = -val;
        }
        DecimalFormat t_df = mFloat2JL;
        if (df != null) {
            t_df = df;
        }

        if (val < 10000) {
            return flag + String.valueOf(val);
        } else if (val >= 100000 && val < 100000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return flag + mDecimalFormat.format(newVal) + "万";
        } else {
            double newVal = val;
            newVal = newVal / 100000000;
            return flag + t_df.format(newVal) + "亿";
        }
    }

    public static String formatTraffic(float val) {
        if (val >= 1048576) {
            return mDecimalFormat.format(val / 1048576) + "M";
        } else if (val >= 1024) {
            return mDecimalFormat.format(val / 1024) + "K";
        }
        return mDecimalFormat.format(val) + "B";
    }

    /**
     * 关注人数 9人, 99人, 999人, 9.99万, 99.9万, 999万, 9.9亿
     * 
     * @param val String
     * @return
     */
    public static String formatFocus(String sVal) {
        try {
            long lVal = Long.valueOf(sVal);
            return formatFocus(lVal);

        } catch (Exception e) {
            // TODO: handle exception
        }

        return " ";
    }

    /**
     * 关注人数 9人, 99人, 999人, 9.99万, 99.9万, 999万, 9.9亿
     * 
     * @param val long
     * @return
     */
    public static String formatFocus(long val) {
        if (val < 1000) {
            return val + "";
        } else if (val >= 1000 && val < 100000) {
            double newVal = val;
            newVal = newVal / 10000;
            return mDecimalFormat2_max.format(newVal) + "万";
        } else if (val >= 100000 && val < 1000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return mDecimalFormat1_max.format(newVal) + "万";
        } else if (val >= 1000000 && val < 10000000) {
            double newVal = val;
            newVal = newVal / 10000;
            return newVal + "万";
        } else if (val >= 10000000) {
            double newVal = val;
            newVal = newVal / 100000000;
            return mDecimalFormat1_max.format(newVal) + "亿";
        }

        double newVal = val;
        newVal = newVal / 100000000;

        return newVal + "亿";
    }

    public static String formatFileSize(long lenByte) {
        BigDecimal lenByte_size = new BigDecimal(lenByte);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = lenByte_size.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
        if (returnValue > 1)
            return (returnValue + " MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = lenByte_size.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
        return (returnValue + " KB");
    }

    // return
    // -1:ver1 < ver2; 1:ver1 > ver2; 0:ver1 == ver2
    public static int compareVersion(String ver1, String ver2) {
        if (ver1 == null || ver2 == null)
            return 0;
        String[] thisParts = ver1.split("\\.");
        String[] thatParts = ver2.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? convertToInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ? convertToInt(thatParts[i]) : 0;

            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    public static String format_BK_GoodCode(int bkGoodid) {
        String sBKCode = "";
        if (IsBK(bkGoodid)) {
            sBKCode = "BK" + (bkGoodid % 10000);
        }
        return sBKCode;
    }

    public static String format_bk_GoodCode(int bkGoodid) {
        String sBKCode = "";
        if (IsBK(bkGoodid)) {
            sBKCode = "bk" + (bkGoodid % 10000);
        }
        return sBKCode;
    }

    public static boolean IsGZQH(int dwGoodsID) {
        return (dwGoodsID / 1000000 == 4);
    }

    public int AmountDiv(int dwGoodsID) {
        if (IsGZQH(dwGoodsID)) {
            return 1;
        } else {
            return 1000;
        }
    }

    public static boolean IsZQ(int dwGoodsID) {
        if (IsIndex(dwGoodsID)) {
            return false;
        }

        if (dwGoodsID < 1000000l) {
            int n6 = dwGoodsID / 100000;

            if (n6 < 3) {
                return true;
            }
        } else {
            int n4 = dwGoodsID / 10000;

            if (n4 >= 110 && n4 < 114) {
                return true;
            }
        }

        return false;
    }

    public static boolean IsIndex(int dwGoodsID) {
        return (dwGoodsID / 1000 == 0 || dwGoodsID / 10000 == 139 || dwGoodsID / 1000 == 5500);
    }

    public static int PriceDiv(int dwGoodsID) {
        if (IsGZQH(dwGoodsID)) {
            return 10;
        } else {
            return 1000;
        }
    }

    public static boolean IsAG(int dwGoodsID) {
        if ((dwGoodsID >= 600000 && dwGoodsID < 699999) || (dwGoodsID > 1000000 && dwGoodsID < 1999999 && (dwGoodsID / 10000 == 100 || dwGoodsID / 10000 == 130))) {
            return true;
        } else {
            return false;
        }
    }

    // 上海B股
    public static boolean IsSHB(int dwGoodsID) {
        return (dwGoodsID / 100000 == 9 || dwGoodsID / 100000 == 2 || dwGoodsID / 100000 == 5 || dwGoodsID / 100000 == 11 || dwGoodsID / 10000 == 103 || dwGoodsID / 1000000 == 5);
    }

    // 深圳B股
    public static boolean IsSZB(int dwGoodsID) {
        return dwGoodsID / 100000 == 12;
    }

    // 权证
    public static boolean IsQZ(int dwGoodsID) {
        return (dwGoodsID / 10000 == 58 || dwGoodsID / 10000 == 103);
    }

    // 港股
    public static boolean IsHK(int dwGoodsID) {
        return (dwGoodsID / 1000000 == 3 || dwGoodsID / 1000000 == 5);
    }

    public static boolean IsBK(int dwGoodsID) {
        return (dwGoodsID / 1000 >= 2001 && dwGoodsID / 1000 <= 2003);
    }

    public static boolean IsHYBK(int dwGoodsID) {
        return (dwGoodsID / 1000 == 2002);
    }

    public static boolean IsDQBK(int dwGoodsID) {
        return (dwGoodsID / 1000 == 2003);
    }

    public static boolean IsGNBK(int dwGoodsID) {
        return (dwGoodsID / 1000 == 2001);
    }

    public static boolean IsJiJin(int dwGoodsID) {
        // 沪市基金
        if (dwGoodsID / 1000000l == 0) {
            // int nHead = dwGoodsID/100000;
            if (dwGoodsID / 100000 == 5 && dwGoodsID / 10000 != 58) {
                return true;
            }
        }
        // 深市基金
        else if (dwGoodsID / 1000000l == 1) {
            int n5 = dwGoodsID / 10000;

            if (n5 >= 115 && n5 <= 118) {
                return true;
            }
        }

        return false;
    }

    public static int GetDotType(int dwGoodsID) {
        if (IsGZQH(dwGoodsID))
            return 2;
        else if (IsSHB(dwGoodsID)/* || dwGoodsID/100000==7 */)
            return 1;
        else
            return 0;
    }

    // 判断是否是指数
    public static boolean IsZS(int nGoodsID) {
        return ((nGoodsID > 0 && nGoodsID < 9000) || nGoodsID / 100000 == 8 || nGoodsID / 10000 == 139 || nGoodsID == 5500001);
    }

    public static int VolMulti(int dwGoodsID) {
        if (IsGZQH(dwGoodsID) || IsIndex(dwGoodsID))
            return 1;
        else if (IsZQ(dwGoodsID)) {
            if (dwGoodsID < 1000000)
                return 1;
            else
                return 10;
        } else
            return 100;
    }

    public static int GetPriceDivide(int dwGoodsID) {
        if (IsSHB(dwGoodsID) || IsIndex(dwGoodsID) || IsBK(dwGoodsID) || IsGZQH(dwGoodsID)/*
                                                                                           * ||
                                                                                           * dwGoodsID
                                                                                           * /
                                                                                           * 100000
                                                                                           * == 7
                                                                                           */)
            return 1;
        else
            return 10;
    }

    public static String getHSL(float hsl) {
        hsl = hsl / 100;
        return mHSLFormat.format(hsl) + "%";
    }

    public static String getHSL(String hsl) {
        return getHSL(convertToInt(hsl));
    }

    /**
     * 格式化换手率
     * */
    public static String formatHsl(String hsl) {
        // 将传入数字字符串除以100，格式化为两位小数返回
        double value = convertToDouble(hsl);
        value /= 100;

        return mHSLFormat.format(value) + "%";
    }

    /**
     * 格式化涨跌幅
     * */
    public static String formatZdf(String zdf) {
        if (!TextUtils.isEmpty(zdf)) {
            double value = convertToDouble(zdf);
            value = value / 100;
            return mZDFFormat.format(value) + "%";
        }

        return zdf;
    }

    public static String getSJL(float sjl) {
        sjl = sjl / 100;
        return mHSLFormat.format(sjl);
    }

    public static String getSJL(String sjl) {
        return getSJL(convertToInt(sjl));
    }

    public static String getSYL(float syl) {
        syl = syl / 100;
        return mHSLFormat.format(syl);
    }

    public static String getSYL(String syl) {
        return getSYL(convertToInt(syl));
    }

    public static String getZDF(DecimalFormat df, float zdf) {
        zdf = zdf / 100;
        return df.format(zdf) + "%";
    }

    public static String getZDF(float zdf) {
        return getZDF(mZDFFormat, zdf);
    }

    public static String getZDF(String zdf) {
        return getZDF(convertToInt(zdf));
    }

    public static String getZDF(DecimalFormat df, String zdf) {
        return getZDF(df, convertToInt(zdf));
    }

    /**
     * 获取财务报表中的比率 原值除以10000加上百分号
     * */
    public static String getFinancialRate(String rate) {
        return mZDFFormat.format(convertToDouble(rate) / 10000) + "%";
    }

    /**
     * 获取财务报表中的每股数值 原值除以10000
     * */
    public static String getFinancialValue(String value) {
        return mDecimalFormat3.format(convertToDouble(value) / 10000);
    }

    /**
     * 获取财务报表中的数值
     * */
    public static String getFinancialAmount(String amount) {
        return formatAmount(convertToLong(amount) / 10000);
    }

    public static String getSignedZDF(float zdf) {
        zdf = zdf / 100;
        if (zdf > 0) {
            return "+" + mZDFFormat.format(zdf) + "%";
        } else {
            return mZDFFormat.format(zdf) + "%";
        }

    }

    public static String getGravity(String gravity) {
        return getGravity(convertToFloat(gravity));
    }

    public static String getGravity(float gravity) {
        gravity = gravity * 100;
        if (gravity >= 1 || gravity == 0) {
            return mDecimalFormat.format(gravity) + "%";
        } else {
            return mDecimalFormat1.format(gravity) + "%";
        }
    }

    public static String getTransferGravity(float gravity) {
        gravity = gravity / 100;
        return mDecimalFormat.format(gravity) + "%";
    }

    public static float getGravityFloat(float gravity) {
        String s;
        if (gravity >= 0.01) {
            s = mDecimalFormat2.format(gravity);
        } else {
            s = mDecimalFormat3.format(gravity);
        }
        return convertToFloat(s);
    }

    public static String getSignedZDF(String zdf) {
        return getSignedZDF(convertToInt(zdf));
    }

    public static String getZD(float zd) {
        zd = zd / 1000;
        return mZDFormat.format(zd);
    }

    public static String getZD(String zd) {
        return getZD(convertToInt(zd));
    }

    public static String getSignedZD(float zd) {
        zd = zd / 1000;
        if (zd > 0) {
            return "+" + mZDFormat.format(zd);
        } else {
            return mZDFormat.format(zd);
        }

    }

    public static String getSignedZD(String zd) {
        return getZD(convertToInt(zd));
    }

    public static String getPrice(float price) {
        price = price / 1000;
        return mPriceFormat.format(price);
    }

    public static String getPrice(String price) {
        return getPrice(convertToInt(price));
    }

    /**
     * 格式化价格 输入多少，返回多少，不做大小变换，只做格式变换
     * */
    public static String formatPrice(String price) {
        return mPriceFormat.format(convertToDouble(price));
    }
    
    public static String formatPrice(double price) {
        return mPriceFormat.format(price);
    }

    public static String formatEmail2Shade(String sEmail) {
        String shadeEmail = "";
        int index = sEmail.indexOf("@");
        int len = sEmail.length();

        shadeEmail = sEmail.substring(0, 1) + "***" + sEmail.substring(index - 1, len);

        return shadeEmail;
    }

    public static String formatPhone2Shade(String sPhone) {
        String shadePhone = "";
        int len = sPhone.length();

        shadePhone = sPhone.substring(0, 3) + "****" + sPhone.substring(7, len);

        return shadePhone;
    }

    /**
     * 用户名(Email或手机号)的掩码
     * 
     * @param usrName
     * @return
     */
    public static String formatUserNameShade(String usrName) {
        if (usrName == null || usrName.trim().length() <= 0) {
            return usrName;
        }

        if (RegularExpressionUtil.isEmail(usrName)) {
            usrName = DataUtils.formatEmail2Shade(usrName);
        } else if (RegularExpressionUtil.isMobilePhoneNum(usrName)) {
            usrName = DataUtils.formatPhone2Shade(usrName);
        }

        return usrName;
    }

    /**
     * string转int
     * 
     * @param str
     * @return
     */
    public static int convertToInt(String str) {
        int iRet = 0;
        try {
            iRet = Integer.parseInt(str);
        } catch (Exception e) {
        }
        return iRet;
    }

    /**
     * string转long
     * 
     * @param str
     * @return
     */
    public static long convertToLong(String str) {
        long iRet = 0;
        try {
            iRet = Long.parseLong(str);
        } catch (Exception e) {
        }
        return iRet;
    }

    /**
     * string转float
     * 
     * @param str
     * @return
     */
    public static float convertToFloat(String str) {
        float fRet = 0;
        try {
            fRet = Float.parseFloat(str);
        } catch (Exception e) {
        }
        return fRet;
    }

    /**
     * string转double
     * 
     * @param value
     * @return
     */
    public static double convertToDouble(String value) {
        double result = 0d;

        try {
            result = Double.parseDouble(value);
        } catch (Exception e) {
        }

        return result;
    }

}
