package cn.emoney.acg.page.motif;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class ModifyMotifIdeaPage extends PageImpl {

    public static final String EXTRA_KEY_GROUP_ID = "extra_group_id";
    public static final String EXTRA_KEY_GROUP_IDEA = "extra_goods_idea";

    private int groupId;

    private EditText etContent;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_modify_motif_idea);

        initViews();

        bindPageTitleBar(R.id.page_modify_motifidea_titlebar);
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);

        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_GROUP_ID)) {
                groupId = arguments.getInt(EXTRA_KEY_GROUP_ID, 0);
            }
            if (arguments.containsKey(EXTRA_KEY_GROUP_IDEA)) {
                String idea = arguments.getString(EXTRA_KEY_GROUP_IDEA);
                etContent.setText(idea);
            }
        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 自动打开软键盘
        etContent.setFocusable(true);
        etContent.setFocusableInTouchMode(true);
        etContent.requestFocus();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) etContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etContent, 0);
            }
        }, 300);
    }

    @Override
    protected void onPagePause() {
        InputMethodUtil.closeSoftKeyBoard(this);

        super.onPagePause();
    }

    private void initViews() {
        View layout = findViewById(R.id.page_modify_view);
        etContent = (EditText) findViewById(R.id.page_modify_motifidea_et_content);

        layout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodUtil.closeSoftKeyBoard(ModifyMotifIdeaPage.this);

                return false;
            }
        });
    }

    private void requestModifyMotifIdea(String idea) {
        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_EDIT_IDEA, token, String.valueOf(groupId), idea);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_EDIT_IDEA);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == BuyClubHttpUrl.FLAG_GROUP_EDIT_IDEA) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();
            // {"body":"{\"retcode\":1,\"retmsg\":\"111鍟﹀暒銆俓\"}","errorCode":0}

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                if (jsObj != null && jsObj.containsKey("errorCode") && jsObj.containsKey("body")) {
                    int errorCode = jsObj.getIntValue("errorCode");

                    if (errorCode == 0) {
                        String body = jsObj.getString("body");
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode") && objBody.containsKey("retmsg")) {
                            int retCode = objBody.getIntValue("retcode");

                            if (retCode == 1) {
                                // 修改组合理念成功
                                Bundle extras = new Bundle();
                                extras.putString("idea", etContent.getText().toString().trim());
                                setResult(RESULT_CODE, extras);
                                showTip("修改成功");
                                finish();
                                return;
                            }
                        }
                    }
                }

            } catch (Exception e) {}

            etContent.setEnabled(true);
            showTip("操作失败");
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_txt, null);
        TextView tvLeftTitle = (TextView) leftView.findViewById(R.id.tv_titlebar_text);
        tvLeftTitle.setText("取消");

        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "修改理念");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        View rightView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_txt, null);
        TextView tvTitle = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        tvTitle.setText("提交");

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        super.onPageTitleBarMenuItemSelected(menuitem);

        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            // 修改理念，提交
            etContent.setEnabled(false);
            String idea = etContent.getText().toString().trim();
            requestModifyMotifIdea(idea);
        }
    }

}
