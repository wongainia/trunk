package cn.emoney.acg.data.quiz;

import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.protocol.quiz.QuizQueryReply.QuizQuery_Reply.QuziItem;
import cn.emoney.acg.page.motif.BuyGroupData;

/**
 * 列表的动动态信息
 * 
 * @ClassName: QuizItemInfo
 * @Description:
 * @author xiechengfa
 * @date 2015年12月7日 上午11:33:30
 *
 */
public class QuizItemInfo {
    public static final int TYPE_ASK_MY = 0;// 我的提问
    public static final int TYPE_ASK_OTHER = 1;// 别人的提问

    public final static int TYPE_STRING = 0;// 字符串
    public final static int TYPE_QUESTION = 1;// 问题
    public final static int TYPE_GROUP = 2;// 组合

    private int type = TYPE_QUESTION;
    private QuizContentInfo quizItem = null;
    private BuyGroupData groupData = null;
    private String tipStr = null;

    public QuizItemInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public QuizItemInfo(String tipStr) {
        super();
        this.type = TYPE_STRING;
        this.tipStr = tipStr;
    }

    public QuizItemInfo(int type, QuizContentInfo quizItem) {
        super();
        this.type = type;
        this.quizItem = quizItem;
    }

    public QuizItemInfo(int type, Item item) {
        this.type = type;
        this.quizItem = QuizContentInfo.initOfServerItem(item);
    }

    public QuizItemInfo(int type, QuziItem item) {
        this.type = type;
        this.quizItem = QuizContentInfo.initOfServerQuizItem(item);
    }

    public QuizItemInfo(int type, BuyGroupData groupData) {
        super();
        this.type = type;
        this.groupData = groupData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public QuizContentInfo getQuizItem() {
        return quizItem;
    }

    public void setQuizItem(QuizContentInfo quizItem) {
        this.quizItem = quizItem;
    }

    public String getTipStr() {
        return tipStr;
    }

    public void setTipStr(String tipStr) {
        this.tipStr = tipStr;
    }

    public BuyGroupData getGroupData() {
        return groupData;
    }

    public void setGroupData(BuyGroupData groupData) {
        this.groupData = groupData;
    }

}
