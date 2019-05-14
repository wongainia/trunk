package cn.emoney.acg.data.protocol.motif;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.motif.GroupTrendReply.GroupTrend_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class GroupTrendPackage extends QuotePackageImpl {
	private GroupTrend_Reply mReply = null;

	public GroupTrendPackage(DataHeadImpl head) {
		super(head);
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = GroupTrend_Reply.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public GroupTrend_Reply getResponse() {
		return mReply;
	}

}