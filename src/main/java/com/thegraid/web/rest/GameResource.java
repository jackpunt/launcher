package com.thegraid.web.rest;

import com.thegraid.share.auth.TicketService.GameTicketService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameCtl")
public class GameResource {

    @Autowired
    GameTicketService gameTicketService;

    @GetMapping("{giid}/login")
    @ResponseBody
    public ResponseEntity<String> login(
        @PathVariable(value = "giid", required = true) Long giid,
        @RequestParam("P") String p,
        @RequestParam("T") String t,
        @RequestParam("U") String u,
        @RequestParam("V") String v,
        HttpServletRequest request
    ) {
        String jsessions = gameTicketService.getCookieValue("JSESSIONID", request);
        String isValid = gameTicketService.validateTicket(p, t, u, v, jsessions) ? "valid" : "invalid";
        return ResponseEntity.ok("ticket " + isValid + ": " + giid);
    }
}
