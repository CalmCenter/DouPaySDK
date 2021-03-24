package me.doupay.sdk.net.exception

import me.doupay.sdk.Constants.language
import me.doupay.sdk.net.Language

enum class ExceString(
    var us: String,
    var cn: String) {
    ServerError("Network error", "网络错误"),
    RequestError("Request error", "请求错误"),
    ServerTimeOut("The network connection timed out. Please check your network status and try again later!", "网络连接超时，请检查您的网络状态，稍后重试！"),
    ServerNullPointer("Null pointer exception", "空指针异常"),
    ServerSslError("Certificate verification failed", "证书验证失败"),
    ServerCastError("Type conversion error", "类型转换错误"),
    ServerParseError("Parse error", "解析错误"),
    ServerUnknown("unknown error", "未知错误");

    companion object {
        fun getString(str: ExceString): String {
            return when (language) {
                Language.en_US -> str.us
                Language.zh_CH -> str.cn
                else -> str.us
            }
        }
    }
}