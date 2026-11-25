package proyectgymit.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 (incluido en el JDK, sin librerías externas).
 * Formato guardado en usuarios.password (VARCHAR(255)):
 *     pbkdf2_sha256$<iteraciones>$<salt Base64>$<hash Base64>
 * Cada contraseña usa su propio salt aleatorio; las iteraciones quedan guardadas junto al hash.
 */
public final class PasswordUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2_sha256";
    public static final int ITERACIONES = 600_000;   // recomendación OWASP para HMAC-SHA256
    private static final int BYTES_SALT = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final class Senuelo {
        static final String HASH = hash("senuelo-no-usar".toCharArray());
    }

    private PasswordUtil() {}

    public static String hash(char[] password) {
        byte[] salt = new byte[BYTES_SALT];
        RANDOM.nextBytes(salt);
        byte[] derivado = derivar(password, salt, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(derivado);
    }

    public static String hash(String password) {
        char[] chars = password.toCharArray();
        try { return hash(chars); } finally { Arrays.fill(chars, '\0'); }
    }

    public static boolean verificar(char[] password, String almacenado) {
        if (!esHashPBKDF2(almacenado)) return false;
        try {
            String[] p = almacenado.split("\\$");
            int iter = Integer.parseInt(p[1]);
            byte[] salt = Base64.getDecoder().decode(p[2]);
            byte[] esperado = Base64.getDecoder().decode(p[3]);
            return MessageDigest.isEqual(esperado, derivar(password, salt, iter)); // tiempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean verificar(String password, String almacenado) {
        char[] chars = password.toCharArray();
        try { return verificar(chars, almacenado); } finally { Arrays.fill(chars, '\0'); }
    }

    /** Gasta el mismo tiempo que una verificación real (evita enumerar correos existentes). */
    public static void verificarSenuelo(char[] password) { verificar(password, Senuelo.HASH); }

    public static boolean esHashPBKDF2(String v) {
        return v != null && v.startsWith(PREFIJO + "$") && v.split("\\$").length == 4;
    }

    /** true si usa menos iteraciones que las actuales o no es PBKDF2. */
    public static boolean requiereActualizacion(String almacenado) {
        if (!esHashPBKDF2(almacenado)) return true;
        try { return Integer.parseInt(almacenado.split("\\$")[1]) < ITERACIONES; }
        catch (NumberFormatException e) { return true; }
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en este JDK", e);
        } finally {
            spec.clearPassword();
        }
    }
}
