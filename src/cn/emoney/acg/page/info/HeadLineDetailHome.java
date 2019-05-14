package cn.emoney.acg.page.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuImgItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.widget.LoopPageSwitcher;

public class HeadLineDetailHome extends PageImpl {
    
    public static final String EXTRA_KEY_URLS = "head_line_detail_urls";
    public static final String EXTRA_KEY_INDEX = "head_line_detail_index";
    
    private int originIndex;           // 第一个显示的页面的index
    
    private LoopPageSwitcher pageSwitcher;
    
    private List<String> listUrls = new ArrayList<String>();
    private List<String> mLstFlagMd5 = new ArrayList<String>();
    
    @Override
    protected void initPage() {
        setContentView(R.layout.page_headline_detailhome);
        
        initViews();
        
        bindPageTitleBar(R.id.titlebar);
    }
    
    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);
        
        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_INDEX)) {
                originIndex = arguments.getInt(EXTRA_KEY_INDEX);
            }

            if (arguments.containsKey(EXTRA_KEY_URLS)) {
                List<String> tListUrls = arguments.getStringArrayList(EXTRA_KEY_URLS);
                listUrls.clear();
                listUrls.addAll(tListUrls);
            }
        }
        
    }
    
    @Override
    protected void initData() {
        pageSwitcher.setPageCount(listUrls.size());
        registViewWithPage(pageSwitcher);
        pageSwitcher.setCurrentItem(originIndex);
        
        String[] aryFlagMd5s = getDBHelper().getStringArray(HeadLinePage.KEY_HEAD_LINE_READ_LIST, new String[] {});
        List<String> t_Lst = Arrays.asList(aryFlagMd5s);
        mLstFlagMd5 = new ArrayList<String>(t_Lst);
    }
    
    @Override
    protected void onPagePause() {
        super.onPagePause();
        
        int t_size = mLstFlagMd5.size();
        int size = t_size < DataModule.G_MAX_READSTATE_COUNT ? t_size : DataModule.G_MAX_READSTATE_COUNT;
        if (t_size > DataModule.G_MAX_READSTATE_COUNT) {
            mLstFlagMd5 = mLstFlagMd5.subList(0, DataModule.G_MAX_READSTATE_COUNT);
        }
        String[] newArys = (String[]) mLstFlagMd5.toArray(new String[size]);

        getDBHelper().setStringArray(HeadLinePage.KEY_HEAD_LINE_READ_LIST, newArys);
    }
    
    private void initViews() {
        pageSwitcher = (LoopPageSwitcher) findViewById(R.id.pageswitcher);
        
        if (pageSwitcher != null) {
            pageSwitcher.setSwitchable(true);
            pageSwitcher.setPreload(true);
            pageSwitcher.setPageFactory(new LoopPageSwitcher.IPageFactory() {

                @Override
                public Page createPage(int position) {
                    HeadLineDetailPage page = new HeadLineDetailPage();
                    page.setSupportAnimation(false);
                    if (listUrls != null && listUrls.size() > position) {
                        page.setUrl(listUrls.get(position));
                    }
                    return page;
                }
            });
            pageSwitcher.setOnPageSwitchListener(new LoopPageSwitcher.OnPageSwitchListener() {
                
                private int currentPageIndex;
                private int lastPageIndex = -1;

                @Override
                public void onPageSelected(int index) {
                    currentPageIndex = index;
                }

                @Override
                public void onPageScrolled(int i, float v, int i2) {
                    if (lastPageIndex != currentPageIndex && i2 == 0) {
                        String currentUrl = listUrls.get(currentPageIndex);
                        String flagMD5 = MD5Util.md5(currentUrl);
                        if (!mLstFlagMd5.contains(flagMD5)) {
                            mLstFlagMd5.add(0, flagMD5);
                        }
                        
                        lastPageIndex = currentPageIndex;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
        }
    }
    
    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        BarMenuImgItem leftItem = new BarMenuImgItem(0, R.drawable.selector_btn_close_infodetail);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem itemTitle = new BarMenuTextItem(1, "财经头条");
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

}
