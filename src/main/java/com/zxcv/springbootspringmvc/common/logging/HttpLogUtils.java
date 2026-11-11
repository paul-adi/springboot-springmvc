package com.zxcv.springbootspringmvc.common.logging;

import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class HttpLogUtils {

    public static final int MAX_LOGGED_BODY_BYTES = 8 * 1024; // 8 KB

    public static String bodyForLog(byte[] body, String contentType) {
        if (body == null || body.length == 0) {
            return "<empty>";
        }
        if (!isText(contentType)) {
            return "<not logged : " + contentType + ", " + body.length + " bytes>";
        }
        int length = Math.min(body.length, MAX_LOGGED_BODY_BYTES);
        String text = new String(body, 0, length, StandardCharsets.UTF_8);
        return body.length > MAX_LOGGED_BODY_BYTES ? text + "... [truncated]" : text;
    }

    public static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static boolean isText(String contentType) {
        if (contentType == null) {
            return false;
        }
        String type = contentType.toLowerCase(Locale.ROOT);
        return type.contains("json")
                || type.contains("xml")
                || type.startsWith("text/")
                || type.startsWith("application/x-www-form-urlencoded");
    }

}
