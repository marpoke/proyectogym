/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;

/**
 *
 * @author mario
 */
import java.sql.Statement;
import proyectgymit.config.conexionbd;
import proyectgymit.model.Clientes;
import proyectgymit.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class ClienteC {
    
    public boolean registrarClienteCompleto(Usuario usuario, Clientes cliente) {
        String sqlUsuario = "INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, 'CLIENTE')";
        String sqlCliente = "INSERT INTO clientes (usuario_id, telefono, contacto_emergencia) VALUES (?, ?, ?)";
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
                    if (rs.next()) idUsuarioGenerado = rs.getInt(1); 
                }
            }

            if (idUsuarioGenerado != -1) {
                try (PreparedStatement psCli = BD.prepareStatement(sqlCliente)) {
                    psCli.setInt(1, idUsuarioGenerado); 
                    psCli.setString(2, cliente.getTelefono());
                    psCli.setString(3, cliente.getContactoEmergencia());
                    psCli.executeUpdate();
                }
                BD.commit(); 
                return true;
            } else {
                BD.rollback(); 
                return false;
            }
        } catch (SQLException e) {
            if (BD != null) try { BD.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (BD != null) try { BD.setAutoCommit(true); BD.close(); } catch (SQLException e) {}
        }
    }

    public boolean eliminarCliente(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection con = conexionbd.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    
    public List<Clientes> listarClientes() {
        List<Clientes> lista = new ArrayList<>();
        String sql = "SELECT id, usuario_id, telefono, contacto_emergencia FROM clientes";
        try (Connection con = conexionbd.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clientes c = new Clientes();
                c.setId(rs.getInt("id"));
                c.setUsuarioID(rs.getInt("usuario_id"));
                c.setTelefono(rs.getString("telefono"));
                c.setContactoEmergencia(rs.getString("contacto_emergencia"));
                lista.add(c);
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public Clientes buscarClientePorId(int id) {
        Clientes c = null;
        String sql = "SELECT id, usuario_id, telefono, contacto_emergencia FROM clientes WHERE id = ?";
        try (Connection con = conexionbd.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Clientes();
                    c.setId(rs.getInt("id"));
                    c.setUsuarioID(rs.getInt("usuario_id"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setContactoEmergencia(rs.getString("contacto_emergencia"));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return c;
    }
    
    public List<Clientes> busquedaClienteDinamica (String txtBusqueda){
        List <Clientes> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.usuario_id, c.telefono, c.contacto_emergencia " +
                     "FROM clientes c " +
                     "INNER JOIN usuarios u ON c.usuario_id = u.id " +
                     "WHERE u.nombre LIKE ? OR c.telefono LIKE ? OR c.contacto_emergencia LIKE ?";
        try(Connection BD = conexionbd.getConexion();
            PreparedStatement ps = BD.prepareStatement(sql)){
            String filtro = "%" + txtBusqueda + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Clientes c = new Clientes();
                    c.setId(rs.getInt("id"));
                    c.setUsuarioID(rs.getInt("usuario_id"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setContactoEmergencia(rs.getString("contacto_emergencia"));
                    lista.add(c);
                }
            }
        }catch (SQLException e){
            System.out.println("error: "+ e.getMessage());
        }
        return lista;
    }
}
