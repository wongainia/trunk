package cn.emoney.acg.data;

public class GoodsParams {
	public static final int CLOSE = 0;// 昨收
	// 请求字段

	public static final int GOODS_NAME = -1;// 股票名称
	public static final int GOODS_CODE = -2;// 股票代码
	public static final int ZXJ = 4;// 最新价（成交价）
	public static final int ZDF = -140;// 涨跌幅
	public static final int JL = -165;// 净流
	public static final int HSL = -162;// 换手率
	public static final int FIVEZDF = -142;// 5日涨跌

	public static final int CPX_DAY = -153; // 操盘线-日线
	public static final int CPX_60M = -156; // 操盘线-60分钟线
	public static final int GROUP_HY = -704; // 行业板块

	// k线界面头部

	public static final int ZHANGDIE = -120;// 涨跌
	public static final int OPEN = 1;// 开盘
	public static final int HiGH = 2;// 最高
	public static final int LOW = 3;// 最低
	public static final int SYL = -161;// 市盈率
	public static final int ZGB = 504;// 总股本
	public static final int LTG = 505;// 流通股
	public static final int ZSZ = -601;// 总市值
	public static final int LTSZ = -602;// 流通市值
	public static final int SJL = -164;// 市净率
	public static final int LB = -160;     // 量比
	public static final int ZHENFU = -145;   // 振幅

	public static final int VOLUME = 500;// 成交量（总手）
	public static final int AMOUNT = 501;// 成交额（金额）
	public static final int RISE = -201;// 涨家（上涨）
	public static final int EQUAL = -203;// 平家（平盘）
	public static final int FALL = -202;// 跌家（下跌）

	public static final int RISE_HEAD_GOODSID = 678; // 板块领涨股id
	public static final int RISE_HEAD_GOODSZDF = -20001; // 板块领涨股涨跌幅
	public static final int RISE_HEAD_GOODSNAME = -20003; // 板块领涨股名称
	public static final int FALL_HEAD_GOODSID = 680; // 板块领跌股id
	public static final int FALL_HEAD_GOODSZDF = -20002; // 板块领跌股涨跌幅
	public static final int FALL_HEAD_GOODSNAME = -20004; // 板块领跌股名称
	public static final int SUSPENSION = -20005; // 停牌信息

}
