package cn.emoney.acg.data.protocol.motif;

public interface BuyClubField {
	public static int FIELD_NAME = 1; // 组合名称
	public static int FIELD_DAY_INCOME = 2; // 日收益 x%
	public static int FIELD_WEEK_INCOME = 3; // 周收益 x%
	public static int FIELD_MONTH_INCOME = 4; // 月收益 x%
	public static int FIELD_TOTAL_INCOME = 5; // 总收益 x%
	public static int FIELD_CREATE_TIME = 6;// 创建时间
	public static int FIELD_CREATOR = 7; // 创建者
	public static int FIELD_FOCUS = 8; // 关注数
	public static int FIELD_PRAISE = 9; // 好评数
	public static int FIELD_MINE_TYPE = 10; // 我的组合类型：类型（1：创建 2：购买 3：关注 4：推荐）
	
}
