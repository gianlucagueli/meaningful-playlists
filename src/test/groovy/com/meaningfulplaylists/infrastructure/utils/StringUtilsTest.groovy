package com.meaningfulplaylists.infrastructure.utils

import spock.lang.Specification

class StringUtilsTest extends Specification {
    def "RemovePunct - should remove from the input string the external punctuation"() {
        when:
        String output = StringUtils.removePunct(input)

        then:
        Objects.equals(output, expected)

        where:
        input               | expected
        "test-1,"           | "test-1"
        ",test-2"           | "test-2"
        ".test-3,"          | "test-3"
        "test-4,:;!"        | "test-4"
        "\'test-5-"         | "test-5"
        "_test.abcd.123-"   | "test.abcd.123"
    }
}
