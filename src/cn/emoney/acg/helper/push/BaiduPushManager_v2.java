package cn.emoney.acg.helper.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;


public class BaiduPushManager_v2 {
    public static final String KEY_PRE_LOCDATA = "key_baidu_push_loc_";
    public static final String KEY_MAIN_SWITCH = "push";
    public static final String KEY_SYSTEM_INFO = "msg";
    public static final String KEY_STOCK_ALERT = "alarm";
    public static final String KEY_GROUP = "zuhe";

    public static final int VALUE_SWITCH_ON = 1;
    public static final int VALUE_SWITCH_OFF = 0;


    // 备注：TAG_GUEST_MSG_ON，TAG_MEMBER_MSG_ON设给百度，最多只能设置其中一个,OFF则删除ON的值
    public static final String TAG_GUEST_MSG_ON = "GUEST_MSG_ON";// 游客，消息-开
    public static final String TAG_MEMBER_MSG_ON = "MEMBER_MSG_ON";// 登录用户，消息-开

    private static Context mContext = null;

    /**
     * 初始化 JPush
     * 
     * @param context
     */
    public static void onStartWork(Context context) {
        mContext = context;
        PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, Util.getMetaValue("api_key"));
    }

    /**
     * 停止
     */
    public static void onStopWork() {
        if (mContext == null) {
            return;
        }
        LogUtil.easylog("PushManager->stopWork");
        PushManager.stopWork(mContext);
    }

    /**
     * 重启
     */
    public static void onResumeWork() {
        if (mContext == null) {
            return;
        }
        LogUtil.easylog("PushManager->resumeWork");
        PushManager.resumeWork(mContext);
    }

    /**
     * 设置开启日志,发布时请关闭日志
     * 
     * @param b
     */
    public static void setDebugModule(boolean b) {
        PushSettings.enableDebugMode(ACGApplication.getInstance(), b);
    }

    /**
     * 列举tags
     */
    public static void listTags() {
        if (mContext == null) {
            return;
        }

        PushManager.listTags(mContext);
    }

    /**
     * 更新Tag { "msg": 0 / 1, "alarm": 0 / 1, "zuhe":0 / 1 }
     */
    public static void updateTags(Map<String, Integer> tMap) {
        if (tMap == null || tMap.size() == 0) {
            return;
        }
        updateTagToBaidu(tMap);

        if (tMap.containsKey(KEY_SYSTEM_INFO)) {
            tMap.remove(KEY_SYSTEM_INFO);
        }
        if (tMap.size() > 0) {
            updateTagToServer(tMap);
        }
    }

    private static void updateTagToServer(Map<String, Integer> tagMap) {
        BaiduPushNetworkHelper helper = BaiduPushNetworkHelper.getInstance(mContext);
        helper.requestCommitTags(tagMap);
    }

    private static void updateTagToBaidu(Map<String, Integer> tagMap) {
        if (tagMap == null || tagMap.size() == 0) {
            return;
        }
        if (tagMap.containsKey(KEY_SYSTEM_INFO)) {
            int value = tagMap.get(KEY_SYSTEM_INFO);
            if (value == VALUE_SWITCH_OFF) {
                List<String> lstTag = new ArrayList<String>(2);
                lstTag.add(TAG_GUEST_MSG_ON);
                lstTag.add(TAG_MEMBER_MSG_ON);
                PushManager.delTags(mContext, lstTag);
            } else {
                boolean bLogined = DataModule.getInstance().getUserInfo().isLogined();
                if (bLogined) {
                    PushManager.delTags(mContext, getTagList(TAG_GUEST_MSG_ON));
                    PushManager.setTags(mContext, getTagList(TAG_MEMBER_MSG_ON));
                } else {
                    PushManager.delTags(mContext, getTagList(TAG_MEMBER_MSG_ON));
                    PushManager.setTags(mContext, getTagList(TAG_GUEST_MSG_ON));
                }
            }
        }

    }


    private static List<String> getTagList(String tag) {
        List<String> list = new ArrayList<String>(1);
        list.add(tag);
        return list;
    }


    /**
     * 获取本地开关状态,包含总开关逻辑
     * 
     * @param key
     * @return -1表示出错
     */
    public static int getLocSwitchState(String key) {

        int enablePush = BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_MAIN_SWITCH);

        return enablePush == VALUE_SWITCH_ON ? getLocSwitchStateSelf(key) : VALUE_SWITCH_OFF;
    }

    /**
     * 获取本地开关状态 自身状态,不包含总开关逻辑
     * 
     * @param key
     * @return -1表示出错
     */
    public static int getLocSwitchStateSelf(String key) {
        if (TextUtils.isEmpty(key)) {
            return -1;
        }
        GlobalDBHelper dbHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
        int state = dbHelper.getInt(KEY_PRE_LOCDATA + key, VALUE_SWITCH_ON);
        return state;
    }

    /**
     * 设置本地开关状态,并同步到服务器
     * 
     * @param tagMap
     */
    public static void saveLocSwitcher(Map<String, Integer> tagMap) {
        saveLocSwitcher(tagMap, true);
    }

    /**
     * 设置本地开关状态
     * 
     * @param tagMap
     */
    public static void saveLocSwitcher(Map<String, Integer> tagMap, boolean bSync) {
        if (tagMap == null || tagMap.size() == 0) {
            return;
        }
        // 更新本地数据
        Iterator<String> iterator = tagMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            saveLocSwitcher(key, tagMap.get(key), bSync);
        }
    }


    /**
     * 设置本地开关状态,并保存到服务器
     * 
     * @param key value
     */

    public static void saveLocSwitcher(String key, int value) {
        saveLocSwitcher(key, value, true);
    }

    /**
     * 设置本地开关状态
     * 
     * @param key value
     * @param bSync 是否同步到服务器
     */
    public static void saveLocSwitcher(String key, int value, boolean bSync) {
        LogUtil.easylog("BaiduManager->saveLocSwitcher->key:" + key + ", value:" + value + ", bSync:" + bSync);
        if (TextUtils.isEmpty(key)) {
            return;
        }
        // 更新本地数据
        GlobalDBHelper dbHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
        String saveKey = KEY_PRE_LOCDATA + key;
        dbHelper.setInt(saveKey, value);

        if (bSync) {
            if (value == VALUE_SWITCH_OFF) {
                LogUtil.easylog("BaiduManager->VALUE_SWITCH_OFF->add key:" + (saveKey + ":0"));
                PushManager.setTags(mContext, getTagList(saveKey + ":0"));
                LogUtil.easylog("push BaiduManager->VALUE_SWITCH_OFF->del key:" + (saveKey + ":1"));
                PushManager.delTags(mContext, getTagList(saveKey + ":1"));

            } else {
                LogUtil.easylog("push BaiduManager->VALUE_SWITCH_ON->add key:" + (saveKey + ":1"));
                PushManager.setTags(mContext, getTagList(saveKey + ":1"));
                LogUtil.easylog("push BaiduManager->VALUE_SWITCH_ON->del key:" + (saveKey + ":0"));
                PushManager.delTags(mContext, getTagList(saveKey + ":0"));

            }
        }
    }

    public static void setOnline() {
        Map<String, Integer> tTagMap = new HashMap<String, Integer>(3);
        tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_SYSTEM_INFO));
        tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_STOCK_ALERT));
        tTagMap.put(BaiduPushManager_v2.KEY_GROUP, BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_GROUP));
        updateTags(tTagMap);
    }

    public static void setOffLine() {
        Map<String, Integer> tTagMap = new HashMap<String, Integer>(3);
        tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_SYSTEM_INFO));
        tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, VALUE_SWITCH_OFF);
        tTagMap.put(BaiduPushManager_v2.KEY_GROUP, VALUE_SWITCH_OFF);
        updateTags(tTagMap);
    }



}
