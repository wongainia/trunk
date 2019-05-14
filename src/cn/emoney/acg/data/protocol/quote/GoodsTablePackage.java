package cn.emoney.acg.data.protocol.quote;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.GoodsTableReply.GoodsTable_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class GoodsTablePackage extends QuotePackageImpl {
	private GoodsTable_Reply mReply = null;

	public GoodsTablePackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = GoodsTable_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public GoodsTable_Reply getResponse() {
		return mReply;
	}

}