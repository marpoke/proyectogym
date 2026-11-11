/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;

import proyectgymit.config.conexionbd;
import proyectgymit.model.Asistencias;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

/**
 *
 * @author mario
 */
public class AsistenciasC {
    
    public boolean registrarAsistencia(int idCliente) {
        String sql = "INSERT INTO asistencias (cliente_id) VALUES (?)";
        try (Connection BD = conexionbd.getConexion(); 
             PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

public List<Asistencias> listarAsistenciasCliente(int clienteId) {
        List<Asistencias> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_id, fecha_entrada FROM asistencias WHERE cliente_id = ? ORDER BY fecha_entrada DESC";
        
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asistencias a = new Asistencias();
                    a.setId(rs.getInt("id"));
                    a.setClienteID(rs.getInt("cliente_id"));
                    
                    // CORRECCIÓN: Obtener como Timestamp y convertir a LocalDateTime
                    Timestamp timestamp = rs.getTimestamp("fecha_entrada");
                    if (timestamp != null) {
                        a.setFechaEntrada(timestamp.toLocalDateTime());
                    }
                    
                    lista.add(a);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }
}
