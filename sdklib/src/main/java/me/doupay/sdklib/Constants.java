package me.doupay.sdklib;

import me.doupay.sdklib.net.Language;

public class Constants {
    private static Constants constants;
    public static boolean openSysLog = false;
    public static final String basrUrl = "http://192.168.10.115:9000/";
    public static Language language = Language.en_US;

    public static Constants getInstance() {
        if (constants == null) {
            constants = new Constants();
        }
        return constants;
    }

    public void init(String secret, String privateKey) {
        Constants.secret = secret;
        Constants.privateKey = privateKey;
    }

    private static String secret = "";
    private static String privateKey = "";

    public static String getSecret() {
        return secret;
    }

    public static String getPrivateKey() {
        return privateKey;
    }



}
