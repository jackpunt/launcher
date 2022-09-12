package com.thegraid.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LauncherResource controller
 */
@RestController
@RequestMapping("/api/launcher")
public class LauncherResource {

    private final Logger log = LoggerFactory.getLogger(LauncherResource.class);

    /**
     * POST launch
     */
    @PostMapping("/launch")
    public String launch() {
        return "launch";
    }
}
