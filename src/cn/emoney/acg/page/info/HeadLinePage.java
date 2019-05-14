package cn.emoney.acg.page.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
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
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.dialog.CustomDialog;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.util.VolleyHelper;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

public class HeadLinePage extends PageImpl {

    public static final String KEY_HEAD_LINE_READ_LIST = "key_head_line_has_read_list";

    private final short REQUEST_FLAG_REQUEST_NEWS = 1001;
    private final short REQUEST_FLAG_REFRESH_NEWS = 1002;
    private final short REQUEST_FLAG_LOAD_MORE_NEWS = 1003;

    private final int MAX_NEWS_COUNT = 50;

    /**
     * 是否正在加载数据
     * */
    private boolean isRequestingRequest;
    private boolean isRequestingPull;
    private boolean isRequestingLoadMore;

    private String lastRequestTime;
    private boolean isEnd;
    private int maxNewsId;
    private int minNewsId = -10001;

    private HeadLineAdapter adapter;
    private List<HeadLineItemBean> listItems = new ArrayList<HeadLineItemBean>();
    private ArrayList<String> listUrls = new ArrayList<String>();    // 打开头条列表时的url列表
    private ImageLoader imageLoader;
    /**
     * 存储已经阅读过的头条
     * */
    private List<String> listHasRead = new ArrayList<String>();

    private SpeechSynthesizer mTts; // 语音播放引擎
    private int playStatus = InfoHome.PLAY_STATUS_INIT; // 语音播放状态
    private int currentPlayPosition; // 当前播放位置
    private OnPlayStatusChanged onPlayStatusChanged;

    private View layoutLoadMore;
    private View layoutProgressBar;
    private TextView tvEmpty;
    private RefreshListView listView;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_headline);

        listView = (RefreshListView) findViewById(R.id.page_headline_list);
        layoutProgressBar = findViewById(R.id.page_headline_layout_loading);
        tvEmpty = (TextView) findViewById(R.id.page_headline_tv_empty);

        View listFooter = View.inflate(getContext(), R.layout.include_layout_listfooter_loadmore, null);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);

        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.addFooterView(listFooter);

        adapter = new HeadLineAdapter(getContext(), listItems);
        listView.setAdapter(adapter);

        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRequestingPull && !isRequestingLoadMore && !isRequestingRequest) {
                    requestUpdateHeadLineNews();
                }
                
                isRequestingPull = true;

                // 3秒钟后，不管有没有网络返回，直接隐藏header的显示
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRequestingPull) {
                            isRequestingPull = false;

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
            public void afterRefresh() {}
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
                        && !isRequestingLoadMore && isScrolling && isHasLoadMore == false && !isEnd;
                if (isLoadMore) {
                    requestMoreHeadLineNews();
                    isHasLoadMore = true;
                }
            }
        });

        tvEmpty.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (!isRequestingPull && !isRequestingRequest && !isRequestingLoadMore) {
                    requestHeadLineNews();
                }
            }
        });

        imageLoader = VolleyHelper.getInstance(getContext()).getImageLoader();
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (listItems.size() == 0 && !isRequestingRequest && !isRequestingPull && !isRequestingLoadMore) {
            requestHeadLineNews();
        } else if (!isRequestingRequest && !isRequestingPull && !isRequestingLoadMore) {
            requestUpdateHeadLineNews();
            
            isRequestingPull = true;
        }

        // 界面显示时，更新播放状态标志
        if (onPlayStatusChanged != null) {
            onPlayStatusChanged.onPlayStatusChanged(playStatus);
        }

        // 获取sqlite中缓存的已阅读过的头条列表
        String[] aryFlagMd5s = getDBHelper().getStringArray(KEY_HEAD_LINE_READ_LIST, new String[] {});
        List<String> t_Lst = Arrays.asList(aryFlagMd5s);
        listHasRead = new ArrayList<String>(t_Lst);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

        // 界面隐藏时，停止播放
        destroySpeechEngine();
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
            
            playStatus = InfoHome.PLAY_STATUS_INIT;
        }
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void resetPlayStatus() {
        
        initSpeechEngine();
        
        if (playStatus == InfoHome.PLAY_STATUS_INIT) {
            // 1. 初始状态时点击，开始播放
            playStatus = InfoHome.PLAY_STATUS_PLAY;
            speakCurrentText();
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

    public void setOnPlayStatusChanged(OnPlayStatusChanged onPlayStatusChanged) {
        this.onPlayStatusChanged = onPlayStatusChanged;
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
        if (listItems.size() > 0) {
            listItems.get(currentPlayPosition).itemIsSpeaking = true;
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取当前播放位置的文字
     * */
    private String getSpeakText() {
        String text = "";

        if (currentPlayPosition < listItems.size()) {
            // 如果有摘要，播放摘要，如果没有摘要，播放标题

            String time = listItems.get(currentPlayPosition).itemTime;
            String title = listItems.get(currentPlayPosition).title;
            String summary = listItems.get(currentPlayPosition).summary;
            text = time + "资讯，" + title + "，" + summary + "。";
        }

        return text;
    }

    /**
     * 从指定网址加载显示图片
     * */
    private void loadImageFromUrl(NetworkImageView iv, String url) {
        if (iv != null) {
            iv.setDefaultImageResId(R.drawable.img_event_lstdefault);
            iv.setErrorImageResId(R.drawable.img_event_lstdefault);
            iv.setImageUrl(url, imageLoader);
        }
    }

    /**
     * 从后台获取头条新闻
     * */
    private void requestHeadLineNews() {
        JSONObject jsObj = null;

        try {
            jsObj = new JSONObject();

            jsObj.put("from", "+inf");
            jsObj.put("to", "-inf");
            jsObj.put("size", 20);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_INFO_HEADLINES, REQUEST_FLAG_REQUEST_NEWS);

        /*
         * 调用该接口获取数据时，头条列表时没有数据，获取数据过程中，显示加载数据进度条，不允许下拉刷新
         * 当超时未获取到数据时，隐藏加载数据进度条，显示空白提示，点击空白提示可以再次调用该接口获取数据，允许下拉刷新
         * 当返回数据并成功时，隐藏加载进度条，隐藏空白提示，允许下拉刷新
         * 当返回数据但未成功时，隐藏加载进度条，显示空白提示，点击空白提示可以再次调用该接口获取数据，允许下拉刷新
         * */
        isRequestingRequest = true;
        // 不允许下拉刷新
        listView.setRefreshable(false);
        layoutProgressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequestingRequest) {
                    isRequestingRequest = false;

                    layoutProgressBar.setVisibility(View.GONE);

                    tvEmpty.setText("加载失败，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);

                    listView.setRefreshable(false);
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }

    /**
     * 更新最新数据
     * */
    private void requestUpdateHeadLineNews() {
        JSONObject jsObj = null;

        try {
            jsObj = new JSONObject();

            jsObj.put("max_id", maxNewsId);
            jsObj.put("min_id", minNewsId);
            jsObj.put("time", lastRequestTime);
            jsObj.put("from", "+inf");
            jsObj.put("to", "(" + maxNewsId);
            jsObj.put("size", 20); // 最多加载20条新数据
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_INFO_HEADLINES, REQUEST_FLAG_REFRESH_NEWS);
    }

    /**
     * 获取更多数据
     * */
    private void requestMoreHeadLineNews() {
        JSONObject jsObj = null;

        try {
            jsObj = new JSONObject();

            jsObj.put("max_id", maxNewsId);
            jsObj.put("min_id", minNewsId);
            jsObj.put("time", lastRequestTime);
            jsObj.put("from", "(" + minNewsId);
            jsObj.put("to", "-inf");
            jsObj.put("size", 20); // 最多加载20条新数据
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_INFO_HEADLINES, REQUEST_FLAG_LOAD_MORE_NEWS);

        // 显示加载更多的footer
        isRequestingLoadMore = true;
        layoutLoadMore.setVisibility(View.VISIBLE);

        // 3秒钟后，如果还未返回，隐藏
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequestingLoadMore) {
                    // 显示加载更多的footer
                    isRequestingLoadMore = false;
                    layoutLoadMore.setVisibility(View.GONE);
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        super.updateFromInfo(pkg);
        int id = pkg.getRequestType();

        if (id == REQUEST_FLAG_REQUEST_NEWS && isRequestingRequest) {
            isRequestingRequest = false;
            layoutProgressBar.setVisibility(View.GONE);

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject obj = JSONObject.parseObject(msgData);

                if (obj == null) {
                    return;
                }

                int resultCode = obj.getIntValue(KEY_RESULT);
                if (resultCode == 0) {

                    lastRequestTime = obj.getString("time");
                    isEnd = obj.getBooleanValue("end");

                    JSONArray arrayAdd = obj.getJSONArray("add");
                    if (arrayAdd != null && arrayAdd.size() > 0) {
                        listItems.clear();
                        listUrls.clear();
                        for (int i = 0; i < arrayAdd.size(); i++) {
                            JSONObject objTip = arrayAdd.getJSONObject(i);
                            if (objTip == null)
                                return;

                            String author = objTip.getString("author");
                            String bks = objTip.getString("bks");
                            int commentNum = objTip.getIntValue("comment_num");
                            String url = objTip.getString("content");
                            int effectLevel = objTip.getIntValue("effect_level");
                            String from = objTip.getString("from");
                            String group = objTip.getString("group");
                            String imageUrl = "";
                            JSONArray jary = objTip.getJSONArray("images");
                            if (jary != null && jary.size() > 0) {
                                imageUrl = jary.getString(0);
                            }
                            int newsId = objTip.getIntValue("news_id");
                            String pushTime = objTip.getString("push_time");
                            int shareNum = objTip.getIntValue("share_num");
                            String relativeStockIds = objTip.getString("stocks");
                            String summary = objTip.getString("summary");
                            String title = objTip.getString("title");
                            int newsType = objTip.getIntValue("type");
                            String updateTime = objTip.getString("update_time");

                            refreshMaxMinNewsId(newsId);

                            HeadLineItemBean item = new HeadLineItemBean(newsId, newsType, commentNum, shareNum, effectLevel, pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl);
                            listItems.add(item);
                            listUrls.add(url);
                        }
                    }

                    //                    JSONArray arrayUpdate = obj.getJSONArray("update");

                    //                    JSONArray arrayDelete = obj.getJSONArray("delete");
                }

                adapter.notifyDataSetChanged();

                if (listItems.size() > 0) {
                    tvEmpty.setVisibility(View.GONE);
                    
                    // 有数据显示时，可以下拉刷新
                    listView.setRefreshable(true);
                } else {
                    tvEmpty.setText("暂无数据，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);
                    
                    // 无数据显示时，不可以下拉刷新
                    listView.setRefreshable(false);
                }
            } catch (Exception e) {
                tvEmpty.setText("加载失败，请点击重试");
                tvEmpty.setVisibility(View.VISIBLE);
                
                // 无数据显示时，不可以下拉刷新
                listView.setRefreshable(false);
            }
        } else if (id == REQUEST_FLAG_REFRESH_NEWS && isRequestingPull) {
            isRequestingPull = false;

            if (listView != null) {
                listView.onRefreshFinished();                
            }

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

                    lastRequestTime = obj.getString("time");
                    isEnd = obj.getBooleanValue("end");

                    JSONArray arrayAdd = obj.getJSONArray("add");
                    if (arrayAdd != null && arrayAdd.size() > 0) {
                        for (int i = 0; i < arrayAdd.size(); i++) {
                            JSONObject objTip = arrayAdd.getJSONObject(i);
                            if (objTip == null)
                                return;

                            String author = objTip.getString("author");
                            String bks = objTip.getString("bks");
                            int commentNum = objTip.getIntValue("comment_num");
                            String url = objTip.getString("content");
                            int effectLevel = objTip.getIntValue("effect_level");
                            String from = objTip.getString("from");
                            String group = objTip.getString("group");
                            String imageUrl = "";
                            JSONArray jary = objTip.getJSONArray("images");
                            if (jary != null && jary.size() > 0) {
                                imageUrl = jary.getString(0);
                            }
                            int newsId = objTip.getIntValue("news_id");
                            String pushTime = objTip.getString("push_time");
                            int shareNum = objTip.getIntValue("share_num");
                            String relativeStockIds = objTip.getString("stocks");
                            String summary = objTip.getString("summary");
                            String title = objTip.getString("title");
                            int newsType = objTip.getIntValue("type");
                            String updateTime = objTip.getString("update_time");

                            refreshMaxMinNewsId(newsId);

                            HeadLineItemBean item = new HeadLineItemBean(newsId, newsType, commentNum, shareNum, effectLevel, pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl);
                            listItems.add(item);
                            listUrls.add(url);
                        }
                    }

                    JSONArray arrayUpdate = obj.getJSONArray("update");
                    if (arrayUpdate != null && arrayUpdate.size() > 0) {
                        for (int i = 0; i < arrayUpdate.size(); i++) {
                            JSONObject objTip = arrayUpdate.getJSONObject(i);
                            if (objTip == null)
                                return;

                            String author = objTip.getString("author");
                            String bks = objTip.getString("bks");
                            int commentNum = objTip.getIntValue("comment_num");
                            String url = objTip.getString("content");
                            int effectLevel = objTip.getIntValue("effect_level");
                            String from = objTip.getString("from");
                            String group = objTip.getString("group");
                            String imageUrl = "";
                            JSONArray jary = objTip.getJSONArray("images");
                            if (jary != null && jary.size() > 0) {
                                imageUrl = jary.getString(0);
                            }
                            int newsId = objTip.getIntValue("news_id");
                            String pushTime = objTip.getString("push_time");
                            int shareNum = objTip.getIntValue("share_num");
                            String relativeStockIds = objTip.getString("stocks");
                            String summary = objTip.getString("summary");
                            String title = objTip.getString("title");
                            int newsType = objTip.getIntValue("type");
                            String updateTime = objTip.getString("update_time");

                            refreshMaxMinNewsId(newsId);

                            HeadLineItemBean item = getNewsItem(newsId);
                            if (item != null) {
                                item.author = author;
                                item.bks = bks;
                                item.commentNum = commentNum;
                                item.url = url;
                                item.effectLevel = effectLevel;
                                item.from = from;
                                item.group = group;
                                item.imageUrl = imageUrl;
                                item.newsId = newsId;
                                item.pushTime = pushTime;
                                item.shareNum = shareNum;
                                item.relativeStockIds = relativeStockIds;
                                item.summary = summary;
                                item.title = title;
                                item.newsType = newsType;
                                item.updateTime = updateTime;
                            } else {
                                item = new HeadLineItemBean(newsId, newsType, commentNum, shareNum, effectLevel, pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl);
                                listItems.add(item);
                                listUrls.add(url);
                            }
                        }
                    }

                    JSONArray arrayDelete = obj.getJSONArray("delete");
                    if (arrayDelete != null && arrayDelete.size() > 0) {
                        for (int i = 0; i < arrayDelete.size(); i++) {
                            int newsId = arrayDelete.getIntValue(i);
                            deleteNewsItem(newsId);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            } catch (Exception e) {
            }
        } else if (id == REQUEST_FLAG_LOAD_MORE_NEWS && isRequestingLoadMore) {
            // 隐藏加载更多的footer
            isRequestingLoadMore = false;
            layoutLoadMore.setVisibility(View.GONE);

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

                    lastRequestTime = obj.getString("time");
                    isEnd = obj.getBooleanValue("end");

                    JSONArray arrayAdd = obj.getJSONArray("add");
                    if (arrayAdd != null && arrayAdd.size() > 0) {
                        for (int i = 0; i < arrayAdd.size(); i++) {
                            JSONObject objTip = arrayAdd.getJSONObject(i);
                            if (objTip == null)
                                return;

                            String author = objTip.getString("author");
                            String bks = objTip.getString("bks");
                            int commentNum = objTip.getIntValue("comment_num");
                            String url = objTip.getString("content");
                            int effectLevel = objTip.getIntValue("effect_level");
                            String from = objTip.getString("from");
                            String group = objTip.getString("group");
                            String imageUrl = "";
                            JSONArray jary = objTip.getJSONArray("images");
                            if (jary != null && jary.size() > 0) {
                                imageUrl = jary.getString(0);
                            }
                            int newsId = objTip.getIntValue("news_id");
                            String pushTime = objTip.getString("push_time");
                            int shareNum = objTip.getIntValue("share_num");
                            String relativeStockIds = objTip.getString("stocks");
                            String summary = objTip.getString("summary");
                            String title = objTip.getString("title");
                            int newsType = objTip.getIntValue("type");
                            String updateTime = objTip.getString("update_time");

                            refreshMaxMinNewsId(newsId);

                            HeadLineItemBean item = new HeadLineItemBean(newsId, newsType, commentNum, shareNum, effectLevel, pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl);
                            listItems.add(item);
                            listUrls.add(url);
                        }
                    }

                    JSONArray arrayUpdate = obj.getJSONArray("update");
                    if (arrayUpdate != null && arrayUpdate.size() > 0) {
                        for (int i = 0; i < arrayUpdate.size(); i++) {
                            JSONObject objTip = arrayUpdate.getJSONObject(i);
                            if (objTip == null)
                                return;

                            String author = objTip.getString("author");
                            String bks = objTip.getString("bks");
                            int commentNum = objTip.getIntValue("comment_num");
                            String url = objTip.getString("content");
                            int effectLevel = objTip.getIntValue("effect_level");
                            String from = objTip.getString("from");
                            String group = objTip.getString("group");
                            String imageUrl = "";
                            JSONArray jary = objTip.getJSONArray("images");
                            if (jary != null && jary.size() > 0) {
                                imageUrl = jary.getString(0);
                            }
                            int newsId = objTip.getIntValue("news_id");
                            String pushTime = objTip.getString("push_time");
                            int shareNum = objTip.getIntValue("share_num");
                            String relativeStockIds = objTip.getString("stocks");
                            String summary = objTip.getString("summary");
                            String title = objTip.getString("title");
                            int newsType = objTip.getIntValue("type");
                            String updateTime = objTip.getString("update_time");

                            refreshMaxMinNewsId(newsId);

                            HeadLineItemBean item = getNewsItem(newsId);
                            if (item != null) {
                                item.author = author;
                                item.bks = bks;
                                item.commentNum = commentNum;
                                item.url = url;
                                item.effectLevel = effectLevel;
                                item.from = from;
                                item.group = group;
                                item.imageUrl = imageUrl;
                                item.newsId = newsId;
                                item.pushTime = pushTime;
                                item.shareNum = shareNum;
                                item.relativeStockIds = relativeStockIds;
                                item.summary = summary;
                                item.title = title;
                                item.newsType = newsType;
                                item.updateTime = updateTime;
                            } else {
                                item = new HeadLineItemBean(newsId, newsType, commentNum, shareNum, effectLevel, pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl);
                                listItems.add(item);
                                listUrls.add(url);
                            }
                        }
                    }

                    JSONArray arrayDelete = obj.getJSONArray("delete");
                    if (arrayDelete != null && arrayDelete.size() > 0) {
                        for (int i = 0; i < arrayDelete.size(); i++) {
                            int newsId = arrayDelete.getIntValue(i);
                            deleteNewsItem(newsId);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 更新最大消息id
     * */
    private void refreshMaxMinNewsId(int newsId) {
        if (newsId > maxNewsId) {
            maxNewsId = newsId;
        }

        if (minNewsId == -10001) {
            minNewsId = newsId;
        } else {
            if (newsId < minNewsId) {
                minNewsId = newsId;
            }
        }
    }

    /**
     * 删除指定newsId的新闻
     * */
    private void deleteNewsItem(int newsId) {
        if (listItems != null && listItems.size() > 0) {
            for (int i = 0; i < listItems.size(); i++) {
                HeadLineItemBean item = listItems.get(i);
                if (newsId == item.newsId) {
                    listItems.remove(item);
                    listUrls.remove(i);
                }
            }
        }
    }

    /**
     * 获取列表中指定id的新闻
     * */
    private HeadLineItemBean getNewsItem(int newsId) {
        if (listItems != null && listItems.size() > 0) {
            for (int i = 0; i < listItems.size(); i++) {
                HeadLineItemBean item = listItems.get(i);
                if (newsId == item.newsId) {
                    return item;
                }
            }
        }

        return null;
    }

    private void gotoHeadLineDetail(ArrayList<String> urls, int index) {
        PageIntent intent = new PageIntent(this, HeadLineDetailHome.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(HeadLineDetailHome.EXTRA_KEY_URLS, urls);
        bundle.putInt(HeadLineDetailHome.EXTRA_KEY_INDEX, index);
        intent.setArguments(bundle);

        intent.setSupportAnimation(false);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    private class HeadLineAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<HeadLineItemBean> listItems;

        public HeadLineAdapter(Context context, List<HeadLineItemBean> listItems) {
            inflater = LayoutInflater.from(context);
            this.listItems = listItems;
        }

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int pos) {
            return listItems.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup root) {
            ViewHolder vh = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_headline_listitem, root, false);

                vh = new ViewHolder(convertView);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final HeadLineItemBean item = listItems.get(pos);

            vh.tvTitle.setText(item.itemTitle);
            vh.tvTime.setText(item.itemTime);
            if (item.itemIsGoodNews > 0) {
                vh.imgTrend.setImageResource(R.drawable.img_news_trend_goodnews);
            } else if (item.itemIsGoodNews < 0) {
                vh.imgTrend.setImageResource(R.drawable.img_news_trend_badnews);
            } else if (item.itemIsGoodNews == 0) {
                vh.imgTrend.setImageBitmap(null);
            }
            if (item.itemIsSpeaking) {
                vh.imgSpeakFlag.setVisibility(View.VISIBLE);
            } else {
                vh.imgSpeakFlag.setVisibility(View.INVISIBLE);
            }
            vh.tvGroup.setText(item.itemGroup);
            if (TextUtils.isEmpty(item.itemGroup)) {
                vh.tvGroup.setVisibility(View.GONE);
            } else {
                vh.tvGroup.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(item.itemImageUrl)) {
                vh.imgIcon.setVisibility(View.GONE);
            } else {
                loadImageFromUrl(vh.imgIcon, item.itemImageUrl);
            }
            // 如果已经读过，显示为灰色
            String md5Flag = MD5Util.md5(item.itemUrl);
            if (listHasRead != null && listHasRead.contains(md5Flag)) {
                vh.tvTitle.setTextColor(getResources().getColor(R.color.t3));
            } else {
                vh.tvTitle.setTextColor(getResources().getColor(R.color.t1));
            }

            final int currentPosition = pos;
            vh.layout.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    gotoHeadLineDetail(listUrls, currentPosition);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;

            public TextView tvTitle, tvTime, tvGroup;
            private ImageView imgTrend, imgSpeakFlag;
            private NetworkImageView imgIcon;

            public ViewHolder(View view) {
                layout = view;

                tvTitle = (TextView) layout.findViewById(R.id.page_headline_listitem_tv_title);
                tvTime = (TextView) layout.findViewById(R.id.page_headline_listitem_tv_time);
                tvGroup = (TextView) layout.findViewById(R.id.page_headline_listitem_tv_group);
                imgTrend = (ImageView) layout.findViewById(R.id.page_headline_listitem_img_tread);
                imgSpeakFlag = (ImageView) layout.findViewById(R.id.page_headline_listitem_img_playstatus);
                imgIcon = (NetworkImageView) layout.findViewById(R.id.page_headline_listitem_img_icon);
            }
        }

    }

    public class HeadLineItemBean {
        public int itemIsGoodNews;
        public boolean itemIsSpeaking;
        public String itemTitle;
        public String itemsummary;
        public String itemTime;
        public String itemUrl;
        public String itemImageUrl;
        public String itemGroup;

        int newsId, newsType, commentNum, shareNum, effectLevel;
        String pushTime, updateTime, group, title, summary, author, from, bks, relativeStockIds, url, imageUrl;

        public HeadLineItemBean(int newsId, int newsType, int commentNum, int shareNum, int effectLevel, String pushTime, String updateTime, String group, String title, String summary, String author, String from, String bks, String relativeStockIds, String url, String imageUrl) {
            super();
            this.newsId = newsId;
            this.newsType = newsType;
            this.commentNum = commentNum;
            this.shareNum = shareNum;
            this.effectLevel = effectLevel;
            this.pushTime = pushTime;
            this.updateTime = updateTime;
            this.group = group;
            this.title = title;
            this.summary = summary;
            this.author = author;
            this.from = from;
            this.bks = bks;
            this.relativeStockIds = relativeStockIds;
            this.url = url;
            this.imageUrl = imageUrl;

            this.itemTitle = title;
            this.itemsummary = summary;
            this.itemTime = DateUtils.formatQuizCommitTime(updateTime);
            this.itemUrl = url;
            this.itemImageUrl = imageUrl;
            this.itemIsGoodNews = effectLevel;
            this.itemGroup = group;
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
            if (listItems.size() > 0) {
                listItems.get(currentPlayPosition).itemIsSpeaking = false;
                adapter.notifyDataSetChanged();

                if (currentPlayPosition >= listItems.size() - 1) {
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
                    if (currentPlayPosition == listItems.size() - 1 && !isRequestingLoadMore && !isEnd
                            && !isRequestingRequest && !isRequestingPull && listItems.size() < MAX_NEWS_COUNT) {
                        requestMoreHeadLineNews();
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
