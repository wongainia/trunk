package cn.emoney.acg.data.helper;

import java.io.DataOutputStream;

import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

public class QuoteHead extends DataHeadImpl {

	public QuoteHead(short dataType) {
		super(dataType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public short getHeadLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void read(byte[] arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(DataOutputStream arg0) {
		// TODO Auto-generated method stub

	}

}
