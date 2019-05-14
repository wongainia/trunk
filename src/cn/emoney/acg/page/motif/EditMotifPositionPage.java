package cn.emoney.acg.page.motif;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.OnRefreshListener;

/**
 * 编辑买吧持仓
 * */
public class EditMotifPositionPage extends PageImpl {
    
    public static final String EXTRA_KEY_GROUP_ID = "extra_group_id";
    
    private int groupId;
    
    private RefreshListView listView;
    
    private List<GroupStockGoods> listDatas = new ArrayList<GroupStockGoods>();
    private PositionListAdapter adapter;
    private ArrayList<String> listGoodsCoods = new ArrayList<String>();

    private float balance;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_edit_motifpositioin);
        
        initViews();
        
        bindPageTitleBar(R.id.page_edit_motifposition_titlebar);
    }
    
    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);
        
        if (arguments != null && arguments.containsKey(EXTRA_KEY_GROUP_ID)) {
            groupId = arguments.getInt(EXTRA_KEY_GROUP_ID, 0);
        }
    }

    @Override
    protected void initData() {
    }
    
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
        
        requestGroupInfo(true);
    }
    
    private void initViews() {
        listView = (RefreshListView) findViewById(R.id.page_edit_motifposition_list);
        View layoutEmptyView = findViewById(R.id.page_edit_motifposition_layout_listEmptyView);
        
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setOnRefreshListener(new OnRefreshListener() {
            
            @Override
            public void onRefresh() {
                requestData();
            }

            @Override
            public void beforeRefresh() {}

            @Override
            public void afterRefresh() { }
            
        });
        listView.setEmptyView(layoutEmptyView);
        adapter = new PositionListAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);
    }
    
    /**
     * 获取成分股列表
     * */
    private void requestGroupInfo(boolean isRefresh) {
        if (groupId == 0) return;
        
        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put(KEY_GROUP_CODE, groupId);
            jsObj.put(KEY_REFRESH, isRefresh);
            jsObj.put(KEY_TOKEN, getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        requestInfo(jsObj, IDUtils.ID_GROUP_DETAIL_INFO);
    }
    
    /**
     * 获取成分股仓位
     * */
    private void requestStockInfo() {
        ArrayList<Integer> goodsIds = new ArrayList<Integer>();
        for (int i = 0; i < listDatas.size(); i++) {
            goodsIds.add(listDatas.get(i).getGoodsId());
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
    
    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();
        
        if (id == IDUtils.ID_GROUP_DETAIL_INFO) {
            listView.onRefreshFinished();
            
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                if (jsObj == null || !jsObj.containsKey("stockList") || !jsObj.containsKey("balance")) return;
                
                balance = jsObj.getLongValue("balance") / 100f;
                    
                JSONArray jAryStock = jsObj.getJSONArray("stockList");
                if (jAryStock == null) return;

                int length = jAryStock.size();

                if (length > 0) {
                    listDatas.clear();
                    listGoodsCoods.clear();
                }

                for (int i = 0; i < length; i++) {
                    JSONArray oneStock = jAryStock.getJSONArray(i);
                    String sGoodCode = Util.FormatStockCode(oneStock.getIntValue(2));
                    ArrayList<Goods> lst = getSQLiteDBHelper().queryStockInfosByCode2(sGoodCode, 1);
                    if (lst != null && lst.size() > 0) {
                        Goods goods = lst.get(0);
                        GroupStockGoods groupStockGoods = new GroupStockGoods(goods.getGoodsId(), goods.getGoodsName());
                        groupStockGoods.setTotalCostValue(oneStock.getLongValue(0));
                        groupStockGoods.setTotalGoodsNum(oneStock.getLongValue(1));
                        groupStockGoods.setPositionAmount(oneStock.getLongValue(3) + "");
                        groupStockGoods.setAddTime(oneStock.getString(4));

                        float costPrice = oneStock.getFloatValue(0) / 100f / oneStock.getLongValue(1);
                        DecimalFormat mDf = new DecimalFormat("0.00");
                        String t_sPrice = mDf.format(costPrice);
                        groupStockGoods.setPositionPrice(t_sPrice);
                        
                        listDatas.add(groupStockGoods);
                        listGoodsCoods.add(groupStockGoods.getGoodsCode());
                    }
                }
            } catch (Exception e) { }
            
            if (listDatas.size() > 0) {
                requestStockInfo();
            }
        }
        
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

                    for (int j = 0; j < listDatas.size(); j++) {
                        GroupStockGoods groupGoods = listDatas.get(j);
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

                BuyClubUtil.reCalcHYGravity(listDatas, balance);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

            }
        }

    }
    
    /**
     * 打开添加买吧成分股界面
     * */
    private void gotoAddMotifStockPage() {
        PageIntent intent = new PageIntent(this, AddMotifPositionPage.class);

        Bundle extras = new Bundle();
        extras.putInt(AddMotifPositionPage.EXTRA_KEY_GROUP_ID, groupId);
        extras.putStringArrayList(AddMotifPositionPage.EXTRA_KEY_GROUP_GOODSCODE, listGoodsCoods);
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }
    
    /**
     * 打开股票调仓界面
     * */
    private void gotoModifyMotifPositionPage(String currentPosition, int goodsId, String goodsName, String goodsCode, String goodsPrice, String goodsZdf) {
        PageIntent intent = new PageIntent(this, ModifyMotifPositionPage.class);

        Bundle extras = new Bundle();
        extras.putInt(ModifyMotifPositionPage.EXTRA_KEY_GROUP_ID, groupId);
        extras.putString(ModifyMotifPositionPage.EXTRA_KEY_CURRENT_POSITION, currentPosition);
        extras.putString(ModifyMotifPositionPage.EXTRA_KEY_GOODS_NAME, goodsName);
        extras.putString(ModifyMotifPositionPage.EXTRA_KEY_GOODS_CODE, goodsCode);
        extras.putString(ModifyMotifPositionPage.EXTRA_KEY_GOODS_PRICE, goodsPrice);
        extras.putString(ModifyMotifPositionPage.EXTRA_KEY_GOODS_ZDF, goodsZdf);
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }
    
    /**
     * 打开股票平仓界面
     * */
    private void gotoCloseMotifPositionPage(String currentPosition, int goodsId, String goodsName, String goodsCode, String goodsPrice, String goodsZdf) {
        PageIntent intent = new PageIntent(this, CloseMotifPositionPage.class);

        Bundle extras = new Bundle();
        extras.putInt(CloseMotifPositionPage.EXTRA_KEY_GROUP_ID, groupId);
        extras.putString(CloseMotifPositionPage.EXTRA_KEY_CURRENT_POSITION, currentPosition);
        extras.putString(CloseMotifPositionPage.EXTRA_KEY_GOODS_NAME, goodsName);
        extras.putString(CloseMotifPositionPage.EXTRA_KEY_GOODS_CODE, goodsCode);
        extras.putString(CloseMotifPositionPage.EXTRA_KEY_GOODS_PRICE, goodsPrice);
        extras.putString(CloseMotifPositionPage.EXTRA_KEY_GOODS_ZDF, goodsZdf);
        intent.setArguments(extras);
        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }
    
    /**
     * 打开个股详情界面
     * */
    private void gotoQuotePage(Goods goods) {
        QuoteJump.gotoQuote(EditMotifPositionPage.this, goods);
//        gotoQuote(goods);
    }
    
    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);
        
        BarMenuTextItem centerItem = new BarMenuTextItem(1, "编辑成分股");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);
        
        View rightView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_txt, null);
        TextView tvLeftTitle = (TextView) leftView.findViewById(R.id.tv_titlebar_text);
        tvLeftTitle.setText("添加");
        
        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

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
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            // 添加成分股
            gotoAddMotifStockPage();
        }
    }

    private class PositionListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<GroupStockGoods> listDatas;
        
        public PositionListAdapter(Context context, List<GroupStockGoods> listDatas) {
            this.inflater = LayoutInflater.from(context);
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
                convertView = inflater.inflate(R.layout.page_edit_motifposition_listitem, parent, false);
                
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            
            final GroupStockGoods bean = listDatas.get(position);
            vh.tvPosition.setText(DataUtils.getGravity(bean.getGravity()));
            vh.tvStockName.setText(bean.getGoodsName());
            vh.tvStockCode.setText(bean.getGoodsCode());
            
            // 设置点击事件
            vh.tvResetPosition.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 打开调仓界面
                    String currentPosition = DataUtils.getGravity(bean.getGravity());
                    gotoModifyMotifPositionPage(currentPosition, bean.getGoodsId(), bean.getGoodsName(), bean.getGoodsCode(), bean.getZxj(), bean.getZdf());
                }
            });
            vh.tvClearPosition.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 打开平仓界面
                    String currentPosition = DataUtils.getGravity(bean.getGravity());
                    gotoCloseMotifPositionPage(currentPosition, bean.getGoodsId(), bean.getGoodsName(), bean.getGoodsCode(), bean.getZxj(), bean.getZdf());
                }
            });
            vh.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取可操作仓位
//                    requestAvailablePosition(bean.getGoodsCode());
                    
                    // 打开个股详情页面
                    int goodsId = bean.getGoodsId();
                    String goodsName = bean.getGoodsName();
                    Goods goods = new Goods(goodsId, goodsName);
                    gotoQuotePage(goods);
                }
            });
            
            return convertView;
        }
        
        public class ViewHolder {
            public View layout;
            public TextView tvPosition, tvStockName, tvStockCode, tvResetPosition, tvClearPosition;
            
            public ViewHolder(View layout) {
                this.layout = layout;
                
                tvPosition = (TextView) layout.findViewById(R.id.tv_position);
                tvStockName = (TextView) layout.findViewById(R.id.tv_stock_name);
                tvStockCode = (TextView) layout.findViewById(R.id.tv_stock_code);
                tvResetPosition = (TextView) layout.findViewById(R.id.tv_reset_position);
                tvClearPosition = (TextView) layout.findViewById(R.id.tv_clear_position);
            }
            
        }
        
    }
    
    private class CellBean {
        public String position, stockName, stockCode;

        public CellBean(String position, String stockName, String stockCode) {
            super();
            this.position = position;
            this.stockName = stockName;
            this.stockCode = stockCode;
        }
        
    }
    
}
