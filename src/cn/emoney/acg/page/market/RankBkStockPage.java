package cn.emoney.acg.page.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.page.Page;
import cn.emoney.sky.libs.utils.SortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper;

public class RankBkStockPage extends PageImpl {
	public final static int CMD_CLOSE_SELF = 111001;
	public static Handler mHandler_rankBKStock = null;

	private ListView mListView = null;

	private RankBKAdapter mAdapter = null;
	private List<Map<String, Object>> mLstData = new ArrayList<Map<String, Object>>();

	private int mGroupType = 0;
	private String mGroupName = "";
	private final String ITEM_ZDF = "item_zdf";
	private final String ITEM_STOCKPRICE = "item_stockprice";
	private final String ITEM_STOCKNAME = "item_stockname";
	private final String ITEM_STOCKCODE = "item_stockcode";
	private final String ITEM_QUOTECOLOR = "item_quotecolor";
	private final String ITEM_GOODSID = "item_goodsid";

	public final static String EXTEA_KEY_GROUPTYPE = "key_grouptype";
	public final static String EXTEA_KEY_GROUPPRICE = "key_groupprice";
	public final static String EXTEA_KEY_GROUPNAME = "key_groupname";
	public final static String EXTEA_KEY_GROUPZDF = "key_groupzdf";
	public final static String EXTRA_KEY_TOPSTOCK = "key_topstock";
	public final static String EXTEA_KEY_GROUPCOLOR = "key_groupcolor";
	private View mHeaderView = null;

	View mHeaderBottomLine = null;

	private TextView mTvGroupName = null;
	private TextView mTvGroupPrice = null;
	private TextView mTvGroupZDF = null;

	private TextView mTvNameCode = null;
	private TextView mTvZDF = null;
	private TextView mTvPrice = null;

	private SymbolSortHelper mSortHelper = null;

	private boolean mbCurrentSortOrder = true;
	private int mCurrcentSortField = GoodsParams.ZDF;

	@Override
	protected void initPage() {
		setContentView(R.layout.page_rank_bk_stock);

		mHeaderBottomLine = findViewById(R.id.rankbkstock_header_bottom_line);

		mListView = (ListView) findViewById(R.id.rankbkstock_listview);
		if (mListView != null) {
			mAdapter = new RankBKAdapter();
			mHeaderView = View.inflate(getContext(), R.layout.page_rank_bk_stock_list_head, null);
			mHeaderView.setOnClickListener(new OnClickEffectiveListener() {
				@Override
				public void onClickEffective(View v) {
					Goods g = new Goods(mGroupType, mGroupName);
					Page page = RankBkHome.mInstance;
					QuoteJump.gotoQuote(page, g);
//					gotoQuote(page, g);
					// gotoQuote(g);
				}
			});
			mListView.addHeaderView(mHeaderView);
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
					ArrayList<Goods> lstGoods = new ArrayList<Goods>();
					for (int i = 0; i < mLstData.size(); i++) {
						Map<String, Object> map = mLstData.get(i);
						String name = (String) map.get(ITEM_STOCKNAME);
						int id = (Integer) map.get(ITEM_GOODSID);
						lstGoods.add(new Goods(id, name));
					}
					Page page = RankBkHome.mInstance;
					QuoteJump.gotoQuote(page, lstGoods, index -1, -1);
//					gotoQuote(page, lstGoods, index - 1, -1);
					// gotoQuote(lstGoods, index - 1);
				}
			});

			mHeaderView.findViewById(R.id.item_layout_unexpand).setOnClickListener(new OnClickEffectiveListener() {

				@Override
				public void onClickEffective(View v) {
					finish();
				}
			});
			mTvGroupName = (TextView) mHeaderView.findViewById(R.id.item_tv_bkname);
			mTvGroupPrice = (TextView) mHeaderView.findViewById(R.id.item_tv_bk_price);
			mTvGroupZDF = (TextView) mHeaderView.findViewById(R.id.item_tv_zdf);
		}

		mSortHelper = new SymbolSortHelper();
		mSortHelper.setItemTextColor(RColor(R.color.t1));
		mSortHelper.setItemSelectedTextColor(getResources().getColor(R.color.c4));
		mSortHelper.updateSort();
		mSortHelper.setStrPadding(" ");

		mTvNameCode = (TextView) findViewById(R.id.item_tv_rankbkstock_namecode);
		mTvZDF = (TextView) findViewById(R.id.item_tv_rankbkstock_zdf);
		mTvPrice = (TextView) findViewById(R.id.item_tv_rankbkstock_price);

		mSortHelper.addSortItem(mTvNameCode, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
		mSortHelper.addSortItem(mTvPrice, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
		mSortHelper.addSortItem(mTvZDF, SortHelper.SORT_RISE | SortHelper.SORT_FALL);

		mbCurrentSortOrder = true;
		mCurrcentSortField = GoodsParams.ZDF;
		mSortHelper.setDefaultSort(mTvZDF, SortHelper.SORT_RISE);

		mSortHelper.setOnSortListener(new SymbolSortHelper.OnSortListener() {

			@Override
			public void onSort(TextView view, int sortType) {
				int id = view.getId();

				if (id == R.id.item_tv_rankbkstock_zdf) {
					if (sortType == SortHelper.SORT_RISE) {
						// Collections.sort(mLstData, new
						// MapsComparator(MapsComparator.ASCENDING_ORDER, new
						// MapsComparator.UserCompare() {
						// @Override
						// public int getRealRelationship(Map<String, Object>
						// map1, Map<String, Object> map2) {
						// return compareLocMap(map1, map2);
						// }
						// }));
						mbCurrentSortOrder = true;
						mCurrcentSortField = GoodsParams.ZDF;
						requestStockRank(true, GoodsParams.ZDF);
					} else if (sortType == SortHelper.SORT_FALL) {
						// Collections.sort(mLstData, new
						// MapsComparator(MapsComparator.DESCENDING_ORDER, new
						// MapsComparator.UserCompare() {
						//
						// @Override
						// public int getRealRelationship(Map<String, Object>
						// map1, Map<String, Object> map2) {
						// return compareLocMap(map1, map2);
						// }
						// }));
						mbCurrentSortOrder = false;
						mCurrcentSortField = GoodsParams.ZDF;
						requestStockRank(false, GoodsParams.ZDF);
					}

				} else if (id == R.id.item_tv_rankbkstock_price) {
					if (sortType == SortHelper.SORT_RISE) {
						mbCurrentSortOrder = true;
						mCurrcentSortField = GoodsParams.ZXJ;
						requestStockRank(true, GoodsParams.ZXJ);
					} else if (sortType == SortHelper.SORT_FALL) {
						mbCurrentSortOrder = false;
						mCurrcentSortField = GoodsParams.ZXJ;
						requestStockRank(false, GoodsParams.ZXJ);
					}
				} else if (id == R.id.item_tv_rankbkstock_namecode) {
					if (sortType == SortHelper.SORT_RISE) {
						mbCurrentSortOrder = true;
						mCurrcentSortField = GoodsParams.GOODS_CODE;
						requestStockRank(true, GoodsParams.GOODS_CODE);
					} else if (sortType == SortHelper.SORT_FALL) {
						mbCurrentSortOrder = false;
						mCurrcentSortField = GoodsParams.GOODS_CODE;
						requestStockRank(false, GoodsParams.GOODS_CODE);
					}
				}
			}
		});

		if (mHandler_rankBKStock == null) {
			mHandler_rankBKStock = new Handler() {
				public void handleMessage(Message msg) {
					int what = msg.what;
					switch (what) {
					case CMD_CLOSE_SELF:
						RankBkStockPage.this.finish();
						break;
					default:
						break;
					}
				}
			};
		}

	}

	private int compareLocMap(Map<String, Object> map_1, Map<String, Object> map_2) {
		String t_zdf1 = (String) map_1.get(ITEM_ZDF);
		String zdf1 = t_zdf1.replaceAll("%", "");
		String t_zdf2 = (String) map_2.get(ITEM_ZDF);
		String zdf2 = t_zdf2.replaceAll("%", "");

		if (zdf1.equals("--") || zdf2.equals("--")) {
			return 0;
		}
		float f_zdf1 = Float.parseFloat(zdf1);
		float f_zdf2 = Float.parseFloat(zdf2);

		if (f_zdf1 == f_zdf2) {
			return 0;
		}

		return f_zdf1 > f_zdf2 ? 1 : -1;
	}

	@Override
	protected void initData() {
	}

	protected void receiveData(Bundle bundle) {
		if (bundle == null) {
			return;
		}
		if (bundle.containsKey(EXTEA_KEY_GROUPTYPE)) {
			mGroupType = bundle.getInt(EXTEA_KEY_GROUPTYPE);
		}

		if (bundle.containsKey(EXTEA_KEY_GROUPPRICE)) {
			String t_gPrice = bundle.getString(EXTEA_KEY_GROUPPRICE);
			mTvGroupPrice.setText(t_gPrice);

		}
		if (bundle.containsKey(EXTEA_KEY_GROUPNAME)) {
			mGroupName = bundle.getString(EXTEA_KEY_GROUPNAME);
			mTvGroupName.setText(mGroupName);
		}
		if (bundle.containsKey(EXTEA_KEY_GROUPZDF)) {
			String groupZDF = bundle.getString(EXTEA_KEY_GROUPZDF);
			mTvGroupZDF.setText(groupZDF);

		}
		if (bundle.containsKey(EXTRA_KEY_TOPSTOCK)) {
			String topStock = bundle.getString(EXTRA_KEY_TOPSTOCK);
		}
		if (bundle.containsKey(EXTEA_KEY_GROUPCOLOR)) {
			int color = bundle.getInt(EXTEA_KEY_GROUPCOLOR);
			mTvGroupZDF.setTextColor(color);
		}
	}

	protected void onPageResume() {
		super.onPageResume();
		LogUtil.easylog("sky", "RankBKStockPage -> onPageResume");

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
		mHandler_rankBKStock = null;
		LogUtil.easylog("sky", "RankBKStockPage -> onStop");
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		LogUtil.easylog("sky", "RankBKStockPage -> onPause");
	}

	public void requestData() {
		requestStockRank(mbCurrentSortOrder, mCurrcentSortField);
	}

	private void requestStockRank(boolean isDescend, int sortField) {

		// type:0 概念 ， 1 行业 ， 2地区 ， 3 系统板块类（沪深A等） ，4 自定义上传股票ID
		int classType = 0;
		if (DataUtils.IsGNBK(mGroupType)) {
			classType = 0;
		} else if (DataUtils.IsHYBK(mGroupType)) {
			classType = 1;
		} else if (DataUtils.IsDQBK(mGroupType)) {
			classType = 2;
		}

		ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
		goodsFiled.add(GoodsParams.ZXJ);
		goodsFiled.add(GoodsParams.ZHANGDIE);
		goodsFiled.add(GoodsParams.GOODS_NAME);
		goodsFiled.add(GoodsParams.ZDF);
		goodsFiled.add(GoodsParams.GOODS_CODE);
		DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
		pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(classType).setGroupType(mGroupType).addAllReqFields(goodsFiled).setSortField(sortField).setSortOrder(isDescend).setReqBegin(0).setReqSize(40).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
		requestQuote(pkg, IDUtils.DynaValueData);
	}

	protected void updateFromQuote(QuotePackageImpl pkg) {
		if (pkg instanceof DynaValueDataPackage) {
			DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
			DynaValueData_Reply gr = goodsTable.getResponse();

			if (gr == null || gr.getQuotaValueList().size() == 0) {
				return;
			}

			mLstData.clear();
			List<Integer> fieldIds = gr.getRepFieldsList();
			int indexZDF = fieldIds.indexOf(GoodsParams.ZDF);
			int indexName = fieldIds.indexOf(GoodsParams.GOODS_NAME);
			int indexPrice = fieldIds.indexOf(GoodsParams.ZXJ);
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

			// DSQLiteDatabase dbHelper = null;
			for (int i = 0; i < data.size(); i++) {
				DynaQuota quote = data.get(i);
				int goodsId = quote.getGoodsId();

				String zdf = quote.getRepFieldValue(indexZDF);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_ZDF, zdf);
				String name = quote.getRepFieldValue(indexName);
				String code = QuoteUtils.getStockCodeByGoodsId(String.valueOf(goodsId));
				String price = quote.getRepFieldValue(indexPrice);

				map.put(ITEM_STOCKPRICE, DataUtils.getPrice(price));
				map.put(ITEM_STOCKNAME, name);
				map.put(ITEM_STOCKCODE, code);
				map.put(ITEM_GOODSID, goodsId);
				mLstData.add(map);
			}
			mAdapter.notifyDataSetChanged();
		}

	}
	
	@Override
	public int enterAnimation() {
		return 0;
	}
	
	@Override
	public int popExitAnimation() {
		return 0;
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
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.page_rank_bk_stock_listitem, null);
				TextView tvZDF = (TextView) convertView.findViewById(R.id.item_tv_zdf);
				TextView tvName = (TextView) convertView.findViewById(R.id.item_tv_stockname);
				TextView tvCode = (TextView) convertView.findViewById(R.id.item_tv_stockcode);
				TextView tvPrice = (TextView) convertView.findViewById(R.id.item_tv_price);
				convertView.setTag(new ListCell(tvPrice, tvZDF, tvName, tvCode));
			}

			ListCell lc = (ListCell) convertView.getTag();

			Map<String, Object> map = (Map<String, Object>) getItem(position);

			String zdf = (String) map.get(ITEM_ZDF);
			float flagColor = FontUtils.getColorByZDF(zdf);
			int t_color = getZDPColor(flagColor);

			lc.getTvZDF().setBackgroundColor(t_color);

			lc.getTvZDF().setText(DataUtils.getSignedZDF(zdf));

			lc.getTvName().setText(String.valueOf(map.get(ITEM_STOCKNAME)));

			lc.getTvCode().setText(String.valueOf(map.get(ITEM_STOCKCODE)));

			lc.getTvPrice().setText(String.valueOf(map.get(ITEM_STOCKPRICE)));
			return convertView;
		}

		private class ListCell {

			public ListCell(TextView tvPrice, TextView tvZDF, TextView tvName, TextView tvCode) {
				this.tvZDF = tvZDF;
				this.tvName = tvName;
				this.tvCode = tvCode;
				this.tvPrice = tvPrice;
			}

			public TextView getTvPrice() {
				return tvPrice;
			}

			public TextView getTvZDF() {
				return tvZDF;
			}

			public TextView getTvName() {
				return tvName;
			}

			public TextView getTvCode() {
				return tvCode;
			}

			private TextView tvPrice;
			private TextView tvZDF;
			private TextView tvName;
			private TextView tvCode;
		}
	}
}
