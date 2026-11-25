package proyectgymit;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import proyectgymit.config.conexionbd;
import proyectgymit.controler.UsuarioC;
import proyectgymit.util.Ui;
import proyectgymit.view.login;

/** Punto de entrada: comprueba la base de datos, crea el primer ADMIN si hace falta y abre el login. */
public class ProyectgymIT {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            // Botones de los cuadros de diálogo en español
            javax.swing.UIManager.put("OptionPane.yesButtonText", "Sí");
            javax.swing.UIManager.put("OptionPane.noButtonText", "No");
            javax.swing.UIManager.put("OptionPane.okButtonText", "Aceptar");
            javax.swing.UIManager.put("OptionPane.cancelButtonText", "Cancelar");
            try (Connection c = conexionbd.getConexion()) {
                // conexión correcta
            } catch (SQLException e) {
                Ui.error(null, "No se pudo conectar a la base de datos gym_db:\n" + e.getMessage()
                        + "\n\nRevise que MySQL esté encendido, que se haya ejecutado el script de la base de datos\n"
                        + "y los datos de conexión (proyectgymit.config.conexionbd).");
                return;
            }
            UsuarioC usuarioC = new UsuarioC();
            if (usuarioC.necesitaPrimerAdmin() && !crearPrimerAdmin(usuarioC)) {
                return;
            }
            new login().setVisible(true);
        });
    }

    /** Primer arranque: no hay ningún ADMIN, se pide uno (la contraseña se guarda con PBKDF2). */
    private static boolean crearPrimerAdmin(UsuarioC usuarioC) {
        JTextField nombre = new JTextField();
        JTextField correo = new JTextField();
        JPasswordField pass = new JPasswordField();
        JPasswordField pass2 = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("No hay administradores. Cree el primero:"));
        p.add(new JLabel("Nombre:")); p.add(nombre);
        p.add(new JLabel("Correo:")); p.add(correo);
        p.add(new JLabel("Contraseña (mínimo 8 caracteres):")); p.add(pass);
        p.add(new JLabel("Repetir contraseña:")); p.add(pass2);
        while (true) {
            if (JOptionPane.showConfirmDialog(null, p, "Primer administrador", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return false;
            }
            char[] a = pass.getPassword();
            char[] b = pass2.getPassword();
            String error = null;
            if (Ui.vacio(nombre.getText())) error = "Escriba el nombre.";
            else if (!Ui.correoValido(correo.getText().trim())) error = "Escriba un correo válido.";
            else if (a.length < 8) error = "La contraseña debe tener al menos 8 caracteres.";
            else if (!java.util.Arrays.equals(a, b)) error = "Las contraseñas no coinciden.";
            if (error != null) {
                Ui.error(null, error);
            } else if (usuarioC.crearPrimerAdmin(nombre.getText(), correo.getText(), a)) {
                java.util.Arrays.fill(a, '\0');
                java.util.Arrays.fill(b, '\0');
                Ui.info(null, "Administrador creado. Ya puede iniciar sesión.");
                return true;
            } else {
                Ui.error(null, "No se pudo crear el administrador.");
            }
        }
    }
}
