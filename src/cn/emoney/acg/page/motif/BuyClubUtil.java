package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.emoney.acg.helper.FixPair;
import cn.emoney.acg.util.DataUtils;

public class BuyClubUtil {
	/**
	 * 
	 * @param dMin
	 * @param dMax
	 * @return [min, middle, max]
	 */
	public static int[] regulateAll(int dMin, int dMax) {

		if (dMin > dMax) {
			return null;
		}

		int t_dmin = 0;
		if (dMin < 0) {
			double td = Math.abs(dMin) / 1000f;
			td = Math.ceil(td) * 1000;
			t_dmin = -(int) td;
		} else {
			double td = dMin / 1000f;
			td = Math.floor(td) * 1000;
			t_dmin = (int) td;
		}

		int t_dmax = 0;
		if (dMax < 0) {
			double td = Math.abs(dMax) / 1000f;
			td = Math.floor(td) * 1000;
			t_dmax = -(int) td;
		} else {
			double td = dMax / 1000f;
			td = Math.ceil(td) * 1000;
			t_dmax = (int) td;
		}

		int t_dmiddle = (t_dmax + t_dmin) / 2;
		int[] aryCoodinate = new int[3];
		aryCoodinate[0] = t_dmin;
		aryCoodinate[1] = t_dmiddle;
		aryCoodinate[2] = t_dmax;

		return aryCoodinate;

	}

	public static List<FixPair<String, Float>> reCalcHYGravity(List<GroupStockGoods> lstGroupStock, float balance) {
		Map<String, Float> tMap = new HashMap<String, Float>();

		float curTotalAmonut = balance;

		for (int i = 0; i < lstGroupStock.size(); i++) {
			GroupStockGoods goods = lstGroupStock.get(i);
			float t_price = Float.valueOf(goods.getZxj());
			if (t_price == 0) {
				t_price = Float.valueOf(goods.getLastClose());
			}

			float fOneStockVaule = t_price * Integer.valueOf(goods.getPositionAmount());
			curTotalAmonut += fOneStockVaule;
		}

		float tAll = 1f;
		for (int i = 0; i < lstGroupStock.size(); i++) {
			GroupStockGoods goods = lstGroupStock.get(i);
			float t_price = Float.valueOf(goods.getZxj());
			if (t_price == 0) {
				t_price = Float.valueOf(goods.getLastClose());
			}
			
			float fOneStockVaule = t_price * Integer.valueOf(goods.getPositionAmount());
			float fGravity = DataUtils.getGravityFloat(fOneStockVaule / curTotalAmonut);

			tAll -= fGravity;
			goods.setGravity(fGravity + "");
		}
		
		float blanceGravity = tAll >= 0 ? tAll : 0;
		
		for (int i = 0; i < lstGroupStock.size(); i++) {
			GroupStockGoods goods = lstGroupStock.get(i);
			
			float fOneStockGravity = Float.parseFloat(goods.getGravity());
			String hy = goods.getBKName();
			if (hy == null || hy.equals("")) {
				hy = "其它";
			}
			if (tMap.containsKey(hy)) {
				float amountGravity = fOneStockGravity + tMap.get(hy);
				tMap.put(hy, amountGravity);
			} else {
				tMap.put(hy, fOneStockGravity);
			}
		}

		List<FixPair<String, Float>> tLstHY = new ArrayList<FixPair<String, Float>>();
		Iterator<String> hyNameIterator = tMap.keySet().iterator();
		String sKey = "";
		float value = 0;

		List<FixPair<String, Float>> lst = new ArrayList<FixPair<String, Float>>();
		while (hyNameIterator.hasNext()) {
			sKey = hyNameIterator.next();
			value = tMap.get(sKey);
			FixPair<String, Float> pair = new FixPair<String, Float>(sKey, value);
			lst.add(pair);
		}

		// 排序
		Collections.sort(lst, new GroupBKGravityComparator(GroupBKGravityComparator.DESCENDING_ORDER));

		List<FixPair<String, Float>> lstBkGravity = new ArrayList<FixPair<String, Float>>();

		float tOther = 0f;
		for (int i = 0; i < lst.size(); i++) {
			if (lst.get(i).first.equals("其它")) {
				tOther = lst.get(i).second;
				continue;
			}
			if (lstBkGravity.size() < 3) {
				lstBkGravity.add(lst.get(i));
			} else {
				FixPair<String, Float> pair = lstBkGravity.get(2);
				pair.first = "其它";
				pair.second += lst.get(i).second;
			}
		}
		if (tOther > 0) {
			if (lstBkGravity.size() < 3) {
				FixPair<String, Float> pair = new FixPair<String, Float>("其它", tOther);
				lstBkGravity.add(pair);
			} else if (lstBkGravity.size() == 3) {
				FixPair<String, Float> pair = lstBkGravity.get(2);
				pair.first = "其它";
				pair.second += tOther;
			}
		}

		lstBkGravity.add(new FixPair<String, Float>("现金", blanceGravity));

		return lstBkGravity;

	}
}
