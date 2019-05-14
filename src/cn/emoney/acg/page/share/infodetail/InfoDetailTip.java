package cn.emoney.acg.page.share.infodetail;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.R;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuImgItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

/**
 * 重大提示详情
 * */
public class InfoDetailTip extends PageImpl {

    /**
     * 重大提示字段
     * */
    public static final String EXTRA_KEY_TIP_DATE = "key_tip_date";
    public static final String EXTRA_KEY_TIP_ID = "key_tip_id";
    public static final String EXTRA_KEY_TIP_TITLE = "key_tip_title";
    public static final String EXTRA_KEY_TIP_TYPE = "key_tip_type";

    private final String HTML_TEMPLATE_TIP = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" 
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
            + "<div id=\"time\">%s</div>"
            + "</div>" 
            + "<section id=\"content\">%s</section>"
            + "</body>"
            + "</html>";
    
    /**
     * 重大提示
     * */
    private int tipId;
    private int tipType;

    private WebView webView;
    private View layoutLoading;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_infodetail);

        webView = (WebView) findViewById(R.id.page_infodetail_webview);
        layoutLoading = findViewById(R.id.page_infodetail_layout_loading);

        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.setEnabled(false);
        webView.getSettings().setDefaultTextEncodingName("UTF -8");
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        // 使用缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

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

        bindPageTitleBar(R.id.page_infodeatil_titlebar);
    }

    @Override
    protected void receiveData(Bundle bundle) {
        super.receiveData(bundle);

        if (bundle != null) {
            // 接收重大提示信息
            if (bundle.containsKey(EXTRA_KEY_TIP_ID)) {
                tipId = bundle.getInt(EXTRA_KEY_TIP_ID);
            }
            if (bundle.containsKey(EXTRA_KEY_TIP_TYPE)) {
                tipType = bundle.getInt(EXTRA_KEY_TIP_TYPE);
            }
        }
    }

    @Override
    protected void initData() {}

    @Override
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
        requestMajorTip();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        BarMenuImgItem leftItem = new BarMenuImgItem(0, R.drawable.selector_btn_close_infodetail);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem itemTitle = new BarMenuTextItem(1, "重大提示");
        itemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(itemTitle);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();

        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;

            finish();
        }
    }

    @Override
    public int popExitAnimation() {
        return 0;
    }

    /**
     * 获取重大提示内容
     * */
    private void requestMajorTip() {
        if (tipId == 0 || tipType == 0) {
            return;
        }

        JSONObject jsObj = null;

        try {
            jsObj = new JSONObject();

            jsObj.put("id", tipId);
            jsObj.put("type", tipType);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        requestInfo(jsObj, IDUtils.ID_MAJOR_TIP_DETAIL);
    }
    
    /**
     * 获取到重大提示详情
     * */
    public void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();
        
        if (id == IDUtils.ID_MAJOR_TIP_DETAIL) {
            // 获取到重大提示详情
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }
            
            String msgData = mc.getMsgData();
            
            try {
                JSONObject obj = JSONObject.parseObject(msgData);
                
                if (obj == null)
                    return;
                
                int resultCode = obj.getIntValue(KEY_RESULT);
                if (resultCode == 0) {
                    JSONObject objData = obj.getJSONObject("data");
                    
                    String date = objData.getString("date");
                    String title = objData.getString("title");
                    String content = objData.getString("abstract");
                    
                    if (!TextUtils.isEmpty(date) && date.length() == 8) {
                        date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                    }
                    
                    // 如果返回没有段落的话，添加段落
                    if (!TextUtils.isEmpty(content) && !content.contains("<p>") && !content.contains("<P>") && !content.contains("</p>") && !content.contains("</P>")) {
                        content = "<p>" + content + "</p>";
                    }
                    
                    String xhtml = String.format(HTML_TEMPLATE_TIP, title, date, content);
                    
                    // 去掉换行符。已有的段落标志已经起到段落间隔作用，再加个换行符的话，间隔会过高，所以去掉。
                    try {
                        xhtml = xhtml.replaceAll("<BR>", "");
                        xhtml = xhtml.replaceAll("<br>", "");
                    } catch (Exception e) {
                    }
                    
                    webView.loadDataWithBaseURL(null, xhtml, "text/html", "utf-8", null);
                }
            } catch (Exception e) {

            }
        }
    }
    
}
