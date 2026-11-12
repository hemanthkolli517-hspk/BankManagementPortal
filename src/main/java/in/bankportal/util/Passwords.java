package in.bankportal.util;
import org.mindrot.jbcrypt.BCrypt;
public final class Passwords {
    private Passwords() {}
    public static String hash(String password) { return BCrypt.hashpw(password, BCrypt.gensalt(12)); }
    public static boolean matches(String password, String hash) {
        try { return hash != null && BCrypt.checkpw(password, hash); }
        catch (IllegalArgumentException ex) { return false; }
    }
}
