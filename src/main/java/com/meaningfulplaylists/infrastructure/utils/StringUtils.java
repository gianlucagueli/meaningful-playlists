package com.meaningfulplaylists.infrastructure.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final String REGEX = "\\s+|[,.!?;:]|[^\\s,.!?;:]+";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return tokens;
        }

        Matcher matcher = PATTERN.matcher(input);

        while (matcher.find()) {
            String token = matcher.group();

            if (!token.trim().isEmpty()) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    public static boolean equals(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }

        String strippedStr1 = normalize(s1);
        String strippedStr2 = normalize(s2);

        return strippedStr1.equalsIgnoreCase(strippedStr2);
    }

    public static String combine(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }

        String joined = String.join(" ", tokens);

        return joined.replaceAll(" ,", ",")
                .replaceAll(" \\.", ".")
                .replaceAll(" !", "!")
                .replaceAll(" \\?", "?")
                .replaceAll(" ;", ";")
                .replaceAll(" :", ":");
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
