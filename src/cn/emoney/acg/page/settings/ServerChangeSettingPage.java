package cn.emoney.acg.page.settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.MinuteLinePackage;
import cn.emoney.acg.data.protocol.quote.MinuteLineReply.MinuteLine_Reply.MinuteData;
import cn.emoney.acg.data.protocol.quote.MinuteLineRequest.MinuteLine_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.BackupUtil;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.DownloadFileThread;
import cn.emoney.acg.util.DownloadFileThread.DownloadFileCallBack;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class ServerChangeSettingPage extends PageImpl implements OnClickListener {
    private TextView mTvCurrentInfo = null;
    private EditText mEtIp = null;
    private EditText mEtPort = null;
    private TextView mBtnConfirm = null;
    private TextView mBtnHostClear = null;

    private TextView mTvLogcat = null;

    private List<TextView> mLstToolBtn = new ArrayList<TextView>();

    private long dTime = 0;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    private ArrayList<Goods> mAryLstGoods = null;

    @SuppressLint("NewApi")
    @Override
    protected void initPage() {
        setContentView(R.layout.page_setting_serverchange);
        mTvCurrentInfo = (TextView) findViewById(R.id.debug_serverchange_tv_current_ip);
        mEtIp = (EditText) findViewById(R.id.debug_serverchange_et_ip);
        mEtPort = (EditText) findViewById(R.id.debug_serverchange_et_port);
        mBtnConfirm = (TextView) findViewById(R.id.debug_serverchange_btn_confirm);
        mBtnHostClear = (TextView) findViewById(R.id.debug_serverchange_btn_clear);
        mTvLogcat = (TextView) findViewById(R.id.debug_tv_logcat);

        mTvLogcat.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView tv = (TextView) v;
                String t_s = tv.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
                clipboardManager.setText(t_s);

                showTip("内容已复制到剪切版");
                return false;
            }
        });

        refreshCurrentHostDisplay();

        mBtnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodUtil.closeSoftKeyBoard(ServerChangeSettingPage.this);
                String sIp = mEtIp.getText().toString();
                String sPort = mEtPort.getText().toString();
                if (sIp != null && !sIp.equals("") && sPort != null && !sPort.equals("")) {
                    int iPort = 8080;
                    try {
                        iPort = Integer.valueOf(sPort);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    RequestUrl.host = String.format(RequestUrl.HOST_FORMAT, sIp, iPort);
                    LogUtil.easylog("sky", "ServerChange:->host:" + RequestUrl.host);
                    getDBHelper().setString(DataModule.G_KEY_LAST_SERVER, RequestUrl.host);
                    getDBHelper().setString(DataModule.G_KEY_LAST_DEBUG_SERVER, RequestUrl.host);
                    refreshCurrentHostDisplay();
                    showTip("更改成功");
                } else {
                    showTip("更改失败");
                }
            }
        });

        mBtnHostClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodUtil.closeSoftKeyBoard(ServerChangeSettingPage.this);
                getDBHelper().setString(DataModule.G_KEY_LAST_DEBUG_SERVER, "");
                showTip("清除host本地缓存成功");
            }
        });

        mLstToolBtn.clear();
        TextView tv_btn = (TextView) findViewById(R.id.debug_tv_tool_back_goodstable);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_testhttp);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_log_switch);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_test_encrypt_decrypt);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_test_LB);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_test_common);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_test_token);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_cleardata);
        mLstToolBtn.add(tv_btn);
        tv_btn = (TextView) findViewById(R.id.debug_tv_tool_push);
        mLstToolBtn.add(tv_btn);

        for (int i = 0; i < mLstToolBtn.size(); i++) {
            mLstToolBtn.get(i).setOnClickListener(this);
        }

        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
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

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "切换服务器(Debug)");
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            InputMethodUtil.closeSoftKeyBoard(this);
            finish();
            return true;
        }
        return false;
    }

    private void refreshCurrentHostDisplay() {
        if (mTvCurrentInfo == null) {
            return;
        }
        if (RequestUrl.host == null || RequestUrl.host.equals("")) {
            return;
        }

        String[] t_aryHost = RequestUrl.host.split("/");

        String notice = "";
        notice += ("UID:" + DataModule.getInstance().getUserInfo().getUid() + "\n");
        notice += ("当前Host:" + t_aryHost[2] + "\n");
        notice += "UM_Channel:" + DataModule.G_APK_CHANEL + "\n";
        notice += "Push_Channel:" + DataModule.G_BAIDU_PUSH_CHANNELID;

        mTvCurrentInfo.setText(notice);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.debug_tv_tool_back_goodstable:
                // 2. 打包备份码表数据库 备份至shopbackup
                mTvLogcat.setText("开始备份");
                BackupUtil backupUtilTask = new BackupUtil(getContext());
                backupUtilTask.execute(BackupUtil.COMMAND_BACKUP);

                mTvLogcat.setText("备份成功" + "\n" + "码表日期:" + DataModule.G_DATABASE_VERNUMBER + "\n" + "路径:" + "/sdcard/emoney/istock/DB_BACK/estockgoods");
                break;
            case R.id.debug_tv_tool_testhttp:
                if (!bIsHttpTestStart) {
                    if (mAryLstGoods == null) {
                        mAryLstGoods = getSrearchResultByLocal("600");
                    }

                    String t_log = "测试启动!";
                    mTvLogcat.setText(t_log);
                    bIsHttpTestStart = true;
                    doTestHttp();
                } else {
                    String t_log = "测试停止!\n日志路径:" + "/sdcard/ACG_Debug_Log/http_test_XXX.txt";
                    mTvLogcat.setText(t_log);
                    if (getLogger() != null) {
                        getLogger().appendln("********End*******");
                        getLogger().output("ACG_Debug_Log", true);
                    }
                    bIsHttpTestStart = false;
                }
                break;
            case R.id.debug_tv_tool_log_switch:
                break;
            case R.id.debug_tv_tool_test_encrypt_decrypt:
                // 3. 生成密钥 加解密 测试
                // String sKey = AESUtil.createFixAESKey(256);
                // try {
                // String sTest = "代志鹏,alvin";
                // AESUtil aesUtil = new AESUtil();
                // byte[] byteEncode = aesUtil.Encrytor(sTest);
                // BASE64Encoder base64Encoder = new BASE64Encoder();
                // String sSave = base64Encoder.encode(byteEncode);
                // BASE64Decoder base64Decoder = new BASE64Decoder();
                // byte[] byteDecoder = base64Decoder.decodeBuffer(sSave);
                // byte[] byteOut = aesUtil.Decryptor(byteDecoder);
                // String sOut = new String(byteOut, "utf-8");
                // LogUtil.easylog("sky", sOut);
                // } catch (NoSuchAlgorithmException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (NoSuchPaddingException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (InvalidKeyException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (IllegalBlockSizeException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (BadPaddingException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (IOException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }



                break;
            case R.id.debug_tv_tool_test_LB:
                // 1. lb测试
                // LoadBalance loadBalance = new LoadBalance(null);
                break;
            case R.id.debug_tv_tool_test_common: {
                // String text = "基于java语言开发的轻量级的中文分词工具包";
                // String text1 = "大家一起努力吧,把爱炒股做成中国第一的炒股软件,还有手机android版哦";
                // // 创建分词对象
                // IKAnalyzer anal = new IKAnalyzer(true);
                // StringReader reader = new StringReader(text);
                // // 分词
                // TokenStream ts = anal.tokenStream("", reader);
                // CharTermAttribute term =
                // ts.getAttribute(CharTermAttribute.class);
                // // 遍历分词数据
                // try {
                // while (ts.incrementToken()) {
                // System.out.print(term.toString() + "|");
                // }
                // } catch (IOException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                // reader.close();
                // System.out.println();
                // reader = new StringReader(text1);
                // ts = anal.tokenStream("", reader);
                // term = ts.getAttribute(CharTermAttribute.class);
                // // 遍历分词数据
                // try {
                // while (ts.incrementToken()) {
                // System.out.print(term.toString() + "|");
                // }
                // } catch (IOException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                // System.out.println();

                doTestDownload("http://down3.emstock.com.cn/istock/phone_files/release/0/test_record.apk", "amr");
            }
                break;

            case R.id.debug_tv_tool_test_token:
                mTvLogcat.setText("token:" + DataModule.getInstance().getUserInfo().getToken());
                break;

            case R.id.debug_tv_tool_cleardata:
                getDBHelper().clear();
                int bootCount = getDBHelper().getInt(DataModule.G_KEY_BOOT_COUNT, -999);
                mTvLogcat.setText("用户数据已清空,第 " + bootCount + "次启动");
                break;

            case R.id.debug_tv_tool_push:
                createMsgNotifycation(getContext(), "debug test push", "debug internal push", null);
                break;
            default:
                break;
        }
    }

    private static boolean bIsHttpTestStart = false;

    // 网络测试
    private void doTestHttp() {
        if (!bIsHttpTestStart) {
            return;
        }
        int goodid = 600884;

        if (mAryLstGoods != null) {
            int len = mAryLstGoods.size();
            int index = (int) (Math.random() * len);
            Goods goods = mAryLstGoods.get(index);
            goodid = goods.getGoodsId();
        }

        Date time = new Date();

        getLogger().append(mDateFormat.format(time));
        dTime = System.currentTimeMillis();
        // int goodid = 600300 + (int) (Math.random() * 600);

        MinuteLinePackage pkg = new MinuteLinePackage(new QuoteHead((short) 0));
        pkg.setRequest(MinuteLine_Request.newBuilder().setLastRecvTime(0).setLastUpdateMarketDate(0).setGoodsId(goodid).build());
        requestQuote(pkg, IDUtils.MinuteLine);
    }


    private long mLastTime = 0;

    // 请先check再update
    public void doTestDownload(String downUrl, String fileExt) {

        new DownloadFileThread(downUrl, "download_file_test." + fileExt, new DownloadFileCallBack() {
            @Override
            public void onStart() {
                mLastTime = DateUtils.getTimeStamp();
                LogUtil.easylog("doTestDownload->onStart:" + mLastTime);
                // mTvLogcat.setText("下载开始:" + mLastTime + "\n");
            }

            @Override
            public void onSuccess(String path) {
                long tTime = DateUtils.getTimeStamp();

                LogUtil.easylog("doTestDownload->onSuccess:" + tTime);
                String loginfo = DataUtils.mZDFFormat.format((tTime - mLastTime) / 1000f) + "秒";
                LogUtil.easylog("doTestDownload->耗时:" + loginfo);
                // String t = mTvLogcat.getText().toString() + "下载成功,耗时:" + loginfo;
                // mTvLogcat.setText(t);

                mLastTime = DateUtils.getTimeStamp();
            }

            @Override
            public void onProcess(int process) {}

            @Override
            public void onFail() {
                LogUtil.easylog("doTestDownload:Err");
                // mTvLogcat.setText("下载成功失败");

            }


        }).start();
    }

    // push
    @SuppressLint("NewApi")
    private void createMsgNotifycation(Context context, String title, String summary, String soundUri) {

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long when = System.currentTimeMillis();
        Resources res = context.getResources();

        // Intent notificationIntent = new Intent(context, SecurityHome.class);
        Intent notificationIntent = new Intent();

        PendingIntent contentIntent = PendingIntent.getActivity(context, 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder/* .setContentIntent(contentIntent) */.setSmallIcon(R.drawable.img_push_title_icon)// 设置状态栏里面的图标（小图标）
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.img_push_icon))// 下拉下拉列表里面的图标（大图标）
                .setWhen(when)// 设置时间发生时间
                .setAutoCancel(true)// 设置可以清除
                .setTicker("爱炒股:" + title) // 设置状态栏的显示的信息
                .setContentTitle(title)// 设置下拉列表里的标题
                .setContentText(summary);// 设置上下文内容
        Notification n = builder.build();// 获取一个Notification

        // android.resource://cn.emoney.acg/raw/audio_quiz_push_teacher
        if (soundUri != null && soundUri.startsWith("android.resource://cn.emoney.acg/raw/")) {
            n.sound = Uri.parse(soundUri);
        } else {
            n.defaults = Notification.DEFAULT_SOUND;// 设置为默认的声音
        }

        mNotificationManager.notify(1000, n);// 显示通知 break;
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg instanceof MinuteLinePackage) {
            MinuteLinePackage minuteLinePkg = (MinuteLinePackage) pkg;
            List<MinuteData> lstData = minuteLinePkg.getResponse().getTrendLineList();

            if (lstData == null || lstData.size() <= 0) {
                getLogger().appendln("Err: lstData.size() <= 0");
                String t_log = "网络出错,测试停止!\n日志路径:" + "/sdcard/ACG_Debug_Log/http_test_XXX.txt";
                mTvLogcat.setText(t_log);
                return;
            }

            MinuteData minuteData = lstData.get(lstData.size() - 1);
            float price = minuteData.getPrice();
            int gid = minuteLinePkg.getResponse().getGoodsId();
            String t_log = "OK: GoodId:" + gid + ", LastPrice:" + price + ", WasteTime:" + (System.currentTimeMillis() - dTime);
            getLogger().appendln(t_log);
            doTestHttp();
        }
    }

    @Override
    protected void onPagePause() {
        if (getLogger() != null) {
            getLogger().appendln("********End*******");
            getLogger().output("ACG_Debug_Log", true);
        }
        bIsHttpTestStart = false;
        super.onPagePause();
    }

    private ArrayList<Goods> getSrearchResultByLocal(String strInput) {

        ArrayList<Goods> cn = null;

        if (TextUtils.isDigitsOnly(strInput)) {
            cn = getSQLiteDBHelper().queryStockInfosByCode(strInput, 2000);
        }

        getSQLiteDBHelper().close();
        return cn;
    }

}
