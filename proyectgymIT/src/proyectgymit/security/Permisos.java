package proyectgymit.security;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Matriz de permisos por rol (cuadro de roles del proyecto).
 *              Dash  Socios Membr. Pagos  Asist. Entren. Ejerc. Rutinas Report. Usuarios Portal
 * ADMIN        Total L+E    L+E    L+E    L+E    L+E     L+E    L+E     L+E     L+E      Modo Admin
 * RECEPCION    Lect  L+E    L+E    L+E    L+E    -       -      -       -       -        -
 * ENTRENADOR   Lect  L+E    -      -      Lect   -       L+E    L+E     -       -        -
 * CLIENTE      -     -      -      -      -      -       -      -       -       -        Solo propio + coach
 */
public final class Permisos {

    public enum Modulo { DASHBOARD, SOCIOS, MEMBRESIAS, PAGOS, ASISTENCIA, ENTRENADORES, EJERCICIOS, RUTINAS, REPORTES, USUARIOS, PORTAL_SOCIO }
    public enum Nivel { BLOQUEADO, LECTURA, LECTURA_ESCRITURA }

    public static final String ADMIN = "ADMIN", RECEPCION = "RECEPCION", ENTRENADOR = "ENTRENADOR", CLIENTE = "CLIENTE";

    private static final Map<String, Map<Modulo, Nivel>> MATRIZ = new HashMap<>();

    static {
        Map<Modulo, Nivel> admin = new EnumMap<>(Modulo.class);
        for (Modulo m : Modulo.values()) admin.put(m, Nivel.LECTURA_ESCRITURA);
        MATRIZ.put(ADMIN, admin);

        Map<Modulo, Nivel> rec = base();
        rec.put(Modulo.DASHBOARD, Nivel.LECTURA);
        rec.put(Modulo.SOCIOS, Nivel.LECTURA_ESCRITURA);
        rec.put(Modulo.MEMBRESIAS, Nivel.LECTURA_ESCRITURA);
        rec.put(Modulo.PAGOS, Nivel.LECTURA_ESCRITURA);
        rec.put(Modulo.ASISTENCIA, Nivel.LECTURA_ESCRITURA);
        MATRIZ.put(RECEPCION, rec);

        Map<Modulo, Nivel> ent = base();
        ent.put(Modulo.DASHBOARD, Nivel.LECTURA);
        ent.put(Modulo.SOCIOS, Nivel.LECTURA_ESCRITURA);
        ent.put(Modulo.ASISTENCIA, Nivel.LECTURA);
        ent.put(Modulo.EJERCICIOS, Nivel.LECTURA_ESCRITURA);
        ent.put(Modulo.RUTINAS, Nivel.LECTURA_ESCRITURA);
        MATRIZ.put(ENTRENADOR, ent);

        Map<Modulo, Nivel> cli = base();
        cli.put(Modulo.PORTAL_SOCIO, Nivel.LECTURA);   // solo sus datos + su coach
        MATRIZ.put(CLIENTE, cli);
    }

    private Permisos() {}

    private static Map<Modulo, Nivel> base() {
        Map<Modulo, Nivel> m = new EnumMap<>(Modulo.class);
        for (Modulo x : Modulo.values()) m.put(x, Nivel.BLOQUEADO);
        return m;
    }

    public static Nivel nivel(String rol, Modulo modulo) {
        if (rol == null) return Nivel.BLOQUEADO;
        Map<Modulo, Nivel> fila = MATRIZ.get(rol.trim().toUpperCase());
        return fila == null ? Nivel.BLOQUEADO : fila.get(modulo);
    }
    public static boolean puedeVer(String rol, Modulo m) { return nivel(rol, m) != Nivel.BLOQUEADO; }
    public static boolean puedeEscribir(String rol, Modulo m) { return nivel(rol, m) == Nivel.LECTURA_ESCRITURA; }
    public static boolean puedeVer(Modulo m) { return puedeVer(Sesion.getRol(), m); }
    public static boolean puedeEscribir(Modulo m) { return puedeEscribir(Sesion.getRol(), m); }

    /** Defensa en profundidad: los controladores la llaman antes de modificar/leer datos protegidos. */
    public static void exigirEscritura(Modulo m) {
        if (!puedeEscribir(m)) throw new SecurityException("El rol " + Sesion.getRol() + " no tiene permiso de escritura en " + m);
    }
    public static void exigirLectura(Modulo m) {
        if (!puedeVer(m)) throw new SecurityException("El rol " + Sesion.getRol() + " no tiene acceso a " + m);
    }
}
