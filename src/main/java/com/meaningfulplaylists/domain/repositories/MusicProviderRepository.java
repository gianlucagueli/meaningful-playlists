package com.meaningfulplaylists.domain.repositories;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import java.util.Optional;

public interface MusicProviderRepository {

    Optional<Track> findByTitle(String title);

    void createPlaylist(Playlist playlist);
}

