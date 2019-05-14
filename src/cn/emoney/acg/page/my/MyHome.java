package cn.emoney.acg.page.my;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.helper.push.RedPointNoticeManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.EquipmentHome;
import cn.emoney.acg.page.settings.AboutSetting;
import cn.emoney.acg.page.settings.FeedbackPage;
import cn.emoney.acg.page.settings.SettingsHome;
import cn.emoney.acg.page.share.LoginPage;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: MyHome
 * @Description:“我”一级页面
 * @author xiechengfa
 * @date 2015年11月9日 上午10:46:43
 */
public class MyHome extends PageImpl {

    private ImageView imgAlert;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_myhome);

        imgAlert = (ImageView) findViewById(R.id.page_myhome_push_msg_img_alert);

        // title
        bindPageTitleBar(R.id.page_myhome_titlebar);

        // 初始化用户信息相关的内容(onresume里调用)
        // initUserInfoLayout();
        // 其它列表
        initListItemLayout();
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        initUserInfoLayout();

        refreshRedPoint();
    }

    @Override
    protected void initData() {}

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        BarMenuTextItem titleItem = new BarMenuTextItem(1, "我");
        titleItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(titleItem);
        return true;
    }

    // 初始化用户信息相关的内容
    private void initUserInfoLayout() {
        ImageView headIV = (ImageView) findViewById(R.id.headIV);
        TextView usrNameView = (TextView) findViewById(R.id.userNameTV);
        findViewById(R.id.myInfoLayout).setOnClickListener(listener);

        if (DataModule.getInstance().getUserInfo().isLogined()) {
            // 登录
            initHeadIcon(headIV);

            usrNameView.setText(DataModule.getInstance().getUserInfo().getConvertNickName());

            findViewById(R.id.loginTipTV).setVisibility(View.GONE);
            findViewById(R.id.myFunctionLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.askIV).setOnClickListener(listener);
            findViewById(R.id.teacherIV).setOnClickListener(listener);
            findViewById(R.id.equipmentIV).setOnClickListener(listener);
        } else {
            // 未登录
            headIV.setImageResource(R.drawable.img_head_icon_default);
            usrNameView.setText("游客");
            findViewById(R.id.loginTipTV).setVisibility(View.VISIBLE);
            findViewById(R.id.myFunctionLayout).setVisibility(View.GONE);
        }
    }

    // 其它列表选项初始化
    private void initListItemLayout() {
        findViewById(R.id.msgLayout).setOnClickListener(listener);
        findViewById(R.id.setLayout).setOnClickListener(listener);
        findViewById(R.id.problemLayout).setOnClickListener(listener);
        findViewById(R.id.recmmandLayout).setOnClickListener(listener);
        findViewById(R.id.aboutLayout).setOnClickListener(listener);
    }

    /**
     * 刷新推送消息提示（红点）是否显示
     * */
    private void refreshRedPoint() {
        // 刷新是否显示红点
        boolean isShowAlert = RedPointNoticeManager.getRedpointDisplay(getContext(), "");
        if (isShowAlert) {
            imgAlert.setImageResource(R.drawable.img_notice_point);
        } else {
            imgAlert.setImageBitmap(null);
        }
    }

    private void startEquipmentPage() {
        PageIntent intent = new PageIntent(this, EquipmentHome.class);
        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    private void initHeadIcon(ImageView headIV) {
        Util.loadHeadIcon(headIV, getUserInfo().getUid(), getUserInfo().getHeadId());
    }

    private OnClickEffectiveListener listener = new OnClickEffectiveListener() {

        @Override
        public void onClickEffective(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.myInfoLayout:
                    // 用户信息
                    if (DataModule.getInstance().getUserInfo().isLogined()) {
                        // 登录
                        startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, UserInfoEditPage.class));
                    } else {
                        // 未登录
                        startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, LoginPage.class));
                    }
                    break;
                case R.id.askIV:
                    // 我的问答
                    break;
                case R.id.teacherIV:
                    // 我的大师
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, MyTeacherPage.class));
                    break;
                case R.id.equipmentIV:
                    // 我的装备
                    startEquipmentPage();
                    break;
                case R.id.msgLayout:
                    // 系统消息
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, PushMessagePage.class));
                    break;
                case R.id.setLayout:
                    // 设置
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, SettingsHome.class));
                    break;
                case R.id.problemLayout:
                    // 问题
                    if (!DataModule.getInstance().getUserInfo().isLogined()) {
                        showTip(Util.getResourcesString(R.string.login_tip));
                        return;
                    }
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, FeedbackPage.class));
                    break;
                case R.id.recmmandLayout:
                    // 好友推荐
                    break;
                case R.id.aboutLayout:
                    // 关于
                    startPage(DataModule.G_CURRENT_FRAME, new PageIntent(MyHome.this, AboutSetting.class));
                    break;
            }
        }
    };
}
