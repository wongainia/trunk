package cn.emoney.acg.page.settings;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.BuildConfig;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.dialog.FixToast;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.ApkUpdateUtil;
import cn.emoney.acg.util.ApkUpdateUtil.ApkUpdateUtil_CheckCallBack;
import cn.emoney.acg.util.ApkUpdateUtil.ApkUpdateUtil_UpdateCallBack;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class SettingsHome extends PageImpl {
    private static long mLastCheckUpdateTime = 0;
    private View mLlChangeServer;
    private List<ViewHolder> mLstItemHolders = new ArrayList<ViewHolder>();

    public SettingsHome() {}

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();
        updateSettingItemData();
    }

    private void updateSettingItemData() {
        if (DataModule.G_USER_DEBUG == true || BuildConfig.DEBUG) {
            mLlChangeServer.setVisibility(View.VISIBLE);
        } else {
            mLlChangeServer.setVisibility(View.GONE);
        }
    }

    private void findSubItem(ViewHolder vh) {
        if (vh != null && vh.mLayout != null) {
            vh.mMainTv = (TextView) vh.mLayout.findViewById(R.id.item_tv_main);
            vh.mSubTv = (TextView) vh.mLayout.findViewById(R.id.item_tv_sub);
            vh.mIcon = (ImageView) vh.mLayout.findViewById(R.id.item_iv_icon);
        }
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_settingshome);

        mLstItemHolders.clear();

        ViewHolder vh;
        // 行情刷新频率
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_priceRefresh);
        findSubItem(vh);
        vh.mIcon.setImageResource(R.drawable.img_setting_refresh);
        vh.mMainTv.setText("行情数据刷新");
        mLstItemHolders.add(vh);

        // 消息推送开关
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_msgPush);
        findSubItem(vh);
        vh.mIcon.setImageResource(R.drawable.img_setting_pushswitch);
        vh.mMainTv.setText("推送设置");

        mLstItemHolders.add(vh);

        // 检查更新
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_checkVersionUpdate);
        findSubItem(vh);

        vh.mIcon.setImageResource(R.drawable.img_setting_update);
        vh.mMainTv.setText("检查更新");
        String appVer = DataModule.G_APKVERNUMBER.equals("") ? "" : "( v" + DataModule.G_APKVERNUMBER + " )";
        vh.mSubTv.setText(appVer);
        mLstItemHolders.add(vh);

        // 用户指南
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_userGuide);
        findSubItem(vh);
        vh.mIcon.setImageResource(R.drawable.img_setting_guide);
        vh.mMainTv.setText("用户指南");
        mLstItemHolders.add(vh);

        // exit
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_exit);
        findSubItem(vh);
        vh.mIcon.setImageResource(R.drawable.img_setting_exit);
        vh.mMainTv.setText("退出系统");
        mLstItemHolders.add(vh);

        // debug
        vh = new ViewHolder();
        vh.mLayout = findViewById(R.id.settinghome_ll_server_change);
        findSubItem(vh);
        mLlChangeServer = vh.mLayout;
        vh.mIcon.setImageResource(R.drawable.img_setting_debug);
        vh.mMainTv.setText("系统调试");
        mLstItemHolders.add(vh);
        setOnSettingItemClickListener();

        bindPageTitleBar(R.id.titlebar);
    }

    // private String calculateCacheSize() {
    // File cacheDir = getCacheDir();
    // String sCacheSize = FileUtils.getFileSizeString(cacheDir);
    //
    // String sSzie = sCacheSize.equals("") ? "(0 KB)" : "( " + sCacheSize + " )";
    // return sSzie;
    // }

    private void setOnSettingItemClickListener() {
        for (int i = 0; i < mLstItemHolders.size(); i++) {
            ViewHolder vh = mLstItemHolders.get(i);
            vh.mLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doItemClick(v);
                }
            });
        }
    }

    @Override
    protected View getPageBarMenuProgress() {
        return null;
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "设置");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);
        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    public void doItemClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.settinghome_ll_priceRefresh:
                // 自动刷新设置
                startPage(DataModule.G_CURRENT_FRAME, new PageIntent(SettingsHome.this, AutoRefreshSettingPage.class));
                break;
            case R.id.settinghome_ll_msgPush:
                // 信息推送开关
                startPage(DataModule.G_CURRENT_FRAME, new PageIntent(SettingsHome.this, PushSettingPage.class));
                break;
            // // 清除缓存
            // case R.id.settinghome_ll_cleanCache: {
            // getCacheManager().clearCaches();
            // getCacheManager().submitClear();
            //
            // String sCacheSzie = calculateCacheSize();
            // mTv_chacheSize.setText("(0 KB)");
            // }
            // break;
            case R.id.settinghome_ll_checkVersionUpdate:
                // 检查更新
                long t_curTime = System.currentTimeMillis();
                if ((t_curTime - mLastCheckUpdateTime) > 12000) {
                    mLastCheckUpdateTime = t_curTime;
                    showTip("开始查询新版本");
                    checkApkVersionUpdate();
                } else {
                    // showTip("请稍后");
                }
                break;
            case R.id.settinghome_ll_userGuide:
                // 用户指南
                startPage(DataModule.G_CURRENT_FRAME, new PageIntent(SettingsHome.this, GuideSettingPage.class));
                break;
            case R.id.settinghome_ll_server_change:
                // 切换服务器 debug加
                startPage(DataModule.G_CURRENT_FRAME, new PageIntent(SettingsHome.this, ServerChangeSettingPage.class));
                break;
            case R.id.settinghome_ll_exit:
                // 退出应用
                DialogUtils.showMessageDialog(getActivity(), "提示", "是否要退出应用?", "确定", "取消", new CustomDialogListener() {
                    @Override
                    public void onConfirmBtnClicked() {
                        // TODO Auto-generated method stub
                        DataModule.G_APP_IS_ACTIVE_FOREGROUND = false;
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                System.exit(0);
                            }
                        }, 1000);
                        getModule().finish();
                    }

                    @Override
                    public void onCancelBtnClicked() {
                        // TODO Auto-generated method stub
                    }
                });
                break;
            default:
                break;
        }

    }

    private void downloadApk(final ApkUpdateUtil apkUpdateUtil) {
        apkUpdateUtil.downloadApk(new ApkUpdateUtil_UpdateCallBack() {

            @Override
            public void onFail() {
                // showTip("下载更新失败!");
            }

            @Override
            public void onDownloadSuccess(String path) {
                showTip("下载更新成功,开始安装");
                if (ApkUpdateUtil.installApkFromFile(getContext(), path) == 0) {
                    LogUtil.easylog("安装开始");
                }
            }

            @Override
            public void onDownloadProcess(int process) {}
        });
    }

    private void checkApkVersionUpdate() {
        final ApkUpdateUtil auu = new ApkUpdateUtil(getContext());
        auu.checkNewVersion(DataModule.G_APK_CHANEL, DataModule.G_APKVERNUMBER, new ApkUpdateUtil_CheckCallBack() {

            @Override
            public void onCheckUpdate(int state, JSONObject obj) {
                if (state == 1) { // 有更新的版本
                    try {
                        String newVer = obj.getString(ApkUpdateUtil.KEY_VER);
                        String apkPath = ApkUpdateUtil.checkIsDownloaded(obj.getString(ApkUpdateUtil.KEY_URL));
                        if (apkPath != null && !apkPath.equals("")) {
                            showTip("检测到最新版本:" + newVer + " 开始安装...");
                            if (ApkUpdateUtil.installApkFromFile(getContext(), apkPath) == 0) {
                                LogUtil.easylog("安装开始");
                            }
                        } else {
                            downloadApk(auu);
                            showTip("检测到最新版本:" + newVer + " 开始下载...", FixToast.TIME_LONG);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (state == 0) { // 没有更新的版本
                    showTip("当前已经是最新版本");
                    mLastCheckUpdateTime = 0;
                } else { // 检查出错 -1
                    // showTip("检查更新失败!");
                }
            }
        });
    }

    @Override
    protected void onPageResult(int requestCode, int resultCode, Bundle data) {
        super.onPageResult(requestCode, resultCode, data);
    }

    class ViewHolder {
        View mLayout = null;
        TextView mMainTv = null;
        TextView mSubTv = null;
        ImageView mIcon = null;
    }
}
