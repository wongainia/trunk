package cn.emoney.acg.data.protocol.optional;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.optional.OptionalShareReply.OptionalShare_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class OptionalSharePackage extends QuotePackageImpl {
	private OptionalShare_Reply mReply = null;

	public OptionalSharePackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = OptionalShare_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public OptionalShare_Reply getResponse() {
		return mReply;
	}

}