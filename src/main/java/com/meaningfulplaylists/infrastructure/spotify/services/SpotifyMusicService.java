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
import com.meaningfulplaylists.infrastructure.utils.StringUtils;
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

    @PostConstruct
    private void init() {
        loadUtilityTracks();
    }

    @Override
    public Track findByTitle(String title) {
        return tracksRepository.findByName(title)
                .orElseGet(() -> retrieveTrackFromSpotify(title));
    }

    private Track retrieveTrackFromSpotify(String title) {
        int SEARCH_LIMIT = 50;
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
                .filter(track -> StringUtils.equals(title, track.name()))
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

    private void loadUtilityTracks() {
        this.tracksRepository.save(getTrack("1A05ibu1DXGIt0F62NG7xU"));
        this.tracksRepository.save(getTrack("1TwN15RFItXAF4b32d8TVU"));
        this.tracksRepository.save(getTrack("6akffeWlG2JB4u8AOJ4WRo"));
        this.tracksRepository.save(getTrack("5e5hRYVA6SatSjvDiq9WXs"));
        this.tracksRepository.save(getTrack("4n08SrZuPK09cHsVfvEcHc"));
        this.tracksRepository.save(getTrack("6P0ob6VV2SzHanRB9Ai7eA"));
        this.tracksRepository.save(getTrack("02Rkdlw2ku7bYCOKF8qAVR"));
    }

    private Track getTrack(String trackId) {
        Call<SpotifyTrack> call = spotifyConfig.getSpotifyApi().getTrack(trackId);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifyMapper::mapToDomain)
                .orElseThrow(() -> new SpotifyTrackNotFoundException(trackId));
    }

}