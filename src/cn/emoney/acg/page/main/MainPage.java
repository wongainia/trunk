package cn.emoney.acg.page.main;

import java.util.Timer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.helper.boot.BootManager;
import cn.emoney.acg.helper.push.RedPointNoticeManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.info.InfoHome;
import cn.emoney.acg.page.market.MarketHome;
import cn.emoney.acg.page.motif.MotifHome;
import cn.emoney.acg.page.my.MyHome;
import cn.emoney.acg.page.quiz.QuizHomePage;
import cn.emoney.acg.util.ApkUpdateUtil;
import cn.emoney.acg.util.ApkUpdateUtil.ApkUpdateUtil_CheckCallBack;
import cn.emoney.sky.libs.bar.Bar.OnBarMenuSelectedListener;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuImgItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.ToolBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.widget.PageSwitcher;

public class MainPage extends PageImpl {
    private final int QUIZ_PAGE_INDEX = 2;// 问股的索引值
    public static Handler mHandler_mainPage = null;
    private static final int PROMPT_UPDATE_DIAG = 10002;

    Timer mTimer = null;
    private ToolBar mMenuBar = null;
    private ImageView mIvQuiz = null;
    private ImageView imgAlert;

    private PageSwitcher mPageSwitcher = null;
    public static boolean isNeedRequestOptional = true;
    // private RedPointNoticeManager mRedPointNoticeManager = null;

    private BootManager mBootManager = null;

    long mLastBackKeyTime = 0;

    public MainPage() {}

    @Override
    protected void initData() {
        initHttpClient();
        mBootManager = new BootManager(this, getContext());
    }

    @Override
    protected void receiveData(Bundle bundle) {
        super.receiveData(bundle);
    }

    @Override
    protected void initPage() {
        quitFullScreen();
        setContentView(R.layout.page_main);

        mIvQuiz = (ImageView) findViewById(R.id.mainpage_iv_quiz);
        mMenuBar = (ToolBar) findViewById(R.id.mainpage_toolbar);

        if (mMenuBar != null) {
            mMenuBar.setOnBarMenuSelectedListener(new OnBarMenuSelectedListener() {
                @Override
                public void onItemSelected(int index, BarMenuItem arg1) {
                    if (index == 2) {
                        mIvQuiz.setSelected(true);
                    } else {
                        mIvQuiz.setSelected(false);
                    }
                    mPageSwitcher.setCurrentItem(index, false);
                }
            });
            mMenuBar.notifyBarSetChanged();
        }

        mPageSwitcher = (PageSwitcher) findViewById(R.id.mainpage_pageswitcher);
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(false);

            Page page = null;

            BarMenuImgItem mItemMarket = new BarMenuImgItem(0, R.drawable.selector_menuitem_market);
            page = new MarketHome();
            page.registBar(mMenuBar, mItemMarket);
            mPageSwitcher.addPage(page);

            BarMenuImgItem mItemInfo = new BarMenuImgItem(1, R.drawable.selector_menuitem_info);
            page = new InfoHome();
            page.registBar(mMenuBar, mItemInfo);
            mPageSwitcher.addPage(page);

            // 问股
            BarMenuImgItem mItemQuiz = new BarMenuImgItem(2, R.drawable.img_menubar_item_empty);
            page = new QuizHomePage();
            // if (getUserInfo().isRoleTeacher()) {
            // page = new TeacherHomePage();
            // } else {
            // page = new QuizHomePage();
            // }

            page.registBar(mMenuBar, mItemQuiz);
            mPageSwitcher.addPage(page);

            BarMenuImgItem mItemMotif = new BarMenuImgItem(3, R.drawable.selector_menuitem_motif);
            page = new MotifHome();
            page.registBar(mMenuBar, mItemMotif);
            mPageSwitcher.addPage(page);

            View view = View.inflate(getContext(), R.layout.page_main_toolbar_item_main, null);
            imgAlert = (ImageView) view.findViewById(R.id.page_main_toolbar_main_img_alert);
            BarMenuCustomItem items = new BarMenuCustomItem(4, view);
            page = new MyHome();
            page.registBar(mMenuBar, items);
            mPageSwitcher.addPage(page);

            registViewWithPage(mPageSwitcher);
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                checkNewVersion();
            }
        }, 6000);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (mBootManager == null) {
            mBootManager = new BootManager(this, getContext());
        }
        requestData();

        refreshRedPoint();
    }

    public void requestData() {
        UserInfo userInfo = getUserInfo();
        if (!userInfo.isLogined()) {
            if (mBootManager != null) {
                mBootManager.requestReLogin(userInfo);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mLastBackKeyTime) > 1500) {
                showTip("再按一次退出程序");

                mLastBackKeyTime = System.currentTimeMillis();
            } else {
                getModule().moveTaskToBack(true);
                DataModule.G_APP_IS_ACTIVE_FOREGROUND = false;
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * 进入时的动画
     * 
     * @return
     */
    public int enterAnimation() {
        return 0;
    }

    // 检查新版本
    private void checkNewVersion() {
        // 自动检查更新
        final ApkUpdateUtil auu = new ApkUpdateUtil(getContext());
        auu.checkNewVersion(DataModule.G_APK_CHANEL, DataModule.G_APKVERNUMBER, new ApkUpdateUtil_CheckCallBack() {
            @Override
            public void onCheckUpdate(int state, JSONObject obj) {
                if (state == 1) { // 有更新的版本
                    try {
                        String newVer = obj.getString(ApkUpdateUtil.KEY_VER);
                        String apkPath = ApkUpdateUtil.checkIsDownloaded(obj.getString(ApkUpdateUtil.KEY_URL));
                        if (apkPath != null && !apkPath.equals("")) {
                            long lastCancelPromptTime = getDBHelper().getLong(DataModule.G_KEY_LAST_UPDATE_PROMPT_CANCEL_TIME, 0);
                            // 用户主动取消后,5天不提示升级
                            if (System.currentTimeMillis() - lastCancelPromptTime >= 5 * 24 * 3600 * 1000) {
                                JSONObject t_jobj = new JSONObject();
                                t_jobj.put("ver", newVer);
                                t_jobj.put("path", apkPath);

                                Message msg = mHandler_mainPage.obtainMessage(PROMPT_UPDATE_DIAG, 0, 0, t_jobj);
                                mHandler_mainPage.sendMessage(msg);
                            }

                        } else {
                            if (DataModule.G_CURRENT_NETWORK_TYPE == ConnectivityManager.TYPE_WIFI) {
                                auu.downloadApk(null);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (state == 0) { // 没有更新的版本
                } else { // 检查出错 -1
                }
            }
        });
    }

    /**
     * 刷新推送消息提示（红点）是否显示
     * */
    private void refreshRedPoint() {
        // 刷新是否显示红点
        boolean isShowAlert = RedPointNoticeManager.getRedpointDisplay(getContext(), "");
        if (isShowAlert) {
            imgAlert.setImageResource(R.drawable.img_notice_point);
        } else {
            imgAlert.setImageBitmap(null);
        }
    }

    private void displayUpdateWindow(final String path, final String ver) {
        // FixDialog dialog = new FixDialog(getContext(), new
        // FixDialogListener() {
        // @Override
        // public void onConfirmBtnClicked(FixDialog dialog) {
        // if (ApkUpdateUtil.installApkFromFile(getContext(), path) == 0) {
        // LogUtil.easylog("安装开始");
        // }
        // dialog.cancel();
        // }
        //
        // @Override
        // public void onCancelBtnClicked(FixDialog dialog) {
        // getDBHelper().setLong(DataModule.G_KEY_LAST_UPDATE_PROMPT_CANCEL_TIME,
        // System.currentTimeMillis());
        // dialog.cancel();
        // }
        // });
        //
        // dialog.show();
        // String verString = "v" + ver;
        // String msg = "Wifi下检测到新版本" + verString + "\n是否更新?";
        // dialog.setMessage(msg);
        // dialog.setBackgroundResource(getTheme().getBgDialog());
        // dialog.setBtnBackgroundResource(getTheme().getBgDialogBtn());
        // dialog.setMessageTxtColor(getTheme().getTxtMain());
        // dialog.setBtnTxtColor(getTheme().getTxtMain());
    }
}
