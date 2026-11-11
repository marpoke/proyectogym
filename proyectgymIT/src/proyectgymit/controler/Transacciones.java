/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;

/**
 *
 * @author mario
 */

import proyectgymit.config.conexionbd;
import proyectgymit.model.Membresias;
import proyectgymit.model.Pagos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Transacciones {
    
public boolean procesarPagoYMembresia(Pagos pago, Membresias membresia) {
        String sqlPago = "INSERT INTO pagos (cliente_id, monto, metodo_pago) VALUES (?, ?, ?)";
        String sqlMembresia = "INSERT INTO membresias (cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)";
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false);
            
            try (PreparedStatement psP = BD.prepareStatement(sqlPago)) {
                psP.setInt(1, pago.getClienteID());
                psP.setDouble(2, pago.getMonto());
                psP.setString(3, pago.getMetodoPago());
                psP.executeUpdate();
            }
            try (PreparedStatement psM = BD.prepareStatement(sqlMembresia)) {
                psM.setInt(1, membresia.getClienteID());
                psM.setString(2, membresia.getTipoPlan());
                psM.setDate(3, java.sql.Date.valueOf(membresia.getFechaInicio()));
                psM.setDate(4, java.sql.Date.valueOf(membresia.getFechaFinal()));
                psM.setString(5, membresia.getEstado()); 
                psM.executeUpdate();
            }
            BD.commit();
            return true;
        } catch (SQLException e) {
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) {}
        }
    }

    public List<Pagos> listarPagosCliente(int clienteId) {
        List<Pagos> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_id, monto, metodo_pago, fecha_pago FROM pagos WHERE cliente_id = ? ORDER BY fecha_pago DESC";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pagos p = new Pagos();
                    p.setId(rs.getInt("id"));
                    p.setClienteID(rs.getInt("cliente_id"));
                    p.setMonto(rs.getDouble("monto"));
                    p.setMetodoPago(rs.getString("metodo_pago"));
                    p.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
                    lista.add(p);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Pagos buscarPagoPorId(int id) {
        Pagos p = null;
        String sql = "SELECT id, cliente_id, monto, metodo_pago, fecha_pago FROM pagos WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Pagos();
                    p.setId(rs.getInt("id"));
                    p.setClienteID(rs.getInt("cliente_id"));
                    p.setMonto(rs.getDouble("monto"));
                    p.setMetodoPago(rs.getString("metodo_pago"));
                    p.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return p;
    }

    public Membresias buscarMembresiaActiva(int clienteId) {
        Membresias m = null;
        String sql = "SELECT id, cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado FROM membresias WHERE cliente_id = ? AND estado = 'ACTIVA'";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m = new Membresias();
                    m.setId(rs.getInt("id"));
                    m.setClienteID(rs.getInt("cliente_id"));
                    m.setTipoPlan(rs.getString("tipo_plan"));
                    m.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    m.setFechaFinal(rs.getDate("fecha_fin").toLocalDate());
                    m.setEstado(rs.getString("estado"));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return m;
    }

    public Membresias buscarMembresiaPorId(int id) {
        Membresias m = null;
        String sql = "SELECT id, cliente_id, tipo_plan, fecha_inicio, fecha_fin, estado FROM membresias WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m = new Membresias();
                    m.setId(rs.getInt("id"));
                    m.setClienteID(rs.getInt("cliente_id"));
                    m.setTipoPlan(rs.getString("tipo_plan"));
                    m.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    m.setFechaFinal(rs.getDate("fecha_fin").toLocalDate());
                    m.setEstado(rs.getString("estado"));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return m;
    }

    public boolean actualizarMembresiasVencidasAutomatico() {
        String sql = "UPDATE membresias SET estado = 'VENCIDA' WHERE fecha_fin < CURRENT_DATE AND estado = 'ACTIVA'";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean cambiarEstadoMembresiaManual(int idMembresia, String nuevoEstado) {
        String sql = "UPDATE membresias SET estado = ? WHERE id = ?";
        try (Connection BD= conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMembresia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}


