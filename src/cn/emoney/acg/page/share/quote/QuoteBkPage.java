package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ViewFlipper;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.acg.view.PriceLayer;
import cn.emoney.acg.widget.SegmentedGroup;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.KVTextLayer;
import cn.emoney.sky.libs.chart.layers.KVTextLayer.KVTextAtom;
import cn.emoney.sky.libs.utils.SortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

public class QuoteBkPage extends QuotePage implements OnClickListener {

    private boolean isCurrentSortOrderRise;
    private boolean isRequestingBkList;
    private int currcentSortField;
    private int previousPeriod;

    private List<CellBean> listDatas = new ArrayList<QuoteBkPage.CellBean>();
    private BkListAdapter adapter;
    private MinutePage minutePage;
    private KLinePage kLinePage;

    private ChartView cvPrice, cvBasic, cvLeftBottom, cvRightBottom;    // 界面顶部，显示个股指标的容器
    private PriceLayer priceLayer;    // 界面顶部，显示价格涨跌（幅）指标的控件
    private KVTextAtom mKVOpen, mKVHigh, mKVLow, mKVHS, mKVZS, mKVDS;    // 基本指标控件
    private KVTextAtom mKVCJL; // 成交量
    private KVTextAtom mKVJL; // 净流
    private KVTextAtom mKVPS; // 平数
    private KVTextAtom mKVCJE; // 成交额
    private KVTextAtom mKVLB; // 量比
    private KVTextAtom mKVZF; // 振幅
    private RadioButton periodMinute, periodDay, periodWeek, periodMonth, period60m;    // 价格走势图标签
    private ViewFlipper viewFlipperTrend;    // 价格走势图容器
    private TextView tvReleatedStock, tvStockPrice, tvStockZDF;
    private RefreshListView listView;
    private ImageView imgOptional;    // 界面底部，发起问股图标

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quotebk);

        initViews();
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 刷新走势图及其头部标签， 必须在onResume时调用，否则，首次进入界面时默认是分时被选中，而期望被选中的可能不是分时
        // 不能在initPage中调用，因为可能在横屏显示走势图时，切换了分时类型
        refreshTrendFlipper();
        refreshPeriodCheckedStatus();

        // 刷新自选图标显示
        if (currentGoodsId >= 0) {
            OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            boolean isHasAddZxg = optionalInfo.hasGoods(currentGoodsId) >= 0;
            if (isHasAddZxg) {
                imgOptional.setImageResource(R.drawable.img_quote_option_delete_gray);
            } else {
                imgOptional.setImageResource(R.drawable.img_quote_option_add);
            }
        }

        // 用户可能快速滑动切换多个界面，每个界面停留时间很短，这时就没有必要加载迅速切换过的界面的数据
        // 设置一个延迟，如果延迟结束时，仍留在此界面，才去请求数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bPageAlive) {
                    /*
                     * 启动自动刷新，放在requestData()中 1. 获取指标数据 2. 获取分时线（按设置刷新频率刷新）、K线数据（5分钟刷一次） 3. 获取版块成分个股列表（按设置刷新频率刷新）
                     */
                    if (!getIsAutoRefresh()) {
                        if (getUserVisibleHint()) {
                            startRequestTask();
                        } else {
                            requestData();
                        }
                    }
                }
            }
        }, QuoteHome.REQ_DELAY_TIME);

    }

    @Override
    public void requestData() {
        super.requestData();

        // 获取行情指标数据
        requestQuotation();

        // 获取分时线、K线
        requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_AUTO);

        // 获取版块指数成分个股列表
        int classType = getCurrentGoodsClassType();
        requestStockRank(classType, isCurrentSortOrderRise, currcentSortField);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.page_quotebk_img_optional:
                addOrRemoveZxg();
                break;
            default:
                break;
        }
    }

    private void initViews() {
        listView = (RefreshListView) findViewById(R.id.page_quotebk_list);
        imgOptional = (ImageView) findViewById(R.id.page_quotebk_img_optional);

        View listHeader = LayoutInflater.from(getContext()).inflate(R.layout.page_quotebk_listheader, listView, false);

        cvPrice = (ChartView) listHeader.findViewById(R.id.page_quotebk_listheader_cv_price);
        cvBasic = (ChartView) listHeader.findViewById(R.id.page_quotebk_listheader_cv_mmp_right);
        cvLeftBottom = (ChartView) listHeader.findViewById(R.id.page_quotebk_listheader_cv_mmp_left);
        cvRightBottom = (ChartView) listHeader.findViewById(R.id.page_quotebk_listheader_cv_mmp_right_bottom);

        SegmentedGroup segmentedGroupPeriods = (SegmentedGroup) listHeader.findViewById(R.id.page_quotebk_segment_periods);
        periodMinute = (RadioButton) listHeader.findViewById(R.id.page_quotebk_period_minute);
        periodDay = (RadioButton) listHeader.findViewById(R.id.page_quotebk_period_day);
        periodWeek = (RadioButton) listHeader.findViewById(R.id.page_quotebk_period_week);
        periodMonth = (RadioButton) listHeader.findViewById(R.id.page_quotebk_period_month);
        period60m = (RadioButton) listHeader.findViewById(R.id.page_quotebk_period_60m);
        viewFlipperTrend = (ViewFlipper) listHeader.findViewById(R.id.page_quotebk_viewflipper_trend);

        tvReleatedStock = (TextView) listHeader.findViewById(R.id.page_quotebk_tv_head_releated_stock);
        tvStockPrice = (TextView) listHeader.findViewById(R.id.page_quotebk_tv_head_price);
        tvStockZDF = (TextView) listHeader.findViewById(R.id.page_quotebk_tv_head_zdf);

        /*
         * 初始化各控件，以界面上显示顺序，从上到下
         * */

        // 初始化个股指标
        if (cvPrice != null) {
            priceLayer = new PriceLayer();
            priceLayer.setBorderColor(0x00FFFFFF);
            priceLayer.setPriceTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_PRICE));
            priceLayer.setPriceTextColor(getResources().getColor(R.color.c1));
            priceLayer.setZDTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_ZDF));
            priceLayer.setZDTextColor(getResources().getColor(R.color.c1));
            priceLayer.setZFTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_ZDF));
            priceLayer.setZFTextColor(getResources().getColor(R.color.c1));
            priceLayer.setPadding(FontUtils.dip2px(getContext(), 10));
            cvPrice.addLayer(priceLayer);
            cvPrice.postInvalidate();
        }
        if (cvBasic != null) {
            KVTextLayer kvLayerBasic = new KVTextLayer();
            kvLayerBasic.setBorderColor(0x00FFFFFF);
            kvLayerBasic.setTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_OTHER));
            kvLayerBasic.setRow(3);
            kvLayerBasic.setCol(2);

            mKVOpen = new KVTextAtom("开盘: ", "—", getResources().getColor(R.color.t3));
            mKVHigh = new KVTextAtom("最高: ", "—", getResources().getColor(R.color.t3));
            mKVLow = new KVTextAtom("最低: ", "—", getResources().getColor(R.color.t3));

            mKVHS = new KVTextAtom("换手: ", "—", getResources().getColor(R.color.t3));
            mKVZS = new KVTextAtom("涨数: ", "—", getResources().getColor(R.color.t3));
            mKVDS = new KVTextAtom("跌数: ", "—", getResources().getColor(R.color.t3));

            kvLayerBasic.addText(0, 0, mKVOpen);
            kvLayerBasic.addText(1, 0, mKVHigh);
            kvLayerBasic.addText(2, 0, mKVLow);

            kvLayerBasic.addText(0, 1, mKVHS);
            kvLayerBasic.addText(1, 1, mKVZS);
            kvLayerBasic.addText(2, 1, mKVDS);

            cvBasic.addLayer(kvLayerBasic);
            cvBasic.postInvalidate();
        }
        if (cvRightBottom != null) {
            KVTextLayer kvLayerRightBottom = new KVTextLayer();
            kvLayerRightBottom.setBorderColor(0x00FFFFFF);
            kvLayerRightBottom.setTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_OTHER));
            kvLayerRightBottom.setRow(2);
            kvLayerRightBottom.setCol(2);

            mKVJL = new KVTextAtom("净流: ", "—", getResources().getColor(R.color.t3));
            mKVPS = new KVTextAtom("平数: ", "—", getResources().getColor(R.color.t3));

            mKVLB = new KVTextAtom("量比: ", "—", getResources().getColor(R.color.t3));
            mKVZF = new KVTextAtom("振幅: ", "—", getResources().getColor(R.color.t3));

            kvLayerRightBottom.addText(0, 0, mKVJL);
            kvLayerRightBottom.addText(0, 1, mKVPS);

            kvLayerRightBottom.addText(1, 0, mKVLB);
            kvLayerRightBottom.addText(1, 1, mKVZF);

            cvRightBottom.addLayer(kvLayerRightBottom);
            cvRightBottom.postInvalidate();
        }
        if (cvLeftBottom != null) {
            KVTextLayer kvLayerLeftBottom = new KVTextLayer();
            kvLayerLeftBottom.setBorderColor(0x00FFFFFF);
            kvLayerLeftBottom.setTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_OTHER));
            kvLayerLeftBottom.setRow(2);
            kvLayerLeftBottom.setCol(1);

            mKVCJL = new KVTextAtom("成交量: ", "—", getResources().getColor(R.color.t3));
            mKVCJE = new KVTextAtom("成交额: ", "—", getResources().getColor(R.color.t3));

            kvLayerLeftBottom.addText(0, 0, mKVCJL);
            kvLayerLeftBottom.addText(1, 0, mKVCJE);

            cvLeftBottom.addLayer(kvLayerLeftBottom);
            cvLeftBottom.postInvalidate();
        }

        // 初始化个股走势图顶部分类标签
        segmentedGroupPeriods.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int itemId) {
                previousPeriod = QuoteHome.currentPeriod;

                switch (itemId) {
                    case R.id.page_quotebk_period_minute:
                        onChangePeriodListener.onChangePeriod(TYPE_MINUTE);
                        break;
                    case R.id.page_quotebk_period_day:
                        onChangePeriodListener.onChangePeriod(TYPE_DAY);
                        break;
                    case R.id.page_quotebk_period_week:
                        onChangePeriodListener.onChangePeriod(TYPE_WEEK);
                        break;
                    case R.id.page_quotebk_period_month:
                        onChangePeriodListener.onChangePeriod(TYPE_MONTH);
                        break;
                    case R.id.page_quotebk_period_60m:
                        onChangePeriodListener.onChangePeriod(QuoteHome.currentMorePeriod);
                        break;
                    default:
                        break;
                }

                refreshTrendFlipper();
                requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_CHANGE_PERIOD);
            }
        });
        period60m.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果前一次是60分钟线，点击时判断more item的显示与隐藏
                if ((previousPeriod == TYPE_MINUTE || previousPeriod == TYPE_DAY || previousPeriod == TYPE_WEEK 
                        || previousPeriod == TYPE_MONTH) && QuoteHome.currentPeriod == QuoteHome.currentMorePeriod) {
                    previousPeriod = QuoteHome.currentPeriod;
                } else if (previousPeriod == QuoteHome.currentMorePeriod && QuoteHome.currentPeriod == QuoteHome.currentMorePeriod) {
                    showMorePeriodOptions();
                }
            }
        });

        // 初始化个股走势
        if (viewFlipperTrend != null) {
            minutePage = new MinutePage();
            viewFlipperTrend.addView(minutePage.convertToView(this, getActivity().getLayoutInflater(), null, null));

            kLinePage = new KLinePage();
            kLinePage.setPeriod(TYPE_DAY);
            viewFlipperTrend.addView(kLinePage.convertToView(this, getActivity().getLayoutInflater(), null, null));

            if (currentGoodsId > 0) {
                minutePage.setGoodsId(currentGoodsId);
                kLinePage.setGoodsId(currentGoodsId);
            }
        }

        // 刷新 “成分个股”或“常用指数”
        if (currentGoodsId > 0 && DataUtils.IsZS(currentGoodsId)) {
            tvReleatedStock.setText("常用指数");
        }

        SymbolSortHelper sortHelper = new SymbolSortHelper();
        sortHelper.setItemTextColor(RColor(R.color.t3));
        sortHelper.setItemSelectedTextColor(RColor(R.color.c4));
        sortHelper.setStrPadding(" ");

        sortHelper.addSortItem(tvReleatedStock, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
        sortHelper.addSortItem(tvStockPrice, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
        sortHelper.addSortItem(tvStockZDF, SortHelper.SORT_RISE | SortHelper.SORT_FALL);

        currcentSortField = GoodsParams.ZDF;
        isCurrentSortOrderRise = true;
        sortHelper.setDefaultSort(tvStockZDF, SortHelper.SORT_RISE);
        sortHelper.setOnSortListener(new SymbolSortHelper.OnSortListener() {

            @Override
            public void onSort(TextView view, int sortType) {
                int id = view.getId();

                if (id == R.id.page_quotebk_tv_head_zdf) {
                    currcentSortField = GoodsParams.ZDF;
                    if (sortType == SortHelper.SORT_RISE) {
                        isCurrentSortOrderRise = true;
                    } else if (sortType == SortHelper.SORT_FALL) {
                        isCurrentSortOrderRise = false;
                    }
                } else if (id == R.id.page_quotebk_tv_head_price) {
                    currcentSortField = GoodsParams.ZXJ;
                    if (sortType == SortHelper.SORT_RISE) {
                        isCurrentSortOrderRise = true;
                    } else if (sortType == SortHelper.SORT_FALL) {
                        isCurrentSortOrderRise = false;
                    }
                } else if (id == R.id.page_quotebk_tv_head_releated_stock) {
                    currcentSortField = GoodsParams.GOODS_CODE;
                    if (sortType == SortHelper.SORT_RISE) {
                        isCurrentSortOrderRise = true;
                    } else if (sortType == SortHelper.SORT_FALL) {
                        isCurrentSortOrderRise = false;
                    }
                }

                requestStockRank(getCurrentGoodsClassType(), isCurrentSortOrderRise, currcentSortField);
            }
        });

        // 下拉刷新头
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 刷新数据
                requestData();

                // 规定时间后，如果还没有返回数据，隐藏下拉刷新头
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (listView != null) {
                            listView.onRefreshFinished();
                        }
                    }
                }, 3000);
            }
            @Override
            public void beforeRefresh() { }
            @Override
            public void afterRefresh() { }
        });
        listView.addHeaderView(listHeader);
        adapter = new BkListAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);

        findViewById(R.id.page_quotebk_img_optional).setOnClickListener(this);
    }

    private int getCurrentGoodsClassType() {
        int classType = 3;

        if (DataUtils.IsGNBK(currentGoodsId)) {
            classType = 0;
        } else if (DataUtils.IsHYBK(currentGoodsId)) {
            classType = 1;
        } else if (DataUtils.IsDQBK(currentGoodsId)) {
            classType = 2;
        } else if (DataUtils.IsZS(currentGoodsId)) {
            classType = 3;
        }

        return classType;
    }

    /**
     * 根据当前period刷新viewflipper选中状态
     * */
    private void refreshTrendFlipper() {
        if (QuoteHome.currentPeriod == TYPE_MINUTE) {
            viewFlipperTrend.setDisplayedChild(0);
        } else {
            viewFlipperTrend.setDisplayedChild(1);
        }
    }

    /**
     * onResume()时刷新items的选中状态
     * */
    private void refreshPeriodCheckedStatus() {
        if (QuoteHome.currentPeriod == TYPE_MINUTE) {
            periodMinute.setChecked(true);
        } else if (QuoteHome.currentPeriod == TYPE_DAY) {
            periodDay.setChecked(true);
        } else if (QuoteHome.currentPeriod == TYPE_WEEK) {
            periodWeek.setChecked(true);
        } else if (QuoteHome.currentPeriod == TYPE_MONTH) {
            periodMonth.setChecked(true);
        } else {
            period60m.setChecked(true);
        }
    }
    
    /**
     * 请求过程中，不能再次请求
     * */
    private void refreshSortEnablable() {
        tvReleatedStock.setEnabled(!isRequestingBkList);
        tvStockPrice.setEnabled(!isRequestingBkList);
        tvStockZDF.setEnabled(!isRequestingBkList);
    }

    /**
     * 显示更多K线选项
     * */
    private void showMorePeriodOptions() {
        // 当非每一交点击60分时，显示更多选项
        View viewMorePeriods = LayoutInflater.from(getContext()).inflate(R.layout.page_quote_more_periods, null, false);

        // 获取组件
        SegmentedGroup periodMore = (SegmentedGroup) viewMorePeriods.findViewById(R.id.page_quote_segment_more_periods);
        RadioButton period60mMore = (RadioButton) viewMorePeriods.findViewById(R.id.page_quote_period_more_60m);
        RadioButton period30m = (RadioButton) viewMorePeriods.findViewById(R.id.page_quote_period_more_30m);
        RadioButton period15m = (RadioButton) viewMorePeriods.findViewById(R.id.page_quote_period_more_15m);

        // 刷新组件状态
        if (QuoteHome.currentMorePeriod == TYPE_60MINUTE) {
            period60mMore.setChecked(true);
        } else if (QuoteHome.currentMorePeriod == TYPE_30MINUTE) {
            period30m.setChecked(true);
        } else if (QuoteHome.currentMorePeriod == TYPE_15MINUTE) {
            period15m.setChecked(true);
        }

        final PopupWindow pw = new PopupWindow(viewMorePeriods, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        pw.setTouchable(true);
        pw.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return false;
            }
        });

        pw.setBackgroundDrawable(getResources().getDrawable(R.color.b4));

        int[] location = new int[2];
        period60m.getLocationOnScreen(location);

        int startX = DataModule.SCREEN_WIDTH - FontUtils.dip2px(getContext(), 88);
        int startY = location[1] + FontUtils.dip2px(getContext(), 27);
        pw.showAtLocation(period60m, Gravity.NO_GRAVITY, startX, startY);

        // 设置组件点击事件
        periodMore.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int itemId) {
                switch (itemId) {
                    case R.id.page_quote_period_more_60m:
                        onChangePeriodListener.onChangeMorePeriod(TYPE_60MINUTE);
                        break;
                    case R.id.page_quote_period_more_30m:
                        onChangePeriodListener.onChangeMorePeriod(TYPE_30MINUTE);
                        break;
                    case R.id.page_quote_period_more_15m:
                        onChangePeriodListener.onChangeMorePeriod(TYPE_15MINUTE);
                        break;
                    default:
                        break;
                }

                previousPeriod = QuoteHome.currentMorePeriod;

                pw.dismiss();

                // 刷新60分钟字段显示
                if (QuoteHome.currentPeriod == TYPE_60MINUTE) {
                    period60m.setText("60分钟");
                } else if (QuoteHome.currentPeriod == TYPE_30MINUTE) {
                    period60m.setText("30分钟");
                } else if (QuoteHome.currentPeriod == TYPE_15MINUTE) {
                    period60m.setText("15分钟");
                }

                requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_CHANGE_PERIOD);
            }
        });
    }

    private void addOrRemoveZxg() {
        final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();
        ArrayList<Goods> lstGoods = new ArrayList<Goods>();
        lstGoods.add(currentGoods);
        final OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
        boolean isHasAddZxg = optionalInfo.hasGoods(currentGoodsId) >= 0;
        if (isHasAddZxg) {
            if (!bIsLogin) {
                String tAddtype = OptionalInfo.TYPE_DEFAULT;

                if (optionalInfo.delGoods(tAddtype, currentGoodsId)) {
                    optionalInfo.save(getDBHelper());
                    showTip("删除自选成功");

                    imgOptional.setImageResource(R.drawable.img_quote_option_add);
                }
            } else {
                String tAddType = optionalInfo.TYPE_KEY_ALL;

                delZXG(tAddType, lstGoods, new OnOperateZXGListener() {
                    @Override
                    public void onOperate(boolean isSuccess, String msg) {
                        if (isSuccess) {
                            String tAddtype = OptionalInfo.TYPE_DEFAULT;
                            if (optionalInfo.delGoods(tAddtype, currentGoodsId)) {
                                optionalInfo.save(getDBHelper());
                                showTip("删除自选成功");

                                imgOptional.setImageResource(R.drawable.img_quote_option_add);
                            } else {
                                showTip("删除自选失败!");
                            }
                        } else {
                            showTip(msg);
                        }
                    }
                });
            }
        } else if (!isHasAddZxg) {

            if (!bIsLogin) {
                String tAddtype = OptionalInfo.TYPE_DEFAULT;

                if (optionalInfo.addGoods(tAddtype, currentGoods)) {
                    optionalInfo.save(getDBHelper());
                    showTip("添加自选成功");

                    imgOptional.setImageResource(R.drawable.img_quote_option_delete_gray);
                }
            } else {
                String tAddType = optionalInfo.TYPE_KEY_ALL;

                addZXG(tAddType, lstGoods, new OnOperateZXGListener() {

                    @Override
                    public void onOperate(boolean isSuccess, String msg) {
                        if (isSuccess) {
                            String tAddtype = OptionalInfo.TYPE_DEFAULT;
                            if (optionalInfo.addGoods(tAddtype, currentGoods)) {
                                optionalInfo.save(getDBHelper());
                                showTip("添加自选成功");

                                imgOptional.setImageResource(R.drawable.img_quote_option_delete_gray);
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

    /**
     * 获取版块行情数据
     * */
    private void requestQuotation() {
        if (currentGoodsId > 0) {
            ArrayList<Integer> goodsId = new ArrayList<Integer>();
            goodsId.add(currentGoodsId);

            ArrayList<Integer> reqFileds = new ArrayList<Integer>();
            reqFileds.add(GoodsParams.ZXJ); // 最新价
            reqFileds.add(GoodsParams.ZDF); // 涨跌幅
            reqFileds.add(GoodsParams.ZHANGDIE); // 涨跌

            reqFileds.add(GoodsParams.OPEN); // 开盘价
            reqFileds.add(GoodsParams.HiGH); // 最高价
            reqFileds.add(GoodsParams.LOW); // 最低价

            reqFileds.add(GoodsParams.HSL); // 换手率
            reqFileds.add(GoodsParams.RISE); // 涨数
            reqFileds.add(GoodsParams.FALL); // 跌数

            reqFileds.add(GoodsParams.VOLUME); // 成交量
            reqFileds.add(GoodsParams.JL); // 净流
            reqFileds.add(GoodsParams.EQUAL); // 平数

            reqFileds.add(GoodsParams.AMOUNT); // 成交额
            reqFileds.add(GoodsParams.LB); // 量比
            reqFileds.add(GoodsParams.ZHENFU); // 振幅

            DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_QUOTATION));
            pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));
            requestQuote(pkg, IDUtils.DynaValueData);
        }
    }

    /**
     * 获取历史走势
     * */
    private void requestHistoryTrend(int refreshType) {
        if (QuoteHome.currentPeriod == TYPE_MINUTE) {
            minutePage.requestData();
        } else {
            kLinePage.setPeriod(QuoteHome.currentPeriod);
            kLinePage.requestData(refreshType);
        }
    }

    /**
     * 获取版块个股列表
     * */
    private void requestStockRank(int classType, boolean isDescend, int sortField) {
        if (classType == 3) {
            ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
            goodsFiled.add(GoodsParams.GOODS_NAME);
            goodsFiled.add(GoodsParams.ZXJ);
            goodsFiled.add(GoodsParams.ZDF);
            DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_STOCK_RANK));
            pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4).setGroupType(0).addAllReqFields(goodsFiled).addAllGoodsId(DataModule.mainStockIndex).setSortField(sortField).setSortOrder(isDescend).setReqBegin(0).setReqSize(20).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
            requestQuote(pkg, IDUtils.DynaValueData);
        } else {
            ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
            goodsFiled.add(GoodsParams.GOODS_NAME);
            goodsFiled.add(GoodsParams.ZXJ);
            goodsFiled.add(GoodsParams.ZDF);
            DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_STOCK_RANK));
            pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(classType).setGroupType(currentGoods.getGoodsId()).addAllReqFields(goodsFiled).setSortField(sortField).setSortOrder(isDescend).setReqBegin(0).setReqSize(20).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
            requestQuote(pkg, IDUtils.DynaValueData);
        }
        
        isRequestingBkList = true;
        refreshSortEnablable();
        
        // 3秒钟后，如果仍未返回成功，则可重新请求
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequestingBkList) {
                    isRequestingBkList = false;
                    refreshSortEnablable();
                }
            }
        }, 3000);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);

        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage ddpkg = (DynaValueDataPackage) pkg;
            int id = ddpkg.getRequestType();

            if (id == REQUEST_TYPE_QUOTATION) {
                if (listView != null) {
                    listView.onRefreshFinished();
                }

                DynaValueData_Reply reply = ddpkg.getResponse();

                int t_date = reply.getCurUpdateMarketDate();
                int t_time = reply.getCurUpdateMarketTime();
                if (mOnNoticeRefresh != null) {
                    mOnNoticeRefresh.refreshNotice(getTransactionStateInfo(t_date, t_time));
                }

                List<Integer> listReqFieldIds = reply.getRepFieldsList();

                List<DynaQuota> listQuotaValues = reply.getQuotaValueList();

                if (listQuotaValues != null && listQuotaValues.size() > 0) {
                    DynaQuota quotaValue = listQuotaValues.get(0);

                    List<String> listReqFieldValues = quotaValue.getRepFieldValueList();

                    // 获取并刷新最新价、涨跌、涨跌幅 --- begin
                    int indexZXJ = listReqFieldIds.indexOf(GoodsParams.ZXJ);
                    int indexZDF = listReqFieldIds.indexOf(GoodsParams.ZDF);
                    int indexZD = listReqFieldIds.indexOf(GoodsParams.ZHANGDIE);

                    String fieldValuePrice = listReqFieldValues.get(indexZXJ);
                    String fieldValueZd = listReqFieldValues.get(indexZD);
                    String fieldValueZdf = listReqFieldValues.get(indexZDF);

                    int color = getZDPColor(FontUtils.getColorByZD(fieldValueZd));
                    String currentPrice = DataUtils.getPrice(fieldValuePrice);
                    String zd = DataUtils.getZD(fieldValueZd);
                    String currentZdf = DataUtils.getZDF(fieldValueZdf);

                    priceLayer.setPriceText(currentPrice);
                    priceLayer.setPriceTextColor(color);
                    priceLayer.setZDText(zd);
                    priceLayer.setZDTextColor(color);
                    priceLayer.setZFText(currentZdf);
                    priceLayer.setZFTextColor(color);
                    float flag = Float.parseFloat(fieldValueZdf);
                    Bitmap bmp = null;
                    if (flag > 0) {
                        bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_arrow_up_sort);
                    } else if (flag < 0) {
                        bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_arrow_down_sort);
                    }
                    priceLayer.setArrowBmp(bmp);

                    cvPrice.postInvalidate();
                    // 获取并刷新最新价、涨跌、涨跌幅 --- end

                    int indexOpen = listReqFieldIds.indexOf(GoodsParams.OPEN);
                    int indexHigh = listReqFieldIds.indexOf(GoodsParams.HiGH);
                    int indexLow = listReqFieldIds.indexOf(GoodsParams.LOW);

                    int indexHsl = listReqFieldIds.indexOf(GoodsParams.HSL);
                    int indexRise = listReqFieldIds.indexOf(GoodsParams.RISE);
                    int indexFall = listReqFieldIds.indexOf(GoodsParams.FALL);

                    int indexVolumn = listReqFieldIds.indexOf(GoodsParams.VOLUME);
                    int indexJl = listReqFieldIds.indexOf(GoodsParams.JL);
                    int indexEqual = listReqFieldIds.indexOf(GoodsParams.EQUAL);

                    int indexAmount = listReqFieldIds.indexOf(GoodsParams.AMOUNT);
                    int indexLb = listReqFieldIds.indexOf(GoodsParams.LB);
                    int indexZhenfu = listReqFieldIds.indexOf(GoodsParams.ZHENFU);

                    String fieldValueOpen = listReqFieldValues.get(indexOpen);
                    String fieldValueHigh = listReqFieldValues.get(indexHigh);
                    String fieldValueLow = listReqFieldValues.get(indexLow);

                    String fieldValueHsl = listReqFieldValues.get(indexHsl);
                    String fieldValueRise = listReqFieldValues.get(indexRise);
                    String fieldValueFall = listReqFieldValues.get(indexFall);

                    String fieldValueVolumn = listReqFieldValues.get(indexVolumn);
                    String fieldValueJl = listReqFieldValues.get(indexJl);
                    String fieldValueEqual = listReqFieldValues.get(indexEqual);

                    String fieldValueAmount = listReqFieldValues.get(indexAmount);
                    String fieldValueLb = listReqFieldValues.get(indexLb);
                    String fieldValueZhenfu = listReqFieldValues.get(indexZhenfu);

                    mKVOpen.setText(DataUtils.getPrice(fieldValueOpen));
                    mKVHigh.setText(DataUtils.getPrice(fieldValueHigh));
                    mKVLow.setText(DataUtils.getPrice(fieldValueLow));

                    mKVHS.setText(DataUtils.getHSL(fieldValueHsl));
                    mKVZS.setText(fieldValueRise);
                    mKVDS.setText(fieldValueFall);

                    if (DataUtils.IsZS(currentGoods.getGoodsId())) {
                        mKVCJL.setText(DataUtils.formatVolume(fieldValueVolumn));
                    } else {
                        long val = Long.valueOf(fieldValueVolumn);
                        val = val / 100;
                        mKVCJL.setText(DataUtils.formatVolume(val));
                    }
                    mKVJL.setText(DataUtils.formatJL(fieldValueJl, DataUtils.mDecimalFormat1));
                    mKVPS.setText(fieldValueEqual);

                    mKVCJE.setText(DataUtils.formatAmount(fieldValueAmount));
                    mKVLB.setText(DataUtils.formatLb(fieldValueLb));
                    mKVZF.setText(DataUtils.formatZdf(fieldValueZhenfu));

                    cvBasic.postInvalidate();
                    cvLeftBottom.postInvalidate();
                    cvRightBottom.postInvalidate();
                }
            } else if (id == REQUEST_TYPE_STOCK_RANK) {
                if (listView != null) {
                    listView.onRefreshFinished();
                }
                
                if (!isRequestingBkList) {
                    refreshSortEnablable();
                    return;
                }
                
                isRequestingBkList = false;
                refreshSortEnablable();

                DynaValueData_Reply gr = ddpkg.getResponse();
                if (gr == null || gr.getQuotaValueList().size() == 0) {
                    return;
                }

                int t_date = gr.getCurUpdateMarketDate();
                int t_time = gr.getCurUpdateMarketTime();
                if (mOnNoticeRefresh != null) {
                    mOnNoticeRefresh.refreshNotice(getTransactionStateInfo(t_date, t_time));
                }

                List<Integer> fieldIds = gr.getRepFieldsList();
                int indexZDF = fieldIds.indexOf(GoodsParams.ZDF);
                int indexName = fieldIds.indexOf(GoodsParams.GOODS_NAME);
                int indexPrice = fieldIds.indexOf(GoodsParams.ZXJ);

                List<DynaQuota> data = gr.getQuotaValueList();
                List<CellBean> listRankInfos = new ArrayList<CellBean>();

                for (int i = 0; i < data.size(); i++) {
                    DynaQuota quote = data.get(i);
                    int goodsId = quote.getGoodsId();

                    String price = quote.getRepFieldValue(indexPrice);
                    String zdf = quote.getRepFieldValue(indexZDF);
                    String name = quote.getRepFieldValue(indexName);
                    String code = QuoteUtils.getStockCodeByGoodsId(String.valueOf(goodsId));

                    CellBean bean = new CellBean(name, code, price, zdf, goodsId);
                    listRankInfos.add(bean);
                }

                listDatas.clear();
                listDatas.addAll(listRankInfos);

                adapter.notifyDataSetChanged();
            }
        }
    }

    private class BkListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<CellBean> listDatas;

        public BkListAdapter(Context context, List<CellBean> listDatas) {
            this.inflater = LayoutInflater.from(getContext());
            this.listDatas = listDatas;
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
                convertView = inflater.inflate(R.layout.page_quotebk_listitem, parent, false);

                vh = new ViewHolder(convertView);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final CellBean bean = listDatas.get(position);
            String zdf = bean.stockZdf;

            vh.tvStockName.setText(bean.stockName);
            vh.tvStockCode.setText(bean.stockCode);
            vh.tvPrice.setText(DataUtils.getPrice(bean.stockPrice));
            vh.tvZdf.setText(DataUtils.getSignedZDF(zdf));
            vh.tvZdf.setBackgroundColor(getZDPColor(FontUtils.getColorByZDF(zdf)));

            vh.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Goods goods = new Goods(bean.goodsId, bean.stockName);
                    QuoteJump.gotoQuote(QuoteBkPage.this, goods);
//                    gotoQuote(goods);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;

            public TextView tvStockName, tvStockCode, tvPrice, tvZdf;

            public ViewHolder(View view) {
                layout = view;

                tvStockName = (TextView) view.findViewById(R.id.item_tv_stockname);
                tvStockCode = (TextView) view.findViewById(R.id.item_tv_stockcode);
                tvPrice = (TextView) view.findViewById(R.id.item_tv_price);
                tvZdf = (TextView) view.findViewById(R.id.item_tv_zdf);
            }
        }

    }

    private class CellBean {
        public String stockName, stockCode, stockZdf, stockPrice;
        public int goodsId;

        public CellBean(String stockName, String stockCode, String stockPrice, String stockZdf, int goodsId) {
            this.stockName = stockName;
            this.stockCode = stockCode;
            this.stockPrice = stockPrice;
            this.stockZdf = stockZdf;
            this.goodsId = goodsId;
        }

    }

}
