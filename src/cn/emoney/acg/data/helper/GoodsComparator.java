package cn.emoney.acg.data.helper;

import java.util.ArrayList;
import java.util.Comparator;

import cn.emoney.acg.data.Goods;

public class GoodsComparator implements Comparator<Goods> {

	public final static int SORTTYPE_CUSTOM = 999;

	public final static int SORTTYPE_PRICE = 1;
	public final static int SORTTYPE_ZDF = 2;
	public final static int SORTTYPE_ZD = 3;
	public final static int SORTTYPE_JL = 4;
	public final static int SORTTYPE_HSL = 5;
	public final static int SORTTYPE_ZDF5 = 6;
	public final static int SORTTYPE_SYL = 7;

	// 其它持仓排序字段
	public final static int SORTTYPE_COST_PRICE = 8;// 成本价
	public final static int SORTTYPE_PROFIT_LOSS = 9;// 盈亏
	public final static int SORTTYPE_PROFIT_LOSS_PERCENT = 10;// 盈亏比
	public final static int SORTTYPE_POSITION_AMOUNT = 11;// 持股数
	public final static int SORTTYPE_MARKET_VALUE = 12;// 市值
	public final static int SORTTYPE_CPX = 13;// 操盘线
	public final static int SORTTYPE_INDUSTRY = 14;// 所属行业
	public final static int SORTTYPE_SJL = 15;// 市净率
	public final static int SORTTYPE_STOCKID = 16;// 股票id

	protected int mSortType = SORTTYPE_PRICE;
	private ArrayList<Integer> mCustomLst;

	public GoodsComparator(int sortType) {
		mSortType = sortType;
	}

	public GoodsComparator(int sortType, ArrayList<Integer> custom) {
		mSortType = sortType;
		mCustomLst = custom;
	}

	@Override
	public int compare(Goods lhs, Goods rhs) {
		int t_sortType = Math.abs(mSortType);
		int ret = 0;
		switch (t_sortType) {
		case SORTTYPE_PRICE: {
			String price1 = lhs.getZxj();
			String price2 = rhs.getZxj();
			if (price1.equals("--") || price2.equals("--")) {
				break;
			}
			float p1 = Float.parseFloat(price1);
			float p2 = Float.parseFloat(price2);

			ret = p1 == p2 ? 0 : (p1 > p2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
			
		}
			break;
		case SORTTYPE_ZDF: {
			String zf1 = lhs.getZdf().replaceAll("%", "");
			String zf2 = rhs.getZdf().replaceAll("%", "");
			if (zf1.equals("--") || zf2.equals("--")) {
				break;
			}
			float z1 = Float.parseFloat(zf1);
			float z2 = Float.parseFloat(zf2);
			
			ret = z1 == z2 ? 0 : (z1 > z2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_ZD: {
			String zd1 = lhs.getZd();
			String zd2 = rhs.getZd();
			if (zd1.equals("--") || zd2.equals("--")) {
				break;
			}
			float z1 = Float.parseFloat(zd1);
			float z2 = Float.parseFloat(zd2);
			ret = z1 == z2 ? 0 : (z1 > z2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_JL: {
			String jl1 = lhs.getJl();
			String jl2 = rhs.getJl();
			if (jl1.equals("--") || jl2.equals("--")) {
				break;
			}
			float fjl1 = Float.parseFloat(jl1);
			float fjl2 = Float.parseFloat(jl2);
			ret = fjl1 == fjl2 ? 0 : (fjl1 > fjl2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_HSL: {
			String hsl1 = lhs.getHsl().replaceAll("%", "");
			String hsl2 = rhs.getHsl().replaceAll("%", "");
			if (hsl1.equals("--") || hsl2.equals("--")) {
				break;
			}
			float fhsl1 = Float.parseFloat(hsl1);
			float fhsl2 = Float.parseFloat(hsl2);
			ret = fhsl1 == fhsl2 ? 0 : (fhsl1 > fhsl2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_ZDF5: {
			String zdf5_1 = lhs.getFiveZdf().replaceAll("%", "");
			String zdf5_2 = rhs.getFiveZdf().replaceAll("%", "");
			if (zdf5_1.equals("--") || zdf5_2.equals("--")) {
				break;
			}
			float fzdf5_1 = Float.parseFloat(zdf5_1);
			float fzdf5_2 = Float.parseFloat(zdf5_2);
			ret = fzdf5_1 == fzdf5_2 ? 0 : (fzdf5_1 > fzdf5_2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_SYL: {
			String syl1 = lhs.getSyl();
			String syl2 = rhs.getSyl();
			if (syl1.equals("--") || syl2.equals("--")) {
				break;
			}
			float s1 = Float.parseFloat(syl1);
			float s2 = Float.parseFloat(syl2);
			ret = s1 == s2 ? 0 : (s1 > s2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_SJL: {
			String sjl1 = lhs.getSjl();
			String sjl2 = rhs.getSjl();
			if (sjl1.equals("--") || sjl2.equals("--")) {
				break;
			}
			float s1 = Float.parseFloat(sjl1);
			float s2 = Float.parseFloat(sjl2);
			ret = s1 == s2 ? 0 : (s1 > s2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_COST_PRICE: {
			String s1 = lhs.getPositionPrice();
			String s2 = rhs.getPositionPrice();
			if (s1.equals("--") || s2.equals("--")) {
				break;
			}
			float f1 = Float.parseFloat(s1);
			float f2 = Float.parseFloat(s2);
			ret = f1 == f2 ? 0 : (f1 > f2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_PROFIT_LOSS: {
			String s1 = lhs.getPositionProfitLoss();
			String s2 = rhs.getPositionProfitLoss();
			if (s1.equals("--") || s2.equals("--")) {
				break;
			}
			float f1 = Float.parseFloat(s1);
			float f2 = Float.parseFloat(s2);
			ret = f1 == f2 ? 0 : (f1 > f2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_PROFIT_LOSS_PERCENT: {
			String s1 = lhs.getPositionProfitLossPercent();
			String s2 = rhs.getPositionProfitLossPercent();
			if (s1.equals("--") || s2.equals("--")) {
				break;
			}
			s1 = s1.replace("%", "");
			s2 = s2.replace("%", "");
			float f1 = Float.parseFloat(s1);
			float f2 = Float.parseFloat(s2);
			ret = f1 == f2 ? 0 : (f1 > f2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_MARKET_VALUE: {
			String s1 = lhs.getPositionMarketValue();
			String s2 = rhs.getPositionMarketValue();
			if (s1.equals("--") || s2.equals("--")) {
				break;
			}

			float f1 = Float.parseFloat(s1);
			float f2 = Float.parseFloat(s2);
			ret = f1 == f2 ? 0 : (f1 > f2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_POSITION_AMOUNT: {
			String s1 = lhs.getPositionAmount();
			String s2 = rhs.getPositionAmount();
			if (s1.equals("--") || s2.equals("--")) {
				break;
			}

			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);
			ret = i1 == i2 ? 0 : (i1 > i2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_CPX: {
			int cpx1 = lhs.getDayBS();
			int cpx2 = rhs.getDayBS();
			ret = cpx1 == cpx2 ? 0 : (cpx1 > cpx2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_INDUSTRY: {
			String s1 = lhs.getBKName();
			String s2 = rhs.getBKName();
			int tCompare = s1.compareTo(s2);
			ret = tCompare == 0 ? 0 : (tCompare > 0 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
		}
			break;
		case SORTTYPE_STOCKID: {
			int gid1 = lhs.getGoodsId();
			int gCode1 = gid1 % 1000000;
			int gid2 = rhs.getGoodsId();
			int gCode2 = gid2 % 1000000;
			
			ret = gCode1 == gCode2 ? 0 : (gCode1 > gCode2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
			
		}
			break;
		case SORTTYPE_CUSTOM: {
			if (mCustomLst != null) {
				int gid1 = lhs.getGoodsId();
				int gid2 = rhs.getGoodsId();
				int index1 = mCustomLst.indexOf(Integer.valueOf(gid1));
				int index2 = mCustomLst.indexOf(Integer.valueOf(gid2));

				ret = index1 == index2 ? 0 : (index1 < index2 ? takePlusOrMinus(mSortType) : -takePlusOrMinus(mSortType));
			}

		}
			break;
		default:
			break;
		}

		return ret;

	}

	public static int takePlusOrMinus(int type) {
		return type < 0 ? 1 : -1;
	}
}
