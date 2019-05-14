package cn.emoney.acg.page.share.quote;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDIndicator;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.IndicatorColor;
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
import cn.emoney.acg.data.protocol.quote.IndicatorPackage;
import cn.emoney.acg.data.protocol.quote.IndicatorReply.Indicator_Reply;
import cn.emoney.acg.data.protocol.quote.IndicatorReply.line;
import cn.emoney.acg.data.protocol.quote.IndicatorReply.point;
import cn.emoney.acg.data.protocol.quote.IndicatorRequest.Indicator_Request;
import cn.emoney.acg.dialog.CustomProgressDialog.OnCancelProgressDiaListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.EquipmentData;
import cn.emoney.acg.page.equipment.SupportEquipment;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.ViewUtil;
import cn.emoney.acg.view.AimLineLayer;
import cn.emoney.acg.view.BSTopLayer;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.ChartView.OnZoomActionListener;
import cn.emoney.sky.libs.chart.layers.ChartLayer;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnActionListener;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnDrawingListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.FixedWidthColumnarLayer;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.ReferenceLineLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.XAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;

import com.google.protobuf.GeneratedMessage;

public class KLineLandscapePage extends PageImpl implements OnCancelProgressDiaListener {

    private static final String MIN_WIDTH = "1000.00亿";

    private ChartView mChartView = null;
    private ColumnarLayer mKLineLayer = null;
    private YAxisLayer mKLineAxisLayer = null;
    private BSTopLayer mBSTopLayer = null;
    private AimLineLayer mAimLineLayer = null;
    private StackLayer mKLineStackLayer = null;


    // 指示通用
    private AimLineLayer mAimIndicatorLayer = null; // 指标瞄准线
    private StackLayer mIndicatorStackLayer = null; // 指标层栈
    private YAxisLayer mIndicatorAxisLayer = null; // 指标纵坐标
    private XAxisLayer mXAxisLayer = null; // 指标横坐标

    // 指标1 成交量
    private ColumnarLayer mVolumeLayer = null;

    // // 指标2 MACD
    // private FixedWidthColumnarLayer mMACD_StickLayer = null;
    // private LineLayer mMACD_DIFLineLayer = null;
    // private LineLayer mMACD_DEALineLayer = null;
    //
    // // 指标3 KDJ
    // private LineLayer mKDJ_KLineLayer = null;
    // private LineLayer mKDJ_DLineLayer = null;
    // private LineLayer mKDJ_JLineLayer = null;

    // private List<ChartLayer> mLstIndicatorLayer = new ArrayList<ChartLayer>();


    private int TEXTSIZE = 12;
    private int KLINE_COUNT = 30;
    private static int mChartViewWidth = 0;

    private final static float COLUMN_WIDTH = 4;
    private final static float MAX_COLUMN_WIDTH = 12;
    private final static float MID_COLUMN_WIDTH = 1.7f;
    private final static float MIN_COLUMN_WIDTH = 1;
    private DecimalFormat mFormat2Decimal = new DecimalFormat("0.00");
    private DecimalFormat mCurrFormat = mFormat2Decimal;

    private float mColumnWidth = 0;
    private float mMinColumnWidth = 0;
    private float mMidColumnWidth = 0;
    private float mMaxColumnWidth = 0;
    private boolean mIsLine = false;

    private LineLayer mMA5Layer = null;
    private LineLayer mMA10Layer = null;
    private LineLayer mMA20Layer = null;


    private int mAimColor = Color.GRAY;

    private int mFrameBoderColor = Color.GRAY;
    private int mCurrPeriod = 0;
    private int mGoodsId = 0;
    private float mBaseWidth = 5;
    private int mBaseMinMovePos = 1;

    // 左右滑动
    private int mStartMovePos = -1;

    // zoom
    private boolean mIsZooming = false;

    // 长压显示aim时的回调
    private QuoteLandscapePage.ShowDetailCallBack mShowDetailCB = null;
    private Bundle mShowDataBundle = new Bundle();
    // 长压瞄准
    private boolean mbLongPressedFlag = false;

    /**
     * 是否第一次请求网络
     * */
    private boolean isFirstRequest = true;

    private RadioGroup mRadioGroup_indexes = null;
    private int mCurCheckedIndexes_tvId = R.id.btn_indexes_CJL;
    private String mIndicatorName = "成交量";

    private Map<String, GeneratedMessage> mMapIndicatorCache = new HashMap<String, GeneratedMessage>();

    @Override
    protected void initPage() {
        setContentView(R.layout.page_kline_landscape);

        mColumnWidth = FontUtils.dip2px(getContext(), COLUMN_WIDTH);
        mMinColumnWidth = FontUtils.dip2px(getContext(), MIN_COLUMN_WIDTH);
        mMaxColumnWidth = FontUtils.dip2px(getContext(), MAX_COLUMN_WIDTH);
        mMidColumnWidth = FontUtils.dip2px(getContext(), MID_COLUMN_WIDTH);
        TEXTSIZE = (int) getContext().getResources().getDimension(R.dimen.txt_s1);

        mChartViewWidth = FontUtils.px2dip(getContext(), DataModule.SCREEN_HEIGHT) - 112;

        KLINE_COUNT = (int) (mChartViewWidth / mBaseWidth);
        LogUtil.easylog("klinepage->KLINE_COUNT:" + KLINE_COUNT);
        mChartView = (ChartView) findViewById(R.id.klinepage_landscape_cv);

        // 特殊处理主题
        mFrameBoderColor = RColor(R.color.b5);
        mAimColor = RColor(R.color.b6);

        if (mChartView != null) {
            mChartView.setOnZoomActionListener(new OnZoomActionListener() {
                @Override
                public void onStartZoom() {
                    if (mbLongPressedFlag) {
                        return;
                    }
                    mAimLineLayer.switchAim(false);
                    mIsZooming = true;
                }

                @Override
                public void onFinishZoom(float rate) {
                    if (mbLongPressedFlag) {
                        return;
                    }
                    mIsZooming = false;

                    float columnWidth = rate * mColumnWidth;
                    if (columnWidth < mMinColumnWidth) {
                        mBaseWidth = mMinColumnWidth / mColumnWidth * mBaseWidth;
                        mColumnWidth = mMinColumnWidth;
                    } else if (columnWidth > mMaxColumnWidth) {
                        mBaseWidth = mMaxColumnWidth / mColumnWidth * mBaseWidth;
                        mColumnWidth = mMaxColumnWidth;
                    } else {
                        mColumnWidth = columnWidth;
                        mBaseWidth = rate * mBaseWidth;
                    }
                    KLINE_COUNT = (int) (mChartViewWidth / mBaseWidth);
                    mChartView.forceAdjustLayers();

                }

                @Override
                public void onZooming(float rate) {
                    if (mbLongPressedFlag) {
                        return;
                    }
                    mIsZooming = true;
                    float columnWidth = rate * mColumnWidth;

                    if (columnWidth >= mMinColumnWidth && columnWidth <= mMaxColumnWidth) {
                        float baseWidth = rate * mBaseWidth;
                        KLINE_COUNT = (int) (mChartViewWidth / baseWidth);
                        boolean isLine = columnWidth < mMidColumnWidth;
                        mKLineLayer.setColumnarWidth(columnWidth);
                        mKLineLayer.change2Line(isLine);
                        // //隐藏线
                        // mMA5Layer.show(!isLine);
                        // mMA10Layer.show(!isLine);
                        // mMA20Layer.show(!isLine);

                        doChangeColumnWidth(columnWidth, isLine);
                    } else {
                        if (columnWidth < mMinColumnWidth) {
                            float baseWidth = mMinColumnWidth / mColumnWidth * mBaseWidth;
                            KLINE_COUNT = (int) (mChartViewWidth / baseWidth);
                            mKLineLayer.setColumnarWidth(mMinColumnWidth);
                            mKLineLayer.change2Line(true);
                            // //隐藏线
                            // mMA5Layer.show(false);
                            // mMA10Layer.show(false);
                            // mMA20Layer.show(false);

                            doChangeColumnWidth(mMinColumnWidth, true);
                        }
                    }

                    resetLines();

                    mChartView.forceAdjustLayers();

                }

            });

            // k线区线左方坐标
            mKLineAxisLayer = new YAxisLayer();
            mKLineAxisLayer.setAxisCount(4);
            // mKLineAxisLayer.setPutCoordinateAboveLine(true);
            mKLineAxisLayer.setMaxValue(0.00f);
            mKLineAxisLayer.setMinValue(0.00f);
            mKLineAxisLayer.setColor(RColor(R.color.t3));
            mKLineAxisLayer.setAlign(Align.LEFT);
            mKLineAxisLayer.setPaddings(10, 5, 0, 5);
            mKLineAxisLayer.setMinWidthString(MIN_WIDTH);
            mKLineAxisLayer.setTextSize(TEXTSIZE);

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

            mKLineLayer.setOnActionListener(new OnActionListener() {
                @Override
                public boolean onActionUp() {
                    LogUtil.easylog("sky", "KLineScape->onActionUp");
                    mbLongPressedFlag = false;
                    mAimLineLayer.switchAim(false);
                    mAimIndicatorLayer.switchAim(false);
                    mStartMovePos = -1;

                    if (mShowDetailCB != null) {
                        mShowDetailCB.closeDetail();
                    }

                    mChartView.forceAdjustLayers();
                    return true;
                }

                @Override
                public boolean onActionMove(int pos) {
                    LogUtil.easylog("sky", "KLineScape->onActionMove:" + pos);
                    if (mIsZooming) {
                        return false;
                    }

                    if (mIsLine) {
                        return false;
                    }

                    // 瞄准线
                    if (mbLongPressedFlag) {
                        PointF pointf = mKLineLayer.getPointByPos(pos);
                        mAimLineLayer.setAimPointf(pointf);
                        mAimIndicatorLayer.setAimPointf(pointf);

                        ColumnarAtom t_cAtom = mKLineLayer.getValue(pos);
                        ColumnarAtom t_preCAtom = null;
                        if (pos >= 1) {
                            t_preCAtom = mKLineLayer.getValue(pos - 1);
                        }
                        float t_fHigh = t_cAtom.mHigh;
                        float t_fOpen = t_cAtom.mOpen;
                        float t_fLow = t_cAtom.mLow;
                        float t_fClose = t_cAtom.mClose;
                        String sHigh = mCurrFormat.format(t_fHigh);
                        String sOpen = mCurrFormat.format(t_fOpen);
                        String sLow = mCurrFormat.format(t_fLow);
                        String sClose = mCurrFormat.format(t_fClose);

                        float t_preClose = 0;
                        if (t_preCAtom != null) {
                            t_preClose = t_preCAtom.mClose;
                        }
                        String sPreClose = mCurrFormat.format(t_preClose);

                        float t_fOffset = 0;
                        float t_fZDF = 0;
                        if (t_preClose > 0) {
                            t_fOffset = t_fClose - t_preClose;
                            t_fZDF = calZDFPercent(t_fOffset, t_preClose);
                        } else {
                            t_fOffset = t_fClose - t_fOpen;
                            t_fZDF = calZDFPercent(t_fOffset, t_fOpen);
                        }

                        String sZDF = DataUtils.formatFloat2Percent(t_fZDF);

                        int t_iDate = (Integer) t_cAtom.mTag;

                        String sDate = "";
                        if (mCurrPeriod == TYPE_60MINUTE || mCurrPeriod == TYPE_30MINUTE || mCurrPeriod == TYPE_15MINUTE) {
                            String tDate = DataUtils.formatDateY_M_D_HHmm(String.valueOf(t_iDate), "/");
                            String[] aryDate = tDate.split("/");
                            if (aryDate != null && aryDate.length == 4) {
                                sDate = aryDate[2] + "/" + aryDate[3];
                            }
                        } else {
                            sDate = DataUtils.formatDateY_M_D(String.valueOf(t_iDate), "-");
                        }

                        if (mShowDetailCB != null) {
                            mShowDataBundle.clear();
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_TIME_S, sDate);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_HIGH_S, sHigh);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_OPEN_S, sOpen);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_LOW_S, sLow);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_CLOSE_S, sClose);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_ZDF_S, sZDF);
                            mShowDataBundle.putString(QuoteLandscapePage.KEY_PRE_CLOSE_S, sPreClose);
                            mShowDetailCB.showDetail(2, mShowDataBundle); // type:K线详情
                        }

                        return true;
                    }
                    // 左右拖动
                    else {
                        if (mStartMovePos == -1) {
                            mStartMovePos = pos;
                        }
                        int offset = pos - mStartMovePos;

                        if (Math.abs(offset) < mBaseMinMovePos) {
                            return false;
                        }
                        mKLineLayer.moveStartPos(-offset);
                        mMA5Layer.moveStartPos(-offset);
                        mMA10Layer.moveStartPos(-offset);
                        mMA20Layer.moveStartPos(-offset);


                        doMoveStartPos(-offset);

                        resetLines();

                        mChartView.forceAdjustLayers();
                        return true;
                    }

                }

                @Override
                public boolean onActionDown(int pos) {
                    LogUtil.easylog("sky", "KLineScape->onActionDown:" + pos);
                    mbLongPressedFlag = true;
                    mAimLineLayer.switchAim(true);
                    mAimIndicatorLayer.switchAim(true);
                    PointF pointf = mKLineLayer.getPointByPos(pos);
                    mAimLineLayer.setAimPointf(pointf);
                    mAimIndicatorLayer.setAimPointf(pointf);

                    ColumnarAtom t_cAtom = mKLineLayer.getValue(pos);
                    ColumnarAtom t_preCAtom = null;
                    if (pos >= 1) {
                        t_preCAtom = mKLineLayer.getValue(pos - 1);
                    }
                    float t_fHigh = t_cAtom.mHigh;
                    float t_fOpen = t_cAtom.mOpen;
                    float t_fLow = t_cAtom.mLow;
                    float t_fClose = t_cAtom.mClose;
                    String sHigh = mCurrFormat.format(t_fHigh);
                    String sOpen = mCurrFormat.format(t_fOpen);
                    String sLow = mCurrFormat.format(t_fLow);
                    String sClose = mCurrFormat.format(t_fClose);

                    float t_preClose = 0;
                    if (t_preCAtom != null) {
                        t_preClose = t_preCAtom.mClose;
                    }
                    String sPreClose = mCurrFormat.format(t_preClose);

                    float t_fOffset = 0;
                    if (t_preClose > 0) {
                        t_fOffset = t_fClose - t_preClose;
                    } else {
                        t_fOffset = t_fClose - t_fOpen;
                    }

                    float t_fZDF = calZDFPercent(t_fOffset, t_fClose);
                    String sZDF = DataUtils.formatFloat2Percent(t_fZDF);

                    int t_iDate = (Integer) t_cAtom.mTag;

                    String sDate = "";
                    if (mCurrPeriod == TYPE_60MINUTE || mCurrPeriod == TYPE_30MINUTE || mCurrPeriod == TYPE_15MINUTE) {
                        String tDate = DataUtils.formatDateY_M_D_HHmm(String.valueOf(t_iDate), "/");
                        String[] aryDate = tDate.split("/");
                        if (aryDate != null && aryDate.length == 4) {
                            sDate = aryDate[2] + "/" + aryDate[3];
                        }
                    } else {
                        sDate = DataUtils.formatDateY_M_D(String.valueOf(t_iDate), "-");
                    }


                    if (mShowDetailCB != null) {
                        mShowDataBundle.clear();
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_TIME_S, sDate);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_HIGH_S, sHigh);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_OPEN_S, sOpen);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_LOW_S, sLow);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_CLOSE_S, sClose);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_ZDF_S, sZDF);
                        mShowDataBundle.putString(QuoteLandscapePage.KEY_PRE_CLOSE_S, sPreClose);
                        mShowDetailCB.showDetail(2, mShowDataBundle); // type:K线详情
                    }

                    return true;
                }
            });



            mMA5Layer = new LineLayer();
            mMA5Layer.setColor(getContext().getResources().getColor(R.color.sky_line_ma5));
            mMA5Layer.setMaxCount(KLINE_COUNT);
            mMA5Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mMA5Layer.setFloorValue(0, false);

            mMA10Layer = new LineLayer();
            mMA10Layer.setColor(getContext().getResources().getColor(R.color.sky_line_ma10));
            mMA10Layer.setMaxCount(KLINE_COUNT);
            mMA10Layer.setFloorValue(0, false);
            mMA10Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            mMA20Layer = new LineLayer();
            mMA20Layer.setColor(getContext().getResources().getColor(R.color.sky_line_ma20));
            mMA20Layer.setMaxCount(KLINE_COUNT);
            mMA20Layer.setFloorValue(0, false);
            mMA20Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            // K线长压瞄准线
            mAimLineLayer = new AimLineLayer();
            mAimLineLayer.setStrokeWidth(1);
            mAimLineLayer.setColor(mAimColor);
            mAimLineLayer.setIsShowHLine(false);
            mAimLineLayer.setIsShowVLine(true);

            // k线 区线 BS点 的栈层
            mKLineStackLayer = new StackLayer();
            mKLineStackLayer.setPaddings(0, FontUtils.dip2px(getContext(), 17), 0, 0);
            mKLineStackLayer.setShowBorder(true);
            mKLineStackLayer.setBorderWidth(1);
            mKLineStackLayer.setShowHPaddingLine(true);
            mKLineStackLayer.setBorderColor(mFrameBoderColor);
            

            mKLineStackLayer.addLayer(mKLineLayer);
            mKLineStackLayer.addLayer(mMA5Layer);
            mKLineStackLayer.addLayer(mMA10Layer);
            mKLineStackLayer.addLayer(mMA20Layer);
            mKLineStackLayer.addLayer(mAimLineLayer);
            // 最后加BS点Layer
            mKLineStackLayer.addLayer(mBSTopLayer);

            // k线 区线 BS点 和 其左边坐标的 group
            GroupLayerOverlap mKLineGroupLayer = new GroupLayerOverlap();
            Bitmap t_bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_avgline_identity);
            mKLineGroupLayer.setAvgLineBitmap(t_bitmap);
            mKLineGroupLayer.switchAvgLineIdentifyOn(true);
            mKLineGroupLayer.setLeftLayer(mKLineAxisLayer);
            mKLineGroupLayer.setRightLayer(mKLineStackLayer);
            mKLineGroupLayer.setHeightPercent(0.66f);
            mChartView.addLayer(mKLineGroupLayer);

            // 指标的坐标
            mIndicatorAxisLayer = new YAxisLayer();
            mIndicatorAxisLayer.setDrawTailCoordinate(false);
            mIndicatorAxisLayer.setAxisCount(2);
            mIndicatorAxisLayer.setMaxValue(0.00f);
            mIndicatorAxisLayer.setMinValue(0.00f);
            mIndicatorAxisLayer.setAlign(Align.LEFT);
            mIndicatorAxisLayer.setColor(RColor(R.color.t3));
            mIndicatorAxisLayer.setPaddings(10, 5, 0, 5);
            mIndicatorAxisLayer.setMinWidthString(MIN_WIDTH);
            mIndicatorAxisLayer.setTextSize(TEXTSIZE);


            mAimIndicatorLayer = new AimLineLayer();
            mAimIndicatorLayer.setStrokeWidth(1);
            mAimIndicatorLayer.setColor(mAimColor);
            mAimIndicatorLayer.setIsShowHLine(false);
            mAimIndicatorLayer.setIsShowVLine(true);

            // 指标的层栈
            mIndicatorStackLayer = new StackLayer();
            mIndicatorStackLayer.setPaddings(0, FontUtils.dip2px(getContext(), 17), 0, 0);
            mIndicatorStackLayer.setShowHPaddingLine(true);
            mIndicatorStackLayer.addLayer(mAimIndicatorLayer);
            mIndicatorStackLayer.setShowBorder(true);
            mIndicatorStackLayer.setBorderWidth(1);
            mIndicatorStackLayer.setBorderColor(mFrameBoderColor);

            // 创建成交量指标
            createIndicator(null, IDIndicator.ID_Indicator_CJL);

            // 指标和指标坐标的group
            GroupLayerOverlap mIndicatorGroupLayer = new GroupLayerOverlap();
            mIndicatorGroupLayer.setLeftLayer(mIndicatorAxisLayer);
            mIndicatorGroupLayer.setRightLayer(mIndicatorStackLayer);
            mIndicatorGroupLayer.setHeightPercent(0.34f);
            mChartView.addLayer(mIndicatorGroupLayer);

            // 下方横坐标
            mXAxisLayer = new XAxisLayer();
            mXAxisLayer.setColor(RColor(R.color.t3));
            mXAxisLayer.setTextSize(TEXTSIZE);

            mChartView.addLayer(mXAxisLayer);
            mChartView.postInvalidate();
        }


        mRadioGroup_indexes = (RadioGroup) findViewById(R.id.klinepage_landscape_radiogroup_indexes);
        RadioButton radiobtn = (RadioButton) findViewById(R.id.btn_indexes_CJZJ);
        radiobtn.setEnabled(SupportEquipment.getInstance().getPermissionByName(radiobtn.getText().toString()));
        
        radiobtn = (RadioButton) findViewById(R.id.btn_indexes_ABJB);
        radiobtn.setEnabled(SupportEquipment.getInstance().getPermissionByName(radiobtn.getText().toString()));
        
        radiobtn = (RadioButton) findViewById(R.id.btn_indexes_DDBL);
        radiobtn.setEnabled(SupportEquipment.getInstance().getPermissionByName(radiobtn.getText().toString()));
        
        radiobtn = (RadioButton) findViewById(R.id.btn_indexes_LTSH);
        radiobtn.setEnabled(SupportEquipment.getInstance().getPermissionByName(radiobtn.getText().toString()));
        
        
        mRadioGroup_indexes.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mCurCheckedIndexes_tvId = checkedId;
                resetLayers_Indicator(true);

                refreshIndicator(checkedId);
            }
        });


    }

    @Override
    protected void initData() {

    }

    public void setPeriod(int period) {
        mCurrPeriod = period;
    }

    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;
    }

    public void requestData(boolean isAutoRefresh) {

        LogUtil.easylog("enter KlineLandscapePage->requestData()");
        if (mbLongPressedFlag || mIsZooming) {
            return;
        }

        if (mGoodsId == 0) {
            return;
        }
        if (!isAutoRefresh) {
            // 非自动刷新请求
            isFirstRequest = true;
            mMapIndicatorCache.clear();
            resetLayers();

            DialogUtils.showProgressDialogNoShadow(getContext(), this);

        } else {
            if (isFirstRequest) {
                mMapIndicatorCache.clear();
                // 自动刷新第一次时,都请求
            } else {
                if (mCurrPeriod == TYPE_15MINUTE || mCurrPeriod == TYPE_30MINUTE || mCurrPeriod == TYPE_60MINUTE) {
                    // 自动刷新非第一次时,以上周期都请求
                } else {
                    // 不请求
                    return;
                }
            }
        }



        CandleStickPackage pkg = new CandleStickPackage(new QuoteHead((short) 0));
        // 3. 发送网络请求，刷新数据
        pkg.setRequest(CandleStick_Request.newBuilder().setGoodsId(mGoodsId).setLastUpdateMarketTime(0).setReqPeriod(mCurrPeriod).setReqBegin(0).setReqMa(7).setReqSize(200).build());
        requestQuote(pkg, IDUtils.CandleStick);
        LogUtil.easylog("requestQuote(pkg, IDUtils.CandleStick)->goodsId:" + mGoodsId);
    }

    private void requestCpx() {
        EquipmentData tCpxPermission = SupportEquipment.getInstance().getById(SupportEquipment.ID_CPX);
        if (!tCpxPermission.hasPermission) {
            return;
        }

        CpxPackage cpx = new CpxPackage(new QuoteHead((short) 0));
        cpx.setRequest(Cpx_Request.newBuilder().setGoodsId(mGoodsId).setReqPeriod(mCurrPeriod).setLastUpdateMarketTime(0).build());
        requestQuote(cpx, IDUtils.Cpx);
    }


    private void resetLayers_Indicator(boolean bReDraw) {

        for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
            layer.resetData();
        }

        if (bReDraw) {
            resetLines_Indicator();
            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();
        }
    }

    /**
     * 将界面清空
     * */
    private void resetLayers() {

        // 1. clear k line, volume, and maN
        mKLineLayer.resetData();

        mMA5Layer.resetData();
        mMA10Layer.resetData();
        mMA20Layer.resetData();

        // 2. clear BS
        mBSTopLayer.clear();

        // 3. clear Axis
        mKLineAxisLayer.setMaxValue(0f);
        mKLineAxisLayer.setMinValue(0f);
        mIndicatorAxisLayer.setMaxValue(0f);
        mIndicatorAxisLayer.setMinValue(0f);
        mXAxisLayer.clearValue();

        // 重置指标layer
        resetLayers_Indicator(false);
        // 必须调用该方法，重新设置charView显示的起点，否则可能显示为空白
        resetLines();

        // 4. post invalidate
        mChartView.forceAdjustLayers();
        mChartView.postInvalidate();
    }


    private void doMoveStartPos(int moveStep) {
        if (mCurCheckedIndexes_tvId == R.id.btn_indexes_CJL) {
            mVolumeLayer.moveStartPos(moveStep);
        } else {
            for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
                layer.moveStartPos(moveStep);
            }
        }

    }


    private void doSetStartPos() {
        if (mCurCheckedIndexes_tvId == R.id.btn_indexes_CJL) {
            mVolumeLayer.setStartPos(mKLineLayer.getStartPos());
        } else {
            for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
                layer.setStartPos(mKLineLayer.getStartPos());
            }
        }
    }


    private void doChangeColumnWidth(float columnWidth, boolean isLine) {

        if (mCurCheckedIndexes_tvId == R.id.btn_indexes_CJL) {
            mVolumeLayer.setColumnarWidth(columnWidth);
            mVolumeLayer.change2Line(isLine);
        } else {
            for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
                if (layer instanceof ColumnarLayer) {
                    ColumnarLayer columnarLayer = (ColumnarLayer) layer;
                    columnarLayer.setColumnarWidth(columnWidth);
                    columnarLayer.change2Line(isLine);
                }
                // //隐藏线
                // else if (layer instanceof LineLayer) {
                // LineLayer lineLayer = (LineLayer) layer;
                // lineLayer.show(!isLine);
                //
                // }
            }
        }
    }


    private void resetLines_Indicator() {
        if (mCurCheckedIndexes_tvId == R.id.btn_indexes_CJL) {
            mVolumeLayer.setMaxCount(KLINE_COUNT);
            mVolumeLayer.calMinAndMaxValue();
            mVolumeLayer.setMaxValue(mVolumeLayer.getMaxValue());
            mVolumeLayer.setMinValue(mVolumeLayer.getMinValue());
            mIndicatorAxisLayer.setMaxValue(mVolumeLayer.getMaxValue());
            mIndicatorAxisLayer.setMinValue(0);
        } else {
            LogUtil.easylog("mIndicatorStackLayer.getMaxValue():" + mIndicatorStackLayer.getMaxValue());
            LogUtil.easylog("mIndicatorStackLayer.getMinValue():" + mIndicatorStackLayer.getMinValue());

            for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
                layer.setMaxCount(KLINE_COUNT);
            }

            mIndicatorStackLayer.calMinAndMaxValue();
            for (ChartLayer layer : mIndicatorStackLayer.getLayers()) {
                layer.setMaxValue(mIndicatorStackLayer.getMaxValue());
                layer.setMinValue(mIndicatorStackLayer.getMinValue());
            }
            mIndicatorAxisLayer.setMaxValue(mIndicatorStackLayer.getMaxValue());
            mIndicatorAxisLayer.setMinValue(mIndicatorStackLayer.getMinValue());

        }

    }

    private void resetLines() {
        mKLineLayer.setMaxCount(KLINE_COUNT);
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

        resetLines_Indicator();

        ColumnarAtom kLast = mKLineLayer.getDisplayLastValue();
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

    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof CandleStickPackage) {
            CandleStickPackage candleStickPkg = (CandleStickPackage) pkg;
            CandleStick_Reply reply = candleStickPkg.getResponse();

            String cacheKey = IDIndicator.ID_Indicator_CJL + "|" + mCurrPeriod;
            mMapIndicatorCache.put(cacheKey, reply);

            List<CandleStick> lstData = reply.getKLinesList();
            mKLineLayer.clear();
            if (mVolumeLayer != null) {
                mVolumeLayer.clear();
            }
            mMA5Layer.clear();
            mMA10Layer.clear();
            mMA20Layer.clear();
            LogUtil.easylog("updateFromQuote->goodsId:" + mGoodsId + "recDataSize:" + lstData.size());
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
                mKLineLayer.addValue(klineCol);
                if (mVolumeLayer != null) {
                    mVolumeLayer.addValue(new ColumnarAtom(candle.getAmount()));
                }
                mMA5Layer.addValue(ma5 / 1000);
                mMA10Layer.addValue(ma10 / 1000);
                mMA20Layer.addValue(ma20 / 1000);

            }



            resetLines();

            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();

            if (mCurCheckedIndexes_tvId == R.id.btn_indexes_CJL) {
                DialogUtils.closeProgressDialog();
            }
            
            requestCpx();
            if (isFirstRequest) {
                isFirstRequest = false;
                refreshIndicator(mCurCheckedIndexes_tvId);
            }


        } else if (pkg instanceof CpxPackage) {
            CpxPackage p = (CpxPackage) pkg;
            Cpx_Reply reply = p.getResponse();

            String cacheKey = "CPX" + "|" + mCurrPeriod;
            mMapIndicatorCache.put(cacheKey, reply);

            List<cpx_item> items = reply.getCpxItemArrayList();

            mBSTopLayer.setBSItems(items);
            mChartView.postInvalidate();
        } else if (pkg instanceof IndicatorPackage) {
            IndicatorPackage indicatorPkg = (IndicatorPackage) pkg;
            Indicator_Reply reply = indicatorPkg.getResponse();

            if (reply.getCycle() != mCurrPeriod) {
                return;
            }
            String cacheKey = reply.getIndicatorId() + "|" + reply.getCycle();

            mMapIndicatorCache.put(cacheKey, reply);

            createIndicator(reply, -1);

            resetLines();

            doSetStartPos();
            resetLines();

            

            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();
            
            DialogUtils.closeProgressDialog();
        }

    }

    @Override
    protected void updateWhenDecodeError() {
        super.updateWhenDecodeError();
        DialogUtils.closeProgressDialog();
    }

    @Override
    protected void updateWhenNetworkError() {
        super.updateWhenNetworkError();
        DialogUtils.closeProgressDialog();
    }

    public void setShowDetailCB(QuoteLandscapePage.ShowDetailCallBack callBack) {
        mShowDetailCB = callBack;
    }



    private void createIndicator(GeneratedMessage data, int iIndicatorType) {
        mIndicatorStackLayer.clear();
        int indicatorId = 0;

        if (iIndicatorType > 0) {
            indicatorId = iIndicatorType;
        } else if (data != null && data instanceof Indicator_Reply) {
            Indicator_Reply indicatorData = (Indicator_Reply) data;
            indicatorId = indicatorData.getIndicatorId();
        }

        if (indicatorId == IDIndicator.ID_Indicator_CJL) {

            mIndicatorAxisLayer.setOnFormatDataListener(new OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    float absVal = Math.abs(val);
                    if (absVal > 1000000) {
                        return DataUtils.mDecimalFormat1.format(val / 100000000) + "亿";
                    } else if (absVal > 10000) {
                        return DataUtils.mDecimalFormat1.format(val / 10000) + "万";
                    }
                    return "成交量 " + DataUtils.mDecimalFormat1.format(val);
                }
            });


            mVolumeLayer = new ColumnarLayer();

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

            if (data != null && data instanceof CandleStick_Reply) {
                CandleStick_Reply candleStickData = (CandleStick_Reply) data;
                List<CandleStick> lstData = candleStickData.getKLinesList();
                for (int i = lstData.size() - 1; i >= 0; i--) {
                    CandleStick candle = lstData.get(i);

                    if (mVolumeLayer != null) {
                        mVolumeLayer.addValue(new ColumnarAtom(candle.getAmount()));
                    }
                }

            }


            // 成交量添加到指标的层栈
            mIndicatorStackLayer.addLayer(mVolumeLayer);
            mIndicatorStackLayer.addLayer(mAimIndicatorLayer);

        }

        else {
            Indicator_Reply indicatorData = (Indicator_Reply) data;
            List<line> layerList = indicatorData.getLinesList();
            for (int i = 0; i < layerList.size(); i++) {
                line layer = layerList.get(i);
                int layerType = layer.getType();
                String layerName = layer.getName();
                ChartLayer chartLayer = createLayerByType(layerName, layerType, i, layer.getPointsList());
                if (chartLayer != null) {
                    mIndicatorStackLayer.addLayer(chartLayer);
                }
            }
            mIndicatorStackLayer.addLayer(mAimIndicatorLayer);


            float rate = 1;
            if (indicatorData.hasRate()) {
                rate = indicatorData.getRate();
            }

            int flag = (int) (rate / Math.abs(rate));
            if (flag > 0) {
                rate = 1 / Math.abs(rate);
            } else if (flag < 0) {
                rate = Math.abs(rate);
            }


            final double finalRate = rate;

            mIndicatorAxisLayer.setOnFormatDataListener(new OnFormatDataListener() {
                @Override
                public String onFormatData(float val) {
                    // double absVal = Math.abs(val) * finalRate;
                    // if (absVal >= 10000000) {
                    // return DataUtils.mDecimalFormat1.format(val / 100000000) + "亿";
                    // } else if (absVal >= 10000) {
                    // return DataUtils.mDecimalFormat1.format(val / 10000) + "万";
                    // }
                    return mIndicatorName;
                }
            });

        }
    }

    private ChartLayer createLayerByType(String lineName, int layerType, int layerSort, List<point> lstPoint) {

        switch (layerType) {
            case 1: // 柱状
            {
                final ColumnarLayer columnarLayer = new ColumnarLayer();
                columnarLayer.showHGrid(1);
                columnarLayer.setMaxCount(KLINE_COUNT);
                columnarLayer.setColumnarWidth(mColumnWidth);
                columnarLayer.setLineWidth(FontUtils.dip2px(getContext(), 1));
                columnarLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
                columnarLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
                columnarLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
                columnarLayer.setOnDrawingListener(new OnDrawingListener() {
                    @Override
                    public void onDrawing(Paint paint, int pos) {
                        ColumnarAtom atom = columnarLayer.getValue(pos);
                        int colorFlag = FontUtils.getColorByZD(atom.mClose);
                        int color = getZDPColor(colorFlag);
                        paint.setColor(color);
                    }
                });

                if (lstPoint != null) {
                    for (int i = 0; i < lstPoint.size(); i++) {
                        point onePoint = lstPoint.get(i);
                        float tClose = 0;
                        if (onePoint.hasValue()) {
                            tClose = onePoint.getValue();
                        }
                        ColumnarAtom columnarAtom = new ColumnarAtom(tClose);
                        columnarLayer.addValue(columnarAtom);
                    }
                }


                return columnarLayer;
            }
            case 2:// 细柱
            {
                final FixedWidthColumnarLayer fixedWidthColumnarLayer = new FixedWidthColumnarLayer();

                fixedWidthColumnarLayer.showHGrid(1);
                fixedWidthColumnarLayer.setMaxCount(KLINE_COUNT);
                fixedWidthColumnarLayer.setColumnarWidth(mColumnWidth);
                fixedWidthColumnarLayer.setLineWidth(FontUtils.dip2px(getContext(), 1)); // 变成line时用
                fixedWidthColumnarLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
                fixedWidthColumnarLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
                fixedWidthColumnarLayer.setOnDrawingListener(new OnDrawingListener() {
                    @Override
                    public void onDrawing(Paint paint, int pos) {
                        ColumnarAtom atom = fixedWidthColumnarLayer.getValue(pos);
                        int colorFlag = FontUtils.getColorByZD(atom.mClose);
                        int color = getZDPColor(colorFlag);
                        paint.setColor(color);
                    }
                });

                if (lstPoint != null) {
                    for (int i = 0; i < lstPoint.size(); i++) {
                        point onePoint = lstPoint.get(i);
                        float tClose = 0;
                        if (onePoint.hasValue()) {
                            tClose = onePoint.getValue();
                        }
                        ColumnarAtom columnarAtom = new ColumnarAtom(tClose);
                        fixedWidthColumnarLayer.addValue(columnarAtom);
                    }
                }

                return fixedWidthColumnarLayer;
            }

            case 3:// 折线
            {
                LineLayer lineLayer = new LineLayer();

                int lineColor = IndicatorColor.getColorBySort(layerSort);

                lineLayer.setColor(lineColor);
                lineLayer.setMaxCount(KLINE_COUNT);
                lineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
                // lineLayer.setFloorValue(0, false);


                if (lstPoint != null) {
                    for (int i = 0; i < lstPoint.size(); i++) {
                        point onePoint = lstPoint.get(i);
                        float tValue = Float.NaN;
                        if (onePoint.hasValue()) {
                            tValue = onePoint.getValue();
                        }
                        
                        LogUtil.easylog("line Point Value:" + tValue);
                        lineLayer.addValue(tValue);
                    }
                }

                if (!TextUtils.isEmpty(lineName)) {
                    mIndicatorName += ("|" + lineName + "," + lineColor);
                }

                return lineLayer;
            }

            case 4:// 参考线
            {
                ReferenceLineLayer lineLayer = new ReferenceLineLayer();

                int lineColor = IndicatorColor.getColorBySort(layerSort);
                lineLayer.setColor(lineColor);
                lineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
                lineLayer.setFloorValue(0, false);


                if (lstPoint != null) {
                    if (lstPoint.size() > 0) {
                        point onePoint = lstPoint.get(0);
                        float tValue = 0;
                        if (onePoint.hasValue()) {
                            tValue = onePoint.getValue();
                        }
                        lineLayer.setValue(tValue);
                    }
                }

                if (!TextUtils.isEmpty(lineName)) {
                    mIndicatorName += ("|" + lineName + "," + lineColor);
                }
                return lineLayer;
            }

            default:
                return null;
        }

    }

    private void refreshIndicator(int checkedId) {
        if (checkedId <= 0) {
            return;
        }

        RadioButton rbtn = (RadioButton) findViewById(checkedId);
        mIndicatorName = rbtn.getText().toString();


        Object o = ViewUtil.getViewTag(rbtn);
        if (o != null) {
            int iTag = -1;
            try {
                String sTag = (String) o;
                iTag = DataUtils.convertToInt(sTag);
            } catch (Exception e) {
            }

            String cacheKey = iTag + "|" + mCurrPeriod;
            if (mMapIndicatorCache.containsKey(cacheKey)) {
                GeneratedMessage indicatorData = mMapIndicatorCache.get(cacheKey);

                if (iTag == IDIndicator.ID_Indicator_CJL) {
                    CandleStick_Reply reply = (CandleStick_Reply) indicatorData;
                    createIndicator(reply, iTag);
                } else {
                    createIndicator(indicatorData, -1);
                }

                resetLines();
                doSetStartPos();
                resetLines();

                mChartView.forceAdjustLayers();
                mChartView.postInvalidate();

            } else {
                if (iTag != IDIndicator.ID_Indicator_CJL) {

                    DialogUtils.showProgressDialogNoShadow(getContext(), this);

                    IndicatorPackage pkg = new IndicatorPackage(new QuoteHead((short) iTag));
                    Indicator_Request request = Indicator_Request.newBuilder().setCycle(mCurrPeriod).setGoodsId(mGoodsId).setIndicatorId(iTag).build();
                    pkg.setRequest(request);
                    requestQuote(pkg, IDUtils.Indicator);

                }
            }

        }
    }

    @Override
    public void onCancelProgressDia() {
        // TODO Auto-generated method stub

    }

}
