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
import proyectgymit.model.Rutinas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RutinasC {

public int crearRutina(Rutinas rutina) {
        String sql = "INSERT INTO rutinas (cliente_id, entrenador_id, nombre_rutina) VALUES (?, ?, ?)";
        int idGenerado = -1;
        try (Connection con = conexionbd.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rutina.getClienteID());
            if (rutina.getEntrenadorID() != null) ps.setInt(2, rutina.getEntrenadorID()); 
            else ps.setNull(2, Types.INTEGER); 
            
            ps.setString(3, rutina.getNombreRutina());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) { 
                    if (rs.next()) idGenerado = rs.getInt(1); 
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return idGenerado;
    }

    // --- NUEVOS MÉTODOS DE BÚSQUEDA ---
    public List<Rutinas> listarRutinasPorCliente(int clienteId) {
        List<Rutinas> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_id, entrenador_id, nombre_rutina FROM rutinas WHERE cliente_id = ?";
        try (Connection con = conexionbd.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rutinas r = new Rutinas();
                    r.setId(rs.getInt("id"));
                    r.setClienteID(rs.getInt("cliente_id"));
                    // Manejo del entrenador_id que puede ser nulo
                    int entId = rs.getInt("entrenador_id");
                    r.setEntrenadorID(rs.wasNull() ? null : entId);
                    r.setNombreRutina(rs.getString("nombre_rutina"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }
}

