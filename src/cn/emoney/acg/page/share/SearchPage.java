package cn.emoney.acg.page.share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.KeyboardUtil;
import cn.emoney.acg.util.KeyboardUtil.KeyboardListener;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.RegularExpressionUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.page.PageIntent;

public class SearchPage extends PageImpl implements KeyboardListener {

    public static void gotoSearch(Page page) {
        gotoSearch(page, null);
    }

    public static void gotoSearch(Page page, String optionalType) {
        PageIntent intent = new PageIntent(page, SearchPage.class);
        intent.setFlags(PageIntent.FLAG_PAGE_CLEAR_TOP);
        if (optionalType != null && !optionalType.equals("")) {
            Bundle bundle = new Bundle();
            bundle.putString(OptionalInfo.KEY_OPTIONAL_TYPE, optionalType);
            intent.setArguments(bundle);
        }

        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    private boolean isClick = false;// 是点击跳转了

    private ListView mLvStocks = null;

    private SearchAdapter mAdapter = null;
    private List<Map<String, Object>> mLstData = new ArrayList<Map<String, Object>>();
    private ArrayList<Goods> mLstGoods = new ArrayList<Goods>();
    private final String ITEM_STOCKNAME = "item_stockname";
    private final String ITEM_STOCKCODE = "item_stockcode";
    private final String ITEM_ADDORDEL = "item_addordel";
    private final String ITEM_GOODSID = "item_goodsid";
    private final String ITEM_GOODS = "item_goods";

    private KeyboardUtil keyBoardUtil = null;
    private EditText et = null;
    private ImageView mIvClearEt = null;

    private OptionalInfo mOptionalInfo = null;

    private final int MAX_RESULT = 30;

    // private int mLastXmlId = R.xml.keyboard_stock_numbers;

    private String mCurOptionalType = null;

    public SearchPage() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onFuncKeyClick(int keyId, String keyName) {
        tryGotoQuotePage(0);
    }

    private void tryGotoQuotePage(int index) {
        if (keyBoardUtil == null) {
            keyBoardUtil = new KeyboardUtil(getContentView(), getContext(), et);
        }

        if (index == 0 && mLstData.size() == 0) {
            return;
        }

        if (keyBoardUtil != null && keyBoardUtil.isKeyboardShow()) {
            keyBoardUtil.hideKeyboard();
        }

        Map<String, Object> map = mLstData.get(index);
        Goods g = (Goods) map.get(ITEM_GOODS);

        int gid = g.getGoodsId();
        Integer[] arySearchGid = getDBHelper().getIntegerArray(DataModule.G_KEY_SEARCH_HISTORY, new Integer[] {});
        List<Integer> t_lstGid = Arrays.asList(arySearchGid);
        List<Integer> lstGid = new ArrayList<Integer>(t_lstGid);

        lstGid.remove(Integer.valueOf(gid));
        if (lstGid.size() > 20) {
            lstGid.remove(t_lstGid.size() - 1);
        }

        lstGid.add(0, Integer.valueOf(gid));

        getDBHelper().setIntegerArray(DataModule.G_KEY_SEARCH_HISTORY, lstGid);
        QuoteJump.gotoQuote(SearchPage.this, g);
        // gotoQuote(g);

        isClick = true;
    }

    @Override
    protected void receiveData(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(OptionalInfo.KEY_OPTIONAL_TYPE)) {
                mCurOptionalType = arguments.getString(OptionalInfo.KEY_OPTIONAL_TYPE);
            }
        }
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_search);

        mLvStocks = (ListView) findViewById(R.id.pagesearch_btn_listview);
        if (mLvStocks != null) {
            mAdapter = new SearchAdapter();
            mLvStocks.setAdapter(mAdapter);
            mLvStocks.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    tryGotoQuotePage(index);
                }
            });

            mLvStocks.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // TODO Auto-generated method stub
                    // if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        if (keyBoardUtil != null && keyBoardUtil.isKeyboardShow()) {
                            keyBoardUtil.hideKeyboard();
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // TODO Auto-generated method stub
                }
            });
        }

        bindPageTitleBar(R.id.page_search_titlebar);
    }

    @Override
    protected void initData() {
        getSQLiteDBHelper();
    }

    private void doSearch(String input) {
        ArrayList<Goods> lstGoods = getSrearchResultByLocal(input);
        mLstGoods.clear();
        mLstGoods.addAll(lstGoods);
        updateData();

        if (input != null && input.trim().length() > 0) {
            setTipLayoutVisiblity(false);
        } else {
            setTipLayoutVisiblity(true);
        }
    }

    private void updateData() {
        mLstData.clear();
        mOptionalInfo = DataModule.getInstance().getOptionalInfo();
        for (int i = 0; i < mLstGoods.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            Goods g = mLstGoods.get(i);
            map.put(ITEM_STOCKNAME, g.getGoodsName());
            map.put(ITEM_STOCKCODE, g.getGoodsCode());
            map.put(ITEM_GOODSID, g.getGoodsId());
            map.put(ITEM_GOODS, g);

            String tControlType = OptionalInfo.TYPE_DEFAULT;
            if (mCurOptionalType != null && !mCurOptionalType.equals("")) {
                tControlType = mCurOptionalType;
            }

            if (mOptionalInfo.hasGoods(tControlType, g.getGoodsId()) >= 0) {
                map.put(ITEM_ADDORDEL, R.drawable.img_delzxg_selected);
            } else {
                map.put(ITEM_ADDORDEL, R.drawable.img_addzxg_selected);
            }
            mLstData.add(map);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void onPageResume() {
        super.onPageResume();
        // 清空内容
        if (et != null && isClick) {
            et.setText("");
            isClick = true;
        }

        doSearch("");

        int lastKeyboardType = getDBHelper().getInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);
        showStockKeyBoard(lastKeyboardType);
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        // TODO Auto-generated method stub
        BarMenuCustomItem centerItem = new BarMenuCustomItem(2);

        View view = View.inflate(getContext(), R.layout.page_search_input, null);

        et = (EditText) view.findViewById(R.id.activity_search_et);

        // et.setInputType(InputType.TYPE_NULL);
        view.findViewById(R.id.backView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (keyBoardUtil == null) {
                    keyBoardUtil = new KeyboardUtil(getContentView(), getContext(), et);
                }
                if (keyBoardUtil.isKeyboardShow()) {
                    keyBoardUtil.hideKeyboard();
                }

                SearchPage.this.finish();
            }
        });

        mIvClearEt = (ImageView) view.findViewById(R.id.activity_search_clear);
        mIvClearEt.setVisibility(View.INVISIBLE);
        mIvClearEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(p);

        // et.setGravity(Gravity.LEFT);
        // et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
        et.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                LogUtil.easylog("sky", "et onTouch");
                int lastKeyboardType = getDBHelper().getInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);

                // 如果系统键盘已经显示 就先隐藏系统键盘
                showStockKeyBoard(lastKeyboardType);

                return true;
            }
        });

        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String text = et.getText().toString();
                doSearch(text);
                mLvStocks.setSelection(0);

                if (text.length() > 0) {
                    mIvClearEt.setVisibility(View.VISIBLE);
                } else {
                    mIvClearEt.setVisibility(View.INVISIBLE);
                }
            }
        });

        centerItem.setCustomView(view);
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);
        return true;
    }

    private void showStockKeyBoard(int type) {
        InputMethodUtil.closeSoftKeyBoard(this);

        et.requestFocus();
        et.requestFocusFromTouch();
        if (keyBoardUtil == null) {
            keyBoardUtil = new KeyboardUtil(getContentView(), getContext(), et);
        }
        keyBoardUtil.setEditText(et);
        keyBoardUtil.setOnkeyboardListener(SearchPage.this);
        if (!keyBoardUtil.isKeyboardShow()) {
            if (KeyboardUtil.TYPE_DIGIT == type) {
                keyBoardUtil.showKeyboard();
            } else if (KeyboardUtil.TYPE_ENGLISH == type) {
                keyBoardUtil.showKeyboardEnglish();
            }
        }

        InputMethodUtil.closeSoftKeyBoard(this);
    }

    protected ArrayList<Goods> getSrearchResultByLocal(String strInput) {
        ArrayList<Goods> cn = null;

        if (strInput == null || strInput.equals("")) {
            Integer[] arySearchGid = getDBHelper().getIntegerArray(DataModule.G_KEY_SEARCH_HISTORY, null);
            if (arySearchGid == null) {
                cn = getSQLiteDBHelper().queryStocksTop20(20);
            } else {
                if (arySearchGid.length == 0) {
                    cn = getSQLiteDBHelper().queryStocksTop20(20);
                } else {
                    List<Integer> lstGid = Arrays.asList(arySearchGid);
                    cn = getSQLiteDBHelper().queryStockArrayByCodes(lstGid);
                }
            }

        } else if (TextUtils.isDigitsOnly(strInput)) {
            // 数字
            cn = getSQLiteDBHelper().queryStockInfosByCode(strInput, MAX_RESULT);
        } else if (RegularExpressionUtil.isDigitalAndLetter(strInput)) {
            // 拼音和数字
            if (strInput.length() >= 2) {
                cn = getSQLiteDBHelper().queryStockInfosByPinyin(strInput, MAX_RESULT, true);
            } else {
                cn = getSQLiteDBHelper().queryStockInfosByPinyin(strInput, MAX_RESULT, false);
            }
        } else {
            // 汉字
            cn = getSQLiteDBHelper().queryStockInfoByName(strInput, MAX_RESULT, true);
        }

        closeSQLDBHelper();
        return cn;
    }

    class SearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mLstData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mLstData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_search_listitem, null);
                TextView tvStockName = (TextView) convertView.findViewById(R.id.item_tv_stockname);
                TextView tvStockCode = (TextView) convertView.findViewById(R.id.item_tv_stockcode);
                ImageView ivAddOrDel = (ImageView) convertView.findViewById(R.id.item_iv_addordel);
                convertView.setTag(new ListCell(tvStockName, tvStockCode, ivAddOrDel));
            }
            ListCell lc = (ListCell) convertView.getTag();

            final Map<String, Object> map = (Map<String, Object>) getItem(position);
            String stockName = (String) map.get(ITEM_STOCKNAME);
            final int resId = (Integer) map.get(ITEM_ADDORDEL);
            final int goodsId = (Integer) map.get(ITEM_GOODSID);
            String stockCode = (String) map.get(ITEM_STOCKCODE);
            final Goods goods = (Goods) map.get(ITEM_GOODS);
            goods.addType(OptionalInfo.TYPE_DEFAULT);

            lc.getTvStockName().setText(stockName);
            lc.getTvStockCode().setText(stockCode);
            lc.getIvAddOrDel().setImageResource(resId);
            lc.getIvAddOrDel().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();
                    ArrayList<Goods> lstGoods = new ArrayList<Goods>();
                    lstGoods.add(goods);
                    if (resId == R.drawable.img_delzxg_selected) {
                        if (!bIsLogin) {
                            String tAddtype = OptionalInfo.TYPE_DEFAULT;
                            if (mCurOptionalType != null) {
                                tAddtype = mCurOptionalType;
                            }

                            if (mOptionalInfo.delGoods(tAddtype, goodsId)) {
                                map.put(ITEM_ADDORDEL, R.drawable.img_addzxg_selected);
                                mOptionalInfo.save(getDBHelper());
                                notifyDataSetChanged();
                                showTip("删除自选成功");
                            }
                        } else {
                            String tAddType = mOptionalInfo.TYPE_KEY_ALL;

                            if (mCurOptionalType != null && !mCurOptionalType.equals("") && !mCurOptionalType.equals("所有自选")) {
                                tAddType = mCurOptionalType;
                            }

                            delZXG(tAddType, lstGoods, new OnOperateZXGListener() {
                                @Override
                                public void onOperate(boolean isSuccess, String msg) {
                                    // TODO Auto-generated
                                    // method stub
                                    if (isSuccess) {
                                        String tAddtype = OptionalInfo.TYPE_DEFAULT;
                                        if (mCurOptionalType != null) {
                                            tAddtype = mCurOptionalType;
                                        }
                                        if (mOptionalInfo.delGoods(tAddtype, goodsId)) {
                                            map.put(ITEM_ADDORDEL, R.drawable.img_addzxg_selected);
                                            mOptionalInfo.save(getDBHelper());
                                            notifyDataSetChanged();
                                            showTip("删除自选成功");
                                        } else {
                                            showTip("删除自选失败!");
                                        }
                                    } else {
                                        showTip(msg);
                                    }
                                }
                            });
                        }
                    } else if (resId == R.drawable.img_addzxg_selected) {
                        if (!bIsLogin) {
                            // 没登录
                            String tAddtype = OptionalInfo.TYPE_DEFAULT;
                            if (mCurOptionalType != null) {
                                tAddtype = mCurOptionalType;
                            }

                            if (mOptionalInfo.addGoods(tAddtype, goods)) {
                                map.put(ITEM_ADDORDEL, R.drawable.img_delzxg_selected);
                                mOptionalInfo.save(getDBHelper());
                                notifyDataSetChanged();
                                showTip("添加自选成功");
                            }
                        } else {
                            // 已登录
                            String tAddType = mOptionalInfo.TYPE_KEY_ALL;

                            if (mCurOptionalType != null && !mCurOptionalType.equals("") && !mCurOptionalType.equals("所有自选")) {
                                tAddType = mCurOptionalType;
                            }
                            addZXG(tAddType, lstGoods, new OnOperateZXGListener() {

                                @Override
                                public void onOperate(boolean isSuccess, String msg) {
                                    // TODO Auto-generated
                                    // method stub
                                    if (isSuccess) {
                                        String tAddtype = OptionalInfo.TYPE_DEFAULT;
                                        if (mCurOptionalType != null) {
                                            tAddtype = mCurOptionalType;
                                        }
                                        if (mOptionalInfo.addGoods(tAddtype, goods)) {
                                            map.put(ITEM_ADDORDEL, R.drawable.img_delzxg_selected);
                                            mOptionalInfo.save(getDBHelper());
                                            notifyDataSetChanged();
                                            showTip("成功添加到\"" + mCurOptionalType + "\"");
                                        } else {
                                            showTip("添加自选失败!");
                                        }
                                    } else {
                                        showTip(msg);
                                    }
                                }
                            });
                        }
                    }
                }
            });

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
            public ListCell(TextView tvStockName, TextView tvStockCode, ImageView ivAddOrDel) {
                this.tvStockName = tvStockName;
                this.tvStockCode = tvStockCode;
                this.ivAddOrDel = ivAddOrDel;
            }

            public TextView getTvStockName() {
                return tvStockName;
            }

            public TextView getTvStockCode() {
                return tvStockCode;
            }

            public ImageView getIvAddOrDel() {
                return ivAddOrDel;
            }

            private TextView tvStockName;
            private TextView tvStockCode;
            private ImageView ivAddOrDel;
        }

    }

    @Override
    protected void onPagePause() {
        InputMethodUtil.closeSoftKeyBoard(this);

        if (keyBoardUtil != null && keyBoardUtil.isKeyboardShow()) {
            keyBoardUtil.hideKeyboard();
        }
        super.onPagePause();
    }

    private void setTipLayoutVisiblity(boolean isVisible) {
        findViewById(R.id.headLayout).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
