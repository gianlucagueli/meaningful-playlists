package com.meaningfulplaylists.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "Application Controller")
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
