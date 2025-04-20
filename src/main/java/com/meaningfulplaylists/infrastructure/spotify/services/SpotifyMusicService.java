package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProvider;
import com.meaningfulplaylists.infrastructure.redis.repository.TracksRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException;
import com.meaningfulplaylists.infrastructure.spotify.models.*;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class SpotifyMusicService implements MusicProvider {
    SpotifyConfig spotifyConfig;
    SpotifyAuthService authService;
    TracksRedisRepository tracksRepository;

    SpotifyMusicService(SpotifyConfig spotifyConfig,
                        SpotifyAuthService authService,
                        TracksRedisRepository tracksRepository) {
        this.spotifyConfig = spotifyConfig;
        this.authService = authService;
        this.tracksRepository = tracksRepository;
    }

    @Override
    public Track findByTitle(String title) {
        return tracksRepository.findByName(title)
                .orElseGet(() -> retrieveTrackFromSpotify(title));
    }

    private Track retrieveTrackFromSpotify(String title) {
        int SEARCH_LIMIT = 20;
        Call<SpotifySearchResponse> call = spotifyConfig.getSpotifyApi()
                .searchTracks(
                        title,
                        SpotifySearchType.TRACK.getType(),
                        SEARCH_LIMIT
                );

        return RetrofitUtils.safeExecute(call)
                .map(SpotifySearchResponse::tracks)
                .map(SpotifyTracks::items)
                .map(tracks -> {
                    saveAllTracks(tracks);
                    return tracks;
                })
                .flatMap(tracks -> findMatchingTrack(title, tracks))
                .map(SpotifyMapper::mapToDomain)
                .orElseThrow(() -> new SpotifyTrackNotFoundException(title));
    }

    private void saveAllTracks(List<SpotifyTrack> tracks) {
        tracks.stream()
                .filter(track -> track.name() != null)
                .map(SpotifyMapper::mapToDomain)
                .forEach(tracksRepository::save);
    }

    private Optional<SpotifyTrack> findMatchingTrack(String title, List<SpotifyTrack> tracks) {
        return tracks.stream()
                .filter(track -> track.name() != null)
                .filter(track -> track.name().equalsIgnoreCase(title))
                .findFirst();
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