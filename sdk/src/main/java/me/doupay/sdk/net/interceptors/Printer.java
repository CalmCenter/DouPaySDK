package me.doupay.sdk.net.interceptors;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import static me.doupay.sdk.Constants.openSysLog;

public class Printer {
    private static final int JSON_INDENT = 3;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
    private static final String PLACEHOLDER = " ";
    private static final String[] OMITTED_RESPONSE = {LINE_SEPARATOR, "Omitted response body"};
    private static final String[] OMITTED_REQUEST = {LINE_SEPARATOR, "Omitted request body"};

    private static final String N = "\n";
    private static final String T = "\t";
    private static final String REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────";
    private static final String END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────";
    private static final String RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────";
    private static final String BODY_TAG = "Body:";
    private static final String URL_TAG = "URL: ";
    private static final String METHOD_TAG = "Method: @";
    private static final String HEADERS_TAG = "Headers:";
    private static final String STATUS_CODE_TAG = "Status Code: ";
    private static final String RECEIVED_TAG = "Received in: ";
    private static final String CORNER_UP = "┌ ";
    private static final String CORNER_BOTTOM = "└ ";
    private static final String CENTER_LINE = "├ ";
    //    private static final String DEFAULT_LINE = "│ ";
    private static final String DEFAULT_LINE = "  ";
    private static final String OOM_OMITTED = LINE_SEPARATOR + "Output omitted because of Object size.";
    private static final int MAX_LEN = 2200;

    protected Printer() {
        throw new UnsupportedOperationException();
    }

    private static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || N.equals(line) || T.equals(line) || TextUtils.isEmpty(line.trim());
    }

    static void printJsonRequest(LoggingInterceptor.Builder builder, Request request) {
        String requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request);
        String tag = builder.getTag(true);
        StringBuilder sb = new StringBuilder();
        sb.append(PLACEHOLDER).append(LINE_SEPARATOR);
        sb.append(REQUEST_UP_LINE).append(LINE_SEPARATOR);
        sb.append(logLines(builder.getType(), tag, new String[]{URL_TAG + request.url()}, builder.getLogger(), false, builder.isLogHackEnable()));
        sb.append(logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(), true, builder.isLogHackEnable()));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            sb.append(logLines(builder.getType(), tag, requestBody.split(LINE_SEPARATOR), builder.getLogger(), true, builder.isLogHackEnable()));
        }
        sb.append(END_LINE);
        PrintStr(tag, sb.toString());
    }


    static void printJsonResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, String bodyString, List<String> segments, String message, final String responseUrl) {
        final String responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(bodyString);
        final String tag = builder.getTag(false);
        final String[] urlLine = {URL_TAG + responseUrl, N};
        final String[] response = getResponse(headers, chainMs, code, isSuccessful,
            builder.getLevel(), segments, message);
        StringBuilder sb = new StringBuilder();
        sb.append(PLACEHOLDER).append(LINE_SEPARATOR);
        sb.append(RESPONSE_UP_LINE).append(LINE_SEPARATOR);

        sb.append(logLines(builder.getType(), tag, urlLine, builder.getLogger(), true, builder.isLogHackEnable()));
        sb.append(logLines(builder.getType(), tag, response, builder.getLogger(), true, builder.isLogHackEnable()));

        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            sb.append(logLines(builder.getType(), tag, responseBody.split(LINE_SEPARATOR), builder.getLogger(),
                true, builder.isLogHackEnable()));
        }
        sb.append(END_LINE);
        PrintStr( tag, sb.toString());

    }

    private static void PrintStr( final String tag, final String msg) {
        int len = msg.length();
        int countOfSub = len / MAX_LEN;
        if (countOfSub > 0) {
            if (openSysLog) {
                System.out.println(tag + " " + msg.substring(0, MAX_LEN));
            }
            int index = MAX_LEN;
            for (int i = 1; i < countOfSub; i++) {
                if (msg.substring(index, index + MAX_LEN).startsWith(DEFAULT_LINE)) {
                    if (openSysLog) {
                        System.out.println(tag + " " + PLACEHOLDER + LINE_SEPARATOR + msg.substring(index, index + MAX_LEN));
                    }
                } else {
                    if (openSysLog) {
                        System.out.println(tag + " " + PLACEHOLDER + LINE_SEPARATOR + DEFAULT_LINE + msg.substring(index, index + MAX_LEN));
                    }
                }
                index += MAX_LEN;
            }
            if (index != len) {
                if (msg.substring(index, len).startsWith(DEFAULT_LINE)) {
                    if (openSysLog) {
                        System.out.println(tag + " " + PLACEHOLDER + LINE_SEPARATOR + msg.substring(index, len));
                    }
                } else {
                    if (openSysLog) {
                        System.out.println(tag + " " + PLACEHOLDER + LINE_SEPARATOR + DEFAULT_LINE + msg.substring(index, len));
                    }
                }
            }
        } else {
            if (openSysLog) {
                System.out.println(tag + " " + msg);
            }

        }
    }

    private static TagHead processTagAndHead(String tag) {
        if (!true && !true) {
            tag = null;
        } else {
            final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            final int stackIndex = 3 + 0;
            if (stackIndex >= stackTrace.length) {
                StackTraceElement targetElement = stackTrace[3];
                final String fileName = getFileName(targetElement);
                if (true && isSpace(tag)) {
                    int index = fileName.indexOf('.');// Use proguard may not find '.'.
                    tag = index == -1 ? fileName : fileName.substring(0, index);
                }
                return new TagHead(tag, null, ": ");
            }
            StackTraceElement targetElement = stackTrace[stackIndex];
            final String fileName = getFileName(targetElement);
            if (true && isSpace(tag)) {
                int index = fileName.indexOf('.');// Use proguard may not find '.'.
                tag = index == -1 ? fileName : fileName.substring(0, index);
            }
            if (true) {
                String tName = Thread.currentThread().getName();
                final String head = new Formatter()
                    .format("%s, %s.%s(%s:%d)",
                        tName,
                        targetElement.getClassName(),
                        targetElement.getMethodName(),
                        fileName,
                        targetElement.getLineNumber())
                    .toString();
                final String fileHead = " [" + head + "]: ";
                if (1 <= 1) {
                    return new TagHead(tag, new String[]{head}, fileHead);
                } else {
                    final String[] consoleHead =
                        new String[Math.min(
                            1,
                            stackTrace.length - stackIndex
                        )];
                    consoleHead[0] = head;
                    int spaceLen = tName.length() + 2;
                    String space = new Formatter().format("%" + spaceLen + "s", "").toString();
                    for (int i = 1, len = consoleHead.length; i < len; ++i) {
                        targetElement = stackTrace[i + stackIndex];
                        consoleHead[i] = new Formatter()
                            .format("%s%s.%s(%s:%d)",
                                space,
                                targetElement.getClassName(),
                                targetElement.getMethodName(),
                                getFileName(targetElement),
                                targetElement.getLineNumber())
                            .toString();
                    }
                    return new TagHead(tag, consoleHead, fileHead);
                }
            }
        }
        return new TagHead(tag, null, ": ");
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String getFileName(final StackTraceElement targetElement) {
        String fileName = targetElement.getFileName();
        if (fileName != null) return fileName;
        // If name of file is null, should add
        // "-keepattributes SourceFile,LineNumberTable" in proguard file.
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1];
        }
        int index = className.indexOf('$');
        if (index != -1) {
            className = className.substring(0, index);
        }
        return className + ".java";
    }

    static void printFileRequest(LoggingInterceptor.Builder builder, Request request) {
        String tag = builder.getTag(true);
        if (builder.getLogger() == null)
            I.log(REQUEST_UP_LINE);
        logLines(builder.getType(), tag, new String[]{URL_TAG + request.url()}, builder.getLogger(),
            false, builder.isLogHackEnable());
        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(),
            true, builder.isLogHackEnable());
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, OMITTED_REQUEST, builder.getLogger(), true, builder.isLogHackEnable());
        }
        if (builder.getLogger() == null)
            I.log(END_LINE);
    }

    static void printFileResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, List<String> segments, String message) {
        String tag = builder.getTag(false);
        if (builder.getLogger() == null)
            I.log(RESPONSE_UP_LINE);
        logLines(builder.getType(), tag, getResponse(headers, chainMs, code, isSuccessful,
            builder.getLevel(), segments, message), builder.getLogger(), true, builder.isLogHackEnable());
        logLines(builder.getType(), tag, OMITTED_RESPONSE, builder.getLogger(), true, builder.isLogHackEnable());
        if (builder.getLogger() == null)
            I.log(END_LINE);
    }


    private static String[] getRequest(Request request, Level level) {
        String log;
        String header = request.headers().toString();
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        log = METHOD_TAG + request.method() + DOUBLE_SEPARATOR +
            (isEmpty(header) ? "" : loggableHeader ? HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header) : "");
        return log.split(LINE_SEPARATOR);
    }

    private static String[] getResponse(String header, long tookMs, int code, boolean isSuccessful,
                                        Level level, List<String> segments, String message) {
        String log;
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        String segmentString = slashSegments(segments);
        log = ((!TextUtils.isEmpty(segmentString) ? segmentString + " - " : "") + "is success : "
            + isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
            code + " / " + message + DOUBLE_SEPARATOR + (isEmpty(header) ? "" : loggableHeader ? HEADERS_TAG + LINE_SEPARATOR +
            dotHeaders(header) : ""));
        return log.split(LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> segments) {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    private static String dotHeaders(String header) {
        String[] headers = header.split(LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        String tag = "─ ";
        if (headers.length > 1) {
            for (int i = 0; i < headers.length; i++) {
                if (i == 0) {
                    tag = CORNER_UP;
                } else if (i == headers.length - 1) {
                    tag = CORNER_BOTTOM;
                } else {
                    tag = CENTER_LINE;
                }
                builder.append(tag).append(headers[i]).append("\n");
            }
        } else {
            for (String item : headers) {
                builder.append(tag).append(item).append("\n");
            }
        }
        return builder.toString();
    }

    private static StringBuilder logLines(int type, String tag, String[] lines, Logger logger,
                                          boolean withLineSize, boolean useLogHack) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int lineLength = line.length();
            int MAX_LONG_SIZE = withLineSize ? 110 : lineLength;
            for (int i = 0; i <= lineLength / MAX_LONG_SIZE; i++) {
                int start = i * MAX_LONG_SIZE;
                int end = (i + 1) * MAX_LONG_SIZE;
                end = end > line.length() ? line.length() : end;
                sb.append(DEFAULT_LINE).append(line.substring(start, end)).append(LINE_SEPARATOR);
            }
        }
        return sb;
    }

    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody body = copy.body();
            if (body == null)
                return "";
            body.writeTo(buffer);
            return getJsonString(buffer.readUtf8());
        } catch (final IOException e) {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }

    static String getJsonString(final String msg) {
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("[")) {
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

    private static class TagHead {
        String tag;
        String[] consoleHead;
        String fileHead;

        TagHead(String tag, String[] consoleHead, String fileHead) {
            this.tag = tag;
            this.consoleHead = consoleHead;
            this.fileHead = fileHead;
        }
    }
}
