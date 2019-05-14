package cn.emoney.acg.page.info;

import java.util.ArrayList;
import java.util.List;

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
import android.view.View.OnClickListener;
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
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

public class LiveBroadcastPage extends PageImpl {
    
    private static final int MAX_NEWS_COUNT = 50;
    
    private static final short REQUEST_FLAG_REQUEST_NEWS = 1001;
    private static final short REQUEST_FLAG_REFRESH_NEWS = 1002;
    private static final short REQUEST_FLAG_LOAD_MORE_NEWS = 1003;
    
    /**
     * 是否处于下拉刷新过程中
     * */
    private boolean isRequestingPull;
    private boolean isRequestingLoadMore;
    private boolean isRequestingRequest;
    
    private boolean isEnd;
    private String lastRequestTime;
    private int maxNewsId;
    private int minNewsId = -10001;
    
    private SpeechSynthesizer mTts; // 语音播放引擎
    private int playStatus = InfoHome.PLAY_STATUS_INIT; // 语音播放状态
    private int currentPlayPosition; // 当前播放位置
    private OnPlayStatusChanged onPlayStatusChanged;
    
    private LiveBroadcastAdapter adapter;
    private List<LiveItemBean> listItems = new ArrayList<LiveItemBean>();
    
    private RefreshListView listView;
    private View layoutLoadMore;
    private View layoutProgressBar;
    private TextView tvEmpty;
    
    @Override
    protected void initPage() {
        setContentView(R.layout.page_live);
        
        listView = (RefreshListView) findViewById(R.id.page_live_list);
        layoutProgressBar = findViewById(R.id.page_live_layout_loading);
        tvEmpty = (TextView) findViewById(R.id.page_live_tv_empty);
        
        View listFooter = View.inflate(getContext(), R.layout.include_layout_listfooter_loadmore, null);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);        

        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.addFooterView(listFooter);

        adapter = new LiveBroadcastAdapter(getContext(), listItems);
        listView.setAdapter(adapter);

        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRequestingPull && !isRequestingLoadMore && !isRequestingRequest) {
                    requestUpdateLives();
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
                        && !isRequestingLoadMore && isScrolling && !isHasLoadMore && !isEnd;
                if (isLoadMore) {
                    requestMoreLives();
                    isHasLoadMore = true;
                }
            }
        });

        tvEmpty.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (!isRequestingPull && !isRequestingRequest && !isRequestingLoadMore) {
                    requestLiveBroadcasts();
                }
            }
        });

    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (listItems.size() == 0 && !isRequestingRequest && !isRequestingPull && !isRequestingLoadMore) {
            requestLiveBroadcasts();
        } else if (!isRequestingRequest && !isRequestingPull && !isRequestingLoadMore) {
            requestUpdateLives();
            
            isRequestingPull = true;
        }

        // 界面显示时，更新播放状态标志
        if (onPlayStatusChanged != null) {
            onPlayStatusChanged.onPlayStatusChanged(playStatus);
        }

    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

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
        }
        
        playStatus = InfoHome.PLAY_STATUS_INIT;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void resetPlayStatus() {
        
        initSpeechEngine();
        
        if (playStatus == InfoHome.PLAY_STATUS_INIT) {
            // 有数据时才播放
            if (listItems.size() > 0) {
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
            listItems.get(currentPlayPosition).itemIsShowAll = true;
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
            String time = DateUtils.formatInfoDate(listItems.get(currentPlayPosition).itemTime, DateUtils.mFormatHHmmWithUnit);
            String summary = listItems.get(currentPlayPosition).itemSummary;
            text = time + "。" + summary;

            if (TextUtils.isEmpty(summary)) {
                text = listItems.get(currentPlayPosition).title;
            }
        }

        return text;
    }

    /**
     * 从后台获取直播
     * */
    private void requestLiveBroadcasts() {
        JSONObject jsObj = null;

        try {
            jsObj = new JSONObject();

            jsObj.put("from", "+inf");
            jsObj.put("to", "-inf");
            jsObj.put("size", 20);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_INFO_LIVES, REQUEST_FLAG_REQUEST_NEWS);

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
    private void requestUpdateLives() {
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

        requestInfo(jsObj, IDUtils.ID_INFO_LIVES, REQUEST_FLAG_REFRESH_NEWS);
    }

    /**
     * 获取更多数据
     * */
    private void requestMoreLives() {
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

        requestInfo(jsObj, IDUtils.ID_INFO_LIVES, REQUEST_FLAG_LOAD_MORE_NEWS);

        // 显示加载更多的footer
        isRequestingLoadMore = true;
        layoutLoadMore.setVisibility(View.VISIBLE);

        // 如果超过限制时间还未返回数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequestingLoadMore) {
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

                if (obj == null)
                    return;

                int resultCode = obj.getIntValue(KEY_RESULT);
                if (resultCode == 0) {

                    lastRequestTime = obj.getString("time");
                    isEnd = obj.getBooleanValue("end");

                    JSONArray arrayAdd = obj.getJSONArray("add");
                    if (arrayAdd != null && arrayAdd.size() > 0) {
                        listItems.clear();
                        for (int i = 0; i < arrayAdd.size(); i++) {
                            JSONObject objItem = arrayAdd.getJSONObject(i);
                            if (objItem == null)
                                return;

                            int newsId = objItem.getIntValue("news_id");
                            int showType = objItem.getIntValue("show_type");
                            int commentNum = objItem.getIntValue("comment_num");
                            int shareNum = objItem.getIntValue("share_num");
                            int effectLevel = objItem.getIntValue("effect_level");
                            String pushTime = objItem.getString("push_time");
                            String updateTime = objItem.getString("update_time");
                            String title = objItem.getString("title");
                            String summary = objItem.getString("summary");
                            String bks = objItem.getString("bks");
                            String stockIds = objItem.getString("stocks");
                            String url = objItem.getString("content");

                            refreshMaxMinNewsId(newsId);

                            LiveItemBean item = new LiveItemBean(newsId, showType, commentNum, shareNum, effectLevel, pushTime, updateTime, title, summary, bks, stockIds, url);

                            listItems.add(item);
                        }
                    }
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
                            JSONObject objItem = arrayAdd.getJSONObject(i);
                            if (objItem == null)
                                return;

                            int newsId = objItem.getIntValue("news_id");
                            int showType = objItem.getIntValue("show_type");
                            int commentNum = objItem.getIntValue("comment_num");
                            int shareNum = objItem.getIntValue("share_num");
                            int effectLevel = objItem.getIntValue("effect_level");
                            String pushTime = objItem.getString("push_time");
                            String updateTime = objItem.getString("update_time");
                            String title = objItem.getString("title");
                            String summary = objItem.getString("summary");
                            String bks = objItem.getString("bks");
                            String stockIds = objItem.getString("stocks");
                            String url = objItem.getString("content");

                            refreshMaxMinNewsId(newsId);

                            LiveItemBean item = new LiveItemBean(newsId, showType, commentNum, shareNum, effectLevel, pushTime, updateTime, title, summary, bks, stockIds, url);

                            listItems.add(item);
                        }
                    }

                    JSONArray arrayUpdate = obj.getJSONArray("update");
                    if (arrayUpdate != null && arrayUpdate.size() > 0) {
                        for (int i = 0; i < arrayUpdate.size(); i++) {
                            JSONObject objItem = arrayUpdate.getJSONObject(i);
                            if (objItem == null)
                                return;

                            int newsId = objItem.getIntValue("news_id");
                            int showType = objItem.getIntValue("show_type");
                            int commentNum = objItem.getIntValue("comment_num");
                            int shareNum = objItem.getIntValue("share_num");
                            int effectLevel = objItem.getIntValue("effect_level");
                            String pushTime = objItem.getString("push_time");
                            String updateTime = objItem.getString("update_time");
                            String title = objItem.getString("title");
                            String summary = objItem.getString("summary");
                            String bks = objItem.getString("bks");
                            String stockIds = objItem.getString("stocks");
                            String url = objItem.getString("content");

                            refreshMaxMinNewsId(newsId);

                            LiveItemBean item = getNewsItem(newsId);
                            if (item != null) {
                                item.newsId = newsId;
                                item.showType = showType;
                                item.commentNum = commentNum;
                                item.shareNum = shareNum;
                                item.effectLevel = effectLevel;
                                item.pushTime = pushTime;
                                item.updateTime = updateTime;
                                item.title = title;
                                item.summary = summary;
                                item.bks = bks;
                                item.stockIds = stockIds;
                                item.url = url;
                            } else {
                                item = new LiveItemBean(newsId, showType, commentNum, shareNum, effectLevel, pushTime, updateTime, title, summary, bks, stockIds, url);
                                listItems.add(item);
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
                            if (listItems != null && listItems.size() >= MAX_NEWS_COUNT) {
                                break;
                            }

                            JSONObject objItem = arrayAdd.getJSONObject(i);
                            if (objItem == null)
                                return;

                            int newsId = objItem.getIntValue("news_id");
                            int showType = objItem.getIntValue("show_type");
                            int commentNum = objItem.getIntValue("comment_num");
                            int shareNum = objItem.getIntValue("share_num");
                            int effectLevel = objItem.getIntValue("effect_level");
                            String pushTime = objItem.getString("push_time");
                            String updateTime = objItem.getString("update_time");
                            String title = objItem.getString("title");
                            String summary = objItem.getString("summary");
                            String bks = objItem.getString("bks");
                            String stockIds = objItem.getString("stocks");
                            String url = objItem.getString("content");

                            refreshMaxMinNewsId(newsId);

                            LiveItemBean item = new LiveItemBean(newsId, showType, commentNum, shareNum, effectLevel, pushTime, updateTime, title, summary, bks, stockIds, url);
                            listItems.add(item);
                        }
                    }

                    JSONArray arrayUpdate = obj.getJSONArray("update");
                    if (arrayUpdate != null && arrayUpdate.size() > 0) {
                        for (int i = 0; i < arrayUpdate.size(); i++) {
                            if (listItems != null && listItems.size() >= MAX_NEWS_COUNT) {
                                break;
                            }

                            JSONObject objItem = arrayUpdate.getJSONObject(i);
                            if (objItem == null)
                                return;

                            int newsId = objItem.getIntValue("news_id");
                            int showType = objItem.getIntValue("show_type");
                            int commentNum = objItem.getIntValue("comment_num");
                            int shareNum = objItem.getIntValue("share_num");
                            int effectLevel = objItem.getIntValue("effect_level");
                            String pushTime = objItem.getString("push_time");
                            String updateTime = objItem.getString("update_time");
                            String title = objItem.getString("title");
                            String summary = objItem.getString("summary");
                            String bks = objItem.getString("bks");
                            String stockIds = objItem.getString("stocks");
                            String url = objItem.getString("content");

                            refreshMaxMinNewsId(newsId);

                            LiveItemBean item = getNewsItem(newsId);
                            if (item != null) {
                                item.newsId = newsId;
                                item.showType = showType;
                                item.commentNum = commentNum;
                                item.shareNum = shareNum;
                                item.effectLevel = effectLevel;
                                item.pushTime = pushTime;
                                item.updateTime = updateTime;
                                item.title = title;
                                item.summary = summary;
                                item.bks = bks;
                                item.stockIds = stockIds;
                                item.url = url;
                            } else {
                                item = new LiveItemBean(newsId, showType, commentNum, shareNum, effectLevel, pushTime, updateTime, title, summary, bks, stockIds, url);
                                listItems.add(item);
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
                LiveItemBean item = listItems.get(i);
                if (newsId == item.newsId) {
                    listItems.remove(item);
                }
            }
        }
    }

    /**
     * 获取列表中指定id的新闻
     * */
    private LiveItemBean getNewsItem(int newsId) {
        if (listItems != null && listItems.size() > 0) {
            for (int i = 0; i < listItems.size(); i++) {
                LiveItemBean item = listItems.get(i);

                if (item.newsId == newsId) {
                    return item;                    
                }

            }
        }

        return null;
    }

    private class LiveBroadcastAdapter extends BaseAdapter {

        private List<LiveItemBean> listItems;
        private LayoutInflater inflater;

        public LiveBroadcastAdapter(Context context, List<LiveItemBean> listItems) {
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
                convertView = inflater.inflate(R.layout.page_live_listitem, root, false);

                vh = new ViewHolder(convertView);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final LiveItemBean item = listItems.get(pos);

            vh.tvTime.setText(DateUtils.formatInfoDate(item.itemTime, DateUtils.mFormatHM));
            vh.tvTitle.setText(item.itemTitle);
            vh.tvSummary.setText(item.itemSummary);
            if (item.itemIsShowAll) {
                vh.tvSummary.setMaxLines(300);
            } else {
                vh.tvSummary.setMaxLines(3);
            }
            if (item.itemIsSpeaking) {
                vh.imgSpeakFlag.setVisibility(View.VISIBLE);
            } else {
                vh.imgSpeakFlag.setVisibility(View.INVISIBLE);
            }

            final TextView tvContent = vh.tvSummary;
            vh.layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (item.itemIsShowAll) {
                        item.itemIsShowAll = false;
                        tvContent.setMaxLines(3);
                    } else {
                        item.itemIsShowAll = true;
                        tvContent.setMaxLines(300);
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;

            public TextView tvTitle, tvTime, tvSummary;
            private ImageView imgSpeakFlag;

            public ViewHolder(View view) {
                layout = view;

                tvTitle = (TextView) layout.findViewById(R.id.page_live_listitem_tv_title);
                tvTime = (TextView) layout.findViewById(R.id.page_live_listitem_tv_time);
                tvSummary = (TextView) layout.findViewById(R.id.page_live_listitem_tv_content);
                imgSpeakFlag = (ImageView) layout.findViewById(R.id.page_live_listitem_img_playstatus);
            }
        }

    }

    public class LiveItemBean {
        public String itemTime;
        public String itemTitle;
        public String itemSummary;
        public boolean itemIsSpeaking;
        public boolean itemIsShowAll; // 摘要是否全部显示

        public int newsId, showType, commentNum, shareNum, effectLevel;
        public String pushTime, updateTime, title, summary, bks, stockIds, url;

        public LiveItemBean(String itemTitle, String itemSummary, String itemTime) {
            super();
            this.itemTitle = itemTitle;
            this.itemSummary = itemSummary;
            this.itemTime = itemTime;

            // 去除summary中的回车
            this.itemSummary = this.itemSummary.replaceAll("\n", "");
        }

        public LiveItemBean(int newsId, int showType, int commentNum, int shareNum, int effectLevel, String pushTime, String updateTime, String title, String summary, String bks, String stockIds, String url) {
            super();
            this.newsId = newsId;
            this.showType = showType;
            this.commentNum = commentNum;
            this.shareNum = shareNum;
            this.effectLevel = effectLevel;
            this.pushTime = pushTime;
            this.updateTime = updateTime;
            this.title = title;
            this.summary = summary;
            this.bks = bks;
            this.stockIds = stockIds;
            this.url = url;

            this.itemTitle = title;
            this.itemSummary = summary;
            this.itemTime = updateTime;

            // 去除summary中的回车
            this.itemSummary = this.itemSummary.replaceAll("\n", "");
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
            // 更新列表中播放状态标志
            if (listItems.size() > 0) {
                listItems.get(currentPlayPosition).itemIsSpeaking = false;
                listItems.get(currentPlayPosition).itemIsShowAll = false;
                adapter.notifyDataSetChanged();

                // 当前位置的文字播放完成后
                if (currentPlayPosition >= listItems.size() - 1) {
                    // 如果当前位置是最后一个，将播放状态置为初始化状态，重置播放位置为第0个
                    playStatus = InfoHome.PLAY_STATUS_INIT;
                    if (onPlayStatusChanged != null) {
                        onPlayStatusChanged.onPlayStatusChanged(playStatus);
                    }
                    currentPlayPosition = 0;

                    // 弹出对话框询问是否重新播放，如果点确定，继续播放
                    showReplayDialog();
                } else {
                    // 如果当前位置不是列表最后一个，重置正在播放位置，然后继续播放下一条
                    currentPlayPosition++;
                    speakCurrentText();

                    // 如果下一条是最后一条，获取更多头条信息
                    if (currentPlayPosition == listItems.size() - 1 && !isRequestingLoadMore && !isEnd
                            && !isRequestingRequest && !isRequestingPull && listItems.size() < MAX_NEWS_COUNT) {
                        requestMoreLives();
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
