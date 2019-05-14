package cn.emoney.acg.page.settings;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class PwdChangePage extends PageImpl {
    private EditText mEdtOldPwd = null;
    private EditText mEdtNewPwd = null;
    private TextView mTvConfirm = null;

    private ImageButton mIbOldPwdClear = null;
    private ImageButton mIbNewPwdClear = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_change_pwd);

        mTvConfirm = (TextView) findViewById(R.id.changepwdpage_tv_confirm_change);
        mIbOldPwdClear = (ImageButton) findViewById(R.id.changepwdpage_btn_oldpwd_clear);
        mIbNewPwdClear = (ImageButton) findViewById(R.id.changepwdpage_btn_newpwd_clear);

        mEdtOldPwd = (EditText) findViewById(R.id.changepwdpage_edt_oldpwd);
        mEdtNewPwd = (EditText) findViewById(R.id.changepwdpage_edt_newpwd);

        // 设置光标
        mEdtOldPwd.requestFocus();
        mEdtOldPwd.setSelection(mEdtOldPwd.getEditableText().toString().length());

        mIbOldPwdClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtOldPwd.setText("");
            }
        });

        mIbNewPwdClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtNewPwd.setText("");
            }
        });

        mEdtOldPwd.addTextChangedListener(new TextWatcher() {

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
                String text = mEdtOldPwd.getText().toString();
                if (text.length() > 0) {
                    mIbOldPwdClear.setVisibility(View.VISIBLE);
                } else {
                    mIbOldPwdClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        // mEdtNewPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // if (hasFocus) {
        // mIbNewPwdClear.setVisibility(View.VISIBLE);
        // } else {
        // mIbNewPwdClear.setVisibility(View.INVISIBLE);
        // }
        // }
        // });
        mEdtNewPwd.addTextChangedListener(new TextWatcher() {

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
                String text = mEdtNewPwd.getText().toString();
                if (text.length() > 0) {
                    mIbNewPwdClear.setVisibility(View.VISIBLE);
                } else {
                    mIbNewPwdClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        mTvConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String t_oldpwd = mEdtOldPwd.getText().toString();
                String t_newpwd = mEdtNewPwd.getText().toString();
                if (t_oldpwd == null || t_oldpwd.length() < 3 || t_oldpwd.length() > 12) {
                    showTip("请输入正确的旧密码(3-12位)");
                    return;
                } else if (t_newpwd == null || t_newpwd.length() < 3 || t_oldpwd.length() > 12) {
                    showTip("请输入正确的新密码(3-12位)");
                    return;
                }

                requestChangePwd();
                InputMethodUtil.closeSoftKeyBoard(PwdChangePage.this);
            }
        });

        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub

    }

    private void requestChangePwd() {
        // uid:"2281” //ID
        // passwdnew:"xxx//新密码
        // passwdold:”xxxx” //旧密码
        UserInfo userInfo = getUserInfo();
        if (userInfo == null || userInfo.getUid().equals("0")) {
            showTip("安全检测失败,请先重新登录");
            return;
        }

        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put("uid", userInfo.getUid());
            jsObj.put("passwdold", mEdtOldPwd.getText().toString());
            jsObj.put("passwdnew", mEdtNewPwd.getText().toString());
            requestInfo(jsObj, IDUtils.ID_USER_CHANGEPWD);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        if (pkg instanceof GlobalMessagePackage) {
            GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
            MessageCommon gr = goodsTable.getResponse();
            if (gr == null || gr.getMsgData() == null) {
                showTip("修改失败,请稍后再试");
                return;
            }

            String msgData = gr.getMsgData();
            LogUtil.easylog("sky", "PwdChange->updateFromInfo: " + msgData);
            if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                showTip("修改失败,请稍后再试");
                return;
            }
            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retCode = jsObj.getIntValue("retCode");
                String retMsg = jsObj.getString("retMsg");
                if (retCode != 0) {
                    showTip(retMsg);
                } else {
                    showTip("修改密码成功");
                    InputMethodUtil.closeSoftKeyBoard(this);
                    PwdChangePage.this.finish();
                }
            } catch (Exception e) {
                showTip("修改失败,请稍后再试");
            }
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "修改密码");
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            InputMethodUtil.closeSoftKeyBoard(this);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
