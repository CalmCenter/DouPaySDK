package me.doupay.sdk;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import me.doupay.sdk.sign.RSAUtils;


public class RSATest {


    @Test
    public  void sign() throws JSONException {
        // TODO Auto-generated method stub

        System.out.println("===开始RSA公、私钥测试===");
        String stc = "appId=502808ee5427490abb40375022e28578,key=3";

        JSONObject jsonObject = new JSONObject("{\"appId\":\"502808ee5427490abb40375022e28578\",\"key\":\"3\"}");
        String message = jsonObject.toString(3);
        System.out.println(message);

        String gj="appId=502808ee5427490abb40375022e28578,key=3,secertSign=5MHD7GsZNQa4tDZ2W9zo3vKZCywC64+OAIMiWWu3mmbio4W1UBf3vNrX2ZwQDZeB,timeStamp=1610697341483";
        String privateKey="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDIZiI+x2Ic2ouxR6seBgj/kuTfNMxcGbdexssw6e0F7n+/BhArV/71xkQMHW31iDM51rbWuyyzZxv9tsfqTjXCO3kzn1/Y4e6iSF+x7RK49J6OlozSVEBA/sBTvi27UmbGd8RPCfPln5vaGMRpJuctgFT+gAybLCYcBrxiUjMRMnrtEQuMSFPsuhcec5t+C4ZSG1HQ0tJPAHFeus9qiNqLMIshLwvyiyiJybjYFSjTKjNDyrX8J4nm1pYtQ1O1uU4ZroBGZKBzIDoZHO93XmVrS6m/MKHpB7Wgzfq3sRPzgVcEaOFptt9uuU2q2FfuqxKvjF7CFR08rpwzY6HrizebAgMBAAECggEAdPLVrVliMoB/4Vd3zy+qdKvfETuYv27qik7tKYB6qGmE94+sQ/84dWndMEzEbPOtIWXikfHtpkzYEvpmNKCr0swucSfjIYjIYMBnyEgYEwP+vCuzxtMQJF4HE1f7DESMYepnD9E5GppIs8CcbtkbNHyeHV/Q+4WKP/TWX3KlBrUrO0YAIQHhkHURblg1g2UvUmP1fGo6AL5v101KrhpXzG99LfDN2qc10EuFq66d6LLnFWJvzW4DC0zCk336z0jyJgAV9y6gCRPKQmgrTJECYGYKY2svDsAtzW7lgjO+33dBxpt0bjqZsc/m7JBJ448Sor8E6cyNGtf2wCS3bin24QKBgQD6oWsyrtFW2/d4Hh8UwAkdQx5oY2swVWX0MGLKxjzqeeeb1WOa/YcbZfj6D+olewNDzWI1zdUcT5lpBAqyWdZkkRV5CsPPlaAAOQyMAXhBCKkIhBINH1j6auKdNKJrNIaApBm4hRRbX7w7EDnuSJJA6EmA/0qesDVAt3htkqLZ6QKBgQDMsThmGtSxoJl7yLI1rE6eLlvJVg9NZxfNDLwc7g1hgQEdr5hUuj1bqHjK8VFbMc5lJVpelJYwpO9pezMZdXUMexNHXJVEo2AnvAljmlYeRXm9lpiIpW+DmoE6B2tzZ22Hcn5y+p9v9j0ZDawQfgXt1rm7XTotGywrpsmeimdO4wKBgQCADiukaLfjBl7Jy7IbuwatIdcOhQWGW4vNGU/QxTronsKc14md7j2y3QY6VhlPbyu639x3GyTw4ybCBmOkvswQ9CQhhUOI860dkAh+HF4h9FfRVxGWDNc8k5IXuoXl+p9iaPYPVkeRbDfTgbXnrsKzUTwFIesxa1y6JUPt0EdOyQKBgGhkue7ZIEC3N4/5+2mER1RFMGquiX9gZLMfG5Fll01zDa6mL3qGwWRNt81I5cUs0aakNkKmZTLJ65BQVO9XCCslWd+7SCWJbTDWpbM2s1Uc+cnHVGPce9MSqXV+8z4YMbQyoGrjhw0C+IYegvKmUz/Jk1ALa/A1O4HHvmwtCiMhAoGAct3TFXFbn6zruHSstncJGtS1c3EzcrBCb6NhAG5GfqiKXqXY0wl/0plPffovH5okcMZiRLX2DPRLdkuDZSVYFDpJgED5C4M+62YiW33Sel5aDCIXME5rkmvAbYrqR7t7r4rj5JjPvvNiBPsPfHsvIEHqW2Y1eY87clsu++BkU/4=";
        String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyGYiPsdiHNqLsUerHgYI/5Lk3zTMXBm3XsbLMOntBe5/vwYQK1f+9cZEDB1t9YgzOda21rsss2cb/bbH6k41wjt5M59f2OHuokhfse0SuPSejpaM0lRAQP7AU74tu1JmxnfETwnz5Z+b2hjEaSbnLYBU/oAMmywmHAa8YlIzETJ67RELjEhT7LoXHnObfguGUhtR0NLSTwBxXrrPaojaizCLIS8L8osoicm42BUo0yozQ8q1/CeJ5taWLUNTtblOGa6ARmSgcyA6GRzvd15la0upvzCh6Qe1oM36t7ET84FXBGjhabbfbrlNqthX7qsSr4xewhUdPK6cM2Oh64s3mwIDAQAB";

        String sign = RSAUtils.sign(privateKey, gj);
        System.out.println(sign);

        RSAUtils.verifySign(publicKey, gj, sign);



    }
}
