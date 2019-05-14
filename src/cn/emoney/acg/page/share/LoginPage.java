package cn.emoney.acg.page.share;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.optional.OptionalSharePackage;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply;
import cn.emoney.acg.data.protocol.optional.OptionalShareRequest.OptionalShare_Request;
import cn.emoney.acg.data.quiz.QuizConfigData;
import cn.emoney.acg.dialog.CustomProgressDialog.OnCancelProgressDiaListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.helper.push.BaiduPushManager_v2;
import cn.emoney.acg.helper.thirdpartylogin.QQAuth;
import cn.emoney.acg.helper.thirdpartylogin.ThirdPartyLogin;
import cn.emoney.acg.helper.thirdpartylogin.ThirdPartyLoginListener;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.authentification.FindPwdPage;
import cn.emoney.acg.page.authentification.UserRegisterPage;
import cn.emoney.acg.page.motif.MotifHome;
import cn.emoney.acg.util.DeviceInfoUtil;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.util.RegularExpressionUtil;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.page.PageIntent;

public class LoginPage extends PageImpl {
    public static void gotoLogin(Page page, int requestCode) {
        PageIntent intent = new PageIntent(page, LoginPage.class);
        page.startPageForResult(DataModule.G_CURRENT_FRAME, intent, requestCode);
    }

    public static final int PAGECODE = 41000;

    public final static String KEY_USER_NAME = "key_user_name";
    public final static String KEY_USER_PWD = "key_user_pwd";

    private EditText mEdtUsername = null;
    private EditText mEdtPwssword = null;

    private ImageButton mIbUserNameClear = null;
    private ImageButton mIbPwdClear = null;

    // debug 入口指纹
    private final String DEBUG_ENTRANCE_CODE = "b25d5471dd58f2c12b2ed719e3c3588c";

    // private final String DEBUG_CHANGE_SERVER_CODE =
    // "4c56ff4ce4aaf9573aa5dff913df997a"; //121

    @Override
    protected void receiveData(Bundle arguments) {}

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_login);

        mIbUserNameClear = (ImageButton) findViewById(R.id.loginpage_btn_username_clear);
        mIbPwdClear = (ImageButton) findViewById(R.id.loginpage_btn_pwd_clear);

        mEdtUsername = (EditText) findViewById(R.id.loginpage_edt_username);
        mEdtPwssword = (EditText) findViewById(R.id.loginpage_edt_password);

        mIbUserNameClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtUsername.setText("");
            }
        });

        mIbPwdClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtPwssword.setText("");
            }
        });

        mEdtUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String text = mEdtUsername.getText().toString();
                if (text.length() > 0) {
                    mIbUserNameClear.setVisibility(View.VISIBLE);
                } else {
                    mIbUserNameClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        mEdtPwssword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String text = mEdtPwssword.getText().toString();
                if (text.length() > 0) {
                    mIbPwdClear.setVisibility(View.VISIBLE);
                } else {
                    mIbPwdClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        UserInfo userInfo = getUserInfo();
        if (userInfo != null) {
            mEdtUsername.setText(userInfo.getUsername());
        }

        // 设置光标
        mEdtUsername.requestFocus();
        mEdtUsername.setSelection(mEdtUsername.getEditableText().toString().length());

        findViewById(R.id.loginpage_tv_login).setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                // TODO Auto-generated method stub
                InputMethodUtil.closeSoftKeyBoard(LoginPage.this);
                String username = mEdtUsername.getText().toString();
                String password = mEdtPwssword.getText().toString();

                if (username == null || username.equals("")) {
                    showTip("用户名不能为空");
                    return;
                }

                if (password == null || password.length() < 3 || password.length() > 12) {
                    showTip("密码格式错误(3-12位)");
                    return;
                }

                boolean bRet = tyrMatchDebugCode(username, password);
                if (bRet) {
                    showTip("开启debug model成功");
                    DataModule.G_USER_DEBUG = true;
                    return;
                }

                if (!RegularExpressionUtil.isEmail(username) && !RegularExpressionUtil.isMobilePhoneNum(username)) {
                    showTip("用户名须为手机号或邮箱");
                    return;
                }

                requestLogin(username, password, -1);
            }
        });

        findViewById(R.id.qqloginLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodUtil.closeSoftKeyBoard(LoginPage.this);

                QQAuth t_qAuth = ThirdPartyLogin.getInstance().getQQAuth(getContext());
                t_qAuth.login(getActivity(), new ThirdPartyLoginListener() {

                    @Override
                    public void onFail(int errCode) {
                        showTip("认证失败");
                    }

                    @Override
                    public void onComplete(JSONObject object) {
                        try {
                            String sUName = object.getString(ThirdPartyLoginListener.LOGIN_RET_OPENID);
                            String sUPwd = object.getString(ThirdPartyLoginListener.LOGIN_RET_TOKEN);
                            requestLogin(sUName, sUPwd, 100);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        findViewById(R.id.pagelogin_ll_content).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodUtil.closeSoftKeyBoard(LoginPage.this);
            }
        });

        findViewById(R.id.contentLayout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodUtil.closeSoftKeyBoard(LoginPage.this);
            }
        });

        // 找回密码
        findViewById(R.id.forgetPwdTV).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PageIntent intent = new PageIntent(LoginPage.this, FindPwdPage.class);
                startPageForResult(intent, LoginPage.PAGECODE);
            }
        });

        TextView tipView = (TextView) findViewById(R.id.tipView);
        final String str = "如果你还没有爱炒股的账号，请点击 快速注册";
        SpannableString ss = new SpannableString(str);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startRegisterPage();
            }
        }, str.length() - 4, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(RColor(R.color.c4)), str.length() - 4, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tipView.setText(ss);
        tipView.setMovementMethod(LinkMovementMethod.getInstance());

        // titleBar
        bindPageTitleBar(R.id.pageLoginTitleBar);
    }


    @Override
    protected void onPageResult(int requestCode, int resultCode, Bundle data) {
        super.onPageResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        String t_uName = null;
        String t_uPwd = null;

        if (data.containsKey(LoginPage.KEY_USER_NAME)) {
            t_uName = data.getString(LoginPage.KEY_USER_NAME);
        }
        if (data.containsKey(LoginPage.KEY_USER_PWD)) {
            t_uPwd = data.getString(LoginPage.KEY_USER_PWD);
        }

        if (requestCode == LoginPage.PAGECODE) {
            if (resultCode == UserRegisterPage.PAGECODE) {
                // 注册返回
                if (t_uName != null && !t_uName.equals("") && t_uPwd != null && !t_uPwd.equals("")) {
                    requestLogin(t_uName, t_uPwd, -1);
                }
            } else if (resultCode == FindPwdPage.PAGECODE) {
                // 重置密码返回
                if (t_uName != null && !t_uName.equals("") && t_uPwd != null && !t_uPwd.equals("")) {
                    requestLogin(t_uName, t_uPwd, -1);
                }
            }
        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }

    // 请求自选股数据
    private void requestOptionalStock() {
        LogUtil.easylog("requestOptionalStock->token:" + getUserInfo().getToken());
        OptionalSharePackage pkg = new OptionalSharePackage(new QuoteHead(IDUtils.ID_OPTIONAL_QUERY_STOCKS_PB));
        pkg.setRequest(OptionalShare_Request.newBuilder().setToken(getUserInfo().getToken()).build());
        requestQuote(pkg, IDUtils.ID_OPTIONAL_QUERY_STOCKS_PB);
    }

    // 请求用户权限
    private void requestUserPermission() {
        UserInfo userInfo = getUserInfo();
        String t_token = userInfo.getToken();
        if (t_token != null && !t_token.equals("")) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put(KEY_TOKEN, t_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsObj, IDUtils.ID_USER_PERMISSION);
        }
    }

    // 获取额外的用户信用 如昵称,头像id
    private void requestExtraUserInfo() {
        UserInfo userInfo = getUserInfo();
        String t_token = userInfo.getToken();
        if (t_token != null && !t_token.equals("") && !t_token.equals(DataModule.G_GUEST_TOKEN)) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put(KEY_TOKEN, t_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsObj, IDUtils.ID_USER_EXTRAINFO);
        }
    }

    // 登录请求
    private void requestLogin(String userName, String password, int loginType) {
        showProgressDialog();

        // 本地有数据的情况 且 上次的登录状态为登录时,自动登录
        if (userName != "" && password != "") {
            JSONObject jsObj = new JSONObject();

            String pack_pwd = "";
            int accountType = 0;

            if (loginType == -1) {
                if (RegularExpressionUtil.isEmail(userName)) {
                    accountType = 3; // 邮箱
                } else if (RegularExpressionUtil.isMobilePhoneNum(userName)) {
                    accountType = 2; // 手机号
                } else {
                    accountType = 1; // EM
                }
                pack_pwd = MD5Util.md5(userName + MD5Util.md5(password));
            } else {
                accountType = loginType;
                pack_pwd = password;
            }

            try {
                int nBuildNum = Integer.valueOf(DataModule.G_APKBUILDNUMBER.replaceAll("\\.", ""));
                String sModel = DeviceInfoUtil.getInstance().mModel;
                String sOSVer = DeviceInfoUtil.getInstance().mSDKVer;
                String sHard = DeviceInfoUtil.getInstance().mDevice;

                jsObj.put("u", userName);
                jsObj.put("t", accountType); // 账号类型
                jsObj.put("a", pack_pwd);
                jsObj.put("s", 1232); // stid最大4位整数 固定
                jsObj.put("c", 2); // 登录类型
                jsObj.put("v", nBuildNum); // 产品build号
                jsObj.put("h", sHard); // 硬件信息 最长16位字符
                jsObj.put("os", sOSVer);
                jsObj.put("dn", sModel); // 设备
                requestInfo(jsObj, IDUtils.ID_USER_LOGIN);
                // requestInfo(jsObj, IDUtils.ID_USER_LOGIN, RequestUrl.host4);
                LogUtil.easylog("sky", "login req: " + jsObj.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void updateWhenDecodeError() {
        onLoginFail(null);
    }

    @Override
    protected void updateWhenNetworkError() {
        onLoginFail(null);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();
        if (pkg instanceof GlobalMessagePackage) {
            GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
            MessageCommon gr = goodsTable.getResponse();
            if (gr == null || gr.getMsgData() == null) {
                return;
            }

            String msgData = gr.getMsgData();
            if (id == ID_USER_LOGIN) {
                // 登录
                LogUtil.easylog("******************login user");
                parserLoginInfo(msgData);
            } else if (id == ID_USER_PERMISSION) {
                // 权限
                LogUtil.easylog("******************login permission");
                pareserPermission(msgData);
            } else if (id == ID_USER_EXTRAINFO) {
                // 用户信息
                LogUtil.easylog("******************login infoEx");
                pareserUserInfo(msgData);
            }
            // else if (id == ID_OPTIONAL_QUERY_STOCKS) {
            // // 自选股
            // pareserOption(msgData);
            // }
        }
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        if (pkg != null && pkg instanceof OptionalSharePackage) {
            OptionalSharePackage optionalPkg = (OptionalSharePackage) pkg;
            OptionalShare_Reply reply = optionalPkg.getResponse();
            if (reply != null) {
                pareserOption(reply);
            }
        }
    }

    // 解析登录信息
    private void parserLoginInfo(String msgData) {
        // {"id":3870,
        // "message":"success to login",
        // "nick":"",
        // "result":0,
        // "source":"QD10000021",
        // "token":"8e13a35bd2ff53f95fb640503ca98aac",
        // "type":3,
        // "user":"wuyiwen@emoney.cn"}
        LogUtil.easylog("login recv:ID_USER_LOGIN:" + System.currentTimeMillis());
        try {
            JSONObject jsObj = JSON.parseObject(msgData);
            LogUtil.easylog("sky", "login reply: " + jsObj.toString());

            int result = jsObj.getIntValue("result");
            String msg = jsObj.getString("message");
            if (result == 0) {
                UserInfo userInfo = getUserInfo();
                String nick = jsObj.getString("nick");
                String channel = jsObj.getString("source");
                int type = jsObj.getIntValue("type");
                String token = jsObj.getString("token");
                String userName = jsObj.getString("user");
                int t_uid = jsObj.getIntValue("id");
                String sUid = "0";
                try {
                    sUid = String.valueOf(t_uid);
                } catch (Exception e) {
                }

                String headid = "0";
                if (jsObj.containsKey("head_id")) {
                    headid = jsObj.getString("head_id");
                }

                // 保存数据
                int nType = jsObj.getIntValue("type");

                // 不再保存密码
                String userPwd = "";

                userInfo.setPassword(userPwd);
                userInfo.setNickName(nick);
                userInfo.setToken(token);
                userInfo.setAccountType(type);
                userInfo.setChannel(channel);
                userInfo.setUsername(userName);
                userInfo.setUid(sUid);
                userInfo.setHeadId(headid);

                userInfo.setLogined(true);

                userInfo.save(getDBHelper());

                LogUtil.easylog("LoginPage->updateInfo->uid:" + sUid);

                // 登录成功,发送获取权限请求（请求顺序：权限->自选股->用户信息）
                requestUserPermission();

                // DataModule.G_LAST_LOGIN_STATE = 1;
                // getDBHelper().setInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE,
                // DataModule.G_LAST_LOGIN_STATE);
                // BuyClubHome.isNeedUpdateMineType = true;
                // // 修改jpush tag为用户
                // JPushManager.updateAliasAndTags("identity", "MEMBER");
            } else {
                // 失败
                onLoginFail(msg);
            }
        } catch (JSONException e) {
            // 失败
            onLoginFail(null);
            e.printStackTrace();
        }
    }

    // 解析权限
    private void pareserPermission(String msgData) {
        try {
            boolean res = getLoginDataParser().pareserPermission(msgData);
            if (res) {
                // 请求自选股
                requestOptionalStock();
            } else {
                // 失败
                onLoginFail(null);
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            // 失败
            onLoginFail(null);
        }
    }

    // 解析自选股
    private void pareserOption(OptionalShare_Reply reply) {
        try {
            boolean ret = getLoginDataParser().parserOptionalInfo_pb(reply);
            if (ret) {
                // 用户信息请求
                requestExtraUserInfo();
            } else {
                // 失败
                onLoginFail(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 失败
            onLoginFail(null);
        }
    }

    // 解析用户信息
    private void pareserUserInfo(String msgData) {
        try {
            boolean res = getLoginDataParser().pareserUserInfo(msgData);
            if (res) {
                // 登录成功
                onLoginSucc();
            } else {
                // 失败
                onLoginFail(null);
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            // 失败
            onLoginFail(null);
        }
        DialogUtils.closeProgressDialog();
    }

    // 登录成功
    private void onLoginSucc() {
        // 保存登录状态
        DataModule.G_LAST_LOGIN_STATE = 1;
        getDBHelper().setInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE, DataModule.G_LAST_LOGIN_STATE);
        MotifHome.isNeedUpdateMineType = true;

        getUserInfo().setLogined(true);
        getUserInfo().save(getDBHelper());

        // 修改百度百度推送为在线
        BaiduPushManager_v2.setOnline();


        // 广播
        sendBroadcast(BroadCastName.BCDC_CHANGE_LOGIN_STATE);
        DialogUtils.closeProgressDialog();

        // 加载问股的配置信息
        QuizConfigData.getInstance().loadServerConfig();

        finish();
    }

    // 登录失败
    private void onLoginFail(String msg) {
        // 登录失败
        // 修改百度百度推送为在线
        BaiduPushManager_v2.setOffLine();

        if (msg != null && msg.trim().length() > 0) {
            showTip(msg);
        } else {
            showTip("登录失败");
        }

        DataModule.getInstance().getUserInfo().setLogined(false);
        DialogUtils.closeProgressDialog();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "用户登录");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        View rightView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView textView = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        textView.setText("注册");
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            InputMethodUtil.closeSoftKeyBoard(LoginPage.this);
            finish();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            startRegisterPage();
        }
    }

    // 启动注册页面
    private void startRegisterPage() {
        PageIntent intent = new PageIntent(this, UserRegisterPage.class);
        startPageForResult(intent, LoginPage.PAGECODE);
    }

    // 特殊code,验证密码进入debug
    private boolean tyrMatchDebugCode(String arg1, String arg2) {
        boolean bRet = false;
        String CurCode = MD5Util.md5(arg1 + arg2);
        if (CurCode.equals(DEBUG_ENTRANCE_CODE)) {
            bRet = true;
        }
        return bRet;
    }

    private void showProgressDialog() {
        DialogUtils.showProgressDialog(getActivity(), new OnCancelProgressDiaListener() {
            @Override
            public void onCancelProgressDia() {
                // TODO Auto-generated method stub
            }
        });
    }

    private LoginDataParser loginDataParser = null;

    private LoginDataParser getLoginDataParser() {
        if (loginDataParser == null) {
            loginDataParser = new LoginDataParser(getContext(), this);
        }

        return loginDataParser;
    }
}
