package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import cn.emoney.acg.util.Util;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class BuyClubHome extends PageImpl implements BuyClubField {
    private final int CHECK_MINE_BACKGROUND = 101;

    private static String URL_GROUP_ICON_TEMPLATES = "";
    private static String URL_GROUP_ICON_TEMPLATES_bak = "http://static.emoney.cn/webupload/motif/mobile/group%d/icon.png";

    private final int CLASS_TYPE_NORMAL = 0;// 0:请求正常排序列表，今日之星或最高收益
    private final int CLASS_TYPE_MINE = 1;// 1:请求"我的"，我的组合
    private final int CLASS_TYPE_RECOMMEND = 2;// 2:请求推荐
    private final int CLASS_TYPE_CUSTOM = 3;// 3:请求定制数据
    private final int CLASS_TYPE_MASTER = 4;// 3:请求大师数据，大师

    public static final String KEY_GROUP_TOTAL_INCOME = "key_total_income";
    public static final String KEY_GROUP_DAY_INCOME = "key_day_income";
    public static final String KEY_GROUP_MONTH_INCOME = "key_month_income";
    public static final String KEY_GROUP_NAME = "key_group_name";
    public static final String KEY_GROUP_CREATE_TIME = "key_create_time";
    public static final String KEY_GROUP_FOCUS = "key_focus";
    public static final String KEY_GROUP_STATE = "key_state";
    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_GROUP_IDEA = "key_group_idea";

    public static final String KEY_BUY_GROUP_DATA_INDEX = "key_buy_group_data_index";

    private final int COUNT_GROUP_ONEPAGE = 10;
    private final int COUNT_GROUP_MAX = 80;

    private int[] dataSize = new int[4];// 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合

    private RefreshListView listView = null;
    private GroupListAdapter mAdapter = null;

    private ProgressBar mPbLvEmpty = null;
    private TextView mTvLvEmptyNotice = null;

    /**
     * 0:从头请求; 1:加载更多
     */
    private int mRequestType = 0;

    private int mClassType = CLASS_TYPE_MASTER;
    private int mSortField = FIELD_TOTAL_INCOME;
    private int mBegin = 0;
    private boolean mSortOrder = true;

    private LinearLayout mLlListFooter = null;
    private boolean mIsLoadFinish = true;
    private boolean mHasMore = true;

    public static boolean isNeedUpdateMineType = true;
    /**
     * resume只请求一次
     */
    private boolean bResumeRequested_Flag = false;

    private static final String[] NOTICE_TAG = {"以下是大师创建的组合", "按总收益排名列表", "按今日收益排名列表", "以下是与我相关的组合"};
    private static final int[] RADIO_BTN_RESID = {R.id.buyclub_header_type1, R.id.buyclub_header_type2, R.id.buyclub_header_type3, R.id.buyclub_header_type4};

    private RadioGroup mRadioGroupHeader = null;
    private TextView mTypeTag = null;


    @Override
    protected void initPage() {
        setContentView(R.layout.page_buyclubhome);

        mPbLvEmpty = (ProgressBar) findViewById(R.id.progressBar);
        mTvLvEmptyNotice = (TextView) findViewById(R.id.progressNotice);

        mRadioGroupHeader = (RadioGroup) findViewById(R.id.rg_buyclub_home_head_content);

        mTypeTag = (TextView) findViewById(R.id.tv_head_tag);

        findViewById(R.id.buyclub_header_type4).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isLogined()) {
                    showTip("请先登录");
                    return true;
                }
                return false;
            }
        });

        mRadioGroupHeader.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                doTypeChecked(checkedId);
            }
        });

        listView = (RefreshListView) findViewById(R.id.buyclub_home_listview);
        mLlListFooter = (LinearLayout) View.inflate(getContext(), R.layout.page_rankbk_list_loadmore, null);

        if (listView != null) {
            // 下拉刷新头
            listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
            listView.initWithHeader(R.layout.layout_listview_header);
            listView.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRequestType = 0;
                    requestBuyClubList(0, mClassType, mSortField, mSortOrder, 0, getDataSize(mClassType, mSortField));
                }

                @Override
                public void beforeRefresh() {}

                @Override
                public void afterRefresh() {}
            });

            listView.addFooterView(mLlListFooter);
            mAdapter = new GroupListAdapter();
            // 设置数据
            listView.setAdapter(mAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 加了headView所以要-1
                    gotoGroupPage(position - 1);
                }
            });

            listView.removeFooterView(mLlListFooter);
            listView.setPostScrollListener(new PostScrollListener() {
                @Override
                public void postScrollStateChanged(AbsListView view, int scrollState) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void postScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // TODO Auto-generated method stub
                    int lastItemid = listView.getLastVisiblePosition(); // 获取当前屏幕最后Item的ID
                    if (mClassType != CLASS_TYPE_MINE && totalItemCount > 1 && totalItemCount < COUNT_GROUP_MAX && (lastItemid + 1) == totalItemCount && mIsLoadFinish == true && mHasMore) { // 达到数据的最后一条记录且小于最大条数
                        mIsLoadFinish = false;
                        listView.addFooterView(mLlListFooter);

                        mRequestType = 1;
                        int t_sortField = mSortField;
                        requestBuyClubList(0, mClassType, t_sortField, mSortOrder, mBegin, COUNT_GROUP_ONEPAGE);
                    }
                }
            });
        }

        View loadingView = findViewById(R.id.loadingLayout);
        listView.setEmptyView(loadingView);

        bindPageTitleBar(R.id.page_buyclubthome_titlebar);
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (!bResumeRequested_Flag) {
            bResumeRequested_Flag = true;
            mRequestType = 0;
            requestBuyClubList(0, CLASS_TYPE_MASTER, FIELD_TOTAL_INCOME, true, 0, COUNT_GROUP_ONEPAGE);
        }

        if (isNeedUpdateMineType) {
            isNeedUpdateMineType = false;
            requestBuyClubList(CHECK_MINE_BACKGROUND, CLASS_TYPE_MINE, -9999, false, 0, 20);
        }

    }

    private void doTypeChecked(int layoutId) {
        switch (layoutId) {
            case R.id.buyclub_header_type1:
                if (mTypeTag != null) {
                    mTypeTag.setText(NOTICE_TAG[0]);
                }
                mClassType = CLASS_TYPE_MASTER;
                mSortField = FIELD_TOTAL_INCOME;
                break;
            case R.id.buyclub_header_type2:
                if (mTypeTag != null) {
                    mTypeTag.setText(NOTICE_TAG[1]);
                }
                mClassType = CLASS_TYPE_NORMAL;
                mSortField = FIELD_TOTAL_INCOME;
                break;
            case R.id.buyclub_header_type3:
                if (mTypeTag != null) {
                    mTypeTag.setText(NOTICE_TAG[2]);

                }
                mClassType = CLASS_TYPE_NORMAL;
                mSortField = FIELD_DAY_INCOME;
                break;
            case R.id.buyclub_header_type4:
                if (!isLogined()) {
                    showTip("请先登录");
                    return;
                }

                if (mTypeTag != null) {
                    mTypeTag.setText(NOTICE_TAG[3]);
                }

                mClassType = CLASS_TYPE_MINE;
                mSortField = -9999;
                break;
            default:
                break;
        }


        mSortOrder = true;
        mRequestType = 0;

        // 移除footerview
        if (listView != null) {
            listView.removeFooterView(mLlListFooter);
        }

        BuyClubModule.getInstance().clearGroupData();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        requestBuyClubList(0, mClassType, mSortField, mSortOrder, 0, COUNT_GROUP_ONEPAGE);
    }

    /**
     * 
     * @param reqFlag 标识请求,标识http, CHECK_MINE_BACKGROUND表示预请求用户关系
     * @param classType 0:请求正常排序列表 1:请求"我的" 2:请求推荐 3:请求定制数据
     * @param sortField BuyClubField
     * @param sortOrder false:升序 true:降序
     * @param begin 分段请求,起始位置
     * @param size 分段请求大小
     */
    private void requestBuyClubList(int reqFlag, int classType, int sortField, boolean sortOrder, int begin, int size) {
        if (mRequestType == 0 && BuyClubModule.getInstance().getGroupDataSize() == 0) {
            if (mPbLvEmpty != null) {
                mPbLvEmpty.setVisibility(View.VISIBLE);
            }
            if (mTvLvEmptyNotice != null) {
                mTvLvEmptyNotice.setVisibility(View.VISIBLE);
                mTvLvEmptyNotice.setText("正在加载...");
            }
        }

        ArrayList<Integer> buyclubFiled = new ArrayList<Integer>();
        if (reqFlag == CHECK_MINE_BACKGROUND) {
            buyclubFiled.add(FIELD_MINE_TYPE);
        } else {
            buyclubFiled.add(FIELD_NAME);
            buyclubFiled.add(FIELD_CREATOR);
            buyclubFiled.add(FIELD_DAY_INCOME);
            buyclubFiled.add(FIELD_WEEK_INCOME);
            buyclubFiled.add(FIELD_MONTH_INCOME);
            buyclubFiled.add(FIELD_TOTAL_INCOME);
            buyclubFiled.add(FIELD_CREATE_TIME);
            buyclubFiled.add(FIELD_PRAISE);
            buyclubFiled.add(FIELD_FOCUS);

            if (classType == CLASS_TYPE_MINE) {
                buyclubFiled.add(FIELD_MINE_TYPE);
            }
        }

        String token = DataModule.getInstance().getUserInfo().getToken();
        LogUtil.easylog("sky", "BuyClubHome->Request->token:" + token);

        BuyClubListPackage pkg = new BuyClubListPackage(new QuoteHead((short) reqFlag));
        pkg.setRequest(BuyClubList_Request.newBuilder().setReqFlag(reqFlag).setClassType(classType).addAllReqFields(buyclubFiled).setSortField(sortField).setSortOrder(sortOrder).setReqBegin(begin).setReqSize(size).build());

        LogUtil.easylog("sky", "requestBuyClubList->sortField:" + sortField + ",  sortOrder:" + sortOrder + " begin:" + begin);

        requestQuote(pkg, IDUtils.ID_GROUP_LIST);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        // 更新时间
        // getDBHelper().setString(DataModule.G_KEY_BUYCLUB_FRESH_TIME,
        // DateUtils.getCurrentQuoteDate());

        BuyClubListPackage groupLst_pkg = (BuyClubListPackage) pkg;
        BuyClubList_Reply bcl_reply = groupLst_pkg.getResponse();

        if (bcl_reply != null) {
            String tUrlPre = bcl_reply.getIconUrlPre();
            LogUtil.easylog("tUrlPre:" + tUrlPre);
            if (tUrlPre != null && !tUrlPre.equals("") && URL_GROUP_ICON_TEMPLATES.equals("")) {
                URL_GROUP_ICON_TEMPLATES = tUrlPre;
            }
        }

        int iReqFlag = groupLst_pkg.getDataHead().getDataType();
        if (iReqFlag == CHECK_MINE_BACKGROUND) {
            if (bcl_reply == null) {
                return;
            }
            int tClassType = bcl_reply.getClassType();
            if (tClassType != CLASS_TYPE_MINE) {
                return;
            }
            List<GroupData> lstGroup = bcl_reply.getGroupListList();
            if (lstGroup == null || lstGroup.size() == 0) {
                return;
            }

            List<Integer> fieldIds = bcl_reply.getRepFieldsList();
            int index_mintype = fieldIds.indexOf(FIELD_MINE_TYPE);
            if (index_mintype < 0) {
                return;
            }

            for (int i = 0; i < lstGroup.size(); i++) {
                GroupData data = lstGroup.get(i);
                int groupId = data.getGroupId();
                List<String> lstFields = data.getRepFieldValueList();
                if (index_mintype != -1) {
                    try {
                        int iMinType = Integer.valueOf(lstFields.get(index_mintype));
                        LogUtil.easylog("sky", "MineGroup->groupId:" + groupId + ", type:" + iMinType);
                        MineGroupModule.getInstance().addMineType(groupId, iMinType);
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            // 刷新结束
            if (listView != null) {
                listView.onRefreshFinished();
            }

            mIsLoadFinish = true;
            if (listView != null && mLlListFooter != null) {
                listView.removeFooterView(mLlListFooter);
            }

            if (bcl_reply == null) {
                mHasMore = false;

                if (mPbLvEmpty != null) {
                    mPbLvEmpty.setVisibility(View.GONE);
                }
                if (mTvLvEmptyNotice != null) {
                    mTvLvEmptyNotice.setVisibility(View.GONE);
                }

                return;
            }

            List<GroupData> lstGroup = bcl_reply.getGroupListList();
            if (lstGroup == null || lstGroup.size() == 0) {
                mHasMore = false;
                if (BuyClubModule.getInstance().getGroupDataSize() == 0) {
                    if (mPbLvEmpty != null) {
                        mPbLvEmpty.setVisibility(View.GONE);
                    }
                    if (mTvLvEmptyNotice != null) {
                        mTvLvEmptyNotice.setVisibility(View.VISIBLE);
                        mTvLvEmptyNotice.setText("没有数据");
                    }
                }
                return;
            }

            if (mPbLvEmpty != null) {
                mPbLvEmpty.setVisibility(View.GONE);
            }
            if (mTvLvEmptyNotice != null) {
                mTvLvEmptyNotice.setVisibility(View.GONE);
            }

            int tClassType = bcl_reply.getClassType();
            int tSortField = bcl_reply.getSortField();

            // 更新数据数量
            udpateDataSize(tClassType, tSortField, bcl_reply.getRepBegin() + lstGroup.size());

            if (bcl_reply.getRepBegin() + lstGroup.size() >= bcl_reply.getTotalSize()) {
                mHasMore = false;
            } else {
                mHasMore = true;
            }

            List<Integer> fieldIds = bcl_reply.getRepFieldsList();

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
            if (tClassType == CLASS_TYPE_MINE) {
                index_mintype = fieldIds.indexOf(FIELD_MINE_TYPE);
            }

            if (mRequestType == 0) {
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

                if (tClassType == CLASS_TYPE_MINE && index_mintype != -1) {
                    int iMinType = Integer.valueOf(lstFields.get(index_mintype));
                    MineGroupModule.getInstance().addMineType(groupId, iMinType);
                }
                // 3232|落袋为安
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

            mBegin = BuyClubModule.getInstance().getGroupDataSize();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            if (mRequestType == 0) {
                listView.setSelection(0);
            }
        }


    }

    @Override
    protected void updateWhenDecodeError() {
        super.updateWhenDecodeError();
        if (listView != null) {
            listView.removeFooterView(mLlListFooter);
        }
    }

    @Override
    protected void updateWhenNetworkError() {
        super.updateWhenNetworkError();
        if (listView != null) {
            listView.removeFooterView(mLlListFooter);
        }

        if (mPbLvEmpty != null) {
            mPbLvEmpty.setVisibility(View.GONE);
        }

        if (mTvLvEmptyNotice != null) {
            mTvLvEmptyNotice.setText(Util.getResourcesString(R.string.no_data_msg));
        }
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
            SearchPage.gotoSearch(BuyClubHome.this);
        }
    }

    class GroupListAdapter extends BaseAdapter {

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
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.layout_buyclub_listitem, null);
                NetworkImageView nivImage = (NetworkImageView) convertView.findViewById(R.id.buyclub_home_lvitem_niv_image);
                TextView tvGroupName = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_groupname);
                TextView tvTypeTotal = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_totalType);
                TextView tvTotalZDF = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_totalZDF);
                TextView tvTypeDay = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_dayType);
                TextView tvDayZDF = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_dayZDF);
                TextView tvCreateTime = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_createTime);
                TextView tvFocus = (TextView) convertView.findViewById(R.id.buyclub_home_lvitem_tv_focus);
                ImageView ivRecommondFlag = (ImageView) convertView.findViewById(R.id.buyclub_home_lvitem_iv_flag);

                ListCell lc = new ListCell(nivImage, tvGroupName, tvTypeTotal, tvTotalZDF, tvTypeDay, tvDayZDF, tvCreateTime, tvFocus, ivRecommondFlag);

                convertView.setTag(lc);
            }

            ListCell lc = (ListCell) convertView.getTag();
            BuyGroupData tGroup = getItem(position);

            String tTotalZDF = (String) tGroup.getTotalZDF();
            String tDayZDF = (String) tGroup.getDayZDF();
            String tGroupName = (String) tGroup.getGroupName();
            String tCreateTime = (String) tGroup.getCreateTime(); // x天前
            String tFocus = (String) tGroup.getStrFocus();
            int tRecommondFlag = tGroup.getGroupState();

            lc.getTvGroupName().setText(tGroupName);

            lc.getTvTotalZDF().setText(tTotalZDF);
            int tColor = getZDPColor(FontUtils.getColorByZDF_percent(tTotalZDF));
            lc.getTvTotalZDF().setTextColor(tColor);

            lc.getTvDayZDF().setText(tDayZDF);
            tColor = getZDPColor(FontUtils.getColorByZDF_percent(tDayZDF));
            lc.getTvDayZDF().setTextColor(tColor);

            lc.getTvCreateTime().setText(tCreateTime);

            int t_groupType = MineGroupModule.getInstance().getMineType(tGroup.getGroupId());
            if (t_groupType == MineGroupModule.MINE_TYPE_BUY) {
                lc.getTvFocus().setVisibility(View.INVISIBLE);
                lc.getIvRecommondFlag().setVisibility(View.VISIBLE);
                lc.getIvRecommondFlag().setBackgroundResource(R.drawable.img_buyclub_gold);
            } else {
                lc.getTvFocus().setVisibility(View.VISIBLE);
                lc.getTvFocus().setText(String.format("%s人关注", tFocus));

                if (tRecommondFlag == 1) {
                    lc.getIvRecommondFlag().setVisibility(View.VISIBLE);
                    lc.getIvRecommondFlag().setBackgroundResource(R.drawable.img_buyclub_recommend);
                } else {
                    lc.getIvRecommondFlag().setVisibility(View.GONE);
                }
            }

            /***************/
            lc.getNivIamge().setDefaultImageResId(R.drawable.img_event_lstdefault);
            lc.getNivIamge().setErrorImageResId(R.drawable.img_event_lstdefault);

            String imageUrl;
            if (URL_GROUP_ICON_TEMPLATES.equals("")) {
                imageUrl = String.format(URL_GROUP_ICON_TEMPLATES_bak, tGroup.getGroupId() - 22000000);
            } else {
                imageUrl = String.format(URL_GROUP_ICON_TEMPLATES, tGroup.getGroupId() - 22000000);
            }

            ImageLoader imageLoader = VolleyHelper.getInstance(getContext()).getImageLoader();

            lc.getNivIamge().setTag("url");
            lc.getNivIamge().setImageUrl(imageUrl, imageLoader);

            return convertView;
        }

        private class ListCell {
            public ListCell(NetworkImageView nivImage, TextView tvGroupName, TextView tvTypeTotal, TextView tvTotalZDF, TextView tvTypeDay, TextView tvDayZDF, TextView tvCreateTime, TextView tvFocus, ImageView ivRecommondFlag) {
                mNivIamge = nivImage;
                mTvGroupName = tvGroupName;
                mTvTypeTotal = tvTypeTotal;
                mTvTotalZDF = tvTotalZDF;
                mTvTypeDay = tvTypeDay;
                mTvDayZDF = tvDayZDF;
                mTvCreateTime = tvCreateTime;
                mTvFocus = tvFocus;
                mIvRecommondFlag = ivRecommondFlag;

            }

            private NetworkImageView mNivIamge;
            private TextView mTvGroupName;
            private TextView mTvTypeTotal;
            private TextView mTvTotalZDF;
            private TextView mTvTypeDay;
            private TextView mTvDayZDF;
            private TextView mTvCreateTime;
            private TextView mTvFocus;
            private ImageView mIvRecommondFlag;

            public NetworkImageView getNivIamge() {
                return mNivIamge;
            }

            public TextView getTvGroupName() {
                return mTvGroupName;
            }

            public TextView getTvTypeTotal() {
                return mTvTypeTotal;
            }

            public TextView getTvTotalZDF() {
                return mTvTotalZDF;
            }

            public TextView getTvTypeDay() {
                return mTvTypeDay;
            }

            public TextView getTvDayZDF() {
                return mTvDayZDF;
            }

            public TextView getTvCreateTime() {
                return mTvCreateTime;
            }

            public TextView getTvFocus() {
                return mTvFocus;
            }

            public ImageView getIvRecommondFlag() {
                return mIvRecommondFlag;
            }
        }
    }

    @Override
    public void showProgress() {

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
            requestBuyClubList(CHECK_MINE_BACKGROUND, CLASS_TYPE_MINE, -9999, false, 0, 20);
            mRequestType = 0;
            requestBuyClubList(0, mClassType, mSortField, mSortOrder, 0, COUNT_GROUP_ONEPAGE);
        }
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

    /**
     * 跳转到组合创建页面
     * */
    private void gotoGroupCreatePage() {
        PageIntent intent = new PageIntent(this, CreateGroupPage.class);

        // Bundle extras = new Bundle();
        // extras.putInt(MoreNewsPage.EXTRA_KEY_GOODS_ID, currentGoodsId);
        // extras.putInt(MoreNewsPage.EXTRA_KEY_NEWS_TYPE, currentInfoType);
        // extras.putString(MoreNewsPage.EXTRA_KEY_GOODS_NAME, currentGoods.getGoodsName());
        // intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    // 更新数量
    private void udpateDataSize(int type, int sortType, int size) {
        // 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合
        switch (type) {
            case CLASS_TYPE_NORMAL:
                // 今日之星或最高收益
                if (sortType == FIELD_TOTAL_INCOME) {
                    // 总收益
                    dataSize[1] = size;
                } else {
                    // 今日之星
                    dataSize[2] = size;
                }
                break;
            case CLASS_TYPE_MINE:
                // 我的组合
                dataSize[3] = size;
                break;
            case CLASS_TYPE_MASTER:
                // 大师组合
                dataSize[0] = size;
                break;
        }
    }

    // 获取数量
    private int getDataSize(int type, int sortType) {
        // 表示请求到数据的总数量，分别为：大师组合、最高收益、今日之星、我的组合
        int size = COUNT_GROUP_ONEPAGE;
        switch (type) {
            case CLASS_TYPE_NORMAL:
                // 今日之星或最高收益
                if (sortType == FIELD_TOTAL_INCOME) {
                    // 总收益
                    size = dataSize[1];
                } else {
                    // 今日之星
                    size = dataSize[2];
                }
                break;
            case CLASS_TYPE_MINE:
                // 我的组合
                size = dataSize[3];
                break;
            case CLASS_TYPE_MASTER:
                // 大师组合
                size = dataSize[0];
                break;
        }

        return size;
    }
}
