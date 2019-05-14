package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.motif.BuyClubField;
import cn.emoney.acg.data.protocol.motif.BuyClubListPackage;
import cn.emoney.acg.data.protocol.motif.BuyClubListReply.BuyClubList_Reply;
import cn.emoney.acg.data.protocol.motif.BuyClubListReply.BuyClubList_Reply.GroupData;
import cn.emoney.acg.data.protocol.motif.BuyClubListRequest.BuyClubList_Request;
import cn.emoney.acg.module.IndependentModuleHome;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.VolleyHelper;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

/**
 * 买吧主界面
 * */
public class MotifHome extends PageImpl implements BuyClubField {

    public static final String KEY_BUY_GROUP_DATA_INDEX = "key_buy_group_data_index";

    private final int COUNT_GROUP_ONEPAGE = 10;
    private final int COUNT_GROUP_MAX = 80;
    private final int CHECK_MINE_BACKGROUND = 101;

    private final int MOTIF_TYPE_NORMAL = 0;      // 0:请求正常排序列表，今日之星或最高收益
    private final int MOTIF_TYPE_MINE = 1;        // 1:请求"我的"，我的组合
    private final int MOTIF_TYPE_MASTER = 4;      // 4:请求大师数据，大师

    private boolean isRequesting, isRefreshing, isLoading;
    private boolean isHasMore = true;
    /**
     * Resume时只请求1次
     * */
    private boolean resumeRequestFlag;
    public static boolean isNeedUpdateMineType = true;

    private int[] dataSize = new int[4];    // 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合
    private int motifType = MOTIF_TYPE_MASTER;
    private int sortField = FIELD_TOTAL_INCOME;
    /**
     * 0: 从头请求； 1：加载更多
     * */
    private int requestType;

    private String URL_GROUP_ICON_TEMPLATES = "";
    private String URL_GROUP_ICON_TEMPLATES_bak = "http://static.emoney.cn/webupload/motif/mobile/group%d/icon.png";

    private MotifListAdapter adapter;

    private TextView tvMotifTypeNotice;
    private RefreshListView listView;
    private TextView tvEmpty;
    private View layoutProgress;
    private View layoutLoadMore;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_motifhome);

        initViews();
        bindPageTitleBar(R.id.page_motifhome_titlebar);
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (resumeRequestFlag == false) {
            resumeRequestFlag = true;
            requestMotifs();
        }

        if (isNeedUpdateMineType) {
            requestMotifList(CHECK_MINE_BACKGROUND, MOTIF_TYPE_MINE, -9999, 0, 20);
            isNeedUpdateMineType = false;
        }
    }

    @Override
    public ArrayList<String> getRegisterBcdc() {
        ArrayList<String> lstBcdc = new ArrayList<String>();
        lstBcdc.add(BroadCastName.BCDC_CHANGE_LOGIN_STATE);
        return lstBcdc;
    }

    @Override
    public void onReceivedBroadcast(String action) {
        super.onReceivedBroadcast(action);
        if (action != null && action.equals(BroadCastName.BCDC_CHANGE_LOGIN_STATE)) {
            requestMotifList(CHECK_MINE_BACKGROUND, MOTIF_TYPE_MINE, -9999, 0, 20);
            requestMotifs();
        }
    }

    private void initViews() {
        RadioGroup radioGroupHeader = (RadioGroup) findViewById(R.id.page_motifhome_head_content);
        tvMotifTypeNotice = (TextView) findViewById(R.id.page_motifhome_tv_head_tag);
        listView = (RefreshListView) findViewById(R.id.page_motifhome_list);
        tvEmpty = (TextView) findViewById(R.id.page_motifhome_tv_empty);
        layoutProgress = findViewById(R.id.page_motifhome_layout_loading);
        View listFooter = View.inflate(getContext(), R.layout.include_layout_listfooter_loadmore, null);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);

        radioGroupHeader.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleMotifTypeChanged(checkedId);
            }
        });

        // 下拉刷新头
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing && !isLoading && !isRequesting) {
                    requestType = 0;
                    requestMotifList(0, motifType, sortField, 0, getDataSize(motifType, sortField));
                    isRefreshing = true;
                }

                // 3秒钟后，如果仍没有返回，就隐藏header
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefreshing) {
                            isRefreshing = false;

                            if (listView != null) {
                                listView.onRefreshFinished();
                            }
                        }
                    }
                }, DataModule.REQUEST_MAX_LIMIT_TIME);
            }

            @Override
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() {}
        });
        listView.setRefreshable(false);

        adapter = new MotifListAdapter(getContext());
        listView.setAdapter(adapter);

        listView.addFooterView(listFooter);
        listView.setPostScrollListener(new PostScrollListener() {
            private int previousScrollState = -1;
            private int mCurrentScrollState = -1;
            private boolean isScrolling;
            private boolean isHasLoadMore = true;    // 滚动期间是否已经加载过更多

            @Override
            public void postScrollStateChanged(AbsListView view, int scrollState) {
                if ( scrollState == OnScrollListener.SCROLL_STATE_IDLE ) {
                    view.invalidateViews();
                }

                previousScrollState = mCurrentScrollState;
                mCurrentScrollState = scrollState;

                if (mCurrentScrollState == OnScrollListener.SCROLL_STATE_FLING || mCurrentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                } else {
                    isScrolling = false;
                }
                if (previousScrollState == OnScrollListener.SCROLL_STATE_IDLE && mCurrentScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isHasLoadMore = false;
                }
            }

            @Override
            public void postScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == totalItemCount || totalItemCount == 0 || visibleItemCount == 0) {
                    layoutLoadMore.setVisibility(View.GONE);
                    return;
                }

                boolean isLoadMore = (firstVisibleItem + visibleItemCount >= totalItemCount) && totalItemCount < COUNT_GROUP_MAX 
                        && !isLoading && isScrolling && isHasLoadMore == false && motifType != MOTIF_TYPE_MINE && isHasMore
                        && !isRequesting && !isRefreshing;
                if (isLoadMore) {
                    requestType = 1;
                    int loadMoreBegin = BuyClubModule.getInstance().getGroupDataSize();
                    requestMotifList(0, motifType, sortField, loadMoreBegin, COUNT_GROUP_ONEPAGE);

                    isLoading = true;
                    layoutLoadMore.setVisibility(View.VISIBLE);

                    // 如果超过限制时间还未返回数据
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isLoading) {
                                isLoading = false;

                                layoutLoadMore.setVisibility(View.GONE);
                            }
                        }
                    }, DataModule.REQUEST_MAX_LIMIT_TIME);
                }
            }
        });

        tvEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMotifs();
            }
        });

        findViewById(R.id.motif_header_type4).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isLogined()) {
                            showTip("请先登录");
                        }                        
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView tvTitle = (TextView) leftView.findViewById(R.id.tv_titlebar_text);
        tvTitle.setText("创建组合");

        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem titleItem = new BarMenuTextItem(1, "买吧");
        titleItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(titleItem);

        View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_search, null);
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();

        if (itemId == 0) {
            gotoGroupCreatePage();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            SearchPage.gotoSearch(this);
        }
    }

    /**
     * 跳转到组合创建页面
     * */
    private void gotoGroupCreatePage() {
        PageIntent intent = new PageIntent(this, CreateGroupPage.class);

        //        Bundle extras = new Bundle();
        //        extras.putInt(MoreNewsPage.EXTRA_KEY_GOODS_ID, currentGoodsId);
        //        extras.putInt(MoreNewsPage.EXTRA_KEY_NEWS_TYPE, currentInfoType);
        //        extras.putString(MoreNewsPage.EXTRA_KEY_GOODS_NAME, currentGoods.getGoodsName());
        //        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    /**
     * 跳转到组合详情页面
     * 
     * @param iGroupId 组合id
     * @param sIdea 组合理念
     */
    private void gotoGroupPage(int index) {
        Bundle localBundle = new Bundle();
        localBundle.putInt(KEY_BUY_GROUP_DATA_INDEX, index);
        localBundle.putString(IndependentModuleHome.KEY_TARGET_CLASS, "cn.emoney.acg.page.motif.GroupHome");
        getModule().startModule(localBundle, IndependentModuleHome.class);
        getModule().overridePendingTransition(0, 0);
    }

    // 更新数量
    private void udpateDataSize(int type, int sortType, int size) {
        // 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合
        switch (type) {
            case MOTIF_TYPE_NORMAL:
                // 今日之星或最高收益
                if (sortType == FIELD_TOTAL_INCOME) {
                    // 总收益
                    dataSize[1] = size;
                } else {
                    // 今日之星
                    dataSize[2] = size;
                }
                break;
            case MOTIF_TYPE_MINE:
                // 我的组合
                dataSize[3] = size;
                break;
            case MOTIF_TYPE_MASTER:
                // 大师组合
                dataSize[0] = size;
                break;
        }
    }

    private int getDataSize(int type, int sortType) {
        // 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合
        int size = COUNT_GROUP_ONEPAGE;
        switch (type) {
            case MOTIF_TYPE_NORMAL:
                // 今日之星或最高收益
                if (sortType == FIELD_TOTAL_INCOME) {
                    // 总收益
                    size = dataSize[1];
                } else {
                    // 今日之星
                    size = dataSize[2];
                }
                break;
            case MOTIF_TYPE_MINE:
                // 我的组合
                size = dataSize[3];
                break;
            case MOTIF_TYPE_MASTER:
                // 大师组合
                size = dataSize[0];
                break;
        }

        return size;
    }

    /**
     * 处理买吧组合点击切换事件
     * */
    private void handleMotifTypeChanged(int checkedId) {
        listView.onRefreshFinished();
        isRequesting = false;
        isRefreshing = false;
        isLoading = false;

        switch (checkedId) {
            case R.id.motif_header_type1:
                tvMotifTypeNotice.setText("以下是大师创建的组合");
                motifType = MOTIF_TYPE_MASTER;
                sortField = FIELD_TOTAL_INCOME;
                break;
            case R.id.motif_header_type2:
                tvMotifTypeNotice.setText("按总收益排名列表");
                motifType = MOTIF_TYPE_NORMAL;
                sortField = FIELD_TOTAL_INCOME;
                break;
            case R.id.motif_header_type3:
                tvMotifTypeNotice.setText("按今日收益排名列表");
                motifType = MOTIF_TYPE_NORMAL;
                sortField = FIELD_DAY_INCOME;
                break;
            case R.id.motif_header_type4:
                if (!isLogined()) {
                    showTip("请先登录");
                    return;
                }

                tvMotifTypeNotice.setText("以下是与我相关的组合");
                motifType = MOTIF_TYPE_MINE;
                sortField = -9999;
                break;
            default:
                break;
        }

        requestMotifs();
    }

    private void requestMotifs() {
        /*
         * 1. 清空原有数据
         * 2. 请求数据
         * 3. 显示正在加载，不允许下拉刷新
         * 4. 请求成功后隐藏正在加载，如果有数据，允许下拉刷新
         * 5. 请求失败后，显示空白提示，不允许下拉刷新
         * */
        requestType = 0;
        BuyClubModule.getInstance().clearData();
        adapter.notifyDataSetChanged();

        requestMotifList(0, motifType, sortField, 0, COUNT_GROUP_ONEPAGE);
        isRequesting = true;

        tvEmpty.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.VISIBLE);
        listView.setRefreshable(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    isRequesting = false;

                    layoutProgress.setVisibility(View.GONE);
                    tvEmpty.setText("请求失败，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);                    
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }

    /**
     * 请求买吧组合列表
     * 
     * @param reqFlag 标识请求,标识http, CHECK_MINE_BACKGROUND表示预请求用户关系
     * @param motifType 0:请求正常排序列表 1:请求"我的" 2:请求推荐 3:请求定制数据
     * @param sortField BuyClubField
     * @param begin 分段请求,起始位置
     * @param size 分段请求大小
     */
    private void requestMotifList(int reqFlag, int motifType, int sortField, int begin, int size) {
        ArrayList<Integer> motifField = new ArrayList<Integer>();
        if (reqFlag == CHECK_MINE_BACKGROUND) {
            motifField.add(FIELD_MINE_TYPE);
        } else {
            motifField.add(FIELD_NAME);
            motifField.add(FIELD_CREATOR);
            motifField.add(FIELD_DAY_INCOME);
            motifField.add(FIELD_WEEK_INCOME);
            motifField.add(FIELD_MONTH_INCOME);
            motifField.add(FIELD_TOTAL_INCOME);
            motifField.add(FIELD_CREATE_TIME);
            motifField.add(FIELD_PRAISE);
            motifField.add(FIELD_FOCUS);

            if (motifType == MOTIF_TYPE_MINE) {
                motifField.add(FIELD_MINE_TYPE);
            }
        }

        String token = DataModule.getInstance().getUserInfo().getToken();
        LogUtil.easylog("sky", "BuyClubHome->Request->token:" + token);

        BuyClubListPackage pkg = new BuyClubListPackage(new QuoteHead((short) reqFlag));
        pkg.setRequest(BuyClubList_Request.newBuilder().setReqFlag(reqFlag).setClassType(motifType)
                .addAllReqFields(motifField).setSortField(sortField).setSortOrder(true)
                .setReqBegin(begin).setReqSize(size).build());

        LogUtil.easylog("sky", "requestBuyClubList->sortField:" + sortField + " begin:" + begin);

        requestQuote(pkg, IDUtils.ID_GROUP_LIST);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);

        BuyClubListPackage motifPkg = (BuyClubListPackage) pkg;
        BuyClubList_Reply reply = motifPkg.getResponse();
        int reqType = pkg.getRequestType();

        if (reqType == CHECK_MINE_BACKGROUND) {
            if (reply != null) {
                List<GroupData> lstGroup = reply.getGroupListList();
                if (reply.getClassType() == MOTIF_TYPE_MINE && lstGroup != null && lstGroup.size() > 0) {
                    List<Integer> fieldIds = reply.getRepFieldsList();
                    int indexMineType = fieldIds.indexOf(FIELD_MINE_TYPE);
                    if (indexMineType < 0) {
                        return;
                    }

                    for (int i = 0; i < lstGroup.size(); i++) {
                        GroupData data = lstGroup.get(i);
                        int groupId = data.getGroupId();
                        List<String> lstFields = data.getRepFieldValueList();
                        if (indexMineType != -1) {
                            try {
                                int iMinType = Integer.valueOf(lstFields.get(indexMineType));
                                MineGroupModule.getInstance().addMineType(groupId, iMinType);
                            } catch (Exception e) {}
                        }
                    }
                }                
            }
        } else if (reqType == 0) {
            if (isRequesting) {
                isRequesting = false;
                layoutProgress.setVisibility(View.GONE);
            }
            if (isRefreshing) {
                isRefreshing = false;

                if (listView != null) {
                    listView.onRefreshFinished();
                }
            }
            if (isLoading) {
                isLoading = false;
                layoutLoadMore.setVisibility(View.GONE);
            }

            if (reply != null) {
                String tUrlPre = reply.getIconUrlPre();
                LogUtil.easylog("tUrlPre:" + tUrlPre);
                if (!TextUtils.isEmpty(tUrlPre) && TextUtils.isEmpty(URL_GROUP_ICON_TEMPLATES)) {
                    URL_GROUP_ICON_TEMPLATES = tUrlPre;
                }

                int tClassType = reply.getClassType();
                int tSortField = reply.getSortField();
                List<GroupData> lstGroup = reply.getGroupListList();

                if (lstGroup != null && lstGroup.size() > 0) {
                    if (reply.getRepBegin() + lstGroup.size() >= reply.getTotalSize()) {
                        isHasMore = false;
                    } else {
                        isHasMore = true;
                    }

                    // 更新数据数量
                    udpateDataSize(tClassType, tSortField, reply.getRepBegin() + lstGroup.size());

                    List<Integer> fieldIds = reply.getRepFieldsList();
                    int index_name = fieldIds.indexOf(FIELD_NAME);
                    int index_creator = fieldIds.indexOf(FIELD_CREATOR);
                    int index_dayZDF = fieldIds.indexOf(FIELD_DAY_INCOME);
                    int index_weekZDF = fieldIds.indexOf(FIELD_WEEK_INCOME);
                    int index_monthZDF = fieldIds.indexOf(FIELD_MONTH_INCOME);
                    int index_ZDF = fieldIds.indexOf(FIELD_TOTAL_INCOME);
                    int index_creatTime = fieldIds.indexOf(FIELD_CREATE_TIME);
                    int index_focus = fieldIds.indexOf(FIELD_FOCUS);
                    int index_praise = fieldIds.indexOf(FIELD_PRAISE);
                    int index_mintype = -1;
                    if (motifType == MOTIF_TYPE_MINE) {
                        index_mintype = fieldIds.indexOf(FIELD_MINE_TYPE);
                    }

                    if (requestType == 0) {
                        BuyClubModule.getInstance().clearData();
                    }

                    for (int i = 0; i < lstGroup.size(); i++) {
                        BuyGroupData t_data = new BuyGroupData();
                        GroupData data = lstGroup.get(i);
                        int groupId = data.getGroupId();
                        int groupState = data.getState();

                        List<String> lstFields = data.getRepFieldValueList();
                        String sGroupName = lstFields.get(index_name);
                        String sDayZDF = DataUtils.getSignedZDF(lstFields.get(index_dayZDF));
                        String sWeekZDF = DataUtils.getSignedZDF(lstFields.get(index_weekZDF));
                        String sMonthZDF = DataUtils.getSignedZDF(lstFields.get(index_monthZDF));
                        String sZDF = DataUtils.getSignedZDF(lstFields.get(index_ZDF));

                        String sTime = DateUtils.createDay(lstFields.get(index_creatTime));
                        long iFocus = Long.valueOf(lstFields.get(index_focus));
                        String sFocus = DataUtils.formatFocus(iFocus);

                        long iPraiseCount = DataUtils.convertToLong(lstFields.get(index_praise));
                        LogUtil.easylog("sky", "PraiseCount:" + iPraiseCount);

                        if (tClassType == MOTIF_TYPE_MINE && index_mintype != -1) {
                            int iMinType = Integer.valueOf(lstFields.get(index_mintype));
                            MineGroupModule.getInstance().addMineType(groupId, iMinType);
                        }
                        String sCreator = lstFields.get(index_creator);

                        int iCreatorId = -1;
                        int splitIndex = sCreator.indexOf("|");
                        if (splitIndex > 0) {
                            String sCreatorId = sCreator.substring(0, splitIndex);
                            iCreatorId = DataUtils.convertToInt(sCreatorId);
                        }

                        String sCreatorName = sCreator.substring(splitIndex >= 0 ? splitIndex + 1 : 0);

                        t_data.setGroupId(groupId);
                        t_data.setGroupName(sGroupName);
                        t_data.setCreator(sCreatorName);
                        t_data.setCreatorId(iCreatorId);
                        t_data.setGroupState(groupState);
                        t_data.setTotalZDF(sZDF);
                        t_data.setDayZDF(sDayZDF);
                        t_data.setWeekZDF(sWeekZDF);
                        t_data.setMonthZDF(sMonthZDF);
                        t_data.setCreateTime(sTime);
                        t_data.setStrFocus(sFocus);
                        t_data.setiFocusNum(iFocus);
                        t_data.setPraise(iPraiseCount);

                        BuyClubModule.getInstance().addData(t_data);
                    }
                } else {
                    isHasMore = false;
                }
            } else {
                isHasMore = false;
            }

            updateListView();
        }

    }

    @Override
    protected void updateWhenDecodeError() {
        super.updateWhenDecodeError();

        if (isRequesting) {
            isRequesting = false;
            layoutProgress.setVisibility(View.GONE);
        }
        if (isRefreshing) {
            isRefreshing = false;

            if (listView != null) {
                listView.onRefreshFinished();
            }
        }
        if (isLoading) {
            isLoading = false;
            layoutLoadMore.setVisibility(View.GONE);
        }

        updateListView();
    }

    @Override
    protected void updateWhenNetworkError() {
        super.updateWhenNetworkError();

        if (isRequesting) {
            isRequesting = false;
            layoutProgress.setVisibility(View.GONE);
        }
        if (isRefreshing) {
            isRefreshing = false;

            if (listView != null) {
                listView.onRefreshFinished();
            }
        }
        if (isLoading) {
            isLoading = false;
            layoutLoadMore.setVisibility(View.GONE);
        }

        updateListView();
    }

    private void updateListView() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        int count = adapter.getCount();
        if (requestType == 0 && count > 0) {
            listView.setSelection(0);
        }

        if (count > 0) {
            listView.setRefreshable(true);

            tvEmpty.setVisibility(View.GONE);
        } else {
            listView.setRefreshable(false);

            tvEmpty.setText("暂无数据，请点击重试");
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    class MotifListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public MotifListAdapter(Context context) {
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return BuyClubModule.getInstance().getGroupDataSize();
        }

        @Override
        public BuyGroupData getItem(int position) {
            return BuyClubModule.getInstance().getGroupData(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_buyclub_listitem, parent, false);

                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            BuyGroupData tGroup = getItem(position);

            String tTotalZDF = (String) tGroup.getTotalZDF();
            String tDayZDF = (String) tGroup.getDayZDF();
            String tGroupName = (String) tGroup.getGroupName();
            String tCreateTime = (String) tGroup.getCreateTime(); // x天前
            String tFocus = (String) tGroup.getStrFocus();
            int tRecommondFlag = tGroup.getGroupState();

            vh.tvMotifName.setText(tGroupName);

            vh.tvTotalZdf.setText(tTotalZDF);
            int tColor = getZDPColor(FontUtils.getColorByZDF_percent(tTotalZDF));
            vh.tvTotalZdf.setTextColor(tColor);

            vh.tvDayZdf.setText(tDayZDF);
            tColor = getZDPColor(FontUtils.getColorByZDF_percent(tDayZDF));
            vh.tvDayZdf.setTextColor(tColor);

            vh.tvCreateTime.setText(tCreateTime);

            int t_groupType = MineGroupModule.getInstance().getMineType(tGroup.getGroupId());
            if (t_groupType == MineGroupModule.MINE_TYPE_BUY) {
                vh.tvFocus.setVisibility(View.INVISIBLE);
                vh.ivFlag.setVisibility(View.VISIBLE);
                vh.ivFlag.setBackgroundResource(R.drawable.img_buyclub_gold);
            } else {
                vh.tvFocus.setVisibility(View.VISIBLE);
                vh.tvFocus.setText(String.format("%s人关注", tFocus));

                if (tRecommondFlag == 1) {
                    vh.ivFlag.setVisibility(View.VISIBLE);
                    vh.ivFlag.setBackgroundResource(R.drawable.img_buyclub_recommend);
                } else {
                    vh.ivFlag.setVisibility(View.GONE);
                }
            }

            vh.nivIcon.setDefaultImageResId(R.drawable.img_event_lstdefault);
            vh.nivIcon.setErrorImageResId(R.drawable.img_event_lstdefault);

            String imageUrl;
            if (URL_GROUP_ICON_TEMPLATES.equals("")) {
                imageUrl = String.format(URL_GROUP_ICON_TEMPLATES_bak, tGroup.getGroupId() - 22000000);
            } else {
                imageUrl = String.format(URL_GROUP_ICON_TEMPLATES, tGroup.getGroupId() - 22000000);
            }

            ImageLoader imageLoader = VolleyHelper.getInstance(getContext()).getImageLoader();

            vh.nivIcon.setTag("url");
            vh.nivIcon.setImageUrl(imageUrl, imageLoader);

            final int index = position;
            vh.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoGroupPage(index);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;

            public NetworkImageView nivIcon;
            public TextView tvMotifName, tvTotalZdf, tvDayZdf, tvCreateTime, tvFocus;
            public ImageView ivFlag;

            public ViewHolder(View convertView) {
                layout = convertView;
                nivIcon = (NetworkImageView) convertView.findViewById(R.id.buyclub_home_lvitem_niv_image);
                tvMotifName = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_groupname);
                tvTotalZdf = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_totalZDF);
                tvDayZdf = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_dayZDF);
                tvCreateTime = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_createTime);
                tvFocus = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_focus);
                ivFlag = (ImageView) convertView.findViewById(R.id.buyclub_home_lvitem_iv_flag);
            }
        }

    }

}
