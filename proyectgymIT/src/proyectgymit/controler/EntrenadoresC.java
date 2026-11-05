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
import proyectgymit.model.Entrenadores;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class EntrenadoresC {
    
    
    public boolean registroEntrenador(Entrenadores entrenador){
    String sql = "INSERT INTO entrenadores (usuario_id, especialidad,telefono ) VALUES (?,?,?)";
    try (Connection BD = conexionbd.getConexion();
         PreparedStatement ps = BD.prepareStatement(sql)){
        
        ps.setInt(1, entrenador.getUsuarioID() );
        ps.setString (2, entrenador.getEspecialidad());
        ps.setString (3, entrenador.getTelefono());
        
        return ps.executeUpdate()> 0;
    }catch (SQLException e){
        System.out.println("Error al registrar entrenador: " + e.getMessage());
        return false;
    }
    }
    
    public Entrenadores obtenerDatos(int usuarioID){
    
        String sql = "SELECT * FROM entrenadores WHERE usuario_id = ?  ";
        try(Connection BD = conexionbd.getConexion();
            PreparedStatement ps = BD.prepareStatement(sql)){
            
            ps.setInt(1, usuarioID);
            try(ResultSet rs = ps.executeQuery()){
            if (rs.next()){
            
                return new Entrenadores (
                
               rs.getInt("id"),
               rs.getInt("usuarioID"),
               rs.getString("especialidad"),
               rs.getString("telefono")
                        
                );
            }
          }      
        }catch (SQLException e){
            System.out.println("Error al obtener entrenador: " + e.getMessage());
            
        }
    return null;
    }   

    }

