package me.doupay.sdk.net.interceptors;


import okhttp3.internal.platform.Platform;

import static me.doupay.sdk.Constants.openSysLog;


public class I {
    private static String[] prefix = {". ", " ."};
    private static int index = 0;

    protected I() {
        throw new UnsupportedOperationException();
    }

    static void log(String msg) {
        if (openSysLog) {
            System.out.println(msg);
        }
    }

    private static String getFinalTag(final String tag, final boolean isLogHackEnable) {
        if (isLogHackEnable) {
            index = index ^ 1;
            return prefix[index] + tag;
        } else {
            return tag;
        }
    }
}
