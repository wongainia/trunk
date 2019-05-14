package cn.emoney.acg.service.goodstable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestUrl;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.info.GlobalMessage;
import cn.emoney.acg.data.protocol.quote.GoodsTablePackage;
import cn.emoney.acg.data.protocol.quote.GoodsTableReply.GoodsTable_Reply;
import cn.emoney.acg.data.protocol.quote.GoodsTableReply.GoodsTable_Reply.GoodsTable;
import cn.emoney.acg.data.protocol.quote.GoodsTableRequest.GoodsTable_Request;
import cn.emoney.acg.data.protocol.quote.MarketDateTimePackage;
import cn.emoney.acg.data.protocol.quote.MarketDateTimeReply.MarketDateTime_Reply;
import cn.emoney.acg.helper.db.DSQLiteDatabase;
import cn.emoney.acg.helper.db.DSQLiteDatabase.StockInfo;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.PinyinUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.sky.libs.db.GlobalDBHelper;
import cn.emoney.sky.libs.network.HttpClient;
import cn.emoney.sky.libs.network.HttpClientFactory;
import cn.emoney.sky.libs.network.HttpDataResponseHandler;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;
import cn.emoney.sky.libs.network.pkg.DataPackageImpl;

public class StockService extends Service {

	// 码表全局属性
	private int mTotalReceivedCount = 0;
	List<GoodsTable> lstDel = new ArrayList<GoodsTable>();
	List<GoodsTable> lstMod = new ArrayList<GoodsTable>();

	protected DSQLiteDatabase mDBHelper = null;
	protected Timer timer = null;
	protected Handler mHandler = null;
	protected boolean hasStockUpdated = false;
	protected final static int MSG_UPDATE_STOCKLIST = 290001;

	private GlobalDBHelper mGlobalDBHelper = null;

	// 从网络取到的码表的最后更新时间
	private int date;
	// 从sharepreference中取到的 码表的最后更新时间
	// private int curModDate;

	HttpClient mHttpClient;

	public DSQLiteDatabase getSQLiteDBHelper() {
		if (mDBHelper == null) {
			mDBHelper = new DSQLiteDatabase(this);
		}

		return mDBHelper;
	}

	public void closeSQLDBHelper() {
		if (mDBHelper != null) {
			mDBHelper.close();
		}
		mDBHelper = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DataModule.G_DATABASE_VERNUMBER = getGlobalDBHelper().getInt(DataModule.G_KEY_DATABASE_VERNUMBERL, DataModule.G_DATABASE_VERNUMBER);

		getSQLiteDBHelper();

		lstDel.clear();
		lstMod.clear();
		requestGoodsTable();
		LogUtil.easylog("curSQLiteDate" + DataModule.G_DATABASE_VERNUMBER);

		final MarketDateTimePackage pkgMarketTime = new MarketDateTimePackage(new QuoteHead((short) 0));
		pkgMarketTime.setRequest(GlobalMessage.MessageCommon.newBuilder().setMsgData("").build());
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				requestMarketDateTime(pkgMarketTime, IDUtils.MarketDateTime);
			}
		};

		if (timer != null) {
			try {
				timer.cancel();
			} catch (Exception e) {
			}

		}

		timer = new Timer();

		timer.schedule(task, 1000, 10 * 60 * 1000); // 10分钟请求一次行情时间

		return super.onStartCommand(intent, flags, startId);
	}

	private void requestGoodsTable() {
		GoodsTablePackage pkg = new GoodsTablePackage(new QuoteHead((short) 0));
		// 拉取最新码表 全部获取
		// DataModule.G_DATABASE_VERNUMBER = 0;

		pkg.setRequest(GoodsTable_Request.newBuilder().setLastModDateVer(DataModule.G_DATABASE_VERNUMBER).setRecvPos(mTotalReceivedCount).build());
//		pkg.setRequest(GoodsTable_Request.newBuilder().setLastModDateVer(0).setRecvPos(mTotalReceivedCount).build());
		requestQuote(pkg, IDUtils.GoodsTable);

	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (mHandler == null) {
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					switch (msg.what) {
					case MSG_UPDATE_STOCKLIST:
						hasStockUpdated = true;
						break;
					default:
						break;
					}
					if (hasStockUpdated) {
						DataModule.G_DATABASE_VERNUMBER = date;
						getGlobalDBHelper().setInt(DataModule.G_KEY_DATABASE_VERNUMBERL, date);
						DataModule.LOAD_STATE_GOODTABLE = 1;
						stopSelf();
					}
				}

			};
		}

	}

	public HttpClient getHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = HttpClientFactory.getInstance().createHttpClient(getApplicationContext());
		}
		return mHttpClient;
	}

	public void requestMarketDateTime(QuotePackageImpl pkg, int cmd) {
		String url = RequestUrl.host + cmd;
		getHttpClient().requestBinaryData(url, pkg, new HttpDataResponseHandler() {

			@Override
			public void onRequestSuccess(DataPackageImpl pkg) {
				// TODO Auto-generated method stub
				super.onRequestSuccess(pkg);

				MarketDateTimePackage dateTimePkg = (MarketDateTimePackage) pkg;
				MarketDateTime_Reply gr = dateTimePkg.getResponse();// 得到最新行情数据

				DataModule.G_CURRENT_MARKET_DATE = gr.getCurMarketDate(); // 行情日期
				DataModule.G_CURRENT_MARKET_TIME = gr.getCurMarketTime(); // 行情时间

				DataModule.G_CURRENT_SERVER_DATE = gr.getCurSysDate(); // 服务器日期
				DataModule.G_CURRENT_SERVER_TIME = gr.getCurSysTime(); // 服务器时间

				if (DataModule.G_CURRENT_MARKET_TIME > 90100 && DataModule.G_CURRENT_MARKET_TIME < 150100) {
					DataModule.G_AUTO_REFRESH = true;
				} else {
					DataModule.G_AUTO_REFRESH = false;
				}
			}

			@Override
			public void onTrafficIn(long byteLen) {
				// TODO Auto-generated method stub
				super.onTrafficIn(byteLen);
			}

			@Override
			public void onTrafficOut(long byteLen) {
				// TODO Auto-generated method stub
				super.onTrafficOut(byteLen);
			}

		});
	}

	public void requestQuote(QuotePackageImpl pkg, int cmd) {
		String url = RequestUrl.host + cmd;

		LogUtil.easylog("sky", "StockService->requestQuote->url:" + url);
		List<Header> headers = new ArrayList<Header>();
		UserInfo userInfo = DataModule.getInstance().getUserInfo();
		headers.add(new BasicHeader("Authorization", userInfo.getToken()));

		getHttpClient().requestBinaryDataWithHeaders(url, pkg, new HttpDataResponseHandler() {

			@Override
			public void onTrafficIn(long byteLen) {
				// TODO Auto-generated method stub
				LogUtil.easylog("sky", "requestQuote->onTrafficIn");
			}

			@Override
			public void onTrafficOut(long byteLen) {
				// TODO Auto-generated method stub
				LogUtil.easylog("sky", "requestQuote->onTrafficOut");
			}

			@Override
			public void onRequestSuccess(DataPackageImpl pkg) {
				// TODO Auto-generated method stub
				super.onRequestSuccess(pkg);

				GoodsTablePackage goodsTable = (GoodsTablePackage) pkg;
				GoodsTable_Reply gr = goodsTable.getResponse();// 得到码表数据

				date = gr.getCurModDateVer();

				// 行情数据日期 ,已增加clock包
				DataModule.G_CURRENT_MARKET_DATE = gr.getCurUpdateMarketDate();

				List<GoodsTable> t_del = gr.getItemDelList();
				List<GoodsTable> t_add = gr.getItemModList();
				if (t_del != null && t_del.size() > 0) {
					lstDel.addAll(t_del);
				}
				if (t_add != null && t_add.size() > 0) {
					lstMod.addAll(t_add);
				}

				int remainSize = gr.getRemainSize();
				if (remainSize > 0) {
					mTotalReceivedCount = lstDel.size() + lstMod.size();
					requestGoodsTable();
				} else {
					LogUtil.easylog("receiveDate=" + date + " curDate=" + DataModule.G_DATABASE_VERNUMBER);
					if (DataModule.G_DATABASE_VERNUMBER < date) {
						operateSqlite_new(date);
					} else {
						closeSQLDBHelper();
						DataModule.LOAD_STATE_GOODTABLE = 1;
					}
				}

			}

			@Override
			public void onDecodeFailure(String msg, DataHeadImpl head) {
				// TODO Auto-generated method stub
				super.onDecodeFailure(msg, head);
				DataModule.LOAD_STATE_GOODTABLE = -1;
				LogUtil.easylog("sky", "requestQuote->onDecodeFailure");
				// stopRequest();
				// Message m = new Message();
				// m.what = MSG_DECODE_ERROR;
				// m.obj = msg;
				// mHandler.sendMessage(m);
			}

			@Override
			public void onRequestFailure(String msg, DataHeadImpl head) {
				// TODO Auto-generated method stub
				super.onRequestFailure(msg, head);
				DataModule.LOAD_STATE_GOODTABLE = -1;
				LogUtil.easylog("sky", "requestQuote->onRequestFailure");
				// stopRequest();
				// Message m = new Message();
				// m.what = MSG_NETWORK_ERROR;
				// m.obj = msg;
				// mHandler.sendMessage(m);
			}

			@Override
			public void onRequestFinish(int reqCount) {
				// TODO Auto-generated method stub
				super.onRequestFinish(reqCount);
				LogUtil.easylog("sky", "requestQuote->onRequestFinish");
				// dismissProgress();
			}

			@Override
			public void onRequestStart(int reqCount) {
				// TODO Auto-generated method stub
				super.onRequestStart(reqCount);
				LogUtil.easylog("sky", "requestQuote->onRequestStart");
				// showProgress();
			}

		}, headers);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		closeSQLDBHelper();

	}

	private void operateSqlite_new(int versionDate) {
		Vector<StockInfo> deletes = new Vector<StockInfo>();
		Vector<StockInfo> adds = new Vector<StockInfo>();

		String[] pinyins;
		String py;
		long dtime = System.currentTimeMillis();
		for (GoodsTable goodsTable : lstDel) {
			StockInfo stockInfo = new StockInfo();

			stockInfo.mStockCode = Util.FormatStockCode(goodsTable.getGoodsId());
			LogUtil.easylog("dels.stockcode:" + goodsTable.getGoodsId());
			deletes.add(stockInfo);
		}

		for (GoodsTable goodsTable : lstMod) {
			StockInfo stockInfo = new StockInfo();

			pinyins = PinyinUtil.GetAllMultiFirstPinyin(goodsTable.getGoodsName());
			py = "";
			int len = pinyins.length;
			for (int j = 0; j < len; j++) {
				if (j == (len - 1)) {
					py += pinyins[j];
				} else {
					py += pinyins[j] + ",";
				}
			}

			if (DataUtils.IsBK(goodsTable.getGoodsId())) {
				py = py + "," + DataUtils.format_bk_GoodCode(goodsTable.getGoodsId());
			}

			stockInfo.mStockName = goodsTable.getGoodsName();
			stockInfo.mStockCode = Util.FormatStockCode(goodsTable.getGoodsId());

			LogUtil.easylog("adds.stockcode:" + goodsTable.getGoodsId() + " 名称: " + goodsTable.getGoodsName());
			stockInfo.mStockPinYin = py;
			stockInfo.mUpdateDate = date;
			adds.add(stockInfo);
		}
		LogUtil.easylog("sky", "get py use time = " + (System.currentTimeMillis() - dtime));
		LogUtil.easylog("goodtable-> addSize:" + adds.size() + " delSize: " + deletes.size());

		SQLiteDatabase db = null;
		try {
			db = getSQLiteDBHelper().getWritableDatabase();
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (db == null) {
			DataModule.LOAD_STATE_GOODTABLE = 1;
			return;
		}
		dtime = System.currentTimeMillis();
		db.beginTransaction();
		getSQLiteDBHelper().addStockInfos2(db, adds);
		getSQLiteDBHelper().deleteStockInfos(db, deletes);
		// db.setVersion(versionDate);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		closeSQLDBHelper();
		LogUtil.easylog("sky", "OpSql use time = " + (System.currentTimeMillis() - dtime));
		Message msg = new Message();
		msg.what = MSG_UPDATE_STOCKLIST;
		mHandler.sendMessage(msg);
		DataModule.LOAD_STATE_GOODTABLE = 1;
	}

	public GlobalDBHelper getGlobalDBHelper() {
		if (mGlobalDBHelper == null) {
			mGlobalDBHelper = new GlobalDBHelper(this, DataModule.DB_GLOBAL);
		}
		return mGlobalDBHelper;
	}

}
