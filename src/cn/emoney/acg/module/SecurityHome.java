package cn.emoney.acg.module;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import cn.emoney.acg.BuildConfig;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.helper.lb.LoadBalance;
import cn.emoney.acg.helper.lb.LoadBalance.CallBack;
import cn.emoney.acg.helper.push.BaiduPushManager_v2;
import cn.emoney.acg.helper.thirdpartylogin.QQAuth;
import cn.emoney.acg.helper.thirdpartylogin.ThirdPartyLogin;
import cn.emoney.acg.media.AudioRecordCacheManager;
import cn.emoney.acg.page.main.MainPage;
import cn.emoney.acg.service.goodstable.StockService;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.VolleyHelper;
import cn.emoney.sky.libs.db.GlobalDBHelper;
import cn.emoney.sky.libs.log.Tracer;
import cn.emoney.sky.libs.module.NettingModule;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.page.SplashPage;

import com.alibaba.fastjson.JSONObject;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

public class SecurityHome extends NettingModule {

    public static final int EXIT_APP_CMD = 1900010;

    Intent mStockService = null;

    private GlobalDBHelper mDBHelper = null;

    private long mTimeBegin = 0;

    // 0:初始状态; -1:LB获取BS失败; 1:LB获取BS成功; 2:码表下载成功;
    private int mFlag_canEnterMain = 0;

    private int mResumeCount = 0;

    private LoadBalance mLoadBalance = null;

    @Override
    protected void onNetworkStatusChanged(boolean isAvailable, int type) {
        DataModule.G_CURRENT_NETWORK_TYPE = type;
        LogUtil.easylog("sky", "onNetworkStatusChanged:" + type);
    }

    @Override
    public void initData() {}

    @Override
    public void initModule() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.module_securityhome);

        Tracer.disable();
        // Tracer.enable();
        // UMeng 设置
        // AnalyticsConfig.setChannel(DataModule.G_APK_CHANEL);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setSessionContinueMillis(10 * 60 * 1000);
        MobclickAgent.updateOnlineConfig(getApplicationContext());
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        VolleyHelper.getInstance(getApplicationContext());

        // JPUSH 设置
        // JPushManager.init(this);
        // JPushManager.setDebugModule(BuildConfig.DEBUG);
        BaiduPushManager_v2.onStartWork(getApplicationContext());
        if (LogUtil.isDebug()) {
            BaiduPushManager_v2.setDebugModule(true);
        } else {
            BaiduPushManager_v2.setDebugModule(false);
        }

        // java设置
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        if (BuildConfig.DEBUG) {
            System.out.println("debug test is debug");
        } else {
            System.out.println("debug test is release");
        }

        // 执行测试代码
        testFunc();

        // LB
        mTimeBegin = System.currentTimeMillis();
        mLoadBalance = new LoadBalance(new CallBack() {
            @Override
            public void onError(int errorCode) {
                LogUtil.easylog("sky", "LB Error:" + errorCode);
                setHost(getDBHelper().getString(DataModule.G_KEY_LAST_SERVER, RequestUrl.host0));

                mFlag_canEnterMain = -1;
            }

            @Override
            public void onComplete(String bsIp, int bsPort) {
                setHost(String.format(RequestUrl.HOST_FORMAT, bsIp, bsPort));
                LogUtil.easylog("sky", "LB OK:->host:" + RequestUrl.host);
                getDBHelper().setString(DataModule.G_KEY_LAST_SERVER, RequestUrl.host);

                mFlag_canEnterMain = 1;
            }
        });

        // 创建应用文件夹
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String t_path = Environment.getExternalStorageDirectory() + "/";
            String sAppDir = t_path + DataModule.G_LOC_PATH;
            File fAppDir = new File(sAppDir);
            if (fAppDir != null && !fAppDir.exists()) {
                fAppDir.mkdirs();
            }
        }

        // 加载本地数据
        DataModule.G_LAST_LOGIN_STATE = getDBHelper().getInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE, DataModule.G_LAST_LOGIN_STATE);
        DataModule.G_AUTO_REFRESH = getDBHelper().getBoolean(DataModule.G_KEY_AUTO_REFRESH, DataModule.G_AUTO_REFRESH);// 是否自动刷新
        DataModule.G_ENABLE_ANIMATION = getDBHelper().getBoolean(DataModule.G_KEY_ENABLE_ANIMATION, DataModule.G_ENABLE_ANIMATION);// 是否支持动画
        DataModule.G_MOBLIEREFRESHTIMEINTERVAL = getDBHelper().getInt(DataModule.G_KEY_MOBLIEREFRESHTIMEINTERVAL, DataModule.G_MOBLIEREFRESHTIMEINTERVAL); // 手机网络刷新间隔
        DataModule.G_WIFIREFRESHTIMEINTERVAL = getDBHelper().getInt(DataModule.G_KEY_WIFIREFRESHTIMEINTERVAL, DataModule.G_WIFIREFRESHTIMEINTERVAL); // wifi网络时刷新间隔秒
        DataModule.G_DATABASE_VERNUMBER = getDBHelper().getInt(DataModule.G_KEY_DATABASE_VERNUMBERL, DataModule.G_DATABASE_VERNUMBER); // 打包的数据库版本号
        DataModule.G_BAIDU_PUSH_CHANNELID = getDBHelper().getString(DataModule.G_KEY_BAIDU_PUSH_CHANNELID, "");


        String umengSdk_channel = AnalyticsConfig.getChannel(getApplicationContext());
        if (umengSdk_channel == null || umengSdk_channel.equals("")) {
            // 从xml meta data获取渠道名
            try {
                ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                String sChannel = appInfo.metaData.getString("UMENG_CHANNEL");
                if (sChannel != null && !sChannel.equals("")) {
                    DataModule.G_APK_CHANEL = sChannel;
                } else {
                    int chanel = appInfo.metaData.getInt("UMENG_CHANNEL");
                    if (chanel != 0) {
                        DataModule.G_APK_CHANEL = String.valueOf(chanel);
                    }
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            DataModule.G_APK_CHANEL = umengSdk_channel;
        }

        LogUtil.easylog("sky", "SecurityHome->load DataModule.G_APK_CHANEL:" + DataModule.G_APK_CHANEL);

        getAppPackageInfo();

        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        userInfo.load(getDBHelper());
        userInfo.setChannel(DataModule.G_APK_CHANEL);

        DataModule.getInstance().getOptionalInfo().loadVisitors(getDBHelper());// 加载本地自选股
        // 游客

        List<Goods> goodsList = DataModule.getInstance().getOptionalInfo().getAllGoods();
        if (goodsList.size() == 0) {
            goodsList.add(new Goods(1, "上证指数"));
            goodsList.add(new Goods(1399001, "深证成指"));
            DataModule.getInstance().getOptionalInfo().save(getDBHelper());
        }

        final SplashPage splashPage = new SplashPage();
        splashPage.setDelayedTime(800);
        splashPage.setContentView(View.inflate(this, R.layout.page_splash, null));
        TextView t_tvBootPageVer = (TextView) splashPage.getContentView().findViewById(R.id.bootpage_tv_ver);
        String t_verinfo = String.format("V%s", DataModule.G_APKVERNUMBER);
        t_tvBootPageVer.setText(t_verinfo);
        splashPage.setSupportAnimation(false);
        splashPage.setOnSplashListener(new SplashPage.OnSplashListener() {

            @Override
            public void onSplashStarted(View view) {

            }

            @Override
            public void onSplashFinished(View view) {
                PageIntent intent = new PageIntent(null, MainPage.class);
                goToMainPage(intent);
                LogUtil.easylog("sky", "onSplashFinished -> startPage:mainPage");
            }
        });
        PageIntent splashIntent = new PageIntent(null, splashPage);
        splashIntent.setFlags(PageIntent.FLAG_PAGE_NO_HISTORY);

        String sLastBootGuideVer = getDBHelper().getString(DataModule.G_KEY_BOOT_GUIDE_VER, "0");//
        // 判断是否显示引导页
        int nRet = DataUtils.compareVersion(DataModule.G_BOOT_GUIDE_VERSION, sLastBootGuideVer);

        // JPushManager.updateAliasAndTags(null, null);
        if (nRet > 0) {
            getDBHelper().setString(DataModule.G_KEY_BOOT_GUIDE_VER, DataModule.G_BOOT_GUIDE_VERSION);

        } else {
            // 不判断引导页显示逻辑
            // startPage(R.id.securityhome_frame, intent);
        }
        // 强制关闭引导页
        startPage(R.id.module_frame, splashIntent);

        int bootCount = getDBHelper().getInt(DataModule.G_KEY_BOOT_COUNT, 0);
        bootCount++;
        getDBHelper().setInt(DataModule.G_KEY_BOOT_COUNT, bootCount);

        mStockService = new Intent(this, StockService.class);

        // 手动写死bs ip
        setHost(null);

        final Timer t_timer = new Timer();
        t_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - mTimeBegin > DataModule.G_BOOT_TIMEOUT_MAX) {
                    t_timer.cancel();
                } else if (mFlag_canEnterMain < 0 || mFlag_canEnterMain >= 1) {
                    t_timer.cancel();
                    startService(mStockService);
                }
            }
        }, 0, 200);

    }

    private void goToMainPage(final PageIntent intent) {
        final Timer t_timer = new Timer();
        t_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                t_timer.cancel();
                mLoadBalance.notifyFinish();
                if (System.currentTimeMillis() - mTimeBegin > DataModule.G_BOOT_TIMEOUT_MAX) {
                    setHost(getDBHelper().getString(DataModule.G_KEY_LAST_SERVER, RequestUrl.host0));
                } else if (DataModule.LOAD_STATE_GOODTABLE >= 1) {
                    setHost(null);
                }
                startPage(R.id.module_frame, intent);
            }
        }, 0, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onResume(this);
        BaiduPushManager_v2.onResumeWork();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        if (DataModule.G_APP_IS_ACTIVE_FOREGROUND == false) {
            DataModule.G_APP_IS_ACTIVE_FOREGROUND = true;
        }

        mResumeCount++;
        if (mResumeCount > 1) {
            startService(mStockService);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // JPushInterface.onPause(this);
        // BaiduPushManager.onStopWork();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        if (!isAppOnForeground()) {
            DataModule.G_APP_IS_ACTIVE_FOREGROUND = false;
        }
        super.onStop();
    }

    @Override
    public void receiveData(Intent intent) {
        super.receiveData(intent);
    }

    @Override
    public void receiveNewData(Intent intent) {
        super.receiveNewData(intent);

    }

    public GlobalDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new GlobalDBHelper(this, DataModule.DB_GLOBAL);
        }
        return mDBHelper;
    }

    private void getAppPackageInfo() {
        try {
            PackageManager manager = SecurityHome.this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(SecurityHome.this.getPackageName(), 0);
            DataModule.G_APKVERNUMBER = info.versionName;
            Resources res = SecurityHome.this.getResources();
            DataModule.G_APKNAME = res.getString(R.string.app_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    /**
     * ViewPager适配器
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == EXIT_APP_CMD) {
            if (mStockService != null) {
                try {
                    stopService(mStockService);
                } catch (Exception e) {
                }
            }

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    MobclickAgent.onKillProcess(SecurityHome.this);
                    System.exit(0);
                }
            }, 1000);
            finish();

            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
        QQAuth t_qAuth = ThirdPartyLogin.getInstance().getQQAuth();
        if (t_qAuth != null) {
            t_qAuth.notifyActResult(requestCode, resultCode, intent);
        }
    }

    public static String getDeviceInfo(Context context) {
        try {
            JSONObject json = new JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = tm.getDeviceId();
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 测试代码
    private void testFunc() {}

    private void setHost(String host) {
        if (host != null && !host.equals("")) {
            RequestUrl.host = host;
        }

        // 使用测试Host 如使用LB,请注释该调用
        // 如用户在Server Setting设置过Host,使用用户设置
        // 否则使用预设的Host
        setDebugHost(RequestUrl.host6);
    }

    /**
     * 
     * @param host
     */
    private void setDebugHost(String host) {
        String debugHost = getDBHelper().getString(DataModule.G_KEY_LAST_DEBUG_SERVER, "");
        if (TextUtils.isEmpty(debugHost)) {
            debugHost = host;
        }

        // 测试用
        RequestUrl.host = debugHost;
    }


    @Override
    protected void onDestory() {
        super.onDestory();
        MobclickAgent.onKillProcess(this);
    }
}
