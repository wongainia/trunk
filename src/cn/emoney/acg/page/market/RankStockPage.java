package cn.emoney.acg.page.market;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import cn.emoney.acg.util.QuoteUtils;
import cn.emoney.sky.libs.utils.SortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper;
import cn.emoney.sky.libs.utils.SymbolSortHelper.OnSortListener;

public class RankStockPage extends PageImpl {

	/**
	 * 排名类型：涨幅靠前，跌幅靠前，换手率，主力流入
	 * */
	public static final int RANK_TYPE_ZF = 1001;
	public static final int RANK_TYPE_DF = 1002;
	public static final int RANK_TYPE_HSL = 1003;
	public static final int RANK_TYPE_ZLLR = 1004;

	private static String[] OPTION_TYPE_NAME = { "涨跌幅", "涨跌", "换手率", "主力净流" };
	private static int[] OPTION_TYPE_FIELD = { GoodsParams.ZDF, GoodsParams.ZHANGDIE, GoodsParams.HSL, GoodsParams.JL };
	int currentOptionIndex = 0;
	private TextView lastSortItem;

	public static final String KEY_RANK_TYPE = "rank_type";

	public static final int GROUP_SH_A = 0x00000002;    // 上证A股
	public final static int GROUP_SZ_A = 0x00002000;    // 深证A股
	public final static int GROUP_SZ_ZXB = 0x02000000;  // 深中小版
	public final static int GROUP_SZ_CYB = 0x10000000;  // 深创业板

	private int currentGroupType;

	private List<String> listSortFieldName = new ArrayList<String>(3);

	private List<StockItemBean> listDatas = new ArrayList<StockItemBean>();
	private StockAdapter adapter;
	private ListView listView;
	private TextView tvStockName, tvPrice, tvOptional;

	private SymbolSortHelper sortHelper;
	private boolean currentSortOrder = true;
	private int currentSortField = GoodsParams.ZDF;
	private int lastSortType;

	@Override
	protected void initPage() {
		setContentView(R.layout.page_rank_stock);

		tvStockName = (TextView) findViewById(R.id.page_rankstock_item_namecode);
		tvPrice = (TextView) findViewById(R.id.page_rankstock_item_price);
		tvOptional = (TextView) findViewById(R.id.page_rankstock_item_option);

		listView = (ListView) findViewById(R.id.page_rankstock_listview);
		adapter = new StockAdapter(listDatas, getContext());
		listView.setAdapter(adapter);

		listSortFieldName.add("股票名称");
		listSortFieldName.add("最新价");
		listSortFieldName.add("涨跌幅");

		lastSortItem = tvOptional;

		sortHelper = new SymbolSortHelper();
		sortHelper.setItemTextColor(RColor(R.color.t3));
		sortHelper.setItemSelectedTextColor(RColor(R.color.c4));
		sortHelper.setStrPadding(" ");

		sortHelper.addSortItem(tvStockName, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
		sortHelper.addSortItem(tvPrice, SortHelper.SORT_RISE | SortHelper.SORT_FALL);
		sortHelper.addSortItem(tvOptional, SortHelper.SORT_RISE | SortHelper.SORT_FALL);

		sortHelper.updateItemLable(listSortFieldName);
		setSortAction();

	}

	@Override
	protected void onPageResume() {
		super.onPageResume();

		listSortFieldName.set(2, OPTION_TYPE_NAME[currentOptionIndex]);
		sortHelper.updateItemLable(listSortFieldName);
		sortHelper.setDefaultSort(lastSortItem, lastSortType);
		sortHelper.notifySort();

		if (!getIsAutoRefresh()) {
			if (getUserVisibleHint()) {
				startRequestTask();
			} else {
				requestData();
			}
		}
	}

	@Override
	protected void initData() {
	}

	private void setSortAction() {
		if (sortHelper != null) {
			sortHelper.setOnSortListener(new OnSortListener() {

				@Override
				public void onSort(TextView view, int sortType) {
					if (view != null) {
						lastSortItem = view;
						lastSortType = sortType;

						int id = view.getId();

						if (id == R.id.page_rankstock_item_option) {
							if (sortType == SortHelper.SORT_RISE) {
								currentSortField = OPTION_TYPE_FIELD[currentOptionIndex];
								currentSortOrder = true;
							} else if (sortType == SortHelper.SORT_FALL) {
								currentSortField = OPTION_TYPE_FIELD[currentOptionIndex];
								currentSortOrder = false;
							}
						} else if (id == R.id.page_rankstock_item_price) {
							if (sortType == SortHelper.SORT_RISE) {
								currentSortField = GoodsParams.ZXJ;
								currentSortOrder = true;
							} else if (sortType == SortHelper.SORT_FALL) {
								currentSortField = GoodsParams.ZXJ;
								currentSortOrder = false;
							}
						} else if (id == R.id.page_rankstock_item_namecode) {
							if (sortType == SortHelper.SORT_RISE) {
								currentSortField = GoodsParams.GOODS_CODE;
								currentSortOrder = true;
							} else if (sortType == SortHelper.SORT_FALL) {
								currentSortField = GoodsParams.GOODS_CODE;
								currentSortOrder = false;
							}
						}

						requestStockRank(currentSortOrder, currentSortField);
					}
				}
			});
		}
	}

	public void setGroupType(int groupType) {
		currentGroupType = groupType;
	}

	public void setRankType(int rankStockType) {

		if (rankStockType == RANK_TYPE_ZF) {
			currentSortOrder = true;
			lastSortType = SortHelper.SORT_RISE;
			currentOptionIndex = 0;
		} else if (rankStockType == RANK_TYPE_DF) {
			currentSortOrder = false;
			lastSortType = SortHelper.SORT_FALL;
			currentOptionIndex = 0;
		} else if (rankStockType == RANK_TYPE_HSL) {
			currentSortOrder = true;
			lastSortType = SortHelper.SORT_RISE;
			currentOptionIndex = 2;
		} else if (rankStockType == RANK_TYPE_ZLLR) {
			currentSortOrder = true;
			lastSortType = SortHelper.SORT_RISE;
			currentOptionIndex = 3;
		}

		currentSortField = OPTION_TYPE_FIELD[currentOptionIndex];

	}

	public void requestData() {
		// 升序或降序，true为降序(涨-跌) ， false为升序(跌-涨)
		requestStockRank(currentSortOrder, currentSortField);
	}

	private void requestStockRank(boolean isDescend, int sortField) {
		ArrayList<Integer> goodsFiled = new ArrayList<Integer>();
		goodsFiled.add(GoodsParams.ZXJ);
		goodsFiled.add(GoodsParams.GOODS_NAME);
		goodsFiled.add(GoodsParams.ZHANGDIE);
		goodsFiled.add(GoodsParams.ZDF);
		goodsFiled.add(GoodsParams.HSL);
		goodsFiled.add(GoodsParams.JL);

		DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead((short) 0));
		pkg.setRequest(DynaValueData_Request.newBuilder().setClassType(3).setGroupType(currentGroupType)
				.addAllReqFields(goodsFiled).setSortField(sortField).setSortOrder(isDescend)
				.setReqBegin(0).setReqSize(50).setLastUpdateMarketTime(0).setLastUpdateMarketDate(0).build());
		requestQuote(pkg, IDUtils.DynaValueData);
	}

	@Override
	protected void updateFromQuote(QuotePackageImpl pkg) {
		super.updateFromQuote(pkg);

		if (pkg instanceof DynaValueDataPackage) {
			DynaValueDataPackage goodsTable = (DynaValueDataPackage) pkg;
			DynaValueData_Reply gr = goodsTable.getResponse();

			if (gr == null || gr.getQuotaValueList().size() == 0) {
				return;
			}

			List<Integer> fieldIds = gr.getRepFieldsList();
			int indexZDF = fieldIds.indexOf(GoodsParams.ZDF);
			int indexZhangdie = fieldIds.indexOf(GoodsParams.ZHANGDIE);
			int indexName = fieldIds.indexOf(GoodsParams.GOODS_NAME);
			int indexPrice = fieldIds.indexOf(GoodsParams.ZXJ);
			int indexHsl = fieldIds.indexOf(GoodsParams.HSL);
			int indexJl = fieldIds.indexOf(GoodsParams.JL);

			List<DynaQuota> datas = gr.getQuotaValueList();
			listDatas.clear();
			for (int i = 0; i < datas.size(); i++) {
				DynaQuota quote = datas.get(i);

				int goodsId = quote.getGoodsId();
				String name = quote.getRepFieldValue(indexName);
				String code = QuoteUtils.getStockCodeByGoodsId(String.valueOf(goodsId));
				String price = quote.getRepFieldValue(indexPrice);
				String zdf = quote.getRepFieldValue(indexZDF);
				String hsl = quote.getRepFieldValue(indexHsl);
				String jl = quote.getRepFieldValue(indexJl);
				String zd = quote.getRepFieldValue(indexZhangdie);

				listDatas.add(new StockItemBean(goodsId, name, code, price, zdf, zd, hsl, jl));
			}

			adapter.notifyDataSetChanged();
		}

	}

	private class StockItemBean {
		public int goodsId;
		public String stockName, stockCode, price, zdf, hsl, jl, zd;

		public StockItemBean(int goodsId, String stockName, String stockCode,
				String price, String zdf, String zd, String hsl, String jl) {
			super();
			this.goodsId = goodsId;
			this.stockName = stockName;
			this.stockCode = stockCode;
			this.price = price;
			this.zdf = zdf;
			this.zd = zd;
			this.hsl = hsl;
			this.jl = jl;
		}

	}

	private class StockAdapter extends BaseAdapter {

		private List<StockItemBean> listDatas;
		private LayoutInflater inflater;

		public StockAdapter(List<StockItemBean> listDatas, Context context) {
			super();
			this.listDatas = listDatas;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return listDatas.size();
		}

		@Override
		public Object getItem(int positioin) {
			return listDatas.get(positioin);
		}

		@Override
		public long getItemId(int positioin) {
			return positioin;
		}

		@Override
		public View getView(final int positioin, View convertView, ViewGroup root) {
			ViewHolder vh = null;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.page_rank_stock_listitem, root, false);

				vh = new ViewHolder(convertView);

				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			// update views
			StockItemBean bean = listDatas.get(positioin);

			vh.tvStockName.setText(bean.stockName);
			vh.tvStockCode.setText(bean.stockCode);
			vh.tvPrice.setText(DataUtils.getPrice(bean.price));

			if (currentOptionIndex == 0) {
				vh.tvOption.setText(DataUtils.getSignedZDF(bean.zdf));
                int resZDP = getZDPRadiusBg(FontUtils.getColorByZDF(bean.zdf));
                vh.tvOption.setBackgroundResource(resZDP);
			} else if (currentOptionIndex == 1) {
				vh.tvOption.setText(DataUtils.getSignedZD(bean.zd));
				int resZDP = getZDPRadiusBg(FontUtils.getColorByZD(bean.zd));
                vh.tvOption.setBackgroundResource(resZDP);
			} else if (currentOptionIndex == 2) {
				vh.tvOption.setText(DataUtils.formatHsl(bean.hsl));
				int resZDP = getZDPRadiusBg(FontUtils.getColorByZD(bean.hsl));
                vh.tvOption.setBackgroundResource(resZDP);
			} else if (currentOptionIndex == 3) {
				vh.tvOption.setText(DataUtils.formatJL(bean.jl, DataUtils.mDecimalFormat1_max));
				int resZDP = getZDPRadiusBg(FontUtils.getColorByZD(bean.jl));
                vh.tvOption.setBackgroundResource(resZDP);
			}
			
			// set events
			vh.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ArrayList<Goods> lstGoods = new ArrayList<Goods>();
					for (int i = 0; i < listDatas.size(); i++) {
						StockItemBean bean = listDatas.get(i);
						lstGoods.add(new Goods(bean.goodsId, bean.stockName));
					}
					QuoteJump.gotoQuote(RankStockPage.this, lstGoods, positioin);
//					gotoQuote(lstGoods, positioin);
				}
			});

			vh.clickArea.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					currentOptionIndex = ++currentOptionIndex > 3 ? 0 : currentOptionIndex;
					listSortFieldName.set(2, OPTION_TYPE_NAME[currentOptionIndex]);

					if (lastSortItem != null && lastSortItem.getId() == R.id.page_rankstock_item_option) {
						sortHelper.updateItemLable(listSortFieldName);
						sortHelper.setDefaultSort(lastSortItem, lastSortType);
						currentSortField = OPTION_TYPE_FIELD[currentOptionIndex];
					} else {
						sortHelper.updateItemLable_exceptSort(listSortFieldName);
					}

					sortHelper.notifySort();

					// 更新数据
					requestData();
				}
			});

			return convertView;
		}

		private class ViewHolder {
			public View layout, clickArea;

			public TextView tvStockName, tvStockCode, tvPrice, tvOption;

			public ViewHolder(View layout) {
				super();
				this.layout = layout;

				tvStockName = (TextView) layout.findViewById(R.id.item_tv_stockname);
				tvStockCode = (TextView) layout.findViewById(R.id.item_tv_stockcode);
				tvPrice = (TextView) layout.findViewById(R.id.item_tv_price);
				tvOption = (TextView) layout.findViewById(R.id.item_tv_zdf);
				clickArea = layout.findViewById(R.id.page_rank_stock_listitem_click_area);
			}

		}

	}

}
