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

public class RutinasC {
    
public boolean asignarRutina(Rutinas rutina){

    String sql = "INSERT INTO rutinas (cliente_id, entrenador_id, nombre_rutina) VALUES (?,?,?) ";
    try (Connection BD = conexionbd.getConexion();
         PreparedStatement ps = BD.prepareStatement(sql)){
        
        ps.setInt(1,rutina.getClienteID());
        ps.setIn
    
    }
}   

}

