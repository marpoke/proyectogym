package proyectgymit.controler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Entrenadores;
import proyectgymit.model.Usuario;
import proyectgymit.security.PasswordUtil;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Item;

public class EntrenadoresC {

    public boolean registrarEntrenadorCompleto(Usuario usuario, Entrenadores entrenador) {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            int idUsuario = -1;
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, 'ENTRENADOR')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre().trim());
                ps.setString(2, usuario.getCorreo().trim());
                ps.setString(3, PasswordUtil.hash(usuario.getPassword()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) idUsuario = rs.getInt(1); }
            }
            if (idUsuario == -1) { BD.rollback(); return false; }
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO entrenadores (usuario_id, especialidad, telefono) VALUES (?, ?, ?)")) {
                ps.setInt(1, idUsuario);
                ps.setString(2, entrenador.getEspecialidad());
                ps.setString(3, entrenador.getTelefono());
                ps.executeUpdate();
            }
            BD.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar entrenador: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    public boolean actualizarEntrenador(int entrenadorId, String nombre, String correo, String especialidad, String telefono, String passwordNueva) {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        boolean cambia = passwordNueva != null && !passwordNueva.isEmpty();
        String sql = "UPDATE usuarios u JOIN entrenadores e ON e.usuario_id = u.id SET u.nombre = ?, u.correo = ?, e.especialidad = ?, e.telefono = ?"
                + (cambia ? ", u.password = ?" : "") + " WHERE e.id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nombre.trim());
            ps.setString(i++, correo.trim());
            ps.setString(i++, especialidad);
            ps.setString(i++, telefono);
            if (cambia) ps.setString(i++, PasswordUtil.hash(passwordNueva));
            ps.setInt(i, entrenadorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar entrenador: " + e.getMessage());
            return false;
        }
    }

    /** Elimina al entrenador; sus rutinas quedan sin entrenador. */
    public boolean eliminarEntrenador(int entrenadorId) {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            int usuarioId = -1;
            try (PreparedStatement ps = BD.prepareStatement("SELECT usuario_id FROM entrenadores WHERE id = ?")) {
                ps.setInt(1, entrenadorId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) usuarioId = rs.getInt(1); }
            }
            if (usuarioId == -1) { BD.rollback(); return false; }
            ejecutar(BD, "UPDATE rutinas SET entrenador_id = NULL WHERE entrenador_id = ?", entrenadorId);
            ejecutar(BD, "DELETE FROM entrenadores WHERE id = ?", entrenadorId);
            ejecutar(BD, "DELETE FROM usuarios WHERE id = ?", usuarioId);
            BD.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar entrenador: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    private void ejecutar(Connection BD, String sql, int id) throws SQLException {
        try (PreparedStatement ps = BD.prepareStatement(sql)) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    /** Filas: id, nombre, correo, especialidad, teléfono. */
    public List<Object[]> listarEntrenadoresTabla(String filtro) {
        Permisos.exigirLectura(Modulo.ENTRENADORES);
        List<Object[]> filas = new ArrayList<>();
        boolean filtra = filtro != null && !filtro.trim().isEmpty();
        String sql = "SELECT e.id, u.nombre, u.correo, e.especialidad, e.telefono FROM entrenadores e INNER JOIN usuarios u ON e.usuario_id = u.id "
                + (filtra ? "WHERE u.nombre LIKE ? OR u.correo LIKE ? OR e.especialidad LIKE ? OR e.telefono LIKE ? " : "") + "ORDER BY u.nombre";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            if (filtra) { String f = "%" + filtro.trim() + "%"; for (int i = 1; i <= 4; i++) ps.setString(i, f); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    public List<Item> listarEntrenadoresCombo() {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT e.id, u.nombre FROM entrenadores e INNER JOIN usuarios u ON e.usuario_id = u.id ORDER BY u.nombre";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Item(rs.getInt(1), rs.getString(2) + " (#" + rs.getInt(1) + ")"));
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    private Entrenadores mapear(ResultSet rs) throws SQLException {
        Entrenadores e = new Entrenadores();
        e.setId(rs.getInt("id"));
        e.setUsuarioID(rs.getInt("usuario_id"));
        e.setEspecialidad(rs.getString("especialidad"));
        e.setTelefono(rs.getString("telefono"));
        return e;
    }

    public List<Entrenadores> listarEntrenadores() {
        List<Entrenadores> lista = new ArrayList<>();
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, usuario_id, especialidad, telefono FROM entrenadores"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException b) { System.err.println("error:" + b.getMessage()); }
        return lista;
    }

    public Entrenadores buscarEntrenadorID(int id) { return uno("SELECT id, usuario_id, especialidad, telefono FROM entrenadores WHERE id = ?", id); }
    public Entrenadores buscarPorUsuarioId(int uid) { return uno("SELECT id, usuario_id, especialidad, telefono FROM entrenadores WHERE usuario_id = ?", uid); }

    private Entrenadores uno(String sql, int id) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException b) { System.err.println("error: " + b.getMessage()); }
        return null;
    }

    /** CORREGIDO: el SQL original no tenía espacios entre las partes ("telefonoFROM", "entrenadoresINNER"...) y siempre fallaba. */
    public List<Entrenadores> busquedaEntrenadoresDinamica(String txt) {
        List<Entrenadores> lista = new ArrayList<>();
        String sql = "SELECT e.id, e.usuario_id, e.especialidad, e.telefono FROM entrenadores e INNER JOIN usuarios u ON e.usuario_id = u.id "
                + "WHERE u.nombre LIKE ? OR u.correo LIKE ? OR e.telefono LIKE ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            String f = "%" + txt + "%";
            ps.setString(1, f); ps.setString(2, f); ps.setString(3, f);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.err.println("error: " + ex.getMessage()); }
        return lista;
    }

    public int contarEntrenadores() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM entrenadores"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return 0; }
    }
}
