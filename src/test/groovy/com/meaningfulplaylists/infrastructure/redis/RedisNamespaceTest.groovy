package com.meaningfulplaylists.infrastructure.redis

import spock.lang.Specification
import spock.lang.Unroll

class RedisNamespaceTest extends Specification {

    @Unroll
    def "getPrefix - should return '#expected' for RedisNamespace.#enumName"() {
        expect:
        enumValue.getPrefix() == expected

        where:
        enumValue               | enumName   | expected
        RedisNamespace.CLIENT   | "CLIENT"   | "client:"
        RedisNamespace.STATE    | "STATE"    | "states:"
        RedisNamespace.TRACK    | "TRACK"    | "tracks:"
        RedisNamespace.USER     | "USER"     | "users:"
    }

    @Unroll
    def "key - should return '#expected' when id is '#id' for RedisNamespace.#enumName"() {
        expect:
        enumValue.key(id) == expected

        where:
        enumValue               | enumName   | id            | expected
        RedisNamespace.CLIENT   | "CLIENT"   | "test-client" | "client:test-client"
        RedisNamespace.STATE    | "STATE"    | "abc123"      | "states:abc123"
        RedisNamespace.TRACK    | "TRACK"    | "song-title"  | "tracks:song-title"
        RedisNamespace.USER     | "USER"     | "user-id-123" | "users:user-id-123"
        RedisNamespace.CLIENT   | "CLIENT"   | ""            | "client:"
        RedisNamespace.STATE    | "STATE"    | null          | "states:null"
    }
}
