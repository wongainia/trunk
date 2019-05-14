package cn.emoney.acg.page.share.quote;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.GoodsParams;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply;
import cn.emoney.acg.data.protocol.quote.DynaValueDataReply.DynaValueData_Reply.DynaQuota;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.helper.alert.StockAlertManagerV2.Operation;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.motif.AddMotifPositionPage;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

/**
 * 设置股票预警界面
 * */
public class SetAlertPage extends PageImpl implements OnClickListener {

	private static final short REQUEST_FLAG_UPDATE = 1001;
	private static final short REQUEST_FLAG_DELETE = 1002;
	private static final short REQUEST_FLAG_SOTCK_PRICE = 1003;

	/** 传入参数的Key值 */
	public static final String KEY_STOCK_ID = "stock_id";
	public static final String KEY_STOCK_NAME = "stock_name";
	public static final String KEY_STOCK_CODE = "stock_code";
	public static final String KEY_STOCK_PRICE = "stock_price";
	public static final String KEY_STOCK_ZDF = "stock_zdf";
	public static final String KEY_STOCK_ZD = "stock_zd";

	private String stockName;
	private String stockZd;
	private String stockPrice;
	private int stockId;

	/** 股票信息 （名称、代号、最新价、涨跌幅） */
	private TextView tvStockName, tvStockCode, tvStockPrice, tvStockZdf;

	/** 输入行情数据 */
	private EditText etPrice, etIncrease, etDecrease, etTurnover;

	/** 操盘线买入点预警开关 */
	private ImageView imgBuyDayCpx, imgBuyWeekCpx, imgBuyHourCpx;

	/** 操盘线卖出点预警开关 */
	private ImageView imgSellDayCpx, imgSellWeekCpx, imgSellHourCpx;

	private ScrollView scrollView;

	/** 标记该股票是否是已设置过预警的股票 */
	private boolean isStockHasSetAlert;

	/**
	 * 存储从网络上获取到信息后，界面各组件的状态，如果从网络上取到的数据中没有该股票的预警设置，则取默认值
	 * */
	private StatusHolder originStatusHolder = new StatusHolder();

	/**
	 * 存储用户改变组件状态后，各组件的状态
	 * */
	private StatusHolder changedStatusHolder = new StatusHolder();

	@Override
	protected void initPage() {
		setContentView(R.layout.page_alert_config);

		initViews();
		
		bindPageTitleBar(R.id.page_alertconfig_titlebar);
	}

	@Override
	protected void receiveData(Bundle extras) {
		super.receiveData(extras);

		if (extras != null) {
			stockId = extras.getInt(KEY_STOCK_ID);
			stockName = extras.getString(KEY_STOCK_NAME);
			String stockCode = extras.getString(KEY_STOCK_CODE);

			stockPrice = extras.getString(KEY_STOCK_PRICE);
			String stockZdf = extras.getString(KEY_STOCK_ZDF);
			stockZd = extras.getString(KEY_STOCK_ZD);

			tvStockName.setText(stockName);
			tvStockCode.setText(stockCode);

			if (TextUtils.isEmpty(stockPrice) || TextUtils.isEmpty(stockZdf)) {
				requestDataForStock();

				if (TextUtils.isEmpty(stockPrice)) {
					stockPrice = "0.00";
				}

				if (TextUtils.isEmpty(stockZdf)) {
					stockZdf = "0.00%";
				}

				tvStockPrice.setText("--");
				tvStockZdf.setText("--%");
			} else {
				// 如果从个股页面拿到当前股票价格，就使各个控件可以编辑
				controlWidgetsEnablable(true);
			}

			tvStockPrice.setText(stockPrice);
			tvStockZdf.setText(stockZdf);
		}
	}

	@Override
	protected void initData() {
	}

	@Override
	protected void onPageResume() {
		super.onPageResume();

		// 设置股票价格和涨跌幅的颜色，不能放在initData()中设置
		if (!TextUtils.isEmpty(stockZd)) {
			int color = getZDPColor(FontUtils.getColorByZD(stockZd));
			tvStockPrice.setTextColor(color);
			tvStockZdf.setTextColor(color);
		}

		/*
		 * 从缓存中获取单支股票的预警配置数据 如果从缓存中获取不到数据，有两种情况，一种是缓存中无数据，一种是缓存中有数据，但没有该支股票的数据
		 * 只有缓存中无数据时，才向网络获取数据 所以先判断缓存中是否有数据，如果没有数据，向网络中获取，如果缓存中有数据，再获取当前股票的数据
		 * 如果获取不到，就将各组件状态的缓存数据置为默认值
		 */
		if (StockAlertManagerV2.getInstance().isCacheHasData()) {
			String warnConfig = StockAlertManagerV2.getInstance().getStockAlert(String.valueOf(stockId));
			if (!TextUtils.isEmpty(warnConfig)) {
				isStockHasSetAlert = true;

				analyseSingleWarnConfig(warnConfig, originStatusHolder);
				originStatusHolder.currentPrice = stockPrice;
				copyWarnConfig(originStatusHolder, changedStatusHolder);

				updateView(originStatusHolder);
			} else {
				// 缓存有数据，但没有该支股票的数据，需要将当前价格保存到缓存中
				originStatusHolder.currentPrice = stockPrice;
				changedStatusHolder.currentPrice = originStatusHolder.currentPrice;
			}
		}
	}

	@Override
	protected void onPagePause() {
		super.onPagePause();

		InputMethodUtil.closeSoftKeyBoard(SetAlertPage.this);
	}

	private void doBack() {
		// 当mPageChangeFlag为-1时，表示Page已finish
		// 首先检查预警配置是否有修改，如果有修改，发送网络请求更新配置，更新配置成功后再关闭页面
		if (originStatusHolder != null && changedStatusHolder != null && (originStatusHolder.hashCode() != changedStatusHolder.hashCode())) {
			// 预警配置已修改，发送网络请求更新配置

			// 判断预警设置当前是否有效，如果无效，删除当前设置，如果有效，更新当前设置
			if (isAlertSettingValid(changedStatusHolder)) {
				addZxgAndAlert();
			} else {
				doRemoveWarn();
			}
		} else {
			// 预警配置没有修改，直接退出界面
			mPageChangeFlag = -1;
			finish();
		}
	}

	private void doRemoveWarn() {
		StockAlertManagerV2.getInstance().removeWarns(stockId + "", new Operation() {
			@Override
			public void onSuccess(int typeTag) {
				if (typeTag == StockAlertManagerV2.TAG_REMOVE) {
					showTip("预警删除成功");
					finish();
				}
			}

			@Override
			public void onFail(int retCode) {
				if (retCode == StockAlertManagerV2.TAG_REMOVE) {
					showTip("预警删除失败");
					finish();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPageChangeFlag == 0) {
				doBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doUpdateWarn() {
		StockAlertManagerV2.getInstance().updateWarns(stockId + "", getUpdateValue(), new Operation() {
			@Override
			public void onSuccess(int typeTag) {
				if (typeTag == StockAlertManagerV2.TAG_ADD_OR_UPDATE) {
					showTip("设置预警成功");
					finish();
				}
			}

			@Override
			public void onFail(int retCode) {
				if (retCode == StockAlertManagerV2.TAG_ADD_OR_UPDATE) {
					showTip("设置预警失败");
					finish();
				}
			}
		});
	}

	@Override
	protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
		View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
		BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
		leftItem.setTag(TitleBar.Position.LEFT);
		menu.addItem(leftItem);

		BarMenuTextItem itemCenter = new BarMenuTextItem(1, "预警设置");
		itemCenter.setTag(TitleBar.Position.CENTER);
		menu.addItem(itemCenter);

		BarMenuCustomItem rightItem = new BarMenuCustomItem(2);
		LinearLayout layoutDelete = (LinearLayout) View.inflate(getContext(), R.layout.layout_setwarn_titlebar_right_item, null);
		rightItem.setCustomView(layoutDelete);
		rightItem.setTag(TitleBar.Position.RIGHT);
		menu.addItem(rightItem);

		return true;
	}

	@Override
	protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {
	}

	@Override
	public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
		int itemId = menuitem.getItemId();

		if (itemId == 0 && mPageChangeFlag == 0) {
			doBack();
		} else if (itemId == 2) {
			if (isStockHasSetAlert == false) {
				// 删除时，如果股票未设置过预警，直接退出界面
				mPageChangeFlag = -1;
				finish();
			} else {
				doRemoveWarn();
			}
		}
	}

	private void addZxgAndAlert() {
		// 1. 判断本地自选股中是否有该股票，如果有，直接添加预警
		// 2. 如果本地自选股中没有，添加股票到后台自选股
		// 3. 如果添加到后台自选股成功，添加自选股到本地，同时添加预警
		// 4. 如果添加到后台自选股失败，退出界面
		final OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
		if (optionalInfo.hasGoods(stockId) >= 0) {
			// 本地自选股有此股票
			doUpdateWarn();
		} else {
			// 本地自选股无此股票，添加股票到服务器自选股
			final Goods goods = new Goods(stockId, stockName);
			addZXG(OptionalInfo.TYPE_KEY_ALL, goods, new OnOperateZXGListener() {

				@Override
				public void onOperate(boolean isSuccess, String msg) {
					if (isSuccess) {
						String tAddtype = OptionalInfo.TYPE_DEFAULT;
						if (optionalInfo.addGoods(tAddtype, goods)) {
							doUpdateWarn();
						} else {
							showTip("添加预警失败");
							SetAlertPage.this.finish();
						}
					} else {
						// 添加到服务器自选失败，直接退出界面
						showTip("添加预警失败");
						SetAlertPage.this.finish();
					}
				}
			});
		}
	}

	/**
	 * 获取更新数据
	 * */
	private String getUpdateValue() {
		JSONObject objCurrent = new JSONObject();
		objCurrent.put("id", String.valueOf(stockId));

		objCurrent.put("b_60", getStatusFromFlag(changedStatusHolder.isBuyHourEnable));
		objCurrent.put("b_day", getStatusFromFlag(changedStatusHolder.isBuyDayEnable));
		objCurrent.put("b_week", getStatusFromFlag(changedStatusHolder.isBuyWeekEnable));

		objCurrent.put("s_60", getStatusFromFlag(changedStatusHolder.isSellHourEnable));
		objCurrent.put("s_day", getStatusFromFlag(changedStatusHolder.isSellDayEnable));
		objCurrent.put("s_week", getStatusFromFlag(changedStatusHolder.isSellWeekEnable));

		objCurrent.put("bdf", getStatusFromFlag(changedStatusHolder.isDfEnable));
		objCurrent.put("bhsl", getStatusFromFlag(changedStatusHolder.isTurnoverEnable));
		objCurrent.put("bp", getStatusFromFlag(changedStatusHolder.isPriceEnable));
		objCurrent.put("bzf", getStatusFromFlag(changedStatusHolder.isZfEnable));

		objCurrent.put("df", changedStatusHolder.df);
		objCurrent.put("fp", changedStatusHolder.price);
		objCurrent.put("hsl", changedStatusHolder.turnover);
		objCurrent.put("refp", changedStatusHolder.currentPrice);
		objCurrent.put("zf", changedStatusHolder.zf);

		return objCurrent.toJSONString();
	}

	private String getStatusFromFlag(boolean flag) {
		if (flag)
			return "1";

		return "0";
	}

	private void initViews() {
		tvStockName = (TextView) findViewById(R.id.page_setwarn_tv_stock_name);
		tvStockCode = (TextView) findViewById(R.id.page_setwarn_tv_stock_code);
		tvStockPrice = (TextView) findViewById(R.id.page_setwarn_tv_stock_price);
		tvStockZdf = (TextView) findViewById(R.id.page_setwarn_tv_stock_zdf);

		etPrice = (EditText) findViewById(R.id.page_setwarn_et_price);
		etIncrease = (EditText) findViewById(R.id.page_setwarn_et_increse);
		etDecrease = (EditText) findViewById(R.id.page_setwarn_et_decrease);
		etTurnover = (EditText) findViewById(R.id.page_setwarn_et_turnover);

		imgBuyDayCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_buy_day);
		imgBuyWeekCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_buy_week);
		imgBuyHourCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_buy_hour);

		imgSellDayCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_sell_day);
		imgSellWeekCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_sell_week);
		imgSellHourCpx = (ImageView) findViewById(R.id.page_setwarn_img_cpx_sell_hour);

		scrollView = (ScrollView) findViewById(R.id.page_setwarn_scroll);
		scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent mv) {
			    InputMethodUtil.closeSoftKeyBoard(SetAlertPage.this);

				return false;
			}
		});

		findViewById(R.id.page_setwarn_layout_buy_day_cpx).setOnClickListener(this);
		findViewById(R.id.page_setwarn_layout_buy_week_cpx).setOnClickListener(this);
		findViewById(R.id.page_setwarn_layout_buy_hour_cpx).setOnClickListener(this);

		findViewById(R.id.page_setwarn_layout_sell_day_cpx).setOnClickListener(this);
		findViewById(R.id.page_setwarn_layout_sell_week_cpx).setOnClickListener(this);
		findViewById(R.id.page_setwarn_layout_sell_hour_cpx).setOnClickListener(this);

		// 默认情况下，所有控件都不能编辑
		controlWidgetsEnablable(false);

		etPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (changedStatusHolder != null) {
					String price = text.toString();
					if (TextUtils.isEmpty(price)) {
						price = "0.00";
					}

					changedStatusHolder.price = DataUtils.formatPrice(price);

					if (TextUtils.isEmpty(text)) {
						changedStatusHolder.isPriceEnable = false;
					} else {
						changedStatusHolder.isPriceEnable = true;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		etIncrease.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (changedStatusHolder != null) {
					String zf = text.toString();
					if (TextUtils.isEmpty(zf)) {
						zf = "0.00";
					}

					changedStatusHolder.zf = DataUtils.formatPercent2Float(zf);

					if (TextUtils.isEmpty(text)) {
						changedStatusHolder.isZfEnable = false;
					} else {
						changedStatusHolder.isZfEnable = true;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		etDecrease.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (changedStatusHolder != null) {
					String df = text.toString();
					if (TextUtils.isEmpty(df)) {
						df = "0.00";
					}

					changedStatusHolder.df = DataUtils.formatPercent2Float(df);

					if (TextUtils.isEmpty(text)) {
						changedStatusHolder.isDfEnable = false;
					} else {
						changedStatusHolder.isDfEnable = true;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		etTurnover.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (changedStatusHolder != null) {
					String turnover = text.toString();
					if (TextUtils.isEmpty(turnover)) {
						turnover = "0.00";
					}

					changedStatusHolder.turnover = DataUtils.formatPercent2Float(turnover);

					if (TextUtils.isEmpty(text)) {
						changedStatusHolder.isTurnoverEnable = false;
					} else {
						changedStatusHolder.isTurnoverEnable = true;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	/**
	 * 获取个股最新价和涨跌幅
	 * */
	private void requestDataForStock() {
		if (stockId == 0)
			return;

		ArrayList<Integer> goodsId = new ArrayList<Integer>();
		goodsId.add(stockId);

		ArrayList<Integer> reqFileds = new ArrayList<Integer>();
		reqFileds.add(GoodsParams.ZXJ);
		reqFileds.add(GoodsParams.ZDF);
		reqFileds.add(GoodsParams.ZHANGDIE);

		DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_FLAG_SOTCK_PRICE));
		pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));

		requestQuote(pkg, IDUtils.DynaValueData);
	}

	/**
	 * 解析单支股票预警配置，并保存在传到参数中
	 * */
	private void analyseSingleWarnConfig(String config, StatusHolder sh) {
		try {
			JSONObject objItem = JSON.parseObject(config);
			if (objItem == null) {
				return;
			}

			// 该支股票已设置预警，保存设置的预警信息，并更新界面
			sh.price = objItem.getString("fp");
			sh.zf = objItem.getString("zf");
			sh.df = objItem.getString("df");
			sh.turnover = objItem.getString("hsl");

			sh.isPriceEnable = isWarnEnable(objItem.getString("bp"));
			sh.isZfEnable = isWarnEnable(objItem.getString("bzf"));
			sh.isDfEnable = isWarnEnable(objItem.getString("bdf"));
			sh.isTurnoverEnable = isWarnEnable(objItem.getString("bhsl"));

			sh.isBuyDayEnable = isWarnEnable(objItem.getString("b_day"));
			sh.isBuyWeekEnable = isWarnEnable(objItem.getString("b_week"));
			sh.isBuyHourEnable = isWarnEnable(objItem.getString("b_60"));

			sh.isSellDayEnable = isWarnEnable(objItem.getString("s_day"));
			sh.isSellWeekEnable = isWarnEnable(objItem.getString("s_week"));
			sh.isSellHourEnable = isWarnEnable(objItem.getString("s_60"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新界面各组件状态
	 * */
	private void updateView(StatusHolder sh) {
		if (sh == null) {
			return;
		}

		// 根据开关决定显示，如果开关打开，正常显示，如果开关关闭，显示为空
		if (sh.isPriceEnable) {
			etPrice.setText(formatValue(sh.price, false));
		} else {
			etPrice.setText("");
		}
		if (sh.isZfEnable) {
			etIncrease.setText(formatValue(sh.zf, true));
		} else {
			etIncrease.setText("");
		}
		if (sh.isDfEnable) {
			etDecrease.setText(formatValue(sh.df, true));
		} else {
			etDecrease.setText("");
		}
		if (sh.isTurnoverEnable) {
			etTurnover.setText(formatValue(sh.turnover, true));
		} else {
			etTurnover.setText("");
		}

		if (sh.isBuyDayEnable) {
			imgBuyDayCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgBuyDayCpx.setImageResource(R.drawable.img_check_uncheck);
		}

		if (sh.isBuyWeekEnable) {
			imgBuyWeekCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgBuyWeekCpx.setImageResource(R.drawable.img_check_uncheck);
		}

		if (sh.isBuyHourEnable) {
			imgBuyHourCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgBuyHourCpx.setImageResource(R.drawable.img_check_uncheck);
		}

		if (sh.isSellDayEnable) {
			imgSellDayCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgSellDayCpx.setImageResource(R.drawable.img_check_uncheck);
		}

		if (sh.isSellWeekEnable) {
			imgSellWeekCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgSellWeekCpx.setImageResource(R.drawable.img_check_uncheck);
		}

		if (sh.isSellHourEnable) {
			imgSellHourCpx.setImageResource(R.drawable.img_check_checked);
		} else {
			imgSellHourCpx.setImageResource(R.drawable.img_check_uncheck);
		}
	}

	/**
	 * 格式化字符串-价格、百分比
	 * */
	private String formatValue(String value, boolean isPercent) {
		if (TextUtils.isEmpty(value)) {
			return "0.00";
		}

		double double1 = 0d;
		try {
			double1 = Double.parseDouble(value);
		} catch (Exception e) {
		}

		if (isPercent) {
			double1 = double1 * 100;
		}

		if (double1 == 0d) {
			return "0.00";
		}

		DecimalFormat formater = new DecimalFormat("0.00");

		return formater.format(double1);
	}

	/**
	 * 预警开关是否已打开，如果flag为1，已开打，否则未打开
	 * */
	private boolean isWarnEnable(String flag) {
		if (TextUtils.isEmpty(flag)) {
			return false;
		}

		if (flag.equals("1")) {
			return true;
		}

		return false;
	}

	protected void updateFromQuote(QuotePackageImpl pkg) {
		if (pkg instanceof DynaValueDataPackage) {
			DynaValueDataPackage ddpkg = (DynaValueDataPackage) pkg;
			int id = ddpkg.getRequestType();

			if (id == REQUEST_FLAG_SOTCK_PRICE) {
				DynaValueData_Reply reply = ddpkg.getResponse();

				List<Integer> lstFieldIds = reply.getRepFieldsList();
				List<DynaQuota> data = reply.getQuotaValueList();

				if (data.size() > 0) {
					DynaQuota quote = data.get(0);
					List<String> lstFieldValues = quote.getRepFieldValueList();

					int indexZXJ = lstFieldIds.indexOf(GoodsParams.ZXJ);
					int indexZDF = lstFieldIds.indexOf(GoodsParams.ZDF);
					int indexZD = lstFieldIds.indexOf(GoodsParams.ZHANGDIE);

					String price = lstFieldValues.get(indexZXJ);
					String zd = lstFieldValues.get(indexZD);
					String zdf = lstFieldValues.get(indexZDF);
					int color = getZDPColor(FontUtils.getColorByZD(zd));

					stockPrice = DataUtils.getPrice(price);
					tvStockPrice.setText(stockPrice);
					tvStockPrice.setTextColor(color);
					tvStockZdf.setText(DataUtils.getZDF(zdf));
					tvStockZdf.setTextColor(color);

					// 如果拿到了当前股票价格，就令各个控件可以编辑
					if (!TextUtils.isEmpty(stockPrice)) {
						controlWidgetsEnablable(true);

						originStatusHolder.currentPrice = stockPrice;
						copyWarnConfig(originStatusHolder, changedStatusHolder);
					}
				}
			}
		}
	}


	/**
	 * 控制所有控件能否编辑
	 * */
	private void controlWidgetsEnablable(boolean isEnable) {
		etPrice.setEnabled(isEnable);
		etIncrease.setEnabled(isEnable);
		etDecrease.setEnabled(isEnable);
		etTurnover.setEnabled(isEnable);

		findViewById(R.id.page_setwarn_layout_buy_day_cpx).setEnabled(isEnable);
		findViewById(R.id.page_setwarn_layout_buy_week_cpx).setEnabled(isEnable);
		findViewById(R.id.page_setwarn_layout_buy_hour_cpx).setEnabled(isEnable);

		findViewById(R.id.page_setwarn_layout_sell_day_cpx).setEnabled(isEnable);
		findViewById(R.id.page_setwarn_layout_sell_week_cpx).setEnabled(isEnable);
		findViewById(R.id.page_setwarn_layout_sell_hour_cpx).setEnabled(isEnable);
	}

	/**
	 * 复制预警信息到另一个对象中
	 * */
	private void copyWarnConfig(StatusHolder originStatusHolder, StatusHolder destStatusHolder) {
		destStatusHolder.price = originStatusHolder.price;
		destStatusHolder.zf = originStatusHolder.zf;
		destStatusHolder.df = originStatusHolder.df;
		destStatusHolder.turnover = originStatusHolder.turnover;
		destStatusHolder.currentPrice = originStatusHolder.currentPrice;

		destStatusHolder.isPriceEnable = originStatusHolder.isPriceEnable;
		destStatusHolder.isZfEnable = originStatusHolder.isZfEnable;
		destStatusHolder.isDfEnable = originStatusHolder.isDfEnable;
		destStatusHolder.isTurnoverEnable = originStatusHolder.isTurnoverEnable;

		destStatusHolder.isBuyDayEnable = originStatusHolder.isBuyDayEnable;
		destStatusHolder.isBuyHourEnable = originStatusHolder.isBuyHourEnable;
		destStatusHolder.isBuyWeekEnable = originStatusHolder.isBuyWeekEnable;

		destStatusHolder.isSellDayEnable = originStatusHolder.isSellDayEnable;
		destStatusHolder.isSellHourEnable = originStatusHolder.isSellHourEnable;
		destStatusHolder.isSellWeekEnable = originStatusHolder.isSellWeekEnable;
	}

	/**
	 * 记录股票预警设置状态
	 * */
	private class StatusHolder {
		public String stockId;

		// 行情数据设置当前值，默认为0.00
		public String price = "0.00", zf = "0.00", df = "0.00", turnover = "0.00", currentPrice = "0.00";

		// 行情数据开关状态
		public boolean isPriceEnable, isZfEnable, isDfEnable, isTurnoverEnable;

		// 操盘线卖出点开关状态
		public boolean isBuyDayEnable, isBuyWeekEnable, isBuyHourEnable;

		// 操盘线买入点开关状态
		public boolean isSellDayEnable, isSellWeekEnable, isSellHourEnable;

		@Override
		public int hashCode() {
			String string = price + zf + df + turnover + isPriceEnable + isZfEnable + isDfEnable + isTurnoverEnable + isBuyDayEnable + isBuyHourEnable + isBuyWeekEnable + isSellDayEnable + isSellHourEnable + isSellWeekEnable;

			return string.hashCode();
		}

	}

	/**
	 * 判断预警是否有效
	 * */
	private boolean isAlertSettingValid(StatusHolder sh) {
		// 如果行情数据开关、操盘线买入卖出开关全部都是关闭，则该支股票的预警设置为无效，否则有效
		if (!sh.isPriceEnable && !sh.isZfEnable && !sh.isDfEnable && !sh.isTurnoverEnable && !sh.isBuyDayEnable && !sh.isBuyHourEnable && !sh.isBuyWeekEnable && !sh.isSellDayEnable && !sh.isSellHourEnable && !sh.isSellWeekEnable) {
			return false;
		}

		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.page_setwarn_layout_buy_day_cpx: {
			if (changedStatusHolder.isBuyDayEnable) {
				changedStatusHolder.isBuyDayEnable = false;
			} else {
				changedStatusHolder.isBuyDayEnable = true;
			}
		}
			break;
		case R.id.page_setwarn_layout_buy_week_cpx: {
			if (changedStatusHolder.isBuyWeekEnable) {
				changedStatusHolder.isBuyWeekEnable = false;
			} else {
				changedStatusHolder.isBuyWeekEnable = true;
			}
		}
			break;
		case R.id.page_setwarn_layout_buy_hour_cpx: {
			if (changedStatusHolder.isBuyHourEnable) {
				changedStatusHolder.isBuyHourEnable = false;
			} else {
				changedStatusHolder.isBuyHourEnable = true;
			}
		}
			break;
		case R.id.page_setwarn_layout_sell_day_cpx: {
			if (changedStatusHolder.isSellDayEnable) {
				changedStatusHolder.isSellDayEnable = false;
			} else {
				changedStatusHolder.isSellDayEnable = true;
			}
		}
			break;
		case R.id.page_setwarn_layout_sell_week_cpx: {
			if (changedStatusHolder.isSellWeekEnable) {
				changedStatusHolder.isSellWeekEnable = false;
			} else {
				changedStatusHolder.isSellWeekEnable = true;
			}
		}
			break;
		case R.id.page_setwarn_layout_sell_hour_cpx: {
			if (changedStatusHolder.isSellHourEnable) {
				changedStatusHolder.isSellHourEnable = false;
			} else {
				changedStatusHolder.isSellHourEnable = true;
			}
		}
			break;
		default:
			break;
		}

		updateView(changedStatusHolder);
	}

}
