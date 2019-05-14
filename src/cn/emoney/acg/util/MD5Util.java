package cn.emoney.acg.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public class MD5Util {

    /**
     * 获取32位 md5 小写字符串
     * 
     * @param string
     * @return
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] inputByteArray = string.getBytes("UTF-8");
            messageDigest.update(inputByteArray);
            hash = messageDigest.digest();
            return bytes2HexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 获取16位 md5 小写字符串
     * 
     * @param string
     * @return
     */
    public static String md5_16(String string) {
        String sMd5 = md5(string);
        if (sMd5 != null && sMd5.length() == 32) {
            return sMd5.substring(8, 24);
        }
        return "";
    }

    public static String md5(byte[] bytes) {
        byte[] hash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            hash = messageDigest.digest();
            return bytes2HexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getMd5ByFile(FileInputStream fis, long fileLen) {
        byte[] hash;
        try {
            MappedByteBuffer byteBuffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileLen);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(byteBuffer);
            hash = messageDigest.digest();
            return bytes2HexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 
     * @param hash 16个字节
     * @return 32位小写字符串
     */
    private static String bytes2HexString(byte[] hash) {
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        String ret = hex.toString();
        LogUtil.easylog(ret);
        return ret;
    }
}
