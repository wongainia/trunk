package cn.emoney.acg.data.quiz;

import java.io.Serializable;

import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.protocol.quiz.QuizQueryReply.QuizQuery_Reply.QuziItem;

/**
 * 问股问题信息
 *
 * @ClassName: QuizContentInfo
 * @Description:
 * @author xiechengfa
 * @date 2015年12月7日 上午11:31:27
 *
 */
public class QuizContentInfo implements Cloneable, Serializable {
    private static final long serialVersionUID = -1068168485439892531L;
    // 内容类型
    public static final int CONTENT_TYPE_TEXT = 0;// 文字
    public static final int CONTENT_TYPE_VOICE = 1;// 语音

    // 0-已提问 1-已分发 2-已被抢 3-已答 4-已评价（完成） -1-超时（完成）
    public static final int STATUS_ASK_WAIT = 0;// 问题状态-等待
    public static final int STATUS_ASK_WAIT2 = 1;// 问题状态-等待2
    public static final int STATUS_ASK_ON = 2;// 问题状态-正在回复appraise
    public static final int STATUS_ASK_REPLED_TO_APPRAISE = 3;// 问题状态-已回复,请评价
    public static final int STATUS_ASK_APPRAISED = 4;// 问题状态-已回复,已评价（完成）
    public static final int STATUS_ASK_CLOSE = -1;// 问题状态-超时（完成）
    public static final int STATE_ANSWER_CLOSE = 100;// 回答超时

    // 问题
    private long id = 0;// 提问时候填写0
    private String content = null;
    private int commitTime = 0;
    private String stock = null;// 关联
    private int status = STATUS_ASK_WAIT;// 0-已提问 1-已分发 2-已被抢 3-已答 4-已评价（完成）

    // 评价
    private int appraiseLevel = 0; // 评分 星数

    // 提出问题的用户
    private UserInfo owner = null;

    // 回复的老师
    private TeacherInfo replier = null;

    // 回复
    private int answerTime = 0;// 回答时间
    private AnswerInfo answer = null; // 答案
    private int takeTime = 0;// 被抢时间

    // add
    private boolean isMyLatestQuestion = false;// 我的问题
    private boolean isPlaying = false;// 是否正在播放语音
    private boolean isDowning = false;// 是否正在下载语音
    private boolean isReplyExpand = false;// 回复内容是否展开


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        this.owner = owner;
    }

    public TeacherInfo getReplier() {
        return replier;
    }

    public void setReplier(TeacherInfo replier) {
        this.replier = replier;
    }

    public AnswerInfo getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerInfo answer) {
        this.answer = answer;
    }

    public int getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(int answerTime) {
        this.answerTime = answerTime;
    }

    public int getAppraiseLevel() {
        return appraiseLevel;
    }

    public void setAppraiseLevel(int appraiseLevel) {
        this.appraiseLevel = appraiseLevel;
    }

    public boolean isMyLatestQuestion() {
        return isMyLatestQuestion;
    }

    public void setMyLatestQuestion(boolean isMyLatestQuestion) {
        this.isMyLatestQuestion = isMyLatestQuestion;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isDowning() {
        return isDowning;
    }

    public void setDowning(boolean isDowning) {
        this.isDowning = isDowning;
    }

    public int getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(int takeTime) {
        this.takeTime = takeTime;
    }

    public boolean isReplyExpand() {
        return isReplyExpand;
    }

    public void setReplyExpand(boolean isReplyExpand) {
        this.isReplyExpand = isReplyExpand;
    }

    public int getQuestionHashCode() {
        String hashStr = content;
        if (replier != null) {
            hashStr = hashStr + "|" + replier.getId();
        }

        if (answer != null) {
            hashStr = hashStr + "|" + answer.getContent();
        }

        return hashStr.hashCode();
    }

    public boolean isMyQuestion() {
        if (DataModule.getInstance().getUserInfo().isLogined()) {
            if (getOwner() != null && DataModule.getInstance().getUserInfo().getUid().equals(getOwner().getUid())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 我的问题是否结束
     * 
     * @return
     */
    public boolean isQuestionOver() {
        return status == STATUS_ASK_APPRAISED || status == STATUS_ASK_CLOSE;
    }

    /**
     * 问题是否回复
     * 
     * @return
     */
    public boolean isQuestionReply() {
        return status == STATUS_ASK_REPLED_TO_APPRAISE || status == STATUS_ASK_APPRAISED;
    }

    public static QuizContentInfo initOfServerItem(Item item) {
        if (item == null) {
            return null;
        }

        QuizContentInfo info = new QuizContentInfo();

        // 问题相关的信息
        info.setId(item.getId());
        info.setContent(item.getContent());
        info.setCommitTime(item.getCommitTime());
        info.setStock(item.getStock());
        info.setStatus(item.getStatus());
        info.setTakeTime(item.getTakeTime());

        // 评价
        info.setAppraiseLevel(item.getAppraiseLevel());

        // 提出问题的用户
        info.setOwner(new UserInfo(item.getOwner().getId() + "", item.getOwner().getNick(), item.getOwner().getIcon()));

        // 回复的老师
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.initOfServerTeacher(item.getReplier());
        info.setReplier(teacherInfo);

        // 回复
        info.setAnswerTime(item.getAnswerTime());
        info.setAnswer(AnswerInfo.initServerAnswer(item.getAnswer()));

        return info;
    }

    public static QuizContentInfo initOfServerQuizItem(QuziItem item) {
        if (item == null) {
            return null;
        }

        QuizContentInfo info = new QuizContentInfo();

        // 问题相关的信息
        info.setId(item.getId());
        info.setContent(item.getContent());
        info.setCommitTime(item.getCommitTime());
        info.setStatus(item.getStatus());
        info.setTakeTime(item.getLastTime());

        // 提出问题的用户
        info.setOwner(new UserInfo(item.getOwner().getId() + "", item.getOwner().getNick(), item.getOwner().getIcon()));

        // 回复的老师
        if (item.getTeacherId() > 0) {
            TeacherInfo teacherInfo = new TeacherInfo();
            teacherInfo.setId(item.getTeacherId());
            info.setReplier(teacherInfo);
        }

        return info;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }
}
