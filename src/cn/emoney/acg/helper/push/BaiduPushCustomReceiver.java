package cn.emoney.acg.helper.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.module.SecurityHome;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSONObject;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushMessageReceiver;

/*
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；onMessage用来接收透传消息；
 * onSetTags、onDelTags、onListTags是tag相关操作的回调；onNotificationClicked在通知被点击时回调；
 * onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下：0 - Success10001 - Network Problem10101 Integrate Check Error30600 - Internal
 * Server Error30601 - Method Not Allowed30602 - Request Params Not Valid30603 - Authentication
 * Failed30604 - Quota Use Up Payment Required30605 -Data Required Not Found30606 - Request Time
 * Expires Timeout30607 - Channel Token Timeout30608 - Bind Relation Not Found30609 - Bind Number
 * Too Many
 * 
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 */
public class BaiduPushCustomReceiver extends PushMessageReceiver {
    private static int mCurNotificationID = 0;
    private RedPointNoticeManager mRedpointManager = null;

    /**
     * 调用PushManager.startWork后，sdk将对push server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。
     * 如果您需要用单播推送，需要把这里获取的channel id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     *
     * @param context BroadcastReceiver的执行Context
     * @param errorCode 绑定接口返回值，0 - 成功
     * @param appid 应用id。errorCode非0时为null
     * @param userId 应用user id。errorCode非0时为null
     * @param channelId 应用channel id。errorCode非0时为null
     * @param requestId 向服务端发起的请求id。在追查问题时有用；
     * @return none
     */
    @Override
    public void onBind(Context context, int errorCode, String appid, String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid=" + appid + " userId=" + userId + " channelId=" + channelId + " requestId=" + requestId;
        if (errorCode == 0) {
            // 绑定成功,列举tag

            DataModule.G_BAIDU_PUSH_CHANNELID = channelId;
            GlobalDBHelper dbHelper = new GlobalDBHelper(ACGApplication.getInstance(), DataModule.DB_GLOBAL);
            dbHelper.setString(DataModule.G_KEY_BAIDU_PUSH_CHANNELID, channelId);

            BaiduPushManager_v2.listTags();
        }
        LogUtil.easylog("************onBind:" + responseString);
    }

    /**
     * 接收透传消息的函数。备注： 格式：{"data":{"title":"1111","inst":"11111"},"type":1}
     * 
     * @param context 上下文
     * @param message 推送的消息
     * @param customContentString 自定义内容,为空或者json字符串
     */
    @Override
    public void onMessage(Context context, String message, String customContentString) {
        String messageString = "message=\"" + message + "\" customContentString=" + customContentString;
        // 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
        // if (!TextUtils.isEmpty(customContentString)) {
        // JSONObject customJson = null;
        // try {
        // customJson = new JSONObject(customContentString);
        // String myvalue = null;
        // if (!customJson.isNull("mykey")) {
        // myvalue = customJson.getString("mykey");
        // }
        // } catch (JSONException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }

        processCustomMessage(context, message);
        LogUtil.easylog("************onMessage:" + messageString);
    }

    /**
     * setTags() 的回调函数。
     *
     * @param context 上下文
     * @param errorCode 错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param successTags 设置成功的tag
     * @param failTags 设置失败的tag
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onSetTags(Context context, int errorCode, List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode + " sucessTags=" + sucessTags + " failTags=" + failTags + " requestId=" + requestId;
        LogUtil.easylog("************push onSetTags:" + responseString);
        if (errorCode == 0) {
            // 成功
        }
    }

    /**
     * delTags() 的回调函数。
     *
     * @param context 上下文
     * @param errorCode 错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param successTags 成功删除的tag
     * @param failTags 删除失败的tag
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onDelTags(Context context, int errorCode, List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode + " sucessTags=" + sucessTags + " failTags=" + failTags + " requestId=" + requestId;
        LogUtil.easylog("************push onDelTags:" + responseString);
        if (errorCode == 0) {
            // 成功
        }
    }

    /**
     * listTags() 的回调函数。
     *
     * @param context 上下文
     * @param errorCode 错误码。0表示列举tag成功；非0表示失败。
     * @param tags 当前应用设置的所有tag。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags=" + tags;
        LogUtil.easylog("************push onListTags:" + responseString);

        if (errorCode == 0) {
            if (tags != null) {

                Map<String, Integer> switchMap = new HashMap<String, Integer>(4);
                switchMap.put(BaiduPushManager_v2.KEY_MAIN_SWITCH, BaiduPushManager_v2.VALUE_SWITCH_ON);
                switchMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, BaiduPushManager_v2.VALUE_SWITCH_ON);
                switchMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, BaiduPushManager_v2.VALUE_SWITCH_ON);
                switchMap.put(BaiduPushManager_v2.KEY_GROUP, BaiduPushManager_v2.VALUE_SWITCH_ON);
                for (String tag : tags) {
                    if (tag.startsWith(BaiduPushManager_v2.KEY_PRE_LOCDATA)) {
                        String[] switchInfo = tag.split(":");
                        if (switchInfo != null && switchInfo.length == 2) {
                            String key = switchInfo[0].substring(BaiduPushManager_v2.KEY_PRE_LOCDATA.length());
                            int value = DataUtils.convertToInt(switchInfo[1]);
                            switchMap.put(key, value);
                        }
                    }
                }

                BaiduPushManager_v2.saveLocSwitcher(switchMap, false);
            }
        }
    }

    /**
     * PushManager.stopWork() 的回调函数。
     *
     * @param context 上下文
     * @param errorCode 错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode + " requestId = " + requestId;
        if (errorCode == 0) {
            // 解绑定成功
        }
        LogUtil.easylog("************onUnbind:" + responseString);
    }

    /**
     * 接收通知点击的函数。
     *
     * @param context 上下文
     * @param title 推送的通知的标题
     * @param description 推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        String notifyString = "通知点击 title=\"" + title + "\" description=\"" + description + "\" customContent=" + customContentString;
        LogUtil.easylog("************onNotificationClicked:" + notifyString);

        // // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        // if (!TextUtils.isEmpty(customContentString)) {
        // JSONObject customJson = null;
        // try {
        // customJson = new JSONObject(customContentString);
        // String myvalue = null;
        // if (!customJson.isNull("mykey")) {
        // myvalue = customJson.getString("mykey");
        // }
        // } catch (JSONException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
    }

    /**
     * 接收通知到达的函数。
     *
     * @param context 上下文
     * @param title 推送的通知的标题
     * @param description 推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        String notifyString = "onNotificationArrived  title=\"" + title + "\" description=\"" + description + "\" customContent=" + customContentString;
        LogUtil.easylog("************onNotificationArrived:" + notifyString);

        // // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        // if (!TextUtils.isEmpty(customContentString)) {
        // JSONObject customJson = null;
        // try {
        // customJson = new JSONObject(customContentString);
        // String myvalue = null;
        // if (!customJson.isNull("mykey")) {
        // myvalue = customJson.getString("mykey");
        // }
        // } catch (JSONException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
    }

    private void processCustomMessage(Context context, String message) {
        try {
            // 备注： 格式：{"data":{"title":"1111","inst":"11111"},"type":1}
            JSONObject jObjectMsg = JSONObject.parseObject(message);

            int type = -1;

            // {"type":2,"data":{"title":"xx","inst":"xx"}}
            if (jObjectMsg.containsKey("type")) {
                type = jObjectMsg.getIntValue("type");
            }

            if (type == 1) {
                // 系统消息
                if (!DataModule.G_APP_IS_ACTIVE_FOREGROUND) {
                    if (jObjectMsg.containsKey("data")) {
                        JSONObject jobjData = jObjectMsg.getJSONObject("data");
                        String title = jobjData.getString("title");
                        String summary = jobjData.getString("inst");
                        if (title != null) {
                            createMsgNotifycation(context, title, summary, null);
                        }
                    }
                }
            } else if (type == 3) {// 问股
                if (!DataModule.G_APP_IS_ACTIVE_FOREGROUND) {
                    if (jObjectMsg.containsKey("data")) {
                        // JSONObject jobjData = jObjectMsg.getJSONObject("data");
                        // createQuizNotifycation(context, 1, "用户有新问股等你解答哦",
                        // "android.resource://cn.emoney.acg/raw/audio_quiz_push_teacher");
                    }
                }
            } else if (type == 4) {// 个股预警
                // {"msg_content":"600588用友网络，最新价达到14.10。","extras":
                if (jObjectMsg.containsKey("data")) {
                    JSONObject jobjData = jObjectMsg.getJSONObject("data");
                    String title = jobjData.getString("title");

                    if (title != null) {
                        createMsgNotifycation(context, "个股预警", title, null);
                    }
                }
            } else if (type == 5) {// 买吧调仓
                if (jObjectMsg.containsKey("data")) {
                    JSONObject jobjData = jObjectMsg.getJSONObject("data");
                    String title = jobjData.getString("title");

                    if (title != null) {
                        createMsgNotifycation(context, "买吧操作", title, null);
                    }
                }
            }

            if (mRedpointManager == null) {
                mRedpointManager = new RedPointNoticeManager(context);
            }
            mRedpointManager.request();

        } catch (Exception e) {
        }
    }

    @SuppressLint("NewApi")
    private void createMsgNotifycation(Context context, String title, String summary, String soundUri) {
        int enablePush = BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_MAIN_SWITCH);

        if (enablePush == BaiduPushManager_v2.VALUE_SWITCH_OFF) {
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long when = System.currentTimeMillis();
        Resources res = context.getResources();

        Intent notificationIntent = new Intent(context, SecurityHome.class);

        PendingIntent contentIntent = PendingIntent.getActivity(context, mCurNotificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.img_push_title_icon)// 设置状态栏里面的图标（小图标）
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.img_push_icon))// 下拉下拉列表里面的图标（大图标）
                .setWhen(when)// 设置时间发生时间
                .setAutoCancel(true)// 设置可以清除
                .setTicker("爱炒股:" + title) // 设置状态栏的显示的信息
                .setContentTitle(title)// 设置下拉列表里的标题
                .setContentText(summary);// 设置上下文内容
        Notification n = builder.build();// 获取一个Notification

        // android.resource://cn.emoney.acg/raw/audio_quiz_push_teacher
        if (soundUri != null && soundUri.startsWith("android.resource://cn.emoney.acg/raw/")) {
            n.sound = Uri.parse(soundUri);
        } else {
            n.defaults = Notification.DEFAULT_SOUND;// 设置为默认的声音
        }

        mNotificationManager.notify(mCurNotificationID++, n);// 显示通知 break;
    }
}
