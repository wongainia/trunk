package cn.emoney.acg.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.emoney.acg.data.DataModule;

public class DateUtils {
    public static SimpleDateFormat mFormatDayFull = new SimpleDateFormat("MM月dd日 HH:mm:ss");
    public static SimpleDateFormat mFormatDayHM = new SimpleDateFormat("MM月dd日 HH:mm");
    public static SimpleDateFormat mFormatM_D_H_M = new SimpleDateFormat("MM-dd HH:mm");
    public static SimpleDateFormat mFormatHM = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat mFormatDay = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat mFormatYearDotMonth = new SimpleDateFormat("yyyy.MM");
    public static SimpleDateFormat mFormatDD = new SimpleDateFormat("dd");
    public static SimpleDateFormat mFormatDayY_M_D = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat mFormatDayM_D = new SimpleDateFormat("MM-dd");
    public static SimpleDateFormat FormatInt = new SimpleDateFormat("yyyyMMdd-hhmmss");
    public static SimpleDateFormat mFormatDotDay = new SimpleDateFormat("yyyy.MM.dd");
    public static SimpleDateFormat mFormatHHmmss = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat mFormatHHmmWithUnit = new SimpleDateFormat("HH时mm分");

    private static SimpleDateFormat FormatFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public DateUtils() {
        // TODO Auto-generated constructor stub
    }

    public static String formatInfoDate(String d, SimpleDateFormat format) {
        try {
            Date date = FormatFull.parse(d);
            return format.format(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d;
    }

    public static String formatTrendDate(String d) {
        if (d == null || d.equals("")) {
            return "";
        }
        try {
            Date date = mFormatDay.parse(d);
            return mFormatDotDay.format(date);
        } catch (Exception e) {
        }

        return "";
    }

    public static String daysBefore(String d) {
        long days = 0;
        try {
            Date date1 = FormatFull.parse(d);
            Date date2 = Calendar.getInstance().getTime();
            Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
            long diff1 = date1.getTime() - orgiDate.getTime();
            long diff2 = date2.getTime() - orgiDate.getTime();
            long t_days1 = diff1 / (3600000 * 24);
            long t_days2 = diff2 / (3600000 * 24);

            days = t_days2 - t_days1;

            if (days == 0) {
                String[] t_sFullTime = d.split(" ");
                String[] t_sTime = t_sFullTime[1].split(":");

                return t_sTime[0] + ":" + t_sTime[1];
            } else {
                return days + "天前";
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取当前时间，并以指定的格式返回 yyyy-MM-dd HH:mm:ss
     * */
    public static String getCurrentTimeByFormat(String format) {
        Date d = new Date();
        SimpleDateFormat formator = new SimpleDateFormat(format);
        String currentTime = formator.format(d);
        return currentTime;
    }

    /**
     * 将时间戳转换为时分秒(HH:mm:ss)格式的字符串 unix timestamp 精确到秒
     * */
    public static String convertTimestampToHHmmss(long timestamp, boolean isUnixTimestamp) {
        if (isUnixTimestamp) {
            timestamp = timestamp * 1000;
        }

        Date date = new Date(timestamp);

        return mFormatHHmmss.format(date);
    }

    /**
     * 已关注多少天
     * 
     * @param 2014-10-14 14:29:39.000
     * @return
     */
    public static String daysFocused(String d) {

        long days = 0;
        try {
            Date date1 = FormatFull.parse(d);
            Date date2 = Calendar.getInstance().getTime();
            Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
            long diff1 = date1.getTime() - orgiDate.getTime();
            long diff2 = date2.getTime() - orgiDate.getTime();
            long t_days1 = diff1 / (3600000 * 24);
            long t_days2 = diff2 / (3600000 * 24);

            days = t_days2 - t_days1;

            return "已关注" + (days + 1) + "天";
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "已关注1天";

    }

    /**
     * 买吧个股关注时间格式化 (当天:今天 hh:mm; 昨天:昨天 hh:mm; 其它:mm-dd hh:mm )
     * 
     * @param 2014-10-14 14:29:39.000
     * @return (当天:今天 hh:mm; 昨天:昨天 hh:mm; 其它:mm-dd hh:mm)
     */
    public static String groupCreateDay(String d) {

        long days = 0;
        try {
            Date date1 = FormatFull.parse(d);
            Date date2 = Calendar.getInstance().getTime();
            Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
            long diff1 = date1.getTime() - orgiDate.getTime();
            long diff2 = date2.getTime() - orgiDate.getTime();
            long t_days1 = diff1 / (3600000 * 24);
            long t_days2 = diff2 / (3600000 * 24);

            days = t_days2 - t_days1;

            if (days <= 0) {
                return "今天\n" + formatInfoDate(d, mFormatHM);
            } else if (days == 1) {
                return "昨天\n " + formatInfoDate(d, mFormatHM);
            } else {
                return formatInfoDate(d, mFormatDayM_D) + "\n" + formatInfoDate(d, mFormatHM);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "- -";

    }



    /**
     * 问股时间格式化
     * 
     * @param fullTime 2014-10-14 14:29:39.000
     * @return
     */
    public static String formatQuizCommitTime(String fullTime) {
        String sRet = "";

        Date date = null;
        try {
            date = FormatFull.parse(fullTime);
        } catch (Exception e) {
        }
        if (date == null) {
            return sRet;
        }

        Date dateCurrent = Calendar.getInstance().getTime();
        Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
        long diff1 = date.getTime() - orgiDate.getTime();
        long diff2 = dateCurrent.getTime() - (DataModule.G_LOCAL_SERVER_TIME_GAP * 1000) - orgiDate.getTime();
        long t_days1 = diff1 / (3600000 * 24);
        long t_days2 = diff2 / (3600000 * 24);

        long days = t_days2 - t_days1; // 计算出参数的日期与当前日期的差距天数, 0为当天, 1为昨天, 2为前天 .....
                                       // 例如23日23:59分 与24日00:01 相差1,为昨天

        long curTimeStamp = dateCurrent.getTime() / 1000 - DataModule.G_LOCAL_SERVER_TIME_GAP;
        long d = curTimeStamp - date.getTime() / 1000; // 计算出参数时间与当前时期的绝对差值 单位 秒
        int dMinute = (int) (d / 60);
        if (dMinute == 0) { // [0,1) 分钟
            sRet = "刚刚";
        } else if (dMinute < 60) { // [1,60) 分钟
            sRet = dMinute + "分钟前";
        } else { // [1, max) 小时
            int dHour = (int) (d / 3600);
            if (dHour < 12) { // [1,12) 小时
                sRet = dHour + "小时前";
            } else { // [12, max)小时
                if (days == 0) {// 今天
                    sRet = dHour + "小时前";
                } else if (days == 1) { // 昨天
                    sRet = "昨天";
                } else if (days <= 30) { // 30天内(含30天)
                    sRet = days + "天前";
                } else if (days > 30) { // 30天以外 显示月-日
                    sRet = mFormatDayM_D.format(date);
                }
            }
        }

        return sRet;
    }


    /**
     * 问股时间格式化
     * 
     * @param timeStamp 时间戳
     * @return
     */
    public static String formatQuizCommitTime(long timeStamp) {
        String sRet = "";

        if (timeStamp <= 0) {
            return sRet;
        }

        Date dateCurrent = Calendar.getInstance().getTime();
        Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
        long diff1 = timeStamp * 1000 - orgiDate.getTime();
        long diff2 = dateCurrent.getTime() - (DataModule.G_LOCAL_SERVER_TIME_GAP * 1000) - orgiDate.getTime();
        long t_days1 = diff1 / (3600000 * 24);
        long t_days2 = diff2 / (3600000 * 24);

        long days = t_days2 - t_days1; // 计算出参数的日期与当前日期的差距天数, 0为当天, 1为昨天, 2为前天 .....
                                       // 例如23日23:59分 与24日00:01 相差1,为昨天

        long curTimeStamp = dateCurrent.getTime() / 1000 - DataModule.G_LOCAL_SERVER_TIME_GAP;
        long d = curTimeStamp - timeStamp; // 计算出参数时间与当前时期的绝对差值 单位 秒
        int dMinute = (int) (d / 60);
        if (dMinute == 0) { // [0,1) 分钟
            sRet = "刚刚";
        } else if (dMinute < 60) { // [1,60) 分钟
            sRet = dMinute + "分钟前";
        } else { // [1, max) 小时
            int dHour = (int) (d / 3600);
            if (dHour < 12) { // [1,12) 小时
                sRet = dHour + "小时前";
            } else { // [12, max)小时
                if (days == 0) {// 今天
                    sRet = dHour + "小时前";
                } else if (days == 1) { // 昨天
                    sRet = "昨天";
                } else if (days <= 30) { // 30天内(含30天)
                    sRet = days + "天前";
                } else if (days > 30) { // 30天以外 显示月-日
                    Date tDate = new Date(timeStamp * 1000);
                    sRet = mFormatDayM_D.format(tDate);
                }
            }

        }



        return sRet;
    }



    /**
     * 创建于n天前 (当天:今天 30天内:n天前 30天外:日期)
     * 
     * @param 2014-10-14 14:29:39.000
     * @return
     */
    public static String createDay(String d) {

        long days = 0;
        try {
            Date date1 = FormatFull.parse(d);
            Date date2 = Calendar.getInstance().getTime();
            Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
            long diff1 = date1.getTime() - orgiDate.getTime();
            long diff2 = date2.getTime() - orgiDate.getTime();
            long t_days1 = diff1 / (3600000 * 24);
            long t_days2 = diff2 / (3600000 * 24);

            days = t_days2 - t_days1;

            if (days <= 0) {
                return "创建于今天";
            } else if (days <= 30) {
                return "创建于" + days + "天前";
            } else {
                return "创建于" + formatInfoDate(d, mFormatDayY_M_D);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "创建于" + formatInfoDate(d, mFormatDayY_M_D);

    }

    /**
     * 调仓时间
     * 
     * @param 2014-10-14 14:29:39.000
     * @return 今天 10:35; 昨天10:22; 06-15 11:01
     */
    public static String getTransferTime(String d) {

        long days = 0;
        try {
            Date date1 = FormatFull.parse(d);
            Date date2 = Calendar.getInstance().getTime();
            Date orgiDate = new Date(2000, 1, 1, 0, 0, 0);
            long diff1 = date1.getTime() - orgiDate.getTime();
            long diff2 = date2.getTime() - orgiDate.getTime();
            long t_days1 = diff1 / (3600000 * 24);
            long t_days2 = diff2 / (3600000 * 24);

            days = t_days2 - t_days1;

            String sH_M = formatInfoDate(d, mFormatHM);
            if (days <= 0) {
                return "今天\n" + sH_M;
            } else if (days == 1) {
                return "昨天\n" + sH_M;
            } else {
                return formatInfoDate(d, mFormatDayM_D) + "\n" + sH_M;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "- -";

    }

    // hhmm -> pos
    public static int minuteTimeToPos(int time) {
        int iRet = 0;
        int h = time / 100;
        int m = time % 100;
        if (h >= 9 && h <= 11) {
            iRet = (h * 60 + m) - 570; // 9 * 60 + 30
            iRet = iRet > 120 ? 120 : iRet;
        } else if (h >= 13 && h <= 15) {
            iRet = (h * 60 + m) - 780; // 13 * 60
            iRet = iRet > 120 ? 240 : iRet + 120;
        }

        iRet = iRet > 0 ? iRet - 1 : 0;

        return iRet;
    }

    public static String minutePosToTime(int pos) {
        String sRet = "";
        int count = pos + 1;
        if (count >= 0 && count <= 120) {
            int d_h = (int) (count + 30) / 60;
            int d_m = (int) (count + 30) % 60;

            String h = (d_h + 9) >= 10 ? "" + (d_h + 9) : "0" + (d_h + 9);
            String m = d_m >= 10 ? "" + d_m : "0" + d_m;
            sRet = h + ":" + m;
        } else if (count > 120 && count <= 240) {
            count -= 120;

            int d_h = (int) count / 60;
            int d_m = (int) count % 60;

            String h = "" + (13 + d_h);
            String m = d_m >= 10 ? "" + d_m : "0" + d_m;
            sRet = h + ":" + m;
        }

        return sRet;
    }

    public static String second2MSLable(long second) {
        String sRet = "";
        long m = second / 60;
        long s = second % 60;
        if (m > 0) {
            sRet = m + "分";
        }
        sRet += (s + "秒");

        return sRet;
    }

    public static String getCurrentQuoteDate() {
        Calendar calendar = Calendar.getInstance();
        return mFormatDayFull.format(calendar.getTime());
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return mFormatDay.format(calendar.getTime());
    }

    public static String getCurrentHM() {
        Calendar calendar = Calendar.getInstance();
        return mFormatHM.format(calendar.getTime());
    }

    public static long getTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        long timeStamp = calendar.getTimeInMillis();
        return timeStamp;
    }

    /**
     * 获取指定时间的时间戳
     * */
    public static long getTimeStamp(String time) {
        long timestamp = 0l;

        try {
            Date date = FormatFull.parse(time);
            timestamp = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    /**
     * Mills转成字符串，如：1100->0:01.10
     * 
     * @param millis
     * @return
     */
    public static String millisToSecondStr(long millis) {
        String sRet = "";
        int second = (int) (millis / 1000);
        int millisTemp = (int) (millis % 1000) / 10;
        int m = second / 60;
        int s = second % 60;
        if (m >= 0) {
            sRet = m + ":";
        }

        sRet += frontCompWithZore(s, 2) + "." + frontCompWithZore(millisTemp, 2);

        return sRet;
    }

    /**
     * Second转成字符串，如：1100->0:01
     * 
     * @param millis
     * @return
     */
    public static String secondValueToSecondStr(int second) {
        String sRet = "";
        int m = second / 60;
        int s = second % 60;
        if (m >= 0) {
            sRet = m + ":";
        }

        sRet += frontCompWithZore(s, 2);

        return sRet;
    }

    /**
     * Second转成字符串，如：1100->0"01'
     * 
     * @param millis
     * @return
     */
    public static String secondValueToSecondStrQuizList(int second) {
        String sRet = "";
        int m = second / 60;
        int s = second % 60;
        if (m >= 0) {
            sRet = m + "\"";
        }

        sRet += frontCompWithZore(s, 2) + "'";

        return sRet;
    }

    /**
     * 将元数据前补零，补后的总长度为指定的长度，以字符串的形式返回
     * 
     * @param sourceDate
     * @param formatLength
     * @return 重组后的数据
     */
    public static String frontCompWithZore(int sourceDate, int formatLength) {
        /*
         * 0 指前面补充零 formatLength 字符总长度为 formatLength d 代表为正数。
         */
        String newString = String.format("%0" + formatLength + "d", sourceDate);
        return newString;
    }
}
