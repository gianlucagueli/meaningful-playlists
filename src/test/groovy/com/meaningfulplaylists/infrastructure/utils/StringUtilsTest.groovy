package com.meaningfulplaylists.infrastructure.utils

import spock.lang.Specification

class StringUtilsTest extends Specification {
    def "tokenize - should correctly split strings into tokens"() {
        when:
        List<String> result = StringUtils.tokenize(input)

        then:
        Objects.equals(result, expected)

        where:
        input                       | expected
        "test,1"                    | List.of("test", ",", "1")
        "Ciao, come stai?"          | List.of("Ciao", ",", "come", "stai", "?")
        "Hello! How are you?"       | List.of("Hello", "!", "How", "are", "you", "?")
        "a b c"                     | List.of("a", "b", "c")
        "123,456;789"               | List.of("123", ",", "456", ";", "789")
        "..."                       | List.of(".", ".", ".")
        "word1 word2,word3.word4!"  | List.of("word1", "word2", ",", "word3", ".", "word4", "!")
        ""                          | List.of()
        "   "                       | List.of()
        "Hello,world!"              | List.of("Hello", ",", "world", "!")
        "Hips don't lie"            | List.of("Hips", "don't", "lie")
    }

    def "equals should correctly compare strings '#s1' and '#s2' with result #expected"() {
        when:
        boolean result = StringUtils.equalsNormalizedIgnoreCase(s1, s2)

        then:
        result == expected

        where:
        s1             | s2             | expected | description
        "hello"        | "hello"        | true     | "identical strings"
        "Hello"        | "hello"        | true     | "case difference"
        "hello"        | "HELLO"        | true     | "all caps difference"
        "héllo"        | "hello"        | true     | "accented character"
        "hèllo"        | "hello"        | true     | "different accent"
        "hello world"  | "hello world"  | true     | "multi-word strings"
        "hello-world"  | "hello-world"  | true     | "strings with hyphens"
        "hello, world" | "hello, world" | true     | "strings with punctuation"
        "café"         | "cafe"         | true     | "accented character at the end"
        "résumé"       | "resume"       | true     | "multiple accents"
        "hello"        | "hello "       | false    | "trailing space"
        "hello"        | "hello!"       | false    | "different punctuation"
        "hello"        | "hello world"  | false    | "different length"
        "hello"        | "hallo"        | false    | "different characters"
        "hello"        | null           | false    | "null second string"
        null           | "hello"        | false    | "null first string"
        null           | null           | false    | "both strings null"
        "Beyoncé"      | "Beyonce"      | true     | "celebrity name with accent"
        "Motörhead"    | "Motorhead"    | true     | "band name with umlaut"
        "naïve"        | "naive"        | true     | "word with diaeresis"
        "piñata"       | "pinata"       | true     | "Spanish word with tilde"
        "façade"       | "facade"       | true     | "word with cedilla"
        "über"         | "uber"         | true     | "German word with umlaut"
        "El Niño"      | "El Nino"      | true     | "Spanish phrase with tilde"
    }


    def "combine - should correctly join tokens with proper punctuation handling for #description"() {
        when:
        String result = StringUtils.combine(tokens)

        then:
        result == expected

        where:
        tokens                                      | expected                       | description
        ["ciao", ",", "come", "stai", "?"]          | "ciao, come stai?"             | "basic greeting with comma and question mark"
        ["Hello", "world", "!"]                     | "Hello world!"                 | "simple exclamation"
        ["a", "b", "c"]                             | "a b c"                        | "simple words"
        ["123", ",", "456", ";", "789"]             | "123, 456; 789"                | "numbers with punctuation"
        ["...", "interessante", "."]                | "... interessante."            | "ellipsis and period"
        ["Questa", ",", "è", "una", "frase", "."]   | "Questa, è una frase."         | "sentence with comma and period"
        ["Non", "lo", "so", "!"]                    | "Non lo so!"                   | "exclamation phrase"
        ["Chi", "sei", "?"]                         | "Chi sei?"                     | "question phrase"
        ["Uno", ",", "due", ",", "tre"]             | "Uno, due, tre"                | "comma-separated list"
        ["Ciao", ":", "come", "stai", "?"]          | "Ciao: come stai?"             | "greeting with colon and question mark"
        ["Attento", ";", "pericolo", "!"]           | "Attento; pericolo!"           | "warning with semicolon and exclamation"
        ["Oggi", "è", "lunedì", ",", "vero", "?"]   | "Oggi è lunedì, vero?"         | "statement with comma and question mark"
        ["Bene", ",", "grazie", "."]                | "Bene, grazie."                | "simple response with comma and period"
        ["Ecco", ":", "1", ",", "2", ",", "3", "."] | "Ecco: 1, 2, 3."               | "list with colon, commas and period"
        []                                          | ""                             | "empty list"
        null                                        | ""                             | "null input"
        ["Solo", "una", "parola"]                   | "Solo una parola"              | "words without punctuation"
        ["!"]                                       | "!"                            | "single punctuation mark"
        ["Ciao", ".", "Come", "stai", "?"]          | "Ciao. Come stai?"             | "two sentences"
    }
}
