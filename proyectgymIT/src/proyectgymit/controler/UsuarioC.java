package proyectgymit.controler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Usuario;
import proyectgymit.security.PasswordUtil;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;

/** Usuarios del sistema. Las contraseñas se guardan con PBKDF2 (PasswordUtil). */
public class UsuarioC {

    public Usuario iniciarSesion(String correo, String password) {
        return iniciarSesion(correo, password.toCharArray());
    }

    public Usuario iniciarSesion(String correo, char[] password) {
        String sql = "SELECT id, nombre, correo, rol, password FROM usuarios WHERE correo = ? LIMIT 1";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, correo == null ? "" : correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    PasswordUtil.verificarSenuelo(password);
                    return null;
                }
                String almacenado = rs.getString("password");
                int id = rs.getInt("id");
                boolean ok, migrar;
                if (PasswordUtil.esHashPBKDF2(almacenado)) {
                    ok = PasswordUtil.verificar(password, almacenado);
                    migrar = ok && PasswordUtil.requiereActualizacion(almacenado);
                } else {
                    // Cuenta antigua en texto plano: se acepta una vez y se convierte a PBKDF2.
                    ok = almacenado != null && MessageDigest.isEqual(
                            almacenado.getBytes(StandardCharsets.UTF_8), new String(password).getBytes(StandardCharsets.UTF_8));
                    migrar = ok;
                }
                if (!ok) return null;
                if (migrar) {
                    try (PreparedStatement up = BD.prepareStatement("UPDATE usuarios SET password = ? WHERE id = ?")) {
                        up.setString(1, PasswordUtil.hash(password));
                        up.setInt(2, id);
                        up.executeUpdate();
                    }
                }
                Usuario u = new Usuario();
                u.setId(id);
                u.setNombre(rs.getString("nombre"));
                u.setCorreo(rs.getString("correo"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesion: " + e.getMessage());
            return null;
        }
    }

    public boolean existeCorreo(String correo, int excluirUsuarioId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE correo = ? AND id <> ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            ps.setInt(2, excluirUsuarioId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return true;
        }
    }

    public boolean registrarPersonal(Usuario p) {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        String sql = "INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, ?)";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, p.getNombre().trim());
            ps.setString(2, p.getCorreo().trim());
            ps.setString(3, PasswordUtil.hash(p.getPassword()));
            ps.setString(4, p.getRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar personal: " + e.getMessage());
            return false;
        }
    }

    /** Actualiza nombre, correo y rol; si passwordNueva no es vacía, también la contraseña. */
    public boolean actualizarUsuario(Usuario u, String passwordNueva) {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        boolean cambia = passwordNueva != null && !passwordNueva.isEmpty();
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, rol = ?" + (cambia ? ", password = ?" : "") + " WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getNombre().trim());
            ps.setString(i++, u.getCorreo().trim());
            ps.setString(i++, u.getRol());
            if (cambia) ps.setString(i++, PasswordUtil.hash(passwordNueva));
            ps.setInt(i, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /** Solo ADMIN/RECEPCION; clientes y entrenadores se eliminan en sus módulos. */
    public boolean eliminarPersonal(int idUsuario) {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        String sql = "DELETE FROM usuarios WHERE id = ? AND rol IN ('ADMIN','RECEPCION')";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public int contarPorRol(String rol) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE rol = ?")) {
            ps.setString(1, rol);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return 0;
        }
    }

    public List<Usuario> listarUsuarios() { return buscar(null); }
    public List<Usuario> busquedaUsuarioDinamica(String t) { return buscar(t); }

    private List<Usuario> buscar(String texto) {
        Permisos.exigirLectura(Modulo.USUARIOS);
        List<Usuario> lista = new ArrayList<>();
        boolean filtra = texto != null && !texto.trim().isEmpty();
        String sql = "SELECT id, nombre, correo, rol FROM usuarios" + (filtra ? " WHERE nombre LIKE ? OR correo LIKE ? OR rol LIKE ?" : "") + " ORDER BY nombre";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            if (filtra) { String f = "%" + texto.trim() + "%"; ps.setString(1, f); ps.setString(2, f); ps.setString(3, f); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id")); u.setNombre(rs.getString("nombre")); u.setCorreo(rs.getString("correo")); u.setRol(rs.getString("rol"));
                    lista.add(u);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Usuario buscarUsuarioPorId(int id) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, nombre, correo, rol FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id")); u.setNombre(rs.getString("nombre")); u.setCorreo(rs.getString("correo")); u.setRol(rs.getString("rol"));
                    return u;
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    /** true solo si la consulta funcionó y no existe ningún ADMIN (primer arranque). */
    public boolean necesitaPrimerAdmin() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /** Crea el administrador inicial. Solo funciona mientras no exista ningún ADMIN. */
    public boolean crearPrimerAdmin(String nombre, String correo, char[] password) {
        if (!necesitaPrimerAdmin()) return false;
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, 'ADMIN')")) {
            ps.setString(1, nombre.trim());
            ps.setString(2, correo.trim());
            ps.setString(3, PasswordUtil.hash(password));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear el administrador: " + e.getMessage());
            return false;
        }
    }
}
