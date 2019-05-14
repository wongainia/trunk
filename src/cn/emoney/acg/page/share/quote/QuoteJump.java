package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;

import android.os.Bundle;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.page.PageIntent;

public class QuoteJump {
    public static void gotoQuote(Page page, Goods g) {
        gotoQuote(page, g, -1);
    }

    public static void gotoQuote(Page page, Goods g, int period) {
        ArrayList<Goods> goodsLst = new ArrayList<Goods>();
        goodsLst.add(g);
        gotoQuote(page, goodsLst, 0, period);
    }


    public static void gotoQuote(Page page, ArrayList<Goods> goodsLst, int index) {
        gotoQuote(page, goodsLst, index, -1);
    }

    public static void gotoQuote(Page page, ArrayList<Goods> goodsLst, int index, int period) {
        PageIntent intent = new PageIntent(page, QuoteHome.class);
        // intent.setFlags(PageIntent.FLAG_PAGE_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QuoteHome.EXTRA_KEY_LIST_GOODS, goodsLst);
        bundle.putInt(QuoteHome.EXTRA_KEY_LIST_INDEX, index);
        if (period != -1) {
            bundle.putInt(QuoteHome.EXTEA_KEY_PERIOD, period);
        }

        intent.setArguments(bundle);

        intent.setSupportAnimation(false);

        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }
}
