package me.doupay.sdk.net.interceptors;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import me.doupay.sdk.Constants;
import me.doupay.sdk.net.exception.ApiException;
import me.doupay.sdk.sign.AES;
import me.doupay.sdk.sign.RSAUtils;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.platform.Platform;

public class LoggingInterceptor implements Interceptor {
    private static final int JSON_INDENT = 3;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String OOM_OMITTED = LINE_SEPARATOR + "Output omitted because of Object size.";
    private final boolean isDebug;
    private final Builder builder;
    private JsonParser parser = new JsonParser();

    private LoggingInterceptor(Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    private static Runnable createPrintJsonRequestRunnable(final Builder builder, final Request request) {
        return () -> Printer.printJsonRequest(builder, request);
    }

    private static Runnable createFileRequestRunnable(final Builder builder, final Request request) {
        return () -> Printer.printFileRequest(builder, request);
    }

    private static Runnable createPrintJsonResponseRunnable(final Builder builder, final long chainMs, final boolean isSuccessful,
                                                            final int code, final String headers, final String bodyString, final List<String> segments, final String message, final String responseUrl) {
        return () -> Printer.printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString, segments, message, responseUrl);
    }

    private static Runnable createFileResponseRunnable(final Builder builder, final long chainMs, final boolean isSuccessful,
                                                       final int code, final String headers, final List<String> segments, final String message) {
        return () -> Printer.printFileResponse(builder, chainMs, isSuccessful, code, headers, segments, message);
    }

    private static String getJsonString(final String msg) {
        String message;
        try {
            String block = "{";
            String brackets = "[";
            if (msg.startsWith(block)) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith(brackets)) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        } catch (OutOfMemoryError e1) {
            message = OOM_OMITTED;
        }
        return message;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String appId = "";
        String timestamp = "";
        // 重新组装 body
        TreeMap<String, Object> params = null;
//        FormBody.Builder newFormBody = null;
        if (request.body() instanceof FormBody) {
            params = new TreeMap<>();
            FormBody oidFormbody = (FormBody) request.body();
            for (int i = 0; i < oidFormbody.size(); i++) {
                switch (oidFormbody.encodedName(i)) {
                    case "appId":
                        appId = oidFormbody.encodedValue(i);
                        params.put(oidFormbody.name(i), oidFormbody.value(i));
                        break;
                    case "timeStamp":
                        timestamp = oidFormbody.encodedValue(i);
                        params.put(oidFormbody.name(i), oidFormbody.value(i));
                        break;
                    default:
                        if (oidFormbody.value(i).contains("[")) {
                            String value = oidFormbody.value(i);
                            String valueSub = value.substring(1, value.length() - 1);
                            String[] valueArray = valueSub.split(",");
                            List<String> demoList = new ArrayList<>();
                            for (String s : valueArray) {
                                demoList.add(s.trim());
                            }
                            params.put(oidFormbody.name(i), demoList);
                        } else {
                            params.put(oidFormbody.name(i), oidFormbody.value(i));
                        }
                        break;
                }
            }
        }

        // 判断高权限
        if (!appId.equals("") && !timestamp.equals("")) {
            String secertSign = AES.Encrypt(appId + timestamp, Constants.getSecret());
            params.put("secertSign", secertSign.replace("+", "%2B"));
        }

        // 组织加密数据
        StringBuilder UrlSign = new StringBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key) instanceof String) {
                    UrlSign.append(key).append("=").append(params.get(key)).append(",");
                }
            }
        }
        String sign = UrlSign.substring(0, UrlSign.toString().length() - 1);
        sign = URLDecoder.decode(sign, "utf-8");
        // body
        String bodyStr = new Gson().toJson(params);//加密前
        bodyStr = URLDecoder.decode(bodyStr, "utf-8");
        request = request.newBuilder().post(RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), bodyStr)).build();

        // 添加头部签名
        Request.Builder requestBuilder = request.newBuilder();
        HashMap<String, String> headerMap = builder.getHeaders();
        for (String key : headerMap.keySet()) {
            requestBuilder.addHeader(key, headerMap.get(key));
        }
        requestBuilder.addHeader("X-Merchant-sign", RSAUtils.sign(Constants.getPrivateKey(), sign));
        request = requestBuilder.build();

        final RequestBody requestBody = request.body();

        String rSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            rSubtype = requestBody.contentType().subtype();
        }

        Executor executor = this.builder.executor;

        if (isNotFileRequest(rSubtype)) {
            if (executor != null) {
                executor.execute(createPrintJsonRequestRunnable(this.builder, request));
            } else {
                //打印request
//                if (LogUtils.getConfig().isLogSwitch()) {
                Printer.printJsonRequest(this.builder, request);
//                }
            }
        } else {
            if (executor != null) {
                executor.execute(createFileRequestRunnable(this.builder, request));
            } else {
                Printer.printFileRequest(this.builder, request);
            }
        }


        final long st = System.nanoTime();
        Response response = null;
        if (this.builder.isMockEnabled) {
            try {
                TimeUnit.MILLISECONDS.sleep(this.builder.sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = new Response.Builder()
                    .body(ResponseBody.create(MediaType.parse("application/json"), this.builder.listener.getJsonResponse(request)))
                    .request(chain.request())
                    .protocol(Protocol.HTTP_2)
                    .message("Mock")
                    .code(200)
                    .build();

        } else {
            //TODO 发送请求
            response = chain.proceed(request);

        }
        final long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);

        final List<String> segmentList = request.url().encodedPathSegments();
        final String header = response.headers().toString();
        final int code = response.code();
        final boolean isSuccessful = response.isSuccessful();
        final String message = response.message();
        final ResponseBody responseBody = response.body();
        final MediaType contentType = responseBody.contentType();

        if (code != 200) {
            //抛出异常,让rxjava捕获,便于统一处理
            throw new ApiException.ServerException(code, message, request.url().toString());
        }
        String subtype = null;
        final ResponseBody body;

        if (contentType != null) {
            subtype = contentType.subtype();
        }
        String bodyString = null;

        bodyString = Printer.getJsonString(responseBody.string());

        final String url = response.request().url().toString();
        if (executor != null) {
            executor.execute(createPrintJsonResponseRunnable(this.builder, chainMs, isSuccessful, code, header, bodyString,
                    segmentList, message, url));
        } else {
//                if (BaseConstants.LogSwitch) {
            Printer.printJsonResponse(this.builder, chainMs, isSuccessful, code, header, bodyString,
                    segmentList, message, url);
//                }
        }
        body = ResponseBody.create(contentType, bodyString);


        return response.newBuilder().
                body(body).
                build();
    }


    private boolean isNotFileRequest(final String subtype) {
        return subtype != null && (subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html"));
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    public static class Builder {

        public static String TAG = "LoggingI";
        public final HashMap<String, String> headers;
        public final HashMap<String, String> queries;
        public boolean isLogHackEnable = false;
        public boolean isDebug;
        public int type = Platform.INFO;
        public String requestTag;
        public String responseTag;
        public Level level = Level.BASIC;
        public Logger logger;
        public Executor executor;
        public boolean isMockEnabled;
        public long sleepMs;
        public BufferListener listener;

        public Builder() {
            headers = new HashMap<>();
            queries = new HashMap<>();
        }

        int getType() {
            return type;
        }

        Level getLevel() {
            return level;
        }

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        HashMap<String, String> getHeaders() {
            return headers;
        }

        HashMap<String, String> getHttpUrl() {
            return queries;
        }

        String getTag(boolean isRequest) {
            if (isRequest) {
                return TextUtils.isEmpty(requestTag) ? TAG : requestTag;
            } else {
                return TextUtils.isEmpty(responseTag) ? TAG : responseTag;
            }
        }

        Logger getLogger() {
            return logger;
        }

        Executor getExecutor() {
            return executor;
        }

        boolean isLogHackEnable() {
            return isLogHackEnable;
        }

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * Add a field with the specified value
         */
        public Builder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * Add a field with the specified value
         */
        public Builder addQueryParam(String name, String value) {
            queries.put(name, value);
            return this;
        }

        /**
         * Set request and response each log tag
         *
         * @param tag general log tag
         * @return Builder
         */
        public Builder tag(String tag) {
            TAG = tag;
            return this;
        }

        /**
         * Set request log tag
         *
         * @param tag request log tag
         * @return Builder
         */
        public Builder request(String tag) {
            this.requestTag = tag;
            return this;
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        public Builder response(String tag) {
            this.responseTag = tag;
            return this;
        }

        /**
         * @param isDebug set can sending log output
         * @return Builder
         */
        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        /**
         * @param type set sending log output type
         * @return Builder
         * @see Platform
         */
        public Builder log(int type) {
            this.type = type;
            return this;
        }

        /**
         * @param logger manuel logging interface
         * @return Builder
         * @see Logger
         */
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * @param executor manual executor for printing
         * @return Builder
         * @see Logger
         */
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * @param useMock let you use json file from asset
         * @param sleep   let you see progress dialog when you request
         * @return Builder
         * @see LoggingInterceptor
         */
        public Builder enableMock(boolean useMock, long sleep, BufferListener listener) {
            this.isMockEnabled = useMock;
            this.sleepMs = sleep;
            this.listener = listener;
            return this;
        }

        /**
         * Call this if you want to have formatted pretty output in Android Studio logCat.
         * By default this 'hack' is not applied.
         *
         * @param useHack setup builder to use hack for Android Studio v3+ in order to have nice
         *                output as it was in previous A.S. versions.
         * @return Builder
         * @see Logger
         */
        public Builder enableAndroidStudio_v3_LogsHack(final boolean useHack) {
            isLogHackEnable = useHack;
            return this;
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }
}
