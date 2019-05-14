package cn.emoney.acg.data.protocol.quote;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.RelateBKsReply.RelateBKs_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class RelateBKsPackage extends QuotePackageImpl {
	private RelateBKs_Reply mReply = null;

	public RelateBKsPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = RelateBKs_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public RelateBKs_Reply getResponse() {
		return mReply;
	}

}