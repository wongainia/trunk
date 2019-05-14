package cn.emoney.acg.page.market;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.DataUtil;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.data.protocol.quote.MarketFocusPackage;
import cn.emoney.acg.data.protocol.quote.MarketFocusReply.MarketFocus_Reply;
import cn.emoney.acg.data.protocol.quote.MarketFocusRequest.MarketFocus_Request;
import cn.emoney.acg.data.protocol.quote.MarketTrendPackage;
import cn.emoney.acg.data.protocol.quote.MarketTrendReply.MarketTrend_Reply;
import cn.emoney.acg.data.protocol.quote.MarketTrendRequest.MarketTrend_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.widget.PinnedSectionListView;
import cn.emoney.acg.widget.PinnedSectionListView.OnRefreshListener;
import cn.emoney.acg.widget.PinnedSectionListView.PinnedSectionListAdapter;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * 市场行情
 * */
public class QuotationPage extends PageImpl implements OnClickListener {

    private List<QuotationListItem> listDatas = new ArrayList<QuotationListItem>();
    private QuotationListAdapter listAdapter;
    private PinnedSectionListView listView;

    // ListView Header
    private int[] INDEX_IDS = { 1, 1399001, 1399005, 1399006 };
    private String[] INDEX_NAMES = { "上证\n指数", "深证\n成指", "中小\n板指", "创业\n板指" };
    private int[] INDEX_VIEW_IDS = { R.id.page_quotation_header_shindex,
            R.id.page_quotation_header_szindex,
            R.id.page_quotation_header_zxbindex,
            R.id.page_quotation_header_cybindex };
    private SparseArray<ListHeaderViewHolder> listHeaderViewHolders = new SparseArray<ListHeaderViewHolder>();

    private final int[] BK_ITEM_IDS = { R.id.page_quotation_list_item_bk_item1,
            R.id.page_quotation_list_item_bk_item2,
            R.id.page_quotation_list_item_bk_item3,
            R.id.page_quotation_list_item_bk_item4,
            R.id.page_quotation_list_item_bk_item5,
            R.id.page_quotation_list_item_bk_item6 };

    // 存储各版块股票的集合，用于传给QuoteHome，QuoteHome切换显示股票集合中的股票
    private ArrayList<Goods> listRiseBks = new ArrayList<Goods>(); // 领涨版块
    private ArrayList<Goods> listFallBks = new ArrayList<Goods>(); // 领跌版块
    private ArrayList<Goods> listRiseStocks = new ArrayList<Goods>(); // 领涨个股
    private ArrayList<Goods> listFallStocks = new ArrayList<Goods>(); // 领跌个股
    private ArrayList<Goods> listHighHsls = new ArrayList<Goods>(); // 高换手率
    private ArrayList<Goods> listLeadingZllrs = new ArrayList<Goods>(); // 主力流入

    // 看涨看跌模块
    private TextView tvViewRiseNum, tvViewFallNum;
    private View viewRise, viewEqual, viewFall;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quotation);

        listView = (PinnedSectionListView) findViewById(R.id.page_quotation_list);
        listView.setShadowVisible(false);

        // get ListView's header
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View listHeader = inflater.inflate(R.layout.page_quotation_list_header,
                listView, false);
        for (int i = 0; i < INDEX_VIEW_IDS.length; i++) {
            View layout = listHeader.findViewById(INDEX_VIEW_IDS[i]);
            ListHeaderViewHolder viewHolder = new ListHeaderViewHolder(layout);
            viewHolder.tvStockName.setText(INDEX_NAMES[i]);
            listHeaderViewHolders.put(INDEX_IDS[i], viewHolder);
        }

        // add header to ListView
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.addHeaderView(listHeader, null, false);
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (listView != null) {
                            listView.onRefreshFinished();
                        }
                    }
                }, DataModule.REQUEST_MAX_LIMIT_TIME);
            }
            @Override
            public void beforeRefresh() { }
            @Override
            public void afterRefresh() { }
        });

        // 获取看涨看跌控件
        tvViewRiseNum = (TextView) listHeader.findViewById(R.id.page_quotation_tv_viewpoint_rise_num);
        tvViewFallNum = (TextView) listHeader.findViewById(R.id.page_quotation_tv_viewpoint_fall_num);
        viewRise = listHeader.findViewById(R.id.page_quotation_viewpoint_bg_rise);
        viewFall = listHeader.findViewById(R.id.page_quotation_viewpoint_bg_fall);
        viewEqual = listHeader.findViewById(R.id.page_quotation_viewpoint_bg_equal);

        listAdapter = new QuotationListAdapter(getContext(), listDatas);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (!getIsAutoRefresh()) {
            if (getUserVisibleHint()) {
                startRequestTask();
            } else {
                requestData();
            }
        }
    }

    @Override
    public void requestData() {
        super.requestData();

        requestIndex();
        requestMarketFocus();
        requestMarketTrend();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.page_quotation_header_shindex:
                showTip("上证指数");
                break;
            case R.id.page_quotation_header_szindex:
                showTip("深证指数");
                break;
            case R.id.page_quotation_header_zxbindex:
                showTip("中小指数");
                break;
            case R.id.page_quotation_header_cybindex:
                showTip("创业指数");
                break;
            default:
                break;
        }
    }

    /**
     * 更新板块指数数据
     * */
    private void requestIndex() {
        ArrayList<Integer> goodsId = new ArrayList<Integer>();
        for (int i = 0; i < INDEX_IDS.length; i++) {
            goodsId.add(INDEX_IDS[i]);
        }

        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.ZXJ);
        goodsFiled.add(GoodsParams.ZHANGDIE);
        goodsFiled.add(GoodsParams.ZDF);
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(
                (short) 0));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4)
                .setGroupType(0).addAllGoodsId(goodsId)
                .addAllReqFields(goodsFiled).setSortField(-9999)
                .setSortOrder(false).setReqBegin(0).setReqSize(4)
                .setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    /**
     * 获取大市数据
     * */
    private void requestMarketFocus() {
        MarketFocusPackage pkg = new MarketFocusPackage(
                new QuoteHead((short) 0));
        pkg.setRequest(MarketFocus_Request.newBuilder()
                .setLastUpdateMarketDate(0).setLastUpdateMarketTime(0).build());
        requestQuote(pkg, IDUtils.MarketFocus);
    }

    /**
     * 获取看涨看跌
     * */
    private void requestMarketTrend() {
        MarketTrendPackage pkg = new MarketTrendPackage(new QuoteHead((short) 0));
        pkg.setRequest(MarketTrend_Request.newBuilder().build());

        requestQuote(pkg, IDUtils.ID_MARKET_TREND);
    }

    /**
     * 收到后台返回的板块指数数据
     * */
    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);
        listView.onRefreshFinished();

        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
            cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply gr = goodsTable
                    .getResponse();

            if (gr == null || gr.getRepFieldsList().size() == 0
                    || gr.getQuotaValueList().size() == 0) {
                return;
            }

            int indexCJJ = gr.getRepFieldsList().indexOf(GoodsParams.ZXJ);
            int indexZD = gr.getRepFieldsList().indexOf(GoodsParams.ZHANGDIE);
            int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);

            List<cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota> lstQuotas = gr
                    .getQuotaValueList();
            final ArrayList<Goods> goodsLst = new ArrayList<Goods>();
            for (int i = 0; i < lstQuotas.size(); i++) {
                cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota quota = lstQuotas
                        .get(i);
                int goodsId = quota.getGoodsId();
                goodsLst.add(new Goods(goodsId, getGoodsNameById(goodsId)));
                for (int j = 0; j < INDEX_IDS.length; j++) {
                    if (goodsId == INDEX_IDS[j]) {
                        ListHeaderViewHolder viewHolder = listHeaderViewHolders
                                .get(goodsId);
                        String price = quota.getRepFieldValueList().get(
                                indexCJJ);
                        String[] intAndDeci = Util
                                .FormatPriceZS(price, goodsId);
                        String zd = Util.FormatPrice(quota
                                .getRepFieldValueList().get(indexZD), goodsId);
                        String zdf = quota.getRepFieldValueList().get(indexZDF);

                        if (intAndDeci != null && intAndDeci.length == 2) {
                            viewHolder.tvPriceInteger.setText(intAndDeci[0]);
                            viewHolder.tvPriceDecimal.setText("."
                                    + intAndDeci[1]);
                        } else if (intAndDeci != null && intAndDeci.length == 1) {
                            viewHolder.tvPriceInteger.setText(intAndDeci[0]);
                            viewHolder.tvPriceDecimal.setText(".00");
                        }

                        viewHolder.tvZd.setText(zd);
                        viewHolder.tvZdf.setText(DataUtils.getZDF(zdf));

                        float flag = 0;
                        flag = FontUtils.getColorByZDF(zdf);
                        // 箭头
                        if (flag == 0) {
                            viewHolder.imgZdArrow.setVisibility(View.GONE);
                        } else {
                            viewHolder.imgZdArrow.setVisibility(View.VISIBLE);
                            if (flag > 0) {
                                viewHolder.imgZdArrow
                                .setBackgroundResource(R.drawable.img_arrow_up_quotation);
                            } else if (flag < 0) {
                                viewHolder.imgZdArrow
                                .setBackgroundResource(R.drawable.img_arrow_down_quotation);
                            }
                        }

                        final int index = i;
                        viewHolder.layout.setBackgroundColor(getZDPColor(flag));
                        viewHolder.layout
                        .setOnClickListener(new OnClickEffectiveListener() {
                            @Override
                            public void onClickEffective(View v) {
                                QuoteJump.gotoQuote(QuotationPage.this, goodsLst, index);
                                //										gotoQuote(goodsLst, index);
                            }
                        });

                        break;
                    }
                }
            }
        } else if (pkg instanceof MarketFocusPackage) {
            MarketFocusPackage marketFocusPackage = (MarketFocusPackage) pkg;
            MarketFocus_Reply reply = marketFocusPackage.getResponse();

            if (reply == null || reply.getQuotaStrongGroupList().size() == 0
                    || reply.getQuotaStrongGoodsList().size() == 0
                    || reply.getQuotaWeakGroupList().size() == 0
                    || reply.getQuotaWeakGoodsList().size() == 0
                    || reply.getQuotaHslGoodsList().size() == 0
                    || reply.getQuotaZjlrGoodsList().size() == 0) {
                return;
            }

            // 正式解析前，先清空缓存中数据
            listDatas.clear();

            /* 解析领涨版块 */
            // 添加Section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "领涨版块",
                    RankBkPage.RANK_TYPE_RISE));

            // 获取领涨版块中各项数据
            List<DynaQuota> strongGroupList = reply.getQuotaStrongGroupList();
            listRiseBks.clear();
            if (strongGroupList != null && strongGroupList.size() > 0) {
                List<BkItem> listBkRiseItems = new ArrayList<BkItem>();

                for (int i = 0; i < strongGroupList.size(); i++) {
                    DynaQuota quota = strongGroupList.get(i);

                    if (quota == null)
                        return;

                    // get good id
                    int goodsId = quota.getGoodsId();
                    listRiseBks.add(new Goods(goodsId,
                            getGoodsNameById(goodsId)));

                    // get good name by id
                    String goodsName = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(goodsId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        goodsName = listGoods.get(0).getGoodsName();
                    } else {
                        Goods goods = new Goods(goodsId, "");
                        goodsName = goods.getGoodsName();
                    }

                    // get zdf
                    String zdf = quota.getRepFieldValue(0);

                    listBkRiseItems.add(new BkItem(goodsId, goodsName, zdf,
                            BkItem.BK_ITEM_TYPE_LZ));
                }

                // 添加领涨版块的六宫格数据项
                listDatas.add(new QuotationListBkItem(
                        QuotationListAdapter.TYPE_BK, listBkRiseItems));
            }

            /* 解析领涨版块 */
            // 添加Section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "领跌版块",
                    RankBkPage.RANK_TYPE_FALL));

            // 获取领跌版块中各项数据
            List<DynaQuota> weakGroupList = reply.getQuotaWeakGroupList();
            listFallBks.clear();
            if (weakGroupList != null && weakGroupList.size() > 0) {
                List<BkItem> listBkDeclineItems = new ArrayList<BkItem>();

                for (int i = 0; i < weakGroupList.size(); i++) {
                    DynaQuota quota = weakGroupList.get(i);

                    if (quota == null)
                        return;

                    // get good id
                    int goodsId = quota.getGoodsId();
                    listFallBks.add(new Goods(goodsId, getGoodsNameById(goodsId)));

                    // get good name by id
                    String goodsName = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(goodsId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        goodsName = listGoods.get(0).getGoodsName();
                    } else {
                        Goods goods = new Goods(goodsId, "");
                        goodsName = goods.getGoodsName();
                    }

                    // get zdf
                    String zdf = quota.getRepFieldValue(0);

                    listBkDeclineItems.add(new BkItem(goodsId, goodsName, zdf,
                            BkItem.BK_ITEM_TYPE_LD));
                }

                // 添加领跌版块的六宫格数据项
                listDatas.add(new QuotationListBkItem(
                        QuotationListAdapter.TYPE_BK, listBkDeclineItems));
            }

            closeSQLDBHelper();

            /* 解析领涨个股版块 */
            // 添加Section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "领涨个股",
                    RankStockPage.RANK_TYPE_ZF));

            List<DynaQuota> listRiseStock = reply.getQuotaStrongGoodsList();
            listRiseStocks.clear();
            if (listRiseStock != null && listRiseStock.size() > 0) {
                // 添加个股详情items
                for (int i = 0; i < listRiseStock.size(); i++) {
                    DynaQuota quota = listRiseStock.get(i);

                    if (quota == null)
                        return;

                    // get stock id
                    int stockId = quota.getGoodsId();
                    listRiseStocks.add(new Goods(stockId, getGoodsNameById(stockId)));

                    // get stock name and code by id
                    String stockNmae = "", stockCode = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(stockId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        stockNmae = listGoods.get(0).getGoodsName();
                        stockCode = listGoods.get(0).getGoodsCode();
                    } else {
                        Goods goods = new Goods(stockId, "");
                        stockNmae = goods.getGoodsName();
                        stockCode = goods.getGoodsCode();
                    }

                    // get zdf and price
                    String zdf = quota.getRepFieldValue(0);
                    String price = quota.getRepFieldValue(1);

                    if (i == 9) {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, zdf, stockId,
                                QuotationListStockItem.TYPE_ZF, true, i));
                    } else {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, zdf, stockId,
                                QuotationListStockItem.TYPE_ZF, false, i));
                    }
                }
            }

            /* 解析领跌个股版块 */
            // 添加Section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "领跌个股",
                    RankStockPage.RANK_TYPE_DF));

            List<DynaQuota> listDeclineStock = reply.getQuotaWeakGoodsList();
            listFallStocks.clear();
            if (listDeclineStock != null && listDeclineStock.size() > 0) {
                // 添加个股详情items
                for (int i = 0; i < listDeclineStock.size(); i++) {
                    DynaQuota quota = listDeclineStock.get(i);

                    if (quota == null)
                        return;

                    // get stock id
                    int stockId = quota.getGoodsId();
                    listFallStocks.add(new Goods(stockId, getGoodsNameById(stockId)));

                    // get stock name
                    String stockNmae = "", stockCode = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(stockId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        stockNmae = listGoods.get(0).getGoodsName();
                        stockCode = listGoods.get(0).getGoodsCode();
                    } else {
                        Goods goods = new Goods(stockId, "");
                        stockNmae = goods.getGoodsName();
                        stockCode = goods.getGoodsCode();
                    }

                    // get stock zdf and price
                    String zdf = quota.getRepFieldValue(0);
                    String price = quota.getRepFieldValue(1);

                    if (i == 9) {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, zdf, stockId,
                                QuotationListStockItem.TYPE_DF, true, i));
                    } else {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, zdf, stockId,
                                QuotationListStockItem.TYPE_DF, false, i));
                    }
                }
            }

            /* 解析换手率个股排行版块 */
            // 添加Section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "高换手率个股",
                    RankStockPage.RANK_TYPE_HSL));

            List<DynaQuota> listHslStock = reply.getQuotaHslGoodsList();
            listHighHsls.clear();
            if (listHslStock != null && listHslStock.size() > 0) {
                // 添加个股详情items
                for (int i = 0; i < listHslStock.size(); i++) {
                    DynaQuota quota = listHslStock.get(i);

                    if (quota == null)
                        return;

                    // get stock id
                    int stockId = quota.getGoodsId();
                    listHighHsls.add(new Goods(stockId, getGoodsNameById(stockId)));

                    // get stock name and code by id
                    String stockNmae = "", stockCode = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(stockId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        stockNmae = listGoods.get(0).getGoodsName();
                        stockCode = listGoods.get(0).getGoodsCode();
                    } else {
                        Goods goods = new Goods(stockId, "");
                        stockNmae = goods.getGoodsName();
                        stockCode = goods.getGoodsCode();
                    }

                    // 获取换手率
                    String hsl = quota.getRepFieldValue(0);
                    String price = quota.getRepFieldValue(1);

                    if (i == 9) {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, hsl, stockId,
                                QuotationListStockItem.TYPE_HSL, true, i));
                    } else {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, hsl, stockId,
                                QuotationListStockItem.TYPE_HSL, false, i));
                    }
                }
            }

            /* 解析主力注入个股 */
            // 添加section
            listDatas.add(new QuotationListSectionItem(
                    QuotationListAdapter.TYPE_SECTION, "主力流入个股",
                    RankStockPage.RANK_TYPE_ZLLR));

            // 获取个股列表，并添加到items中
            List<DynaQuota> listZljlStock = reply.getQuotaZjlrGoodsList();
            listLeadingZllrs.clear();
            if (listZljlStock != null && listZljlStock.size() > 0) {
                for (int i = 0; i < listZljlStock.size(); i++) {
                    DynaQuota quota = listZljlStock.get(i);

                    if (quota == null)
                        return;

                    // get stock id
                    int stockId = quota.getGoodsId();
                    listLeadingZllrs.add(new Goods(stockId, getGoodsNameById(stockId)));

                    // get stock name and code by id
                    String stockNmae = "", stockCode = "";
                    ArrayList<Goods> listGoods = getSQLiteDBHelper()
                            .queryStockInfosByCode2(
                                    Util.FormatStockCode(stockId), 1);
                    if (listGoods != null && listGoods.size() > 0) {
                        stockNmae = listGoods.get(0).getGoodsName();
                        stockCode = listGoods.get(0).getGoodsCode();
                    } else {
                        Goods goods = new Goods(stockId, "");
                        stockNmae = goods.getGoodsName();
                        stockCode = goods.getGoodsCode();
                    }

                    // 获取主力流入金额
                    String incomeAmount = quota.getRepFieldValue(0);
                    String price = quota.getRepFieldValue(1);

                    if (i == 9) {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, incomeAmount, stockId,
                                QuotationListStockItem.TYPE_ZLJLR, true, i));
                    } else {
                        listDatas.add(new QuotationListStockItem(
                                QuotationListAdapter.TYPE_STOCK, stockNmae,
                                stockCode, price, incomeAmount, stockId,
                                QuotationListStockItem.TYPE_ZLJLR, false, i));
                    }
                }
            }

            // 刷新ListView显示
            listAdapter.notifyDataSetChanged();
        } else if (pkg instanceof MarketTrendPackage) {
            MarketTrendPackage marketTrendPackage = (MarketTrendPackage) pkg;
            MarketTrend_Reply reply = marketTrendPackage.getResponse();

            if (reply == null) {
                return;
            }

            int rise = reply.getRise();
            int fall = reply.getFall();
            int flat = reply.getFlat();

            // 刷新涨跌比显示
            updateMarketTrend(rise, fall, flat);
        }
    }

    /**
     * 启动排名界面
     * */
    private void gotoRankPage(int rankType) {
        if (rankType == RankBkPage.RANK_TYPE_RISE
                || rankType == RankBkPage.RANK_TYPE_FALL) {
            PageIntent intent = new PageIntent(this, RankBkHome.class);
            Bundle bundle = new Bundle();
            bundle.putInt(RankBkPage.KEY_RANK_TYPE, rankType);
            intent.setArguments(bundle);
            startPage(DataModule.G_CURRENT_FRAME, intent);
        } else {
            PageIntent intent = new PageIntent(this, RankStockHome.class);
            Bundle bundle = new Bundle();
            bundle.putInt(RankStockPage.KEY_RANK_TYPE, rankType);
            intent.setArguments(bundle);
            startPage(DataModule.G_CURRENT_FRAME, intent);
        }
    }

    /**
     * 更新看涨看跌
     * */
    private void updateMarketTrend(int rise, int fall, int flat) {

        if (rise < 0 || fall < 0 || flat < 0) return;
        if (tvViewRiseNum == null || tvViewFallNum == null 
                || viewRise == null || viewEqual == null || viewFall == null) {
            return;
        }

        float total = rise + fall + flat;

        double risePercent = rise / total * 100;
        double fallPercent = fall / total * 100;

        tvViewRiseNum.setText(DataUtils.formatPrice(risePercent));
        tvViewFallNum.setText(DataUtils.formatPrice(fallPercent));

        viewRise.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, rise));
        viewEqual.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, flat));
        viewFall.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, fall));
    }

    private class QuotationListAdapter extends BaseAdapter implements
    PinnedSectionListAdapter {

        public static final int TYPE_SECTION = 0;
        public static final int TYPE_BK = 1;
        public static final int TYPE_STOCK = 2;
        private final int TYPE_COUNT = 3;

        private List<QuotationListItem> listDatas;
        private LayoutInflater inflater;

        public QuotationListAdapter(Context context,
                List<QuotationListItem> listDatas) {
            this.listDatas = listDatas;
            inflater = LayoutInflater.from(context);
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
        public int getItemViewType(int position) {
            return listDatas.get(position).viewType;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup root) {

            int viewType = getItemViewType(position);

            if (viewType == TYPE_SECTION) {
                ViewHolderSection vh = null;

                if (convertView == null) {
                    convertView = inflater.inflate(
                            R.layout.page_quotation_list_item_section, root,
                            false);

                    vh = new ViewHolderSection(convertView);

                    convertView.setTag(vh);
                } else {
                    vh = (ViewHolderSection) convertView.getTag();
                }

                // update View
                final QuotationListSectionItem item = (QuotationListSectionItem) listDatas
                        .get(position);
                vh.tvTitle.setText(item.sectionTitle);

                // set events
                vh.layoutMore
                .setOnClickListener(new OnClickEffectiveListener() {
                    @Override
                    public void onClickEffective(View v) {
                        gotoRankPage(item.rankType);
                    }
                });

            } else if (viewType == TYPE_BK) {
                ViewHolderBk vh = null;

                if (convertView == null) {
                    convertView = inflater.inflate(
                            R.layout.page_quotation_list_item_bk, root, false);

                    vh = new ViewHolderBk(convertView);

                    convertView.setTag(vh);
                } else {
                    vh = (ViewHolderBk) convertView.getTag();
                }

                // update view
                final QuotationListBkItem item = (QuotationListBkItem) listDatas
                        .get(position);
                if (!item.isDefault) {
                    List<BkItem> listBkItems = item.listBkItems;
                    for (int i = 0; i < listBkItems.size(); i++) {
                        final BkItem bkItem = listBkItems.get(i);
                        final String bkTitle = bkItem.bkTitle;
                        final int index = i;

                        vh.listTvTitles.get(i).setText(bkTitle);
                        vh.listTvZdfs.get(i).setText(
                                DataUtils.getSignedZDF(bkItem.bkContent));
                        vh.listTvZdfs.get(i).setTextColor(
                                getZDPColor(FontUtils
                                        .getColorByZDF(bkItem.bkContent)));

                        // set events
                        vh.listLayout.get(i).setOnClickListener(
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (bkItem.bkItemType == BkItem.BK_ITEM_TYPE_LZ) {
                                            QuoteJump.gotoQuote(QuotationPage.this, listRiseBks, index);
                                        } else if (bkItem.bkItemType == BkItem.BK_ITEM_TYPE_LD) {
                                            QuoteJump.gotoQuote(QuotationPage.this, listFallBks, index);
                                        }
                                    }
                                });
                    }
                }

            } else if (viewType == TYPE_STOCK) {
                ViewHolderStock vh = null;

                if (convertView == null) {
                    convertView = inflater.inflate(
                            R.layout.page_quotation_list_item_stock, root,
                            false);

                    vh = new ViewHolderStock(convertView);

                    convertView.setTag(vh);
                } else {
                    vh = (ViewHolderStock) convertView.getTag();
                }

                // update view
                final QuotationListStockItem item = (QuotationListStockItem) listDatas
                        .get(position);
                if (!item.isDefault) {
                    vh.tvStockName.setText(item.stockNmae);
                    vh.tvStockCode.setText(item.stockCode);
                    vh.tvStockprice.setText(DataUtils.getPrice(item.price));

                    if (item.stockItemType == QuotationListStockItem.TYPE_ZF
                            || item.stockItemType == QuotationListStockItem.TYPE_DF) {
                        vh.tvStockValue.setText(DataUtils.getSignedZDF(item.value));
                        vh.tvStockValue.setTextColor(getResources().getColor(R.color.t8));
                        vh.tvStockValue.setBackgroundColor(getZDPColor(FontUtils.getColorByZDF(item.value)));
                    } else if (item.stockItemType == QuotationListStockItem.TYPE_HSL) {
                        vh.tvStockValue.setText(DataUtils.formatHsl(item.value));
                        vh.tvStockValue.setTextColor(getResources().getColor(R.color.t1));
                        vh.tvStockValue.setBackgroundColor(getResources().getColor(R.color.bg_transparent));
                    } else if (item.stockItemType == QuotationListStockItem.TYPE_ZLJLR) {
                        vh.tvStockValue.setText(DataUtils.formatVolume(item.value)); // 后续需要转换显示格式
                        vh.tvStockValue.setTextColor(getZDPColor(FontUtils.getColorByZDF(item.value)));
                        vh.tvStockValue.setBackgroundColor(getResources().getColor(R.color.bg_transparent));
                    }

                    // set click event
                    vh.view.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (item.stockItemType == QuotationListStockItem.TYPE_ZF) {
                                QuoteJump.gotoQuote(QuotationPage.this, listRiseStocks, item.index);
                            } else if (item.stockItemType == QuotationListStockItem.TYPE_DF) {
                                QuoteJump.gotoQuote(QuotationPage.this, listFallStocks, item.index);
                            } else if (item.stockItemType == QuotationListStockItem.TYPE_HSL) {
                                QuoteJump.gotoQuote(QuotationPage.this, listHighHsls, item.index);
                            } else if (item.stockItemType == QuotationListStockItem.TYPE_ZLJLR) {
                                QuoteJump.gotoQuote(QuotationPage.this, listLeadingZllrs, item.index);
                            }
                        }
                    });
                }

                if (item.isHideBottomLine) {
                    vh.bottomDividerLine.setVisibility(View.GONE);
                } else {
                    vh.bottomDividerLine.setVisibility(View.VISIBLE);
                }

            }

            return convertView;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == TYPE_SECTION;
        }

        private class ViewHolderSection {
            public TextView tvTitle;
            public LinearLayout layoutMore;

            public ViewHolderSection(View layout) {
                tvTitle = (TextView) layout
                        .findViewById(R.id.page_quotation_list_item_section_title);
                layoutMore = (LinearLayout) layout
                        .findViewById(R.id.page_quotation_list_item_section_layout_more);
            }
        }

        private class ViewHolderBk {
            public List<View> listLayout = new ArrayList<View>();
            public List<TextView> listTvTitles = new ArrayList<TextView>();
            public List<TextView> listTvZdfs = new ArrayList<TextView>();

            public ViewHolderBk(View view) {
                for (int i = 0; i < BK_ITEM_IDS.length; i++) {
                    View layout = view.findViewById(BK_ITEM_IDS[i]);
                    TextView tvTitle = (TextView) layout
                            .findViewById(R.id.page_quotation_list_item_bk_item_tv_title);
                    TextView tvSummary = (TextView) layout
                            .findViewById(R.id.page_quotation_list_item_bk_item_tv_summary);

                    listLayout.add(layout);
                    listTvTitles.add(tvTitle);
                    listTvZdfs.add(tvSummary);
                }
            }
        }

        private class ViewHolderStock {
            public View view;
            public TextView tvStockName, tvStockCode, tvStockprice,
            tvStockValue;
            public View bottomDividerLine;

            public ViewHolderStock(View view) {
                this.view = view;

                tvStockName = (TextView) view
                        .findViewById(R.id.page_quotation_list_item_stockname);
                tvStockCode = (TextView) view
                        .findViewById(R.id.page_quotation_list_item_stockcode);
                tvStockprice = (TextView) view
                        .findViewById(R.id.page_quotation_list_item_stockprice);
                tvStockValue = (TextView) view
                        .findViewById(R.id.page_quotation_list_item_stockzdf);
                bottomDividerLine = view
                        .findViewById(R.id.page_quotation_list_item_divider);
            }

        }

    }

    /**
     * List item的父类
     * */
    private class QuotationListItem {
        public int viewType = 0;
        public boolean isDefault;

        public QuotationListItem(int viewType, boolean isDefault) {
            super();
            this.viewType = viewType;
            this.isDefault = isDefault;
        }

    }

    /**
     * List item 之section
     * */
    private class QuotationListSectionItem extends QuotationListItem {

        public String sectionTitle;
        public int rankType; // 领涨版块，领跌版块，领涨个股，领跌个股，换手率排行，主力流入排行

        public QuotationListSectionItem(int viewType, String sectionTitle,
                int rankType) {
            super(viewType, false);
            this.sectionTitle = sectionTitle;
            this.rankType = rankType;
        }

    }

    /**
     * List item 之版块六宫格
     * */
    private class QuotationListBkItem extends QuotationListItem {

        public List<BkItem> listBkItems;

        public QuotationListBkItem(int viewType, List<BkItem> listBkItems) {
            super(viewType, false);
            this.listBkItems = listBkItems;
        }

        public QuotationListBkItem(int viewType, boolean isDefault) {
            super(viewType, isDefault);
        }

    }

    /**
     * 领涨版块中每一项显示的数据内容
     * */
    private class BkItem {

        public static final int BK_ITEM_TYPE_LZ = 0; // 领涨版块
        public static final int BK_ITEM_TYPE_LD = 1; // 领跌版块

        public int bkId;
        public int bkItemType;
        public String bkTitle;
        public String bkContent;

        public BkItem(int bkId, String bkTitle, String bkContent, int itemType) {
            super();
            this.bkId = bkId;
            this.bkTitle = bkTitle;
            this.bkContent = bkContent;
            this.bkItemType = itemType;
        }

    }

    /**
     * List item 之股票
     * */
    private class QuotationListStockItem extends QuotationListItem {

        public static final int TYPE_ZF = 0; // 涨幅
        public static final int TYPE_DF = 1; // 跌幅
        public static final int TYPE_HSL = 2; // 换手率
        public static final int TYPE_ZLJLR = 3; // 主力净流入

        private int stockItemType;
        public int stockId;
        public int index; // 当前股票在股票列表中的序号
        public String stockNmae, stockCode, price, value;
        private boolean isHideBottomLine;

        public QuotationListStockItem(int viewType, String stockNmae,
                String stockCode, String price, String value, int stockId,
                int stockItemType, boolean isHideBottomLine, int index) {
            super(viewType, false);
            this.stockNmae = stockNmae;
            this.stockCode = stockCode;
            this.price = price;
            this.value = value;
            this.stockId = stockId;
            this.stockItemType = stockItemType;
            this.isHideBottomLine = isHideBottomLine;
            this.index = index;
        }

        public QuotationListStockItem(int viewType, boolean isHideBottomLine,
                boolean isDefault) {
            super(viewType, isDefault);
            this.isHideBottomLine = isHideBottomLine;
        }

    }

    private class ListHeaderViewHolder {

        public View layout;
        public TextView tvStockName;
        public TextView tvPriceInteger, tvPriceDecimal;
        public TextView tvZdf, tvZd;
        public ImageView imgZdArrow;

        public ListHeaderViewHolder(View layout) {
            this.layout = layout;

            tvStockName = (TextView) layout
                    .findViewById(R.id.item_tv_stockname);
            tvPriceInteger = (TextView) layout
                    .findViewById(R.id.item_tv_price_integer);
            tvPriceDecimal = (TextView) layout
                    .findViewById(R.id.item_tv_price_decimal);
            tvZdf = (TextView) layout.findViewById(R.id.item_tv_zdf);
            tvZd = (TextView) layout.findViewById(R.id.item_tv_zd);
            imgZdArrow = (ImageView) layout
                    .findViewById(R.id.item_img_zd_arrow);
        }

    }

    private String getGoodsNameById(int goodsId) {
        String goodsName = "";

        ArrayList<Goods> listGoods = getSQLiteDBHelper()
                .queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
        if (listGoods != null && listGoods.size() > 0) {
            goodsName = listGoods.get(0).getGoodsName();
        } else {
            Goods goods = new Goods(goodsId, "");
            goodsName = goods.getGoodsName();
        }

        return goodsName;
    }

}
