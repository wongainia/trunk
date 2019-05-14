package cn.emoney.acg.page.share.quote;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.CandleStickPackage;
import cn.emoney.acg.data.protocol.quote.CandleStickReply.CandleStick_Reply;
import cn.emoney.acg.data.protocol.quote.CandleStickReply.CandleStick_Reply.CandleStick;
import cn.emoney.acg.data.protocol.quote.CandleStickRequest.CandleStick_Request;
import cn.emoney.acg.data.protocol.quote.CpxPackage;
import cn.emoney.acg.data.protocol.quote.CpxReply.Cpx_Reply;
import cn.emoney.acg.data.protocol.quote.CpxReply.Cpx_Reply.cpx_item;
import cn.emoney.acg.data.protocol.quote.CpxRequest.Cpx_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.EquipmentData;
import cn.emoney.acg.page.equipment.SupportEquipment;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.ViewUtil;
import cn.emoney.acg.view.BSTopLayer;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.ChartView.OnSingleTapListener;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnDrawingListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap;
import cn.emoney.sky.libs.chart.layers.GroupLayer_shell;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.XAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;

public class KLinePage extends PageImpl {

    /**
     * K线刷新类型分为三种： 1. 手动切换K线类型时刷新，如果缓存有数据，显示缓存数据，如果缓存无数据，立即刷新。重置K线显示 2. 手动下拉刷新，无论缓存有无数据，立即刷新。不重置K线显示
     * 3. 定时自动刷新，如果缓存有数据，5分钟刷新1次，如果缓存无数据，立即刷新。不重置K线显示
     * */
    public static final int KLINE_REFRESH_TYPE_CHANGE_PERIOD = 1001;
    public static final int KLINE_REFRESH_TYPE_PULL = 1002;
    public static final int KLINE_REFRESH_TYPE_AUTO = 1003;

    private static final String MIN_WIDTH = "1000.00亿";

    private ChartView mChartView = null;

    private ColumnarLayer mKLineLayer = null;

    private YAxisLayer mKLineAxisLayer = null;

    private ColumnarLayer mVolumeLayer = null;

    private XAxisLayer mXAxisLayer = null; // 横坐标

    private int TEXTSIZE = 12;

    private int KLINE_COUNT = 30;
    private final static int COLUMN_WIDTH = 4;

    private final String KEY_CANDLE_STICK = "candle_stick";
    private final String KEY_CPX = "cpx";

    private DecimalFormat mFormat2Decimal = new DecimalFormat("0.00");
    private DecimalFormat mCurrFormat = mFormat2Decimal;

    private float mColumnWidth = 0;

    private LineLayer mMA5Layer = null;
    private LineLayer mMA10Layer = null;
    private LineLayer mMA20Layer = null;

    private BSTopLayer mBSTopLayer = null;

    private StackLayer mKLineStackLayer = null;
    private int mFrameBoderColor = Color.GRAY;
    private int mCurrPeriod = 0;
    private int mGoodsId = 0;
    private float mBaseWidth = 5;

    private static int SCREEN_WIDTH = 0;

    private TextView mTvHideBS = null;

    // 自动刷新请求次数
    private int nRefreshCount = 0;
    private int nPeriodCount = 0;

    private String mGoodsName = "";

    /**
     * 缓存网络返回的数据，以K线类型对应的TYPE值加K线/操盘线为key
     * */
    private Map<String, QuotePackageImpl> mapPackages = new HashMap<String, QuotePackageImpl>();

    public KLinePage() {}

    @Override
    protected void initData() {
        int nCurInterval = getInterval();
        if (nCurInterval > 0) {
            nPeriodCount = DataModule.G_KLINE_REFRESH_INTERVAL / nCurInterval;
        }

    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_kline);


        mColumnWidth = FontUtils.dip2px(getContext(), COLUMN_WIDTH);

        SCREEN_WIDTH = FontUtils.px2dip(getContext(), DataModule.SCREEN_WIDTH) - 17;

        KLINE_COUNT = (int) (SCREEN_WIDTH / mBaseWidth);
        LogUtil.easylog("klinepage->KLINE_COUNT:" + KLINE_COUNT);

        mChartView = (ChartView) findViewById(R.id.page_kline_cv);

        mFrameBoderColor = RColor(R.color.b5);

        if (mChartView != null) {

            mChartView.setOnSingleTapListener(new OnSingleTapListener() {
                @Override
                public void onSingleTapConfirm() {
                    LogUtil.easylog("sky", "KLinePage->mChartView->onSingleTapConfirm");
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_GOODSID, mGoodsId);
                    bundle.putString(KEY_GOODSNAME, mGoodsName);
                    bundle.putInt(KEY_LINEPERIOD, mCurrPeriod);
                    startModule(bundle, QuoteLandscapeHome.class);
                }
            });

            // k线区线左方坐标
            mKLineAxisLayer = new YAxisLayer();

            mKLineAxisLayer.setAxisCount(2);
            mKLineAxisLayer.setMaxValue(0.00f);
            mKLineAxisLayer.setMinValue(0.00f);
            mKLineAxisLayer.setColor(RColor(R.color.t3));
            mKLineAxisLayer.setAlign(Align.LEFT);
            mKLineAxisLayer.setPaddings(10, 5, 0, 5);
            mKLineAxisLayer.setMinWidthString(MIN_WIDTH);
            mKLineAxisLayer.setTextSize(FontUtils.dip2px(getContext(), TEXTSIZE));

            mKLineAxisLayer.setOnFormatDataListener(new OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    return mCurrFormat.format(val);
                }
            });

            // BS点
            mBSTopLayer = new BSTopLayer(getContext());

            // K线
            mKLineLayer = new ColumnarLayer();

            mKLineLayer.setWriteOutCallback(mBSTopLayer);

            mKLineLayer.setMaxCount(KLINE_COUNT);
            mKLineLayer.setColumnarWidth(mColumnWidth);
            mKLineLayer.setLineWidth(FontUtils.dip2px(getContext(), 1));
            mKLineLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
            mKLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 0));
            mKLineLayer.showHGrid(2);
            mKLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mKLineLayer.setOnDrawingListener(new OnDrawingListener() {

                @Override
                public void onDrawing(Paint paint, int pos) {
                    ColumnarAtom atom = mKLineLayer.getValue(pos);

                    int color = getZDPColor(1);

                    int zdFlag = FontUtils.getColorByPrice(atom.mOpen, atom.mClose);
                    if (zdFlag == 0) {
                        if (pos >= 1) {
                            ColumnarAtom atomPreDay = mKLineLayer.getValue(pos - 1);
                            float fPrePrice = atomPreDay.mClose;
                            if (atom.mClose < fPrePrice) {
                                color = getZDPColor(-1);
                            }
                        }
                    } else {
                        color = getZDPColor(zdFlag);
                    }

                    paint.setColor(color);

                }
            });


            mMA5Layer = new LineLayer();

            mMA5Layer.setColor(RColor(R.color.sky_line_ma5));
            mMA5Layer.setMaxCount(KLINE_COUNT);
            mMA5Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mMA5Layer.setFloorValue(0, false);

            mMA10Layer = new LineLayer();

            mMA10Layer.setColor(RColor(R.color.sky_line_ma10));
            mMA10Layer.setMaxCount(KLINE_COUNT);
            mMA10Layer.setFloorValue(0, false);
            mMA10Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            mMA20Layer = new LineLayer();

            mMA20Layer.setColor(RColor(R.color.sky_line_ma20));
            mMA20Layer.setMaxCount(KLINE_COUNT);
            mMA20Layer.setFloorValue(0, false);
            mMA20Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            // k线 区线 BS点 的栈层
            mKLineStackLayer = new StackLayer();
            mKLineStackLayer.setPaddings(0, FontUtils.dip2px(getContext(), 17), 0, FontUtils.dip2px(getContext(), 17));
            mKLineStackLayer.setShowBorder(true);
            mKLineStackLayer.setBorderWidth(1);
            mKLineStackLayer.setBorderColor(mFrameBoderColor);
            mKLineStackLayer.setShowHPaddingLine(true);

            mKLineStackLayer.addLayer(mKLineLayer);
            mKLineStackLayer.addLayer(mMA5Layer);
            mKLineStackLayer.addLayer(mMA10Layer);
            mKLineStackLayer.addLayer(mMA20Layer);
            // 最后加BS点Layer
            mKLineStackLayer.addLayer(mBSTopLayer);

            // k线 区线 BS点 和 其左边坐标的 group
            GroupLayerOverlap mKLineGroupLayer = new GroupLayerOverlap();
            Bitmap t_bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_avgline_identity);
            mKLineGroupLayer.setAvgLineBitmap(t_bitmap);
            mKLineGroupLayer.switchAvgLineIdentifyOn(true);

            mKLineGroupLayer.setLeftLayer(mKLineAxisLayer);
            mKLineGroupLayer.setRightLayer(mKLineStackLayer);
            mKLineGroupLayer.setHeightPercent(0.72f);
            mChartView.addLayer(mKLineGroupLayer);

            mVolumeLayer = new ColumnarLayer();
            mVolumeLayer.setPaddings(0, 2, 0, 0);
            mVolumeLayer.setShowBorder(true);
            mVolumeLayer.setBorderWidth(1);
            mVolumeLayer.setBorderColor(mFrameBoderColor);

            mVolumeLayer.showHGrid(1);
            mVolumeLayer.setMaxCount(KLINE_COUNT);
            mVolumeLayer.setColumnarWidth(mColumnWidth);
            mVolumeLayer.setLineWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
            mVolumeLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setOnDrawingListener(new OnDrawingListener() {

                @Override
                public void onDrawing(Paint paint, int pos) {
                    ColumnarAtom atom = mKLineLayer.getValue(pos);

                    int color = getZDPColor(1);

                    int zdFlag = FontUtils.getColorByPrice(atom.mOpen, atom.mClose);
                    if (zdFlag == 0) {
                        if (pos >= 1) {
                            ColumnarAtom atomPreDay = mKLineLayer.getValue(pos - 1);
                            float fPrePrice = atomPreDay.mClose;
                            if (atom.mClose < fPrePrice) {
                                color = getZDPColor(-1);
                            }
                        }
                    } else {
                        color = getZDPColor(zdFlag);
                    }

                    paint.setColor(color);

                }
            });


            GroupLayer_shell groupLayer_shell = new GroupLayer_shell();
            groupLayer_shell.setContenLayer(mVolumeLayer);
            groupLayer_shell.setHeightPercent(0.28f);

            mChartView.addLayer(groupLayer_shell);

            // 下方横坐标
            mXAxisLayer = new XAxisLayer();

            mXAxisLayer.setColor(RColor(R.color.t3));
            // mXAxisLayer.setMinLeftPaddingString(MIN_WIDTH);
            mXAxisLayer.setTextSize(FontUtils.dip2px(getContext(), TEXTSIZE));

            mChartView.addLayer(mXAxisLayer);

            // mBSTopLayer.clearLstKLineData();
            mChartView.postInvalidate();


            mTvHideBS = (TextView) findViewById(R.id.tv_hide_bs_btn);
            mTvHideBS.setTag("hide");
            mTvHideBS.setText("隐藏 BS");

            EquipmentData tCpxPermission = SupportEquipment.getInstance().getById(SupportEquipment.ID_CPX);
            if (!tCpxPermission.hasPermission) {
                mTvHideBS.setVisibility(View.GONE);
            } else {
                mTvHideBS.setVisibility(View.VISIBLE);
                mTvHideBS.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object o = ViewUtil.getViewTag(mTvHideBS);
                        if (o != null) {
                            String sTag = (String) o;
                            if (sTag != null && sTag.equals("hide")) { // do隐藏
                                mTvHideBS.setTag("show");
                                mTvHideBS.setText("显示 BS");
                                mBSTopLayer.show(false);
                                mChartView.postInvalidate();
                            } else if (sTag != null && sTag.equals("show")) { // do显示
                                mTvHideBS.setTag("hide");
                                mTvHideBS.setText("隐藏 BS");
                                mBSTopLayer.show(true);
                                mChartView.postInvalidate();
                            }
                        }

                    }
                });
            }
        }

    }

    @Override
    protected void onPageDestroy() {
        if (mapPackages != null && mapPackages.size() > 0) {
            mapPackages.clear();
            mapPackages = null;
        }
        super.onPageDestroy();
    }

    public void setPeriod(int period) {
        mCurrPeriod = period;
    }

    /**
     * 刷新K线走势图数据
     * */
    public void requestData(int refreshType) {
        if (mGoodsId == 0)
            return;

        /*
         * K线刷新类型分为三种： 1. 手动切换K线类型时刷新，如果缓存有数据，显示缓存数据，如果缓存无数据，立即刷新。重置K线显示 2.
         * 手动下拉刷新，无论缓存有无数据，立即刷新。不重置K线显示 3. 定时自动刷新，如果缓存有数据，5分钟刷新1次，如果缓存无数据，立即刷新。不重置K线显示
         */

        if (refreshType == KLINE_REFRESH_TYPE_CHANGE_PERIOD) {
            // 1. 重置K线显示，清空界面显示，重置各组件数据及位置
            resetLayers();

            // 2. 判断缓存中是否有数据，如果有数据，直接使用缓存数据刷新，如果没有数据，立即发送网络请求刷新
            if (updateQuoteFromCache()) {
                // 3. 使用缓存的操盘线数据刷新，如果缓存中没有操盘线数据，从网络中获取
                if (!updateCpxFromCache()) {
                    // 4. 从缓存中读取不到操盘线数据，通过网络获取
                    requestCpx();
                }
            } else {
                requestKLine();
            }
        } else if (refreshType == KLINE_REFRESH_TYPE_PULL) {
            // 无论缓存有无数据，立即刷新
            requestKLine();
        } else if (refreshType == KLINE_REFRESH_TYPE_AUTO) {
            /*
             * 如果有缓存数据，使用缓存数据刷新报价走势图，并对时间进行检查，以决定是否使用网络刷新数据。
             * 如果缓存中没有数据，或缓存数据为空，刷新失败，首先清空界面显示，然后跳过对时间的检查，直接发送网络请求，获取数据。
             */
            if (updateQuoteFromCache()) {
                if (nPeriodCount > 0 && nRefreshCount % nPeriodCount != 0) {

                    nRefreshCount++;

                    // 使用缓存的操盘线数据刷新，如果缓存中没有操盘线数据，从网络中获取
                    if (!updateCpxFromCache()) {
                        // 从缓存中读取不到操盘线数据，通过网络获取
                        requestCpx();
                    }

                    return;
                }
            }

            // 发送网络请求，刷新数据
            requestKLine();
        }

    }

    /**
     * 更新K线数据
     * */
    private void requestKLine() {
        CandleStickPackage pkg = new CandleStickPackage(new QuoteHead((short) 0));
        pkg.setRequest(CandleStick_Request.newBuilder().setGoodsId(mGoodsId).setLastUpdateMarketTime(0).setReqPeriod(mCurrPeriod).setReqBegin(0).setReqMa(7).setReqSize(200).build());
        requestQuote(pkg, IDUtils.CandleStick);
        LogUtil.easylog("requestData()->requestQuote(pkg, IDUtils.CandleStick)->goodid:" + mGoodsId);
    }

    /**
     * 更新操盘线数据
     * */
    private void requestCpx() {
        EquipmentData tCpxPermission = SupportEquipment.getInstance().getById(SupportEquipment.ID_CPX);
        if (!tCpxPermission.hasPermission) {
            return;
        }

        CpxPackage cpx = new CpxPackage(new QuoteHead((short) 0));
        cpx.setRequest(Cpx_Request.newBuilder().setGoodsId(mGoodsId).setReqPeriod(mCurrPeriod).setLastUpdateMarketTime(0).build());
        requestQuote(cpx, IDUtils.Cpx);
        LogUtil.easylog("Request CPX goodsid:" + mGoodsId + ";ReqPeriod:" + mCurrPeriod + ";UpdateMarketTime:0");
    }

    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;

        ArrayList<Goods> t_lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
        closeSQLDBHelper();
        if (t_lstGoods != null && t_lstGoods.size() > 0) {
            mGoodsName = t_lstGoods.get(0).getGoodsName();
        }

    }

    private void resetLines() {
        mKLineLayer.setMaxCount(KLINE_COUNT);
        mVolumeLayer.setMaxCount(KLINE_COUNT);

        mMA5Layer.setMaxCount(KLINE_COUNT);
        mMA10Layer.setMaxCount(KLINE_COUNT);
        mMA20Layer.setMaxCount(KLINE_COUNT);

        mKLineStackLayer.calMinAndMaxValue();
        mKLineLayer.setMaxValue(mKLineStackLayer.getMaxValue());
        mMA5Layer.setMaxValue(mKLineStackLayer.getMaxValue());
        mMA10Layer.setMaxValue(mKLineStackLayer.getMaxValue());
        mMA20Layer.setMaxValue(mKLineStackLayer.getMaxValue());

        mKLineLayer.setMinValue(mKLineStackLayer.getMinValue());
        mMA5Layer.setMinValue(mKLineStackLayer.getMinValue());
        mMA10Layer.setMinValue(mKLineStackLayer.getMinValue());
        mMA20Layer.setMinValue(mKLineStackLayer.getMinValue());

        mKLineAxisLayer.setMaxValue(mKLineStackLayer.getMaxValue());
        mKLineAxisLayer.setMinValue(mKLineStackLayer.getMinValue());

        mVolumeLayer.calMinAndMaxValue();
        mVolumeLayer.setMaxValue(mVolumeLayer.getMaxValue());
        mVolumeLayer.setMinValue(mVolumeLayer.getMinValue());

        ColumnarAtom kLast = mKLineLayer.getLastValue();
        ColumnarAtom kFirst = mKLineLayer.getDisplayFirstValue();
        int date_last = 0;
        int date_first = 0;
        if (kLast != null && kFirst != null) {
            date_last = (Integer) kLast.mTag;
            date_first = (Integer) kFirst.mTag;
        }

        mXAxisLayer.clearValue();

        String sDate_first = "";
        String sDate_last = "";

        if (mCurrPeriod == TYPE_60MINUTE) {
            String tDate = DataUtils.formatDateY_M_D_HHmm(String.valueOf(date_first), "/");
            String[] aryDate = tDate.split("/");
            if (aryDate != null && aryDate.length == 4) {
                sDate_first = aryDate[1] + "/" + aryDate[2];
            }
            tDate = DataUtils.formatDateY_M_D_HHmm(String.valueOf(date_last), "/");
            aryDate = tDate.split("/");
            if (aryDate != null && aryDate.length == 4) {
                sDate_last = aryDate[1] + "/" + aryDate[2];
            }
        } else {
            sDate_first = DataUtils.formatDateY_M(String.valueOf(date_first), "/");
            sDate_last = DataUtils.formatDateY_M(String.valueOf(date_last), "/");
        }

        mXAxisLayer.addValue(sDate_first);
        mXAxisLayer.addValue(sDate_last);
    }

    /**
     * 将网络端获取到的数据缓存在本地 使用缓存在本地的数据刷新报价走势
     * */
    protected void updateFromQuote(QuotePackageImpl pkg) {
        cacheNetworkData(pkg);

        if (pkg instanceof CandleStickPackage) {
            CandleStickPackage candleStickPkg = (CandleStickPackage) pkg;
            CandleStick_Reply reply = candleStickPkg.getResponse();
            List<CandleStick> lstData = reply.getKLinesList();
            mKLineLayer.clear();
            mVolumeLayer.clear();
            mMA5Layer.clear();
            mMA10Layer.clear();
            mMA20Layer.clear();


            LogUtil.easylog("updateFromQuote()->lstData.size():" + lstData.size());

            for (int i = lstData.size() - 1; i >= 0; i--) {
                CandleStick candle = lstData.get(i);
                float open = candle.getOpen();
                float high = candle.getHigh();
                float close = candle.getPrice();
                float low = candle.getLow();
                float ma5 = candle.getMa5();
                float ma10 = candle.getMa10();
                float ma20 = candle.getMa20();
                // Tracer.V("ZYL", "ma5="+ma5+" ma10="+ma10+" ma20="+ma20);
                ColumnarAtom klineCol = new ColumnarAtom(open / 1000, high / 1000, close / 1000, low / 1000);

                klineCol.mTag = candle.getDatetime();
                // klineCol.mTag = 0;
                mKLineLayer.addValue(klineCol);
                mVolumeLayer.addValue(new ColumnarAtom(candle.getAmount()));
                mMA5Layer.addValue(ma5 / 1000);
                mMA10Layer.addValue(ma10 / 1000);
                mMA20Layer.addValue(ma20 / 1000);
            }

            resetLines();

            // mBSTopLayer.clearLstKLineData();
            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();

            nRefreshCount++;

            requestCpx();
        } else if (pkg instanceof CpxPackage) {
            CpxPackage p = (CpxPackage) pkg;
            Cpx_Reply reply = p.getResponse();
            List<cpx_item> items = reply.getCpxItemArrayList();
            LogUtil.easylog("sky" + "KLinePage->updateQuote:cpx");
            // LogUtil.easylog("CPX:" + items.toString());
            // for(int i = 0; i < items.size(); i++)
            // {
            // cpx_item item = items.get(i);
            // String bsFlag = item.getBsFlag();
            // int dateTime = item.getDatetime();
            // Tracer.V("ZYL", dateTime+":"+bsFlag);
            // }
            // mKLineLayer.setBSItems(items);

            // mBSTopLayer.clearLstKLineData();
            mBSTopLayer.setBSItems(items);
            mChartView.postInvalidate();
        }
    }

    /**
     * 使用内存缓存数据刷新K线走势 当在ToolBar中切换K线类型时。切换显示K线数据，如果本地有缓存数据，就显示并返回true，如果本地没有缓存数据，就返回false
     * 首先刷新显示缓存的K线数据，然后刷新显示缓存的操盘线数据
     * */
    private boolean updateQuoteFromCache() {

        // 使用缓存的K线数据刷新，如果刷新失败，返回false
        String keyCandle = String.valueOf(mCurrPeriod) + KEY_CANDLE_STICK;
        if (mapPackages.containsKey(keyCandle)) {
            QuotePackageImpl pkg = mapPackages.get(keyCandle);

            // 如果缓存的数据内容为空，缓存刷新失败，发送网络请求刷新数据
            if (isCacheDataEmpty(pkg))
                return false;

            updateChartView(pkg);
            return true;
        }

        return false;
    }

    /**
     * 从缓存中读取操盘线数据
     * */
    private boolean updateCpxFromCache() {
        String keyCpx = String.valueOf(mCurrPeriod) + KEY_CPX;
        if (mapPackages.containsKey(keyCpx)) {
            QuotePackageImpl pkg = mapPackages.get(keyCpx);

            // 如果缓存的数据内容为空，缓存刷新失败，发送网络请求刷新数据
            if (isCacheDataEmpty(pkg))
                return false;

            updateChartView(pkg);
            return true;
        }

        return false;
    }

    /**
     * 缓存的数据内容是否为空
     * */
    private boolean isCacheDataEmpty(QuotePackageImpl pkg) {
        if (pkg instanceof CandleStickPackage) {
            CandleStickPackage candleStickPkg = (CandleStickPackage) pkg;
            CandleStick_Reply reply = candleStickPkg.getResponse();
            List<CandleStick> lstData = reply.getKLinesList();
            if (lstData != null && lstData.size() > 0) {
                return false;
            }
        } else if (pkg instanceof CpxPackage) {
            CpxPackage p = (CpxPackage) pkg;
            Cpx_Reply reply = p.getResponse();
            List<cpx_item> items = reply.getCpxItemArrayList();
            if (items != null && items.size() > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 在内存中缓存从网络端获取到的蜡状图数据和操盘线数据 将pkg以mCurrPeriod为key缓存下来。如果没有，就缓存，如果有，就替换
     * */
    private void cacheNetworkData(QuotePackageImpl pkg) {
        if (pkg instanceof CandleStickPackage) {
            String key = String.valueOf(mCurrPeriod) + KEY_CANDLE_STICK;
            mapPackages.put(key, pkg);
        } else if (pkg instanceof CpxPackage) {
            String key = String.valueOf(mCurrPeriod) + KEY_CPX;
            mapPackages.put(key, pkg);
        }
    }

    /**
     * 使用缓存数据刷新K线或操盘线界面
     * */
    private void updateChartView(QuotePackageImpl pkg) {
        if (pkg instanceof CandleStickPackage) {
            CandleStickPackage candleStickPkg = (CandleStickPackage) pkg;
            CandleStick_Reply reply = candleStickPkg.getResponse();
            List<CandleStick> lstData = reply.getKLinesList();
            mKLineLayer.clear();
            mVolumeLayer.clear();
            mMA5Layer.clear();
            mMA10Layer.clear();
            mMA20Layer.clear();
            for (int i = lstData.size() - 1; i >= 0; i--) {
                CandleStick candle = lstData.get(i);
                float open = candle.getOpen();
                float high = candle.getHigh();
                float close = candle.getPrice();
                float low = candle.getLow();
                float ma5 = candle.getMa5();
                float ma10 = candle.getMa10();
                float ma20 = candle.getMa20();
                // Tracer.V("ZYL", "ma5="+ma5+" ma10="+ma10+" ma20="+ma20);
                ColumnarAtom klineCol = new ColumnarAtom(open / 1000, high / 1000, close / 1000, low / 1000);

                klineCol.mTag = candle.getDatetime();
                // klineCol.mTag = 0;
                mKLineLayer.addValue(klineCol);
                mVolumeLayer.addValue(new ColumnarAtom(candle.getAmount()));
                mMA5Layer.addValue(ma5 / 1000);
                mMA10Layer.addValue(ma10 / 1000);
                mMA20Layer.addValue(ma20 / 1000);

                // getLogger("").appendln("date:" + candle.getDatetime() +
                // "ma5="+ma5+" ma10="+ma10+" ma20="+ma20);

            }
            // getLogger("").appendln("****************************");
            // getLogger("").output("LOGGER", true);

            resetLines();

            // mBSTopLayer.clearLstKLineData();
            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();
        } else if (pkg instanceof CpxPackage) {
            CpxPackage p = (CpxPackage) pkg;
            Cpx_Reply reply = p.getResponse();
            List<cpx_item> items = reply.getCpxItemArrayList();
            LogUtil.easylog("sky" + "KLinePage->updateQuote:cpx");
            // LogUtil.easylog("CPX:" + items.toString());
            // for(int i = 0; i < items.size(); i++)
            // {
            // cpx_item item = items.get(i);
            // String bsFlag = item.getBsFlag();
            // int dateTime = item.getDatetime();
            // Tracer.V("ZYL", dateTime+":"+bsFlag);
            // }
            // mKLineLayer.setBSItems(items);

            // mBSTopLayer.clearLstKLineData();
            mBSTopLayer.setBSItems(items);
            mChartView.postInvalidate();

            // nRefreshCount++;
        }

    }

    /**
     * 将界面清空
     * */
    private void resetLayers() {
        // 1. clear k line, volume, and maN
        mKLineLayer.resetData();
        mVolumeLayer.resetData();
        mMA5Layer.resetData();
        mMA10Layer.resetData();
        mMA20Layer.resetData();

        // 2. clear BS
        mBSTopLayer.clear();

        // 3. clear Axis
        mKLineAxisLayer.setMaxValue(0f);
        mKLineAxisLayer.setMinValue(0f);
        mXAxisLayer.clearValue();

        // 必须调用该方法，重新设置charView显示的起点，否则可能显示为空白
        resetLines();

        // 4. post invalidate
        mChartView.forceAdjustLayers();
        mChartView.postInvalidate();
    }

}
