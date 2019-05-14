package cn.emoney.acg.util.security;

import java.io.UnsupportedEncodingException;

import cn.emoney.acg.data.DataModule;
import u.aly.by;

public class CommonSecurityToolUtil {

	public static void mixByte(byte[] byteIn) {
		int n_cerLen = byteIn.length;
		Integer n_key = getLocNum1() + getLocNum2();
		String s_key = n_key.toString();
		byte[] bs_key = s_key.getBytes();
		int n_keyLen = bs_key.length;
		int i = 0;
		do {
			for (int j = 0; j < n_keyLen; j++) {
				int index = i + j;
				if (index < n_cerLen) {
					byteIn[index] = (byte) (byteIn[index] ^ bs_key[j]);
				} else {
					break;
				}

			}
			i = i + n_keyLen;
		} while (i < n_cerLen);
	}

	// 工具函数
	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEXCHAR[(b[i] & 0xf0) >>> 4]);
			sb.append(HEXCHAR[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static final byte[] toBytes(String s) {
		byte[] bytes;
		bytes = new byte[s.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	private static char[] HEXCHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String bytesToString(byte[] encrytpByte) {

		String result = "";

		for (Byte bytes : encrytpByte) {

			result += (char) bytes.intValue();

		}
		return result;
	}

	// byte[] 转 utf-8 编码的string
	public static String utf8ByteToString(byte[] bytes) {
		String retS = "";
		try {
			retS = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retS;
	}

	// 特殊混淆代码种子 start
	private static int getLocNum1() {
		return DataModule.G_SEED_NUM_1;
	}

	private static int getLocNum2() {
		return DataModule.G_SEED_NUM_2;
	}
	// 特殊混淆代码种子 end
}
