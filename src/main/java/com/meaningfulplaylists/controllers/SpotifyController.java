package com.meaningfulplaylists.controllers;

import com.meaningfulplaylists.controllers.dto.CreatePlaylistRequest;
import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.usecases.CallbackUseCase;
import com.meaningfulplaylists.domain.usecases.CreatePlaylistUseCase;
import com.meaningfulplaylists.domain.usecases.RedirectUseCase;
import com.meaningfulplaylists.infrastructure.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/spotify")
@Tag(name = "Spotify Controller")
public class SpotifyController {
    CreatePlaylistUseCase createPlaylistUseCase;
    RedirectUseCase redirectUseCase;
    CallbackUseCase callbackUseCase;

    SpotifyController(CreatePlaylistUseCase createPlaylistUseCase,
                      RedirectUseCase redirectUseCase,
                      CallbackUseCase callbackUseCase) {
        this.createPlaylistUseCase = createPlaylistUseCase;
        this.redirectUseCase = redirectUseCase;
        this.callbackUseCase = callbackUseCase;
    }


    @GetMapping()
    @Operation(
            summary = "Start Spotify authentication and playlist creation flow",
            description = "Redirects the user to Spotify's authentication page for playlist creation."
    )
    public ModelAndView redirect() {
        String url = this.redirectUseCase.execute(Action.CREATE_PLAYLIST);

        ModelAndView mav = new ModelAndView("spotify/playlist-landing");
        mav.addObject("url", url);

        return mav;
    }

    @Operation(
            summary = "Spotify authentication callback",
            description = "Handles the callback from Spotify after user authentication.",
            parameters = {
                    @Parameter(name = "code", description = "Authorization code returned by Spotify", required = true),
                    @Parameter(name = "state", description = "Session state for validation", required = true)
            }
    )
    @GetMapping("/callback")
    public ModelAndView callback(@RequestParam String code, @RequestParam String state) {
        this.callbackUseCase.execute(code, state);
        ModelAndView mav = new ModelAndView("spotify/playlist-create-form");
        mav.addObject("state", state);
        mav.addObject("createPlaylistRequest", new CreatePlaylistRequest(state, "", ""));
        return mav;
    }

    @Operation(
            summary = "Create a new Spotify playlist",
            description = "Creates a new Spotify playlist using the provided name and keywords."
    )
    @PostMapping()
    public ModelAndView create(@Valid CreatePlaylistRequest request) {
        ModelAndView mav = new ModelAndView("spotify/playlist-created");

        List<String> keywordList = StringUtils.tokenize(request.keywords());
        log.info("Creating playlist {} with keywords {}", request.playlistName(), keywordList);

        try {
            Playlist playlist = createPlaylistUseCase.createPlaylist(request.state(), request.playlistName(), keywordList);

            mav.addObject("playlistName", playlist.name());
        } catch (Exception ignored) {
            return new ModelAndView("spotify/playlist-error");
        }

        return mav;
    }
}
