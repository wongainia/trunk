package cn.emoney.acg.page.quiz;

import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizStatusReply.QuizStatus_Reply.ItemStatus;
import cn.emoney.acg.data.protocol.quiz.QuizTakePackage;
import cn.emoney.acg.data.protocol.quiz.QuizTakeReply.QuizTake_Reply;
import cn.emoney.acg.data.quiz.QuestionQueryObserver;
import cn.emoney.acg.data.quiz.QuestionQueryObserver.QuestionQueryListener;
import cn.emoney.acg.data.quiz.QuestionStateObserver;
import cn.emoney.acg.data.quiz.QuestionStateObserver.QuestionStateListener;
import cn.emoney.acg.data.quiz.QuizCommonRequest;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.data.quiz.TeacherInfo;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.textviewlink.LinkManager;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.db.GlobalDBHelper;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: TeacherHomePage
 * @Description:老师抢问题页面
 * @author xiechengfa
 * @date 2015年12月15日 下午6:46:36
 */
public class TeacherHomePage extends PageImpl implements QuestionStateListener, QuestionQueryListener {
    public static boolean isQuestionOverOfReply = false;// 问题是否因为回复和超时结束
    private static final int QUESTION_OVER_SHOW_TIME = 5;// 问题被抢或结束，状态显示的时间（单位：S）
    private static final int MSG_REFRESH_QUESTION = 1;

    private final int TYPE_DROP = 101;// 放弃问题
    private final int TYPE_QUESTION_TAKE = 103;// 抢问题

    private int currType = TYPE_QUESTION_TAKE;

    private QuizCommonRequest request = null;
    private QuizContentInfo info = null;

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_teacher_home);

        request = new QuizCommonRequest(this);
        initContentLayout();
        bindPageTitleBar(R.id.pageTeacherHomeTitleBar);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        onResumeInit();
    }

    /**
     * 一级的resume只有调用QuizHomePage.onPageResume
     */
    public void onResumeOfSwitcher() {
        onResumeInit();
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        onPauseInit();
        super.onPagePause();
    }

    /**
     * 一级的resume只有调用QuizHomePage.onPagePause
     */
    public void onPauseOfSwitcher() {
        onPauseInit();
    }

    private void onResumeInit() {
        // 设置状态监听器
        QuestionStateObserver.getInstance().setListener(this);
        // 启动查询状态
        if (info != null) {
            QuestionStateObserver.getInstance().startWork(info.getId());
        }

        // 新问题
        loadNewData();

        // 刷问题的监听器
        QuestionQueryObserver.getInstance().setListener(this);
        if (getMyDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_ONLINE_STATE, true)) {
            QuestionQueryObserver.getInstance().setIsQuestionExist(true);
            QuestionQueryObserver.getInstance().startWork();
        } else {
            QuestionQueryObserver.getInstance().pauseWork();
        }

        onReusmeInitLayout();

        isQuestionOverOfReply = false;
    }

    private void loadNewData() {
        if (info == null && getMyDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_ONLINE_STATE, true)) {
            // 请求数据
            QuestionQueryObserver.getInstance().requestQueryQuestion();
        }
    }

    private void onPauseInit() {
        // 暂停查询状态
        QuestionStateObserver.getInstance().pauseWork();
        QuestionQueryObserver.getInstance().pauseWork();
    }

    @Override
    protected void onPageDestroy() {
        // TODO Auto-generated method stub

        // 停止查询状态
        QuestionStateObserver.getInstance().stopWork();
        QuestionQueryObserver.getInstance().stopWork();

        super.onPageDestroy();
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    // 放弃问题
    private void dropQuestion() {
        if (info == null) {
            return;
        }

        showLoading();
        this.currType = TYPE_DROP;
        request.onDropQuestion(info.getId(), TYPE_DROP);
        info = null;
    }

    // 抢问题
    private void takeQuestion() {
        DialogUtils.showProgressDialog(getContext(), "正在抢答...", null);

        this.currType = TYPE_QUESTION_TAKE;
        request.onTeacherTakeQuestion(info.getId(), getUserInfo().convertToTeacherBuilder(), TYPE_QUESTION_TAKE);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        // TODO Auto-generated method stub
        closeLoadingEvent(currType);

        if (pkg == null) {
            onGetDataFail(currType);
            return;
        }

        if (pkg.getRequestType() == TYPE_DROP) {
            // 放弃问题
            // 请求数据
            // QuizDropPackage dataPackage = (QuizDropPackage) pkg;
            // QuizDrop_Reply reply = dataPackage.getResponse();
            QuestionQueryObserver.getInstance().requestQueryQuestion();
        } else if (pkg.getRequestType() == TYPE_QUESTION_TAKE) {
            // 抢问题
            QuizTakePackage dataPackage = (QuizTakePackage) pkg;
            QuizTake_Reply reply = dataPackage.getResponse();

            if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                info.setTakeTime(reply.getTakeTime());
                info.setReplier(getUserInfo().converToTeacherInfo());
                TeacherReplayPage.startPage(this, info);
            } else {
                onGetDataFail(pkg.getRequestType());
            }
        }
    }

    @Override
    protected void updateWhenDecodeError(short type) {
        // TODO Auto-generated method stub
        closeLoadingEvent(type);
        onGetDataFail(type);
    }

    @Override
    protected void updateWhenNetworkError(short type) {
        // TODO Auto-generated method stub
        closeLoadingEvent(type);
        onGetDataFail(type);
    }

    private void closeLoadingEvent(int type) {
        if (type == TYPE_QUESTION_TAKE) {
            DialogUtils.closeProgressDialog();
        }
    }

    private void onGetDataFail(int type) {
        if (type == TYPE_DROP) {
            // 放弃失败
            // 请求数据
            QuestionQueryObserver.getInstance().requestQueryQuestion();
        } else if (type == TYPE_QUESTION_TAKE) {
            showTip("抢答失败,请稍后重试");
        }
    }

    private void onGetDataSucc(QuizContentInfo info) {
        setContentLayout();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        ImageView imageView = (ImageView) leftView.findViewById(R.id.backView);
        imageView.setImageResource(R.drawable.selector_titlebar_quiz_question_list);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "解答问股");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_search, null);
        ImageView searchImageView = (ImageView) rightItemView.findViewById(R.id.searchImageView);
        searchImageView.setImageResource(R.drawable.selector_titlebar_quiz_set);
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            // 列表
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            // 设置
            mPageChangeFlag = -1;
            startPage(DataModule.G_CURRENT_FRAME, new PageIntent(this, QuizSettingPage.class));
        }
    }

    private void showLoading() {
        closeContentLayout();
        closeNoDataLayout();

        findViewById(R.id.quizLoading).setVisibility(View.VISIBLE);
        TextView progressNotice = (TextView) findViewById(R.id.progressNotice);
        progressNotice.setText("正在获取新问题，请稍候...");
    }

    private void closeLoading() {
        findViewById(R.id.quizLoading).setVisibility(View.GONE);
    }

    // 显示无问题的layout
    private void showNoDataLayout() {
        findViewById(R.id.noDataLayout).setVisibility(View.VISIBLE);
        TextView noDataView = (TextView) findViewById(R.id.noDataView);
        noDataView.setTextColor(Util.getResourcesColor(R.color.t2));
        noDataView.setText("暂无问题需要回复");
    }

    // 隐藏无问题的layout
    private void closeNoDataLayout() {
        findViewById(R.id.noDataLayout).setVisibility(View.GONE);
    }

    private void showContentLayout() {
        findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);
    }

    private void closeContentLayout() {
        findViewById(R.id.contentLayout).setVisibility(View.GONE);
    }

    // 初始化UI
    private void onReusmeInitLayout() {
        TextView tipView = (TextView) getContentView().findViewById(R.id.setTipView);
        if (getMyDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_ONLINE_STATE, true)) {
            tipView.setVisibility(View.GONE);
            if (info != null) {
                if (isQuestionOverOfReply) {
                    // 问题结束，则立即查询新的问题
                    info = null;
                    QuestionQueryObserver.getInstance().requestQueryQuestion();
                } else {
                    initQuizBtnState();
                }
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            final String str = "您设置了在线状态为OFF，点击此处修改在线状态为ON才能收到问题";
            SpannableString ss = new SpannableString(str);
            ss.setSpan(new ForegroundColorSpan(RColor(R.color.c4)), 12, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipView.setText(ss);

            closeContentLayout();
            showNoDataLayout();
        }
    }

    private void initContentLayout() {
        findViewById(R.id.closeView).setOnClickListener(onClickEffectiveListener);
        findViewById(R.id.quizBtn).setOnClickListener(onClickEffectiveListener);
        closeContentLayout();
    }

    private void setContentLayout() {
        closeNoDataLayout();
        showContentLayout();

        // 内容
        TextView contentView = (TextView) findViewById(R.id.contentView);
        contentView.setText(info.getContent());
        // 加事件
        LinkManager.addStockLinkToTv(TeacherHomePage.this, contentView);

        // 头像
        ImageView iv = (ImageView) findViewById(R.id.headIV);
        Util.loadHeadIcon(iv, info.getOwner().getUid(), info.getOwner().getHeadId());

        // 昵称
        TextView nameView = (TextView) findViewById(R.id.nameView);
        nameView.setText(info.getOwner().getNickName());

        // 时间
        TextView dateView = (TextView) findViewById(R.id.dateView);
        dateView.setText(DateUtils.formatQuizCommitTime(info.getCommitTime()));

        // 按钮状态
        initQuizBtnState();
    }

    // 按钮状态
    private void initQuizBtnState() {
        TextView quizBtn = (TextView) findViewById(R.id.quizBtn);

        if ((info.getStatus() == QuizContentInfo.STATUS_ASK_ON && info.getReplier() != null && !getUserInfo().getUid().equals(info.getReplier().getId() + "")) || info.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE || info.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED) {
            // 问题被抢
            quizBtn.setBackgroundResource(R.drawable.selector_btn_normal);
            if (info.getReplier() != null && !TextUtils.isEmpty(info.getReplier().getNick())) {
                quizBtn.setText("问题已被" + info.getReplier().getNick() + "抢答");
            } else {
                quizBtn.setText("问题已被抢答");
            }

            // 5秒后再请求新的问题
            handler.sendEmptyMessageDelayed(MSG_REFRESH_QUESTION, QUESTION_OVER_SHOW_TIME * 1000);
        } else if (info.getStatus() == QuizContentInfo.STATUS_ASK_CLOSE) {
            // 问题结束
            quizBtn.setBackgroundResource(R.drawable.selector_btn_normal);
            quizBtn.setText("问题已超时");

            // 5秒后再请求新的问题
            handler.sendEmptyMessageDelayed(MSG_REFRESH_QUESTION, QUESTION_OVER_SHOW_TIME * 1000);
        } else {
            if (info.getReplier() != null && getUserInfo().getUid().equals(info.getReplier().getId() + "")) {
                // 继续解答
                quizBtn.setBackgroundResource(R.drawable.selector_btn_quiz_question_red);
                quizBtn.setText("继续解答");
            } else {
                quizBtn.setBackgroundResource(R.drawable.selector_btn_normal);
                quizBtn.setText("立即抢答");
            }
        }
    }

    private OnClickEffectiveListener onClickEffectiveListener = new OnClickEffectiveListener() {

        @Override
        public void onClickEffective(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.closeView:
                    // 关闭
                    dropQuestion();
                    break;
                case R.id.quizBtn:
                    // 抢问题
                    if (info.getReplier() != null && getUserInfo().getUid().equals(info.getReplier().getId() + "")) {
                        // 继续回答
                        TeacherReplayPage.startPage(TeacherHomePage.this, info);
                    } else {
                        // 抢问题
                        // 设置抢问的人
                        takeQuestion();
                    }
                    break;
            }
        }
    };

    private GlobalDBHelper mDBHelper = null;

    public GlobalDBHelper getMyDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new GlobalDBHelper(ACGApplication.getInstance(), DataModule.DB_GLOBAL);
        }
        return mDBHelper;
    }

    /**
     * 更新问题状态
     * 
     * @param itemStatus
     */
    public void onUpdateQuestionState(int retCode, ItemStatus itemStatus, int time) {
        if (info != null && itemStatus != null && info.getId() == itemStatus.getId()) {
            if (info.getStatus() != itemStatus.getStatus()) {
                // 状态有变化
                info.setStatus(itemStatus.getStatus());
                if (itemStatus.getReplier() != null) {
                    TeacherInfo teacherInfo = new TeacherInfo();
                    teacherInfo.initOfServerTeacher(itemStatus.getReplier());
                    info.setReplier(teacherInfo);
                }

                initQuizBtnState();
            }

            if (itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_CLOSE || itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE || itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED) {
                // 状态到了用户要处理状态，别人无法修改这问题的状态，则停止查询
                QuestionStateObserver.getInstance().stopWork();
            }
        }
    }

    /**
     * 更新问题
     * 
     * @param tempInfo
     */
    public void onUpdateQuestion(QuizContentInfo tempInfo) {
        if (info != null) {
            return;
        }

        closeLoading();
        this.info = tempInfo;
        onGetDataSucc(tempInfo);

        // 启动查询状态
        QuestionStateObserver.getInstance().startWork(tempInfo.getId());
        QuestionQueryObserver.getInstance().setIsQuestionExist(true);
    }

    /**
     * 更新开始
     */
    public void onUpdateStart() {
        if (info != null) {
            return;
        }

        showLoading();
    }

    /**
     * 更新结束
     */
    public void onUpdateFail() {
        if (info != null) {
            return;
        }

        QuestionQueryObserver.getInstance().setIsQuestionExist(false);
        closeLoading();
        showNoDataLayout();
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_QUESTION:
                    // 刷新问题(问题被抢或超时)
                    info = null;
                    QuestionQueryObserver.getInstance().requestQueryQuestion();
                    break;
            }
        };
    };
}
