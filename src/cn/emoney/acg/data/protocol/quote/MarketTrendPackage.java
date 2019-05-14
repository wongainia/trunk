package cn.emoney.acg.data.protocol.quote;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.MarketTrendReply.MarketTrend_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class MarketTrendPackage extends QuotePackageImpl {
	private MarketTrend_Reply mReply = null;

	public MarketTrendPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = MarketTrend_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public MarketTrend_Reply getResponse() {
		return mReply;
	}

}