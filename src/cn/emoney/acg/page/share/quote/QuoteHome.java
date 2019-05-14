package cn.emoney.acg.page.share.quote;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.page.share.quote.QuotePage.OnNoticeRefresh;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.widget.LoopPageSwitcher;

public class QuoteHome extends PageImpl {

    public final static String EXTRA_KEY_LIST_INDEX = "key_list_index";
    public final static String EXTRA_KEY_LIST_GOODS = "key_list_goods";
    public final static String EXTEA_KEY_PERIOD = "key_period";

    // 防止暴力滑动,延时请求
    public static long REQ_DELAY_TIME = 100;

    // 个股详情标题
    private TextView mTvStockName, mTvStockCode, mTvStockNotice;

    private LoopPageSwitcher mPageSwitcher;

    private List<Goods> listGoods;
    private int currentIndex = 0;
    public static int currentPeriod = TYPE_MINUTE;
    public static int currentMorePeriod = TYPE_60MINUTE;

    private Goods currentGoods;
    private int mCurrPageIndex = 0;
    private int mLastPageIndex = -1;

    private String mLatestNoticeInfo;

    public QuoteHome() {
        needPringLog(true);
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quotehome);

        mPageSwitcher = (LoopPageSwitcher) findViewById(R.id.page_quotehome_switcher);
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(true);
            mPageSwitcher.setPreload(true);
            mPageSwitcher.setPageFactory(new LoopPageSwitcher.IPageFactory() {
                
                @Override
                public Page createPage(int position) {
                    QuotePage page = null;
                    if (listGoods != null && listGoods.size() > 0) {
                        if (listGoods.size() > position) {
                            int goodsId = listGoods.get(position).getGoodsId();
                            if (DataUtils.IsBK(goodsId) || DataUtils.IsZS(goodsId)) {
                                page = new QuoteBkPage();
                            } else {
                                page = new QuoteStockPage();
                            }
                            page.setGoods(listGoods.get(position));
                        } else {
                            int goodsId = listGoods.get(0).getGoodsId();
                            if (DataUtils.IsBK(goodsId) || DataUtils.IsZS(goodsId)) {
                                page = new QuoteBkPage();
                            } else {
                                page = new QuoteStockPage();
                            }
                            page.setGoods(listGoods.get(0));
                        }
                    } else {
                        page = new QuoteBkPage();
                        page.setGoods(new Goods(1, "上证指数"));
                    }
                    page.needPringLog(false);
                    page.setSupportAnimation(false);
                    page.setOnNoticeRefresh(new OnNoticeRefresh() {

                        @Override
                        public void refreshNotice(String notice) {
                            mLatestNoticeInfo = notice;
                            mTvStockNotice.setText(notice);
                        }
                    });
                    page.setOnChangePeriodListener(new OnChangePeriodListener() {
                        @Override
                        public void onChangePeriod(int period) {
                            currentPeriod = period;
                        }

                        @Override
                        public void onChangeMorePeriod(int period) {
                            currentMorePeriod = period;
                            currentPeriod = period;
                        }
                    });

                    return page;

                }
            });
            mPageSwitcher.setOnPageSwitchListener(new LoopPageSwitcher.OnPageSwitchListener() {

                @Override
                public void onPageSelected(int index) {
                    if (listGoods != null && listGoods.size() > index) {
                        mCurrPageIndex = index;
                    }
                }

                @Override
                public void onPageScrolled(int i, float v, int i2) {
                    if (mLastPageIndex != mCurrPageIndex && i2 == 0) {
                        if (listGoods != null && listGoods.size() > mCurrPageIndex) {
                            currentGoods = listGoods.get(mCurrPageIndex);
                            resetTitle();
                            mLastPageIndex = mCurrPageIndex;
                        }

                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == 0) {
                        REQ_DELAY_TIME = 400;
                    }
                }
            });

        }

        bindPageTitleBar(R.id.page_quotehome_titlebar);
    }

    protected void onPageResume() {
        super.onPageResume();
        REQ_DELAY_TIME = 0;
        resetTitle();
    }

    @Override
    public void onPagePause() {
        super.onPagePause();
    }

    private void resetTitle() {
        mImplHandler.post(new Runnable() {

            @Override
            public void run() {

                if (currentGoods != null) {
                    getDBHelper().setInt(DataModule.G_KEY_LAST_LOOK_GOODID, currentGoods.getGoodsId());

                    mTvStockName.setText(currentGoods.getGoodsName());
                    mTvStockCode.setText(currentGoods.getGoodsCode());

                    if (mLatestNoticeInfo != null) {
                        mTvStockNotice.setText(mLatestNoticeInfo);
                    }
                }
            }

        });
    }

    @Override
    protected void initData() {
        currentPeriod = TYPE_MINUTE;
        currentMorePeriod = TYPE_60MINUTE;

        mPageSwitcher.setPageCount(listGoods.size());
        registViewWithPage(mPageSwitcher);
        mPageSwitcher.setCurrentItem(currentIndex);
    }

    @Override
    protected void receiveData(Bundle bundle) {
        super.receiveData(bundle);

        if (bundle == null) {
            return;
        }

        if (bundle.containsKey(EXTEA_KEY_PERIOD)) {
            currentPeriod = bundle.getInt(EXTEA_KEY_PERIOD);
        }
        if (bundle.containsKey(EXTRA_KEY_LIST_INDEX)) {
            currentIndex = bundle.getInt(EXTRA_KEY_LIST_INDEX);
            if (currentIndex < 0) {
                currentIndex = 0;
            }
        }
        if (bundle.containsKey(EXTRA_KEY_LIST_GOODS)) {
            listGoods = bundle.getParcelableArrayList(EXTRA_KEY_LIST_GOODS);
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        View centerView = LayoutInflater.from(getContext()).inflate(R.layout.page_quotehome_title, null);
        BarMenuCustomItem centerItem = new BarMenuCustomItem(1, centerView);
        mTvStockName = (TextView) centerView.findViewById(R.id.item_goods_name);
        mTvStockCode = (TextView) centerView.findViewById(R.id.item_goods_code);
        mTvStockNotice = (TextView) centerView.findViewById(R.id.item_notice);
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
            mPageChangeFlag = -1;
            finish();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            SearchPage.gotoSearch(QuoteHome.this);
        }
    }

    public void allowSwitch() {
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(true);
        }
    }

    public void disallowSwitch() {
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(false);
        }
    }
    
}
