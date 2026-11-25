package proyectgymit.controler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Asistencias;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;

public class AsistenciasC {

    /** @return null si se registró; si no, el motivo del rechazo. */
    public String registrarAsistencia(int idCliente) {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        try (Connection BD = conexionbd.getConexion()) {
            try (PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM clientes WHERE id = ?")) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) == 0) return "No existe un cliente con el id " + idCliente + "."; }
            }
            try (PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM membresias WHERE cliente_id = ? AND estado = 'ACTIVA' AND fecha_fin >= CURRENT_DATE")) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) == 0) return "El cliente no tiene una membresía activa y vigente."; }
            }
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO asistencias (cliente_id) VALUES (?)")) {
                ps.setInt(1, idCliente);
                return ps.executeUpdate() > 0 ? null : "No se pudo registrar la asistencia.";
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return "Error de base de datos al registrar la asistencia.";
        }
    }

    /** Filas: id, id cliente, nombre, fecha/hora (500 más recientes). */
    public List<Object[]> listarAsistenciasTabla() {
        Permisos.exigirLectura(Modulo.ASISTENCIA);
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT a.id, a.cliente_id, u.nombre, a.fecha_entrada FROM asistencias a INNER JOIN clientes c ON a.cliente_id = c.id "
                + "INNER JOIN usuarios u ON c.usuario_id = u.id ORDER BY a.fecha_entrada DESC LIMIT 500";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getTimestamp(4).toLocalDateTime()});
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    public boolean actualizarAsistencia(int id, int clienteId, LocalDateTime fecha) {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE asistencias SET cliente_id = ?, fecha_entrada = ? WHERE id = ?")) {
            ps.setInt(1, clienteId);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean eliminarAsistencia(int id) {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("DELETE FROM asistencias WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public List<Asistencias> listarAsistenciasCliente(int clienteId) {
        List<Asistencias> lista = new ArrayList<>();
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, cliente_id, fecha_entrada FROM asistencias WHERE cliente_id = ? ORDER BY fecha_entrada DESC")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asistencias a = new Asistencias();
                    a.setId(rs.getInt("id"));
                    a.setClienteID(rs.getInt("cliente_id"));
                    Timestamp t = rs.getTimestamp("fecha_entrada");
                    if (t != null) a.setFechaEntrada(t.toLocalDateTime());
                    lista.add(a);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    /** CORREGIDO: la consulta original usaba la columna "fecha", que no existe (es fecha_entrada). */
    public int contarAsistenciasHoy() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM asistencias WHERE DATE(fecha_entrada) = CURRENT_DATE"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { System.err.println("Error al contar asistencias: " + e.getMessage()); return 0; }
    }
}
