package com.meaningfulplaylists.infrastructure.utils;

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
}
