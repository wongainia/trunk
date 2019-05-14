package cn.emoney.acg.page.optional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.helper.GoodsComparator;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.optional.MiniMarketBoardPage.MiniMarketBoardCB;
import cn.emoney.acg.page.share.LoginPage;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.utils.SymbolSortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper.OnSortListener;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

public class OptionalHome extends PageImpl {
    private static final int ZS_SH = 1; // 上证指数
    private static final int ZS_SZ = 1399001; // 深证成指
    private static final int ZS_CY = 1399006; // 创业板指

    public static final int REFRESH_DATA = 2001;
    public static final int REFRESH_PROFITLOSS = 2002;

    // public static boolean bIsNeedRefresh = true;

    private View mMiniMarketBar = null;
    private TextView mTvMinibarSH = null;
    private TextView mTvMinibarSZ = null;
    private TextView mTvMinibarCY = null;

    private MiniMarketBoardPage mMiniboardpage = null;

    private FrameLayout mFrMiniboardExpand = null;

    private MiniMarketBoardDispalyCB mMaskCallback = null;

    private TextView mTvLoginNotice = null;

    // 持仓详情board
    private View mVPositonStatistics = null;
    // private TextView


    private static String[] OTHER_TYPE_NAME_NORMAL = {"涨跌幅", "涨跌", "主力净流", "换手率"};
    private static int[] OTHER_TYPE_FIELD_NORMAL = {GoodsComparator.SORTTYPE_ZDF, GoodsComparator.SORTTYPE_ZD, GoodsComparator.SORTTYPE_JL, GoodsComparator.SORTTYPE_HSL};
    private static String[] OTHER_TYPE_NAME_POSITION = {"涨跌幅", "成本价", "浮动盈亏", "盈亏比", "持股数"};
    private static int[] OTHER_TYPE_FIELD_POSITION = {GoodsComparator.SORTTYPE_ZDF, GoodsComparator.SORTTYPE_COST_PRICE, GoodsComparator.SORTTYPE_PROFIT_LOSS, GoodsComparator.SORTTYPE_PROFIT_LOSS_PERCENT, GoodsComparator.SORTTYPE_POSITION_AMOUNT};
    private int mCurDisplayIndex = 0;

    private List<String> mLstSortFieldName = new ArrayList<String>(3);

    private SymbolSortHelper mSortHelper = null;
    /**
     * 
     * 排序的字段,参照:GoodsComparator.SORTTYPE_PRICE 正数为升序,负数为降序
     */
    private int mLastSortField = -999;
    private TextView mLastSortItem = null;
    private int mLastSortType = -999;

    private RefreshListView mLvOptional = null;
    private OptionalAdapter mAdapter = null;

    private ArrayList<Goods> mLstGoods = new ArrayList<Goods>();
    private String mOptionalType = OptionalInfo.TYPE_DEFAULT;


    private class DynaValue {
        String zdf = "";
    }

    private Map<Integer, DynaValue> mMapDynaValue = new HashMap<Integer, DynaValue>(3);
    private TextView mTv_total_market_value_content;
    private TextView mTv_total_profitAndLoss_content;
    private TextView mTv_total_profitAndLoss_percent_content;
    private TextView tvSortField0;

    private Handler mHandler_optionalHome;

    /* public func */
    public void setMaskCB(MiniMarketBoardDispalyCB callback) {
        mMaskCallback = callback;
    }

    public void closeMask() {
        if (mMiniMarketBar != null) {
            if (isLogined()) {
                mMiniMarketBar.setVisibility(View.VISIBLE);
            } else {
                mMiniMarketBar.setVisibility(View.INVISIBLE);
            }
        }
        if (mFrMiniboardExpand != null) {
            mFrMiniboardExpand.setVisibility(View.GONE);
            mMiniboardpage.setIsHide(true);
        }
    }

    private void refreshMiniMarketBar() {
        if (mTvMinibarSH != null && mMapDynaValue.containsKey(ZS_SH)) {
            String zdf = mMapDynaValue.get(ZS_SH).zdf;
            int colorFlag = FontUtils.getColorByZDF_percent(zdf);
            int color = getZDPColor(colorFlag);
            mTvMinibarSH.setText("上证 " + zdf);
            mTvMinibarSH.setTextColor(color);
        }
        if (mTvMinibarSZ != null && mMapDynaValue.containsKey(ZS_SZ)) {
            String zdf = mMapDynaValue.get(ZS_SZ).zdf;
            int colorFlag = FontUtils.getColorByZDF_percent(zdf);
            int color = getZDPColor(colorFlag);
            mTvMinibarSZ.setText("深证 " + zdf);
            mTvMinibarSZ.setTextColor(color);
        }
        if (mTvMinibarCY != null && mMapDynaValue.containsKey(ZS_CY)) {
            String zdf = mMapDynaValue.get(ZS_CY).zdf;
            int colorFlag = FontUtils.getColorByZDF_percent(zdf);
            int color = getZDPColor(colorFlag);
            mTvMinibarCY.setText("创业板 " + zdf);
            mTvMinibarCY.setTextColor(color);
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

    @Override
    protected void initPage() {
        setContentView(R.layout.page_optional);

        mTvMinibarSH = (TextView) findViewById(R.id.tv_marketboard_sh);
        mTvMinibarSZ = (TextView) findViewById(R.id.tv_marketboard_sz);
        mTvMinibarCY = (TextView) findViewById(R.id.tv_marketboard_cy);

        mFrMiniboardExpand = (FrameLayout) findViewById(R.id.fr_mimiboardexpand_content);
        mMiniboardpage = new MiniMarketBoardPage();
        mMiniboardpage.setCallback(new MiniMarketBoardCB() {
            @Override
            public void onCloseBtnClicked() {
                closeMask();
                if (mMaskCallback != null) {
                    mMaskCallback.miniboardSwitch(false);
                }
            }
        });

        View vMiniboard = mMiniboardpage.convertToView(this, getActivity().getLayoutInflater(), null, null);
        mFrMiniboardExpand.addView(vMiniboard);

        mMiniMarketBar = findViewById(R.id.ll_mini_marketboard);

        mMiniMarketBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrMiniboardExpand.setVisibility(View.VISIBLE);
                mMiniboardpage.setIsHide(false);
                mMiniboardpage.requestData();
                mMiniMarketBar.setVisibility(View.INVISIBLE);
                if (mMaskCallback != null) {
                    mMaskCallback.miniboardSwitch(true);
                }
            }
        });

        mTvLoginNotice = (TextView) findViewById(R.id.tv_login_notice);
        mTvLoginNotice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到登录
                LoginPage.gotoLogin(OptionalHome.this, -1);
            }
        });

        mVPositonStatistics = findViewById(R.id.optionalhome_ll_positon_statistics);
        mVPositonStatistics.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 屏蔽事件传到下层
            }
        });
        mTv_total_market_value_content = (TextView) findViewById(R.id.tv_total_marketValue_content);
        mTv_total_profitAndLoss_content = (TextView) findViewById(R.id.tv_total_profitAndLoss_content);
        mTv_total_profitAndLoss_percent_content = (TextView) findViewById(R.id.tv_total_profitAndLoss_percent_content);
        mHandler_optionalHome = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        refreshOptionalData();
                        break;
                    case REFRESH_PROFITLOSS:
                        double[] t_ary = (double[]) msg.obj;
                        refreshTotalProfitLoss(t_ary[0], t_ary[1], t_ary[2]);
                        break;
                    default:
                        break;
                }
            }
        };


        // 排序
        tvSortField0 = (TextView) findViewById(R.id.tv_optional_sortfield0);
        TextView tvSortField1 = (TextView) findViewById(R.id.tv_optional_sortfield1);
        TextView tvSortField2 = (TextView) findViewById(R.id.tv_optional_sortfield2);

        mSortHelper = new SymbolSortHelper();
        mSortHelper.setItemTextColor(RColor(R.color.t3));
        mSortHelper.setItemSelectedTextColor(RColor(R.color.c4));
        mSortHelper.addSortItem(tvSortField0, SymbolSortHelper.SORT_DEFAULT | SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);
        mSortHelper.addSortItem(tvSortField1, SymbolSortHelper.SORT_DEFAULT | SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);
        mSortHelper.addSortItem(tvSortField2, SymbolSortHelper.SORT_DEFAULT | SymbolSortHelper.SORT_RISE | SymbolSortHelper.SORT_FALL);

        initFiled();

        setSortAction();

        mLvOptional = (RefreshListView) findViewById(R.id.lv_optional_home);

        View vLvEmpty = findViewById(R.id.optionalhome_iv_listempty);
        vLvEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchPage.gotoSearch(OptionalHome.this, mOptionalType);
                // gotoSearch(mOptionalType);
            }
        });

        mLvOptional.setEmptyView(vLvEmpty);

        mAdapter = new OptionalAdapter();
        if (mLvOptional != null) {
            mLvOptional.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
            mLvOptional.initWithHeader(R.layout.layout_listview_header);
            mLvOptional.setAdapter(mAdapter);

            mLvOptional.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestData();
                }

                @Override
                public void beforeRefresh() {

                }

                @Override
                public void afterRefresh() {
                    // mLvOptional.updateRefreshDate("最近更新:" +
                    // getDBHelper().getString("refresh_zxg", DateUtils.getCurrentQuoteDate()));
                }
            });

            mLvOptional.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (OptionalHome.this.mPageChangeFlag == 0) {
                        // 因增加了refresh头,index - 1 对应同LstGoods下标
                        QuoteJump.gotoQuote(OptionalHome.this, mLstGoods, position - 1);
                        // gotoQuote(mLstGoods, position - 1);
                    }
                }
            });
        }

    }

    @Override
    protected void initData() {
        mMapDynaValue.put(ZS_SH, new DynaValue());
        mMapDynaValue.put(ZS_SZ, new DynaValue());
        mMapDynaValue.put(ZS_CY, new DynaValue());
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        LogUtil.easylog("OptionalHome->onPageResume");

        refreshMiniMarketBar();

        if (isLogined()) {
            mMiniMarketBar.setVisibility(View.VISIBLE);
            mTvLoginNotice.setVisibility(View.INVISIBLE);
        } else {
            mMiniMarketBar.setVisibility(View.INVISIBLE);
            mTvLoginNotice.setVisibility(View.VISIBLE);
        }

        // if (bIsNeedRefresh) {
        refreshOptionalData();
        // bIsNeedRefresh = false;
        // }

        if (!getIsAutoRefresh()) {
            if (getUserVisibleHint()) {
                startRequestTask();
            } else {
                requestData();
            }
        }

        if (mLstGoods != null && mLastSortField != -999) {
            Collections.sort(mLstGoods, new GoodsComparator(mLastSortField));

            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        // 如果当前自选股类型是所有自选，列表头部提示显示为所有自选，其它情况下，提示显示为股票名称
        if (OptionalInfo.TYPE_DEFAULT.equals(mOptionalType)) {
            tvSortField0.setText("所有自选");
        } else {
            tvSortField0.setText("股票名称");
        }
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
                        if (sortType == SymbolSortHelper.SORT_DEFAULT) {
                            OptionalInfo mOptionalInfo = DataModule.getInstance().getOptionalInfo();
                            mLstGoods.clear();
                            mLstGoods.addAll(mOptionalInfo.getGoodsListByType(mOptionalType));
                            mLastSortField = -999;
                        } else {
                            // 分类为持仓
                            if (mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
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
                                        mLastSortField = OTHER_TYPE_FIELD_POSITION[mCurDisplayIndex];
                                    } else {
                                        mLastSortField = -OTHER_TYPE_FIELD_POSITION[mCurDisplayIndex];
                                    }
                                    Collections.sort(mLstGoods, new GoodsComparator(mLastSortField));
                                }
                            } else {
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

                        }
                    }

                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }

                }
            });
        }
    }

    public String getCurOptionalType() {
        return mOptionalType;
    }

    public int getCurOptionalCount() {
        int count = 0;
        if (mLstGoods != null) {
            count = mLstGoods.size();
        }

        return count;
    }



    private void refreshTotalProfitLoss(double totalMarketValue, double totalCostValue, double totalCost) {
        DecimalFormat df = new DecimalFormat("0.00");

        mTv_total_market_value_content.setText("总市值\n" + df.format(totalMarketValue));
        mTv_total_market_value_content.setTextColor(RColor(R.color.t2));

        double totalProfitLoss = totalCostValue - totalCost;

        String t_sPL = df.format(totalProfitLoss);
        int color = getZDPColor(FontUtils.getColorByZD(t_sPL));
        mTv_total_profitAndLoss_content.setText("总盈亏\n" + t_sPL);
        mTv_total_profitAndLoss_content.setTextColor(color);

        float totalProfitLossPercent = 0.0f;
        if (totalCost > 0) {
            totalProfitLossPercent = (float) (totalProfitLoss / totalCost);
        }

        String t_sPLP = DataUtils.getSignedZDF(totalProfitLossPercent * 10000);
        mTv_total_profitAndLoss_percent_content.setText("盈亏比\n" + t_sPLP);
        mTv_total_profitAndLoss_percent_content.setTextColor(color);
    }

    public void refreshTotalPosition() {
        if (mOptionalType == null || !mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
            return;
        }

        double totalCost = 0;
        double totalCostValue = 0; // 有持股数且有成本价的 总市值
        double totalValue = 0; // 有持股数的 总市值

        float costPrice = 0;
        float currentPrice = 0;
        long amount = 0;

        for (int i = 0; i < mLstGoods.size(); i++) {
            Goods goods = mLstGoods.get(i);

            costPrice = Float.valueOf(goods.getPositionPrice());
            currentPrice = Float.valueOf(goods.getZxj());
            if (currentPrice <= 0) {
                currentPrice = Float.valueOf(goods.getLastClose());
            }

            amount = DataUtils.convertToLong(goods.getPositionAmount());

            // 成本和持仓数小于等于0时不计算
            if (amount <= 0) {
                continue;
            }

            double dValueTemp = currentPrice * amount;
            totalValue += dValueTemp;
            if (costPrice > 0) {
                totalCostValue += dValueTemp;
                totalCost += costPrice * amount;
            }
        }

        double[] t_ary = new double[] {totalValue, totalCostValue, totalCost};
        Message msg = mHandler_optionalHome.obtainMessage(REFRESH_PROFITLOSS, 0, 0, t_ary);
        mHandler_optionalHome.sendMessage(msg);
    }


    private void refreshOptionalData() {
        List<Goods> goodsList = null;
        OptionalInfo oi = DataModule.getInstance().getOptionalInfo();

        if (!isLogined()) {
            mOptionalType = OptionalInfo.TYPE_DEFAULT;
        } else {
            int nRet = oi.hasType(mOptionalType);
            if (nRet < 0) {
                mOptionalType = OptionalInfo.TYPE_DEFAULT;
            }
        }
        if (mVPositonStatistics != null) {
            if (mOptionalType != null && mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
                mVPositonStatistics.setVisibility(View.VISIBLE);
            } else {
                mVPositonStatistics.setVisibility(View.GONE);
            }
        }


        goodsList = DataModule.getInstance().getOptionalInfo().getGoodsListByType(mOptionalType);

        if (goodsList != null) {

            mLstGoods.clear();
            mLstGoods.addAll(goodsList);
            requestData();
        }

        mAdapter.notifyDataSetChanged();
        refreshTotalPosition();
    }

    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
            DynaValueData_Reply gr = goodsTable.getResponse();
            if (gr == null || gr.getRepFieldsList().size() == 0 || gr.getQuotaValueList().size() == 0) {
                return;
            }

            int indexCLOSE = gr.getRepFieldsList().indexOf(GoodsParams.CLOSE);
            int indexZXJ = gr.getRepFieldsList().indexOf(GoodsParams.ZXJ);
            int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);
            int indexZD = gr.getRepFieldsList().indexOf(GoodsParams.ZHANGDIE);
            int indexJL = gr.getRepFieldsList().indexOf(GoodsParams.JL);
            int indexHSL = gr.getRepFieldsList().indexOf(GoodsParams.HSL);

            List<DynaQuota> quota = gr.getQuotaValueList();

            LogUtil.easylog("size" + quota.size());

            for (int i = 0; i < quota.size(); i++) {
                int goodsId = quota.get(i).getGoodsId();

                String lastDayClosePrice = quota.get(i).getRepFieldValueList().get(indexCLOSE);
                String price = quota.get(i).getRepFieldValueList().get(indexZXJ);
                String zdf = quota.get(i).getRepFieldValueList().get(indexZDF);
                String zd = quota.get(i).getRepFieldValueList().get(indexZD);
                String jl = quota.get(i).getRepFieldValueList().get(indexJL);
                String hsl = quota.get(i).getRepFieldValueList().get(indexHSL);

                if (mMapDynaValue.containsKey(goodsId)) {
                    DynaValue value = mMapDynaValue.get(goodsId);
                    value.zdf = DataUtils.getSignedZDF(zdf);
                }


                for (int j = 0; j < mLstGoods.size(); j++) {
                    Goods g = mLstGoods.get(j);
                    if (goodsId == g.getGoodsId()) {
                        g.setLastClose(DataUtils.getPrice(lastDayClosePrice));
                        g.setZxj(DataUtils.getPrice(price));
                        int color = FontUtils.getColorByZDF(zdf);
                        g.setQuoteColor(color);
                        g.setZdf(zdf);
                        g.setZd(DataUtils.getZD(zd));
                        g.setJl(jl);
                        g.setHsl(DataUtils.getHSL(hsl));
                        break;
                    }
                }
            }

            mAdapter.notifyDataSetChanged();

            // getDBHelper().setString("refresh_zxg", DateUtils.getCurrentQuoteDate());
            refreshTotalPosition();
            refreshMiniMarketBar();
        }
    }

    public void notifyTypeChange(String type) {
        LogUtil.easylog("notifyTypeChange->type:" + type);
        if (!type.equals(mOptionalType)) {

            OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
            int nRet = oi.hasType(type);
            if (nRet < 0) {
                mOptionalType = OptionalInfo.TYPE_DEFAULT;
            } else {
                mOptionalType = type;
            }

            initFiled();

            // bIsNeedRefresh = true;
        }

        // 如果当前自选股类型是所有自选，列表头部提示显示为所有自选，其它情况下，提示显示为股票名称
        if (OptionalInfo.TYPE_DEFAULT.equals(type)) {
            tvSortField0.setText("所有自选");
        } else {
            tvSortField0.setText("股票名称");
        }
    }

    @Override
    protected View getPageBarMenuProgress() {
        return null;
    }

    @Override
    public void showProgress() {}

    @Override
    public void dismissProgress() {
        super.dismissProgress();
        if (mLvOptional != null) {
            mLvOptional.onRefreshFinished();
        }
    }

    @Override
    public void requestData() {
        if (mMiniboardpage != null) {
            mMiniboardpage.requestData();
        }
        getDataFromNet(-9999, true);
    }

    /**
     * 
     * @param sortField 排序字段
     * @param sortOrder true降序 false升序
     */
    private void getDataFromNet(int sortField, Boolean sortOrder) {
        if (mLstGoods.size() == 0) {
            mLvOptional.onRefreshFinished();
            return;
        }
        ArrayList<Integer> lstGoodsId = new ArrayList<Integer>();
        for (Goods goods : mLstGoods) {
            lstGoodsId.add(goods.getGoodsId());
        }

        if (!lstGoodsId.contains(ZS_SH)) {
            lstGoodsId.add(ZS_SH);
        }
        if (!lstGoodsId.contains(ZS_SZ)) {
            lstGoodsId.add(ZS_SZ);
        }

        if (!lstGoodsId.contains(ZS_CY)) {
            lstGoodsId.add(ZS_CY);
        }

        ArrayList<Integer> reqFileds = new ArrayList<Integer>();
        reqFileds.add(GoodsParams.CLOSE);// 昨收
        reqFileds.add(GoodsParams.ZXJ);// 最新价
        reqFileds.add(GoodsParams.ZDF);// 涨跌幅
        reqFileds.add(GoodsParams.ZHANGDIE);// 涨跌
        reqFileds.add(GoodsParams.JL);// 净流
        reqFileds.add(GoodsParams.HSL);// 换手率

        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
        pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, lstGoodsId, reqFileds, sortField, sortOrder, 0, 0, 0, 0));
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    private class OptionalAdapter extends BaseAdapter {

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
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_optionalhome_listitem, null);
                TextView tvName = (TextView) convertView.findViewById(R.id.tv_item_0_0);
                TextView tvCode = (TextView) convertView.findViewById(R.id.tv_item_0_1);
                TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_item1);
                TextView tvOther = (TextView) convertView.findViewById(R.id.tv_item_2);
                View vClickArea = convertView.findViewById(R.id.fr_item_click_area);

                convertView.setTag(new ListCell(tvName, tvCode, tvPrice, tvOther, vClickArea));
            }
            ListCell lc = (ListCell) convertView.getTag();

            lc.vClickArea.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
                        mCurDisplayIndex = mCurDisplayIndex >= 4 ? 0 : mCurDisplayIndex + 1;
                        mLstSortFieldName.set(2, OTHER_TYPE_NAME_POSITION[mCurDisplayIndex]);
                    } else {
                        mCurDisplayIndex = mCurDisplayIndex >= 3 ? 0 : mCurDisplayIndex + 1;
                        mLstSortFieldName.set(2, OTHER_TYPE_NAME_NORMAL[mCurDisplayIndex]);
                    }

                    if (mLastSortItem != null && mLastSortItem.getId() == R.id.tv_optional_sortfield2) {
                        mSortHelper.updateItemLable(mLstSortFieldName);

                        if (mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
                            if (OTHER_TYPE_FIELD_POSITION[mCurDisplayIndex] == Math.abs(mLastSortField)) {
                                mSortHelper.setDefaultSort(mLastSortItem, mLastSortType);
                            }
                        } else {
                            if (OTHER_TYPE_FIELD_NORMAL[mCurDisplayIndex] == Math.abs(mLastSortField)) {
                                mSortHelper.setDefaultSort(mLastSortItem, mLastSortType);
                            }
                        }

                    } else {
                        mSortHelper.updateItemLable_exceptSort(mLstSortFieldName);
                    }
                    mSortHelper.notifySort();
                }
            });

            Goods goods = (Goods) getItem(position);

            lc.tvName.setText(goods.getGoodsName());
            lc.tvCode.setText(goods.getGoodsCode());
            lc.tvPrice.setText(goods.getZxj());

            // 分类为持仓
            if (mOptionalType.equals(OptionalInfo.TYPE_POSITION)) {
                float costPrice = DataUtils.convertToFloat(goods.getPositionPrice());
                float currentPrice = DataUtils.convertToFloat(goods.getZxj());
                long amount = DataUtils.convertToLong(goods.getPositionAmount());

                float d_price = 0.0f; // 每股价差
                if (costPrice > 0) {
                    float nowPrice = currentPrice;
                    if (nowPrice <= 0) {
                        nowPrice = DataUtils.convertToFloat(goods.getLastClose());
                    }
                    d_price = nowPrice - costPrice;
                }


                switch (mCurDisplayIndex) {
                    case 0: {// 涨跌幅
                        String sZDF = goods.getZdf();
                        lc.tvOther.setText(DataUtils.getSignedZDF(sZDF));

                        int colorFlag = FontUtils.getColorByZDF(sZDF);
                        int resZDP = getZDPRadiusBg(colorFlag);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;
                    case 1: {// 成本价
                        String sCost = DataUtils.mDecimalFormat2.format(costPrice);
                        lc.tvOther.setText(sCost);
                        int resZDP = getZDPRadiusBg(1);
                        lc.tvOther.setBackgroundResource(resZDP);

                    }
                        break;
                    case 2: {// 浮动盈亏
                        // 浮动盈亏
                        float profitOrLoss = d_price * amount;
                        int resZDP = getZDPRadiusBg(FontUtils.getColorByZD(d_price));
                        String t_yk = DataUtils.mDecimalFormat1_max.format(profitOrLoss);
                        goods.setPositionProfitLoss(t_yk);
                        lc.tvOther.setText(t_yk);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;
                    case 3: {// 盈亏比
                        float profitOrLossPercent = 0.0f;
                        if (costPrice > 0 && amount > 0) {
                            profitOrLossPercent = d_price / costPrice * 10000;
                        }
                        String t_ykb = DataUtils.getSignedZDF(profitOrLossPercent);
                        goods.setPositionProfitLossPercent(t_ykb);

                        int resZDP = getZDPRadiusBg(FontUtils.getColorByZD(d_price));
                        lc.tvOther.setText(t_ykb);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;

                    case 4: {// 持股数
                        int resZDP = getZDPRadiusBg(1);
                        lc.tvOther.setText(String.valueOf(amount));
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;

                    default:
                        break;
                }
            } else {
                switch (mCurDisplayIndex) {
                    case 0: {// 涨跌幅
                        String sZDF = goods.getZdf();
                        lc.tvOther.setText(DataUtils.getSignedZDF(sZDF));

                        int colorFlag = FontUtils.getColorByZDF(sZDF);
                        int resZDP = getZDPRadiusBg(colorFlag);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;
                    case 1: {// 涨跌
                        String sZD = goods.getZd();
                        lc.tvOther.setText(DataUtils.formatZD(sZD, DataUtils.mDecimalFormat2));

                        int colorFlag = FontUtils.getColorByZD(sZD);
                        int resZDP = getZDPRadiusBg(colorFlag);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;
                    case 2: {// 主力净流
                        lc.tvOther.setText(DataUtils.formatJL(goods.getJl(), DataUtils.mDecimalFormat1_max));

                        int colorFlag = FontUtils.getColorByZD(goods.getJl());
                        int resZDP = getZDPRadiusBg(colorFlag);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;
                    case 3: {// 换手率
                        lc.tvOther.setText(goods.getHsl());

                        int resZDP = getZDPRadiusBg(1);
                        lc.tvOther.setBackgroundResource(resZDP);
                    }
                        break;

                    default:
                        break;
                }
            }

            return convertView;
        }

        private class ListCell {

            public ListCell(TextView tvName, TextView tvCode, TextView tvPrice, TextView tvOther, View vClickArea) {
                this.tvPrice = tvPrice;
                this.tvOther = tvOther;
                this.tvName = tvName;
                this.tvCode = tvCode;
                this.vClickArea = vClickArea;
            }

            public TextView tvName;
            public TextView tvCode;
            public TextView tvPrice;
            public TextView tvOther;
            public View vClickArea;

        }

    }

    @Override
    public ArrayList<String> getRegisterBcdc() {
        ArrayList<String> lstBcd = new ArrayList<String>();
        lstBcd.add(BroadCastName.BCDC_CHANGE_LOGIN_STATE);
        lstBcd.add(BroadCastName.BCDC_OPTIONAL_DATA_UPDATE);

        return lstBcd;
    }

    @Override
    public void onReceivedBroadcast(String action) {
        if (action == BroadCastName.BCDC_CHANGE_LOGIN_STATE) {
            if (isLogined()) {
                mMiniMarketBar.setVisibility(View.VISIBLE);
                mTvLoginNotice.setVisibility(View.GONE);
            } else {
                mMiniMarketBar.setVisibility(View.GONE);
                mTvLoginNotice.setVisibility(View.VISIBLE);
            }
        } else if (action == BroadCastName.BCDC_OPTIONAL_DATA_UPDATE) {
            refreshOptionalData();
        }
    }

    public interface MiniMarketBoardDispalyCB {
        public void miniboardSwitch(boolean open);
    }
}
