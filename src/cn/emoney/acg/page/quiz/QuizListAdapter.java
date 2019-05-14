package cn.emoney.acg.page.quiz;

import java.util.ArrayList;

import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.data.quiz.QuizItemInfo;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.motif.BuyGroupData;
import cn.emoney.acg.page.motif.MineGroupModule;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.VolleyHelper;
import cn.emoney.acg.util.textviewlink.LinkManager;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

/**
 * 问股的列表的适配器
 * 
 * @ClassName: QuizListAdapter
 * @Description:
 * @author xiechengfa
 * @date 2015年12月7日 上午11:29:22
 *
 */
public class QuizListAdapter extends BaseAdapter {
    private final int LIMIT_LINE_COUNT = 3;
    private long currCountDownQuizId = 0;
    private PageImpl page;
    private TextView countDownTextView = null;
    private ArrayList<QuizItemInfo> listDatas = null;
    private QuizListViewlListener listener = null;
    private CountDownTimer mCountDownTimer = null;

    public QuizListAdapter(PageImpl page, QuizListViewlListener listener) {
        this.page = page;
        this.listener = listener;
    }

    public void setData(ArrayList<QuizItemInfo> listDatas) {
        this.listDatas = listDatas;
    }

    @Override
    public int getCount() {
        if (listDatas == null) {
            return 0;
        }

        return listDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return listDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return listDatas.get(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        int viewType = getItemViewType(position);
        if (viewType == QuizItemInfo.TYPE_QUESTION) {
            // 动态
            StatusViewHolder vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ACGApplication.getInstance()).inflate(R.layout.page_quizhome_list_item, root, false);

                vh = new StatusViewHolder();
                // 问
                vh.askLayout = (RelativeLayout) convertView.findViewById(R.id.askLayout);
                vh.askNameView = (TextView) convertView.findViewById(R.id.askNameView);
                vh.askDateView = (TextView) convertView.findViewById(R.id.askDateView);
                vh.askcontentView = (TextView) convertView.findViewById(R.id.askcontentView);

                // 回复
                vh.replyLayout = convertView.findViewById(R.id.replyLayout);
                vh.headIV = (ImageView) convertView.findViewById(R.id.headIV);
                vh.replyNameView = (TextView) convertView.findViewById(R.id.replyNameView);
                vh.replyDateView = (TextView) convertView.findViewById(R.id.replyDateView);
                vh.levLayout = convertView.findViewById(R.id.levLayout);
                vh.levViewArr = new ImageView[4];
                vh.levViewArr[0] = (ImageView) convertView.findViewById(R.id.levView1);
                vh.levViewArr[1] = (ImageView) convertView.findViewById(R.id.levView2);
                vh.levViewArr[2] = (ImageView) convertView.findViewById(R.id.levView3);
                vh.levViewArr[3] = (ImageView) convertView.findViewById(R.id.levView4);

                // 回复内容
                vh.replycontentView = (TextView) convertView.findViewById(R.id.replycontentView);

                // 语音
                vh.voiceLayout = convertView.findViewById(R.id.voiceLayout);
                vh.voiceBgLayout = (LinearLayout) convertView.findViewById(R.id.voiceBgLayout);
                vh.voiceView = (ImageView) convertView.findViewById(R.id.voiceView);
                vh.voidTimeView = (TextView) convertView.findViewById(R.id.voidTimeView);

                // 等待或关闭的状态
                vh.replyStateView = (TextView) convertView.findViewById(R.id.replyStateView);
                // 正在回复
                vh.replyOnStateLayout = convertView.findViewById(R.id.replyOnStateLayout);
                vh.rePlyingHeadIV = (ImageView) convertView.findViewById(R.id.rePlyingHeadIV);
                vh.replyingNameView = (TextView) convertView.findViewById(R.id.replyingNameView);
                // 回复成功，评价
                vh.commentStateLayout = convertView.findViewById(R.id.commentStateLayout);
                vh.goodView = convertView.findViewById(R.id.goodView);
                vh.wellView = convertView.findViewById(R.id.wellView);
                vh.normalView = convertView.findViewById(R.id.normalView);
                vh.badView = convertView.findViewById(R.id.badView);

                // 分隔线
                vh.dividerLayout = convertView.findViewById(R.id.dividerLayout);

                convertView.setTag(vh);
            } else {
                vh = (StatusViewHolder) convertView.getTag();
            }

            // 初始View
            initStatusView(vh, listDatas.get(position), position);

            // 分隔线
            if (position + 1 < getCount() && listDatas.get(position + 1).getType() == QuizItemInfo.TYPE_STRING || position == getCount() - 1) {
                vh.dividerLayout.setVisibility(View.GONE);
            } else {
                vh.dividerLayout.setVisibility(View.VISIBLE);
            }
        } else if (viewType == QuizItemInfo.TYPE_STRING) {
            StringViewHolder vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ACGApplication.getInstance()).inflate(R.layout.page_quizhome_list_string_item, root, false);
                vh = new StringViewHolder();
                vh.tipView = (TextView) convertView.findViewById(R.id.tipView);
                convertView.setTag(vh);
            } else {
                vh = (StringViewHolder) convertView.getTag();
            }

            vh.tipView.setText(listDatas.get(position).getTipStr());
        } else if (viewType == QuizItemInfo.TYPE_GROUP) {
            // 组合
            GroupViewHolder vh = null;
            if (convertView == null) {
                vh = new GroupViewHolder();
                convertView = View.inflate(ACGApplication.getInstance(), R.layout.layout_teacher_group_list_item, null);
                vh.mNivIamge = (NetworkImageView) convertView.findViewById(R.id.buyclub_home_lvitem_niv_image);
                vh.mTvGroupName = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_groupname);
                vh.mTvTotalZDF = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_totalZDF);
                vh.mTvDayZDF = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_dayZDF);
                vh.mTvCreateTime = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_createTime);
                vh.mTvFocus = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_focus);
                vh.mTvTypeTotal = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_totalType);
                vh.mTvTypeDay = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_dayType);
                vh.mIvRecommondFlag = (ImageView) convertView.findViewById(R.id.buyclub_home_lvitem_iv_flag);
                vh.lineView = convertView.findViewById(R.id.buyclub_home_lvitem_line);

                convertView.setTag(vh);
            } else {
                vh = (GroupViewHolder) convertView.getTag();
            }

            // 初始View
            initGroupView(vh, listDatas.get(position), position);

            if (position + 1 < getCount() && listDatas.get(position + 1).getType() == QuizItemInfo.TYPE_STRING) {
                vh.lineView.setVisibility(View.GONE);
            } else {
                vh.lineView.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    // 初始View
    private void initStatusView(final StatusViewHolder view, final QuizItemInfo info, final int pos) {
        // 问
        if (info.getQuizItem() != null) {
            int left = view.askLayout.getPaddingLeft();
            int right = view.askLayout.getPaddingRight();
            int top = view.askLayout.getPaddingTop();
            int bottom = view.askLayout.getPaddingBottom();

            if (info.getQuizItem().isMyQuestion()) {
                // 我的提问
                view.askLayout.setBackgroundResource(R.drawable.img_quiz_txt_self_bg);
                view.askNameView.setText(Util.getResourcesString(R.string.quiz_my_question));
            } else {
                // 别人的提问
                view.askLayout.setBackgroundResource(R.drawable.img_quiz_txt_other_bg);
                view.askNameView.setText(info.getQuizItem().getOwner().getNickName());
            }
            view.askLayout.setPadding(left, top, right, bottom);

            view.askDateView.setText(DateUtils.formatQuizCommitTime(info.getQuizItem().getCommitTime()));
            view.askcontentView.setText(info.getQuizItem().getContent());
            // 加事件
            LinkManager.addStockLinkToTv(page, view.askcontentView);

            // 答
            if ((info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_APPRAISED || info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE) && info.getQuizItem().getReplier() != null && info.getQuizItem().getAnswer() != null) {
                // 回复
                // 回复内容
                view.replyLayout.setVisibility(View.VISIBLE);
                // 等待和无人回复
                view.replyStateView.setVisibility(View.GONE);
                // 正在回复
                view.replyOnStateLayout.setVisibility(View.GONE);
                // 评价
                view.commentStateLayout.setVisibility(View.GONE);

                Util.loadHeadIcon(view.headIV, info.getQuizItem().getReplier().getId() + "", info.getQuizItem().getReplier().getIcon());
                view.headIV.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (listener != null) {
                            listener.onClickHeadIcon(pos);
                        }
                    }
                });

                view.replyNameView.setText(info.getQuizItem().getReplier().getNick());
                view.replyDateView.setText(DateUtils.formatQuizCommitTime(info.getQuizItem().getAnswerTime()));

                // 等级
                if (info.getQuizItem().isMyLatestQuestion() && info.getQuizItem().getStatus() != QuizContentInfo.STATUS_ASK_APPRAISED) {
                    view.levLayout.setVisibility(View.GONE);
                } else {
                    view.levLayout.setVisibility(View.VISIBLE);
                    if (view.levViewArr != null) {
                        for (int i = 0; i < view.levViewArr.length; i++) {
                            if (i < info.getQuizItem().getAppraiseLevel() && info.getQuizItem().getAppraiseLevel() > 1) {
                                view.levViewArr[i].setSelected(true);
                            } else {
                                view.levViewArr[i].setSelected(false);
                            }
                        }
                    }
                }

                // 内容
                if (info.getQuizItem().getAnswer().getType() == QuizContentInfo.CONTENT_TYPE_TEXT) {
                    // 文本
                    view.voiceLayout.setVisibility(View.GONE);
                    view.replycontentView.setVisibility(View.VISIBLE);
                    view.replycontentView.setText(info.getQuizItem().getAnswer().getContent());

                    if (!info.getQuizItem().isReplyExpand()) {
                        // 收起
                        initContentEllipsize(view.replycontentView);
                    }

                    view.replycontentView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (info.getQuizItem().isReplyExpand()) {
                                // 原来展开，则收起
                                listDatas.get(pos).getQuizItem().setReplyExpand(false);
                                initContentEllipsize(view.replycontentView);
                            } else {
                                // 原来收起，则展开
                                listDatas.get(pos).getQuizItem().setReplyExpand(true);
                                view.replycontentView.setText(info.getQuizItem().getAnswer().getContent());
                            }
                        }
                    });

                    // 加事件
                    LinkManager.addStockLinkToTv(page, view.replycontentView);
                } else {
                    // 语音
                    view.voiceLayout.setVisibility(View.VISIBLE);
                    view.replycontentView.setVisibility(View.GONE);
                    view.voidTimeView.setText(info.getQuizItem().getAnswer().getVoiceTime());

                    if (info.getQuizItem().isPlaying()) {
                        // 播放
                        view.voiceView.setImageResource(R.drawable.anim_quiz_voice);
                        AnimationDrawable animationDrawable = (AnimationDrawable) view.voiceView.getDrawable();
                        animationDrawable.start();
                    } else {
                        view.voiceView.setImageResource(R.drawable.img_voice3);
                    }

                    if (info.getQuizItem().isDowning()) {
                        // 正在下载，不要点击
                        view.voiceBgLayout.setClickable(false);
                        view.voiceBgLayout.setSelected(true);
                    } else {
                        // 可点击
                        view.voiceBgLayout.setClickable(true);
                        view.voiceBgLayout.setSelected(false);

                        view.voiceBgLayout.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                if (listener != null) {
                                    listener.onPlayVoice(pos);
                                }
                            }
                        });
                    }
                }

                // 是否评价
                if (info.getQuizItem().isMyLatestQuestion() && info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_REPLED_TO_APPRAISE) {
                    // 我的提问，且要评价
                    view.commentStateLayout.setVisibility(View.VISIBLE);
                    view.goodView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (listener != null) {
                                listener.onAppraise(info.getQuizItem().getId(), QuizListViewlListener.COMMENT_GOOD);
                            }
                        }
                    });

                    view.wellView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (listener != null) {
                                listener.onAppraise(info.getQuizItem().getId(), QuizListViewlListener.COMMENT_WELL);
                            }
                        }
                    });

                    view.normalView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (listener != null) {
                                listener.onAppraise(info.getQuizItem().getId(), QuizListViewlListener.COMMENT_NORAML);
                            }
                        }
                    });

                    view.badView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (listener != null) {
                                listener.onAppraise(info.getQuizItem().getId(), QuizListViewlListener.COMMENT_BAD);
                            }
                        }
                    });
                } else {
                    view.commentStateLayout.setVisibility(View.GONE);
                }
            } else {
                // 无回复
                // 回复内容
                view.replyLayout.setVisibility(View.GONE);
                // 评价
                view.commentStateLayout.setVisibility(View.GONE);

                // 等待和无人回复
                if (info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_WAIT || info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_WAIT2) {
                    // 等待
                    view.replyStateView.setVisibility(View.VISIBLE);
                    // 正在回复
                    view.replyOnStateLayout.setVisibility(View.GONE);
                    view.replyStateView.setTextColor(Util.getResourcesColor(R.color.c3));
                    view.replyStateView.setText(Util.getResourcesString(R.string.quiz_status_wait));
                    setCountDownTimer(view.replyStateView, info.getQuizItem().getCommitTime(), info.getQuizItem().getId(), pos);
                } else if (info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_CLOSE) {
                    // 关闭
                    view.replyStateView.setVisibility(View.VISIBLE);
                    // 正在回复
                    view.replyOnStateLayout.setVisibility(View.GONE);
                    view.replyStateView.setTextColor(Util.getResourcesColor(R.color.t4));
                    view.replyStateView.setText(R.string.quiz_status_close);
                } else if (info.getQuizItem().getStatus() == QuizContentInfo.STATUS_ASK_ON) {
                    // 正在回复
                    view.replyStateView.setVisibility(View.GONE);
                    view.replyOnStateLayout.setVisibility(View.VISIBLE);
                    Util.loadHeadIcon(view.rePlyingHeadIV, info.getQuizItem().getReplier().getId() + "", info.getQuizItem().getReplier().getIcon());
                    view.replyingNameView.setText(info.getQuizItem().getReplier().getNick());
                }
            }
        }
    }

    // 长文本收缩
    private void initContentEllipsize(TextView textView) {
        if (textView.getLineCount() > LIMIT_LINE_COUNT) {
            int lineEndIndex = textView.getLayout().getLineEnd(LIMIT_LINE_COUNT - 1);
            String text = textView.getText().subSequence(0, lineEndIndex - LIMIT_LINE_COUNT) + "...";
            textView.setText(text);
        }
    }

    // 设置倒数时间
    private void setCountDownTimer(final TextView tv, int createTime, long quizId, final int pos) {
        countDownTextView = tv;
        long tRemainTime = QuizConfigData.getInstance().getQustionLifeTime() - (DateUtils.getTimeStamp() / 1000 - DataModule.G_LOCAL_SERVER_TIME_GAP - createTime);

        if (currCountDownQuizId != quizId) {
            recycleCountDownTimer();
        }
        currCountDownQuizId = quizId;

        if (tRemainTime > 0) {
            tv.setText(Util.getResourcesString(R.string.quiz_status_wait) + DateUtils.second2MSLable(tRemainTime));
            if (mCountDownTimer == null) {
                mCountDownTimer = new CountDownTimer(tRemainTime * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateCountDownTimerTick(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        oupdateCountDowmTimerFinish(pos);
                    }
                }.start();
            }
        } else {
            oupdateCountDowmTimerFinish(pos);
        }
    }

    // 倒数结束
    private void oupdateCountDowmTimerFinish(int pos) {
        if (listener != null) {
            listener.onQuestionClose(pos);
        }

        if (countDownTextView != null) {
            countDownTextView.setTextColor(Util.getResourcesColor(R.color.t4));
            countDownTextView.setText(R.string.quiz_status_close);
        }
    }

    // 更新倒数时间
    private void updateCountDownTimerTick(long millisUntilFinished) {
        if (countDownTextView != null) {
            countDownTextView.setText(Util.getResourcesString(R.string.quiz_status_wait) + DateUtils.second2MSLable(millisUntilFinished / 1000));
        }
    }

    // 初始View
    private void initGroupView(GroupViewHolder view, QuizItemInfo info, final int pos) {
        BuyGroupData data = info.getGroupData();
        if (data != null) {
            String tTotalZDF = (String) data.getTotalZDF();
            String tDayZDF = (String) data.getDayZDF();
            String tGroupName = (String) data.getGroupName();
            String tCreateTime = (String) data.getCreateTime(); // x天前
            String tFocus = (String) data.getStrFocus();
            int tRecommondFlag = data.getGroupState();

            view.mTvGroupName.setText(tGroupName);

            view.mTvTotalZDF.setText(tTotalZDF);
            int tColor = page.getZDPColor(FontUtils.getColorByZDF_percent(tTotalZDF));
            view.mTvTotalZDF.setTextColor(tColor);

            view.mTvDayZDF.setText(tDayZDF);
            tColor = page.getZDPColor(FontUtils.getColorByZDF_percent(tDayZDF));
            view.mTvDayZDF.setTextColor(tColor);

            view.mTvCreateTime.setText(tCreateTime);

            int t_groupType = MineGroupModule.getInstance().getMineType(data.getGroupId());
            if (t_groupType == MineGroupModule.MINE_TYPE_BUY) {
                view.mTvFocus.setVisibility(View.INVISIBLE);
                view.mIvRecommondFlag.setVisibility(View.VISIBLE);
                view.mIvRecommondFlag.setBackgroundResource(R.drawable.img_buyclub_gold);
            } else {
                view.mTvFocus.setVisibility(View.VISIBLE);
                view.mTvFocus.setText(String.format("%s人关注", tFocus));

                if (tRecommondFlag == 1) {
                    view.mIvRecommondFlag.setVisibility(View.VISIBLE);
                    view.mIvRecommondFlag.setBackgroundResource(R.drawable.img_buyclub_recommend);
                } else {
                    view.mIvRecommondFlag.setVisibility(View.GONE);
                }
            }

            view.mNivIamge.setDefaultImageResId(R.drawable.img_event_lstdefault);
            view.mNivIamge.setErrorImageResId(R.drawable.img_event_lstdefault);

            String imageUrl = "";
            ImageLoader imageLoader = VolleyHelper.getInstance(ACGApplication.getInstance()).getImageLoader();
            view.mNivIamge.setTag("url");
            view.mNivIamge.setImageUrl(imageUrl, imageLoader);
        }
    }

    private class StatusViewHolder {
        // 问
        public RelativeLayout askLayout;
        public TextView askNameView;
        public TextView askDateView;
        public TextView askcontentView;

        // 回复
        public View replyLayout;
        public ImageView headIV;
        public TextView replyNameView;
        public TextView replyDateView;
        public View levLayout;
        public ImageView[] levViewArr;

        // 回复内容
        public TextView replycontentView;

        // 语音
        public View voiceLayout;
        public LinearLayout voiceBgLayout;
        public ImageView voiceView;
        public TextView voidTimeView;

        // 等待或关闭的状态
        public TextView replyStateView;
        // 正在回复
        public View replyOnStateLayout;
        public ImageView rePlyingHeadIV;
        public TextView replyingNameView;
        // 回复成功，评价
        public View commentStateLayout;
        public View goodView;
        public View wellView;
        public View normalView;
        public View badView;

        // 分隔线
        public View dividerLayout;
    }

    private class StringViewHolder {
        public TextView tipView;
    }

    private class GroupViewHolder {
        public NetworkImageView mNivIamge;
        public TextView mTvGroupName;
        public TextView mTvTypeTotal;
        public TextView mTvTotalZDF;
        public TextView mTvTypeDay;
        public TextView mTvDayZDF;
        public TextView mTvCreateTime;
        public TextView mTvFocus;
        public ImageView mIvRecommondFlag;
        public View lineView;
    }

    public void recycleCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

}
