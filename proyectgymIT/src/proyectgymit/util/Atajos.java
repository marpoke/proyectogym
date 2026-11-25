package proyectgymit.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Atajos de teclado de las pantallas:
 *  Ctrl+F  filtrar la tabla      Ctrl+S  guardar / registrar      Ctrl+N o Esc  limpiar el formulario
 * Reutiliza los botones que ya existen, así respeta permisos y validaciones.
 */
public final class Atajos {

    private Atajos() {}

    private static final int CTRL = mascara();

    private static int mascara() {
        try { return java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(); }
        catch (java.awt.HeadlessException e) { return java.awt.event.InputEvent.CTRL_DOWN_MASK; }
    }

    public static void registrar(JComponent c, KeyStroke k, String nombre, Runnable r) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, nombre);
        c.getActionMap().put(nombre, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    public static void filtro(JComponent donde, JTextField filtro) {
        registrar(donde, KeyStroke.getKeyStroke(KeyEvent.VK_F, CTRL), "tema.filtrar", () -> {
            filtro.requestFocusInWindow();
            filtro.selectAll();
        });
    }

    /** Ctrl+S / Ctrl+N / Esc usando los botones "Guardar/Registrar" y "Limpiar" de la pantalla. */
    public static void formulario(JComponent panel) {
        JButton guardar = null, limpiar = null;
        for (JButton b : botones(panel)) {
            String t = b.getText() == null ? "" : b.getText().toLowerCase();
            if (guardar == null && (t.startsWith("guardar") || t.startsWith("registr"))) guardar = b;
            if (limpiar == null && t.startsWith("limpiar")) limpiar = b;
        }
        final JButton g = guardar, l = limpiar;
        if (g != null) {
            registrar(panel, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL), "tema.guardar", () -> { if (g.isEnabled() && g.isShowing()) g.doClick(); });
            g.setToolTipText("Ctrl+S");
        }
        if (l != null) {
            Runnable r = () -> { if (l.isEnabled() && l.isShowing()) l.doClick(); };
            registrar(panel, KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL), "tema.nuevo", r);
            l.setToolTipText("Ctrl+N");
        }
    }

    private static java.util.List<JButton> botones(Container c) {
        java.util.List<JButton> res = new java.util.ArrayList<>();
        for (Component h : c.getComponents()) {
            if (h instanceof JButton b) res.add(b);
            if (h instanceof Container k) res.addAll(botones(k));
        }
        return res;
    }

    /** Esc cierra el diálogo / la ventana secundaria. */
    public static void escCierra(javax.swing.RootPaneContainer w, Runnable cerrar) {
        JComponent rp = w.getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "tema.cerrar");
        rp.getActionMap().put("tema.cerrar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { cerrar.run(); }
        });
    }
}
