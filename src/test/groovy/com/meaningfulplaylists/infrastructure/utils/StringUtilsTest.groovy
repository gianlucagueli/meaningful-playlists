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
}
