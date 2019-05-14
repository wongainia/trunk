// package cn.emoney.acg.helper.push;
//
// import android.content.Context;
// import cn.emoney.acg.data.DataModule;
// import cn.emoney.sky.libs.db.GlobalDBHelper;
//
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
//
// /**
// * 管理预警本地存储
// *
// * @author emoney_sky
// *
// */
// public class AlarmDataManager {
// public static final int TYPE_STOCK_ALARM = 1;
// public static final int TYPE_BUYCLUB_ALARM = 2;
//
// /**
// * @param context
// * @param type
// * @param alarmItem
// * 格式:{"content":"xxxxx", "id":"1000022",
// * "time":"2015-10-09 10:30:22.000", "alarmType":1}
// */
// public static void addAlarm(Context context, int type, String alarmItem) {
// String sKey = getKey(type);
//
// if (sKey != null && !sKey.equals("")) {
// GlobalDBHelper dbHelper = getDBHelper(context);
// String tAlarm = dbHelper.getString(sKey, "[]");
// try {
// JSONArray jaryAlarm = JSONArray.parseArray(tAlarm);
// if (jaryAlarm.size() >= 20) {
// for (int i = 19; i < jaryAlarm.size(); i++) {
// jaryAlarm.remove(i);
// }
// }
//
//
// jaryAlarm.add(0, JSONObject.parse(alarmItem));
// dbHelper.setString(sKey, jaryAlarm.toJSONString());
// } catch (Exception e) {
// }
//
// }
// }
//
// public static JSONArray getAlarm(Context context, int type) {
// JSONArray jAryAlarm = null;
//
// String sKey = getKey(type);
//
// if (sKey != null && !sKey.equals("")) {
// GlobalDBHelper dbHelper = getDBHelper(context);
// String tAlarm = dbHelper.getString(sKey, "[]");
//
// try {
// jAryAlarm = JSONArray.parseArray(tAlarm);
// } catch (Exception e) {
// }
// }
//
// return jAryAlarm;
// }
//
// /**
// *
// * @param context
// * @param type
// * @param id
// * @return 格式{"content":"xxxxx", "id":1000022,
// * "time":"2015-10-09 10:30:22.000", "alarmType":1}
// */
// public static JSONObject getLastAlarmInfoById(Context context, int type, int id) {
// JSONObject jobjAlarm = null;
// String sKey = getKey(type);
//
// if (sKey != null && !sKey.equals("")) {
// GlobalDBHelper dbHelper = getDBHelper(context);
// String tAlarm = dbHelper.getString(sKey, "[]");
// try {
// JSONArray jaryAlarm = JSONArray.parseArray(tAlarm);
// for (int i = 0; i < jaryAlarm.size(); i++) {
// JSONObject alarm = jaryAlarm.getJSONObject(i);
// if (alarm.getIntValue("id") == id) {
// jobjAlarm = alarm;
// break;
// }
// }
//
// } catch (Exception e) {
// }
// }
// return jobjAlarm;
// }
//
// private static String getKey(int type) {
// String tkey = null;
// switch (type) {
// case TYPE_STOCK_ALARM:
// tkey = DataModule.G_KEY_ALARM_STOCK_LIST;
// break;
// case TYPE_BUYCLUB_ALARM:
// tkey = DataModule.G_KEY_ALARM_BUYCLUB_LIST;
// break;
// default:
// break;
// }
//
// return tkey;
// }
//
// private static GlobalDBHelper getDBHelper(Context context) {
// return new GlobalDBHelper(context, DataModule.DB_GLOBAL);
// }
// }
