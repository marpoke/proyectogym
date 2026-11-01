/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author mario
 */
public class conexionbd {

    private static final String URL = "jdbc:mysql://localhost:3306/gym_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "marpoke";
    private static final String PASSWORD = "marpokevaca1";

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC no encontrado: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection con = conexionbd.getConexion()) {
            if (con != null && !con.isClosed()) {
                System.out.println("----------------------------------------");
                System.out.println("CONEXION EXITOSA A LA BASE DE DATOS");
                System.out.println("Base de datos: " + con.getCatalog());
                System.out.println("----------------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("----------------------------------------");
            System.err.println("ERROR DE CONEXION:");
            System.err.println(e.getMessage());
            System.err.println("----------------------------------------");
        }
    }
}
