package cn.emoney.acg.page.quiz;

/**
 * 
 * 列表的监听器
 * 
 * @ClassName: QuizListViewlListener
 * @Description:
 * @author xiechengfa
 * @date 2015年12月7日 下午3:28:36
 *
 */
public interface QuizListViewlListener {
    public final static int COMMENT_BAD = 1;// 差评
    public final static int COMMENT_NORAML = 2;// 一般
    public final static int COMMENT_WELL = 3;// 满意
    public final static int COMMENT_GOOD = 4;// 非常满意

    /**
     * 评价
     * 
     * @param type
     */
    public void onAppraise(long id, int lev);

    /**
     * 播放语音
     * 
     * @param pos
     */
    public void onPlayVoice(int pos);

    /**
     * 点击头像
     * 
     * @param pos
     */
    public void onClickHeadIcon(int pos);

    /**
     * 问题超时
     * 
     * @param pos
     */
    public void onQuestionClose(int pos);
}
