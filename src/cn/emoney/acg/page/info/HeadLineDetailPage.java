package cn.emoney.acg.page.info;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.R;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.Util;

/**
 * 今日头条详情界面
 * */
public class HeadLineDetailPage extends PageImpl {
    
//    private static final String URL_PREFIX = "http://192.168.3.51/webapp/info/headline?curl=";
    private static final String URL_PREFIX = "http://app.i.emoney.cn/info/headline?curl=";
    
    private String url;
    
    private WebView webView;
    private View layoutLoading;
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initPage() {
        setContentView(R.layout.page_infodetail_without_titlebar);

        webView = (WebView) findViewById(R.id.page_infodetail_webview);
        layoutLoading = findViewById(R.id.page_infodetail_layout_loading);

        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.setEnabled(false);
        webView.getSettings().setDefaultTextEncodingName("UTF -8");
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSCallLocal(), "external");
        // 使用缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置自动适应屏幕大小
        webView.getSettings().setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= 7) {
            webView.getSettings().setLoadWithOverviewMode(true);
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (webView != null) {
                            webView.setVisibility(View.VISIBLE);
                        }
                    }
                }, 500);
            }

        });
        
    }

    @Override
    protected void initData() {}
    
    @Override
    protected void onPageResume() {
        super.onPageResume();
        
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(URL_PREFIX + url);
        }
        
        // 5秒后，不管有没有加载完成，隐藏正在加载中
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutLoading.setVisibility(View.INVISIBLE);
            }
        }, 5000);
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    // 重要:4.0之后的target,一定要在方法前标注@JavascriptInterface
    final class JSCallLocal {
        JSCallLocal() {}

        @JavascriptInterface
        public void OnCallLocalFunction(final String what, final String arg) {
            ArrayList<Goods> lst = null;
            String t_gid = null;
            if (!arg.startsWith("6")) {
                t_gid = Util.FormatStockCode("1" + arg);
            } else {
                t_gid = Util.FormatStockCode(arg);
            }

            lst = getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);
            closeSQLDBHelper();
            if (lst != null && lst.size() > 0) {
                final Goods g = lst.get(0);
                mImplHandler.post(new Runnable() {
                    public void run() {
                        QuoteJump.gotoQuote(HeadLineDetailPage.this, g);
//                        gotoQuote(g);
                    }
                });
            }
        }

        @JavascriptInterface
        public void onStockClick(final String arg) {
            ArrayList<Goods> lst = null;
            String t_gid = null;
            if (!arg.startsWith("6")) {
                t_gid = Util.FormatStockCode("1" + arg);
            } else {
                t_gid = Util.FormatStockCode(arg);
            }

            lst = getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);
            closeSQLDBHelper();
            if (lst != null && lst.size() > 0) {
                final Goods g = lst.get(0);
                mImplHandler.post(new Runnable() {
                    public void run() {
                        QuoteJump.gotoQuote(HeadLineDetailPage.this, g);
//                        gotoQuote(g);
                    }
                });
            }
        }
    }
}
