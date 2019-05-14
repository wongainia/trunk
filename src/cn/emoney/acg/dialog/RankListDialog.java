package cn.emoney.acg.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.emoney.acg.ACGApplication;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.helper.db.DSQLiteDatabase;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;

/**
 * @ClassName: RankListDialog
 * @Description:排行榜对话框
 * @author xiechengfa
 * @date 2015年11月13日 下午1:51:06
 */
public class RankListDialog {
    private final String TAG_LIGHTTHEME = "white";
    private static final String URL_KHERORANK_FORMAT = "http://client.i.emoney.cn/cpyx/appranking?color=%s&uid=%s";
    private Context context;
    private Dialog dialog;
    private WebView mWvContent = null;
    private View loadingLayout = null;

    public RankListDialog(Context context) {
        this.context = context;
        builder();
    }

    private void builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.page_khero_ranking_list, null);
        init(view);

        // 获取自定义Dialog布局中的控件
        // FrameLayout lLayout_bg = (FrameLayout) view.findViewById(R.id.lLayout_bg);
        // lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (DataModule.SCREEN_HEIGHT *
        // DataModule.DIALOG_WIDTH_SCALE), (int) (DataModule.SCREEN_WIDTH *
        // DataModule.DIALOG_WIDTH_SCALE)));

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogIOSStyle);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
    }

    public RankListDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);

        // 根据当前的需求的特殊处理，当需求要求不能返回键取消同样也不能点击取消
        if (!cancel) {
            setCanceledOnTouchOutside(false);
        }
        return this;
    }

    public RankListDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }


    private void setLayout() {}

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void show() {
        setLayout();
        dialog.show();
    }

    private void init(View view) {
        mWvContent = (WebView) view.findViewById(R.id.kherorank_wv_content);

        loadingLayout = view.findViewById(R.id.kherorank_pb_busy);

        String sUid = DataModule.getInstance().getUserInfo().getUid();
        String rankUrl = "";
        int webBgColor = 0;
        webBgColor = Util.getResourcesColor(R.color.b4);
        rankUrl = String.format(URL_KHERORANK_FORMAT, TAG_LIGHTTHEME, sUid);
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

        if (LogUtil.isDebug()) {
            rankUrl = "http://192.168.3.51/clienti/cpyx/appranking?uid=21914&color=white";
        }

        mWvContent.loadUrl(rankUrl);

        view.findViewById(R.id.kherorank_imgbtn_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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

    private DSQLiteDatabase mSqliteHelper = null;

    public DSQLiteDatabase getSQLiteDBHelper() {
        if (mSqliteHelper == null) {
            mSqliteHelper = new DSQLiteDatabase(ACGApplication.getInstance());
        }

        return mSqliteHelper;
    }
}
