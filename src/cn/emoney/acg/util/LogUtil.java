package cn.emoney.acg.util;

import android.text.TextUtils;
import cn.emoney.acg.BuildConfig;
import cn.emoney.acg.data.DataModule;

/**
 * Log工具，类似android.util.Log。 tag自动产生，格式: customTagPrefix:className.methodName(L:lineNumber),
 * customTagPrefix为空时只输出：className.methodName(L:lineNumber)。
 */
public class LogUtil {
    public static String customTagPrefix = "";

    /**
     * <p>
     * 在android中不同取值的打印结果 我们需要3
     * <p>
     * 0 VMStack.getThreadStackTrace(L:-2)
     * <p>
     * 1 Thread.getStackTrace(L:591)
     * <p>
     * 2 MyLog.i(L:136)
     * <p>
     * 3 MainActivity.onCreate(L:61)
     * <p>
     * 4 Activity.performCreate()
     * <p>
     * 5 Instrumentation.callActivityOnCreate(L:1049)
     */
    private static final int indexStack = 3;

    /**
     * log 总开关标志
     */
    // public static final boolean isTrue = true;

    private LogUtil() {}

    private static String generateTag(StackTraceElement caller) {
        // 例: cn.emoney.acg.page.MainPage$3.run(MainPage.java:252)
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static void easylog(String content) {
        LogUtil.easylog("", content);
    }

    public static void easylog(String tag, String content) {
        if (!BuildConfig.DEBUG && !DataModule.G_USER_DEBUG)
            return;

        StackTraceElement caller = Thread.currentThread().getStackTrace()[indexStack];
        String pathtag = generateTag(caller);
        System.out.println(pathtag + "-> [" + tag + "]:" + content);
    }


    public static boolean isDebug() {
        return BuildConfig.DEBUG && DataModule.G_USER_DEBUG;
    }
}
