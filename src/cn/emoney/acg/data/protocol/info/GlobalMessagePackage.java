package cn.emoney.acg.data.protocol.info;

import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * k线
 * 
 * @author songyuxiang
 *
 */
public class GlobalMessagePackage extends InfoPackageImpl {

	private GlobalMessage.MessageCommon mReply = null;
	public GlobalMessagePackage(DataHeadImpl head) {
		super(head);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean readData(byte[] data, int arg1, int arg2) {
		try {
			mReply = GlobalMessage.MessageCommon.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public GlobalMessage.MessageCommon getResponse()
	{
		return mReply;
	}
}
