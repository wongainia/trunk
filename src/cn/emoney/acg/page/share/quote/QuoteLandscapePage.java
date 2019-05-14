package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import cn.emoney.acg.R;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.widget.SegmentedGroup;
import cn.emoney.sky.libs.bar.Bar.OnBarMenuSelectedListener;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.KVTextLayer;
import cn.emoney.sky.libs.chart.layers.KVTextLayer.KVTextAtom;

public class QuoteLandscapePage extends PageImpl {

    public static String KEY_TIME_S = "key_time_s"; // 时间
    public static String KEY_PRICE_S = "key_price_s"; // 价格
    public static String KEY_ZDF_S = "key_zdf_s"; // 涨跌幅
    public static String KEY_CJ_S = "key_cj_s"; // 成交量
    public static String KEY_JJ_S = "key_jj_s"; // 均价
    public static String KEY_OPEN_S = "key_open_s"; // 开盘价
    public static String KEY_HIGH_S = "key_high_s"; // 最高价
    public static String KEY_LOW_S = "key_low_s"; // 最低价
    public static String KEY_CLOSE_S = "key_close_s"; // 收盘价
    public static String KEY_PRE_CLOSE_S = "key_pre_close_s"; // 昨日收盘价

    private int[] PERIOD_IDS = {TYPE_MINUTE, TYPE_DAY, TYPE_WEEK, TYPE_MONTH, TYPE_60MINUTE, TYPE_30MINUTE, TYPE_15MINUTE};
    private String[] PERIOD_NAMES = {"分时", "日线", "周线", "月线", "60分钟", "30分钟", "15分钟"};

    private SegmentedGroup mSegmentedGroup = null;
    // 分时k线容器
    private ViewFlipper mViewFlipper = null;

    private static int mCurrPeriod = TYPE_MINUTE;

    private PageImpl[] mLstPages = new PageImpl[2];
    private MinuteLandscapePage mMinuteLandscapePage = null;
    private KLineLandscapePage mKLineLandscapePage;

    private ImageButton mBtn_closeModule = null;
    // private LinearLayout mLlTbQuote_content = null;

    private ViewSwitcher mVsInfoDeatail = null;
    public ShowDetailCallBack mCallBack = null;
    private ChartView mCVInfoDetail = null;
    private KVTextLayer mKVTextDetailLayer = null;
    private int mLastCVType = -1;

    private TextView mTvGoodsName = null;
    private TextView mTvGoodsPrice = null;
    private TextView mTvGoodsZdf = null;
    private TextView mTvGoodsLastTime = null;

    private int mGoodsId = 0;
    private String mGoodsName = "";
    private String mCurPrice = "";
    private String mCurZDF = "";
    private int mTextColor = Color.GRAY;

    @Override
    protected void receiveData(Bundle bundle) {
        super.receiveData(bundle);
        if (bundle == null) {
            return;
        }
        if (bundle.containsKey(KEY_GOODSID)) {
            mGoodsId = bundle.getInt(KEY_GOODSID);
        }
        if (bundle.containsKey(KEY_GOODSNAME)) {
            mGoodsName = bundle.getString(KEY_GOODSNAME);
        }
        if (bundle.containsKey(KEY_LINEPERIOD)) {
            mCurrPeriod = bundle.getInt(KEY_LINEPERIOD);
        }
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quote_landscape);
        mBtn_closeModule = (ImageButton) findViewById(R.id.quotepage_landscape_btn_close);
        mBtn_closeModule.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                getModule().finish();
            }
        });

        mVsInfoDeatail = (ViewSwitcher) findViewById(R.id.quotepage_landscape_vs_titleinfo_content);
        mTvGoodsName = (TextView) findViewById(R.id.quotepage_landscape_tv_stockname);
        mTvGoodsPrice = (TextView) findViewById(R.id.quotepage_landscape_tv_stockprice);
        mTvGoodsZdf = (TextView) findViewById(R.id.quotepage_landscape_tv_stockzdf);
        mTvGoodsLastTime = (TextView) findViewById(R.id.quotepage_landscape_tv_time);

        mCallBack = new ShowDetailCallBack() {
            @Override
            public void showDetail(int type, Bundle bundle) {
                mVsInfoDeatail.setDisplayedChild(1);
                if (mLastCVType != type) {
                    mLastCVType = type;
                    initCV(type);
                }

                updateKVText(type, bundle);
            }

            @Override
            public void closeDetail() {
                mVsInfoDeatail.setDisplayedChild(0);
            }
        };

        mViewFlipper = (ViewFlipper) findViewById(R.id.quotepage_landscape_viewflipper);
        if (mViewFlipper != null) {
            mMinuteLandscapePage = new MinuteLandscapePage();
            mMinuteLandscapePage.setShowDetailCB(mCallBack);
            mLstPages[0] = mMinuteLandscapePage;
            mViewFlipper.addView(mMinuteLandscapePage.convertToView(this, getActivity().getLayoutInflater(), null, null));

            mKLineLandscapePage = new KLineLandscapePage();
            mKLineLandscapePage.setShowDetailCB(mCallBack);
            // mKLineLandscapePage.setOnChangeQuoteListener(this);
            mKLineLandscapePage.setParent(this);
            mLstPages[1] = mKLineLandscapePage;
            mViewFlipper.addView(mKLineLandscapePage.convertToView(this, getActivity().getLayoutInflater(), null, null));
        }

        mSegmentedGroup = (SegmentedGroup) findViewById(R.id.page_landscape_kline_segment_periods);

        if (mSegmentedGroup != null) {
            mSegmentedGroup.setOnCheckedChangeListener(new SegmentedGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.page_landscape_kline_radiobtn_period_minute) {
                        mCurrPeriod = PERIOD_IDS[0];
                        mViewFlipper.setDisplayedChild(0);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_day) {
                        mCurrPeriod = PERIOD_IDS[1];
                        mViewFlipper.setDisplayedChild(1);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_week) {
                        mCurrPeriod = PERIOD_IDS[2];
                        mViewFlipper.setDisplayedChild(1);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_month) {
                        mCurrPeriod = PERIOD_IDS[3];
                        mViewFlipper.setDisplayedChild(1);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_60m) {
                        mCurrPeriod = PERIOD_IDS[4];
                        mViewFlipper.setDisplayedChild(1);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_30m) {
                        mCurrPeriod = PERIOD_IDS[5];
                        mViewFlipper.setDisplayedChild(1);
                    } else if (checkedId == R.id.page_landscape_kline_radiobtn_period_15m) {
                        mCurrPeriod = PERIOD_IDS[6];
                        mViewFlipper.setDisplayedChild(1);
                    }

                    mKLineLandscapePage.setPeriod(mCurrPeriod);
                    requestChild(false);
                }


            });
        }

        mCVInfoDetail = (ChartView) findViewById(R.id.quotepage_landscape_cv_infodetail);
        mKVTextDetailLayer = new KVTextLayer();
        mCVInfoDetail.addLayer(mKVTextDetailLayer);
    }

    @Override
    protected void initData() {
        if (mTvGoodsName != null) {
            mTvGoodsName.setText(mGoodsName + " " + QuoteUtils.getGoodsCodeByGoodsid(mGoodsId));
        }

        if (mMinuteLandscapePage != null) {
            mMinuteLandscapePage.setGoodsId(mGoodsId);
        }

        if (mKLineLandscapePage != null) {
            mKLineLandscapePage.setGoodsId(mGoodsId);
        }

        mTextColor = RColor(R.color.t1);

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        doAutoRefresh();

        if (mSegmentedGroup != null && mViewFlipper != null) {
            if (mCurrPeriod == PERIOD_IDS[0]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_minute)).setChecked(true);
                mViewFlipper.setDisplayedChild(0);
            } else if (mCurrPeriod == PERIOD_IDS[1]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_day)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            } else if (mCurrPeriod == PERIOD_IDS[2]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_week)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            } else if (mCurrPeriod == PERIOD_IDS[3]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_month)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            } else if (mCurrPeriod == PERIOD_IDS[4]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_60m)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            } else if (mCurrPeriod == PERIOD_IDS[5]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_30m)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            } else if (mCurrPeriod == PERIOD_IDS[6]) {
                ((RadioButton) findViewById(R.id.page_landscape_kline_radiobtn_period_15m)).setChecked(true);
                mViewFlipper.setDisplayedChild(1);
            }
        }

    }

    private void doAutoRefresh() {
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
        requestChild(true);
        requestBaseQute();
    }

    public void requestBaseQute() {
        ArrayList<Integer> goodsId = new ArrayList<Integer>();
        goodsId.add(mGoodsId);

        ArrayList<Integer> reqFileds = new ArrayList<Integer>();
        reqFileds.add(GoodsParams.ZXJ);
        reqFileds.add(GoodsParams.ZDF);
        reqFileds.add(GoodsParams.VOLUME);

        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 1));
        pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    public void requestChild(boolean isAutoRefresh) {
        PageImpl pageImpl = mLstPages[mViewFlipper.getDisplayedChild()];

        if (pageImpl instanceof KLineLandscapePage) {
            if (isAutoRefresh == true) {
                // 不自动刷新k线
                return;
            }
            KLineLandscapePage klp = (KLineLandscapePage) pageImpl;
            klp.requestData(isAutoRefresh);
        } else if (pageImpl instanceof MinuteLandscapePage) {
            MinuteLandscapePage mlp = (MinuteLandscapePage) pageImpl;
            mlp.requestData();
            mlp.setCurrentPrice(mCurPrice, mCurZDF);
        }
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage dynaValuePkg = (DynaValueDataPackage) pkg;

            DynaValueData_Reply dynaValueReply = dynaValuePkg.getResponse();

            int t_time = dynaValueReply.getCurUpdateMarketTime();

            List<Integer> lstFieldIds = dynaValueReply.getRepFieldsList();
            List<DynaQuota> t_lstDQData = dynaValueReply.getQuotaValueList();
            if (t_lstDQData.size() > 0) {
                DynaQuota quote = t_lstDQData.get(0);
                List<String> lstFieldValues = quote.getRepFieldValueList();

                int indexZXJ = lstFieldIds.indexOf(GoodsParams.ZXJ);
                int indexZDF = lstFieldIds.indexOf(GoodsParams.ZDF);
                // int indexVolume = lstFieldIds.indexOf(GoodsParams.VOLUME);

                String t_price = lstFieldValues.get(indexZXJ);
                String t_zdf = lstFieldValues.get(indexZDF);
                // String t_volume = lstFieldValues.get(indexVolume);

                mCurPrice = DataUtils.getPrice(t_price);
                mCurZDF = DataUtils.getZDF(t_zdf);

                mTvGoodsPrice.setText(mCurPrice);
                int t_color = getZDPColor(FontUtils.getColorByZDF(t_zdf));
                mTvGoodsPrice.setTextColor(t_color);

                mTvGoodsZdf.setText(mCurZDF);
                mTvGoodsZdf.setTextColor(t_color);

                // String sVolume = "";
                // if (DataUtils.IsZS(mGoodsId)) {
                // sVolume = DataUtils.formatVolume(t_volume);
                // } else {
                // long val = Long.valueOf(t_volume);
                // val = val / 100;
                // sVolume = DataUtils.formatVolume(val);
                // }

                String sTime = DataUtils.formatTimeH_M(t_time);
                mTvGoodsLastTime.setText("更新时间  " + sTime);

                PageImpl pageImpl = mLstPages[mViewFlipper.getDisplayedChild()];
                if (pageImpl instanceof MinuteLandscapePage) {
                    ((MinuteLandscapePage) pageImpl).setCurrentPrice(mCurPrice, mCurZDF);
                }
            }
        }

    }


    // type: 1->分时详情; 2->K线详情;
    public static interface ShowDetailCallBack {
        public void showDetail(int type, Bundle bundle);

        public void closeDetail();
    }

    int lableColor = 0xff828282;
    private KVTextAtom mKV1 = new KVTextAtom("", "--", lableColor);
    private KVTextAtom mKV2 = new KVTextAtom("", "--", lableColor);
    private KVTextAtom mKV3 = new KVTextAtom("", "--", lableColor);
    private KVTextAtom mKV4 = new KVTextAtom("", "--", lableColor);
    private KVTextAtom mKV5 = new KVTextAtom("", "--", lableColor);
    private KVTextAtom mKV6 = new KVTextAtom("", "--", lableColor);

    /**
     * 初始化详细行情显示面板
     * 
     * @param type 1->分时详情; 2->K线详情;
     * @return 无
     */
    private void initCV(int type) {
        if (type == 1) {
            initCVForMinute();
        } else if (type == 2) {
            initCVForKLine();
        }
    }

    private void initCVForMinute() {
        if (mCVInfoDetail == null || mKVTextDetailLayer == null) {
            return;
        }

        mKVTextDetailLayer.clearTexts();
        float hPadding = FontUtils.dip2px(getContext(), 10);
        mKVTextDetailLayer.setPaddings(hPadding, 0, hPadding, 0);
        mKVTextDetailLayer.setBorderColor(0x00FFFFFF);
        mKVTextDetailLayer.setTextSize(FontUtils.dip2px(getContext(), 12));
        mKVTextDetailLayer.setRow(1);
        mKVTextDetailLayer.setCol(5);

        mKV1.setLabel("时间:");
        mKV2.setLabel("价格:");
        mKV3.setLabel("涨幅:");
        mKV4.setLabel("成交:");
        mKV5.setLabel("均价:");

        mKVTextDetailLayer.addText(0, 0, mKV1);
        mKVTextDetailLayer.addText(0, 1, mKV2);
        mKVTextDetailLayer.addText(0, 2, mKV3);
        mKVTextDetailLayer.addText(0, 3, mKV4);
        mKVTextDetailLayer.addText(0, 4, mKV5);

        mCVInfoDetail.forceAdjustLayers();
        mCVInfoDetail.postInvalidate();
    }

    private void initCVForKLine() {
        if (mCVInfoDetail == null || mKVTextDetailLayer == null) {
            return;
        }
        mKVTextDetailLayer.clearTexts();
        float hPadding = FontUtils.dip2px(getContext(), 10);
        mKVTextDetailLayer.setPaddings(hPadding, 0, hPadding, 0);
        mKVTextDetailLayer.setBorderColor(0x00FFFFFF);
        mKVTextDetailLayer.setTextSize(FontUtils.dip2px(getContext(), 12));
        mKVTextDetailLayer.setRow(1);
        mKVTextDetailLayer.setCol(6);

        mKV1.setLabel(" ");
        mKV2.setLabel(" 高:");
        mKV3.setLabel(" 开:");
        mKV4.setLabel(" 低:");
        mKV5.setLabel(" 收:");
        mKV6.setLabel(" 涨幅:");

        mKVTextDetailLayer.addText(0, 0, mKV1);
        mKVTextDetailLayer.addText(0, 1, mKV2);
        mKVTextDetailLayer.addText(0, 2, mKV3);
        mKVTextDetailLayer.addText(0, 3, mKV4);
        mKVTextDetailLayer.addText(0, 4, mKV5);
        mKVTextDetailLayer.addText(0, 5, mKV6);

        mCVInfoDetail.forceAdjustLayers();
        mCVInfoDetail.postInvalidate();
    }

    private void updateKVText(int type, Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (type == 1) {
            updateMinuteKVText(bundle);
        } else if (type == 2) {
            updateKLineKVText(bundle);
        }
        if (mCVInfoDetail != null) {
            mCVInfoDetail.postInvalidate();
        }
    }

    private void updateMinuteKVText(Bundle bundle) {
        String t_time = "";
        String t_price = "";
        String t_zdf = "";
        String t_cjl = "";
        String t_jj = "";

        if (bundle.containsKey(KEY_TIME_S)) {
            t_time = bundle.getString(KEY_TIME_S);
        }
        if (bundle.containsKey(KEY_PRICE_S)) {
            t_price = bundle.getString(KEY_PRICE_S);
        }
        if (bundle.containsKey(KEY_ZDF_S)) {
            t_zdf = bundle.getString(KEY_ZDF_S);
        }
        if (bundle.containsKey(KEY_CJ_S)) {
            t_cjl = bundle.getString(KEY_CJ_S);
        }
        if (bundle.containsKey(KEY_JJ_S)) {
            t_jj = bundle.getString(KEY_JJ_S);
        }

        int t_color = getZDPColor(FontUtils.getColorByZDF_percent(t_zdf));
        mKV1.setText(t_time);
        mKV1.setTextColor(mTextColor);
        mKV2.setText(t_price);
        mKV2.setTextColor(t_color);
        mKV3.setText(t_zdf);
        mKV3.setTextColor(t_color);
        mKV4.setText(t_cjl);
        mKV4.setTextColor(mTextColor);
        mKV5.setText(t_jj);
        mKV5.setTextColor(mTextColor);
    }

    private void updateKLineKVText(Bundle bundle) {
        String t_time = "";
        String t_high = "";
        String t_open = "";
        String t_low = "";
        String t_close = "";
        String t_zdf = "";
        String t_preClose = "";

        if (bundle.containsKey(KEY_TIME_S)) {
            t_time = bundle.getString(KEY_TIME_S);
        }
        if (bundle.containsKey(KEY_HIGH_S)) {
            t_high = bundle.getString(KEY_HIGH_S);
        }
        if (bundle.containsKey(KEY_OPEN_S)) {
            t_open = bundle.getString(KEY_OPEN_S);
        }
        if (bundle.containsKey(KEY_LOW_S)) {
            t_low = bundle.getString(KEY_LOW_S);
        }
        if (bundle.containsKey(KEY_CLOSE_S)) {
            t_close = bundle.getString(KEY_CLOSE_S);
        }
        if (bundle.containsKey(KEY_ZDF_S)) {
            t_zdf = bundle.getString(KEY_ZDF_S);
        }
        if (bundle.containsKey(KEY_PRE_CLOSE_S)) {
            t_preClose = bundle.getString(KEY_PRE_CLOSE_S);
        }

        int t_color = mTextColor;
        mKV1.setText(t_time);
        mKV1.setTextColor(mTextColor);

        try {
            t_color = getZDPColor(FontUtils.getColorByPrice(Float.valueOf(t_preClose), Float.valueOf(t_high)));
            mKV2.setTextColor(t_color);
        } catch (Exception e) {
            mKV2.setTextColor(mTextColor);
        }
        mKV2.setText(t_high);

        try {
            t_color = getZDPColor(FontUtils.getColorByPrice(Float.valueOf(t_preClose), Float.valueOf(t_open)));
            mKV3.setTextColor(t_color);
        } catch (Exception e) {
            mKV3.setTextColor(mTextColor);
        }
        mKV3.setText(t_open);

        try {
            t_color = getZDPColor(FontUtils.getColorByPrice(Float.valueOf(t_preClose), Float.valueOf(t_low)));
            mKV4.setTextColor(t_color);
        } catch (Exception e) {
            mKV4.setTextColor(mTextColor);
        }
        mKV4.setText(t_low);

        try {
            t_color = getZDPColor(FontUtils.getColorByPrice(Float.valueOf(t_preClose), Float.valueOf(t_close)));
            mKV5.setTextColor(t_color);
        } catch (Exception e) {
            mKV5.setTextColor(mTextColor);
        }
        mKV5.setText(t_close);

        t_color = getZDPColor(FontUtils.getColorByZDF_percent(t_zdf));
        mKV6.setText(t_zdf);
        mKV6.setTextColor(t_color);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getModule().finish();
            // LogUtil.easylog("QuoteLandscapePage->onkeyDown:KEYCODE_BACK");
        }
        return true;
    }
}
