package cn.emoney.acg.data;

public class BuyClubHttpUrl {

     public static final String URL_DOMAIN = "http://192.168.3.51/clientmotif/";
//    public static final String URL_DOMAIN = "http://maiba.i.emoney.cn/";
    /*
     * 加关注
     */
    public static final String URL_ADD_FOCUS = URL_DOMAIN + "motif/mobilefollow?token=%s&code=%d";
    public static final short FLAG_URL_ADD_FOCUS = 101;
    /**
     * 取消关注
     */
    public static final String URL_CANCEL_FOCUS = URL_DOMAIN + "motif/mobilecancelfollow?token=%s&code=%d";
    public static final short FLAG_URL_CANCEL_FOCUS = 102;
    /**
     * 点赞
     */
    public static final String URL_PRAISE = URL_DOMAIN + "motif/mobilepraise?token=%s&code=%d";
    public static final short FLAG_URL_PRAISE = 103;
    /**
     * 获取组合评论列表
     */
    public static final String URL_GROUP_COMMENT_LIST = URL_DOMAIN + "motif/mobilecommentlist?token=%s&code=%d&start=%d&size=%d&type=%d";
    public static final short FLAG_GROUP_COMMENT_LIST = 104;
    /**
     * 获取组合评论列表的回复列表
     */
    public static final String URL_GROUP_COMMENT_REPLY = URL_DOMAIN + "motif/mobilereplylist?token=%s&code=%d&commentidx=%d";
    public static final short FLAG_GROUP_COMMENT_REPLY = 105;

    /**
     * 发表评论
     */
    public static final String URL_GROUP_COMMENT_PUBLISH = URL_DOMAIN + "motif/mobileaddcomment?token=%s&code=%d&comment=%s&reply_commentid=%d&dst_usr_id=%d";
    public static final short FLAG_GROUP_COMMENT_PUBLISH = 106;

    /**
     * 删除评论
     */
    public static final String URL_GROUP_COMMENT_DELETE = URL_DOMAIN + "motif/mobiledelcomment?token=%s&commentid=%d";
    public static final short FLAG_GROUP_COMMENT_DELETE = 107;

    /**
     * 修改组合理念
     * */
    public static final String URL_GROUP_EDIT_IDEA = URL_DOMAIN + "mobile/investment?token=%s&code=%s&investment=%s";
    public static final short FLAG_GROUP_EDIT_IDEA = 108;

    /**
     * 获取可操作仓位
     * */
    public static final String URL_GROUP_AVAILABLE_POSITION = URL_DOMAIN + "mobile/position?token=%s&code=%s&stock=%s";
    public static final short FLAG_GROUP_AVAILABLE_POSITION = 109;

    /**
     * 操作股票（加仓，平仓，建仓）
     * */
    public static final String URL_GROUP_RESET_POSITION = URL_DOMAIN + "mobile/groupstock?token=%s&code=%s&stock=%s&srcpos=%s&dstpos=%s&remark=%s&price=%s";
    public static final short FLAG_GROUP_RESET_POSITION = 110;

    /**
     * 取消订单
     * */
    public static final String URL_GROUP_CANCEL_ORDER = URL_DOMAIN + "mobile/cancelstock?token=%s&code=%s&orderid=%s";
    public static final short FLAG_GROUP_CANCEL_ORDER = 111;

}
