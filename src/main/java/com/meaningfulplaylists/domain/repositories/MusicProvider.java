package com.meaningfulplaylists.domain.repositories;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;

import java.util.List;

public interface MusicProvider {
    List<Track> findTracks(List<String> titles);
    void createPlaylist(Playlist playlist);
}

