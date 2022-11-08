package com.thegraid.web.rest;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import com.thegraid.security.AuthoritiesConstants;
import com.thegraid.share.auth.AuthUtils;
import com.thegraid.share.auth.TicketService.GameTicketService;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/gameCtl")
public class GameResource {

    private static final Logger log = LoggerFactory.getLogger(GameResource.class);

    @Value("${thegraid.lobbyUrl}")
    String lobbyUrl;

    @Autowired
    GameTicketService gameTicketService;

    @GetMapping("{giid}/login")
    @ResponseBody
    public RedirectView login(
        @PathVariable(value = "giid", required = true) Long giid,
        @RequestParam("P") String p, // hash
        @RequestParam("T") String t, // timelimit
        @RequestParam("U") String u, // user.loginId
        @RequestParam("V") String v, // gpid [giid, gamePlayer -> {role,display_client}]
        HttpServletRequest request
    ) {
        final String qsf = "gi/afterLogin/%s?P=%s&T=%s&U=%s&V=%s";
        final String rvf = "{valid: %s, giid: %s, user: %s, gpid: %s, JSESSIONID: %s}";
        final String jsessions = AuthUtils.getCookieValue("JSESSIONID", request);
        final boolean isValid = gameTicketService.validateTicket(p, t, u, v, jsessions);
        if (isValid) loginAs(u, request);
        String rv = String.format(rvf, isValid, giid, u, v, jsessions);
        //return ResponseEntity.ok(rv);
        String rdv = lobbyUrl + String.format(qsf, isValid, p, t, u, v);
        log.info("afterLogin: " + rv + "\n" + rdv);
        return new RedirectView(rdv);
    }

    /* @return */
    private Authentication loginAs(String loginid, HttpServletRequest request) {
        Authentication authToken0 = SecurityContextHolder.getContext().getAuthentication();
        // are we *already* logged-in as member?
        if (loginid.equals(authToken0.getName())) {
            final String jsessions = AuthUtils.getCookieValue("JSESSIONID", request);
            log.debug("loginAs: already \'{}\' JSESSIONID= {}", loginid, jsessions);
            return authToken0;
        }
        // create AuthenticationToken with ROLE_USER
        String credential = "fromToken";
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Authentication authToken = new UsernamePasswordAuthenticationToken(loginid, credential, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.getSession(true).setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        log.debug("loginAs: loginid={}", loginid);
        return authToken; // = SecurityContextHolder.getContext().getAuthentication();
    }

    @GetMapping("{giid}")
    @ResponseBody
    public ResponseEntity<String> gameCtl(@PathVariable(value = "giid", required = true) Long giid, HttpServletRequest request) {
        String jsessions = AuthUtils.getCookieValue("JSESSIONID", request);
        return ResponseEntity.ok("giid" + ": " + giid + " jsessions: " + jsessions);
    }
}
