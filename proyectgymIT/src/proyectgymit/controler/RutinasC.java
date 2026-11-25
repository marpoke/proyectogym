package proyectgymit.controler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Rutinas;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;

public class RutinasC {

    public int crearRutina(Rutinas rutina) {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        int id = -1;
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement("INSERT INTO rutinas (cliente_id, entrenador_id, nombre_rutina) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rutina.getClienteID());
            if (rutina.getEntrenadorID() != null) ps.setInt(2, rutina.getEntrenadorID()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, rutina.getNombreRutina());
            if (ps.executeUpdate() > 0) try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) id = rs.getInt(1); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return id;
    }

    public boolean actualizarRutina(Rutinas r) {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE rutinas SET cliente_id = ?, entrenador_id = ?, nombre_rutina = ? WHERE id = ?")) {
            ps.setInt(1, r.getClienteID());
            if (r.getEntrenadorID() != null) ps.setInt(2, r.getEntrenadorID()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, r.getNombreRutina());
            ps.setInt(4, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean eliminarRutina(int id) {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            try (PreparedStatement ps = BD.prepareStatement("DELETE FROM rutina_ejercicio WHERE rutina_id = ?")) { ps.setInt(1, id); ps.executeUpdate(); }
            int n;
            try (PreparedStatement ps = BD.prepareStatement("DELETE FROM rutinas WHERE id = ?")) { ps.setInt(1, id); n = ps.executeUpdate(); }
            BD.commit();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    /** Filas: id, id cliente, cliente, id entrenador (null si no hay), entrenador, rutina, # ejercicios. */
    public List<Object[]> listarRutinasTabla() {
        Permisos.exigirLectura(Modulo.RUTINAS);
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT r.id, r.cliente_id, uc.nombre, r.entrenador_id, ue.nombre, r.nombre_rutina, "
                + "(SELECT COUNT(*) FROM rutina_ejercicio re WHERE re.rutina_id = r.id) "
                + "FROM rutinas r INNER JOIN clientes c ON r.cliente_id = c.id INNER JOIN usuarios uc ON c.usuario_id = uc.id "
                + "LEFT JOIN entrenadores e ON r.entrenador_id = e.id LEFT JOIN usuarios ue ON e.usuario_id = ue.id ORDER BY r.id DESC";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int ent = rs.getInt(4);
                Integer entId = rs.wasNull() ? null : ent;
                String entNombre = rs.getString(5);
                filas.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), entId, entNombre == null ? "Sin asignar" : entNombre, rs.getString(6), rs.getInt(7)});
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    /** Portal del socio: filas {rutina, ejercicio, series, repeticiones} de ese cliente. */
    public List<Object[]> listarDetalleRutinasCliente(int clienteId) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT r.nombre_rutina, e.nombre, re.series, re.repeticiones FROM rutinas r LEFT JOIN rutina_ejercicio re ON re.rutina_id = r.id "
                + "LEFT JOIN ejercicios e ON re.ejercicio_id = e.id WHERE r.cliente_id = ? ORDER BY r.id DESC, re.id";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ej = rs.getString(2);
                    filas.add(new Object[]{rs.getString(1), ej == null ? "(sin ejercicios)" : ej, ej == null ? "" : rs.getInt(3), ej == null ? "" : rs.getInt(4)});
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    /** Coach actual = entrenador de la rutina más reciente. {id, nombre, especialidad} o null. */
    public String[] obtenerEntrenadorDeCliente(int clienteId) {
        String sql = "SELECT e.id, u.nombre, e.especialidad FROM rutinas r INNER JOIN entrenadores e ON r.entrenador_id = e.id "
                + "INNER JOIN usuarios u ON e.usuario_id = u.id WHERE r.cliente_id = ? ORDER BY r.id DESC LIMIT 1";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new String[]{rs.getString(1), rs.getString(2), rs.getString(3)}; }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    /** Solo ADMIN (modo admin del portal). Reasigna las rutinas del cliente o crea una inicial. */
    public boolean asignarEntrenador(int clienteId, int entrenadorId) {
        Permisos.exigirEscritura(Modulo.PORTAL_SOCIO);
        try (Connection BD = conexionbd.getConexion()) {
            int n;
            try (PreparedStatement ps = BD.prepareStatement("UPDATE rutinas SET entrenador_id = ? WHERE cliente_id = ?")) {
                ps.setInt(1, entrenadorId); ps.setInt(2, clienteId); n = ps.executeUpdate();
            }
            if (n == 0) {
                try (PreparedStatement ps = BD.prepareStatement("INSERT INTO rutinas (cliente_id, entrenador_id, nombre_rutina) VALUES (?, ?, 'Rutina inicial')")) {
                    ps.setInt(1, clienteId); ps.setInt(2, entrenadorId); n = ps.executeUpdate();
                }
            }
            return n > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public List<Rutinas> listarRutinasPorCliente(int clienteId) {
        List<Rutinas> lista = new ArrayList<>();
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, cliente_id, entrenador_id, nombre_rutina FROM rutinas WHERE cliente_id = ?")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Rutinas buscarRutinaPorId(int id) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, cliente_id, entrenador_id, nombre_rutina FROM rutinas WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    private Rutinas mapear(ResultSet rs) throws SQLException {
        Rutinas r = new Rutinas();
        r.setId(rs.getInt("id"));
        r.setClienteID(rs.getInt("cliente_id"));
        int e = rs.getInt("entrenador_id");
        r.setEntrenadorID(rs.wasNull() ? null : e);
        r.setNombreRutina(rs.getString("nombre_rutina"));
        return r;
    }
}
