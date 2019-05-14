package cn.emoney.acg.data.protocol.quote;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quote.CpxReply.Cpx_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class CpxPackage extends QuotePackageImpl {
	private Cpx_Reply mReply = null;

	public CpxPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = Cpx_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Cpx_Reply getResponse() {
		return mReply;
	}

}