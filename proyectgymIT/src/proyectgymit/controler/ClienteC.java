package proyectgymit.controler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Clientes;
import proyectgymit.model.Membresias;
import proyectgymit.model.Pagos;
import proyectgymit.model.Usuario;
import proyectgymit.security.PasswordUtil;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Item;

public class ClienteC {

    public boolean registrarClienteCompleto(Usuario usuario, Clientes cliente) {
        return registrarClienteCompleto(usuario, cliente, null, null);
    }

    /** Crea usuario + cliente (+ primera membresía y pago opcionales) en UNA transacción. */
    public boolean registrarClienteCompleto(Usuario usuario, Clientes cliente, Membresias membresia, Pagos pago) {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        if (membresia != null) Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        if (pago != null) Permisos.exigirEscritura(Modulo.PAGOS);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            int idUsuario;
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, 'CLIENTE')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre().trim());
                ps.setString(2, usuario.getCorreo().trim());
                ps.setString(3, PasswordUtil.hash(usuario.getPassword()));
                ps.executeUpdate();
                idUsuario = clave(ps);
            }
            int idCliente;
            try (PreparedStatement ps = BD.prepareStatement("INSERT INTO clientes (usuario_id, telefono, contacto_emergencia) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, cliente.getTelefono());
                ps.setString(3, cliente.getContactoEmergencia());
                ps.executeUpdate();
                idCliente = clave(ps);
            }
            if (membresia != null) {
                try (PreparedStatement ps = BD.prepareStatement("INSERT INTO membresias (cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, idCliente);
                    ps.setString(2, membresia.getTipoPlan());
                    ps.setDate(3, java.sql.Date.valueOf(membresia.getFechaInicio()));
                    ps.setDate(4, java.sql.Date.valueOf(membresia.getFechaFinal()));
                    ps.setString(5, membresia.getEstado());
                    ps.executeUpdate();
                }
            }
            if (pago != null) {
                try (PreparedStatement ps = BD.prepareStatement("INSERT INTO pagos (cliente_id, monto, metodo_pago) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idCliente);
                    ps.setDouble(2, pago.getMonto());
                    ps.setString(3, pago.getMetodoPago());
                    ps.executeUpdate();
                }
            }
            BD.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    private int clave(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        throw new SQLException("No se obtuvo el id generado");
    }

    /** passwordNueva vacío/nulo = no cambiar contraseña. */
    public boolean actualizarCliente(int clienteId, String nombre, String correo, String telefono, String emergencia, String passwordNueva) {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        boolean cambia = passwordNueva != null && !passwordNueva.isEmpty();
        String sql = "UPDATE usuarios u JOIN clientes c ON c.usuario_id = u.id SET u.nombre = ?, u.correo = ?, c.telefono = ?, c.contacto_emergencia = ?"
                + (cambia ? ", u.password = ?" : "") + " WHERE c.id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nombre.trim());
            ps.setString(i++, correo.trim());
            ps.setString(i++, telefono);
            ps.setString(i++, emergencia);
            if (cambia) ps.setString(i++, PasswordUtil.hash(passwordNueva));
            ps.setInt(i, clienteId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    /** Elimina al cliente y todo su historial en una transacción. */
    public boolean eliminarCliente(int clienteId) {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            int usuarioId = -1;
            try (PreparedStatement ps = BD.prepareStatement("SELECT usuario_id FROM clientes WHERE id = ?")) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) usuarioId = rs.getInt(1); }
            }
            if (usuarioId == -1) { BD.rollback(); return false; }
            ejecutar(BD, "DELETE FROM asistencias WHERE cliente_id = ?", clienteId);
            ejecutar(BD, "DELETE FROM pagos WHERE cliente_id = ?", clienteId);
            ejecutar(BD, "DELETE FROM membresias WHERE cliente_id = ?", clienteId);
            ejecutar(BD, "DELETE FROM rutina_ejercicio WHERE rutina_id IN (SELECT id FROM rutinas WHERE cliente_id = ?)", clienteId);
            ejecutar(BD, "DELETE FROM rutinas WHERE cliente_id = ?", clienteId);
            ejecutar(BD, "DELETE FROM clientes WHERE id = ?", clienteId);
            ejecutar(BD, "DELETE FROM usuarios WHERE id = ?", usuarioId);
            BD.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) { }
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { }
        }
    }

    private void ejecutar(Connection BD, String sql, int id) throws SQLException {
        try (PreparedStatement ps = BD.prepareStatement(sql)) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    /** Filas: id, nombre, correo, teléfono, emergencia, membresía más reciente. */
    public List<Object[]> listarClientesTabla(String filtro) {
        Permisos.exigirLectura(Modulo.SOCIOS);
        List<Object[]> filas = new ArrayList<>();
        boolean filtra = filtro != null && !filtro.trim().isEmpty();
        String sql = "SELECT c.id, u.nombre, u.correo, c.telefono, c.contacto_emergencia, "
                + "(SELECT CONCAT(m.tipo_plan, ' (', m.estado, ')') FROM membresias m WHERE m.cliente_id = c.id ORDER BY m.fecha_fin DESC, m.id DESC LIMIT 1) AS membresia "
                + "FROM clientes c INNER JOIN usuarios u ON c.usuario_id = u.id "
                + (filtra ? "WHERE u.nombre LIKE ? OR u.correo LIKE ? OR c.telefono LIKE ? OR c.contacto_emergencia LIKE ? " : "") + "ORDER BY u.nombre";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            if (filtra) { String f = "%" + filtro.trim() + "%"; for (int i = 1; i <= 4; i++) ps.setString(i, f); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mem = rs.getString("membresia");
                    filas.add(new Object[]{rs.getInt("id"), rs.getString("nombre"), rs.getString("correo"), rs.getString("telefono"),
                        rs.getString("contacto_emergencia"), mem == null ? "Sin membresía" : mem});
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return filas;
    }

    /** Clientes para combos (id de la tabla clientes). */
    public List<Item> listarClientesCombo() {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT c.id, u.nombre FROM clientes c INNER JOIN usuarios u ON c.usuario_id = u.id ORDER BY u.nombre";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Item(rs.getInt(1), rs.getString(2) + " (#" + rs.getInt(1) + ")"));
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public List<Clientes> listarClientes() {
        List<Clientes> lista = new ArrayList<>();
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, usuario_id, telefono, contacto_emergencia FROM clientes"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    private Clientes mapear(ResultSet rs) throws SQLException {
        Clientes c = new Clientes();
        c.setId(rs.getInt("id"));
        c.setUsuarioID(rs.getInt("usuario_id"));
        c.setTelefono(rs.getString("telefono"));
        c.setContactoEmergencia(rs.getString("contacto_emergencia"));
        return c;
    }

    /** {nombre, correo, telefono, contacto_emergencia} o null. */
    public String[] obtenerClientePorId(int idCliente) {
        String sql = "SELECT u.nombre, u.correo, c.telefono, c.contacto_emergencia FROM clientes c INNER JOIN usuarios u ON c.usuario_id = u.id WHERE c.id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)};
            }
        } catch (SQLException e) { System.err.println("Error al buscar cliente: " + e.getMessage()); }
        return null;
    }

    /** Perfil de cliente del usuario que inició sesión (portal del socio). */
    public Clientes buscarPorUsuarioId(int usuarioId) {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT id, usuario_id, telefono, contacto_emergencia FROM clientes WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return null;
    }

    public List<Clientes> busquedaClienteDinamica(String txt) {
        List<Clientes> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.usuario_id, c.telefono, c.contacto_emergencia FROM clientes c INNER JOIN usuarios u ON c.usuario_id = u.id WHERE u.nombre LIKE ? OR c.telefono LIKE ? OR c.contacto_emergencia LIKE ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            String f = "%" + txt + "%";
            ps.setString(1, f); ps.setString(2, f); ps.setString(3, f);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public int contarClientes() {
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement("SELECT COUNT(*) FROM clientes"); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); return 0; }
    }
}
