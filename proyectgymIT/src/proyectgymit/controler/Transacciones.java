package proyectgymit.controler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Membresias;
import proyectgymit.model.Pagos;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;

/** Membresías y pagos. */
public class Transacciones {

    // ------------------------------------------------------------------ PAGO + MEMBRESIA
    public boolean procesarPagoYMembresia(Pagos pago, Membresias membresia) {
        Permisos.exigirEscritura(Modulo.PAGOS);
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO pagos (cliente_id, monto, metodo_pago, fecha_pago) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, pago.getClienteID());
                ps.setDouble(2, pago.getMonto());
                ps.setString(3, pago.getMetodoPago());
                ps.setDate(4, Date.valueOf(pago.getFechaPago() != null ? pago.getFechaPago() : LocalDate.now()));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO membresias (cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, membresia.getClienteID());
                ps.setString(2, membresia.getTipoPlan());
                ps.setDate(3, Date.valueOf(membresia.getFechaInicio()));
                ps.setDate(4, Date.valueOf(membresia.getFechaFinal()));
                ps.setString(5, membresia.getEstado());
                ps.executeUpdate();
            }
            BD.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al procesar pago y membresia: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    /** Membresía sin pago asociado (cortesía / pago registrado aparte). */
    public boolean registrarMembresia(Membresias m) {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("INSERT INTO membresias (cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, m.getClienteID());
            ps.setString(2, m.getTipoPlan());
            ps.setDate(3, Date.valueOf(m.getFechaInicio()));
            ps.setDate(4, Date.valueOf(m.getFechaFinal()));
            ps.setString(5, m.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    // ------------------------------------------------------------------ PAGOS
    public boolean registrarPago(Pagos p) {
        Permisos.exigirEscritura(Modulo.PAGOS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("INSERT INTO pagos (cliente_id, monto, metodo_pago, fecha_pago) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, p.getClienteID());
            ps.setDouble(2, p.getMonto());
            ps.setString(3, p.getMetodoPago());
            ps.setDate(4, Date.valueOf(p.getFechaPago() != null ? p.getFechaPago() : LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean actualizarPago(Pagos p) {
        Permisos.exigirEscritura(Modulo.PAGOS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE pagos SET cliente_id = ?, monto = ?, metodo_pago = ?, fecha_pago = ? WHERE id = ?")) {
            ps.setInt(1, p.getClienteID());
            ps.setDouble(2, p.getMonto());
            ps.setString(3, p.getMetodoPago());
            ps.setDate(4, Date.valueOf(p.getFechaPago()));
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean eliminarPago(int id) {
        Permisos.exigirEscritura(Modulo.PAGOS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("DELETE FROM pagos WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    /** Filas: id, id cliente, cliente, monto, método, fecha. Filtros opcionales (null = sin filtro). */
    public List<Object[]> listarPagosTabla(Integer clienteId, LocalDate desde, LocalDate hasta) {
        Permisos.exigirLectura(Modulo.PAGOS);
        return consultaPagos(clienteId, desde, hasta);
    }

    /** Para el reporte de caja (ADMIN). */
    public List<Object[]> listarPagosReporte() {
        Permisos.exigirLectura(Modulo.REPORTES);
        return consultaPagos(null, null, null);
    }

    private List<Object[]> consultaPagos(Integer clienteId, LocalDate desde, LocalDate hasta) {
        List<Object[]> filas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.id, p.cliente_id, u.nombre, p.monto, p.metodo_pago, p.fecha_pago FROM pagos p "
                + "INNER JOIN clientes c ON p.cliente_id = c.id INNER JOIN usuarios u ON c.usuario_id = u.id WHERE 1=1");
        if (clienteId != null) sql.append(" AND p.cliente_id = ?");
        if (desde != null) sql.append(" AND DATE(p.fecha_pago) >= ?");
        if (hasta != null) sql.append(" AND DATE(p.fecha_pago) <= ?");
        sql.append(" ORDER BY p.fecha_pago DESC, p.id DESC");
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql.toString())) {
            int i = 1;
            if (clienteId != null) ps.setInt(i++, clienteId);
            if (desde != null) ps.setDate(i++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(i++, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date f = rs.getDate(6);
                    filas.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5), f == null ? null : f.toLocalDate()});
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    public double totalIngresos() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COALESCE(SUM(monto),0) FROM pagos"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return 0; }
    }

    public List<Pagos> listarPagosCliente(int clienteId) {
        List<Pagos> lista = new ArrayList<>();
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, cliente_id, monto, metodo_pago, fecha_pago FROM pagos WHERE cliente_id = ? ORDER BY fecha_pago DESC")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapearPago(rs)); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Pagos buscarPagoPorId(int id) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, cliente_id, monto, metodo_pago, fecha_pago FROM pagos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapearPago(rs); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    private Pagos mapearPago(ResultSet rs) throws SQLException {
        Pagos p = new Pagos();
        p.setId(rs.getInt("id"));
        p.setClienteID(rs.getInt("cliente_id"));
        p.setMonto(rs.getDouble("monto"));
        p.setMetodoPago(rs.getString("metodo_pago"));
        Date f = rs.getDate("fecha_pago");
        p.setFechaPago(f == null ? null : f.toLocalDate());
        return p;
    }

    // ------------------------------------------------------------------ MEMBRESIAS
    /** Filas: id, id cliente, cliente, plan, inicio, fin, estado. */
    public List<Object[]> listarMembresiasTabla() {
        Permisos.exigirLectura(Modulo.MEMBRESIAS);
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT m.id, m.cliente_id, u.nombre, m.tipo_plan, m.fecha_inicio, m.fecha_fin, m.estado FROM membresias m "
                + "INNER JOIN clientes c ON m.cliente_id = c.id INNER JOIN usuarios u ON c.usuario_id = u.id ORDER BY m.fecha_fin DESC, m.id DESC";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDate(5).toLocalDate(), rs.getDate(6).toLocalDate(), rs.getString(7)});
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    public boolean actualizarMembresia(Membresias m) {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE membresias SET cliente_id = ?, tipo_plan = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id = ?")) {
            ps.setInt(1, m.getClienteID());
            ps.setString(2, m.getTipoPlan());
            ps.setDate(3, Date.valueOf(m.getFechaInicio()));
            ps.setDate(4, Date.valueOf(m.getFechaFinal()));
            ps.setString(5, m.getEstado());
            ps.setInt(6, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean eliminarMembresia(int id) {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("DELETE FROM membresias WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public Membresias buscarMembresiaActiva(int clienteId) {
        return buscarUna("SELECT id, cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado FROM membresias WHERE cliente_id = ? AND estado = 'ACTIVA' ORDER BY fecha_fin DESC LIMIT 1", clienteId);
    }
    /** Última membresía (activa o no), para el portal del socio. */
    public Membresias buscarUltimaMembresia(int clienteId) {
        return buscarUna("SELECT id, cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado FROM membresias WHERE cliente_id = ? ORDER BY fecha_fin DESC, id DESC LIMIT 1", clienteId);
    }
    public Membresias buscarMembresiaPorId(int id) {
        return buscarUna("SELECT id, cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado FROM membresias WHERE id = ?", id);
    }

    private Membresias buscarUna(String sql, int param) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Membresias m = new Membresias();
                    m.setId(rs.getInt("id"));
                    m.setClienteID(rs.getInt("cliente_id"));
                    m.setTipoPlan(rs.getString("tipo_plan"));
                    m.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    m.setFechaFinal(rs.getDate("fecha_fin").toLocalDate());
                    m.setEstado(rs.getString("estado"));
                    return m;
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    public boolean actualizarMembresiasVencidasAutomatico() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE membresias SET estado = 'VENCIDA' WHERE fecha_fin < CURRENT_DATE AND estado = 'ACTIVA'")) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    public boolean cambiarEstadoMembresiaManual(int idMembresia, String nuevoEstado) {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("UPDATE membresias SET estado = ? WHERE id = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMembresia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return false; }
    }

    // ------------------------------------------------------------------ CONTADORES
    public int contarMembresiasActivas() { return contar("SELECT COUNT(*) FROM membresias WHERE estado = 'ACTIVA' AND fecha_fin >= CURRENT_DATE"); }
    public int contarMembresiasVencidas() { return contar("SELECT COUNT(*) FROM membresias WHERE estado = 'VENCIDA' OR (estado = 'ACTIVA' AND fecha_fin < CURRENT_DATE)"); }
    public int contarMembresiasPorVencer() {
        return contar("SELECT COUNT(*) FROM membresias WHERE estado = 'ACTIVA' AND fecha_fin BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)");
    }

    private int contar(String sql) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { System.err.println("Error al contar: " + e.getMessage()); return 0; }
    }
}
