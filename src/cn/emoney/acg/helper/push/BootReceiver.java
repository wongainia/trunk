package cn.emoney.acg.helper.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.emoney.acg.util.LogUtil;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.easylog("sky", "BootReceiver -> onReceive");
        context.startService(new Intent("cn.emoney.acg.service.push.AutoBootService_action"));
    }

}
