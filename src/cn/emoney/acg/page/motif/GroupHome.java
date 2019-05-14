package cn.emoney.acg.page.motif;

import android.os.Bundle;
import cn.emoney.acg.R;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.widget.LoopPageSwitcher;

public class GroupHome extends PageImpl {
    // 防止暴力滑动,延时请求
    public static long REQ_DELAY_TIME = 0;

    private int mCurrIndex = 0;

    private int mCurrPageIndex = 0;
    private int mLastPageIndex = -1;

    private LoopPageSwitcher mPageSwitcher;

    @Override
    protected void initPage() {
        quitFullScreen();
        setContentView(R.layout.page_group_home);

        mPageSwitcher = (LoopPageSwitcher) findViewById(R.id.switchpage_pageswitcher);
        if (mPageSwitcher != null) {
            mPageSwitcher.setSwitchable(true);
            mPageSwitcher.setPreload(false);
            mPageSwitcher.setPageFactory(new LoopPageSwitcher.IPageFactory() {

                @Override
                public Page createPage(int position) {
                    GroupPage page = new GroupPage();
                    int dataSize = BuyClubModule.getInstance().getDataSize();
                    if (dataSize > 0) {
                        if (BuyClubModule.getInstance().getDataSize() > position) {
                            page.setBuyGroupData(BuyClubModule.getInstance().getData(position));
                        } else {
                            page.setBuyGroupData(BuyClubModule.getInstance().getData(0));
                        }
                    }
                    page.setSupportAnimation(false);
                    page.needPringLog(false);

                    return page;
                }
            });
            mPageSwitcher.setOnPageSwitchListener(new LoopPageSwitcher.OnPageSwitchListener() {
                @Override
                public void onPageSelected(int index) {
                    if (BuyClubModule.getInstance().getDataSize() > index) {
                        mCurrPageIndex = index;
                    }
                }

                @Override
                public void onPageScrolled(int i, float v, int i2) {
                    if (mLastPageIndex != mCurrPageIndex && i2 == 0) {
                        if (BuyClubModule.getInstance().getDataSize() > mCurrPageIndex) {
                            mLastPageIndex = mCurrPageIndex;
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == 1) {
                        GroupPage tPage = (GroupPage) mPageSwitcher.getCurrentPage();
                        if (tPage != null) {
                            tPage.restoreListViewRefresh();
                        }

                        REQ_DELAY_TIME = 400;
                    }
                }
            });

            mPageSwitcher.setPageCount(BuyClubModule.getInstance().getDataSize());
            mPageSwitcher.setCurrentItem(mCurrIndex);

            registViewWithPage(mPageSwitcher);
        }

    }
    
    @Override
    protected void receiveData(Bundle bundle) {
        if (bundle != null && bundle.containsKey(MotifHome.KEY_BUY_GROUP_DATA_INDEX)) {
            mCurrIndex = bundle.getInt(MotifHome.KEY_BUY_GROUP_DATA_INDEX);
        }
    }
    
    @Override
    protected void initData() {}

    protected void onPageResume() {
        super.onPageResume();
        REQ_DELAY_TIME = 0;
    }

    @Override
    public void onPagePause() {
        InputMethodUtil.closeSoftKeyBoard(this);
        super.onPagePause();
    }

    @Override
    public void onStop() {
        GroupPraiseModule.getInstance(getContext()).saveDb();
        GroupPraiseModule.letFree();

        super.onStop();
    }

    /**
     * 进入时的动画
     * 
     * @return
     */
    public int enterAnimation() {
        return 0;
    }

    

}
