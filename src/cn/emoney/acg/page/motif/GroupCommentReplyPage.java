package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.dialog.ListDialog.OnListDialogItemClickListener;
import cn.emoney.acg.dialog.ListDialogMenuInfo;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.EmojiKeyboardUtil;
import cn.emoney.acg.util.EmojiKeyboardUtil.OnSendKeyListener;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.ViewUtil;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.widget.CircleImageView;
import cn.emoney.sky.fixcmojitv.EmojiconTextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GroupCommentReplyPage extends PageImpl implements OnGlobalLayoutListener {

    private final Object synObj = new Object();
    private View root = null;
    private LinearLayout mLlInputContent = null;
    private EmojiKeyboardUtil mEmojiKeyboardUtil = null;
    private View mGroupTitleBar;

    private ListView mListView = null;
    private View mBackBtn;
    private int mTitlebarColor;
    private CommentReplyAdapter mAdapter;

    private CircleImageView mCivHeader_mainComment = null;
    private TextView mTvAuther_mainComment = null;
    private TextView mTvCreateTime_mainComment = null;
    private EmojiconTextView mTvMainComment = null;

    private static int mKeyboardState = 0;
    private int mScrollHeight = 0;

    private Handler mPageHandler = null;

    private GroupCommentData mCommentData = null;
    private int mEnterType = 0;
    private int mCreatorId = -999;

    private View mHeaderView = null;
    private View mHeaderDivideLine = null;

    private int mDeleteIndex_temp = -1;

    private UserInfo mUserInfo = null;

    private RelativeLayout.LayoutParams mInputGroupParams = null;

    private List<CommentReplyData> mLstReply = new ArrayList<CommentReplyData>();

    // 临时记录回复内容
    private CommentReplyData mReplyTemp = new CommentReplyData();

    @SuppressLint("NewApi")
    private void cancelObserver(final View root) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    private void controlKeyboardLayout(final View root) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        synchronized (synObj) {
            int tLastKeyboardState = mKeyboardState;
            LogUtil.easylog("onGlobalLayout->mKeyboardState:" + mKeyboardState);
            Rect rect = new Rect();
            // 获取root在窗体的可视区域
            root.getWindowVisibleDisplayFrame(rect);
            // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
            int rootInvisibleHeight = DataModule.SCREEN_HEIGHT - rect.bottom;
            Log.e("sky", "rootInvisibleHeight:" + rootInvisibleHeight);
            // 若不可视区域高度大于100，则键盘显示
            if (rootInvisibleHeight > 100) {
                mKeyboardState = 1;

                LogUtil.easylog("rootInvisibleHeight > 100->mKeyboardState:" + mKeyboardState);
                int[] location = new int[2];
                // 获取scrollToView在窗体的坐标
                mLlInputContent.getLocationInWindow(location);
                // 计算root滚动高度，使scrollToView在可见区域
                int scrollToViewHeight = mLlInputContent.getHeight();
                mScrollHeight = (location[1] + scrollToViewHeight) - rect.bottom;
                LogUtil.easylog("sky", "mScrollHeight:" + mScrollHeight);
            } else {
                mKeyboardState = 0;
                LogUtil.easylog("else mKeyboardState:" + mKeyboardState);
            }

            if (tLastKeyboardState != mKeyboardState && mPageHandler != null) {
                Message msg = mPageHandler.obtainMessage(991);
                mPageHandler.sendMessage(msg);
            }
        }
    }

    @Override
    protected void receiveData(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        if (bundle.containsKey(GroupPage.KEY_COMMENT_DATA)) {
            mCommentData = (GroupCommentData) bundle.getSerializable(GroupPage.KEY_COMMENT_DATA);
        }

        if (bundle.containsKey(GroupPage.KEY_ENTER_TYPE)) {
            mEnterType = bundle.getInt(GroupPage.KEY_ENTER_TYPE);
        }

        if (bundle.containsKey(GroupPage.KEY_TITLEBAR_COLOR)) {
            mTitlebarColor = bundle.getInt(GroupPage.KEY_TITLEBAR_COLOR);
        }

        if (bundle.containsKey(GroupPage.KEY_GROUP_CREATOR_ID)) {
            mCreatorId = bundle.getInt(GroupPage.KEY_GROUP_CREATOR_ID);
        }

    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_comment_reply);

        mUserInfo = getUserInfo();

        root = findViewById(R.id.commentreply_rl_homecontent);

        final LinearLayout mLlInputGroup = (LinearLayout) findViewById(R.id.commentreply_keyboard_input_group);
        mInputGroupParams = (RelativeLayout.LayoutParams) mLlInputGroup.getLayoutParams();
        mLlInputContent = (LinearLayout) findViewById(R.id.ll_input_tool_layout);

        mEmojiKeyboardUtil = new EmojiKeyboardUtil(getActivity(), getContext(), getContentView());
        mEmojiKeyboardUtil.setOnSendKeyListener(new OnSendKeyListener() {
            @Override
            public void gotInputMsg(String inputMsg) {
                LogUtil.easylog("sky", "gotInputMsg:" + inputMsg);
                requestPublishReply(inputMsg);
                closedPageKeyboard();
            }
        });


        mGroupTitleBar = findViewById(R.id.commentreply_title_bar);
        //
        // mGroupTitleBar.setOnClickListener(new OnDoubleClickListener() {
        // @Override
        // public void onDoubleClick(View v) {
        // LogUtil.easylog("mGroupTitlebar->OnDoubleClickListener");
        // if (mListView != null) {
        // mListView.smoothScrollToPosition(0);
        // }
        //
        // }
        // });

        mBackBtn = findViewById(R.id.commentreply_layout_backbtn);

        mListView = (ListView) findViewById(R.id.commentreply_lv_content);

        mHeaderView = View.inflate(getContext(), R.layout.layout_comment_reply_header, null);
        mCivHeader_mainComment = (CircleImageView) mHeaderView.findViewById(R.id.item_head_pic);
        mTvAuther_mainComment = (TextView) mHeaderView.findViewById(R.id.item_author);
        mTvCreateTime_mainComment = (TextView) mHeaderView.findViewById(R.id.item_publish_time);
        mTvMainComment = (EmojiconTextView) mHeaderView.findViewById(R.id.item_emojtv_comment_content);
        mHeaderDivideLine = mHeaderView.findViewById(R.id.item_divide_line);

        mListView.addHeaderView(mHeaderView);

        mHeaderView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enterReplyState(0, 0, 0);
            }
        });

        mAdapter = new CommentReplyAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    CommentReplyData replyData = mLstReply.get(position - 1);
                    if (replyData.getPublisherId() == DataUtils.convertToInt(mUserInfo.getUid()) && mCreatorId != replyData.getPublisherId()) {
                        // 删除对话框
                        openDelDialog(position - 1);
                    } else {
                        enterReplyState(1, position - 1, 1);
                    }
                }

            }
        });

        if (mCreatorId == DataUtils.convertToInt(mUserInfo.getUid())) {
            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        if (mCreatorId == DataUtils.convertToInt(mUserInfo.getUid())) {
                            // 删除对话框
                            openDelDialog(position - 1);
                        }
                    }
                    return true;
                }
            });
        }

        mBackBtn.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                closedPageKeyboard();
                GroupCommentReplyPage.this.finish();
            }
        });


        mPageHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 991:
                        LogUtil.easylog("sky", "keyboard state change:" + mKeyboardState);
                        if (mKeyboardState == 1) {
                            // mLlInputGroup.scrollTo(0, mScrollHeight + 8);

                            mInputGroupParams.setMargins(0, 0, 0, mScrollHeight + 8);
                            mLlInputGroup.setLayoutParams(mInputGroupParams);

                            LogUtil.easylog("sky", "root.scrollTo(0, mScrollHeight);" + mScrollHeight);
                            mEmojiKeyboardUtil.requestInputFocus();
                        } else {
                            // mLlInputGroup.scrollTo(0, 0);
                            mInputGroupParams.setMargins(0, 0, 0, 0);
                            mLlInputGroup.setLayoutParams(mInputGroupParams);
                            if (mEmojiKeyboardUtil.getKeyboardType() != 1) {
                                closedPageKeyboard();
                            }
                            LogUtil.easylog("sky", "root.scrollTo(0, root.scrollTo(0, 0);");
                        }
                        break;
                    default:
                        break;
                }

            };
        };
    }

    @Override
    protected void initData() {
        if (mCommentData != null) {
            Util.loadHeadIcon(mCivHeader_mainComment, mCommentData.getPublisherId() + "", mCommentData.getPublisherHeaderId());

            mGroupTitleBar.setBackgroundColor(mTitlebarColor);
            mTvAuther_mainComment.setText(mCommentData.getPublisherName());
            mTvCreateTime_mainComment.setText(mCommentData.getPublishTime());
            mTvMainComment.setText(mCommentData.getContent());
        }
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        controlKeyboardLayout(root);
        requestCommentReply();

        if (mEnterType == 1) {
            mEnterType = 0;
            enterReplyState(0, 0, 0);
        }
    }

    /**
     * 
     * @param type 0:主评论 1:回复列表
     * @param index 回复列表index
     * @param from 1:src 2:dst
     */
    private void enterReplyState(int type, int index, int from) {
        boolean bLogined = DataModule.getInstance().getUserInfo().isLogined();
        if (!bLogined) {
            showTip("登录后才可回复");
            return;
        }

        if (mEmojiKeyboardUtil.getKeyboardType() == -1) {
            mEmojiKeyboardUtil.openKeyboard();

            mReplyTemp.setBelongGroupCode(mCommentData.getBelongGroupCode());
            mReplyTemp.setPublisherId(DataUtils.convertToInt(mUserInfo.getUid()));
            mReplyTemp.setPublisherName(mUserInfo.getNickName());

            if (type == 0) {
                // mReplyTemp.setDstId(mCommentData.getPublisherId());
                // mReplyTemp.setDstName(mCommentData.getPublisherName());
                mEmojiKeyboardUtil.setInputHint("发表新回复:");
            } else if (type == 1) {
                CommentReplyData tReply = mLstReply.get(index);
                if (from == 1) {
                    mReplyTemp.setDstId(tReply.getPublisherId());
                    mReplyTemp.setDstName(tReply.getPublisherName());
                } else if (from == 2) {
                    mReplyTemp.setDstId(tReply.getDstId());
                    mReplyTemp.setDstName(tReply.getDstName());
                }

                mEmojiKeyboardUtil.setInputHint("回复 " + mReplyTemp.getDstName() + ":");
            }

        }
    }

    public void requestCommentReply() {
        if (mCommentData == null) {
            return;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_REPLY, token, mCommentData.getBelongGroupCode(), mCommentData.getId());
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        LogUtil.easylog("sky", "requestReplyList:" + jObject.toJSONString());
        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_REPLY);
    }

    private void requestPublishReply(String reply) {
        if (reply == null || reply.equals("")) {
            return;
        }
        UserInfo info = getUserInfo();

        mReplyTemp.setContent(reply);
        mReplyTemp.setId(-999);
        mReplyTemp.setPublishTime("");

        String token = DataModule.getInstance().getUserInfo().getToken();

        // "token=%s&code=%d&comment=%s&reply_commentid=%d&dst_usr_id=%d"
        String toWebComment = GroupCommentEmojUtil.localComment2Server(reply);
        try {
            // String encodeContent = URLEncoder.encode(toWebComment,
            // "UTF-8");// URLEncoding
            String encodeContent = Base64.encodeToString(toWebComment.getBytes("utf-8"), Base64.NO_WRAP); // BASE64_Encoding
            // BASE64Encoder base64Encoder = new BASE64Encoder();
            // encodeContent =
            // base64Encoder.encode(toWebComment.getBytes("utf-8"));

            String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_PUBLISH, token, mCommentData.getBelongGroupCode(), encodeContent, mCommentData.getId(), mReplyTemp.getDstId());

            JSONObject jObject = new JSONObject();
            jObject.put(KeysInterface.KEY_URL, reqUrl);
            LogUtil.easylog("sky", "requestReplyPublish:" + jObject.toJSONString());
            requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_PUBLISH);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void requestDeleteComment(int index) {
        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_DELETE, token, mLstReply.get(index).getId());

        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        LogUtil.easylog("sky", "requestReplyDelete:" + jObject.toJSONString());
        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_DELETE);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
        MessageCommon mc = gmp.getResponse();
        if (mc == null || mc.getMsgData() == null) {
            return;
        }

        String msgData = mc.getMsgData();

        if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_REPLY) {
            LogUtil.easylog("sky", "GroupPage->FLAG_GROUP_COMMENT_REPLY" + msgData);

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retServerCode = jsObj.getIntValue("errorCode");
                if (retServerCode == 0) {
                    String body = jsObj.getString("body");
                    if (body != null && !body.equals("")) {
                        JSONObject jObjBody = JSONObject.parseObject(body);
                        int retWebCode = jObjBody.getIntValue("retcode");
                        if (retWebCode == 0) {
                            String replyMsg = jObjBody.getString("message");
                            updateCommentReplyList(replyMsg);
                        }
                    }
                }
            } catch (Exception e) {
            }

        } else if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_PUBLISH) {
            LogUtil.easylog("sky", "GroupPage->FLAG_GROUP_COMMENT_PUBLISH" + msgData);
            updateReplyPublish(msgData);
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_DELETE) {
            LogUtil.easylog("FLAG_GROUP_COMMENT_DELETE:" + msgData);
            updateReplyDelete(msgData);
        }
    }

    private void updateReplyDelete(String replyMsg) {
        try {
            JSONObject jsObj = JSON.parseObject(replyMsg);
            int retServerCode = jsObj.getIntValue("errorCode");
            if (retServerCode == 0) {
                String body = jsObj.getString("body");
                if (body != null && !body.equals("")) {
                    JSONObject jObjBody = JSONObject.parseObject(body);
                    int retWebCode = jObjBody.getIntValue("retcode");
                    String errMsg = jObjBody.getString("retmsg");
                    if (retWebCode == 1) {
                        if (mDeleteIndex_temp >= 0 && mDeleteIndex_temp < mLstReply.size()) {
                            mLstReply.remove(mDeleteIndex_temp);
                        }

                        showTip("删除成功");
                        mAdapter.notifyDataSetChanged();
                    } else {
                        showTip("出错了," + errMsg);
                    }
                }
            } else {
                showTip("出错了,请稍后再试");
            }
        } catch (Exception e) {
            showTip("出错了...");
        }

        mDeleteIndex_temp = -1;
    }

    private void updateReplyPublish(String replyMsg) {
        try {
            JSONObject jsObj = JSON.parseObject(replyMsg);
            int retServerCode = jsObj.getIntValue("errorCode");
            if (retServerCode == 0) {
                String body = jsObj.getString("body");
                if (body != null && !body.equals("")) {
                    JSONObject jObjBody = JSONObject.parseObject(body);
                    int retWebCode = jObjBody.getIntValue("retcode");
                    String errMsg = jObjBody.getString("retmsg");
                    if (retWebCode == 1) {
                        mEmojiKeyboardUtil.clearInput();
                        int tCommentId = jObjBody.getIntValue("idx");
                        String tCreateTimer = jObjBody.getString("create_datetimeshow");

                        mReplyTemp.setId(tCommentId);
                        mReplyTemp.setPublishTime(tCreateTimer);

                        CommentReplyData tData = (CommentReplyData) mReplyTemp.clone();
                        mLstReply.add(tData);
                        mReplyTemp.reset();
                        showTip("回复成功");
                        mAdapter.notifyDataSetChanged();
                        if (mListView != null) {
                            mListView.smoothScrollToPosition(mLstReply.size());
                        }
                    } else {
                        showTip("出错了," + errMsg);
                    }
                }
            } else {
                showTip("出错了,请稍后再试");
            }
        } catch (Exception e) {
            showTip("出错了...");
        }
    }

    private void updateCommentReplyList(String replyMsg) {
        JSONArray jAryReply = JSON.parseArray(replyMsg);
        int tSize = jAryReply.size();
        if (tSize > 0) {
            mLstReply.clear();
            for (int i = 0; i < tSize; i++) {
                JSONObject jObjOneReply = jAryReply.getJSONObject(i);
                CommentReplyData tReplyData = new CommentReplyData();
                tReplyData.setId(jObjOneReply.getIntValue("idx"));
                tReplyData.setBelongGroupCode(jObjOneReply.getIntValue("code"));
                tReplyData.setPublisherId(jObjOneReply.getIntValue("src_usr_id"));

                tReplyData.setPublisherName(jObjOneReply.getString("src_usr_name"));

                tReplyData.setDstId(jObjOneReply.getIntValue("dst_usr_id"));
                tReplyData.setDstName(jObjOneReply.getString("dst_usr_nick"));

                String sWebComment = jObjOneReply.getString("comment");
                String sLocalComment = GroupCommentEmojUtil.serverComment2Local(sWebComment);
                tReplyData.setContent(sLocalComment);

                mLstReply.add(tReplyData);
            }
            mAdapter.notifyDataSetChanged();

        }
    }

    private void openDelDialog(int deleteIndex) {
        mDeleteIndex_temp = deleteIndex;
        ArrayList<ListDialogMenuInfo> sheetItemList = new ArrayList<ListDialogMenuInfo>();
        sheetItemList.add(new ListDialogMenuInfo(0, "删除"));
        DialogUtils.showListDialog(getActivity(), sheetItemList, new OnListDialogItemClickListener() {

            @Override
            public void onItemClicked(int code) {
                // TODO Auto-generated method stub
                if (mDeleteIndex_temp >= 0) {
                    requestDeleteComment(mDeleteIndex_temp);
                }
            }
        });
    }

    public void closedPageKeyboard() {
        if (mEmojiKeyboardUtil != null) {
            mEmojiKeyboardUtil.closeKeyboard();
        }
    }

    @Override
    protected void onPagePause() {
        if (root != null) {
            cancelObserver(root);
        }
        super.onPagePause();
    }

    class CommentReplyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLstReply.size();
        }

        @Override
        public CommentReplyData getItem(int position) {
            return mLstReply.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_commentreply_listitem, null);
                TextView tvSrc = (TextView) convertView.findViewById(R.id.item_comment_src);
                TextView tvTo = (TextView) convertView.findViewById(R.id.item_comment_to);
                TextView tvDst = (TextView) convertView.findViewById(R.id.item_comment_dest);
                TextView tvCommentContent = (TextView) convertView.findViewById(R.id.item_comment_content);
                TextView tvColon = (TextView) convertView.findViewById(R.id.item_comment_colon);
                ListCell lc = new ListCell(tvSrc, tvTo, tvDst, tvCommentContent, tvColon);
                convertView.setTag(lc);
            }

            String tHead = ": ";

            ListCell lc = (ListCell) convertView.getTag();
            lc.setIndexTag(position);

            CommentReplyData replyData = getItem(position);

            String src = "";
            int uid = DataUtils.convertToInt(mUserInfo.getUid());
            if (replyData.getPublisherId() == uid) {
                src = "我";
                lc.getTvSrc().setTextColor(getZDPColor(1));
            } else if (replyData.getPublisherId() == mCreatorId) {
                src = "创建者";
                lc.getTvSrc().setTextColor(getZDPColor(1));
            } else {
                src = replyData.getPublisherName();
                lc.getTvSrc().setTextColor(RColor(R.color.c3));
            }

            tHead += src;

            lc.getTvSrc().setText(src);

            String dst = "";
            if (replyData.getDstId() != 0) {
                if (replyData.getDstId() == uid) {
                    dst = "我";
                    lc.getTvDst().setTextColor(getZDPColor(1));
                } else if (replyData.getDstId() == mCreatorId) {
                    dst = "创建者";
                    lc.getTvDst().setTextColor(getZDPColor(1));
                } else {
                    dst = replyData.getDstName();
                    lc.getTvDst().setTextColor(RColor(R.color.c3));
                }
            }

            // RelativeLayout.LayoutParams lp = new
            // RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            // ViewGroup.LayoutParams.WRAP_CONTENT);

            if (dst != null && !dst.equals("") && !dst.equals(src)) {
                tHead += " 回复 ";
                tHead += dst;
                // lp.addRule(RelativeLayout.RIGHT_OF, R.id.item_comment_dest);
                lc.getTvTo().setVisibility(View.VISIBLE);
                lc.getTvDst().setVisibility(View.VISIBLE);
                lc.getTvDst().setText(dst);
            } else {
                // lp.addRule(RelativeLayout.RIGHT_OF, R.id.item_comment_src);
                lc.getTvTo().setVisibility(View.GONE);
                lc.getTvDst().setVisibility(View.GONE);
            }

            lc.getTvSrc().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int t_index = (Integer) ViewUtil.getViewTag(v);
                    enterReplyState(1, t_index, 1);
                }
            });

            lc.getTvDst().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int t_index = (Integer) ViewUtil.getViewTag(v);
                    enterReplyState(1, t_index, 2);
                }
            });

            // TextView tv = new TextView(getContext());
            // tv.setTextColor(mTxtMain);
            // tv.setTextSize(15);
            // tv.setText(":");

            // RelativeLayout relativeLayout = (RelativeLayout) convertView;
            // relativeLayout.addView(tv, lp);

            Paint pFont = new Paint();
            int txtSize = getContext().getResources().getDimensionPixelSize(R.dimen.txt_s4);
            pFont.setTextSize(txtSize);
            int spaceW = (int) pFont.measureText(tHead);
            LogUtil.easylog(tHead + "width:" + spaceW);

            Drawable tDrawable = getContext().getResources().getDrawable(R.drawable.img_transparent);
            // / 这一步必须要做,否则不会显示.
            tDrawable.setBounds(0, 0, spaceW, 5);

            CharSequence text = replyData.getContent();
            SpannableStringBuilder builder = new SpannableStringBuilder("◆" + text);

            ImageSpan imageSpan = new ImageSpan(tDrawable, ImageSpan.ALIGN_BASELINE);
            builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            lc.getTvCommentContent().setText(builder);

            return convertView;
        }

        private class ListCell {

            public ListCell(TextView tvSrc, TextView tvTo, TextView tvDst, TextView tvCommentContent, TextView tvColon) {
                this.tvSrc = tvSrc;
                this.tvTo = tvTo;
                this.tvDst = tvDst;
                this.tvCommentContent = tvCommentContent;
                this.tvColon = tvColon;
            }

            public TextView getTvSrc() {
                return tvSrc;
            }

            public TextView getTvTo() {
                return tvTo;
            }

            public TextView getTvDst() {
                return tvDst;
            }

            public TextView getTvCommentContent() {
                return tvCommentContent;
            }

            public TextView getTvColon() {
                return tvColon;
            }

            public void setIndexTag(int i) {
                tvSrc.setTag(i);
                tvDst.setTag(i);
            }

            private TextView tvSrc = null;
            private TextView tvTo = null;
            private TextView tvDst = null;
            private TextView tvCommentContent = null;
            private TextView tvColon = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mEmojiKeyboardUtil.getKeyboardType() != -1) {
                mEmojiKeyboardUtil.closeKeyboard();
                return true;
            }

            GroupCommentReplyPage.this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
