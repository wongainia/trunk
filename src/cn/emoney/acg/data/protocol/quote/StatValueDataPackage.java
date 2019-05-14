package cn.emoney.acg.data.protocol.quote;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.StatValueDataReply.StatValueData_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class StatValueDataPackage extends QuotePackageImpl {
	private StatValueData_Reply mReply = null;

	public StatValueDataPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = StatValueData_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public StatValueData_Reply getResponse() {
		return mReply;
	}

}