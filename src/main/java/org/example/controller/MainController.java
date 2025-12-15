package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class MainController {
    @GetMapping(value = {"/"})
    public String home() throws IOException {
        return "index.html";
    }
}