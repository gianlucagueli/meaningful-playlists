package com.meaningfulplaylists.infrastructure.spotify;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProviderRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException;
import com.meaningfulplaylists.infrastructure.spotify.models.*;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import com.meaningfulplaylists.infrastructure.spotify.services.SpotifyAuthService;
import com.meaningfulplaylists.infrastructure.spotify.services.SpotifyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;

@Slf4j
@Component
public class SpotifyRepository implements MusicProviderRepository {
    SpotifyConfig spotifyConfig;
    SpotifyAuthService authService;

    SpotifyRepository(SpotifyConfig spotifyConfig, SpotifyAuthService authService) {
        this.spotifyConfig = spotifyConfig;
        this.authService = authService;
    }

    @Override
    public Track findByTitle(String title) {
        String type = "track"; // todo: fare un enum
        int SEARCH_LIMIT = 1;

        Call<SpotifySearchResponse> call = spotifyConfig.getSpotifyApi().searchTracks(title, type, SEARCH_LIMIT);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifySearchResponse::tracks)
                .map(SpotifyTracks::items)
                .flatMap(items -> items.stream().findFirst())
                .map(SpotifyMapper::mapToDomain)
                .orElseThrow(() -> new SpotifyTrackNotFoundException(title));
    }

    @Override
    public void createPlaylist(Playlist playlist) {
        log.info("Creating playlist {}", playlist.name());

        // fixme: questa classe non dovrebbe occuparsi di auth
        String userId = authService.getUserIdFromState(playlist.stateAssociated());
        String authToken = authService.getUserAuthorization(userId);

        SpotifyCreatePlaylistResponse playlistResponse = createPlaylist(authToken, userId, playlist);
        updatePlaylist(authToken, playlistResponse.id(), playlist);

        log.info("Playlist [{}:{}] created successfully", playlistResponse.id(), playlist.name());
    }


    private SpotifyCreatePlaylistResponse createPlaylist(String authToken, String userId, Playlist playlist) {
        SpotifyCreatePlaylistRequest request = new SpotifyCreatePlaylistRequest(
                playlist.name(),
                playlist.description(),
                false
        );

        Call<SpotifyCreatePlaylistResponse> call = spotifyConfig.getSpotifyApi().createPlaylist(authToken, userId, request);
        return RetrofitUtils.safeExecute(call)
                .orElseThrow(() -> new RuntimeException("Failed to create playlist for userId: " + userId));
    }

    private void updatePlaylist(String authToken, String playlistId, Playlist playlist) {
        if (playlist.tracks() == null || playlist.tracks().isEmpty()) {
            log.info("No track present in playlist {}, leaving...", playlist.name());
            return;
        }

        SpotifyAddTracksRequest addTracksRequest = new SpotifyAddTracksRequest(playlist.tracks().stream().map(Track::uri).toList(), 0);
        Call<Void> addTracksCall = spotifyConfig.getSpotifyApi().addTracksToPlaylist(authToken, playlistId, addTracksRequest);

        RetrofitUtils.safeExecute(addTracksCall)
                .orElseThrow(() -> new RuntimeException("Failed to add tracks to playlist"));
    }
}