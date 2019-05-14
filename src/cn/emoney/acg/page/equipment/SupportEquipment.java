package cn.emoney.acg.page.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.text.TextUtils;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.helper.DualHashMap;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class SupportEquipment {
    public static final int ID_KHero = 1000000; /* "K线英雄" */
    public static final int ID_CPX = 1101001; /* "操盘线" */
    public static final int ID_FLZT = 1201002; /* "飞龙在天" */
    public static final int ID_ZLZC = 1201003; /* "主力增仓" */
    public static final int ID_ZDLH = 1301002; /* "重大利好" */
    public static final int ID_ZLXC = 1201008; /* "主力吸筹" */
    public static final int ID_ZLQM = 1201009; /* "主力强买" */



    private String SUPPORT = "{\"1101001\":[\"操盘线\",\"B点买入，S点卖出，波段操作利器\",\"1101001\",\"0\",\"0\",\"1\"],\"1201002\":[\"飞龙在天\",\"识别强中恒强个股，短线激进\",\"1101001\",\"0\",\"0\",\"1\"],\"1201003\":[\"主力增仓\",\"10交易日内主力资金介入较多\",\"1101001\",\"0\",\"0\",\"1\"],\"1201008\":[\"主力吸筹\",\"主力开始吸筹操作\",\"1201008\",\"0\",\"1\",\"1\"],\"1201009\":[\"主力强买\",\"主力大量买进\",\"1201009\",\"0\",\"1\",\"1\"],\"1301002\":[\"重大利好\",\"公司出现重大利好消息\",\"1301002\",\"0\",\"2\",\"1\"],\"1101002\":[\"龙腾四海\",\"\",\"1101002\",\"0\",\"0\",\"1\"],\"1101003\":[\"按部就班\",\"\",\"1101003\",\"0\",\"0\",\"1\"],\"1101005\":[\"超级资金\",\"\",\"1101005\",\"0\",\"0\",\"1\"],\"1101007\":[\"大单比率\",\"\",\"1101007\",\"0\",\"0\",\"1\"]}";
    public static final int[] arySortEquip = new int[] {ID_FLZT, ID_ZDLH, ID_CPX, ID_ZLQM, ID_ZLZC, ID_ZLXC};

    private Map<Integer, Boolean> mMapPermission = new HashMap<Integer, Boolean>();
    /**
     * 装备id和装备名双向map
     */
    private DualHashMap mMapEquipId_Name = new DualHashMap();

    private static SupportEquipment mInstance = null;
    private JSONObject mJObjEquipment = null;

    private void create() {
        try {
            mJObjEquipment = JSON.parseObject(SUPPORT);
            mMapEquipId_Name.clear();
            Iterator<String> iterator = mJObjEquipment.keySet().iterator();
            while (iterator.hasNext()) {
                String skey = iterator.next();
                String sName = mJObjEquipment.getJSONArray(skey).getString(0);
                mMapEquipId_Name.put(skey, sName);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SupportEquipment() {
        create();
    }

    public static SupportEquipment getInstance() {
        if (mInstance == null) {
            mInstance = new SupportEquipment();
        }
        return mInstance;
    }

    public boolean isContain(int id) {
        String sId = String.valueOf(id);
        return isContain(sId);
    }

    public boolean isContain(String sId) {
        if (mJObjEquipment != null) {
            return mJObjEquipment.containsKey(sId);
        }

        return false;

    }

    public EquipmentData getById(int id) {
        String sId = String.valueOf(id);
        if (isContain(sId)) {
            try {
                EquipmentData data = new EquipmentData();
                JSONArray tJAry = mJObjEquipment.getJSONArray(sId);

                data.id = id;
                data.title = tJAry.getString(0);
                data.subTitle = tJAry.getString(1);
                data.imgId = tJAry.getString(2);
                data.noticePoint = Integer.valueOf(tJAry.getString(3));
                data.noticeFlag = Integer.valueOf(tJAry.getString(4));

                if (Integer.valueOf(tJAry.getString(5)) == 1) { // 该装备需要权限
                    data.hasPermission = getPermissionById(id);
                } else { // 无需权限,都可以使用
                    data.hasPermission = true;
                }

                return data;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void clearPermission() {
        mMapPermission.clear();
    }

    public void readPermission(String strJsonPermission) {
        if (strJsonPermission != null && !strJsonPermission.equals("") && !strJsonPermission.equals("null")) {
            try {
                JSONArray jAryPersimmion = JSON.parseArray(strJsonPermission);
                mMapPermission.clear();
                for (int i = 0; i < jAryPersimmion.size(); i++) {
                    JSONObject JobItem = jAryPersimmion.getJSONObject(i);
                    int id = JobItem.getIntValue("id");
                    if (isContain(id)) {
                        String sTime = DateUtils.formatInfoDate(JobItem.getString("end"), DateUtils.FormatInt);
                        String[] splitTime = sTime.split("-");
                        if (splitTime != null && splitTime.length == 2) {
                            int iDay = Integer.valueOf(splitTime[0]);
                            int iTime = Integer.valueOf(splitTime[1]);
                            if (iDay > DataModule.G_CURRENT_SERVER_DATE || (iDay == DataModule.G_CURRENT_SERVER_DATE && iTime > DataModule.G_CURRENT_SERVER_TIME)) {
                                mMapPermission.put(id, true);
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<EquipmentData> getSupportEquipList() {
        ArrayList<EquipmentData> lst = new ArrayList<EquipmentData>();
        for (int i = 0; i < arySortEquip.length; i++) {
            lst.add(getById(arySortEquip[i]));
        }
        return lst;
    }


    /**
     * 判断是否有使用权限
     * 
     * @param id 装备id
     * @return true:有权限; false:无权限
     */
    public boolean getPermissionById(int id) {
        boolean b = false;
        if (mMapPermission.containsKey(id)) {
            b = mMapPermission.get(id);
        }
        return b;
    }

    /**
     * 判断是否有使用权限
     * 
     * @param id 装备id
     * @return true:有权限; false:无权限
     */
    public boolean getPermissionByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        Object key = mMapEquipId_Name.getKeyByValue(name);
        if (key != null) {
            String sKey = (String) key;
            int id = DataUtils.convertToInt(sKey);
            return getPermissionById(id);
        }
        return false;
    }



}
