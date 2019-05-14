package cn.emoney.acg.helper.lb;

import java.io.UnsupportedEncodingException;

import android.os.Handler;
import android.os.Message;
import cn.emoney.acg.util.LogUtil;

public class LoadBalance {
	public static final String host1 = "istockm1.emoney.cn";
	public static final int port1 = 8080;
	
	public static final String host2 = "istockm2.emoney.cn";
	public static final int port2 = 8080;
	
	public static final short protocolNum = 30107;
	
	public static final byte[] PROTOCOL_LB = new byte[]{-120, -103, -101, 117, 0, 0, 0, 0, 9, 0, 0, 0, -16, 10, 0, 0, 0, 0, 0, 0, 0, -112, 31, 0, 0};
	private TinySocketClient tinySocketClient = null;
	
	private CallBack mCallBack = null;
	private Handler mHandler = null;
	
	public LoadBalance(CallBack callBack)
	{
		mCallBack = callBack;
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == TinySocketClient.CMD_IN) {
					byte[] out = (byte[]) msg.obj;
					if (out != null) {
						byte cAck = out[0];
						//成功
						if (cAck == 1) {
							byte[] byte_size = new byte[2];
							System.arraycopy(out, 1, byte_size, 0, 2);
							short sSize = DataConvert.byteArrayToShort(byte_size);
							if (out.length >= sSize + 3 + 2) {
								byte[] byte_str = new byte[sSize];
								System.arraycopy(out, 3, byte_str, 0, sSize);
								String sIp = "";
								try {
									sIp = new String(byte_str, "utf-8");
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								byte[] byte_port = new byte[2];
								System.arraycopy(out, 3 + sSize, byte_port, 0, 2);
								short sPort = DataConvert.byteArrayToShort(byte_port);
								
								if (sIp != null && !sIp.equals("") && sPort != 0) {
									if (mCallBack != null) {
										mCallBack.onComplete(sIp, sPort);
									}
								}
								else {
									if (mCallBack != null) {
										mCallBack.onError(-1);
									}
								}
								
							}
							else {
								if (mCallBack != null) {
									mCallBack.onError(-1);
								}
							}
						}
						else {
							if (mCallBack != null) {
								mCallBack.onError(cAck);
							}
						}
					}
					else {
						if (mCallBack != null) {
							mCallBack.onError(-1);
						}
					}
				}
				else if (msg.what == TinySocketClient.CMD_ERROR) {
					if (mCallBack != null) {
						mCallBack.onError(-1);
					}
				}
				
				tinySocketClient.close();
			}
			
		};
		tinySocketClient = new TinySocketClient(host1, port1, host2, port2);
		tinySocketClient.setInHandler(mHandler);
		new Thread(tinySocketClient).start();
	}
	
	public void requestBSData() {
		
		boolean bRet = isBigendian();
		
		ProtocolHeader header = new ProtocolHeader(protocolNum);
		byte[] arg1 = new byte[1];
		arg1[0] = 0;
		byte[] arg2 = DataConvert.integerToByteArray(0);
		byte[] arg3 = DataConvert.integerToByteArray(8080);
		
		byte[] byte_body = new byte[9];
		int offset = 0;
		System.arraycopy(arg1, 0, byte_body, offset, 1);
		offset += 1;
		System.arraycopy(arg2, 0, byte_body, offset, arg2.length);
		offset += arg2.length;
		System.arraycopy(arg3, 0, byte_body, offset, arg3.length);
		offset += arg3.length;
		
		header.m_iDataLength = byte_body.length;
		int sum = 0;
		for (int i = 0; i < byte_body.length; i++) {
			sum += (byte_body[i] & 0xff);
		}
		int iCheckSum = sum & 0xffff;
		short sCheckSum = (short) ((iCheckSum &0x0fff) << 4);
		header.m_sChecksum = sCheckSum;
		
		byte[] byte_head = header.toByte();
		
		int totalLen = byte_head.length + byte_body.length;
		byte[] byte_send = new byte[totalLen];
		System.arraycopy(byte_head, 0, byte_send, 0, byte_head.length);
		System.arraycopy(byte_body, 0, byte_send, byte_head.length, byte_body.length);
		
		tinySocketClient.sendData(byte_send);
	}
	
	public void notifyFinish()
	{
		tinySocketClient.close();
	}

	public interface CallBack {
		public void onComplete(String bsIp, int bsPort);
		public void onError(int errorCode);
	}
	
	public static boolean isBigendian() {  
        short i = 0x1;  
        boolean bRet = ((i >> 8) == 0x1);  
        LogUtil.easylog("sky", "bRet = " + bRet);  
        return bRet;  
    }  
}
