package com.meaningfulplaylists.domain.usecases;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CreatePlaylistUseCase {
    private static final String DEFAULT_DESCRIPTION = "";
    private final MusicProvider musicProvider;

    public CreatePlaylistUseCase(MusicProvider musicProvider) {
        this.musicProvider = musicProvider;
    }

    public Playlist createPlaylist(String stateAssociated, String playlistName, List<String> titleList) {
        List<Track> trackList = findTracksByTitle(titleList);

        Playlist playlist = new Playlist(playlistName, DEFAULT_DESCRIPTION, stateAssociated, true, trackList);

        musicProvider.createPlaylist(playlist);

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
        Track track = musicProvider.findByTitle(title);
        log.info("Found track: {}", track);

        return track;
    }
}
