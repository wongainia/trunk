package cn.emoney.acg.helper.push;

import android.content.Context;
import android.content.Intent;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.helper.NetworkManager;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class RedPointNoticeManager implements NetworkManager.InfoCallBack {
	private Context mContext = null;
	private NetworkManager mNetworkUtil = null;
	
	public RedPointNoticeManager(Context context) {
		mContext = context;
	}
	
	public void request() {
		// {"types":["msg", "alarm", "zuhe"]}
	    
		JSONObject reqJObj = new JSONObject();
		JSONArray typeJAry = new JSONArray();
		
		UserInfo userInfo = DataModule.getInstance().getUserInfo();
		if (userInfo == null) {
			return;
		}
		if (userInfo.isLogined()) {
			typeJAry.add("alarm");
			typeJAry.add("zuhe");
		}
		typeJAry.add("msg");
		
		reqJObj.put("types", typeJAry);
		
		mNetworkUtil = new NetworkManager(mContext, this, null);
		mNetworkUtil.requestInfo(reqJObj, IDUtils.ID_RED_POINT_NOTICE);
	}
	
	@Override
	public void updateFromInfo(int retCode, String retMsg, InfoPackageImpl pkg) {
		if (retCode == 0) {
			int id = pkg.getRequestType();
			if (id == IDUtils.ID_RED_POINT_NOTICE) {
				GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
				MessageCommon mc = gmp.getResponse();
				if (mc == null || mc.getMsgData() == null) {
					return;
				}
				
				/*
				 * { "result":0, "rep":{"msg" :1,"alarm" :1,"zuhe" :1} }
				 */
				
				String msgData = mc.getMsgData();
				try {
					JSONObject retJObj = JSONObject.parseObject(msgData);
					int ret = retJObj.getIntValue("result");
					if (ret == 0) {
						JSONObject redPointInfo = retJObj.getJSONObject("rep");
						LogUtil.easylog("redPointInfo:" + redPointInfo.toJSONString());
						int msgCount = -1;
						int alarmCount = -1;
						int groupCount = -1;
						
						if (redPointInfo.containsKey("msg")) {
							msgCount = redPointInfo.getIntValue("msg");
						}
						if (redPointInfo.containsKey("alarm")) {
							alarmCount = redPointInfo.getIntValue("alarm");
						}
						if (redPointInfo.containsKey("zuhe")) {
							groupCount = redPointInfo.getIntValue("zuhe");
						}

						GlobalDBHelper dbHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
						// {"msg":[25,true],"alarm":[33,false],"zuhe":[0,true]}
						String jstringRedpoint = dbHelper.getString(DataModule.G_KEY_RED_POINT_NOTICE, "{}");
						JSONObject jObject = JSONObject.parseObject(jstringRedpoint);
						if (jObject != null) {
							if (jObject.containsKey("msg") && msgCount >= 0) {
								JSONArray jary = jObject.getJSONArray("msg");
								if (msgCount > jary.getIntValue(0)) {
									jary.set(0, msgCount);
									jary.set(1, true);
								}
							} else if (msgCount >= 0) {
								JSONArray tJAry = new JSONArray();
								tJAry.add(msgCount);
								tJAry.add(msgCount > 0 ? true : false);
								jObject.put("msg", tJAry);
							}

							if (jObject.containsKey("alarm") && alarmCount >= 0) {
								JSONArray jary = jObject.getJSONArray("alarm");
								if (alarmCount > jary.getIntValue(0)) {
									jary.set(0, alarmCount);
									jary.set(1, true);
								}
							} else if (alarmCount >= 0) {
								JSONArray tJAry = new JSONArray();
								tJAry.add(alarmCount);
								tJAry.add(alarmCount > 0 ? true : false);
								jObject.put("alarm", tJAry);
							}

							if (jObject.containsKey("zuhe")) {
								JSONArray jary = jObject.getJSONArray("zuhe");
								if (groupCount > jary.getIntValue(0) && groupCount >= 0) {
									jary.set(0, groupCount);
									jary.set(1, true);
								}
							} else if (groupCount >= 0) {
								JSONArray tJAry = new JSONArray();
								tJAry.add(groupCount);
								tJAry.add(groupCount > 0 ? true : false);

								jObject.put("zuhe", tJAry);
							}
						}

						dbHelper.setString(DataModule.G_KEY_RED_POINT_NOTICE, jObject.toJSONString());

						Intent bcdcIntent = new Intent();
						bcdcIntent.setAction(BroadCastName.BCDC_RED_POINT_UPDATE);
						mContext.sendBroadcast(bcdcIntent);

					}
				} catch (Exception e) {
				}
			}
		}

	}

	public static void updateRedPointDisplay(Context context, String typeKey, boolean isNew) {
		GlobalDBHelper dbHelper = new GlobalDBHelper(context, DataModule.DB_GLOBAL);
		String sRed = dbHelper.getString(DataModule.G_KEY_RED_POINT_NOTICE, "{}");
		try {
			try {
				JSONObject jObjRed = JSONObject.parseObject(sRed);
				if (jObjRed == null) {
					return;
				}

				if (typeKey != null && !typeKey.equals("")) {
					if (jObjRed.containsKey(typeKey)) {
						JSONArray jary = jObjRed.getJSONArray(typeKey);
						jary.set(1, isNew);
						jObjRed.put(typeKey, jary);
					}
				} else {
					if (jObjRed.containsKey("msg")) {
						JSONArray jary = jObjRed.getJSONArray("msg");
						jary.set(1, isNew);
						jObjRed.put("msg", jary);
					}

					if (jObjRed.containsKey("alarm")) {
						JSONArray jary = jObjRed.getJSONArray("msg");
						jary.set(1, isNew);
						jObjRed.put("msg", jary);
					}
					
					if (jObjRed.containsKey("zuhe")) {
						JSONArray jary = jObjRed.getJSONArray("zuhe");
						jary.set(1, isNew);
						jObjRed.put("zuhe", jary);
					}
				}
				
				dbHelper.setString(DataModule.G_KEY_RED_POINT_NOTICE, jObjRed.toJSONString());
			} catch (Exception e) {
			}

		} catch (Exception e) {
		}
	}

	public static boolean getRedpointDisplay(Context context, String typeKey) {

		GlobalDBHelper dbHelper = new GlobalDBHelper(context, DataModule.DB_GLOBAL);
		String sRed = dbHelper.getString(DataModule.G_KEY_RED_POINT_NOTICE, "{}");
		try {
			try {
				JSONObject jObjRed = JSONObject.parseObject(sRed);
				if (jObjRed == null) {
					return false;
				}

				if (typeKey != null && !typeKey.equals("")) {
					if (jObjRed.containsKey(typeKey)) {
						JSONArray jary = jObjRed.getJSONArray(typeKey);
						return jary.getBooleanValue(1);
					}

					return false;
				}

				int nRet = 0;
				if (jObjRed.containsKey("msg")) {
					if (jObjRed.getJSONArray("msg").getBoolean(1)) {
						nRet++;
					}
				}

				if (jObjRed.containsKey("alarm")) {
					if (jObjRed.getJSONArray("alarm").getBoolean(1)) {
						nRet++;
					}
				}

				if (jObjRed.containsKey("zuhe")) {
					if (jObjRed.getJSONArray("zuhe").getBoolean(1)) {
						nRet++;
					}
				}

				return nRet > 0 ? true : false;
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}
		return false;
	}

}
