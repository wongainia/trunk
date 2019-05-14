package cn.emoney.acg.page.share.infodetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.KeyEvent;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuImgItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.LoopPageSwitcher;

public class InfoDetailHome extends PageImpl {

    public static void gotoInfoDetail(Page page, ArrayList<Map<String, String>> listItem, int index, String infoType) {
        PageIntent intent = new PageIntent(page, InfoDetailHome.class);
        // // intent.setFlags(PageIntent.FLAG_PAGE_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable(InfoDetailHome.EXTRA_KEY_LIST_ITEMS, listItem);
        bundle.putInt(InfoDetailHome.EXTRA_KEY_LIST_INDEX, index);
        bundle.putString(InfoDetailHome.EXTRA_KEY_INFO_TYPE, infoType);

        intent.setArguments(bundle);

        intent.setSupportAnimation(true);

        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    public final static String EXTRA_KEY_INFO_TYPE = "key_info_type";
    public final static String EXTRA_KEY_LIST_ITEMS = "key_list_items";
    public final static String EXTRA_KEY_LIST_INDEX = "key_list_index";

    private List<Map<String, String>> mLstInfos = new ArrayList<Map<String, String>>();

    private LoopPageSwitcher pageSwitcher;
    BarMenuTextItem itemTitle;

    private int mCurrPageIndex = 0;
    private int mLastPageIndex = -1;
    private int mCurrIndex = 0;
    private Map<String, String> mCurrInfoMap = null;

    private String mInfoType = null;

    List<String> mLstFlagMd5 = null;

    public InfoDetailHome() {}

    @Override
    protected void initPage() {
        setContentView(R.layout.page_infodetailhome);

        pageSwitcher = (LoopPageSwitcher) findViewById(R.id.page_infodetailhome_pageswitcher);
        if (pageSwitcher != null) {
            pageSwitcher.setSwitchable(true);
            pageSwitcher.setPreload(true);
            pageSwitcher.setPageFactory(new LoopPageSwitcher.IPageFactory() {

                @Override
                public Page createPage(int position) {
                    InfoDetailPage page = new InfoDetailPage();
                    page.setSupportAnimation(false);
                    page.setData(mLstInfos.get(position));
                    return page;
                }
            });
            pageSwitcher.setOnPageSwitchListener(new LoopPageSwitcher.OnPageSwitchListener() {

                @Override
                public void onPageSelected(int index) {
                    mCurrPageIndex = index;
                }

                @Override
                public void onPageScrolled(int i, float v, int i2) {
                    if (mLastPageIndex != mCurrPageIndex && i2 == 0) {
                        mCurrInfoMap = mLstInfos.get(mCurrPageIndex);
                        resetTitle();
                        mLastPageIndex = mCurrPageIndex;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

        }

        bindPageTitleBar(R.id.infoDetailTitlebar);
    }

    @Override
    protected void onPagePause() {
        int t_size = mLstFlagMd5.size();
        int size = t_size < DataModule.G_MAX_READSTATE_COUNT ? t_size : DataModule.G_MAX_READSTATE_COUNT;
        if (t_size > DataModule.G_MAX_READSTATE_COUNT) {
            mLstFlagMd5 = mLstFlagMd5.subList(0, DataModule.G_MAX_READSTATE_COUNT);
        }
        String[] newArys = (String[]) mLstFlagMd5.toArray(new String[size]);

        getDBHelper().setStringArray(mInfoType, newArys);
        super.onPagePause();
    }

    protected void receiveData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (bundle.containsKey(EXTRA_KEY_LIST_INDEX)) {
            mCurrIndex = bundle.getInt(EXTRA_KEY_LIST_INDEX);
        }

        if (bundle.containsKey(EXTRA_KEY_LIST_ITEMS)) {
            ArrayList<Map<String, String>> listMap = (ArrayList<Map<String, String>>) bundle.getSerializable(EXTRA_KEY_LIST_ITEMS);
            mLstInfos.clear();
            mLstInfos.addAll(listMap);

            if (mLstInfos.size() > mCurrIndex && mCurrIndex >= 0) {
                mCurrInfoMap = mLstInfos.get(mCurrIndex);
            }
        }

        if (bundle.containsKey(EXTRA_KEY_INFO_TYPE)) {
            mInfoType = bundle.getString(EXTRA_KEY_INFO_TYPE);

            String[] aryFlagMd5s = getDBHelper().getStringArray(mInfoType, new String[] {});
            List<String> t_Lst = Arrays.asList(aryFlagMd5s);
            mLstFlagMd5 = new ArrayList<String>(t_Lst);
        }

    }

    @Override
    protected void initData() {
        pageSwitcher.setPageCount(mLstInfos.size());

        registViewWithPage(pageSwitcher);

        pageSwitcher.setCurrentItem(mCurrIndex);
    }

    private void resetTitle() {
        String cls = mCurrInfoMap.get(InfoDetailPage.EXTRA_KEY_SORTCLS);
        String url = mCurrInfoMap.get(InfoDetailPage.EXTRA_KEY_CONTENT_URL);

        
        if (itemTitle != null) {
            itemTitle.getItemView().setText(cls);
        }

        String sFlag = url;
        String flagMD5 = MD5Util.md5(sFlag);

        if (!mLstFlagMd5.contains(flagMD5)) {
            mLstFlagMd5.add(0, flagMD5);
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        BarMenuImgItem leftItem = new BarMenuImgItem(0, R.drawable.selector_btn_close_infodetail);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        itemTitle = new BarMenuTextItem(1, mCurrInfoMap.get(InfoDetailPage.EXTRA_KEY_SORTCLS));
        itemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(itemTitle);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

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
            InputMethodUtil.closeSoftKeyBoard(this);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public int popExitAnimation() {
        return 0;
    }

}
