package cn.emoney.acg.page.motif;

import java.util.HashMap;
import java.util.Map;

public class MineGroupModule {
	public static final int MINE_TYPE_CREATE = 1;
	public static final int MINE_TYPE_BUY = 2;
	public static final int MINE_TYPE_FOCUS = 3;

	private Map<Integer, Integer> mMapMineType = new HashMap<Integer, Integer>();

	private static MineGroupModule mInstance = null;

	private MineGroupModule() {
	}

	public static MineGroupModule getInstance() {
		if (mInstance == null) {
			mInstance = new MineGroupModule();
		}

		return mInstance;
	}

	public void addMineType(int groupid, int type) {
		mMapMineType.put(groupid, type);
	}

	public void delMineType(int groupid) {
		if (mMapMineType.containsKey(groupid)) {
			mMapMineType.remove(groupid);
		}
	}

	public void clear() {
		mMapMineType.clear();
	}

	/**
	 * 
	 * @param groupid
	 * @return 1:create 2:buy 3:focus
	 */
	public int getMineType(int groupid) {
		if (mMapMineType.containsKey(groupid)) {
			return mMapMineType.get(groupid);
		}
		return -1;
	}

}
