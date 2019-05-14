package cn.emoney.acg.data;

public interface IDUtils {

    // protobuf protocol
    // 心跳
    public final static int MarketDateTime = 20000;
    // 码表
    public final static int GoodsTable = 20100;
    // 分时
    public final static int MinuteLine = 20300;
    // k线
    public final static int CandleStick = 20400;
    // 行情
    public final static int DynaValueData = 20200;
    // 行情热点
    public final static int MarketFocus = 20501;
    // 操盘线
    public final static int Cpx = 20600;
    // 十日净流
    public final static int FundInflow = 20700;
    // 指标
    public final static int Indicator = 20800;



    // json protocol
    // 查询自选股类型
    public final static int URL_OPTIONAL_QUERY_TYPES = 4100;

    // 系统消息推送
    public final static short ID_PUSH_MSG = 7001;

    // 请求红点
    public final static short ID_RED_POINT_NOTICE = 7300;

    public final static short ID_EVENT_NEWSLIST = 6000;
    public final static short ID_OPTIONAL_QUERY_STOCKS = 4600;
    public final static short ID_OPTIONAL_QUERY_STOCKS_PB = 4601;// pb新协议
    public final static short ID_OPTIONAL_ADD_STOCK = 4200;
    public final static short ID_OPTIONAL_DEL_STOCK = 4300;
    public final static short ID_OPTIONAL_OPTION_STOCK_TYPE = 4000;
    public final static short ID_OPTIONAL_CHANGE_DETAIL_STOCK = 4400;
    public final static short ID_OPTIONAL_CHANGE_SORT_STOCK = 4500;
    public final static short ID_MARKET_INFO = 8200; // 新市场资讯协议
    public final static short ID_MARKET_NARRATE = 13300;
    public final static short ID_STOCK_NEWS = 6301;
    public final static short ID_STOCK_NOTICE = ID_STOCK_NEWS;
    public final static short ID_STOCK_REPORT = ID_STOCK_NEWS;
    public final static short ID_STOCK_DIAGNOSE = 10701; // server统一编码,版本升级到01
    public final static short ID_STOCK_QUESTION = 18800; // 个股问答
    public final static short ID_MAJOR_TIPS = 6601; // 重大提示列表
    public final static short ID_MAJOR_TIP_DETAIL = 6701; // 重大提示详情
    public final static short ID_INFO_HEADLINES = 10800; // 今日头条列表
    public final static short ID_INFO_LIVES = 10900; // 直播列表
    public final static short ID_SUSPENSION = 24000; // 停牌信息
    public final static short ID_OPTIONAL_NEWS = 5200; // 自选股新闻

    public final static short ID_EVENT_COMMENT = 6100;
    public final static short ID_EVENT_INFO = 6200;
    public final static short ID_USER_LOGIN = 3003;
    public final static short ID_USER_RELOGIN = 3100;
    public final static short ID_USER_PERMISSION = 3400;
    public final static short ID_USER_EXTRAINFO = 3700;
    public final static short ID_USER_CHANGEPWD = 3900;

    public final static short ID_KHERO = (short) 65400; //

    public final static short ID_STOCK_SELECTION_STRATEGY = 8000; // 选股策略
    public final static short ID_SYSTEM_DATA = 4900; // 全局配置数据
    public final static short ID_USER_DATA = 4800; // 用户数据查询
    public final static short ID_USER_DATA_UPDATE = 4700; // 用户数据修改

    public final static short ID_ZDLH = 10501; // 重大利好

    /* ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 买吧组合 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */
    public final static int ID_GROUP_LIST = 8500; // 组合列表, pb协议
    public final static short ID_GROUP_DETAIL_INFO = 8600; // 组合info
    public final static short ID_GROUP_CONTROL_HISTORY = 8700; // 组合调仓记录
    public final static short ID_GROUP_TREND = 8800; // 组合收益趋势, pb协议
    public final static short ID_GROUP_HTTP_INTERFACE = 8900; // WEB中转
    public final static short ID_GROUP_HTTP_GROUP_PRAISE = 8901; // 组合点赞
    /* ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 买吧组合 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

    /* ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 问股 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */
    public final static short ID_QUIZ_REQUIRE_REQ = 18000; // 提交提问
    public final static short ID_QUIZ_QUERY_REQ = 18100; // 老师刷问题
    public final static short ID_QUIZ_TAKE_REQ = 18200; // 讲师抢答
    public final static short ID_QUIZ_DROP_REQ = 18300; // 抢到之后放弃回答
    public final static short ID_QUIZ_ANSWER_REQ = 18400; // 讲师回复
    public final static short ID_QUIZ_APPRAISE_REQ = 18500; // 对问题进行评价
    public final static short ID_QUIZ_CONFIG_REQ = 18600; // 配置数据
    public final static short ID_QUIZ_MY_LIST_REQ = 18700; // 我的问题列表
    public final static short ID_QUIZ_RELATE_LIST_REQ = 18800; // 相关问题列表
    public final static short ID_QUIZ_QRY_STATUS_REQ = 18900; // 查询状态
    public final static short ID_QUIZ_TEACHER_DETAIL_REQ = 19000; // 老师详情
    public final static short ID_QUIZ_TEACHER_SETTING_REQ = 19100; // 老师的设置
    /* ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 问股 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

    // 推送消息
    public final static short ID_STOCK_ALERT_MSG_LIST = 7200; // 个股预警推送消息列表
    public final static short ID_STOCK_ALERT_BUY_CLUB_LIST = 7400; // 买吧推送消息列表

    public static final short ID_MARKET_TREND = 19500; // 获取大盘趋势，看涨看跌

    public static final short ID_RELATIVE_BKS = 21000; // 个股关联版块
    public static final short ID_STOCK_FINALCIAL_REPORT = 20900; // 个股财务报告
    
    // 百度推送
    public final static short ID_BAIDU_PUSH_SET = 7500;
    public final static short ID_BAIDU_PUSH_READ = 7600;
}
