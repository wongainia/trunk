package cn.emoney.acg.data.bdcast;

/**
 * 
 * @author daizhipeng 效率优先,尽量少使用广播
 */
public class BroadCastName {
    public static final String BCDC_CHANGE_LOGIN_STATE = "cn.emoney.acg.BCDC_CHANGE_LOGIN_STATE"; // 登录状态更新
    public static final String BCDC_RED_POINT_UPDATE = "cn.emoney.acg.BCDC_RED_POINT_UPDATE"; // 红点更新
    public static final String BCDC_OPTIONAL_DATA_UPDATE = "cn.emoney.acg.BCDC_OPTIONAL_DATA_UPDATE"; // 自选更新
    public static final String BCDC_CHANGE_LOGIN_STATE_QIZE = "cn.emoney.acg.BCDC_CHANGE_LOGIN_STATE_QIZE"; // 登录状态更新(登录成功或失败，都会发这个广播)
    public static final String BCDK_QUIZ_MY_QUESTION_REST = "cn.emoney.acg.BCDK_QUIZ_MY_QUESTION_REST";// 问股，重置我的问题
}
