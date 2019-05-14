package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseArray;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.QuoteItemBean;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.protocol.quiz.QuizRelatePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRelateReply.QuizRalate_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizRelateRequest.QuizRalate_Request;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.data.quiz.QuizGlobalData;
import cn.emoney.acg.dialog.CustomDialog;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.media.AppMediaPlayerManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.page.quiz.QuizListViewlListener;
import cn.emoney.acg.page.quiz.StartAskPageManager;
import cn.emoney.acg.page.quiz.TeacherDetailPage;
import cn.emoney.acg.page.share.LoginPage;
import cn.emoney.acg.page.share.infodetail.InfoDetailHome;
import cn.emoney.acg.page.share.infodetail.InfoDetailPage;
import cn.emoney.acg.page.share.infodetail.InfoDetailTip;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.textviewlink.LinkManager;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.view.PriceLayer;
import cn.emoney.acg.widget.SegmentedGroup;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.KVTextLayer;
import cn.emoney.sky.libs.chart.layers.KVTextLayer.KVTextAtom;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

/**
 * 个股详情界面
 * */
public class QuoteStockPage extends QuotePage implements OnClickListener, QuizListViewlListener, MyMediaPlayerListener {
    private static final int QUOTE_PAGE_CODE = 400001; // 跳转到登录界面时标记本界面，以便返回

    // 个股消息类型
    public static final int INFO_TYPE_NEWS = 1001;
    public static final int INFO_TYPE_QUESTION = 1002;
    public static final int INFO_TYPE_NOTICE = 1003;
    public static final int INFO_TYPE_REPORT = 1004;

    // 个股数据类型
    public static final int DATA_TYPE_FUND = 2001;
    public static final int DATA_TYPE_RELATIVE = 2002;
    public static final int DATA_TYPE_FINANCIAL = 2003;
    public static final int DATA_TYPE_DIAGNOSE = 2004;

    private final short REQUEST_TYPE_RELATIVE_QUIZE = 3001;

    /**
     * 新闻、公告最大展示条数
     * */
    private final int MAX_ITEM_COUNT = 10;
    private final int MIN_ITEM_COUNT = 5;

    private int previousPeriod;
    private int currentInfoType = INFO_TYPE_NEWS; // 当前消息类型
    private int currentDataType = DATA_TYPE_FUND; // 当前数据类型

    private boolean isHasAddZxg;
    private boolean isRequestingNews;

    private String currentPrice;
    private String currentZdf;
    private String currentZd;

    private QuoteListAdapter adapter;
    private List<QuoteItemBean> listItems = new ArrayList<QuoteItemBean>();
    private SparseArray<List<QuoteItemBean>> sparseArrayListInfos = new SparseArray<List<QuoteItemBean>>();

    private MinutePage minutePage;
    private KLinePage kLinePage;
    private FundInflowPage fundFlowPage; // 资金流动
    private QuoteRelativePage quoteRelativePage; // 个股关联版块
    private QuoteFinalcialPage quoteFinalcialPage; // 个股财务报告
    private QuoteDiagnosePage quoteDiagnosePage; // 个股诊股

    /**
     * 缓存个股新闻公告跳转信息
     * */
    private SparseArray<ArrayList<Map<String, String>>> sparseArrayInfoDetails = new SparseArray<ArrayList<Map<String, String>>>();
    private MajorTipBean majorTipBean;
    // 存储已经读过的消息
    private SparseArray<List<String>> sparseArrayReaded = new SparseArray<List<String>>();

    /*
     * 语音播放相关
     */
    private int playPos = -1;
    private AppMediaPlayerManager mediaPlayerManager;
    
    private StartAskPageManager startAskPageManager;

    /*
     * 界面控件放置区
     */
    private RefreshListView listView; // 消息列表
    private ImageView imgAlertStatus, imgOptional, ivQuize; // 界面底部，发起问股、预警设置图标
    private ChartView cvPrice, cvBasic, cvLeftBottom, cvRightBottom; // 界面顶部，显示个股指标的容器
    private PriceLayer priceLayer; // 界面顶部，显示价格涨跌（幅）指标的控件
    private KVTextLayer kvLayerBasic, kvLayerLeftBottom, kvLayerRightBottom; // 界面顶部，显示指标的子容器
    private KVTextAtom mKVOpen, mKVHigh, mKVLow, mKVHS, mKVSY, mKVSJ; // 基本指标控件
    private KVTextAtom mKVCJL; // 成交量
    private KVTextAtom mKVJL; // 净流
    private KVTextAtom mKVZZ; // 总值
    private KVTextAtom mKVCJE; // 成交额
    private KVTextAtom mKVLB; // 量比
    private KVTextAtom mKVLZ; // 流值
    private TextView tvSuspension; // 停牌信息
    private RadioButton periodMinute, periodDay, periodWeek, periodMonth, period60m; // 价格走势图标签
    private ViewFlipper viewFlipperTrend; // 价格走势图容器
    private LinearLayout layoutTips; // 重大提示布局
    private TextView tvTips; // 重大提示
    private TextView tvEmpty; // 刷新失败，请点击重试，有数据时隐藏，默认显示
    private View layoutLoading; // 正在加载新闻列表
    private View layoutLoadMore; // 查看更多
    private View footerDivider; // 查看更多和个股底部标签之间的分隔区，有新闻数据时显示，反之不显示，默认不显示
    private View viewQuizBlank;
    private ViewFlipper viewFlipperDatas; // 个股数据容器

    

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quote);

        initViews();
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        LogUtil.easylog("test QuoteStockPage->onPgaeResume:" + currentGoodsId);

        // 刷新走势图及其头部标签 ??? 是否必须在resume时调用
        refreshTrendFlipper();
        refreshPeriodCheckedStatus();

        // 刷新个股资金等数据显示类型 ??? 是否必须在resume时调用
        refreshDataFlipper();

        // 刷新预警图标显示
        refreshImgAlertStatus();
        
        // 刷新我的问答显示与否，老师不显示，其它显示
        if (getUserInfo().isRoleTeacher()) {
            ivQuize.setVisibility(View.GONE);
        } else {
            ivQuize.setVisibility(View.VISIBLE);
        }

        // 刷新自选图标显示
        if (currentGoodsId >= 0) {
            OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            isHasAddZxg = optionalInfo.hasGoods(currentGoodsId) >= 0;
            if (isHasAddZxg) {
                imgOptional.setImageResource(R.drawable.img_quote_option_delete_gray);
            } else {
                imgOptional.setImageResource(R.drawable.img_quote_option_add);
            }
        }

        // 获取sqlite中存储的已经读到的消息的集合
        if (currentInfoType == INFO_TYPE_NEWS) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NEWS, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                sparseArrayReaded.put(INFO_TYPE_NEWS, mLstReaded);
            }
        } else if (currentInfoType == INFO_TYPE_NOTICE) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NOTICE, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                sparseArrayReaded.put(INFO_TYPE_NOTICE, mLstReaded);
            }
        } else if (currentInfoType == INFO_TYPE_REPORT) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_REPORT, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                sparseArrayReaded.put(INFO_TYPE_REPORT, mLstReaded);
            }
        }

        // 用户可能快速滑动切换多个界面，每个界面停留时间很短，这时就没有必要加载迅速切换过的界面的数据
        // 设置一个延迟，如果延迟结束时，仍留在此界面，才去请求数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bPageAlive) {
                    doRequestData();
                }
            }
        }, QuoteHome.REQ_DELAY_TIME);

        if (mediaPlayerManager != null) {
            mediaPlayerManager.setPageState(true);
        }

    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

        if (mediaPlayerManager != null) {
            mediaPlayerManager.onPause();
            mediaPlayerManager.setPageState(false);
        }
    }

    @Override
    protected void onPageDestroy() {
        super.onPageDestroy();

        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
            mediaPlayerManager = null;
        }
    }

    private void initViews() {
        /*
         * 从布局中查找组件
         */
        listView = (RefreshListView) findViewById(R.id.page_quote_list);
        imgAlertStatus = (ImageView) findViewById(R.id.page_quote_img_alert);
        ivQuize = (ImageView) findViewById(R.id.page_quote_img_quize);
        imgOptional = (ImageView) findViewById(R.id.page_quote_img_optional);

        View listHeader = LayoutInflater.from(getContext()).inflate(R.layout.page_quote_listheader, null);
        View listFooter = LayoutInflater.from(getContext()).inflate(R.layout.page_quote_listfooter, null);

        cvPrice = (ChartView) listHeader.findViewById(R.id.page_quote_listheader_cv_price);
        cvBasic = (ChartView) listHeader.findViewById(R.id.page_quote_listheader_cv_mmp_right);
        cvLeftBottom = (ChartView) listHeader.findViewById(R.id.page_quote_listheader_cv_mmp_left);
        cvRightBottom = (ChartView) listHeader.findViewById(R.id.page_quote_listheader_cv_mmp_right_bottom);

        tvSuspension = (TextView) listHeader.findViewById(R.id.page_quote_tv_suspension);

        SegmentedGroup segmentedGroupPeriods = (SegmentedGroup) listHeader.findViewById(R.id.page_quote_segment_periods);
        periodMinute = (RadioButton) listHeader.findViewById(R.id.page_quote_period_minute);
        periodDay = (RadioButton) listHeader.findViewById(R.id.page_quote_period_day);
        periodWeek = (RadioButton) listHeader.findViewById(R.id.page_quote_period_week);
        periodMonth = (RadioButton) listHeader.findViewById(R.id.page_quote_period_month);
        period60m = (RadioButton) listHeader.findViewById(R.id.page_quote_period_60m);
        viewFlipperTrend = (ViewFlipper) listHeader.findViewById(R.id.page_quote_viewflipper_trend);

        layoutTips = (LinearLayout) listHeader.findViewById(R.id.page_quote_layout_tips);
        tvTips = (TextView) listHeader.findViewById(R.id.page_quote_tv_tips);

        SegmentedGroup segmentedGroupInfos = (SegmentedGroup) listHeader.findViewById(R.id.page_quote_segment_group_infos);

        tvEmpty = (TextView) listFooter.findViewById(R.id.page_quote_listfooter_tv_empty);
        layoutLoading = listFooter.findViewById(R.id.page_quote_listfooter_layout_loading);
        layoutLoadMore = listFooter.findViewById(R.id.page_quote_listfooter_layout_loadmore);
        footerDivider = listFooter.findViewById(R.id.page_quote_listfooter_divider);
        viewQuizBlank = listFooter.findViewById(R.id.page_quote_listfooter_quizblank);

        viewFlipperDatas = (ViewFlipper) listFooter.findViewById(R.id.page_quote_listfooter_viewflipper);
        SegmentedGroup segmentedGroupDatas = (SegmentedGroup) listFooter.findViewById(R.id.page_quote_listfooter_segment_datas);
        
        /*
         * 初始化各控件，以界面上显示顺序，从上到下
         */

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
            kvLayerBasic = new KVTextLayer();
            kvLayerBasic.setBorderColor(0x00FFFFFF);
            kvLayerBasic.setTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_OTHER));
            kvLayerBasic.setRow(3);
            kvLayerBasic.setCol(2);

            mKVOpen = new KVTextAtom("开盘: ", "—", getResources().getColor(R.color.t3));
            mKVHigh = new KVTextAtom("最高: ", "—", getResources().getColor(R.color.t3));
            mKVLow = new KVTextAtom("最低: ", "—", getResources().getColor(R.color.t3));

            mKVHS = new KVTextAtom("换手: ", "—", getResources().getColor(R.color.t3));
            mKVSY = new KVTextAtom("市盈: ", "—", getResources().getColor(R.color.t3));
            mKVSJ = new KVTextAtom("市净: ", "—", getResources().getColor(R.color.t3));

            kvLayerBasic.addText(0, 0, mKVOpen);
            kvLayerBasic.addText(1, 0, mKVHigh);
            kvLayerBasic.addText(2, 0, mKVLow);

            kvLayerBasic.addText(0, 1, mKVHS);
            kvLayerBasic.addText(1, 1, mKVSY);
            kvLayerBasic.addText(2, 1, mKVSJ);

            cvBasic.addLayer(kvLayerBasic);
            cvBasic.postInvalidate();
        }
        if (cvRightBottom != null) {
            kvLayerRightBottom = new KVTextLayer();
            kvLayerRightBottom.setBorderColor(0x00FFFFFF);
            kvLayerRightBottom.setTextSize(FontUtils.dip2px(getContext(), FontUtils.SIZE_TXT_QUOTE_OTHER));
            kvLayerRightBottom.setRow(2);
            kvLayerRightBottom.setCol(2);

            mKVJL = new KVTextAtom("净流: ", "—", getResources().getColor(R.color.t3));
            mKVZZ = new KVTextAtom("总值: ", "—", getResources().getColor(R.color.t3));

            mKVLB = new KVTextAtom("量比: ", "—", getResources().getColor(R.color.t3));
            mKVLZ = new KVTextAtom("流值: ", "—", getResources().getColor(R.color.t3));

            kvLayerRightBottom.addText(0, 0, mKVJL);
            kvLayerRightBottom.addText(0, 1, mKVZZ);

            kvLayerRightBottom.addText(1, 0, mKVLB);
            kvLayerRightBottom.addText(1, 1, mKVLZ);

            cvRightBottom.addLayer(kvLayerRightBottom);
            cvRightBottom.postInvalidate();
        }
        if (cvLeftBottom != null) {
            kvLayerLeftBottom = new KVTextLayer();
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
                    case R.id.page_quote_period_minute:
                        onChangePeriodListener.onChangePeriod(TYPE_MINUTE);
                        break;
                    case R.id.page_quote_period_day:
                        onChangePeriodListener.onChangePeriod(TYPE_DAY);
                        break;
                    case R.id.page_quote_period_week:
                        onChangePeriodListener.onChangePeriod(TYPE_WEEK);
                        break;
                    case R.id.page_quote_period_month:
                        onChangePeriodListener.onChangePeriod(TYPE_MONTH);
                        break;
                    case R.id.page_quote_period_60m:
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
                if ((previousPeriod == TYPE_MINUTE || previousPeriod == TYPE_DAY || previousPeriod == TYPE_WEEK || previousPeriod == TYPE_MONTH) && QuoteHome.currentPeriod == QuoteHome.currentMorePeriod) {
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

        // 初始化个股重大提示
        tvTips.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                // 显示重大提示请情页
                if (majorTipBean != null) {
                    gotoMajorTipDetail(majorTipBean);
                }
            }
        });

        // 初始化消息头部标签
        segmentedGroupInfos.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int itemId) {
                switch (itemId) {
                    case R.id.page_quote_info_item_news:
                        currentInfoType = INFO_TYPE_NEWS;
                        break;
                    case R.id.page_quote_info_item_question:
                        currentInfoType = INFO_TYPE_QUESTION;
                        break;
                    case R.id.page_quote_info_item_notice:
                        currentInfoType = INFO_TYPE_NOTICE;
                        break;
                    case R.id.page_quote_info_item_report:
                        currentInfoType = INFO_TYPE_REPORT;
                        break;
                    default:
                        break;
                }

                // 如果正在请求，取消请求
                if (isRequestingNews) {
                    isRequestingNews = false;

                    layoutLoading.setVisibility(View.GONE);
                }

                refreshListView();

                // 如果无缓存，向网络获取数据
                List<QuoteItemBean> listBeans = sparseArrayListInfos.get(currentInfoType, null);
                if (!(listBeans != null && listBeans.size() > 0) && isRequestingNews == false) {
                    doRequestStockInfos();
                }
            }
        });

        // 初始化消息列表，主ListView
        // 下拉刷新头
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 获取个股行情指标
                requestQuotation();

                // 获取分时线、K线
                requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_PULL);

                // 获取问答是否有新数据

                if (currentDataType == DATA_TYPE_FUND) {
                    // 获取当日资金
                    fundFlowPage.requestData(FundInflowPage.FUNDFLOW_REFRESH_TYPE_PULL);
                } else if (currentDataType == DATA_TYPE_FINANCIAL) {
                    quoteFinalcialPage.requestData();
                } else if (currentDataType == DATA_TYPE_RELATIVE) {
                    quoteRelativePage.requestData();
                } else if (currentDataType == DATA_TYPE_DIAGNOSE) {
                    quoteDiagnosePage.requestData();
                }

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
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() {}
        });
        // 添加Header，Footer和Adapter
        listView.addHeaderView(listHeader);
        listView.addFooterView(listFooter);
        adapter = new QuoteListAdapter(listItems, getContext(), this, this);
        listView.setAdapter(adapter);

        // 初始化加载数据的点击事件
        tvEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRequestingNews) {
                    doRequestStockInfos();
                }
            }
        });

        // 初始化查看更多点击事件
        layoutLoadMore.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (currentInfoType == INFO_TYPE_QUESTION) {
                    gotoMoreQuizePage();
                } else {
                    gotoMoreNewsPage();
                }
            }
        });

        // 初始化数据内容
        if (viewFlipperDatas != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();

            fundFlowPage = new FundInflowPage();
            viewFlipperDatas.addView(fundFlowPage.convertToView(this, inflater, null, null));

            quoteRelativePage = new QuoteRelativePage();
            viewFlipperDatas.addView(quoteRelativePage.convertToView(this, inflater, null, null));

            quoteFinalcialPage = new QuoteFinalcialPage();
            viewFlipperDatas.addView(quoteFinalcialPage.convertToView(this, inflater, null, null));

            quoteDiagnosePage = new QuoteDiagnosePage();
            viewFlipperDatas.addView(quoteDiagnosePage.convertToView(this, inflater, null, null));

            if (currentGoodsId > 0) {
                fundFlowPage.setGoodsId(currentGoodsId);
                quoteRelativePage.setGoodsId(currentGoodsId);
                quoteFinalcialPage.setGoodsId(currentGoodsId);
                quoteDiagnosePage.setGoodsId(currentGoodsId);
            }
        }

        // 初始化个股数据头部标签
        segmentedGroupDatas.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.page_quote_listfooter_data_funds:
                        currentDataType = DATA_TYPE_FUND;
                        fundFlowPage.requestData(FundInflowPage.FUNDFLOW_REFRESH_TYPE_PULL);
                        break;
                    case R.id.page_quote_listfooter_data_relatives:
                        currentDataType = DATA_TYPE_RELATIVE;
                        quoteRelativePage.requestData();
                        break;
                    case R.id.page_quote_listfooter_data_financial:
                        currentDataType = DATA_TYPE_FINANCIAL;
                        quoteFinalcialPage.requestData();
                        break;
                    case R.id.page_quote_listfooter_data_diagnose:
                        currentDataType = DATA_TYPE_DIAGNOSE;
                        quoteDiagnosePage.requestData();
                        break;
                    default:
                        break;
                }

                refreshDataFlipper();
            }
        });

        // 点击bottom中的选项，切换到相应功能
        findViewById(R.id.page_quote_img_alert).setOnClickListener(this);
        findViewById(R.id.page_quote_img_quize).setOnClickListener(this);
        findViewById(R.id.page_quote_img_optional).setOnClickListener(this);
    }

    /**
     * 循环刷新的数据
     * */
    @Override
    public void requestData() {
        super.requestData();
        LogUtil.easylog("test QuoteStockPage->requestData");

        // 获取个股行情指标
        requestQuotation();

        // 获取分时线、K线
        requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_AUTO);

        // 获取问答是否有新数据

        if (currentDataType == DATA_TYPE_FUND) {
            // 获取当日资金
            fundFlowPage.requestData(FundInflowPage.FUNDFLOW_REFRESH_TYPE_AUTO);
            LogUtil.easylog("fundFlowPage.requestData");
        } else if (currentDataType == DATA_TYPE_RELATIVE) {
            // 获取个股关联版块
            quoteRelativePage.requestData();
        }
    }

    /**
     * 请求各种数据
     * */
    private void doRequestData() {

        /*
         * 循环请求的，放在requestData()中 1. 获取指标数据 2. 获取分时线（按设置刷新频率刷新）、K线数据（5分钟刷一次） 3. 获取问答是否有新数据 4.
         * 当日资金(资金) 5. 个股关联版块信息
         */
        if (!getIsAutoRefresh()) {
            if (getUserVisibleHint()) {
                startRequestTask();
            } else {
                requestData();
            }
        }

        /*
         * 只请求一次的 1. 获取重大提示 2. 获取个股新闻、公告、研报 3. 主力净流(资金) 4. 关联版块 5. 财务 6. 诊股
         */
        // 获取重大提示
        requestMajorTips(1);

        // 获取个股新闻/公告/诊股信息
        refreshListView();

        // 如果无缓存，向网络获取数据
        List<QuoteItemBean> listBeans = sparseArrayListInfos.get(currentInfoType, null);
        if (!(listBeans != null && listBeans.size() > 0) && isRequestingNews == false) {
            doRequestStockInfos();
        }

    }

    /**
     * 更新股票预警图标状态
     * */
    private void refreshImgAlertStatus() {
        // 更新预警图标状态
        if (currentGoodsId > 0) {
            if (StockAlertManagerV2.getInstance().isStockHasSetAlert(String.valueOf(currentGoodsId))) {
                imgAlertStatus.setImageResource(R.drawable.img_alert_quote_set);
            } else {
                imgAlertStatus.setImageResource(R.drawable.img_alert_quote_unset);
            }
        }
    }

    /**
     * 打开重大提示详情页
     * */
    private void gotoMajorTipDetail(MajorTipBean majorTipBean) {
        PageIntent intent = new PageIntent(this, InfoDetailTip.class);
        // // intent.setFlags(PageIntent.FLAG_PAGE_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putString(InfoDetailTip.EXTRA_KEY_TIP_DATE, majorTipBean.date);
        bundle.putInt(InfoDetailTip.EXTRA_KEY_TIP_ID, majorTipBean.tipId);
        bundle.putString(InfoDetailTip.EXTRA_KEY_TIP_TITLE, majorTipBean.title);
        bundle.putInt(InfoDetailTip.EXTRA_KEY_TIP_TYPE, majorTipBean.tipType);
        intent.setArguments(bundle);

        intent.setSupportAnimation(false);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    private class QuoteListAdapter extends BaseAdapter {

        private final int TYPE_COUNT = 2;
        private final int TYPE_STOCK_NEWS = 0;
        private final int TYPE_STOCK_QUESTIONS = 1;

        private List<QuoteItemBean> listBeans;
        private LayoutInflater inflater;

        private CountDownTimer mCountDownTimer;
        private QuizListViewlListener listener;
        private PageImpl page;

        public QuoteListAdapter(List<QuoteItemBean> listBeans, Context context, PageImpl page, QuizListViewlListener listener) {
            this.listBeans = listBeans;
            this.inflater = LayoutInflater.from(context);
            this.page = page;
            this.listener = listener;
        }

        @Override
        public int getCount() {
            if (listBeans.size() > 0) {
                if (currentInfoType == INFO_TYPE_QUESTION) {
                    return listBeans.size() >= 2 ? 2 : 1;
                } else {
                    if (listBeans.size() >= MIN_ITEM_COUNT) {
                        if (listBeans.size() >= MAX_ITEM_COUNT) {
                            return MAX_ITEM_COUNT;
                        } else {
                            return listBeans.size();
                        }
                    } else {
                        return MIN_ITEM_COUNT;
                    }
                }
            }

            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (position < listBeans.size()) {
                return listBeans.get(position);
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < listBeans.size()) {
                int itemType = listBeans.get(position).itemType;

                if (itemType == IDUtils.ID_STOCK_NEWS) {
                    return TYPE_STOCK_NEWS;
                } else if (itemType == IDUtils.ID_STOCK_QUESTION) {
                    return TYPE_STOCK_QUESTIONS;
                }
            }

            return TYPE_STOCK_NEWS;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup root) {
            int viewType = getItemViewType(position);

            if (viewType == TYPE_STOCK_NEWS) {
                StockNewsViewHolder vh = null;

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.page_quote_listitem_news, null, false);

                    vh = new StockNewsViewHolder(convertView);

                    convertView.setTag(vh);
                } else {
                    vh = (StockNewsViewHolder) convertView.getTag();
                }

                if (position < listBeans.size()) {
                    vh.layout.setVisibility(View.VISIBLE);
                    vh.viewBlank.setVisibility(View.GONE);

                    final QuoteItemBean bean = listBeans.get(position);

                    vh.tvTime.setText(DateUtils.formatInfoDate(bean.time, DateUtils.mFormatDayM_D));
                    vh.tvTitle.setText(bean.title);

                    if ((position == MAX_ITEM_COUNT - 1 && listBeans.size() == MAX_ITEM_COUNT) || (position == MIN_ITEM_COUNT - 1 && listBeans.size() == MIN_ITEM_COUNT)) {
                        vh.imgDivider.setVisibility(View.GONE);
                    } else {
                        vh.imgDivider.setVisibility(View.VISIBLE);
                    }

                    final int index = position;
                    vh.layout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 打开消息详情界面
                            ArrayList<Map<String, String>> listMessageMap = sparseArrayInfoDetails.get(currentInfoType);

                            switch (currentInfoType) {
                                case INFO_TYPE_NEWS:
                                    InfoDetailHome.gotoInfoDetail(page, listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_NEWS);
                                    break;
                                case INFO_TYPE_NOTICE:
                                    InfoDetailHome.gotoInfoDetail(page, listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_NOTICE);
                                    break;
                                case INFO_TYPE_REPORT:
                                    InfoDetailHome.gotoInfoDetail(page, listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_REPORT);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

                    List<String> mLstReaded = null;
                    if (currentInfoType == INFO_TYPE_NEWS) {
                        mLstReaded = sparseArrayReaded.get(INFO_TYPE_NEWS);
                    } else if (currentInfoType == INFO_TYPE_NOTICE) {
                        mLstReaded = sparseArrayReaded.get(INFO_TYPE_NOTICE);
                    } else if (currentInfoType == INFO_TYPE_REPORT) {
                        mLstReaded = sparseArrayReaded.get(INFO_TYPE_REPORT);
                    }
                    String md5Flag = MD5Util.md5(bean.url);
                    if (mLstReaded != null && mLstReaded.contains(md5Flag)) {
                        vh.tvTitle.setTextColor(getResources().getColor(R.color.t3));
                    } else {
                        vh.tvTitle.setTextColor(getResources().getColor(R.color.t1));
                    }
                } else {
                    vh.layout.setVisibility(View.GONE);
                    vh.viewBlank.setVisibility(View.VISIBLE);
                }
            } else if (viewType == TYPE_STOCK_QUESTIONS) {
                ReplyViewHolder vh = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.page_quote_listitem_reply, root, false);

                    vh = new ReplyViewHolder(convertView);
                    convertView.setTag(vh);
                } else {
                    vh = (ReplyViewHolder) convertView.getTag();
                }

                // 初始View
                initStatusView(vh, listItems.get(position), position);

                // 分隔线
                if (position == getCount() - 1) {
                    vh.dividerLayout.setVisibility(View.GONE);
                } else {
                    vh.dividerLayout.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        /**
         * 个股消息（新闻、公告、研报）
         * */
        private class StockNewsViewHolder {
            public View layout, viewBlank;

            public TextView tvTime, tvTitle;
            public View imgDivider;

            public StockNewsViewHolder(View view) {
                layout = view.findViewById(R.id.page_quote_listitem_layout);
                viewBlank = view.findViewById(R.id.page_quote_listitem_blank);

                tvTime = (TextView) view.findViewById(R.id.page_quote_listitem_tv_time);
                tvTitle = (TextView) view.findViewById(R.id.page_quote_listitem_tv_title);
                imgDivider = view.findViewById(R.id.page_quote_listitem_img_divider);
            }
        }

        private class ReplyViewHolder {
            // 问
            public RelativeLayout askLayout;
            public TextView askNameView;
            public TextView askDateView;
            public TextView askcontentView;

            // 回复
            public View replyLayout;
            public ImageView headIV;
            public TextView replyNameView;
            public TextView replyDateView;
            public View levLayout;
            public ImageView[] levViewArr;

            // 回复内容
            public TextView replycontentView;

            // 语音
            public View voiceLayout;
            public ImageView voiceView;
            public TextView voidTimeView;

            // 等待或关闭的状态
            public TextView replyStateView;
            // 正在回复
            public View replyOnStateLayout;
            public ImageView rePlyingHeadIV;
            public TextView replyingNameView;
            // 回复成功，评价
            public View commentStateLayout;
            public View goodView;
            public View wellView;
            public View normalView;
            public View badView;

            // 分隔线
            public View dividerLayout;

            public ReplyViewHolder(View convertView) {
                // 问
                askLayout = (RelativeLayout) convertView.findViewById(R.id.askLayout);
                askNameView = (TextView) convertView.findViewById(R.id.askNameView);
                askDateView = (TextView) convertView.findViewById(R.id.askDateView);
                askcontentView = (TextView) convertView.findViewById(R.id.askcontentView);

                // 回复
                replyLayout = convertView.findViewById(R.id.replyLayout);
                headIV = (ImageView) convertView.findViewById(R.id.headIV);
                replyNameView = (TextView) convertView.findViewById(R.id.replyNameView);
                replyDateView = (TextView) convertView.findViewById(R.id.replyDateView);
                levLayout = convertView.findViewById(R.id.levLayout);
                levViewArr = new ImageView[4];
                levViewArr[0] = (ImageView) convertView.findViewById(R.id.levView1);
                levViewArr[1] = (ImageView) convertView.findViewById(R.id.levView2);
                levViewArr[2] = (ImageView) convertView.findViewById(R.id.levView3);
                levViewArr[3] = (ImageView) convertView.findViewById(R.id.levView4);

                // 回复内容
                replycontentView = (TextView) convertView.findViewById(R.id.replycontentView);

                // 语音
                voiceLayout = convertView.findViewById(R.id.voiceLayout);
                voiceView = (ImageView) convertView.findViewById(R.id.voiceView);
                voidTimeView = (TextView) convertView.findViewById(R.id.voidTimeView);

                // 等待或关闭的状态
                replyStateView = (TextView) convertView.findViewById(R.id.replyStateView);
                // 正在回复
                replyOnStateLayout = convertView.findViewById(R.id.replyOnStateLayout);
                rePlyingHeadIV = (ImageView) convertView.findViewById(R.id.rePlyingHeadIV);
                replyingNameView = (TextView) convertView.findViewById(R.id.replyingNameView);
                // 回复成功，评价
                commentStateLayout = convertView.findViewById(R.id.commentStateLayout);
                goodView = convertView.findViewById(R.id.goodView);
                wellView = convertView.findViewById(R.id.wellView);
                normalView = convertView.findViewById(R.id.normalView);
                badView = convertView.findViewById(R.id.badView);

                // 分隔线
                dividerLayout = convertView.findViewById(R.id.dividerLayout);
            }
        }

        private void initStatusView(ReplyViewHolder view, final QuoteItemBean info, final int pos) {
            if (info.quizItem != null) {
                final QuizContentInfo item = info.quizItem;
                int left = view.askLayout.getPaddingLeft();
                int right = view.askLayout.getPaddingRight();
                int top = view.askLayout.getPaddingTop();
                int bottom = view.askLayout.getPaddingBottom();

                if (item.isMyQuestion()) {
                    // 我的提问
                    view.askLayout.setBackgroundResource(R.drawable.img_quiz_txt_self_bg);
                    view.askNameView.setText(Util.getResourcesString(R.string.quiz_my_question));
                } else {
                    // 别人的提问
                    view.askLayout.setBackgroundResource(R.drawable.img_quiz_txt_other_bg);
                    view.askNameView.setText(item.getOwner().getNickName());
                }
                view.askLayout.setPadding(left, top, right, bottom);

                view.askDateView.setText(DateUtils.formatQuizCommitTime(item.getCommitTime()));
                view.askcontentView.setText(item.getContent());
                // 加事件
                LinkManager.addStockLinkToTv(page, view.askcontentView);

                // 答
                if ((item.getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED || item.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE) && item.getReplier() != null && item.getAnswer() != null) {
                    // 回复
                    // 回复内容
                    view.replyLayout.setVisibility(View.VISIBLE);
                    // 等待和无人回复
                    view.replyNameView.setVisibility(View.GONE);
                    // 正在回复
                    view.replyOnStateLayout.setVisibility(View.GONE);
                    // 评价
                    view.commentStateLayout.setVisibility(View.GONE);

                    Util.loadHeadIcon(view.headIV, item.getReplier().getId() + "", item.getReplier().getIcon());
                    view.headIV.setOnClickListener(new OnClickEffectiveListener() {

                        @Override
                        public void onClickEffective(View v) {
                            if (listener != null) {
                                listener.onClickHeadIcon(pos);
                            }
                        }
                    });

                    view.replyNameView.setText(item.getReplier().getNick());
                    view.replyDateView.setText(DateUtils.formatQuizCommitTime(item.getAnswerTime()));

                    // 等级
                    if (item.isMyLatestQuestion() && item.getStatus() != QuizContentInfo.STATUS_ASK_APPRAISED) {
                        view.levLayout.setVisibility(View.GONE);
                    } else {
                        view.levLayout.setVisibility(View.VISIBLE);
                        if (view.levViewArr != null) {
                            for (int i = 0; i < view.levViewArr.length; i++) {
                                if (i < item.getAppraiseLevel()) {
                                    view.levViewArr[i].setSelected(true);
                                } else {
                                    view.levViewArr[i].setSelected(false);
                                }
                            }
                        }
                    }

                    // 内容
                    if (item.getAnswer().getType() == QuizContentInfo.CONTENT_TYPE_TEXT) {
                        // 文本
                        view.voiceLayout.setVisibility(View.GONE);
                        view.replycontentView.setVisibility(View.VISIBLE);
                        view.replycontentView.setText(item.getAnswer().getContent());
                        // 加事件
                        LinkManager.addStockLinkToTv(page, view.replycontentView);
                    } else {
                        // 语音
                        view.voiceLayout.setVisibility(View.VISIBLE);
                        view.replycontentView.setVisibility(View.GONE);
                        view.voidTimeView.setText(item.getAnswer().getVoiceTime());
                        if (item.isPlaying()) {
                            // 播放
                            view.voiceView.setImageResource(R.drawable.anim_quiz_voice);
                            AnimationDrawable animationDrawable = (AnimationDrawable) view.voiceView.getDrawable();
                            animationDrawable.start();
                        } else {
                            view.voiceView.setImageResource(R.drawable.img_voice3);
                        }

                        view.voiceLayout.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onPlayVoice(pos);
                                }
                            }
                        });
                    }

                    // 是否评价
                    if (item.isMyLatestQuestion() && item.getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE) {
                        // 我的提问，且要评价
                        view.commentStateLayout.setVisibility(View.VISIBLE);
                        view.goodView.setOnClickListener(new OnClickEffectiveListener() {

                            @Override
                            public void onClickEffective(View v) {
                                if (listener != null) {
                                    listener.onAppraise(item.getId(), QuizListViewlListener.COMMENT_GOOD);
                                }
                            }
                        });

                        view.wellView.setOnClickListener(new OnClickEffectiveListener() {

                            @Override
                            public void onClickEffective(View v) {
                                if (listener != null) {
                                    listener.onAppraise(item.getId(), QuizListViewlListener.COMMENT_WELL);
                                }
                            }
                        });

                        view.normalView.setOnClickListener(new OnClickEffectiveListener() {

                            @Override
                            public void onClickEffective(View v) {
                                if (listener != null) {
                                    listener.onAppraise(item.getId(), QuizListViewlListener.COMMENT_NORAML);
                                }
                            }
                        });

                        view.badView.setOnClickListener(new OnClickEffectiveListener() {

                            @Override
                            public void onClickEffective(View v) {
                                if (listener != null) {
                                    listener.onAppraise(item.getId(), QuizListViewlListener.COMMENT_BAD);
                                }
                            }
                        });
                    } else {
                        view.commentStateLayout.setVisibility(View.GONE);
                    }
                } else {
                    // 无回复
                    // 回复内容
                    view.replyLayout.setVisibility(View.GONE);
                    // 评价
                    view.commentStateLayout.setVisibility(View.GONE);

                    // 等待和无人回复
                    if (item.getStatus() == QuizContentInfo.STATUS_ASK_WAIT || item.getStatus() == QuizContentInfo.STATUS_ASK_WAIT2) {
                        // 等待
                        view.replyStateView.setVisibility(View.VISIBLE);
                        // 正在回复
                        view.replyOnStateLayout.setVisibility(View.GONE);
                        view.replyStateView.setTextColor(Util.getResourcesColor(R.color.c3));
                        view.replyStateView.setText(Util.getResourcesString(R.string.quiz_status_wait));
                        setCountDownTimer(view.replyStateView, item.getCommitTime(), pos);
                    } else if (item.getStatus() == QuizContentInfo.STATUS_ASK_CLOSE) {
                        // 关闭
                        view.replyStateView.setVisibility(View.VISIBLE);
                        // 正在回复
                        view.replyOnStateLayout.setVisibility(View.GONE);
                        view.replyStateView.setTextColor(Util.getResourcesColor(R.color.t4));
                        view.replyStateView.setText(R.string.quiz_status_close);
                    } else if (item.getStatus() == QuizContentInfo.STATUS_ASK_ON) {
                        // 正在回复
                        view.replyStateView.setVisibility(View.GONE);
                        view.replyOnStateLayout.setVisibility(View.VISIBLE);
                        Util.loadHeadIcon(view.rePlyingHeadIV, item.getReplier().getId() + "", item.getReplier().getIcon());
                        view.replyingNameView.setText(item.getReplier().getNick());
                    }
                }
            }
        }

        private void setCountDownTimer(final TextView tv, int createTime, final int pos) {
            long tRemainTime = QuizGlobalData.getGlobalData().getQustionLifeTime() - (DateUtils.getTimeStamp() / 1000 - DataModule.G_LOCAL_SERVER_TIME_GAP - createTime);

            if (tRemainTime > 0) {
                tv.setText(Util.getResourcesString(R.string.quiz_status_wait) + DateUtils.second2MSLable(tRemainTime));
                if (mCountDownTimer == null) {
                    mCountDownTimer = new CountDownTimer(tRemainTime * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tv.setText(Util.getResourcesString(R.string.quiz_status_wait) + DateUtils.second2MSLable(millisUntilFinished / 1000));
                        }

                        @Override
                        public void onFinish() {
                            if (listener != null) {
                                listener.onQuestionClose(pos);
                            }
                        }
                    }.start();
                }
            } else {
                if (listener != null) {
                    listener.onQuestionClose(pos);
                }
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.page_quote_img_alert:
                gotoAlertConfigPage();
                break;
            case R.id.page_quote_img_quize:
                // 如果未登录，提示登录
                if (!isLogined()) {
                    showTip("请先登录再使用该功能");
                    return;
                }
                
                // 启动问题页面
                startAskPage();
                break;
            case R.id.page_quote_img_optional:
                addOrRemoveZxg();
                break;
            default:
                break;
        }
    }

    // 启动问题页面
    private void startAskPage() {
        if (startAskPageManager == null) {
            startAskPageManager = new StartAskPageManager();
        }
        startAskPageManager.startAskPage(this, null, false, currentGoods);
    }

    /**
     * 从所有自选中添加或删除
     * */
    private void addOrRemoveZxg() {
        final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();
        ArrayList<Goods> lstGoods = new ArrayList<Goods>();
        lstGoods.add(currentGoods);
        final OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
        final int goodsId = currentGoods.getGoodsId();
        if (isHasAddZxg) {
            if (!bIsLogin) {
                String tAddtype = OptionalInfo.TYPE_DEFAULT;

                if (optionalInfo.delGoods(tAddtype, goodsId)) {
                    optionalInfo.save(getDBHelper());
                    showTip("删除自选成功");

                    isHasAddZxg = false;
                    imgOptional.setImageResource(R.drawable.img_quote_option_add);
                }
            } else {
                String tAddType = optionalInfo.TYPE_KEY_ALL;

                delZXG(tAddType, lstGoods, new OnOperateZXGListener() {
                    @Override
                    public void onOperate(boolean isSuccess, String msg) {
                        if (isSuccess) {
                            String tAddtype = OptionalInfo.TYPE_DEFAULT;
                            if (optionalInfo.delGoods(tAddtype, goodsId)) {
                                optionalInfo.save(getDBHelper());
                                showTip("删除自选成功");

                                isHasAddZxg = false;
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

                    isHasAddZxg = true;
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

                                isHasAddZxg = true;
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
     * 打开预警设置界面
     * */
    private void gotoAlertConfigPage() {
        // 登录状态下，才打开预警设置界面，否则，提示用户登录
        boolean isLogined = DataModule.getInstance().getUserInfo().isLogined();
        if (isLogined) {
            // 判断预警单例列表是否初始化成功，如不成功，不允许进个股预警
            if (StockAlertManagerV2.getInstance().isCacheHasData()) {
                // 判断预警个股数量是否已达到上限，是否允许继续添加预警
                if (StockAlertManagerV2.getInstance().isSetAlertAllowed(String.valueOf(currentGoods.getGoodsId()))) {
                    // 打开预警设置界面
                    PageIntent intent = new PageIntent(QuoteStockPage.this, SetAlertPage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(SetAlertPage.KEY_STOCK_NAME, currentGoods.getGoodsName());
                    bundle.putString(SetAlertPage.KEY_STOCK_CODE, currentGoods.getGoodsCode());
                    bundle.putInt(SetAlertPage.KEY_STOCK_ID, currentGoods.getGoodsId());
                    bundle.putString(SetAlertPage.KEY_STOCK_PRICE, currentPrice);
                    bundle.putString(SetAlertPage.KEY_STOCK_ZDF, currentZdf);
                    bundle.putString(SetAlertPage.KEY_STOCK_ZD, currentZd);
                    intent.setArguments(bundle);
                    intent.setSupportAnimation(true);
                    intent.needPringLog(true);

                    startPage(DataModule.G_CURRENT_FRAME, intent);
                } else {
                    // 提示预警设置数量已达上限，不允许继续添加预警
                    showTip("预警个股数量已达到上限");
                }
            } else {
                showTip("预警服务器繁忙");
            }
        } else {
            showLoginDialog();
        }
    }

    /**
     * 打开新闻更多界面
     * */
    private void gotoMoreNewsPage() {
        if (currentGoodsId > 0) {
            PageIntent intent = new PageIntent(this, MoreNewsPage.class);

            Bundle extras = new Bundle();
            extras.putInt(MoreNewsPage.EXTRA_KEY_GOODS_ID, currentGoodsId);
            extras.putInt(MoreNewsPage.EXTRA_KEY_NEWS_TYPE, currentInfoType);
            extras.putString(MoreNewsPage.EXTRA_KEY_GOODS_NAME, currentGoods.getGoodsName());
            intent.setArguments(extras);
            intent.setSupportAnimation(true);

            startPage(DataModule.G_CURRENT_FRAME, intent);
        }
    }

    /**
     * 跳转到个股问答列表
     * */
    private void gotoMoreQuizePage() {
        PageIntent intent = new PageIntent(this, MoreQuizePage.class);

        Bundle extras = new Bundle();
        extras.putString(MoreQuizePage.EXTRA_KEY_GOODS_NAME, currentGoods.getGoodsName());
        extras.putString(MoreQuizePage.EXTRA_KEY_GOODS_CODE, currentGoods.getGoodsCode());
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    private void showLoginDialog() {
        final CustomDialog dialog = new CustomDialog(getContext(), new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {
                LoginPage.gotoLogin(QuoteStockPage.this, QUOTE_PAGE_CODE);
            }

            @Override
            public void onCancelBtnClicked() {}
        });

        dialog.setCustomMessage("请先登录后再设置预警");
        dialog.setButtonText("立即登录", "取消");
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.show();
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

            if (QuoteHome.currentPeriod == TYPE_60MINUTE) {
                period60m.setText("60分钟");
            } else if (QuoteHome.currentPeriod == TYPE_30MINUTE) {
                period60m.setText("30分钟");
            } else if (QuoteHome.currentPeriod == TYPE_15MINUTE) {
                period60m.setText("15分钟");
            }
        }
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
        // pw.showAsDropDown(period60m, -10, 24); // 显示在指定控件的下方，左侧尝试对齐，上边缘与指定控件下边缘紧贴
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
                refreshPeriodCheckedStatus();
                requestHistoryTrend(KLinePage.KLINE_REFRESH_TYPE_CHANGE_PERIOD);
            }
        });
    }

    /**
     * 获取个股行情数据
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
            reqFileds.add(GoodsParams.SYL); // 市盈率
            reqFileds.add(GoodsParams.SJL); // 市净率

            reqFileds.add(GoodsParams.VOLUME); // 成交量
            reqFileds.add(GoodsParams.JL); // 净流
            reqFileds.add(GoodsParams.ZSZ); // 总市值

            reqFileds.add(GoodsParams.AMOUNT); // 成交额
            reqFileds.add(GoodsParams.LB); // 量比
            reqFileds.add(GoodsParams.LTSZ); // 流通市值

            reqFileds.add(GoodsParams.SUSPENSION); // 停牌信息

            DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_QUOTATION));
            pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));
            requestQuote(pkg, IDUtils.DynaValueData);
        }
    }

    /**
     * 获取重大提示列表
     * */
    private void requestMajorTips(int size) {
        if (currentGoodsId > 0) {
            JSONObject jsObj = null;

            try {
                jsObj = new JSONObject();

                jsObj.put("goods_id", currentGoodsId);
                jsObj.put(KEY_SIZE, size);
                jsObj.put(KEY_TOKEN, getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsObj, IDUtils.ID_MAJOR_TIPS);

            // 3秒种后，如果重大提示内容为空，就不显示重大提示布局
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (TextUtils.isEmpty(tvTips.getText().toString().trim())) {
                        layoutTips.setVisibility(View.GONE);
                    }
                }
            }, 3000);
        }
    }

    /**
     * 获取停牌信息
     * */
    private void requestSuspensionInfo() {
        if (currentGoodsId > 0) {
            JSONObject jsObj = null;

            try {
                jsObj = new JSONObject();

                jsObj.put("goods_id", currentGoodsId);
                // jsObj.put("data_stamp", DateUtils.getTimeStamp() + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsObj, IDUtils.ID_SUSPENSION);
        }
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

                LogUtil.easylog("ENTER updateQutation->postInvalidate->good:" + currentGoods.getGoodsId());
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

                    // 获取停牌标志
                    boolean isSuspension = false;
                    int indexSuspension = listReqFieldIds.indexOf(GoodsParams.SUSPENSION);
                    String suspensionFlag = listReqFieldValues.get(indexSuspension);
                    if ("1".equals(suspensionFlag)) {
                        isSuspension = true;
                        // 该支股票停牌，获取停牌信息
                        requestSuspensionInfo();
                    }

                    if (!isSuspension) {
                        // 获取并刷新最新价、涨跌、涨跌幅 --- begin
                        int indexZXJ = listReqFieldIds.indexOf(GoodsParams.ZXJ);
                        int indexZDF = listReqFieldIds.indexOf(GoodsParams.ZDF);
                        int indexZD = listReqFieldIds.indexOf(GoodsParams.ZHANGDIE);

                        String fieldValuePrice = listReqFieldValues.get(indexZXJ);
                        currentZd = listReqFieldValues.get(indexZD);
                        String fieldValueZdf = listReqFieldValues.get(indexZDF);

                        int color = getZDPColor(FontUtils.getColorByZD(currentZd));
                        currentPrice = DataUtils.getPrice(fieldValuePrice);
                        String zd = DataUtils.getZD(currentZd);
                        currentZdf = DataUtils.getZDF(fieldValueZdf);

                        priceLayer.setPriceText(getIndexDisplayValue(currentPrice, isSuspension));
                        priceLayer.setPriceTextColor(color);
                        priceLayer.setZDText(getIndexDisplayValue(zd, isSuspension));
                        priceLayer.setZDTextColor(color);
                        priceLayer.setZFText(getIndexDisplayValue(currentZdf, isSuspension));
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
                    }

                    int indexOpen = listReqFieldIds.indexOf(GoodsParams.OPEN);
                    int indexHigh = listReqFieldIds.indexOf(GoodsParams.HiGH);
                    int indexLow = listReqFieldIds.indexOf(GoodsParams.LOW);

                    int indexHsl = listReqFieldIds.indexOf(GoodsParams.HSL);
                    int indexSyl = listReqFieldIds.indexOf(GoodsParams.SYL);
                    int indexSjl = listReqFieldIds.indexOf(GoodsParams.SJL);

                    int indexVolumn = listReqFieldIds.indexOf(GoodsParams.VOLUME);
                    int indexJl = listReqFieldIds.indexOf(GoodsParams.JL);
                    int indexZsz = listReqFieldIds.indexOf(GoodsParams.ZSZ);

                    int indexAmount = listReqFieldIds.indexOf(GoodsParams.AMOUNT);
                    int indexLb = listReqFieldIds.indexOf(GoodsParams.LB);
                    int indexLtsz = listReqFieldIds.indexOf(GoodsParams.LTSZ);

                    String fieldValueOpen = listReqFieldValues.get(indexOpen);
                    String fieldValueHigh = listReqFieldValues.get(indexHigh);
                    String fieldValueLow = listReqFieldValues.get(indexLow);

                    String fieldValueHsl = listReqFieldValues.get(indexHsl);
                    String fieldValueSyl = listReqFieldValues.get(indexSyl);
                    String fieldValueSjl = listReqFieldValues.get(indexSjl);

                    String fieldValueVolumn = listReqFieldValues.get(indexVolumn);
                    String fieldValueJl = listReqFieldValues.get(indexJl);
                    String fieldValueZsz = listReqFieldValues.get(indexZsz);

                    String fieldValueAmount = listReqFieldValues.get(indexAmount);
                    String fieldValueLb = listReqFieldValues.get(indexLb);
                    String fieldValueLtsz = listReqFieldValues.get(indexLtsz);

                    mKVOpen.setText(getIndexDisplayValue(DataUtils.getPrice(fieldValueOpen), isSuspension));
                    mKVHigh.setText(getIndexDisplayValue(DataUtils.getPrice(fieldValueHigh), isSuspension));
                    mKVLow.setText(getIndexDisplayValue(DataUtils.getPrice(fieldValueLow), isSuspension));

                    String syl = getIndexDisplayValue(DataUtils.getSYL(fieldValueSyl), isSuspension);
                    String sjl = getIndexDisplayValue(DataUtils.getSJL(fieldValueSjl), isSuspension);
                    mKVHS.setText(getIndexDisplayValue(DataUtils.getHSL(fieldValueHsl), isSuspension));
                    mKVSY.setText(syl);
                    mKVSJ.setText(sjl);
                    if (quoteFinalcialPage != null) {
                        quoteFinalcialPage.setSyl(syl);
                        quoteFinalcialPage.setSjl(sjl);
                    }

                    if (DataUtils.IsZS(currentGoods.getGoodsId())) {
                        mKVCJL.setText(getIndexDisplayValue(DataUtils.formatVolume(fieldValueVolumn), isSuspension));
                    } else {
                        long val = Long.valueOf(fieldValueVolumn);
                        val = val / 100;
                        mKVCJL.setText(getIndexDisplayValue(DataUtils.formatVolume(val), isSuspension));
                    }
                    mKVJL.setText(getIndexDisplayValue(DataUtils.formatJL(fieldValueJl, DataUtils.mDecimalFormat1), isSuspension));
                    mKVZZ.setText(getIndexDisplayValue(DataUtils.formatSZ(fieldValueZsz), isSuspension));

                    mKVCJE.setText(getIndexDisplayValue(DataUtils.formatAmount(fieldValueAmount), isSuspension));
                    mKVLB.setText(getIndexDisplayValue(DataUtils.formatLb(fieldValueLb), isSuspension));
                    mKVLZ.setText(getIndexDisplayValue(DataUtils.formatSZ(fieldValueLtsz), isSuspension));

                    cvBasic.postInvalidate();
                    cvLeftBottom.postInvalidate();
                    cvRightBottom.postInvalidate();

                    LogUtil.easylog("END updateQutation->postInvalidate->good:" + currentGoods.getGoodsId());
                }
            }
        } else if (pkg instanceof QuizRelatePackage && isRequestingNews) {
            isRequestingNews = false;
            layoutLoading.setVisibility(View.GONE);

            QuizRelatePackage ddpkg = (QuizRelatePackage) pkg;
            int id = ddpkg.getRequestType();

            if (id == REQUEST_TYPE_RELATIVE_QUIZE) {
                QuizRalate_Reply reply = ddpkg.getResponse();

                List<Item> listItems = reply.getItemsList();

                List<QuoteItemBean> listTempInfos = sparseArrayListInfos.get(currentInfoType, null);
                if (listTempInfos == null) {
                    listTempInfos = new ArrayList<QuoteItemBean>();
                    sparseArrayListInfos.put(currentInfoType, listTempInfos);
                }

                if (listItems != null && listItems.size() > 0) {
                    for (int i = 0; i < listItems.size(); i++) {
                        if (listTempInfos != null && listTempInfos.size() > 2) {
                            break;
                        }

                        Item item = listItems.get(i);

                        listTempInfos.add(new QuoteItemBean(ID_STOCK_QUESTION, item));
                    }
                }
            }

            refreshListView();
        }
    }

    public void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == IDUtils.ID_STOCK_NEWS && isRequestingNews) {
            isRequestingNews = false;
            layoutLoading.setVisibility(View.GONE);

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            int reqInfoType = INFO_TYPE_NEWS;

            if (mc != null && mc.getMsgData() != null) {
                String msgData = mc.getMsgData();

                try {
                    JSONObject jsObj = JSONObject.parseObject(msgData);

                    if (jsObj != null && jsObj.containsKey(KEY_CLS) && jsObj.containsKey(KEY_END) && jsObj.containsKey(KEY_NEWS)) {
                        String clsName = jsObj.getString(KEY_CLS);
                        int end = jsObj.getIntValue(KEY_END);
                        JSONArray newsArr = jsObj.getJSONArray(KEY_NEWS);

                        if (newsArr != null && newsArr.size() > 0) {
                            int itemCount = newsArr.size();

                            if (clsName.equals("个股新闻")) {
                                reqInfoType = INFO_TYPE_NEWS;
                            } else if (clsName.equals("个股公告")) {
                                reqInfoType = INFO_TYPE_NOTICE;
                            } else if (clsName.equals("个股研报")) {
                                reqInfoType = INFO_TYPE_REPORT;
                            } else {
                                reqInfoType = INFO_TYPE_NEWS;
                            }

                            /*
                             * 切换TAB时，只有缓存中无数据才会网络获取，自动刷新时，始终会清空缓存，这两种情况下，网络返回时，缓存中当前TAB都没有数据 。
                             * 加载更多时，缓存中当前TAB有数据。
                             * 定义一个List，如果缓存中无数据，new一个对象，并添加到map中，如果缓存中有数据，获取对象的引用。
                             * 然后解析数据并添加到List中，因为map指向该list，也就更新了map
                             */
                            List<QuoteItemBean> listTempInfos = sparseArrayListInfos.get(currentInfoType, null);
                            if (listTempInfos == null) {
                                listTempInfos = new ArrayList<QuoteItemBean>();
                                sparseArrayListInfos.put(currentInfoType, listTempInfos);
                            }

                            /*
                             * 个股消息跳转信息 如果当前消息类型对应的有数据，在原有基础上添加，如果当前消息对应的没有数据，新建一个，在新建的数据上添加
                             */
                            ArrayList<Map<String, String>> listInfoDetails = sparseArrayInfoDetails.get(currentInfoType, null);
                            if (listInfoDetails == null) {
                                listInfoDetails = new ArrayList<Map<String, String>>();
                                sparseArrayInfoDetails.put(currentInfoType, listInfoDetails);
                            }

                            for (int i = 0; i < newsArr.size(); i++) {
                                if (listTempInfos != null && listTempInfos.size() > MAX_ITEM_COUNT) {
                                    break;
                                }

                                JSONObject newsObj = newsArr.getJSONObject(i);

                                String time = newsObj.getString(KEY_PT);
                                String title = newsObj.getString(KEY_TITLE);
                                String url = newsObj.getString(KEY_CONTENT_URL);
                                String from = newsObj.getString(KEY_FROM);
                                int sortId = newsObj.getIntValue("sortid");

                                String sortcls = null;
                                switch (currentInfoType) {
                                    case INFO_TYPE_NEWS:
                                        sortcls = "个股新闻";
                                        break;
                                    case INFO_TYPE_NOTICE:
                                        sortcls = "个股公告";
                                        break;
                                    case INFO_TYPE_REPORT:
                                        sortcls = "个股研报";
                                        break;
                                    default:
                                        break;
                                }

                                Map<String, String> map = new HashMap<String, String>();
                                map.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                                map.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                                map.put(InfoDetailPage.EXTRA_KEY_TIME, time);
                                map.put(InfoDetailPage.EXTRA_KEY_FROM, from);
                                map.put(InfoDetailPage.EXTRA_KEY_SORTCLS, sortcls);
                                map.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                                map.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                                listInfoDetails.add(map);

                                QuoteItemBean bean = new QuoteItemBean(ID_STOCK_NEWS, url, title, time, from, sortcls, end, itemCount, sortId);
                                listTempInfos.add(bean);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    tvEmpty.setText("加载失败，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);

                    layoutLoadMore.setVisibility(View.GONE);
                    footerDivider.setVisibility(View.GONE);
                    return;
                }
            }

            // 只有当发送网络时的item与当前显示的item是同一个时，才刷新ListView显示
            if (reqInfoType == currentInfoType) {
                if (currentInfoType == INFO_TYPE_NEWS) {
                    String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NEWS, null);
                    if (aryReaded != null) {
                        List<String> mLstReaded = Arrays.asList(aryReaded);
                        sparseArrayReaded.put(INFO_TYPE_NEWS, mLstReaded);
                    }
                } else if (currentInfoType == INFO_TYPE_NOTICE) {
                    String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NOTICE, null);
                    if (aryReaded != null) {
                        List<String> mLstReaded = Arrays.asList(aryReaded);
                        sparseArrayReaded.put(INFO_TYPE_NOTICE, mLstReaded);
                    }
                } else if (currentInfoType == INFO_TYPE_REPORT) {
                    String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_REPORT, null);
                    if (aryReaded != null) {
                        List<String> mLstReaded = Arrays.asList(aryReaded);
                        sparseArrayReaded.put(INFO_TYPE_REPORT, mLstReaded);
                    }
                }

                refreshListView();
            }

        } else if (id == IDUtils.ID_MAJOR_TIPS) {
            // 获取到重大提示
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject obj = JSONObject.parseObject(msgData);

                if (obj == null)
                    return;

                int resultCode = obj.getIntValue(KEY_RESULT);
                if (resultCode == 0) {
                    JSONArray arrayResult = obj.getJSONArray(KEY_DATA);
                    if (arrayResult != null && arrayResult.size() > 0) {
                        JSONObject objTip = arrayResult.getJSONObject(0);
                        if (objTip == null)
                            return;

                        String tipDate = objTip.getString("date");
                        int tipId = objTip.getIntValue("id");
                        String tipTitle = objTip.getString("title");
                        int tipType = objTip.getIntValue("type");

                        String tipTime = tipDate;
                        if (!TextUtils.isEmpty(tipDate) && tipDate.length() == 8) {
                            tipTime = tipDate.substring(4, 6) + "-" + tipDate.substring(6, 8);
                        }

                        String tipContent = tipTitle + "  (" + tipTime + ")";

                        tvTips.setText(tipContent);

                        majorTipBean = new MajorTipBean(tipDate, tipId, tipType, tipTitle);

                        layoutTips.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            } catch (Exception e) {
            }

            layoutTips.setVisibility(View.GONE);

        } else if (id == IDUtils.ID_SUSPENSION) {
            // 获取到停牌信息
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();
            // {"data":[{"B":"20151027 0930","E":"","R":"临时停牌","S":1300431,"suspension":true}],"data_stamp":"65c6a3bce641c05c67959cb0ea9906c1","md5":"","message":"","result":0}

            try {
                JSONObject obj = JSONObject.parseObject(msgData);

                if (obj == null)
                    return;

                int resultCode = obj.getIntValue(KEY_RESULT);
                if (resultCode == 0) {
                    JSONArray arrayResult = obj.getJSONArray(KEY_DATA);
                    if (arrayResult != null && arrayResult.size() > 0) {
                        JSONObject objTip = arrayResult.getJSONObject(0);
                        if (objTip == null)
                            return;

                        // int goodsId = objTip.getIntValue("S");
                        boolean isSuspension = objTip.getBooleanValue("suspension");
                        // String suspensionBeginTime = objTip.getString("B");
                        // String suspensionEndTime = objTip.getString("E");
                        String suspensionReason = objTip.getString("R");

                        if (isSuspension == true && !TextUtils.isEmpty(suspensionReason)) {
                            // 加载并显示停牌信息
                            tvSuspension.setText(suspensionReason);
                            tvSuspension.setVisibility(View.VISIBLE);

                            // 更新PriceLayer显示
                            priceLayer.setPriceText("停 牌");
                            priceLayer.setPriceTextSize(FontUtils.dip2px(getContext(), 30));
                            priceLayer.setPriceTextColor(getResources().getColor(R.color.c1));
                            priceLayer.setZDText("—");
                            priceLayer.setZFText("—");
                            cvPrice.postInvalidate();

                            return;
                        }
                    }
                }
            } catch (Exception e) {
            }

            // 隐藏停牌信息
            tvSuspension.setVisibility(View.GONE);
        }
    }

    /**
     * 设置指标的值
     * */
    private String getIndexDisplayValue(String value, boolean isSuspension) {
        // 如果没有停牌，直接显示
        // 如果停牌，且为值为0， 显示为横杠，如果值不为0，显示值
        // 即，如果是停牌且值为0，显示为横杠，其它情况下，显示本值

        float tValue = -1f;
        try {
            String str = value;
            if (!TextUtils.isEmpty(value) && value.endsWith("%")) {
                str = value.replaceAll("\\%s+", "");
                str = str.substring(0, str.length() - 1);
            }
            tValue = Float.parseFloat(str);
        } catch (Exception e) {
        }

        if (isSuspension && tValue == 0f) {
            value = "—";
        }

        return value;
    }

    /**
     * 获取个股消息 (新闻、公告、研报)
     * */
    private void requestStockInfos(int infoType) {
        if (currentGoodsId > 0) {
            if (infoType == INFO_TYPE_NEWS || infoType == INFO_TYPE_NOTICE || infoType == INFO_TYPE_REPORT) {
                JSONObject jsObj = new JSONObject();

                String cls = null;
                if (infoType == INFO_TYPE_NEWS) {
                    cls = "个股新闻";
                } else if (infoType == INFO_TYPE_NOTICE) {
                    cls = "个股公告";
                } else if (infoType == INFO_TYPE_REPORT) {
                    cls = "个股研报";
                }

                try {
                    jsObj.put(KEY_CLS, cls);
                    jsObj.put(KEY_DIRECTION, 1);
                    jsObj.put(KEY_SORTID, getLastInfoSortId());
                    jsObj.put(KEY_STOCK, String.valueOf(currentGoodsId));
                    jsObj.put("size", 12);
                    jsObj.put(KEY_TOKEN, getToken());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                requestInfo(jsObj, IDUtils.ID_STOCK_NEWS);
            }
        }
    }

    /**
     * 获取个股相关问答列表
     * */
    private void requestStockRelativeQuize() {
        // 1. 构造QuoteHead，传入request type
        QuoteHead quoteHead = new QuoteHead(REQUEST_TYPE_RELATIVE_QUIZE);
        // 2. 传入QuoteHead，构造Package
        QuizRelatePackage pkg = new QuizRelatePackage(quoteHead);
        // 3. 构造Request并设置参数
        QuizRalate_Request request = QuizRalate_Request.newBuilder().setTokenId(getToken()).setStocks(currentGoods.getGoodsCode()).setNeedCount(3).setLastId(0).build();
        // 4. 将Request设置到Package中
        pkg.setRequest(request);
        // 5. 发送请求requestQuote
        requestQuote(pkg, ID_STOCK_QUESTION);
    }

    private void doRequestStockInfos() {
        if (currentInfoType == INFO_TYPE_QUESTION) {
            requestStockRelativeQuize();
        } else {
            requestStockInfos(currentInfoType);
        }

        isRequestingNews = true;

        tvEmpty.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoadMore.setVisibility(View.GONE);
        footerDivider.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequestingNews && layoutLoading != null) {
                    isRequestingNews = false;

                    tvEmpty.setText("加载失败，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.GONE);
                    layoutLoadMore.setVisibility(View.GONE);
                    footerDivider.setVisibility(View.GONE);
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }

    /**
     * 获取最后一条消息的sortid
     * */
    private int getLastInfoSortId() {
        // 获取当前消息类型对应的消息列表
        List<QuoteItemBean> listBeans = sparseArrayListInfos.get(currentInfoType, null);

        if (listBeans != null && listBeans.size() > 0) {
            return listBeans.get(listBeans.size() - 1).sortId;
        }

        return 0;
    }

    /**
     * 使用缓存数据刷新ListView显示
     * */
    private void refreshListView() {
        // 刷新ListView需要显示的数据
        resetListDatas();

        // 刷新ListView显示
        adapter.notifyDataSetChanged();

        if (listItems.size() > 0) {
            tvEmpty.setVisibility(View.GONE);

            // 刷新查看更多的显示，显示的数据大于等于限制数据时显示，其它情况下不显示
            if (currentInfoType == INFO_TYPE_QUESTION) {
                if (listItems.size() > 2) {
                    layoutLoadMore.setVisibility(View.VISIBLE);
                    footerDivider.setVisibility(View.VISIBLE);
                } else {
                    layoutLoadMore.setVisibility(View.GONE);
                    footerDivider.setVisibility(View.GONE);
                }
            } else {
                if (listItems.size() > MAX_ITEM_COUNT) {
                    layoutLoadMore.setVisibility(View.VISIBLE);
                } else {
                    layoutLoadMore.setVisibility(View.GONE);
                }
                if (listItems.size() > MIN_ITEM_COUNT) {
                    footerDivider.setVisibility(View.VISIBLE);
                } else {
                    footerDivider.setVisibility(View.GONE);
                }
            }

            // 只有当前是个股问答且只有一条时显示
            if (currentInfoType == INFO_TYPE_QUESTION && listItems.size() == 1) {
                viewQuizBlank.setVisibility(View.VISIBLE);
            } else {
                viewQuizBlank.setVisibility(View.GONE);
            }

        } else {
            footerDivider.setVisibility(View.GONE);
            tvEmpty.setText("暂无数据，请点击重试");
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 从缓存中读取要显示的数据
     * */
    public void resetListDatas() {
        // 1. 清空显示缓存的数据
        // 2. 从缓存中加载数据到显示缓存
        listItems.clear();

        List<QuoteItemBean> listInfos = sparseArrayListInfos.get(currentInfoType, null);
        if (listInfos != null) {
            listItems.addAll(listInfos);
        }
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

    private void refreshDataFlipper() {
        if (currentDataType == DATA_TYPE_FUND) {
            viewFlipperDatas.setDisplayedChild(0);
        } else if (currentDataType == DATA_TYPE_RELATIVE) {
            viewFlipperDatas.setDisplayedChild(1);
        } else if (currentDataType == DATA_TYPE_FINANCIAL) {
            viewFlipperDatas.setDisplayedChild(2);
        } else if (currentDataType == DATA_TYPE_DIAGNOSE) {
            viewFlipperDatas.setDisplayedChild(3);
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
     * 重大提示详情
     * */
    public class MajorTipBean {
        public String date;
        public int tipId;
        public int tipType;
        public String title;

        public MajorTipBean(String date, int tipId, int tipType, String title) {
            super();
            this.date = date;
            this.tipId = tipId;
            this.tipType = tipType;
            this.title = title;
        }
    }

    @Override
    public void onAppraise(long id, int lev) {}

    @Override
    public void onPlayVoice(int pos) {}

    @Override
    public void onClickHeadIcon(int pos) {
        if (currentInfoType == INFO_TYPE_QUESTION) {
            if (listItems.get(pos).quizItem.getReplier() != null) {
                TeacherDetailPage.startPage(QuoteStockPage.this, listItems.get(pos).quizItem.getReplier().getId(), listItems.get(pos).quizItem.getReplier().getNick());
            }
        }
    }

    @Override
    public void onQuestionClose(int pos) {
        if (mediaPlayerManager == null) {
            mediaPlayerManager = new AppMediaPlayerManager(this);
            mediaPlayerManager.setPageState(true);
        }

        if (listItems.get(pos).quizItem != null) {
            mediaPlayerManager.onStartPlayer(listItems.get(pos).quizItem.getAnswer().getVoiceUrl());
        }

        // 重置上次的状态
        if (playPos >= 0 && playPos != pos) {
            listItems.get(playPos).quizItem.setPlaying(false);
        }

        // 更新列表
        refreshListView();

        this.playPos = pos;
    }

    @Override
    public void onPlayerStart() {
        if (listItems != null) {
            listItems.get(playPos).quizItem.setPlaying(true);
        }

        // 更新列表
        refreshListView();
    }

    @Override
    public void onPlayerPause() {
        if (listItems != null) {
            listItems.get(playPos).quizItem.setPlaying(false);
        }

        // 更新列表
        refreshListView();
    }

    @Override
    public void onPlayerCompletion() {
        listItems.get(playPos).quizItem.setPlaying(false);

        // 更新列表
        refreshListView();
    }

    @Override
    public void onPlayerError() {}

}
