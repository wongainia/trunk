package cn.emoney.acg.page.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.dialog.CustomDialog;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.helper.FixedLengthList;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.infodetail.InfoDetailHome;
import cn.emoney.acg.page.share.infodetail.InfoDetailPage;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

/**
 * 自选资讯
 * */
public class OptionalNewsPage extends PageImpl {

    private final int MAX_NEWS_COUNT = 50;

    private final short REQUEST_FLAG_REQUEST = 1101;
    private final short REQUEST_FLAG_UPDATE = 1102;
    private final short REQUEST_FLAG_LOADMORE = 1103;
    private final short REQUEST_FLAG_QUOTATION = 1104;

    protected boolean isLoading, isRequesting, isUpdating;

    private String firstGuid, lastGuid;

    private int playStatus = InfoHome.PLAY_STATUS_INIT; // 语音播放状态
    private int currentPlayPosition; // 当前播放位置
    private SpeechSynthesizer mTts; // 语音播放引擎

    private OnPlayStatusChanged onPlayStatusChanged;
    private ArrayList<Integer> listGoodsId = new ArrayList<Integer>();
    private ArrayList<Map<String, String>> listMessageMap = new ArrayList<Map<String,String>>();
    private ArrayList<String> listHasRead = new ArrayList<String>();

    private FixedLengthList<CellBean> listDatas = new FixedLengthList<OptionalNewsPage.CellBean>(MAX_NEWS_COUNT);
    private NewsListAdapter adapter;

    private RefreshListView listView;
    private TextView tvEmpty;    // ListView为空时显示，暂无数据
    private View layoutProgressBar;    // 正在加载
    private View layoutLoadMore;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_optionalnews);

        initViews();
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 启动自动刷新
        if (listDatas.size() == 0 && !isLoading && !isRequesting && !isUpdating) {
            requestOptionalNews();
        } else if (!isLoading && !isRequesting && !isUpdating) {
            requestUpdateOptionalNews();
            isUpdating = true;
        }

        // 界面显示时，更新播放状态标志
        if (onPlayStatusChanged != null) {
            onPlayStatusChanged.onPlayStatusChanged(playStatus);
        }

        // 获取sqlite中缓存的已阅读过的消息列表
        String[] aryFlagMd5s = getDBHelper().getStringArray(DataModule.G_KEY_INFO_OPTIONAL_NEWS, new String[] {});
        List<String> t_Lst = Arrays.asList(aryFlagMd5s);
        listHasRead = new ArrayList<String>(t_Lst);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

        destroySpeechEngine();
    }

    /**
     * 停止并销毁语音播放引擎
     * */
    private void destroySpeechEngine() {
        if (mTts != null) {
            if (mTts.isSpeaking()) {
                mTts.stopSpeaking();
            }

            mTts.destroy();
            mTts = null;
        }
        
        playStatus = InfoHome.PLAY_STATUS_INIT;
    }

    private void initViews() {
        listView = (RefreshListView) findViewById(R.id.page_optionalnews_list);
        tvEmpty = (TextView) findViewById(R.id.page_optionalnews_tv_empty);
        layoutProgressBar = findViewById(R.id.page_optionalnews_layout_loading);
        View listFooter = View.inflate(getContext(), R.layout.include_layout_listfooter_loadmore, null);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);

        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.addFooterView(listFooter);

        layoutLoadMore.setVisibility(View.GONE);

        adapter = new NewsListAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);

        tvEmpty.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (!isRequesting && !isLoading && !isUpdating) {
                    requestOptionalNews();                    
                }
            }
        });

        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isUpdating && !isLoading && !isRequesting) {
                    requestUpdateOptionalNews();
                    isUpdating = true;
                }

                // 3秒钟后，如果仍没有返回，就隐藏header
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isUpdating) {
                            isUpdating = false;

                            if (listView != null) {
                                listView.onRefreshFinished();
                            }
                        }
                    }
                }, DataModule.REQUEST_MAX_LIMIT_TIME);
            }

            @Override
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() { }
        });
        listView.setPostScrollListener(new PostScrollListener() {
            private int previousScrollState = -1;
            private int mCurrentScrollState = -1;
            private boolean isScrolling;
            private boolean isHasLoadMore = true;    // 滚动期间是否已经加载过更多

            @Override
            public void postScrollStateChanged(AbsListView view, int scrollState) {
                if ( scrollState == OnScrollListener.SCROLL_STATE_IDLE ) {
                    view.invalidateViews();
                }

                previousScrollState = mCurrentScrollState;
                mCurrentScrollState = scrollState;

                if (mCurrentScrollState == OnScrollListener.SCROLL_STATE_FLING || mCurrentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                } else {
                    isScrolling = false;
                }
                if (previousScrollState == OnScrollListener.SCROLL_STATE_IDLE && mCurrentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isHasLoadMore = false;
                }
            }

            @Override
            public void postScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == totalItemCount || totalItemCount == 0 || visibleItemCount == 0) {
                    layoutLoadMore.setVisibility(View.GONE);
                    return;
                }

                boolean isLoadMore = (firstVisibleItem + visibleItemCount >= totalItemCount) && totalItemCount < MAX_NEWS_COUNT 
                        && !isLoading && isScrolling && isHasLoadMore == false;
                if (isLoadMore) {
                    requestMoreOptionalNews();
                    isHasLoadMore = true;
                }
            }
        });
    }

    /**
     * 创建并初始化语音播放引擎
     * */
    private void initSpeechEngine() {
        if (mTts == null) {
            // 1. 创建 SpeechSynthesizer对象，第二个参数：本地合成时传InitListener
            mTts = SpeechSynthesizer.createSynthesizer(getContext(), null);
            
            // 2. 全成参数设置
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); // 设置发音人
            mTts.setParameter(SpeechConstant.SPEED, "50"); // 设置语速
            mTts.setParameter(SpeechConstant.VOLUME, "80"); // 设置音量，范围0~100
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端            
        }
    }

    public void resetPlayStatus() {
        initSpeechEngine();
        
        if (playStatus == InfoHome.PLAY_STATUS_INIT) {
            // 有数据时才播放
            if (listDatas.size() > 0) {
                // 1. 初始状态时点击，开始播放
                playStatus = InfoHome.PLAY_STATUS_PLAY;
                speakCurrentText();
            }
        } else if (playStatus == InfoHome.PLAY_STATUS_PLAY) {
            // 2. 播放状态时点击，暂停播放
            playStatus = InfoHome.PLAY_STATUS_PAUSE;
            mTts.pauseSpeaking();
        } else if (playStatus == InfoHome.PLAY_STATUS_PAUSE) {
            // 3. 暂停状态时点击，继续播放
            playStatus = InfoHome.PLAY_STATUS_PLAY;
            mTts.resumeSpeaking();
        }
    }

    /**
     * 播放从当前位置开始播放文字
     * */
    private void speakCurrentText() {
        // 播放当前位置的文字
        mTts.startSpeaking(getSpeakText(), mSynListener);

        playStatus = InfoHome.PLAY_STATUS_PLAY;
        if (onPlayStatusChanged != null) {
            onPlayStatusChanged.onPlayStatusChanged(playStatus);
        }

        // 更新头条列表中播放状态标志
        if (listDatas.size() > currentPlayPosition) {
            listDatas.get(currentPlayPosition).isSpeaking = true;
            updateViews();
        }
    }

    /**
     * 获取当前播放位置的文字
     * */
    private String getSpeakText() {
        String text = "";

        if (currentPlayPosition < listDatas.size()) {
            // 如果有摘要，播放摘要，如果没有摘要，播放标题
            CellBean bean = listDatas.get(currentPlayPosition);
            String goodsName = bean.stockName;
            String time = bean.newsTime;
            String type = bean.newsType;
            String title = bean.newsTitle;
            text = goodsName + " " 
                    + time + " " 
                    + type + "，" 
                    + title + " 。";
        }

        return text;
    }

    /**
     * 刷新界面显示
     * */
    private void updateViews() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新自选资讯消息列表
     * */
    private void requestOptionalNews() {
        if (isRequesting == false) {
            JSONObject jsObj = new JSONObject();

            try {
                jsObj.put(KEY_FROM, "");
                jsObj.put(KEY_TO, "");
                jsObj.put(KEY_SIZE, 10);
                jsObj.put(KEY_TOKEN, getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsObj, IDUtils.ID_OPTIONAL_NEWS, REQUEST_FLAG_REQUEST);

            isRequesting = true;
            // 正在加载数据，显示正在加载
            listView.setRefreshable(false);
            layoutProgressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);

            // 如果超过限制时间还未返回数据
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRequesting) {
                        isRequesting = false;

                        if (layoutProgressBar != null) {
                            layoutProgressBar.setVisibility(View.GONE);                            
                        }

                        if (tvEmpty != null) {
                            tvEmpty.setText("加载失败，请点击重试");
                            tvEmpty.setVisibility(View.VISIBLE);
                        }

                        // 无数据显示时，不可以下拉刷新
                        listView.setRefreshable(false);
                    }
                }
            }, DataModule.REQUEST_MAX_LIMIT_TIME);

        }
    }

    /**
     * 更新自选资讯消息列表
     * */
    private void requestUpdateOptionalNews() {
        JSONObject jsObj = new JSONObject();

        try {
            jsObj.put(KEY_FROM, "");
            jsObj.put(KEY_TO, firstGuid);
            jsObj.put(KEY_SIZE, 10);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_OPTIONAL_NEWS, REQUEST_FLAG_UPDATE);
    }

    /**
     * 加载更多自选资讯消息列表
     * */
    private void requestMoreOptionalNews() {
        JSONObject jsObj = new JSONObject();

        try {
            jsObj.put(KEY_FROM, lastGuid);
            jsObj.put(KEY_TO, "");
            jsObj.put(KEY_SIZE, 10);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_OPTIONAL_NEWS, REQUEST_FLAG_LOADMORE);

        isLoading = true;
        layoutLoadMore.setVisibility(View.VISIBLE);

        // 如果超过限制时间还未返回数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoading) {
                    isLoading = false;

                    layoutLoadMore.setVisibility(View.GONE);
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }

    /**
     * 获取股票涨跌幅
     * */
    private void requestZdf() {
        if (listGoodsId != null && listGoodsId.size() > 0) {
            ArrayList<Integer> reqFileds = new ArrayList<Integer>();
            reqFileds.add(GoodsParams.ZDF); // 涨跌幅

            DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_FLAG_QUOTATION));
            pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, listGoodsId, reqFileds, -9999, true, 0, 0, 0, 0));
            requestQuote(pkg, IDUtils.DynaValueData);
        }
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        super.updateFromInfo(pkg);
        int id = pkg.getRequestType();

        if (id == REQUEST_FLAG_REQUEST && isRequesting) {
            // 如果尚未超时，就进行解析处理，如果已做超时处理，就再次手动请求
            isRequesting = false;
            layoutProgressBar.setVisibility(View.GONE);

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) return;

            String msgData = mc.getMsgData();

            try {
                JSONObject objMsg = JSONObject.parseObject(msgData);

                if (objMsg != null && objMsg.containsKey("data") && objMsg.containsKey("result")) {
                    String data = objMsg.getString("data");
                    int retCode = objMsg.getIntValue("result");

                    if (retCode == 0) {
                        JSONArray objData = JSONArray.parseArray(data);

                        if (objData != null && objData.size() > 0) {
                            listDatas.clear();
                            listGoodsId.clear();
                            listMessageMap.clear();

                            for (int i = 0; i < objData.size(); i++) {
                                if (listDatas != null && listDatas.size() >= MAX_NEWS_COUNT) {
                                    break;
                                }

                                JSONObject objItem = objData.getJSONObject(i);

                                if (objItem != null && objItem.containsKey("code") && objItem.containsKey("group") && objItem.containsKey("date") 
                                        && objItem.containsKey("title") && objItem.containsKey("from") && objItem.containsKey("summary") 
                                        && objItem.containsKey("content") && objItem.containsKey("guid")) {

                                    int goodsId = objItem.getIntValue("code");
                                    String newsType = objItem.getString("group");
                                    int date = objItem.getIntValue("date");
                                    String title = objItem.getString("title");
                                    String from = objItem.getString("from");
                                    String summary = objItem.getString("summary");
                                    String url = objItem.getString("content");
                                    String guid = objItem.getString("guid");

                                    // 如果url为空，跳过该项
                                    if (TextUtils.isEmpty(url)) continue;

                                    // 更新guid
                                    if (TextUtils.isEmpty(firstGuid)) {
                                        firstGuid = guid;
                                    }
                                    lastGuid = guid;

                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                                    map.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                                    map.put(InfoDetailPage.EXTRA_KEY_TIME, DateUtils.formatQuizCommitTime(date));
                                    map.put(InfoDetailPage.EXTRA_KEY_FROM, from);
                                    map.put(InfoDetailPage.EXTRA_KEY_SORTCLS, newsType);
                                    map.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                                    map.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                                    listMessageMap.add(map);

                                    CellBean bean = new CellBean(goodsId, newsType, date, title, from, summary, url, guid);
                                    listDatas.add(bean);

                                    if (!listGoodsId.contains(goodsId)) {
                                        listGoodsId.add(goodsId);
                                    }
                                }
                            }
                        }

                        if (listDatas.size() > 0) {
                            // 有数据，隐藏空白提示
                            tvEmpty.setVisibility(View.GONE);

                            // 有数据显示时，可以下拉刷新
                            listView.setRefreshable(true);
                        } else {
                            // 无数据，显示空白提示，暂无数据
                            tvEmpty.setText("暂无数据，请点击重试");
                            tvEmpty.setVisibility(View.VISIBLE);

                            // 无数据显示时，不可以下拉刷新
                            listView.setRefreshable(false);
                        }
                    }
                }
            } catch (Exception e) {
                tvEmpty.setText("加载失败，请点击重试");
                tvEmpty.setVisibility(View.VISIBLE);

                // 无数据显示时，不可以下拉刷新
                listView.setRefreshable(false);
            }

        } else if (id == REQUEST_FLAG_UPDATE && isUpdating == true) {
            isUpdating = false;
            if (listView != null) {
                listView.onRefreshFinished();
            }

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) return;

            String msgData = mc.getMsgData();

            try {
                JSONObject objMsg = JSONObject.parseObject(msgData);

                if (objMsg != null && objMsg.containsKey("data") && objMsg.containsKey("result")) {
                    String data = objMsg.getString("data");
                    int retCode = objMsg.getIntValue("result");

                    if (retCode == 0) {
                        JSONArray objData = JSONArray.parseArray(data);

                        if (objData != null && objData.size() > 0) {

                            for (int i = 0; i < objData.size(); i++) {
                                JSONObject objItem = objData.getJSONObject(i);

                                if (objItem != null && objItem.containsKey("code") && objItem.containsKey("group") && objItem.containsKey("date") 
                                        && objItem.containsKey("title") && objItem.containsKey("from") && objItem.containsKey("summary") 
                                        && objItem.containsKey("content") && objItem.containsKey("guid")) {

                                    int goodsId = objItem.getIntValue("code");
                                    String newsType = objItem.getString("group");
                                    int date = objItem.getIntValue("date");
                                    String title = objItem.getString("title");
                                    String from = objItem.getString("from");
                                    String summary = objItem.getString("summary");
                                    String url = objItem.getString("content");
                                    String guid = objItem.getString("guid");

                                    // 如果url为空，跳过该项
                                    if (TextUtils.isEmpty(url)) continue;

                                    // 更新guid
                                    firstGuid = guid;

                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                                    map.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                                    map.put(InfoDetailPage.EXTRA_KEY_TIME, DateUtils.formatQuizCommitTime(date));
                                    map.put(InfoDetailPage.EXTRA_KEY_FROM, from);
                                    map.put(InfoDetailPage.EXTRA_KEY_SORTCLS, newsType);
                                    map.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                                    map.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                                    listMessageMap.add(map);

                                    CellBean bean = new CellBean(goodsId, newsType, date, title, from, summary, url, guid);
                                    listDatas.add(0, bean);

                                    if (!listGoodsId.contains(goodsId)) {
                                        listGoodsId.add(goodsId);
                                    }
                                }

                            }

                            // 如果数据超过最大限制，去除最后多余的
                            if (listDatas.size() > MAX_NEWS_COUNT) {
                                listDatas.subList(0, MAX_NEWS_COUNT - 1);
                            }
                            if (listGoodsId.size() > MAX_NEWS_COUNT) {
                                listGoodsId.subList(0, MAX_NEWS_COUNT - 1);
                            }
                            if (listMessageMap.size() > MAX_NEWS_COUNT) {
                                listMessageMap.subList(0, MAX_NEWS_COUNT - 1);
                            }
                        }
                    }
                }
            } catch (Exception e) { }

        } else if (id == REQUEST_FLAG_LOADMORE && isLoading == true) {
            isLoading = false;
            layoutLoadMore.setVisibility(View.GONE);

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) return;

            String msgData = mc.getMsgData();

            try {
                JSONObject objMsg = JSONObject.parseObject(msgData);

                if (objMsg != null && objMsg.containsKey("data") && objMsg.containsKey("result")) {
                    String data = objMsg.getString("data");
                    int retCode = objMsg.getIntValue("result");

                    if (retCode == 0) {
                        JSONArray objData = JSONArray.parseArray(data);

                        if (objData != null && objData.size() > 0) {

                            for (int i = 0; i < objData.size(); i++) {
                                if (listDatas != null && listDatas.size() >= MAX_NEWS_COUNT) {
                                    break;
                                }

                                JSONObject objItem = objData.getJSONObject(i);

                                if (objItem != null && objItem.containsKey("code") && objItem.containsKey("group") && objItem.containsKey("date") 
                                        && objItem.containsKey("title") && objItem.containsKey("from") && objItem.containsKey("summary") 
                                        && objItem.containsKey("content") && objItem.containsKey("guid")) {

                                    int goodsId = objItem.getIntValue("code");
                                    String newsType = objItem.getString("group");
                                    int date = objItem.getIntValue("date");
                                    String title = objItem.getString("title");
                                    String from = objItem.getString("from");
                                    String summary = objItem.getString("summary");
                                    String url = objItem.getString("content");
                                    String guid = objItem.getString("guid");

                                    // 如果url为空，跳过该项
                                    if (TextUtils.isEmpty(url)) continue;

                                    // 更新guid
                                    if (TextUtils.isEmpty(firstGuid)) {
                                        firstGuid = guid;
                                    }
                                    lastGuid = guid;

                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                                    map.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                                    map.put(InfoDetailPage.EXTRA_KEY_TIME, DateUtils.formatQuizCommitTime(date));
                                    map.put(InfoDetailPage.EXTRA_KEY_FROM, from);
                                    map.put(InfoDetailPage.EXTRA_KEY_SORTCLS, newsType);
                                    map.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                                    map.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                                    listMessageMap.add(map);

                                    CellBean bean = new CellBean(goodsId, newsType, date, title, from, summary, url, guid);
                                    listDatas.add(bean);

                                    if (!listGoodsId.contains(goodsId)) {
                                        listGoodsId.add(goodsId);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) { }

        }

        // 刷新界面显示
        updateViews();

        // 刷新股票涨跌幅
        requestZdf();
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);

        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage ddpkg = (DynaValueDataPackage) pkg;
            int id = ddpkg.getRequestType();

            if (id == REQUEST_FLAG_QUOTATION) {
                DynaValueData_Reply reply = ddpkg.getResponse();

                List<DynaQuota> listQuotaValues = reply.getQuotaValueList();    // 股票列表
                List<Integer> listReqFieldIds = reply.getRepFieldsList();       // 字段列表

                if (listQuotaValues != null && listQuotaValues.size() > 0) {

                    for (int i = 0; i < listQuotaValues.size(); i++) {
                        DynaQuota quotaValue = listQuotaValues.get(i);

                        List<String> listReqFieldValues = quotaValue.getRepFieldValueList();

                        int indexZDF = listReqFieldIds.indexOf(GoodsParams.ZDF);
                        String zdf = listReqFieldValues.get(indexZDF);

                        int goodsId = quotaValue.getGoodsId();

                        // 遍历列表，找到goodsId相同的股票，设置其涨跌幅
                        updateZdf(goodsId, zdf);
                    }

                    updateViews();

                }
            }
        }
    }

    /**
     * 遍历列表，找到goodsId相同的股票，设置其涨跌幅
     * */
    private void updateZdf(int goodsId, String zdf) {
        for (int i = 0; i < listDatas.size(); i++) {
            CellBean bean = listDatas.get(i);
            if (goodsId == bean.goodsId) {
                bean.stockZdf = zdf;
            }
        }
    }

    private Goods getGoodsById(int goodsId) {
        Goods goods = null;

        ArrayList<Goods> listGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
        if (listGoods != null && listGoods.size() > 0) {
            goods = listGoods.get(0);
        } else {
            goods = new Goods(goodsId, "");
        }

        return goods;
    }

    public void setOnPlayStatusChanged(OnPlayStatusChanged onPlayStatusChanged) {
        this.onPlayStatusChanged = onPlayStatusChanged;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public class NewsListAdapter extends BaseAdapter {

        private List<CellBean> listItems;
        private LayoutInflater inflater;

        public NewsListAdapter(Context context, List<CellBean> listItems) {
            inflater = LayoutInflater.from(context);
            this.listItems = listItems;
        }

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_optionalnews_listitem, parent, false);

                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final CellBean bean = listItems.get(position);
            vh.tvStockName.setText(bean.stockName);
            vh.tvStockCode.setText(bean.stockCode);
            vh.tvStockZdf.setText(DataUtils.getSignedZDF(bean.stockZdf));
            vh.tvNewsName.setText(bean.newsTitle);
            vh.tvNewsType.setText(bean.newsType);
            vh.tvNewsTime.setText(bean.newsTime);
            if (bean.isSpeaking) {
                vh.imgSpeakFlag.setVisibility(View.VISIBLE);
            } else {
                vh.imgSpeakFlag.setVisibility(View.INVISIBLE);
            }
            vh.layoutGoods.setBackgroundResource(getZDPRadiusBg(FontUtils.getColorByZDF(bean.stockZdf)));
            String md5Flag = MD5Util.md5(bean.url);
            if (listHasRead != null && listHasRead.contains(md5Flag)) {
                vh.tvNewsName.setTextColor(getResources().getColor(R.color.t3));
            } else {
                vh.tvNewsName.setTextColor(getResources().getColor(R.color.t1));
            }

            final int index = position;
            vh.layout.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    // 打开自选资讯详情
                    InfoDetailHome.gotoInfoDetail(OptionalNewsPage.this, listMessageMap, index, DataModule.G_KEY_INFO_OPTIONAL_NEWS);
                }
            });

            vh.layoutGoods.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    // 打开个股详情界面
                    QuoteJump.gotoQuote(OptionalNewsPage.this, getGoodsById(bean.goodsId));
                }
            });

            return convertView;
        }

        private class ViewHolder {

            public View layout, layoutGoods;

            public TextView tvStockName, tvStockCode, tvStockZdf, tvNewsName, tvNewsType, tvNewsTime;
            public ImageView imgSpeakFlag;

            public ViewHolder(View view) {
                layout = view;

                tvStockName = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_stock_name);
                tvStockCode = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_stock_code);
                tvStockZdf = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_stock_zdf);
                tvNewsName = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_news_title);
                tvNewsType = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_news_type);
                tvNewsTime = (TextView) view.findViewById(R.id.page_optionalnews_listitem_tv_news_time);
                imgSpeakFlag = (ImageView) view.findViewById(R.id.page_optionalnews_listitem_img_speak);
                layoutGoods = view.findViewById(R.id.layout_left_container);
            }

        }

    }

    public class CellBean {

        public int goodsId;
        public String newsType;
        public String newsTitle;
        public String newsFrom;
        public String newsSummary;
        public String url;
        public String guid;
        public String newsTime;
        public boolean isSpeaking;

        public String stockName;
        public String stockCode;
        public String stockZdf = "--%";

        public CellBean(int goodsId, String newsType, int date, String title, String from, String summary, String url, String guid) {
            this.goodsId = goodsId;
            this.newsType = newsType;
            this.newsTime = DateUtils.formatQuizCommitTime(date);
            this.newsTitle = title;
            this.newsFrom = from;
            this.newsSummary = summary;
            this.url = url;
            this.guid = guid;

            ArrayList<Goods> listGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
            if (listGoods != null && listGoods.size() > 0) {
                stockName = listGoods.get(0).getGoodsName();
                stockCode = listGoods.get(0).getGoodsCode();
            } else {
                Goods goods = new Goods(goodsId, "");
                stockName = goods.getGoodsName();
                stockCode = goods.getGoodsCode();
            }
        }

    }

    // 合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {

        @Override
        public void onSpeakResumed() {}

        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {}

        @Override
        public void onSpeakPaused() {}

        @Override
        public void onSpeakBegin() {}

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}

        @Override
        public void onCompleted(SpeechError arg0) {
            // 更新头条列表中播放状态标志
            if (listDatas.size() > 0) {
                listDatas.get(currentPlayPosition).isSpeaking = false;
                updateViews();

                if (currentPlayPosition >= listDatas.size() - 1) {
                    // 如果是最后一样播放完成，将播放状态置为初始化状态，重置播放位置为第0个
                    playStatus = InfoHome.PLAY_STATUS_INIT;
                    if (onPlayStatusChanged != null) {
                        onPlayStatusChanged.onPlayStatusChanged(playStatus);
                    }
                    currentPlayPosition = 0;

                    // 弹出对话框询问是否重新播放，如果点确定，继续播放
                    showReplayDialog();
                } else {
                    // 如果不是最后一条播放完成，然后继续播放下一条
                    currentPlayPosition++;
                    speakCurrentText();

                    // 如果下一条是最后一条，获取更多头条信息
                    if (currentPlayPosition == listDatas.size() - 1 && listDatas.size() < MAX_NEWS_COUNT
                            && !isRequesting && !isUpdating && !isLoading) {
                        requestMoreOptionalNews();
                    }
                }
            }
        }

        @Override
        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {}
    };

    /**
     * 播放完毕，是否重新播放
     * */
    private void showReplayDialog() {
        final CustomDialog dialog = new CustomDialog(getContext(), new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {
                speakCurrentText();
            }

            @Override
            public void onCancelBtnClicked() {}
        });

        dialog.setCustomMessage("播放完成，是否重播放？");
        dialog.setButtonText("重播", "取消");
        dialog.setMessageGravity(Gravity.CENTER);
        dialog.show();
    }

}
