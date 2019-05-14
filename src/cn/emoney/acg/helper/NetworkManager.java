package cn.emoney.acg.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.sky.libs.http.AsyncHttpClient;
import cn.emoney.sky.libs.network.HttpClient;
import cn.emoney.sky.libs.network.HttpClientFactory_v2;
import cn.emoney.sky.libs.network.HttpDataResponseHandler;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;
import cn.emoney.sky.libs.network.pkg.DataPackageImpl;

import com.alibaba.fastjson.JSONObject;

public class NetworkManager {

    private final static int MSG_UPDATE = 0;
    private final static int MSG_NETWORK_ERROR = -1;
    private final static int MSG_DECODE_ERROR = -2;

    private HttpClient mHttpClient = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private QuoteCallBack mQuoteCallBack = null;
    private InfoCallBack mInfoCallBack = null;

    public NetworkManager(Context context, InfoCallBack infoCallBack, QuoteCallBack quoteCallBack) {
        mContext = context;
        mInfoCallBack = infoCallBack;
        mQuoteCallBack = quoteCallBack;
        init();
    }

    /**
     * 检查网络是否可用
     * 
     * @return true:有网络，false,无网络
     */
    public static boolean IsNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) ACGApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {// 当前网络不可用
            return false;
        } else {
            return true;
        }
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MSG_UPDATE:
                        if (msg.arg1 == 1) {
                            if (mQuoteCallBack != null) {
                                mQuoteCallBack.updateFromQuote(0, "", (QuotePackageImpl) msg.obj);
                            }
                        } else if (msg.arg1 == 2) {
                            if (mInfoCallBack != null) {
                                mInfoCallBack.updateFromInfo(0, "", (InfoPackageImpl) msg.obj);
                            }
                        }

                        break;
                    case MSG_NETWORK_ERROR:
                        if (msg.arg1 == 1) {
                            if (mQuoteCallBack != null) {
                                mQuoteCallBack.updateFromQuote(-1, "" + msg.arg2, null);
                            }
                        } else if (msg.arg1 == 2) {
                            if (mInfoCallBack != null) {
                                mInfoCallBack.updateFromInfo(-1, "" + msg.arg2, null);
                            }
                        }
                        break;
                    case MSG_DECODE_ERROR:
                        if (msg.arg1 == 1) {
                            if (mQuoteCallBack != null) {
                                mQuoteCallBack.updateFromQuote(-2, "" + msg.arg2, null);
                            }
                        } else if (msg.arg1 == 2) {
                            if (mInfoCallBack != null) {
                                mInfoCallBack.updateFromInfo(-2, "" + msg.arg2, null);
                            }
                        }
                        break;

                    default:
                        break;
                }
            }
        };
    }

    public interface QuoteCallBack {
        public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg);
    }

    public interface InfoCallBack {
        public void updateFromInfo(int retCode, String retMsg, InfoPackageImpl pkg);
    }

    public void requestQuote(QuotePackageImpl pkg, int cmd) {
        requestQuote(pkg, cmd, null);
    }

    public void requestQuote(QuotePackageImpl pkg, int cmd, String sUrl) {
        pkg.setRequestCMD(String.valueOf(cmd));

        String requestUrl = RequestUrl.host + cmd;
        if (sUrl != null && !sUrl.equals("")) {
            requestUrl = sUrl + cmd;
        }

        List<Header> headers = new ArrayList<Header>();
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        headers.add(new BasicHeader("Authorization", userInfo.getToken()));

        getHttpClient().requestBinaryDataWithHeaders(requestUrl, pkg, new HttpDataResponseHandler() {

            @Override
            public void onRequestSuccess(DataPackageImpl pkg) {
                super.onRequestSuccess(pkg);
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_UPDATE, 1, 0, pkg);
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onDecodeFailure(String retMsg, DataHeadImpl head) {
                super.onDecodeFailure(retMsg, head);
                cancelRequest();
                if (mHandler != null) {
                    if (retMsg == null) {
                        retMsg = "decode fail";
                    }

                    int arg2 = 0;
                    if (head != null) {
                        arg2 = head.getDataType();
                    }
                    Message msg = mHandler.obtainMessage(-2, 1, arg2, retMsg);
                    mHandler.sendMessage(msg);
                }

            }

            @Override
            public void onRequestFailure(String retMsg, DataHeadImpl head) {
                super.onRequestFailure(retMsg, head);
                cancelRequest();
                if (mHandler != null) {
                    if (retMsg == null) {
                        retMsg = "req fail";
                    }

                    int arg2 = 0;
                    if (head != null) {
                        arg2 = head.getDataType();
                    }
                    Message msg = mHandler.obtainMessage(-1, 1, arg2, retMsg);
                    mHandler.sendMessage(msg);
                }

            }

        }, headers);
    }

    public void requestInfo(JSONObject json, short id) {
        requestInfo(json, id, null, (short) -1);
    }

    public void requestInfo(JSONObject json, short id, short reqFlag) {
        requestInfo(json, id, null, reqFlag);
    }

    public void requestInfo(JSONObject json, short id, String url) {
        requestInfo(json, id, url, (short) -1);
    }

    @SuppressWarnings("deprecation")
    public void requestInfo(JSONObject json, short id, String url, short reqFlag) {

        GlobalMessagePackage pkg = null;
        if (reqFlag > 0) {
            pkg = new GlobalMessagePackage(new QuoteHead(reqFlag));
        } else {
            pkg = new GlobalMessagePackage(new QuoteHead(id));
        }

        pkg.setRequest(GlobalMessage.MessageCommon.newBuilder().setMsgData(json.toString()).build());
        pkg.setRequestCMD(String.valueOf(id));

        String pack_url = "";

        if (url == null || url.equals("")) {
            pack_url = RequestUrl.host + id;
        } else {
            pack_url = url + id;
        }

        List<Header> headers = new ArrayList<Header>();
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        headers.add(new BasicHeader("Authorization", userInfo.getToken()));
        headers.add(new BasicHeader("Content-Type", "text/plain"));

        getHttpClient().requestBinaryDataWithHeaders(pack_url, pkg, new HttpDataResponseHandler() {

            @Override
            public void onRequestSuccess(DataPackageImpl pkg) {
                super.onRequestSuccess(pkg);
                Message msg = mHandler.obtainMessage(MSG_UPDATE, 2, 0, pkg);
                mHandler.sendMessage(msg);

            }

            @Override
            public void onDecodeFailure(String retMsg, DataHeadImpl head) {
                super.onDecodeFailure(retMsg, head);
                cancelRequest();
                if (retMsg == null) {
                    retMsg = "decode fail";
                }
                int arg2 = 0;
                if (head != null) {
                    arg2 = head.getDataType();
                }
                Message msg = mHandler.obtainMessage(MSG_DECODE_ERROR, 2, arg2, retMsg);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onRequestFailure(String retMsg, DataHeadImpl head) {
                super.onRequestFailure(retMsg, head);
                cancelRequest();
                if (retMsg == null) {
                    retMsg = "req fail";
                }

                int arg2 = 0;
                if (head != null) {
                    arg2 = head.getDataType();
                }

                Message msg = mHandler.obtainMessage(MSG_NETWORK_ERROR, 2, arg2, retMsg);
                mHandler.sendMessage(msg);
            }

        }, headers);
    }

    private void cancelRequest() {
        try {
            AsyncHttpClient httpClient = getHttpClient().getHttpClientProxy();
            if (httpClient != null)
                httpClient.cancelRequests(mContext, true);
        } catch (Exception e) {
        }

    }

    private HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = HttpClientFactory_v2.getInstance().createHttpClient(mContext.getApplicationContext());
        }
        return mHttpClient;
    }

}
