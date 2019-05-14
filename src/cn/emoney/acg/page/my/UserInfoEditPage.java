package cn.emoney.acg.page.my;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.helper.push.BaiduPushManager_v2;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.SupportEquipment;
import cn.emoney.acg.page.motif.MineGroupModule;
import cn.emoney.acg.page.motif.MotifHome;
import cn.emoney.acg.page.my.AlbumAndCaptureDialog.LoadAlbumAndCameraCallBack;
import cn.emoney.acg.page.settings.PwdChangePage;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: UserInfoEditPage
 * @Description:用户信息编辑
 * @author xiechengfa
 * @date 2015年11月18日 下午5:34:54
 *
 */
public class UserInfoEditPage extends PageImpl {
    private final int CHANGE_NICKNAME_CODE = 10000;
    private AlbumAndCaptureDialog loadAlbumAndCameraDialog = null;

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_usrinfo_edit);

        // title
        bindPageTitleBar(R.id.titleBar);

        // 用户信息
        initUserInfoLayout();
        // 其它列表
        initListItemLayout();
    }

    @Override
    protected void onPageResume() {
        // TODO Auto-generated method stub
        super.onPageResume();
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "我的信息");
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

    // 初始化用户信息相关的内容
    private void initUserInfoLayout() {
        initHeadIcon();

        TextView nickNameView = (TextView) findViewById(R.id.nickNameView);
        if (DataModule.getInstance().getUserInfo().getNickName() != null && DataModule.getInstance().getUserInfo().getNickName().trim().length() > 0) {
            nickNameView.setText(DataModule.getInstance().getUserInfo().getNickName());
        } else {
            nickNameView.setText(DataUtils.formatUserNameShade(DataModule.getInstance().getUserInfo().getUsername()));
        }

        TextView userNameView = (TextView) findViewById(R.id.userNameView);
        if (getUserInfo().getAccountTYpe() == DataModule.LOGIN_TYPE_QQ) {
            userNameView.setText("QQ联登");
        } else {
            userNameView.setText(DataUtils.formatUserNameShade(DataModule.getInstance().getUserInfo().getUsername()));
        }
    }

    // 其它列表选项初始化
    private void initListItemLayout() {
        findViewById(R.id.headViewLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 修改头像
                getAlbumAndCaptureDialog().show();
            }
        });

        findViewById(R.id.nickNameLayout).setOnClickListener(onClickListener);
        if (getUserInfo().getAccountTYpe() == DataModule.LOGIN_TYPE_QQ) {
            findViewById(R.id.pwdLayout).setVisibility(View.GONE);
            findViewById(R.id.pwdLineView).setVisibility(View.GONE);
        } else {
            findViewById(R.id.pwdLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.pwdLineView).setVisibility(View.VISIBLE);
            findViewById(R.id.pwdLayout).setOnClickListener(onClickListener);
        }

        findViewById(R.id.logOffLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 注销
                logOff();
            }
        });
    }

    private OnClickEffectiveListener onClickListener = new OnClickEffectiveListener() {

        @Override
        public void onClickEffective(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.nickNameLayout:
                    // 修改昵称
                    PageIntent intent = new PageIntent(UserInfoEditPage.this, EditNickNamePage.class);
                    startPageForResult(intent, CHANGE_NICKNAME_CODE);
                    break;
                case R.id.pwdLayout:
                    // 修改密码
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(UserInfoEditPage.this, PwdChangePage.class));
                    break;
            }
        }
    };

    // 注销
    private void logOff() {
        DialogUtils.showMessageDialog(getActivity(), "提示", "注销之后无法与云端进行自选股同步,确认注销?", "确定", "取消", new CustomDialogListener() {
            @Override
            public void onCancelBtnClicked() {}

            @Override
            public void onConfirmBtnClicked() {
                DataModule.G_LAST_LOGIN_STATE = 0;
                getDBHelper().setInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE, DataModule.G_LAST_LOGIN_STATE);

                UserInfo info = getUserInfo();
                info.setLogined(false);
                info.setToken("");
                info.setRole(0);
                info.setRealHeadId("0");
                info.setRealName("");


                DataModule.getInstance().getOptionalInfo().loadVisitors(getDBHelper());

                // 修改更新状态
                // OptionalHome.bIsNeedRefresh = true;

                // 清除装备权限
                SupportEquipment.getInstance().clearPermission();
                // EquipmentHome.isNeedRefresh = true;

                // 清除我的买吧
                MineGroupModule.getInstance().clear();
                MotifHome.isNeedUpdateMineType = true;
                // 要确认
                // BuyClubHome.isNeedRequestCustom = true;

                BaiduPushManager_v2.setOffLine();

                // 退出登录时，清空单例中预警缓存数据
                StockAlertManagerV2.getInstance().clearCache();

                // 关闭当前页面
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        getAlbumAndCaptureDialog().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPageResult(int requestCode, int resultCode, Bundle data) {
        // TODO Auto-generated method stub
        if (resultCode != RESULT_CODE) {
            return;
        }

        if (requestCode == CHANGE_NICKNAME_CODE) {
            String nickName = data.getString(EditNickNamePage.RESULT_DATA);
            if (nickName != null && nickName.trim().length() > 0) {
                TextView nickNameView = (TextView) findViewById(R.id.nickNameView);
                nickNameView.setText(nickName);
            }
        } else {
            getAlbumAndCaptureDialog().onPageResult(requestCode, resultCode, data);
        }
    }

    private AlbumAndCaptureDialog getAlbumAndCaptureDialog() {
        if (loadAlbumAndCameraDialog == null) {
            loadAlbumAndCameraDialog = new AlbumAndCaptureDialog(this, getActivity(), new LoadAlbumAndCameraCallBack() {

                @Override
                public void imageCallBack(String url) {
                    // TODO Auto-generated method stub
                    getUserInfo().setHeadId(Util.getFileNameOfUrl(url));
                    initHeadIcon();
                }
            });
        }

        return loadAlbumAndCameraDialog;
    }

    private void initHeadIcon() {
        ImageView headIV = (ImageView) findViewById(R.id.headIV);
        Util.loadHeadIcon(headIV, getUserInfo().getUid(), getUserInfo().getHeadId());
    }
}
