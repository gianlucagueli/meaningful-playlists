package com.meaningfulplaylists.infrastructure.spotify.models;

import lombok.Getter;

@Getter
public enum SpotifyScope {
    UGC_IMAGE_UPLOAD("ugc-image-upload"),
    USER_READ_PLAYBACK_STATE("user-read-playback-state"),
    USER_MODIFY_PLAYBACK_STATE("user-modify-playback-state"),
    USER_READ_CURRENTLY_PLAYING("user-read-currently-playing"),
    APP_REMOTE_CONTROL("app-remote-control"),
    STREAMING("streaming"),
    PLAYLIST_READ_PRIVATE("playlist-read-private"),
    PLAYLIST_READ_COLLABORATIVE("playlist-read-collaborative"),
    PLAYLIST_MODIFY_PRIVATE("playlist-modify-private"),
    PLAYLIST_MODIFY_PUBLIC("playlist-modify-public"),
    USER_FOLLOW_MODIFY("user-follow-modify"),
    USER_FOLLOW_READ("user-follow-read"),
    USER_READ_PLAYBACK_POSITION("user-read-playback-position"),
    USER_TOP_READ("user-top-read"),
    USER_READ_RECENTLY_PLAYED("user-read-recently-played"),
    USER_LIBRARY_MODIFY("user-library-modify"),
    USER_LIBRARY_READ("user-library-read"),
    USER_READ_EMAIL("user-read-email"),
    USER_READ_PRIVATE("user-read-private"),
    USER_SOA_LINK("user-soa-link"),
    USER_SOA_UNLINK("user-soa-unlink"),
    SOA_MANAGE_ENTITLEMENTS("soa-manage-entitlements"),
    SOA_MANAGE_PARTNER("soa-manage-partner"),
    SOA_CREATE_PARTNER("soa-create-partner");

    private final String scope;

    SpotifyScope(String scope) {
        this.scope = scope;
    }
}
