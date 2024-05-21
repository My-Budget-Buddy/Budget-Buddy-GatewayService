package com.skillstorm.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class FallbackController {

    @GetMapping("/users")
    public String cachedUsers() {
        return "Retrieved cached user";
    }

    @GetMapping("/taxes")
    public String cachedTaxes() {
        return "Retrieved cached taxes";
    }
}
