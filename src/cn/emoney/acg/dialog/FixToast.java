package cn.emoney.acg.dialog;

import android.content.Context;
import android.widget.Toast;
import cn.emoney.acg.ACGApplication;

public class FixToast {
    public static final int TIME_SHORT = 0;
    public static final int TIME_LONG = 1;

    private static Toast mToast;

    // 修改了，要确认测试
    // public static void createMsg(Context context, String msg, int time) {
    // if (mToast != null) {
    // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    // mToast.cancel();
    // }
    // } else {
    // mToast = Toast.makeText(context, msg, time);
    // }
    //
    // mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    // mToast.show();
    // }

    public static void createMsg(Context context, String msg, int time) {
        // toast.cancel();
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void createMsg(String msg) {
        Toast.makeText(ACGApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }
}
