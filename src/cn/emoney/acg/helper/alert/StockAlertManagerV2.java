package cn.emoney.acg.helper.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.util.DataUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 管理股票预警数据
 * */
public class StockAlertManagerV2 implements KeysInterface, NetworkManager.InfoCallBack {

    /**
     * 个股预警最多允许设置数量
     * */
    private final int MAX_ALERT_COUNT = 20;
    public static final short TAG_REMOVE = 1;
    public static final short TAG_ADD_OR_UPDATE = 2;
    public static final short TAG_REQUEST_LIST = 101;

    NetworkManager mNetworkManager = null;

    private static StockAlertManagerV2 instance;

    private boolean mIsGetListSuccess = false;

    /**
     * 是否添加key，默认为true。 当获取预警列表返回retCode为0时，置为false. 当获取预警列表返回retCode为8时，置为true. 当为true时，request
     * update 的option为1
     * */

    /**
     * 数据版本，默认为3
     * */
    private int dataVersion = 3;

    /**
     * 存储各支股票预警数据 - HashMap格式，key为股票id，value为股票预警数据的json字符串
     * */
    private Map<String, String> mapStockAlerts = new HashMap<String, String>();
    private Map<String, String> mapStockTemp = new HashMap<String, String>();

    private Operation mCallback = null;

    private StockAlertManagerV2() {
        getNetworkManager();

    }

    public static StockAlertManagerV2 getInstance() {
        if (instance == null) {
            synchronized (StockAlertManagerV2.class) {
                if (instance == null) {
                    instance = new StockAlertManagerV2();
                }
            }
        }

        return instance;
    }

    /**
     * 清空缓存数据
     * */
    public void clearCache() {
        if (mapStockAlerts != null) {
            mapStockAlerts.clear();
        }
    }

    /**
     * 获取单股预警配置数据
     * */
    public String getStockAlert(String goodsId) {
        if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
            return mapStockAlerts.get(goodsId);
        }

        return null;
    }

    /**
     * 获取缓存中是否已缓存有预警数据
     * */
    public boolean isCacheHasData() {
        if (mapStockAlerts != null && mIsGetListSuccess) {
            return true;
        }

        return false;
    }

    /**
     * 获取已设置预警个股数量
     * */
    public int getAlertCount() {
        if (mapStockAlerts != null) {
            return mapStockAlerts.size();
        }

        return 0;
    }

    /**
     * 判断是否允许继续添加预警设置
     * */
    public boolean isSetAlertAllowed(String goodsId) {

        // 如果已缓存当前股票的预警数据，就允许修改预警设置
        if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
            return true;
        }

        // 对于没有设置过预警的个股，如果设置预警个股数量已达到数量限制，就不再允许继续设置预警
        if (getAlertCount() < MAX_ALERT_COUNT) {
            return true;
        }

        return false;
    }

    /**
     * 判断某支股票是否已设置预警
     * */
    public boolean isStockHasSetAlert(String goodsId) {
        if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
            return true;
        }

        return false;
    }

    /**
     * 删除预警 一组
     * 
     * @param lstStockIds
     * @param callback
     */
    public void removeWarns(List<String> lstStockIds, Operation callback) {
        if (lstStockIds == null || lstStockIds.size() == 0) {
            if (callback != null) {
                callback.onFail(TAG_REMOVE);
            }
            return;
        }

        mCallback = callback;

        mapStockTemp.clear();
        mapStockTemp.putAll(mapStockAlerts);

        for (String sStock : lstStockIds) {
            if (mapStockTemp.containsKey(sStock)) {
                mapStockTemp.remove(sStock);
            }
        }

        JSONArray updateAry = stockMap2ReqJAry(mapStockTemp);
        JSONObject reqValue = new JSONObject();
        try {
            reqValue.put("ver", dataVersion);
            reqValue.put("data", updateAry);
        } catch (Exception e) {
        }

        requestUpdateStockWarnList(TAG_REMOVE, reqValue.toJSONString());



    }

    /**
     * 删除预警 一只
     * 
     * @param stockId
     * @param callback
     */
    public void removeWarns(String stockId, Operation callback) {
        if (TextUtils.isEmpty(stockId)) {
            return;
        }

        List<String> lstStockIds = new ArrayList<String>(1);
        lstStockIds.add(stockId);

        removeWarns(lstStockIds, callback);

    }

    /**
     * 更新或添加预警 一组
     * 
     * @param lstStock
     * @param lstValues
     * @param callback
     */
    public void updateWarns(List<String> lstStocks, List<String> lstValues, Operation callback) {
        if (lstStocks == null || lstStocks.size() == 0 || lstStocks.size() != lstValues.size()) {
            if (callback != null) {
                callback.onFail(TAG_ADD_OR_UPDATE);
            }
            return;
        }

        mCallback = callback;

        mapStockTemp.clear();
        mapStockTemp.putAll(mapStockAlerts);

        for (int i = 0; i < lstStocks.size(); i++) {
            mapStockTemp.put(lstStocks.get(i), lstValues.get(i));
        }

        JSONArray updateAry = stockMap2ReqJAry(mapStockTemp);
        JSONObject reqValue = new JSONObject();
        try {
            reqValue.put("ver", dataVersion);

            reqValue.put("data", updateAry);
        } catch (Exception e) {
        }

        requestUpdateStockWarnList(TAG_ADD_OR_UPDATE, reqValue.toJSONString());

    }

    /**
     * 更新或添加预警 一只
     * 
     * @param stockId
     * @param value
     * @param callback
     */
    public void updateWarns(String stockId, String value, Operation callback) {
        if (TextUtils.isEmpty(stockId) || TextUtils.isEmpty(value)) {
            if (callback != null) {
                callback.onFail(TAG_ADD_OR_UPDATE);
            }
            return;
        }

        List<String> lstStocks = new ArrayList<String>(1);
        List<String> lstValues = new ArrayList<String>(1);
        lstStocks.add(stockId);
        lstValues.add(value);

        updateWarns(lstStocks, lstValues, callback);
    }

    /**
     * 如果缓存无预警数据，从后台获取
     * */
    public void requestStockAlertsIfCashEmpty(Operation callback) {
        if (!isCacheHasData()) {
            requestStockWarnList(callback);
        }
    }

    /**
     * 发送网络请求，获取个股预警列表数据
     * */
    public void requestStockWarnList(Operation callback) {
        // 如果没有登录，直接退出
        if (!DataModule.getInstance().getUserInfo().isLogined())
            return;

        mCallback = callback;

        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        String key = "key_alarms_setting";

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TOKEN, userInfo.getToken());
            json.put(KEY_KEY, key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getNetworkManager().requestInfo(json, IDUtils.ID_USER_DATA, TAG_REQUEST_LIST);
    }

    /**
     * 将预警配置信息由json格式转换为map
     * */
    private void cacheStockWarn(String configJson) {
        // 如果json为空，直接返回
        if (TextUtils.isEmpty(configJson)) {
            return;
        }

        // 如果json格式不正确，直接返回
        try {
            JSONObject obj = JSONObject.parseObject(configJson);

            // 获取dataVersion
            dataVersion = obj.getIntValue("ver");

            // 获取data并存储到hashMap中
            JSONArray array = obj.getJSONArray("data");
            if (array != null && array.size() > 0) {
                mapStockAlerts.clear();
                int size = array.size();
                for (int i = 0; i < size; i++) {
                    JSONObject objItem = array.getJSONObject(i);
                    if (objItem != null) {
                        String id = objItem.getString("id");
                        String json = objItem.toJSONString();

                        mapStockAlerts.put(id, json);
                    }
                }
            }
        } catch (Exception e) {
        }

    }

    private JSONArray stockMap2ReqJAry(Map<String, String> stockMap) {

        JSONArray tJary = new JSONArray();
        if (stockMap != null) {
            Set<String> keys = stockMap.keySet();
            for (String key : keys) {

                JSONObject tJobj = null;
                try {
                    tJobj = JSONObject.parseObject(stockMap.get(key));
                } catch (Exception e) {
                }
                if (tJobj != null) {
                    tJary.add(tJobj);
                }
            }
        }

        return tJary;
    }

    /**
     * 更新缓存数据 map 必须首先更新网络端数据且成功后，才能更新缓存数据，否则如果更新了本地缓存数据后，更新网络数据失败，会造成两端数据不同步
     *
     * */
    private void updateCacheAlerts() {
        if (mapStockTemp != null) {
            clearCache();
            mapStockAlerts.putAll(mapStockTemp);
            mapStockTemp.clear();
        }
    }

    /**
     * 添加、更新、删除网络端预警配置信息
     * 
     * @param option 3 update
     * */
    private void requestUpdateStockWarnList(short requestFlag, String jsonValue) {

        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        // 如果没有登录，直接退出
        if (!userInfo.isLogined())
            return;

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TOKEN, userInfo.getToken());
            json.put(KEY_KEY, "key_alarms_setting");
            json.put(KEY_OP, 3);
            // json.put(KEY_VALUE, "{\"data\":[],\"ver\": 3 }");
            json.put(KEY_VALUE, jsonValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getNetworkManager().requestInfo(json, IDUtils.ID_USER_DATA_UPDATE, requestFlag);
    }

    @Override
    public void updateFromInfo(int retCode, String retMsg, InfoPackageImpl pkg) {

        if (retCode == -1 || retCode == -2) {
            int errCode = -999;
            if (!TextUtils.isEmpty(retMsg)) {
                errCode = DataUtils.convertToInt(retMsg);
            }
            if (mCallback != null) {
                mCallback.onFail(errCode);
            }
            return;
        }

        if (pkg == null) {
            if (mCallback != null) {
                mCallback.onFail(-999);
            }
            return;
        }

        int id = pkg.getRequestType();

        if (id == TAG_REQUEST_LIST) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {

                if (mCallback != null) {
                    mCallback.onFail(id);
                }
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);

                // 返回码是否是正确
                int result = jsObj.getIntValue("result");
                if (result == 0) {
                    String warnConfigJson = jsObj.getString("value");
//                    mIsGetListSuccess = true;
                    cacheStockWarn(warnConfigJson);

                    if (mCallback != null) {
                        mCallback.onSuccess(TAG_REQUEST_LIST);
                    }
                    mIsGetListSuccess = true;
                } else if (result == 8) {
                    if (mCallback != null) {
                        mCallback.onSuccess(TAG_REQUEST_LIST);
                    }

                    mIsGetListSuccess = true;
                } else {
                    if (mCallback != null) {
                        mCallback.onFail(TAG_REQUEST_LIST);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mCallback != null) {
                    mCallback.onFail(TAG_REQUEST_LIST);
                }
            }
        } else if (id == TAG_REMOVE || id == TAG_ADD_OR_UPDATE) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {

                if (mCallback != null) {
                    mCallback.onFail(id);
                }
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int result = jsObj.getIntValue("result");
                if (result == 0) {
                    updateCacheAlerts();
                    if (mCallback != null) {
                        mCallback.onSuccess(id);
                    }
                } else {
                    if (mCallback != null) {
                        mCallback.onFail(id);
                    }
                }
            } catch (Exception e) {
                if (mCallback != null) {
                    mCallback.onFail(id);
                }
            }

        }
    }

    private NetworkManager getNetworkManager() {
        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager(ACGApplication.getInstance().getApplicationContext(), this, null);
        }

        return mNetworkManager;
    }

    public interface Operation {
        public void onSuccess(int typeTag);

        public void onFail(int retCode);
    }

}
