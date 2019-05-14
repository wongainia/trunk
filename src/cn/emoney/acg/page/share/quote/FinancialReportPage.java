package cn.emoney.acg.page.share.quote;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.view.OnClickEffectiveListener;

public class FinancialReportPage extends PageImpl {
    
    public static final String EXTRA_KEY_GOODS_CODE = "key_goods_code";
    public static final String EXTRA_KEY_GOODS_NAME = "key_goods_name";
    
//    private String URL_FORMAT = "http://192.168.3.51/webapp/info/report?code=%s&name=%s";
    private String URL_FORMAT = "http://app.i.emoney.cn/info/report?code=%s&name=%s";
    
    private String url;
    
    private WebView webView;
    private View layoutLoading;
    
    @Override
    protected void initPage() {
        setContentView(R.layout.page_financial_report);
        
        webView = (WebView) findViewById(R.id.page_financial_webview);
        layoutLoading = findViewById(R.id.page_financial_layout_loading);
        findViewById(R.id.page_financial_btn_close).setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                getModule().finish();
            }
        });
        
        webView.setFocusable(false);
        webView.setFocusableInTouchMode(false);
        webView.setEnabled(false);
        webView.getSettings().setDefaultTextEncodingName("UTF -8");
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        // 使用缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptEnabled(true);
        
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
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);
        
        if (arguments != null) {
            String goodsCode = "600600";
            String goodsName = "青岛啤酒";
            if (arguments.containsKey(EXTRA_KEY_GOODS_CODE)) {
                goodsCode = arguments.getString(EXTRA_KEY_GOODS_CODE);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_NAME)) {
                goodsName = arguments.getString(EXTRA_KEY_GOODS_NAME);
            }
            if (TextUtils.isEmpty(goodsCode)) {
                goodsName = "600600";
            }
            if (TextUtils.isEmpty(goodsName)) {
                goodsName = "青岛啤酒";
            }
            url = String.format(URL_FORMAT, goodsCode, goodsName);
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
                if (layoutLoading != null) {
                    layoutLoading.setVisibility(View.INVISIBLE);                    
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
        
        webView.loadUrl(url);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getModule().finish();
        }
        
        return true;
    }

}
