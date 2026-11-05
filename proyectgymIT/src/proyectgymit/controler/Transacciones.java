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
import proyectgymit.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Transacciones {
    
    public Usuario autenticar(String correo, String password) {
        String sql = "SELECT * FROM usuarios WHERE correo = ? AND password = ?";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("password"),
                        rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }
        return null;
    }

}
