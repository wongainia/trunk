package cn.emoney.acg.data.protocol.motif;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.motif.BuyClubListReply.BuyClubList_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class BuyClubListPackage extends QuotePackageImpl {
	private BuyClubList_Reply mReply = null;

	public BuyClubListPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = BuyClubList_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public BuyClubList_Reply getResponse() {
		return mReply;
	}

}