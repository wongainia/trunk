package cn.emoney.acg.page.settings;

import android.view.View;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class AboutSetting extends PageImpl {
    private TextView mTvVerInfo = null;

    private final String VERINFO_TEMPLATE = "V%s  Build %s";

    @Override
    protected void initPage() {
        setContentView(R.layout.page_setting_about);

        mTvVerInfo = (TextView) findViewById(R.id.setting_about_verinfo);

        String t_verinfo = String.format(VERINFO_TEMPLATE, DataModule.G_APKVERNUMBER, DataModule.G_APKBUILDNUMBER);
        mTvVerInfo.setText(t_verinfo);

        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub

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

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "关于");
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
}
