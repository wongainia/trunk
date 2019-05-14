package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import android.graphics.Paint;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.RecentNetInflowPackage;
import cn.emoney.acg.data.protocol.quote.RecentNetInflowReply.RecentNetInflow_Reply;
import cn.emoney.acg.data.protocol.quote.RecentNetInflowReply.RecentNetInflow_Reply.DayNetInflow;
import cn.emoney.acg.data.protocol.quote.RecentNetInflowRequest.RecentNetInflow_Request;
import cn.emoney.acg.helper.FixPair;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.view.PieChartView;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.ChartLayer.OnDrawingListener;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer;
import cn.emoney.sky.libs.chart.layers.ColumnarLayer.ColumnarAtom;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;

public class FundInflowPage extends PageImpl {
    private final static int COLUMN_WIDTH = 19;

    private final static int UNITS = 10000;

    /**
     * 主力资金净流刷新类型有两种： 1. 手动下拉刷新： 立即刷新 2. 定时自动刷新： 5分钟刷新1次
     * */
    public static final int FUNDFLOW_REFRESH_TYPE_PULL = 1001;
    public static final int FUNDFLOW_REFRESH_TYPE_AUTO = 1002;

    private LineLayer mDivideLineLayer;
    private ColumnarLayer mFundinflowLayer;
    private StackLayer mStacklayer;

    private int mGoodsId = 0;
    private int mColumnWidth = 0;
    // 自动刷新请求次数
    private int nRefreshCount = 0;
    private int nPeriodCount = 0;

    private TextView mTvDateStart;
    private TextView mTvDateEnd;

    private PieChartView mPieChartView;
    private ChartView mTenDayChartView;

    private List<TextView> mLstTvDayInflow = new ArrayList<TextView>(4);

    @Override
    protected void initPage() {
        setContentView(R.layout.page_fundinflow);

        createTenDayFundView();
        createDayFundView();
    }

    private void createTenDayFundView() {
        mColumnWidth = FontUtils.dip2px(getContext(), COLUMN_WIDTH);

        mTvDateStart = (TextView) findViewById(R.id.fundinflow_page_tv_date_start);
        mTvDateEnd = (TextView) findViewById(R.id.fundinflow_page_tv_date_end);

        mTenDayChartView = (ChartView) findViewById(R.id.fundinflow_page_cv);

        mDivideLineLayer = new LineLayer();
        mDivideLineLayer.setMaxCount(10);
        mDivideLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
        mDivideLineLayer.setStrokeWidth(1);
        mDivideLineLayer.setColor(RColor(R.color.b5));
        // mDivideLineLayer.setFloorValue(0, false);

        mFundinflowLayer = new ColumnarLayer();
        mFundinflowLayer.setMaxCount(10);
        mFundinflowLayer.setIgnoreParentPadding(true);
        mFundinflowLayer.setColumnarWidth(mColumnWidth);
        mFundinflowLayer.setLineWidth(FontUtils.dip2px(getContext(), 1));
        mFundinflowLayer.setLineColor(RColor(R.color.sky_line_min_ratio));
        mFundinflowLayer.setCheckWidth(FontUtils.dip2px(getContext(), 0));
        mFundinflowLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
        mFundinflowLayer.setPaddings(22, 0, 22, 0);
        mFundinflowLayer.setOnDrawingListener(new OnDrawingListener() {

            @Override
            public void onDrawing(Paint paint, int pos) {
                ColumnarAtom atom = mFundinflowLayer.getValue(pos);

                int color = getZDPColor(1);
                int zdFlag = FontUtils.getColorByZD(atom.mClose);
                color = getZDPColor(zdFlag);

                paint.setColor(color);
            }
        });

        mStacklayer = new StackLayer();
        mStacklayer.addLayer(mDivideLineLayer);
        mStacklayer.addLayer(mFundinflowLayer);

        mTenDayChartView.addLayer(mStacklayer);
        mTenDayChartView.postInvalidate();
    }

    private void createDayFundView() {
        mPieChartView = (PieChartView) findViewById(R.id.day_fundinflow_pie);

        mPieChartView.setBgColor(RColor(R.color.b1));
        mPieChartView.setBgEdgeWidth(FontUtils.dip2px(getContext(), 4));
        mPieChartView.setAnnulusWidth(FontUtils.dip2px(getContext(), 24));
        mPieChartView.setFgColor(RColor(R.color.b4));
        mPieChartView.setStartAngle(180);

        mLstTvDayInflow.clear();
        for (int i = 1; i <= 4; i++) {
            int resId = getResIdByStr("id", "tv_day_fundinflow_tag_", i);
            TextView tv = (TextView) findViewById(resId);
            mLstTvDayInflow.add(tv);
        }

    }

    private void refreshDayFund(RecentNetInflow_Reply reply) {
        int colors[] = new int[] {0xffe60012, 0xfff95d5b, 0xff009600, 0xff1dbf60};

        List<Integer> lstFlow = new ArrayList<Integer>();
        if (reply.hasCurrentdayInflow()) {
            String currentDayInflow = reply.getCurrentdayInflow();
            LogUtil.easylog(currentDayInflow);
            try {
                JSONObject jObject = JSONObject.parseObject(currentDayInflow);
                long bLarge = jObject.getLongValue("bHuge") + jObject.getLongValue("bLarge");
                long sLarge = jObject.getLongValue("sHuge") + jObject.getLongValue("sLarge");

                long bSmall = jObject.getLongValue("bMiddle") + jObject.getLongValue("bLittle");
                long sSmall = jObject.getLongValue("sMiddle") + jObject.getLongValue("sLittle");

                double total = Math.abs(bLarge) + Math.abs(sLarge) + Math.abs(bSmall) + Math.abs(sSmall);
                int percent_bLarge = (int) (Math.abs(bLarge) / total * 100);
                int percent_bSmall = 50 - percent_bLarge;

                int percent_sLarge = (int) (Math.abs(sLarge) / total * 100);
                int percent_sSmall = 50 - percent_sLarge;


                lstFlow.clear();
                lstFlow.add(percent_bLarge);
                lstFlow.add(percent_bSmall);
                lstFlow.add(percent_sSmall);
                lstFlow.add(percent_sLarge);

                // {"Date":20151222,"bHuge":25211479,"sHuge":15754016,"bLarge":85557145,"sLarge":163157045,"bMiddle":109711118,"sMiddle":93771943,"bLittle":99994674,"sLittle":47791412}
            } catch (Exception e) {
            }


        }

        if (mPieChartView != null) {
            List<FixPair<Integer, Integer>> lstPieData = new ArrayList<FixPair<Integer, Integer>>();
            if (lstFlow.size() == 4) {
                for (int i = 0; i < lstFlow.size(); i++) {
                    FixPair<Integer, Integer> piePair = new FixPair<Integer, Integer>(lstFlow.get(i), colors[i]);
                    lstPieData.add(piePair);
                }
            }


            if (lstPieData.size() == 0) {
                FixPair<Integer, Integer> piePair = new FixPair<Integer, Integer>(100, colors[0]);
                lstPieData.add(piePair);
            }

            mPieChartView.setData(lstPieData);
            mPieChartView.postInvalidate();

            if (mLstTvDayInflow != null && mLstTvDayInflow.size() == 4) {
                for (int i = 0; i < 4 && i < lstFlow.size(); i++) {
                    TextView tv = mLstTvDayInflow.get(i);
                    if (tv != null) {
                        tv.setText(lstFlow.get(i) + "%");
                    }
                }
            }
        }
    }


    private void refreshTenDayFlow(RecentNetInflow_Reply reply) {
        int tCount = reply.getDayNetInflowCount();
        if (tCount > 0) {
            List<DayNetInflow> tLstFuninflow = reply.getDayNetInflowList();

            long max = tLstFuninflow.get(0).getNetInflow() / UNITS;
            long min = tLstFuninflow.get(0).getNetInflow() / UNITS;

            for (int i = 1; i < tCount && i < 10; i++) {
                DayNetInflow dayNetInflow = tLstFuninflow.get(i);
                long fundValue = dayNetInflow.getNetInflow() / UNITS;
                if (fundValue > max) {
                    max = fundValue;
                }

                if (fundValue < min) {
                    min = fundValue;
                }
            }

            float maxValue = Math.max(Math.abs(max), Math.abs(min));
            mDivideLineLayer.clear();

            mFundinflowLayer.clear();
            for (int i = 0; i < tCount && i < 10; i++) {
                mDivideLineLayer.addValue(0);
                DayNetInflow dayNetInflow = tLstFuninflow.get(i);
                long fundValue = dayNetInflow.getNetInflow() / UNITS;

                LogUtil.easylog("DayNetInflow:" + dayNetInflow.getNetInflow());

                ColumnarAtom klineCol;
                if (fundValue > 0) {
                    klineCol = new ColumnarAtom(0, fundValue, fundValue, 0);
                } else {
                    klineCol = new ColumnarAtom(0, 0, fundValue, fundValue);
                }

                mFundinflowLayer.addValue(klineCol);

            }

            mStacklayer.calMinAndMaxValue();
            mDivideLineLayer.setMaxValue(maxValue);
            mDivideLineLayer.setMinValue(-maxValue);
            mFundinflowLayer.setMaxValue(maxValue);
            mFundinflowLayer.setMinValue(-maxValue);

            mTenDayChartView.forceAdjustLayers();
            mTenDayChartView.postInvalidate();

            String sStartDate = DataUtils.formatDateM_D(tLstFuninflow.get(0).getDate() + "", null);
            String sEndDate = DataUtils.formatDateM_D(tLstFuninflow.get(tCount - 1).getDate() + "", null);
            mTvDateStart.setText(sStartDate);
            mTvDateEnd.setText(sEndDate);
        }
    }

    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;
    }

    public void requestData(int refreshType) {
        if (mGoodsId == 0)
            return;

        LogUtil.easylog("test 1:" + mGoodsId);
        if (refreshType == FUNDFLOW_REFRESH_TYPE_PULL) {
            LogUtil.easylog("test 2:" + mGoodsId);
            // 立即刷新
            requestFundFlow();
        } else if (refreshType == FUNDFLOW_REFRESH_TYPE_AUTO) {
            LogUtil.easylog("test 3:" + mGoodsId);
            // 5分钟刷新1次
            if (nPeriodCount > 0 && nRefreshCount % nPeriodCount != 0) {
                nRefreshCount++;
                LogUtil.easylog("test 4:" + mGoodsId);
                return;
            }
            LogUtil.easylog("test 5:" + mGoodsId);
            // 发送网络请求，刷新数据
            requestFundFlow();
        }

    }

    private void requestFundFlow() {
        RecentNetInflowPackage pkg = new RecentNetInflowPackage(new QuoteHead((short) 0));
        pkg.setRequest(RecentNetInflow_Request.newBuilder().setGoodsId(mGoodsId).build());
        requestQuote(pkg, IDUtils.FundInflow);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof RecentNetInflowPackage) {
            RecentNetInflowPackage fundinflowPkg = (RecentNetInflowPackage) pkg;
            RecentNetInflow_Reply reply = fundinflowPkg.getResponse();
            refreshTenDayFlow(reply);

            refreshDayFund(reply);
            nRefreshCount++;
        }
    }


    @Override
    protected void initData() {
        int nCurInterval = getInterval();
        if (nCurInterval > 0) {
            nPeriodCount = DataModule.G_KLINE_REFRESH_INTERVAL / nCurInterval;
        }
    }
}
