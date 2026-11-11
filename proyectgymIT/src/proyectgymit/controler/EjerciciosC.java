/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Ejercicios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author mario
 */
public class EjerciciosC {
    
    public boolean registrarEjercicios (Ejercicios ejercicio){
        String sql = "INSERT INTO ejercicios (nombre, grupo_muscular, descripcion) VALUES (?,?,?)";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql)){
            ps.setString(1, ejercicio.getNombre());
            ps.setString(2, ejercicio.getGrupoMuscular());
            ps.setString(3, ejercicio.getDescripcion());
            return ps.executeUpdate() > 0 ;
           
        } catch (SQLException e){
            System.err.println("error: " + e.getMessage());
        }
        return false;
    }
    
    public boolean actualizarEjercicios(Ejercicios ejercicio){
        String sql = "UPDATE ejercicios SET nombre = ?, grupo_muscular = ?, descripcion = ? WHERE id = ?";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql)){
            ps.setString(1, ejercicio.getNombre());
            ps.setString(2, ejercicio.getGrupoMuscular());
            ps.setString(3, ejercicio.getDescripcion());
            ps.setInt(4, ejercicio.getId());
            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            System.err.println("error: " + e.getMessage());
        
        }
        return false;
    }
    
    public boolean eliminarEjercicios(Ejercicios ejercicio){
        String sql = "DELATE FROM ejercicios WHERE id = ?";
        try(Connection BD = conexionbd.getConexion();
            PreparedStatement ps = BD.prepareStatement(sql)){
            ps.setInt(1, ejercicio.getId());
            return ps.executeUpdate() > 0 ;
            
        }catch (SQLException e){
            System.err.println("error: "+ e.getMessage()); 
        }
        return false;
    }
    
    public List<Ejercicios> listarEjercicios(){
        List<Ejercicios> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, grupo_muscular, descripcion FROM ejercicios";
        try(Connection BD = conexionbd.getConexion();
            PreparedStatement ps = BD.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                     lista.add(new Ejercicios(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("grupo_muscular"),
                        rs.getString("descripcion")
                ));
            }
        }catch(SQLException e){
            System.err.println("error: " + e.getMessage());
        }
            
        return lista;
    }
    
    public Ejercicios buscarEjercicioPorId(int id) {
        Ejercicios ej = null;
        String sql = "SELECT id, nombre, grupo_muscular, descripcion FROM ejercicios WHERE id = ?";
        try (Connection con = conexionbd.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ej = new Ejercicios(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("grupo_muscular"),
                            rs.getString("descripcion")
                    );
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return ej;
    }

    public List<Ejercicios> buscarEjerciciosDinamico(String textoBusqueda) {
        List<Ejercicios> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, grupo_muscular, descripcion FROM ejercicios WHERE nombre LIKE ? OR grupo_muscular LIKE ?";
        try (Connection con = conexionbd.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = "%" + textoBusqueda + "%";
            ps.setString(1, filtro); 
            ps.setString(2, filtro); 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Ejercicios(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("grupo_muscular"),
                            rs.getString("descripcion")
                    ));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    
}
