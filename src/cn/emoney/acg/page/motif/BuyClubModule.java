package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

public class BuyClubModule {
	private static BuyClubModule mInstance = null;

	private List<BuyGroupData> mLstGroupData = new ArrayList<BuyGroupData>();

	private BuyClubModule() {

	}

	public static BuyClubModule getInstance() {
		if (mInstance == null) {
			mInstance = new BuyClubModule();
		}

		return mInstance;
	}

	public void clearGroupData() {
		mLstGroupData.clear();
	}

	public void addGroupData(BuyGroupData data) {
		mLstGroupData.add(data);
	}

	public int getGroupDataSize() {
		return mLstGroupData.size();
	}

	public BuyGroupData getGroupData(int index) {
		if (index < getGroupDataSize()) {
			return mLstGroupData.get(index);
		}

		return null;
	}

	public void addData(BuyGroupData data) {
		addGroupData(data);
	}

	public int getDataSize() {
		return getGroupDataSize();
	}

	public BuyGroupData getData(int index) {
		return getGroupData(index);
	}

	public void clearData() {
		clearGroupData();
	}

	public void letFree() {
		mInstance = null;
	}
}
