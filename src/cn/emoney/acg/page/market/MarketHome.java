package cn.emoney.acg.page.market;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.optional.ChooseOptionalPage;
import cn.emoney.acg.page.optional.EditOptionalPage;
import cn.emoney.acg.page.optional.EditPositionPage;
import cn.emoney.acg.page.optional.OptionalHome;
import cn.emoney.acg.page.optional.OptionalHome.MiniMarketBoardDispalyCB;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.PageSwitcher;
import cn.emoney.sky.libs.widget.PageSwitcher.OnPageSwitchListener;

public class MarketHome extends PageImpl {
    // 自选编辑页面
    public final static int REQUEST_CHOOSE_OPTIONALTYPE = 2003;

    private int currentPageIndex;

    private View mMask = null;
    private PageSwitcher pageSwitcher;
    private RadioButton tabOptional, tabQuotation;

    private OptionalHome mOptionalHomePage;

    private View mLeftMenuView;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_markethome);

        mMask = findViewById(R.id.page_markethome_mask);

        mMask.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeMiniboardAndMask();
                    return true;
                }
                mMask.setVisibility(View.GONE);
                pageSwitcher.setSwitchable(true);
                return false;
            }
        });

        pageSwitcher = (PageSwitcher) findViewById(R.id.page_markethome_pageswitcher);
        if (pageSwitcher != null) {
            // 设置为true时可以左右滑动切换子Page
            pageSwitcher.setSwitchable(true);

            mOptionalHomePage = new OptionalHome();
            mOptionalHomePage.setMaskCB(new MiniMarketBoardDispalyCB() {
                @Override
                public void miniboardSwitch(boolean open) {
                    if (open) {
                        mMask.setVisibility(View.VISIBLE);
                        pageSwitcher.setSwitchable(false);

                    } else {
                        pageSwitcher.setSwitchable(true);
                        mMask.setVisibility(View.GONE);
                    }
                }
            });
            pageSwitcher.addPage(mOptionalHomePage);

            Page page = new QuotationPage();
            pageSwitcher.addPage(page);

            pageSwitcher.setOnPageSwitchListener(new OnPageSwitchListener() {
                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        currentPageIndex = 0;

                        tabOptional.setChecked(true);
                        mLeftMenuView.setVisibility(View.VISIBLE);
                    } else if (position == 1) {
                        currentPageIndex = 1;

                        tabQuotation.setChecked(true);
                        mLeftMenuView.setVisibility(View.INVISIBLE);
                    }

                    updateTitleBarTextSize();
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {}

                @Override
                public void onPageScrollStateChanged(int arg0) {}
            });

            // 如果不调用此句，pageSwitcher与其中的Page不会显示在界面上
            registViewWithPage(pageSwitcher);
        }

        bindPageTitleBar(R.id.page_markethome_titlebar);
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        closeMiniboardAndMask();
        updateTitleBarTextSize();
    }

    private void closeMiniboardAndMask() {
        if (pageSwitcher != null) {
            Page page = pageSwitcher.getPage(0);
            if (page != null && page instanceof OptionalHome) {
                OptionalHome optionalHome = (OptionalHome) page;
                optionalHome.closeMask();
                mMask.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        mLeftMenuView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_menu, null);
        BarMenuCustomItem leftMenu = new BarMenuCustomItem(0, mLeftMenuView);
        leftMenu.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftMenu);

        View centerView = View.inflate(getContext(), R.layout.page_markethome_custom_titlebar, null);
        tabOptional = (RadioButton) centerView.findViewById(R.id.page_markethome_segment_optional);
        tabQuotation = (RadioButton) centerView.findViewById(R.id.page_markethome_segment_quotation);

        tabOptional.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageSwitcher.getCurrentItem() == 0 && mPageChangeFlag == 0) {
                    if (!isLogined()) {
                        showTip("登录后即可使用自选股分类功能");
                        return;
                    }

                    goChooseOptionalPage();
                } else {
                    currentPageIndex = 0;
                    pageSwitcher.setCurrentItem(0, false);
                    mLeftMenuView.setVisibility(View.VISIBLE);
                    updateTitleBarTextSize();
                }
            }
        });

        tabQuotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPageIndex = 1;
                pageSwitcher.setCurrentItem(1, false);
                mLeftMenuView.setVisibility(View.INVISIBLE);
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
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            int count = mOptionalHomePage.getCurOptionalCount();
            if (count <= 0) {
                showTip("请先添加自选股");
                return;
            } else {
                PageIntent intent = null;
                String type = mOptionalHomePage.getCurOptionalType();
                if (!TextUtils.isEmpty(type)) {
                    if (type.equals(OptionalInfo.TYPE_POSITION)) {
                        intent = new PageIntent(this, EditPositionPage.class);
                    } else {
                        intent = new PageIntent(this, EditOptionalPage.class);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(EditOptionalPage.EXTRA_KEY_OPTIONTYPE, type);
                    intent.setArguments(bundle);
                    startPage(DataModule.G_CURRENT_FRAME, intent);
                }

            }
        }


        else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;

            String fromType = OptionalInfo.TYPE_DEFAULT;
            if (mOptionalHomePage != null) {
                fromType = mOptionalHomePage.getCurOptionalType();
            }
            SearchPage.gotoSearch(MarketHome.this, fromType);
//            gotoSearch(fromType);
        }
    }

    // 自选分类编辑页面
    private void goChooseOptionalPage() {
        mPageChangeFlag = -1;
        PageIntent intent = new PageIntent(this, ChooseOptionalPage.class);
        Bundle bundle = new Bundle();
        String curOptionalType = mOptionalHomePage.getCurOptionalType();

        bundle.putString(ChooseOptionalPage.EXTRA_KEY_TITLE, curOptionalType);
        intent.setArguments(bundle);
        startPageForResult(intent, REQUEST_CHOOSE_OPTIONALTYPE);
    }

    @Override
    protected void onPageResult(int requestCode, int resultCode, Bundle data) {
        super.onPageResult(requestCode, resultCode, data);

        if (resultCode == ChooseOptionalPage.RESULT_CHOOSE_OPTIONALTYPE) {
            if (requestCode == REQUEST_CHOOSE_OPTIONALTYPE) {
                if (data == null) {
                    return;
                }
                if (data.containsKey("type")) {
                    String newType = data.getString("type");

                    if (!isLogined()) {
                        newType = OptionalInfo.TYPE_DEFAULT;
                    } else {
                        int nRet = DataModule.getInstance().getOptionalInfo().hasType(newType);
                        if (nRet < 0) {
                            newType = OptionalInfo.TYPE_DEFAULT;
                        }
                    }

                    String displayType = "";
                    if (newType != null && !newType.equals("")) {
                        if (newType.equals(OptionalInfo.TYPE_DEFAULT))
                            displayType = "自选";

                        else if (newType.length() >= 4) {
                            displayType = newType.substring(0, 4);
                        } else {
                            displayType = newType;
                        }
                    }

                    tabOptional.setText(displayType);

                    if (mOptionalHomePage != null) {
                        mOptionalHomePage.notifyTypeChange(newType);
                    }

                }
            }
        }
    }

    /**
     * 更新标题栏字体大小
     * */
    private void updateTitleBarTextSize() {
        tabOptional.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);    // s7
        tabQuotation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);    // s7

        Drawable rightArrow = getResources().getDrawable(R.drawable.img_markethome_titlebar_arrow_normal);  
        rightArrow.setBounds(0, 0, rightArrow.getMinimumWidth(), rightArrow.getMinimumHeight());  
        tabOptional.setCompoundDrawables(null, null, rightArrow, null);

        if (currentPageIndex == 0) {
            tabOptional.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

            Drawable rightArrowSelected = getResources().getDrawable(R.drawable.img_markethome_titlebar_arrow_selected);  
            rightArrowSelected.setBounds(0, 0, rightArrowSelected.getMinimumWidth(), rightArrowSelected.getMinimumHeight());  
            tabOptional.setCompoundDrawables(null, null, rightArrowSelected, null);
        } else if (currentPageIndex == 1) {
            tabQuotation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        }
    }

}
