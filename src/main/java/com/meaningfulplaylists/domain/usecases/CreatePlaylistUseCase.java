package com.meaningfulplaylists.domain.usecases;

import com.meaningfulplaylists.domain.exceptions.TrackNotFoundException;
import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CreatePlaylistUseCase {
    private final MusicProviderRepository musicProviderRepository;
    private final Map<String, Track> collectedTracks;

    public CreatePlaylistUseCase(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.collectedTracks = new HashMap<>();
    }

    public Playlist createPlaylist(String userId, String playlistName, List<String> titleList) {
        try {
            List<Track> trackList = findTracksByTitle(titleList);
        } catch (TrackNotFoundException e) {
            log.error("Error while creating playlist. ", e);
        }

        // todo: implement next part
        return null;
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

        Track track = musicProviderRepository
                .findByTitle(title)
                .orElseThrow(() -> new TrackNotFoundException(title));

        collectedTracks.put(title, track);
        return track;
    }
}
