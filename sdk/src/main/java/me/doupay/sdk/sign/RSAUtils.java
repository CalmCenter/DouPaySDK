package me.doupay.sdk.sign;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {

    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;//设置长度
    public static final String PUBLIC_KEY_NAME = "publicKey";
    public static final String PRIVATE_KEY_NAME = "privateKey";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String ENCODE_ALGORITHM = "SHA-256";

    /**
     * RSA加密
     */
    public static String encryptData(String data, String publicKeyString) {
        try {
            PublicKey publicKey = getPublicKey(publicKeyString);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataToEncrypt = data.getBytes("utf-8");
            byte[] encryptedData = cipher.doFinal(dataToEncrypt);
            String encryptString = Base64Utils.encode(encryptedData);
            return encryptString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * RSA解密
     */
    public static String decryptData(String data, String privateKeyString) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyString);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] descryptData = Base64Utils.decode(data);
            byte[] descryptedData = cipher.doFinal(descryptData);
            return new String(descryptedData, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解码PublicKey
     */
    public static PublicKey getPublicKey(String key) {
        try {
            byte[] byteKey = Base64Utils.decode(key);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byteKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解码PrivateKey
     */
    public static PrivateKey getPrivateKey(String key) {
        try {
            byte[] byteKey = Base64Utils.decode(key);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 签名
     * @param key         私钥
     * @param requestData 请求参数
     * @return
     */
    public static String sign(String key, String requestData) {
        String signature = null;
        byte[] signed = null;
        try {
            PrivateKey privateKey = getPrivateKey(key);

            Signature Sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            Sign.initSign(privateKey);
            Sign.update(requestData.getBytes());
            signed = Sign.sign();

            signature = Base64Utils.encode(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

    /**
     * 验签
     *
     * @param key         公钥
     * @param requestData 请求参数
     * @param signature   签名
     * @returnm
     */
    public static boolean verifySign(String key, String requestData, String signature) {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("publicKey:"+key);
        System.out.println("str:"+requestData);
        System.out.println("sign:"+signature);
        System.out.println("--------------------------------------------------------------------------");


       boolean verifySignSuccess = false;
        try {
            PublicKey publicKey = getPublicKey(key);

            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify(publicKey);
            verifySign.update(requestData.getBytes());

//            verifySignSuccess = verifySign.verify(Base64.getDecoder().decode(signature));
            verifySignSuccess = verifySign.verify(Base64Utils.decode(signature));
            System.out.println("===验签结果：" + verifySignSuccess);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return verifySignSuccess;
    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub

        System.out.println("===开始RSA公、私钥测试===");
        String stc = "appId=502808ee5427490abb40375022e28578,key=3";

        String gj="appId=502808ee5427490abb40375022e28578,key=3,secertSign=5MHD7GsZNQa4tDZ2W9zo3vKZCywC64+OAIMiWWu3mmbio4W1UBf3vNrX2ZwQDZeB,timeStamp=1610697341483";
        String privateKey="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDIZiI+x2Ic2ouxR6seBgj/kuTfNMxcGbdexssw6e0F7n+/BhArV/71xkQMHW31iDM51rbWuyyzZxv9tsfqTjXCO3kzn1/Y4e6iSF+x7RK49J6OlozSVEBA/sBTvi27UmbGd8RPCfPln5vaGMRpJuctgFT+gAybLCYcBrxiUjMRMnrtEQuMSFPsuhcec5t+C4ZSG1HQ0tJPAHFeus9qiNqLMIshLwvyiyiJybjYFSjTKjNDyrX8J4nm1pYtQ1O1uU4ZroBGZKBzIDoZHO93XmVrS6m/MKHpB7Wgzfq3sRPzgVcEaOFptt9uuU2q2FfuqxKvjF7CFR08rpwzY6HrizebAgMBAAECggEAdPLVrVliMoB/4Vd3zy+qdKvfETuYv27qik7tKYB6qGmE94+sQ/84dWndMEzEbPOtIWXikfHtpkzYEvpmNKCr0swucSfjIYjIYMBnyEgYEwP+vCuzxtMQJF4HE1f7DESMYepnD9E5GppIs8CcbtkbNHyeHV/Q+4WKP/TWX3KlBrUrO0YAIQHhkHURblg1g2UvUmP1fGo6AL5v101KrhpXzG99LfDN2qc10EuFq66d6LLnFWJvzW4DC0zCk336z0jyJgAV9y6gCRPKQmgrTJECYGYKY2svDsAtzW7lgjO+33dBxpt0bjqZsc/m7JBJ448Sor8E6cyNGtf2wCS3bin24QKBgQD6oWsyrtFW2/d4Hh8UwAkdQx5oY2swVWX0MGLKxjzqeeeb1WOa/YcbZfj6D+olewNDzWI1zdUcT5lpBAqyWdZkkRV5CsPPlaAAOQyMAXhBCKkIhBINH1j6auKdNKJrNIaApBm4hRRbX7w7EDnuSJJA6EmA/0qesDVAt3htkqLZ6QKBgQDMsThmGtSxoJl7yLI1rE6eLlvJVg9NZxfNDLwc7g1hgQEdr5hUuj1bqHjK8VFbMc5lJVpelJYwpO9pezMZdXUMexNHXJVEo2AnvAljmlYeRXm9lpiIpW+DmoE6B2tzZ22Hcn5y+p9v9j0ZDawQfgXt1rm7XTotGywrpsmeimdO4wKBgQCADiukaLfjBl7Jy7IbuwatIdcOhQWGW4vNGU/QxTronsKc14md7j2y3QY6VhlPbyu639x3GyTw4ybCBmOkvswQ9CQhhUOI860dkAh+HF4h9FfRVxGWDNc8k5IXuoXl+p9iaPYPVkeRbDfTgbXnrsKzUTwFIesxa1y6JUPt0EdOyQKBgGhkue7ZIEC3N4/5+2mER1RFMGquiX9gZLMfG5Fll01zDa6mL3qGwWRNt81I5cUs0aakNkKmZTLJ65BQVO9XCCslWd+7SCWJbTDWpbM2s1Uc+cnHVGPce9MSqXV+8z4YMbQyoGrjhw0C+IYegvKmUz/Jk1ALa/A1O4HHvmwtCiMhAoGAct3TFXFbn6zruHSstncJGtS1c3EzcrBCb6NhAG5GfqiKXqXY0wl/0plPffovH5okcMZiRLX2DPRLdkuDZSVYFDpJgED5C4M+62YiW33Sel5aDCIXME5rkmvAbYrqR7t7r4rj5JjPvvNiBPsPfHsvIEHqW2Y1eY87clsu++BkU/4=";
        String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyGYiPsdiHNqLsUerHgYI/5Lk3zTMXBm3XsbLMOntBe5/vwYQK1f+9cZEDB1t9YgzOda21rsss2cb/bbH6k41wjt5M59f2OHuokhfse0SuPSejpaM0lRAQP7AU74tu1JmxnfETwnz5Z+b2hjEaSbnLYBU/oAMmywmHAa8YlIzETJ67RELjEhT7LoXHnObfguGUhtR0NLSTwBxXrrPaojaizCLIS8L8osoicm42BUo0yozQ8q1/CeJ5taWLUNTtblOGa6ARmSgcyA6GRzvd15la0upvzCh6Qe1oM36t7ET84FXBGjhabbfbrlNqthX7qsSr4xewhUdPK6cM2Oh64s3mwIDAQAB";

        String sign = sign(privateKey, gj);
        System.out.println(sign);

        verifySign(publicKey, gj, sign);



    }


}
