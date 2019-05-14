package cn.emoney.acg.data.quiz;

import android.os.Handler;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizStatusPackage;
import cn.emoney.acg.data.protocol.quiz.QuizStatusReply.QuizStatus_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizStatusReply.QuizStatus_Reply.ItemStatus;
import cn.emoney.acg.data.protocol.quiz.QuizStatusRequest.QuizStatus_Request;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.NetworkManager.QuoteCallBack;
import cn.emoney.acg.util.LogUtil;

/**
 * @ClassName: QuestionStateObserver
 * @Description:问题状态监听器
 * @author xiechengfa
 * @date 2015年12月26日 下午2:19:47
 *
 */
public class QuestionStateObserver implements QuoteCallBack {
    private final static int DELAY_TIME = 5; // 秒钟

    private static QuestionStateObserver instance = null;
    private static Handler taskHandler = null;

    private boolean isRunning = false;
    private long questionId = 0;

    private Runnable mTask = null;
    private NetworkManager networkManager = null;
    private QuestionStateListener listener = null;

    private QuestionStateObserver() {
        networkManager = new NetworkManager(ACGApplication.getInstance(), null, this);
    }

    public static QuestionStateObserver getInstance() {
        if (instance == null) {
            instance = new QuestionStateObserver();
        }
        return instance;
    }

    public void setListener(QuestionStateListener listener) {
        this.listener = listener;
    }

    /**
     * 开始工作
     * 
     * @param questionId
     */
    public void startWork(long questionId) {
        isRunning = true;
        this.questionId = questionId;

        if (mTask == null || taskHandler == null) {
            taskHandler = new Handler();
            mTask = new Runnable() {
                @Override
                public void run() {
                    if (NetworkManager.IsNetworkAvailable() && isRunning) {
                        requestQuizStatus();
                    }

                    if (taskHandler != null) {
                        taskHandler.postDelayed(this, DELAY_TIME * 1000);
                    }
                }
            };

            taskHandler.post(mTask);
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
        questionId = 0;
        isRunning = false;

        if (taskHandler != null) {
            taskHandler.removeCallbacks(mTask);
            taskHandler = null;
        }

        if (mTask != null) {
            mTask = null;
        }
    }

    private void requestQuizStatus() {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        if (!userInfo.isLogined()) {
            stopWork();
            return;
        }

        LogUtil.easylog("*****************test query questionStatus runnable");
        QuizStatusPackage pkg = new QuizStatusPackage(new QuoteHead((short) IDUtils.ID_QUIZ_QRY_STATUS_REQ));
        QuizStatus_Request.Builder request = QuizStatus_Request.newBuilder();
        request.setTokenId(userInfo.getToken());
        request.addIds(questionId);
        pkg.setRequest(request.build());

        if (networkManager == null) {
            networkManager = new NetworkManager(ACGApplication.getInstance(), null, this);
        }
        networkManager.requestQuote(pkg, IDUtils.ID_QUIZ_QRY_STATUS_REQ, RequestUrl.host4);
    }

    @Override
    public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg) {
        if (retCode == 0) {
            if (pkg != null && pkg instanceof QuizStatusPackage) {
                QuizStatusPackage quizStatusPackage = (QuizStatusPackage) pkg;
                QuizStatus_Reply reply = quizStatusPackage.getResponse();
                int count = reply.getItemsCount();
                if (count > 0) {
                    ItemStatus item = reply.getItems(0);
                    if (listener != null) {
                        listener.onUpdateQuestionState(0, item, (int) reply.getServerTimestamp());
                    }
                }
            }
        } else {
            if (listener != null) {
                listener.onUpdateQuestionState(retCode, null, 0);
            }
        }
    }

    public interface QuestionStateListener {
        /**
         * 更新问题状态
         * 
         * @param itemStatus
         */
        public void onUpdateQuestionState(int retCode, ItemStatus itemStatus, int time);
    }
}
