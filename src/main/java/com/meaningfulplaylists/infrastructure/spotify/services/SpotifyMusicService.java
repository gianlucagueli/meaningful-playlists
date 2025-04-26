package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProvider;
import com.meaningfulplaylists.infrastructure.redis.repository.TracksRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException;
import com.meaningfulplaylists.infrastructure.spotify.models.*;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyMapper;
import com.meaningfulplaylists.infrastructure.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class SpotifyMusicService implements MusicProvider {
    private final SpotifyConfig spotifyConfig;
    private final SpotifyProperties spotifyProperties;
    private final SpotifyAuthService authService;
    private final TracksRedisRepository tracksRepository;

    SpotifyMusicService(SpotifyConfig spotifyConfig,
                        SpotifyProperties properties,
                        SpotifyAuthService authService,
                        TracksRedisRepository tracksRepository) {
        this.spotifyConfig = spotifyConfig;
        this.spotifyProperties = properties;
        this.authService = authService;
        this.tracksRepository = tracksRepository;
    }

    @PostConstruct
    private void init() {
        loadUtilityTracks();
    }

    @Override
    public List<Track> findTracks(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        List<Track> result = new ArrayList<>();
        int currentIndex = 0;

        while (currentIndex < keywords.size()) {
            Pair<Integer, Track> bestMatch = findLongestMatchingSequence(keywords, currentIndex);

            if (bestMatch == null) {
                log.warn("No matching tracks found for {}", keywords.get(currentIndex));
                currentIndex++;
                continue;
            }

            result.add(bestMatch.getSecond());
            currentIndex += bestMatch.getFirst();
        }

        return result;
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


    private Pair<Integer, Track> findLongestMatchingSequence(List<String> keywords, int startIndex) {
        int maxLength = Math.min(keywords.size() - startIndex, 5);

        for (int length = maxLength; length > 0; length--) {
            if (startIndex + length > keywords.size()) {
                continue;
            }

            List<String> sequence = keywords.subList(startIndex, startIndex + length);
            String combinedTitle = StringUtils.combine(sequence);

            try {
                Track track = findByTitle(combinedTitle);
                return Pair.of(length, track);
            } catch (SpotifyTrackNotFoundException ignored) {
                log.debug("No matching tracks found for {}", combinedTitle);
            }
        }

        return null;
    }

    private Track findByTitle(String title) {
        log.info("Finding track by title: {}", title);

        Track result = tracksRepository.findByName(title)
                .orElseGet(() -> getTrackFromSpotify(title));

        log.info("Found track: {}", result);
        return result;
    }

    private Track getTrackFromSpotify(String title) {
        int SEARCH_LIMIT = 10;
        Call<SpotifySearchResponse> call = spotifyConfig.getSpotifyApi()
                .search(
                        title,
                        SpotifySearchType.TRACK.getType(),
                        SEARCH_LIMIT
                );

        return RetrofitUtils.safeExecute(call)
                .map(SpotifySearchResponse::tracks)
                .map(SpotifyTracks::items)
                .flatMap(tracks -> findMatchingTrack(title, tracks))
                .map(SpotifyMapper::mapToDomain)
                .map(track -> {
                    tracksRepository.save(track);
                    return track;
                })
                .orElseThrow(() -> new SpotifyTrackNotFoundException(title));
    }

    private Optional<SpotifyTrack> findMatchingTrack(String title, List<SpotifyTrack> tracks) {
        return tracks.stream()
                .filter(track -> track.name() != null)
                .filter(track -> StringUtils.equalsNormalizedIgnoreCase(title, track.name()))
                .findFirst();
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
        spotifyProperties.utilityTracks().forEach(this::loadUtilityTrack);
    }

    private void loadUtilityTrack(String id, String title) {
        try {
            tracksRepository.findByName(title)
                    .orElseGet(() -> {
                        Track track = findById(id);
                        tracksRepository.save(track);
                        return track;
                    });

            log.info("Found utility track {}", title);
        } catch (SpotifyTrackNotFoundException ignored) {
            log.error("Failed to load utility track {}", title);
        }
    }

    private Track findById(String trackId) {
        log.info("Finding track by id {}", trackId);
        Call<SpotifyTrack> call = spotifyConfig.getSpotifyApi().getTrack(trackId);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifyMapper::mapToDomain)
                .orElseThrow(() -> new SpotifyTrackNotFoundException(trackId));
    }
}
