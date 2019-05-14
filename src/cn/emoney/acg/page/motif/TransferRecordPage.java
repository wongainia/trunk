package cn.emoney.acg.page.motif;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.BuyClubHttpUrl;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.KeysInterface;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class TransferRecordPage extends PageImpl {

    public static final String EXTRA_KEY_GROUP_ID = "key_group_id";

    // 调仓成交状态
    public static final int TRANSFER_STATUS_DOING = 1101;
    public static final int TRANSFER_STATUS_SUCCESS = 1102;
    public static final int TRANSFER_STATUS_FAIL = 1103;
    public static final int TRANSFER_STATUS_CANCELED = 1104;

    private int groupId;
    private int lastTransferId;
    private boolean isShowPrice;

    /**
     * 是否正在请求数据过程中
     * */
    private boolean isRequesting;

    private RefreshListView listView;
    private TextView tvEmpty;
    private View layoutLoading;

    private List<ListCellBean> listDatas = new ArrayList<ListCellBean>();
    private TransferListAdapter adapter;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_transfer_record);

        View clickView = findViewById(R.id.page_transfer_record_layout_click);
        final TextView tvTransferZd = (TextView) findViewById(R.id.page_transfer_record_tv_zd);
        listView = (RefreshListView) findViewById(R.id.page_transfer_record_list);
        tvEmpty = (TextView) findViewById(R.id.page_transfer_record_tv_empty);
        layoutLoading = findViewById(R.id.page_transfer_record_layout_loading);

        // 初始化最后一列点击事件
        clickView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowPrice) {
                    tvTransferZd.setText("仓位变化");
                    isShowPrice = false;
                } else {
                    tvTransferZd.setText("成交价");
                    isShowPrice = true;
                }

                adapter.notifyDataSetChanged();
            }
        });

        // 初始化ListView
        adapter = new TransferListAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);

        // 为ListView添加下拉刷新头，并设置下拉刷新事件
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();

                // 规定时间后，如果还没有返回数据，隐藏下拉刷新头
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (listView != null && isRequesting) {
                            listView.onRefreshFinished();
                        }
                    }
                }, DataModule.REQUEST_MAX_LIMIT_TIME);
            }

            @Override
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() {}
        });
        // 默认不可以下拉刷新
        listView.setRefreshable(false);

        tvEmpty.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                requestData();
            }
        });

        bindPageTitleBar(R.id.page_transfer_record_titlebar);
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);

        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_GROUP_ID)) {
                groupId = arguments.getInt(EXTRA_KEY_GROUP_ID);
            }
        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        if (!getIsAutoRefresh()) {
            if (getUserVisibleHint()) {
                startRequestTask();
            } else {
                requestData();
            }
        }
    }

    @Override
    public void requestData() {
        super.requestData();

        if (!isRequesting) {
            requestTransferRecords();
            isRequesting = true;

            // 请求时，隐藏空白提示文本
            tvEmpty.setVisibility(View.GONE);

            // 请求过程中，如果无数据，表明是点击文本加载或首次进入页面时加载，此时应显示加载
            // 当请求返回，或超时时，隐藏加载
            if (listDatas.size() == 0) {
                layoutLoading.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (layoutLoading != null && isRequesting) {
                            isRequesting = false;
                            layoutLoading.setVisibility(View.GONE);

                            // 显示空白提示文本
                            tvEmpty.setText("请求失败，请点击重试");
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                }, DataModule.REQUEST_MAX_LIMIT_TIME);
            }
        }

    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "调仓记录");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {

    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        super.onPageTitleBarMenuItemSelected(menuitem);

        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    /**
     * 获取调仓记录
     * 
     * @param type 0:获取记录集; 1:检测是否有记录
     * */
    private void requestTransferRecords() {
        if (groupId == 0)
            return;

        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KEY_ID, 0);
            jsObj.put(KEY_CHECK, 0);
            jsObj.put(KEY_GROUP_CODE, groupId);
            jsObj.put(KEY_SIZE, 50);
            jsObj.put(KEY_REFRESH, true);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestInfo(jsObj, IDUtils.ID_GROUP_CONTROL_HISTORY);
    }

    /**
     * 取消订单
     * */
    private void requestCancelOrder(int orderId) {
        if (orderId < 0)
            return;

        String token = DataModule.getInstance().getUserInfo().getToken();

        String reqUrl = String.format(BuyClubHttpUrl.URL_GROUP_CANCEL_ORDER, token, String.valueOf(groupId), orderId);
        JSONObject jObject = new JSONObject();
        jObject.put(KeysInterface.KEY_URL, reqUrl);

        requestInfo(jObject, IDUtils.ID_GROUP_HTTP_INTERFACE, BuyClubHttpUrl.FLAG_GROUP_CANCEL_ORDER);
    }

    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        super.updateFromInfo(pkg);
        int id = pkg.getRequestType();

        if (id == IDUtils.ID_GROUP_CONTROL_HISTORY && isRequesting) {
            isRequesting = false;
            if (listView != null) {
                listView.onRefreshFinished();
            }

            // 请求返回时，隐藏加载进度条
            layoutLoading.setVisibility(View.GONE);

            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int recordCount = jsObj.getIntValue("count");
                int check = jsObj.getIntValue("check");
                if (check == 1 && recordCount > 0) {
                    // 有新调仓记录 {"check":1,"count":9}
                    // if (mIvMenuItemPushPrompt != null) {
                    // mIvMenuItemPushPrompt.setVisibility(View.VISIBLE);
                    // }
                } else if (check == 0) {
                    if (recordCount > 0) {
                        listDatas.clear();
                        JSONArray arrayRecords = jsObj.getJSONArray("records");

                        for (int i = 0; i < arrayRecords.size(); i++) {
                            JSONArray arrayItem = arrayRecords.getJSONArray(i);

                            int transferId = arrayItem.getIntValue(0);
                            String recordTime = arrayItem.getString(1);
                            int goodsId = arrayItem.getIntValue(2);
                            long price = arrayItem.getLongValue(3);
                            int posSrc = arrayItem.getIntValue(4);
                            int posDst = arrayItem.getIntValue(5);
                            int tradeType = arrayItem.getIntValue(6);

                            lastTransferId = transferId;

                            int transferStatus = TRANSFER_STATUS_SUCCESS;

                            if (arrayItem != null && arrayItem.size() >= 9) {
                                int status = arrayItem.getIntValue(8);
                                if (status == 0 || status == 2) {
                                    transferStatus = TRANSFER_STATUS_DOING;
                                } else if (status == -1) {
                                    transferStatus = TRANSFER_STATUS_FAIL;
                                } else if (status == -2) {
                                    transferStatus = TRANSFER_STATUS_CANCELED;
                                }
                            }

                            String[] times = recordTime.split("\\s+");
                            String date = times[0];
                            String time = times[1];

                            String category = getRecordCategory(tradeType);

                            String FORMAT_TRANSFER = "%s <font color=\"#e94b35\">→</font> %s";
                            String transferHtml = String.format(FORMAT_TRANSFER, DataUtils.getTransferGravity(posSrc), DataUtils.getTransferGravity(posDst));
                            String transfer = Html.fromHtml(transferHtml).toString();

                            ArrayList<Goods> listGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodsId), 1);
                            Goods goods = null;
                            if (listGoods != null && listGoods.size() > 0) {
                                goods = listGoods.get(0);
                            }

                            String goodsPrice = DataUtils.getPrice(price * 10);

                            ListCellBean bean = new ListCellBean(transferStatus, transferId, date, time, category, transfer, goods, goodsPrice);
                            listDatas.add(bean);
                        }

                        adapter.notifyDataSetChanged();
                    }

                }
            } catch (Exception e) {
            }

            try {
                String transferIds = getDBHelper().getString(DataModule.G_KEY_GROUP_TRANSFER_RECORD, "{}");
                JSONObject jObjTransfer = JSONObject.parseObject(transferIds);
                jObjTransfer.put(groupId + "", lastTransferId);
                getDBHelper().setString(DataModule.G_KEY_GROUP_TRANSFER_RECORD, jObjTransfer.toJSONString());
            } catch (Exception e) {
            }

            // 如果有数据，可以下拉刷新，如果无数据，不可以下拉刷新
            // 如果有数据，不显示空白提示，如果无数据，显示空白提示
            if (listDatas.size() > 0) {
                listView.setRefreshable(true);

                tvEmpty.setText("暂无数据");
                tvEmpty.setVisibility(View.GONE);
            } else {
                listView.setRefreshable(false);

                tvEmpty.setText("暂无数据，请点击重试");
                tvEmpty.setVisibility(View.VISIBLE);
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
                                showTip("撤单成功");

                                // 重新请求调仓记录，使用返回数据刷新界面
                                requestTransferRecords();
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

            showTip("撤单失败");
        }
    }

    /**
     * 获取调仓类型
     * */
    private String getRecordCategory(int recordCategory) {
        String category = "";

        String[] TRADE_TYPE = {"- -", "建仓", "加仓", "平仓", "减仓"};
        if (0 <= recordCategory && recordCategory <= 4) {
            category = TRADE_TYPE[recordCategory];
        } else {
            category = "- -";
        }

        return category;
    }

    class TransferListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<ListCellBean> listDatas;

        public TransferListAdapter(Context context, List<ListCellBean> listDatas) {
            inflater = LayoutInflater.from(context);
            this.listDatas = listDatas;
        }

        @Override
        public int getCount() {
            return listDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_transfer_record_listitem, parent, false);

                vh = new ViewHolder(convertView);
                convertView.setTag(vh);;
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            // 设置cell中每项的显示
            final ListCellBean bean = listDatas.get(position);
            vh.tvDate.setText(bean.date);
            vh.tvTime.setText(bean.time);
            vh.tvName.setText(bean.name);
            vh.tvCode.setText(bean.code);
            vh.tvCategory.setText(bean.category);
            if (isShowPrice) {
                vh.tvTransfer.setText(bean.price);
            } else {
                vh.tvTransfer.setText(bean.transfer);
            }
            if (bean.transferStatus == TRANSFER_STATUS_DOING) {
                // 调仓待成交，显示撤单，显示为待成交
                vh.ivCancelOrder.setVisibility(View.VISIBLE);

                vh.tvTransferStatus.setText("待成交");
                vh.tvTransferStatus.setVisibility(View.VISIBLE);
            } else if (bean.transferStatus == TRANSFER_STATUS_FAIL) {
                // 调仓失败，不显示撤单，显示为失败
                vh.ivCancelOrder.setVisibility(View.INVISIBLE);

                vh.tvTransferStatus.setText("失败");
                vh.tvTransferStatus.setVisibility(View.VISIBLE);
            } else if (bean.transferStatus == TRANSFER_STATUS_CANCELED) {
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

            // 设置cell点击事件，点击cell时跳转到相关个股详情
            vh.layout.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    QuoteJump.gotoQuote(TransferRecordPage.this, bean.goods);
                    // gotoQuote(bean.goods);
                }
            });

            // 点击撤单，执行撤单操作
            vh.ivCancelOrder.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    // 发送撤单请求
                    requestCancelOrder(bean.transferId);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;
            public TextView tvDate, tvTime, tvName, tvCode, tvCategory, tvTransferStatus, tvTransfer;
            public ImageView ivCancelOrder;

            public ViewHolder(View view) {
                layout = view;

                tvDate = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_date);
                tvTime = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_time);
                tvName = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_name);
                tvCode = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_code);
                tvCategory = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_category);
                tvTransferStatus = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_transfer_status);
                tvTransfer = (TextView) view.findViewById(R.id.page_transfer_listitem_tv_transfer);
                ivCancelOrder = (ImageView) view.findViewById(R.id.img_cancel_motif_order);
            }
        }

    }

    /**
     * 存储ListView中每一个cell显示的数据
     * */
    private class ListCellBean {
        public int transferStatus, transferId;
        public String date, time, name, code, category, transfer, price;
        public Goods goods;

        public ListCellBean(int transferStatus, int transferId, String date, String time, String category, String transfer, Goods goods, String price) {
            this.transferStatus = transferStatus;
            this.transferId = transferId;
            this.date = date;
            this.time = time;
            this.category = category;
            this.transfer = transfer;
            this.goods = goods;
            this.name = goods.getGoodsName();
            this.code = goods.getGoodsCode();
            this.price = price;
        }

    }

}
