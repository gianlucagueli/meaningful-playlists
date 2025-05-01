package com.meaningfulplaylists.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for creating a new Spotify playlist")
public record CreatePlaylistRequest(
        @Schema(description = "Session state for validation", example = "xyz123")
        @NotBlank(message = "State cannot be empty")
        String state,

        @Schema(description = "Name of the playlist to be created", example = "My Mood Playlist")
        @NotBlank(message = "playlistName cannot be empty")
        String playlistName,

        @Schema(description = "Keywords for the playlist, separated by space or comma", example = "happy, summer, chill")
        @NotBlank(message = "keyword cannot be empty")
        String keywords
) {}