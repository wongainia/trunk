package cn.emoney.acg.page.motif;

import java.util.ArrayList;

import android.content.Context;
import cn.emoney.acg.data.DataModule;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class GroupPraiseModule {
	private static GroupPraiseModule mInstance = null;
	private Context mContext = null;
	private GlobalDBHelper mDBHelper;

	private ArrayList<Integer> mLstPraiseGroup = new ArrayList<Integer>();

	private GroupPraiseModule() {

	}

	public static GroupPraiseModule getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new GroupPraiseModule();
			mInstance.mContext = context;
			mInstance.loadDb();
		}

		return mInstance;
	}

	@SuppressWarnings("unchecked")
	private void loadDb() {
		String t_praise = getDBHelper().getString(DataModule.G_KEY_GROUP_PRAISED, "");
		if (t_praise != null && !t_praise.equals("")) {
			try {

				JSONObject jObjPraise = JSONObject.parseObject(t_praise);
				if (jObjPraise != null && jObjPraise.containsKey(DataModule.G_CURRENT_SERVER_DATE + "")) {
					String jAryPraise = jObjPraise.getString(DataModule.G_CURRENT_SERVER_DATE + "");
					if (jAryPraise != null && !jAryPraise.equals("")) {
						ArrayList<Integer> t_lst = JSON.parseObject(jAryPraise, mLstPraiseGroup.getClass());
						mLstPraiseGroup.addAll(t_lst);
					}
				} else {
					getDBHelper().setString(DataModule.G_KEY_GROUP_PRAISED, "");
				}
			} catch (Exception e) {
			}
		}
	}

	private GlobalDBHelper getDBHelper() {
		if (mDBHelper == null) {
			mDBHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
		}
		return mDBHelper;
	}

	public void saveDb() {
		JSONObject jObjPraise = new JSONObject();
		String sPraise = JSON.toJSONString(mLstPraiseGroup);
		jObjPraise.put(DataModule.G_CURRENT_SERVER_DATE + "", sPraise);
		getDBHelper().setString(DataModule.G_KEY_GROUP_PRAISED, jObjPraise.toJSONString());

	}

	public boolean contains(int groupId) {
		return mLstPraiseGroup.contains(groupId);
	}

	public void addPraise(int groupid) {
		if (!mLstPraiseGroup.contains(groupid)) {
			mLstPraiseGroup.add(groupid);
		}
	}

	public static void letFree() {
		mInstance = null;
	}

}
