package proyectgymit.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Function;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Validación visual en los campos: borde rojo y mensaje en el tooltip. No reemplaza las validaciones de los botones. */
public final class Validacion {

    private Validacion() {}

    public static final String ERROR = "tema.error";

    private static void instalar(JTextField f, boolean obligatorio, Function<String, String> regla) {
        boolean[] tocado = {false};
        Runnable revisar = () -> {
            String t = f.getText().trim();
            String msg = null;
            if (t.isEmpty()) { if (obligatorio && tocado[0]) msg = "Este campo es obligatorio"; }
            else msg = regla.apply(t);
            f.putClientProperty(ERROR, msg);
            f.setToolTipText(msg);
            if (msg != null) {
                if (f.getClientProperty("validacion.borde") == null) f.putClientProperty("validacion.borde", f.getBorder());
                f.setBorder(javax.swing.BorderFactory.createLineBorder(Paleta.MAL, 2));
            } else if (f.getClientProperty("validacion.borde") != null) {
                f.setBorder((javax.swing.border.Border) f.getClientProperty("validacion.borde"));
                f.putClientProperty("validacion.borde", null);
            }
            f.repaint();
        };
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { revisar.run(); }
            public void removeUpdate(DocumentEvent e) { revisar.run(); }
            public void changedUpdate(DocumentEvent e) { revisar.run(); }
        });
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { tocado[0] = true; revisar.run(); }
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
        });
    }

    public static void obligatorio(JTextField f) { instalar(f, true, t -> null); }

    public static void correo(JTextField f, boolean obligatorio) {
        instalar(f, obligatorio, t -> Ui.correoValido(t) ? null : "Escriba un correo válido (ejemplo: nombre@correo.com)");
    }

    public static void numero(JTextField f, boolean obligatorio) {
        instalar(f, obligatorio, t -> {
            try { return Double.parseDouble(t) >= 0 ? null : "El valor no puede ser negativo"; }
            catch (NumberFormatException e) { return "Escriba un número (ejemplo: 500 o 499.90)"; }
        });
    }

    public static void entero(JTextField f, boolean obligatorio) {
        instalar(f, obligatorio, t -> {
            try { return Integer.parseInt(t) > 0 ? null : "Debe ser mayor que cero"; }
            catch (NumberFormatException e) { return "Escriba un número entero"; }
        });
    }
}
