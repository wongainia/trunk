package cn.emoney.acg.helper.alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.text.TextUtils;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.page.PageImpl;

/**
 * 管理股票预警数据
 * */
public class StockAlertManager implements KeysInterface {

	/**
	 * 个股预警最多允许设置数量
	 * */
	private final int MAX_ALERT_COUNT = 20;

	private static StockAlertManager instance;

	/**
	 * 是否添加key，默认为true。 当获取预警列表返回retCode为0时，置为false. 当获取预警列表返回retCode为8时，置为true.
	 * 当为true时，request update 的option为1
	 * */
	private boolean isAddKey = true;

	/**
	 * 数据版本，默认为3
	 * */
	private int dataVersion = 3;

	/**
	 * 存储各支股票预警数据 - HashMap格式，key为股票id，value为股票预警数据的json字符串
	 * */
	private Map<String, String> mapStockAlerts = new HashMap<String, String>();

	private StockAlertManager() {
	}

	public static StockAlertManager getInstance() {
		if (instance == null) {
			synchronized (StockAlertManager.class) {
				if (instance == null) {
					instance = new StockAlertManager();
				}
			}
		}

		return instance;
	}

	/**
	 * 将预警配置信息由json格式转换为map，缓存起来
	 * */
	public void cacheStockWarnJson(String configJson, PageImpl page) {
		// 如果json为空，直接返回
		if (TextUtils.isEmpty(configJson)) {
			return;
		}

		// 如果json格式不正确，直接返回
		JSONObject obj = null;
		try {
			obj = JSONObject.parseObject(configJson);
		} catch (Exception e) {
		}
		if (obj == null) {
			return;
		}

		// 获取dataVersion
		dataVersion = obj.getIntValue("ver");

		// 获取data并存储到hashMap中
		JSONArray array = obj.getJSONArray("data");
		if (array != null && array.size() > 0) {
			mapStockAlerts.clear();
			int size = array.size();
			for (int i = 0; i < size; i++) {
				JSONObject objItem = array.getJSONObject(i);
				if (objItem != null) {
					String id = objItem.getString("id");
					String json = objItem.toJSONString();

					mapStockAlerts.put(id, json);
				}
			}
		}

	}

	/**
	 * 获取缓存中是否已缓存有预警数据
	 * */
	public boolean isCacheHasData() {
		if (mapStockAlerts != null && mapStockAlerts.size() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * 获取已设置预警个股数量
	 * */
	public int getAlertCount() {
		if (mapStockAlerts != null) {
			return mapStockAlerts.size();
		}

		return 0;
	}

	/**
	 * 判断是否允许继续添加预警设置
	 * */
	public boolean isSetAlertAllowed(String goodsId) {

		// 如果已缓存当前股票的预警数据，就允许修改预警设置
		if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
			return true;
		}

		// 对于没有设置过预警的个股，如果设置预警个股数量已达到数量限制，就不再允许继续设置预警
		if (getAlertCount() < MAX_ALERT_COUNT) {
			return true;
		}

		return false;
	}

	/**
	 * 判断某支股票是否已设置预警
	 * */
	public boolean isStockHasSetAlert(String goodsId) {
		if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
			return true;
		}

		return false;
	}

	/**
	 * 添加、更新、删除网络端预警配置信息
	 * 
	 * @param option
	 *            1 add, 2 delete, 3 update
	 * */
	public void requestUpdateStockWarnList(String stockId, String value, PageImpl page, short requestFlag) {
		// 如果没有登录，直接退出
		if (!page.isLogined())
			return;

		UserInfo userInfo = DataModule.getInstance().getUserInfo();

		List<String> tLstStockIds = new ArrayList<String>(1);
		tLstStockIds.add(stockId);

		List<String> tLstValues = new ArrayList<String>(1);
		tLstValues.add(value);

		JSONObject json = new JSONObject();

		try {
			json.put(KEY_TOKEN, userInfo.getToken());
			json.put(KEY_KEY, "key_alarms_setting");
			if (isAddKey) {
				json.put(KEY_OP, 1);

				json.put(KEY_VALUE, getAddValue(tLstValues));
			} else {
				json.put(KEY_OP, 3);
				json.put(KEY_VALUE, getUpdateValue(tLstStockIds, tLstValues));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		page.requestInfo(json, IDUtils.ID_USER_DATA_UPDATE, requestFlag);
	}

	/**
	 * 添加、更新、删除网络端预警配置信息
	 * 
	 * @param option
	 *            1 add, 2 delete, 3 update
	 * */
	public void requestUpdateStockWarnList(List<String> lstStockIds, List<String> lstValues, PageImpl page, short requestFlag) {
		// 如果没有登录，直接退出
		if (!page.isLogined())
			return;

		UserInfo userInfo = DataModule.getInstance().getUserInfo();

		JSONObject json = new JSONObject();
		try {
			json.put(KEY_TOKEN, userInfo.getToken());
			json.put(KEY_KEY, "key_alarms_setting");
			if (isAddKey) {
				json.put(KEY_OP, 1);
				json.put(KEY_VALUE, getAddValue(lstValues));
			} else {
				json.put(KEY_OP, 3);
				json.put(KEY_VALUE, getUpdateValue(lstStockIds, lstValues));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		page.requestInfo(json, IDUtils.ID_USER_DATA_UPDATE, requestFlag);
	}

	private String getUpdateValue(List<String> lstStockIds, List<String> lstValues) {
		// 添加、更新、删除股票预警
		/*
		 * 如果map中不包含该支股票预警数据，直接添加到map中 如果map中包含该支股票预警数据，如果value为空，删除该股票预警数据
		 * 如果value不为空，更新该股票预警数据
		 * 
		 * 遍历map中每一项，如果不是stockId，就把它添加到JSON中， 如果是stockId，分两种情况，
		 * 如果value为空，跳过这一项，如果不为空，把value作为值添加到JSON中，最后返回JSON的字符串
		 */

		if (lstStockIds == null || lstValues == null || lstStockIds.size() == 0 || lstValues.size() == 0 || lstStockIds.size() != lstValues.size()) {
			return null;
		}

		try {
			JSONObject obj = new JSONObject();

			obj.put("ver", dataVersion);

			if (mapStockAlerts != null) {
				JSONArray array = new JSONArray();

				for (int i = 0; i < lstStockIds.size(); i++) {
					String sStockId = lstStockIds.get(i);
					String sValue = lstValues.get(i);

					if (TextUtils.isEmpty(sStockId)) {
						continue;
					}
					if (!mapStockAlerts.containsKey(sStockId)) {
						// 添加
						if (!TextUtils.isEmpty(sValue)) {
							try {
								JSONObject objItem = JSONObject.parseObject(sValue);
								array.add(objItem);
							} catch (Exception e) {
							}
						}
					}
				}

				// 更新或删除
				Set<String> keys = mapStockAlerts.keySet();
				for (String key : keys) {
					int index = lstStockIds.indexOf(key);
					if (index >= 0) {
						String sValue = lstValues.get(index);
						if (TextUtils.isEmpty(sValue)) {
							// 跳过，不添加
						} else {
							try {
								JSONObject objItem = JSONObject.parseObject(sValue);
								array.add(objItem);
							} catch (Exception e) {
							}
						}
					} else {
						String itemValue = mapStockAlerts.get(key);
						try {
							JSONObject objItem = JSONObject.parseObject(itemValue);
							array.add(objItem);
						} catch (Exception e) {
						}
					}
				}

				obj.put("data", array);
			} else {
				obj.put("data", JSONArray.parse("[]"));
			}

			return obj.toJSONString();
		} catch (Exception e) {
		}

		return null;
	}

	private String getAddValue(List<String> lstValues) {
		if (lstValues == null) {
			return null;
		}

		JSONObject obj = new JSONObject();

		obj.put("ver", dataVersion);

		JSONArray array = new JSONArray();
		for (String sVaule : lstValues) {
			try {
				JSONObject objItem = JSONObject.parseObject(sVaule);
				array.add(objItem);
			} catch (Exception e) {
			}

		}
		obj.put("data", array);

		return obj.toJSONString();
	}

	/**
	 * 更新缓存数据 map 必须首先更新网络端数据且成功后，才能更新缓存数据，否则如果更新了本地缓存数据后，更新网络数据失败，会造成两端数据不同步
	 * */
	public void updateCacheAlerts(String stockId, String value) {
		// 更新或删除map中存储的某支股票的预警数据
		// 若value不为空，将map中id项数据替换为value，若value为空，将map中id项数据删除
		if (TextUtils.isEmpty(value)) {
			if (mapStockAlerts.containsKey(stockId)) {
				// 将id项从map中删除
				mapStockAlerts.remove(stockId);
			}
		} else {
			// 更新缓存或添加到缓存
			mapStockAlerts.put(stockId, value);
		}
	}

	/**
	 * 获取单股预警配置数据
	 * */
	public String getStockAlert(PageImpl page, String goodsId) {
		if (mapStockAlerts != null && mapStockAlerts.containsKey(goodsId)) {
			return mapStockAlerts.get(goodsId);
		}

		return null;
	}

	/**
	 * 发送网络请求，获取个股预警列表数据
	 * */
	public void requestStockWarnList(PageImpl page) {
		// 如果没有登录，直接退出
		if (!page.isLogined())
			return;

		UserInfo userInfo = DataModule.getInstance().getUserInfo();
		String key = "key_alarms_setting";

		JSONObject json = new JSONObject();
		try {
			json.put(KEY_TOKEN, userInfo.getToken());
			json.put(KEY_KEY, key);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		page.requestInfo(json, IDUtils.ID_USER_DATA);
	}

	/**
	 * 发送网络请求，获取个股预警列表数据
	 * */
	public void requestStockWarnList(PageImpl page, short reqFlag) {
		// 如果没有登录，直接退出
		if (!page.isLogined())
			return;

		UserInfo userInfo = DataModule.getInstance().getUserInfo();
		String key = "key_alarms_setting";

		JSONObject json = new JSONObject();
		try {
			json.put(KEY_TOKEN, userInfo.getToken());
			json.put(KEY_KEY, key);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		page.requestInfo(json, IDUtils.ID_USER_DATA, reqFlag);
	}

	/**
	 * 如果缓存无预警数据，从后台获取
	 * */
	public void requestStockAlertsIfCashEmpty(PageImpl page, short reqFlag) {
		if (!isCacheHasData()) {
			requestStockWarnList(page, reqFlag);
		}
	}

	/**
	 * 根据StockId获取StockCode, StockName
	 * */
	private Goods getStockInfoById(PageImpl page, String stockId) {
		ArrayList<Goods> lst = null;

		lst = page.getSQLiteDBHelper().queryStockInfosByCode2(stockId, 1);

		Goods g = null;
		if (lst != null && lst.size() > 0) {
			g = lst.get(0);
		} else {
			// 从码表中查询不到该股票或版块
			g = new Goods(Integer.parseInt(stockId), "未知名称");
			g.setSupport(false);
		}

		return g;
	}

	public Map<String, String> getMapStockWarn() {
		return mapStockAlerts;
	}

	/**
	 * 更新指定个股最新预警时间
	 * */
	public void updateLastAlertTime(PageImpl page, String id, String time) {
		if (TextUtils.isEmpty(id) || TextUtils.isEmpty(time)) {
			return;
		}

		// 从SP中获取缓存的内容，如果缓存中无内容，默认为"{}"
		String origin = page.getDBHelper().getString(DataModule.G_KEY_LAST_STOCK_ALERT_TIME, "{}");
		JSONObject obj = JSONObject.parseObject(origin);
		if (obj == null) {
			obj = new JSONObject();
		}

		// 将id和time写入json中，如果之前已存在，则覆盖
		obj.put(id, time);

		// 更新SP缓存中内容
		page.getDBHelper().setString(DataModule.G_KEY_LAST_STOCK_ALERT_TIME, obj.toJSONString());
	}

	/**
	 * 获取预警配置列表显示的数据
	 * */
	public List<StockWarnInfo> getWarnListInfos(PageImpl page) {
		List<StockWarnInfo> listInfos = null;

		// 1. 从map中获取数据
		if (mapStockAlerts != null) {
			listInfos = new ArrayList<StockWarnInfo>();

			Set<String> keys = mapStockAlerts.keySet();
			for (String key : keys) {
				String value = mapStockAlerts.get(key);
				JSONObject objItem = JSONObject.parseObject(value);
				if (objItem != null) {
					String id = objItem.getString("id");

					Goods goods = getStockInfoById(page, id);
					if (goods != null) {
						// 2. 获取最近更新时间
						String lastUpdateTime = getLastAlertTime(page, id);
						StockWarnInfo info = new StockWarnInfo(lastUpdateTime, goods);
						listInfos.add(info);
					}
				}
			}
		}

		// 3. 排序
		if (listInfos != null && listInfos.size() > 0) {
			Collections.sort(listInfos, new WarnDataComparator(true));
		}

		return listInfos;
	}

	/**
	 * 获取指定个股最新预警时间
	 * */
	private String getLastAlertTime(PageImpl page, String id) {
		String lastAlertTime = "--";

		if (TextUtils.isEmpty(id)) {
			return lastAlertTime;
		}

		// 从SP中获取缓存的内容，如果缓存中无内容，默认为"{}"
		String origin = page.getDBHelper().getString(DataModule.G_KEY_LAST_STOCK_ALERT_TIME, "{}");
		JSONObject obj = JSONObject.parseObject(origin);
		if (obj == null) {
			obj = new JSONObject();
		}

		// 从缓存中获取指定id对应的时间，如果缓存中不包含指定id，返回默认值
		if (obj.containsKey(id)) {
			String time = obj.getString(id);

			if (!TextUtils.isEmpty(time)) {
				lastAlertTime = time;
			}
		}

		return lastAlertTime;
	}

	/**
	 * 清空缓存数据
	 * */
	public void clearCache() {
		if (mapStockAlerts != null) {
			mapStockAlerts.clear();
		}
	}

	public void setIsAddKey(boolean isAdd) {
		isAddKey = isAdd;
	}

	/**
	 * 工具类，存储预警列表中每个Item包含的数据
	 * */
	public class StockWarnInfo {
		public String latestWarnTime = "";
		public Goods goods;

		public StockWarnInfo() {
		}

		public StockWarnInfo(String latestWarnTime, Goods goods) {
			super();
			this.latestWarnTime = latestWarnTime;
			this.goods = goods;
		}

	}

	private class WarnDataComparator implements Comparator<StockWarnInfo> {

		int type = 1;

		public WarnDataComparator(boolean desc) {
			if (desc) {
				type = -type;
			}
		}

		@Override
		public int compare(StockWarnInfo info0, StockWarnInfo info1) {
			String time1 = info0.latestWarnTime;
			String time2 = info1.latestWarnTime;

			return type * (time1.equals(time2) ? 0 : (time1.compareTo(time2) > 0 ? 1 : -1));
		}

	}

}
