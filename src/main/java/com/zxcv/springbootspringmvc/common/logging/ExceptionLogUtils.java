package com.zxcv.springbootspringmvc.common.logging;

import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ExceptionLogUtils {

    private static final String APP_PACKAGE = rootPackage();
    private static final int MAX_FRAMES = 5;
    private static final int MAX_MESSAGE_LENGTH = 200;

    private static String rootPackage() {
        String[] parts = ExceptionLogUtils.class.getPackageName().split("\\.");
        return parts[0] + "." + parts[1] + ".";
    }

    public static String summarize(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        Throwable rootCause = rootCauseOf(throwable);

        String frames = appFramesOf(rootCause);
        if (frames.isEmpty()) {
            frames = appFramesOf(throwable);
        }

        String type = rootCause.getClass().getCanonicalName();
        String message = messageOf(rootCause);
        String summary = message.isEmpty() ? "[" + type + "]" : "[" + type + "] " + message;

        return frames.isEmpty() ? summary : summary + " at (" + frames + ")";
    }

    public static Throwable rootCauseOf(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = throwable;
        while (current.getCause() != null && seen.add(current)) {
            current = current.getCause();
        }
        return current;
    }

    private static String messageOf(Throwable throwable) {
        if (throwable instanceof RestClientResponseException ex) {
            return "HTTP " + ex.getStatusCode().value();
        }
        return shorten(throwable.getMessage());
    }

    private static String shorten(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String oneLine = message.replaceAll("\\s+", " ").strip();
        return oneLine.length() > MAX_MESSAGE_LENGTH
                ? oneLine.substring(0, MAX_MESSAGE_LENGTH) + "..."
                : oneLine;
    }

    private static String appFramesOf(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .filter(frame -> frame.getClassName().startsWith(APP_PACKAGE))
                .filter(frame -> !frame.getClassName().contains("$$"))
                .filter(frame -> frame.getFileName() != null)
                .limit(MAX_FRAMES)
                .map(frame -> frame.getFileName() + ":" + frame.getLineNumber())
                .collect(Collectors.joining(", "));
    }

}
