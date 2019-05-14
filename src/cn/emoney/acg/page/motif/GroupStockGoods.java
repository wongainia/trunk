package cn.emoney.acg.page.motif;

import cn.emoney.acg.data.Goods;

public class GroupStockGoods extends Goods {
	/**
	 * 仓位 25.6% mGravity 0.256;
	 */
	private String mGravity = "0";

	/**
	 * 总花费金额,单位元
	 */
	private long mTotalCostValue = 0;

	/**
	 * 总买入股数
	 */
	private long mTotalGoodsNum = 0;

	/**
	 * 关联时间(加入组合时间) 格式:2015-04-22 00:00:00.000
	 */
	private String mAddTime = "";

	public String getAddTime() {
		return mAddTime;
	}

	/**
	 * 关联时间(加入组合时间)
	 * 
	 * @param time
	 *            格式:2015-04-22 00:00:00.000
	 * @return
	 */
	public void setAddTime(String time) {
		mAddTime = time;
	}

	public GroupStockGoods(int id, String name) {
		super(id, name);
	}

	public String getGravity() {
		return mGravity;
	}

	public void setGravity(String mGravity) {
		this.mGravity = mGravity;
	}

	public long getTotalCostValue() {
		return mTotalCostValue;
	}

	public void setTotalCostValue(long mTotalCostValue) {
		this.mTotalCostValue = mTotalCostValue;
	}

	public long getTotalGoodsNum() {
		return mTotalGoodsNum;
	}

	public void setTotalGoodsNum(long mTotalGoodsNum) {
		this.mTotalGoodsNum = mTotalGoodsNum;
	}

}
