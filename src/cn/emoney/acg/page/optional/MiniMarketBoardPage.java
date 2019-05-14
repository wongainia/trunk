package cn.emoney.acg.page.optional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.data.protocol.quote.MinuteLinePackage;
import cn.emoney.acg.data.protocol.quote.MinuteLineReply.MinuteLine_Reply.MinuteData;
import cn.emoney.acg.data.protocol.quote.MinuteLineRequest.MinuteLine_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap_count3;
import cn.emoney.sky.libs.chart.layers.GroupLayer_shell;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.RightSideYAxisLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayerOverlap;

public class MiniMarketBoardPage extends PageImpl {

    private static final int MAX_MINUTE_COUNT = 240;
    private static final int ZS_SH = 1; // 上证指数
    private static final int ZS_SZ = 1399001; // 深证成指
    private static final int ZS_CY = 1399006; // 创业板指

    private TextView mTvZXJ = null;
    private TextView mTvZDF = null;
    private TextView mTvAmount = null;

    private ChartView mChartView = null;
    private LineLayer mAvgLayer = null;
    private LineLayer mLineLayer = null;
    private RightSideYAxisLayer mRightSideAxisLayer = null;

    private ColumnarLayer mVolumeLayer = null;
    private MiniMarketBoardCB mCallback = null;
    private StackLayer mLineStackLayer = null;
    private DecimalFormat mCurrFormat = new DecimalFormat("0.00");
    private int mFrameBoderColor = Color.GRAY;
    private int mTextSize = 10;

    private int mGoodsId = ZS_SH;
    // 增量请求
    private int nMarketTime = 0;
    private int nLastPointTime = 0;

    private boolean mIsHide = true;

    private View vBtnClose = null;
    private RadioGroup radioGroupZSBtn = null;

    private class DynaValue {
        String price = "";
        String amount = "";
        String zdf = "";
    }

    private Map<Integer, DynaValue> mMapDynaValue = new HashMap<Integer, DynaValue>(3);

    public void setCallback(MiniMarketBoardPage.MiniMarketBoardCB callback) {
        mCallback = callback;
    }

    private void resetChartView() {
        if (mAvgLayer != null) {
            mAvgLayer.resetData();
            mAvgLayer.setMaxCount(MAX_MINUTE_COUNT);
        }
        if (mLineLayer != null) {
            mLineLayer.resetData();
            mLineLayer.setMaxCount(MAX_MINUTE_COUNT);
        }
        if (mVolumeLayer != null) {
            mVolumeLayer.resetData();
            mVolumeLayer.setMaxCount(MAX_MINUTE_COUNT);
        }
        mChartView.forceAdjustLayers();
        mChartView.postInvalidate();
    }

    private void refreshDynaDisplay() {
        if (mMapDynaValue.containsKey(mGoodsId)) {
            DynaValue value = mMapDynaValue.get(mGoodsId);

            int zdpFlag = FontUtils.getColorByZDF_percent(value.zdf);
            int zdfColor = getZDPColor(zdpFlag);

            if (mTvZXJ != null) {
                mTvZXJ.setText(value.price);
                mTvZXJ.setTextColor(zdfColor);
            }
            if (mTvZDF != null) {
                mTvZDF.setText(value.zdf);
                mTvZDF.setTextColor(zdfColor);
            }
            if (mTvAmount != null) {
                mTvAmount.setText(value.amount);
            }
        }
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_minimarket_board_expand);

        getContentView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        vBtnClose = findViewById(R.id.iv_mini_marketboard_btn_unexpand);

        mTvZXJ = (TextView) findViewById(R.id.tv_mini_marketboard_zxj);
        mTvZDF = (TextView) findViewById(R.id.tv_mini_marketboard_zdf);
        mTvAmount = (TextView) findViewById(R.id.tv_mini_marketboard_amount);

        radioGroupZSBtn = (RadioGroup) findViewById(R.id.radiogroup_mini_marketboard_btn_content);

        radioGroupZSBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.tv_mini_marketboard_btn_sh:
                        mGoodsId = ZS_SH;

                        break;
                    case R.id.tv_mini_marketboard_btn_sz:
                        mGoodsId = ZS_SZ;

                        break;
                    case R.id.tv_mini_marketboard_btn_cy:
                        mGoodsId = ZS_CY;
                        break;

                    default:
                        break;

                }
                refreshDynaDisplay();

                nLastPointTime = 0;
                nMarketTime = 0;
                resetChartView();
                requestData();
            }
        });

        vBtnClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onCloseBtnClicked();
                }
            }
        });

        // 特殊处理主题
        mFrameBoderColor = RColor(R.color.b5);

        mChartView = (ChartView) findViewById(R.id.cv_mini_marketboard);
        if (mChartView != null) {
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

            mAvgLayer = new LineLayer();

            mAvgLayer.setMaxCount(MAX_MINUTE_COUNT);
            mAvgLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mAvgLayer.setColor(RColor(R.color.sky_line_average));

            mLineLayer = new LineLayer();

            mLineLayer.setMaxCount(MAX_MINUTE_COUNT);
            mLineLayer.showHGrid(3);
            mLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setColor(RColor(R.color.sky_line_minute));
            mLineStackLayer = new StackLayer();

            mLineStackLayer.setPaddings(0, 10, 0, 10);
            mLineStackLayer.addLayer(mAvgLayer);
            mLineStackLayer.addLayer(mLineLayer);
            mLineStackLayer.setShowBorder(true);
            mLineStackLayer.setMiddleLineIsFull(false);
            mLineStackLayer.setBorderWidth(1);
            mLineStackLayer.setBorderColor(mFrameBoderColor);

            GroupLayerOverlap_count3 mLineGroupLayer = new GroupLayerOverlap_count3();

            YAxisLayerOverlap leftYAxisLayer = new YAxisLayerOverlap();
            leftYAxisLayer.show(false);
            mLineGroupLayer.setLeftLayer(leftYAxisLayer);
            mLineGroupLayer.setCenterLayer(mLineStackLayer);
            mLineGroupLayer.setRightLayer(mRightSideAxisLayer);
            mLineGroupLayer.setHeightPercent(0.74f);
            mChartView.addLayer(mLineGroupLayer);

            mVolumeLayer = new ColumnarLayer();
            mVolumeLayer.showHGrid(1);
            mVolumeLayer.setPaddings(0, 3, 0, 0);
            mVolumeLayer.setShowBorder(true);
            mVolumeLayer.setBorderWidth(1);
            mVolumeLayer.setBorderColor(mFrameBoderColor);

            mVolumeLayer.setMaxCount(MAX_MINUTE_COUNT);
            mVolumeLayer.setColumnarWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.showHGrid(1);
            mVolumeLayer.setColor(RColor(R.color.sky_line_volume));

            GroupLayer_shell groupLayer_shell = new GroupLayer_shell();
            groupLayer_shell.setContenLayer(mVolumeLayer);
            groupLayer_shell.setHeightPercent(0.26f);

            mChartView.addLayer(groupLayer_shell);
        }

    }

    public void setIsHide(boolean bHide) {
        mIsHide = bHide;
    }

    public void requestData() {
        if (mGoodsId == 0 || mIsHide == true) {
            return;
        }

        requestMinuteLine();
        requestDynaValue();
    }

    private void requestMinuteLine() {
        MinuteLinePackage pkg = new MinuteLinePackage(new QuoteHead((short) 0));
        pkg.setRequest(MinuteLine_Request.newBuilder().setLastRecvTime(nLastPointTime).setLastUpdateMarketDate(nMarketTime).setGoodsId(mGoodsId).build());
        requestQuote(pkg, IDUtils.MinuteLine);
    }

    private void requestDynaValue() {
        ArrayList<Integer> lstGoodsId = new ArrayList<Integer>(3);
        lstGoodsId.add(ZS_SH);
        lstGoodsId.add(ZS_SZ);
        lstGoodsId.add(ZS_CY);

        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.ZXJ);
        goodsFiled.add(GoodsParams.AMOUNT);
        goodsFiled.add(GoodsParams.ZDF);
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 1));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4).setGroupType(0).addAllGoodsId(lstGoodsId).addAllReqFields(goodsFiled)
        // -9999 代表不排序
                .setSortField(-9999).setSortOrder(false).setReqBegin(0).setReqSize(3).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    @Override
    protected void initData() {
        
    }

    public static interface MiniMarketBoardCB {
        public void onCloseBtnClicked();
    }

    public void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof MinuteLinePackage) {
            MinuteLinePackage minuteLinePkg = (MinuteLinePackage) pkg;
            nMarketTime = minuteLinePkg.getResponse().getMarketDate();
            List<MinuteData> lstData = minuteLinePkg.getResponse().getTrendLineList();

            if (lstData.size() <= 0) {
                return;
            }

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

                long t_volume = minuteData.getVolume();

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

        else if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage dynaValueDataPackage = (DynaValueDataPackage) pkg;
            DynaValueData_Reply gr = dynaValueDataPackage.getResponse();

            if (gr == null || gr.getRepFieldsList().size() == 0 || gr.getQuotaValueList().size() == 0) {
                return;
            }

            int tCount = gr.getQuotaValueCount();
            if (tCount > 0) {
                int indexZXJ = gr.getRepFieldsList().indexOf(GoodsParams.ZXJ);
                int indexAmount = gr.getRepFieldsList().indexOf(GoodsParams.AMOUNT);
                int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);
                List<DynaQuota> lstQuotas = gr.getQuotaValueList();
                for (int i = 0; i < tCount; i++) {
                    DynaQuota quota = lstQuotas.get(i);
                    int tGoodid = quota.getGoodsId();
                    String price = quota.getRepFieldValueList().get(indexZXJ);
                    String zdf = quota.getRepFieldValueList().get(indexZDF);
                    String amount = quota.getRepFieldValueList().get(indexAmount);

                    if (mMapDynaValue.containsKey(tGoodid)) {
                        DynaValue value = mMapDynaValue.get(tGoodid);
                        value.price = DataUtils.getPrice(price);
                        value.amount = DataUtils.formatAmount(amount);
                        value.zdf = DataUtils.getSignedZDF(zdf);
                    } else {
                        DynaValue value = new DynaValue();
                        value.price = DataUtils.getPrice(price);
                        value.amount = DataUtils.formatAmount(amount);
                        value.zdf = DataUtils.getSignedZDF(zdf);
                        mMapDynaValue.put(tGoodid, value);
                    }
                }

                refreshDynaDisplay();

            }

        }
    }
}
