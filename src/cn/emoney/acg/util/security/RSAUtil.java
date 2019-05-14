package cn.emoney.acg.util.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

public class RSAUtil {

	RSAPublicKey cer_public = null;
	RSAPrivateKey cer_private = null;

	private static RSAUtil mInstance = null;

	public static RSAUtil getInstance() {
		if (mInstance == null) {
			mInstance = new RSAUtil();
		}
		return mInstance;
	}

	public RSAUtil() {
		initEnv();
	}

	private void initEnv() {
		String pukey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlbQyJmJGeUF30GNJNVFoKTE40" + "pC7RF3CGIhGZXkbvMIfuPqeguXeOEmbxHgwmk04QMYkNOBAqEqdvAFqtd4qw6LGO" + "10bxYw6Rg3+AIfhAH6zBvBxpZavNo7PFMS6jSa4k11CE2h5s9V6Rqq9IR6N4C8v7" + "y/Pwvm3p5H65ntQ+tQIDAQAB";
		String s_cer = new String(pukey);

		byte[] bs_cer = CommonSecurityToolUtil.toBytes(s_cer);
		CommonSecurityToolUtil.mixByte(bs_cer);

		String sPublicKey = new String(bs_cer);
		
		try {
			cer_public = (RSAPublicKey) loadPublicKey(sPublicKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] encryptText(String sEncryptText) {
		// 重要:
		// 如果加密文本不满128位,要使用128位byte[],不足补0,否则跨平台不能互通
		byte[] en_content = new byte[128];
		System.arraycopy(sEncryptText.getBytes(), 0, en_content, 0, sEncryptText.getBytes().length);
		byte[] ret = encrypt(cer_public, en_content);

		return ret;
	}

	public byte[] decryptText(byte[] decryptBytes) {
		byte[] ret = new byte[0];
		// ret = decrypt(cer_private, decryptBytes);
		return ret;
	}

	
	
	public KeyPair createRSAKey(int keySize) throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(keySize, new SecureRandom());

		KeyPair keyPair = keyPairGen.generateKeyPair();
		return keyPair;
	}

	public String saveKeyHex(PublicKey key) {
		// save public key
		String tString = CommonSecurityToolUtil.toHexString(key.getEncoded());
		return tString;
	}
	
	public String saveKeyHex(PrivateKey key) {
		String tString = CommonSecurityToolUtil.toHexString(key.getEncoded());
		return tString;
	}
	
	public String saveKeyB64(PublicKey key) {
		BASE64Encoder base64Encoder = new BASE64Encoder();
		String tString = base64Encoder.encode(key.getEncoded());
		return tString;
	}

	public String saveKeyB64(PrivateKey key) {
		BASE64Encoder base64Encoder = new BASE64Encoder();
		String tString = base64Encoder.encode(key.getEncoded());
		return tString;
	}

	private PublicKey loadPublicKey(String sPublicKey) throws Exception {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			// publicKey
			String publicKeyStr = sPublicKey;// public key;

			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);

			X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(buffer);

			PublicKey publicKey = keyFactory.generatePublic(bobPubKeySpec);

			return publicKey;

		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}

	}

	private PrivateKey loadPrivateKey(String sPrivateKey) throws Exception {
		PrivateKey privateKey;

		try {
			// privateKey
			String privateKeyStr = sPrivateKey;// private key
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			BASE64Decoder base64Decoder = new BASE64Decoder();

			byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);

			privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

		} catch (NoSuchAlgorithmException e) {

			throw new Exception("无此算法");

		} catch (InvalidKeySpecException e) {

			throw new Exception("私钥非法");

		} catch (IOException e) {

			throw new Exception("私钥数据内容读取错误");

		} catch (NullPointerException e) {

			throw new Exception("私钥数据为空");

		}
		return privateKey;
	}

	private byte[] encrypt(RSAPublicKey publicKey, byte[] data) {
		if (publicKey != null) {
			try {
				Cipher cipher = Cipher.getInstance("RSA/ECB/NOPADDING");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				return cipher.doFinal(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private byte[] decrypt(RSAPrivateKey privateKey, byte[] raw) {
		if (privateKey != null) {
			try {
				Cipher cipher = Cipher.getInstance("RSA/ECB/NOPADDING");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				return cipher.doFinal(raw);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}