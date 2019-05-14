package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.FinanceParams;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.StatValueDataPackage;
import cn.emoney.acg.data.protocol.quote.StatValueDataReply.StatValueData_Reply;
import cn.emoney.acg.data.protocol.quote.StatValueDataReply.StatValueData_Reply.StatValue;
import cn.emoney.acg.data.protocol.quote.StatValueDataRequest.StatValueData_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.acg.view.OnClickEffectiveListener;

public class QuoteFinalcialPage extends PageImpl {
    
    private final short REQUEST_TYPE_FINANCIAL = 1001;
    
    private int currentGoodsId;
    private String syl, sjl;
    
    private TextView tvReportDate;
    private TextView tvZgb, tvLtag, tvSyl, tvSjl;
    private TextView tvMgsy, tvMgjyxjl, tvMggjj, tvMgwfplr, tvMgjzc, tvJzcsyl;
    private TextView tvZysr, tvZylr, tvZysrtb, tvZylrtb;
    private TextView tvJlrtb, tvXsmll, tvGdqyb, tvZcfzl, tvYfzk, tvYszk;
    
    @Override
    protected void initPage() {
        setContentView(R.layout.page_quotefinancial);
        
        initViews();
    }
    
    @Override
    protected void initData() {}
    
    private void initViews() {
        View layoutMore = findViewById(R.id.page_quotefinalcial_layout_more);
        tvReportDate = (TextView) findViewById(R.id.page_quotefinalcial_tv_report_date);
        
        tvZgb = (TextView) findViewById(R.id.page_quotefinancial_tv_zgb);
        tvLtag = (TextView) findViewById(R.id.page_quotefinancial_tv_ltag);
        tvSyl = (TextView) findViewById(R.id.page_quotefinancial_tv_syl);
        tvSjl = (TextView) findViewById(R.id.page_quotefinancial_tv_sjl);
        
        tvMgsy = (TextView) findViewById(R.id.page_quotefinancial_tv_mgsy);
        tvMgjyxjl = (TextView) findViewById(R.id.page_quotefinancial_tv_mgjyxjl);
        tvMggjj = (TextView) findViewById(R.id.page_quotefinancial_tv_mggjj);
        tvMgwfplr = (TextView) findViewById(R.id.page_quotefinancial_tv_mgwfplr);
        tvMgjzc = (TextView) findViewById(R.id.page_quotefinancial_tv_mgjzc);
        tvJzcsyl = (TextView) findViewById(R.id.page_quotefinancial_tv_jjcsyl);
        
        tvZysr = (TextView) findViewById(R.id.page_quotefinancial_tv_zysr);
        tvZylr = (TextView) findViewById(R.id.page_quotefinancial_tv_zylr);
        tvZysrtb = (TextView) findViewById(R.id.page_quotefinancial_tv_zysrtb);
        tvZylrtb = (TextView) findViewById(R.id.page_quotefinancial_tv_zylrtb);
        
        tvJlrtb = (TextView) findViewById(R.id.page_quotefinancial_tv_jlrtb);
        tvXsmll = (TextView) findViewById(R.id.page_quotefinancial_tv_xsmll);
        tvGdqyb = (TextView) findViewById(R.id.page_quotefinancial_tv_gdqyb);
        tvZcfzl = (TextView) findViewById(R.id.page_quotefinancial_tv_zcfzb);
        tvYfzk = (TextView) findViewById(R.id.page_quotefinancial_tv_yfzk);
        tvYszk = (TextView) findViewById(R.id.page_quotefinancial_tv_ysk);
        
        layoutMore.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                Goods goods = QuoteUtils.getGoodsByGoodsId(currentGoodsId, QuoteFinalcialPage.this);
                
                Bundle bundle = new Bundle();
                bundle.putString(FinancialReportPage.EXTRA_KEY_GOODS_CODE, goods.getGoodsCode());
                bundle.putString(FinancialReportPage.EXTRA_KEY_GOODS_NAME, goods.getGoodsName());
                startModule(bundle, FinancialReportHome.class);
            }
        });
        
        tvSyl.setText(syl);
        tvSjl.setText(sjl);
    }
    
    @Override
    public void requestData() {
        super.requestData();
        
        requestRelativeBks();
    }
    
    public void setGoodsId(int goodsId) {
        currentGoodsId = goodsId;
    }
    
    public void setSyl(String syl) {
        this.syl = syl;
        
        if (tvSyl != null) {
            tvSyl.setText(syl);
        }
    }

    public void setSjl(String sjl) {
        this.sjl = sjl;
        
        if (tvSjl != null) {
            tvSjl.setText(sjl);
        }
    }

    private void requestRelativeBks() {
        ArrayList<Integer> listFields = new ArrayList<Integer>();
        
        // 1. 构造QuoteHead，传入request type
        QuoteHead quoteHead = new QuoteHead(REQUEST_TYPE_FINANCIAL);
        // 2. 传入QuoteHead，构造Package
        StatValueDataPackage pkg = new StatValueDataPackage(quoteHead);
        // 3. 构造Request并设置参数
        StatValueData_Request request = StatValueData_Request.newBuilder().addGoodsId(currentGoodsId).addAllReqFields(listFields).build();
        // 4. 将Request设置到Package中
        pkg.setRequest(request);
        // 5. 发送请求requestQuote
        requestQuote(pkg, ID_STOCK_FINALCIAL_REPORT);
    }
    
    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);
        int reqType = pkg.getRequestType();
        
        if (reqType == REQUEST_TYPE_FINANCIAL && pkg instanceof StatValueDataPackage) {
            StatValueDataPackage dataPackage = (StatValueDataPackage) pkg;
            StatValueData_Reply reply = dataPackage.getResponse();
            
            if (reply != null && reply.getRepFieldsList() != null && reply.getStaticValueList() != null) {
                List<Integer> listReqFields = reply.getRepFieldsList();
                List<StatValue> listValues = reply.getStaticValueList();
                
                if (listValues.size() > 0) {
                    StatValue sv = listValues.get(0);
                    
                    // 报告日期
                    if (listReqFields.contains(FinanceParams.EndDate)) {
                        String date = sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.EndDate));
                        if (!TextUtils.isEmpty(date) && date.length() == 8) {
                            date = date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6, 8);
                        }
                        date = "报告日期： " + date;
                        tvReportDate.setText(date);
                    }
                    
                    // 总股本
                    if (listReqFields.contains(FinanceParams.ZGB)) {
                        tvZgb.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZGB))) + "股");
                    }
                    
                    // 流通股本
                    if (listReqFields.contains(FinanceParams.LTGB)) {
                        tvLtag.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.LTGB))) + "股");
                    }
                    
                    // 每股收益
                    if (listReqFields.contains(FinanceParams.MGSY)) {
                        tvMgsy.setText(DataUtils.getFinancialValue(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.MGSY))) + "元");
                    }
                    
                    // 每股经营现金流
                    if (listReqFields.contains(FinanceParams.MGJYXJL)) {
                        tvMgjyxjl.setText(DataUtils.getFinancialValue(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.MGJYXJL))) + "元");
                    }
                    
                    // 每股公积金
                    if (listReqFields.contains(FinanceParams.MGGJJ)) {
                        tvMggjj.setText(DataUtils.getFinancialValue(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.MGGJJ))) + "元");
                    }
                    
                    // 每股未分配利润
                    if (listReqFields.contains(FinanceParams.MGWFPLR)) {
                        tvMgwfplr.setText(DataUtils.getFinancialValue(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.MGWFPLR))) + "元");
                    }
                    
                    // 每股净资产
                    if (listReqFields.contains(FinanceParams.MGJZC)) {
                        tvMgjzc.setText(DataUtils.getFinancialValue(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.MGJZC))) + "元");
                    }
                    
                    // 净资产收益率
                    if (listReqFields.contains(FinanceParams.JZCSYL)) {
                        tvJzcsyl.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.JZCSYL))));
                    }
                    
                    // 主营收入
                    if (listReqFields.contains(FinanceParams.ZYSR)) {
                        tvZysr.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZYSR))) + "元");
                    }
                    
                    // 主营利润
                    if (listReqFields.contains(FinanceParams.ZYLR)) {
                        tvZylr.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZYLR))) + "元");
                    }
                    
                    // 主营收入同比
                    if (listReqFields.contains(FinanceParams.ZYSRTB)) {
                        tvZysrtb.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZYSRTB))));
                    }
                    
                    // 主营利润同比
                    if (listReqFields.contains(FinanceParams.ZYLRTB)) {
                        tvZylrtb.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZYLRTB))));
                    }
                    
                    // 净利润同比
                    if (listReqFields.contains(FinanceParams.JLRTB)) {
                        tvJlrtb.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.JLRTB))));
                    }
                    
                    // 销售毛利率
                    if (listReqFields.contains(FinanceParams.XSMLL)) {
                        tvXsmll.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.XSMLL))));
                    }
                    
                    // 股东权益比
                    if (listReqFields.contains(FinanceParams.GDQYB)) {
                        tvGdqyb.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.GDQYB))));
                    }
                    
                    // 资产负债率
                    if (listReqFields.contains(FinanceParams.ZCFZL)) {
                        tvZcfzl.setText(DataUtils.getFinancialRate(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.ZCFZL))));
                    }
                    
                    // 预付账款
                    if (listReqFields.contains(FinanceParams.YFZK)) {
                        tvYfzk.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.YFZK))) + "元");
                    }
                    
                    // 应收账款
                    if (listReqFields.contains(FinanceParams.YSZK)) {
                        tvYszk.setText(DataUtils.getFinancialAmount(sv.getRepFieldValue(listReqFields.indexOf(FinanceParams.YSZK))) + "元");
                    }
                    
                }
            }
            
        }
    }

}
