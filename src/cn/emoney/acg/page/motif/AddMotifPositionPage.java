package cn.emoney.acg.page.motif;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.dialog.CustomDialog;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.KeyboardUtil;
import cn.emoney.acg.util.KeyboardUtil.KeyboardListener;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

import com.alibaba.fastjson.JSONObject;

/**
 * 股票建仓界面
 */
public class AddMotifPositionPage extends PageImpl implements KeyboardListener {

    public static final String EXTRA_KEY_GROUP_ID = "extra_group_id";
    public static final String EXTRA_KEY_GROUP_GOODSCODE = "extra_group_goodscode";

    private static final short REQUEST_TYPE_ZXJ = 1002;
    private final int MAX_RESULT = 15;
    private static final String FORMAT_AVAILABLE_POSITION = "仓位范围 %d%% ~ %d%%";

    private int currentChildIndex;
    private int groupId;
    private int minPosition, maxPosition;
    private String currentGoodsCode;
    private boolean isSelect; // 是否是手动选择股票时改变股票代码值

    private KeyboardUtil keyboardUtil;
    private ArrayList<String> listGoodsCodes;

    private List<CellBean> listDatas = new ArrayList<CellBean>();
    private SearchAdapter adapter;

    private View viewRightTitlebarItem;
    private View viewClearInput; // 清空输入
    private EditText etSearch, etPosition, etPrice, etReason;
    private ListView lvStocks;
    private ViewSwitcher viewSwitcher;
    private TextView tvStockName, tvStockPrice, tvAvailablePosition;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_add_motif_position);

        initViews();

        bindPageTitleBar(R.id.page_add_motifposition_titlebar);
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);

        if (arguments != null && arguments.containsKey(EXTRA_KEY_GROUP_ID)) {
            groupId = arguments.getInt(EXTRA_KEY_GROUP_ID, 0);
        }

        if (arguments != null && arguments.containsKey(EXTRA_KEY_GROUP_GOODSCODE)) {
            listGoodsCodes = arguments.getStringArrayList(EXTRA_KEY_GROUP_GOODSCODE);

            if (listGoodsCodes == null) {
                listGoodsCodes = new ArrayList<String>();
            }
        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        setChildDisplay();
    }

    @Override
    protected void onPagePause() {
        InputMethodUtil.closeSoftKeyBoard(this);
        if (keyboardUtil != null && keyboardUtil.isKeyboardShow()) {
            keyboardUtil.hideKeyboard();
        }
        super.onPagePause();
    }

    private void initViews() {
        etSearch = (EditText) findViewById(R.id.page_add_motifposition_et_search);
        etPosition = (EditText) findViewById(R.id.page_add_motifposition_et_position);
        etPrice = (EditText) findViewById(R.id.page_add_motifposition_et_price);
        etReason = (EditText) findViewById(R.id.page_add_motifposition_et_reason);
        viewClearInput = findViewById(R.id.page_add_motifposition_layout_clear_search_input);
        lvStocks = (ListView) findViewById(R.id.page_add_motifposition_list);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.page_add_motifposition_viewswitcher);
        tvStockName = (TextView) findViewById(R.id.page_add_motifposition_tv_stock_name);
        tvStockPrice = (TextView) findViewById(R.id.page_add_motifposition_tv_stock_price);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        tvAvailablePosition = (TextView) findViewById(R.id.page_add_motifposition_tv_available_position);

        adapter = new SearchAdapter(listDatas, getContext());
        lvStocks.setAdapter(adapter);
        lvStocks.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    if (keyboardUtil != null && keyboardUtil.isKeyboardShow()) {
                        keyboardUtil.hideKeyboard();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });

        etSearch.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int lastKeyboardType = getDBHelper().getInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);
                // 如果系统键盘已经显示 就先隐藏系统键盘
                showStockKeyBoard(lastKeyboardType);

                return true;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (isSelect) {
                    isSelect = false;
                    return;
                }

                if (currentChildIndex == 1) {
                    currentGoodsCode = "";

                    currentChildIndex = 0;
                    setChildDisplay();
                }

                String text = etSearch.getText().toString();
                doSearch(text);
                lvStocks.setSelection(0);

                if (text.length() > 0) {
                    viewClearInput.setVisibility(View.VISIBLE);
                } else {
                    viewClearInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        viewClearInput.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                currentGoodsCode = "";
                etSearch.setText("");

                // 显示搜索列表子界面
                currentChildIndex = 0;
                setChildDisplay();
            }
        });

        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodUtil.closeSoftKeyBoard(AddMotifPositionPage.this);
                if (keyboardUtil != null && keyboardUtil.isKeyboardShow()) {
                    keyboardUtil.hideKeyboard();
                }
                return false;
            }
        });

    }

    private void showStockKeyBoard(int type) {
        InputMethodUtil.closeSoftKeyBoard(this);

        etSearch.requestFocus();
        etSearch.requestFocusFromTouch();
        if (keyboardUtil == null) {
            keyboardUtil = new KeyboardUtil(getContentView(), getContext(), etSearch);
        }
        keyboardUtil.setEditText(etSearch);
        keyboardUtil.setOnkeyboardListener(AddMotifPositionPage.this);
        if (!keyboardUtil.isKeyboardShow()) {
            if (KeyboardUtil.TYPE_DIGIT == type) {
                keyboardUtil.showKeyboard();
            } else if (KeyboardUtil.TYPE_ENGLISH == type) {
                keyboardUtil.showKeyboardEnglish();
            }
        }

        InputMethodUtil.closeSoftKeyBoard(this);
    }

    private void doSearch(String input) {
        listDatas.clear();

        ArrayList<Goods> lstGoods = getSrearchResultByLocal(input);
        if (lstGoods != null && lstGoods.size() > 0) {
            for (Goods goods : lstGoods) {
                int goodsId = goods.getGoodsId();

                if (DataUtils.IsAG(goodsId)) {
                    listDatas.add(new CellBean(goods.getGoodsName(), goods.getGoodsCode(), goods.getGoodsId()));
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    protected ArrayList<Goods> getSrearchResultByLocal(String strInput) {
        ArrayList<Goods> cn = null;

        if (TextUtils.isEmpty(strInput)) {
            // return null;
        } else if (TextUtils.isDigitsOnly(strInput)) {
            cn = getSQLiteDBHelper().queryAGStockInfosByCode(strInput, MAX_RESULT);
        } else {
            if (strInput.length() >= 2) {
                cn = getSQLiteDBHelper().queryAGStockInfosByPinyin(strInput, MAX_RESULT, true);
            } else {
                cn = getSQLiteDBHelper().queryAGStockInfosByPinyin(strInput, MAX_RESULT, false);
            }
        }
        closeSQLDBHelper();

        return cn;
    }

    private void setChildDisplay() {
        // 1. 设置子布局的切换显示
        // 2. 设置右上角提交按钮的隐藏与显示
        // 3. 设置股票名称与股票价格的隐藏与显示，隐藏时清空内容
        // 4. 设置自定义软键盘的显示与否，搜索界面显示，编辑界面不显示

        viewSwitcher.setDisplayedChild(currentChildIndex);

        if (currentChildIndex == 1) {
            viewRightTitlebarItem.setVisibility(View.VISIBLE);

            tvStockName.setVisibility(View.VISIBLE);
            tvStockPrice.setVisibility(View.VISIBLE);

            // 隐藏自定义软键盘
            if (keyboardUtil != null && keyboardUtil.isKeyboardShow()) {
                keyboardUtil.hideKeyboard();
            }

            // 目标仓位输入框获取焦点，并自动弹出软键盘
            etPosition.setFocusable(true);
            etPosition.setFocusableInTouchMode(true);
            etPosition.requestFocus();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) etPosition.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etPosition, 0);
                }
            }, 300);
        } else {
            viewRightTitlebarItem.setVisibility(View.INVISIBLE);

            tvStockName.setText("");
            tvStockPrice.setText("");
            tvStockName.setVisibility(View.INVISIBLE);
            tvStockPrice.setVisibility(View.INVISIBLE);

            // 显示自定义软键盘
            int lastKeyboardType = getDBHelper().getInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);
            showStockKeyBoard(lastKeyboardType);
        }
    }

    /**
     * 组合已包含股票提示框
     * */
    private void showWarnDialog() {
        final CustomDialog dialog = new CustomDialog(getContext(), new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {}

            @Override
            public void onCancelBtnClicked() {}
        });

        dialog.setCustomMessage("该支股票已添加在组合中");
        dialog.setButtonText("确定", "");
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.show();
    }

    /**
     * 请求添加股票到组合
     * */
    private void requestAddMotifPosition(String goodsCode, int srcPos, int destPos, String remark, float price) {
        if (groupId == 0 || TextUtils.isEmpty(goodsCode)) {
            return;
        }

        if (srcPos < minPosition || srcPos > maxPosition) {
            showTip("初始仓位越界");
            return;
        }

        if (destPos < minPosition || destPos > maxPosition) {
            showTip("目标仓位越界");
            return;
        }

        if (srcPos == destPos) {
            showTip("初始仓位与目标仓位不能相同");
            return;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();
        String groupCode = String.valueOf(groupId);

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_RESET_POSITION, token, groupCode, goodsCode, String.valueOf(srcPos), String.valueOf(destPos), remark, String.valueOf(price));
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_RESET_POSITION);
    }

    /**
     * 获取指定股票的可操作仓位
     * */
    private void requestAvailablePosition(String goodsCode) {
        if (groupId == 0 || TextUtils.isEmpty(goodsCode)) {
            return;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();
        String groupCode = String.valueOf(groupId);

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_AVAILABLE_POSITION, token, groupCode, goodsCode);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_AVAILABLE_POSITION);
    }

    /**
     * 获取股票最新价
     * */
    private void requestGoodsZxj(int goodsId) {
        if (goodsId < 0)
            return;
        ArrayList<Integer> goodsIds = new ArrayList<Integer>();
        goodsIds.add(goodsId);

        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.ZXJ);
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_ZXJ));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4).setGroupType(0).addAllGoodsId(goodsIds).addAllReqFields(goodsFiled)
        // -9999 代表不排序
                .setSortField(-9999).setSortOrder(false).setReqBegin(0).setReqSize(0).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);

        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage ddpkg = (DynaValueDataPackage) pkg;
            int id = ddpkg.getRequestType();

            if (id == REQUEST_TYPE_ZXJ) {
                DynaValueData_Reply reply = ddpkg.getResponse();
                if (reply == null || reply.getRepFieldsList().size() == 0 || reply.getQuotaValueList().size() == 0) {
                    return;
                }

                List<Integer> listReqFieldIds = reply.getRepFieldsList();
                List<DynaQuota> listQuotaValues = reply.getQuotaValueList();

                if (listQuotaValues != null && listQuotaValues.size() > 0) {
                    DynaQuota quotaValue = listQuotaValues.get(0);

                    List<String> listReqFieldValues = quotaValue.getRepFieldValueList();

                    int indexZXJ = listReqFieldIds.indexOf(GoodsParams.ZXJ);
                    String fieldValuePrice = listReqFieldValues.get(indexZXJ);
                    String currentPrice = DataUtils.getPrice(fieldValuePrice);

                    tvStockPrice.setText(currentPrice);
                }

            }
        }

    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == BuyClubHttpUrl.FLAG_GROUP_RESET_POSITION) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();
            // {"body":"{\"retcode\":0,\"retmsg\":\"无权限关联股票\"}","errorCode":0}

            try {
                JSONObject objResult = JSONObject.parseObject(msgData);

                if (objResult != null && objResult.containsKey("errorCode") && objResult.containsKey("body")) {
                    int errorCode = objResult.getIntValue("errorCode");
                    if (errorCode == 0) {
                        String body = objResult.getString("body");
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode")) {
                            int retCode = objBody.getIntValue("retcode");
                            String retMsg = objBody.getString("retmsg");
                            if (retCode == 1) {
                                // 添加股票到组合成功，关闭当前界面
                                showTip("提交成功");
                                AddMotifPositionPage.this.finish();
                                return;
                            } else {
                                // 添加股票到组合失败
                                showTip("操作失败： " + retMsg);
                                return;
                            }
                        }

                    }
                }

            } catch (Exception e) {
            }

            showTip("操作失败");
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_AVAILABLE_POSITION) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();
            // {"body":"{\"retcode\":1,\"retmsg\":null,\"message\":{\"min\":6,\"max\":66,\"cur\":7}}","errorCode":0}

            try {
                JSONObject objReturn = JSONObject.parseObject(msgData);

                if (objReturn != null && objReturn.containsKey("body") && objReturn.containsKey("errorCode")) {
                    int errorCode = objReturn.getIntValue("errorCode");
                    String body = objReturn.getString("body");

                    if (errorCode == 0) {
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode") && objBody.containsKey("message")) {
                            int retCode = objBody.getIntValue("retcode");
                            String message = objBody.getString("message");

                            if (retCode == 1) {
                                JSONObject objMsg = JSONObject.parseObject(message);

                                if (objMsg != null && objMsg.containsKey("min") && objMsg.containsKey("max")) {
                                    minPosition = objMsg.getIntValue("min");
                                    maxPosition = objMsg.getIntValue("max");

                                    // 更新提示显示
                                    tvAvailablePosition.setText(String.format(FORMAT_AVAILABLE_POSITION, minPosition, maxPosition));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

        }

    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "股票建仓");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        viewRightTitlebarItem = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_txt, null);
        TextView tvTitle = (TextView) viewRightTitlebarItem.findViewById(R.id.tv_titlebar_text);
        tvTitle.setText("提交");
        viewRightTitlebarItem.setVisibility(View.GONE);

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, viewRightTitlebarItem);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {

    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        super.onPageTitleBarMenuItemSelected(menuitem);

        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            // 建仓
            int srcPos = 0;
            int destPos = 0;
            try {
                destPos = Integer.parseInt(etPosition.getText().toString().trim());
            } catch (Exception e) {
            }
            String remark = etReason.getText().toString().trim();
            try {
                remark = Base64.encodeToString(remark.getBytes("utf-8"), Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            float price = 0f;
            try {
                price = Float.parseFloat(etPrice.getText().toString().trim());
            } catch (Exception e) {
            }
            requestAddMotifPosition(currentGoodsCode, srcPos, destPos, remark, price);
        }
    }

    @Override
    public void onFuncKeyClick(int keyId, String keyName) {}

    private class SearchAdapter extends BaseAdapter {

        private List<CellBean> listDatas;
        private LayoutInflater inflater;

        public SearchAdapter(List<CellBean> listDatas, Context context) {
            this.listDatas = listDatas;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return listDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_add_motifposition_listitem, parent, false);

                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final CellBean bean = listDatas.get(position);
            vh.tvStockName.setText(bean.goodsName);
            vh.tvStockCode.setText(bean.goodsCode);

            vh.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                     * 组合中是否已包含该股票，如果已包含，弹出对话框提示，点击确定后，隐藏对话框
                     */
                    if (listGoodsCodes.contains(bean.goodsCode)) {
                        // 弹出对话框提示组合中已包含该股票
                        showWarnDialog();
                    } else {
                        // 切换子界面显示为编辑模式
                        currentChildIndex = 1;
                        setChildDisplay();

                        currentGoodsCode = bean.goodsCode;

                        isSelect = true;
                        etSearch.setText(bean.goodsCode);
                        tvStockName.setText(bean.goodsName);

                        // 获取股票当前价格，将当前股票价格显示在输入框中
                        requestGoodsZxj(bean.goodsId);

                        // 获取仓位范围，将仓位范围显示在说明中
                        requestAvailablePosition(bean.goodsCode);
                    }

                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;
            public TextView tvStockName, tvStockCode;

            public ViewHolder(View layout) {
                this.layout = layout;

                tvStockName = (TextView) layout.findViewById(R.id.tv_stock_name);
                tvStockCode = (TextView) layout.findViewById(R.id.tv_stock_code);
            }

        }

    }

    private class CellBean {
        public String goodsName, goodsCode;
        public int goodsId;

        public CellBean(String goodsName, String goodsCode, int goodsId) {
            this.goodsName = goodsName;
            this.goodsCode = goodsCode;
            this.goodsId = goodsId;
        }

    }

}
