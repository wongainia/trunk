package cn.emoney.acg.page.equipment.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import cn.emoney.acg.R;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.widget.LoopPageSwitcher;

public class ZDLHDetailHome extends PageImpl {
    public final static String EXTRA_KEY_LIST_DATA = "key_list_data";
    public final static String EXTRA_KEY_LIST_INDEX = "key_list_index";

    private List<Map<String, String>> mLstInfos = new ArrayList<Map<String, String>>();

    private LoopPageSwitcher mPageSwitcher = null;

    private int mCurrIndex = 0;

    public ZDLHDetailHome() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void initData() {
        mPageSwitcher.setPageCount(mLstInfos.size());

        registViewWithPage(mPageSwitcher);

        mPageSwitcher.setCurrentItem(mCurrIndex);
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_zdlhinfo_detailhome);

        mPageSwitcher = (LoopPageSwitcher) findViewById(R.id.page_infodetailhome_pageswitcher);
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(true);
            mPageSwitcher.setPreload(true);
            mPageSwitcher.setPageFactory(new LoopPageSwitcher.IPageFactory() {

                @Override
                public Page createPage(int position) {
                    ZDLHDetailPage page = new ZDLHDetailPage();
                    page.setSupportAnimation(false);
                    page.setData(mLstInfos.get(position));
                    return page;
                }
            });
            mPageSwitcher.setOnPageSwitchListener(new LoopPageSwitcher.OnPageSwitchListener() {

                @Override
                public void onPageSelected(int index) {

                }

                @Override
                public void onPageScrolled(int i, float v, int i2) {}

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        bindPageTitleBar(R.id.zdlhInfoDetailTitlebar);
    }

    protected void receiveData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (bundle.containsKey(EXTRA_KEY_LIST_INDEX)) {
            mCurrIndex = bundle.getInt(EXTRA_KEY_LIST_INDEX);
        }

        if (ZDLHPage.mLstZDLHData != null) {
            mLstInfos = ZDLHPage.mLstZDLHData;
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "重大利好");
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPageChangeFlag == 0) {
                mPageChangeFlag = -1;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
