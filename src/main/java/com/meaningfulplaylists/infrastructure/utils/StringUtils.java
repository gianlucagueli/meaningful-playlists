package com.meaningfulplaylists.infrastructure.utils;

public class StringUtils {
    public static final String REGEX_PUNCTUATION = "^\\p{Punct}+|\\p{Punct}+$";

    public static String removePunct(String rawInput) {
        return rawInput.replaceAll(REGEX_PUNCTUATION, "");
    }
}
