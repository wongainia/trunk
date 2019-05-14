package cn.emoney.acg.page.share.quote;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.MinuteLinePackage;
import cn.emoney.acg.data.protocol.quote.MinuteLineReply.MinuteLine_Reply.MinuteData;
import cn.emoney.acg.data.protocol.quote.MinuteLineRequest.MinuteLine_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteLandscapePage.ShowDetailCallBack;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.view.AimLineLayer;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnActionListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap_count3;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.RightSideYAxisLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.XAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;
import cn.emoney.sky.libs.chart.layers.YAxisLayerOverlap;

public class MinuteLandscapePage extends PageImpl {

    private ShowDetailCallBack mShowDetailCB;
    private int mGoodsId;
    // 长压状态
    private boolean mbLongPressedFlag = false;
    private boolean mIsShowTradingTape = false; // 是否显示买卖五档(指数,版块不显示)
    // 增量请求
    private int nMarketTime = 0;
    private int nLastPointTime = 0;

    private static final String MIN_WIDTH = "99999.99";

    private final int MAX_MINUTE_COUNT = 240;

    private ChartView mChartView = null;
    private LinearLayout mLlTradingTape = null;
    private int mTextSize = 12;
    private YAxisLayerOverlap mAxisLineLayer = null;
    private LineLayer mAvgLayer = null;
    private LineLayer mLineLayer = null;
    private AimLineLayer mAimLineLayer = null;
    private AimLineLayer mAimVolumeLayer = null;
    private int mAimColor = Color.GRAY;
    private int mTextNormalColor = Color.GRAY;
    private RightSideYAxisLayer mRightSideAxisLayer = null;

    private YAxisLayer mAxisVolumeLayer = null;
    private ColumnarLayer mVolumeLayer = null;

    private StackLayer mLineStackLayer = null;
    private StackLayer mVolumeStackLayer = null;
    private DecimalFormat mFormat2Decimal = new DecimalFormat("0.00");
    private DecimalFormat mCurrFormat = mFormat2Decimal;

    private int mFrameBoderColor = Color.GRAY;


    // 长压时浮动价格涨跌幅坐标
    private YAxisLayer.TextAtom mTALeftPrice = new YAxisLayer.TextAtom("", 0, 28, Align.RIGHT, 0);
    private RightSideYAxisLayer.TextAtom mTARightZDF = new RightSideYAxisLayer.TextAtom("", 0, 28, Align.LEFT, 0);
    private float mLastPrice = 0.0f; // 收盘价

    // 买卖五档
    private List<FiveTradingTapeItem> mLstFiveSale = new ArrayList<FiveTradingTapeItem>();
    private List<FiveTradingTapeItem> mLstFiveBuy = new ArrayList<FiveTradingTapeItem>();
    private TextView mTvCurPrice = null;
    private TextView mTvCurZdf = null;

    // 长压显示aim时的回调
    private Bundle mShowDataBundle = new Bundle();


    // 最新价和涨跌幅
    private String mCurrentPrice = "0";
    private String mCurrentZDF = "0";

    @Override
    protected void initPage() {
        setContentView(R.layout.page_minute_landscape);

        // 特殊处理主题
        mFrameBoderColor = RColor(R.color.b5);
        mTextNormalColor = RColor(R.color.t1);
        mAimColor = RColor(R.color.b6);
        mTextSize = (int) getContext().getResources().getDimension(R.dimen.txt_s1);

        mTALeftPrice.setTextColor(mTextNormalColor);
        mTARightZDF.setTextColor(mTextNormalColor);

        mChartView = (ChartView) findViewById(R.id.minutepage_landscape_cv);
        mLlTradingTape = (LinearLayout) findViewById(R.id.minutepage_landscape_ll_trading_tape);
        mTvCurPrice = (TextView) findViewById(R.id.pageminute_landscape_item_cur_price);
        mTvCurZdf = (TextView) findViewById(R.id.pageminute_landscape_item_cur_zdf);
        if (mChartView != null) {
            // 右部涨跌百分比
            mRightSideAxisLayer = new RightSideYAxisLayer();
            mRightSideAxisLayer.setBorderWidth(0);
            mRightSideAxisLayer.setColor(RColor(R.color.t3));
            mRightSideAxisLayer.setAxisCount(2);
            mRightSideAxisLayer.setMaxValue(0.00f);
            mRightSideAxisLayer.setMinValue(0.00f);
            mRightSideAxisLayer.setAlign(Align.RIGHT);
            mRightSideAxisLayer.setMinWidthString("-99.99%");
            mRightSideAxisLayer.setTextSize(mTextSize);
            mRightSideAxisLayer.setPaddings(0, 10, 10, 10);

            mRightSideAxisLayer.setOnFormatDataListener(new RightSideYAxisLayer.OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    return DataUtils.formatFloat2Percent(val);
                }
            });


            // 左边涨跌价
            mAxisLineLayer = new YAxisLayerOverlap();

            mAxisLineLayer.setColor(RColor(R.color.t3));
            mAxisLineLayer.setAxisCount(3);
            mAxisLineLayer.setMaxValue(0.00f);
            mAxisLineLayer.setMinValue(0.00f);
            mAxisLineLayer.setAlign(Align.LEFT);
            mAxisLineLayer.setMinWidthString(MIN_WIDTH);
            mAxisLineLayer.setTextSize(mTextSize);
            mAxisLineLayer.setPaddings(10, 10, 0, 10);
            mAxisLineLayer.setOnFormatDataListener(new OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    return mCurrFormat.format(val);
                }
            });


            // 均线
            mAvgLayer = new LineLayer();
            mAvgLayer.setMaxCount(MAX_MINUTE_COUNT);
            mAvgLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mAvgLayer.setColor(RColor(R.color.sky_line_average));

            // 分时线
            mLineLayer = new LineLayer();
            mLineLayer.setMaxCount(MAX_MINUTE_COUNT);
            mLineLayer.showHGrid(3);
            mLineLayer.showVGrid(3);
            mLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mLineLayer.setColor(RColor(R.color.sky_line_minute));

            mLineLayer.setShowShadow(true);
            mLineLayer.setShadowColor(RColor(R.color.sky_line_minute_shadow));
            mLineLayer.setOnActionListener(new OnActionListener() {
                @Override
                public boolean onActionUp() {
                    // LogUtil.easylog("sky", "MinuteLandscape->onActionUp()");
                    mAimLineLayer.switchAim(false);
                    mAimVolumeLayer.switchAim(false);
                    mAxisLineLayer.switchFloatCoordinateOn(false);
                    mRightSideAxisLayer.switchFloatCoordinateOn(false);
                    mbLongPressedFlag = false;
                    if (mShowDetailCB != null) {
                        mShowDetailCB.closeDetail();
                    }

                    return true;
                }

                @Override
                public boolean onActionMove(int pos) {
                    // LogUtil.easylog("sky", "MinuteLandscape->onActionMove():"
                    // + pos);

                    if (mbLongPressedFlag) {
                        PointF pointf = mLineLayer.getPointByPos(pos);
                        float t_price = mLineLayer.getValue(pos);
                        String s_price = mCurrFormat.format(t_price);

                        mAimLineLayer.setAimPointf(pointf);
                        mAimVolumeLayer.setAimPointf(pointf);
                        mTALeftPrice.setText(s_price);
                        mTALeftPrice.setCoorDinateY(pointf.y);

                        int colorFlag = FontUtils.getColorByPrice(mLastPrice, t_price);
                        int t_colorPrice = getZDPColor(colorFlag);


                        mTALeftPrice.setTextColor(t_colorPrice);
                        mAxisLineLayer.setFloatCoordinateText(mTALeftPrice);

                        mTARightZDF.setCoorDinateY(pointf.y);
                        String sZDFPercent = "";
                        if (mLastPrice > 0) {
                            float t_offset = t_price - mLastPrice;
                            float fZDF = calZDFPercent(t_offset, mLastPrice);
                            sZDFPercent = DataUtils.formatFloat2Percent(fZDF);
                            mTARightZDF.setText(sZDFPercent);

                            int t_colorFlag = FontUtils.getColorByZDF(fZDF);
                            int t_colorZdf = getZDPColor(t_colorFlag);


                            mTARightZDF.setTextColor(t_colorZdf);
                            mRightSideAxisLayer.setFloatCoordinateText(mTARightZDF);
                        }
                        ColumnarAtom t_cAtom = mVolumeLayer.getValue(pos);
                        float t_volume = t_cAtom.mClose;
                        // LogUtil.easylog("sky", "MinLand->volume:" +
                        // t_volume);
                        int t_iVolume = (int) t_volume / 100;
                        String sVolume = DataUtils.formatVolume(t_iVolume);
                        float t_averagePrice = mAvgLayer.getValue(pos);
                        String s_averagePrice = mCurrFormat.format(t_averagePrice);

                        if (mShowDetailCB != null) {
                            mShowDataBundle.clear();
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_TIME_S, DateUtils.minutePosToTime(pos));
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_PRICE_S, s_price);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_ZDF_S, sZDFPercent);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_CJ_S, sVolume + "手");
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_JJ_S, s_averagePrice);
                            mShowDetailCB.showDetail(1, mShowDataBundle);
                        }

                        return true;
                    }

                    return false;

                }

                @Override
                public boolean onActionDown(int pos) {
                    PointF pointf = mLineLayer.getPointByPos(pos);
                    // LogUtil.easylog("sky", "MinuteLandscape->onActionDown:" +
                    // pointf.toString());
                    mAimLineLayer.switchAim(true);
                    mAimVolumeLayer.switchAim(true);
                    mAxisLineLayer.switchFloatCoordinateOn(true);
                    mRightSideAxisLayer.switchFloatCoordinateOn(true);
                    mbLongPressedFlag = true;
                    mAimLineLayer.setAimPointf(pointf);
                    mAimVolumeLayer.setAimPointf(pointf);

                    float t_price = mLineLayer.getValue(pos);
                    String s_price = mCurrFormat.format(t_price);

                    mTALeftPrice.setCoorDinateY(pointf.y);
                    mTALeftPrice.setText(s_price);

                    int t_colorPrice = mTextNormalColor;
                    int colorFlag = FontUtils.getColorByPrice(mLastPrice, t_price);
                    if (colorFlag != FontUtils.COLOR_EQUAL) {
                        t_colorPrice = getZDPColor(colorFlag);
                    }


                    mTALeftPrice.setTextColor(t_colorPrice);
                    mAxisLineLayer.setFloatCoordinateText(mTALeftPrice);

                    mTARightZDF.setCoorDinateY(pointf.y);
                    String sZDFPercent = "";
                    if (mLastPrice > 0) {
                        float t_offset = t_price - mLastPrice;
                        float fZDF = calZDFPercent(t_offset, mLastPrice);
                        sZDFPercent = DataUtils.formatFloat2Percent(fZDF);

                        int t_colorZdf = mTextNormalColor;
                        int t_colorFlag = FontUtils.getColorByZDF(fZDF);
                        if (t_colorFlag != FontUtils.COLOR_EQUAL) {
                            t_colorZdf = getZDPColor(t_colorFlag);
                        }

                        mTARightZDF.setTextColor(t_colorZdf);
                        mTARightZDF.setText(sZDFPercent);
                        mRightSideAxisLayer.setFloatCoordinateText(mTARightZDF);
                    }

                    ColumnarAtom t_cAtom = mVolumeLayer.getValue(pos);
                    float t_volume = t_cAtom.mClose;
                    // LogUtil.easylog("sky", "MinLand->volume:" + t_volume);
                    int t_iVolume = (int) t_volume / 100;
                    String sVolume = DataUtils.formatVolume(t_iVolume);
                    float t_averagePrice = mAvgLayer.getValue(pos);
                    String s_averagePrice = mCurrFormat.format(t_averagePrice);

                    if (mShowDetailCB != null) {
                        mShowDataBundle.clear();
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_TIME_S, DateUtils.minutePosToTime(pos));
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_PRICE_S, s_price);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_ZDF_S, sZDFPercent);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_CJ_S, sVolume + "手");
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_JJ_S, s_averagePrice);
                        mShowDetailCB.showDetail(1, mShowDataBundle); // type:分时详情
                    }

                    return true;
                }
            });

            // 十字瞄准线
            mAimLineLayer = new AimLineLayer();
            mAimLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 0.5f));
            mAimLineLayer.setColor(mAimColor);
            mAimLineLayer.setIsShowHLine(true);
            mAimLineLayer.setIsShowVLine(true);
            mAimLineLayer.setAimPointRadius(4);

            // 层栈
            mLineStackLayer = new StackLayer();
            mLineStackLayer.setPaddings(0, 10, 0, 1);
            mLineStackLayer.addLayer(mAvgLayer);
            mLineStackLayer.addLayer(mLineLayer);
            mLineStackLayer.addLayer(mAimLineLayer);
            mLineStackLayer.setShowBorder(true);
            mLineStackLayer.setMiddleLineIsFull(true);
            mLineStackLayer.setBorderWidth(1);
            mLineStackLayer.setBorderColor(mFrameBoderColor);


            // 上部group
            GroupLayerOverlap_count3 mLineGroupLayer = new GroupLayerOverlap_count3();
            mLineGroupLayer.setLeftLayer(mAxisLineLayer);
            mLineGroupLayer.setCenterLayer(mLineStackLayer);
            mLineGroupLayer.setRightLayer(mRightSideAxisLayer);
            mLineGroupLayer.setHeightPercent(0.73f);
            mChartView.addLayer(mLineGroupLayer);


            // 下部左边量的坐标
            mAxisVolumeLayer = new YAxisLayer();
            mAxisVolumeLayer.setDrawTailCoordinate(false);
            mAxisVolumeLayer.setColor(RColor(R.color.t3));
            mAxisVolumeLayer.setAxisCount(2);
            mAxisVolumeLayer.setMaxValue(0.00f);
            mAxisVolumeLayer.setMinValue(0.00f);
            mAxisVolumeLayer.setAlign(Align.LEFT);
            mAxisVolumeLayer.setPaddings(10, 10, 0, 10);
            mAxisVolumeLayer.setMinWidthString(MIN_WIDTH);
            mAxisVolumeLayer.setTextSize(mTextSize);
            final DecimalFormat format = new DecimalFormat("0.0");
            mAxisVolumeLayer.setOnFormatDataListener(new OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    if (val > 1000000) {
                        return format.format(val / 100000000) + "亿";
                    } else if (val > 10000) {
                        return format.format(val / 10000) + "万";
                    }
                    return format.format(val);
                }
            });

            // 成交量
            mVolumeLayer = new ColumnarLayer();
            mVolumeLayer.showHGrid(1);
            mVolumeLayer.showVGrid(3);
            mVolumeLayer.setPaddings(0, 3, 0, 0);

            mVolumeLayer.setMaxCount(MAX_MINUTE_COUNT);
            mVolumeLayer.setColumnarWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setColor(RColor(R.color.sky_line_volume));

            // 量的十字瞄准线
            mAimVolumeLayer = new AimLineLayer();
            mAimVolumeLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 0.5f));
            mAimVolumeLayer.setColor(mAimColor);
            mAimVolumeLayer.setIsShowVLine(true);

            // 量的层栈
            mVolumeStackLayer = new StackLayer();
            mVolumeStackLayer.addLayer(mVolumeLayer);
            mVolumeStackLayer.addLayer(mAimVolumeLayer);
            mVolumeStackLayer.setShowBorder(true);
            mVolumeStackLayer.setBorderColor(mFrameBoderColor);
            mVolumeStackLayer.setBorderWidth(1);

            // 下部组
            GroupLayerOverlap mVolumeGroupLayer = new GroupLayerOverlap();
            mVolumeGroupLayer.setLeftLayer(mAxisVolumeLayer);
            mVolumeGroupLayer.setRightLayer(mVolumeStackLayer);
            mVolumeGroupLayer.setHeightPercent(0.27f);
            
            mChartView.addLayer(mVolumeGroupLayer);


            XAxisLayer mXAxisLayer = new XAxisLayer();

            mXAxisLayer.setColor(RColor(R.color.t3));
            mXAxisLayer.addValue("9:30");
            mXAxisLayer.addValue("11:30 13:00");
            mXAxisLayer.addValue("15:00");
            mXAxisLayer.setTextSize(mTextSize);

            mChartView.addLayer(mXAxisLayer);

        }

        mLstFiveBuy.clear();
        mLstFiveSale.clear();

        for (int i = 1; i <= 5; i++) {
            int iItemId = getResIdByStr("id", "pageminute_landscape_item_sale", i);

            View tLLItem = findViewById(iItemId);
            TextView tvItemName = (TextView) tLLItem.findViewById(R.id.trading_tape_item_name);
            tvItemName.setText("卖 " + i);
            TextView tvItemPrice = (TextView) tLLItem.findViewById(R.id.trading_tape_item_price);
            TextView tvItemCount = (TextView) tLLItem.findViewById(R.id.trading_tape_item_count);
            FiveTradingTapeItem fttItem = new FiveTradingTapeItem(tLLItem, tvItemName, tvItemPrice, tvItemCount);

            mLstFiveSale.add(fttItem);
        }

        for (int i = 1; i <= 5; i++) {
            int iItemId = getResIdByStr("id", "pageminute_landscape_item_buy", i);
            View tLLItem = findViewById(iItemId);
            TextView tvItemName = (TextView) tLLItem.findViewById(R.id.trading_tape_item_name);
            tvItemName.setText("买 " + i);
            TextView tvItemPrice = (TextView) tLLItem.findViewById(R.id.trading_tape_item_price);
            TextView tvItemCount = (TextView) tLLItem.findViewById(R.id.trading_tape_item_count);
            FiveTradingTapeItem fttItem = new FiveTradingTapeItem(tLLItem, tvItemName, tvItemPrice, tvItemCount);

            mLstFiveBuy.add(fttItem);
        }
    }

    @Override
    protected void initData() {

    }


    public void setGoodsId(int goodsId) {
        LogUtil.easylog("sky", "MinuteLand->setGoodsId():" + goodsId);
        mGoodsId = goodsId;

        if (DataUtils.IsZS(mGoodsId) || DataUtils.IsBK(mGoodsId)) {
            mIsShowTradingTape = false;
            if (mLlTradingTape != null) {
                mLlTradingTape.setVisibility(View.GONE);
            }
        } else {
            mIsShowTradingTape = true;
            if (mLlTradingTape != null) {
                mLlTradingTape.setVisibility(View.VISIBLE);
            }
        }
    }


    // 分母限制大于0
    public static float calZDFPercent(float offset, float orgiPrice) {
        if (orgiPrice <= 0) {
            return 0.0f;
        }
        BigDecimal b_offset = new BigDecimal(offset);
        BigDecimal b_orgiPrice = new BigDecimal(orgiPrice);
        BigDecimal percent = b_offset.divide(b_orgiPrice, 4, BigDecimal.ROUND_HALF_UP);
        return percent.floatValue();
    }

    public void setCurrentPrice(String price, String zdf) {
        if (mTvCurPrice != null) {
            mTvCurPrice.setText(price);
            mTvCurZdf.setText(zdf);
            int t_color = getZDPColor(FontUtils.getColorByZDF_percent(zdf));
            mTvCurPrice.setTextColor(t_color);
            mTvCurZdf.setTextColor(t_color);
        }
    }

    public void requestData() {
        if (mbLongPressedFlag) {
            return;
        }

        if (mGoodsId == 0) {
            return;
        }
        LogUtil.easylog("sky", "MinuteLandscape->requestData->goodid:" + mGoodsId);
        MinuteLinePackage pkg = new MinuteLinePackage(new QuoteHead((short) 0));
        pkg.setRequest(MinuteLine_Request.newBuilder().setRequestMmp(mIsShowTradingTape).setLastRecvTime(nLastPointTime).setLastUpdateMarketDate(nMarketTime).setGoodsId(mGoodsId).build());
        requestQuote(pkg, IDUtils.MinuteLine);
    }


    public void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof MinuteLinePackage) {
            MinuteLinePackage minuteLinePkg = (MinuteLinePackage) pkg;
            nMarketTime = minuteLinePkg.getResponse().getMarketDate();

            float lastPrice = minuteLinePkg.getResponse().getClose() / 1000.0f;
            if (lastPrice > 0) {
                mLastPrice = lastPrice;
            }

            if (mIsShowTradingTape) {
                // [卖价5--卖价1,买价1---买价5,卖量5---卖量1,买量1-买量5]
                List<Integer> t_lstMmp = minuteLinePkg.getResponse().getMmpList();
                // LogUtil.easylog("sky", "MinuteLand->mmp:" + t_lstMmp.toString());
                if (t_lstMmp.size() >= 20 && mLstFiveBuy.size() == 5 && mLstFiveSale.size() == 5) {
                    int i = 0;
                    for (int j = mLstFiveSale.size() - 1; j >= 0; j--) {
                        float tPrice = t_lstMmp.get(i++) / 1000f;
                        int tColor = getZDPColor(FontUtils.getColorByPrice(mLastPrice, tPrice));
                        if (tPrice > 0) {
                            mLstFiveSale.get(j).mTvPrice.setText(mCurrFormat.format(tPrice));
                            mLstFiveSale.get(j).mTvCount.setText(DataUtils.formatVolume(t_lstMmp.get(i + 9) / 100));
                        } else {
                            mLstFiveSale.get(j).mTvPrice.setText("--");
                            mLstFiveSale.get(j).mTvCount.setText("--");
                        }
                        mLstFiveSale.get(j).mTvPrice.setTextColor(tColor);
                    }
                    for (int j = 0; j < mLstFiveBuy.size(); j++) {
                        float tPrice = t_lstMmp.get(i++) / 1000f;
                        int tColor = getZDPColor(FontUtils.getColorByPrice(mLastPrice, tPrice));
                        if (tPrice > 0) {
                            mLstFiveBuy.get(j).mTvPrice.setText(mCurrFormat.format(tPrice));
                            mLstFiveBuy.get(j).mTvCount.setText(DataUtils.formatVolume(t_lstMmp.get(i + 9) / 100));
                        } else {
                            mLstFiveBuy.get(j).mTvPrice.setText("--");
                            mLstFiveBuy.get(j).mTvCount.setText("--");
                        }
                        mLstFiveBuy.get(j).mTvPrice.setTextColor(tColor);
                    }
                }

            }

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

            LogUtil.easylog("sky", "MinuteLandscape->updateFromQuote->lstData.size:" + lstData.size());

            for (int i = 0; i < lstData.size(); i++) {
                MinuteData minuteData = lstData.get(i);

                float price = minuteData.getPrice();
                float avgPrice = minuteData.getAve();
                int pointTime = minuteData.getTime();
                long t_volume = minuteData.getVolume();
                // 防止意外情况益出
                int valueCount = mLineLayer.getValueCount();
                if (valueCount >= MAX_MINUTE_COUNT) {
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

                    fZdPercent = calZDFPercent(offset, lastPrice);
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
            mAxisVolumeLayer.setMaxValue(mVolumeLayer.getMaxValue());
            mAxisVolumeLayer.setMinValue(0);

            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();

        }
    }


    private class FiveTradingTapeItem {
        private View mView = null;
        private TextView mTvName = null;
        private TextView mTvPrice = null;
        private TextView mTvCount = null;

        public FiveTradingTapeItem(View view, TextView tvName, TextView tvPrice, TextView tvCount) {
            mView = view;
            mTvName = tvName;
            mTvPrice = tvPrice;
            mTvCount = tvCount;
        }

        public View getView() {
            return mView;
        }

        public void setView(View view) {
            this.mView = view;
        }

        public TextView getTvName() {
            return mTvName;
        }

        public void setTvName(TextView tvName) {
            this.mTvName = tvName;
        }

        public TextView getTvPrice() {
            return mTvPrice;
        }

        public void setTvPrice(TextView tvPrice) {
            this.mTvPrice = tvPrice;
        }

        public TextView getTvCount() {
            return mTvCount;
        }

        public void setTvCount(TextView tvCount) {
            this.mTvCount = tvCount;
        }
    }

    public void setShowDetailCB(QuoteLandscapePage.ShowDetailCallBack callBack) {
        mShowDetailCB = callBack;
    }
}
