package proyectgymit.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a MySQL. Puede sobreescribirse con variables de entorno GYM_DB_URL, GYM_DB_USER y
 * GYM_DB_PASSWORD para no depender de credenciales escritas en el código.
 */
public class conexionbd {

    private static final String URL = valor("GYM_DB_URL",
            "jdbc:mysql://localhost:3306/gym_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = valor("GYM_DB_USER", "marpoke");
    private static final String PASSWORD = valor("GYM_DB_PASSWORD", "marpokevaca1");

    private static String valor(String variable, String porDefecto) {
        String v = System.getenv(variable);
        return (v == null || v.isEmpty()) ? porDefecto : v;
    }

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
            if (con != null && !con.isClosed()) System.out.println("CONEXION EXITOSA. Base de datos: " + con.getCatalog());
        } catch (SQLException e) {
            System.err.println("ERROR DE CONEXION: " + e.getMessage());
        }
    }
}
