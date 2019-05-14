package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.data.protocol.quote.RelateBKsPackage;
import cn.emoney.acg.data.protocol.quote.RelateBKsReply.RelateBKs_Reply;
import cn.emoney.acg.data.protocol.quote.RelateBKsRequest.RelateBKs_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.page.Page;

public class QuoteRelativePage extends PageImpl {

    private final short REQUEST_TYPE_RELATIVE = 1001;
    private final short REQUEST_TYPE_RELATIVE_INFO = 1002;
    
    /**
     * 是否正在请求个股关联版块
     * */
    private boolean isRequesting;
    private int currentGoodsId;
    
    private List<CellBean> listDatas = new ArrayList<CellBean>();
    private MyAdapter adapter;
    
    private TextView tvEmpty;
    private View layoutLoading;
    private ListView listView;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quoterelative);
        
        tvEmpty = (TextView) findViewById(R.id.page_quoterelative_tv_empty);
        layoutLoading = findViewById(R.id.page_quoterelative_layout_loading);
        listView = (ListView) findViewById(R.id.page_quoterelative_list);
        
        adapter = new MyAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);
        
        // 点击空白提示，重新加载数据
        tvEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData();
            }
        });
        
    }

    @Override
    protected void initData() {}
    
    @Override
    public void requestData() {
        super.requestData();
        
        /*
         * 不缓存数据，直接刷新
         * */
        requestRelativeBks();
        
        isRequesting = true;
        
        // 获取数据开始，且当前无数据时，显示加载进度条
        if (listDatas.size() == 0) {
            layoutLoading.setVisibility(View.VISIBLE);
            
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRequesting && layoutLoading != null) {
                        isRequesting = false;
                        
                        layoutLoading.setVisibility(View.GONE);
                        
                        // 请求失败时，可以点击空白再次刷新
                        tvEmpty.setEnabled(true);
                        tvEmpty.setText("更新失败，请点击重试");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }, DataModule.REQUEST_MAX_LIMIT_TIME);
        }
    }
    
    public void setGoodsId(int goodsId) {
        currentGoodsId = goodsId;
    }
    
    private void resizeListView() {
        if (listView != null) {
            int size = listDatas.size();
            int oneHeigth = FontUtils.dip2px(getContext(), 56);
            
            LayoutParams params = listView.getLayoutParams();
            int totalHeight = (oneHeigth + 1) * size - 1;
            params.height = totalHeight;
            listView.setLayoutParams(params);
        }
    }
    
    /**
     * 获取个股关联版块列表
     * */
    private void requestRelativeBks() {
        // 1. 构造QuoteHead，传入request type
        QuoteHead quoteHead = new QuoteHead(REQUEST_TYPE_RELATIVE);
        // 2. 传入QuoteHead，构造Package
        RelateBKsPackage pkg = new RelateBKsPackage(quoteHead);
        // 3. 构造Request并设置参数
        RelateBKs_Request request = RelateBKs_Request.newBuilder().setGoodsId(currentGoodsId).build();
        // 4. 将Request设置到Package中
        pkg.setRequest(request);
        // 5. 发送请求requestQuote
        requestQuote(pkg, ID_RELATIVE_BKS);
    }
    
    /**
     * 获取各关联版块详细信息
     * */
    private void requestRelativeBkInfos(List<Integer> listIds) {
        ArrayList<Integer> listBksId = new ArrayList<Integer>();
        for (Integer integer : listIds) {
            listBksId.add(integer);
        }
        
        ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
        goodsFiled.add(GoodsParams.GOODS_NAME);
        goodsFiled.add(GoodsParams.ZDF);
        goodsFiled.add(GoodsParams.RISE_HEAD_GOODSID); // 678领涨id
        goodsFiled.add(GoodsParams.RISE_HEAD_GOODSZDF); // -20001领涨股涨幅
        goodsFiled.add(GoodsParams.RISE_HEAD_GOODSNAME); // -20003领涨股的名称,服务器暂未实现返回"0"
        
        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_RELATIVE_INFO));
        pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(4)
                .addAllGoodsId(listBksId).addAllReqFields(goodsFiled)
                .setSortField(-9999).setSortOrder(false).setReqBegin(0).setReqSize(1)
                .setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
        requestQuote(pkg, IDUtils.DynaValueData);
    }
    
    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);
        int reqType = pkg.getRequestType();
        
        if (reqType == REQUEST_TYPE_RELATIVE && pkg instanceof RelateBKsPackage && isRequesting) {
            RelateBKsPackage goodsTable = (RelateBKsPackage) pkg;
            RelateBKs_Reply reply = goodsTable.getResponse();

            if (reply != null && reply.getBksIdList() != null) {
                // 返回成功，请求版块列表详细信息
                List<Integer> listBksId = reply.getBksIdList();
                
                if (listBksId.size() > 0) {
                    requestRelativeBkInfos(listBksId);
                } else {
                    isRequesting = false;
                    
                    // 无相关版块，显示无数据，请点击重试
                    layoutLoading.setVisibility(View.GONE);
                    
                    tvEmpty.setEnabled(true);
                    tvEmpty.setText("暂无数据，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                isRequesting = false;
                
                // 返回失败，隐藏加载，显示加载失败，请重试
                layoutLoading.setVisibility(View.GONE);
                
                tvEmpty.setEnabled(true);
                tvEmpty.setText("更新失败，请点击重试");
                tvEmpty.setVisibility(View.VISIBLE);
            }
        } else if (reqType == REQUEST_TYPE_RELATIVE_INFO && pkg instanceof DynaValueDataPackage && isRequesting) {
            DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
            DynaValueData_Reply gr = goodsTable.getResponse();
            
            if (gr != null && gr.getQuotaValueList() != null) {
                List<DynaQuota> listDynaQuota = gr.getQuotaValueList();
                
                if (listDynaQuota.size() > 0) {
                    isRequesting = false;
                    
                    layoutLoading.setVisibility(View.GONE);
                    
                    tvEmpty.setEnabled(false);
                    tvEmpty.setText("暂无数据");
                    tvEmpty.setVisibility(View.GONE);
                    
                    List<Integer> fieldIds = gr.getRepFieldsList();
                    
                    int indexZDF = fieldIds.indexOf(GoodsParams.ZDF);
                    int indexName = fieldIds.indexOf(GoodsParams.GOODS_NAME);
                    int indexHeadStock_id = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSID);
                    int indexHeadStock_zf = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSZDF);
                    int indexHeadStock_name = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSNAME);
                    
                    listDatas.clear();
                    for (int i = 0; i < listDynaQuota.size(); i++) {
                        DynaQuota quote = listDynaQuota.get(i);
                        int bkId = quote.getGoodsId();
                        
                        String stockZdf = quote.getRepFieldValue(indexHeadStock_zf);
                        String stockName = "";
                        stockName = quote.getRepFieldValue(indexHeadStock_name);
                        if (stockName == null || stockName.equals("") || stockName.equals("0")) {
                            String headStock_id = quote.getRepFieldValue(indexHeadStock_id);
                            List<Goods> lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(headStock_id), 1);
                            if (lstGoods != null && lstGoods.size() > 0) {
                                stockName = lstGoods.get(0).getGoodsName();
                            }
                        }

                        String bkZdf = quote.getRepFieldValue(indexZDF);
                        String bkName = quote.getRepFieldValue(indexName);

                        CellBean bean = new CellBean(bkId, bkName, bkZdf, stockName, stockZdf);
                        listDatas.add(bean);
                    }
                    
                    // 返回数据成功时调用
                    resizeListView();
                    adapter.notifyDataSetChanged();
                } else {
                    isRequesting = false;
                    
                    // 无相关版块，显示无数据，请点击重试
                    layoutLoading.setVisibility(View.GONE);
                    
                    tvEmpty.setEnabled(true);
                    tvEmpty.setText("暂无数据，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                isRequesting = false;
                
                // 返回失败，隐藏加载，显示加载失败，请重试
                layoutLoading.setVisibility(View.GONE);
                
                tvEmpty.setEnabled(true);
                tvEmpty.setText("更新失败，请点击重试");
                tvEmpty.setVisibility(View.VISIBLE);
            }

        }
        
    }
    
    private class MyAdapter extends BaseAdapter {
        
        private List<CellBean> listDatas;
        private LayoutInflater inflater;
        
        public MyAdapter(Context context, List<CellBean> listDatas) {
            this.listDatas = listDatas;
            inflater = LayoutInflater.from(context);
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
                convertView = inflater.inflate(R.layout.page_quoterelative_listitem, parent, false);
                
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            
            final CellBean bean = listDatas.get(position);
            vh.tvBkName.setText(bean.bkName);
            vh.tvStockName.setText(bean.stockName);
            vh.tvStockZdf.setText(DataUtils.getSignedZDF(bean.stockZdf));
            vh.tvBkZdf.setText(DataUtils.getSignedZDF(bean.bkZdf));
            vh.tvBkZdf.setBackgroundColor(getZDPColor(FontUtils.getColorByZDF(bean.bkZdf)));
            
            vh.layout.setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    // 跳转到版块详情界面
                    Goods goods = new Goods(bean.bkId, bean.bkName);
                    Page page = QuoteRelativePage.this.getParent();
                    QuoteJump.gotoQuote(page, goods);
                }
            });
            
            return convertView;
        }
        
        private class ViewHolder {
            public View layout;
            public TextView tvBkName, tvStockName, tvStockZdf, tvBkZdf;
            
            public ViewHolder(View layout) {
                this.layout = layout;
                
                this.tvBkName = (TextView) layout.findViewById(R.id.page_quoterelative_listitem_tv_bkname);
                this.tvStockName = (TextView) layout.findViewById(R.id.page_quoterelative_listitem_tv_stockname);
                this.tvStockZdf = (TextView) layout.findViewById(R.id.page_quoterelative_listitem_tv_stockzdf);
                this.tvBkZdf = (TextView) layout.findViewById(R.id.page_quoterelative_listitem_tv_bkzdf);
            }
        }
        
    }
    
    private class CellBean {
        public int bkId;
        public String bkName, bkZdf, stockName, stockZdf;

        public CellBean(int bkId, String bkName, String bkZdf, String stockName, String stockZdf) {
            this.bkId = bkId;
            this.bkName = bkName;
            this.bkZdf = bkZdf;
            this.stockName = stockName;
            this.stockZdf = stockZdf;
        }
        
    }

}
