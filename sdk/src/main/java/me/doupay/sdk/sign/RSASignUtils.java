package me.doupay.sdk.sign;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RSASignUtils {


    /**
     * 验证商家端签名需要跳过的字段列表
     */
    public static final List<String> signIgnoreFiledList = Arrays.asList("userId", "merchantSign", "permissionCode", "requestIp");


    /**
     * 检查请求 rsa 签名
     *
     * @param appPublicKey
     * @param jsonStr
     * @param rawSign
     */
    public static boolean checkSign(String appPublicKey, String jsonStr, String rawSign) {
        // 验证 RSA 签名
        boolean verifySignSuccess = verifyRSASign(appPublicKey, jsonStr, rawSign);
        if (!verifySignSuccess) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 验证 RSA 签名
     *
     * @param publicKey
     * @param jsonStr
     * @param rawSign
     * @return
     */
    public static boolean verifyRSASign(String publicKey, String jsonStr, String rawSign) {
        String clearTextSign = generateClearTextSign(jsonStr);
        // 验证签名
        return RSAUtils.verifySign(publicKey, clearTextSign, rawSign);
    }


    /**
     * 根据json串生成明文签名
     *
     * @param jsonStr
     * @return
     */
    public static String generateClearTextSign(String jsonStr) {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        JsonParser p = new JsonParser();
        JsonElement e = p.parse(jsonStr);
        sort(e);
        String s = g.toJson(e).replaceAll("\n", "").replaceAll(" ", "");
        return s;
    }


    /**
     * 排序
     *
     * @param e
     */
    public static void sort(JsonElement e) {
        if (e.isJsonNull() || e.isJsonPrimitive()) {
            return;
        }

        if (e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            Iterator<JsonElement> it = a.iterator();
            it.forEachRemaining(i -> sort(i));
            return;
        }

        if (e.isJsonObject()) {
            Map<String, JsonElement> tm = new TreeMap<>(getComparator());
            for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
                tm.put(en.getKey(), en.getValue());
            }

            String key;
            JsonElement val;
            for (Map.Entry<String, JsonElement> en : tm.entrySet()) {
                key = en.getKey();
                val = en.getValue();
                e.getAsJsonObject().remove(key);
                e.getAsJsonObject().add(key, val);
                sort(val);
            }
        }
    }

    /**
     * 定义比较规则
     *
     * @return
     */
    private static Comparator<String> getComparator() {
        return (s1, s2) -> s1.compareTo(s2);
    }
}
