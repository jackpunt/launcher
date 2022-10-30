package com.thegraid.share.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.function.Supplier;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AuthUtils {

    static final Logger log = LoggerFactory.getLogger(AuthUtils.class);

    public static class AuthRequest {

        public final String username;
        public final String password;

        public AuthRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    /**
     * Object returned as body in JWT Authentication.
     */
    public static class JWTToken {

        private String idToken;

        public JWTToken() {}

        public JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }

    /**
     * obtain a JWT from the designated hostPort & path.
     * supplying given username/password
     *
     * @param hostPort "host.domain:port"
     * @param authPath "/api/auth"
     * @return the JWT (or null if login failed)
     */
    public static String loginToUrl(RestTemplate rest, String url, String username, String password) {
        AuthUtils.AuthRequest ar = new AuthRequest(username, password);
        try {
            JWTToken jwt = rest.postForObject(url, ar, JWTToken.class);
            String token = jwt.getIdToken();
            return token;
        } catch (RestClientException ex) {
            log.error("loginToHost: FAILED - " + ex.getMessage());
            return null;
        }
    }

    /** retrieve named Cookie from HttpServletRequest */
    public static String getCookieValue(String name, HttpServletRequest req) {
        final Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }

    public static class RestTemplateWithAuth extends RestTemplate {

        /**
         * inject Authorization header if getToken supplies non-null String.
         * @param getToken
         */
        public RestTemplateWithAuth(Supplier<String> getToken) {
            super();
            this.getInterceptors()
                .add((request, body, clientHttpRequestExecution) -> {
                    String jwt = getToken.get();
                    if (jwt != null && !request.getHeaders().containsKey("Authorization")) {
                        request.getHeaders().add("Authorization", "Bearer " + jwt);
                    }
                    return clientHttpRequestExecution.execute(request, body);
                });
        }
    }
}
