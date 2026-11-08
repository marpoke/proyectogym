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
import proyectgymit.model.Usuario;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EntrenadoresC {
    
    public boolean registrarEntrenadorCompleto(Usuario usuario, Entrenadores entrenador) {
        String sqlUsuario = "INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, 'ENTRENADOR')";
        String sqlEntrenador = "INSERT INTO entrenadores (usuario_id, especialidad, telefono) VALUES (?, ?, ?)";
        
        Connection BD = null;
        try {
            BD = conexionbd.getConexion();
            BD.setAutoCommit(false); 

            int idUsuarioGenerado = -1;

            try (PreparedStatement psUser = BD.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, usuario.getNombre());
                psUser.setString(2, usuario.getCorreo());
                psUser.setString(3, usuario.getPassword());
                psUser.executeUpdate();
                
                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        idUsuarioGenerado = rs.getInt(1); 
                    }
                }
            }

            if (idUsuarioGenerado != -1) {
                try (PreparedStatement psEnt = BD.prepareStatement(sqlEntrenador)) {
                    psEnt.setInt(1, idUsuarioGenerado); 
                    psEnt.setString(2, entrenador.getEspecialidad());
                    psEnt.setString(3, entrenador.getTelefono());
                    psEnt.executeUpdate();
                }
                BD.commit(); 
                return true;
            } else {
                BD.rollback(); 
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar entrenador: " + e.getMessage());
            if (BD != null) { try { BD.rollback(); } catch (SQLException ex) { } }
            return false;
        } finally {
            if (BD != null) { try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) { } }
        }
    }

    public boolean eliminarEntrenador(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); 
             PreparedStatement ps = BD.prepareStatement(sql)) {
            
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar entrenador: " + e.getMessage());
            return false;
        }
    }
    
    public List<Entrenadores> listarEntrenadores (){
        List<Entrenadores> lista = new ArrayList <> ();
    String sql = "SELECT id, usuario_id, especialidad, telefono FROM entrenadores";
    try (Connection BD = conexionbd.getConexion();
         PreparedStatement ps = BD.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()){
    while (rs.next()){
        Entrenadores e = new Entrenadores();
        e.setId(rs.getInt("id"));
        e.setUsuarioID(rs.getInt("usuario_id"));
        e.setEspecialidad(rs.getString("especialidad"));
        e.setTelefono(rs.getString("telefono"));
        lista.add(e);
        }
    }catch (SQLException b){
            System.out.println("error:" + b.getMessage());
}
    return lista;
}
 
    

    }

