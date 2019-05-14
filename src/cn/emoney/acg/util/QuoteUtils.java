package cn.emoney.acg.util;

import java.util.ArrayList;

import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;

public class QuoteUtils {

    public QuoteUtils() {
        // TODO Auto-generated constructor stub
    }


    public static String getMarketByGoodsId(String goodsId) {
        if (goodsId.length() < 7) {
            return "0";
        }
        return goodsId.substring(0, 1);
    }


    /*
     * 
     * 仅个股 不包含版块返回 bk20XXXX
     */
    public static String getStockCodeByGoodsId(String goodsId) {
        if (goodsId.length() == 6) {
            return goodsId;
        } else if (goodsId.length() < 6) {
            int left = 6 - goodsId.length();
            String stockCode = goodsId;
            for (int i = 0; i < left; i++) {
                stockCode = "0" + stockCode;
            }
            return stockCode;
        } else {
            int left = goodsId.length() - 6;
            return goodsId.substring(left);
        }
    }


    /*
     * 
     * 包含版块返回 bk20XXXX
     */
    public static String getGoodsCodeByGoodsid(int goodsId) {
        String goodsCode = goodsId + "";
        if (DataUtils.IsBK(goodsId)) {
            goodsCode = DataUtils.format_BK_GoodCode(goodsId);
        } else {
            goodsCode = QuoteUtils.getStockCodeByGoodsId(String.valueOf(goodsId));
        }

        return goodsCode;
    }
    
    public static Goods getGoodsByGoodsId(int goodsId, PageImpl page) {
        Goods goods = null;

        ArrayList<Goods> listGoods = page.getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
        if (listGoods != null && listGoods.size() > 0) {
            goods = listGoods.get(0);
        } else {
            goods = new Goods(goodsId, "");
        }

        return goods;
    }
    
}
