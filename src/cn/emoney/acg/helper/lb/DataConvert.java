package cn.emoney.acg.helper.lb;

import cn.emoney.acg.util.LogUtil;

public class DataConvert {
	public static byte[] byteArraysToByteArray(byte[]... byteArrays) {
		int allLen = 0;
		for (byte[] tempByteArray : byteArrays) {
			allLen += tempByteArray.length;
		}

		byte[] newByteArray = new byte[allLen];
		for (byte[] tempByteArray : byteArrays) {
			for (byte tempByte : tempByteArray) {
				newByteArray[newByteArray.length - allLen] = tempByte;
				allLen--;
			}
		}

		return newByteArray;
	}

	public static byte[] copyOfRange(byte[] byteArray, int from, int to) {
		if (to <= from || to > byteArray.length) {
			LogUtil.easylog("Converter", "[copyOfRange]: byteArray.length=" + byteArray.length + ",from=" + from + "" + ",to=" + to);
			return null;
		}
		byte[] copyArray = new byte[to - from];
		for (int index = 0; index < to - from; index++) {
			copyArray[index] = byteArray[from + index];
		}

		return copyArray;
	}

	public static byte[] integerToByteArray(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (temp & 0xff);
			temp = temp >> 8;
		}
		return b;
	}

	public static int byteArrayToInteger(byte[] b) {
		int s = 0;
		for (int i = 3; i >= 0; i--) {
			s = s << 8;
			s += b[i] & 0xff;

		}
		return s;
	}

	public static byte[] shortToByteArray(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			b[i] = (byte) (temp & 0xff);
			temp = temp >> 8;
		}
		return b;
	}

	public static short byteArrayToShort(byte[] b) {
		int s = 0;
		for (int i = 1; i >= 0; i--) {
			s = s << 8;
			s += b[i] & 0xff;
		}
		return (short) (s & 0xffff);
	}
	
	
	//有大小端的转换
	public static byte[] little_intToByte(int i, int len) {  
        byte[] abyte = new byte[len];  
        if (len == 1) {  
            abyte[0] = (byte) (0xff & i);  
        } else if (len == 2) {  
            abyte[0] = (byte) (0xff & i);  
            abyte[1] = (byte) ((0xff00 & i) >> 8);  
        } else {  
            abyte[0] = (byte) (0xff & i);  
            abyte[1] = (byte) ((0xff00 & i) >> 8);  
            abyte[2] = (byte) ((0xff0000 & i) >> 16);  
            abyte[3] = (byte) ((0xff000000 & i) >> 24);  
        }  
        return abyte;  
    }  
   
    public static int little_bytesToInt(byte[] bytes) {  
        int addr = 0;  
        if (bytes.length == 1) {  
            addr = bytes[0] & 0xFF;  
        } else if (bytes.length == 2) {  
            addr = bytes[0] & 0xFF;  
            addr |= (((int) bytes[1] << 8) & 0xFF00);  
        } else {  
            addr = bytes[0] & 0xFF;  
            addr |= (((int) bytes[1] << 8) & 0xFF00);  
            addr |= (((int) bytes[2] << 16) & 0xFF0000);  
            addr |= (((int) bytes[3] << 24) & 0xFF000000);  
        }  
        return addr;  
    }  
   
    /** 
     * int to byte[] 支持 1或者 4 个字节 
     *  
     * @param i 
     * @param len 
     * @return 
     */ 
    public static byte[] big_intToByte(int i, int len) {  
        byte[] abyte = new byte[len];  
        ;  
        if (len == 1) {  
            abyte[0] = (byte) (0xff & i);  
        } else if (len == 2) {  
            abyte[0] = (byte) ((i >>> 8) & 0xff);  
            abyte[1] = (byte) (i & 0xff);  
        } else {  
            abyte[0] = (byte) ((i >>> 24) & 0xff);  
            abyte[1] = (byte) ((i >>> 16) & 0xff);  
            abyte[2] = (byte) ((i >>> 8) & 0xff);  
            abyte[3] = (byte) (i & 0xff);  
        }  
        return abyte;  
    }  
   
    public static int big_bytesToInt(byte[] bytes) {  
        int addr = 0;  
        if (bytes.length == 1) {  
            addr = bytes[0] & 0xFF;  
        } else if (bytes.length == 2) {  
            addr = bytes[0] & 0xFF;  
            addr = (addr << 8) | (bytes[1] & 0xff);  
        } else {  
            addr = bytes[0] & 0xFF;  
            addr = (addr << 8) | (bytes[1] & 0xff);  
            addr = (addr << 8) | (bytes[2] & 0xff);  
            addr = (addr << 8) | (bytes[3] & 0xff);  
        }  
        return addr;  
    }
}
