package cn.emoney.acg.data.quiz;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizQueryPackage;
import cn.emoney.acg.data.protocol.quiz.QuizQueryReply.QuizQuery_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizQueryRequest.QuizQuery_Request;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.NetworkManager.QuoteCallBack;

/**
 * @ClassName: QuestionQueryObserver
 * @Description:刷问题监听器
 * @author xiechengfa
 * @date 2015年12月26日 下午2:19:47
 *
 */
public class QuestionQueryObserver implements QuoteCallBack {
    private final static int REQUEST_DELAY_TIME = 10 * 1000; // 刷问题的频率,单位：秒
    private final static int CALLBACK_DEYLAY_TIME = 1 * 1000;// 回调的延时(延时1秒，因为网络回调很快，loading会一闪消失，所以做了1秒的延时)

    private final static int TYPE_START = 1;// 开始加载
    private final static int TYPE_SUCC = 2;// 加载成功
    private final static int TYPE_FAIL = 3;// 加载失败

    private static Handler taskHandler = null;
    private static QuestionQueryObserver instance = null;

    private boolean isQuestionExist = false;// 问题是否存在
    private boolean isRunning = false;

    private Runnable mTask = null;
    private NetworkManager networkManager = null;
    private QuestionQueryListener listener = null;

    private QuestionQueryObserver() {
        networkManager = new NetworkManager(ACGApplication.getInstance(), null, this);
    }

    public static QuestionQueryObserver getInstance() {
        if (instance == null) {
            instance = new QuestionQueryObserver();
        }
        return instance;
    }

    public void setListener(QuestionQueryListener listener) {
        this.listener = listener;
    }

    public void setIsQuestionExist(boolean isQuestionExist) {
        this.isQuestionExist = isQuestionExist;
    }

    /**
     * 开始工作
     */
    public void startWork() {
        isRunning = true;

        if (mTask == null || taskHandler == null) {
            taskHandler = new Handler();
            mTask = new Runnable() {
                @Override
                public void run() {
                    if (NetworkManager.IsNetworkAvailable() && isRunning && !isQuestionExist) {
                        requestQueryQuestion();
                    }

                    if (taskHandler != null) {
                        taskHandler.postDelayed(this, REQUEST_DELAY_TIME);
                    }
                }
            };

            taskHandler.postDelayed(mTask, REQUEST_DELAY_TIME);
        }
    }

    /**
     * 暂停工作
     */
    public void pauseWork() {
        isRunning = false;
    }

    /**
     * 停止工作
     */
    public void stopWork() {
        isRunning = false;

        if (taskHandler != null) {
            taskHandler.removeCallbacks(mTask);
            taskHandler = null;
        }

        if (mTask != null) {
            mTask = null;
        }
    }

    // 请求问题
    public void requestQueryQuestion() {
        // 开始
        myHandler.sendEmptyMessage(TYPE_START);

        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        if (!userInfo.isLogined() || !userInfo.isRoleTeacher()) {
            // 没有登录或非老师端，停止刷问题
            stopWork();
            myHandler.sendEmptyMessageDelayed(TYPE_FAIL, CALLBACK_DEYLAY_TIME);
            return;
        }

        QuizQueryPackage pkg = new QuizQueryPackage(new QuoteHead((short) IDUtils.ID_QUIZ_QUERY_REQ));
        QuizQuery_Request.Builder request = QuizQuery_Request.newBuilder();
        request.setTokenId(userInfo.getToken());
        pkg.setRequest(request.build());

        if (networkManager == null) {
            networkManager = new NetworkManager(ACGApplication.getInstance(), null, this);
        }
        networkManager.requestQuote(pkg, IDUtils.ID_QUIZ_QUERY_REQ, RequestUrl.host4);
    }

    @Override
    public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg) {
        if (retCode == 0) {
            QuizQueryPackage dataPackage = (QuizQueryPackage) pkg;
            QuizQuery_Reply reply = dataPackage.getResponse();
            if (reply != null && reply.getItemsCount() > 0) {
                // 成功
                QuizContentInfo info = QuizContentInfo.initOfServerQuizItem(reply.getItems(0));
                // if (info.getStatus() == QuizContentInfo.STATUS_ASK_WAIT || info.getStatus() ==
                // QuizContentInfo.STATUS_ASK_WAIT2 || info.getStatus() ==
                // QuizContentInfo.STATUS_ASK_ON) {
                Message msg = Message.obtain();
                msg.what = TYPE_SUCC;
                msg.obj = info;
                myHandler.sendMessageDelayed(msg, CALLBACK_DEYLAY_TIME);
                // } else {
                // // 失败
                // myHandler.sendEmptyMessageDelayed(TYPE_FAIL, CALLBACK_DEYLAY_TIME);
                // }
            } else {
                // 失败
                myHandler.sendEmptyMessageDelayed(TYPE_FAIL, CALLBACK_DEYLAY_TIME);
            }
        } else {
            // 失败
            myHandler.sendEmptyMessageDelayed(TYPE_FAIL, CALLBACK_DEYLAY_TIME);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (listener == null) {
                return;
            }

            switch (msg.what) {
                case TYPE_START:
                    // 开始
                    listener.onUpdateStart();
                    break;
                case TYPE_SUCC:
                    // 成功
                    QuizContentInfo info = (QuizContentInfo) msg.obj;
                    listener.onUpdateQuestion(info);
                    break;
                case TYPE_FAIL:
                    // 失败
                    listener.onUpdateFail();
                    break;
            }
        };
    };

    public interface QuestionQueryListener {
        /**
         * 更新问题
         * 
         * @param info
         */
        public void onUpdateQuestion(QuizContentInfo info);

        /**
         * 更新开始
         */
        public void onUpdateStart();

        /**
         * 更新结束
         */
        public void onUpdateFail();
    }
}
