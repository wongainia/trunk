package cn.emoney.acg.page.info;

import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.widget.PageSwitcher;

/**
 * 资讯首页
 * @author liulei0905
 * */
public class InfoHome extends PageImpl implements OnPlayStatusChanged {

    public static final int PLAY_STATUS_INIT = 0;
    public static final int PLAY_STATUS_PLAY = 1;
    public static final int PLAY_STATUS_PAUSE = 2;

    private int currentPageIndex;

    private HeadLinePage headLinePage;
    private OptionalNewsPage optionalNewsPage;
    private LiveBroadcastPage liveBroadcastPage;
    
    private ImageView imgPlayStatus;
    private RadioButton tabHeadline, tabLive, tabOptionalNews;
    private PageSwitcher pageSwitcher;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_infohome);

        pageSwitcher = (PageSwitcher) findViewById(R.id.page_infohome_pageswitcher);
        if (pageSwitcher != null) {
            // 设置为true时可以左右滑动切换子Page
            pageSwitcher.setSwitchable(true);

            headLinePage = new HeadLinePage();
            headLinePage.setOnPlayStatusChanged(this);
            pageSwitcher.addPage(headLinePage);

            liveBroadcastPage = new LiveBroadcastPage();
            liveBroadcastPage.setOnPlayStatusChanged(this);
            pageSwitcher.addPage(liveBroadcastPage);
            
            boolean isLogined = DataModule.getInstance().getUserInfo().isLogined();
            if (isLogined && optionalNewsPage == null) {
                optionalNewsPage = new OptionalNewsPage();
                optionalNewsPage.setOnPlayStatusChanged(this);
                pageSwitcher.addPage(optionalNewsPage);
            }
            
            pageSwitcher.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    currentPageIndex = position;

                    if (position == 0) {
                        tabHeadline.setChecked(true);
                    } else if (position == 1) {
                        tabLive.setChecked(true);
                    } else if (position == 2) {
                        tabOptionalNews.setChecked(true);
                    }
                    
                    updateTitleBarTextSize();
                }
                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) { }
                @Override
                public void onPageScrollStateChanged(int arg0) { }
            });

            // 如果不调用此句，pageSwitcher与其中的Page不会显示在界面上
            registViewWithPage(pageSwitcher);
        }
        
        bindPageTitleBar(R.id.page_infothome_titlebar);
    }
    
    @Override
    protected void initData() {}
    
    @Override
    protected void onPageResume() {
        super.onPageResume();
        
        boolean isLogined = DataModule.getInstance().getUserInfo().isLogined();
        if (isLogined) {
            tabOptionalNews.setVisibility(View.VISIBLE);
            
            if (optionalNewsPage == null) {
                optionalNewsPage = new OptionalNewsPage();
                optionalNewsPage.setOnPlayStatusChanged(this);
                pageSwitcher.addPage(optionalNewsPage);
                pageSwitcher.getAdapter().notifyDataSetChanged();                
            }
        } else {
            tabOptionalNews.setVisibility(View.GONE);
            
            if (optionalNewsPage != null && pageSwitcher.getPageCount() == 3) {
                // 退出登录时，去除
                pageSwitcher.removePage(2);
                pageSwitcher.getAdapter().notifyDataSetChanged();
                
                optionalNewsPage = null;
            }
        }
        
        // 根据当前子page的播放状态，根据子page播放状态更新播放状态标志
        if (currentPageIndex == 0) {
            refreshPlayFlag(headLinePage.getPlayStatus());
        } else if (currentPageIndex == 1) {
            refreshPlayFlag(liveBroadcastPage.getPlayStatus());
        } else if (currentPageIndex == 2 && optionalNewsPage != null) {
            refreshPlayFlag(optionalNewsPage.getPlayStatus());
        }
        
        updateTitleBarTextSize();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {

        View leftMenuView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_play, null);
        imgPlayStatus = (ImageView) leftMenuView.findViewById(R.id.layout_titlebar_item_play_img_play_status);

        BarMenuCustomItem leftMenu = new BarMenuCustomItem(0, leftMenuView);
        leftMenu.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftMenu);

        View centerView = View.inflate(getContext(), R.layout.page_infohome_custom_titlebar, null);
        tabHeadline = (RadioButton) centerView.findViewById(R.id.page_infohome_tab_headline);
        tabLive = (RadioButton) centerView.findViewById(R.id.page_infohome_tab_live);
        tabOptionalNews = (RadioButton) centerView.findViewById(R.id.page_infohome_tab_optionalnews);
        
        tabHeadline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPageIndex = 0;
                pageSwitcher.setCurrentItem(0, false);
                updateTitleBarTextSize();
            }
        });

        tabLive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPageIndex = 1;
                pageSwitcher.setCurrentItem(1, false);
                updateTitleBarTextSize();
            }
        });
        
        tabOptionalNews.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPageIndex = 2;
                pageSwitcher.setCurrentItem(2, false);
                updateTitleBarTextSize();
            }
        });

        BarMenuCustomItem centerItem = new BarMenuCustomItem(1, centerView);
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_search, null);
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();

        if (itemId == 0 && mPageChangeFlag == 0) {
            if (currentPageIndex == 0) {    // 头条
                // 1. 更改头条中播放状态
                headLinePage.resetPlayStatus();
                // 2. 修改首页中的播放标志
                refreshPlayFlag(headLinePage.getPlayStatus());
            } else if (currentPageIndex == 1) {     // 直播
                // 1. 更改直播中播放状态
                liveBroadcastPage.resetPlayStatus();
                // 2. 修改首页中的播放标志
                refreshPlayFlag(liveBroadcastPage.getPlayStatus());
            } else if (currentPageIndex == 2 && optionalNewsPage != null) {
                // 更改自选新闻播放状态
                optionalNewsPage.resetPlayStatus();
                refreshPlayFlag(optionalNewsPage.getPlayStatus());
            }
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            SearchPage.gotoSearch(InfoHome.this);
        }
    }

    /**
     * 刷新播放图标状态
     * */
    private void refreshPlayFlag(int playStatus) {
        switch (playStatus) {
            case PLAY_STATUS_INIT:
                imgPlayStatus.setImageResource(R.drawable.img_info_title_play_status_3);
                break;
            case PLAY_STATUS_PLAY:
                loopPlayStatus();
                break;
            case PLAY_STATUS_PAUSE:
                imgPlayStatus.setImageResource(R.drawable.img_info_title_play_status_pause);
                break;
            default:
                break;
        }
    }

    /**
     * 循环显示3种播放状态
     * */
    private void loopPlayStatus() {
        imgPlayStatus.setImageResource(R.drawable.anim_infohome_play_status);
        AnimationDrawable animationDrawable = (AnimationDrawable) imgPlayStatus.getDrawable();
        animationDrawable.start();
    }

    @Override
    public void onPlayStatusChanged(int playStatus) {
        refreshPlayFlag(playStatus);
    }
    
    /**
     * 更新标题栏字体大小
     * */
    private void updateTitleBarTextSize() {
        tabHeadline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);    // s7
        tabLive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);    // s7
        tabOptionalNews.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);    // s7
        
        if (currentPageIndex == 0) {
            tabHeadline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        } else if (currentPageIndex == 1) {
            tabLive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        } else if (currentPageIndex == 2) {
            tabOptionalNews.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        }
    }

}
