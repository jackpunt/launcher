package com.thegraid.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameCtl")
public class GameResource {

    @GetMapping("{giid}/login")
    @ResponseBody
    public ResponseEntity<String> login(@PathVariable(value = "giid", required = true) Long giid) {
        return ResponseEntity.ok("test done: " + giid);
    }
}
