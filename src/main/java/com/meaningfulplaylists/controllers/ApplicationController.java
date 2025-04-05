package com.meaningfulplaylists.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class ApplicationController {

    @GetMapping
    public String home() {
        return "Hej! Välkommen Meaningful Playlists!";
    }

    @GetMapping("healthz")
    public String healthz() {
        return "Up and running";
    }
}
