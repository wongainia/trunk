package cn.emoney.acg.helper.lb;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.emoney.acg.util.LogUtil;

public class TinySocketClient implements Runnable {
	public static final int CMD_OUT = 0x345;
	public static final int CMD_IN = 0x346;
	public static final int CMD_ERROR = 0x347;

	private String mHost1 = null;
	private int mPort1 = 0;
	private String mHost2 = null;
	private int mPort2 = 0;

	private Socket mSocket = null;
	private Handler mInHandler = null;

	private Handler mOutHandler = null;

	private DataInputStream din = null;
	private DataOutputStream dout = null;

	ByteBuffer mByteBuffer = ByteBuffer.allocate(8 * 1024);

	private boolean bAlive = true;
	private int mSocketState = 0;

	TinySocketClient(String host, int port) {
		mHost1 = host;
		mPort1 = port;
	}

	TinySocketClient(String host1, int port1, String host2, int port2) {
		mHost1 = host1;
		mPort1 = port1;

		mHost2 = host2;
		mPort2 = port2;
	}

	public void setInHandler(Handler handler) {
		mInHandler = handler;
	}

	private boolean connectTOServer(String host, int port) {
		Socket oldSocket = mSocket;
		try {
			mSocket = new Socket(host, port);
			mSocket.setSoTimeout(2000);

			if (mSocket == null || !mSocket.isConnected()) {
				bAlive = false;
				return false;
			}
			din = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
			dout = new DataOutputStream(mSocket.getOutputStream());

			mSocketState = 0;
			bAlive = true;

			LogUtil.easylog("sky", "connected -> sendData() begin");
			dout.write(LoadBalance.PROTOCOL_LB);
			LogUtil.easylog("sky", "connected -> sendData() done");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			LogUtil.easylog("sky", "connected ->  UnknownHostException");
			bAlive = false;
			// e.printStackTrace();
		} catch (IOException e) {
			LogUtil.easylog("sky", "connected ->  IOException");
			bAlive = false;
			// e.printStackTrace();
		}

		try {
			if (oldSocket != null) {
				oldSocket.close();
				oldSocket = null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mSocket != null) {
			return mSocket.isConnected();
		} else {
			return false;
		}

	}

	private void initEnv() {
		mByteBuffer.clear();

		if (mOutHandler == null) {
			mOutHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.what == CMD_OUT) {
						try {
							if (dout != null) {
								LogUtil.easylog("sky", "dout.write begin");
								dout.write((byte[]) msg.obj);
								LogUtil.easylog("sky", "dout.write done");
							}
						} catch (IOException e) {
							LogUtil.easylog("sky", "dout.write error");
							e.printStackTrace();
						}
					}
				}
			};
		}

		new Thread() {
			@Override
			public void run() {
				byte[] byteIn = new byte[8 * 1024];
				ProtocolHeader pHeader = null;
				mByteBuffer.clear();
				while (mSocketState != -1) {
					if (bAlive == false) {
						mByteBuffer.clear();
						continue;
					}
					if (din != null) {
						int len = 0;
						try {
							len = din.read(byteIn);
						} catch (Exception e) {
							bAlive = false;
							LogUtil.easylog("sky", "TinySocketClient -> din.read(byteIn):Error");
						}
						if (len <= 0) {
							try {
								sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						}

						LogUtil.easylog("sky", "din.read(byteIn) > 0");
						byte[] byteTemp = new byte[len];
						System.arraycopy(byteIn, 0, byteTemp, 0, len);
						mByteBuffer.put(byteTemp);

						if (mByteBuffer.position() < 16) {
							try {
								sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						} else {
							byte[] byteHead = new byte[16];
							for (int i = 0; i < 16; i++) {
								byteHead[i] = mByteBuffer.get(i);
							}
							pHeader = new ProtocolHeader(byteHead);

							if (mByteBuffer.position() < 16 + pHeader.m_iDataLength) {
								try {
									sleep(50);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								continue;
							} else {
								mByteBuffer.flip();
							}
						}
						byte[] bData = null;
						if (pHeader.m_iDataLength > 0) {
							bData = new byte[pHeader.m_iDataLength];
							for (int i = 0; i < pHeader.m_iDataLength; i++) {
								bData[i] = mByteBuffer.get(i + 16);
							}
						}

						mByteBuffer.clear();
						Message msg = new Message();
						msg.what = CMD_IN;
						msg.obj = bData;
						if (mInHandler != null) {
							mInHandler.sendMessage(msg);
						}
					}

				}
			};
		}.start();
	}

	// -1:未连接 ; 0:发送成功
	public int sendData(byte[] outBuf) {
		if (mSocket == null || !mSocket.isConnected() || mOutHandler == null) {
			return -1;
		}
		Message msg = mOutHandler.obtainMessage(CMD_OUT, outBuf);
		mOutHandler.sendMessage(msg);
		return 0;
	}

	public void close() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			bAlive = false;
			mSocket = null;
		}
		LogUtil.easylog("sky", "TinySocketClient -> close");
		mSocketState = -1;
	}

	@Override
	public void run() {
		Looper.prepare();
		initEnv();

		boolean bRet = true;
		if (mHost1 != null) {
			bRet = connectTOServer(mHost1, mPort1);
			if (bRet == false) {
				if (mHost2 != null) {
					bRet = connectTOServer(mHost2, mPort2);
				}
			}
		}

		if (bRet == false) {
			sendBackErr();
		}

		Looper.loop();

	}

	public void sendBackErr() {
		Message msg = new Message();
		msg.what = CMD_ERROR;
		msg.obj = null;
		if (mInHandler != null) {
			mInHandler.sendMessage(msg);
		}
	}

}
