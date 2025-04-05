package com.meaningfulplaylists.controllers;

import com.meaningfulplaylists.domain.usecases.CreatePlaylistUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private final CreatePlaylistUseCase createPlaylistUseCase;

    public PlaylistController(CreatePlaylistUseCase createPlaylistUseCase) {
        this.createPlaylistUseCase = createPlaylistUseCase;
    }

    @PostMapping("/create-from-keywords")
    public String createPlaylistFromKeywords(
            @RequestParam String userId,
            @RequestParam String playlistName,
            @RequestBody List<String> keywords) {
        return createPlaylistUseCase.createPlaylist(userId, playlistName, keywords).toString();
    }

} 