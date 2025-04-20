package com.meaningfulplaylists.infrastructure.spotify;

import com.meaningfulplaylists.infrastructure.spotify.models.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface SpotifyApi {

    @GET("v1/search")
    Call<SpotifySearchResponse> search(
        @Query("q") String query,
        @Query("type") String type,
        @Query("limit") int limit
    );

    @GET("v1/tracks/{track_id}")
    Call<SpotifyTrack> getTrack(
            @Path("track_id") String trackId
    );

    @GET("v1/me")
    Call<SpotifyUserProfile> getCurrentUserProfile(
            @Header("Authorization") String accessToken
    );

    @POST("v1/users/{user_id}/playlists")
    Call<SpotifyCreatePlaylistResponse> createPlaylist(
            @Header("Authorization") String accessToken,
            @Path("user_id") String userId,
            @Body SpotifyCreatePlaylistRequest playlistRequest
    );

    @POST("v1/playlists/{playlist_id}/tracks")
    Call<Void> addTracksToPlaylist(
            @Header("Authorization") String accessToken,
            @Path("playlist_id") String playlistId,
            @Body SpotifyAddTracksRequest request
    );
}
