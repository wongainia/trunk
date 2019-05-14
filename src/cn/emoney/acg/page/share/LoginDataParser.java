package cn.emoney.acg.page.share;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;

import android.content.Context;
import android.text.TextUtils;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply.GoodsItem;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply.TypeItem;
import cn.emoney.acg.helper.db.DSQLiteDatabase;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.SupportEquipment;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.db.GlobalDBHelper;

/**
 * @ClassName: LoginDataParser
 * @Description:登录相关的数据解析
 * @author xiechengfa
 * @date 2015年11月20日 下午2:06:55
 *
 */
public class LoginDataParser {
    private PageImpl mPI = null;
    private DSQLiteDatabase mSqliteHelper;
    private GlobalDBHelper mDbHelper;
    private Context mContext = null;

    public LoginDataParser(Context mContext, PageImpl mPI) {
        this.mContext = mContext;
        this.mPI = mPI;
    }

    /**
     * 解析权限
     * 
     * @param msgData
     * @return
     * @throws Exception
     */
    public boolean pareserPermission(String msgData) throws Exception {
        try {
            LogUtil.easylog("login recv:ID_USER_PERMISSION:" + System.currentTimeMillis());
            JSONObject jsObj = JSON.parseObject(msgData);
            LogUtil.easylog("sky", "permission reply: " + jsObj.toString());
            int result = jsObj.getIntValue("result");
            if (result == 0) {
                String sPermission = jsObj.getString("right");
                SupportEquipment.getInstance().readPermission(sPermission);
                return true;
            } else {
                // 失败
                return false;
            }
        } catch (JSONException e) {
            // 修改jpush tag为游客
            e.printStackTrace();
            throw new Exception("Json解析异常");
        }
    }


    /**
     * 解析用户信息
     * 
     * @param msgData
     */
    public boolean pareserUserInfo(String msgData) throws Exception {
        try {
            JSONObject jsObj = JSON.parseObject(msgData);

            LogUtil.easylog("login recv:ID_USER_EXTRAINFO:" + System.currentTimeMillis() + ",json:" + msgData);

            String nick = jsObj.getString("nick");
            String headid = jsObj.getString("head_id");
            UserInfo userInfo = DataModule.getInstance().getUserInfo();
            boolean bFlag = false;
            if (DataUtils.convertToInt(headid) > 0) {
                userInfo.setHeadId(headid);
                bFlag = true;
            }
            if (nick != null && !nick.equals("")) {
                userInfo.setNickName(nick);
                bFlag = true;
            }
            if (bFlag) {
                userInfo.save(getDBHelper());
            }
            // 成功
            return true;
        } catch (JSONException e) {
            // 修改jpush tag为游客
            e.printStackTrace();
            throw new Exception("Json解析异常");
        }
    }

    /**
     * 解析自选股
     * 
     * @param msgData
     * @return
     * @throws Exception
     */
    public boolean parserOptionalInfo(String msgData) throws Exception {
        boolean res = false;
        try {
            OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            JSONObject js = JSON.parseObject(msgData);
            // LogUtil.easylog("sky", "收到自选股列表:" + js.toString());
            optionalInfo.clear();
            getSQLiteDBHelper();

            Iterator<String> iterator = js.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String type = null;
                if (key.equals(".")) {
                    type = OptionalInfo.TYPE_DEFAULT;
                } else {
                    type = key;
                }

                if (type.equals(OptionalInfo.TYPE_DEFAULT)) {
                    optionalInfo.addType(type, 0);
                } else if (type.equals(OptionalInfo.TYPE_POSITION)) {
                    List<String> t_lstTypes = optionalInfo.getTypes();
                    if (t_lstTypes != null && t_lstTypes.size() > 0 && t_lstTypes.get(0).equals(OptionalInfo.TYPE_DEFAULT)) {
                        optionalInfo.addType(type, 1);
                    } else {
                        optionalInfo.addType(type, 0);
                    }
                } else {
                    optionalInfo.addType(type);
                }

                String stocks = js.getString(key);
                String[] items = stocks.split(";");
                for (int i = 0; i < items.length; i++) {
                    String item = items[i].trim();

                    if (item != null && item.length() == 0) {
                        continue;
                    }
                    if (item.contains("#")) {
                        String[] s = item.split("#");

                        try {
                            int code = Integer.valueOf(s[0]);
                            if (code <= 0) {
                                LogUtil.easylog("MainPage->updateOptional->stockcode exception:" + item);
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                        if (s.length == 2) { // 持仓

                            Goods g = null;

                            String tFilter = Util.FormatStockCode(s[0]);
                            ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(tFilter, 1);
                            if (m_pGN != null && m_pGN.size() > 0) {
                                g = m_pGN.get(0);
                            } else {
                                LogUtil.easylog("MainPage->updateOptional->stockcode exception:" + item);
                                continue;
                                // TODO,暂不支持码表查不到的股票,如港股
                                // int goodsId = Integer.parseInt(s[0]);
                                // g = new Goods(goodsId, "");
                            }

                            if (s[1].equals("--") || s[1].equals("")) {
                                g.setPositionAmount("0");
                            } else {
                                g.setPositionAmount(s[1]);
                            }

                            optionalInfo.addGoods(type, g);

                        } else if (s.length == 3) { // 持仓,持仓价
                            Goods g = null;
                            String tFilter = Util.FormatStockCode(s[0]);
                            ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(tFilter, 1);
                            if (m_pGN != null && m_pGN.size() > 0) {
                                g = m_pGN.get(0);
                            } else {
                                LogUtil.easylog("MainPage->updateOptional->stockcode exception:" + item);
                                continue;
                                // TODO,暂不支持码表查不到的股票,如港股
                                // int goodsId = Integer.parseInt(s[0]);
                                // g = new Goods(goodsId, "");
                            }

                            if (s[1].equals("--") || s[1].equals("")) {
                                g.setPositionAmount("0");
                            } else {
                                g.setPositionAmount(s[1]);
                            }

                            if (s[2].equals("--") || s[2].equals("")) {
                                g.setPositionPrice("0.00");
                            } else {
                                g.setPositionPrice(s[2]);
                            }

                            optionalInfo.addGoods(type, g);
                        }
                    } else { // 无持仓数据
                        try {
                            int code = Integer.valueOf(item);
                            if (code <= 0) {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }

                        Goods g = null;
                        String tFilter = Util.FormatStockCode(item);
                        ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(tFilter, 1);
                        if (m_pGN != null && m_pGN.size() > 0) {
                            g = m_pGN.get(0);
                        } else {
                            LogUtil.easylog("MainPage->updateOptional->stockcode exception:" + item);
                            continue;
                            // TODO,暂不支持码表查不到的股票,如港股
                            // int goodsId = Integer.parseInt(item);
                            // g = new Goods(goodsId, "");
                        }

                        optionalInfo.addGoods(type, g);
                    }
                }
            }

            List<String> lstTypes = optionalInfo.getTypes();
            if (!lstTypes.contains(OptionalInfo.TYPE_POSITION)) {
                mPI.requestControlOptionalType(1, OptionalInfo.TYPE_POSITION, null, new OnOperateZXGListener() {

                    @Override
                    public void onOperate(boolean isSuccess, String msg) {
                        LogUtil.easylog("sky", "requestControlOptionalType -> isSuccess:" + isSuccess);
                    }
                });
                optionalInfo.addType(OptionalInfo.TYPE_POSITION);
            }

            // optionalInfo.save(getDBHelper());
            if (mPI != null) {
                mPI.sendBroadcast(BroadCastName.BCDC_OPTIONAL_DATA_UPDATE);
            }

            // // 修改更新状态
            // if (isRefreshUI) {
            // OptionalHome.bIsNeedRefresh = true;
            // }

            res = true;
        } catch (JSONException e) {
            // 修改jpush tag为游客
            res = false;
            e.printStackTrace();
            throw new Exception("Json解析异常");
        } finally {
            closeSQLDBHelper();
        }

        return res;
    }



    /**
     * 解析自选股
     * 
     * @param msgData
     * @return
     * @throws Exception
     */
    public boolean parserOptionalInfo_pb(OptionalShare_Reply reply) throws Exception {
        boolean ret = false;
        if (reply == null) {
            return ret;
        }
        try {
            OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            // JSONObject js = JSON.parseObject(msgData);
            // // LogUtil.easylog("sky", "收到自选股列表:" + js.toString());
            optionalInfo.clear();
            getSQLiteDBHelper();

            List<GoodsItem> lstAllGoods = reply.getGoodsListList();
            List<Goods> lstGoods = new ArrayList<Goods>();

            for (GoodsItem goodsItem : lstAllGoods) {
                int goodsid = goodsItem.getGoodsId();
                String tFilter = Util.FormatStockCode(goodsid);
                ArrayList<Goods> m_pGN = getSQLiteDBHelper().queryStockInfosByCode2(tFilter, 1);
                Goods g = null;
                if (m_pGN != null && m_pGN.size() > 0) {
                    g = m_pGN.get(0);
                } else {
                    g = new Goods(goodsid, "");
                    g.setSupport(false);
                }
                if (goodsItem.hasAmount()) {
                    g.setPositionAmount(goodsItem.getAmount() + "");
                }
                if (goodsItem.hasCostPrice()) {
                    String positonPrice = DataUtils.mDecimalFormat2.format(goodsItem.getCostPrice() / 100f);
                    g.setPositionPrice(positonPrice);
                }

                lstGoods.add(g);
            }

            List<TypeItem> lstItemTypes = reply.getTypeListList();

            for (TypeItem typeItem : lstItemTypes) {
                String type = typeItem.getTypeName();
                if (!TextUtils.isEmpty(type)) {
                    if (type.equals(".")) {
                        type = OptionalInfo.TYPE_DEFAULT;
                    }

                    if (type.equals(OptionalInfo.TYPE_DEFAULT)) {
                        optionalInfo.addType(type, 0);
                    } else if (type.equals(OptionalInfo.TYPE_POSITION)) {
                        List<String> t_lstTypes = optionalInfo.getTypes();
                        if (t_lstTypes != null && t_lstTypes.size() > 0 && t_lstTypes.get(0).equals(OptionalInfo.TYPE_DEFAULT)) {
                            optionalInfo.addType(type, 1);
                        } else {
                            optionalInfo.addType(type, 0);
                        }
                    } else {
                        optionalInfo.addType(type);
                    }

                    ByteString goodsIndex = typeItem.getRelativeGoodsIndex();
                    for (int i = 0; i < goodsIndex.size() / 2; i++) {
                        byte[] bytesIndex = new byte[2];
                        goodsIndex.copyTo(bytesIndex, i * 2, 0, 2);
                        int iIndex = byteArrayToInteger(bytesIndex);
                        if (iIndex >= 0 && iIndex < lstGoods.size()) {
                            Goods g = lstGoods.get(iIndex);
                            if (g.isSupport()) {
                                optionalInfo.addGoods(type, g);
                            }
                        }
                    }
                }
            }

            List<String> tLstType = optionalInfo.getTypes();
            if (!tLstType.contains(OptionalInfo.TYPE_POSITION)) {
                mPI.requestControlOptionalType(1, OptionalInfo.TYPE_POSITION, null, new OnOperateZXGListener() {
                    @Override
                    public void onOperate(boolean isSuccess, String msg) {}
                });
                optionalInfo.addType(OptionalInfo.TYPE_POSITION);
            }

            // optionalInfo.save(getDBHelper());
            if (mPI != null) {
                mPI.sendBroadcast(BroadCastName.BCDC_OPTIONAL_DATA_UPDATE);
            }

            ret = true;
        } catch (JSONException e) {
            // 修改jpush tag为游客
            ret = false;
            e.printStackTrace();
            throw new Exception("Json解析异常");
        } finally {
            closeSQLDBHelper();
        }

        return ret;
    }

    private DSQLiteDatabase getSQLiteDBHelper() {
        if (mSqliteHelper == null) {
            mSqliteHelper = new DSQLiteDatabase(mContext);
        }

        return mSqliteHelper;
    }

    private void closeSQLDBHelper() {
        if (mSqliteHelper != null) {
            mSqliteHelper.close();
        }
        mSqliteHelper = null;
    }

    private GlobalDBHelper getDBHelper() {
        if (mDbHelper == null) {
            mDbHelper = new GlobalDBHelper(mContext, DataModule.DB_GLOBAL);
        }
        return mDbHelper;
    }


    public static int byteArrayToInteger(byte[] b) {
        int s = 0;
        int len = b.length < 4 ? b.length : 4;
        for (int i = 0; i < len; i++) {
            s = s << 8;
            s += b[i] & 0xff;
        }
        return s;
    }
}
