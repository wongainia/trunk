package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.QuoteUtils;

public class QuoteDiagnosePage extends PageImpl {

    private boolean isRequesting;
    private int currentGoodsId;
    
    private List<CellBean> listDatas = new ArrayList<CellBean>();

    private TextView tvEmpty;
    private View layoutLoading;
    private LinearLayout layoutContent;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quotediagnose);

        tvEmpty = (TextView) findViewById(R.id.page_quotediagnose_tv_empty);
        layoutLoading = findViewById(R.id.page_quotediagnose_layout_loading);
        layoutContent = (LinearLayout) findViewById(R.id.page_quotediagnose_layout_content);
        
        // 点击空白提示，重新加载数据
        tvEmpty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData();
            }
        });
        
        refreshViews();
    }

    @Override
    protected void initData() {
    }
    
    public void refreshViews() {
        addChildViews();
        
        // 如果有数据显示，就隐藏空白提示，否则显示
        if (listDatas.size() > 0) {
            tvEmpty.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            
            tvEmpty.setEnabled(false);
            tvEmpty.setText("暂无数据");
        }
    }
    
    /**
     * 添加子布局
     * */
    private void addChildViews() {
        layoutContent.removeAllViews();
        
        for (int i = 0; i < listDatas.size(); i++) {
            CellBean bean = listDatas.get(i);
            
            View view = LayoutInflater.from(getContext()).inflate(R.layout.page_quotediagnose_listitem, layoutContent, false);
            ViewHolder vh = new ViewHolder(view);
            
            if (TextUtils.isEmpty(bean.stockGrade)) {
                vh.layoutRank.setVisibility(View.GONE);
                vh.layoutContent.setVisibility(View.VISIBLE);

                vh.img.setImageResource(bean.imgResourceId);

                vh.tvTitle.getPaint().setFakeBoldText(true);
                vh.tvTitle.setText(bean.diagnoseTitle);

                vh.tvCompress.setText(bean.diagnoseContent);
            } else {
                vh.layoutContent.setVisibility(View.GONE);

                String[] t_args = bean.stockGrade.split(",");
                if (t_args.length < 13) {
                    vh.layoutRank.setVisibility(View.GONE);
                } else {
                    vh.layoutRank.setVisibility(View.VISIBLE);

                    String sGrade = t_args[9];
                    String sRank = t_args[12];

                    vh.tvGrade.setText(sGrade);

                    vh.tvRank.setText(String.format("超过了%s的股票", sRank));
                }
            }

            vh.layout.setEnabled(false);
            
            layoutContent.addView(view);
            
            View divider = new View(getContext());
            divider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundResource(R.drawable.img_light_divider_line);
            layoutContent.addView(divider);
        }
        
        if (layoutContent != null && layoutContent.getChildCount() > 0) {
            layoutContent.removeViewAt(layoutContent.getChildCount() - 1);            
        }
    }

    public void setGoodsId(int goodsId) {
        currentGoodsId = goodsId;
    }
    
    @Override
    public void requestData() {
        super.requestData();
        
        /*
         * 如果缓存有数据，直接显示缓存数据，如果缓存无数据，请求数据
         * */
        if (listDatas.size() > 0) {
            // 缓存有数据，直接显示缓存数据
            refreshViews();
        } else {
            // 缓存无数据，请求数据
            requestDiagnose();
            isRequesting = true;
            
            // 获取数据开始，显示加载进度条
            layoutLoading.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRequesting && layoutLoading != null) {
                        layoutLoading.setVisibility(View.GONE);

                        if (listDatas.size() == 0) {
                            // 请求失败时，可以点击空白再次刷新
                            tvEmpty.setEnabled(true);
                            tvEmpty.setText("更新失败，请点击重试");                            
                        }
                    }
                }
            }, 3000);
        }
    }
    
    private void requestDiagnose() {
        if (currentGoodsId > 0) {
            JSONObject jsObj = new JSONObject();
            
            try {
                String goodsCode = QuoteUtils.getStockCodeByGoodsId(String.valueOf(currentGoodsId));
                
                JSONArray arr = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put(KEY_GOODSID, goodsCode);
                JSONArray a = new JSONArray();
                a.add("AiChaoGu_StockDes_All" + goodsCode);
                a.add("sdstockscoreex" + goodsCode);
                obj.put(KEY_KEYS, a);
                
                arr.add(obj);
                jsObj.put(KEY_INFO, arr);
                jsObj.put(KEY_TOKEN, getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            requestInfo(jsObj, IDUtils.ID_STOCK_DIAGNOSE);
        }
    }
    
    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        super.updateFromInfo(pkg);
        int id = pkg.getRequestType();
        
        if (id == IDUtils.ID_STOCK_DIAGNOSE && pkg instanceof GlobalMessagePackage && isRequesting) {
            isRequesting = false;
            
            // 请求返回时，隐藏加载进度提示
            if (layoutLoading != null) {
                layoutLoading.setVisibility(View.GONE);
            }
            
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int code = jsObj.getIntValue(KEY_CODE);
                String msg = jsObj.getString(KEY_INFO);
                JSONArray jsArr = jsObj.getJSONArray(KEY_INFO);

                if (jsArr.size() > 0) {
                    boolean bHasGradeInfo = false;

                    JSONObject oneStock = jsArr.getJSONObject(0);
                    String goodsId = oneStock.getString(KEY_GOODSID);
                    JSONArray kvArr = oneStock.getJSONArray(KEY_REP);

                    String goodsCode = QuoteUtils.getStockCodeByGoodsId(String.valueOf(currentGoodsId));

                    for (int i = 0; i < kvArr.size(); i++) {
                        JSONObject kvObj = kvArr.getJSONObject(i);
                        String keyType = kvObj.getString(KEY_K);
                        if (keyType.equals("AiChaoGu_StockDes_All" + goodsCode)) {
                            String result = kvObj.getString(KEY_V);
                            parseResult(JSON.parseObject(result), listDatas);
                        } else if (keyType.equals("sdstockscoreex" + goodsCode)) {
                            bHasGradeInfo = true;
                            String result = kvObj.getString(KEY_V);

                            CellBean bean = new CellBean(result);
                            listDatas.add(0, bean);
                        }
                    }

                    // 返回数据成功时调用
                    addChildViews();
                    
                    if (listDatas.size() > 0) {
                        // 有数据
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("暂无数据，请点击重试");
                        tvEmpty.setEnabled(true);
                    }
                    
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            // 返回数据失败时调用
            tvEmpty.setEnabled(true);
            tvEmpty.setText("更新失败，请点击重试");
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
    
    private void parseResult(JSONObject js, List<CellBean> listInfos) {
        try {
            JSONArray arr = js.getJSONArray(KEY_ROOT);
            if (arr.size() > 0) {
                JSONObject obj = arr.getJSONObject(0);
                JSONArray arr1 = obj.getJSONArray("Data");
                if (arr1.size() > 0) {

                    JSONObject dataJs = arr1.getJSONObject(0);

                    if (dataJs.containsKey("StockTechDes")) {
                        String StockTechDes = dataJs.getString("StockTechDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_trend, "个股走势", StockTechDes);
                        listInfos.add(bean);
                    }

                    if (dataJs.containsKey("MsgDes")) {
                        String MsgDes = dataJs.getString("MsgDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_analyze, "消息预测", MsgDes);
                        listInfos.add(bean);
                    }

                    if (dataJs.containsKey("BasicDes")) {
                        String BasicDes = dataJs.getString("BasicDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_manage, "公司运营", BasicDes);
                        listInfos.add(bean);
                    }

                    if (dataJs.containsKey("FunDes")) {
                        String FunDes = dataJs.getString("FunDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_flow, "机构动向", FunDes);
                        listInfos.add(bean);
                    }

                    if (dataJs.containsKey("BlockTechDes")) {
                        String BlockTechDes = dataJs.getString("BlockTechDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_bktrend, "板块走势", BlockTechDes);
                        listInfos.add(bean);
                    }

                    if (dataJs.containsKey("IndustryDes")) {
                        String IndustryDes = dataJs.getString("IndustryDes");
                        CellBean bean = new CellBean(R.drawable.img_quote_diagnose_background, "行业背景", IndustryDes);
                        listInfos.add(bean);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private class ViewHolder {
        public View layout;

        public View layoutRank;
        public TextView tvGrade;
        public TextView tvRank;

        public View layoutContent;
        public ImageView img;
        public TextView tvTitle;
        public TextView tvCompress;

        public ViewHolder(View layout) {
            this.layout = layout;

            layoutRank = layout.findViewById(R.id.page_quotediagnose_listitem_layout_level);
            tvGrade = (TextView) layout.findViewById(R.id.page_quotediagnose_listitem_tv_grade);
            tvRank = (TextView) layout.findViewById(R.id.page_quotediagnose_listitem_tv_rank);
            layoutContent = layout.findViewById(R.id.page_quotediagnose_listitem_layout_content);
            img = (ImageView) layout.findViewById(R.id.page_quotediagnose_listitem_img_icon);
            tvTitle = (TextView) layout.findViewById(R.id.page_quotediagnose_listitem_tv_icon);
            tvCompress = (TextView) layout.findViewById(R.id.page_quotediagnose_listitem_tv_content);
        }
    }

    private class CellBean {
        private String stockGrade;

        private int imgResourceId;
        private String diagnoseTitle;
        private String diagnoseContent;

        public CellBean(int imgResourceId, String diagnoseTitle, String diagnoseContent) {
            this.stockGrade = stockGrade;
            this.imgResourceId = imgResourceId;
            this.diagnoseTitle = diagnoseTitle;
            this.diagnoseContent = diagnoseContent;
        }

        public CellBean(String stockGrade) {
            this.stockGrade = stockGrade;
        }

    }

}
