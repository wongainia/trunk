package cn.emoney.acg.data.quiz;

import android.text.TextUtils;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizConfigPackage;
import cn.emoney.acg.data.protocol.quiz.QuizConfigReply.QuizConfig_Reply;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.NetworkManager.QuoteCallBack;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.db.GlobalDBHelper;

/**
 * @ClassName: QuizConfigData
 * @Description:问股的配置信息
 * @author xiechengfa
 * @date 2015年12月24日 下午2:55:49
 *
 */
public class QuizConfigData {
    private static QuizConfigData mInstance = null;

    private final int STATE_NO = 0;// 没有加载
    private final int STATE_LOADING = 1;// 正在加载
    private final int STATE_SUCC = 2;// 加载成功

    private int currState = STATE_NO;// 是否加载服务的配置
    private int qustionLifeTime = 0;// 存活时间,单位：秒,默认：3600
    private int qustionLimitCount = 0; // 每日提问上限，,默认：2
    private int teacherHandleTime = 0;// 老师锁定问题的时间,单位：秒，,默认：600

    private int currQustionCount = 0;// 用户当日已提问有效次数

    private boolean isCallBack = false;
    private GlobalDBHelper mDbHelper;
    private NetworkManager mNetworkManager = null;
    private QuizConfigListener listener = null;
    private QuizCommonRequest quizCommonRequest = null;

    private QuizConfigData() {
        String configInfo = getDBHelper().getString(DataModule.G_KEY_QUIZ_CONFIGINFO, null);
        if (!TextUtils.isEmpty(configInfo)) {
            String[] configInfoArr = configInfo.split("\\|");
            if (configInfoArr != null && configInfoArr.length == 3) {
                qustionLifeTime = DataUtils.convertToInt(configInfoArr[0]);
                qustionLimitCount = DataUtils.convertToInt(configInfoArr[0]);
                teacherHandleTime = DataUtils.convertToInt(configInfoArr[0]);
            }
        }

        if (qustionLifeTime == 0) {
            qustionLifeTime = 3600;
        }

        if (qustionLimitCount == 0) {
            qustionLimitCount = 2;
        }

        if (teacherHandleTime == 0) {
            teacherHandleTime = 600;
        }
    }

    public static QuizConfigData getInstance() {
        if (mInstance == null) {
            mInstance = new QuizConfigData();
        }

        return mInstance;
    }

    /**
     * 
     * onResume设置监听器，onPause取消监听器
     * 
     * @param listener
     */
    public void setListener(QuizConfigListener listener) {
        this.listener = listener;
    }

    /**
     * 重置加载状态
     */
    public void resestLoadState() {
        currState = STATE_NO;
    }

    /**
     * 是否加载成功
     * 
     * @return
     */
    public boolean isLoadOver() {
        return currState == STATE_SUCC ? true : false;
    }

    /**
     * 加载Server的配置信息
     */
    public void loadServerConfigOfCallBack() {
        isCallBack = true;
        requestConfig();
    }

    /**
     * 加载Server的配置信息
     */
    public void loadServerConfig() {
        isCallBack = false;
        requestConfig();
    }

    public int getQustionLifeTime() {
        // 加载Server的配置信息
        if (currState == STATE_NO) {
            loadServerConfig();
        }

        return qustionLifeTime;
    }

    /**
     * 每日提问上限
     * 
     * @return
     */
    public int getQustionLimitCount() {
        // 加载Server的配置信息
        if (currState == STATE_NO) {
            loadServerConfig();
        }

        return qustionLimitCount;
    }

    /**
     * 老师锁定问题的时间
     * 
     * @return
     */
    public int getTeacherHandleTime() {
        // 加载Server的配置信息
        if (currState == STATE_NO) {
            loadServerConfig();
        }

        return teacherHandleTime;
    }

    /**
     * 获取当日剩余的次数
     * 
     * @return
     */
    public int getLeaveQuestionCount() {
        if (currState == STATE_NO) {
            loadServerConfig();
        }

        int leaveCountCount = qustionLimitCount - currQustionCount;
        return leaveCountCount > 0 ? leaveCountCount : 0;
    }

    public void setCurrQustionCount(int currQustionCount) {
        this.currQustionCount = currQustionCount;
    }

    /**
     * 发起问题成功，更新次数
     */
    public void updateCurrQuestionCountOfSendSucc() {
        currQustionCount = currQustionCount + 1;
    }

    private void requestConfig() {
        if (currState == STATE_LOADING) {
            return;
        }

        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager(ACGApplication.getInstance(), null, new QuoteCallBack() {

                @Override
                public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg) {
                    // TODO Auto-generated method stub
                    if (retCode == 0) {
                        if (pkg != null && pkg instanceof QuizConfigPackage) {
                            QuizConfigPackage dataPackage = (QuizConfigPackage) pkg;
                            QuizConfig_Reply reply = dataPackage.getResponse();
                            if (reply != null) {
                                onGetDataSucc(reply);
                            } else {
                                onGetFail();
                            }
                        } else {
                            onGetFail();
                        }
                    } else {
                        onGetFail();
                    }
                }
            });
        }

        // 请求
        currState = STATE_LOADING;
        // QuizConfigPackage pkg = new QuizConfigPackage(new QuoteHead((short)
        // IDUtils.ID_QUIZ_CONFIG_REQ));
        // QuizConfig_Request.Builder request = QuizConfig_Request.newBuilder();
        // if (DataModule.getInstance().getUserInfo().isLogined()) {
        // request.setTokenId(DataModule.getInstance().getUserInfo().getToken());
        // } else {
        // request.setTokenId(DataModule.G_GUEST_TOKEN);
        // }
        // pkg.setRequest(request.build());

        if (quizCommonRequest == null) {
            quizCommonRequest = new QuizCommonRequest(null);
        }
        mNetworkManager.requestQuote(quizCommonRequest.getConfigPackage(), IDUtils.ID_QUIZ_CONFIG_REQ, RequestUrl.host4);
    }

    // 解析返回的数据
    private void onGetDataSucc(QuizConfig_Reply reply) {
        if (reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
            currState = STATE_SUCC;

            qustionLifeTime = reply.getLifeCycle();
            qustionLimitCount = reply.getDailyAskTimes();
            teacherHandleTime = reply.getHandleTimeout();
            currQustionCount = reply.getDailyInvoked();

            DataModule.G_LOCAL_SERVER_TIME_GAP = DateUtils.getTimeStamp() / 1000 - reply.getServerTimestamp();

            // 保存
            getDBHelper().setString(DataModule.G_KEY_QUIZ_CONFIGINFO, qustionLifeTime + "|" + qustionLimitCount + "|" + teacherHandleTime);

            if (isCallBack && listener != null) {
                listener.onGetLeaveQuestionCounSucc(getLeaveQuestionCount());
            }

            LogUtil.easylog("*************quizConfigInfo qustionLifeTime:" + qustionLifeTime + ",qustionLimitCount:" + qustionLimitCount + ",teacherHandleTime:" + teacherHandleTime + ",currQustionCount:" + currQustionCount + ",G_LOCAL_SERVER_TIME_GAP:" + DataModule.G_LOCAL_SERVER_TIME_GAP);
        } else {
            onGetFail();
        }
    }

    private void onGetFail() {
        currState = STATE_NO;

        if (isCallBack && listener != null) {
            listener.onGetLeaveQuestionCountFail();
        }
    }

    private GlobalDBHelper getDBHelper() {
        if (mDbHelper == null) {
            mDbHelper = new GlobalDBHelper(ACGApplication.getInstance(), DataModule.DB_GLOBAL);
        }
        return mDbHelper;
    }

    public interface QuizConfigListener {
        /**
         * 获取剩余次数失败
         */
        public void onGetLeaveQuestionCountFail();

        /**
         * 获取剩余次数成功
         * 
         * @param leaveCount
         */
        public void onGetLeaveQuestionCounSucc(int leaveCount);
    }
}
