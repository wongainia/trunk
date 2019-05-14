package cn.emoney.acg.page.my;

import android.os.Bundle;
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
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 编辑用户昵称
 * 
 * @ClassName: EditNickNamePage
 * @Description:
 * @author xiechengfa
 * @date 2015年11月19日 下午5:22:57
 *
 */
public class EditNickNamePage extends PageImpl {
    public static final String RESULT_DATA = "nick";
    // private final String URL = "http://user.i.emoney.cn/ucenter/editnick?longtoken=%s&nick=%s";
    private final String URL = "http://192.168.3.51:8011/ucenter/editnick?longtoken=%s&nick=%s";
    private String nickName = null;
    private String orgNickName = null;
    private TextView tipView = null;
    private EditText nameView = null;
    private ImageButton clearView = null;

    @Override
    protected void receiveData(Bundle arguments) {}

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_edit_nickname);

        clearView = (ImageButton) findViewById(R.id.clearView);
        nameView = (EditText) findViewById(R.id.nickNameView);
        tipView = (TextView) findViewById(R.id.tipView);

        clearView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                nameView.setText("");
            }
        });

        nameView.addTextChangedListener(new TextWatcher() {

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
                String text = nameView.getText().toString();
                if (text.length() > 0) {
                    clearView.setVisibility(View.VISIBLE);
                } else {
                    clearView.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (getUserInfo().getNickName() != null) {
            orgNickName = getUserInfo().getNickName();
            nameView.setText(getUserInfo().getNickName());
        } else {
            orgNickName = "";
        }

        // 设置光标
        nameView.requestFocus();
        nameView.setSelection(nameView.getEditableText().toString().length());

        // titleBar
        bindPageTitleBar(R.id.editNickNameTitleBar);

        // 显示键盘
        InputMethodUtil.switchSoftKeyBoard(this);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "修改昵称");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        View rightView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView textView = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        textView.setText("提交");

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            onFinishPage();
        } else if (itemId == 2) {
            onClickSubmit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回键
            onFinishPage();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void onFinishPage() {
        InputMethodUtil.closeSoftKeyBoard(this);
        if (checkIsEdited()) {
            showEditedDialog();
        } else {
            finishPage();
        }
    }

    // 是否编辑过
    private boolean checkIsEdited() {
        if (orgNickName != null && orgNickName.equals(nameView.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }

    private void showEditedDialog() {
        DialogUtils.showMessageDialog(getActivity(), "提示", "是否放弃对昵称的修改?", "放弃", "继续编辑", new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {
                // TODO Auto-generated method stub
                finishPage();
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub

            }
        });
    }

    private void finishPage() {
        mPageChangeFlag = -1;
        finish();
    }

    private void onClickSubmit() {
        showErrorMsg("");
        String nickName = nameView.getText().toString();
        if (nickName == null || nickName.equals("")) {
            showTip("昵称不能为空");
            return;
        }

        if (nickName.trim().length() > 15) {
            showTip("昵称最长15个字符");
            return;
        }

        if (orgNickName != null && orgNickName.equals(nickName)) {
            showTip("昵称没有修改");
            return;
        }

        InputMethodUtil.closeSoftKeyBoard(this);
        DialogUtils.showProgressDialog(getActivity(), null);
        requestEditName(nickName.trim());
    }

    private void requestEditName(String tempNickName) {
        nickName = tempNickName;
        try {
            // String url = String.format(URL, getUserInfo().getToken(),
            // Base64.encodeToString(tempNickName.getBytes("utf-8"), Base64.NO_WRAP));
            String url = String.format(URL, getUserInfo().getToken(), tempNickName);

            JSONObject jsObj = new JSONObject();
            jsObj.put(KeysInterface.KEY_URL, url);
            jsObj.put("token", getUserInfo().getToken());
            requestInfo(jsObj, IDUtils.ID_GROUP_HTTP_INTERFACE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            showTip("修改失败,请稍后再试");
        }
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        DialogUtils.closeProgressDialog();
        if (pkg instanceof GlobalMessagePackage) {
            GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
            MessageCommon gr = goodsTable.getResponse();
            if (gr == null || gr.getMsgData() == null) {
                showTip("修改失败,请稍后再试");
                return;
            }

            String msgData = gr.getMsgData();
            if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                showTip("修改失败,请稍后再试");
                return;
            }

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retServerCode = jsObj.getIntValue("errorCode");
                String body = jsObj.getString("body");
                if (retServerCode == 0 && body != null && !body.equals("")) {
                    JSONObject resObjObj = JSON.parseObject(body);
                    int retCode = resObjObj.getIntValue("retcode");
                    // retMsg是base64加密
                    // String retMsg = new String(Base64.decode(resObjObj.getString("RetMsg"),
                    // Base64.NO_WRAP));
                    if (retCode == 0) {
                        showTip("修改昵称成功");
                        getUserInfo().setNickName(nickName);
                        getUserInfo().save(getDBHelper());

                        Bundle bundle = new Bundle();
                        bundle.putString(RESULT_DATA, nickName);
                        setResult(RESULT_CODE, bundle);
                        finish();
                    } else {
                        String retMsg = resObjObj.getString("retmsg");
                        if (retMsg != null && retMsg.trim().length() > 0) {
                            showErrorMsg(retMsg);
                        } else {
                            showTip("修改失败,请稍后再试");
                        }
                    }
                } else {
                    showTip("修改失败,请稍后再试");
                }
            } catch (Exception e) {
                showTip("修改失败,请稍后再试");
            }
        } else {
            showTip("修改失败,请稍后再试");
        }
    }

    @Override
    protected void updateWhenNetworkError() {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        showTip("网络异常,请稍后再试");
    }

    @Override
    protected void updateWhenDecodeError() {
        // TODO Auto-generated method stub
        DialogUtils.closeProgressDialog();
        showTip("修改失败,请稍后再试");
    }

    private void showErrorMsg(String msg) {
        tipView.setText(msg);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    // private void requestEditName1(String usrName) {
    // UserInfo useInfo = getUserInfo();
    // HttpClient httpClient = new HttpClient(getContext());
    //
    // HttpModel model = new HttpModel();
    // // JsonModel model = new JsonModel();
    // model.setSourceUrl(URL);
    // model.setMethod(HttpModel.GET);
    // model.setCacheType(HttpModel.CACHE_TYPE_NONE);
    // HttpModelParams params = new HttpModelParams();
    // if (LogUtil.isDebug()) {
    // params.addParam("longtoken", "987a103048fda1153066a69e8db37c3f");
    // } else {
    // params.addParam("longtoken", useInfo.getToken());
    // }
    // try {
    // params.addParam("nick", URLEncoder.encode(usrName, "UTF-8"));
    // } catch (UnsupportedEncodingException e) {
    // e.printStackTrace();
    // }
    // model.setRequestParams(params);
    //
    // httpClient.requestTextData(model, new HttpTextResponseHandler() {
    // public void onRequestSuccess(String response) {
    // System.out.println("************nickName succc:" + response);
    // }
    //
    // public void onRequestSuccessFromHttpCache(String response) {
    // System.out.println("************nickName onRequestSuccessFromHttpCache:" + response);
    // };
    //
    // public void onRequestStart(int reqCount) {
    // System.out.println("************nickName onRequestStart");
    // }
    //
    // public void onRequestFinish(int reqCount) {
    // System.out.println("************nickName onRequestFinish");
    // }
    //
    // public void onRequestFailure(String msg, DataHeadImpl head) {
    // System.out.println("************nickName onRequestFailure");
    // showTip("修改失败,请稍后再试");
    // }
    //
    // public void onDecodeFailure(String msg, DataHeadImpl head) {
    // System.out.println("************nickName onDecodeFailure");
    // showTip("修改失败,请稍后再试");
    // }
    // });
    // }
}
