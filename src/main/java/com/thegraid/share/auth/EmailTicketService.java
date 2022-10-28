package com.thegraid.share.auth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Validate Ticket using User and BCryptSaltSource for Key. */
@Service
public class EmailTicketService extends TicketService {

    /** P=(40-digit Token)(&T=(11-xDigit timelimit)&U=(username)&...) */
    private final Pattern pat = Pattern.compile("P=(?<P>\\p{XDigit}{40,})(?<raw2>&T=(?<T>\\p{XDigit}{11,})&U=(?<U>[^&]+))(?<rest>&.*)?$");

    /**
     * Create an email-able hash token encapsulating login credentials.
     * Create ticket for *this* user, include additional info in token:
     * passwordUserDetails.getEmailTicket(time, "V", iview.getId(), "W",
     * job.getId())
     *
     * @param user      the Member to login (so we can getSalt/secret)
     * @param validTime deadlne for ticket to be valid
     * @param args      &amp;"key"="value" to be included in ticket
     * @return a String that can be validated by NoFormLoginDecoder.
     */
    public String getTicket(String username, String bcpw, long validTime, String... args) {
        // do not modify pwh or salt if already exists
        Long genSalt = getSaltFromUser(bcpw); // assert(genSalt != null)
        return super.getGenericTicket(username, genSalt, validTime, args);
    }

    public boolean validateTicket(CharSequence raw, Long salt) {
        return super.validateTicket(pat, raw, salt);
    }

    public Ticket parseTicket(String query) {
        return new Ticket(pat, query);
    }

    private final Long no_salt = 5345909788840343976L; // or 0 or null I suppose
    private final BCryptSaltSource bcss = new BCryptSaltSource();

    public Long getSaltFromUser(String bcpw) {
        if (bcpw.length() < 60) return no_salt;
        // 0:{bcrypt}, 1:2a, 2:12, 3:salt+passwd
        String salt22 = bcpw.split("\\$", 4)[3].substring(0, 22);
        return bcss.decodeSalt(salt22);
    }

    /** decodeSalt using BCrypt special Base64 coding. */
    static class BCryptSaltSource {

        long decodeSalt(String salt22) {
            String base64Salt = utilStringTr(salt22, CHARS_BCRYPT, CHARS_BASE64);
            byte[] bytes = Base64.getDecoder().decode(base64Salt); // 8 bytes
            return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
        }

        long byteToLong(byte[] bytes) {
            return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
        }

        byte[] longToBytes(long number) {
            return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(number).array();
        }

        /**
         * BCrypt base64 encoding alphabet
         */
        static String CHARS_BCRYPT = "./ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        /**
         * RFC-4648 base64 encoding alphabet
         */
        static String CHARS_BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        /** translate chars of str from xin to xout */
        String utilStringTr(String str, String xin, String xout) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                int ndx = xin.indexOf(str.substring(i, i + 1));
                sb.append(xout.substring(ndx, ndx + 1));
            }
            return sb.toString();
        }
    }
}
