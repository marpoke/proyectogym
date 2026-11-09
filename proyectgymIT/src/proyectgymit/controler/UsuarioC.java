/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Usuario;
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
public class UsuarioC {
    //ADMIN, RECEPCIONISTA, CLIENTE, ENTRENADOR 
    public Usuario iniciarSesion(String correo, String password) {
        Usuario usuarioLogueado = null;
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE correo = ? AND password = ?";
        
        try (Connection BD = conexionbd.getConexion(); 
             PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioLogueado = new Usuario();
                    usuarioLogueado.setId(rs.getInt("id"));
                    usuarioLogueado.setNombre(rs.getString("nombre"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    usuarioLogueado.setRol(rs.getString("rol")); 
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return usuarioLogueado;
    }

    public boolean registrarPersonal(Usuario personal) {
        String sql = "INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, ?)";
        try (Connection BD = conexionbd.getConexion(); 
             PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setString(1, personal.getNombre());
            ps.setString(2, personal.getCorreo());
            ps.setString(3, personal.getPassword());
            ps.setString(4, personal.getRol()); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean eliminarPersonal(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, rol FROM usuarios";
        try (Connection BD = conexionbd.getConexion();
             PreparedStatement ps = BD.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setCorreo(rs.getString("correo"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Usuario buscarUsuarioPorId(int id) {
        Usuario u = null;
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    u.setRol(rs.getString("rol"));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return u;
    }
    
    public List<Usuario> busquedaUsuarioDinamica(String txtBusqueda){
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE nombre LIKE ? OR correo LIKE ?";
        try(Connection BD = conexionbd.getConexion();
            PreparedStatement ps = BD.prepareStatement(sql)){
            String filtro = "%" + txtBusqueda + "%";
            ps.setString(1,filtro);
            ps.setString(2,filtro);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    u.setRol(rs.getString("rol"));
                    lista.add(u);
                    
                }
            }
        }catch (SQLException e){
            System.out.println("error: "+ e.getMessage());
        }return lista;
    }
}
