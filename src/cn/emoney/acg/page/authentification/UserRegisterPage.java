package cn.emoney.acg.page.authentification;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.R;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.LoginPage;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class UserRegisterPage extends PageImpl {
    public static final int PAGECODE = 41100;
    private final String REG_URL_PRE = "http://app.i.emoney.cn/userinfo/reg";// "http://ds.emoney.cn/istock/Account/regmobile";
    private final String TAG_LIGHTTHEME = "white";

    private WebView mWvContent = null;

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void initPage() {
        setContentView(R.layout.page_webview);

        mWvContent = (WebView) findViewById(R.id.webView);
        mWvContent.setVisibility(View.INVISIBLE);

        String cmd = TAG_LIGHTTHEME + "?sid=1232&t=" + System.currentTimeMillis();
        String regUrl = REG_URL_PRE + cmd;
        if (LogUtil.isDebug()) {
            regUrl = "http://192.168.3.51/webapp/userinfo/reg";
        }

        Util.initWebSetting(mWvContent);
        mWvContent.addJavascriptInterface(new JSCallLocal(), "external");
        mWvContent.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LogUtil.easylog("sky", "mWebView -> onReceivedError");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogUtil.easylog("sky", "mWebView -> onPageStarted");
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogUtil.easylog("sky", "mWebView -> onPageFinished");
                // View t_view =
                // mLlContent_content.findViewWithTag("infodetail_webview");
                // if (t_view == null) {
                // mWebView.setTag("infodetail_webview");
                // mLlContent_content.addView(mWebView);
                // }
                mWvContent.setVisibility(View.VISIBLE);
                closeLoading();

                super.onPageFinished(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                LogUtil.easylog("sky", "mWebView -> shouldInterceptRequest");
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // if (url.startsWith("http://") && getRespStatus(url) == 404) {
                // view.stopLoading();
                // // 载入本地assets文件夹下面的错误提示页面404.html
                // view.loadUrl("file:///android_asset/404.html");
                // } else {
                // view.loadUrl(url);
                // }
                return true;
            }
        });

        showLoading();
        mWvContent.loadUrl(regUrl);
        // mWvContent.loadUrl("http://i.emoney.cn/OAuth/QQ");
        // mWvContent.loadUrl("http://www.webhek.com/demo/html5-placeholder/");
        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    // 重要:4.0之后的target,一定要在方法前标注@JavascriptInterface
    final class JSCallLocal {
        JSCallLocal() {}

        @JavascriptInterface
        public void OnCallLocalFunction(final int retCode, final String userName, final String userPwd) {
            String strFormat = "reg OnCallLocalFunction(retCode = %d, userName = %s, userPwd = %s)";
            String call_log = "";
            if (retCode == 1 || retCode == -1) {
                call_log = String.format(strFormat, retCode, "null", "null");
            } else {
                call_log = String.format(strFormat, retCode, userName, userPwd);
            }

            if (retCode == 1) {
                // click注册btn
                InputMethodUtil.closeSoftKeyBoard(UserRegisterPage.this);
                showLoadingDialog();
            } else if (retCode == -1) {
                // 注册失败
                closeLoadingDialog();
            } else if (retCode == 0) {
                // 注册成功
                closeLoadingDialog();
                InputMethodUtil.closeSoftKeyBoard(UserRegisterPage.this);

                Bundle bundle = new Bundle();
                bundle.putString(LoginPage.KEY_USER_NAME, userName);
                bundle.putString(LoginPage.KEY_USER_PWD, userPwd);
                setResult(UserRegisterPage.PAGECODE, bundle);
                finish();
            } else {
                closeLoadingDialog();
            }
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "免费注册");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            InputMethodUtil.closeSoftKeyBoard(UserRegisterPage.this);
            finish();
        }
    }

    private void showLoading() {
        findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
    }

    private void closeLoading() {
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
    }

    private void showLoadingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.closeProgressDialog();
            }
        });
    }

    private void closeLoadingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.closeProgressDialog();
            }
        });
    }
}
