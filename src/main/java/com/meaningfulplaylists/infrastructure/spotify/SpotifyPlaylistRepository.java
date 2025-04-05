package com.meaningfulplaylists.infrastructure.spotify;

import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.domain.repositories.MusicProviderRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SpotifyPlaylistRepository implements MusicProviderRepository {
    SpotifyConfig spotifyConfig;

    SpotifyPlaylistRepository(SpotifyConfig spotifyConfig) {
        this.spotifyConfig = spotifyConfig;
    }

    @Override
    public Optional<Track> findByTitle(String title) {
        return Optional.empty();
    }
}