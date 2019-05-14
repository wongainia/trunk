package cn.emoney.acg.data.quiz;

import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.quiz.QuizAnswerPackage;
import cn.emoney.acg.data.protocol.quiz.QuizAnswerRequest.QuizAnswer_Request;
import cn.emoney.acg.data.protocol.quiz.QuizAppraisePackage;
import cn.emoney.acg.data.protocol.quiz.QuizAppraiseRequest.QuizAppraise_Request;
import cn.emoney.acg.data.protocol.quiz.QuizConfigPackage;
import cn.emoney.acg.data.protocol.quiz.QuizConfigRequest.QuizConfig_Request;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Answer;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Teacher;
import cn.emoney.acg.data.protocol.quiz.QuizDropPackage;
import cn.emoney.acg.data.protocol.quiz.QuizDropRequest.QuizDrop_Request;
import cn.emoney.acg.data.protocol.quiz.QuizListPackage;
import cn.emoney.acg.data.protocol.quiz.QuizListRequest.QuizList_Request;
import cn.emoney.acg.data.protocol.quiz.QuizQueryPackage;
import cn.emoney.acg.data.protocol.quiz.QuizQueryRequest.QuizQuery_Request;
import cn.emoney.acg.data.protocol.quiz.QuizRelatePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRelateRequest.QuizRalate_Request;
import cn.emoney.acg.data.protocol.quiz.QuizRequirePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRequireRequest.QuizRequire_Request;
import cn.emoney.acg.data.protocol.quiz.QuizStatusPackage;
import cn.emoney.acg.data.protocol.quiz.QuizStatusRequest.QuizStatus_Request;
import cn.emoney.acg.data.protocol.quiz.QuizTakePackage;
import cn.emoney.acg.data.protocol.quiz.QuizTakeRequest.QuizTake_Request;
import cn.emoney.acg.data.protocol.quiz.TeacherDetailPackage;
import cn.emoney.acg.data.protocol.quiz.TeacherDetailRequest.TeacherDetail_Request;
import cn.emoney.acg.data.protocol.quiz.TeacherStatusPackage;
import cn.emoney.acg.data.protocol.quiz.TeacherStatusRequest.TeacherStatus_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;

/**
 * @ClassName: QuizCommonRequest
 * @Description:问股协议请求类
 * @author xiechengfa
 * @date 2015年12月21日 上午10:08:48
 *
 */
public class QuizCommonRequest {
    PageImpl mPI = null;

    public QuizCommonRequest(PageImpl pi) {
        mPI = pi;
    }

    /**
     * 提交问题
     * 
     * @param content
     */
    public void onCommitQuestionRequest(String content, String stockStr) {
        QuizRequirePackage pkg = new QuizRequirePackage(new QuoteHead((short) IDUtils.ID_QUIZ_REQUIRE_REQ));
        QuizRequire_Request.Builder request = QuizRequire_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.setUserNick(mPI.getUserInfo().getConvertNickName());
        request.setUserIcon(mPI.getUserInfo().getHeadId());
        request.setContent(content);
        request.setStocks(stockStr);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_REQUIRE_REQ, RequestUrl.host4);
    }

    /**
     * 老师刷问题
     */
    public void onTeacherRefreshQuestion(int type) {
        QuizQueryPackage pkg = new QuizQueryPackage(new QuoteHead((short) type));
        QuizQuery_Request.Builder request = QuizQuery_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_QUERY_REQ, RequestUrl.host4);
    }

    /**
     * 老师抢答问题
     */
    public void onTeacherTakeQuestion(long questionId, Teacher.Builder info, int type) {
        QuizTakePackage pkg = new QuizTakePackage(new QuoteHead((short) type));
        QuizTake_Request.Builder request = QuizTake_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.setId(questionId);
        request.setReplier(info);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_TAKE_REQ, RequestUrl.host4);
    }

    /**
     * 抢到之后放弃回答
     */
    public void onDropQuestion(long questionId, int type) {
        LogUtil.easylog("***************test onDropQuestion");
        QuizDropPackage pkg = new QuizDropPackage(new QuoteHead((short) type));
        QuizDrop_Request.Builder request = QuizDrop_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.setId(questionId);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_DROP_REQ, RequestUrl.host4);
    }

    /**
     * 老师回复
     */
    public void onAnswerQuestion(long questionId, Answer.Builder answer, String stock, int TYPE_SEND) {
        LogUtil.easylog("***************test onAnswerQuestion ");
        QuizAnswerPackage pkg = new QuizAnswerPackage(new QuoteHead((short) TYPE_SEND));
        QuizAnswer_Request.Builder request = QuizAnswer_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.setId(questionId);
        request.setAnswer(answer);
        request.setStocks(stock);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_ANSWER_REQ, RequestUrl.host4);
    }

    /**
     * 评价
     */
    public void onQppraiseQuestion(long questionId, int lev, int type) {
        LogUtil.easylog("***************test onAnswerQuestion");
        QuizAppraisePackage pkg = new QuizAppraisePackage(new QuoteHead((short) type));
        QuizAppraise_Request.Builder request = QuizAppraise_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.setId(questionId);
        request.setLevel(lev);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_APPRAISE_REQ, RequestUrl.host4);
    }

    /**
     * 请求配置数据
     */
    public void onConfigRequest() {
        mPI.requestQuote(getConfigPackage(), IDUtils.ID_QUIZ_CONFIG_REQ, RequestUrl.host4);
    }

    public QuizConfigPackage getConfigPackage() {
        QuizConfigPackage pkg = new QuizConfigPackage(new QuoteHead((short) IDUtils.ID_QUIZ_CONFIG_REQ));
        QuizConfig_Request.Builder request = QuizConfig_Request.newBuilder();
        if (DataModule.getInstance().getUserInfo().isLogined()) {
            request.setTokenId(DataModule.getInstance().getUserInfo().getToken());
        } else {
            request.setTokenId(DataModule.G_GUEST_TOKEN);
        }
        pkg.setRequest(request.build());

        return pkg;
    }

    /**
     * 我的问题列表
     */
    public void onMyList(int count, int lastId, String userId) {
        LogUtil.easylog("***************test onMyList");
        mPI.requestQuote(getQuizListPackage(count, lastId, userId), IDUtils.ID_QUIZ_MY_LIST_REQ, RequestUrl.host4);
    }

    public QuizListPackage getQuizListPackage(int count, int lastId, String userId) {
        QuizListPackage pkg = new QuizListPackage(new QuoteHead((short) IDUtils.ID_QUIZ_MY_LIST_REQ));
        QuizList_Request.Builder request = QuizList_Request.newBuilder();
        request.setTokenId(DataModule.getInstance().getUserInfo().getToken());
        request.setUserId(DataUtils.convertToLong(userId));
        request.setNeedCount(count);
        request.setLastId(lastId);
        pkg.setRequest(request.build());

        return pkg;
    }

    /**
     * 相关问题列表
     */
    public void onRelateList(String stocks, int count, int lastId, int requestID) {
        LogUtil.easylog("***************test onRelateList stocks:");
        QuizRelatePackage pkg = new QuizRelatePackage(new QuoteHead((short) requestID));
        QuizRalate_Request.Builder request = QuizRalate_Request.newBuilder();
        if (mPI.isLogined()) {
            request.setTokenId(mPI.getUserInfo().getToken());
        } else {
            request.setTokenId(DataModule.G_GUEST_TOKEN);
        }

        request.setStocks(stocks);
        request.setNeedCount(count);
        request.setLastId(lastId);
        pkg.setRequest(request.build());

        LogUtil.easylog("**************token:" + request.getTokenId() + ",stock:" + request.getStocks() + ",needCount:" + request.getNeedCount() + ",lastId:" + request.getLastId());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_RELATE_LIST_REQ, RequestUrl.host4);
    }

    /**
     * 查询状态
     */
    public void onQryStatusList(int questionId) {
        LogUtil.easylog("***************test onQryStatusList");
        QuizStatusPackage pkg = new QuizStatusPackage(new QuoteHead((short) IDUtils.ID_QUIZ_QRY_STATUS_REQ));
        QuizStatus_Request.Builder request = QuizStatus_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        request.addIds(questionId);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_QRY_STATUS_REQ, RequestUrl.host4);
    }

    /**
     * 查询老师详情
     */
    public void onQryTeacherDetail(int teacherId) {
        LogUtil.easylog("***************test onQryTeacherDetail");
        TeacherDetailPackage pkg = new TeacherDetailPackage(new QuoteHead((short) IDUtils.ID_QUIZ_TEACHER_DETAIL_REQ));
        TeacherDetail_Request.Builder request = TeacherDetail_Request.newBuilder();
        request.setTeacherId(teacherId);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_TEACHER_DETAIL_REQ, RequestUrl.host4);
    }

    /**
     * 老师设置
     */
    public void onTeacherSetting(boolean isOn) {
        LogUtil.easylog("***************test onTeacherSetting");
        TeacherStatusPackage pkg = new TeacherStatusPackage(new QuoteHead((short) IDUtils.ID_QUIZ_TEACHER_SETTING_REQ));
        TeacherStatus_Request.Builder request = TeacherStatus_Request.newBuilder();
        request.setTokenId(mPI.getUserInfo().getToken());
        // online:1, offline:2
        request.setTag(isOn ? 0 : 1);
        pkg.setRequest(request.build());

        mPI.requestQuote(pkg, IDUtils.ID_QUIZ_TEACHER_SETTING_REQ, RequestUrl.host4);
    }
}
