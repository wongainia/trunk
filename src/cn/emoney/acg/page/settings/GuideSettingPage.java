package cn.emoney.acg.page.settings;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.emoney.acg.R;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class GuideSettingPage extends PageImpl {

    private ViewPager mVp_guide = null;
    private ImageView mIvIndicatorPointer = null;
    private ArrayList<View> mLstGuideView = new ArrayList<View>();

    @Override
    protected void initPage() {
        setContentView(R.layout.page_setting_userguide);
        mIvIndicatorPointer = (ImageView) findViewById(R.id.iv_guide_pointer_indicator);
        mIvIndicatorPointer.setImageResource(R.drawable.img_guide_indicator_1);
        mLstGuideView.clear();
        mLstGuideView.add(View.inflate(getContext(), R.layout.layout_guide_1, null));
        mLstGuideView.add(View.inflate(getContext(), R.layout.layout_guide_2, null));
        View guide3 = View.inflate(getContext(), R.layout.layout_guide_3, null);

        guide3.setEnabled(true);

        guide3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVp_guide.getCurrentItem() == 2) {

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 300);

                }
            }
        });

        mLstGuideView.add(guide3);

        mVp_guide = (ViewPager) findViewById(R.id.setting_userguide_vp_guide);
        mVp_guide.setAdapter(new MyPagerAdapter(mLstGuideView));

        mVp_guide.setOnPageChangeListener(new OnPageChangeListener() {
            boolean bRealScroll = false;

            @Override
            public void onPageSelected(int arg0) {
                if (mIvIndicatorPointer != null) {
                    if (arg0 == 0) {
                        mIvIndicatorPointer.setImageResource(R.drawable.img_guide_indicator_1);
                    } else if (arg0 == 1) {
                        mIvIndicatorPointer.setImageResource(R.drawable.img_guide_indicator_2);
                    } else if (arg0 == 2) {
                        mIvIndicatorPointer.setImageResource(R.drawable.img_guide_indicator_3);
                    }
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                LogUtil.easylog("sky", "onPageScrollStateChanged -> arg0:" + arg0);
                if (arg0 == 2) {
                    bRealScroll = true;
                } else if (mVp_guide.getCurrentItem() == 2 && arg0 == 0 && bRealScroll == false) {
                    // 最后一页,向右滑动
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 300);
                } else {
                    bRealScroll = false;
                }

            }
        });

        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    /**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {}

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {}
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

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "用户指南");
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
