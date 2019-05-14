package cn.emoney.acg.page.motif;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
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

import com.alibaba.fastjson.JSONObject;

/**
 * 平仓
 * */
public class CloseMotifPositionPage extends PageImpl {

    public static final String EXTRA_KEY_GROUP_ID = "extra_group_id";
    public static final String EXTRA_KEY_GOODS_NAME = "extra_goods_name";
    public static final String EXTRA_KEY_GOODS_CODE = "extra_goods_code";
    public static final String EXTRA_KEY_GOODS_PRICE = "extra_goods_price";
    public static final String EXTRA_KEY_GOODS_ZDF = "extra_goods_zdf";
    public static final String EXTRA_KEY_CURRENT_POSITION = "extra_current_position";

    private int groupId, minPosition = -1, currentPosition;
    private String goodsCode;

    private View layoutLoading, layoutScrollView, rightView;
    private TextView tvGoodsName, tvGoodsCode, tvGoodsPrice, tvGoodsZdf, tvCurrentPosition;
    private TextView tvNotice;
    private EditText etPrice, etReason;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_close_motifposition);

        initViews();

        bindPageTitleBar(R.id.page_close_motifposition_titlebar);
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);

        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_GROUP_ID)) {
                groupId = arguments.getInt(EXTRA_KEY_GROUP_ID, 0);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_NAME)) {
                String goodsName = arguments.getString(EXTRA_KEY_GOODS_NAME);
                tvGoodsName.setText(goodsName);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_CODE)) {
                goodsCode = arguments.getString(EXTRA_KEY_GOODS_CODE);
                tvGoodsCode.setText(goodsCode);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_PRICE)) {
                String goodsPrice = arguments.getString(EXTRA_KEY_GOODS_PRICE);
                tvGoodsPrice.setText(goodsPrice);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_ZDF)) {
                String goodsZdf = arguments.getString(EXTRA_KEY_GOODS_ZDF);
                tvGoodsZdf.setText(goodsZdf);
            }
            if (arguments.containsKey(EXTRA_KEY_CURRENT_POSITION)) {
                String position = arguments.getString(EXTRA_KEY_CURRENT_POSITION);
                tvCurrentPosition.setText(position);

                currentPosition = convertPercentToInt(position);
            }

            requestAvailablePosition(goodsCode);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 如果5秒钟后仍未收到返回数据，隐藏加载牛
                    if (layoutLoading != null) {
                        layoutLoading.setVisibility(View.INVISIBLE);
                    }
                }
            }, 5000);
        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPagePause() {
        InputMethodUtil.closeSoftKeyBoard(this);

        super.onPagePause();
    }

    private void initViews() {
        tvGoodsName = (TextView) findViewById(R.id.page_close_motifposition_tv_goods_name);
        tvGoodsCode = (TextView) findViewById(R.id.page_close_motifposition_tv_goods_code);
        tvGoodsPrice = (TextView) findViewById(R.id.page_close_motifposition_tv_goods_price);
        tvGoodsZdf = (TextView) findViewById(R.id.page_close_motifposition_tv_goods_zdf);
        tvCurrentPosition = (TextView) findViewById(R.id.page_close_motifposition_tv_current_position);
        etPrice = (EditText) findViewById(R.id.page_close_motifposition_et_price);
        etReason = (EditText) findViewById(R.id.page_close_motifposition_et_reason);
        layoutScrollView = findViewById(R.id.page_close_motifposition_scroll);
        layoutLoading = findViewById(R.id.page_close_motifposition_layout_loading);
        tvNotice = (TextView) findViewById(R.id.page_close_motifposition_tv_notice);

        layoutScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodUtil.closeSoftKeyBoard(CloseMotifPositionPage.this);
                return false;
            }
        });
    }

    /**
     * 获取指定股票的可操作仓位
     * */
    private void requestAvailablePosition(String goodsCode) {
        if (groupId == 0 || TextUtils.isEmpty(goodsCode)) {
            return;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();
        String groupCode = String.valueOf(groupId);

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_AVAILABLE_POSITION, token, groupCode, goodsCode);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_AVAILABLE_POSITION);
    }

    /**
     * 买吧股票请求平仓
     * */
    private void requestCloseMotifPosition(int srcPos, String remark, float price) {
        if (groupId == 0 || TextUtils.isEmpty(goodsCode)) {
            return;
        }

        if (minPosition > 0) {
            showTip("不能平仓");
            return;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_RESET_POSITION, token, String.valueOf(groupId), goodsCode, String.valueOf(srcPos), "0", remark, String.valueOf(price));
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_RESET_POSITION);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == BuyClubHttpUrl.FLAG_GROUP_RESET_POSITION) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();

            try {
                JSONObject objResult = JSONObject.parseObject(msgData);

                if (objResult != null && objResult.containsKey("errorCode") && objResult.containsKey("body")) {
                    int errorCode = objResult.getIntValue("errorCode");
                    if (errorCode == 0) {
                        String body = objResult.getString("body");
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode")) {
                            int retCode = objBody.getIntValue("retcode");
                            if (retCode == 1) {
                                // 添加股票到组合成功，关闭当前界面
                                showTip("提交成功");
                                CloseMotifPositionPage.this.finish();
                                return;
                            }
                        }

                    }
                }

            } catch (Exception e) {
            }

            showTip("操作失败");
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_AVAILABLE_POSITION) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();
            // {"body":"{\"retcode\":1,\"retmsg\":null,\"message\":{\"min\":6,\"max\":66,\"cur\":7}}","errorCode":0}

            try {
                JSONObject objReturn = JSONObject.parseObject(msgData);

                if (objReturn != null && objReturn.containsKey("body") && objReturn.containsKey("errorCode")) {
                    int errorCode = objReturn.getIntValue("errorCode");
                    String body = objReturn.getString("body");

                    if (errorCode == 0) {
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode") && objBody.containsKey("message")) {
                            int retCode = objBody.getIntValue("retcode");
                            String message = objBody.getString("message");

                            if (retCode == 1) {
                                JSONObject objMsg = JSONObject.parseObject(message);

                                if (objMsg != null && objMsg.containsKey("min")) {
                                    minPosition = objMsg.getIntValue("min");
                                    currentPosition = objMsg.getIntValue("cur");
                                }

                                if (minPosition == 0) {

                                    if (currentPosition == 0) {
                                        // 已平仓，不可再平仓
                                        if (layoutLoading != null && tvNotice != null) {
                                            layoutLoading.setVisibility(View.INVISIBLE);
                                            tvNotice.setText("当前仓位为0，无需平仓");
                                        }
                                    } else {
                                        // 未平仓，可以平仓
                                        if (layoutScrollView != null) {
                                            layoutScrollView.setVisibility(View.VISIBLE);
                                            rightView.setVisibility(View.VISIBLE);

                                            // 自动打开软键盘
                                            etReason.setFocusable(true);
                                            etReason.setFocusableInTouchMode(true);
                                            etReason.requestFocus();

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    InputMethodManager imm = (InputMethodManager) etReason.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.showSoftInput(etReason, 0);
                                                }
                                            }, 300);
                                        }
                                    }

                                } else if (minPosition > 0) {
                                    // 不可平仓
                                    if (layoutLoading != null && tvNotice != null) {
                                        layoutLoading.setVisibility(View.INVISIBLE);
                                        tvNotice.setText("该股票部分数量不可卖，请隔日再试");
                                    }
                                } else {
                                    if (layoutLoading != null && tvNotice != null) {
                                        layoutLoading.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "股票平仓");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        rightView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_txt, null);
        rightView.setVisibility(View.INVISIBLE);
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
            // 调仓
            String remark = etReason.getText().toString().trim();
            try {
                remark = Base64.encodeToString(remark.getBytes("utf-8"), Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            float price = 0f;
            try {
                price = Float.parseFloat(etPrice.getText().toString().trim());
            } catch (Exception e) {
            }
            requestCloseMotifPosition(currentPosition, remark, price);
        }
    }

    /**
     * 将百分数转换为小数
     * */
    private int convertPercentToInt(String value) {
        int tValue = 0;

        try {
            String str = value;
            if (!TextUtils.isEmpty(value) && value.endsWith("%")) {
                str = value.replaceAll("\\%s+", "");
                str = str.substring(0, str.length() - 1);
            }
            tValue = Integer.parseInt(str);
        } catch (Exception e) {
        }

        return tValue;
    }

}
