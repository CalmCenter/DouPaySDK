package me.doupay.sdklib.net.interceptors;

import android.util.Log;

import okhttp3.internal.platform.Platform;

import static me.doupay.sdklib.Constants.openSysLog;


public class I {
    private static String[] prefix = {". ", " ."};
    private static int index = 0;

    protected I() {
        throw new UnsupportedOperationException();
    }

    static void log(int type, String tag, String msg, final boolean isLogHackEnable) {
        final String finalTag = getFinalTag(tag, isLogHackEnable);
        switch (type) {
            case Platform.INFO:
                Log.println(Log.INFO,tag, msg);
                if (openSysLog) {
                    System.out.println(msg);
                }
                break;
            default:
                Log.println(Log.WARN,tag, msg);
                if (openSysLog) {
                    System.out.println(msg);
                }
                break;
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
