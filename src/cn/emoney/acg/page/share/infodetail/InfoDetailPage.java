package cn.emoney.acg.page.share.infodetail;

import java.util.ArrayList;
import java.util.Map;

import android.os.Handler;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.R;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.network.data.JsonData;

public class InfoDetailPage extends PageImpl {

    public final static String EXTRA_KEY_CONTENT_URL = "key_content_url";
    public final static String EXTRA_KEY_TITLE = "key_title";
    public final static String EXTRA_KEY_AUTHOR = "key_author";
    public final static String EXTRA_KEY_TIME = "key_time";
    public final static String EXTRA_KEY_SORTCLS = "key_sortcls";
    public final static String EXTRA_KEY_FROM = "key_from";
    public final static String EXTRA_KEY_RELATED_STOCKS = "key_related_stocks";

    private final String HTML_TEMPLATE_ERROR = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + "<head>" 
            + "<meta charste=\"utf-8\">" + "<style type=\"text/css\">" + "body{background-color:%s}" 
            + "#content{color:%s;font-size:18px;margin-top:0px;line-height:32px;letter-spacing:0px;text-align:center;}" 
            + "</style>" + "</head>" + "<body><div id=\"content\">%s</div></body></html>";
    private final String HTML_TEMPLATE_NEW_VERSION = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" 
            + "<head>" 
            + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0,maximum-scale=1.0, user-scalable=no\" />"
            + "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />"
            + "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" />"
            + "<meta name=\"format-detection\" content=\"telephone=no; email=no\" />"
            + "<style type=\"text/css\">" 
            + "*{margin:0; padding:0; border:0;}"
            + "body{background-color:#ffffff; -webkit-overflow-scrolling:touch; -webkit-user-select:none; -moz-user-select:none; -ms-user-select:none; -o-user-select:none; user-select:none;}" 
            + "#header{background-color:#f8f8f8; padding: 15px 20px 10px 20px; border-bottom: 1px solid #E4E4E4; font-family: \"微软雅黑, Microsoft YaHei\";}"
            + "#title{font-size:20px; color:#353535;}"
            + "#time{font-size:13px; color:#8a8a8a; display:block; margin:10px 0px 0px 0px;}"
            + "#content{padding: 15px 20px; line-height:30px; font-family: \"微软雅黑, Microsoft YaHei\"; white-space: pre; color:#353535; font-size: 15.9996px; white-space: normal;}"
            + "</style>"
            + "</head>"
            + "<body>" 
            + "<div id=\"header\">"
            + "<div id=\"title\">%s</div>" 
            + "<div id=\"time\">%s&nbsp;&nbsp;%s</div>"
            + "</div>" 
            + "<section id=\"content\">%s</section>"
            + "</body>"
            + "</html>";

    private Map<String, String> mDetailInfoMap;

    private WebView mWebView;
    private View layoutLoading;

    private boolean bFlag = true;

    @Override
    protected void initData() {
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_infodetail_without_titlebar);

        mWebView = (WebView) findViewById(R.id.page_infodetail_webview);
        layoutLoading = findViewById(R.id.page_infodetail_layout_loading);

        mWebView.setFocusable(false);
        mWebView.setFocusableInTouchMode(false);
        mWebView.setEnabled(false);
        mWebView.getSettings().setDefaultTextEncodingName("UTF -8");
        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setVerticalScrollbarOverlay(false);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JSCallLocal(), "external");

        // 使用缓存
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (mWebView != null) {
                            mWebView.setVisibility(View.VISIBLE);                            
                        }
                    }
                }, 500);
            }
        });
    }

    protected void onPageResume() {
        super.onPageResume();

        // 5秒后，不管有没有加载完成，隐藏正在加载中
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutLoading.setVisibility(View.INVISIBLE);
            }
        }, 5000);

        // 获取重大提示内容
        if (mDetailInfoMap != null) {
            // 获取新闻详情数据
            requestJson(mDetailInfoMap.get(EXTRA_KEY_CONTENT_URL), InfoDetailJson.class.getName(), false);          
        }
    }

    public void setData(Map<String, String> map) {
        mDetailInfoMap = map;
    }

    @Override
    protected void updateWhenNetworkError() {
        LogUtil.easylog("sky", "updateWhenNetworkError");

        String t_content = "数据获取失败";

        String xhtml = String.format(HTML_TEMPLATE_ERROR, "#ffffff", "#333333", t_content);

        mWebView.loadDataWithBaseURL(null, xhtml, "text/html", "utf-8", null);
        super.updateWhenNetworkError();
    }

    @Override
    protected void updateWhenDecodeError() {
        LogUtil.easylog("sky", "updateWhenDecodeError");
        String t_content = "数据获取失败";

        final String xhtml = String.format(HTML_TEMPLATE_ERROR, "#ffffff", "#333333", t_content);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadDataWithBaseURL(null, xhtml, "text/html", "utf-8", null);
            }
        });
        thread.start();

        super.updateWhenDecodeError();
    }

    public void updateFromJson(JsonData data) {
        if (!bFlag) {
            bFlag = !bFlag;
            return;
        }
        bFlag = !bFlag;

        if (data instanceof InfoDetailJson) {
            InfoDetailJson infoDetail = (InfoDetailJson) data;
            String t_author = infoDetail.mAuthor;
            String t_from = mDetailInfoMap.get(EXTRA_KEY_FROM);
            String strFrom = "";
            if ((t_author != null && !t_author.equals("")) && (!t_from.equals(""))) {
                strFrom = t_from + " " + t_author;
            } else if (t_author != null && !t_author.equals("")) {
                strFrom = t_author;
            } else if (!t_from.equals("")) {
                strFrom = t_from;
            }

            if (strFrom == null || strFrom.equals("")) {
                strFrom = mDetailInfoMap.get(EXTRA_KEY_SORTCLS);;
            }

            String title = mDetailInfoMap.get(EXTRA_KEY_TITLE);
            String time = DateUtils.formatInfoDate(mDetailInfoMap.get(EXTRA_KEY_TIME), DateUtils.mFormatDayHM);
            String t_content = infoDetail.mContent;

            String xhtml = String.format(HTML_TEMPLATE_NEW_VERSION, title, strFrom, time, t_content);

            // 后台返回的html内容t_content中包含段落标志，删除第一个段落标签,否则上方空得太多
            xhtml = xhtml.replaceFirst("<p>", "");
            xhtml = xhtml.replaceFirst("</p>", "");

            mWebView.loadDataWithBaseURL(null, xhtml, "text/html", "utf-8", null);
        }
    }

    // 重要:4.0之后的target,一定要在方法前标注@JavascriptInterface
    final class JSCallLocal {
        JSCallLocal() {
        }

        @JavascriptInterface
        public void OnCallLocalFunction(final String what, final String arg) {
            LogUtil.easylog("sky", "OnCallLocalFunction(final String what, final String arg)");

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
                        QuoteJump.gotoQuote(InfoDetailPage.this, g);
//                        gotoQuote(g);
                    }
                });
            }

        }
    }

}
