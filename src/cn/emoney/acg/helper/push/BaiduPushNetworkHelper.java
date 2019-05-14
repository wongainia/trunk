package cn.emoney.acg.helper.push;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;

public class BaiduPushNetworkHelper implements NetworkManager.InfoCallBack {
    public static final String KEY_CHANNELID = "channelId";
    public static final String KEY_PLATFORMID = "platformId";
    public static final String KEY_PUSHSETTINGS = "pushSettings";

    private boolean mIsCommitSuccessLastTime = true;

    public boolean isCommitSuccessLastTime() {
        return mIsCommitSuccessLastTime;
    }

    private NetworkManager mNetworkManager = null;
    private Context mContext = null;


    private static BaiduPushNetworkHelper mInstance = null;

    public static BaiduPushNetworkHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BaiduPushNetworkHelper(context);
        }
        return mInstance;
    }

    private BaiduPushNetworkHelper(Context context) {
        mContext = context;
        if (mNetworkManager != null) {
            mNetworkManager = new NetworkManager(context, this, null);
        }
    }

    public void requestCommitTags(Map<String, Integer> mapTags) {
        if (TextUtils.isEmpty(DataModule.G_BAIDU_PUSH_CHANNELID)) {
            return;
        }

        JSONObject jobjReq = new JSONObject();

        jobjReq.put(KEY_CHANNELID, DataModule.G_BAIDU_PUSH_CHANNELID);
        jobjReq.put(KEY_PLATFORMID, 3);// 3 android; 4 ios

        String jsonString = JSON.toJSONString(mapTags);
        JSONObject jobjTags = JSONObject.parseObject(jsonString);
        jobjReq.put(KEY_PUSHSETTINGS, jobjTags);

        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager(mContext, this, null);
        }
        LogUtil.easylog("BaiduPushNet->commitTags:" + jobjReq.toJSONString());
        mNetworkManager.requestInfo(jobjReq, IDUtils.ID_BAIDU_PUSH_SET, RequestUrl.host0);
    }


    @Override
    public void updateFromInfo(int retCode, String retMsg, InfoPackageImpl pkg) {
       
        if (retCode == 0) {

            if (pkg.getRequestType() == IDUtils.ID_BAIDU_PUSH_SET) {
                mIsCommitSuccessLastTime = true;
                if (pkg instanceof GlobalMessagePackage) {
                    GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
                    MessageCommon gr = goodsTable.getResponse();
                    if (gr == null || gr.getMsgData() == null) {
                        return;
                    }

                    String msgData = gr.getMsgData();
                    LogUtil.easylog("BaiduPushNetwork->update->set:" + msgData);
                    if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                        return;
                    }
                }
            } else if (pkg.getRequestType() == IDUtils.ID_BAIDU_PUSH_READ) {
                mIsCommitSuccessLastTime = true;
                if (pkg instanceof GlobalMessagePackage) {
                    GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
                    MessageCommon gr = goodsTable.getResponse();
                    if (gr == null || gr.getMsgData() == null) {
                        return;
                    }

                    String msgData = gr.getMsgData();
                    LogUtil.easylog("BaiduPushNetwork->update->read:" + msgData);
                    if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                        return;
                    }
                }
            }


        } else {
            if (retMsg != null) {
                int reqCode = DataUtils.convertToInt(retMsg);
                LogUtil.easylog("BaiduPushNet->updateinfo->error:" + reqCode);
                if (reqCode == IDUtils.ID_BAIDU_PUSH_SET) {
                    mIsCommitSuccessLastTime = false;
                } else if (reqCode == IDUtils.ID_BAIDU_PUSH_READ) {

                }
            }
        }
    }
}
