package cn.emoney.acg.page.equipment.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.dialog.CustomDialog;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.BCConvert;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 重大利好
 * 
 * @author emoney_sky
 *
 */
public class ZDLHPage extends PageImpl {
    private final String HEAD_NOTICE_TEMPLATE = "今天共有<font color=\"#00aaff\"><big> %s </big></font><font color=\"#828282\">条</font><font color=\"#f24957\">利好消息</font>";

    public static final int PLAY_STATUS_INIT = 0;
    public static final int PLAY_STATUS_PLAY = 1;
    public static final int PLAY_STATUS_PAUSE = 2;

    private final String TAG_STAR = "star";

    private TextView mTvHeadNotice = null;
    private ListView mListView = null;
    private ZHLHAdapter mAdapter = null;

    private ProgressBar mPbLvEmpty = null;
    private TextView mTvLvEmptyNotice = null;
    private ImageView imgPlayStatus;

    public static ArrayList<Map<String, String>> mLstZDLHData = new ArrayList<Map<String, String>>();
    private ArrayList<Goods> mLstGoods = new ArrayList<Goods>();

    private long mLstNewsId = 0;
    private int currentPlayStatus = PLAY_STATUS_INIT;
    private SpeechSynthesizer mTts; // 语音播放引擎
    private int currentPlayPosition;

    private List<String> mLstReaded = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_equipment_zdlh);

        mTvHeadNotice = (TextView) findViewById(R.id.zdlhpage_tv_head_notice);
        mListView = (ListView) findViewById(R.id.zdlhpage_lv_lstcontent);

        if (mListView != null) {
            // list空时显示
            LinearLayout mLlLvEmpty = (LinearLayout) findViewById(R.id.zdlhpage_ll_lvempty);
            mPbLvEmpty = (ProgressBar) findViewById(R.id.progressBar);
            mTvLvEmptyNotice = (TextView) findViewById(R.id.progressNotice);

            mListView.setEmptyView(mLlLvEmpty);

            mAdapter = new ZHLHAdapter();

            // 设置数据
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    if (mPageChangeFlag == 0) {
                        mPageChangeFlag = -1;
                        gotoZDLHDetail(index);
                    }
                }
            });
        }

        initSpeechEngine();

        bindPageTitleBar(R.id.zdlhTitleBar);
    }

    @Override
    protected void initData() {
        mLstZDLHData.clear();
        mLstNewsId = 0;
        String historySave = getDBHelper().getString(DataModule.G_KEY_EQUIPMENT_ZDLH_CACHE, "");
        if (historySave != null && !historySave.equals("")) {
            try {
                JSONObject jObj = JSON.parseObject(historySave);
                if (jObj.containsKey(String.valueOf(DataModule.G_CURRENT_SERVER_DATE))) {
                    String historyList = jObj.getString(String.valueOf(DataModule.G_CURRENT_SERVER_DATE));

                    if (historyList != null && !historyList.equals("") && historyList.startsWith("[") && historyList.endsWith("]")) {
                        @SuppressWarnings("unchecked")
                        final ArrayList<Map<String, String>> t_lst = JSON.parseObject(historyList, mLstZDLHData.getClass());
                        if (t_lst != null) {
                            mLstZDLHData.addAll(t_lst);
                        }

                        if (mLstZDLHData != null && mLstZDLHData.size() > 0) {
                            try {
                                mLstNewsId = Long.valueOf(mLstZDLHData.get(0).get("newsId"));
                            } catch (NumberFormatException e) {
                            }

                            // for (Map<String, String> mapItem : mLstZDLHData)
                            // {
                            // if (Long.valueOf(mapItem.get("newsId")) >
                            // mLstNewsId)
                            // {
                            // mLstNewsId = Long.valueOf(mapItem.get("newsId"));
                            // }
                            // }
                            updateLstGoods();
                        }

                    } else {
                        getDBHelper().setString(DataModule.G_KEY_EQUIPMENT_ZDLH_CACHE, "");
                    }

                } else {
                    getDBHelper().setString(DataModule.G_KEY_EQUIPMENT_ZDLH_CACHE, "");
                }

            } catch (JSONException e) {
                getDBHelper().setString(DataModule.G_KEY_EQUIPMENT_ZDLH_CACHE, "");
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUI();
                requestZDLH(mLstNewsId, 0);
            }
        }, 400);

    }

    @Override
    protected void onPageResume() {
        // TODO Auto-generated method stub
        super.onPageResume();
        refreshPlayFlag(currentPlayStatus);

        // 加载读取过的状态
        onLoadReadRecord();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

        // 界面隐藏时，暂停播放
        if (mTts != null && currentPlayStatus == PLAY_STATUS_PLAY) {
            currentPlayStatus = PLAY_STATUS_PAUSE;
            mTts.pauseSpeaking();
        }
    }

    @Override
    protected void onPageDestroy() {
        super.onPageDestroy();

        destroySpeechEngine();
    }

    private void requestZDLH(long begin, int size) {
        // ID： 105
        // {
        // token:"342424....", // 通信token(字符串32字节)
        // day:20131017, // 日期
        // begin:20001, // 起始数据id 0表示从头开始
        // size:2 // 请求条数 0表示从id-->last; 非0:从id-->id+size;
        // }

        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KEY_TOKEN, getToken());
            jsObj.put(KEY_DAY, DataModule.G_CURRENT_SERVER_DATE);
            jsObj.put(KEY_BEGIN, begin);
            jsObj.put(KEY_SIZE, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LogUtil.easylog("sky", "ZDLH->requestZDLH:" + jsObj.toJSONString());
        requestInfo(jsObj, IDUtils.ID_ZDLH);
    }

    private void requestGoods() {
        if (mLstGoods.size() == 0) {
            return;
        }
        ArrayList<Integer> goodsId = new ArrayList<Integer>();
        for (Goods goods : mLstGoods) {
            goodsId.add(goods.getGoodsId());
        }

        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.ZDF);
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4).setGroupType(0).addAllGoodsId(goodsId).addAllReqFields(goodsFiled)
        // -9999 代表不排序
                .setSortField(-9999).setSortOrder(false).setReqBegin(0).setReqSize(0).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();
        if (id == IDUtils.ID_ZDLH) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc != null || mc.getMsgData() != null) {
                String msgData = mc.getMsgData();

                LogUtil.easylog("sky", "ZDLH->Rec:" + msgData);

                try {

                    JSONObject jsObj = JSON.parseObject(msgData);
                    String news = jsObj.getString("news");
                    JSONArray jAryNews = JSON.parseArray(news);

                    JSONObject jObjItem = null;
                    if (jAryNews != null) {
                        for (int i = 0; i < jAryNews.size(); i++) {
                            jObjItem = jAryNews.getJSONObject(i);
                            if (jObjItem != null) {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("title", jObjItem.getString("cn2") + " - " + jObjItem.getString("cn4"));
                                map.put("content", jObjItem.getString("summary"));
                                map.put("stockId", jObjItem.getString("stock_id"));
                                map.put("newsId", jObjItem.getString("new_id"));
                                map.put("detailTitle", jObjItem.getString("title"));
                                map.put("isPlaying", "false");
                                map.put(TAG_STAR, jObjItem.getString(TAG_STAR));

                                mLstZDLHData.add(0, map);
                            }
                        }
                    }

                    updateLstGoods();

                    String sLst = JSON.toJSONString(mLstZDLHData);

                    JSONObject jObjSave = new JSONObject();
                    jObjSave.put(String.valueOf(DataModule.G_CURRENT_SERVER_DATE), sLst);

                    getDBHelper().setString(DataModule.G_KEY_EQUIPMENT_ZDLH_CACHE, jObjSave.toJSONString());

                    updateUI();

                } catch (JSONException e) {

                }
            }

            if (mLstZDLHData.size() == 0) {
                if (mPbLvEmpty != null) {
                    mPbLvEmpty.setVisibility(View.GONE);
                }
                if (mTvLvEmptyNotice != null) {
                    mTvLvEmptyNotice.setText("--今日无数据--");
                }
            } else {
                if (mPbLvEmpty != null) {
                    mPbLvEmpty.setVisibility(View.VISIBLE);
                }
                if (mTvLvEmptyNotice != null) {
                    mTvLvEmptyNotice.setText("正在获取数据");
                }
            }

            requestGoods();
        }
    }

    private void updateUI() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        // String sCount = mLstZDLHData.size() > 0 ?
        // String.valueOf(mLstZDLHData.size()) : "_";
        String sCount = mLstZDLHData.size() + "";
        String notice = String.format(HEAD_NOTICE_TEMPLATE, sCount);
        if (mTvHeadNotice != null) {
            mTvHeadNotice.setText(Html.fromHtml(notice));
        }
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof DynaValueDataPackage) {
            DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
            DynaValueData_Reply gr = goodsTable.getResponse();
            if (gr == null || gr.getRepFieldsList().size() == 0 || gr.getQuotaValueList().size() == 0) {
                return;
            }

            int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);

            List<DynaQuota> quota = gr.getQuotaValueList();

            for (int i = 0; i < quota.size(); i++) {
                String zdf = quota.get(i).getRepFieldValueList().get(indexZDF);
                int goodsId = quota.get(i).getGoodsId();

                LogUtil.easylog("sky", "ZDLHPage-> goodsId:" + goodsId + ", ZDF:" + zdf);

                for (int j = 0; j < mLstGoods.size(); j++) {
                    Goods g = mLstGoods.get(j);
                    if (goodsId == g.getGoodsId()) {
                        g.setZdf(zdf);
                        // break;
                    }
                }
            }

            updateUI();
        }
    }

    private void updateLstGoods() {
        mLstGoods.clear();
        for (int i = 0; i < mLstZDLHData.size(); i++) {
            String stockId = mLstZDLHData.get(i).get("stockId");

            String t_gid = null;
            ArrayList<Goods> lst = null;
            if (!stockId.startsWith("6")) {
                t_gid = Util.FormatStockCode("1" + stockId);
            } else {
                t_gid = Util.FormatStockCode(stockId);
            }

            lst = getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);

            Goods g = null;
            if (lst != null && lst.size() > 0) {
                g = lst.get(0);
            } else {
                g = new Goods(Integer.valueOf(stockId), "");
            }

            mLstGoods.add(g);
        }

        closeSQLDBHelper();
    }

    private void gotoZDLHDetail(int index) {
        PageIntent intent = new PageIntent(this, ZDLHDetailHome.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ZDLHDetailHome.EXTRA_KEY_LIST_INDEX, index);
        intent.setArguments(bundle);
        intent.setSupportAnimation(true);
        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "重大利好");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        View rightMenuView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_play, null);
        imgPlayStatus = (ImageView) rightMenuView.findViewById(R.id.layout_titlebar_item_play_img_play_status);

        BarMenuCustomItem rightMenu = new BarMenuCustomItem(2, rightMenuView);
        rightMenu.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightMenu);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        } else if (itemId == 2) {
            if (!NetworkManager.IsNetworkAvailable()) {
                showTip(Util.getResourcesString(R.string.net_error_try));
                return;
            }

            // 1. 更改头条中播放状态
            resetPlayStatus();
            // 2. 修改首页中的播放标志
            refreshPlayFlag(currentPlayStatus);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPageChangeFlag == 0) {
                mPageChangeFlag = -1;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void resetPlayStatus() {
        if (currentPlayStatus == PLAY_STATUS_INIT) {
            // 1. 初始状态时点击，开始播放
            currentPlayStatus = PLAY_STATUS_PLAY;
            speakCurrentText();
        } else if (currentPlayStatus == PLAY_STATUS_PLAY) {
            // 2. 播放状态时点击，暂停播放
            currentPlayStatus = PLAY_STATUS_PAUSE;
            mTts.pauseSpeaking();
        } else if (currentPlayStatus == PLAY_STATUS_PAUSE) {
            // 3. 暂停状态时点击，继续播放
            currentPlayStatus = PLAY_STATUS_PLAY;
            mTts.resumeSpeaking();
        }
    }

    /**
     * 播放从当前位置开始播放文字
     * */
    private void speakCurrentText() {
        // 播放当前位置的文字
        mTts.startSpeaking(getSpeakText(), mSynListener);

        currentPlayStatus = PLAY_STATUS_PLAY;
        refreshPlayFlag(currentPlayStatus);

        // 更新头条列表中播放状态标志
        if (mLstZDLHData.size() > 0) {
            mLstZDLHData.get(currentPlayPosition).put("isPlaying", "true");
            mAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 获取当前播放位置的文字
     * */
    private String getSpeakText() {
        String text = "";

        if (currentPlayPosition < mLstZDLHData.size()) {
            // 如果有摘要，播放摘要，如果没有摘要，播放标题
            String title = mLstZDLHData.get(currentPlayPosition).get("title");
            String summary = mLstZDLHData.get(currentPlayPosition).get("content");
            text = title + " 。： " + summary;
        }

        return text;
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
            if (mLstZDLHData.size() > 0) {
                mLstZDLHData.get(currentPlayPosition).put("isPlaying", "false");
                mAdapter.notifyDataSetChanged();
            }

            if (currentPlayPosition >= mLstZDLHData.size() - 1) {
                // 如果是最后一样播放完成，将播放状态置为初始化状态，重置播放位置为第0个
                currentPlayStatus = PLAY_STATUS_INIT;
                refreshPlayFlag(currentPlayStatus);
                currentPlayPosition = 0;

                // 弹出对话框询问是否重新播放，如果点确定，继续播放
                showReplayDialog();
            } else {
                // 如果不是最后一条播放完成，然后继续播放下一条
                currentPlayPosition++;
                speakCurrentText();
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

    /**
     * 刷新播放图标状态
     * */
    private void refreshPlayFlag(int playStatus) {
        switch (playStatus) {
            case PLAY_STATUS_INIT:
                imgPlayStatus.setImageResource(R.drawable.img_info_title_play_status_3);
                break;
            case PLAY_STATUS_PLAY:
                loopPlayStatus();
                break;
            case PLAY_STATUS_PAUSE:
                imgPlayStatus.setImageResource(R.drawable.img_info_title_play_status_pause);
                break;
            default:
                break;
        }
    }

    /**
     * 创建并初始化语音播放引擎
     * */
    private void initSpeechEngine() {
        // 1. 创建 SpeechSynthesizer对象，第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(getContext(), null);

        // 2. 全成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); // 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50"); // 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80"); // 设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
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
    }

    /**
     * 循环显示3种播放状态
     * */
    private void loopPlayStatus() {
        imgPlayStatus.setImageResource(R.drawable.anim_infohome_play_status);
        AnimationDrawable animationDrawable = (AnimationDrawable) imgPlayStatus.getDrawable();
        animationDrawable.start();
    }

    class ZHLHAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int size = mLstZDLHData.size();
            return size;
        }

        @Override
        public Object getItem(int position) {
            return mLstZDLHData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.layout_zdlh_listitem, null);
                View vStockMask = convertView.findViewById(R.id.zdlhitem_v_stockmask);
                LinearLayout llStockInfoContent = (LinearLayout) convertView.findViewById(R.id.zdlhitem_ll_stockinfo);
                TextView tvStockName = (TextView) convertView.findViewById(R.id.zdlhitem_tv_stockname);
                TextView tvStockCode = (TextView) convertView.findViewById(R.id.zdlhitem_tv_stockcode);
                TextView tvStockZdf = (TextView) convertView.findViewById(R.id.zdlhitem_tv_zdf);
                TextView tvTitle = (TextView) convertView.findViewById(R.id.titleView);
                ImageView imgPlayStatus = (ImageView) convertView.findViewById(R.id.playingTagView);

                ImageView[] starViewArr = new ImageView[5];
                starViewArr[0] = (ImageView) convertView.findViewById(R.id.levView1);
                starViewArr[1] = (ImageView) convertView.findViewById(R.id.levView2);
                starViewArr[2] = (ImageView) convertView.findViewById(R.id.levView3);
                starViewArr[3] = (ImageView) convertView.findViewById(R.id.levView4);
                starViewArr[4] = (ImageView) convertView.findViewById(R.id.levView5);
                ListCell lc = new ListCell(vStockMask, llStockInfoContent, tvStockName, tvStockCode, tvStockZdf, tvTitle, convertView.findViewById(R.id.dividerLayout), imgPlayStatus, starViewArr);

                convertView.setTag(lc);
            }

            ListCell lc = (ListCell) convertView.getTag();
            // lc.tvTitle.setText(mLstZDLHData.get(position).get("title"));
            // lc.tvTitle.setTextColor(mTxtMain);
            String strContent = mLstZDLHData.get(position).get("title");
            // 正则 去掉开头的全角和半角空格
            String regStartSpace = "^[ 　]*";
            strContent = strContent.replaceFirst(regStartSpace, "");

            strContent = BCConvert.qj2bj(strContent);
            // strContent = "  " + strContent;
            Map<String, String> mapItem = mLstZDLHData.get(position);
            if (mapItem != null && mapItem.containsKey("isPlaying")) {
                String playStatus = mapItem.get("isPlaying");
                if ("true".equals(playStatus)) {
                    lc.imgPlayStatus.setVisibility(View.VISIBLE);
                } else {
                    lc.imgPlayStatus.setVisibility(View.GONE);
                }
            } else {
                lc.imgPlayStatus.setVisibility(View.GONE);
            }

            lc.tvTitle.setText(Html.fromHtml(strContent));
            // 读过的状态
            if (mLstReaded != null && mLstReaded.contains(mLstZDLHData.get(position).get("newsId"))) {
                // 读过
                lc.tvTitle.setTextColor(Util.getResourcesColor(R.color.t3));
            } else {
                // 未读
                lc.tvTitle.setTextColor(Util.getResourcesColor(R.color.t1));
            }

            if (position < mLstGoods.size()) {
                Goods g = mLstGoods.get(position);
                lc.tvStockName.setText(g.getGoodsName());
                lc.tvStockCode.setText(g.getGoodsCode());
                String zdf = g.getZdf();
                lc.tvStockZdf.setText(DataUtils.getSignedZDF(zdf));
                lc.llStockInfoContent.setBackgroundResource(getBgResId(FontUtils.getColorByZDF(zdf)));

                // 等级
                if (lc.starViewArr != null) {
                    int star = DataUtils.convertToInt(mapItem.containsKey(TAG_STAR) ? mapItem.get(TAG_STAR) : "0");
                    for (int i = 0; i < lc.starViewArr.length; i++) {
                        if (i < star) {
                            lc.starViewArr[i].setSelected(true);
                        } else {
                            lc.starViewArr[i].setSelected(false);
                        }
                    }
                }
            } else {
                lc.llStockInfoContent.setBackgroundResource(getBgResId(0));
            }

            // 分隔线
            if (position == mLstGoods.size() - 1) {
                lc.dividerLayout.setVisibility(View.GONE);
            } else {
                lc.dividerLayout.setVisibility(View.VISIBLE);
            }

            final int index = position;
            lc.vStockMask.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    if (mPageChangeFlag == 0) {
                        mPageChangeFlag = -1;
                        QuoteJump.gotoQuote(ZDLHPage.this, mLstGoods, index);
//                        gotoQuote(mLstGoods, index);
                    }
                }
            });

            return convertView;
        }
    }

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }

        return new String(c);
    }


    private int getBgResId(float flag) {
        if (flag == 0) {
            return R.drawable.shape_bg_c1_radius;
        } else if (flag > 0) {
            return R.drawable.shape_bg_c1_radius;
        } else {
            return R.drawable.shape_bg_c2_radius;
        }
    }

    class ListCell {
        public View vStockMask = null;
        public LinearLayout llStockInfoContent = null;
        public TextView tvStockName = null;
        public TextView tvStockCode = null;
        public TextView tvStockZdf = null;
        public TextView tvTitle = null;
        private View dividerLayout = null;
        private ImageView imgPlayStatus;
        private ImageView[] starViewArr = null;

        public ListCell(View vStockMask, LinearLayout llStockInfoContent, TextView tvStockName, TextView tvStockCode, TextView tvStockZdf, TextView tvTitle, View dividerLayout, ImageView imgPlayStatus, ImageView[] starViewArr) {
            this.vStockMask = vStockMask;
            this.llStockInfoContent = llStockInfoContent;
            this.tvStockName = tvStockName;
            this.tvStockCode = tvStockCode;
            this.tvStockZdf = tvStockZdf;
            this.tvTitle = tvTitle;
            this.dividerLayout = dividerLayout;
            this.imgPlayStatus = imgPlayStatus;
            this.starViewArr = starViewArr;
        }
    }


    // 加载读取过的状态(保存的是newsId)
    private void onLoadReadRecord() {
        String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_ZDLH_READ_RECORD, new String[] {});
        if (aryReaded != null) {
            mLstReaded = Arrays.asList(aryReaded);
        }
    }
}
