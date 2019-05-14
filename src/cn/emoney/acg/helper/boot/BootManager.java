package cn.emoney.acg.helper.boot;

import android.content.Context;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.optional.OptionalSharePackage;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply;
import cn.emoney.acg.data.protocol.optional.OptionalShareRequest.OptionalShare_Request;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.helper.push.BaiduPushManager_v2;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.LoginDataParser;
import cn.emoney.acg.util.DeviceInfoUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class BootManager implements KeysInterface, NetworkManager.InfoCallBack, NetworkManager.QuoteCallBack {
    PageImpl mPI = null;
    Context mContext = null;
    NetworkManager mNetworkManager = null;
    // private DSQLiteDatabase mSqliteHelper;
    private GlobalDBHelper mDbHelper;

    private int mRequestSuccessCount = 0;

    public BootManager(PageImpl pi, Context context) {
        mPI = pi;
        mContext = context;
        mNetworkManager = new NetworkManager(context, this, this);
    }

    public void requestReLogin(UserInfo userInfo) {
        String userToken = userInfo.getReLoginToken();

        // 本地有数据的情况 且 上次的登录状态为登录时,自动登录
        if (userToken != "" && DataModule.G_LAST_LOGIN_STATE == 1) {
            JSONObject jsObj = new JSONObject();

            int nBuildNum = Integer.valueOf(DataModule.G_APKBUILDNUMBER.replaceAll("\\.", ""));
            String sHard = DeviceInfoUtil.getInstance().mDevice;

            /*
             * {"c":2,"h":"     WD-WCAYUZ64","n":"6d8c6153d023e605c4cdbda50328e9d5" ,
             * "p":222,"u":"15202155419","v":1409301}
             */// c: 1电脑, 2手机
            try {
                jsObj.put("c", 2); // 登录类型
                jsObj.put("h", sHard); // 硬件信息 最长16位字符
                jsObj.put("n", userToken);
                jsObj.put("p", 99);
                jsObj.put("u", "");
                jsObj.put("v", nBuildNum); // 产品版本 最大4位整数

                if (mNetworkManager != null) {
                    mNetworkManager.requestInfo(jsObj, IDUtils.ID_USER_RELOGIN);
                }

                LogUtil.easylog("sky", "relogin data = " + jsObj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            onBootFail();
        }
    }

    public void requestOptionalStock(UserInfo userInfo) {
        LogUtil.easylog("sky", "MainPage->requestOptionalStock");

        OptionalSharePackage pkg = new OptionalSharePackage(new QuoteHead(IDUtils.ID_OPTIONAL_QUERY_STOCKS));
        pkg.setRequest(OptionalShare_Request.newBuilder().setToken(userInfo.getToken()).build());

        if (mNetworkManager != null) {
            mNetworkManager.requestQuote(pkg, IDUtils.ID_OPTIONAL_QUERY_STOCKS_PB);
        }
    }

    // 请求用户权限
    public void requestUserPermission() {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        String t_token = userInfo.getToken();
        if (t_token != null && !t_token.equals("")) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put(KEY_TOKEN, t_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mNetworkManager != null) {
                mNetworkManager.requestInfo(jsObj, IDUtils.ID_USER_PERMISSION);
            }
            LogUtil.easylog("sky", "requestUserPermission: " + jsObj.toString());
        }
    }

    /**
     * 
     * 获取额外的用户信用 如昵称,头像id
     */
    private void requestExtraUserInfo() {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        String t_token = userInfo.getToken();
        if (t_token != null && !t_token.equals("") && !t_token.equals(DataModule.G_GUEST_TOKEN)) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put(KEY_TOKEN, t_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mNetworkManager != null) {
                mNetworkManager.requestInfo(jsObj, IDUtils.ID_USER_EXTRAINFO);
            }
        }
    }

    public void processUpdateInfo(InfoPackageImpl pkg) {
        if (pkg instanceof GlobalMessagePackage) {

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }
            String msgData = mc.getMsgData();
            LogUtil.easylog("sky", "mainpage updateFromInfo data = " + msgData);
            int reqType = gmp.getRequestType();
            if (reqType == IDUtils.ID_USER_RELOGIN) {
                updateLoginInfo(msgData);
            }

            // else if (reqType == IDUtils.ID_OPTIONAL_QUERY_STOCKS) {
            // updateOptionalInfo(msgData);
            // }

            else if (reqType == IDUtils.ID_USER_PERMISSION) {
                updatePermissionInfo(msgData);
            } else if (reqType == IDUtils.ID_SYSTEM_DATA) {
            } else if (reqType == IDUtils.ID_USER_EXTRAINFO) {
                updateExtraInfo(msgData);
            }
        }
    }

    private void updateLoginInfo(String msgData) {
        LogUtil.easylog("3333");
        // {
        // "message": "success to relogin",
        // "result": 0,
        // "token": "6d8c6153d023e605c4cdbda50328e9d5",
        // "user": "15202155419"
        // }
        try {
            JSONObject jsObj = JSON.parseObject(msgData);
            LogUtil.easylog("sky", "mainpage updateLoginInfo = " + jsObj.toString());
            int result = jsObj.getIntValue("result");
            String msg = jsObj.getString("message");
            if (result == 0) {
                UserInfo userInfo = DataModule.getInstance().getUserInfo();

                String token = jsObj.getString("token");
                // username为传什么返回什么
                // String userName = jsObj.getString("user");
                // userInfo.setToken(token);
                // if (userName != null && !userName.equals("")) {
                // userInfo.setUsername(userName);
                // }
                userInfo.setLogined(true);

                userInfo.save(getDBHelper());

                DataModule.G_LAST_LOGIN_STATE = 1;
                getDBHelper().setInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE, DataModule.G_LAST_LOGIN_STATE);

                mRequestSuccessCount++;

                // 请求自选股
                requestOptionalStock(userInfo);
                // 请求权限
                requestUserPermission();

                StockAlertManagerV2.getInstance().requestStockAlertsIfCashEmpty(null);
            } else {
                // 重登录失败
                onBootFail();
            }
        } catch (JSONException e) {
            // 重登录失败
            onBootFail();
            e.printStackTrace();
        }
    }

    private void updateOptionalInfo(OptionalShare_Reply reply) {
        try {
            boolean ret = getLoginDataParser().parserOptionalInfo_pb(reply);
            if (ret) {
                mRequestSuccessCount++;
                isBootSuccessed();
            } else {
                // 重登录失败
                onBootFail();
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            // 重登录失败
            onBootFail();
        }
    }

    private void updatePermissionInfo(String msgData) {
        try {
            boolean res = getLoginDataParser().pareserPermission(msgData);
            if (res) {
                mRequestSuccessCount++;
                isBootSuccessed();
            } else {
                // 重登录失败
                onBootFail();
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            // 重登录失败
            onBootFail();
        }
    }

    private void updateExtraInfo(String msgData) {
        try {
            getLoginDataParser().pareserUserInfo(msgData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 不处理
        }
    }


    @Override
    public void updateFromInfo(int retCode, String retMsg, InfoPackageImpl pkg) {
        if (retCode == 0) {
            processUpdateInfo(pkg);
        }
    }

    private boolean isBootSuccessed() {
        if (mRequestSuccessCount >= 3) {
            // 重登录成功
            onBootSucc();
            return true;
        }
        return false;
    }

    // 重登录成功
    private void onBootSucc() {
        LogUtil.easylog("push 2");
        BaiduPushManager_v2.setOnline();

        if (mPI != null) {
            mPI.sendBroadcast(BroadCastName.BCDC_CHANGE_LOGIN_STATE);
        }

        // 请求用户信息
        requestExtraUserInfo();

        // 加载问股的配置信息
        QuizConfigData.getInstance().loadServerConfig();
    }

    // 重登录失败
    private void onBootFail() {
        LogUtil.easylog("push 1");
        DataModule.getInstance().getUserInfo().setLogined(false);

        BaiduPushManager_v2.setOffLine();

        // 加载问股的配置信息
        QuizConfigData.getInstance().loadServerConfig();
    }

    private GlobalDBHelper getDBHelper() {
        if (mDbHelper == null) {
            mDbHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
        }
        return mDbHelper;
    }


    private LoginDataParser loginDataParser = null;

    private LoginDataParser getLoginDataParser() {
        if (loginDataParser == null) {
            loginDataParser = new LoginDataParser(mContext, mPI);
        }

        return loginDataParser;
    }

    @Override
    public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg) {
        if (retCode == 0) {
            if (pkg != null && pkg instanceof OptionalSharePackage) {
                OptionalSharePackage optionalPkg = (OptionalSharePackage) pkg;
                OptionalShare_Reply reply = optionalPkg.getResponse();
                if (reply != null) {
                    updateOptionalInfo(reply);
                }
            }
        }

    }
}
