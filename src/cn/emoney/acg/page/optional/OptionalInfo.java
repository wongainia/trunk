package cn.emoney.acg.page.optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class OptionalInfo {

    private HashMap<String, ArrayList<Goods>> mMapGoods = new HashMap<String, ArrayList<Goods>>();

    public static final String KEY_OPTIONAL_GOODS = "key_optional_goods";
    public static final String KEY_OPTIONAL_TYPE = "key_optional_type";

    private final String KEY_OPTIONAL_GOODS_VISITORS = "key_optional_goods_visitors";
    private final String KEY_OPTIONAL_TYPE_VISITORS = "key_optional_type_visitors";

    public final static String TYPE_DEFAULT = "所有自选";
    public final static String TYPE_POSITION = "持仓";
    public final static String TYPE_1 = "自选1";
    public final static String TYPE_2 = "自选2";

    public final static String TYPE_KEY_ALL = ".";
    private List<String> mLstTypes = new ArrayList<String>();

    public OptionalInfo() {
        addType(TYPE_DEFAULT);
        addType(TYPE_POSITION);
        addType(TYPE_1);
        addType(TYPE_2);
    }

    public void addType(String typeName) {
        if (hasType(typeName) < 0) {
            mLstTypes.add(typeName);
            ArrayList<Goods> t_lstGoods = new ArrayList<Goods>();
            mMapGoods.put(typeName, t_lstGoods);
        }
    }

    public void addType(String typeName, int position) {
        if (hasType(typeName) < 0) {
            int index = position > mLstTypes.size() ? mLstTypes.size() : position;
            mLstTypes.add(index, typeName);
            ArrayList<Goods> t_lstGoods = new ArrayList<Goods>();
            mMapGoods.put(typeName, t_lstGoods);
        } else {
            mLstTypes.remove(typeName);
            int index = position > mLstTypes.size() ? mLstTypes.size() : position;
            mLstTypes.add(position, typeName);
        }
    }

    // -2: 不能删除该分类 ; -1:无此分类; 0:删除成功
    public int removeType(String typeName) {
        if (typeName.equals(OptionalInfo.TYPE_DEFAULT) || typeName.equals(OptionalInfo.TYPE_POSITION)) {
            return -2;
        }
        if (hasType(typeName) >= 0) {
            mLstTypes.remove(typeName);
            mMapGoods.remove(typeName);
            return 0;
        } else {
            return -1;
        }
    }

    // -2:oldType无效; 0:修改成功
    public int updateTypeName(String oldType, String newType) {
        int index = hasType(oldType);
        if (index >= 0) {
            mLstTypes.set(index, newType);
            ArrayList<Goods> t_lst = mMapGoods.get(oldType);
            mMapGoods.remove(oldType);
            mMapGoods.put(newType, t_lst);
            return 0;
        } else {
            return -2;
        }
    }

    // 返回type的下标,找不到,返回-1
    public int hasType(String typeName) {
        for (int i = 0; i < mLstTypes.size(); i++) {
            if (mLstTypes.get(i).equals(typeName)) {
                return i;
            }
        }
        return -1;
    }

    public List<String> getTypes() {
        return mLstTypes;
    }

    public List<String> getTypesExcept(String type) {
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < mLstTypes.size(); i++) {
            String t = mLstTypes.get(i);
            if (!t.equals(type)) {
                types.add(t);
            }
        }
        return types;
    }

    public int getTypeCount() {
        return mLstTypes.size();
    }

    // return: -2:无此类型 ; -1:有类型,但无此股 ; >=0 ,index
    public int hasGoods(String type, int goodsId) {
        if (!mMapGoods.containsKey(type)) {
            return -2;
        }
        ArrayList<Goods> t_lstGoodsType = mMapGoods.get(type);
        for (int i = 0; i < t_lstGoodsType.size(); i++) {
            Goods goods = t_lstGoodsType.get(i);
            if (goods.getGoodsId() == goodsId) {
                return i;
            }
        }
        return -1;
    }
    
    public int hasGoods(int goodsId) {
        return hasGoods(TYPE_DEFAULT, goodsId);
    }

    // 返回第一次出现的loc
    public List<Object> hasGoodsExceptType(String type, int goodsId) {
        List<Object> lst_ret = null;
        for (String t_type : mLstTypes) {
            if (t_type.equals(type)) {
                continue;
            }
            int index = hasGoods(t_type, goodsId);
            if (index >= 0) {
                lst_ret = new ArrayList<Object>();
                lst_ret.add(t_type);
                lst_ret.add(index);
                break;
            }
        }

        return lst_ret;
    }

    public boolean addGoods(int goodsId, String goodsName) {
        return addGoods(new Goods(goodsId, goodsName));
    }

    public boolean addGoods(String type, int goodsId, String goodsName) {
        return addGoods(type, new Goods(goodsId, goodsName));
    }

    public boolean addGoods(Goods goods) {
        return addGoods(TYPE_DEFAULT, goods);
    }

    public boolean addGoods(String type, Goods goods) {

        int ret = hasGoods(type, goods.getGoodsId());

        ArrayList<Goods> t_lstGoodsType = null;
        if (ret == -2) { // 无此type
            return false;
        }

        else if (ret == -1) { // 有type,还未添加
            goods.addType(type);
            t_lstGoodsType = mMapGoods.get(type);
            t_lstGoodsType.add(goods);

            if (!type.equals(TYPE_DEFAULT)) {
                goods.addType(TYPE_DEFAULT);
                int ret_all = hasGoods(TYPE_DEFAULT, goods.getGoodsId());
                if (ret_all == -1) { // 在默认type中,还未添加
                    t_lstGoodsType = mMapGoods.get(TYPE_DEFAULT);
                    t_lstGoodsType.add(goods);
                }
            }

            return true;
        } else { // 已存在
            Goods g = getGoods(type, goods.getGoodsId());
            g.addType(type);
            return true;
        }

    }

    public Goods getGoods(String type, int goodsId) {
        if (hasGoods(type, goodsId) != -1) {
            ArrayList<Goods> t_lstGoodsType = mMapGoods.get(type);
            for (int i = 0; i < t_lstGoodsType.size(); i++) {
                Goods goods = t_lstGoodsType.get(i);
                if (goods.getGoodsId() == goodsId) {
                    return goods;
                }
            }
        }
        return null;
    }

    public boolean delGoods(String type, Goods goods) {
        int goodsId = goods.getGoodsId();
        return delGoods(type, goodsId);
    }

    public boolean delGoods(String type, int goodsId) {

        if (type.equals(OptionalInfo.TYPE_DEFAULT)) {
            List<String> tLstType = getTypes();
            for (int i = 0; i < tLstType.size(); i++) {
                int index = hasGoods(tLstType.get(i), goodsId);
                if (index >= 0) {
                    ArrayList<Goods> t_lstGoodsType = mMapGoods.get(tLstType.get(i));
                    t_lstGoodsType.remove(index);
                }
            }
            return true;
        } else {
            int index = hasGoods(type, goodsId);
            if (index >= 0) {
                ArrayList<Goods> t_lstGoodsType = mMapGoods.get(type);
                t_lstGoodsType.remove(index);
                return true;
            }
            return false;
        }
    }

    public void delGoods(String type, List<Goods> lst) {
        for (Goods g : lst) {
            delGoods(type, g);
        }
    }


    public List<Goods> getAllGoods() {
        return mMapGoods.get(TYPE_DEFAULT);
    }

    public List<Goods> getGoodsListByType(String type) {
        ArrayList<Goods> t_lstGoodsType = mMapGoods.get(type);

        return t_lstGoodsType;
    }

    public void addAll(String type, List<Goods> lstGoods) {
        for (int i = 0; i < lstGoods.size(); i++) {
            addGoods(type, lstGoods.get(i));
            if (!type.equals(TYPE_DEFAULT)) {
                addGoods(TYPE_DEFAULT, lstGoods.get(i));
            }
        }
    }

    public boolean isEmpty() {

        return getCount() == 0;
    }

    public int getCount() {
        int count = 0;
        Set<String> setKyes = mMapGoods.keySet();
        Iterator<String> it = setKyes.iterator();
        String sKey = "";
        while (it.hasNext()) {
            sKey = it.next();
            ArrayList<Goods> t_lst = mMapGoods.get(sKey);
            count += t_lst.size();
        }

        return count;
    }

    public int getCountByType(String type) {
        int count = 0;

        if (type != null) {
            if (hasType(type) >= 0) {
                ArrayList<Goods> t_lst = mMapGoods.get(type);
                count = t_lst.size();
            }
        }
        return count;
    }

    public void save(GlobalDBHelper dbHelper) {
        if (dbHelper == null) {
            return;
        }

        UserInfo userInfo = DataModule.getInstance().getUserInfo();

        String sKeyGoods;
        String sKeyType;
        if (userInfo.isLogined()) {
            sKeyGoods = KEY_OPTIONAL_GOODS;
            sKeyType = KEY_OPTIONAL_TYPE;


            return; // 登录用户不再保存自选股 20151112
        } else {
            sKeyGoods = KEY_OPTIONAL_GOODS_VISITORS;
            sKeyType = KEY_OPTIONAL_TYPE_VISITORS;
        }

        JSONObject jObj_optionalInfo = optionalMap2JsonObj();

        String str_optionalMap = jObj_optionalInfo.toString();
        dbHelper.setString(sKeyGoods, str_optionalMap);

        JSONArray jAry_optionalType = new JSONArray();
        for (String sType : mLstTypes) {
            jAry_optionalType.add(sType);
        }
        String s_optionalType = jAry_optionalType.toString();
        dbHelper.setString(sKeyType, s_optionalType);
    }

    public void saveVisitors(GlobalDBHelper dbHelper) {
        if (dbHelper == null) {
            return;
        }

        JSONObject jObj_optionalInfo = optionalMap2JsonObj();

        String str_optionalMap = jObj_optionalInfo.toString();
        dbHelper.setString(KEY_OPTIONAL_GOODS_VISITORS, str_optionalMap);

        JSONArray jAry_optionalType = new JSONArray();
        for (String sType : mLstTypes) {
            jAry_optionalType.add(sType);
        }
        String s_optionalType = jAry_optionalType.toString();
        dbHelper.setString(KEY_OPTIONAL_TYPE_VISITORS, s_optionalType);
    }

    public boolean load(GlobalDBHelper dbHelper) {
        if (dbHelper == null) {
            return false;
        }

        String str_optionalMap = dbHelper.getString(KEY_OPTIONAL_GOODS, "");
        String s_optionalType = dbHelper.getString(KEY_OPTIONAL_TYPE, "");

        if (str_optionalMap.equals("")) {
            return false;
        }

        try {
            this.clear();

            if (!s_optionalType.equals("")) {
                JSONArray jAry_optionalType = JSON.parseArray(s_optionalType);

                for (int i = 0; i < jAry_optionalType.size(); i++) {
                    addType(jAry_optionalType.getString(i));
                }
            }

            // JSONObject jsonObject = new JSONObject(str_optionalMap);
            JSONObject jsonObject = JSON.parseObject(str_optionalMap);
            Set<String> keySet = jsonObject.keySet();
            Iterator<String> it_type = keySet.iterator();
            String sKey = "";
            while (it_type.hasNext()) {
                sKey = it_type.next();
                addType(sKey);
                JSONArray jAry_oneType = jsonObject.getJSONArray(sKey);
                for (int i = 0; i < jAry_oneType.size(); i++) {
                    JSONArray jAry_oneGoods = jAry_oneType.getJSONArray(i);

                    int t_gid = jAry_oneGoods.getIntValue(0);

                    String t_gName = jAry_oneGoods.getString(1);
                    String t_pAmount = jAry_oneGoods.getString(2);
                    String t_pPrice = jAry_oneGoods.getString(3);
                    Goods g = new Goods(t_gid, t_gName);
                    g.setPositionAmount(t_pAmount);
                    g.setPositionPrice(t_pPrice);

                    for (int j = 4; j < jAry_oneGoods.size(); j++) {
                        String key_type = jAry_oneGoods.getString(j);
                        g.addType(key_type);
                    }
                    mMapGoods.get(sKey).add(g);
                }

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public boolean loadVisitors(GlobalDBHelper dbHelper) {
        if (dbHelper == null) {
            return false;
        }

        String str_optionalMap = dbHelper.getString(KEY_OPTIONAL_GOODS_VISITORS, "");
        String s_optionalType = dbHelper.getString(KEY_OPTIONAL_TYPE_VISITORS, "");

        if (str_optionalMap.equals("")) {
            return false;
        }

        try {
            this.clear();

            if (!s_optionalType.equals("")) {
                JSONArray jAry_optionalType = JSON.parseArray(s_optionalType);
                for (int i = 0; i < jAry_optionalType.size(); i++) {
                    addType(jAry_optionalType.getString(i));
                }
            }

            JSONObject jsonObject = JSON.parseObject(str_optionalMap);
            Iterator<String> it_type = jsonObject.keySet().iterator();
            String sKey = "";
            while (it_type.hasNext()) {
                sKey = it_type.next();
                addType(sKey);
                JSONArray jAry_oneType = jsonObject.getJSONArray(sKey);
                for (int i = 0; i < jAry_oneType.size(); i++) {
                    JSONArray jAry_oneGoods = jAry_oneType.getJSONArray(i);

                    int t_gid = jAry_oneGoods.getIntValue(0);
                    String t_gName = jAry_oneGoods.getString(1);
                    String t_pAmount = jAry_oneGoods.getString(2);
                    String t_pPrice = jAry_oneGoods.getString(3);
                    Goods g = new Goods(t_gid, t_gName);
                    g.setPositionAmount(t_pAmount);
                    g.setPositionPrice(t_pPrice);

                    for (int j = 4; j < jAry_oneGoods.size(); j++) {
                        String key_type = jAry_oneGoods.getString(j);
                        g.addType(key_type);
                    }
                    mMapGoods.get(sKey).add(g);
                }

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public void clear() {
        mMapGoods.clear();
        mLstTypes.clear();
    }

    public void reset(String type) {
        mMapGoods.get(type).clear();
    }

    public void reset() {
        mMapGoods.clear();
    }

    private JSONObject optionalMap2JsonObj() {
        JSONObject jsonObject = new JSONObject();
        try {
            for (String key_type : mLstTypes) {
                ArrayList<Goods> lst_goods = mMapGoods.get(key_type);
                JSONArray jAry_oneType = new JSONArray();
                for (Goods g : lst_goods) {
                    JSONArray jAry_oneGoods = new JSONArray();
                    jAry_oneGoods.add(g.getGoodsId());
                    jAry_oneGoods.add(g.getGoodsName());
                    List<String> lst_types = g.getTypes();

                    String sPositionAmount = g.getPositionAmount();
                    String sPositionPrice = g.getPositionPrice();
                    jAry_oneGoods.add(sPositionAmount);
                    jAry_oneGoods.add(sPositionPrice);

                    for (String type : lst_types) {
                        jAry_oneGoods.add(type);
                    }

                    jAry_oneType.add(jAry_oneGoods);
                }

                jsonObject.put(key_type, jAry_oneType);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject;
    }
}
