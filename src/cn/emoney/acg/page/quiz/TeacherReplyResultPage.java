package cn.emoney.acg.page.quiz;

import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.media.AppMediaPlayerManager;
import cn.emoney.acg.media.AudioRecordCacheManager;
import cn.emoney.acg.media.AudioRecordCacheManager.AudioCacheBackListener;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.main.MainPage;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.textviewlink.LinkManager;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: TeacherReplyResultPage
 * @Description:老师回答问题结果页面
 * @author xiechengfa
 * @date 2015年12月15日 下午6:46:36
 */
public class TeacherReplyResultPage extends PageImpl implements MyMediaPlayerListener, AudioCacheBackListener {
    private static final String KEY_ITEM = "item";

    private QuizContentInfo info = null;
    private AppMediaPlayerManager mediaPlayerManager = null;

    public static void startPage(PageImpl page, QuizContentInfo info) {
        PageIntent intent = new PageIntent(page, TeacherReplyResultPage.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_ITEM, info);
        intent.setArguments(bundle);
        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_teacher_reply_result);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(KEY_ITEM)) {
            info = (QuizContentInfo) bundle.getSerializable(KEY_ITEM);
        }

        init();

        bindPageTitleBar(R.id.pageTeacherReplyResultTitleBar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回键
            finishToPage();
            return true;
        }

        return onKeyUp(keyCode, event);
    }

    @Override
    protected void onPageResume() {
        if (mediaPlayerManager != null) {
            mediaPlayerManager.setPageState(true);
        }

        AudioRecordCacheManager.getInstance().setListener(this);

        // 设置当前的媒体类型
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        resetPlayVoiceState();

        super.onPageResume();
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onPause();
            mediaPlayerManager.setPageState(false);
        }

        AudioRecordCacheManager.getInstance().setListener(null);

        // 设置当前的媒体类型
        getActivity().setVolumeControlStream(AudioManager.STREAM_RING);

        super.onPagePause();
    }

    protected void onPageDestroy() {
        // TODO Auto-generated method stub
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
            mediaPlayerManager = null;
        }

        super.onPageDestroy();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "已解答");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            // back
            mPageChangeFlag = -1;
            finishToPage();
        }
    }

    private void init() {
        findViewById(R.id.stateView).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finishToPage();
            }
        });

        TextView tipView = (TextView) findViewById(R.id.tipView);
        tipView.setText("解答");
        findViewById(R.id.levLayout).setVisibility(View.GONE);


        // 问题
        TextView askNameView = (TextView) findViewById(R.id.askNameView);
        askNameView.setText(info.getOwner().getNickName());

        TextView askDateView = (TextView) findViewById(R.id.askDateView);
        askDateView.setText(DateUtils.formatQuizCommitTime(info.getCommitTime()));

        TextView askcontentView = (TextView) findViewById(R.id.askcontentView);
        askcontentView.setText(info.getContent());
        // 加事件
        LinkManager.addStockLinkToTv(this, askcontentView);

        // 回复
        ImageView headIV = (ImageView) findViewById(R.id.headIV);
        Util.loadHeadIcon(headIV, info.getReplier().getId() + "", info.getReplier().getIcon());
        headIV.setOnClickListener(new OnClickEffectiveListener() {

            @Override
            public void onClickEffective(View v) {
                // TODO Auto-generated method stub
                TeacherDetailPage.startPage(TeacherReplyResultPage.this, info.getReplier().getId(), info.getReplier().getNick());
            }
        });

        TextView replyNameView = (TextView) findViewById(R.id.replyNameView);
        replyNameView.setText(info.getReplier().getNick());

        TextView replyDateView = (TextView) findViewById(R.id.replyDateView);
        replyDateView.setText(DateUtils.formatQuizCommitTime(info.getAnswerTime()));

        findViewById(R.id.levLayout).setVisibility(View.GONE);

        if (info.getAnswer().getType() == QuizContentInfo.CONTENT_TYPE_TEXT) {
            // 文本
            findViewById(R.id.voiceLayout).setVisibility(View.GONE);
            TextView replycontentView = (TextView) findViewById(R.id.replycontentView);
            replycontentView.setVisibility(View.VISIBLE);
            replycontentView.setText(info.getAnswer().getContent());
            // 加事件
            LinkManager.addStockLinkToTv(this, replycontentView);
        } else {
            // 语音
            findViewById(R.id.voiceLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.replycontentView).setVisibility(View.GONE);

            TextView voidTimeView = (TextView) findViewById(R.id.voidTimeView);
            voidTimeView.setText(info.getAnswer().getVoiceTime());

            findViewById(R.id.voiceBgLayout).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    onPlayVoice();
                }
            });

            initVoiceState();
        }
    }

    private void onPlayVoice() {
        info.setDowning(true);
        AudioRecordCacheManager.getInstance().getLocPathByUrl(info.getAnswer().getVoiceUrl(), info.getId());
    }

    private void finishToPage() {
        // 设置问题结束的状态
        TeacherHomePage.isQuestionOverOfReply = true;
        PageIntent pageIntent = new PageIntent(null, MainPage.class);
        finishToPage(pageIntent);
    }

    /**
     * 准备完成，可以播放
     * 
     * @param mp
     */
    @Override
    public void onPlayerStart() {
        // TODO Auto-generated method stub
        info.setDowning(false);
        info.setPlaying(true);
        initVoiceState();
    }

    /**
     * 播放器暂停
     */
    public void onPlayerPause() {
        resetPlayVoiceState();
    }

    /**
     * 播放结束
     * 
     * @param mp
     */
    @Override
    public void onPlayerCompletion() {
        // TODO Auto-generated method stub
        resetPlayVoiceState();
    }

    /**
     * 加载播放器出错
     */
    @Override
    public void onPlayerError() {
        // TODO Auto-generated method stub
        resetPlayVoiceState();
    }

    /**
     * 缓存成功
     * 
     * @param path
     */
    public void onRecordCacheSucc(String path, long recordId) {
        if (mediaPlayerManager == null) {
            mediaPlayerManager = new AppMediaPlayerManager(this);
            mediaPlayerManager.setPageState(true);
        }

        mediaPlayerManager.onStartPlayer(info.getAnswer().getVoiceUrl());
    }

    /**
     * 缓存失败
     */
    public void onRecordCacheFail(long recordId) {
        info.setDowning(false);
        initVoiceState();
    }

    private void resetPlayVoiceState() {
        info.setPlaying(false);
        info.setDowning(false);
        initVoiceState();
    }

    private void initVoiceState() {
        ImageView voiceView = (ImageView) findViewById(R.id.voiceView);
        if (info.isPlaying()) {
            // 播放
            voiceView.setImageResource(R.drawable.anim_quiz_voice);
            AnimationDrawable animationDrawable = (AnimationDrawable) voiceView.getDrawable();
            animationDrawable.start();
        } else {
            voiceView.setImageResource(R.drawable.img_voice3);
        }

        LinearLayout voiceBgLayout = (LinearLayout) findViewById(R.id.voiceBgLayout);
        if (info.isDowning()) {
            // 正在下载，不要点击
            voiceBgLayout.setClickable(false);
            voiceBgLayout.setSelected(true);
        } else {
            // 可点击
            voiceBgLayout.setClickable(true);
            voiceBgLayout.setSelected(false);
        }
    }
}
