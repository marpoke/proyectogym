/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.controler;
import proyectgymit.config.conexionbd;
import proyectgymit.model.RutinaEjercicio;
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
public class RutinaEjercicioC {
    public boolean agregarEjercicioARutina(RutinaEjercicio detalle) {
        String sql = "INSERT INTO rutina_ejercicio (rutina_id, ejercicio_id, series, repeticiones) VALUES (?, ?, ?, ?)";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, detalle.getRutinaID());
            ps.setInt(2, detalle.getEjercicioID());
            ps.setInt(3, detalle.getSeries());
            ps.setInt(4, detalle.getRepeticiones());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<RutinaEjercicio> listarEjerciciosDeRutina(int rutinaId) {
        List<RutinaEjercicio> lista = new ArrayList<>();
        String sql = "SELECT id, rutina_id, ejercicio_id, series, repeticiones FROM rutina_ejercicio WHERE rutina_id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, rutinaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RutinaEjercicio re = new RutinaEjercicio();
                    re.setId(rs.getInt("id"));
                    re.setRutinaID(rs.getInt("rutina_id"));
                    re.setEjercicioID(rs.getInt("ejercicio_id"));
                    re.setSeries(rs.getInt("series"));
                    re.setRepeticiones(rs.getInt("repeticiones"));
                    lista.add(re);
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public RutinaEjercicio buscarRutinaEjercicioPorId(int id) {
        RutinaEjercicio re = null;
        String sql = "SELECT id, rutina_id, ejercicio_id, series, repeticiones FROM rutina_ejercicio WHERE id = ?";
        try (Connection BD = conexionbd.getConexion(); PreparedStatement ps = BD.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    re = new RutinaEjercicio();
                    re.setId(rs.getInt("id"));
                    re.setRutinaID(rs.getInt("rutina_id"));
                    re.setEjercicioID(rs.getInt("ejercicio_id"));
                    re.setSeries(rs.getInt("series"));
                    re.setRepeticiones(rs.getInt("repeticiones"));
                }
            }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return re;
    }
}
