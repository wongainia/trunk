package cn.emoney.acg.page.equipment.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.QuoteSelectionStrategy;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.helper.GoodsComparator;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.SupportEquipment;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.utils.SymbolSortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper.OnSortListener;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 飞龙在天
 * 
 * @author emoney_sky
 *
 */
public class FLZTPage extends PageImpl {

    public static final String EXTRA_KEY_TYPE = "key_type";

    private static String STRATEGY_NAME = "飞龙在天";

    private static final String ITEM_TYPE = "item_type";

    private ArrayList<Goods> mLstGoods = new ArrayList<Goods>();

    private ArrayList<Integer> mLstCheckedGid = new ArrayList<Integer>();


    private RefreshListView mListView = null;
    private LinearLayout mLlLvEmpty = null;
    private FLZTLvAdapter mAdapter = null;

    private PopupWindow mCheckTypeWin = null;
    private ListView mLvChoose = null;
    private PopChooseAdapter mChooseAdapter = null;

    private SymbolSortHelper mSortHelper = null;

    private List<Map<String, Object>> mLstChooseData = new ArrayList<Map<String, Object>>();

    private BarMenuTextItem mCenterItem = null;
    private boolean bAlreadyGotFLZTList = false;

    int mStrategyId = 0;

    private ImageView mVAddZxg;

    private static String[] OTHER_TYPE_NAME_NORMAL = {"涨跌幅", "主力净流", "5日涨跌"};
    private static int[] OTHER_TYPE_FIELD_NORMAL = {GoodsComparator.SORTTYPE_ZDF, GoodsComparator.SORTTYPE_JL, GoodsComparator.SORTTYPE_ZDF5};
    private int mCurDisplayIndex = 0;

    /**
     * 
     * 排序的字段,参照:GoodsComparator.SORTTYPE_PRICE 正数为升序,负数为降序
     */
    private int mLastSortField = -999;
    private TextView mLastSortItem = null;
    private int mLastSortType = -999;
    private List<String> mLstSortFieldName = new ArrayList<String>(3);

    @Override
    protected void receiveData(Bundle arguments) {

        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_TYPE)) {
                int typeId = arguments.getInt(EXTRA_KEY_TYPE);
                if (typeId != 0) {
                    STRATEGY_NAME = SupportEquipment.getInstance().getById(typeId).title;
                    mStrategyId = QuoteSelectionStrategy.getInstance().getStrategyIdByProductId(typeId);
                }
            }
        }
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_equipment_flzt);

        getSQLiteDBHelper();

        mVAddZxg = (ImageView) findViewById(R.id.item_flzt_zddzxg);
        mVAddZxg.setEnabled(false);

        mVAddZxg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionalTypeWin(v);
            }
        });

        // setContentView(R.layout.include_stock_list);
        mListView = (RefreshListView) findViewById(R.id.flztpage_listview);
        mLlLvEmpty = (LinearLayout) findViewById(R.id.flztpage_ll_lvempty);

        if (mListView != null) {
            mListView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
            mListView.initWithHeader(R.layout.layout_listview_header);
            mListView.setEmptyView(mLlLvEmpty);

            mAdapter = new FLZTLvAdapter();
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    // 因增加了头,index - 1 对应同LstGoods下标
                    if (FLZTPage.this.mPageChangeFlag == 0) {
                        QuoteJump.gotoQuote(FLZTPage.this, mLstGoods, index - 1);
                        // gotoQuote(mLstGoods, index - 1);
                    }
                }
            });
            mListView.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    requestData();
                }

                @Override
                public void beforeRefresh() {}

                @Override
                public void afterRefresh() {

                }
            });

            // 排序
            TextView tvSortField0 = (TextView) findViewById(R.id.tv_optional_sortfield0);
            TextView tvSortField1 = (TextView) findViewById(R.id.tv_optional_sortfield1);
            TextView tvSortField2 = (TextView) findViewById(R.id.tv_optional_sortfield2);

            mSortHelper = new SymbolSortHelper();
            mSortHelper.setItemTextColor(RColor(R.color.t3));
            mSortHelper.setItemSelectedTextColor(RColor(R.color.c4));
            mSortHelper.addSortItem(tvSortField0, SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);
            mSortHelper.addSortItem(tvSortField1, SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);
            mSortHelper.addSortItem(tvSortField2, SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);

            initFiled();

            setSortAction();


        }

        bindPageTitleBar(R.id.titleBar);
    }

    @Override
    protected void initData() {
        // mLstOrigLst.clear();
        mLstCheckedGid.clear();

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        int count = mLstGoods.size();
        if (mCenterItem != null && count > 0) {
            mCenterItem.getItemView().setText(STRATEGY_NAME + "(" + count + ")");
        }

        if (!getIsAutoRefresh()) {
            LogUtil.easylog("sky", "MarketHot -> onPageResume -> startRequestTask");
            startRequestTask();
        }

    }


    private void initFiled() {
        mLstSortFieldName.add("股票名称");
        mLstSortFieldName.add("最新价");
        mLstSortFieldName.add("涨跌幅");
        mCurDisplayIndex = 0;

        mLastSortField = -999;
        mLastSortItem = null;
        mLastSortType = -999;

        mSortHelper.updateItemLable(mLstSortFieldName);
    }


    private void setSortAction() {
        if (mSortHelper != null) {
            mSortHelper.setOnSortListener(new OnSortListener() {

                @Override
                public void onSort(TextView view, int sortType) {
                    if (view != null) {
                        mLastSortItem = view;
                        mLastSortType = sortType;

                        int id = view.getId();
                        // 分类为持仓
                        if (id == R.id.tv_optional_sortfield0) {

                            if (sortType == SymbolSortHelper.SORT_RISE) {
                                mLastSortField = GoodsComparator.SORTTYPE_STOCKID;
                            } else {
                                mLastSortField = -GoodsComparator.SORTTYPE_STOCKID;
                            }
                            Collections.sort(mLstGoods, new GoodsComparator(mLastSortField));
                        } else if (id == R.id.tv_optional_sortfield1) {
                            if (sortType == SymbolSortHelper.SORT_RISE) {
                                mLastSortField = GoodsComparator.SORTTYPE_PRICE;
                            } else {
                                mLastSortField = -GoodsComparator.SORTTYPE_PRICE;
                            }
                            Collections.sort(mLstGoods, new GoodsComparator(mLastSortField));
                        } else if (id == R.id.tv_optional_sortfield2) {

                            if (sortType == SymbolSortHelper.SORT_RISE) {
                                mLastSortField = OTHER_TYPE_FIELD_NORMAL[mCurDisplayIndex];
                            } else {
                                mLastSortField = -OTHER_TYPE_FIELD_NORMAL[mCurDisplayIndex];
                            }
                            Collections.sort(mLstGoods, new GoodsComparator(mLastSortField));
                        }
                    }


                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }

                }
            });
        }
    }

    @Override
    public void requestData() {
        if (bAlreadyGotFLZTList) {
            getDataFromNet();
        } else {
            requestFTZTList();
        }

    }

    private void requestFTZTList() {
        UserInfo info = DataModule.getInstance().getUserInfo();

        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KeysInterface.KEY_TOKEN, info.getToken());
            jsObj.put(KeysInterface.KEY_CLID, mStrategyId);
            LogUtil.easylog("sky", "FLZTPage->requestFLZTList:" + jsObj.toString());
            requestInfo(jsObj, IDUtils.ID_STOCK_SELECTION_STRATEGY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDataFromNet() {
        if (mLstGoods.size() == 0) {
            mListView.onRefreshFinished();
            return;
        }
        ArrayList<Integer> goodsId = new ArrayList<Integer>();
        for (Goods goods : mLstGoods) {
            goodsId.add(goods.getGoodsId());
        }

        ArrayList<Integer> reqFileds = new ArrayList<Integer>();
        reqFileds.add(GoodsParams.ZXJ);// 最新价
        reqFileds.add(GoodsParams.ZDF);// 涨跌幅
        reqFileds.add(GoodsParams.CPX_DAY);// 操盘线天数
        reqFileds.add(GoodsParams.JL);// 净流
        reqFileds.add(GoodsParams.GROUP_HY);// 所属行业
        reqFileds.add(GoodsParams.FIVEZDF);// 5日涨跌
        reqFileds.add(GoodsParams.SYL);// 市盈率
        reqFileds.add(GoodsParams.SJL);// 市盈率

        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
        pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));
        requestQuote(pkg, IDUtils.DynaValueData);
        LogUtil.easylog("sky", "FLZTPage->requestQuote");
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
            DynaValueData_Reply gr = goodsTable.getResponse();
            if (gr == null || gr.getRepFieldsList().size() == 0 || gr.getQuotaValueList().size() == 0) {
                return;
            }

            int indexZXJ = gr.getRepFieldsList().indexOf(GoodsParams.ZXJ);
            int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);
            int indexJL = gr.getRepFieldsList().indexOf(GoodsParams.JL);
            int indexFIVEZDF = gr.getRepFieldsList().indexOf(GoodsParams.FIVEZDF);
            int indexSYL = gr.getRepFieldsList().indexOf(GoodsParams.SYL);
            int indexIndustry = gr.getRepFieldsList().indexOf(GoodsParams.GROUP_HY);
            int indexDayCpx = gr.getRepFieldsList().indexOf(GoodsParams.CPX_DAY);
            int indexSJL = gr.getRepFieldsList().indexOf(GoodsParams.SJL);

            List<DynaQuota> quota = gr.getQuotaValueList();

            for (int i = 0; i < quota.size(); i++) {
                int goodsId = quota.get(i).getGoodsId();

                String price = quota.get(i).getRepFieldValueList().get(indexZXJ);
                String zdf = quota.get(i).getRepFieldValueList().get(indexZDF);
                String sjl = quota.get(i).getRepFieldValueList().get(indexSJL);
                String jl = quota.get(i).getRepFieldValueList().get(indexJL);
                String zdf5 = quota.get(i).getRepFieldValueList().get(indexFIVEZDF);
                String industryId = quota.get(i).getRepFieldValueList().get(indexIndustry);
                String syl = quota.get(i).getRepFieldValueList().get(indexSYL);
                String day_cpx = quota.get(i).getRepFieldValueList().get(indexDayCpx);

                for (int j = 0; j < mLstGoods.size(); j++) {
                    Goods g = mLstGoods.get(j);
                    if (goodsId == g.getGoodsId()) {
                        g.setZxj(DataUtils.getPrice(price));
                        g.setFiveZdf(zdf5);
                        int color = FontUtils.getColorByZDF(zdf);
                        g.setQuoteColor(color);
                        g.setZdf(zdf);
                        g.setJl(jl);
                        g.setBKId(Integer.valueOf(industryId));
                        g.setDayBS(Integer.valueOf(day_cpx));
                        g.setSyl(DataUtils.getSYL(syl));
                        g.setSjl(DataUtils.getSJL(sjl));

                        break;
                    }
                }
            }
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            // mListView.onRefreshFinished();
            // getDBHelper().setString("refresh_ftzt", DateUtils.getCurrentQuoteDate());
        }

    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        if (pkg instanceof GlobalMessagePackage) {
            GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
            MessageCommon gr = goodsTable.getResponse();
            if (gr == null || gr.getMsgData() == null) {
                return;
            }

            String msgData = gr.getMsgData();
            LogUtil.easylog("sky", "FLZTPage->updateFromInfo:" + msgData);
            if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                return;
            }
            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retCode = jsObj.getIntValue("result");
                if (retCode != 0) {
                    showTip(jsObj.getString("message"));
                    return;
                }

                // JSONArray jAry = jsObj.getJSONArray("stocks");
                boolean b = jsObj.containsKey("stocks");
                if (b) {
                    LogUtil.easylog("ddddddddjkldsajflkjdsalkfjd");
                    JSONArray jAry = jsObj.getJSONArray("stocks");
                }

                JSONArray jAry = jsObj.getJSONArray("stocks");
                if (jAry != null && jAry.size() > 0) {

                    // mLstOrigLst.clear();
                    mLstGoods.clear();

                    Goods g;
                    for (int i = 0; i < jAry.size(); i++) {
                        String t_gCode = jAry.getString(i);

                        String t_gid = null;
                        if (!t_gCode.startsWith("6")) {
                            t_gid = Util.FormatStockCode("1" + t_gCode);
                        } else {
                            t_gid = Util.FormatStockCode(t_gCode);
                        }

                        ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);
                        if (m_pGN != null && m_pGN.size() > 0) {
                            g = m_pGN.get(0);
                            mLstGoods.add(g);
                            // mLstOrigLst.add(g.getGoodsId());
                        }
                    }

                    int count = mLstGoods.size();
                    if (mCenterItem != null && count > 0) {
                        mCenterItem.getItemView().setText(STRATEGY_NAME + "(" + count + ")");
                    }

                    closeSQLDBHelper();

                    bAlreadyGotFLZTList = true;
                    // getDataFromNet();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getDataFromNet();
                        }
                    }, 150);

                }

            } catch (JSONException e) {
                LogUtil.easylog("sky", e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        LogUtil.easylog("sky", "FLZTPage -> onCreatePageBarMenu");
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        mCenterItem = new BarMenuTextItem(1, STRATEGY_NAME);
        mCenterItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(mCenterItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        LogUtil.easylog("sky", "FLZTPage -> onPageBarMenuItemSelected");
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    @Override
    protected View getPageBarMenuProgress() {
        return null;
    }

    @Override
    public void showProgress() {
        // super.showProgress();
    }

    @Override
    public void dismissProgress() {
        super.dismissProgress();
        if (mListView != null) {
            mListView.onRefreshFinished();
        }
    }

    class FLZTLvAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mLstGoods.size();
        }

        @Override
        public Object getItem(int position) {
            return mLstGoods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_flzt_listitem, null);
                ImageView ivCheck = (ImageView) convertView.findViewById(R.id.item_iv_ischecked);
                TextView tvName = (TextView) convertView.findViewById(R.id.tv_item_0_0);
                TextView tvCode = (TextView) convertView.findViewById(R.id.tv_item_0_1);
                TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_item1);
                TextView tvOther = (TextView) convertView.findViewById(R.id.tv_item_2);
                View vClickArea = convertView.findViewById(R.id.fr_item_click_area);

                convertView.setTag(new ListCell(ivCheck, tvName, tvCode, tvPrice, tvOther, vClickArea));
            }
            ListCell lc = (ListCell) convertView.getTag();



            Goods goods = (Goods) getItem(position);

            lc.vClickArea.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurDisplayIndex = mCurDisplayIndex >= 2 ? 0 : mCurDisplayIndex + 1;
                    mLstSortFieldName.set(2, OTHER_TYPE_NAME_NORMAL[mCurDisplayIndex]);

                    if (mLastSortItem != null && mLastSortItem.getId() == R.id.tv_optional_sortfield2) {
                        mSortHelper.updateItemLable(mLstSortFieldName);

                        if (OTHER_TYPE_FIELD_NORMAL[mCurDisplayIndex] == Math.abs(mLastSortField)) {
                            mSortHelper.setDefaultSort(mLastSortItem, mLastSortType);
                        }

                    } else {
                        mSortHelper.updateItemLable_exceptSort(mLstSortFieldName);
                    }
                    mSortHelper.notifySort();
                }
            });



            lc.ivCheck.setTag(goods.getGoodsId());
            if (mLstCheckedGid.contains(goods.getGoodsId())) {
                lc.ivCheck.setSelected(true);
            } else {
                lc.ivCheck.setSelected(false);
            }

            lc.tvName.setText(goods.getGoodsName());
            lc.tvCode.setText(goods.getGoodsCode());
            lc.tvPrice.setText(goods.getZxj());

            switch (mCurDisplayIndex) {
                case 0: {// 涨跌幅
                    String sZDF = goods.getZdf();
                    lc.tvOther.setText(DataUtils.getSignedZDF(sZDF));

                    int colorFlag = FontUtils.getColorByZDF(sZDF);
                    int resZDP = getZDPRadiusBg(colorFlag);
                    lc.tvOther.setBackgroundResource(resZDP);
                }
                    break;

                case 1: {// 主力净流
                    lc.tvOther.setText(DataUtils.formatJL(goods.getJl(), DataUtils.mDecimalFormat1_max));

                    int colorFlag = FontUtils.getColorByZD(goods.getJl());
                    int resZDP = getZDPRadiusBg(colorFlag);
                    lc.tvOther.setBackgroundResource(resZDP);
                }
                    break;
                case 2: {// 5涨跌幅
                    String sZDF5 = goods.getFiveZdf();
                    lc.tvOther.setText(DataUtils.getSignedZDF(sZDF5));

                    int colorFlag = FontUtils.getColorByZDF(sZDF5);
                    int resZDP = getZDPRadiusBg(colorFlag);
                    lc.tvOther.setBackgroundResource(resZDP);
                }
                    break;
                default:
                    break;
            }
            return convertView;
        }

    }



    private class ListCell {

        public ListCell(ImageView ivCheck, TextView tvName, TextView tvCode, TextView tvPrice, TextView tvOther, View vClickArea) {
            this.ivCheck = ivCheck;
            this.tvPrice = tvPrice;
            this.tvOther = tvOther;
            this.tvName = tvName;
            this.tvCode = tvCode;
            this.vClickArea = vClickArea;
            addCheckListener();
        }

        private void addCheckListener() {
            // check
            ivCheck.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer gid = (Integer) v.getTag();
                    int index = mLstCheckedGid.indexOf(gid);
                    if (index >= 0) {
                        mLstCheckedGid.remove(index);
                    } else {
                        mLstCheckedGid.add(gid);
                    }

                    if (mLstCheckedGid.size() > 0) {
                        mVAddZxg.setEnabled(true);
                    } else {
                        mVAddZxg.setEnabled(false);
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        public ImageView ivCheck;
        public TextView tvName;
        public TextView tvCode;
        public TextView tvPrice;
        public TextView tvOther;
        public View vClickArea;

    }

    private void showOptionalTypeWin(View v) {
        int t_itemH = FontUtils.dip2px(getContext(), 45);

        if (mCheckTypeWin == null) {

            mLvChoose = new ListView(getActivity());
            mLvChoose.setDivider(getContext().getResources().getDrawable(R.drawable.img_light_divider_line_short));
            // mLvChoose.setDivider(null);
            mLvChoose.setDividerHeight(1);
            mLvChoose.setVerticalScrollBarEnabled(false);
            mLvChoose.setCacheColorHint(0x00000000);
            final OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            List<String> types = optionalInfo.getTypes();
            for (int i = 0; i < types.size(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(ITEM_TYPE, types.get(i));
                mLstChooseData.add(map);
            }
            mChooseAdapter = new PopChooseAdapter(getContext(), mLstChooseData, R.layout.page_editoptional_poplistitem, new String[] {ITEM_TYPE}, new int[] {R.id.item_tv_title});

            mLvChoose.setAdapter(mChooseAdapter);
            mLvChoose.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    Map<String, Object> map = mLstChooseData.get(index);

                    final String type = (String) map.get(ITEM_TYPE);

                    final ArrayList<Goods> lstGoods = new ArrayList<Goods>();
                    for (int i = 0; i < mLstGoods.size(); i++) {
                        Goods g = mLstGoods.get(i);
                        int t_index = mLstCheckedGid.indexOf((Integer) g.getGoodsId());
                        if (t_index >= 0) {
                            lstGoods.add(g);
                        }
                    }

                    if (type.equals(OptionalInfo.TYPE_POSITION)) {
                        int t_has = optionalInfo.getCountByType(type);
                        if ((lstGoods.size() + t_has) > 15) {
                            showTip("超过最大持仓个数(15个)");
                            mCheckTypeWin.dismiss();
                            return;
                        }
                    }

                    final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();

                    if (!bIsLogin) {
                        // 不支持
                    } else {
                        LogUtil.easylog("sky", "addZXG(type, lstGoods, new OnOperateZXGListener(), type = " + type);

                        String t_type = OptionalInfo.TYPE_KEY_ALL;
                        if (!type.equals(OptionalInfo.TYPE_DEFAULT)) {
                            t_type = type;
                        }

                        addZXG(t_type, lstGoods, new OnOperateZXGListener() {
                            @Override
                            public void onOperate(boolean isSuccess, String msg) {
                                // TODO Auto-generated method stub
                                if (isSuccess) {
                                    optionalInfo.addAll(type, lstGoods);
                                    optionalInfo.save(getDBHelper());
                                    showTip("添加自选成功");
                                } else {
                                    showTip("添加自选失败");
                                }
                            }
                        });
                    }

                    mCheckTypeWin.dismiss();
                }
            });

            mCheckTypeWin = new PopupWindow(getActivity());
            mCheckTypeWin.setWidth(v.getMeasuredWidth() + 80);
            mCheckTypeWin.setHeight(LayoutParams.WRAP_CONTENT);
            mCheckTypeWin.setTouchable(true);
            mCheckTypeWin.setOutsideTouchable(true);
            mCheckTypeWin.setFocusable(true);
            mCheckTypeWin.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.img_editoptional_addtotype_bg));

            mCheckTypeWin.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {

                }
            });

            mCheckTypeWin.setContentView(mLvChoose);
        }

        int[] location = new int[2];
        v.getLocationOnScreen(location);

        int t_h = t_itemH * mLstChooseData.size();

        if (!mCheckTypeWin.isShowing()) {
            mCheckTypeWin.showAtLocation(v, Gravity.NO_GRAVITY, location[0] - 40, location[1] - t_h);
        }
    }

    class PopChooseAdapter extends SimpleAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);

        }

        public PopChooseAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

    }

}
