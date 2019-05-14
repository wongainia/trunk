package cn.emoney.acg.page.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.data.protocol.quote.DynaValueDataRequest.DynaValueData_Request;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.utils.SortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper;

public class RankBkPage extends PageImpl {

	public final static int REQUEST_QUOTE_SIZE = 30;
	
	public static final String KEY_RANK_TYPE = "rank_type";
	public static final int RANK_TYPE_RISE = 2001;
	public static final int RANK_TYPE_FALL = 2002;

	public final static int BK_TYPE_GN = 0;
	public final static int BK_TYPE_HY = 1;
	public final static int BK_TYPE_DQ = 2;
	private ListView mListView = null;

	private RankBKAdapter mAdapter = null;
	private List<Map<String, Object>> mLstData = new ArrayList<Map<String, Object>>();

	private int mCurrType = BK_TYPE_GN;
	private final String ITEM_ZDF = "item_zdf";
	private final String ITEM_BK_NAME = "item_bk_name";
	private final String ITEM_BK_ID = "item_bk_id";
	private final String ITEM_BK_PRICE = "item_bk_price";
	private final String ITEM_QUOTECOLOR = "item_quotecolor";
	private final String ITEM_HEAD_STOCK_NAME = "item_head_stock_name";
	private final String ITEM_HEAD_STOCK_ZF = "item_head_stock_zf";

	private LinearLayout mLlListFooter = null;

	TextView mTvBKZdf = null;
	TextView mTvBKName = null;
	TextView mTvBKRiseStock = null;
	View mHeaderBottomLine = null;
	
	private SymbolSortHelper mSortHelper = null;

	/**
	 * 0: 从头请求; 1: 加载更多; 2: 自动刷新数据
	 */
	private int mRequestType = 0; 
	private int mAlreadyRequestCount = 0;
	private boolean mHasMore = true;
	private boolean mIsLoadFinish = true;
	private boolean mbCurrentSortOrder = true;
	private int mCurrcentSortField = GoodsParams.ZDF;

	public RankBkPage() {
	}

	@Override
	protected void initPage() {
		setContentView(R.layout.page_rank_bk);

		mHeaderBottomLine = findViewById(R.id.rankbk_header_bottom_line);
		mLlListFooter = (LinearLayout) View.inflate(getContext(), R.layout.page_rank_bk_list_loadmore, null);
		mListView = (ListView) findViewById(R.id.rankbk_listview);
		
		if (mListView != null) {
			mListView.addFooterView(mLlListFooter);

			mAdapter = new RankBKAdapter();
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
					ArrayList<Goods> lstGoods = new ArrayList<Goods>();
					for (int i = 0; i < mLstData.size(); i++) {
						Map<String, Object> map = mLstData.get(i);
						String name = (String) map.get(ITEM_BK_NAME);
						int id = (Integer) map.get(ITEM_BK_ID);
						lstGoods.add(new Goods(id, name));
					}
					QuoteJump.gotoQuote(RankBkPage.this, lstGoods, index);
//					gotoQuote(lstGoods, index);
				}
			});
		}
		mListView.removeFooterView(mLlListFooter);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int lastItemid = mListView.getLastVisiblePosition(); // 获取当前屏幕最后Item的ID
				if ((lastItemid + 1) == totalItemCount && mIsLoadFinish == true) { // 达到数据的最后一条记录
					if (totalItemCount > 0) {
						if (mHasMore) {
							mIsLoadFinish = false;
							mListView.addFooterView(mLlListFooter);

							mRequestType = 1;
							requestBkRank(mbCurrentSortOrder, mCurrcentSortField, mAlreadyRequestCount, REQUEST_QUOTE_SIZE);
						}
					}
				}
			}
		});

		mSortHelper = new SymbolSortHelper();
		mSortHelper.setItemTextColor(RColor(R.color.t1));
		mSortHelper.setItemSelectedTextColor(getResources().getColor(R.color.c4));
		mSortHelper.setStrPadding(" ");
		mSortHelper.updateSort();

		mTvBKName = (TextView) findViewById(R.id.rankbk_tv_header_bkName);
		mTvBKZdf = (TextView) findViewById(R.id.rankbk_tv_header_bk_df);
		mTvBKRiseStock = (TextView) findViewById(R.id.rankbk_tv_header_ledstock_zdf);

		mSortHelper.addSortItem(mTvBKZdf, SortHelper.SORT_RISE | SortHelper.SORT_FALL);

		mCurrcentSortField = GoodsParams.ZDF;
		mSortHelper.setDefaultSort(mTvBKZdf, mbCurrentSortOrder ? SortHelper.SORT_RISE : SortHelper.SORT_FALL);

		mSortHelper.setOnSortListener(new SymbolSortHelper.OnSortListener() {

			@Override
			public void onSort(TextView view, int sortType) {
				int id = view.getId();

				if (id == R.id.rankbk_tv_header_bk_df) {
					if (sortType == SortHelper.SORT_RISE) {
						mRequestType = 0;
						mCurrcentSortField = GoodsParams.ZDF;
						mbCurrentSortOrder = true;
						requestBkRank(true, GoodsParams.ZDF, 0, REQUEST_QUOTE_SIZE);
					} else if (sortType == SortHelper.SORT_FALL) {
						mRequestType = 0;
						mCurrcentSortField = GoodsParams.ZDF;
						mbCurrentSortOrder = false;
						requestBkRank(false, GoodsParams.ZDF, 0, REQUEST_QUOTE_SIZE);
					}
				} else if (id == R.id.rankbk_tv_header_bkName) {
					if (sortType == SortHelper.SORT_RISE) {
						mRequestType = 0;
						mCurrcentSortField = GoodsParams.GOODS_CODE;
						mbCurrentSortOrder = true;
						requestBkRank(true, GoodsParams.GOODS_CODE, 0, REQUEST_QUOTE_SIZE);
					} else if (sortType == SortHelper.SORT_FALL) {
						mRequestType = 0;
						mCurrcentSortField = GoodsParams.GOODS_CODE;
						mbCurrentSortOrder = false;
						requestBkRank(false, GoodsParams.GOODS_CODE, 0, REQUEST_QUOTE_SIZE);
					}
				}

			}
		});

	}

	@Override
	protected void initData() {
	}

	protected void onPageResume() {
		super.onPageResume();
		LogUtil.easylog("sky", "RankBKPage -> onPageResume");
		
		// mRequestType = 0;
		if (!getIsAutoRefresh()) {
			if (getUserVisibleHint()) {
				startRequestTask();
			} else {
				requestData();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtil.easylog("sky", "RankBKPage -> onStop");
	}

	@Override
	public void onPause() {
		super.onPause();
		LogUtil.easylog("sky", "RankBKPage -> onPause");
	}

	public void requestData() {
		if (mRequestType == 0) {
			requestBkRank(mbCurrentSortOrder, mCurrcentSortField, 0, REQUEST_QUOTE_SIZE);
		} else if (mRequestType == 1) {
			return;
		} else if (mRequestType == 2) {
			int count = mLstData.size() > 0 ? mLstData.size() : REQUEST_QUOTE_SIZE;
			requestBkRank(mbCurrentSortOrder, mCurrcentSortField, 0, count);
		}
	}

	private void requestBkRank(boolean isDescend, int sortField, int begin, int count) {
		ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
		goodsFiled.add(GoodsParams.ZXJ);
		goodsFiled.add(GoodsParams.ZHANGDIE);
		goodsFiled.add(GoodsParams.ZDF);
		goodsFiled.add(GoodsParams.GOODS_NAME);
		goodsFiled.add(GoodsParams.RISE_HEAD_GOODSID); // 678领涨id
		goodsFiled.add(GoodsParams.RISE_HEAD_GOODSZDF); // -20001领涨股涨幅
		goodsFiled.add(GoodsParams.RISE_HEAD_GOODSNAME); // -20003领涨股的名称,服务器暂未实现返回"0"
		// goodsFiled.add(GoodsParams.FALL_HEAD_GOODSID); //680领跌股id
		// goodsFiled.add(GoodsParams.FALL_HEAD_GOODSZDF); //-20002领跌股跌幅
		// goodsFiled.add(GoodsParams.FALL_HEAD_GOODSNAME);
		// //-20004领跌股的名称,服务器暂未实现返回"0"
		DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
		pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(mCurrType).setGroupType(0).addAllReqFields(goodsFiled).setSortField(sortField).setSortOrder(isDescend).setReqBegin(begin).setReqSize(count).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
		requestQuote(pkg, IDUtils.DynaValueData);
	}

	public void setBKType(int bkType) {
		mCurrType = bkType;
	}

	protected void updateFromQuote(QuotePackageImpl pkg) {
		if (pkg instanceof DynaValueDataPackage) {
			DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
			DynaValueData_Reply gr = goodsTable.getResponse();

			if (gr == null) {
				mListView.removeFooterView(mLlListFooter);
				mIsLoadFinish = true;
				return;
			}

			if (gr.getQuotaValueList().size() == 0) {
				mHasMore = false;
				mListView.removeFooterView(mLlListFooter);
				mIsLoadFinish = true;

				return;
			}

			if (mRequestType == 0) {
				mLstData.clear();
				mHasMore = true;
			} else if (mRequestType == 1) {

			} else if (mRequestType == 2) {
				mLstData.clear();
				// mHasMore = false;
			}

			List<Integer> fieldIds = gr.getRepFieldsList();

			int indexZDF = fieldIds.indexOf(GoodsParams.ZDF);
			int indexName = fieldIds.indexOf(GoodsParams.GOODS_NAME);
			int indexPrice = fieldIds.indexOf(GoodsParams.ZXJ);
			int indexHeadStock_id = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSID);
			int indexHeadStock_zf = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSZDF);
			int indexHeadStock_name = fieldIds.indexOf(GoodsParams.RISE_HEAD_GOODSNAME);

			List<DynaQuota> lst_data = gr.getQuotaValueList();

			ArrayList<DynaQuota> data = new ArrayList<DynaQuota>(lst_data);

			if (gr.hasSortField()) {
				final int sortIndexField = fieldIds.indexOf(gr.getSortField());
				final boolean bSortType = gr.getSortOrder();
				Collections.sort(data, new Comparator<DynaQuota>() {
					@Override
					public int compare(DynaQuota lhs, DynaQuota rhs) {
						String arg1 = lhs.getRepFieldValue(sortIndexField);
						String arg2 = rhs.getRepFieldValue(sortIndexField);
						if (arg1.equals("--") || arg2.equals("--")) {
							return 0;
						}
						float a1 = Float.parseFloat(arg1);
						float a2 = Float.parseFloat(arg2);
						int flag = bSortType ? 1 : -1;
						return a1 == a2 ? 0 : (a1 > a2 ? -flag : flag);
					}
				});
			}

			for (int i = 0; i < data.size(); i++) {
				DynaQuota quote = data.get(i);
				int goodsId = quote.getGoodsId();

				String headStock_zf = quote.getRepFieldValue(indexHeadStock_zf);
				String headStock_name = "";
				headStock_name = quote.getRepFieldValue(indexHeadStock_name);
				if (headStock_name == null || headStock_name.equals("") || headStock_name.equals("0")) {
					String headStock_id = quote.getRepFieldValue(indexHeadStock_id);
					List<Goods> lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(headStock_id), 1);
					if (lstGoods != null && lstGoods.size() > 0) {
						headStock_name = lstGoods.get(0).getGoodsName();
					}
				}

				String zdf = quote.getRepFieldValue(indexZDF);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_ZDF, DataUtils.getSignedZDF(zdf));

				String price = quote.getRepFieldValue(indexPrice);
				map.put(ITEM_BK_PRICE, DataUtils.getPrice(price));

				if (headStock_name == null || headStock_name.equals("")) {
					headStock_name = "_  _";
					map.put(ITEM_HEAD_STOCK_ZF, "");
				} else {
					map.put(ITEM_HEAD_STOCK_ZF, DataUtils.getSignedZDF(headStock_zf));

				}
				map.put(ITEM_HEAD_STOCK_NAME, headStock_name);

				int color = getZDPColor(FontUtils.getColorByZDF(zdf));
				map.put(ITEM_BK_NAME, quote.getRepFieldValue(indexName));
				map.put(ITEM_BK_ID, goodsId);
				map.put(ITEM_QUOTECOLOR, color);
				mLstData.add(map);
			}

			closeSQLDBHelper();
			mAlreadyRequestCount = mLstData.size();
			mListView.removeFooterView(mLlListFooter);
			mIsLoadFinish = true;
			mAdapter.notifyDataSetChanged();

			if (mRequestType == 0) {
				mListView.setSelection(0);
				mRequestType = 2;
			} else if (mRequestType == 1) {
				mRequestType = 2;
			} else if (mRequestType == 2) {

			}

		}

	}
	
	public void setRankType(int rankType) {
		// 方法执行顺序： 1. 构造函数；  2.setRankType()；  3. initPage()；
		if (rankType == RANK_TYPE_RISE) {
			mbCurrentSortOrder = true;
		} else if (rankType == RANK_TYPE_FALL) {
			mbCurrentSortOrder = false;
		}
	}

	class RankBKAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mLstData.size();
		}

		@Override
		public Object getItem(int position) {
			return mLstData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    ViewHolder vh = null;
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.page_rank_bk_listitem, null);
				
				vh = new ViewHolder(convertView);
				convertView.setTag(vh);
			} else {
			    vh = (ViewHolder) convertView.getTag();
			}
			
			final Map<String, Object> map = (Map<String, Object>) getItem(position);
			int color = (Integer) map.get(ITEM_QUOTECOLOR);
			
			vh.tvZDF.setText(String.valueOf(map.get(ITEM_ZDF)));
			vh.tvZDF.setBackgroundColor(color);
			vh.tvBKName.setText(String.valueOf(map.get(ITEM_BK_NAME)));
			vh.tvStockName.setText(String.valueOf(map.get(ITEM_HEAD_STOCK_NAME)));
			vh.tvStockZDF.setText(String.valueOf(map.get(ITEM_HEAD_STOCK_ZF)));
			
			vh.layoutExpand.setOnClickListener(new OnClickEffectiveListener() {
				@Override
				public void onClickEffective(View v) {
					PageIntent intent = new PageIntent(RankBkPage.this.getParent(), RankBkStockPage.class);
					Bundle bundle = new Bundle();
					int goodsId = (Integer) map.get(ITEM_BK_ID);
					String price = (String) map.get(ITEM_BK_PRICE);
					String bkName = (String) map.get(ITEM_BK_NAME);
					String zdf = (String) map.get(ITEM_ZDF);
					int color = (Integer) map.get(ITEM_QUOTECOLOR);
					bundle.putInt(RankBkStockPage.EXTEA_KEY_GROUPTYPE, goodsId);
					bundle.putString(RankBkStockPage.EXTEA_KEY_GROUPPRICE, price);
					bundle.putString(RankBkStockPage.EXTEA_KEY_GROUPNAME, bkName);
					bundle.putString(RankBkStockPage.EXTEA_KEY_GROUPZDF, zdf);
					bundle.putInt(RankBkStockPage.EXTEA_KEY_GROUPCOLOR, color);
					intent.setArguments(bundle);
					intent.setSupportAnimation(false);
					startPage(R.id.rankbk_content, intent);
				}
			});
			
			return convertView;
		}
		
		private class ViewHolder {

		    public View layoutExpand;
		    
		    private TextView tvBKName;
		    private TextView tvStockName;
		    private TextView tvStockZDF;
		    private TextView tvZDF;
		    
			public ViewHolder(View view) {
			    layoutExpand = view.findViewById(R.id.item_layout_expand);
                tvBKName = (TextView) view.findViewById(R.id.item_tv_bkname);
                tvStockName = (TextView) view.findViewById(R.id.item_tv_stockname);
                tvStockZDF = (TextView) view.findViewById(R.id.item_tv_stock_zdf);
                tvZDF = (TextView) view.findViewById(R.id.item_tv_zdf);
			}

		}
		
	}
	
}
