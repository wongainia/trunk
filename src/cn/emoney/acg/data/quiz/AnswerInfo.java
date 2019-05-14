package cn.emoney.acg.data.quiz;

import java.io.Serializable;

import cn.emoney.acg.data.protocol.quiz.QuizDefine.Answer;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;

/**
 * @ClassName: AnswerInfo
 * @Description:回复信息
 * @author xiechengfa
 * @date 2015年12月31日 上午9:47:55
 *
 */
public class AnswerInfo implements Serializable {
    private static final long serialVersionUID = -5169164946966735825L;
    private int type = QuizContentInfo.STATUS_ASK_WAIT;
    private String content = null;// 内容
    private String voiceUrl = null;// 语音
    private String voiceTime = null;// 语音时间

    public AnswerInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AnswerInfo(String content, String voiceUrl, String voiceTime, int type) {
        super();
        this.content = content;
        this.voiceUrl = voiceUrl;
        this.voiceTime = voiceTime;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public String getVoiceTime() {
        return voiceTime;
    }

    public void setVoiceTime(String voiceTime) {
        this.voiceTime = voiceTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static AnswerInfo initServerAnswer(Answer answer) {
        if (answer == null) {
            return null;
        }

        AnswerInfo info = new AnswerInfo();

        info.setContent(answer.getContent());
        info.setType(answer.getType());
        initVoicInfo(info);

        return info;
    }

    public static AnswerInfo initServerAnswer(Answer.Builder answer) {
        if (answer == null) {
            return null;
        }

        AnswerInfo info = new AnswerInfo();

        info.setContent(answer.getContent());
        info.setType(answer.getType());
        initVoicInfo(info);

        return info;
    }

    // 解析语音信息
    private static void initVoicInfo(AnswerInfo info) {
        if (info == null) {
            return;
        }

        if (info.getType() == QuizContentInfo.CONTENT_TYPE_VOICE) {
            // 语音
            String content = info.getContent();
            if (content != null && content.trim().length() > 0) {
                String[] contentArr = content.split("\\|");
                if (contentArr != null && contentArr.length == 2) {
                    info.setVoiceTime(DateUtils.secondValueToSecondStrQuizList(DataUtils.convertToInt(contentArr[0])));
                    info.setVoiceUrl(contentArr[1]);
                    // LogUtil.easylog("****************voiceInfo time:" + info.getVoiceTime() +
                    // ",url:" + info.getVoiceUrl() + ",time2:" + content);
                }
            }
        }
    }
}
