package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.infodetail.InfoDetailHome;
import cn.emoney.acg.page.share.infodetail.InfoDetailPage;
import cn.emoney.acg.util.DateUtils;
import cn.emoney.acg.util.MD5Util;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;
import cn.emoney.sky.libs.widget.RefreshListView.PostScrollListener;

public class MoreNewsPage extends PageImpl {

    public static final String EXTRA_KEY_NEWS_TYPE = "key_news_type";
    public static final String EXTRA_KEY_GOODS_ID = "key_goods_id";
    public static final String EXTRA_KEY_GOODS_NAME = "key_goods_name";

    private static final int MAX_NEWS_COUNT = 50;

    private int goodsId;
    private int infoType;
    private String goodName = "";
    private boolean isLoading;

    private List<CellBean> listDatas = new ArrayList<CellBean>();
    private NewsAdapter adapter;
    private ArrayList<Map<String, String>> listMessageMap = new ArrayList<Map<String, String>>();
    private List<String> listHasRead = new ArrayList<String>();

    private RefreshListView listView;
    private View layoutLoadMore;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_more_news);

        initViews();

        bindPageTitleBar(R.id.page_morenews_titlebar);
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);

        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_GOODS_ID)) {
                goodsId = arguments.getInt(EXTRA_KEY_GOODS_ID, 0);
            }
            if (arguments.containsKey(EXTRA_KEY_NEWS_TYPE)) {
                infoType = arguments.getInt(EXTRA_KEY_NEWS_TYPE, 0);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_NAME)) {
                goodName = arguments.getString(EXTRA_KEY_GOODS_NAME);
            }

            requestStockInfos();

        }
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        // 获取sqlite中存储的已经读到的消息的集合
        if (infoType == QuoteStockPage.INFO_TYPE_NEWS) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NEWS, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                listHasRead.clear();
                listHasRead.addAll(mLstReaded);
            }
        } else if (infoType == QuoteStockPage.INFO_TYPE_NOTICE) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_NOTICE, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                listHasRead.clear();
                listHasRead.addAll(mLstReaded);
            }
        } else if (infoType == QuoteStockPage.INFO_TYPE_REPORT) {
            String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_INFODETAIL_QUOTE_REPORT, null);
            if (aryReaded != null) {
                List<String> mLstReaded = Arrays.asList(aryReaded);
                listHasRead.clear();
                listHasRead.addAll(mLstReaded);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void initViews() {
        listView = (RefreshListView) findViewById(R.id.page_morenews_list);
        View listFooter = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_listfooter_loadmore, listView, false);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);

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

                boolean isLoadMore = (firstVisibleItem + visibleItemCount >= totalItemCount) && totalItemCount < MAX_NEWS_COUNT 
                        && !isLoading && isScrolling && isHasLoadMore == false;
                if (isLoadMore) {
                    loadMoreData();
                    isHasLoadMore = true;
                }
            }
        });

        adapter = new NewsAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);
        listView.addFooterView(listFooter);
    }

    private void loadMoreData() {
        int endFlag = listDatas.get(listDatas.size() - 1).endFlag;

        if (endFlag == 0) {
            // load more data
            requestStockInfos();

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

    private void loadComplete() {
        layoutLoadMore.setVisibility(View.GONE);
        isLoading = false;
    }

    public void gotoInfoDetail(ArrayList<Map<String, String>> listItem, int index, String infoType) {
        PageIntent intent = new PageIntent(this, InfoDetailHome.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(InfoDetailHome.EXTRA_KEY_LIST_ITEMS, listItem);
        bundle.putInt(InfoDetailHome.EXTRA_KEY_LIST_INDEX, index);
        bundle.putString(InfoDetailHome.EXTRA_KEY_INFO_TYPE, infoType);
        intent.setArguments(bundle);

        intent.setSupportAnimation(true);

        startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    /**
     * 获取最后一条消息的sortid
     * */
    private int getLastInfoSortId() {
        if (listDatas != null && listDatas.size() > 0) {
            return listDatas.get(listDatas.size() - 1).sortId;
        }

        return 0;
    }

    private void requestStockInfos() {
        if (goodsId > 0) {
            JSONObject jsObj = new JSONObject();
            
            String cls = null;
            if (infoType == QuoteStockPage.INFO_TYPE_NEWS) {
                cls = "个股新闻";
            } else if (infoType == QuoteStockPage.INFO_TYPE_NOTICE) {
                cls = "个股公告";
            } else if (infoType == QuoteStockPage.INFO_TYPE_REPORT) {
                cls = "个股研报";
            }
            
            try {
                jsObj.put(KEY_CLS, cls);
                jsObj.put(KEY_DIRECTION, 1);
                jsObj.put(KEY_SORTID, getLastInfoSortId());
                jsObj.put(KEY_STOCK, String.valueOf(goodsId));
                jsObj.put("size", 20);
                jsObj.put(KEY_TOKEN, getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            requestInfo(jsObj, IDUtils.ID_STOCK_NEWS);            
        }
    }

    public void updateFromInfo(InfoPackageImpl pkg) {
        int id = pkg.getRequestType();

        if (id == IDUtils.ID_STOCK_NEWS) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();
            if (mc == null || mc.getMsgData() == null) {
                return;
            }

            String msgData = mc.getMsgData();

            try {
                JSONObject jsObj = JSON.parseObject(msgData);
                int end = jsObj.getIntValue(KEY_END);
                JSONArray newsArr = jsObj.getJSONArray(KEY_NEWS);

                for (int i = 0; i < newsArr.size(); i++) {
                    if (listDatas != null && listDatas.size() >= MAX_NEWS_COUNT) {
                        break;
                    }

                    JSONObject newsObj = newsArr.getJSONObject(i);

                    String time = newsObj.getString(KEY_PT);
                    String title = newsObj.getString(KEY_TITLE);
                    String url = newsObj.getString(KEY_CONTENT_URL);
                    String from = newsObj.getString(KEY_FROM);
                    int sortId = newsObj.getIntValue("sortid");

                    String sortcls = null;
                    switch (infoType) {
                        case QuoteStockPage.INFO_TYPE_NEWS:
                            sortcls = "个股新闻";
                            break;
                        case QuoteStockPage.INFO_TYPE_NOTICE:
                            sortcls = "个股公告";
                            break;
                        case QuoteStockPage.INFO_TYPE_REPORT:
                            sortcls = "个股研报";
                            break;
                        default:
                            break;
                    }

                    Map<String, String> map = new HashMap<String, String>();
                    map.put(InfoDetailPage.EXTRA_KEY_CONTENT_URL, url);
                    map.put(InfoDetailPage.EXTRA_KEY_TITLE, title);
                    map.put(InfoDetailPage.EXTRA_KEY_TIME, time);
                    map.put(InfoDetailPage.EXTRA_KEY_FROM, from);
                    map.put(InfoDetailPage.EXTRA_KEY_SORTCLS, sortcls);
                    map.put(InfoDetailPage.EXTRA_KEY_AUTHOR, "");
                    map.put(InfoDetailPage.EXTRA_KEY_RELATED_STOCKS, "");

                    listMessageMap.add(map);

                    CellBean bean = new CellBean(time, title, url, sortId, end);
                    listDatas.add(bean);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();

            loadComplete();
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        String title = "";
        if (infoType == QuoteStockPage.INFO_TYPE_NOTICE) {
            title = goodName + " 公告";
        } else if (infoType == QuoteStockPage.INFO_TYPE_REPORT) {
            title = goodName + " 研报";
        } else {
            title = goodName + " 新闻";
        }

        BarMenuTextItem centerItem = new BarMenuTextItem(1, title);
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        super.onPageTitleBarMenuItemSelected(menuitem);

        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    private class NewsAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<CellBean> listDatas;

        public NewsAdapter(Context context, List<CellBean> listDatas) {
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
                convertView = inflater.inflate(R.layout.page_quote_listitem_news, null, false);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            final CellBean bean = listDatas.get(position);

            vh.tvTime.setText(DateUtils.formatInfoDate(bean.time, DateUtils.mFormatDayM_D));
            vh.tvTitle.setText(bean.title);

            String md5Flag = MD5Util.md5(bean.url);
            if (listHasRead != null && listHasRead.contains(md5Flag)) {
                vh.tvTitle.setTextColor(getResources().getColor(R.color.t3));
            } else {
                vh.tvTitle.setTextColor(getResources().getColor(R.color.t1));
            }

            final int index = position;
            vh.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 打开消息详情界面
                    switch (infoType) {
                        case QuoteStockPage.INFO_TYPE_NEWS:
                            gotoInfoDetail(listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_NEWS);
                            break;
                        case QuoteStockPage.INFO_TYPE_NOTICE:
                            gotoInfoDetail(listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_NOTICE);
                            break;
                        case QuoteStockPage.INFO_TYPE_REPORT:
                            gotoInfoDetail(listMessageMap, index, DataModule.G_KEY_INFODETAIL_QUOTE_REPORT);
                            break;
                        default:
                            break;
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;
            public TextView tvTime, tvTitle;

            public ViewHolder(View view) {
                layout = view;

                tvTime = (TextView) view.findViewById(R.id.page_quote_listitem_tv_time);
                tvTitle = (TextView) view.findViewById(R.id.page_quote_listitem_tv_title);
            }
        }

    }

    private class CellBean {
        public String time, title, url;
        public int sortId, endFlag;

        public CellBean(String time, String title, String url, int sortId, int endFlag) {
            super();
            this.time = time;
            this.title = title;
            this.url = url;
            this.sortId = sortId;
            this.endFlag = endFlag;
        }

    }

}
