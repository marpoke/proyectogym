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
import proyectgymit.model.Clientes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class ClienteC {
public boolean registrarCliente(Clientes cliente) {
        String sql = "INSERT INTO clientes (usuario_id, telefono, contacto_emergencia) VALUES (?, ?, ?)";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql)) {

            ps.setInt(1, cliente.getUsuarioID());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getContactoEmergencia());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    public Clientes obtenerDatos(int usuarioId) {
        String sql = "SELECT * FROM clientes WHERE usuario_id = ?";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Clientes(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("telefono"),
                        rs.getString("contacto_emergencia")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }
}
