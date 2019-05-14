package cn.emoney.acg.page.quiz;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.quiz.QuizItemInfo;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;

/**
 * @ClassName: TeacherDetailPage
 * @Description:股大师详情
 * @author xiechengfa
 * @date 2015年12月11日 下午2:22:53
 *
 */
public class TeacherDetailPage extends PageImpl implements QuizListViewlListener, OnPageChangeListener, OnScrollListener, OnClickListener {
    private static final int GET_DATA_SUCC = 1;// 获取信息成功
    private static final int GET_DATA_FAIL = 2;// 获取信息失败
    private static final int GET_DATA_NET_ERROR = 3;// 获取网络异常
    private static final int GET_MORE_SUCC = 4;// 获取更多成功
    private static final int GET_MORE_FAIL = 5;// 获取更多失败
    private static final int GET_MORE_NET_ERROR = 6;// 获取更多网路异常

    private final static String INTENT_KEY_ID = "user_id";
    private final static String INTENT_KEY_NAME = "user_name";

    private long userId = 0;
    private String userName = null;
    private RefreshListView listView = null;
    private QuizListAdapter adapter = null;
    private ViewPager viewPager = null;
    private ImageView pointView = null;
    private TitleBar bar = null;
    private ArrayList<QuizItemInfo> listDatas = null;

    // 加载更多的定义
    private boolean isRequestDataing = false;// 是请求数据完成
    private boolean isLoadOver = false;

    // footerview
    private View footerView = null;
    private LinearLayout footerMoreLayout;
    private TextView footerMoreText;

    public static void startPage(PageImpl page, long userId, String userName) {
        PageIntent intent = new PageIntent(page, TeacherDetailPage.class);
        Bundle bundle = new Bundle();
        bundle.putLong(INTENT_KEY_ID, userId);
        bundle.putString(INTENT_KEY_NAME, userName);
        intent.setArguments(bundle);
        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_teacher_detail);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(INTENT_KEY_ID)) {
                userId = bundle.getLong(INTENT_KEY_ID);
            }
            if (bundle.containsKey(INTENT_KEY_NAME)) {
                userName = bundle.getString(INTENT_KEY_NAME);
            }
        }

        initFooter();
        initListView();

        setNoDataLayoutState(false);
        bar = (TitleBar) findViewById(R.id.teacherTitleBar);
        bindPageTitleBar(R.id.teacherTitleBar);
    }

    private void initListView() {
        View headView = LayoutInflater.from(getContext()).inflate(R.layout.layout_teacher_viewpage_header, null);
        initViewPage(headView);

        listView = (RefreshListView) findViewById(R.id.listView);
        listView.setOnScrollListener(this);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, Util.getResourcesDimension(R.dimen.quiz_teacher_header_height));
        headView.setLayoutParams(params);
        listView.addHeaderView(headView);
        listView.addFooterView(footerView, null, false);
        adapter = new QuizListAdapter(this, this);

        adapter.setData(listDatas);
        listView.setAdapter(adapter);
    }

    private void initFooter() {
        footerView = View.inflate(getContext(), R.layout.footer_more_view, null);
        footerMoreLayout = (LinearLayout) footerView.findViewById(R.id.moreLayout);
        footerMoreLayout.setVisibility(View.VISIBLE);
        footerMoreText = (TextView) footerView.findViewById(R.id.moreTxt);
        footerMoreText.setVisibility(View.GONE);
        footerMoreText.setOnClickListener(this);
    }

    private void initViewPage(View headView) {
        // TODO Auto-generated method stub
        viewPager = (ViewPager) headView.findViewById(R.id.viewPager);
        pointView = (ImageView) headView.findViewById(R.id.pointImageView);
        viewPager.setOnPageChangeListener(this);

        View page1 = View.inflate(getContext(), R.layout.layout_teacher_viewpage_item_userinfo, null);
        View page2 = View.inflate(getContext(), R.layout.layout_teacher_viewpage_item_summary, null);
        List<View> mListViews = new ArrayList<View>();
        mListViews.add(page1);
        mListViews.add(page2);

        viewPager.setAdapter(new MyViewPagerAdapter(mListViews));
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }


    @Override
    protected void initData() {
        //
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        View centerView = LayoutInflater.from(getContext()).inflate(R.layout.page_teacher_detail_title, null);
        BarMenuCustomItem centerItem = new BarMenuCustomItem(1, centerView);
        TextView nameView = (TextView) centerView.findViewById(R.id.nameView);
        TextView titleView = (TextView) centerView.findViewById(R.id.titleView);
        nameView.setText(userName);
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);


        View rightView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView textView = (TextView) rightView.findViewById(R.id.tv_titlebar_text);
        textView.setText("关注");

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            // 返回
            mPageChangeFlag = -1;
            finish();
        } else if (itemId == 2) {
            // 关注
        }
    }

    private OnClickEffectiveListener listener = new OnClickEffectiveListener() {

        @Override
        public void onClickEffective(View v) {
            // TODO Auto-generated method stub
        }
    };


    /**
     * 评论
     * 
     * @param type
     */
    public void onAppraise(long id, int lev) {
        // 不实现
    }

    /**
     * 问题超时
     * 
     * @param pos
     */
    public void onQuestionClose(int pos) {
        // 不实现
    }

    /**
     * 点击头像
     * 
     * @param pos
     */
    public void onClickHeadIcon(int pos) {
        // 不实现
    }

    /**
     * 播放语音
     * 
     * @param pos
     */
    public void onPlayVoice(int pos) {

    }

    private void setNoDataLayoutState(boolean isShow) {
        if (isShow) {
            findViewById(R.id.noDataLayout).setVisibility(View.VISIBLE);
            TextView noDataView = (TextView) findViewById(R.id.noDataView);
            noDataView.setText(R.string.no_data_msg);
        } else {
            findViewById(R.id.noDataLayout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        // 不实现
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        // 不实现
    }

    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        if (pointView != null) {
            if (arg0 == 0) {
                pointView.setImageResource(R.drawable.img_viewpage_point_1);
            } else {
                pointView.setImageResource(R.drawable.img_viewpage_point_2);
            }
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (isLoadOver) {
            // 加载完成
            return;
        }

        // 判断滚动到底部
        if (view.getLastVisiblePosition() == (view.getCount() - 1) && footerMoreLayout.getVisibility() == View.VISIBLE) {
            loadMoreData();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

        // 修改标题头的背景透明度
        if (firstVisibleItem == 0 && bar != null) {
            int top = 0;
            View child = view.getChildAt(0);
            if (child != null) {
                top = -child.getTop();
            }

            int alpha = (int) (1.0f * top / Util.getResourcesDimension(R.dimen.quiz_teacher_viewpage_height) * 255);
            if (alpha > 255) {
                alpha = 255;
            }

            // 原来的颜色是C9(2A323F)
            bar.setBackgroundColor(Color.argb(alpha, 42, 50, 63));
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.moreTxt) {
            if (footerMoreText.getVisibility() == View.VISIBLE) {
                footerMoreLayout.setVisibility(View.VISIBLE);
                footerMoreText.setVisibility(View.GONE);
                loadMoreData();
            }
        }
    }

    // 加载翻页数据
    private void loadMoreData() {
        if (isRequestDataing) {
            // 当前请求未完成或者数据全部加载完成，不再重新启动任务
            return;
        }

        isRequestDataing = true;

        new AsyncTask<Object, Void, Message>() {
            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
            }

            @Override
            protected Message doInBackground(Object... params) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain();
                msg.what = GET_MORE_SUCC;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Message result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                isRequestDataing = false;
                handleMyMessage(result);
            }
        }.execute();
    }

    private void handleMyMessage(Message msg) {
        if (msg == null) {
            return;
        }

        switch (msg.what) {
            case GET_DATA_SUCC:
                // 获取数据成功
                // 自动加载
                footerMoreText.setVisibility(View.GONE);
                if (isLoadOver) {
                    footerMoreLayout.setVisibility(View.INVISIBLE);
                }
                break;
            case GET_DATA_FAIL:
                // 获取数据失败
                break;
            case GET_DATA_NET_ERROR:
                // 获取数据网络异常
                break;
            case GET_MORE_SUCC:
                // 获取更多数据成功
                // test
                footerMoreText.setVisibility(View.GONE);
                if (isLoadOver) {
                    footerMoreLayout.setVisibility(View.INVISIBLE);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                break;
            case GET_MORE_FAIL:
                // 加载更多失败
                // 自动加载
                footerMoreLayout.setVisibility(View.GONE);
                footerMoreText.setVisibility(View.VISIBLE);
                showTip(Util.getResourcesString(R.string.loading_fail));
                break;
            case GET_MORE_NET_ERROR:
                // 加载网络异常
                footerMoreLayout.setVisibility(View.GONE);
                footerMoreText.setVisibility(View.VISIBLE);
                showTip(Util.getResourcesString(R.string.net_error_check));
                break;
        }
    }

    // private void getTestData(boolean isLoadMore) {
    // if (listDatas == null) {
    // listDatas = new ArrayList<QuizItemInfo>();
    // }
    //
    // for (int i = 0; i < 20; i++) {
    // QuizItemInfo info = new QuizItemInfo();
    // info.setType(QuizItemInfo.TYPE_STATUS);
    //
    // String mp3Url =
    // "http://222.186.30.212:8082/demo_store/2015/12/08/05fc66890595745c82b704c32f6b4fdc.mp3";
    // QuizContentInfo askInfo = new QuizContentInfo(QuizContentInfo.TYPE_ASK_MY,
    // QuizContentInfo.CONTENT_TYPE_TXT, QuizContentInfo.STATE_ASK_REPLED, "945575", "101", "送大米",
    // "青岛啤酒600600怎么样？可以买嘛？600600 青岛啤酒可不可以卖了", "3小时前", 3, mp3Url, "6'30''", "6'30''");
    // QuizContentInfo replyInfo = new QuizContentInfo(QuizContentInfo.TYPE_REPLY,
    // QuizContentInfo.CONTENT_TYPE_TXT, QuizContentInfo.STATE_ASK_REPLED, "945575", "102", "送小米",
    // "青岛啤酒600600可以买，赶紧建仓，青岛啤酒可以买，赶紧建仓，青岛啤酒可以买，赶紧建仓，青岛啤酒可以买，赶紧建仓，青岛啤酒可以买，赶紧建仓，青岛啤可以买，赶紧建仓", "3小时前",
    // 3, mp3Url, "6'30''", "6'30''");
    //
    // askInfo.setType(QuizContentInfo.TYPE_ASK_OTHER);
    // if (i % 2 == 0) {
    // replyInfo.setContentType(QuizContentInfo.CONTENT_TYPE_TXT);
    // } else {
    // replyInfo.setContentType(QuizContentInfo.CONTENT_TYPE_VOICE);
    // }
    //
    // if (!isLoadMore) {
    // if (i == 0) {
    // info.setType(QuizItemInfo.TYPE_STRING);
    // info.setTipStr("TA的打理组合");
    // } else if (i == 1 || i == 2) {
    // // 组合
    // info.setType(QuizItemInfo.TYPE_GROUP);
    // } else if (i == 3) {
    // info.setType(QuizItemInfo.TYPE_STRING);
    // info.setTipStr("TA解答的问题");
    // }
    // }
    //
    // info.setAskInfo(askInfo);
    // info.setReplyInfo(replyInfo);
    // listDatas.add(info);
    // }
    // }
}
