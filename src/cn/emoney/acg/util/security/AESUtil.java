package cn.emoney.acg.util.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import cn.emoney.acg.data.DataModule;
import u.aly.bu;

public class AESUtil {

	// KeyGenerator 提供对称密钥生成器的功能，支持各种算法
	private KeyGenerator keygen;
	// SecretKey 负责保存对称密钥
	private SecretKey deskey;
	// Cipher负责完成加密或解密工作
	private Cipher c;
	// 该字节数组负责保存加密的结果
	private byte[] cipherByte;

	public AESUtil() throws NoSuchAlgorithmException, NoSuchPaddingException {
		initEnv();
	}
	
	public static int G_SEED_NUM_1 = 26895494;
    public static int G_SEED_NUM_2 = 35063394;
	
	public static void mixByte(byte[] byteIn) throws Exception{
		int n_cerLen = byteIn.length;
		Integer n_key = G_SEED_NUM_1 + G_SEED_NUM_2;
		String s_key = n_key.toString();
		byte[] bs_key = s_key.getBytes("GBK");
		for(int i = 0; i < bs_key.length; i++) {
			System.out.print(bs_key[i]);
		}
		System.out.print("\n");
		bs_key = s_key.getBytes("UTF-8");
		for(int i = 0; i < bs_key.length; i++) {
			System.out.print(bs_key[i]);
		}
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
	
	public static void main(String... arg) throws Exception{
		System.out.print((char)0x80ff);
		System.out.print("-的byte:"+(byte)'-');
		System.out.println("new string 调用"+new String(new byte[]{48}));
		String packKey = DataModule.G_LOC_DATA_KEY_1;
		System.out.print("packKey:"+packKey+"\n");
		String s_cer = new String(packKey);
		System.out.print("s_cer:"+s_cer+"\n");
		int[] a = {};
		System.out.print(a.toString());
		byte[] bs_cer = CommonSecurityToolUtil.toBytes(s_cer);
		System.out.print("bs_cer:"+String.valueOf(bs_cer)+"\n");
		mixByte(bs_cer);
		System.out.print("混淆后的byte:");
		System.out.print(Integer.valueOf("-20",10));
		for(int i = 0; i< bs_cer.length; i++) {
			System.out.print(bs_cer[i]+"\n");
		}
		String sAESKey = new String(bs_cer);
		System.out.print("aesKey:"+sAESKey);
		try {
			SecretKey deskey = loadSecretKey(sAESKey);
			System.out.print(deskey.getAlgorithm());
			System.out.print("\n");
			System.out.print(deskey.getFormat());
			System.out.print("\n");
			byte[] encodes = deskey.getEncoded();
			for(int i = 0; i < encodes.length; i++) {
				System.out.print(encodes[i]+"\n");
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		Cipher c = Cipher.getInstance("AES");
	}

	private void initEnv() throws NoSuchAlgorithmException, NoSuchPaddingException {
		String packKey = DataModule.G_LOC_DATA_KEY_1 + DataModule.G_LOC_DATA_KEY_2 + DataModule.G_LOC_DATA_KEY_3 + DataModule.G_LOC_DATA_KEY_4 + DataModule.G_LOC_DATA_KEY_5;
		String s_cer = new String(packKey);
		byte[] bs_cer = CommonSecurityToolUtil.toBytes(s_cer);
		CommonSecurityToolUtil.mixByte(bs_cer);
		String sAESKey = new String(bs_cer);
		try {
			deskey = loadSecretKey(sAESKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 生成Cipher对象,指定其支持的DES算法
		c = Cipher.getInstance("AES");
	}

	public static String createAESKey(int keySize)
	{
		String sKey = "";
		if (keySize == 0) {
			keySize = 128;
		}
		
		try {
			KeyGenerator t_keygen = KeyGenerator.getInstance("AES");
			t_keygen.init(keySize);
			//生成密钥
			SecretKey t_deskey = t_keygen.generateKey();
			byte[] byteDesKey = t_deskey.getEncoded();
			sKey = CommonSecurityToolUtil.toHexString(byteDesKey);
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		return sKey;
	}
	
	public static String createFixAESKey(int keySize) {
		String fixAESKey = "";
		String tAESKey = createAESKey(keySize);
		if (tAESKey != null && !tAESKey.equals("")) {
			
			byte[] byteDesKey = CommonSecurityToolUtil.toBytes(tAESKey);
			//混淆
			CommonSecurityToolUtil.mixByte(byteDesKey);
			fixAESKey = CommonSecurityToolUtil.toHexString(byteDesKey);
		}
		return fixAESKey;
	}

	/**
	 * 对字符串加密
	 * 
	 * @param str
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Encrytor(String str) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
		c.init(Cipher.ENCRYPT_MODE, deskey);
		byte[] src = str.getBytes();
		// 加密，结果保存进cipherByte
		cipherByte = c.doFinal(src);
		return cipherByte;
	}

	/**
	 * 对字符串解密
	 * 
	 * @param buff
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Decryptor(byte[] buff) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，DECRYPT_MODE表示加密模式
		c.init(Cipher.DECRYPT_MODE, deskey);
		cipherByte = c.doFinal(buff);
		return cipherByte;
	}

	private static SecretKey loadSecretKey(String skey) throws IOException {
		String t_key = skey;// public key;
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] buffer = base64Decoder.decodeBuffer(t_key);
		System.out.print("base64 decoder\n");
		for(int i= 0; i < buffer.length; i++) {
			System.out.print(buffer[i]);
		}
		SecretKey t_desKey = new SecretKeySpec(buffer, "AES");
		return t_desKey;

	}

}
