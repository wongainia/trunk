package cn.emoney.acg.page.authentification;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
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

@SuppressLint("NewApi")
public class FindPwdPage extends PageImpl {
    public static final int PAGECODE = 41200;
    private final String RESET_URL_PRE = "http://app.i.emoney.cn/userinfo/resetpwd";// "http://ds.emoney.cn/istock/Account/resetmobile";
    private final String TAG_LIGHTTHEME = "white";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initPage() {
        setContentView(R.layout.page_webview);
        final WebView mWvContent = (WebView) findViewById(R.id.webView);
        mWvContent.setVisibility(View.INVISIBLE);

        String cmd = TAG_LIGHTTHEME + "?sid=1232&t=" + System.currentTimeMillis();
        String regUrl = RESET_URL_PRE + cmd;
        if (LogUtil.isDebug()) {
            regUrl = "http://192.168.3.51/webapp/userinfo/resetpwd";
        }

        Util.initWebSetting(mWvContent);
        mWvContent.addJavascriptInterface(new JSCallLocal(), "external");
        mWvContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Field defaultScale = WebView.class.getDeclaredField("mDefaultScale");
                        defaultScale.setAccessible(true);
                        // WebViewSettingUtil.getInitScaleValue(VideoNavigationActivity.this,
                        // false )/100.0f 是我的程序的一个方法，可以用float 的scale替代
                        defaultScale.setFloat(mWvContent, 1);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


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
            String strFormat = "find OnCallLocalFunction(retCode = %d, userName = %s, userPwd = %s)";
            String call_log = "";
            if (retCode == 1 || retCode == -1) {
                call_log = String.format(strFormat, retCode, "null", "null");
            } else {
                call_log = String.format(strFormat, retCode, userName, userPwd);
            }

            LogUtil.easylog("sky", call_log);
            if (retCode == 1) {
                // click重置btn
                InputMethodUtil.closeSoftKeyBoard(FindPwdPage.this);
                showLoadingDialog();
            } else if (retCode == -1) {
                // 重置
                closeLoadingDialog();
            } else if (retCode == 0) {
                // 重置密码成功
                closeLoadingDialog();
                InputMethodUtil.closeSoftKeyBoard(FindPwdPage.this);

                Bundle bundle = new Bundle();
                bundle.putString(LoginPage.KEY_USER_NAME, userName);
                bundle.putString(LoginPage.KEY_USER_PWD, userPwd);
                setResult(FindPwdPage.PAGECODE, bundle);
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

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "重置密码");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            InputMethodUtil.closeSoftKeyBoard(FindPwdPage.this);
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
                DialogUtils.showProgressDialog(getActivity(), null);
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
