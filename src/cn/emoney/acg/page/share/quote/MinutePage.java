package cn.emoney.acg.page.share.quote;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import cn.emoney.acg.R;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.MinuteLinePackage;
import cn.emoney.acg.data.protocol.quote.MinuteLineReply.MinuteLine_Reply.MinuteData;
import cn.emoney.acg.data.protocol.quote.MinuteLineRequest.MinuteLine_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.ChartView.OnSingleTapListener;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnDrawingListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap_count3;
import cn.emoney.sky.libs.chart.layers.GroupLayer_shell;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.RightSideYAxisLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.XAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;
import cn.emoney.sky.libs.chart.layers.YAxisLayerOverlap;

public class MinutePage extends PageImpl {

    // private static final String MIN_WIDTH = "10.00亿";
    private static final String MIN_WIDTH = "99999.99";

    private final int MAX_MINUTE_COUNT = 240;

    private ChartView mChartView = null;
    private int mTextSize = 10;
    private YAxisLayerOverlap mAxisLineLayer = null;
    private LineLayer mAvgLayer = null;
    private LineLayer mLineLayer = null;
    private RightSideYAxisLayer mRightSideAxisLayer = null;

    private ColumnarLayer mVolumeLayer = null;
    private XAxisLayer mXAxisLayer = null;

    private StackLayer mLineStackLayer = null;
    private DecimalFormat mFormat2Decimal = new DecimalFormat("0.00");
    private DecimalFormat mCurrFormat = mFormat2Decimal;

    private int mGoodsId = 0;
    private String mGoodsName = "";
    private int mFrameBoderColor = Color.GRAY;

    // 增量请求
    private int nMarketTime = 0;
    private int nLastPointTime = 0;

    private long nLastTime = 0;

    public MinutePage() {}

    @Override
    protected void initData() {}

    @Override
    protected void initPage() {
        setContentView(R.layout.page_minute);

        // 特殊处理主题
        mFrameBoderColor = RColor(R.color.b5);

        mChartView = (ChartView) findViewById(R.id.page_minute_cv);
        if (mChartView != null) {

            // mChartView.setOnDoubleTapListener(new OnDoubleTapListener() {
            // @Override
            // public void onDoubleTap() {
            // LogUtil.easylog("sky", "MinutePage->mChartView->onDoubleTap");
            // // PageIntent pageIntent = new PageIntent(MinutePage.this,
            // // MinuteLandscapePage.class);
            // // startPage(R.id.mainpage_content, pageIntent);
            //
            // // startModule(QuoteLandscapeHome.class);
            // Bundle bundle = new Bundle();
            // bundle.putInt(KEY_GOODSID, mGoodsId);
            // bundle.putString(KEY_GOODSNAME, mGoodsName);
            // PageImpl t_pageQuotePage = (PageImpl) getParent();
            // // String t_price = t_pageQuotePage.getCurPrice();
            // String t_price = "15.02";
            // bundle.putString(KEY_GOODSPRICE, t_price);
            // startModule(bundle, QuoteLandscapeHome.class);
            // }
            // });

            mChartView.setOnSingleTapListener(new OnSingleTapListener() {
                @Override
                public void onSingleTapConfirm() {
                    LogUtil.easylog("sky", "MinutePage->mChartView->onSingleTapConfirm");
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_GOODSID, mGoodsId);
                    bundle.putString(KEY_GOODSNAME, mGoodsName);
                    bundle.putInt(KEY_LINEPERIOD, TYPE_MINUTE);
                    startModule(bundle, QuoteLandscapeHome.class);
                }
            });

            mRightSideAxisLayer = new RightSideYAxisLayer();
            mRightSideAxisLayer.setBorderWidth(0);
            mRightSideAxisLayer.setColor(RColor(R.color.t3));
            mRightSideAxisLayer.setAxisCount(2);
            mRightSideAxisLayer.setMaxValue(0.00f);
            mRightSideAxisLayer.setMinValue(0.00f);
            mRightSideAxisLayer.setAlign(Align.RIGHT);
            mRightSideAxisLayer.setMinWidthString("-99.99%");
            mRightSideAxisLayer.setTextSize(FontUtils.dip2px(getContext(), mTextSize));
            mRightSideAxisLayer.setPaddings(0, 10, 10, 10);

            mRightSideAxisLayer.setOnFormatDataListener(new RightSideYAxisLayer.OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    return DataUtils.formatFloat2Percent(val);
                }
            });

            mAxisLineLayer = new YAxisLayerOverlap();

            mAxisLineLayer.setColor(RColor(R.color.t3));
            mAxisLineLayer.setAxisCount(3);
            mRightSideAxisLayer.setBorderWidth(0);
            mAxisLineLayer.setMaxValue(0.00f);
            mAxisLineLayer.setMinValue(0.00f);
            mAxisLineLayer.setAlign(Align.LEFT);
            mAxisLineLayer.setMinWidthString(MIN_WIDTH);
            mAxisLineLayer.setTextSize(FontUtils.dip2px(getContext(), mTextSize));
            mAxisLineLayer.setPaddings(10, 10, 0, 10);

            mAxisLineLayer.setOnDrawingListener(new OnDrawingListener() {

                @Override
                public void onDrawing(Paint paint, int pos) {
                    float price = mAxisLineLayer.getValue(pos);
                }
            });
            mAxisLineLayer.setOnFormatDataListener(new OnFormatDataListener() {

                @Override
                public String onFormatData(float val) {
                    return mCurrFormat.format(val);
                }
            });
            mAvgLayer = new LineLayer();

            mAvgLayer.setMaxCount(MAX_MINUTE_COUNT);
            mAvgLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mAvgLayer.setColor(RColor(R.color.sky_line_average));

            mLineLayer = new LineLayer();
            mLineLayer.setMaxCount(MAX_MINUTE_COUNT);
            mLineLayer.showHGrid(3);
            mLineLayer.showVGrid(3);
            mLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setColor(RColor(R.color.sky_line_minute));

            mLineLayer.setShowShadow(true);
            mLineLayer.setShadowColor(RColor(R.color.sky_line_minute_shadow));


            // line 层
            mLineStackLayer = new StackLayer();

            mLineStackLayer.setPaddings(0, 10, 0, 1);
            mLineStackLayer.addLayer(mAvgLayer);
            mLineStackLayer.addLayer(mLineLayer);
            mLineStackLayer.setShowBorder(true);
            mLineStackLayer.setMiddleLineIsFull(true);
            mLineStackLayer.setBorderWidth(1);
            mLineStackLayer.setBorderColor(mFrameBoderColor);

            GroupLayerOverlap_count3 mLineGroupLayer = new GroupLayerOverlap_count3();

            mLineGroupLayer.setLeftLayer(mAxisLineLayer);
            mLineGroupLayer.setCenterLayer(mLineStackLayer);
            mLineGroupLayer.setRightLayer(mRightSideAxisLayer);
            mLineGroupLayer.setHeightPercent(0.73f);
            mChartView.addLayer(mLineGroupLayer);

            mVolumeLayer = new ColumnarLayer();
            mVolumeLayer.showHGrid(1);
            mVolumeLayer.showVGrid(3);
            mVolumeLayer.setPaddings(0, 3, 0, 0);
            mVolumeLayer.setShowBorder(true);
            mVolumeLayer.setBorderWidth(1);
            mVolumeLayer.setBorderColor(mFrameBoderColor);

            mVolumeLayer.setMaxCount(MAX_MINUTE_COUNT);
            mVolumeLayer.setColumnarWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setColor(RColor(R.color.sky_line_volume));

            GroupLayer_shell groupLayer_shell = new GroupLayer_shell();
            groupLayer_shell.setContenLayer(mVolumeLayer);
            groupLayer_shell.setHeightPercent(0.27f);

            // mVolumeLayer.showVGrid(3);
            // mVolumeLayer.setOnDrawingListener(new OnDrawingListener() {
            //
            // @Override
            // public void onDrawing(Paint paint, int pos) {
            // float val = mLineLayer.getValue(pos);
            // if(pos == 0)
            // {
            // if(val > mLastPrice)
            // {
            // paint.setColor(FontUtils.COLOR_RISE);
            // }
            // else if(val < mLastPrice)
            // {
            // paint.setColor(FontUtils.COLOR_FALL);
            // }
            // else
            // {
            // paint.setColor(FontUtils.COLOR_EQUAL);
            // }
            // }
            // else
            // {
            // float lastVal = mLineLayer.getValue(pos - 1);
            // if(val > lastVal)
            // {
            // paint.setColor(FontUtils.COLOR_RISE);
            // }
            // else if(val < lastVal)
            // {
            // paint.setColor(FontUtils.COLOR_FALL);
            // }
            // else
            // {
            // paint.setColor(FontUtils.COLOR_EQUAL);
            // }
            // }
            // }
            // });

            mChartView.addLayer(groupLayer_shell);

            mXAxisLayer = new XAxisLayer();

            mXAxisLayer.setColor(RColor(R.color.t3));
            mXAxisLayer.addValue("9:30");
            mXAxisLayer.addValue("11:30 13:00");
            mXAxisLayer.addValue("15:00");
            mXAxisLayer.setTextSize(FontUtils.dip2px(getContext(), mTextSize));

            mChartView.addLayer(mXAxisLayer);
        }

    }

    public void requestData() {
        if (mGoodsId == 0) {
            return;
        }

        nLastTime = System.currentTimeMillis();
        MinuteLinePackage pkg = new MinuteLinePackage(new QuoteHead((short) 0));
        pkg.setRequest(MinuteLine_Request.newBuilder().setLastRecvTime(nLastPointTime).setLastUpdateMarketDate(nMarketTime).setGoodsId(mGoodsId).build());
        requestQuote(pkg, IDUtils.MinuteLine, RequestUrl.host);
    }

    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;

        ArrayList<Goods> t_lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
        closeSQLDBHelper();
        if (t_lstGoods != null && t_lstGoods.size() > 0) {
            mGoodsName = t_lstGoods.get(0).getGoodsName();
        }
    }

    public void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof MinuteLinePackage) {
            MinuteLinePackage minuteLinePkg = (MinuteLinePackage) pkg;
            nMarketTime = minuteLinePkg.getResponse().getMarketDate();
            List<MinuteData> lstData = minuteLinePkg.getResponse().getTrendLineList();

            if (lstData.size() <= 0) {
                return;
            }

            LogUtil.easylog("sky", "MinutePage->updateFromQuote(good:" + mGoodsName + ")->UseTime:" + (System.currentTimeMillis() - nLastTime));

            if (nLastPointTime <= 0) {
                try {
                    mLineLayer.clear();
                    mAvgLayer.clear();
                    mVolumeLayer.clear();
                } catch (Exception e) {
                    return;
                }
            }

            for (int i = 0; i < lstData.size(); i++) {
                MinuteData minuteData = lstData.get(i);

                float price = minuteData.getPrice();
                float avgPrice = minuteData.getAve();
                int pointTime = minuteData.getTime();
                int gid = minuteLinePkg.getResponse().getGoodsId();

                long t_volume = minuteData.getVolume();
                // LogUtil.easylog("gcode:" + gCode + ", volume = " + t_volume);

                // 防止意外情况益出
                if (mLineLayer.getValueCount() >= MAX_MINUTE_COUNT) {
                    break;
                }

                int t_pos = DateUtils.minuteTimeToPos(pointTime);
                if (t_pos >= 0 && t_pos <= mLineLayer.getValueCount()) {
                    mLineLayer.setValue(t_pos, price / 1000);
                    mAvgLayer.setValue(t_pos, avgPrice / 1000);
                    mVolumeLayer.setValue(t_pos, new ColumnarAtom(t_volume));
                } else if (t_pos > mLineLayer.getValueCount()) {
                    float t_lastValPrice = mLineLayer.getLastValue();
                    float t_lastValAvg = mAvgLayer.getLastValue();

                    for (int j = 0; j < t_pos - mLineLayer.getValueCount(); j++) {
                        mLineLayer.addValue(t_lastValPrice);
                        mAvgLayer.addValue(t_lastValAvg);
                        mVolumeLayer.addValue(new ColumnarAtom(0));
                    }
                    mLineLayer.setValue(t_pos, price / 1000);
                    mAvgLayer.setValue(t_pos, avgPrice / 1000);
                    mVolumeLayer.setValue(t_pos, new ColumnarAtom(t_volume));
                }

                // mAvgLayer.addValue(avgPrice / 1000);
                // mLineLayer.addValue(price / 1000);

                // mVolumeLayer.addValue(new ColumnarAtom(t_volume));
                // mAvgLayer.setValue(t_pos, avgPrice / 1000);
                // mLineLayer.setValue(t_pos, price / 1000);

                // LogUtil.easylog("gcode:" + gCode + " P:" + price + " A:" +
                // avgPrice);

                if (i == lstData.size() - 1) {
                    nLastPointTime = minuteData.getTime();
                }
            }

            float fMinVal = 0;
            float fMaxVal = 0;
            float fZdPercent = 0.1000f;
            float fAry_max_min[] = mLineStackLayer.calMinAndMaxValue();
            if (fAry_max_min != null) {
                float lastPrice = minuteLinePkg.getResponse().getClose() / 1000.0f;
                fMinVal = fAry_max_min[0];
                fMaxVal = fAry_max_min[1];

                // 开盘前处理
                if ((fMinVal == lastPrice) && (fMaxVal == lastPrice)) {
                    fMinVal = lastPrice * 0.9f;
                    fMaxVal = lastPrice * 1.1f;
                    fZdPercent = 0.1000f;
                } else {
                    float offset_max = Math.abs(fMaxVal - lastPrice);
                    float offset_min = Math.abs(fMinVal - lastPrice);
                    float offset = offset_max > offset_min ? offset_max : offset_min;
                    fMinVal = lastPrice - offset;
                    fMaxVal = lastPrice + offset;

                    BigDecimal b_offset = new BigDecimal(offset);
                    if (lastPrice > 0) {
                        BigDecimal b_lastPrice = new BigDecimal(lastPrice);
                        BigDecimal percent = b_offset.divide(b_lastPrice, 4, BigDecimal.ROUND_HALF_UP);
                        fZdPercent = percent.floatValue();
                    }
                }

            }

            mAxisLineLayer.setMaxValue(fMaxVal);
            mAxisLineLayer.setMinValue(fMinVal);

            mRightSideAxisLayer.setMaxValue(fZdPercent);
            mRightSideAxisLayer.setMinValue(-fZdPercent);

            mLineLayer.setMaxValue(fMaxVal);
            mLineLayer.setMinValue(fMinVal);

            mAvgLayer.setMaxValue(fMaxVal);
            mAvgLayer.setMinValue(fMinVal);

            mVolumeLayer.calMinAndMaxValue();

            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();
        }
    }
}
