package com.meaningfulplaylists.domain.usecases;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CreatePlaylistUseCase {
    private static final String DEFAULT_DESCRIPTION = "";
    private final MusicProviderRepository musicProviderRepository;
    private final Map<String, Track> collectedTracks; //fixme: non dovrebbe stare qui, piu giusto nel repository

    public CreatePlaylistUseCase(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.collectedTracks = new HashMap<>();
    }

    public Playlist createPlaylist(String stateAssociated, String playlistName, List<String> titleList) {
        List<Track> trackList = findTracksByTitle(titleList);

        Playlist playlist = new Playlist(playlistName, DEFAULT_DESCRIPTION, stateAssociated, true, trackList);

        musicProviderRepository.createPlaylist(playlist);

        return playlist;
    }

    private List<Track> findTracksByTitle(List<String> titleList) {
        return Optional.ofNullable(titleList)
                .filter(list -> !list.isEmpty())
                .stream()
                .flatMap(List::stream)
                .map(this::findTrackByTitle)
                .toList();
    }

    private Track findTrackByTitle(String title) {
        if (collectedTracks.containsKey(title)) {
            return collectedTracks.get(title);
        }

        Track track = musicProviderRepository.findByTitle(title);
        log.info("Found track: {}", track);

        collectedTracks.put(title, track);
        return track;
    }
}
