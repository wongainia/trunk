package cn.emoney.acg.data.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;

import cn.emoney.sky.libs.network.pkg.DataHeadImpl;
import cn.emoney.sky.libs.network.pkg.DataPackageImpl;
import cn.emoney.sky.libs.network.pkg.ExDataOutputStream;

import com.google.protobuf.GeneratedMessage;

public abstract class QuotePackageImpl extends DataPackageImpl {

	private GeneratedMessage mGeneratedMessage = null;
	public QuotePackageImpl(DataHeadImpl head) {
		super(head);
		// TODO Auto-generated constructor stub
	}

	public void setRequest(GeneratedMessage msg)
	{
		mGeneratedMessage = msg;
	}
	
	
	
	@Override
	public void writeData(ExDataOutputStream arg0) {
		// TODO Auto-generated method stub
		try {
			byte[] data = mGeneratedMessage.toByteArray();
			arg0.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public HttpEntity getEntity()
	{
		byte[] bd = null;

		try
		{
			ByteArrayOutputStream bs1 = new ByteArrayOutputStream();
			ExDataOutputStream ds1 = new ExDataOutputStream(bs1);
			writeData(ds1);
			bs1.close();
			ds1.close();
			bd = bs1.toByteArray();

		} catch (Exception e)
		{
		}

		byte[] bytes = new byte[bd.length];
		System.arraycopy(bd, 0, bytes, 0, bd.length);
		return new ByteArrayEntity(bytes);
	}

	public boolean decodeData(byte[] binaryData)
	{
		return readData(binaryData, 0, binaryData.length);
	}

	
}
