package cn.emoney.acg.util.textviewlink;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;

public class LinkManager {
    /**
     * 给TextView添加股票跳转超链接
     * 
     * @param tv
     */
    public static void addStockLinkToTv(final PageImpl pageImpl, TextView tv) {
        LinkBuilder.on(tv).addLinks(getStocktLinks(pageImpl)).build();
    }

    private static List<Link> getStocktLinks(final PageImpl pageImpl) {
        List<Link> links = new ArrayList<Link>();

        Link stockLink = new Link(Pattern.compile("\\d{6}"));
        stockLink.setTextColor(pageImpl.RColor(R.color.c4));
        stockLink.setHighlightAlpha(.4f);
        stockLink.setUnderlined(false);
        stockLink.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                LogUtil.easylog("stockLink:" + clickedText);

                String t_gid = null;
                ArrayList<Goods> lst = null;
                if (!clickedText.startsWith("6")) {
                    t_gid = Util.FormatStockCode("1" + clickedText);
                } else {
                    t_gid = Util.FormatStockCode(clickedText);
                }

                lst = pageImpl.getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);

                Goods g = null;
                if (lst != null && lst.size() > 0) {
                    g = lst.get(0);
                }
                if (g != null) {
                    QuoteJump.gotoQuote(pageImpl, g);
                    // gotoQuote(g);
                }

            }
        });
        links.add(stockLink);

        return links;
    }
}
