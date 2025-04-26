package com.meaningfulplaylists.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlaylistRequest(
        @NotBlank(message = "State cannot be empty")
        String state,

        @NotBlank(message = "playlistName cannot be empty")
        String playlistName,

        @NotBlank(message = "keyword cannot be empty")
        String keywords
) {}
