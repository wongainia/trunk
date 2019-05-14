package cn.emoney.acg.page.equipment.klinehero;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;

public class KLineHeroRankingList extends PageImpl {

    // private static final String URL_KHERORANK_FORMAT =
    // "http://192.168.8.102/istockclient/cpyx/appranking?color=%s&uid=%s";
    // private static final String URL_KHERORANK_FORMAT =
    // "http://192.168.3.51/clienti/cpyx/appranking?color=%s&uid=21944&t=%s";
    private static final String URL_KHERORANK_FORMAT = "http://client.i.emoney.cn/cpyx/appranking?color=%s&uid=%s";

    private final String TAG_LIGHTTHEME = "white";

    private WebView mWvContent = null;
    private ImageView mivCloseBtn = null;

    private View loadingLayout = null;

    @SuppressLint("NewApi")
    @Override
    protected void initPage() {
        setContentView(R.layout.page_khero_ranking_list);

        mivCloseBtn = (ImageView) findViewById(R.id.kherorank_imgbtn_close);
        mWvContent = (WebView) findViewById(R.id.kherorank_wv_content);

        loadingLayout = findViewById(R.id.kherorank_pb_busy);

        String sUid = DataModule.getInstance().getUserInfo().getUid();
        String rankUrl = "";
        int webBgColor = 0;
        // if (DataModule.G_THEME == ThemeManager.THEME_DARK) {
        // webBgColor = getContext().getResources().getColor(R.color.sky_dark_bg_kherorank_web);
        // closePng = R.drawable.img_dark_khero_ranklist_colose;
        // rankUrl = String.format(URL_KHERORANK_FORMAT, TAG_DARKTHEME, sUid);
        // } else if (DataModule.G_THEME == ThemeManager.THEME_LIGHT) {
        webBgColor = getContext().getResources().getColor(R.color.b4);
        rankUrl = String.format(URL_KHERORANK_FORMAT, TAG_LIGHTTHEME, sUid);
        // }
        mWvContent.setBackgroundColor(webBgColor);
        // mWvContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // mWvContent.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWvContent.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWvContent.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        // mWvContent.getSettings().setSupportZoom(false);
        // mWvContent.getSettings().setBuiltInZoomControls(false);
        mWvContent.getSettings().setDefaultTextEncodingName("UTF -8");
        // mWvContent.setInitialScale(100);
        mWvContent.getSettings().setJavaScriptEnabled(true);

        mWvContent.addJavascriptInterface(new JSCallLocal(), "external");

        mWvContent.setWebViewClient(new webViewClient());
        LogUtil.easylog("sky", "KHeroRankList->ranUrl:" + rankUrl);
        mWvContent.loadUrl(rankUrl);
        final Animation roomin = AnimationUtils.loadAnimation(getContext(), R.anim.roomin);
        final Animation roomout = AnimationUtils.loadAnimation(getContext(), R.anim.roomout);
        roomin.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mivCloseBtn.setVisibility(View.VISIBLE);
            }
        });

        roomout.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                KLineHeroRankingList.this.finish();
            }
        });

        mWvContent.startAnimation(roomin);

        mivCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mivCloseBtn.setVisibility(View.GONE);
                int clear = getContext().getResources().getColor(R.color.bg_transparent);
                KLineHeroRankingList.this.getContentView().setBackgroundColor(clear);

                mWvContent.startAnimation(roomout);
                mWvContent.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mivCloseBtn.performClick();
        }
        return true;
    }

    // 重要:4.0之后的target,一定要在方法前标注@JavascriptInterface
    final class JSCallLocal {
        JSCallLocal() {}

        @JavascriptInterface
        public String OnCallLocalFunction(final String type, String sGoodCode) {
            String gName = "";
            if (type != null && type.equals("ISTOCK_FUNC_GETSECUABBR")) {
                gName = getGoodsNameByCode(sGoodCode);
                LogUtil.easylog("sky", "KHeroRank->JSCallLocal->sCode:" + sGoodCode + ", Name:" + gName);

            }
            return gName;

        }

    }

    private String getGoodsNameByCode(String gCode) {

        LogUtil.easylog("sky", "KHeroRank->getGoodsNameByCode->gCode:" + gCode);
        String gName = "";
        ArrayList<Goods> lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(gCode), 1);
        if (lstGoods != null && lstGoods.size() > 0) {
            gName = lstGoods.get(0).getGoodsName();
        }

        return gName;
    }

    class webViewClient extends WebViewClient {
        // 重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            // 如果不需要其他对点击链接事件的处理返回true，否则返回false
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (loadingLayout != null) {
                loadingLayout.setVisibility(View.GONE);
            }
            super.onPageFinished(view, url);
        }

    }
}
