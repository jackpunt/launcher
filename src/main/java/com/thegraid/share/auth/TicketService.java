package com.thegraid.share.auth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** common code with gammaJH and launcher. */
public class TicketService {

    protected static final Logger log = LoggerFactory.getLogger(TicketService.class);

    /**
     * create a time-limited, validate-able String, with labeled properties.
     * [implicit arg: username]
     * @param username asserts the username
     * @param salt either the user.salt OR salt(UUID=JSESSIONID) ?
     * @param validTime token is invalid after validTime
     * @param args "Key Value ..."
     * @return "P=signature,T=dddd,U=username,...""
     */
    // 2 cases: salt from JSESSIONID/UUID [EmailTicket] -or- salt from member.getSalt() [GameTicket]
    protected String getGenericTicket(String username, Long salt, long validTime, String... args) {
        // compose: ^P=(1)&(T=(ddd)&U=(username)&...)$
        // String username = user.getLogin();
        Long timelimit = new Date().getTime() + validTime;
        StringBuilder sb = new StringBuilder();
        sb.append("&T=").append(Long.toHexString(timelimit));
        sb.append("&U=").append(username);
        for (int ndx = 0; ndx < args.length; ndx += 2) {
            sb.append("&").append(args[ndx]).append("=").append(args[ndx + 1]);
        }
        String raw = sb.toString();
        String token = signature(raw, salt); // encode ticket with theSalt
        return "P=" + token + raw;
    }

    /** get Signature/hash for given text + salt. */
    private String signature(String text, Long salt) {
        String algo = "SHA-256"; // or SHA-512?
        try {
            MessageDigest digest = MessageDigest.getInstance(algo);
            byte[] text_bytes = text.getBytes(StandardCharsets.UTF_8);
            byte[] salt_bytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(salt).array();
            byte[] hash = digest.digest(concatBytes(salt_bytes, text_bytes));
            String rv = Base64.getUrlEncoder().encodeToString(hash);
            return rv;
        } catch (NoSuchAlgorithmException ex) {
            log.error("Error: {}", ex);
            return ""; // only if algo not found
        }
    }

    private byte[] concatBytes(byte[] bytes1, byte[] bytes2) {
        int len = bytes1.length + bytes2.length;
        byte[] all_bytes = Arrays.copyOf(bytes1, len);
        System.arraycopy(bytes2, 0, all_bytes, bytes1.length, bytes2.length);
        return all_bytes;
    }

    public static class Ticket {

        public boolean match = false;
        public String token; // @RequestParam("P")
        public String raw2; // (&T=...&U=...)
        public String tls; // @RequestParam("T")
        public String user; // @RequestParam("U")
        public String rest; // @RequestParam("V") or whatever

        protected Ticket(Pattern pat, CharSequence raw) {
            if (raw == null) return;
            Matcher matcher = pat.matcher(raw);
            match = matcher.matches();
            if (!match) return;
            token = matcher.group("P"); // P=encode(raw2,theSalt)
            raw2 = matcher.group("raw2"); // raw2=(&T=...&U=...)
            tls = matcher.group("T"); // T=expiryTime
            user = matcher.group("U"); // U=loginId [logged and validated in hash]
            rest = matcher.group("rest"); // possibly null or empty
        }
    }

    Boolean validateTicket(Pattern pat, CharSequence raw, Long salt) {
        log.trace("pat={}", pat);
        log.trace("raw={}", raw);
        Ticket ticket = new Ticket(pat, raw);
        if (!ticket.match) {
            log.debug("not a ticket: {}", raw.length());
            return null;
        }

        String hash = signature(ticket.raw2, salt);
        if (!hash.equals(ticket.token)) {
            log.trace(String.format("hash=%s,token=%s,raw2=%s\n", hash, ticket.token, ticket.raw2));
            log.debug("TicketService.validateTicket={}, user={}", "(corrupt)", ticket.user);
            return false; // corrupt data: throw new BadCredentialsException("") ?
        }
        long timelimit = Long.parseLong(ticket.tls, 16);
        if (new Date().getTime() > timelimit) {
            log.debug("TicketService.validateTicket={}, user={}", "(expired)", ticket.user);
            //throw new CredentialsExpiredException("User can try again with a new token");
            return false;
        }
        log.debug("TicketService.validateTicket={}, user={}", true, ticket.user);
        return true;
    }

    public String getCookieValue(String name, HttpServletRequest req) {
        final Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }

    /** suitable for using a random String (JSESSIONID) as a per-user/session Salt
     * @param jsessionid an unguessable String
     * @return Long hash of derived from jesssionid
     */
    public Long getSaltFromJSession(String jsessionid) {
        String s1 = jsessionid.substring(0, jsessionid.length() / 2 - 1);
        String s2 = jsessionid.substring(jsessionid.length() / 2);
        int h1 = s1.hashCode();
        int h2 = s2.hashCode();
        String lstr = Integer.toUnsignedString(h1) + Integer.toUnsignedString(h2);
        Long salt = null;
        while (salt == null) {
            try {
                salt = Long.parseUnsignedLong(lstr);
            } catch (NumberFormatException ex) {
                lstr = lstr.substring(1);
            }
        }
        log.trace("getSaltFromJSession: {} --> {}", jsessionid, salt);
        return salt;
    }

    /** Validate Ticket using JSESSIONID as key. */
    @Service
    public static class GameTicketService extends TicketService {

        /**
         * P=(?<P>[a-zA-Z0-9_-]*)(?<raw2>&T=(?<T>\\p{XDigit}{11,})&U=(?<U>[^&]+)&V=(\\d{1,19}))(?<rest>&.*)?$
         */
        private final Pattern pat = Pattern.compile(
            "P=(?<P>[a-zA-Z0-9=_-]*)(?<raw2>&T=(?<T>\\p{XDigit}{11,})&U=(?<U>[^&]+)(?<rest>&V=(\\d{1,19}).*))?$"
        );

        /**
         * Create query string Ticket for DisplayClient to authenticate to game server.
         *
         * @param username  include user.loginid
         * @param validTime deadlne for ticket to be valid
         * @param gpid      the long id (not null, not 0L)
         * @param jsessions [unguessable] String that is shared with the GameServer.
         * @return Ticket signed with uuids
         */
        public String getTicket(String username, Long validTime, Long gpid, String jsessions) {
            return getTicket(username, validTime, gpid, getSaltFromJSession(jsessions));
        }

        // export this if there is some other salt to share with the validating side
        private String getTicket(String username, Long validTime, Long gpid, Long salt) {
            return super.getGenericTicket(username, salt, validTime, "V", gpid.toString());
        }

        public boolean validateTicket(String p, String t, String u, String v, String jsessions) {
            String raw = String.format("P=%s&T=%s&U=%s&V=%s", p, t, u, v);
            Long salt = getSaltFromJSession(jsessions);
            return validateTicket(raw, salt);
        }

        public boolean validateTicket(CharSequence raw, Long salt) {
            return super.validateTicket(pat, raw, salt);
        }

        public Ticket parseTicket(String query) {
            return new Ticket(pat, query);
        }
    }
}
