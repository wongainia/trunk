package cn.emoney.acg.page.settings;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import cn.emoney.acg.util.DeviceInfoUtil;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: FeedbackPage2
 * @Description:反馈
 * @author xiechengfa
 * @date 2015年11月23日 下午4:54:25
 */
public class FeedbackPage extends PageImpl {
    // xietest
    private final String URL = "http://client.i.emoney.cn/account/submitfb";// "http://192.168.3.51/clienti/account/submitfb";
    private EditText editText = null;

    @Override
    protected void receiveData(Bundle arguments) {}

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_feedback);

        editText = (EditText) findViewById(R.id.feedbackView);
        InputMethodUtil.setImeOptions(editText, EditorInfo.IME_ACTION_NONE);
        // titleBar
        bindPageTitleBar(R.id.feedbackTitleBar);

        // 设置光标
        editText.requestFocus();
        editText.setSelection(editText.getEditableText().toString().length());

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

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "用户反馈");
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

    // 是否有内容
    private boolean checkIsEdited() {
        String content = editText.getText().toString();
        if (content != null && content.trim().length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void showEditedDialog() {
        DialogUtils.showMessageDialog(getActivity(), "提示", "是否放弃对反馈的提交?", "放弃", "继续编辑", new CustomDialogListener() {

            @Override
            public void onConfirmBtnClicked() {
                // TODO Auto-generated method stub
                finishPage();
            }

            @Override
            public void onCancelBtnClicked() {}
        });
    }

    private void finishPage() {
        mPageChangeFlag = -1;
        finish();
    }

    private void onClickSubmit() {
        String content = editText.getText().toString();
        if (content == null || content.equals("")) {
            showTip("内容不能为空");
            return;
        }

        // if (content.trim().length() >
        // Util.getResourcesInteger(R.integer.feedback_conten_max_len)) {
        // showTip("内容最长200个字符");
        // return;
        // }

        InputMethodUtil.closeSoftKeyBoard(this);
        DialogUtils.showProgressDialog(getActivity(), null);
        submitContent(content.trim());
    }

    private void submitContent(String content) {
        try {
            JSONObject jsObj = new JSONObject();
            jsObj.put(KeysInterface.KEY_URL, URL);
            jsObj.put("token", getUserInfo().getToken());
            jsObj.put("post", "txtSuggest=" + content + "&platform=" + DeviceInfoUtil.getPlatForm() + ".v" + DeviceInfoUtil.getVersionChars() + "&token=" + getUserInfo().getToken());
            requestInfo(jsObj, IDUtils.ID_GROUP_HTTP_INTERFACE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            showTip("提交失败,请稍后再试");
        }

        // HttpClient client = new HttpClient(getContext());
        // HttpModel model = new HttpModel(URL);
        // HttpModelParams params = new HttpModelParams();
        // try {
        // params.addParam("txtSuggest", Base64.encodeToString(content.getBytes("utf-8"),
        // Base64.NO_WRAP));
        // } catch (UnsupportedEncodingException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // params.addParam("platform", DeviceInfoUtil.getPlatForm() + ".v" +
        // DeviceInfoUtil.getVersionChars());
        // params.addParam("token", getUserInfo().getToken());
        // model.setRequestParams(params);
        // model.setCharSet("UTF-8");
        // model.setMethod(HttpModel.POST);
        //
        // client.requestTextData(model, new HttpTextResponseHandler() {
        // public void onRequestStart(int reqCount) {
        // System.out.println("************onRequestStart");
        // }
        //
        // public void onRequestFinish(int reqCount) {
        // System.out.println("************onRequestFinish");
        // DialogUtils.closeProgressDialog();
        // }
        //
        // public void onRequestSuccess(String response) {
        // System.out.println("************onRequestSuccess:" + response);
        // if (response != null && response.trim().length() > 0) {
        // JSONObject jsObj = JSON.parseObject(response);
        // if (jsObj.getIntValue("retcode") == 1) {
        // // 成功
        // // showTip("提交成功");
        // DialogUtils.showMessageDialogOfDefaultSingleBtnCallBack(getActivity(), null, "提交成功", new
        // CustomDialogListener() {
        //
        // @Override
        // public void onConfirmBtnClicked() {
        // // TODO Auto-generated method stub
        // finish();
        // }
        //
        // @Override
        // public void onCancelBtnClicked() {
        // // TODO Auto-generated method stub
        // }
        // });
        // } else {
        // String msg = new String(Base64.decode(jsObj.getString("retmsg"), Base64.NO_WRAP));;
        // if (msg != null && msg.trim().length() > 0) {
        // showTip(msg);
        // } else {
        // showTip("提交失败");
        // }
        // }
        // } else {
        // showTip("提交失败");
        // }
        // }
        //
        // public void onRequestFailure(String msg, DataHeadImpl head) {
        // System.out.println("************onRequestFailure");
        // showTip("提交失败");
        // }
        //
        // public void onDecodeFailure(String msg, DataHeadImpl head) {
        // System.out.println("************onDecodeFailure");
        // showTip("提交失败");
        // }
        // });
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        DialogUtils.closeProgressDialog();
        if (pkg instanceof GlobalMessagePackage) {
            GlobalMessagePackage goodsTable = (GlobalMessagePackage) pkg;
            MessageCommon gr = goodsTable.getResponse();
            if (gr == null || gr.getMsgData() == null) {
                showTip("提交失败,请稍后再试");
                return;
            }

            String msgData = gr.getMsgData();
            LogUtil.easylog("sky", "FeedBackPage->updateFromInfo: " + msgData);
            if (msgData == null || msgData.equals("") || msgData.equals("{}")) {
                showTip("提交失败,请稍后再试");
                return;
            }

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retServerCode = jsObj.getIntValue("errorCode");
                String body = jsObj.getString("body");
                if (retServerCode == 0 && body != null && !body.equals("")) {
                    JSONObject resObjObj = JSON.parseObject(body);
                    if (resObjObj.getIntValue("retcode") == 1) {
                        // showTip("提交成功");
                        // finish();
                        DialogUtils.showMessageDialogOfDefaultSingleBtnCallBack(getActivity(), null, "提交成功", new CustomDialogListener() {

                            @Override
                            public void onConfirmBtnClicked() {
                                // TODO Auto-generated method stub
                                finish();
                            }

                            @Override
                            public void onCancelBtnClicked() {
                                // TODO Auto-generated method stub
                            }
                        });
                    } else {
                        String retMsg = resObjObj.getString("retmsg");
                        if (retMsg != null && retMsg.trim().length() > 0) {
                            showTip(retMsg);
                        } else {
                            showTip("提交失败,请稍后再试");
                        }
                    }
                } else {
                    showTip("提交失败,请稍后再试");
                }
            } catch (Exception e) {
                showTip("提交失败,请稍后再试");
            }
        } else {
            showTip("提交失败,请稍后再试");
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
        showTip("提交失败,请稍后再试");
    }


    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }
}
