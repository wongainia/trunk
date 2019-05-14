package cn.emoney.acg.page.my;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.helper.push.RedPointNoticeManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.infodetail.InfoDetailHome;
import cn.emoney.acg.page.share.infodetail.InfoDetailPage;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.acg.widget.SegmentedGroup;
import cn.emoney.acg.widget.pinnedheader.PinnedHeaderListView;
import cn.emoney.acg.widget.pinnedheader.SectionedBaseAdapter;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class PushMessagePage extends PageImpl implements android.widget.AdapterView.OnItemClickListener {
    public static final int MAX_PUSH_COUNT = 25;

    private final String ITEM_INDEX_IN_LSTINFO = "item_index_in_lstinfo";
    private final String ITEM_ID = "item_id";
    private final String ITEM_TITLE = "item_title";
    private final String ITEM_TIME = "item_time";
    private final String ITEM_SUBTITLE = "item_subtitle";
    private final String ITEM_SUMMARY = "item_summary";
    private final String ITEM_URL = "item_url";

    private static final int MSG_TYPE_SYSTEM_NOTICE = 0;
    private static final int MSG_TYPE_STOCK_ALERT = 1;
    private static final int MSG_TYPE_BUY_CLUB = 2;

    /** 当前消息类型 */
    private int currentMessageType = MSG_TYPE_SYSTEM_NOTICE;
    /** 上一次选中的消息类型 */
    private int lastMessageType = MSG_TYPE_SYSTEM_NOTICE;

    private ImageView imgAlert, imgBuyClub;
    private ViewSwitcher viewSwitcher;

    private PinnedHeaderListView listSysInfos;
    private PushMsgAdapter mAdapter;
    private ArrayList<Map<String, String>> mLstInfo = new ArrayList<Map<String, String>>();
    private ArrayList<Map<String, String>> mLstCell = new ArrayList<Map<String, String>>();
    private ArrayList<Integer> mLstDivideCount = new ArrayList<Integer>();

    private ListView listAlert;
    /** 个股预警和买吧提示列表数据 */
    private List<AlertInfo> listAlertInfos = new ArrayList<PushMessagePage.AlertInfo>();
    private List<AlertInfo> listStockAlertInfos = new ArrayList<PushMessagePage.AlertInfo>(); // 缓存个股预警数据
    private List<AlertInfo> listBuyClubInfos = new ArrayList<PushMessagePage.AlertInfo>(); // 缓存买吧消息
    /** 个股预警和买吧提示列表的Adapter */
    private AlertAdapter alertAdapter;

    List<String> mLstReaded = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_push_msg);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.page_push_switcher);
        listSysInfos = (PinnedHeaderListView) findViewById(R.id.push_msg_listview);
        listAlert = (ListView) findViewById(R.id.page_push_list_alert);

        View lvEmpty = findViewById(R.id.item_alert_listview_emptyview);
        listAlert.setEmptyView(lvEmpty);
        View sysListEmpty = findViewById(R.id.item_alert_syslist_emptyview);
        listSysInfos.setEmptyView(sysListEmpty);

        mAdapter = new PushMsgAdapter();
        listSysInfos.setAdapter(mAdapter);

        alertAdapter = new AlertAdapter(getContext(), listAlertInfos);
        listAlert.setAdapter(alertAdapter);
        listAlert.setOnItemClickListener(this);

        // 获取红点提示图标
        imgAlert = (ImageView) findViewById(R.id.page_push_img_notice_alert);
        imgBuyClub = (ImageView) findViewById(R.id.page_push_img_notice_group);

        // 初始化Segment点击切换事件
        SegmentedGroup segmentedGroup = (SegmentedGroup) findViewById(R.id.page_push_segmentgroup);
        segmentedGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.page_push_segment_sysinfo) {
                    currentMessageType = MSG_TYPE_SYSTEM_NOTICE;
                    // 如果重复点击一个item，则不操作
                    if (currentMessageType == lastMessageType) {
                        return;
                    }

                    setViewSwitchDisplay(currentMessageType);

                    // 显示系统消息页面，设置不显示红点
                    RedPointNoticeManager.updateRedPointDisplay(getContext(), "msg", false);

                    refreshPushList();

                    requestSysInfos();

                    lastMessageType = MSG_TYPE_SYSTEM_NOTICE;
                } else if (checkedId == R.id.page_push_segment_alert) {
                    currentMessageType = MSG_TYPE_STOCK_ALERT;
                    // 如果重复点击一个item，则不操作
                    if (currentMessageType == lastMessageType) {
                        return;
                    }

                    setViewSwitchDisplay(currentMessageType);

                    // 个股预警不再显示红点
                    imgAlert.setImageBitmap(null);
                    RedPointNoticeManager.updateRedPointDisplay(getContext(), "alarm", false);

                    updateAlertListView(listStockAlertInfos);

                    requestStockAlertInfos();

                    lastMessageType = MSG_TYPE_STOCK_ALERT;
                } else if (checkedId == R.id.page_push_segment_group) {
                    currentMessageType = MSG_TYPE_BUY_CLUB;
                    // 如果重复点击一个item，则不操作
                    if (currentMessageType == lastMessageType) {
                        return;
                    }

                    setViewSwitchDisplay(currentMessageType);

                    // 买吧提示不再显示红点
                    imgBuyClub.setImageBitmap(null);
                    RedPointNoticeManager.updateRedPointDisplay(getContext(), "zuhe", false);

                    updateAlertListView(listBuyClubInfos);

                    requestBuyClubInfos();

                    lastMessageType = MSG_TYPE_BUY_CLUB;
                }

            }
        });

        bindPageTitleBar(R.id.page_push_msg_titlebar);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 根据当前消息类型决定界面显示哪个ListView
        setViewSwitchDisplay(currentMessageType);

        // 刷新个股预警和买吧提示两个item右上角的红点是否显示
        refreshRedPoint();

        // 首先显示缓存中的数据，然后从网络刷新数据
        if (currentMessageType == MSG_TYPE_STOCK_ALERT) {
            // 个股预警不再显示红点
            imgAlert.setImageBitmap(null);
            RedPointNoticeManager.updateRedPointDisplay(getContext(), "alarm", false);

            updateAlertListView(listStockAlertInfos);

            requestStockAlertInfos();
        } else if (currentMessageType == MSG_TYPE_BUY_CLUB) {
            // 买吧提示不再显示红点
            imgBuyClub.setImageBitmap(null);
            RedPointNoticeManager.updateRedPointDisplay(getContext(), "zuhe", false);

            updateAlertListView(listBuyClubInfos);

            requestBuyClubInfos();
        } else {
            // 显示系统消息页面，设置不显示红点
            RedPointNoticeManager.updateRedPointDisplay(getContext(), "msg", false);

            refreshPushList();

            requestSysInfos();
        }

    }

    /**
     * 网络获取系统消息，使用系统返回的消息刷新缓存
     * */
    private void requestSysInfos() {
        JSONObject jsonObject = new JSONObject();
        int t_uType = 0;
        int t_uLastId = 0;

        t_uLastId = getDBHelper().getInt(DataModule.G_KEY_PUSH_MSG_LIST_LAST_ID, 0);
        DataModule.G_LAST_LOGIN_STATE = getDBHelper().getInt(DataModule.G_KEY_USER_LAST_LOGIN_STATE, DataModule.G_LAST_LOGIN_STATE);
        if (DataModule.G_LAST_LOGIN_STATE == 1) {
            t_uType = 1;
        }

        try {
            jsonObject.put("type", t_uType);
            jsonObject.put("from", t_uLastId);
            jsonObject.put("size", MAX_PUSH_COUNT);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        requestInfo(jsonObject, IDUtils.ID_PUSH_MSG);
    }

    /**
     * 网络获取个股预警消息
     * */
    private void requestStockAlertInfos() {
        int size = 100;

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_SIZE, size);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(json, IDUtils.ID_STOCK_ALERT_MSG_LIST);
    }

    /**
     * 网络获取买吧消息
     * */
    private void requestBuyClubInfos() {
        int size = 100;

        JSONObject json = new JSONObject();
        try {
            json.put(KEY_SIZE, size);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(json, IDUtils.ID_STOCK_ALERT_BUY_CLUB_LIST);
    }

    /**
     * 解析后台返回的消息数据
     * */
    public void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == IDUtils.ID_STOCK_ALERT_MSG_LIST) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            // 解析服务器返回的数据并显示在ListView中
            try {
                JSONObject jsObj = JSON.parseObject(msgData);

                // 返回码是否是正确
                int retCode = jsObj.getIntValue("result");
                if (retCode != 0) {
                    return;
                }

                String infoArray = jsObj.getString("data");

                // 使用网络返回的数据刷新缓存数据
                // 无论当前显示的item是不是个股预警，都刷新缓存数据
                updateStockAlertInfos(infoArray);

                // 使用缓存数据刷新ListView
                // 只有当当前显示的item是个股预警时，才刷新ListView
                if (currentMessageType == MSG_TYPE_STOCK_ALERT) {
                    updateAlertListView(listStockAlertInfos);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (id == IDUtils.ID_STOCK_ALERT_BUY_CLUB_LIST) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            // 解析服务器返回的数据并显示在ListView中
            try {
                JSONObject jsObj = JSON.parseObject(msgData);

                // 返回码是否是正确
                int retCode = jsObj.getIntValue("result");
                if (retCode != 0) {
                    return;
                }

                String infoArray = jsObj.getString("data");

                // 使用网络返回的数据刷新缓存数据
                // 无论当前显示的item是不是买吧提示，都刷新缓存数据
                updateBuyClubInfos(infoArray);

                // 使用缓存数据刷新ListView
                // 只有当当前显示的item是买吧提示时，才刷新ListView
                if (currentMessageType == MSG_TYPE_BUY_CLUB) {
                    updateAlertListView(listBuyClubInfos);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (id == IDUtils.ID_PUSH_MSG) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                // 使用网络返回的数据刷新缓存数据
                /*
                 * [ ["id","title","time","subTitle","summary","url"],
                 * ["id","title","time","subTitle","summary","url"],
                 * ["id","title","time","subTitle","summary","url"],
                 * ["id","title","time","subTitle","summary","url"], ]
                 */
                if (msgData == null || msgData.equals("")) {
                    return;
                }

                JSONArray jsonRecArray = JSON.parseArray(msgData);
                int len_rec = jsonRecArray.size();

                if (len_rec > 0) {
                    String t_jsonStr = getDBHelper().getString(DataModule.G_KEY_PUSH_MSG_LIST, "[]");
                    JSONArray jAryOldList = JSON.parseArray(t_jsonStr);
                    int len_old = jAryOldList.size();

                    ArrayList<JSONArray> lstTemp = new ArrayList<JSONArray>();

                    for (int i = 0; i < len_rec; i++) {
                        lstTemp.add(jsonRecArray.getJSONArray(i));
                    }
                    for (int i = 0; i < len_old; i++) {
                        lstTemp.add(jAryOldList.getJSONArray(i));
                    }

                    JSONArray jAryNewList = new JSONArray();

                    for (int i = 0; i < MAX_PUSH_COUNT; i++) {
                        if (i >= lstTemp.size()) {
                            break;
                        }
                        jAryNewList.add(lstTemp.get(i));
                    }

                    String sSaveList = jAryNewList.toString();
                    getDBHelper().setString(DataModule.G_KEY_PUSH_MSG_LIST, sSaveList);

                    JSONArray sub_jsonArray = jAryNewList.getJSONArray(0);
                    String sId = sub_jsonArray.getString(0);
                    int iLstLastId = Integer.valueOf(sId);

                    int t_uLastId = getDBHelper().getInt(DataModule.G_KEY_PUSH_MSG_LIST_LAST_ID, 0);
                    if (iLstLastId != t_uLastId) { // 理论上应该改为大于
                        getDBHelper().setInt(DataModule.G_KEY_PUSH_MSG_LIST_LAST_ID, iLstLastId);
                    }
                }

                // 使用缓存数据刷新ListView
                refreshPushList();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 使用网络返回的数据刷新个股预警缓存数据
     * */
    private void updateStockAlertInfos(String infoArray) {
        try {
            JSONArray array = JSONArray.parseArray(infoArray);

            if (array != null && array.size() > 0) {
                // 只有当网络返回的有数据时，才清空缓存
                listStockAlertInfos.clear();

                // 解析网络返回的数据，并添加到缓存中
                for (int i = 0; i < array.size(); i++) {
                    JSONObject objItem = array.getJSONObject(i);

                    if (objItem != null) {
                        listStockAlertInfos.add(new AlertInfo(objItem.getIntValue("cond"), objItem.getString("name"), objItem.getString("param"), objItem.getString("stock_name"), objItem.getIntValue("stock"), objItem.getIntValue("time")));

                        String todayTime = DateUtils.getCurrentTimeByFormat("yyyyMMdd");

                        // 使用网络返回的数据刷新各股最新推送时间
                        String time = objItem.getIntValue("time") + "";
                        String date = objItem.getIntValue("date") + "";
                        time = time.length() > 5 ? time : "0" + time;

                        if (todayTime.equals(date + "")) {
                            // 最新刷新时间是今天，时间的年月日显示为今天
                            time = "今天 " + time.substring(0, 2) + ":" + time.substring(2, 4);
                        } else {
                            // 最新刷新时间不是今天，时间的年月日显示为yyyy-MM-dd
                            time = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) + " " + time.substring(0, 2) + ":" + time.substring(2, 4);
                        }

                    }
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * 使用网络返回的数据刷新买吧提示缓存数据
     * */
    private void updateBuyClubInfos(String infoArray) {
        try {
            JSONArray array = JSONArray.parseArray(infoArray);

            if (array != null && array.size() > 0) {
                // 只有当网络返回的有数据时，才清空缓存
                listBuyClubInfos.clear();

                // 解析网络返回的数据，并添加到缓存中
                for (int i = 0; i < array.size(); i++) {
                    JSONObject objItem = array.getJSONObject(i);

                    // {"bs_type":"建仓","business_NO":4100896,"date_time":1445840213,"group_code":214,"group_name":"青蒿素概念","pos_dst":1000,"pos_src":0,"price":1156,"stock_id":1000001,"stock_name":"平安银行"}
                    if (objItem != null) {
                        listBuyClubInfos.add(new AlertInfo(objItem.getString("stock_name"), objItem.getIntValue("stock_id"), objItem.getString("group_name"), objItem.getIntValue("date_time"), objItem.getIntValue("pos_src"), objItem.getIntValue("pos_dst"), objItem.getString("bs_type")));
                    }
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * 使用指定缓存数据刷新个股预警和买吧提示ListView
     * */
    private void updateAlertListView(List<AlertInfo> infos) {

        listAlertInfos.clear();
        listAlertInfos.addAll(infos);

        alertAdapter.notifyDataSetChanged();
    }

    /**
     * 使用缓存的数据刷新ListView显示
     * */
    private void refreshPushList() {
        String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_PUSH_MSG, null);
        if (aryReaded != null) {
            mLstReaded = Arrays.asList(aryReaded);
        }

        String sJAry = getDBHelper().getString(DataModule.G_KEY_PUSH_MSG_LIST, "[]");
        try {
            JSONArray jAry = JSON.parseArray(sJAry);
            int t_len = jAry.size();
            mLstInfo.clear();
            mLstCell.clear();
            mLstDivideCount.clear();

            String sDay = "";
            if (t_len > 0) {
                JSONArray tFisrtMsg = jAry.getJSONArray(0);
                String tFirstTime = tFisrtMsg.getString(2);
                if (tFirstTime != null && !tFirstTime.equals("")) {
                    sDay = DateUtils.formatInfoDate(tFirstTime, DateUtils.mFormatDay);
                }
                mLstDivideCount.add(0);
            }

            for (int i = 0; i < t_len; i++) {
                JSONArray jAry_oneMsg = jAry.getJSONArray(i);
                String id = jAry_oneMsg.getString(0);
                String title = jAry_oneMsg.getString(1);
                String time = jAry_oneMsg.getString(2);
                String subTitle = jAry_oneMsg.getString(3);
                String summary = jAry_oneMsg.getString(4);
                String url = jAry_oneMsg.getString(5);

                Map<String, String> map_oneCell = new HashMap<String, String>();
                map_oneCell.put(ITEM_ID, id);
                map_oneCell.put(ITEM_TITLE, title.trim());
                map_oneCell.put(ITEM_TIME, time);
                map_oneCell.put(ITEM_SUBTITLE, subTitle.trim());
                map_oneCell.put(ITEM_SUMMARY, summary.trim());
                map_oneCell.put(ITEM_URL, url);

                if (url != null && !url.equals("")) {
                    Map<String, String> map_oneMsg = new HashMap<String, String>();
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_TIME, time);
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_FROM, "");
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_SORTCLS, "系统消息");
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                    map_oneMsg.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                    mLstInfo.add(map_oneMsg);
                    map_oneCell.put(ITEM_INDEX_IN_LSTINFO, String.valueOf(mLstInfo.size() - 1));
                }

                if (time != null && !time.equals("")) {
                    String tDay = DateUtils.formatInfoDate(time, DateUtils.mFormatDay);
                    if (sDay.compareTo(tDay) > 0) {
                        sDay = tDay;
                        mLstDivideCount.add(i);
                    }
                }

                mLstCell.add(map_oneCell);
            }

            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mLstInfo.size() >= 1) {
            int t_id = Integer.valueOf(mLstCell.get(0).get(ITEM_ID));
            getDBHelper().setInt(DataModule.G_KEY_PUSH_MSG_LIST_LAST_READED_ID, t_id);
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {

        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem itemTitle = new BarMenuTextItem(1, "推送消息");
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    class PushMsgAdapter extends SectionedBaseAdapter {

        @Override
        public Object getItem(int section, int position) {
            return null;
        }

        @Override
        public long getItemId(int section, int position) {
            return 0;
        }

        @Override
        public int getSectionCount() {
            return mLstDivideCount.size();
        }

        @Override
        public int getCountForSection(int section) {

            if (section == mLstDivideCount.size() - 1) {
                return mLstCell.size() - mLstDivideCount.get(section);
            } else {
                return mLstDivideCount.get(section + 1) - mLstDivideCount.get(section);
            }

        }

        @Override
        public View getItemView(final int section, final int position, View convertView, ViewGroup parent) {
            ViewHolderListCell vh = null;

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.layout_push_lst_item, null);

                vh = new ViewHolderListCell(convertView);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolderListCell) convertView.getTag();
            }

            int t_index = mLstDivideCount.get(section) + position;
            Map<String, String> mapCell = null;
            if (mLstCell.size() > t_index) {
                mapCell = (Map<String, String>) mLstCell.get(t_index);
            }

            if (mapCell != null) {
                String time = DateUtils.formatInfoDate(mapCell.get(ITEM_TIME), DateUtils.mFormatHM);
                vh.tvTime.setText(time);
                vh.tvTitle.setText(mapCell.get(ITEM_TITLE));
                vh.tvSummary.setText(mapCell.get(ITEM_SUMMARY));

                String sFlag = mapCell.get(ITEM_URL);
                if (sFlag != null && !sFlag.equals("")) {
                    String sMd5Flag = MD5Util.md5(sFlag);

                    if (mLstReaded != null && mLstReaded.contains(sMd5Flag)) {
                        vh.tvTitle.setTextColor(getResources().getColor(R.color.t3));
                        vh.tvSummary.setTextColor(getResources().getColor(R.color.t3));
                    } else {
                        vh.tvTitle.setTextColor(getResources().getColor(R.color.t1));
                        vh.tvSummary.setTextColor(getResources().getColor(R.color.t2));
                    }
                } else {
                    vh.tvTitle.setTextColor(getResources().getColor(R.color.t1));
                    vh.tvSummary.setTextColor(getResources().getColor(R.color.t2));
                }

                // set events
                vh.layout.setOnClickListener(new OnClickEffectiveListener() {
                    @Override
                    public void onClickEffective(View v) {
                        Map<String, String> mapCell = mLstCell.get(mLstDivideCount.get(section) + position);
                        String t_url = mapCell.get(ITEM_URL);
                        if (t_url == null || t_url.equals("")) {
                            showTip("没有详细内容");
                        } else {
                            gotoInfo(mLstInfo, Integer.valueOf(mapCell.get(ITEM_INDEX_IN_LSTINFO)), DataModule.G_KEY_INFODETAIL_PUSH_MSG);
                        }
                    }
                });
            }

            return convertView;
        }

        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            Log.v("sky", "getSectionHeaderView: section:" + section);
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.layout_push_lst_head, null);
                TextView tvDay = (TextView) convertView.findViewById(R.id.item_day_number);
                TextView tvYearMonth = (TextView) convertView.findViewById(R.id.item_day_year_month);
                View tDivideLine = convertView.findViewById(R.id.item_sep_line);
                tDivideLine.setBackgroundColor(getResources().getColor(R.color.b7));
                HeadCell hc = new HeadCell(tvDay, tvYearMonth);
                convertView.setTag(hc);
            }
            HeadCell hc = (HeadCell) convertView.getTag();

            int t_index = mLstDivideCount.get(section);
            Map<String, String> mapCell = null;
            if (mLstCell.size() > t_index) {
                mapCell = (Map<String, String>) mLstCell.get(t_index);
            }

            if (mapCell != null) {
                String yymm = DateUtils.formatInfoDate(mapCell.get(ITEM_TIME), DateUtils.mFormatYearDotMonth);
                String dd = DateUtils.formatInfoDate(mapCell.get(ITEM_TIME), DateUtils.mFormatDD);

                hc.tvDay.setText(dd);
                hc.tvYearMonth.setText(yymm);
            }

            return convertView;
        }

        class ViewHolderListCell {
            public View layout;
            public TextView tvTime, tvTitle, tvSummary;

            ViewHolderListCell(View view) {
                layout = view;
                tvTitle = (TextView) view.findViewById(R.id.item_title);
                tvTime = (TextView) view.findViewById(R.id.item_time);
                tvSummary = (TextView) view.findViewById(R.id.item_content);
            }

        }

    }

    class HeadCell {
        HeadCell(TextView tvDay, TextView tvYearMonth) {
            this.tvDay = tvDay;
            this.tvYearMonth = tvYearMonth;
        }

        public TextView tvDay;
        public TextView tvYearMonth;
    }

    /**
     * 个股预警和买吧提示ListView显示的Adapter
     * */
    private class AlertAdapter extends BaseAdapter {
        private List<AlertInfo> listInfos;
        private LayoutInflater layoutInflater;

        public AlertAdapter(Context context, List<AlertInfo> listInfos) {
            layoutInflater = LayoutInflater.from(context);
            this.listInfos = listInfos;
        }

        @Override
        public int getCount() {
            return listInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return listInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoler vh = null;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.page_push_msg_listalert_item, null);

                vh = new ViewHoler();
                vh.tvTime = (TextView) convertView.findViewById(R.id.page_push_msg_item_time);
                vh.tvTitle = (TextView) convertView.findViewById(R.id.page_push_msg_item_title);
                vh.tvSummary = (TextView) convertView.findViewById(R.id.page_push_msg_item_summary);
                vh.tvContent = (TextView) convertView.findViewById(R.id.page_push_msg_item_content);

                convertView.setTag(vh);
            } else {
                vh = (ViewHoler) convertView.getTag();
            }

            AlertInfo info = listAlertInfos.get(position);

            vh.tvTime.setText(info.showTime);
            vh.tvTitle.setText(info.title);
            vh.tvSummary.setText(info.summary);
            vh.tvContent.setText(info.content);

            return convertView;
        }

        private class ViewHoler {
            public TextView tvTitle, tvSummary, tvTime, tvContent;
        }

    }

    private class AlertInfo {

        public String showTime;
        public String title;
        public String summary;
        public String content;

        public Goods goods;

        public AlertInfo(int cound, String name, String value, String stockName, int stockId, int time) {
            // 获取时间
            String timeStr = String.valueOf(time);
            if (timeStr.length() > 5) {
                this.showTime = timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4) + ":" + timeStr.substring(4, 6);
            } else {
                this.showTime = "0" + timeStr.substring(0, 1) + ":" + timeStr.substring(1, 3) + ":" + timeStr.substring(3, 5);
            }

            this.goods = new Goods(stockId, stockName);
            this.title = this.goods.getGoodsName();
            this.summary = this.goods.getGoodsCode();

            // 获取显示内容
            if (cound >= (2 << 1) && cound <= (2 << 3)) {
                // 涨幅达到、跌幅达到、换手率
                value = DataUtils.formatFloat2Percent(DataUtils.convertToFloat(value));
                this.content = name + " " + value;
            } else if (cound == (2 << 4)) {
                // 最新价达到
                value = DataUtils.formatPrice(value);
                this.content = name + " " + value;
            } else if (cound >= (2 << 5) && cound <= (2 << 10)) {
                // BS点
                this.content = name;
            } else {
                // 不应该走到这里
                value = DataUtils.formatPrice(value);
                this.content = name + " " + value;
            }

        }

        public AlertInfo(String stockName, int stockId, String groupName, int dateTime, int posSrc, int posDst, String type) {
            this.goods = new Goods(stockId, stockName);

            // 获取时间
            this.showTime = DateUtils.convertTimestampToHHmmss(dateTime, true);

            this.title = groupName;
            this.summary = "组合";

            this.content = type + " " + this.goods.getGoodsCode() + " " + stockName + "， 仓位： " + posSrc / 100 + "% → " + posDst / 100 + "%";
        }

    }

    public void gotoInfo(ArrayList<Map<String, String>> listItem, int index, String infoType) {
        PageIntent intent = new PageIntent(this, InfoDetailHome.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(InfoDetailHome.EXTRA_KEY_LIST_ITEMS, listItem);
        bundle.putInt(InfoDetailHome.EXTRA_KEY_LIST_INDEX, index);
        bundle.putString(InfoDetailHome.EXTRA_KEY_INFO_TYPE, infoType);

        intent.setArguments(bundle);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    /**
     * 根据当前消息类型决定显示哪个ListView
     * */
    private void setViewSwitchDisplay(int msgType) {
        if (msgType == MSG_TYPE_SYSTEM_NOTICE) {
            viewSwitcher.setDisplayedChild(0);
        } else {
            viewSwitcher.setDisplayedChild(1);
        }
    }

    /**
     * 刷新推送消息提示（红点）是否显示
     * */
    private void refreshRedPoint() {
        // 刷新个股预警是否显示红点
        boolean isShowRedAlert = RedPointNoticeManager.getRedpointDisplay(getContext(), "alarm");
        if (isShowRedAlert) {
            imgAlert.setImageResource(R.drawable.img_notice_point);
        }

        // 刷新买吧提示是否显示红点
        boolean isShowRedBuyClub = RedPointNoticeManager.getRedpointDisplay(getContext(), "zuhe");
        if (isShowRedBuyClub) {
            imgBuyClub.setImageResource(R.drawable.img_notice_point);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentMessageType == MSG_TYPE_STOCK_ALERT && listAlertInfos != null && listAlertInfos.size() > 0) {
            AlertInfo info = listAlertInfos.get(position);
            Goods goods = info.goods;
            QuoteJump.gotoQuote(PushMessagePage.this, goods);
            // gotoQuote(goods);
        }
    }

}
