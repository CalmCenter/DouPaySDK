package me.doupay.sdk.net.exception;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;



import java.io.NotSerializableException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

import javax.net.ssl.SSLHandshakeException;

import retrofit2.HttpException;

/**
 * <pre>
 * </pre>
 */
public class ApiException extends Exception {
    private int errorCode;
    private String msg;
    private String url;

    private ApiException(Throwable throwable, int errorCode) {
        super(throwable);
        this.errorCode = errorCode;
        this.msg = throwable.getMessage();
    }

    public static ApiException handlerException(Throwable throwable) {
        ApiException exception = null;
        String errorUrl = "";

        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            exception = new ApiException(httpException, httpException.code());
            if (exception.errorCode >= 500) {
                exception.setMsg(ExceString.Companion.getString(ExceString.ServerError));
            } else if (exception.errorCode >= 400) {
                exception.setMsg(ExceString.Companion.getString(ExceString.RequestError));
            }

        } else if (throwable instanceof SocketTimeoutException || throwable instanceof ConnectException ||
                throwable instanceof UnknownHostException) {
            exception = new ApiException(throwable, ERROR.TIMEOUT_ERROR);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerTimeOut));
        } else if (throwable instanceof NullPointerException) {
            exception = new ApiException(throwable, ERROR.NULL_POINTER_EXCEPTION);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerNullPointer));
        } else if (throwable instanceof SSLHandshakeException) {
            exception = new ApiException(throwable, ERROR.SSL_ERROR);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerSslError));
        } else if (throwable instanceof ClassCastException) {
            exception = new ApiException(throwable, ERROR.CAST_ERROR);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerCastError));
        } else if (throwable instanceof IllegalStateException) {
            exception = new ApiException(throwable, ERROR.ILLEGAL_STATE_ERROR);
            exception.setMsg(throwable.getMessage());
        } else if (throwable instanceof JsonParseException || throwable instanceof JsonSerializer || throwable instanceof NotSerializableException || throwable instanceof ParseException) {
            exception = new ApiException(throwable, ERROR.PARSE_ERROR);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerParseError) + throwable.getMessage());
        } else if (throwable instanceof ServerException) {
            int errorCode = ((ServerException) throwable).getErrorCode();
            String msg = ((ServerException) throwable).getErrorMsg();
            errorUrl = ((ServerException) throwable).getUrl();
            exception = new ApiException(throwable, errorCode);
            exception.setMsg(msg);

        } else {
            exception = new ApiException(throwable, ERROR.UNKNOWN);
            exception.setMsg(ExceString.Companion.getString(ExceString.ServerUnknown) + throwable.getMessage());
        }
        exception.setUrl(errorUrl);

        return exception;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        if (msg == null) {
            return "";
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 约定异常
     */
    public static class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 连接超时
         */
        public static final int TIMEOUT_ERROR = 1001;
        /**
         * 空指针错误
         */
        public static final int NULL_POINTER_EXCEPTION = 1002;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1003;

        /**
         * 类转换错误
         */
        public static final int CAST_ERROR = 1004;

        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1005;

        /**
         * 非法数据异常
         */
        public static final int ILLEGAL_STATE_ERROR = 1006;
    }

    /**
     * 服务端异常
     */
    public static class ServerException extends RuntimeException {
        private int errorCode;
        private String errorMsg;
        private String url;

        public ServerException(int errorCode, String errorMsg, String url) {
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
            this.url = url;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        public String getErrorMsg() {
            return this.errorMsg;
        }

        public String getUrl() {
            return url;
        }
    }
}
