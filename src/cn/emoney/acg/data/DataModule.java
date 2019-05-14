package cn.emoney.acg.data;

import java.util.ArrayList;

import cn.emoney.acg.R;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.sky.libs.db.GlobalDBHelper;

public class DataModule {

    private static DataModule mInstance = null;

    private UserInfo mUserInfo = null;
    private OptionalInfo mOptionalInfo = null;

    public static int G_CURRENT_NETWORK_TYPE = -1;

    /**
     * 请求超时限制时间 xxx ms
     * */
    public static final int REQUEST_MAX_LIMIT_TIME = 2500;

    public static int G_CURRENT_MARKET_DATE = 0; // 行情日期 20140725
    public static int G_CURRENT_MARKET_TIME = 0; // 行情时间150355

    public static int G_CURRENT_SERVER_DATE = 0; // 服务器日期 20140725
    public static int G_CURRENT_SERVER_TIME = 0; // 服务器时间150355

    public static long G_LOCAL_SERVER_TIME_GAP = 0;
    public static boolean G_APP_IS_ACTIVE_FOREGROUND = false;

    public static int G_CURRENT_FRAME = R.id.module_frame;

    public final static String DB_GLOBAL = "acg_db_global";

    public final static String G_KEY_USER_INFO = "key_user_info"; // json存储用户信息(暂不做加密处理)
    public final static String G_KEY_USER_INFO_TYPE = "key_user_info_type"; // 账号类型
    public final static String G_KEY_USER_INFO_USERNMAE = "key_user_info_usernmae"; // 用户名
    public final static String G_KEY_USER_INFO_USERID = "key_user_info_userid"; // 用户id
    public final static String G_KEY_USER_INFO_HEADID = "key_user_info_headid"; // 用户头像id
    public final static String G_KEY_USER_INFO_PWD = "key_user_info_pwd"; // 密码md5
    public final static String G_KEY_USER_INFO_NICKNAME = "key_user_info_nickname"; // 昵称
    public final static String G_KEY_USER_INFO_CHANNEL = "key_user_info_channel"; // 渠道
    public final static String G_KEY_USER_INFO_TOKOEN = "key_user_info_tokoen"; // last
    public final static String G_KEY_USER_INFO_REALNAME = "key_user_info_realname"; // realname
    public final static String G_KEY_USER_INFO_ROLE = "key_user_info_role"; // role
    public final static String G_KEY_USER_INFO_REALHEADID = "key_user_info_realheadid"; // realheadid

    public final static String G_KEY_LAST_CLICK_ADV_TIME = "key_last_click_adv_date"; // 上一次点击或关闭广告日期
    public final static String G_KEY_LAST_UPDATE_PROMPT_CANCEL_TIME = "key_last_update_prompt_cancel_time"; // 上一次取消升级提示的时间
    public final static String G_KEY_USER_LAST_LOGIN_STATE = "key_user_last_login_state"; // 上次是否登录
    public final static String G_KEY_THEME = "key_theme";
    public final static String G_KEY_AUTO_REFRESH = "key_auto_refresh";// 是否自动刷新key
    public final static String G_KEY_ENABLE_ANIMATION = "key_enable_animation";// 是否支持动画key
    public final static String G_KEY_MOBLIEREFRESHTIMEINTERVAL = "key_moblierefreshtimeinterval"; // 手机网络刷新间隔key
    public final static String G_KEY_WIFIREFRESHTIMEINTERVAL = "key_wifirefreshtimeinterval"; // wifi网络时刷新间隔key
    public final static String G_KEY_DATABASE_VERNUMBERL = "key_database_vernumberl"; // wifi网络时刷新间隔key
    public final static String G_KEY_BOOT_GUIDE_VER = "key_boot_guide_ver"; // 开机引导画面的版本号
    public final static String G_KEY_BOOT_COUNT = "g_key_boot_count"; // 启动次数

    // 游客k线英雄数据
    public final static String G_KEY_KHEROINFO = "key_kheroinfo";

    // 本地记录资讯是否阅读
    public final static String G_KEY_INFODETAIL_MARKET = "key_infodetail_market"; // market
                                                                                  // info
                                                                                  // detail
    public final static String G_KEY_INFODETAIL_EVENT = "key_infodetail_event"; // event
                                                                                // info
                                                                                // detail
    public final static String G_KEY_INFODETAIL_QUOTE_NEWS = "key_infodetail_quote_news"; // quote_news
    public final static String G_KEY_INFODETAIL_QUOTE_NOTICE = "key_infodetail_quote_notice"; // quote_notice
    public final static String G_KEY_INFODETAIL_QUOTE_REPORT = "key_infodetail_quote_report"; // quote_findings
    public final static String G_KEY_INFODETAIL_PUSH_MSG = "key_infodetail_push_msg"; // push
    public final static String G_KEY_INFO_OPTIONAL_NEWS = "key_info_optional_news";

    public final static int G_MAX_READSTATE_COUNT = 50; // 以上每一个type的阅读状态最大的记录条数

    // 本地记录搜索历史
    public final static String G_KEY_SEARCH_HISTORY = "key_search_history"; // 搜索历史记录

    // 最后浏览的股票
    public final static String G_KEY_LAST_LOOK_GOODID = "key_last_look_goodid";

    // 最后使用的搜索方式 拼音/数字
    public final static String G_KEY_LAST_KEYBOARD_TYPE = "key_last_keyboard_type";

    // 重大利好本地缓存
    public final static String G_KEY_EQUIPMENT_ZDLH_CACHE = "key_equipment_zdlh_cache";

    // 买吧调仓记录
    public final static String G_KEY_GROUP_TRANSFER_RECORD = "key_group_transfer_record"; // 买吧调仓记录
    // 买吧今日点赞记录
    public final static String G_KEY_GROUP_PRAISED = "key_group_praised"; // 已点赞groupid

    // 系统消息
    public final static String G_KEY_PUSH_MSG_LIST = "key_push_msg_list"; // 本地缓存的push

    public final static String G_KEY_PUSH_MSG_LIST_LAST_ID = "key_push_msg_list_last_id"; // 本地缓存的pushlist最新一条的id
    public final static String G_KEY_PUSH_MSG_LIST_LAST_READED_ID = "key_push_msg_list_last_readed_id"; // 本地缓存的pushlist

    // 红点记录
    public final static String G_KEY_RED_POINT_NOTICE = "key_red_point_notice"; // 是否显示红点的本地存储

    public static String G_KEY_LAST_SERVER = "key_last_server";
    public static String G_KEY_LAST_DEBUG_SERVER = "key_last_debug_server";

    // 预警:
    public final static String G_KEY_ALARM_STOCK_LIST = "key_alarm_stock_list"; // 本地缓存个股预警,最大20个
    public final static String G_KEY_ALARM_BUYCLUB_LIST = "key_alarm_buyclub_list"; // 本地缓存买吧调仓预警,最大20个
    public final static String G_KEY_LAST_STOCK_ALERT_TIME = "key_last_stock_alert_time"; // 最近一次股票预警时间

    // 个股下拉刷新的时间
    // public final static String G_KEY_QUOTEPAGE_FRESH_TIME = "key_quotepage_fresh_time";
    // public final static String G_KEY_BUYCLUB_FRESH_TIME = "key_buyclub_fresh_time";

    public final static String G_KEY_ZDLH_READ_RECORD = "G_KEY_ZDLH_READ_RECORD"; // 本地记录重大利好是否阅读
    public final static String G_KEY_QUIZ_SET_ONLINE_STATE = "G_KEY_QUIZ_SET_ONLINE_STATE";// 问股老师端-设置在线状态
    public final static String G_KEY_QUIZ_SET_QUESTION_VOICE = "G_KEY_QUIZ_SET_QUESTION_VOICE";// 问股老师端-有新问题语音提示
    public final static String G_KEY_QUIZ_SET_QUESTION_VIBRATE = "G_KEY_QUIZ_SET_QUESTION_VIBRATE";// 问股老师端-有新问题振动
    public final static String G_KEY_QUIZ_CONFIGINFO = "G_KEY_QUIZ_CONFIGINFO";// 配置信息:存活时间|每日提问上限|老师锁定问题的时间

    // 百度推送channelId
    public static String G_BAIDU_PUSH_CHANNELID = "";
    public final static String G_KEY_BAIDU_PUSH_CHANNELID = "key_baidu_push_channelid";

    // 请配置默认值*********************

    public static String G_LOC_PATH = "emoney/istock/";
    // public static int G_ENABLE_PUSH = 1; //0:disable;1:enable
    public static boolean G_USER_DEBUG = true;
    // public static int G_THEME = ThemeManager.THEME_LIGHT;
    public static boolean G_IS_ADV_CLICKED = false; // 是否已经点击过广告
    public static int G_BOOT_TIMEOUT_MAX = 1000 * 4; // 启动超时
    public static int G_LAST_LOGIN_STATE = 0; // 0,上次为未登录(注销状态), 1,上次为登录状态
    public static boolean G_AUTO_REFRESH = true;// 是否自动刷新
    public static boolean G_ENABLE_ANIMATION = false;// 是否支持动画
    public static int G_MOBLIEREFRESHTIMEINTERVAL = 30; // 手机网络刷新间隔 秒
    public static int G_WIFIREFRESHTIMEINTERVAL = 30; // wifi网络时刷新间隔 秒
    public static int G_KLINE_REFRESH_INTERVAL = 5 * 60; // 特殊处理K线刷新间隔 秒

    public static final long G_KHERO_COMMIT_INTERVAL = 6 * 1000; // k线英雄提交数据间隔

    public static String G_APKNAME = "爱炒股"; // apk名称
    public static int G_PUAH_INTERVAL = 10; // push推送的获取间隔 分钟
    public static int G_SEED_NUM_1 = 26895494;
    public static int G_SEED_NUM_2 = 35063394;

    // apk打包前相关数据配置
    public static String G_BOOT_GUIDE_VERSION = "1.0.0";
    public static int G_DATABASE_VERNUMBER = 201509010; // 码表数据库版本号
    public static String G_APKVERNUMBER = "1.6.6"; // apk版本号
    public static String G_APKBUILDNUMBER = "2015.10.26"; // build号
    public static String G_GUEST_TOKEN = "GUEST_TOKEN"; // 游客token

    public static String G_APK_CHANEL = ChanelList.EMoney;



    public static String G_LOC_DATA_KEY_3 = "ae4be92b594b0c";
    public static String G_LOC_DATA_KEY_4 = "be6055cfe83e38";
    public static String G_LOC_DATA_KEY_1 = "dc6721c34ff110";
    public static String G_LOC_DATA_KEY_2 = "4186c8bc21e853";
    public static String G_LOC_DATA_KEY_5 = "41561136";

    // 全局加载状态
    public static int LOAD_STATE_GOODTABLE = 0;
    public static int LOAD_STATE_LB = 0;

    // 屏幕宽高（PageImpl会初始化）
    public static int SCREEN_WIDTH = 0;
    public static int SCREEN_HEIGHT = 0;
    public static final float DIALOG_WIDTH_SCALE = 0.85f;// 对话框对于屏幕的比例

    // 主要指数
    /*
     * 上证指数 000001 深证成指 1399001 沪深300 000300 中小板指 1399005 创业板指 1399006 深证综指 1399106 Ａ股指数 000002 Ｂ股指数
     * 000003 成份Ｂ指 1399003 上证380 000009 上证180 000010 上证50 000016 深证100R 1399004
     */
    public static ArrayList<Integer> mainStockIndex = new ArrayList<Integer>();

    // 登录类型
    public final static int LOGIN_TYPE_QQ = 100;// 登录-QQ

    // 头像图片的后缀名
    public static final String FORMAT_PNG = "png";
    // 录音的后缀名
    public static final String FORMAT_AMR = "amr";
    // 上传头像和语音地址
    public static final String IMAGE_VOICE_URL = "http://192.168.3.51/clienti/upload/uploadfile?ext=";
    // 头像(上传)的前缀地址(下载)
    public static final String HEAD_ICON_PRE_URL = "http://192.168.8.102/down/istockupload/userpic/";
    // 头像(系统)的前缀地址(下载)
    public static final String HEAD_ICON_SYSTEM_PRE_URL = "http://192.168.8.102/down/istockupload/userpic/system/77/";

    /********************************** 问股相关 start **********************************/
    // 服务器定义的状态值
    // enum {
    // ID_UNKNOW_ERROR = -1,
    // 0:ID_SUCCESSED,
    // 2:ID_EXISTS,
    // 3:ID_LOGIN_ERROR,
    // 4:ID_OPERATION_ERROR,
    // 5:ID_EXCEED_LIMIT,
    // 6:ID_STOCK_EXCEED_LIMIT_PER_GROUP,
    // 7:ID_NOT_EXIST,
    // 8:ID_PARAM_EMPTY,
    // 9:ID_CONFIG_DATA_NOT_EXIST,
    // 10:ID_CHANNEL_NOT_EXIST,
    // 11:ID_CHANNEL_NO_RIGHT,
    // 12:ID_FINANCE_NEWS_NO_EXIST,
    // 13:ID_NO_RIGHT,
    // 14:ID_ZDLH_IS_LAST,
    // 15:ID_ACTIVE_LICENCE_ERROR,
    // 16:ID_QUERY_LICENCE_ERROR,
    // 17:ID_NO_EXIT_SOURCE_ERROR,
    // 18:ID_FORBID_LOGIN,
    // 19:ID_PARAM_ERROR,
    // 20:ID_QUIZ_LIMIT,
    // 21:ID_QUIZ_SCRIPT_ERROR,
    // 22:ID_QUIZ_LOGIC_ERROR,
    // 23:ID_QUIZ_UNCOMPLETE,
    // 24:ID_QUIZ_SUBMIT_LIMIT,
    // };
    // 客户端会用到的状态值
    public static final int QUIZ_SERVER_STATE_ERROR = -1;
    public static final int QUIZ_SERVER_STATE_SUCC = 0;
    public static final int QUIZ_SERVER_STATE_LIMIT = 20;
    public static final int QUIZ_SERVER_STATE_SCRIPT_ERROR = 21;
    public static final int QUIZ_SERVER_STATE_LOCGIC_ERROR = 22;
    public static final int QUIZ_SERVER_STATE_UNCOMPLETE = 23;
    public static final int QUIZ_SERVER_STATE_SUBMIT_LIMIT = 24;

    // 语音的内容格式：时长|url
    /********************************** 问股相关 end **********************************/

    private DataModule() {
        mainStockIndex.clear();
        mainStockIndex.add(1);
        mainStockIndex.add(1399001);
        mainStockIndex.add(300);
        mainStockIndex.add(1399005);
        mainStockIndex.add(1399006);
        mainStockIndex.add(1399106);
        mainStockIndex.add(2);
        mainStockIndex.add(3);
        mainStockIndex.add(1399003);
        mainStockIndex.add(9);
        mainStockIndex.add(10);
        mainStockIndex.add(16);
        mainStockIndex.add(1399004);
    }

    public static void ResetWebUrls() {}

    public static DataModule getInstance() {
        if (mInstance == null) {
            mInstance = new DataModule();
        }
        return mInstance;
    }

    public UserInfo getUserInfo() {
        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }
        return mUserInfo;
    }

    public OptionalInfo getOptionalInfo() {
        if (mOptionalInfo == null) {
            mOptionalInfo = new OptionalInfo();
        }
        return mOptionalInfo;
    }

    public void clear() {

    }

    public void load(GlobalDBHelper helper) {
        if (helper == null) {
            return;
        }

    }

    public void save(GlobalDBHelper helper) {
        if (helper == null) {
            return;
        }
    }

    public void reset() {

    }
}
