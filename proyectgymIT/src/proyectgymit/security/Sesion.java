package proyectgymit.security;

import proyectgymit.model.Usuario;

/** Usuario autenticado actualmente en la aplicación. */
public final class Sesion {
    private static Usuario usuario;
    private Sesion() {}
    public static void iniciar(Usuario u) { usuario = u; }
    public static void cerrar() { usuario = null; }
    public static Usuario getUsuario() { return usuario; }
    public static boolean activa() { return usuario != null; }
    public static String getRol() { return usuario == null ? null : usuario.getRol(); }
}
