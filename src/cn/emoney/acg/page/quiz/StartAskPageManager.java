package cn.emoney.acg.page.quiz;

import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizListPackage;
import cn.emoney.acg.data.protocol.quiz.QuizListReply.QuizList_Reply;
import cn.emoney.acg.data.quiz.QuizCommonRequest;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.data.quiz.QuizConfigData.QuizConfigListener;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.helper.NetworkManager.QuoteCallBack;
import cn.emoney.acg.page.PageImpl;

/**
 * 启动问股页面的管理器(用户问股一级页面和个股页面都会进入问股，所以把启动问股页面的逻辑集中处理)
 * 
 * @ClassName: StartAskPageManager
 * @Description:
 * @author xiechengfa
 * @date 2016年1月5日 下午5:24:13
 */
public class StartAskPageManager implements QuizConfigListener {
    private Goods goods = null;
    private PageImpl page = null;
    private QuizContentInfo myQuestion = null;
    private NetworkManager mNetworkManager = null;
    private QuizCommonRequest request = null;

    public void startAskPage(PageImpl page, QuizContentInfo myQuestion, boolean isGetMyQuestionStateOver, Goods goods) {
        this.page = page;
        this.myQuestion = myQuestion;
        this.goods = goods;

        if (myQuestion != null) {
            if (myQuestion.isQuestionOver()) {
                // 问题已结束
                startPageOfQuizCountInfo();
            } else {
                // 问题没有结束
                QuizResultPage.startPage(page, myQuestion, true);
            }
        } else {
            if (isGetMyQuestionStateOver) {
                // 没有问过问题
                startPageOfQuizCountInfo();
            } else {
                // 问题没有获取到,则重新获取
                requestMyQuestion();
            }
        }
    }

    // 检查问股次数
    private void startPageOfQuizCountInfo() {
        if (QuizConfigData.getInstance().isLoadOver()) {
            if (QuizConfigData.getInstance().getLeaveQuestionCount() > 0) {
                // 次数没用完
                QuizQuestionAskPage.startPage(page, goods);
            } else {
                // 次数已用完
                page.showTip("当日免费提问机会已用完");
            }
        } else {
            // 请求配置信息
            requestQuizConfig();
        }
    }

    // 请求配置信息
    private void requestQuizConfig() {
        QuizConfigData.getInstance().setListener(this);
        QuizConfigData.getInstance().loadServerConfigOfCallBack();
    }

    // 请求我的问题信息
    private void requestMyQuestion() {
        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager(ACGApplication.getInstance(), null, new QuoteCallBack() {

                @Override
                public void updateFromQuote(int retCode, String retMsg, QuotePackageImpl pkg) {
                    // TODO Auto-generated method stub
                    if (retCode == 0) {
                        if (pkg != null && pkg instanceof QuizListPackage) {
                            QuizListPackage dataPackage = (QuizListPackage) pkg;
                            QuizList_Reply reply = dataPackage.getResponse();
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

        if (request == null) {
            request = new QuizCommonRequest(null);
        }
        mNetworkManager.requestQuote(request.getQuizListPackage(1, 0, DataModule.getInstance().getUserInfo().getUid()), IDUtils.ID_QUIZ_MY_LIST_REQ, RequestUrl.host4);
    }

    // 解析返回的数据
    private void onGetDataSucc(QuizList_Reply reply) {
        if (reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
            // 成功
            if (reply != null && reply.getItemsCount() > 0) {
                myQuestion = QuizContentInfo.initOfServerItem(reply.getItems(0));
                if (myQuestion.isQuestionOver()) {
                    // 问题已结束
                    startPageOfQuizCountInfo();
                } else {
                    // 问题没有结束
                    QuizResultPage.startPage(page, myQuestion, true);
                }
            } else {
                // 最近没有问题
                QuizQuestionAskPage.startPage(page, goods);
            }
        } else {
            // 失败
            onGetFail();
        }
    }

    private void onGetFail() {
        page.showTip("请稍后再试");
    }

    /**
     * 获取剩余次数失败
     */
    public void onGetLeaveQuestionCountFail() {
        page.showTip("请稍后再试");
    }

    /**
     * 获取剩余次数成功
     * 
     * @param leaveCount
     */
    public void onGetLeaveQuestionCounSucc(int leaveCount) {
        if (leaveCount > 0) {
            QuizQuestionAskPage.startPage(page, goods);
        } else {
            // 次数已用完
            page.showTip("当日免费提问机会已用完");
        }
    }
}
