package cn.emoney.acg.page.motif;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.motif.GroupTrendPackage;
import cn.emoney.acg.data.protocol.motif.GroupTrendReply.GroupTrend_Reply;
import cn.emoney.acg.data.protocol.motif.GroupTrendReply.GroupTrend_Reply.TrendData;
import cn.emoney.acg.data.protocol.motif.GroupTrendRequest.GroupTrend_Request;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.helper.FixPair;
import cn.emoney.acg.helper.FixedLengthList;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.view.PieChartView;
import cn.emoney.acg.widget.SegmentedGroup;
import cn.emoney.sky.libs.chart.ChartView;
import cn.emoney.sky.libs.chart.layers.GroupLayer;
import cn.emoney.sky.libs.chart.layers.LineLayer;
import cn.emoney.sky.libs.chart.layers.StackLayer;
import cn.emoney.sky.libs.chart.layers.XAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer;
import cn.emoney.sky.libs.chart.layers.YAxisLayer.OnFormatDataListener;
import cn.emoney.sky.libs.page.PageIntent;

public class GroupHeaderPage extends PageImpl {
    /**
     * 调仓纪录最大显示条数
     * */
    private static final int CHANGE_RECORD_ITEM_MAX_SIZE = 8;
    public static final int REQUEST_CODE_MODIFY_IDEA = 1111;

    private final int RESET_GROUPBK_LABLE_CONTENT = 300101;
    private static final String FORMAT_OTHER_ZDF = "日 %s   |   周 %s   |   月 %s";
    private static final String FORMAT_TRANSFER = "%s <font color=\"#e94b35\">→</font> %s";

    private static final int ITEM_TYPE_RELATIVE_STOCK = 1004;
    private static final int ITEM_TYPE_CHANGE_RECORD = 1005;

    private String[] GROUP_STOCK_HEADER = {"仓位", "股票名称", "最新价", "涨跌幅"};
    private String[] GROUP_TRANSFER_RECORD_HEADER = {"时间", "股票名称", "类型", "仓位变化"};

    private static final String MIN_WIDTH = "-10000%";

    private DecimalFormat mDf = new DecimalFormat("0.00");

    private int currentListItemType = ITEM_TYPE_RELATIVE_STOCK;
    private boolean isCurrentUserCreator = false; // 当前用户是否是买吧创建者
    private boolean isShowCreatorOnly; // 是否只显示创建人与自己的评论

    private FrameLayout mGroupInfo_TabContent = null;

    // main info
    private View mGroupHeaderContent = null;
    private TextView mTvTotalZDF = null;
    private TextView mTvOtherZDF = null;

    private ImageView mIvAddFocus = null;
    private float mBalance = 0; // 单位元

    /**
     * 持仓个股列表
     */
    private List<GroupStockGoods> listDataRelativeStocks = new ArrayList<GroupStockGoods>();

    private FixedLengthList<TransferRecord> mLstTransferRecords = new FixedLengthList<TransferRecord>(10);

    // trend
    private TextView mTvHighestZF = null;
    private ChartView mChartView = null;
    private YAxisLayer mAxisLineLayer = null;
    private LineLayer mHS300LineLayer = null;
    private LineLayer mGroupLineLayer = null;
    private XAxisLayer mXAxisLayer = null;
    private StackLayer mLineStackLayer = null;
    private int mFrameBoderColor = Color.GRAY;
    // private CheckBox cbCreatorOnly;

    // pie
    /**
     * 行业资金比重
     */
    private List<FixPair<String, Float>> mLstBkGravity = new ArrayList<FixPair<String, Float>>();
    private PieChartView mPieChartView = null;
    private LinearLayout mLlGroupBkLableContent = null;

    // 关联个股
    private ListView mLvGroupStock = null;
    private int mHeaderType_stock = 0;
    private TextView tvFocus; // 关注人数

    // 调仓记录
    private int mHeaderType_record = 0;
    private GroupStockAdapter mAdapter = null;
    // private SymbolSortHelper mSortHelper = null;
    private List<View> mLstGroupStockHeaderItems = new ArrayList<View>();

    // 组合id
    private int mGroupId = 0;
    private BuyGroupData mGroupData = null;

    /**
     * 
     [ [ 20150101 // 0 日期 YYYYMMDD 1234 // 1 沪深300收益率, 单位：万分之一，1234表示12.34% 1234 // 2 组合收益率 ], ...
     * ]
     */
    private List<List<Integer>> mLstGroupTrend = new ArrayList<List<Integer>>();
    private GroupPage.GroupPageListener mListener = null;
    private Handler mHandler = null;

    private ImageView mIvMenuItemPushPrompt;
    private TextView tvListEmptyView;
    private TextView mTvTotalGravity; // 总仓位
    private EditText etIdeaContent;
    private View layoutEditPosition; // 编辑买吧，点击进入买吧打理界面
    private ImageView imgCreatorOnly; // 是否只显示创建人与自己的评论
    private View layoutCreatorOnly; // 是否只显示创建人与自己的评论

    // 父容器传入group data
    public void setGroupId(BuyGroupData data) {
        mGroupData = data;
        mGroupId = mGroupData.getGroupId();

        refreshGroupMainInfo();
    }

    public void setGroupPageListener(GroupPage.GroupPageListener listener) {
        mListener = listener;
    }

    @Override
    protected void initPage() {
        setContentView(R.layout.page_group_header);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESET_GROUPBK_LABLE_CONTENT:
                        if (mLlGroupBkLableContent != null) {
                            LayoutParams p = mLlGroupBkLableContent.getLayoutParams();
                            p.width = msg.arg1;
                            mLlGroupBkLableContent.setLayoutParams(p);
                        }

                        break;
                    default:
                        break;
                }
            };
        };

        tvFocus = (TextView) findViewById(R.id.group_header_tv_focus);
        mGroupHeaderContent = findViewById(R.id.group_header_rl_info_content);
        mTvTotalZDF = (TextView) findViewById(R.id.group_header_tv_maininfo_totalzdf);
        mTvOtherZDF = (TextView) findViewById(R.id.group_header_tv_maininfo_otherzdf);
        layoutEditPosition = findViewById(R.id.page_groupheader_layout_edit_position);

        mIvAddFocus = (ImageView) findViewById(R.id.group_header_iv_maininfo_addfocus);
        mIvAddFocus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean bIsLogined = DataModule.getInstance().getUserInfo().isLogined();
                if (!bIsLogined) {
                    showTip("你还未登录,不能关注");
                    return;
                }

                try {
                    String tag = (String) mIvAddFocus.getTag();
                    if (tag != null) {
                        requestFoucs(Integer.valueOf(tag));
                    }
                } catch (Exception e) {
                    LogUtil.easylog("sky", e.toString());
                }
            }
        });

        SegmentedGroup segmentGroupInfos = (SegmentedGroup) findViewById(R.id.page_group_header_infos);
        segmentGroupInfos.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                deleteGroupInfoView();

                if (checkedId == R.id.page_group_header_info_shouyi) {
                    View view = createGroupTrendView();
                    if (view != null) {
                        mGroupInfo_TabContent.addView(view);
                        refreshGroupTrend();
                    }
                } else if (checkedId == R.id.page_group_header_info_hangye) {
                    View view = createGroupBKGravityView();
                    if (view != null) {
                        mGroupInfo_TabContent.addView(view);
                        refreshGroupBKGravity();
                    }
                } else if (checkedId == R.id.page_group_header_info_linian) {
                    View view = createGroupIdeaView();
                    if (view != null) {
                        mGroupInfo_TabContent.addView(view);
                    }
                }
            }
        });

        mIvMenuItemPushPrompt = (ImageView) findViewById(R.id.page_group_header_img_alert_change_record);
        SegmentedGroup segmentGroupItems = (SegmentedGroup) findViewById(R.id.page_group_header_items);
        segmentGroupItems.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.page_group_header_item_relative_stocks) {
                    currentListItemType = ITEM_TYPE_RELATIVE_STOCK;

                    int t_size = listDataRelativeStocks.size();
                    refreshGroupStockHeader();
                    resizeGroupStockLv(t_size);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    findViewById(R.id.page_group_header_layout_totalgravity).setVisibility(View.VISIBLE);

                    if (isCurrentUserCreator) {
                        // 如果当前用户是创建者，显示买吧打理入口
                        layoutEditPosition.setVisibility(View.VISIBLE);

                        tvListEmptyView.setText("当前仓位为0，请选择“编辑”建仓");
                    } else {
                        // 如果当前用户不是创建者，不显示买吧打理入口
                        layoutEditPosition.setVisibility(View.GONE);

                        // 如果当前用户不是创建者，ListView为空时，显示为暂无数据
                        tvListEmptyView.setText("暂无数据");
                    }

                    requestGroupInfo();
                } else if (checkedId == R.id.page_group_header_item_change_record) {
                    currentListItemType = ITEM_TYPE_CHANGE_RECORD;

                    int t_size = mLstTransferRecords.size();
                    refreshGroupStockHeader();
                    resizeGroupStockLv(t_size);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    findViewById(R.id.page_group_header_layout_totalgravity).setVisibility(View.GONE);

                    // 如果当前用户不是创建者，ListView为空时，显示为暂无数据
                    tvListEmptyView.setText("暂无数据");

                    requestTransferRecord(0);
                }
            }
        });

        mGroupInfo_TabContent = (FrameLayout) findViewById(R.id.group_header_fr_info_content);

        deleteGroupInfoView();

        View view = createGroupTrendView();
        if (view != null) {
            mGroupInfo_TabContent.addView(view);
        }

        mLstGroupStockHeaderItems.clear();
        for (int i = 1; i <= 4; i++) {
            View v = findViewById(getResIdByStr("id", "item_tv_group_stock_", i));
            mLstGroupStockHeaderItems.add(v);
        }

        View vHeaderSwitch = findViewById(R.id.item_fl_group_stock_4_content);
        vHeaderSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                    mHeaderType_stock = mHeaderType_stock >= 3 ? 0 : mHeaderType_stock + 1;
                } else if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
                    mHeaderType_record = mHeaderType_record >= 1 ? 0 : mHeaderType_record + 1;
                }

                refreshGroupRelative();
            }
        });

        mLvGroupStock = (ListView) findViewById(R.id.group_header_lv_groupstock);
        tvListEmptyView = (TextView) findViewById(R.id.group_header_tv_listEmptyView);
        View listFooter = View.inflate(getContext(), R.layout.page_groupheader_list_footer, null);
        listFooter.findViewById(R.id.page_group_header_listfooter_tv_more_record).setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                // 打开调仓记录界面
                gotoTransferRecord();
            }
        });

        if (mLvGroupStock != null) {
            mLvGroupStock.setEmptyView(tvListEmptyView);
            mLvGroupStock.addFooterView(listFooter);
            mAdapter = new GroupStockAdapter();
            mLvGroupStock.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

            mLvGroupStock.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                    int tGoodid = 0;
                    String tGoodName = "";
                    if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                        tGoodid = listDataRelativeStocks.get(index).getGoodsId();
                        tGoodName = listDataRelativeStocks.get(index).getGoodsName();
                    } else {
                        tGoodid = mLstTransferRecords.get(index).getGoodid();
                        tGoodName = mLstTransferRecords.get(index).getGoodname();
                    }

                    Goods tGoods = new Goods(tGoodid, tGoodName);
                    PageImpl pageImpl = (PageImpl) getParent();

                    QuoteJump.gotoQuote(pageImpl, tGoods);
                }
            });

        }

        mTvTotalGravity = (TextView) findViewById(R.id.group_header_groupstock_totalgravity);

        layoutEditPosition.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                // 点击打开编辑买吧持仓界面
                gotoEditMotifPositionPage();
            }
        });

        imgCreatorOnly = (ImageView) findViewById(R.id.page_groupheader_iv_creator_only);
        layoutCreatorOnly = findViewById(R.id.page_groupheader_layout_creator_only);
        layoutCreatorOnly.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowCreatorOnly = !isShowCreatorOnly;

                if (isShowCreatorOnly) {
                    imgCreatorOnly.setImageResource(R.drawable.img_check_checked);
                } else {
                    imgCreatorOnly.setImageResource(R.drawable.img_check_uncheck);
                }
                commentCheckedChangeListener.onCheckChanged(isShowCreatorOnly);
            }
        });

    }

    @Override
    protected void initData() {

    }

    protected void onPageResume() {
        super.onPageResume();

        if (mLstGroupTrend.size() == 0) {
            // requestGroupTrend();
            requestGroupTrend_pb();
        }

        if (!getIsAutoRefresh()) {
            if (getUserVisibleHint()) {
                startRequestTask();
            } else {
                requestData();
            }
        }

        if (mGroupData != null) {
            tvFocus.setText(mGroupData.getStrFocus() + "人关注");
        }

        // 判断当前用户是否是创建者
        String userId = DataModule.getInstance().getUserInfo().getUid();
        if (!TextUtils.isEmpty(userId)) {
            String creatorId = mGroupData.getCreatorId() + "";
            if (userId.equals(creatorId)) {
                isCurrentUserCreator = true;
            }
        }

        // 修改组合理成功后返回，更新组合理念
        if (etIdeaContent != null) {
            etIdeaContent.setText(mGroupData.getGroupIdea());
        }

        if (isCurrentUserCreator) {
            // 如果当前用户是创建者，显示买吧打理入口
            layoutEditPosition.setVisibility(View.VISIBLE);

            // 如果当前用户是创建者，ListView为空时，显示为“当前仓位为0，请选择编辑建仓”
            if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                tvListEmptyView.setText("当前仓位为0，请选择“编辑”建仓");
            } else {
                tvListEmptyView.setText("暂无数据");
            }
        } else {
            // 如果当前用户不是创建者，不显示买吧打理入口
            layoutEditPosition.setVisibility(View.GONE);

            // 如果当前用户不是创建者，ListView为空时，显示为暂无数据
            tvListEmptyView.setText("暂无数据");
        }

    }

    @Override
    protected void onPagePause() {
        super.onPagePause();
    }

    @Override
    protected void onPageDestroy() {
        super.onPageDestroy();
    }

    @Override
    public void requestData() {
        if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
            requestTransferRecord(1);
        }
        requestGroupInfo();
    }

    /**
     * 请求调仓记录
     * 
     * @param type 0:获取记录集; 1:检测是否有记录
     */
    private void requestTransferRecord(int type) {
        if (mGroupId == 0) {
            return;
        }
        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KEY_ID, 0);
            jsObj.put(KEY_CHECK, type);
            jsObj.put(KEY_GROUP_CODE, mGroupId);
            jsObj.put(KEY_SIZE, 10);
            jsObj.put(KEY_REFRESH, true);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_GROUP_CONTROL_HISTORY);
    }

    /**
     * 获取组合关联个股信息
     * */
    private void requestGroupInfo() {
        if (mGroupId == 0)
            return;

        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KEY_GROUP_CODE, mGroupId);
            jsObj.put(KEY_REFRESH, true);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LogUtil.easylog("sky", "requestGroupInfo:" + jsObj);
        requestInfo(jsObj, IDUtils.ID_GROUP_DETAIL_INFO);
    }

    private void requestGroupTrend_pb() {
        if (mGroupId == 0) {
            return;
        }

        GroupTrendPackage pkg = new GroupTrendPackage(new QuoteHead((short) 1));
        pkg.setRequest(GroupTrend_Request.newBuilder().setGroupCode(mGroupId).build());

        requestQuote(pkg, IDUtils.ID_GROUP_TREND);
    }

    private void requestStockInfo() {
        ArrayList<Integer> goodsIds = new ArrayList<Integer>();
        for (int i = 0; i < listDataRelativeStocks.size(); i++) {
            goodsIds.add(listDataRelativeStocks.get(i).getGoodsId());
        }

        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.CLOSE);
        goodsFiled.add(GoodsParams.ZXJ);
        goodsFiled.add(GoodsParams.ZDF);
        goodsFiled.add(GoodsParams.GROUP_HY);
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4).setGroupType(0).addAllGoodsId(goodsIds).addAllReqFields(goodsFiled)
        // -9999 代表不排序
                .setSortField(-9999).setSortOrder(false).setReqBegin(0).setReqSize(0).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }

    /**
     * 买吧关注 取消关注动作
     * 
     * @param type +1:关注, -1:取消关注
     */
    private void requestFoucs(int type) {
        String reqURLTemplates = "";
        if (type == 1) {
            reqURLTemplates = BuyClubHttpUrl.URL_ADD_FOCUS;
        } else if (type == -1) {
            reqURLTemplates = BuyClubHttpUrl.URL_CANCEL_FOCUS;
        }

        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(reqURLTemplates, token, mGroupId);
        JSONObject jObject = new JSONObject();
        jObject.put("url", reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE);
    }

    /**
     * 取消订单
     * */
    private void requestCancelOrder(int orderId) {
        if (orderId < 0)
            return;

        String token = DataModule.getInstance().getUserInfo().getToken();
        String code = mGroupData.getGroupId() + "";

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_CANCEL_ORDER, token, code, orderId);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_CANCEL_ORDER);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();
        if (id == IDUtils.ID_GROUP_TREND) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();
            LogUtil.easylog("sky", "GroupTrend->updateInfo:" + msgData);

            try {
                JSONObject jsObj = JSON.parseObject(msgData);

                String jsAry = jsObj.getString(KEY_LIST);
                @SuppressWarnings("unchecked")
                List<List<Integer>> t_lst = JSON.parseObject(jsAry, mLstGroupTrend.getClass());
                if (t_lst != null) {
                    mLstGroupTrend.clear();
                    mLstGroupTrend.addAll(t_lst);
                }

            } catch (Exception e) {
            }

            if (mLstGroupTrend.size() > 0) {
                refreshGroupTrend();
            }
        } else if (id == IDUtils.ID_GROUP_DETAIL_INFO) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();

            try {
                JSONObject objMsg = JSON.parseObject(msgData);

                if (objMsg != null && objMsg.containsKey("balance") && objMsg.containsKey("dayRate") && objMsg.containsKey("weekRate") && objMsg.containsKey("monthRate") && objMsg.containsKey("totalRate") && objMsg.containsKey("investment") && objMsg.containsKey("stockList")) {
                    mBalance = objMsg.getLongValue("balance") / 100f;
                    String sDayZDF = DataUtils.getSignedZDF(objMsg.getIntValue("dayRate"));
                    String sWeekZDF = DataUtils.getSignedZDF(objMsg.getIntValue("weekRate"));
                    String sMonthZDF = DataUtils.getSignedZDF(objMsg.getIntValue("monthRate"));
                    String mTotalZDF = DataUtils.getSignedZDF(objMsg.getIntValue("totalRate"));
                    String mIdea = objMsg.getString("investment");
                    JSONArray arrayStocks = objMsg.getJSONArray("stockList");

                    mGroupData.setDayZDF(sDayZDF);
                    mGroupData.setWeekZDF(sWeekZDF);
                    mGroupData.setMonthZDF(sMonthZDF);
                    mGroupData.setTotalZDF(mTotalZDF);
                    mGroupData.setGroupIdea(mIdea);
                    refreshGroupMainInfo();

                    if (arrayStocks != null && arrayStocks.size() > 0) {
                        listDataRelativeStocks.clear();

                        for (int i = 0; i < arrayStocks.size(); i++) {
                            JSONArray arrayStock = arrayStocks.getJSONArray(i);

                            if (arrayStock != null && arrayStock.size() > 4) {
                                String sGoodCode = Util.FormatStockCode(arrayStock.getIntValue(2));

                                ArrayList<Goods> listGoods = getSQLiteDBHelper().queryStockInfosByCode2(sGoodCode, 1);
                                if (listGoods != null && listGoods.size() > 0) {
                                    Goods goods = listGoods.get(0);
                                    GroupStockGoods groupStockGoods = new GroupStockGoods(goods.getGoodsId(), goods.getGoodsName());
                                    groupStockGoods.setTotalCostValue(arrayStock.getLongValue(0));
                                    groupStockGoods.setTotalGoodsNum(arrayStock.getLongValue(1));
                                    groupStockGoods.setPositionAmount(arrayStock.getLongValue(3) + "");
                                    groupStockGoods.setAddTime(arrayStock.getString(4));

                                    float costPrice = arrayStock.getFloatValue(0) / 100f / arrayStock.getLongValue(1);
                                    String t_sPrice = mDf.format(costPrice);
                                    groupStockGoods.setPositionPrice(t_sPrice);

                                    listDataRelativeStocks.add(groupStockGoods);
                                }
                            }

                        }
                    }

                }
            } catch (Exception e) {
            }

            if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                int t_size = listDataRelativeStocks.size();
                resizeGroupStockLv(t_size);
                if (t_size > 0) {
                    requestStockInfo();
                } else {
                    if (mTvTotalGravity != null) {
                        mTvTotalGravity.setText("0%");
                    }
                }
            }

        } else if (id == IDUtils.ID_GROUP_CONTROL_HISTORY) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null)
                return;

            String msgData = mc.getMsgData();
            LogUtil.easylog("sky", "GroupTransferHistory->updateInfo:" + msgData);

            try {
                JSONObject jsObj = JSON.parseObject(msgData);

                if (jsObj != null && jsObj.containsKey("check") && jsObj.containsKey("count") && jsObj.containsKey("records")) {
                    int newCount = jsObj.getIntValue("count");
                    int nCheck = jsObj.getIntValue("check");

                    if (nCheck == 1 && newCount > 0) {
                        if (mIvMenuItemPushPrompt != null) {
                            mIvMenuItemPushPrompt.setVisibility(View.VISIBLE);
                        }
                    } else if (nCheck == 0) {
                        mIvMenuItemPushPrompt.setVisibility(View.GONE);
                        if (newCount > 0) {
                            mLstTransferRecords.clear();
                            JSONArray jAryRecords = jsObj.getJSONArray("records");

                            if (jAryRecords != null && jAryRecords.size() > 0) {

                                for (int i = 0; i < jAryRecords.size(); i++) {
                                    if (mLstTransferRecords.size() >= CHANGE_RECORD_ITEM_MAX_SIZE) {
                                        break;
                                    }

                                    TransferRecord t_rRecord = new TransferRecord();
                                    JSONArray jsonItem = jAryRecords.getJSONArray(i);
                                    if (jsonItem == null)
                                        break;

                                    t_rRecord.setId(jsonItem.getIntValue(0));
                                    t_rRecord.setDate(jsonItem.getString(1));
                                    int t_goodid = jsonItem.getIntValue(2);

                                    ArrayList<Goods> lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(t_goodid), 1);
                                    if (lstGoods != null && lstGoods.size() > 0) {
                                        Goods g = lstGoods.get(0);
                                        t_rRecord.setGoodid(g.getGoodsId());
                                        t_rRecord.setGoodname(g.getGoodsName());
                                    } else {
                                        t_rRecord.setGoodid(t_goodid);
                                    }

                                    String t_bidPrice = DataUtils.getPrice(jsonItem.getLong(3) * 10);
                                    t_rRecord.setBidPrice(t_bidPrice);
                                    t_rRecord.setPosSrc(jsonItem.getIntValue(4));
                                    t_rRecord.setPosDst(jsonItem.getIntValue(5));
                                    t_rRecord.setTradeType(jsonItem.getIntValue(6));
                                    t_rRecord.setReason(jsonItem.getString(7));

                                    int transferStatus = TransferRecordPage.TRANSFER_STATUS_SUCCESS;

                                    if (jsonItem.size() >= 9) {
                                        int status = jsonItem.getIntValue(8);

                                        if (status == 0 || status == 2) {
                                            transferStatus = TransferRecordPage.TRANSFER_STATUS_DOING;
                                        } else if (status == -1) {
                                            transferStatus = TransferRecordPage.TRANSFER_STATUS_FAIL;
                                        } else if (status == -2) {
                                            transferStatus = TransferRecordPage.TRANSFER_STATUS_CANCELED;
                                        }
                                    }

                                    t_rRecord.setTransferStatus(transferStatus);

                                    mLstTransferRecords.add(t_rRecord);
                                }

                            }

                        }

                    }
                    LogUtil.easylog("sky", "newCount = " + newCount);
                }

            } catch (Exception e) {
            }

            if (mLstTransferRecords.size() > 0) {
                int mTransferId = mLstTransferRecords.get(0).getId();

                try {
                    String t_sTransfer = getDBHelper().getString(DataModule.G_KEY_GROUP_TRANSFER_RECORD, "{}");
                    JSONObject jObjTransfer = JSONObject.parseObject(t_sTransfer);
                    jObjTransfer.put(mGroupId + "", mTransferId);
                    getDBHelper().setString(DataModule.G_KEY_GROUP_TRANSFER_RECORD, jObjTransfer.toJSONString());
                } catch (Exception e) {
                }
            }

            int t_size = mLstTransferRecords.size();
            if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
                resizeGroupStockLv(t_size);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == IDUtils.ID_GROUP_HTTP_INTERFACE) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();
            LogUtil.easylog("sky", "GroupFocus->updateInfo:" + msgData);
            // {"body":"{\"retcode\":1,\"retmsg\":\"关注组合成功\",\"followcount\":2}","errorCode":0}

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int retServerCode = jsObj.getIntValue("errorCode");
                if (retServerCode == 0) {
                    String body = jsObj.getString("body");
                    if (body != null && !body.equals("")) {
                        JSONObject jObjBody = JSONObject.parseObject(body);
                        int retWebCode = jObjBody.getIntValue("retcode");
                        if (retWebCode == 1) {
                            int controlType = jObjBody.getIntValue("type");
                            if (controlType == 1) {
                                MineGroupModule.getInstance().addMineType(mGroupId, MineGroupModule.MINE_TYPE_FOCUS);
                            } else if (controlType == -1) {
                                MineGroupModule.getInstance().delMineType(mGroupId);
                            }
                            showTip(jObjBody.getString("retmsg"));
                            refreshGroupMainInfo();
                        } else {
                            showTip("操作失败");
                        }
                    } else {
                        showTip("操作失败");
                    }
                } else {
                    showTip("操作失败");
                }
            } catch (Exception e) {
                showTip("操作失败");
            }
        } else if (id == BuyClubHttpUrl.FLAG_GROUP_CANCEL_ORDER) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject objReturn = JSONObject.parseObject(msgData);
                if (objReturn != null && objReturn.containsKey("errorCode") && objReturn.containsKey("body")) {
                    int errorCode = objReturn.getIntValue("errorCode");
                    if (errorCode == 0) {
                        String body = objReturn.getString("body");
                        JSONObject objBody = JSONObject.parseObject(body);

                        if (objBody != null && objBody.containsKey("retcode")) {
                            int retCode = objBody.getIntValue("retcode");

                            if (retCode == 0) {
                                // 撤单成功
                                // 重新请求调仓记录，使用返回数据刷新界面
                                requestTransferRecord(0);
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

            showTip("撤单失败");
        }

    }

    private void resizeGroupStockLv(int size) {
        if (mLvGroupStock != null && mAdapter != null) {
            int oneHeigth = FontUtils.dip2px(getContext(), 56);
            LayoutParams params = mLvGroupStock.getLayoutParams();
            int tH = (oneHeigth + 1) * size;

            // 如果是调仓记录，且条数是8，就把ListView高度加上footer高度
            if (currentListItemType == ITEM_TYPE_CHANGE_RECORD && mLstTransferRecords.size() == CHANGE_RECORD_ITEM_MAX_SIZE) {
                int footerHeight = FontUtils.dip2px(getContext(), 48);
                tH += footerHeight;
            }

            if (tH == 0) {
                tH = FontUtils.dip2px(getContext(), 150);
            }
            params.height = tH;
            mLvGroupStock.setLayoutParams(params);

        }
    }

    /**
     * 打开调仓记录界面
     * */
    private void gotoTransferRecord() {
        PageIntent intent = new PageIntent(this, TransferRecordPage.class);

        Bundle extras = new Bundle();
        extras.putInt(TransferRecordPage.EXTRA_KEY_GROUP_ID, mGroupId);
        intent.setSupportAnimation(true);
        intent.setArguments(extras);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    /**
     * 打开编辑买吧持仓界面
     * */
    private void gotoEditMotifPositionPage() {
        PageIntent intent = new PageIntent(this, EditMotifPositionPage.class);

        Bundle extras = new Bundle();
        extras.putInt(EditMotifPositionPage.EXTRA_KEY_GROUP_ID, mGroupId);
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    /**
     * 打开编辑投资理念界面
     * */
    private void gotoModifyModifIdeaPage(String idea) {
        PageIntent intent = new PageIntent(this, ModifyMotifIdeaPage.class);

        Bundle extras = new Bundle();
        extras.putInt(ModifyMotifIdeaPage.EXTRA_KEY_GROUP_ID, mGroupId);
        extras.putString(ModifyMotifIdeaPage.EXTRA_KEY_GROUP_IDEA, idea);
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPageForResult(DataModule.G_CURRENT_FRAME, intent, REQUEST_CODE_MODIFY_IDEA);
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        int iType = pkg.getRequestType();
        if (iType == 0) {
            // 动态行情
            if (pkg instanceof DynaValueDataPackage) {
                DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
                DynaValueData_Reply gr = goodsTable.getResponse();

                if (gr == null || gr.getRepFieldsList().size() == 0 || gr.getQuotaValueList().size() == 0) {
                    return;
                }

                int indexCLOSE = gr.getRepFieldsList().indexOf(GoodsParams.CLOSE);
                int indexPrice = gr.getRepFieldsList().indexOf(GoodsParams.ZXJ);
                int indexZDF = gr.getRepFieldsList().indexOf(GoodsParams.ZDF);
                int indexHY = gr.getRepFieldsList().indexOf(GoodsParams.GROUP_HY);
                List<DynaQuota> lstQuote = gr.getQuotaValueList();
                for (int i = 0; i < lstQuote.size(); i++) {
                    DynaQuota quote = lstQuote.get(i);
                    int goodid = quote.getGoodsId();
                    String strlastDayClosePrice = quote.getRepFieldValue(indexCLOSE);
                    String slastDayClosePrice = DataUtils.getPrice(strlastDayClosePrice);

                    String strPrice = quote.getRepFieldValue(indexPrice);
                    String price = DataUtils.getPrice(strPrice);

                    String strZdf = quote.getRepFieldValue(indexZDF);
                    String zdf = DataUtils.getSignedZDF(strZdf);

                    String hyid = quote.getRepFieldValue(indexHY);

                    for (int j = 0; j < listDataRelativeStocks.size(); j++) {
                        GroupStockGoods groupGoods = listDataRelativeStocks.get(j);
                        if (groupGoods.getGoodsId() == goodid) {

                            groupGoods.setLastClose(slastDayClosePrice);
                            groupGoods.setZxj(price);
                            groupGoods.setZdf(zdf);
                            groupGoods.setBKId(Integer.valueOf(hyid));
                            ArrayList<Goods> t_lstGoodsBK = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(groupGoods.getBKId()), 1);
                            if (t_lstGoodsBK != null && t_lstGoodsBK.size() > 0) {
                                groupGoods.setBKName(t_lstGoodsBK.get(0).getGoodsName());
                            }

                            // 算成盈亏比
                            float costPrice = Float.valueOf(groupGoods.getPositionPrice());
                            float currentPrice = Float.valueOf(price);

                            float d_price = 0.0f;
                            if (costPrice > 0) {
                                float nowPrice = currentPrice;
                                if (nowPrice <= 0) {
                                    nowPrice = Float.valueOf(groupGoods.getLastClose());
                                }
                                d_price = nowPrice - costPrice;
                            }

                            float profitOrLossPercent = 0.0f;
                            if (costPrice > 0) {
                                profitOrLossPercent = d_price / costPrice * 10000;
                            }
                            String t_ykb = DataUtils.getSignedZDF(profitOrLossPercent);
                            groupGoods.setPositionProfitLossPercent(t_ykb);
                        }

                    }
                }

                List<FixPair<String, Float>> tLstGravity = BuyClubUtil.reCalcHYGravity(listDataRelativeStocks, mBalance);

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    refreshGroupTotalGravity();
                }

                if (tLstGravity != null && tLstGravity.size() > 0) {
                    mLstBkGravity.clear();
                    mLstBkGravity.addAll(tLstGravity);

                    refreshGroupBKGravity();
                }

            }
        } else if (iType == 1) {
            // 走势
            if (pkg instanceof GroupTrendPackage) {
                GroupTrendPackage gTrendPkg = (GroupTrendPackage) pkg;
                GroupTrend_Reply gTrend = gTrendPkg.getResponse();

                if (gTrend.hasHighestIncome()) {
                    int highestIncome = gTrend.getHighestIncome();
                    String highestZF = DataUtils.getZDF(highestIncome);
                    if (mTvHighestZF != null) {
                        mTvHighestZF.setText("历史最高:" + highestZF);
                    }

                }


                List<TrendData> tLstTrendData = gTrend.getTrendListList();

                if (tLstTrendData != null) {
                    mLstGroupTrend.clear();
                    for (int i = 0; i < tLstTrendData.size(); i++) {
                        TrendData oneTrendData = tLstTrendData.get(i);
                        List<Integer> tOnePoint = new ArrayList<Integer>(3);
                        tOnePoint.add(oneTrendData.getDate());
                        tOnePoint.add(oneTrendData.getHs300Investment());
                        tOnePoint.add(oneTrendData.getGroupInvestment());
                        mLstGroupTrend.add(tOnePoint);
                    }
                    if (mLstGroupTrend.size() > 0) {
                        refreshGroupTrend();
                    }
                }
            }
        }

    }

    private void refreshGroupMainInfo() {
        String tTotalZDF = mGroupData.getTotalZDF();
        if (!TextUtils.isEmpty(tTotalZDF)) {
            mTvTotalZDF.setText(tTotalZDF);
        }

        String tDayZDF = mGroupData.getDayZDF();
        String tWeekZDF = mGroupData.getWeekZDF();
        String tMonthZDF = mGroupData.getMonthZDF();

        String tOtherZDF = String.format(FORMAT_OTHER_ZDF, tDayZDF, tWeekZDF, tMonthZDF);
        if (mTvOtherZDF != null) {
            mTvOtherZDF.setText(tOtherZDF);
        }

        if (mListener != null) {
            mListener.refreshZDFUI();
        }

        if (mIvAddFocus != null) {
            int type = MineGroupModule.getInstance().getMineType(mGroupId);
            if (type == MineGroupModule.MINE_TYPE_CREATE) {
                mIvAddFocus.setImageResource(R.drawable.selector_btn_group_addfocus);
                mIvAddFocus.setEnabled(false);
                mIvAddFocus.setTag("0");
            } else if (type == MineGroupModule.MINE_TYPE_FOCUS) {
                mIvAddFocus.setImageResource(R.drawable.selector_btn_group_unfocus);
                mIvAddFocus.setEnabled(true);
                mIvAddFocus.setTag("-1");
            } else if (type == MineGroupModule.MINE_TYPE_BUY) {
                mIvAddFocus.setImageResource(R.drawable.img_buygroup_gold_flag);
                mIvAddFocus.setEnabled(false);
                mIvAddFocus.setTag("0");
            } else {
                mIvAddFocus.setImageResource(R.drawable.selector_btn_group_addfocus);
                mIvAddFocus.setEnabled(true);
                mIvAddFocus.setTag("1");
            }
        }

    }

    /**
     * 点击header 切换显示项目后调用
     */
    private void refreshGroupRelative() {
        if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
            if (mHeaderType_stock == 0) {
                GROUP_STOCK_HEADER[3] = "涨跌幅";
            } else if (mHeaderType_stock == 1) {
                GROUP_STOCK_HEADER[3] = "建仓时间";
            } else if (mHeaderType_stock == 2) {
                GROUP_STOCK_HEADER[3] = "成本价格";
            } else if (mHeaderType_stock == 3) {
                GROUP_STOCK_HEADER[3] = "浮云盈亏";
            }
        } else if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
            if (mHeaderType_record == 0) {
                GROUP_TRANSFER_RECORD_HEADER[3] = "仓位变化";
            } else if (mHeaderType_record == 1) {
                GROUP_TRANSFER_RECORD_HEADER[3] = "成交价";
            }
        }
        refreshGroupStockHeader();

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void refreshGroupTotalGravity() {
        if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
            float totalGravity = 0;
            for (int i = 0; i < listDataRelativeStocks.size(); i++) {
                String sGravity = listDataRelativeStocks.get(i).getGravity();
                totalGravity += DataUtils.convertToFloat(sGravity);
            }

            if (mTvTotalGravity != null) {
                mTvTotalGravity.setText(DataUtils.mDecimalFormat1_max.format(totalGravity * 100) + "%");
            }
        }
    }

    private void refreshGroupTrend() {
        if (mChartView != null && mGroupLineLayer != null && mHS300LineLayer != null && mLineStackLayer != null && mAxisLineLayer != null) {
            mGroupLineLayer.setMaxCount(mLstGroupTrend.size());
            mHS300LineLayer.setMaxCount(mLstGroupTrend.size());

            String t_startDate = null;
            String t_endDate = null;

            for (int i = 0; i < mLstGroupTrend.size(); i++) {
                List<Integer> onePoint = mLstGroupTrend.get(i);
                float fHS300Value = onePoint.get(1);
                float fGroupTrendValue = onePoint.get(2);
                mHS300LineLayer.addValue(fHS300Value);
                mGroupLineLayer.addValue(fGroupTrendValue);

                if (i == 0) {
                    t_startDate = onePoint.get(0) + "";
                }

                if (i == mLstGroupTrend.size() - 1) {
                    t_endDate = onePoint.get(0) + "";
                }
            }

            int dMinVal = 0;
            int dMaxVal = 0;
            float fAry_max_min[] = mLineStackLayer.calMinAndMaxValue();

            if (fAry_max_min != null) {
                int[] aryCoodinate = BuyClubUtil.regulateAll((int) fAry_max_min[0], (int) fAry_max_min[1]);
                if (aryCoodinate != null && aryCoodinate.length == 3) {
                    dMinVal = aryCoodinate[0];
                    dMaxVal = aryCoodinate[2];
                }

            }

            mHS300LineLayer.setMinValue(dMinVal);
            mHS300LineLayer.setMaxValue(dMaxVal);

            mGroupLineLayer.setMinValue(dMinVal);
            mGroupLineLayer.setMaxValue(dMaxVal);

            mAxisLineLayer.setMinValue(dMinVal);
            mAxisLineLayer.setMaxValue(dMaxVal);

            String startDate = DateUtils.formatTrendDate(t_startDate);
            String endDate = DateUtils.formatTrendDate(t_endDate);

            mXAxisLayer.clearValue();
            mXAxisLayer.addValue(startDate);
            mXAxisLayer.addValue(endDate);

            mChartView.forceAdjustLayers();
            mChartView.postInvalidate();
        }
    }

    private View createGroupTrendView() {

        View view = View.inflate(getContext(), R.layout.layout_group_trend, null);
        mTvHighestZF = (TextView) view.findViewById(R.id.tv_grouptrend_highest_income);

        mChartView = (ChartView) view.findViewById(R.id.grouptrend_chartview);

        mFrameBoderColor = RColor(R.color.b5);

        mAxisLineLayer = new YAxisLayer();

        mAxisLineLayer.setColor(RColor(R.color.t3));
        mAxisLineLayer.setAxisCount(3);
        mAxisLineLayer.setMaxValue(0.00f);
        mAxisLineLayer.setMinValue(0.00f);
        mAxisLineLayer.setAlign(Align.CENTER);
        mAxisLineLayer.setPaddings(0, 5, 0, 5);
        mAxisLineLayer.setMinWidthString(MIN_WIDTH);
        mAxisLineLayer.setShowBorder(false);
        mAxisLineLayer.setBorderWidth(1);
        mAxisLineLayer.setBorderColor(RColor(R.color.b5));
        mAxisLineLayer.setTextSize(FontUtils.dip2px(getContext(), 10));
        mAxisLineLayer.setOnFormatDataListener(new OnFormatDataListener() {
            @Override
            public String onFormatData(float val) {
                String zdf = DataUtils.getZDF(DataUtils.mDecimalFormat1, val);
                return zdf;
            }
        });

        mHS300LineLayer = new LineLayer();
        mHS300LineLayer.setPaddings(0, 0, 0, 0);
        mHS300LineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
        mHS300LineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
        mHS300LineLayer.setColor(RColor(R.color.c1));

        /**
         * 组合收益走势线
         */
        mGroupLineLayer = new LineLayer();
        mGroupLineLayer.setPaddings(0, 0, 0, 0);
        mGroupLineLayer.showHGrid(3);
        mGroupLineLayer.setCheckWidth(FontUtils.dip2px(getContext(), 1));
        mGroupLineLayer.setStrokeWidth(FontUtils.dip2px(getContext(), 1));
        mGroupLineLayer.setColor(RColor(R.color.c4));

        mLineStackLayer = new StackLayer();
        mLineStackLayer.setPaddings(0, 0, 0, 0);
        mLineStackLayer.addLayer(mHS300LineLayer);
        mLineStackLayer.addLayer(mGroupLineLayer);
        mLineStackLayer.setBorderColor(mFrameBoderColor);
        mLineStackLayer.setShowBorder(false);
        mLineStackLayer.setBorderWidth(1);

        GroupLayer mGroupLayer = new GroupLayer();
        mGroupLayer.setSideColor(mFrameBoderColor);
        mGroupLayer.setSideWidth(1);
        mGroupLayer.setShowSide(GroupLayer.SIDE_LEFT_V | GroupLayer.SIDE_RIGHT_V | GroupLayer.SIDE_BOTTOM_H | GroupLayer.SIDE_MIDDLE_V);



        mGroupLayer.setPaddings(0, 0, 0, 0);
        mGroupLayer.setLeftLayer(mAxisLineLayer);
        mGroupLayer.setRightLayer(mLineStackLayer);
        mGroupLayer.setHeightPercent(1.0f);
        mChartView.addLayer(mGroupLayer);

        mXAxisLayer = new XAxisLayer();
        mXAxisLayer.setPaddings(0, 10, 0, 10);
        mXAxisLayer.setColor(RColor(R.color.t3));
        mXAxisLayer.setMinLeftPaddingString(MIN_WIDTH);
        mXAxisLayer.setTextSize(FontUtils.dip2px(getContext(), 10));

        mChartView.addLayer(mXAxisLayer);

        return view;
    }

    private void refreshGroupBKGravity() {
        if (mPieChartView != null) {

            List<FixPair<Integer, Integer>> lstPieData = new ArrayList<FixPair<Integer, Integer>>();
            for (int i = 0; i < mLstBkGravity.size() && i < 4; i++) {
                FixPair<String, Float> pair_S_F = mLstBkGravity.get(i);

                int tColor = 0;
                TextView tv = null;
                if (pair_S_F.first.equals("现金")) {
                    tColor = RColor(getResIdByStr("color", "group_bk_", 4));
                    tv = (TextView) findViewById(getResIdByStr("id", "groupbk_tv_", 4));
                } else {
                    tColor = RColor(getResIdByStr("color", "group_bk_", i + 1));
                    tv = (TextView) findViewById(getResIdByStr("id", "groupbk_tv_", i + 1));
                }
                FixPair<Integer, Integer> piePair = new FixPair<Integer, Integer>(Math.round(pair_S_F.second.floatValue() * 100), tColor);

                lstPieData.add(piePair);
                if (i == mLstBkGravity.size() - 1) {
                    if (piePair.second == 0) {
                        lstPieData.get(0).second -= 1;
                        piePair.second = 1;
                    }
                }

                String bkName = pair_S_F.first;

                String sEmpty = "";
                for (int j = bkName.length(); j < 6; j++) {
                    sEmpty += "　";
                }
                bkName += sEmpty;
                bkName = bkName.substring(0, 5);
                int dPercent = Math.round(pair_S_F.second.floatValue() * 100);

                String sPercent = String.format("%2d", dPercent) + "%";

                String fullBKName = bkName + sPercent;
                tv.setText(fullBKName);
                tv.setTextColor(RColor(R.color.t1));
                tv.setVisibility(View.VISIBLE);
            }

            if (lstPieData.size() == 0) {
                int tColor = RColor(getResIdByStr("color", "group_bk_", 4));
                TextView tv = (TextView) findViewById(getResIdByStr("id", "groupbk_tv_", 4));
                tv.setText("现金　　　100%");
                tv.setTextColor(RColor(R.color.t1));
                tv.setVisibility(View.VISIBLE);
                FixPair<Integer, Integer> piePair = new FixPair<Integer, Integer>(100, tColor);
                lstPieData.add(piePair);
            }

            mPieChartView.setData(lstPieData);

            mPieChartView.postInvalidate();

        }

    }

    private View createGroupBKGravityView() {
        View view = View.inflate(getContext(), R.layout.layout_group_bkgravity, null);
        mPieChartView = (PieChartView) view.findViewById(R.id.groupbk_pie);

        mPieChartView.setBgColor(RColor(R.color.b1));
        mPieChartView.setBgEdgeWidth(FontUtils.dip2px(getContext(), 5));
        mPieChartView.setAnnulusWidth(FontUtils.dip2px(getContext(), 20));
        mPieChartView.setFgColor(RColor(R.color.b4));
        mPieChartView.setStartAngle(180);

        mLlGroupBkLableContent = (LinearLayout) view.findViewById(R.id.groupbk_ll_lable_content);
        final TextView tLable = (TextView) view.findViewById(R.id.groupbk_tv_gone);
        ViewTreeObserver vto = tLable.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tLable.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int tW = tLable.getWidth();
                Message msg = mHandler.obtainMessage(RESET_GROUPBK_LABLE_CONTENT, tW, 0);
                mHandler.sendMessage(msg);
            }
        });

        return view;
    }

    /**
     * 创建组合理念界面
     * */
    private View createGroupIdeaView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.page_groupheader_layout_idea, null);

        etIdeaContent = (EditText) view.findViewById(R.id.page_groupheader_layout_idea_et_idea);
        ImageView ivIdeaEdit = (ImageView) view.findViewById(R.id.page_groupheader_layout_idea_iv_edit);

        etIdeaContent.setText(mGroupData.getGroupIdea());

        if (isCurrentUserCreator) {
            ivIdeaEdit.setVisibility(View.VISIBLE);
        } else {
            ivIdeaEdit.setVisibility(View.GONE);
        }

        ivIdeaEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果当前用户是创建者，令输入框可以编辑，同时显示保存按钮
                if (isCurrentUserCreator) {
                    String idea = etIdeaContent.getText().toString().trim();
                    gotoModifyModifIdeaPage(idea);
                }
            }
        });

        return view;
    }

    /**
     * 刷新
     * */
    private void refreshGroupStockHeader() {
        String[] tAry = GROUP_STOCK_HEADER;

        if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
            tAry = GROUP_TRANSFER_RECORD_HEADER;
        }

        for (int i = 0; i < mLstGroupStockHeaderItems.size() && i < tAry.length; i++) {
            View v = mLstGroupStockHeaderItems.get(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(tAry[i]);
            }
        }
    }

    private void deleteGroupInfoView() {
        if (mGroupInfo_TabContent != null) {
            mGroupInfo_TabContent.removeAllViews();
        }

        // trend 变量置空
        mChartView = null;
        mAxisLineLayer = null;
        mHS300LineLayer = null;
        mGroupLineLayer = null;
        mXAxisLayer = null;
        mLineStackLayer = null;

        // pie变量
        mPieChartView = null;
        mLlGroupBkLableContent = null;
    }

    public void setHeaderViewBgColor(int color) {
        if (mGroupHeaderContent != null) {
            mGroupHeaderContent.setBackgroundColor(color);
        }
    }

    class GroupStockAdapter extends BaseAdapter {

        private static final int ITEM_VIEW_COUNT = 2;
        private static final int ITEM_VIEW_TYPE_RELATIVE = 0;
        private static final int ITEM_VIEW_TYPE_TRANSFER = 1;

        @Override
        public int getCount() {
            if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                return listDataRelativeStocks.size();
            } else if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
                return mLstTransferRecords.size();
            }

            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                return listDataRelativeStocks.get(position);
            } else if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
                return mLstTransferRecords.get(position);
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (currentListItemType == ITEM_TYPE_RELATIVE_STOCK) {
                return ITEM_VIEW_TYPE_RELATIVE;
            } else if (currentListItemType == ITEM_TYPE_CHANGE_RECORD) {
                return ITEM_VIEW_TYPE_TRANSFER;
            } else {
                return ITEM_VIEW_TYPE_RELATIVE;
            }
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int itemViewType = getItemViewType(position);

            if (itemViewType == ITEM_VIEW_TYPE_RELATIVE) {
                StockViewHolder vh = null;
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.page_grouppage_stocklineitem, null);

                    vh = new StockViewHolder(convertView);
                    convertView.setTag(vh);
                } else {
                    vh = (StockViewHolder) convertView.getTag();
                }

                GroupStockGoods groupStock = (GroupStockGoods) getItem(position);

                vh.tvGravity.setText(DataUtils.getGravity(groupStock.getGravity()));
                vh.tvGoodsName.setText(groupStock.getGoodsName());
                vh.tvGoodsCode.setText(groupStock.getGoodsCode());

                String tPrice = groupStock.getZxj();
                if (tPrice == null || tPrice.equals("0.00")) {
                    tPrice = groupStock.getLastClose();
                }
                vh.tvGoodsZxj.setText(tPrice);

                if (mHeaderType_stock == 0) {
                    vh.tvOptional.setText(groupStock.getZdf());
                    vh.tvOptional.setTextColor(Color.parseColor("#FFFFFF"));
                    int colorFlag = FontUtils.getColorByZDF_percent(groupStock.getZdf());
                    int color = getZDPColor(colorFlag);
                    vh.tvOptional.setBackgroundColor(color);
                } else if (mHeaderType_stock == 1) {
                    String createTime = DateUtils.groupCreateDay(groupStock.getAddTime());
                    vh.tvOptional.setText(createTime);
                    vh.tvOptional.setTextColor(Color.parseColor("#353535"));
                    vh.tvOptional.setBackgroundColor(0);
                } else if (mHeaderType_stock == 2) {
                    vh.tvOptional.setText(groupStock.getPositionPrice());
                    vh.tvOptional.setTextColor(Color.parseColor("#353535"));
                    vh.tvOptional.setBackgroundColor(0);
                } else if (mHeaderType_stock == 3) {
                    vh.tvOptional.setText(groupStock.getPositionProfitLossPercent());
                    vh.tvOptional.setTextColor(Color.parseColor("#FFFFFF"));
                    int colorFlag = FontUtils.getColorByZDF_percent(groupStock.getPositionProfitLossPercent());
                    int color = getZDPColor(colorFlag);
                    vh.tvOptional.setBackgroundColor(color);
                }

            } else if (itemViewType == ITEM_VIEW_TYPE_TRANSFER) {
                TransferViewHolder vh = null;
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.page_grouppage_transfer_listitem, null);

                    vh = new TransferViewHolder(convertView);
                    convertView.setTag(vh);
                } else {
                    vh = (TransferViewHolder) convertView.getTag();
                }

                final TransferRecord record = (TransferRecord) getItem(position);

                String tTime = record.getDate();
                tTime = tTime.replaceFirst("\\s+", "\n");
                vh.tvTime.setText(tTime);

                int tItemColor = RColor(R.color.t1);
                if (tTime != null && tTime.startsWith("今天")) {
                    tItemColor = getZDPColor(1);
                }
                vh.tvTime.setTextColor(tItemColor);

                vh.tvGoodsName.setText(record.getGoodname());
                vh.tvGoodsCode.setText(QuoteUtils.getStockCodeByGoodsId(record.getGoodid() + ""));
                vh.tvCategory.setText(record.getStrTradeType());

                if (mHeaderType_record == 0) {
                    // GROUP_TRANSFER_RECORD_HEADER[3] = "仓位变化";
                    String tTransfer = String.format(FORMAT_TRANSFER, DataUtils.getTransferGravity(record.getPosSrc()), DataUtils.getTransferGravity(record.getPosDst()));
                    vh.tvOptional.setText(Html.fromHtml(tTransfer));
                } else if (mHeaderType_record == 1) {
                    // GROUP_STOCK_HEADER[3] = "成交价";
                    vh.tvOptional.setText(record.getBidPrice());
                }

                if (record.getTransferStatus() == TransferRecordPage.TRANSFER_STATUS_DOING) {
                    // 调仓待成交，显示撤单，显示为待成交
                    vh.ivCancelOrder.setVisibility(View.VISIBLE);

                    vh.tvTransferStatus.setText("待成交");
                    vh.tvTransferStatus.setVisibility(View.VISIBLE);
                } else if (record.getTransferStatus() == TransferRecordPage.TRANSFER_STATUS_FAIL) {
                    // 调仓失败，不显示撤单，显示为失败
                    vh.ivCancelOrder.setVisibility(View.INVISIBLE);

                    vh.tvTransferStatus.setText("失败");
                    vh.tvTransferStatus.setVisibility(View.VISIBLE);
                } else if (record.getTransferStatus() == TransferRecordPage.TRANSFER_STATUS_CANCELED) {
                    // 撤单成功，不显示撤单，显示为已撤
                    vh.ivCancelOrder.setVisibility(View.INVISIBLE);

                    vh.tvTransferStatus.setText("已撤");
                    vh.tvTransferStatus.setVisibility(View.VISIBLE);
                } else {
                    // 调成功或其它，不显示撤单，不显示调仓标志
                    vh.ivCancelOrder.setVisibility(View.INVISIBLE);

                    vh.tvTransferStatus.setText("");
                    vh.tvTransferStatus.setVisibility(View.GONE);
                }

                // 点击撤单，执行撤单操作
                vh.ivCancelOrder.setOnClickListener(new OnClickEffectiveListener() {
                    @Override
                    public void onClickEffective(View v) {
                        // 发送撤单请求
                        requestCancelOrder(record.getId());
                    }
                });

            }

            return convertView;
        }

        private class StockViewHolder {
            public View layout;
            public TextView tvGravity, tvGoodsName, tvGoodsCode, tvGoodsZxj, tvOptional;

            public StockViewHolder(View view) {
                layout = view;

                tvGravity = (TextView) view.findViewById(R.id.item_tv_1);
                tvGoodsName = (TextView) view.findViewById(R.id.item_tv_2_1);
                tvGoodsCode = (TextView) view.findViewById(R.id.item_tv_2_2);
                tvGoodsZxj = (TextView) view.findViewById(R.id.item_tv_3);
                tvOptional = (TextView) view.findViewById(R.id.item_tv_4);
            }
        }

        private class TransferViewHolder {
            public View layout;
            public TextView tvTime, tvGoodsName, tvGoodsCode, tvCategory, tvTransferStatus, tvOptional;
            public ImageView ivCancelOrder;

            public TransferViewHolder(View view) {
                layout = view;

                tvTime = (TextView) view.findViewById(R.id.item_tv_1);
                tvGoodsName = (TextView) view.findViewById(R.id.item_tv_2_1);
                tvGoodsCode = (TextView) view.findViewById(R.id.item_tv_2_2);
                tvCategory = (TextView) view.findViewById(R.id.item_tv_3);
                tvTransferStatus = (TextView) view.findViewById(R.id.item_tv_transfer_status);
                tvOptional = (TextView) view.findViewById(R.id.item_tv_4);
                ivCancelOrder = (ImageView) view.findViewById(R.id.img_cancel_motif_order);
            }
        }

    }

    /**
     * 监控买吧评论列表只看创建人与自己Checkbox的点击事件
     * */
    public interface OnCommentCheckedChangeListener {
        public void onCheckChanged(boolean isChecked);
    }

    private OnCommentCheckedChangeListener commentCheckedChangeListener;

    public void setCommentCheckedChangeListener(OnCommentCheckedChangeListener commentCheckedChangeListener) {
        this.commentCheckedChangeListener = commentCheckedChangeListener;
    }

}
