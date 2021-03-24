package me.doupay.sdk.sign;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加解密工具
 *
 * @author Frank
 *
 */
public class AES {

	public final static String ALGORITHM_AEPP = "AES/ECB/PKCS5Padding";

	/**
	 * AES加密
	 *
	 * @param content
	 *            内容
	 * @param password
	 *            密钥
	 * @param algorithm
	 *            算法
	 * @return 加密后数据
	 */
	public static byte[] encrypt(byte[] content, byte[] password, String algorithm) {
		if (content == null || password == null) {
			return null;
		}
		try {
			Cipher cipher = null;
			if (algorithm.endsWith("PKCS7Padding")) {
				cipher = Cipher.getInstance(algorithm, "BC");
			} else {
				cipher = Cipher.getInstance(algorithm);
			}
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(password, "AES"));
			return cipher.doFinal(content);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * AES解密
	 *
	 * @param content
	 *            加密内容
	 * @param password
	 *            密钥
	 * @param algorithm
	 *            算法
	 * @return 解密后数据
	 */
	public static byte[] decrypt(byte[] content, byte[] password, String algorithm) {
		if (content == null || password == null) {
			return null;
		}
		try {
			Cipher cipher = null;
			if (algorithm.endsWith("PKCS7Padding")) {
				cipher = Cipher.getInstance(algorithm, "BC");
			} else {
				cipher = Cipher.getInstance(algorithm);
			}
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password, "AES"));
			byte[] bytes = cipher.doFinal(content);
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * AES加密
	 *
	 * @param content
	 *            内容
	 * @param password
	 *            密钥
	 * @param algorithm
	 *            算法
	 * @param ivStr
	 *            向量
	 * @return 加密后数据
	 */
	public static byte[] encrypt(byte[] content, byte[] password, byte[] ivStr, String algorithm) {
		if (content == null || password == null) {
			return null;
		}
		try {
			Cipher cipher = null;
			if (algorithm.endsWith("PKCS7Padding")) {
				cipher = Cipher.getInstance(algorithm, "BC");
			} else {
				cipher = Cipher.getInstance(algorithm);
			}
			IvParameterSpec iv = new IvParameterSpec(ivStr);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(password, "AES"), iv);
			return cipher.doFinal(content);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * AES解密
	 *
	 * @param content
	 *            加密内容
	 * @param password
	 *            密钥
	 * @param algorithm
	 *            算法
	 * @param ivStr
	 *            向量
	 * @return 解密后数据
	 */
	public static byte[] decrypt(byte[] content, byte[] password, byte[] ivStr, String algorithm) {
		if (content == null || password == null) {
			return null;
		}
		try {
			Cipher cipher = null;
			if (algorithm.endsWith("PKCS7Padding")) {
				cipher = Cipher.getInstance(algorithm, "BC");
			} else {
				cipher = Cipher.getInstance(algorithm);
			}
			IvParameterSpec iv = new IvParameterSpec(ivStr);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password, "AES"), iv);
			byte[] bytes = cipher.doFinal(content);
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	// 加密
	public static String Encrypt(String sSrc, String sKey)
	{
		if (sKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
        /*if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }*/
		byte[] encrypted = null;
		try {
			byte[] raw = sKey.getBytes("utf-8");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
		}catch(Exception e){
			System.out.println("AES 加密失败 原因为:"+e.getMessage());
		}
		return Base64Utils.encode(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
	}

	// 解密
	public static String Decrypt(String sSrc, String sKey) throws Exception {
		try {
			// 判断Key是否正确
			if (sKey == null) {
				System.out.print("Key为空null");
				return null;
			}
			// 判断Key是否为16位
            /*if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }*/
			byte[] raw = sKey.getBytes("utf-8");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] encrypted1 = Base64Utils.decode(sSrc);//先用base64解密
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original,"utf-8");
				return originalString;
			} catch (Exception e) {
				System.out.println(e.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}


	public static void main(String[] args) {
		String timeStamp = "1610697341483";
		String appId="502808ee5427490abb40375022e28578";
		String secret="c67100f61bfc684a8a288190026b53fb";
		String advancedPermissionsSign = "5MHD7GsZNQa4tDZ2W9zo3vKZCywC64+OAIMiWWu3mmbio4W1UBf3vNrX2ZwQDZeB";

		advancedPermissionsSign=AES.Encrypt(appId+timeStamp,secret);
		System.out.println(advancedPermissionsSign);
	}

}
