package com.meaningfulplaylists.controllers;

import com.meaningfulplaylists.controllers.dto.CreatePlaylistRequest;
import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.models.Playlist;
import com.meaningfulplaylists.domain.usecases.CallbackUseCase;
import com.meaningfulplaylists.domain.usecases.CreatePlaylistUseCase;
import com.meaningfulplaylists.domain.usecases.RedirectUseCase;
import com.meaningfulplaylists.infrastructure.utils.StringUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/spotify")
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
    public ModelAndView redirect() {
        String url = this.redirectUseCase.execute(Action.CREATE_PLAYLIST);

        ModelAndView mav = new ModelAndView("spotify/playlist-landing");
        mav.addObject("url", url);

        return mav;
    }

    @GetMapping("/callback")
    public ModelAndView callback(@RequestParam String code, @RequestParam String state) {
        this.callbackUseCase.execute(code, state);
        ModelAndView mav = new ModelAndView("spotify/playlist-create-form");
        mav.addObject("state", state);
        mav.addObject("createPlaylistRequest", new CreatePlaylistRequest(state, "", ""));
        return mav;
    }

    @PostMapping()
    public ModelAndView create(@Valid CreatePlaylistRequest request) {
        ModelAndView mav = new ModelAndView("spotify/playlist-created");;

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
