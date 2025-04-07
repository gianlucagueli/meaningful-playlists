package com.meaningfulplaylists.domain.repositories;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;

public interface MusicProviderRepository {
    Track findByTitle(String title);
    void createPlaylist(Playlist playlist);
}

