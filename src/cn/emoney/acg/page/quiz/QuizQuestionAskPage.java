package cn.emoney.acg.page.quiz;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizRequirePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRequireReply.QuizRequire_Reply;
import cn.emoney.acg.data.quiz.QuizCommonRequest;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.KeyboardUtil;
import cn.emoney.acg.util.KeyboardUtil.KeyboardListener;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.RegularExpressionUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * 问股输入
 * 
 * @ClassName: StockQuestionAskPage
 * @Description:
 * @author xiechengfa
 * @date 2015年12月2日 下午4:19:41
 *
 */
public class QuizQuestionAskPage extends PageImpl implements KeyboardListener {
    public static final String BUNDLE_MY_QUESTION = "my_question";// 我的问题

    public static final String INTENT_KEY_QUERY_STR = "query_codeId";//
    public static final String INTENT_KEY_QUERY_GOOD = "query_codeId";//

    public static final int PAGE_CODE = 4110000; // 提问页问股成功跳转
    private final int CODE_LEN = 6;// 股票编码的最大长度
    private final int MAX_RESULT = 20;
    private final int QUSTION_TYPE1 = 0;// 问题模板1
    private final int QUSTION_TYPE2 = 1;// 问题模板2
    private final int QUSTION_TYPE3 = 2;// 问题模板3
    private final int QUSTION_TYPE4 = 3;// 问题模板4

    private final String[] QUSTION_TEMPLATE = {"现在还能买吗", "现在卖合适吗", "怎么样", "还能继续持有吗"};

    private boolean isHideKeyBoardOfInit = false;// 初始化是否键盘(个股跳转)
    private boolean isHideSendOfInit = false;// 是否隐藏发送(个股跳转)

    private String relateGoodId = null;// 关联股票
    private String questionContent = null;
    private EditText searchET = null;
    private TextView mTvEtGoodName = null;
    private ImageView mEtClearBtn = null;
    private KeyboardUtil keyBoardUtil = null;

    private Goods currGood = null;
    private ArrayList<Goods> mLstGoods = new ArrayList<Goods>();

    private ListView stockLiveView = null;
    private MyLvStockAdapter mAdapterStock = null;

    // 问题模板的股票名称
    private TextView questionStockNameView1;
    private TextView questionStockNameView2;
    private TextView questionStockNameView3;
    private TextView questionStockNameView4;

    private QuizCommonRequest request = null;

    public static void startPage(PageImpl page, Goods goods) {
        PageIntent intent = new PageIntent(page, QuizQuestionAskPage.class);
        if (goods != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(INTENT_KEY_QUERY_STR, goods);
            intent.setArguments(bundle);
        }
        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_stockquestion_ask);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(INTENT_KEY_QUERY_STR)) {
            currGood = bundle.getParcelable(INTENT_KEY_QUERY_STR);

            if (currGood != null) {
                isHideKeyBoardOfInit = true;
                isHideSendOfInit = true;
            }
        }

        // 初始搜索
        initSearchLayout();
        // 初始化股票列表
        initStockListView();
        // 初始化问题列表
        initQuestionListLayout();

        if (currGood != null) {
            showQuestionTemplateLayout();
        }

        bindPageTitleBar(R.id.quizAskbar);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                LogUtil.easylog("StockQuestionAsk->onPageResume");
                QuizQuestionAskPage.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        showStockKeyBoard();
                    }
                });
            }
        }, 500);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isShowKeyBoard()) {
            // 返回键
            closedPageKeyboard();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void initData() {
        //
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);


        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "发起问股");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    @Override
    protected void onPagePause() {
        closedPageKeyboard();
        super.onPagePause();
    }

    // 初始化问题列表
    private void initQuestionListLayout() {
        findViewById(R.id.questionTipView).setOnClickListener(onClickListener);
        findViewById(R.id.questionLayout).setOnClickListener(onClickListener);
        findViewById(R.id.inputBtnView).setOnClickListener(onClickListener);
        findViewById(R.id.quest1Layout).setOnClickListener(onClickListener);
        findViewById(R.id.quest2Layout).setOnClickListener(onClickListener);
        findViewById(R.id.quest3Layout).setOnClickListener(onClickListener);
        findViewById(R.id.quest4Layout).setOnClickListener(onClickListener);

        if (QUSTION_TEMPLATE != null && QUSTION_TEMPLATE.length == 4) {
            ((TextView) findViewById(R.id.quest1View)).setText(QUSTION_TEMPLATE[0]);
            ((TextView) findViewById(R.id.quest2View)).setText(QUSTION_TEMPLATE[1]);
            ((TextView) findViewById(R.id.quest3View)).setText(QUSTION_TEMPLATE[2]);
            ((TextView) findViewById(R.id.quest4View)).setText(QUSTION_TEMPLATE[3]);
        }
    }

    // 初始化股票列表
    private void initStockListView() {
        stockLiveView = ((ListView) findViewById(R.id.stockquestion_lv_stock));

        TextView tvHeaderStock = (TextView) View.inflate(getContext(), R.layout.layout_stockquestion_ask_lv_header, null);
        tvHeaderStock.setText("请选择你要咨询的股票");
        stockLiveView.addHeaderView(tvHeaderStock);

        mAdapterStock = new MyLvStockAdapter();
        stockLiveView.setAdapter(mAdapterStock);
        stockLiveView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    closedPageKeyboard();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });

        stockLiveView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListViewItemClick(position - 1);
            }
        });
    }

    // 初始化键盘
    private void initSearchLayout() {
        searchET = (EditText) findViewById(R.id.stockquestion_et_inputbox);
        mTvEtGoodName = (TextView) findViewById(R.id.stockquestion_tv_et_goodname);
        mEtClearBtn = (ImageView) findViewById(R.id.stockquestion_iv_etclear_btn);
        mEtClearBtn.setVisibility(View.GONE);
        findViewById(R.id.sendImageView).setVisibility(View.GONE);

        mEtClearBtn.setOnClickListener(onClickListener);
        findViewById(R.id.sendImageView).setOnClickListener(onClickListener);

        if (searchET != null) {
            searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        // 发送
                        closedPageKeyboard();
                        String tQuestionContent = searchET.getText().toString();
                        commitQuestion(tQuestionContent);
                        setQustionLayoutVisibility(false);
                        return true;
                    }
                    return false;
                }
            });

            searchET.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String text = searchET.getText().toString();
                    if (text.length() > CODE_LEN) {
                        // 系统键盘
                        if (!keyBoardUtil.isKeyboardShow()) {
                            return false;
                        }
                        return true;
                    }

                    // 如果系统键盘已经显示 就先隐藏系统键盘
                    showStockKeyBoard();
                    return true;
                }
            });

            searchET.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // 隐藏问题模板和股票名
                    setQustionLayoutVisibility(false);
                    mTvEtGoodName.setText("");
                    mTvEtGoodName.setVisibility(View.INVISIBLE);

                    String text = searchET.getText().toString();
                    if (text.length() > 0) {
                        if (text.length() > CODE_LEN) {
                            // 输入内容，显示发送
                            if (isHideSendOfInit) {
                                setCleanImageVisibility(true);
                                setSendBtnVisibility(false);
                            } else {
                                setCleanImageVisibility(false);
                                setSendBtnVisibility(true);
                            }

                            isHideSendOfInit = false;

                            // 股票列表
                            if (mAdapterStock != null) {
                                mLstGoods.clear();
                                mAdapterStock.notifyDataSetChanged();
                            }

                            if (stockLiveView != null) {
                                stockLiveView.setVisibility(View.INVISIBLE);
                            }

                            // 内容提示
                            setInputTipViewVisibility(true);
                        } else {
                            // 输入框
                            setCleanImageVisibility(true);
                            setSendBtnVisibility(false);

                            // 股票列表
                            if (stockLiveView != null) {
                                stockLiveView.setVisibility(View.VISIBLE);
                            }
                            doSearch(text);

                            // 内容提示
                            setInputTipViewVisibility(false);
                        }

                        // 设置提示的显示状态
                        setTipLayoutVisibility(false);
                    } else {
                        // 输入框
                        setCleanImageVisibility(false);
                        setSendBtnVisibility(false);

                        // 列表
                        // 设置提示的显示状态
                        setTipLayoutVisibility(true);
                        setInputTipViewVisibility(false);
                        // 股票列表
                        stockLiveView.setVisibility(View.VISIBLE);
                        doSearch(text);
                    }
                }
            });
        }

        if (currGood != null) {
            searchET.setText(currGood.getGoodsCode() + " ");
            InputMethodUtil.setEditTextFocus(searchET);
        }
    }

    // 设置提示的显示状态
    private void setTipLayoutVisibility(boolean isVisible) {
        findViewById(R.id.tipLayout).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    // 设置清除的显示状态
    private void setCleanImageVisibility(boolean isVisible) {
        if (mEtClearBtn != null) {
            mEtClearBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    // 设置发送按钮的显示状态
    private void setSendBtnVisibility(boolean isVisible) {
        findViewById(R.id.sendImageView).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    // 设置问题模板的显示状态
    private void setQustionLayoutVisibility(boolean isVisible) {
        findViewById(R.id.questionLayout).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    // 设置问题模板的股票名称
    private void initQuestionStockName(String name) {
        if (name == null) {
            return;
        }

        if (questionStockNameView1 == null) {
            questionStockNameView1 = (TextView) findViewById(R.id.quest1StockNameView);
        }

        if (questionStockNameView2 == null) {
            questionStockNameView2 = (TextView) findViewById(R.id.quest2StockNameView);
        }

        if (questionStockNameView3 == null) {
            questionStockNameView3 = (TextView) findViewById(R.id.quest3StockNameView);
        }

        if (questionStockNameView4 == null) {
            questionStockNameView4 = (TextView) findViewById(R.id.quest4StockNameView);
        }

        questionStockNameView1.setText(name);
        questionStockNameView2.setText(name);
        questionStockNameView3.setText(name);
        questionStockNameView4.setText(name);
    }

    // 设置输入内容提示View的显示状态
    private void setInputTipViewVisibility(boolean isVisible) {
        findViewById(R.id.inputTextTipView).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void showStockKeyBoard() {
        InputMethodUtil.closeSoftKeyBoard(this);

        int type = getDBHelper().getInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);

        searchET.requestFocus();
        searchET.requestFocusFromTouch();
        if (keyBoardUtil == null) {
            keyBoardUtil = new KeyboardUtil(getContentView(), getContext(), searchET);
        }

        keyBoardUtil.setEditText(searchET);
        keyBoardUtil.setMaxInputLen(CODE_LEN);
        keyBoardUtil.setOnkeyboardListener(QuizQuestionAskPage.this);

        if (!isHideKeyBoardOfInit && !keyBoardUtil.isKeyboardShow()) {
            if (KeyboardUtil.TYPE_DIGIT == type) {
                keyBoardUtil.showKeyboard();
            } else if (KeyboardUtil.TYPE_ENGLISH == type) {
                keyBoardUtil.showKeyboardEnglish();
            }
        }

        // 重置
        isHideKeyBoardOfInit = false;

        InputMethodUtil.closeSoftKeyBoard(this);
    }

    private void closedPageKeyboard() {
        if (keyBoardUtil != null && keyBoardUtil.isKeyboardShow()) {
            keyBoardUtil.hideKeyboard();
        }
        InputMethodUtil.closeSoftKeyBoard(this);
    }

    private boolean isShowKeyBoard() {
        if (keyBoardUtil != null && keyBoardUtil.isKeyboardShow()) {
            return true;
        }
        return false;
    }

    protected ArrayList<Goods> getSrearchResultByLocal(String strInput) {
        ArrayList<Goods> cn = null;

        if (strInput == null || strInput.equals("")) {

        } else if (RegularExpressionUtil.isOnlyDigital(strInput)) {
            cn = getSQLiteDBHelper().queryAGStockInfosByCode(strInput, MAX_RESULT);
        } else if (RegularExpressionUtil.isDigitalAndLetter(strInput)) {
            if (strInput.length() >= 2) {
                cn = getSQLiteDBHelper().queryAGStockInfosByPinyin(strInput, MAX_RESULT, true);
            } else {
                cn = getSQLiteDBHelper().queryAGStockInfosByPinyin(strInput, MAX_RESULT, false);
            }
        } else {
            cn = getSQLiteDBHelper().queryStockInfoByName(strInput, MAX_RESULT, true);
        }
        closeSQLDBHelper();
        return cn;
    }

    // 点击事件
    private OnClickEffectiveListener onClickListener = new OnClickEffectiveListener() {

        @Override
        public void onClickEffective(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.questionTipView:
                case R.id.questionLayout:
                    // 问题模板页面
                    closedPageKeyboard();
                    break;
                case R.id.quest1Layout:
                    // 问题模板1
                    onClickQustionTemplate(QUSTION_TYPE1);
                    break;
                case R.id.quest2Layout:
                    // 问题模板2
                    onClickQustionTemplate(QUSTION_TYPE2);
                    break;
                case R.id.quest3Layout:
                    // 问题模板3
                    onClickQustionTemplate(QUSTION_TYPE3);
                    break;
                case R.id.quest4Layout:
                    // 问题模板4
                    onClickQustionTemplate(QUSTION_TYPE4);
                    break;
                case R.id.inputBtnView:
                    // 手动输入
                    setCleanImageVisibility(false);
                    setSendBtnVisibility(true);
                    searchET.setText(searchET.getText().toString() + " " + mTvEtGoodName.getText().toString() + "，");

                    // 设置光标
                    searchET.requestFocus();
                    searchET.setSelection(searchET.getText().toString().trim().length());

                    // 系统键盘
                    InputMethodUtil.openInputMethod(QuizQuestionAskPage.this);
                    break;
                case R.id.stockquestion_iv_etclear_btn:
                    // 清除
                    if (searchET != null) {
                        searchET.setText("");
                    }
                    break;
                case R.id.sendImageView:
                    // 发送
                    commitQuestion(searchET.getText().toString());
                    break;
            }
        }
    };

    // 点击问题模板
    private void onClickQustionTemplate(int type) {
        if (type < QUSTION_TEMPLATE.length && currGood != null) {
            commitQuestion(currGood.getGoodsCode() + " " + currGood.getGoodsName() + "," + QUSTION_TEMPLATE[type] + "?");
        }
    }

    // 提交问题
    private void commitQuestion(String questionContent) {
        if (questionContent == null || questionContent.equals("")) {
            showTip("不能提交空白内容哦");
        }

        if (getUserInfo() == null) {
            return;
        }

        this.questionContent = questionContent;
        DialogUtils.showProgressDialog(getContext(), "正在发送...", null);
        if (request == null) {
            request = new QuizCommonRequest(this);
        }

        if (currGood != null && questionContent.contains(currGood.getGoodsCode())) {
            relateGoodId = "[" + currGood.getGoodsId() + "]";
        } else {
            relateGoodId = Util.getReleativeStockList(questionContent);
        }

        request.onCommitQuestionRequest(questionContent, relateGoodId);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        DialogUtils.closeProgressDialog();
        if (pkg == null) {
            onSendFail(null);
            return;
        }

        if (pkg.getRequestType() == ID_QUIZ_REQUIRE_REQ) {
            QuizRequirePackage dataPackage = (QuizRequirePackage) pkg;
            QuizRequire_Reply reply = dataPackage.getResponse();

            if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                // 成功
                QuizContentInfo questioin = new QuizContentInfo();
                questioin.setMyLatestQuestion(true);
                questioin.setContent(questionContent);
                questioin.setId(reply.getId());
                questioin.setStatus(reply.getStatus());
                questioin.setCommitTime(reply.getCommitTime());
                questioin.setOwner(getUserInfo());
                questioin.setStock(relateGoodId);

                onSendSucc(questioin);
            } else {
                // 失败
                onSendFail(null);
            }
        } else {
            onSendFail(null);
        }
    }

    @Override
    protected void updateWhenNetworkError(short type) {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        onSendFail(null);
    }

    @Override
    protected void updateWhenDecodeError(short type) {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
    }

    private void onSendSucc(QuizContentInfo questioin) {
        // 更新当天问股的次数
        QuizConfigData.getInstance().updateCurrQuestionCountOfSendSucc();
        QuizResultPage.startPage(this, questioin, false);

        // 发广播
        Intent intent = new Intent(BroadCastName.BCDK_QUIZ_MY_QUESTION_REST);
        intent.putExtra(BUNDLE_MY_QUESTION, questioin);
        Util.sendBroadcast(intent);
    }

    private void onSendFail(String msg) {
        if (msg != null && msg.trim().length() > 0) {
            showTip(msg);
        } else {
            showTip("发送失败");
        }
    }

    // 点击股票
    private void onListViewItemClick(int index) {
        if (mLstGoods != null && index >= 0 && index < mLstGoods.size()) {
            closedPageKeyboard();
            currGood = mLstGoods.get(index);

            searchET.setText(currGood.getGoodsCode());
            searchET.clearFocus();
            mLstGoods.clear();

            // 隐藏列表
            stockLiveView.setVisibility(View.INVISIBLE);
            if (mAdapterStock != null) {
                mAdapterStock.notifyDataSetChanged();
            }

            // 显示问题模板
            showQuestionTemplateLayout();
        }
    }

    private void showQuestionTemplateLayout() {
        // 设置提示的显示状态
        setInputTipViewVisibility(false);

        // 显示股票名
        mTvEtGoodName.setText(currGood.getGoodsName());
        mTvEtGoodName.setVisibility(View.VISIBLE);

        // 显示问题模板
        setQustionLayoutVisibility(true);
        initQuestionStockName(currGood.getGoodsName());
    }

    private void doSearch(String input) {
        ArrayList<Goods> lstGoods = getSrearchResultByLocal(input);
        mLstGoods.clear();
        if (lstGoods != null) {
            mLstGoods.addAll(lstGoods);
        }

        if (mAdapterStock != null) {
            mAdapterStock.notifyDataSetChanged();
        }
    }

    @Override
    public void onFuncKeyClick(int keyId, String keyName) {
        onListViewItemClick(0);
    }

    // 股票适配器
    private class MyLvStockAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int count = mLstGoods.size();
            if (count > 0) {
                stockLiveView.setVisibility(View.VISIBLE);
            } else {
                stockLiveView.setVisibility(View.INVISIBLE);
            }
            return mLstGoods.size();
        }

        @Override
        public Object getItem(int position) {
            return mLstGoods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_stockquestion_ask_listitem, null);
                TextView tvStockName = (TextView) convertView.findViewById(R.id.item_tv_stockname);
                TextView tvStockCode = (TextView) convertView.findViewById(R.id.item_tv_stockcode);
                convertView.setTag(new ListCell(tvStockName, tvStockCode));
            }
            ListCell lc = (ListCell) convertView.getTag();

            String stockName = mLstGoods.get(position).getGoodsName();
            String stockCode = mLstGoods.get(position).getGoodsCode();

            lc.getTvStockName().setText(stockName);
            lc.getTvStockCode().setText(stockCode);

            if (position == 0) {
                lc.getTvStockName().setTextColor(RColor(R.color.c3));
                lc.getTvStockCode().setTextColor(RColor(R.color.c3));
            } else {
                lc.getTvStockName().setTextColor(RColor(R.color.t1));
                lc.getTvStockCode().setTextColor(RColor(R.color.t3));
            }

            return convertView;
        }

        private class ListCell {
            public ListCell(TextView tvStockName, TextView tvStockCode) {
                this.tvStockName = tvStockName;
                this.tvStockCode = tvStockCode;
            }

            public TextView getTvStockName() {
                return tvStockName;
            }

            public TextView getTvStockCode() {
                return tvStockCode;
            }

            private TextView tvStockName;
            private TextView tvStockCode;
        }
    }
}
