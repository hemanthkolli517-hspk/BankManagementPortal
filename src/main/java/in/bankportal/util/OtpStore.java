package in.bankportal.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class OtpStore {
    private static final ConcurrentHashMap<String, Entry> STORE = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private OtpStore() {}
    public static String issue(String email) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        STORE.put(email.toLowerCase(), new Entry(otp, Instant.now().plusSeconds(600), 0));
        return otp;
    }
    public static boolean verify(String email, String otp) {
        Entry e = STORE.get(email.toLowerCase());
        if (e == null || Instant.now().isAfter(e.expiresAt) || e.attempts >= 5) {
            STORE.remove(email.toLowerCase()); return false;
        }
        if (!e.otp.equals(otp == null ? "" : otp.trim())) {
            STORE.put(email.toLowerCase(), new Entry(e.otp, e.expiresAt, e.attempts + 1)); return false;
        }
        STORE.remove(email.toLowerCase());
        return true;
    }
    private record Entry(String otp, Instant expiresAt, int attempts) {}
}
