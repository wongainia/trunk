package cn.emoney.acg.page.motif;

import java.util.Comparator;

import cn.emoney.acg.helper.FixPair;

public class GroupBKGravityComparator implements Comparator<FixPair<String, Float>> {
	public final static int ASCENDING_ORDER = 1; // 升序
	public final static int DESCENDING_ORDER = -1; // 降序

	private int mSortType = ASCENDING_ORDER;

	public GroupBKGravityComparator(int sortType) {
		mSortType = sortType;
	}

	@Override
	public int compare(FixPair<String, Float> pair1, FixPair<String, Float> pair2) {
		return pair1.second == pair2.second ? 0 : (pair1.second > pair2.second ? mSortType : -mSortType);
	}

}
