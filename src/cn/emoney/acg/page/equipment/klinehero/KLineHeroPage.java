package cn.emoney.acg.page.equipment.klinehero;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.FixPair;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.CpxReply.Cpx_Reply.cpx_item;
import cn.emoney.acg.dialog.GameOverDialog;
import cn.emoney.acg.dialog.RankListDialog;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.klinehero.KLineHeroData.KLineHero_Data;
import cn.emoney.acg.page.equipment.klinehero.KLineHeroData.KLineHero_Data.CandleStick;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DownloadFileThread;
import cn.emoney.acg.util.DownloadFileThread.DownloadFileCallBack;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.KHeroBSTopLayer;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnDrawingListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.GroupLayerOverlap;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class KLineHeroPage extends PageImpl {
    private static final String MIN_WIDTH = "1000.00亿";

    private static final String FORMAT_CURPROFIT = "本局收益: ";
    private static final String FORMAT_MAXPROFIT = "最大收益: ";
    private static final String FORMAT_TOTALPROFIT = "累计收益: ";
    private static final String FORMAT_TOTALASSETS = "总资产: ";
    private static final String FORMAT_WINPROFIT = "胜利率: ";

    private static final int ID_QRYUSERINFO_MIN = 0;
    private static final int ID_STARTGAME_MIN = 100000;
    private static final int ID_COMMITSULT_MIN = 200000;
    private FixPair<String, Integer> CMD_QRYUSERINFO = new FixPair<String, Integer>("qryUserInfo", ID_QRYUSERINFO_MIN);
    private FixPair<String, Integer> CMD_STARTGAME = new FixPair<String, Integer>("quickStartNewGame", ID_STARTGAME_MIN);
    private FixPair<String, Integer> CMD_COMMITSULT = new FixPair<String, Integer>("commitMobileGameResult", ID_COMMITSULT_MIN);

    private KHeroUInfo mKHeroUInfo_loc = null;
    /**
     * 用户初始资金
     */
    private float M_INITASSETS = 500000; // 用户初始资金,

    private void setInitAssets(float f) {
        M_INITASSETS = f + miGameRandom;
    }

    private float getInitAssets() {
        return M_INITASSETS - miGameRandom;
    }

    private LinearLayout mLlBusyNotice = null;

    private ChartView mChartView = null;
    private ColumnarLayer mKLineLayer = null;
    private YAxisLayer mKLineAxisLayer = null;
    private YAxisLayer mVolumeAxisLayer = null;
    private ColumnarLayer mVolumeLayer = null;

    private int TEXTSIZE = 12;
    private int KLINE_COUNT = 30;
    private static int mChartViewWidth = 0;
    private final static float COLUMN_WIDTH = 6;
    private final static float MIN_COLUMN_WIDTH = 2f;

    private float mBaseWidth = 8;

    private DecimalFormat mFormat2Decimal = new DecimalFormat("0.00");
    private DecimalFormat mCurrFormat = mFormat2Decimal;

    private float mColumnWidth = 0;
    private float mMinColumnWidth = 0;

    private LineLayer mMA5Layer = null;
    private LineLayer mMA10Layer = null;
    private LineLayer mMA20Layer = null;

    private KHeroBSTopLayer mBSTopLayer = null;

    private RelativeLayout mBtnRlControl1 = null;
    private RelativeLayout mBtnRlControl2 = null;

    private TextView mTvTotalProfit = null;
    private TextView mTvMaxProfit = null;
    private TextView mTvTotalAssets = null;
    private TextView mTvWinPercentage = null;
    private TextView mTvCurProfit = null;
    private TextView mTvStockName = null;
    private TextView mTvGameInfo = null;

    private TextView mBtnTvControl1 = null;
    private TextView mBtnTvControl2 = null;

    private ImageButton mBtnClose = null;

    private StackLayer mKLineStackLayer = null;
    private StackLayer mVolumeStackLayer = null;
    private int mFrameBoderColor = Color.GRAY;
    private int mBgCvColor = Color.TRANSPARENT;
    private int mBgCpx = Color.TRANSPARENT;

    private String mGoodsName = "";
    private String mGoodsCode = "";

    private int mFirstClose = 0;
    private boolean mIsBuy_in = false;
    /**
     * -1:准备中; 0:准备完成,未开始; 1:进行中; 2:结束;
     */
    private int mGameState = 2;
    private int mAlreadyShowCount = 0;
    private int mRemainCount = 0;
    private String mStartTime = "";
    /**
     * 上一个买入价
     */
    private float mfLastBuyPrice = 0;

    private GameOverDialog gameOverDialog = null;

    private void setLastBuyPrice(float f) {
        mfLastBuyPrice = f + miGameRandom;
    }

    private float getLastBuyPrice() {
        return mfLastBuyPrice - miGameRandom;
    }

    /**
     * 上一个买入价时的资产
     */
    private double mfLastBuyTotalAssets = 0;

    private void setLastBuyTotalAssets(double d) {
        mfLastBuyTotalAssets = d + miGameRandom;
    }

    private double getLastBuyTotalAssets() {
        return mfLastBuyTotalAssets - miGameRandom;
    }

    /**
     * 本局初始资金
     */
    private double mfFirstAssets = 500000;

    private void setFirstAssets(double d) {
        mfFirstAssets = d + miGameRandom;
    }

    private double getFirstAssets() {
        return mfFirstAssets - miGameRandom;
    }

    /**
     * 最大收益率
     */
    private float m_fMaxProfit = 0;
    /**
     * 我的资产
     */
    private double mfTotalAssets = 500000;

    private void setTotalAssets(double d) {
        mfTotalAssets = d + miGameRandom;
    }

    private double getTotalAssets() {
        return mfTotalAssets - miGameRandom;
    }

    /**
     * 本局操作记录
     */
    private String m_sOPHistory = "";

    /**
     * 赢的局数
     */
    private int m_iWinCount = 0;

    /**
     * 总共玩的局数
     */
    private int m_iTotalGameCount = 0;

    private String m_sDataUrl;

    private String m_sBufMd5;

    private int m_iInitShowCount;

    private String m_sGameOverMsg;

    /**
     * 一局的操盘记录
     */
    private List<cpx_item> m_lstCpx = new ArrayList<cpx_item>();

    private long mLastCommitTime = 0;

    private int miGameRandom = 3;

    /**
     * 测试数据用
     */
    // private TextView mTvLastKPrice = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_kline_hero);

        miGameRandom = (int) (Math.random() * 10) + 1;

        mColumnWidth = FontUtils.dip2px(getContext(), COLUMN_WIDTH);
        mMinColumnWidth = FontUtils.dip2px(getContext(), MIN_COLUMN_WIDTH);
        TEXTSIZE = (int) getContext().getResources().getDimension(R.dimen.txt_s1);
        mChartViewWidth = FontUtils.px2dip(getContext(), DataModule.SCREEN_HEIGHT) - 10;

        KLINE_COUNT = (int) (mChartViewWidth / mBaseWidth);

        mAlreadyShowCount = KLINE_COUNT;

        mLlBusyNotice = (LinearLayout) findViewById(R.id.zdlhpage_ll_lvempty);
        // mLlBusyNotice.setVisibility(View.VISIBLE);
        mLlBusyNotice.setVisibility(View.GONE);

        mBtnClose = (ImageButton) findViewById(R.id.pagekhero_btn_close);
        mBtnClose.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                getModule().finish();
            }
        });

        findViewById(R.id.pagekhero_tv_heroranklist).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo userInfo = DataModule.getInstance().getUserInfo();
                if (userInfo.isLogined()) {
                    // PageIntent intent = new PageIntent(KLineHeroPage.this,
                    // KLineHeroRankingList.class);
                    // intent.setSupportAnimation(false);
                    // startPage(intent);
                    RankListDialog dialog = new RankListDialog(getContext());
                    dialog.show();
                } else {
                    showTip("去登录跟股神们一较高下吧");
                }
            }
        });

        mChartView = (ChartView) findViewById(R.id.pagekhero_cv_content);

        mTvTotalProfit = (TextView) findViewById(R.id.pagekhero_tv_totalprofit);
        mTvMaxProfit = (TextView) findViewById(R.id.pagekhero_tv_maxprofit);
        mTvTotalAssets = (TextView) findViewById(R.id.pagekhero_tv_totalassets);
        mTvWinPercentage = (TextView) findViewById(R.id.pagekhero_tv_winprofit);
        mTvCurProfit = (TextView) findViewById(R.id.pagekhero_tv_curprofit);
        mTvStockName = (TextView) findViewById(R.id.pagekhero_tv_stockname);
        mTvGameInfo = (TextView) findViewById(R.id.pagekhero_tv_gameinfo);

        mBtnRlControl1 = (RelativeLayout) findViewById(R.id.pagekhero_rl_control1);
        mBtnRlControl2 = (RelativeLayout) findViewById(R.id.pagekhero_rl_control2);

        mBtnTvControl1 = (TextView) findViewById(R.id.pagekhero_tv_control1);
        mBtnTvControl2 = (TextView) findViewById(R.id.pagekhero_tv_control2);

        updateControlBtn();


        mFrameBoderColor = RColor(R.color.b5);
        mBgCvColor = RColor(R.color.bg_transparent);
        mBgCpx = RColor(R.color.bg_khero_cpxarea);

        if (mChartView != null) {
            // k线区线左方坐标
            mKLineAxisLayer = new YAxisLayer();
            mKLineAxisLayer.setAxisCount(4);
            mKLineAxisLayer.setPutCoordinateAboveLine(true);
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

            mBSTopLayer = new KHeroBSTopLayer(getContext());
            mBSTopLayer.setBSAreaColor(mBgCpx);
            mBSTopLayer.switchAvgLineIdentifyOn(false);

            // K线
            mKLineLayer = new ColumnarLayer();
            mKLineLayer.setWriteOutCallback(mBSTopLayer);
            mKLineLayer.setNeedPreCalc(true);
            mKLineLayer.setMaxCount(KLINE_COUNT, 0);
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
            mMA5Layer.setMaxCount(KLINE_COUNT, 0);
            mMA5Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mMA5Layer.setFloorValue(0, false);

            mMA10Layer = new LineLayer();

            mMA10Layer.setColor(RColor(R.color.sky_line_ma10));
            mMA10Layer.setMaxCount(KLINE_COUNT, 0);
            mMA10Layer.setFloorValue(0, false);
            mMA10Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            mMA20Layer = new LineLayer();

            mMA20Layer.setColor(RColor(R.color.sky_line_ma20));
            mMA20Layer.setMaxCount(KLINE_COUNT, 0);
            mMA20Layer.setFloorValue(0, false);
            mMA20Layer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));

            // k线 均线的栈层
            mKLineStackLayer = new StackLayer();
            mKLineStackLayer.setPaddings(0, FontUtils.dip2px(getContext(), 17), 0, FontUtils.dip2px(getContext(), 17));
            mKLineStackLayer.setShowBorder(true);
            mKLineStackLayer.setBorderWidth(1);
            mKLineStackLayer.setShowHPaddingLine(true);
            mKLineStackLayer.setBorderColor(mFrameBoderColor);

            mKLineStackLayer.addLayer(mBSTopLayer);
            mKLineStackLayer.addLayer(mKLineLayer);
            mKLineStackLayer.addLayer(mMA5Layer);
            mKLineStackLayer.addLayer(mMA10Layer);
            mKLineStackLayer.addLayer(mMA20Layer);

            // k线 均线 其左边坐标的 group
            GroupLayerOverlap mKLineGroupLayer = new GroupLayerOverlap();
            Bitmap t_bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_avgline_identity);
            mKLineGroupLayer.setAvgLineBitmap(t_bitmap);
            mKLineGroupLayer.switchAvgLineIdentifyOn(true);
            mKLineGroupLayer.setLeftLayer(mKLineAxisLayer);
            mKLineGroupLayer.setRightLayer(mKLineStackLayer);
            mKLineGroupLayer.setHeightPercent(0.73f);
            mChartView.addLayer(mKLineGroupLayer);

            // 量的坐标
            mVolumeAxisLayer = new YAxisLayer();
            mVolumeAxisLayer.setDrawTailCoordinate(false);
            mVolumeAxisLayer.setAxisCount(2);
            mVolumeAxisLayer.setMaxValue(0.00f);
            mVolumeAxisLayer.setMinValue(0.00f);
            mVolumeAxisLayer.setAlign(Align.LEFT);
            mVolumeAxisLayer.setColor(RColor(R.color.t3));
            mVolumeAxisLayer.setPaddings(10, 10, 0, 10);
            mVolumeAxisLayer.setMinWidthString(MIN_WIDTH);
            mVolumeAxisLayer.setTextSize(TEXTSIZE);
            final DecimalFormat format = new DecimalFormat("0.0");
            mVolumeAxisLayer.setOnFormatDataListener(new OnFormatDataListener() {

                @Override
                public String onFormatData(float val) {
                    float absVal = Math.abs(val);
                    if (absVal > 1000000) {
                        return format.format(val / 100000000) + "亿";
                    } else if (absVal > 10000) {
                        return format.format(val / 10000) + "万";
                    }
                    return format.format(val);
                }
            });

            // 量
            mVolumeLayer = new ColumnarLayer();
            mVolumeLayer.showHGrid(1);
            mVolumeLayer.setMaxCount(KLINE_COUNT, 0);
            mVolumeLayer.setColumnarWidth(mColumnWidth);
            mVolumeLayer.setLineWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
            mVolumeLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
            mVolumeLayer.setPaddings(0, 2, 0, 0);
            mVolumeLayer.setShowBorder(true);
            mVolumeLayer.setBorderWidth(1);
            mVolumeLayer.setBorderColor(mFrameBoderColor);

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

            // 量和量的坐标的group
            GroupLayerOverlap mVolumeGroupLayer = new GroupLayerOverlap();

            mVolumeGroupLayer.setLeftLayer(mVolumeAxisLayer);
            mVolumeGroupLayer.setRightLayer(mVolumeLayer);
            mVolumeGroupLayer.setHeightPercent(0.27f);
            mChartView.addLayer(mVolumeGroupLayer);

            notifyChartRefresh(false);
        }

        mBtnTvControl1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // VibratorUtil.Vibrate(getActivity(), 10);
                mGameState = 1;
                float t_fLastKPrice = mKLineLayer.getDisplayLastValue().mClose;
                int t_time = (Integer) mKLineLayer.getDisplayLastValue().mTag;

                if (mIsBuy_in) { // 点击持有

                } else { // 点击买入
                    mIsBuy_in = true;
                    setBuyData(t_fLastKPrice);
                    addCpxRecord(1, t_time);
                }

                mRemainCount = showNextKLine();

                t_fLastKPrice = mKLineLayer.getDisplayLastValue().mClose;
                calcNewData(t_fLastKPrice);

                if (mRemainCount <= 0) {
                    mBtnRlControl2.setEnabled(false);
                    mGameState = 2;
                    mKLineLayer.setColumnarWidth(mMinColumnWidth);
                    mVolumeLayer.setColumnarWidth(mMinColumnWidth);
                    resetLines(mKLineLayer.getValueCount());
                    mChartView.forceAdjustLayers();
                    notifyChartRefresh(false);

                    // 显示结束对话框
                    showGameOverDialog();

                    saveAndCommit();
                } else {
                    long t_curTime = System.currentTimeMillis();
                    if (t_curTime - mLastCommitTime > DataModule.G_KHERO_COMMIT_INTERVAL) {
                        mLastCommitTime = t_curTime;
                        saveAndCommit();
                    }
                }

                updateControlBtn();
                updateGameNotice();
            }

        });

        mBtnTvControl2.setOnClickListener(new OnClickListener() {
            private long mGameOverTime;

            @Override
            public void onClick(View v) {
                if (mGameState == 2) {
                    if (System.currentTimeMillis() - mGameOverTime < 1000) {
                        return;
                    }
                    mGameState = -1;
                    updateControlBtn();
                    reStartGame();
                } else {
                    mGameState = 1;
                    int t_time = (Integer) mKLineLayer.getDisplayLastValue().mTag;
                    if (mIsBuy_in) { // 点击卖出
                        mIsBuy_in = false;
                        setSaleData();
                        addCpxRecord(-1, t_time);
                    } else { // 点击观望

                    }

                    mRemainCount = showNextKLine();
                    if (mRemainCount <= 0) {
                        mBtnRlControl2.setEnabled(false);
                        mGameOverTime = System.currentTimeMillis();
                        mGameState = 2;
                        mKLineLayer.setColumnarWidth(mMinColumnWidth);
                        mVolumeLayer.setColumnarWidth(mMinColumnWidth);
                        resetLines(mKLineLayer.getValueCount());
                        mChartView.forceAdjustLayers();
                        notifyChartRefresh(false);

                        // 显示结束对话框
                        showGameOverDialog();

                        saveAndCommit();
                    }

                    updateControlBtn();
                }
                updateGameNotice();
            }
        });

        /**
         * 测试数据用
         */
        // mTvLastKPrice = (TextView) findViewById(R.id.pagekhero_tv_nowprice);

        // renderTheme();
        // mRlGameoverinfoZone.setBackgroundResource(getTheme().getBgKHeroGameOver());

    }

    private void notifyChartRefresh(boolean b) {
        if (mChartView != null) {
            mChartView.postInvalidate();
        }

        if (b) {
            // if (mTvLastKPrice != null && mKLineLayer != null) {
            // float t_fLastKPrice = 0;
            // try {
            // t_fLastKPrice = mKLineLayer.getDisplayLastValue().mClose;
            // } catch (Exception e) {
            // // TODO: handle exception
            // }
            //
            // DecimalFormat decimalFormat = new DecimalFormat("0.0###");
            // String sLastPrice = decimalFormat.format(t_fLastKPrice);
            // mTvLastKPrice.setText(sLastPrice);
            // }
        }

    }

    @Override
    protected void initData() {
        getSQLiteDBHelper();
        mGameState = -1;
        updateControlBtn();
        updateGameNotice();

        // 获取用户信用
        getKHeroUserInfo();
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }

    private void resetLines(int lineCount) {
        int nLineCount = KLINE_COUNT;
        if (lineCount > 0) {
            nLineCount = lineCount;
        }
        mKLineLayer.setMaxCount(nLineCount, 0);
        mVolumeLayer.setMaxCount(nLineCount, 0);
        mMA5Layer.setMaxCount(nLineCount, 0);
        mMA10Layer.setMaxCount(nLineCount, 0);
        mMA20Layer.setMaxCount(nLineCount, 0);

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
        mVolumeAxisLayer.setMaxValue(mVolumeLayer.getMaxValue());
        mVolumeAxisLayer.setMinValue(0);
    }

    private void resetLines() {
        resetLines(0);
    }

    private void reStartGame() {
        m_lstCpx.clear();
        mBSTopLayer.setBSItems(m_lstCpx);
        m_sOPHistory = "";
        m_sGameOverMsg = "";

        setSaleData();
        mIsBuy_in = false;
        // m_fFirstAsserts = m_fTotalAssets;
        setFirstAssets(getTotalAssets());
        resetLayers();
        updateGameNotice();
        notifyChartRefresh(false);
        mLlBusyNotice.setVisibility(View.VISIBLE);
        interactServer(CMD_STARTGAME);
    }

    private void resetLayers() {
        mKLineLayer.setColumnarWidth(mColumnWidth);
        mVolumeLayer.setColumnarWidth(mColumnWidth);

        mKLineLayer.resetData();
        mVolumeLayer.resetData();
        mMA5Layer.resetData();
        mMA10Layer.resetData();
        mMA20Layer.resetData();
    }

    private void putData(KLineHero_Data heroData) {
        float close5 = 0;
        float close10 = 0;
        float close20 = 0;
        Iterator<CandleStick> it5;
        Iterator<CandleStick> it10;
        Iterator<CandleStick> it20;

        if (heroData != null) {
            resetLayers();
        }

        int goodsid = heroData.getGoodsId();
        String sGoodid = Util.FormatStockCode(goodsid);
        ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(sGoodid, 1);
        closeSQLDBHelper();
        if (m_pGN != null && m_pGN.size() > 0) {
            Goods g = m_pGN.get(0);
            mGoodsName = g.getGoodsName();
            mGoodsCode = g.getGoodsCode();
        }

        mFirstClose = heroData.getFisrtClose();
        LogUtil.easylog("sky", "firstClose:" + mFirstClose);

        List<CandleStick> lstData = heroData.getKLinesList();
        LogUtil.easylog("sky", "TotalKLineCount:" + lstData.size());
        it5 = lstData.iterator();
        it10 = lstData.iterator();
        it20 = lstData.iterator();
        for (int i = 0; i < lstData.size(); i++) {
            CandleStick cs = lstData.get(i);
            // LogUtil.easylog("sky", "" + "price: " + cs.getPrice() + ";open: "
            // + cs.getOpen() + ";low: " + cs.getLow() + ";high: " +
            // cs.getHigh() + ";volume: " + cs.getVolume() + ";amount: " +
            // cs.getAmount() + ";time: " + cs.getDatetime());
            close5 += cs.getPrice();
            close10 += cs.getPrice();
            close20 += cs.getPrice();

            float ma5 = 0;
            float ma10 = 0;
            float ma20 = 0;
            if (i >= 4) {
                ma5 = close5 / 5;
                close5 -= it5.next().getPrice();
            }

            if (i >= 9) {
                ma10 = close10 / 10;
                close10 -= it10.next().getPrice();
            }

            if (i >= 19) {
                ma20 = close20 / 20;
                close20 -= it20.next().getPrice();
            } else {
                // 去掉前19根
                continue;
            }

            float open = cs.getOpen();
            float high = cs.getHigh();
            float close = cs.getPrice();
            float low = cs.getLow();

            ColumnarAtom klineCol = new ColumnarAtom(open / 1000, high / 1000, close / 1000, low / 1000);
            klineCol.mTag = cs.getDatetime();

            if (i == 19) {
                int t_iDate = cs.getDatetime();
                mStartTime = DataUtils.formatDateY_M_D(String.valueOf(t_iDate), "/");
            }

            mKLineLayer.addValue(klineCol);
            mVolumeLayer.addValue(new ColumnarAtom(cs.getAmount()));
            mMA5Layer.addValue(ma5 / 1000);
            mMA10Layer.addValue(ma10 / 1000);
            mMA20Layer.addValue(ma20 / 1000);
        }

        resetLines();

        mChartView.forceAdjustLayers();
        notifyChartRefresh(true);

        mGameState = 0;

        getModule().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateControlBtn();
            }
        });

        mRemainCount = mKLineLayer.getValueCount() - KLINE_COUNT;

    }

    private void updateControlBtn() {
        if (mGameState == 2) {
            mBtnRlControl1.setVisibility(View.INVISIBLE);
            mBtnRlControl2.setVisibility(View.VISIBLE);
            mBtnTvControl1.setEnabled(true);
            mBtnTvControl2.setEnabled(true);
            mBtnTvControl2.setText(R.string.klinehero_game_again);
        } else if (mGameState == 1 || mGameState == 0) {
            mBtnRlControl1.setVisibility(View.VISIBLE);
            mBtnRlControl2.setVisibility(View.VISIBLE);
            mBtnTvControl1.setEnabled(true);
            mBtnTvControl2.setEnabled(true);

            if (mIsBuy_in) {
                // 持仓
                mBtnTvControl1.setText(R.string.klinehero_holde);
                mBtnTvControl2.setText(R.string.klinehero_sell);
                if (getCurDayKLineState() == -1) { // 当前跌停,禁止卖出
                    mBtnTvControl2.setEnabled(false);
                }
            } else {
                // 空仓
                mBtnTvControl1.setText(R.string.klinehero_buy);
                mBtnTvControl2.setText(R.string.klinehero_wait);
                if (getCurDayKLineState() == 1) { // 当前跌停,禁止买入
                    mBtnTvControl1.setEnabled(false);
                }
            }
        } else {
            mBtnRlControl1.setVisibility(View.VISIBLE);
            mBtnRlControl2.setVisibility(View.VISIBLE);
            mBtnTvControl1.setEnabled(false);
            mBtnTvControl2.setEnabled(false);
            mBtnTvControl1.setText(R.string.klinehero_buy);
            mBtnTvControl2.setText(R.string.klinehero_wait);
        }
    }

    private void updateWinPercent() {
        if (m_iTotalGameCount > 0 && m_iWinCount >= 0) {
            float t_winPercent = (float) m_iWinCount / m_iTotalGameCount;
            mTvWinPercentage.setText(FORMAT_WINPROFIT + DataUtils.formatFloat2Percent(t_winPercent));
        }
    }

    /**
     * 更新游戏界面提示
     */
    private void updateGameNotice() {
        if (mGameState == -1) {
            mTvStockName.setText("");
            mTvStockName.setVisibility(View.INVISIBLE);

            mTvGameInfo.setText("");
            mTvGameInfo.setVisibility(View.INVISIBLE);

            mTvCurProfit.setText("");
            mTvCurProfit.setTextColor(RColor(R.color.t3));
            // mTvCurProfit.setText(FORMAT_CURPROFIT + "--%");
        } else if (mGameState == 0 || mGameState == 1) {
            mTvStockName.setText("");
            mTvStockName.setVisibility(View.INVISIBLE);

            if (mRemainCount <= 30) {
                mTvGameInfo.setText("当前还剩" + mRemainCount + "根K线");
            } else {
                mTvGameInfo.setText("");
            }

            mTvGameInfo.setVisibility(View.VISIBLE);

            if (mGameState == 0) {
                mTvCurProfit.setText("点击买入或观望开始操盘");
                mTvCurProfit.setTextColor(RColor(R.color.t3));
            } else if (mGameState == 1 && mTvCurProfit.getText().toString().equals("点击买入或观望开始操盘")) {
                mLastCommitTime = System.currentTimeMillis();
                Spanned ts = Html.fromHtml(FORMAT_CURPROFIT + "<b><big>0.0%</big></b>");
                mTvCurProfit.setText(ts);
            }

            /*
             * if (mTvLastKPrice != null && mKLineLayer != null) { float t_fLastKPrice = 0; try {
             * t_fLastKPrice = mKLineLayer.getDisplayLastValue().mClose; } catch (Exception e) { //
             * TODO: handle exception }
             * 
             * DecimalFormat decimalFormat = new DecimalFormat("0.000"); String sLastPrice =
             * decimalFormat.format(t_fLastKPrice); mTvLastKPrice.setText(sLastPrice); }
             */
        } else if (mGameState == 2) {
            mTvStockName.setText(mGoodsName + " (" + mGoodsCode + ")");
            mTvStockName.setVisibility(View.VISIBLE);

            mTvGameInfo.setText(mStartTime + "始");
            mTvGameInfo.setVisibility(View.VISIBLE);

            if (gameOverDialog != null && gameOverDialog.isShowing()) {
                gameOverDialog.setCustomMessage(m_sGameOverMsg);
            }
            // mTvGameOverNotice.setText(m_sGameOverMsg);
            //
            // if (m_sGameOverMsg != null && !m_sGameOverMsg.equals("")) {
            // mTvGameOverNotice.setVisibility(View.VISIBLE);
            // mPbGameOverNoticeWait.setVisibility(View.GONE);
            // mTvGameOverNotice.setText(m_sGameOverMsg);
            //
            // }
        }
    }

    /**
     * move dir right one step
     * 
     * @return 剩余未显示的根数
     */
    private int showNextKLine() {
        mKLineLayer.moveStartPos(1);
        mMA5Layer.moveStartPos(1);
        mMA10Layer.moveStartPos(1);
        mMA20Layer.moveStartPos(1);
        mVolumeLayer.moveStartPos(1);

        resetLines();
        mChartView.forceAdjustLayers();
        notifyChartRefresh(false);

        mAlreadyShowCount = mKLineLayer.getStartPos() + KLINE_COUNT;
        int t_remain = mKLineLayer.getValueCount() - mAlreadyShowCount;
        // LogUtil.easylog("sky", " start:" + mKLineLayer.getStartPos() +
        // " alShow:" + mAlreadyShowCount + " remain:" + t_remain);

        notifyChartRefresh(true);
        return t_remain;
    }

    /**
     * 设置买入点数据
     */
    private void setBuyData(float buyPrice) {
        // m_fLastBuyPrice = buyPrice;
        setLastBuyPrice(buyPrice);
        // m_fLastBuyTotalAssets = m_fTotalAssets;
        setLastBuyTotalAssets(getTotalAssets());
    }

    /**
     * 卖出时清除数据
     */
    private void setSaleData() {
        // m_fLastBuyPrice = 0;
        setLastBuyPrice(0);
        // m_fLastBuyTotalAssets = 0;
        setLastBuyTotalAssets(0);
    }

    /**
     * 获取用户数据
     */
    private void getKHeroUserInfo() {
        UserInfo uinfo = DataModule.getInstance().getUserInfo();
        boolean bIsLogined = uinfo.isLogined();
        if (bIsLogined) { // 登录提交数据
            interactServer(CMD_QRYUSERINFO);
        } else {
            String t_s = getDBHelper().getString(DataModule.G_KEY_KHEROINFO, "");
            if (t_s != null && !t_s.equals("")) {
                mKHeroUInfo_loc = KHeroUInfo.parseFrom(t_s);
            } else {
                mKHeroUInfo_loc = new KHeroUInfo();
            }
            m_iTotalGameCount = mKHeroUInfo_loc.mTotalCount;
            m_iWinCount = mKHeroUInfo_loc.mWinCount;
            // m_fTotalAssets = mKHeroUInfo_loc.mTotalMoney;
            setTotalAssets(mKHeroUInfo_loc.mTotalMoney);
            // M_INITASSERTS = (float) (m_fTotalAssets - mKHeroUInfo_loc.mEarn);
            setInitAssets((float) (getTotalAssets() - mKHeroUInfo_loc.mEarn));

            m_fMaxProfit = (float) mKHeroUInfo_loc.mMaxProfit;

            // 累计收益
            // double t_fTotalProfit = (m_fTotalAssets - M_INITASSERTS) /
            // M_INITASSERTS;
            double t_fTotalProfit = (getTotalAssets() - getInitAssets()) / getInitAssets();

            // updateUserInfo(m_fMaxProfit, t_fTotalProfit, m_fMaxProfit,
            // m_fTotalAssets);
            updateUserInfo(m_fMaxProfit, t_fTotalProfit, m_fMaxProfit, getTotalAssets());
            updateWinPercent();

            reStartGame();
        }

    }

    /**
     * 保存数据,上传server
     */
    private void saveAndCommit() {
        UserInfo uinfo = DataModule.getInstance().getUserInfo();
        boolean bIsLogined = uinfo.isLogined();
        if (bIsLogined) { // 登录提交数据
            interactServer(CMD_COMMITSULT);
        } else {
            if (mGameState == 2) {
                // double t_dEarn = m_fTotalAssets - m_fFirstAsserts;
                double t_dEarn = getTotalAssets() - getFirstAssets();
                if (t_dEarn > 0) {
                    mKHeroUInfo_loc.mWinCount = ++m_iWinCount;
                } else if (t_dEarn < 0) {
                    mKHeroUInfo_loc.mLostCount++;
                }
                mKHeroUInfo_loc.mTotalCount = ++m_iTotalGameCount;

                // mKHeroUInfo_loc.mTotalMoney = m_fTotalAssets;
                mKHeroUInfo_loc.mTotalMoney = getTotalAssets();
                // mKHeroUInfo_loc.mEarn = mKHeroUInfo_loc.mTotalMoney -
                // M_INITASSERTS;
                mKHeroUInfo_loc.mEarn = mKHeroUInfo_loc.mTotalMoney - getInitAssets();

                // if (m_fFirstAsserts != 0) {
                if (getFirstAssets() != 0) {
                    float t_fCurProfit = (float) (t_dEarn / getFirstAssets());
                    // 更新最大收益
                    if (t_fCurProfit > m_fMaxProfit) {
                        m_fMaxProfit = t_fCurProfit;
                    }
                }

                mKHeroUInfo_loc.mMaxProfit = m_fMaxProfit;

                String t_s = mKHeroUInfo_loc.toJsonString();
                getDBHelper().setString(DataModule.G_KEY_KHEROINFO, t_s);

                updateWinPercent();
                m_sGameOverMsg = "每局进步一点点";
                updateGameNotice();
            }

        }

    }

    private void updateUserInfo(float curProfit, double totalProfit, float maxProfit, double totalAssets) {
        if (curProfit >= -1) {
            String sCurProfit = FORMAT_CURPROFIT + "<b><big>" + DataUtils.formatFloat2Percent(curProfit) + "</big></b>";
            Spanned ts = Html.fromHtml(sCurProfit);
            int color = getZDPColor(FontUtils.getColorByZDF(curProfit));
            mTvCurProfit.setText(ts);
            mTvCurProfit.setTextColor(color);
        }

        if (totalProfit >= -1) {
            String sTotalProfit = FORMAT_TOTALPROFIT + DataUtils.formatPercentLevel(totalProfit);
            mTvTotalProfit.setText(sTotalProfit);
        }

        if (maxProfit >= -1) {
            String sMaxProfit = FORMAT_MAXPROFIT + DataUtils.formatFloat2Percent(maxProfit);
            mTvMaxProfit.setText(sMaxProfit);

        }

        if (totalAssets >= 0) {
            String sTotalAssets = FORMAT_TOTALASSETS + DataUtils.formatNumLevel(totalAssets);
            // String sTotalAssets = FORMAT_TOTALASSETS +
            // DataUtils.formatNumLevel(DataUtils.mDecimalFormat4, totalAssets);
            mTvTotalAssets.setText(sTotalAssets);
        }
    }

    /**
     * 买入和持仓状态,计算收益
     */
    private void calcNewData(float nowPrice) {
        // float t_percent = nowPrice / m_fLastBuyPrice;
        float t_percent = nowPrice / getLastBuyPrice();
        // 当前总资产
        // m_fTotalAssets = t_percent * m_fLastBuyTotalAssets;
        setTotalAssets(t_percent * getLastBuyTotalAssets());
        // 本局收益
        // float t_fCurProfit = (float) ((m_fTotalAssets - m_fFirstAsserts) /
        // m_fFirstAsserts);
        float t_fCurProfit = (float) ((getTotalAssets() - getFirstAssets()) / getFirstAssets());
        // 更新最大收益
        float t_fMaxProfit = m_fMaxProfit;
        if (t_fCurProfit > m_fMaxProfit) {
            t_fMaxProfit = t_fCurProfit;
        }
        // 累计收益
        // double t_fTotalProfit = (m_fTotalAssets - M_INITASSERTS) /
        // M_INITASSERTS;
        double t_fTotalProfit = (getTotalAssets() - getInitAssets()) / getInitAssets();

        // updateUserInfo(t_fCurProfit, t_fTotalProfit, t_fMaxProfit,
        // m_fTotalAssets);
        updateUserInfo(t_fCurProfit, t_fTotalProfit, t_fMaxProfit, getTotalAssets());
    }

    /**
     * 计算本局当前收益
     * 
     * @return
     */
    private double calcEarn() {
        // return m_fTotalAssets - m_fFirstAsserts;
        return getTotalAssets() - getFirstAssets();
    }

    @Override
    public void requestData() {

    }

    private void interactServer(FixPair<String, Integer> cmd) {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        if (userInfo == null) {
            return;
        }

        JSONObject jsObj = new JSONObject();
        JSONObject jsObj_params = new JSONObject();
        try {
            // "earn": -9195.402298850575,
            // "flag": 0,
            // "op": "20111109_B|",
            // "token": "64384fc054b6d4f2cb7e4d199a91c391"
            jsObj_params.put(KeysInterface.KEY_TOKEN, userInfo.getToken());
            if (cmd.second >= ID_COMMITSULT_MIN) {
                String sd_earn = "0.0";
                DecimalFormat decimalFormat = new DecimalFormat("0.0###");
                sd_earn = decimalFormat.format(calcEarn());
                // LogUtil.e("earn:" + sd_earn);

                jsObj_params.put("earn", sd_earn);
                int flag = mGameState == 2 ? 1 : 0;
                jsObj_params.put("flag", flag);
                jsObj_params.put("op", m_sOPHistory);
            }

            jsObj.put("id", cmd.second);
            jsObj.put("jsonrpc", "jsonrpc_server");
            jsObj.put("method", cmd.first);
            jsObj.put(KeysInterface.KEY_PARAMS, jsObj_params);

            requestInfo(jsObj, IDUtils.ID_KHERO);
            LogUtil.easylog("sky", "KHeroPage->Request: " + jsObj.toString());
            cmd.second++;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            LogUtil.easylog("sky", "KHeroPage->updateFromInfo: " + msgData);
            if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                return;
            }
            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                JSONObject jsObj_result = null;
                String sResult = jsObj.getString("result");
                if (sResult != null && !sResult.equals("") && !sResult.equals("null")) {
                    jsObj_result = jsObj.getJSONObject("result");
                }

                int t_id = jsObj.getIntValue(KeysInterface.KEY_ID);
                // 查询用户信用
                if (t_id >= ID_QRYUSERINFO_MIN && t_id < ID_STARTGAME_MIN) {
                    receiveUserInfo(jsObj_result);
                }
                // 新开一局
                else if (t_id >= ID_STARTGAME_MIN && t_id < ID_COMMITSULT_MIN) {
                    receiveStartGame(jsObj_result);
                }
                // 提交数据
                else if (t_id >= ID_COMMITSULT_MIN) {
                    receiveCommit(jsObj_result);
                }

            } catch (Exception e) {
            }
        }
    }

    /**
     * 收到服务器用户信息回复
     * 
     * @param jsonObject
     */
    private void receiveUserInfo(JSONObject result) {
        // "result": {
        // "accountInfo": {
        // "earn": 0, //赚的钱
        // "lost": 0, //输的局数
        // "maxProfit": 0, //最大收益率
        // "money": 500000, //资产
        // "total": 0, //玩的总局数
        // "win": 0 //胜利局数
        // }
        // }

        if (result == null) {
            updateGameNotice();
            return;
        }

        try {
            JSONObject accountInfo = result.getJSONObject("accountInfo");
            m_iTotalGameCount = accountInfo.getIntValue("total");
            m_iWinCount = accountInfo.getIntValue("win");
            // m_fTotalAssets = accountInfo.getDouble("money");
            setTotalAssets(accountInfo.getDouble("money"));
            // M_INITASSERTS = (float) (m_fTotalAssets -
            // accountInfo.getDouble("earn"));
            setInitAssets((float) (getTotalAssets() - accountInfo.getDouble("earn")));
            m_fMaxProfit = (float) accountInfo.getDoubleValue("maxProfit") / 100;

            // 累计收益
            // double t_fTotalProfit = (m_fTotalAssets - M_INITASSERTS) /
            // M_INITASSERTS;
            double t_fTotalProfit = (getTotalAssets() - getInitAssets()) / getInitAssets();

            // updateUserInfo(-999, t_fTotalProfit, m_fMaxProfit,
            // m_fTotalAssets);
            updateUserInfo(-999, t_fTotalProfit, m_fMaxProfit, getTotalAssets());
            updateWinPercent();
            reStartGame();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 收到服务器新开一局回复
     * 
     * @param jsonObject
     */
    private void receiveStartGame(JSONObject result) {
        // "result": {
        // "data":
        // "http://down3.emstock.com.cn/istock/klinegame/2014081801/9832.dat",
        // "md5": "105271a3004e674dabf42f9d8712a42a",
        // "page": 80
        // }

        if (result == null) {
            updateGameNotice();
            return;
        }
        try {
            m_sDataUrl = result.getString("data");
            m_sBufMd5 = result.getString("md5");
            m_iInitShowCount = result.getIntValue("page");

            // LogUtil.easylog("sky", "KLinePage->ServerMd5:" + m_sBufMd5);
            downloadKLineData(m_sDataUrl, new DownloadFileCallBack() {
                @Override
                public void onSuccess(String path) {
                    try {
                        File apkFile = new File(path);
                        if (!apkFile.exists()) {
                            return;
                        }
                        FileInputStream in = new FileInputStream(apkFile);

                        String myCalcMd5 = MD5Util.getMd5ByFile(in, apkFile.length());
                        // LogUtil.easylog("sky", "KLinePage->MyCalcMd5:" +
                        // myCalcMd5);
                        final KLineHero_Data heroData = KLineHero_Data.parseFrom(in);
                        in.close();
                        apkFile.delete();

                        if (!myCalcMd5.equals(m_sBufMd5)) {
                            // LogUtil.e("KLinePage->receiveStartGame fileError");
                            showTip("fileError");
                            return;
                        }
                        putData(heroData);

                        getModule().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateGameNotice();
                                mLlBusyNotice.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProcess(int process) {

                }

                @Override
                public void onFail() {
                    showTip("服务器正忙...");
                }

                @Override
                public void onStart() {
                    // TODO Auto-generated method stub
                    
                }
            });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 收到服务器提交数据回复
     * 
     * @param jsonObject
     */
    private void receiveCommit(JSONObject result) {
        // "result": {
        // "accountInfo": {
        // "earn": -112643.6782,
        // "lost": 1,
        // "maxProfit": -22.5287,
        // "money": 387356.3218,
        // "total": 1,
        // "win": 0
        // },
        // "begin": "20110613",
        // "code": "1002079",
        // "end": "20120326",
        // "msg": "好的交易，源自良好的心态，拼命加油哦！"
        // }

        LogUtil.easylog("sky", "KHero->receiveCommit:" + result);
        if (result == null) {
            updateGameNotice();
            return;
        }

        JSONObject accountInfo;
        try {
            accountInfo = result.getJSONObject("accountInfo");
            m_iTotalGameCount = accountInfo.getIntValue("total");
            m_iWinCount = accountInfo.getIntValue("win");
            // m_fTotalAssets = accountInfo.getDouble("money");
            setTotalAssets(accountInfo.getDouble("money"));

            // M_INITASSERTS = (float) (m_fTotalAssets -
            // accountInfo.getDouble("earn"));
            setInitAssets((float) (getTotalAssets() - accountInfo.getDouble("earn")));
            m_fMaxProfit = (float) accountInfo.getDoubleValue("maxProfit") / 100;

            // 累计收益
            // double t_fTotalProfit = (m_fTotalAssets - M_INITASSERTS) /
            // M_INITASSERTS;
            double t_fTotalProfit = (getTotalAssets() - getInitAssets()) / getInitAssets();

            // updateUserInfo(-999, t_fTotalProfit, m_fMaxProfit,
            // m_fTotalAssets);
            updateUserInfo(-999, t_fTotalProfit, m_fMaxProfit, getTotalAssets());
            updateWinPercent();

            String sGoodid = Util.FormatStockCode(result.getIntValue("code"));
            ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(sGoodid, 1);
            closeSQLDBHelper();
            if (m_pGN != null && m_pGN.size() > 0) {
                Goods g = m_pGN.get(0);
                mGoodsName = g.getGoodsName();
                mGoodsCode = g.getGoodsCode();
            }
            m_sGameOverMsg = result.getString("msg");

            updateGameNotice();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void downloadKLineData(String apkDownloadUrl, DownloadFileCallBack callback) {
        String[] aryStr = apkDownloadUrl.split("/");
        new DownloadFileThread(apkDownloadUrl, "KHero", aryStr[aryStr.length - 1], callback).start();
    }

    /**
     * 添加操盘记录
     * 
     * @param cpx_cmd -1:卖; 1:买
     * @param time 操作k线日期
     */
    private void addCpxRecord(int cpx_cmd, int time) {
        String sCpx = "";
        if (cpx_cmd == 1) {
            sCpx = time + "_B|";
        } else if (cpx_cmd == -1) {
            sCpx = time + "_S|";
        }
        m_sOPHistory += sCpx;

        cpx_item.Builder builder = cpx_item.newBuilder();
        builder.setBsFlag(String.valueOf(cpx_cmd));
        builder.setDatetime(time);
        cpx_item item = builder.build();
        m_lstCpx.add(item);
        mBSTopLayer.setBSItems(m_lstCpx);

        // mChartView.postInvalidate();
    }

    /**
     * 判断当日涨停跌停情况
     * 
     * @return 0:正常; 1:一字涨停; -1:一字跌停
     */
    private int getCurDayKLineState() {
        DecimalFormat df = new DecimalFormat("#.00");

        ColumnarAtom cAtom = mKLineLayer.getDisplayLastValue();
        ColumnarAtom preCAtom = mKLineLayer.getDisplayLastValue(-1);

        float limitUp = Float.valueOf(df.format(preCAtom.mClose * 1.1f));
        float limitDown = Float.valueOf(df.format(preCAtom.mClose * 0.9f));

        // LogUtil.easylog("sky", "preClose:" + preCAtom.mClose + ", limitUp:" +
        // fLimitUpOrig + "->" + limitUp + ", limitDown:" + fLimitDownOrig +
        // "->" + limitDown);

        int nRet = 0;
        if (cAtom.mLow == cAtom.mHigh && cAtom.mLow >= limitUp) {
            nRet = 1;
        } else if (cAtom.mLow == cAtom.mHigh && cAtom.mHigh <= limitDown) {
            nRet = -1;
        }

        return nRet;
    }


    private void showGameOverDialog() {
        if (gameOverDialog == null) {
            gameOverDialog = new GameOverDialog(getContext(), null);
            gameOverDialog.setButtonText("确认", null);
            gameOverDialog.setCanceledOnTouchOutside(false);
        }

        gameOverDialog.setCustomTitle(mTvCurProfit.getText().toString(), mTvCurProfit.getCurrentTextColor());
        gameOverDialog.setCustomMessage(m_sGameOverMsg);
        gameOverDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getModule().finish();
        }
        return true;
    }

    private static class KHeroUInfo {
        double mEarn = 0;
        int mLostCount = 0;
        float mMaxProfit = 0;
        double mTotalMoney = 500000;
        int mTotalCount = 0;
        int mWinCount = 0;

        public String toJsonString() {
            JSONObject t_jsonObjKHero = new JSONObject();
            try {
                t_jsonObjKHero.put("earn", mEarn);
                t_jsonObjKHero.put("lost", mLostCount);
                t_jsonObjKHero.put("maxProfit", mMaxProfit);
                t_jsonObjKHero.put("money", mTotalMoney);
                t_jsonObjKHero.put("total", mTotalCount);
                t_jsonObjKHero.put("win", mWinCount);

                return t_jsonObjKHero.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        public static KHeroUInfo parseFrom(String jsonString) {
            try {
                KHeroUInfo t_kHeroUInfo = new KHeroUInfo();
                JSONObject t_jsonObjKHero = JSON.parseObject(jsonString);
                t_kHeroUInfo.mEarn = t_jsonObjKHero.getDouble("earn");
                t_kHeroUInfo.mLostCount = t_jsonObjKHero.getIntValue("lost");
                t_kHeroUInfo.mMaxProfit = (float) t_jsonObjKHero.getDoubleValue("maxProfit");
                t_kHeroUInfo.mTotalMoney = t_jsonObjKHero.getDouble("money");
                t_kHeroUInfo.mTotalCount = t_jsonObjKHero.getIntValue("total");
                t_kHeroUInfo.mWinCount = t_jsonObjKHero.getIntValue("win");

                return t_kHeroUInfo;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
