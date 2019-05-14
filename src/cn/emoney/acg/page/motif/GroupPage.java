package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.motif.GroupHeaderPage.OnCommentCheckedChangeListener;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.EmojiKeyboardUtil;
import cn.emoney.acg.util.EmojiKeyboardUtil.OnSendKeyListener;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.widget.CircleImageView;
import cn.emoney.sky.fixcmojitv.EmojiconTextView;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

public class GroupPage extends PageImpl implements OnGlobalLayoutListener {

    public static final String KEY_COMMENT_DATA = "key_comment_data";
    /**
     * 1:点击评论内容进去,不自动弹出键盘 2:点击回复进入,自动弹出键盘
     */
    public static final String KEY_ENTER_TYPE = "key_enter_type";
    public static final String KEY_TITLEBAR_COLOR = "key_titlebar_color";
    public static final String KEY_GROUP_CREATOR_ID = "key_group_creator_id";

    private final int COUNT_COMMENT_MAX = 50;

    /**
     * 是否只看创建人与自己的评论
     * */
    private boolean isCreatorOnly;

    private final Object synObj = new Object();

    private View root = null;

    private View mGroupTitleBar = null;
    private View mBackBtn = null;
    private TextView mTvGroupName = null;
    private TextView mTvGroupDetail = null;

    private RelativeLayout mRlControlBar = null;
    private TextView mTvPraiseBtn = null;
    private TextView mTvCommentBtn = null;

    private RefreshListView mListView = null;
    private GroupCommentAdapter mAdapter = null;

    private GroupHeaderPage mLvHeaderPage = null;

    private BuyGroupData mGroupData = null;
    private int mGroupId = 0;

    private List<GroupCommentData> mLstComment = new ArrayList<GroupCommentData>();

    private static int mKeyboardState = 0;
    private int mScrollHeight = 0;

    private RelativeLayout mRootView = null;

    private View mMaskView = null;

    private Handler mPageHandler = null;

    private RelativeLayout.LayoutParams mInputGroupParams = null;
    private LinearLayout mLlInputContent;

    private GroupCommentData mCommentDataSendTemp = new GroupCommentData();
    private int mDeletePosition = -1;

    private EmojiKeyboardUtil mEmojiKeyboardUtil = null;
    private int mColorTitleBar = 0;

    /**
     * 服务器此组合共有多少条评论
     */
    private int mTotalCount = 0;
    private boolean mIsLoadFinish = true;
    private LinearLayout mLlListFooter = null;
    protected boolean mHasMore = true;

    public static interface GroupPageListener {
        public void refreshZDFUI();
    }

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

    public void setBuyGroupData(BuyGroupData data) {
        if (data != null) {
            mGroupData = data;
            mGroupId = mGroupData.getGroupId();
        }
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_group);

        root = findViewById(R.id.grouppage_rl_homecontent);
        final LinearLayout mLlInputGroup = (LinearLayout) findViewById(R.id.pagepgroup_keyboard_input_group);
        mInputGroupParams = (RelativeLayout.LayoutParams) mLlInputGroup.getLayoutParams();
        mLlInputContent = (LinearLayout) findViewById(R.id.ll_input_tool_layout);

        mEmojiKeyboardUtil = new EmojiKeyboardUtil(getActivity(), getContext(), getContentView());
        mEmojiKeyboardUtil.setOnSendKeyListener(new OnSendKeyListener() {
            @Override
            public void gotInputMsg(String inputMsg) {
                LogUtil.easylog("sky", "gotInputMsg:" + inputMsg);
                requestPublishComment(inputMsg);
                closedPageKeyboard();
            }
        });

        mMaskView = findViewById(R.id.pagegroup_mask);

        mMaskView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closedPageKeyboard();
                return true;
            }
        });

        mGroupTitleBar = findViewById(R.id.pagegroup_title_bar);

        mGroupTitleBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListView != null) {
                    mListView.smoothScrollToPosition(0);
                }
            }
        });

        mBackBtn = findViewById(R.id.pagegroup_ll_backbtn);
        mTvGroupName = (TextView) findViewById(R.id.pagegroup_tv_groupname);

        mTvGroupDetail = (TextView) findViewById(R.id.pagegroup_tv_groupdetail);

        mRlControlBar = (RelativeLayout) findViewById(R.id.pagegroup_rl_control_bar);

        mListView = (RefreshListView) findViewById(R.id.pagegroup_lv_content);

        // mListView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        mListView.initWithHeader(R.layout.layout_listview_header);
        mListView.updateTextColor(RColor(R.color.t5));

        mLvHeaderPage = new GroupHeaderPage();

        mLvHeaderPage.needPringLog(true);
        View tHeaderView = mLvHeaderPage.convertToView(this, getActivity().getLayoutInflater(), null, null);
        mLvHeaderPage.setCommentCheckedChangeListener(new OnCommentCheckedChangeListener() {
            @Override
            public void onCheckChanged(final boolean isChecked) {
                isCreatorOnly = isChecked;
                requestGroupComment(0, 5, isCreatorOnly);
            }
        });
        mLlListFooter = (LinearLayout) View.inflate(getContext(), R.layout.page_rankbk_list_loadmore, null);
        mListView.addHeaderView(tHeaderView);
        mListView.addFooterView(mLlListFooter, null, false);

        mTvPraiseBtn = (TextView) findViewById(R.id.pagegroup_tv_bar_praise);
        mTvPraiseBtn.setText("点赞 (" + mGroupData.getPraise() + ")");

        boolean t_bPraised = GroupPraiseModule.getInstance(getContext()).contains(mGroupId);
        mTvPraiseBtn.setEnabled(!t_bPraised);

        mTvPraiseBtn.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                boolean bLogined = DataModule.getInstance().getUserInfo().isLogined();
                if (!bLogined) {
                    showTip("登录后才可点赞哦");
                    return;
                }
                requestPraise();
            }
        });

        mTvCommentBtn = (TextView) findViewById(R.id.pagegroup_tv_bar_comment);
        mTvCommentBtn.setText("评论 (0)");
        mTvCommentBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bLogined = DataModule.getInstance().getUserInfo().isLogined();
                if (!bLogined) {
                    showTip("登录后才可评论哦");
                    return;
                }

                if (mEmojiKeyboardUtil.getKeyboardType() == -1) {
                    mEmojiKeyboardUtil.openKeyboard();
                }
            }
        });

        /**
         * 设置颜色
         */
        setTitleBar();

        mAdapter = new GroupCommentAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshFinished();
                    }
                }, 1000);

                if (mLvHeaderPage != null) {
                    mLvHeaderPage.setGroupId(mGroupData);
                    mLvHeaderPage.onPageResume();
                }

                int tCount = mLstComment.size() > 5 ? mLstComment.size() : 5;
                requestGroupComment(0, tCount, isCreatorOnly);

                mLstComment.clear();
            }

            @Override
            public void beforeRefresh() {
                // mListView.updateRefreshDate("最近更新:" +
                // getDBHelper().getString("refresh_grouppage", "--"));
            }

            @Override
            public void afterRefresh() {
                // getDBHelper().setString("refresh_grouppage", DateUtils.getCurrentQuoteDate());
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = position >= 2 ? position - 2 : 0;
                if (index >= mLstComment.size()) {
                    return;
                }
                GroupCommentData commentData = mLstComment.get(index);
                gotoCommentReply(commentData, 0);
            }
        });

        mListView.removeFooterView(mLlListFooter);

        mListView.setPostScrollListener(new PostScrollListener() {

            @Override
            public void postScrollStateChanged(AbsListView view, int scrollState) {
                LogUtil.easylog("scrollState:" + scrollState);
            }

            @Override
            public void postScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItemid = mListView.getLastVisiblePosition(); // 获取当前屏幕最后Item的ID
                if (totalItemCount >= 6 && totalItemCount < COUNT_COMMENT_MAX && (lastItemid + 1) == totalItemCount && mIsLoadFinish == true && mHasMore) { // 达到数据的最后一条记录且小于最大条数
                    if (mLstComment != null && mLstComment.size() >= 5) {
                        mIsLoadFinish = false;

                        try {
                            if (mLstComment.size() >= 5) {
                                GroupCommentData data = mLstComment.get(mLstComment.size() - 1);
                                mListView.addFooterView(mLlListFooter, null, false);
                                requestGroupComment(data.getId(), 5, isCreatorOnly);
                            }

                        } catch (Exception e) {
                        }

                    }
                }
            }
        });

        mBackBtn.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                closedPageKeyboard();
                getModule().finish();
                getModule().overridePendingTransition(0, 0);
            }
        });

        mPageHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 991:
                        LogUtil.easylog("sky", "keyboard state change:" + mKeyboardState);
                        if (mKeyboardState == 1) {
                            // root.scrollTo(0, mScrollHeight + 8);
                            mInputGroupParams.setMargins(0, 0, 0, mScrollHeight + 8);
                            mLlInputGroup.setLayoutParams(mInputGroupParams);
                            LogUtil.easylog("sky", "root.scrollTo(0, mScrollHeight);" + mScrollHeight);
                            mEmojiKeyboardUtil.requestInputFocus();
                        } else {
                            // root.scrollTo(0, 0);
                            mInputGroupParams.setMargins(0, 0, 0, 0);
                            mLlInputGroup.setLayoutParams(mInputGroupParams);
                            if (mEmojiKeyboardUtil.getKeyboardType() != 1) {
                                closedPageKeyboard();
                            }
                            LogUtil.easylog("sky", "root.scrollTo(0, root.scrollTo(0, 0);");
                        }

                        if (mEmojiKeyboardUtil.getKeyboardType() != -1) {
                            mMaskView.setVisibility(View.VISIBLE);
                        } else {
                            mEmojiKeyboardUtil.clearInputFocus();
                            mMaskView.setVisibility(View.GONE);
                        }
                        break;

                    default:
                        break;
                }

            };
        };

        GroupPageListener tListener = new GroupPageListener() {
            @Override
            public void refreshZDFUI() {
                setTitleBar();
            }
        };
        mLvHeaderPage.setGroupPageListener(tListener);

    }

    public void closedPageKeyboard() {
        if (mEmojiKeyboardUtil != null) {
            mEmojiKeyboardUtil.closeKeyboard();
        }
        if (mMaskView != null) {
            mMaskView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        controlKeyboardLayout(root);

        if (mLvHeaderPage != null) {
            mLvHeaderPage.setGroupId(mGroupData);
            mLvHeaderPage.onPageResume();
        }

        int tCount = mLstComment.size() > 5 ? mLstComment.size() : 5;
        requestGroupComment(0, tCount, isCreatorOnly);

        mLstComment.clear();
    }

    private void requestDeleteComment(int position) {
        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_DELETE, token, mLstComment.get(position).getId());

        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        LogUtil.easylog("sky", "requestDeleteComment:" + jObject.toJSONString());
        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_DELETE);
    }

    private void requestPraise() {
        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_PRAISE, token, mGroupId);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);
        jObject.put(KeysInterface.KEY_ID, mGroupId);

        LogUtil.easylog("sky", "requestPraise:" + jObject.toJSONString());
        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_GROUP_PRAISE, BuyClubHttpUrl.FLAG_URL_PRAISE);
    }

    private void requestGroupComment(int start, int size, boolean isCreatorOnly) {
        String token = DataModule.getInstance().getUserInfo().getToken();

        int type = isCreatorOnly ? 1 : 0;

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_LIST, token, mGroupId, start, size, type);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        LogUtil.easylog("sky", "requestGroupCommentList:" + jObject.toJSONString());
        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_LIST);
    }

    private void requestPublishComment(String comment) {
        if (comment == null || comment.equals("")) {
            return;
        }
        UserInfo info = getUserInfo();

        mCommentDataSendTemp.reset();
        mCommentDataSendTemp.setBelongGroupCode(mGroupId - 22000000);
        mCommentDataSendTemp.setContent(comment);
        mCommentDataSendTemp.setId(-999);
        mCommentDataSendTemp.setPublisherHeaderId(info.getHeadId());
        mCommentDataSendTemp.setPublisherId(DataUtils.convertToInt(info.getUid()));
        mCommentDataSendTemp.setPublisherName(info.getNickName());
        mCommentDataSendTemp.setPublishTime("");
        mCommentDataSendTemp.setReplyCount(0);

        String token = DataModule.getInstance().getUserInfo().getToken();
        // "token=%s&code=%d&comment=%s&reply_commentid=%d&dst_usr_id=%d"
        String toWebComment = GroupCommentEmojUtil.localComment2Server(comment);

        try {
            // String encodeContent = URLEncoder.encode(toWebComment,
            // "UTF-8");// URLEncoding
            // String encodeContent = Base64.encodeToString(toWebComment.getBytes("utf-8"),
            // Base64.NO_WRAP); // BASE64_Encoding

            String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_COMMENT_PUBLISH, token, mGroupId, toWebComment, 0, 0);
            JSONObject jObject = new JSONObject();
            jObject.put(KeysInterface.KEY_URL, reqUrl);
            LogUtil.easylog("sky", "requestGroupCommentPublish:" + jObject.toJSONString());
            requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_COMMENT_PUBLISH);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        if (id == BuyClubHttpUrl.FLAG_URL_PRAISE) {
            LogUtil.easylog("sky", "GroupPage->updateInfo:FLAG_URL_PRAISE" + msgData);
            updatePraiseInfo(msgData);
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_LIST) {
            LogUtil.easylog("sky", "GroupPage->FLAG_GROUP_COMMENT_LIST" + msgData);
            if (mListView != null && mLlListFooter != null) {
                mListView.removeFooterView(mLlListFooter);
            }

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retServerCode = jsObj.getIntValue("errorCode");
                if (retServerCode == 0) {
                    String body = jsObj.getString("body");
                    if (body != null && !body.equals("")) {
                        JSONObject jObjBody = JSONObject.parseObject(body);
                        int retWebCode = jObjBody.getIntValue("retcode");
                        mTotalCount = jObjBody.getIntValue("totalcount");
                        if (retWebCode == 0) {
                            String commentMsg = jObjBody.getString("message");

                            JSONArray jAryComment = JSON.parseArray(commentMsg);
                            int tSize = jAryComment.size();
                            if (tSize > 0) {
                                List<GroupCommentData> tListComment = new ArrayList<GroupCommentData>();
                                for (int i = 0; i < tSize; i++) {
                                    JSONObject jObjOneComment = jAryComment.getJSONObject(i);
                                    GroupCommentData tCommentData = new GroupCommentData();
                                    tCommentData.setId(jObjOneComment.getIntValue("idx"));
                                    tCommentData.setBelongGroupCode(jObjOneComment.getIntValue("code"));
                                    tCommentData.setPublisherId(jObjOneComment.getIntValue("src_usr_id"));
                                    tCommentData.setPublisherName(jObjOneComment.getString("src_usr_name"));
                                    tCommentData.setPublisherHeaderId(jObjOneComment.getString("src_usr_pic"));
                                    tCommentData.setReplyCount(jObjOneComment.getIntValue("reply_count"));
                                    tCommentData.setPublishTime(jObjOneComment.getString("create_datetimeshow"));

                                    String sWebComment = jObjOneComment.getString("comment");
                                    String sLocalComment = GroupCommentEmojUtil.serverComment2Local(sWebComment);
                                    tCommentData.setContent(sLocalComment);

                                    tListComment.add(tCommentData);
                                }

                                mLstComment.addAll(tListComment);

                                mIsLoadFinish = true;
                                notifyCommentLvReload();
                                return;
                            }

                        }
                    }
                }
            } catch (Exception e) {
            }
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_PUBLISH) {
            LogUtil.easylog("sky", "GroupPage->FLAG_GROUP_COMMENT_PUBLISH" + msgData);
            updateCommentPublish(msgData);
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_COMMENT_DELETE) {
            LogUtil.easylog("FLAG_GROUP_COMMENT_DELETE:" + msgData);
            updateCommentDelete(msgData);
        }
    }

    private void updateCommentDelete(String replyMsg) {
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
                        if (mDeletePosition >= 0 && mDeletePosition < mLstComment.size()) {
                            mLstComment.remove(mDeletePosition);
                        }

                        showTip("删除成功");
                        notifyCommentLvReload();
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

    private void updateCommentPublish(String replyMsg) {
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

                        mCommentDataSendTemp.setId(tCommentId);
                        mCommentDataSendTemp.setPublishTime(tCreateTimer);

                        GroupCommentData tData = (GroupCommentData) mCommentDataSendTemp.clone();
                        mLstComment.add(0, tData);
                        mCommentDataSendTemp.reset();
                        showTip("添加评论成功");

                        mTotalCount++;

                        notifyCommentLvReload();
                        if (mListView != null) {
                            mListView.smoothScrollToPosition(2);
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

    private void updatePraiseInfo(String replyMsg) {
        try {
            JSONObject jsObj = JSON.parseObject(replyMsg);
            int retServerCode = jsObj.getIntValue("errorCode");
            if (retServerCode == 0) {
                String body = jsObj.getString("body");
                if (body != null && !body.equals("")) {
                    JSONObject jObjBody = JSONObject.parseObject(body);
                    int retWebCode = jObjBody.getIntValue("retcode");
                    if (retWebCode == 1) {
                        mTvPraiseBtn.setEnabled(false);

                        long tPraiseCount = jObjBody.getLong("praisecount");
                        mGroupData.setPraise(tPraiseCount);
                        GroupPraiseModule.getInstance(getContext()).addPraise(mGroupId);

                        mTvPraiseBtn.setText("点赞 (" + tPraiseCount + ")");
                        mTvPraiseBtn.setEnabled(false);
                        Toast toast = Toast.makeText(getContext(), "+1", Toast.LENGTH_SHORT);
                        // toast.setGravity(Gravity.CENTER, 0, 0);
                        LinearLayout toastView = (LinearLayout) toast.getView();
                        toastView.setOrientation(LinearLayout.HORIZONTAL);
                        toastView.setGravity(Gravity.CENTER_VERTICAL);
                        ImageView imageCodeProject = new ImageView(getContext());
                        imageCodeProject.setPadding(30, 0, 30, 0);
                        imageCodeProject.setImageResource(R.drawable.img_heart_praise);
                        toastView.addView(imageCodeProject, 0);
                        toast.show();
                    } else if (retWebCode == -1) {
                        GroupPraiseModule.getInstance(getContext()).addPraise(mGroupId);
                        mTvPraiseBtn.setEnabled(false);
                        showTip("已赞过该组合");
                    } else {
                        showTip("出错了");
                    }
                } else {
                    showTip("出错了,稍后再试");
                }
            } else {
                showTip("出错了,稍后再试");
            }
        } catch (Exception e) {
            showTip("出错了,稍后再试");
        }
    }

    public void restoreListViewRefresh() {
        if (mListView != null) {
            mListView.onRefreshFinished();
        }
    }

    public void setTitleBar() {
        if (mGroupTitleBar != null) {
            int colorFlag = 0;
            if (mGroupData != null) {
                colorFlag = FontUtils.getColorByZDF_percent(mGroupData.getTotalZDF());
            }

            mColorTitleBar = getBgGroupHeader(colorFlag);
            if (mGroupTitleBar != null) {
                mGroupTitleBar.setBackgroundColor(mColorTitleBar);
            }

            if (mLvHeaderPage != null) {
                mLvHeaderPage.setHeaderViewBgColor(mColorTitleBar);
            }

            if (mListView != null) {
                mListView.updateRefreshBgColor(mColorTitleBar);
            }

            if (mTvGroupName != null) {
                mTvGroupName.setText(mGroupData.getGroupName());
            }
            if (mTvGroupDetail != null) {
                mTvGroupDetail.setText(mGroupData.getCreator() + " " + mGroupData.getCreateTime());
            }

        }
    }

    private int getBgGroupHeader(int type) {
        int bgGroupHeader_rise = getContext().getResources().getColor(R.color.c1);
        int bgGroupHeader_fall = getContext().getResources().getColor(R.color.light_bg_groupheader_fall);

        if (type == -1) {
            return bgGroupHeader_fall;
        }

        return bgGroupHeader_rise;
    }

    @Override
    protected void onPagePause() {
        cancelObserver(root);
        if (mLvHeaderPage != null) {
            mLvHeaderPage.onPagePause();
        }
        super.onPagePause();
    }

    @Override
    public void onStop() {
        if (mLvHeaderPage != null) {
            mLvHeaderPage.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onPageResult(int requestCode, int resultCode, Bundle data) {
        super.onPageResult(requestCode, resultCode, data);

        if (requestCode == GroupHeaderPage.REQUEST_CODE_MODIFY_IDEA && resultCode == RESULT_CODE && data != null) {
            String idea = data.getString("idea");
            mGroupData.setGroupIdea(idea);
        }
    }

    @Override
    protected void onPageDestroy() {
        if (mLvHeaderPage != null) {
            mLvHeaderPage.onPageDestroy();
        }
        super.onPageDestroy();
    }

    private void notifyCommentLvReload() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        if (mLstComment.size() >= mTotalCount || mLstComment.size() >= COUNT_COMMENT_MAX) {
            mHasMore = false;
        } else {
            mHasMore = true;
        }

        if (mTvCommentBtn != null) {
            mTvCommentBtn.setText("评论 (" + mTotalCount + ")");
        }
    }

    private void gotoCommentReply(GroupCommentData commentData, int enterType) {
        PageIntent intent = new PageIntent(this, GroupCommentReplyPage.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_COMMENT_DATA, commentData);
        bundle.putInt(KEY_ENTER_TYPE, enterType);
        bundle.putInt(KEY_TITLEBAR_COLOR, mColorTitleBar);
        bundle.putInt(KEY_GROUP_CREATOR_ID, mGroupData.getCreatorId());
        intent.setArguments(bundle);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    class GroupCommentAdapter extends BaseAdapter {

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            int tSize = mLstComment.size();
            if (tSize == 0) {
                return 1;
            }
            return mLstComment.size();
        }

        @Override
        public GroupCommentData getItem(int position) {
            if (mLstComment.size() > position) {
                return mLstComment.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LogUtil.easylog("GroupPage->GroupNoticeAdapter->getView->position:" + position);
            if (mLstComment.size() > 0) {
                boolean bNeedInflate = false;
                if (convertView == null) {
                    bNeedInflate = true;
                } else {
                    try {
                        String tag = (String) convertView.getTag();
                        if (tag != null && tag.equals("Empty")) {
                            bNeedInflate = true;
                        }
                    } catch (Exception e) {

                    }
                }

                if (bNeedInflate) {
                    convertView = View.inflate(getContext(), R.layout.layout_groupcomment_listitem, null);
                    convertView.setTag("Normal");

                    CircleImageView civAuthorHeader = (CircleImageView) convertView.findViewById(R.id.item_head_pic);
                    TextView tvAuthor = (TextView) convertView.findViewById(R.id.item_author);
                    TextView tvTime = (TextView) convertView.findViewById(R.id.item_publish_time);
                    EmojiconTextView commentContent = (EmojiconTextView) convertView.findViewById(R.id.item_emojtv_comment_content);
                    commentContent.setUseSystemDefault(false);
                    TextView tvDelete = (TextView) convertView.findViewById(R.id.item_tv_delete);
                    TextView tvReply = (TextView) convertView.findViewById(R.id.item_tv_reply);
                    View vDivide = convertView.findViewById(R.id.item_divide_line);
                    ListCell lc = new ListCell(civAuthorHeader, tvAuthor, tvTime, commentContent, tvDelete, tvReply, vDivide);
                    convertView.setTag(lc);
                }
                ListCell lc = (ListCell) convertView.getTag();
                lc.setIndexTag(position);
                GroupCommentData tCommentData = getItem(position);
                UserInfo userInfo = getUserInfo();

                Util.loadHeadIcon(lc.mCivAuthorHeader, tCommentData.getPublisherId() + "", tCommentData.getPublisherHeaderId());

                if (("" + tCommentData.getPublisherId()).equals(userInfo.getUid())) {
                    lc.mTvAuthor.setText("我");
                    lc.mTvAuthor.setTextColor(getZDPColor(1));
                } else if (tCommentData.getPublisherId() == mGroupData.getCreatorId()) {
                    lc.mTvAuthor.setText("创建者");
                    lc.mTvAuthor.setTextColor(getZDPColor(1));
                } else {
                    lc.mTvAuthor.setText(tCommentData.getPublisherName());
                    lc.mTvAuthor.setTextColor(RColor(R.color.t1));
                }

                lc.mTvTime.setText(tCommentData.getPublishTime());
                lc.mCommentContent.setText(tCommentData.getContent());

                boolean bDeleteDisplay = false;
                int type = MineGroupModule.getInstance().getMineType(mGroupId);
                if (type == MineGroupModule.MINE_TYPE_CREATE) {
                    lc.mTvDelete.setVisibility(View.VISIBLE);
                    bDeleteDisplay = true;
                } else if (tCommentData.getPublisherId() == DataUtils.convertToInt(DataModule.getInstance().getUserInfo().getUid())) {
                    lc.mTvDelete.setVisibility(View.VISIBLE);
                    bDeleteDisplay = true;
                } else {
                    lc.mTvDelete.setVisibility(View.INVISIBLE);
                }

                if (bDeleteDisplay) {
                    lc.mTvDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDeletePosition = (Integer) v.getTag();
                            // requestDeleteComment(mDeletePosition);
                            showDelDialog();
                        }
                    });
                }

                lc.mTvReply.setText("回复 (" + tCommentData.getReplyCount() + ")");
                lc.mTvReply.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (Integer) v.getTag();
                        GroupCommentData commentData = mLstComment.get(index);
                        gotoCommentReply(commentData, 1);
                    }
                });

            } else {
                convertView = View.inflate(getContext(), R.layout.page_listitem_empty, null);
                convertView.setTag("Empty");
                TextView tvEmpty = (TextView) convertView.findViewById(R.id.item_tv_empty_notice);
                tvEmpty.setText("暂无评论");
            }

            return convertView;
        }

        class ListCell {
            public ListCell(CircleImageView civAuthorHeader, TextView tvAuthor, TextView tvTime, EmojiconTextView commentContent, TextView tvDelete, TextView tvReply, View vDivide) {
                mCivAuthorHeader = civAuthorHeader;
                mTvAuthor = tvAuthor;
                mTvTime = tvTime;
                mCommentContent = commentContent;
                mTvDelete = tvDelete;
                mTvReply = tvReply;
                mVDivide = vDivide;
            }

            public void setIndexTag(int i) {
                mTvDelete.setTag(i);
                mTvReply.setTag(i);
            }

            public CircleImageView mCivAuthorHeader = null;
            public TextView mTvAuthor = null;
            public TextView mTvTime = null;
            public EmojiconTextView mCommentContent = null;
            public TextView mTvDelete = null;
            public TextView mTvReply = null;
            public View mVDivide = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mEmojiKeyboardUtil.getKeyboardType() != -1) {
                mEmojiKeyboardUtil.closeKeyboard();
                return true;
            }
            getModule().finish();
            getModule().overridePendingTransition(0, 0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 删除的对话框
    private void showDelDialog() {
        DialogUtils.showMessageDialog(getActivity(), "提示", "是否删除当前评论？", "确定", "取消", new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {
                requestDeleteComment(mDeletePosition);
            }

            @Override
            public void onCancelBtnClicked() {}
        });
    }
}
