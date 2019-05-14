package cn.emoney.acg.page.quiz;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizAppraisePackage;
import cn.emoney.acg.data.protocol.quiz.QuizAppraiseReply.QuizAppraise_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.protocol.quiz.QuizListPackage;
import cn.emoney.acg.data.protocol.quiz.QuizListReply.QuizList_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizRelatePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRelateReply.QuizRalate_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizStatusReply.QuizStatus_Reply.ItemStatus;
import cn.emoney.acg.data.quiz.AnswerInfo;
import cn.emoney.acg.data.quiz.QuestionStateObserver;
import cn.emoney.acg.data.quiz.QuestionStateObserver.QuestionStateListener;
import cn.emoney.acg.data.quiz.QuizCommonRequest;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.data.quiz.QuizItemInfo;
import cn.emoney.acg.data.quiz.TeacherInfo;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.media.AppMediaPlayerManager;
import cn.emoney.acg.media.AudioRecordCacheManager;
import cn.emoney.acg.media.AudioRecordCacheManager.AudioCacheBackListener;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.main.MainPage;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

/**
 * @ClassName: QuizListImpl
 * @Description:问股的列表页面的实现类，供QuizResultPage和QuizLatestListPage使用，这样维护代码方便
 * @author xiechengfa
 * @date 2015年12月18日 上午11:11:13
 *
 */
public class QuizListImpl implements QuizListViewlListener, MyMediaPlayerListener, OnClickListener, PostScrollListener, QuestionStateListener, AudioCacheBackListener {
    public final static int TYPE_LATEST = 1;// 最近列表
    public final static int TYPE_RESULT = 2;// 结果列表

    private static final int REQUEST_TYPE_FIRSTPAGE = 0;// 加载第一页数据
    private static final int REQUEST_TYPE_MORE = 1;// 加载更多
    private static final int REQUEST_TYPE_REFRESH = 2;// 下拉刷新
    private static final int REQUEST_TYPE_APPRAISE = 3;// 评价

    private static final int QUIZ_LIST_SIZE = 10;
    private static final int QUIZ_LIST_MAX_SIZE = 5 * QUIZ_LIST_SIZE;

    private boolean isFromHomePage = false;
    private boolean isGetListDataSucc = false;
    private boolean isResetListViewScrollState = false;// 重置listview的滚动状态
    private boolean isGetMyQuestionStateOver = false;// 获取我的问题是否成功(表示返回状态成功，可能有问题可能没有问题)

    private int type = TYPE_LATEST;
    private int playPos = -1;
    private int appraiseLevel = 0;
    private long playRecordId = 0;

    private RefreshListView listView = null;
    private QuizListAdapter adapter = null;
    private AppMediaPlayerManager mediaPlayerManager = null;
    private ArrayList<QuizItemInfo> listDatas = new ArrayList<QuizItemInfo>();

    // 加载更多的定义
    private boolean isRequestDataing = false;// 是请求数据完成
    private boolean isLoadOver = false;// 所以数据加载完成，没有更多数据
    private int currRequestType = REQUEST_TYPE_FIRSTPAGE;

    // footerview
    private View footerView = null;
    private LinearLayout footerMoreLayout;
    private TextView footerMoreText;

    private PageImpl page = null;
    private QuizContentInfo myQuestion = null;
    private QuizCommonRequest request = null;
    private IntentFilter mIntentFilter = null;

    private StartAskPageManager startAskPageManager = null;

    public QuizListImpl(PageImpl page) {
        this.page = page;
        request = new QuizCommonRequest(page);
    }

    protected void initPage() {
        // TODO Auto-generated method stub
        Bundle bundle = page.getArguments();
        if (bundle != null && bundle.containsKey(QuizResultPage.INTENT_TYPE)) {
            type = bundle.getInt(QuizResultPage.INTENT_TYPE);
        }

        if (bundle != null && bundle.containsKey(QuizResultPage.INTENT_IS_FROM_HOME)) {
            isFromHomePage = bundle.getBoolean(QuizResultPage.INTENT_IS_FROM_HOME);
        }

        if (bundle != null && bundle.containsKey(QuizResultPage.INTENT_ITEM)) {
            myQuestion = (QuizContentInfo) bundle.getSerializable(QuizResultPage.INTENT_ITEM);
        }

        page.setContentView(R.layout.page_quiz);

        initTitle();
        initFooterView();
        initListView();
        registerBroadcast();

        page.bindPageTitleBar(R.id.pageQuizTitleBar);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回键
            onKeyBackEvent();
            return true;
        }

        return page.onKeyUp(keyCode, event);
    }

    protected void onPageResume() {
        // 设置当前的媒体类型
        page.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (mediaPlayerManager != null) {
            mediaPlayerManager.setPageState(true);
        }

        // 设置状态监听器
        QuestionStateObserver.getInstance().setListener(this);
        // 启动查询状态
        if (myQuestion != null) {
            QuestionStateObserver.getInstance().startWork(myQuestion.getId());
        }

        AudioRecordCacheManager.getInstance().setListener(this);

        // 加载初始化的数据
        loadInitData();

        resetPlayVoiceState();

        notifyListViewDataChange();

        // 重置listview的位置
        if (isResetListViewScrollState && listView != null) {
            isResetListViewScrollState = false;
            listView.setSelection(0);
        }
    }

    protected void onPagePause() {
        // TODO Auto-generated method stub
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onPause();
            mediaPlayerManager.setPageState(false);
        }

        // 停止查询状态
        QuestionStateObserver.getInstance().stopWork();

        QuizConfigData.getInstance().setListener(null);

        AudioRecordCacheManager.getInstance().setListener(null);

        // 设置当前的媒体类型
        page.getActivity().setVolumeControlStream(AudioManager.STREAM_RING);
    }

    protected void onPageDestroy() {
        // TODO Auto-generated method stub
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
            mediaPlayerManager = null;
        }

        unRegisterBroadcast();

        if (adapter != null) {
            adapter.recycleCountDownTimer();
        }
    }

    private void initListView() {
        listView = (RefreshListView) page.findViewById(R.id.quizeListView);
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.addFooterView(footerView, null, false);
        listView.setPostScrollListener(this);
        listView.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                currRequestType = REQUEST_TYPE_REFRESH;
                requestListQuestion();
            }

            @Override
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() {}
        });

        // 设置没数据时不能下拉刷新
        listView.setRefreshable(false);

        adapter = new QuizListAdapter(page, this);
        adapter.setData(listDatas);
        listView.setAdapter(adapter);
    }

    protected void initData() {
        // TODO Auto-generated method stub
    }

    private void loadInitData() {
        // 我的问题
        requestMyQuestion();

        if (!isGetListDataSucc) {
            currRequestType = REQUEST_TYPE_FIRSTPAGE;
            requestListQuestion();
        }
    }

    // 我的问题
    private void requestMyQuestion() {
        if (type == TYPE_LATEST && page.isLogined() && myQuestion == null) {
            isGetMyQuestionStateOver = false;
            request.onMyList(1, 0, page.getUserInfo().getUid());
        }
    }

    // 列表问题
    private void requestListQuestion() {
        if (isRequestDataing) {
            return;
        }

        if (type == TYPE_LATEST) {
            // 最近列表
            isRequestDataing = true;
            int lastId = 0;
            if (currRequestType == REQUEST_TYPE_MORE) {
                lastId = (int) listDatas.get(listDatas.size() - 1).getQuizItem().getId();
            }

            showLoadingEvent();
            request.onRelateList("", QUIZ_LIST_SIZE, lastId, currRequestType);
        } else if (type == TYPE_RESULT) {
            // 相关列表
            if (listDatas == null) {
                listDatas = new ArrayList<QuizItemInfo>();
            }

            if (myQuestion != null && listDatas.size() <= 0) {
                QuizItemInfo itemInfo = new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, myQuestion);
                itemInfo.getQuizItem().setMyLatestQuestion(true);
                addInfoItem(itemInfo, true);
                adapter.setData(listDatas);
                notifyListViewDataChange();
            }

            if (myQuestion != null && !TextUtils.isEmpty(myQuestion.getStock())) {
                // 有相关的股票
                isRequestDataing = true;
                int lastId = 0;
                if (currRequestType == REQUEST_TYPE_MORE) {
                    lastId = (int) listDatas.get(listDatas.size() - 1).getQuizItem().getId();
                }

                showLoadingEvent();
                request.onRelateList(getRelateStock(myQuestion.getStock()), QUIZ_LIST_SIZE, lastId, currRequestType);
            } else {
                // 无相关的股票
                onGetDataFail();
            }
        }
    }

    // 解析数据
    public void updateFromQuote(QuotePackageImpl pkg) {
        // TODO Auto-generated method stub
        try {
            LogUtil.easylog("************************return udpateFromQuote");
            closeLoadingEvent();

            if (pkg == null) {
                onGetDataFail();
                return;
            }

            if (pkg.getRequestType() == REQUEST_TYPE_FIRSTPAGE || pkg.getRequestType() == REQUEST_TYPE_MORE) {
                // 初始化和更多的数据
                QuizRelatePackage dataPackage = (QuizRelatePackage) pkg;
                QuizRalate_Reply reply = dataPackage.getResponse();

                if (reply != null) {
                    LogUtil.easylog("***************test reply count:" + reply.getItemsCount());
                }

                if (reply != null && reply.getItemsCount() > 0) {
                    // 组装数据
                    isGetListDataSucc = true;
                    onGetDataSucc(reply.getItemsList());
                } else {
                    onGetDataFail();
                }
            } else if (pkg.getRequestType() == REQUEST_TYPE_REFRESH) {
                // 下拉刷新
                QuizRelatePackage dataPackage = (QuizRelatePackage) pkg;
                QuizRalate_Reply reply = dataPackage.getResponse();

                if (reply != null && reply.getItemsCount() > 0) {
                    onGetDataSucc(reply.getItemsList());
                }
            } else if (pkg.getRequestType() == IDUtils.ID_QUIZ_MY_LIST_REQ) {
                // 我的问题
                QuizListPackage dataPackage = (QuizListPackage) pkg;
                QuizList_Reply reply = dataPackage.getResponse();

                if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                    isGetMyQuestionStateOver = true;
                } else {
                    isGetMyQuestionStateOver = false;
                }

                if (reply != null && reply.getItemsCount() > 0) {
                    myQuestion = QuizContentInfo.initOfServerItem(reply.getItems(0));

                    QuizItemInfo itemInfo = new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, myQuestion);
                    itemInfo.getQuizItem().setMyLatestQuestion(true);
                    addInfoItem(itemInfo, true);

                    adapter.setData(listDatas);
                    notifyListViewDataChange();

                    // 启动查询状态
                    QuestionStateObserver.getInstance().startWork(myQuestion.getId());
                }
            } else if (pkg.getRequestType() == REQUEST_TYPE_APPRAISE) {
                // 评价
                QuizAppraisePackage dataPackage = (QuizAppraisePackage) pkg;
                QuizAppraise_Reply reply = dataPackage.getResponse();
                if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                    page.showTip("评价成功");

                    if (listDatas != null && listDatas.size() > 0) {
                        for (int i = 0; i < listDatas.size(); i++) {
                            if (listDatas.get(i).getType() == QuizItemInfo.TYPE_QUESTION && listDatas.get(i).getQuizItem().isMyLatestQuestion()) {
                                listDatas.get(i).getQuizItem().setStatus(QuizContentInfo.STATUS_ASK_APPRAISED);
                                listDatas.get(i).getQuizItem().setAppraiseLevel(appraiseLevel);

                                if (type == TYPE_RESULT) {
                                    // 发广播
                                    Intent intent = new Intent(BroadCastName.BCDK_QUIZ_MY_QUESTION_REST);
                                    intent.putExtra(QuizQuestionAskPage.BUNDLE_MY_QUESTION, listDatas.get(i).getQuizItem());
                                    Util.sendBroadcast(intent);
                                }

                                notifyListViewDataChange();

                                if (type == TYPE_RESULT) {
                                    myQuestion.setStatus(QuizContentInfo.STATUS_ASK_APPRAISED);
                                    initTitle();
                                }
                                break;
                            }
                        }
                    }
                } else {
                    onGetDataFail();
                }
            } else {
                // 其它
                onGetDataFail();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            isRequestDataing = false;
        }
    }

    public void updateWhenNetworkError(short type) {
        // TODO Auto-generated method stub
        LogUtil.easylog("************************return updateWhenNetworkError");
        isRequestDataing = false;
        closeLoadingEvent();
        onGetDataFail();
    }

    public void updateWhenDecodeError(short type) {
        LogUtil.easylog("************************return updateWhenDecodeError");
        isRequestDataing = false;
        closeLoadingEvent();
        onGetDataFail();
    }


    // 获取数据成功
    private void onGetDataSucc(List<Item> itemList) {
        if (itemList == null || itemList.size() <= 0) {
            onGetDataFail();
            return;
        }

        LogUtil.easylog("***********************getDataSucc size:" + itemList.size());

        // 相关列表
        if (currRequestType == REQUEST_TYPE_FIRSTPAGE) {
            // 第一页数据
            // 添加数据
            for (int i = 0; i < itemList.size(); i++) {
                addInfoItem(new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, itemList.get(i)), false);
            }

            // 设置数据
            adapter.setData(listDatas);

            // 设置可下拉刷新
            if (type == TYPE_LATEST) {
                listView.setRefreshable(true);
            }
        } else if (currRequestType == REQUEST_TYPE_MORE) {
            // 更多
            for (int i = 0; i < itemList.size(); i++) {
                addInfoItem(new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, itemList.get(i)), false);
            }
        } else if (currRequestType == REQUEST_TYPE_REFRESH) {
            // 下拉刷新
            for (int i = listDatas.size() - 1; i >= 0; i--) {
                if (listDatas.get(i).getType() == QuizItemInfo.TYPE_STRING || (listDatas.get(i).getType() == QuizItemInfo.TYPE_QUESTION && listDatas.get(i).getQuizItem().isMyLatestQuestion())) {
                    continue;
                } else {
                    listDatas.remove(i);
                }
            }

            // 添加数据
            for (int i = 0; i < itemList.size(); i++) {
                addInfoItem(new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, itemList.get(i)), false);
            }

            // 通知结束
            listView.onRefreshFinished();
        }

        // 设置加载更多的状态
        setMoreLayoutOfGetDataSucc(itemList.size());

        // 检查数据
        if (listDatas.size() > 0 && listDatas.get(0).getType() == QuizItemInfo.TYPE_STRING) {
            listDatas.remove(listDatas.size() - 1);
            if (listDatas.size() <= 0) {
                onGetDataFail();
            }
        }

        // 通知更新
        notifyListViewDataChange();
    }

    // 获取数据失败
    private void onGetDataFail() {
        if (currRequestType == REQUEST_TYPE_APPRAISE) {
            // 评价
            page.showTip("评价失败");
        } else {
            // 相关列表
            if (currRequestType == REQUEST_TYPE_FIRSTPAGE) {
                // 第一页数据
                if (listDatas != null && listDatas.size() > 0) {
                    return;
                }

                showNodata();
            } else if (currRequestType == REQUEST_TYPE_MORE) {
                // 更多
                setMoreLayoutOfGetDataFail();
            } else if (currRequestType == REQUEST_TYPE_REFRESH) {
                // 下拉刷新
                listView.onRefreshFinished();
            }
        }
    }

    // 查看问题是否有效
    private boolean checkItemValide(QuizItemInfo info) {
        if (myQuestion != null && (myQuestion.getId() == info.getQuizItem().getId() || myQuestion.isQuestionReply() && myQuestion.getQuestionHashCode() == info.getQuizItem().getQuestionHashCode())) {
            return false;
        }
        return true;
    }

    // 删除不合法的数据
    private void delListInvalideQuestion() {
        if (listDatas != null && listDatas.size() > 0) {
            for (int i = listDatas.size() - 1; i >= 0; i--) {
                if (listDatas.get(i).getType() == QuizItemInfo.TYPE_QUESTION && !listDatas.get(i).getQuizItem().isMyLatestQuestion() && !checkItemValide(listDatas.get(i))) {
                    listDatas.remove(i);
                }
            }
        }
    }

    // 添加问题(只有这一个添加的数据的方法，其它地方不能有添加数据的操作)
    private void addInfoItem(QuizItemInfo info, boolean isMyQuesion) {
        if (info == null) {
            return;
        }

        if (listDatas == null) {
            listDatas = new ArrayList<QuizItemInfo>();
        }

        if (isMyQuesion) {
            // 删除不合法的数据
            delListInvalideQuestion();
        } else {
            if (!checkItemValide(info)) {
                // 数据非法
                return;
            }
        }

        // 添加title
        if (listDatas.size() <= 0 && type == TYPE_LATEST) {
            listDatas.add(new QuizItemInfo("最近问答"));
        }

        if (listDatas.size() == 1 && type == TYPE_RESULT) {
            listDatas.add(new QuizItemInfo("相关问答"));
        }

        if (info.getType() == QuizItemInfo.TYPE_QUESTION && info.getQuizItem().isMyLatestQuestion()) {
            if (type == TYPE_LATEST) {
                listDatas.add(1, info);
            } else {
                listDatas.add(0, info);
            }
        } else {
            listDatas.add(info);
        }
    }

    private void showLoadingEvent() {
        closeNodata();
        if (currRequestType == REQUEST_TYPE_FIRSTPAGE) {
            // 第一页数据
            showLoading();
        } else if (currRequestType == REQUEST_TYPE_MORE) {
            // 更多
        }
    }

    private void closeLoadingEvent() {
        if (currRequestType == REQUEST_TYPE_FIRSTPAGE) {
            // 第一页数据
            closeLoading();
        } else if (currRequestType == REQUEST_TYPE_APPRAISE) {
            // 评价
            DialogUtils.closeProgressDialog();
        }
    }

    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(page.getContext(), R.layout.include_layout_titlebar_item_back, null);
        if (type == TYPE_LATEST) {
            // 相关列表
            ImageView imageView = (ImageView) leftView.findViewById(R.id.backView);
            imageView.setImageResource(R.drawable.selector_titlebar_person);
        }
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);


        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "问股");
        if (type == TYPE_LATEST) {
            // 相关列表
            mItemTitle.setItemName("问股");
        } else {
            mItemTitle.setItemName("发起问股");
        }
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        if (type == TYPE_LATEST) {
            // 相关列表
            View rightItemView = View.inflate(page.getContext(), R.layout.include_layout_titlebar_item_search, null);
            BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
            rightItem.setTag(TitleBar.Position.RIGHT);
            menu.addItem(rightItem);
        }

        return true;
    }

    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && page.mPageChangeFlag == 0) {
            page.mPageChangeFlag = -1;
            if (type == TYPE_LATEST) {
                // 相关列表
            } else {
                finishToPage();
            }
        } else if (itemId == 2 && page.mPageChangeFlag == 0) {
            page.mPageChangeFlag = -1;
            SearchPage.gotoSearch(page);
        }
    }

    /**
     * 评价
     * 
     * @param type
     */
    public void onAppraise(long id, int lev) {
        this.appraiseLevel = lev;
        DialogUtils.showProgressDialog(page.getContext(), "正在评价...", null);

        this.currRequestType = REQUEST_TYPE_APPRAISE;
        request.onQppraiseQuestion(id, lev, REQUEST_TYPE_APPRAISE);
    }

    /**
     * 问题超时
     * 
     * @param pos
     */
    public void onQuestionClose(int pos) {
        listDatas.get(pos).getQuizItem().setStatus(QuizContentInfo.STATUS_ASK_CLOSE);
        notifyListViewDataChange();
    }

    /**
     * 点击头像
     * 
     * @param pos
     */
    public void onClickHeadIcon(int pos) {
        if (listDatas.get(pos).getQuizItem().getReplier() != null) {
            TeacherDetailPage.startPage(page, listDatas.get(pos).getQuizItem().getReplier().getId(), listDatas.get(pos).getQuizItem().getReplier().getNick());
        }
    }

    /**
     * 播放语音
     * 
     * @param pos
     */
    public void onPlayVoice(int pos) {
        // 重置上次的状态
        if (playPos >= 0 && playPos != pos) {
            listDatas.get(playPos).getQuizItem().setDowning(false);
            listDatas.get(playPos).getQuizItem().setPlaying(false);
        }

        if (listDatas.get(pos).getQuizItem() != null) {
            this.playPos = pos;
            QuizContentInfo info = listDatas.get(pos).getQuizItem();
            playRecordId = info.getId();
            listDatas.get(pos).getQuizItem().setDowning(true);
            // 启动下载
            AudioRecordCacheManager.getInstance().getLocPathByUrl(info.getAnswer().getVoiceUrl(), info.getId());
        } else {
            this.playPos = -1;
            playRecordId = 0;
        }

        // 更新列表
        notifyListViewDataChange();
    }

    /**
     * 准备完成，可以播放
     * 
     * @param mp
     */
    @Override
    public void onPlayerStart() {
        // TODO Auto-generated method stub
        if (playPos > 0 && listDatas != null && playPos < listDatas.size()) {
            listDatas.get(playPos).getQuizItem().setDowning(false);
            listDatas.get(playPos).getQuizItem().setPlaying(true);

            // 更新列表
            notifyListViewDataChange();
        }
    }

    /**
     * 播放器暂停
     */
    public void onPlayerPause() {
        resetPlayVoiceState();
    }

    /**
     * 播放结束
     * 
     * @param mp
     */
    @Override
    public void onPlayerCompletion() {
        // TODO Auto-generated method stub
        resetPlayVoiceState();
    }

    private void resetPlayVoiceState() {
        if (playPos > 0 && listDatas != null && playPos < listDatas.size()) {
            listDatas.get(playPos).getQuizItem().setDowning(false);
            listDatas.get(playPos).getQuizItem().setPlaying(false);

            // 更新列表
            notifyListViewDataChange();
        }
    }

    /**
     * 加载播放器出错
     */
    @Override
    public void onPlayerError() {
        // TODO Auto-generated method stub
        resetPlayVoiceState();
    }

    // 更新数据
    private void notifyListViewDataChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (listDatas != null && listDatas.size() > 0) {
            closeNodata();
        } else {
            showNodata();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.moreTxt) {
            if (footerMoreText.getVisibility() == View.VISIBLE) {
                footerMoreLayout.setVisibility(View.VISIBLE);
                footerMoreText.setVisibility(View.GONE);
                loadMoreData();
            }
        }
    }

    @Override
    public void postScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (isLoadOver) {
            // 加载完成
            return;
        }

        // 判断滚动到底部
        if (view.getCount() > 1 && view.getLastVisiblePosition() == (view.getCount() - 1) && footerMoreLayout.getVisibility() == View.VISIBLE) {
            loadMoreData();
        }
    }

    @Override
    public void postScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    }

    // 加载翻页数据
    private void loadMoreData() {
        currRequestType = REQUEST_TYPE_MORE;
        requestListQuestion();
    }

    // 初始化加载更多
    private void initFooterView() {
        footerView = View.inflate(page.getContext(), R.layout.footer_more_view, null);
        footerMoreLayout = (LinearLayout) footerView.findViewById(R.id.moreLayout);
        footerMoreLayout.setVisibility(View.GONE);
        footerMoreText = (TextView) footerView.findViewById(R.id.moreTxt);
        footerMoreText.setVisibility(View.GONE);
        footerMoreText.setOnClickListener(this);
    }

    // 设置加载更多的状态(加载更多数据获取成功)
    private void setMoreLayoutOfGetDataSucc(int dataSize) {
        if (dataSize < QUIZ_LIST_SIZE || listDatas.size() >= QUIZ_LIST_MAX_SIZE) {
            isLoadOver = true;
        } else {
            isLoadOver = false;
        }

        footerMoreText.setVisibility(View.GONE);
        if (isLoadOver) {
            footerMoreLayout.setVisibility(View.INVISIBLE);
        } else {
            footerMoreLayout.setVisibility(View.VISIBLE);
        }
    }

    // 设置加载更多的状态(加载更多数据获取失败)
    private void setMoreLayoutOfGetDataFail() {
        footerMoreLayout.setVisibility(View.GONE);
        footerMoreText.setVisibility(View.VISIBLE);
        page.showTip(Util.getResourcesString(R.string.loading_fail));
    }

    private void onKeyBackEvent() {
        if (type == TYPE_LATEST) {
            // 相关列表
            page.finish();
        } else {
            finishToPage();
        }
    }

    private void finishToPage() {
        PageIntent pageIntent = new PageIntent(null, MainPage.class);
        page.finishToPage(pageIntent);
    }

    private void showLoading() {
        if (type == TYPE_LATEST) {
            page.findViewById(R.id.quizLoading).setVisibility(View.VISIBLE);
        }
    }

    private void closeLoading() {
        page.findViewById(R.id.quizLoading).setVisibility(View.GONE);
    }

    private void showNodata() {
        page.findViewById(R.id.quizNodataLayout).setVisibility(View.VISIBLE);
    }

    private void closeNodata() {
        page.findViewById(R.id.quizNodataLayout).setVisibility(View.GONE);
    }

    /**
     * 更新问题状态
     * 
     * @param itemStatus
     */
    public void onUpdateQuestionState(int retCode, ItemStatus itemStatus, int time) {
        if (itemStatus != null && myQuestion != null && listDatas != null && listDatas.size() > 0) {
            for (int i = 0; i < listDatas.size(); i++) {
                if (listDatas.get(i).getType() == QuizItemInfo.TYPE_QUESTION && listDatas.get(i).getQuizItem().isMyLatestQuestion() && listDatas.get(i).getQuizItem().getId() == itemStatus.getId()) {
                    if (listDatas.get(i).getQuizItem().getStatus() != itemStatus.getStatus()) {
                        // 更新状态
                        listDatas.get(i).getQuizItem().setStatus(itemStatus.getStatus());

                        // 回复的老师
                        if (itemStatus.getReplier() != null) {
                            TeacherInfo teacherInfo = new TeacherInfo();
                            teacherInfo.initOfServerTeacher(itemStatus.getReplier());
                            listDatas.get(i).getQuizItem().setReplier(teacherInfo);
                        }

                        // 回复的内容
                        if (itemStatus.getAnswer() != null) {
                            listDatas.get(i).getQuizItem().setAnswer(AnswerInfo.initServerAnswer(itemStatus.getAnswer()));
                            listDatas.get(i).getQuizItem().setAnswerTime(time);
                        }

                        // 回收CountDownTimer
                        if (itemStatus.getStatus() != QuizContentInfo.STATUS_ASK_WAIT && itemStatus.getStatus() != QuizContentInfo.STATUS_ASK_WAIT2 && adapter != null) {
                            adapter.recycleCountDownTimer();
                        }

                        if (itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE || itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED) {
                            // 删除不合法的数据
                            delListInvalideQuestion();
                        }

                        notifyListViewDataChange();

                        if (type == TYPE_RESULT) {
                            myQuestion.setStatus(itemStatus.getStatus());
                            initTitle();
                        }
                    }

                    if (itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_CLOSE || itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE || itemStatus.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED) {
                        // 状态到了用户要处理状态，别人无法修改这问题的状态，则停止查询
                        QuestionStateObserver.getInstance().stopWork();
                    }
                    break;
                }
            }
        }
    }

    // 注册广播
    private void registerBroadcast() {
        if (type == TYPE_LATEST) {
            mIntentFilter = new IntentFilter();
            // 登录状态改变
            mIntentFilter.addAction(BroadCastName.BCDC_CHANGE_LOGIN_STATE_QIZE);
            // 我的问题重置
            mIntentFilter.addAction(BroadCastName.BCDK_QUIZ_MY_QUESTION_REST);

            page.getContext().registerReceiver(broadcastReceiver, mIntentFilter);
        }
    }

    // 取消注册
    private void unRegisterBroadcast() {
        if (type == TYPE_LATEST && mIntentFilter != null) {
            page.getContext().unregisterReceiver(broadcastReceiver);
        }
    }


    // 广播接收器
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BroadCastName.BCDC_CHANGE_LOGIN_STATE_QIZE.equals(action)) {
                // 登录状态改变
                removeMyQuestion();
                isResetListViewScrollState = true;

                // 重置
                QuizConfigData.getInstance().resestLoadState();
            } else if (BroadCastName.BCDK_QUIZ_MY_QUESTION_REST.equals(action)) {
                // 重置我的问题
                removeMyQuestion();
                isResetListViewScrollState = true;

                QuizContentInfo tempInfo = (QuizContentInfo) intent.getSerializableExtra(QuizQuestionAskPage.BUNDLE_MY_QUESTION);
                if (tempInfo != null) {
                    myQuestion = tempInfo;
                    myQuestion.setMyLatestQuestion(true);
                    addInfoItem(new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, myQuestion), true);

                    notifyListViewDataChange();
                }
            }
        }
    };

    // 重置我的问题
    private void removeMyQuestion() {
        myQuestion = null;

        if (listDatas != null && listDatas.size() > 0) {
            for (int i = 0; i < listDatas.size(); i++) {
                if (listDatas.get(i).getType() == QuizItemInfo.TYPE_QUESTION && listDatas.get(i).getQuizItem().isMyLatestQuestion()) {
                    listDatas.get(i).getQuizItem().setMyLatestQuestion(false);
                    notifyListViewDataChange();
                    break;
                }
            }
        }
    }

    private void initTitle() {
        View askGoodIV = page.findViewById(R.id.askGoodIV);
        ImageView stateIV = (ImageView) page.findViewById(R.id.stateIV);
        TextView numTV = (TextView) page.findViewById(R.id.numTV);
        if (type == TYPE_LATEST) {
            // 最近列表
            askGoodIV.setVisibility(View.VISIBLE);
            askGoodIV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // 发起问股
                    if (!page.isLogined()) {
                        page.showTip("登录后即可使用问股功能");
                        return;
                    }

                    // 启动问题页面
                    startAskPage();
                }
            });

            stateIV.setVisibility(View.GONE);
            numTV.setVisibility(View.GONE);
        } else {
            // 相关列表
            askGoodIV.setVisibility(View.GONE);
            stateIV.setVisibility(View.VISIBLE);
            numTV.setVisibility(View.VISIBLE);
            if (isFromHomePage && QuizConfigData.getInstance().getLeaveQuestionCount() <= 0) {
                // 首页,次数0
                stateIV.setImageResource(R.drawable.img_quiz_ask_num_over);
            } else if (isFromHomePage && (myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_WAIT || myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_WAIT2)) {
                // 首页，正在等待
                stateIV.setImageResource(R.drawable.img_quiz_ask_wait);
            } else if (myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_ON) {
                // 正在回复
                stateIV.setImageResource(R.drawable.img_quiz_ask_replying);
            } else if (myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE) {
                // 评论
                stateIV.setImageResource(R.drawable.img_quiz_ask_comment);
            } else if (myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED || myQuestion.getStatus() == QuizContentInfo.STATUS_ASK_CLOSE) {
                // 已评价或问题结束
                stateIV.setImageResource(R.drawable.img_quiz_ask_question_over);
            } else {
                // 发送成功
                stateIV.setImageResource(R.drawable.img_quiz_ask_send_succ);
            }

            numTV.setText(String.format(Util.getResourcesString(R.string.quiz_ask_count_str), QuizConfigData.getInstance().getLeaveQuestionCount()));
        }
    }

    // 启动问题页面
    private void startAskPage() {
        if (startAskPageManager == null) {
            startAskPageManager = new StartAskPageManager();
        }
        startAskPageManager.startAskPage(page, myQuestion, isGetMyQuestionStateOver, null);
    }

    // 获取相关股票
    private String getRelateStock(String stockStr) {
        if (TextUtils.isEmpty(stockStr)) {
            return "";
        }

        String stock = "";
        // try {
        // JSONArray jsonArray = new JSONArray(stockStr);
        // if (jsonArray != null && jsonArray.length() > 0) {
        // for (int i = 0; i < jsonArray.length(); i++) {
        // JSONObject object = (JSONObject) jsonArray.get(i);
        // System.out.println("******************test arr:" + jsonArray.get(i) + ",string:" +
        // object.getString(""));
        // }
        // }
        // } catch (JSONException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }

        stockStr = stockStr.substring(1, stockStr.length() - 1);
        if (!TextUtils.isEmpty(stockStr)) {
            String[] stockArr = stockStr.split(",");
            if (stockArr != null && stockArr.length > 0) {
                stock = stockArr[0];
            }
        }
        return stock;
    }


    /**
     * 缓存成功
     * 
     * @param path
     */
    public void onRecordCacheSucc(String path, long recordId) {
        LogUtil.easylog("***************onRecoirdCacheSucc:" + path + ",recordId:" + recordId);
        if (playRecordId == recordId) {
            if (mediaPlayerManager == null) {
                mediaPlayerManager = new AppMediaPlayerManager(this);
                mediaPlayerManager.setPageState(true);
            }

            mediaPlayerManager.onStartPlayer(path);
        }
    }

    /**
     * 缓存失败
     */
    public void onRecordCacheFail(long recordId) {
        LogUtil.easylog("***************onRecordCacheFail:" + recordId);
        if (playPos > 0 && listDatas != null && playPos < listDatas.size()) {
            listDatas.get(playPos).getQuizItem().setDowning(false);

            // 更新列表
            notifyListViewDataChange();
        }
    }
}
