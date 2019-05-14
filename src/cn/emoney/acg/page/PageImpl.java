package cn.emoney.acg.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.QuotePeriod;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.dialog.FixToast;
import cn.emoney.acg.helper.FixedLengthList;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.db.DSQLiteDatabase;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.sky.libs.db.GlobalDBHelper;
import cn.emoney.sky.libs.http.AsyncHttpClient;
import cn.emoney.sky.libs.log.Logger;
import cn.emoney.sky.libs.network.HttpClient;
import cn.emoney.sky.libs.network.HttpClientFactory_v2;
import cn.emoney.sky.libs.network.HttpDataResponseHandler;
import cn.emoney.sky.libs.network.HttpJSONResponseHandler;
import cn.emoney.sky.libs.network.HttpTextResponseHandler;
import cn.emoney.sky.libs.network.data.JsonData;
import cn.emoney.sky.libs.network.model.HttpModel;
import cn.emoney.sky.libs.network.model.JsonModel;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;
import cn.emoney.sky.libs.network.pkg.DataPackageImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.umeng.analytics.MobclickAgent;

public abstract class PageImpl extends PageTitlebar implements IDUtils, KeysInterface, QuotePeriod {
    public static final int RESULT_CODE = 1;
    public int mPageChangeFlag = 0;

    public final static int MSG_SHOWTIP = 0;
    public final static int MSG_UPDATE_TRADE = 1;
    public final static int MSG_UPDATE_QUOTE = 2;
    public final static int MSG_NETWORK_ERROR = 3;
    public final static int MSG_UPDATE_JSON = 4;
    public final static int MSG_UPDATE_TEXT = 5;
    public final static int MSG_UPDATE_IMAGE = 6;
    public final static int MSG_DECODE_ERROR = 7;
    public final static int MSG_UPDATE_INFO = 8;
    public final static int MSG_UPDATE_JSON_PKG = 9;

    private FixedLengthList<HttpClient> mlstHttpClient = new FixedLengthList<HttpClient>(30);

    private DSQLiteDatabase mSqliteHelper = null;
    protected Logger mLogger = null;
    protected Handler mTaskHandler = new Handler();
    private Runnable mTask = null;
    private GlobalDBHelper mDBHelper = null;

    private IntentFilter mIntentFilter = null;

    protected boolean bIsAutoRefresh = false;

    protected Handler mImplHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int what = msg.what;
            if (isHidden()) {
                return;
            }
            switch (what) {
                case MSG_SHOWTIP:
                    String m = (String) msg.obj;
                    if (m != null && getContext() != null) {
                        FixToast.createMsg(getContext(), m, FixToast.TIME_LONG);
                    }
                    break;
                case MSG_NETWORK_ERROR:
                    String errorMsg = (String) msg.obj;
                    if (errorMsg != null && !errorMsg.equals("") && msg.arg1 != -1 && getContext() != null) {
                        FixToast.createMsg(getContext(), errorMsg, FixToast.TIME_LONG);
                    }
                    updateWhenNetworkError();
                    updateWhenNetworkError((short) msg.arg2);
                    break;
                case MSG_DECODE_ERROR:
                    String error = (String) msg.obj;
                    if (error != null && !error.equals("") && msg.arg1 != -1 && getContext() != null) {
                        FixToast.createMsg(getContext(), error, FixToast.TIME_LONG);
                    }
                    updateWhenDecodeError();
                    updateWhenDecodeError((short) msg.arg2);

                    break;
                case MSG_UPDATE_TRADE:
                    // updateFromTrade((TradePackageImpl) msg.obj);
                    break;
                case MSG_UPDATE_QUOTE:
                    updateFromQuote((QuotePackageImpl) msg.obj);
                    break;
                case MSG_UPDATE_INFO:
                    updateFromInfo((InfoPackageImpl) msg.obj);
                    break;
                case MSG_UPDATE_JSON:
                    updateFromJson((JsonData) msg.obj);
                    break;
                case MSG_UPDATE_TEXT:
                    updateFromText((String) msg.obj);
                    break;
                case MSG_UPDATE_IMAGE:
                    break;
            }
        }

    };

    public void showProgress() {
        if (!getUserVisibleHint()) {
            return;
        }
        mImplHandler.post(new Runnable() {
            @Override
            public void run() {

            }

        });
    }

    public void dismissProgress() {
        mImplHandler.post(new Runnable() {
            @Override
            public void run() {}
        });
    }


    public DSQLiteDatabase getSQLiteDBHelper() {
        if (mSqliteHelper == null) {
            mSqliteHelper = new DSQLiteDatabase(getContext());
        }

        return mSqliteHelper;
    }

    public void closeSQLDBHelper() {
        if (mSqliteHelper != null) {
            mSqliteHelper.close();
        }
        mSqliteHelper = null;
    }

    public GlobalDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new GlobalDBHelper(getContext(), DataModule.DB_GLOBAL);
        }
        return mDBHelper;
    }

    public HttpClient getHttpClient() {
        HttpClient httpClient = null;

        if (getContext() == null) {
            httpClient = HttpClientFactory_v2.getInstance().createHttpClient(getActivity());
        } else {
            httpClient = HttpClientFactory_v2.getInstance().createHttpClient(getContext());
        }

        if (httpClient != null) {
            httpClient.setSupportGZipEncoding(false);
            HttpClient tail = mlstHttpClient.addFirstSafe(httpClient);
            if (tail != null) {
                AsyncHttpClient asynHttpClient = tail.getHttpClientProxy();
                if (asynHttpClient != null) {
                    try {
                        asynHttpClient.cancelRequests(getContext(), true);
                    } catch (Exception e) {
                    }
                }
            }
        }

        return httpClient;
    }

    public void initHttpClient() {
        getHttpClient();
    }

    public ArrayList<String> getRegisterBcdc() {
        return null;
    }

    public void requestData() {}

    protected void onPageResume() {
        super.onPageResume();

        String t_className = getClass().getName();
        // 例: cn.emoney.acg.page.MainPage
        String sTag = t_className.substring(t_className.lastIndexOf(".") + 1);
        MobclickAgent.onPageStart(sTag);
        mPageChangeFlag = 0;

        registerBroadcast();
    }

    private void registerBroadcast() {
        if (mIntentFilter == null) {
            ArrayList<String> lstBcdc = getRegisterBcdc();
            if (lstBcdc == null || lstBcdc.size() == 0) {
                return;
            }

            mIntentFilter = new IntentFilter();
            for (int i = 0; i < lstBcdc.size(); i++) {
                mIntentFilter.addAction(lstBcdc.get(i));
            }
        }

        if (bcrIntenal != null && mIntentFilter != null) {
            getContext().registerReceiver(bcrIntenal, mIntentFilter);
        }
    }

    private void cancelRequest() {
        dismissProgress();

        if (mlstHttpClient != null) {
            for (int i = 0; i < mlstHttpClient.size(); i++) {
                HttpClient tClient = mlstHttpClient.get(i);
                if (tClient != null) {
                    AsyncHttpClient asynHttpClient = tClient.getHttpClientProxy();
                    if (asynHttpClient != null) {
                        try {
                            asynHttpClient.cancelRequests(getContext(), true);
                        } catch (Exception e) {
                        }
                    }

                }
            }

            mlstHttpClient.clear();
        }

    }

    protected boolean getIsAutoRefresh() {
        return bIsAutoRefresh;
    }

    protected void cancelRequestTask() {
        mTaskHandler.removeCallbacks(mTask);
        bIsAutoRefresh = false;
    }

    protected void startRequestTask(final int intervalTime) {
        cancelRequestTask();

        mTask = new Runnable() {

            @Override
            public void run() {
                if (NetworkManager.IsNetworkAvailable()) {
                    requestData();
                }

                if (intervalTime > 0 && DataModule.G_AUTO_REFRESH) {
                    mTaskHandler.postDelayed(this, intervalTime * 1000);
                } else {
                    cancelRequestTask();
                }
            }
        };
        mTaskHandler.post(mTask);
        bIsAutoRefresh = true;
    }

    protected void startRequestTask() {
        int t_interval = getInterval();
        startRequestTask(t_interval);
    }

    protected void stopRequest() {
        try {
            cancelRequest();
        } catch (Exception e) {

        }
        cancelRequestTask();
    }

    protected void onPagePause() {
        String t_className = getClass().getName();
        String sTag = t_className.substring(t_className.lastIndexOf(".") + 1);
        MobclickAgent.onPageEnd(sTag);
        stopRequest();

        if (bcrIntenal != null && mIntentFilter != null) {
            try {
                getContext().unregisterReceiver(bcrIntenal);
            } catch (Exception e) {
            }
        }

        super.onPagePause();
    }

    @Override
    protected void onPageDestroy() {
        closeSQLDBHelper();

        super.onPageDestroy();
    }

    protected void updateFromQuote(QuotePackageImpl pkg) {

    }

    protected void updateFromInfo(InfoPackageImpl pkg) {

    }

    protected void updateFromJson(JsonData data) {

    }

    protected void updateFromText(String data) {

    }

    protected void updateWhenNetworkError() {

    }

    protected void updateWhenNetworkError(short type) {

    }

    protected void updateWhenDecodeError() {

    }

    protected void updateWhenDecodeError(short type) {

    }

    // 原生文本请求,xml,腾讯新闻增加
    protected void requestText(final String url, final boolean isShowErrorToast) {
        HttpModel model = new HttpModel();
        model.setSourceUrl(url);
        model.setMethod(HttpModel.GET);
        model.setCharSet("utf-8");

        getHttpClient().requestTextData(model, new HttpTextResponseHandler() {
            @Override
            public void onRequestSuccess(String response) {
                super.onRequestSuccess(response);
                Message msg = new Message();
                msg.what = MSG_UPDATE_TEXT;
                msg.obj = response;
                mImplHandler.sendMessage(msg);
            }

            @Override
            public void onRequestSuccessFromHttpCache(String response) {
                super.onRequestSuccessFromHttpCache(response);
                Message msg = new Message();
                msg.what = MSG_UPDATE_TEXT;
                msg.obj = response;
                mImplHandler.sendMessage(msg);
            }

            @Override
            public void onRequestStart(int reqCount) {
                super.onRequestStart(reqCount);
            }

            @Override
            public void onRequestFinish(int reqCount) {
                super.onRequestFinish(reqCount);
            }

            @Override
            public void onRequestFailure(String msg, DataHeadImpl head) {
                super.onRequestFailure(msg, head);
                Message m = new Message();
                m.what = MSG_NETWORK_ERROR;

                if (!isShowErrorToast) {
                    m.arg1 = -1; // 不打印toast
                }

                if (head != null) {
                    m.arg2 = head.getDataType();
                }
                m.obj = msg;
                mImplHandler.sendMessage(m);
                dismissProgress();
            }

            @Override
            public void onDecodeFailure(String msg, DataHeadImpl head) {
                super.onDecodeFailure(msg, head);
                Message m = new Message();
                m.what = MSG_DECODE_ERROR;
                if (!isShowErrorToast) {
                    m.arg1 = -1; // 不打印toast
                }
                if (head != null) {
                    m.arg2 = head.getDataType();
                }
                m.obj = msg;
                mImplHandler.sendMessage(m);
                dismissProgress();
            }

        });

    }

    protected void requestJson(String url, String clzz, final boolean isShowErrorToast) {
        JsonModel model = new JsonModel(url);
        model.clazz = clzz;
        getHttpClient().requestJsonData(model, new HttpJSONResponseHandler() {

            @Override
            public void onRequestSuccess(JsonData response) {
                super.onRequestSuccess(response);
                Message msg = new Message();
                msg.what = MSG_UPDATE_JSON;
                msg.obj = response;
                mImplHandler.sendMessage(msg);
            }

            @Override
            public void onRequestSuccessFromHttpCache(JsonData response) {
                super.onRequestSuccessFromHttpCache(response);
                Message msg = new Message();
                msg.what = MSG_UPDATE_JSON;
                msg.obj = response;
                mImplHandler.sendMessage(msg);
            }

            @Override
            public void onRequestStart(int reqCount) {
                super.onRequestStart(reqCount);
                showProgress();
            }

            @Override
            public void onRequestFinish(int reqCount) {
                super.onRequestFinish(reqCount);
                dismissProgress();
            }

            @Override
            public void onRequestFailure(String msg, DataHeadImpl head) {
                super.onRequestFailure(msg, head);
                Message m = new Message();
                m.what = MSG_NETWORK_ERROR;

                if (!isShowErrorToast) {
                    m.arg1 = -1; // 不打印toast
                }

                if (head != null) {
                    m.arg2 = head.getDataType();
                }

                m.obj = msg;
                mImplHandler.sendMessage(m);
                dismissProgress();
            }

        });
    }

    public void requestInfo(JSONObject json, short id) {
        requestInfo(json, id, null, true, null, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, short reqFlag) {
        requestInfo(json, id, null, true, null, reqFlag);
    }

    public void requestInfo(JSONObject json, short id, String url, short reqFlag) {
        requestInfo(json, id, null, true, url, reqFlag);
    }

    public void requestInfo(JSONObject json, short id, final boolean isShowErrorToast) {
        requestInfo(json, id, null, isShowErrorToast, null, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, String url) {
        requestInfo(json, id, null, true, url, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, final OnOperateZXGListener listener) {
        requestInfo(json, id, listener, true, null, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, final OnOperateZXGListener listener, String url) {
        requestInfo(json, id, listener, true, url, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, final OnOperateZXGListener listener, final boolean isShowErrorToast) {
        requestInfo(json, id, listener, true, null, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, final OnOperateZXGListener listener, final boolean isShowErrorToast, String url, short reqFlag) {
        GlobalMessagePackage pkg = null;
        if (reqFlag > 0) {
            pkg = new GlobalMessagePackage(new QuoteHead(reqFlag));
        } else {
            pkg = new GlobalMessagePackage(new QuoteHead(id));
        }

        pkg.setRequest(GlobalMessage.MessageCommon.newBuilder().setMsgData(json.toString()).build());
        pkg.setRequestCMD(String.valueOf(id));

        String pack_url = "";
        Short sId = id;

        int iId = id & 0x0FFFF;
        if (TextUtils.isEmpty(url)) {
            pack_url = RequestUrl.host + id;
        } else {
            pack_url = url + id;
        }

        List<Header> headers = new ArrayList<Header>();
        UserInfo userInfo = getUserInfo();
        headers.add(new BasicHeader("Authorization", userInfo.getToken()));
        headers.add(new BasicHeader("Content-Type", "text/plain"));

        getHttpClient().requestBinaryDataWithHeaders(pack_url, pkg, new HttpDataResponseHandler() {

            @Override
            public void onTrafficIn(long byteLen) {}

            @Override
            public void onTrafficOut(long byteLen) {}

            @Override
            public void onRequestSuccess(DataPackageImpl pkg) {
                super.onRequestSuccess(pkg);
                Message msg = new Message();
                msg.what = MSG_UPDATE_INFO;
                msg.obj = pkg;
                mImplHandler.sendMessage(msg);

                if (listener != null) {
                    GlobalMessagePackage p = (GlobalMessagePackage) pkg;
                    String result = p.getResponse().getMsgData();
                    JSONObject js;
                    try {
                        js = JSON.parseObject(result);
                        int code = js.getIntValue(KEY_RESULT);
                        String m = js.getString(KEY_MESSAGE);
                        listener.onOperate(code == 0, m);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDecodeFailure(String msg, DataHeadImpl head) {
                super.onDecodeFailure(msg, head);
                stopRequest();
                Message m = new Message();
                m.what = MSG_DECODE_ERROR;
                if (!isShowErrorToast) {
                    m.arg1 = -1; // 不打印toast
                }
                if (head != null) {
                    m.arg2 = head.getDataType();
                }

                m.obj = msg;

                mImplHandler.sendMessage(m);

                if (listener != null) {
                    listener.onOperate(false, msg);
                }
                dismissProgress();
            }

            @Override
            public void onRequestFailure(String msg, DataHeadImpl head) {
                super.onRequestFailure(msg, head);
                stopRequest();
                Message m = new Message();
                m.what = MSG_NETWORK_ERROR;
                if (!isShowErrorToast) {
                    m.arg1 = -1; // 不打印toast
                }

                if (head != null) {
                    m.arg2 = head.getDataType();
                }
                m.obj = msg;
                mImplHandler.sendMessage(m);

                if (listener != null) {
                    listener.onOperate(false, msg);
                }
                dismissProgress();
            }

            @Override
            public void onRequestFinish(int reqCount) {
                super.onRequestFinish(reqCount);
                dismissProgress();
            }

            @Override
            public void onRequestStart(int reqCount) {
                super.onRequestStart(reqCount);
                showProgress();
            }

        }, headers);
    }

    public void requestQuote(QuotePackageImpl pkg, int cmd, String sUrl) {
        pkg.setRequestCMD(String.valueOf(cmd));
        // String requestUrl = RequestUrl.host + cmd;

        String requestUrl = RequestUrl.host + cmd;
        if (sUrl != null && !sUrl.equals("")) {
            requestUrl = sUrl + cmd;
        }

        List<Header> headers = new ArrayList<Header>();
        UserInfo userInfo = getUserInfo();
        headers.add(new BasicHeader("Authorization", userInfo.getToken()));

        getHttpClient().requestBinaryDataWithHeaders(requestUrl, pkg, new HttpDataResponseHandler() {

            @Override
            public void onTrafficIn(long byteLen) {}

            @Override
            public void onTrafficOut(long byteLen) {}

            @Override
            public void onRequestSuccess(DataPackageImpl pkg) {
                super.onRequestSuccess(pkg);
                Message msg = new Message();
                msg.what = MSG_UPDATE_QUOTE;
                msg.obj = pkg;
                mImplHandler.sendMessage(msg);
            }

            @Override
            public void onDecodeFailure(String msg, DataHeadImpl head) {
                super.onDecodeFailure(msg, head);
                // stopRequest();
                Message m = new Message();
                m.what = MSG_DECODE_ERROR;
                if (head != null) {
                    m.arg2 = head.getDataType();
                }
                m.obj = msg;
                mImplHandler.sendMessage(m);
                dismissProgress();
            }

            @Override
            public void onRequestFailure(String msg, DataHeadImpl head) {
                super.onRequestFailure(msg, head);
                // stopRequest();
                Message m = new Message();
                m.what = MSG_NETWORK_ERROR;
                if (head != null) {
                    m.arg2 = head.getDataType();
                }
                m.obj = msg;

                mImplHandler.sendMessage(m);
                dismissProgress();
            }

            @Override
            public void onRequestFinish(int reqCount) {
                super.onRequestFinish(reqCount);
                dismissProgress();
            }

            @Override
            public void onRequestStart(int reqCount) {
                super.onRequestStart(reqCount);
                showProgress();
            }

        }, headers);
    }

    public void requestQuote(QuotePackageImpl pkg, int cmd) {
        requestQuote(pkg, cmd, null);
    }

    public boolean isLogined() {
        UserInfo userInfo = getUserInfo();
        return userInfo.isLogined();
    }

    public String getToken() {
        UserInfo userInfo = getUserInfo();
        return userInfo.getToken();
    }

    public UserInfo getUserInfo() {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        return userInfo;
    }

    public void showTip(final String msg) {
        mImplHandler.post(new Runnable() {
            @Override
            public void run() {
                FixToast.createMsg(getContext(), msg, FixToast.TIME_SHORT);
            }

        });
    }

    public void showTip(final String msg, final int type) {
        mImplHandler.post(new Runnable() {

            @Override
            public void run() {
                FixToast.createMsg(getContext(), msg, type);
            }
        });
    }

    public void addZXG(String type, Goods goods, final OnOperateZXGListener listener) {
        List<Goods> lstGoods = new ArrayList<Goods>();
        lstGoods.add(goods);
        addZXG(type, lstGoods, listener);
    }

    public void addZXG(String type, List<Goods> lstGoods, final OnOperateZXGListener listener) {
        UserInfo userInfo = getUserInfo();

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TOKEN, userInfo.getToken());
            json.put(KEY_CLS, type);
            JSONArray arr = new JSONArray();
            for (int i = 0; i < lstGoods.size(); i++) {
                Goods g = lstGoods.get(i);
                JSONArray a = new JSONArray();
                /*
                 * ids:[ ["600123","10#15.5"], ["1000065"] ]
                 */
                String t_gCode = String.valueOf(g.getGoodsId());
                a.add(t_gCode);

                if (type.equals(OptionalInfo.TYPE_POSITION)) {
                    String t_amount = g.getPositionAmount();
                    if (t_amount.equals("0") || t_amount.equals("--") || t_amount.equals("")) {
                        t_amount = "1";
                    }
                    String t_gDetail = t_amount + "#" + g.getPositionPrice();
                    a.add(t_gDetail);
                }

                arr.add(a);
            }
            json.put(KEY_IDS, arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(json, IDUtils.ID_OPTIONAL_ADD_STOCK, listener);
    }

    public void delZXG(String type, Goods goods, final OnOperateZXGListener listener) {
        List<Goods> lstGoods = new ArrayList<Goods>();
        lstGoods.add(goods);
        delZXG(type, lstGoods, listener);
    }

    public void delZXG(String type, List<Goods> lstGoods, final OnOperateZXGListener listener) {
        UserInfo userInfo = getUserInfo();

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TOKEN, userInfo.getToken());
            json.put(KEY_CLS, type);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < lstGoods.size(); i++) {
                Goods g = lstGoods.get(i);
                if (i == lstGoods.size() - 1) {
                    sb.append(g.getGoodsId());
                } else {
                    sb.append(g.getGoodsId() + ";");
                }
            }
            json.put(KEY_IDS, sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(json, IDUtils.ID_OPTIONAL_DEL_STOCK, listener);
    }

    // op 1:add, 2:del, 3:update
    public void requestControlOptionalType(int op, String type, String reType, OnOperateZXGListener listener) {
        UserInfo userInfo = getUserInfo();

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TOKEN, userInfo.getToken());
            json.put(KEY_OP, op);
            json.put(KEY_CLS, type);
            if (op == 3 && reType != null && !reType.equals("")) {
                json.put(KEY_RECLS, reType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(json, IDUtils.ID_OPTIONAL_OPTION_STOCK_TYPE, listener);

    }

    /**
     * 切换主题
     */

    public void quitFullScreen() {
        final WindowManager.LayoutParams attrs = getModule().getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getModule().getWindow().setAttributes(attrs);
        getModule().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    // public boolean closeSoftKeyBoard() {
    // InputMethodManager imm = (InputMethodManager)
    // getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
    // // 得到InputMethodManager的实例
    // boolean bRet = imm.isActive();
    // if (bRet) {
    // // 如果存在关闭
    // if (getActivity().getCurrentFocus() != null) {
    // imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
    // InputMethodManager.HIDE_NOT_ALWAYS);
    // return true;
    // } else {
    // return false;
    // }
    //
    // // 切换开启和关闭
    // // imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
    // // InputMethodManager.HIDE_NOT_ALWAYS);
    // // 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
    // }
    //
    // return false;
    // }
    //
    // public void switchSoftKeyBoard() {
    // InputMethodManager imm = (InputMethodManager)
    // getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
    // // 得到InputMethodManager的实例
    // boolean bRet = imm.isActive();
    // if (bRet) {
    // // 切换开启和关闭
    // // 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
    // imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    // }
    // }

    public int getInterval() {
        int interval = DataModule.G_WIFIREFRESHTIMEINTERVAL;
        if (DataModule.G_CURRENT_NETWORK_TYPE == ConnectivityManager.TYPE_MOBILE) {
            interval = DataModule.G_MOBLIEREFRESHTIMEINTERVAL;
        } else if (DataModule.G_CURRENT_NETWORK_TYPE == ConnectivityManager.TYPE_WIFI) {
            interval = DataModule.G_WIFIREFRESHTIMEINTERVAL;
        }

        return interval;
    }

    public int getPageChangeFlag() {
        return mPageChangeFlag;
    }

    public Logger getLogger() {
        String t_className = getClass().getName();
        // 例: cn.emoney.acg.page.MainPage
        String sTag = t_className.substring(t_className.lastIndexOf(".") + 1);

        if (mLogger == null) {
            mLogger = Logger.create(sTag);
        }

        return mLogger;
    }

    private BroadcastReceiver bcrIntenal = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadCastName.BCDC_CHANGE_LOGIN_STATE)) {
                onLoginStateChanged();
            }

            // 回调子类
            onReceivedBroadcast(action);
        }
    };

    public void onLoginStateChanged() {

    }

    /**
     * 收到注册的广播, 登录状态和主题切换父类处理 或请重载onLoginStateChanged, onChangeTheme 其它广播请重载onReceivedBroadcast
     * 
     * @param action
     */
    public void onReceivedBroadcast(String action) {

    }

    /**
     * 发送广播事件
     * 
     * @param action
     */
    public void sendBroadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        getContext().sendBroadcast(intent);
    }

    public int RColor(int id) {
        return getContext().getResources().getColor(id);
    }

    public int getResIdByStr(String dir, String strPre, int id) {
        if (id >= 0) {
            String sItemId = strPre + id;
            try {
                int iItemId = getContext().getResources().getIdentifier(sItemId, dir, ACGApplication.getInstance().getPackageName());
                return iItemId;
            } catch (Exception e) {
            }

        }
        return 0;
    }

    public int getZDPColor(float flag) {
        if (flag == 0) {
            return RColor(R.color.c1);
        } else if (flag > 0) {
            return RColor(R.color.c1);
        } else {
            return RColor(R.color.c2);
        }
    }

    // shape_bg_red_round_radius
    protected int getZDPRadiusBg(float flag) {
        if (flag == 0) {
            return R.drawable.shape_bg_c1_radius;
        } else if (flag > 0) {
            return R.drawable.shape_bg_c1_radius;
        } else {
            return R.drawable.shape_bg_c2_radius;
        }
    }


    // 通用页面切换动画
    @Override
    public int enterAnimation() {
        if (isSupportAnimation()) {
            return R.anim.sys_slide_in_right;
        }
        return 0;

    }

    @Override
    public int popExitAnimation() {
        if (isSupportAnimation()) {
            return R.anim.sys_slide_out_right;
        }
        return 0;
    }

    /**
     * 初始化屏幕宽高相关的数据
     */
    protected void initScreenSupport() {
        if (DataModule.SCREEN_WIDTH == 0 || DataModule.SCREEN_HEIGHT == 0) {
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
            DataModule.SCREEN_WIDTH = mDisplayMetrics.widthPixels;
            DataModule.SCREEN_HEIGHT = mDisplayMetrics.heightPixels;
        }
    }
}
