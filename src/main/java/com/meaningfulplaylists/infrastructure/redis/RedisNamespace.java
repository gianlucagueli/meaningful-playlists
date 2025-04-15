package com.meaningfulplaylists.infrastructure.redis;

import lombok.Getter;

@Getter
public enum RedisNamespace {
    CLIENT("client:"),
    STATE("states:"),
    TRACK("tracks:"),
    USER("users:");

    private final String prefix;

    RedisNamespace(String prefix) {
        this.prefix = prefix;
    }

    public String key(String id) {
        return prefix + id;
    }
}
