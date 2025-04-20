package com.meaningfulplaylists.infrastructure.spotify.models

import spock.lang.Specification
import spock.lang.Unroll

class SpotifyScopeTest extends Specification {

    @Unroll
    def "getScope - should return '#expected' for SpotifyScope.#enumName"() {
        expect:
        enumValue.getScope() == expected

        where:
        enumValue                               | enumName                      | expected
        SpotifyScope.UGC_IMAGE_UPLOAD           | "UGC_IMAGE_UPLOAD"           | "ugc-image-upload"
        SpotifyScope.USER_READ_PLAYBACK_STATE   | "USER_READ_PLAYBACK_STATE"   | "user-read-playback-state"
        SpotifyScope.USER_MODIFY_PLAYBACK_STATE | "USER_MODIFY_PLAYBACK_STATE" | "user-modify-playback-state"
        SpotifyScope.USER_READ_CURRENTLY_PLAYING| "USER_READ_CURRENTLY_PLAYING"| "user-read-currently-playing"
        SpotifyScope.APP_REMOTE_CONTROL         | "APP_REMOTE_CONTROL"         | "app-remote-control"
        SpotifyScope.STREAMING                  | "STREAMING"                  | "streaming"
        SpotifyScope.PLAYLIST_READ_PRIVATE      | "PLAYLIST_READ_PRIVATE"      | "playlist-read-private"
        SpotifyScope.PLAYLIST_READ_COLLABORATIVE| "PLAYLIST_READ_COLLABORATIVE"| "playlist-read-collaborative"
        SpotifyScope.PLAYLIST_MODIFY_PRIVATE    | "PLAYLIST_MODIFY_PRIVATE"    | "playlist-modify-private"
        SpotifyScope.PLAYLIST_MODIFY_PUBLIC     | "PLAYLIST_MODIFY_PUBLIC"     | "playlist-modify-public"
        SpotifyScope.USER_FOLLOW_MODIFY         | "USER_FOLLOW_MODIFY"         | "user-follow-modify"
        SpotifyScope.USER_FOLLOW_READ           | "USER_FOLLOW_READ"           | "user-follow-read"
        SpotifyScope.USER_READ_PLAYBACK_POSITION| "USER_READ_PLAYBACK_POSITION"| "user-read-playback-position"
        SpotifyScope.USER_TOP_READ              | "USER_TOP_READ"              | "user-top-read"
        SpotifyScope.USER_READ_RECENTLY_PLAYED  | "USER_READ_RECENTLY_PLAYED"  | "user-read-recently-played"
        SpotifyScope.USER_LIBRARY_MODIFY        | "USER_LIBRARY_MODIFY"        | "user-library-modify"
        SpotifyScope.USER_LIBRARY_READ          | "USER_LIBRARY_READ"          | "user-library-read"
        SpotifyScope.USER_READ_EMAIL            | "USER_READ_EMAIL"            | "user-read-email"
        SpotifyScope.USER_READ_PRIVATE          | "USER_READ_PRIVATE"          | "user-read-private"
        SpotifyScope.USER_SOA_LINK              | "USER_SOA_LINK"              | "user-soa-link"
        SpotifyScope.USER_SOA_UNLINK            | "USER_SOA_UNLINK"            | "user-soa-unlink"
        SpotifyScope.SOA_MANAGE_ENTITLEMENTS    | "SOA_MANAGE_ENTITLEMENTS"    | "soa-manage-entitlements"
        SpotifyScope.SOA_MANAGE_PARTNER         | "SOA_MANAGE_PARTNER"         | "soa-manage-partner"
        SpotifyScope.SOA_CREATE_PARTNER         | "SOA_CREATE_PARTNER"         | "soa-create-partner"
    }
}
