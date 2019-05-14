package cn.emoney.acg.page.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.helper.push.BaiduPushManager_v2;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

/**
 * 推送设置 （推送总开关、系统消息、个股预警、买吧调仓）
 * */
public class PushSettingPage extends PageImpl implements View.OnClickListener {

    private ImageView cbPush, cbSysInfo, cbStockAlert, cbGroup;
    private RelativeLayout layoutSysInfo, layoutStockAlert, layoutGroup;
    private List<View> listLines = new ArrayList<View>();
    private List<TextView> listLabels = new ArrayList<TextView>();

    @Override
    protected void initData() {}

    @Override
    protected void initPage() {
        setContentView(R.layout.page_setting_push);
        initViews();
        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 更新各个CheckBox的选中状态
        updateCheckBoxStatus();
    }


    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "推送设置");
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

    private void initViews() {
        cbPush = (ImageView) findViewById(R.id.page_set_push_cb_total);
        cbSysInfo = (ImageView) findViewById(R.id.page_set_push_cb_sysinfo);
        cbStockAlert = (ImageView) findViewById(R.id.page_set_push_cb_stockalert);
        cbGroup = (ImageView) findViewById(R.id.page_set_push_cb_group);

        layoutSysInfo = (RelativeLayout) findViewById(R.id.page_set_push_layout_sysinfo);
        layoutStockAlert = (RelativeLayout) findViewById(R.id.page_set_push_layout_stockalert);
        layoutGroup = (RelativeLayout) findViewById(R.id.page_set_push_layout_group);

        listLines = new ArrayList<View>();
        listLines.add(findViewById(R.id.page_set_push_line_total_top));
        listLines.add(findViewById(R.id.page_set_push_line_total_bottom));
        listLines.add(findViewById(R.id.page_set_push_line_sysinfo_top));
        listLines.add(findViewById(R.id.page_set_push_line_sysinfo_bottom));
        listLines.add(findViewById(R.id.page_set_push_line_stockalert_bottom));
        listLines.add(findViewById(R.id.page_set_push_line_group_bottom));

        listLabels = new ArrayList<TextView>();
        listLabels.add((TextView) findViewById(R.id.page_set_push_tv_push));
        listLabels.add((TextView) findViewById(R.id.page_set_push_tv_sysinfo));
        listLabels.add((TextView) findViewById(R.id.page_set_push_tv_stockalert));
        listLabels.add((TextView) findViewById(R.id.page_set_push_tv_group));

        // cbPush.setOnCheckedChangeListener(this);
        // cbSysInfo.setOnCheckedChangeListener(this);
        // cbStockAlert.setOnCheckedChangeListener(this);
        // cbGroup.setOnCheckedChangeListener(this);

        cbPush.setOnClickListener(this);
        cbSysInfo.setOnClickListener(this);
        cbStockAlert.setOnClickListener(this);
        cbGroup.setOnClickListener(this);

    }

    // @Override
    // public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    // int tagValue = isChecked ? 1 : 0;
    // switch (buttonView.getId()) {
    // case R.id.page_set_push_cb_total: {
    // getDBHelper().setInt(BaiduPushManager_v2.KEY_PRE_LOCDATA +
    // BaiduPushManager_v2.KEY_MAIN_SWITCH, tagValue);
    //
    //
    // Map<String, Integer> tTagMap = new HashMap<String, Integer>(3);
    // if (tagValue == 0) {
    // tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, 0);
    // tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, 0);
    // tTagMap.put(BaiduPushManager_v2.KEY_GROUP, 0);
    // } else {
    // tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO,
    // BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_SYSTEM_INFO));
    // int tValue = isLogined() ?
    // BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_STOCK_ALERT) : 0;
    // tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, tValue);
    // tValue = isLogined() ? BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_GROUP) :
    // 0;
    // tTagMap.put(BaiduPushManager_v2.KEY_GROUP, tValue);
    // }
    // BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_MAIN_SWITCH, tagValue);
    // BaiduPushManager_v2.updateTags(tTagMap);
    //
    // if (isChecked) {
    // displayShowAnimation(200);
    // } else {
    // displayHideAnimation(200);
    // }
    // }
    // break;
    // case R.id.page_set_push_cb_sysinfo: {
    // BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_SYSTEM_INFO, tagValue);
    // Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
    // tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, tagValue);
    // BaiduPushManager_v2.updateTags(tTagMap);
    // }
    // break;
    // case R.id.page_set_push_cb_stockalert: {
    // BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_STOCK_ALERT, tagValue);
    // Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
    // tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, tagValue);
    // BaiduPushManager_v2.updateTags(tTagMap);
    // }
    // break;
    // case R.id.page_set_push_cb_group: {
    // BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_GROUP, tagValue);
    // Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
    // tTagMap.put(BaiduPushManager_v2.KEY_GROUP, tagValue);
    // BaiduPushManager_v2.updateTags(tTagMap);
    // }
    //
    // break;
    // default:
    // break;
    // }
    // }


    @Override
    public void onClick(View v) {
        if (v != null) {
            boolean isSelected = v.isSelected();
            LogUtil.easylog("push isSelected 1:" + isSelected);
            isSelected = !isSelected;
            LogUtil.easylog("push isSelected 2:" + isSelected);
            v.setSelected(isSelected);

            int tagValue = isSelected ? BaiduPushManager_v2.VALUE_SWITCH_ON : BaiduPushManager_v2.VALUE_SWITCH_OFF;
            LogUtil.easylog("push tagValue 3:" + tagValue);
            switch (v.getId()) {
                case R.id.page_set_push_cb_total: {

                    Map<String, Integer> tTagMap = new HashMap<String, Integer>(3);
                    if (tagValue == 0) {
                        tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, BaiduPushManager_v2.VALUE_SWITCH_OFF);
                        tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, BaiduPushManager_v2.VALUE_SWITCH_OFF);
                        tTagMap.put(BaiduPushManager_v2.KEY_GROUP, BaiduPushManager_v2.VALUE_SWITCH_OFF);
                    } else {
                        tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_SYSTEM_INFO));
                        int tValue = isLogined() ? BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_STOCK_ALERT) : BaiduPushManager_v2.VALUE_SWITCH_OFF;
                        tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, tValue);
                        tValue = isLogined() ? BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_GROUP) : BaiduPushManager_v2.VALUE_SWITCH_OFF;
                        tTagMap.put(BaiduPushManager_v2.KEY_GROUP, tValue);
                    }
                    LogUtil.easylog("push saveLocSwitcher 4:" + tagValue);
                    BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_MAIN_SWITCH, tagValue);
                    BaiduPushManager_v2.updateTags(tTagMap);

                    if (tagValue == BaiduPushManager_v2.VALUE_SWITCH_ON) {
                        displayShowAnimation(200);
                    } else {
                        displayHideAnimation(200);
                    }
                }
                    break;
                case R.id.page_set_push_cb_sysinfo: {
                    BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_SYSTEM_INFO, tagValue);
                    Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
                    LogUtil.easylog("push saveLocSwitcher 5:" + tagValue);
                    tTagMap.put(BaiduPushManager_v2.KEY_SYSTEM_INFO, tagValue);
                    BaiduPushManager_v2.updateTags(tTagMap);
                }
                    break;
                case R.id.page_set_push_cb_stockalert: {
                    BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_STOCK_ALERT, tagValue);
                    Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
                    tTagMap.put(BaiduPushManager_v2.KEY_STOCK_ALERT, tagValue);
                    BaiduPushManager_v2.updateTags(tTagMap);
                }
                    break;
                case R.id.page_set_push_cb_group: {
                    BaiduPushManager_v2.saveLocSwitcher(BaiduPushManager_v2.KEY_GROUP, tagValue);
                    Map<String, Integer> tTagMap = new HashMap<String, Integer>(1);
                    tTagMap.put(BaiduPushManager_v2.KEY_GROUP, tagValue);
                    BaiduPushManager_v2.updateTags(tTagMap);
                }

                    break;
                default:
                    break;
            }
        }

    }


    /**
     * 更新各个checkBox的选中状态
     * */
    private void updateCheckBoxStatus() {
        int enablePush = BaiduPushManager_v2.getLocSwitchState(BaiduPushManager_v2.KEY_MAIN_SWITCH);
        LogUtil.easylog("push updateCheckBoxStatus->enablePush:" + enablePush);
        cbPush.setSelected(enablePush == BaiduPushManager_v2.VALUE_SWITCH_ON ? true : false);
        cbSysInfo.setSelected(BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_SYSTEM_INFO) == BaiduPushManager_v2.VALUE_SWITCH_ON ? true : false);
        LogUtil.easylog("push updateCheckBoxStatus->sysPush:" + BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_SYSTEM_INFO));
        cbStockAlert.setSelected(BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_STOCK_ALERT) == BaiduPushManager_v2.VALUE_SWITCH_ON ? true : false);
        LogUtil.easylog("push updateCheckBoxStatus->stockAlertPush:" + BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_STOCK_ALERT));
        cbGroup.setSelected(BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_GROUP) == BaiduPushManager_v2.VALUE_SWITCH_ON ? true : false);
        LogUtil.easylog("push updateCheckBoxStatus->groupPush:" + BaiduPushManager_v2.getLocSwitchStateSelf(BaiduPushManager_v2.KEY_GROUP));

        if (enablePush == BaiduPushManager_v2.VALUE_SWITCH_ON) {
            // 如果总开关打开，就显示下方3个分开关
            displayShowAnimation(0);
        } else {
            // 如果总开关关闭，就不显示下方3个分开关
            displayHideAnimation(0);
        }
    }

    /**
     * 展示系统消息、个股预警、买吧调仓的隐藏动画
     * */
    private void displayHideAnimation(int duration) {
        // 系统消息透明度动画
        ObjectAnimator animatorSysInfoAlpha = ObjectAnimator.ofFloat(layoutSysInfo, "alpha", 1.0f, 0.9f, 0.7f, 0.4f, 0.0f);

        // 个股预警透明度动画
        ObjectAnimator animatorAlertAlpha = ObjectAnimator.ofFloat(layoutStockAlert, "alpha", 1.0f, 0.9f, 0.7f, 0.4f, 0.0f);
        // 个股预警平移动画
        int alertTranslateHeight = getTranslateHeight(1);
        ObjectAnimator animatorAlertTranslate = ObjectAnimator.ofFloat(layoutStockAlert, "translationY", 0.0f, -alertTranslateHeight);

        // 买吧调仓透明度动画
        ObjectAnimator animatorGroupAlpha = ObjectAnimator.ofFloat(layoutGroup, "alpha", 1.0f, 0.9f, 0.7f, 0.4f, 0.0f);
        // 买吧调仓平移动画
        int groupTranslateHeight = getTranslateHeight(2);
        ObjectAnimator animatorGroupTranslate = ObjectAnimator.ofFloat(layoutGroup, "translationY", 0.0f, -groupTranslateHeight);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.playTogether(animatorSysInfoAlpha, animatorAlertAlpha, animatorAlertTranslate, animatorGroupAlpha, animatorGroupTranslate);
        animatorSet.start();
    }

    /**
     * 展示系统消息、个股预警、买吧调仓的显示动画
     * */
    private void displayShowAnimation(int duration) {
        // 系统消息透明度动画
        ObjectAnimator animatorSysInfoAlpha = ObjectAnimator.ofFloat(layoutSysInfo, "alpha", 0.0f, 0.4f, 0.7f, 0.9f, 1.0f);

        // 个股预警透明度动画
        ObjectAnimator animatorAlertAlpha = ObjectAnimator.ofFloat(layoutStockAlert, "alpha", 0.0f, 0.4f, 0.7f, 0.9f, 1.0f);
        // 个股预警平移动画
        int alertTranslateHeight = getTranslateHeight(1);
        ObjectAnimator animatorAlertTranslate = ObjectAnimator.ofFloat(layoutStockAlert, "translationY", -alertTranslateHeight, 0.0f);

        // 买吧调仓透明度动画
        ObjectAnimator animatorGroupAlpha = ObjectAnimator.ofFloat(layoutGroup, "alpha", 0.0f, 0.4f, 0.7f, 0.9f, 1.0f);
        // 买吧调仓平移动画
        int groupTranslateHeight = getTranslateHeight(2);
        ObjectAnimator animatorGroupTranslate = ObjectAnimator.ofFloat(layoutGroup, "translationY", -groupTranslateHeight, 0.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.playTogether(animatorSysInfoAlpha, animatorAlertAlpha, animatorAlertTranslate, animatorGroupAlpha, animatorGroupTranslate);
        animatorSet.start();
    }

    /**
     * 获取单行高度
     **/
    private int getTranslateHeight(int lines) {
        int singleLineHeight = layoutGroup.getHeight();

        return lines * singleLineHeight;
    }

}
