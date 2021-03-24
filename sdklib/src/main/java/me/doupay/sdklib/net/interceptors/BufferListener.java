package me.doupay.sdklib.net.interceptors;
import java.io.IOException;

import okhttp3.Request;

public interface BufferListener {
    String getJsonResponse(Request request) throws IOException;
}
