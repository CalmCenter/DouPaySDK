package me.doupay.sdk.net;

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


/**
 * <pre>
 *     描述   : okhttp配置
 * </pre>
 */
public class OkhttpHelper {
    private static final int CONNECT_TIME = 10;
    private static final int READ_TIME = 20;
    private static final int WRITE_TIME = 20;
    private static OkHttpClient.Builder builder;
    private static OkHttpClient client;

    private static OkHttpClient.Builder getDefaultBuilder(boolean addSSL) {
        builder = new OkHttpClient.Builder();
        if (addSSL) {

            SSLSocketFactory sslSocketFactory = null;
            X509TrustManager trustManager = null;
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(Constants.getAppContext().getResources().openRawResource(R.raw.fullchain));
//                keyStore.setCertificateEntry("fullchain", certificate);

                trustManagerFactory.init(keyStore);

                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                trustManager = (X509TrustManager) trustManagers[0];

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, null);
                sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, trustManager);
            } catch (Exception e) {
                System.out.println("SSL设置错误");
            }
        }

        // 下面3个超时,不设置默认就是10s
        builder.hostnameVerifier(new TrustAllHostnameVerifier())
//                .dns(new HttpDns())
                .connectTimeout(CONNECT_TIME, TimeUnit.SECONDS)
                .readTimeout(READ_TIME, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME, TimeUnit.SECONDS)
                //失败重试
                .retryOnConnectionFailure(true);

        if (!addSSL) {
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }}, new SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
            HostnameVerifier hv1 = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            String workerClassName = "okhttp3.OkHttpClient";
            try {
                Class workerClass = Class.forName(workerClassName);
                Field hostnameVerifier = workerClass.getDeclaredField("hostnameVerifier");
                hostnameVerifier.setAccessible(true);
                hostnameVerifier.set(builder.build(), hv1);
                Field sslSocketFactory = workerClass.getDeclaredField("sslSocketFactoryOrNull");
                sslSocketFactory.setAccessible(true);
                sslSocketFactory.set(builder.build(), sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    public static OkHttpClient getOkhttpClient(boolean isCache, Interceptor... interceptors) {
        if (builder == null) {
            builder = getDefaultBuilder(isCache);
        }
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

    public static OkHttpClient getDefaultClient(boolean addSSL, Interceptor interceptor) {
        builder = getDefaultBuilder(addSSL);
        builder.addInterceptor(interceptor);
        client = builder.build();
        return client;
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


}
