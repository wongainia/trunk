package cn.emoney.acg.page.quiz;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizAnswerPackage;
import cn.emoney.acg.data.protocol.quiz.QuizAnswerReply.QuizAnswer_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Answer;
import cn.emoney.acg.data.protocol.quiz.QuizDropPackage;
import cn.emoney.acg.data.protocol.quiz.QuizDropReply.QuizDrop_Reply;
import cn.emoney.acg.data.quiz.AnswerInfo;
import cn.emoney.acg.data.quiz.QuizCommonRequest;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.media.AppMediaPlayerManager;
import cn.emoney.acg.media.AppMediaRecorder;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FileUtils;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.textviewlink.LinkManager;
import cn.emoney.acg.view.RoundProgressBar;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.http.AsyncHttpResponseHandler;
import cn.emoney.sky.libs.network.HttpClient;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: TeacherReplayPage
 * @Description:老师回答问题页面
 * @author xiechengfa
 * @date 2015年12月15日 下午6:46:36
 */
public class TeacherReplayPage extends PageImpl implements OnClickListener, MyMediaPlayerListener {
    private static final String KEY_ITEM = "item";
    private static final int TYPE_SEND = 100;
    private static final int TYPE_DROP = 101;

    private final int REC_MAX_TIME = 120;// 录音最长时间为120s
    private final int RECORD_ANIMATION_TIME_SP = 50;// 录音的刷新时间间隔
    private final int PLAYER_ANIMATION_TIME_SP = 30;// 播放的刷新时间间隔
    private final String RECORDING_STR = "录音中  ";

    private final static int MSG_TYPE_KEYBOARD = 1;// 键盘
    private final static int MSG_TYPE_VOICE = 2;// 语音
    private final static int MSG_RECORDING = 3;// 正在录音
    private final static int MSG_PLAYING = 4;// 正在播放

    private final static int STATE_NONE = 0;// 无
    private final static int STATE_RECORDING = 1;// 正在录音
    private final static int STATE_RECORD_OVER = 2;// 录音结束
    private final static int STATE_PLAYING = 3;// 正在播放

    private int requestType = TYPE_SEND;
    private int currType = QuizContentInfo.CONTENT_TYPE_TEXT;
    private int currState = STATE_NONE;

    private String userName = "";

    private Answer.Builder answer = null;
    private QuizContentInfo info = null;
    private QuizCommonRequest request = null;

    // 输入
    private EditText inputEditText = null;
    private View voicImageView = null;
    private View sendImageView = null;

    // 语音
    private long startTime = 0;// 录音的开始时间，单位 ：ms
    private int recDuration = 0;// 语音时长,单位：ms
    private String recPath = FileUtils.getStoragePath() + DataModule.G_LOC_PATH + "voice_temp." + DataModule.FORMAT_AMR;
    private TextView voiceTipView = null;
    private ImageView recordImage = null;
    private RoundProgressBar progressBar = null;
    private AppMediaRecorder recorder = null;
    private AppMediaPlayerManager mediaPlayerManager = null;
    private CountDownTimer mCountDownTimer = null;

    public static void startPage(PageImpl page, QuizContentInfo info) {
        PageIntent intent = new PageIntent(page, TeacherReplayPage.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_ITEM, info);
        intent.setArguments(bundle);
        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_teacher_reply);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(KEY_ITEM)) {
            info = (QuizContentInfo) bundle.getSerializable(KEY_ITEM);
            userName = info.getOwner().getNickName();
        }

        request = new QuizCommonRequest(this);

        init();

        bindPageTitleBar(R.id.pageTeacherReplyTitleBar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onPageResume() {
        // 设置当前的媒体类型
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (mediaPlayerManager != null) {
            mediaPlayerManager.setPageState(true);
        }

        if (currType == QuizContentInfo.CONTENT_TYPE_TEXT) {
            handler.sendEmptyMessageDelayed(MSG_TYPE_KEYBOARD, 500);
        }

        super.onPageResume();
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        if (currType == QuizContentInfo.CONTENT_TYPE_TEXT) {
            InputMethodUtil.closeSoftKeyBoard(this);
        }

        if (mediaPlayerManager != null) {
            mediaPlayerManager.onPause();
            mediaPlayerManager.setPageState(false);
        }

        // 设置当前的媒体类型
        getActivity().setVolumeControlStream(AudioManager.STREAM_RING);

        super.onPagePause();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回键
            onBackEvent();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPageDestroy() {
        // TODO Auto-generated method stub
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
            mediaPlayerManager = null;
        }

        recycleCountDownTimer();

        super.onPageDestroy();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "解答中");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        View rightView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView textView = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        textView.setText("放弃");

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            // back
            onBackEvent();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            // 放弃
            showDropDialog();
        }
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        if (pkg == null) {
            onFail();
            return;
        }

        if (pkg.getRequestType() == TYPE_SEND) {
            // 回复
            QuizAnswerPackage dataPackage = (QuizAnswerPackage) pkg;
            QuizAnswer_Reply reply = dataPackage.getResponse();
            if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                onSucc(reply.getTime());
            } else {
                onFail();
            }
        } else if (pkg.getRequestType() == TYPE_DROP) {
            // 放弃
            QuizDropPackage dataPackage = (QuizDropPackage) pkg;
            QuizDrop_Reply reply = dataPackage.getResponse();
            if (reply != null && reply.getResult() != null && reply.getResult().getResult() == DataModule.QUIZ_SERVER_STATE_SUCC) {
                onSucc(0);
            } else {
                onFail();
            }
        }
    }

    @Override
    protected void updateWhenDecodeError(short type) {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        onFail();
    }

    @Override
    protected void updateWhenNetworkError(short type) {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        onFail();
    }

    private void onFail() {
        if (requestType == TYPE_SEND) {
            showTip("提交失败");
        } else {
            showTip("放弃失败");
        }
    }

    private void onSucc(int time) {
        TeacherHomePage.isQuestionOverOfReply = true;

        if (requestType == TYPE_SEND) {
            // 发送
            info.setAnswerTime(time);
            info.setAnswer(AnswerInfo.initServerAnswer(answer));
            info.setReplier(getUserInfo().converToTeacherInfo());

            recycleCountDownTimer();

            TeacherReplyResultPage.startPage(this, info);
        } else {
            // 放弃
            // 设置问题结束的状态
            finish();
        }
    }

    private void recycleCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.editRightLayout:
                // 发送或切换到语音
                if (inputEditText.getText().toString().trim().length() > 0) {
                    // 发送文字
                    onSend(QuizContentInfo.CONTENT_TYPE_TEXT);
                } else {
                    // 切换
                    setKeyboardAndVoiceLayout(false);
                }
                break;
            case R.id.keyBoardImageView:
                // 键盘
                setKeyboardAndVoiceLayout(true);
                break;
            case R.id.recordBtn:
                // 录音
                recBtnEvent();
                break;
            case R.id.cancelVoiceBtn:
                // 重录
                onStopRecordAndPlayer();
                currState = STATE_NONE;
                resetVoicLayout();
                break;
            case R.id.sendVoiceBtn:
                // 发送语音
                onStopRecordAndPlayer();
                onSend(QuizContentInfo.CONTENT_TYPE_VOICE);
                break;
        }
    }

    private void recBtnEvent() {
        switch (currState) {
            case STATE_NONE:
                // 开始录音
                onStartRecord();
                break;
            case STATE_RECORDING:
                // 正在录音
                onStopRecord();
                onPlayRecord();
                break;
            case STATE_RECORD_OVER:
                // 录音结束
                onPlayRecord();
                break;
            case STATE_PLAYING:
                // 正在播放
                onStopPlayRecord();
                break;
        }
    }

    // 开始录音
    private void onStartRecord() {
        currState = STATE_RECORDING;
        startTime = System.currentTimeMillis();
        if (recorder == null) {
            recorder = new AppMediaRecorder();
        }
        recorder.start(recPath);

        // ui
        recordImage.setImageResource(R.drawable.img_quiz_recording);
        findViewById(R.id.voiceTimeTipView).setVisibility(View.VISIBLE);
        voiceTipView.setText(RECORDING_STR + "0:00");

        handler.sendEmptyMessageDelayed(MSG_RECORDING, RECORD_ANIMATION_TIME_SP);
    }

    // 停止录音
    private void onStopRecord() {
        currState = STATE_RECORD_OVER;
        recDuration = (int) (System.currentTimeMillis() - startTime);
        if (recorder != null) {
            recorder.stop();
        }

        // ui
        recordImage.setImageResource(R.drawable.img_quiz_record_start_play);
        findViewById(R.id.voiceTimeTipView).setVisibility(View.GONE);
        findViewById(R.id.btnLayout).setVisibility(View.VISIBLE);
        voiceTipView.setText("" + DateUtils.secondValueToSecondStr(recDuration / 1000));
    }

    // 播放录音
    private void onPlayRecord() {
        currState = STATE_PLAYING;

        if (mediaPlayerManager == null) {
            mediaPlayerManager = new AppMediaPlayerManager(this);
            mediaPlayerManager.setPageState(true);
        }

        mediaPlayerManager.onStartPlayer(recPath);

        recordImage.setImageResource(R.drawable.img_quiz_recording);
        findViewById(R.id.voiceTimeTipView).setVisibility(View.GONE);
        findViewById(R.id.btnLayout).setVisibility(View.VISIBLE);
        voiceTipView.setText("0:00");
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(recDuration);
        progressBar.setProgress(0);
    }

    // 停止播放
    private void onStopPlayRecord() {
        currState = STATE_RECORD_OVER;
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
        }

        setStateOfStopPlayer();
    }

    private void setStateOfStopPlayer() {
        currState = STATE_RECORD_OVER;
        recordImage.setImageResource(R.drawable.img_quiz_record_start_play);
        progressBar.setVisibility(View.GONE);
        voiceTipView.setText(DateUtils.secondValueToSecondStr(recDuration / 1000));
    }

    // 设置输入法和语音输入
    private void setKeyboardAndVoiceLayout(boolean isInput) {
        if (isInput) {
            // 文字输入
            currType = QuizContentInfo.CONTENT_TYPE_TEXT;
            InputMethodUtil.openInputMethod(this);
            findViewById(R.id.inputLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.voiceLayout).setVisibility(View.GONE);

            // 停止对录音文件的操作
            onStopRecordAndPlayer();
        } else {
            // 语音
            currType = QuizContentInfo.CONTENT_TYPE_VOICE;
            InputMethodUtil.closeSoftKeyBoard(this);
            handler.sendEmptyMessageDelayed(MSG_TYPE_VOICE, 100);
        }
    }

    // 停止对录音文件的操作
    private void onStopRecordAndPlayer() {
        if (currState == STATE_RECORDING) {
            onStopRecord();
        } else if (currState == STATE_PLAYING) {
            onStopPlayRecord();
        }
    }

    // 重置语音Layout
    private void resetVoicLayout() {
        currState = STATE_NONE;
        voiceTipView.setText(R.string.quiz_reply_start_rec);
        recordImage.setImageResource(R.drawable.img_quiz_record_start);
        progressBar.setProgress(0);
        findViewById(R.id.btnLayout).setVisibility(View.GONE);
        findViewById(R.id.voiceTimeTipView).setVisibility(View.GONE);
    }

    private void init() {
        TextView stateView = (TextView) findViewById(R.id.stateView);
        setCountDownTimer(stateView, info.getTakeTime());

        ImageView headIV = (ImageView) findViewById(R.id.headIV);
        Util.loadHeadIcon(headIV, info.getOwner().getUid(), info.getOwner().getHeadId());

        TextView nameView = (TextView) findViewById(R.id.nameView);
        nameView.setText(userName);

        TextView dateView = (TextView) findViewById(R.id.dateView);
        dateView.setText(DateUtils.formatQuizCommitTime(info.getCommitTime()));

        TextView contentView = (TextView) findViewById(R.id.contentView);
        contentView.setText(info.getContent());
        // 加事件
        LinkManager.addStockLinkToTv(TeacherReplayPage.this, contentView);

        // 输入
        inputEditText = (EditText) findViewById(R.id.inputET);
        InputMethodUtil.setImeOptions(inputEditText, EditorInfo.IME_ACTION_NONE);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = inputEditText.getText().toString();
                if (text.trim().length() > 0) {
                    voicImageView.setVisibility(View.GONE);
                    sendImageView.setVisibility(View.VISIBLE);
                } else {
                    voicImageView.setVisibility(View.VISIBLE);
                    sendImageView.setVisibility(View.GONE);
                }
            }
        });
        inputEditText.setHint("回复 " + userName);

        voicImageView = findViewById(R.id.voicImageView);
        sendImageView = findViewById(R.id.sendImageView);
        findViewById(R.id.editRightLayout).setOnClickListener(this);
        findViewById(R.id.inputLayout).setVisibility(View.VISIBLE);

        // 语音
        voiceTipView = (TextView) findViewById(R.id.voiceTipView);
        recordImage = (ImageView) findViewById(R.id.recordBtn);
        progressBar = (RoundProgressBar) findViewById(R.id.roundProgressBar);
        recordImage.setOnClickListener(this);
        findViewById(R.id.cancelVoiceBtn).setOnClickListener(this);
        findViewById(R.id.sendVoiceBtn).setOnClickListener(this);
        findViewById(R.id.keyBoardImageView).setOnClickListener(this);
        findViewById(R.id.voiceLayout).setVisibility(View.GONE);
    }

    // 返回操作
    private void onBackEvent() {
        if (checkIsEdited()) {
            showBackDialog();
        } else {
            finishPage();
        }
    }

    // 是否有内容
    private boolean checkIsEdited() {
        String content = inputEditText.getText().toString();
        if (content != null && content.trim().length() > 0 || currState != STATE_NONE) {
            return true;
        } else {
            return false;
        }
    }

    // 返回的二次确认
    private void showBackDialog() {
        // 停止对录音文件的操作
        onStopRecordAndPlayer();

        DialogUtils.showMessageDialog(getActivity(), "提示", "是否放弃编辑?", "放弃", "继续编辑", new CustomDialogListener() {
            @Override
            public void onConfirmBtnClicked() {
                // TODO Auto-generated method stub
                finishPage();
            }

            @Override
            public void onCancelBtnClicked() {}
        });
    }

    // 放弃的二次确认
    private void showDropDialog() {
        // 停止对录音文件的操作
        onStopRecordAndPlayer();

        DialogUtils.showMessageDialog(getActivity(), "提示", "是否放弃解答?", "确定", "取消", new CustomDialogListener() {
            @Override
            public void onConfirmBtnClicked() {
                // TODO Auto-generated method stub
                onDrop();
            }

            @Override
            public void onCancelBtnClicked() {

            }
        });
    }

    private void finishPage() {
        mPageChangeFlag = -1;
        onStopRecordAndPlayer();
        InputMethodUtil.closeSoftKeyBoard(this);
        finish();
    }

    private void onSend(int type) {
        if (info.getStatus() == QuizContentInfo.STATE_ANSWER_CLOSE) {
            showTip("问题已超时，不能回复");
            return;
        }

        if (currType == QuizContentInfo.CONTENT_TYPE_TEXT) {
            // 发送文字
            DialogUtils.showProgressDialog(getContext(), "正在发送...", null);
            sendReply(inputEditText.getText().toString(), currType);;
        } else {
            // 发送语音
            uploadVoiceFile();
        }
    }

    // 上传语音
    private void uploadVoiceFile() {
        DialogUtils.showProgressDialog(getContext(), "正在发送...", null);

        String token = getUserInfo().getToken();
        final String url = DataModule.IMAGE_VOICE_URL + DataModule.FORMAT_AMR + "&token=" + token;
        HttpClient httpClient = new HttpClient(getContext());
        httpClient.uploadFileRequst(url, recPath, new AsyncHttpResponseHandler() {
            public void onSuccess(String response) {
                LogUtil.easylog("onSuccess:" + response);
                try {
                    JSONObject jsObj = JSON.parseObject(response);
                    int retCode = jsObj.getIntValue("retcode");
                    String msg = jsObj.getString("retmsg");
                    if (retCode == 1) {
                        // 成功
                        sendReply((int) (recDuration / 1000) + "|" + msg, QuizContentInfo.CONTENT_TYPE_VOICE);
                    } else {
                        showTip("发送失败");
                    }
                } catch (Exception e) {
                    showTip("发送失败");
                }
            }

            @Override
            public void onStart() {
                LogUtil.easylog("onStart");
            }

            @Override
            public void onFinish() {
                LogUtil.easylog("onFinish");
            }

            @Override
            public void onFailure(Throwable error, String content) {
                DialogUtils.closeProgressDialog();
                showTip("发送失败");
                LogUtil.easylog("onFailure:" + error.getMessage() + ",content:" + content);
            }
        });
    }

    // 提交
    private void sendReply(String content, int type) {
        LogUtil.easylog("****************test content:" + content);
        if (answer == null) {
            answer = Answer.newBuilder();
        }
        answer.setContent(content);
        answer.setType(type);

        this.requestType = TYPE_SEND;
        request.onAnswerQuestion(info.getId(), answer, Util.getReleativeStockList(info.getContent()), TYPE_SEND);
    }


    private void onDrop() {
        DialogUtils.showProgressDialog(getContext(), "正在处理...", null);
        this.requestType = TYPE_DROP;
        request.onDropQuestion(info.getId(), TYPE_DROP);
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_TYPE_KEYBOARD:
                    // 键盘
                    InputMethodUtil.setEditTextFocus(inputEditText);
                    InputMethodUtil.openInputMethod(TeacherReplayPage.this);
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    break;
                case MSG_TYPE_VOICE:
                    // 语音
                    findViewById(R.id.inputLayout).setVisibility(View.GONE);
                    findViewById(R.id.voiceLayout).setVisibility(View.VISIBLE);
                    resetVoicLayout();
                    break;
                case MSG_RECORDING:
                    // 正在录音
                    if (currState == STATE_RECORDING) {
                        voiceTipView.setText(RECORDING_STR + DateUtils.millisToSecondStr(System.currentTimeMillis() - startTime));
                        handler.sendEmptyMessageDelayed(MSG_RECORDING, RECORD_ANIMATION_TIME_SP);
                        if ((System.currentTimeMillis() - startTime) / 1000 >= REC_MAX_TIME) {
                            onStopRecord();
                        }
                    }
                    break;
                case MSG_PLAYING:
                    // 正在播放
                    if (currState == STATE_PLAYING) {
                        voiceTipView.setText("" + DateUtils.secondValueToSecondStr(mediaPlayerManager.getCurrentPosition() / 1000));
                        if (mediaPlayerManager != null) {
                            progressBar.setProgress(mediaPlayerManager.getCurrentPosition());
                        }
                        handler.sendEmptyMessageDelayed(MSG_PLAYING, PLAYER_ANIMATION_TIME_SP);
                    }
                    break;
            }
        };
    };


    /**
     * 准备完成，可以播放
     * 
     */
    public void onPlayerStart() {
        handler.sendEmptyMessageDelayed(MSG_PLAYING, PLAYER_ANIMATION_TIME_SP);
    }

    /**
     * 播放器暂停
     */
    public void onPlayerPause() {
        setStateOfStopPlayer();
    }

    /**
     * 播放结束
     * 
     */
    public void onPlayerCompletion() {
        if (progressBar != null) {
            progressBar.setProgress(recDuration);
        }
        setStateOfStopPlayer();
    }

    /**
     * 加载播放器出错
     */
    public void onPlayerError() {
        setStateOfStopPlayer();
    }

    private void setCountDownTimer(final TextView tv, int time) {
        long tRemainTime = QuizConfigData.getInstance().getTeacherHandleTime() - (DateUtils.getTimeStamp() / 1000 - DataModule.G_LOCAL_SERVER_TIME_GAP - time);
        if (tRemainTime > 0) {
            tv.setText(String.format(Util.getResourcesString(R.string.quiz_reply_hold_time_str), DateUtils.second2MSLable(tRemainTime)));
            if (mCountDownTimer == null) {
                mCountDownTimer = new CountDownTimer(tRemainTime * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        tv.setText(String.format(Util.getResourcesString(R.string.quiz_reply_hold_time_str), DateUtils.second2MSLable(millisUntilFinished / 1000)));
                    }

                    @Override
                    public void onFinish() {
                        tv.setText("问题已超时");
                        TeacherHomePage.isQuestionOverOfReply = true;
                        info.setStatus(QuizContentInfo.STATE_ANSWER_CLOSE);
                    }
                }.start();
            }
        } else {
            tv.setText("问题已超时");
            TeacherHomePage.isQuestionOverOfReply = true;
            info.setStatus(QuizContentInfo.STATE_ANSWER_CLOSE);
        }
    }
}
