package cn.emoney.acg.page.share.quote;

import android.os.Handler;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.DataUtils;

public class QuotePage extends PageImpl {
    
    protected final short REQUEST_TYPE_QUOTATION = 1101;    // 请求行情
    protected final short REQUEST_TYPE_STOCK_RANK = 1102;    // 请求版块列表
    
    protected boolean bPageAlive;
    protected int currentGoodsId;
    
    protected OnNoticeRefresh mOnNoticeRefresh;
    protected OnChangePeriodListener onChangePeriodListener;
    protected Goods currentGoods;

    @Override
    protected void initPage() {
    }

    @Override
    protected void initData() {
    }
    
    @Override
    protected void onPageResume() {
        super.onPageResume();
        bPageAlive = true;
        
        // 用户可能快速滑动切换多个界面，每个界面停留时间很短，这时就没有必要加载迅速切换过的界面的数据
        // 设置一个延迟，如果延迟结束时，仍留在此界面，才去请求数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bPageAlive) {
                    /*
                     * 启动自动刷新，放在requestData()中 1. 获取指标数据 2. 获取分时线（按设置刷新频率刷新）、K线数据（5分钟刷一次） 3. 获取版块成分个股列表（按设置刷新频率刷新）
                     */
                    if (!getIsAutoRefresh()) {
                        if (getUserVisibleHint()) {
                            startRequestTask();
                        } else {
                            requestData();
                        }
                    }
                }
            }
        }, QuoteHome.REQ_DELAY_TIME);
    }
    
    @Override
    protected void onPagePause() {
        super.onPagePause();
        
        bPageAlive = false;
    }
    
    public interface OnNoticeRefresh {
        public void refreshNotice(String notice);
    }
    
    public void setOnNoticeRefresh(OnNoticeRefresh onNoticeRefresh) {
        this.mOnNoticeRefresh = onNoticeRefresh;
    }
    
    public void setOnChangePeriodListener(OnChangePeriodListener onChangePeriodListener) {
        this.onChangePeriodListener = onChangePeriodListener;
    }
    
    public void setGoods(Goods goods) {
        currentGoods = goods;
        
        if (currentGoods != null) {
            currentGoodsId = currentGoods.getGoodsId();            
        }
    }
    
    protected String getTransactionStateInfo(int marketDate, int marketTime) {
        String s_transactionStatus = "";
        String s_date = DataUtils.formatDateM_D(String.valueOf(marketDate), "-");
        String s_time = DataUtils.formatTimeH_M_S(String.valueOf(marketTime));

        if (s_date != null && !s_date.equals("") && s_time != null && !s_time.equals("")) {
            if ((marketTime >= 91500 && marketTime <= 113000) || (marketTime >= 130000 && marketTime <= 150000)) {
                s_transactionStatus = "交易中 ";
            } else {
                if (marketDate == DataModule.G_CURRENT_SERVER_DATE) {
                    if (marketTime < 91500) {
                        s_transactionStatus = "开盘前 ";
                    } else if (marketTime > 150000) {
                        s_transactionStatus = "已收盘 ";
                    } else {
                        s_transactionStatus = "午盘休息 ";
                    }
                } else {
                    s_transactionStatus = "非交易时间 ";
                }
            }

            return s_transactionStatus + s_date + " " + s_time;
        }

        return null;
    }

}
