package proyectgymit.util;

import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Utilidades compartidas por las vistas. */
public final class Ui {
    private Ui() {}

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(new Object[0][0], columnas) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        };
    }
    public static void info(Component p, String m) { JOptionPane.showMessageDialog(p, m, "GojoGym", JOptionPane.INFORMATION_MESSAGE); }
    public static void error(Component p, String m) { JOptionPane.showMessageDialog(p, m, "GojoGym", JOptionPane.ERROR_MESSAGE); }
    public static boolean confirmar(Component p, String m) {
        return JOptionPane.showConfirmDialog(p, m, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static LocalDate aLocalDate(Date d) { return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
    public static Date aDate(LocalDate d) { return d == null ? null : Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()); }

    /** Los combos del diseñador son JComboBox<String>: se les asocia una lista paralela de Item (id de BD). */
    public static List<Item> llenarCombo(JComboBox<String> combo, List<Item> items) {
        String[] t = new String[items.size()];
        for (int i = 0; i < t.length; i++) t[i] = items.get(i).toString();
        combo.setModel(new DefaultComboBoxModel<>(t));
        return items;
    }
    public static int idSeleccionado(JComboBox<String> combo, List<Item> items) {
        int i = combo.getSelectedIndex();
        return (i < 0 || i >= items.size()) ? -1 : items.get(i).getId();
    }
    public static void seleccionarPorId(JComboBox<String> combo, List<Item> items, int id) {
        for (int i = 0; i < items.size(); i++) if (items.get(i).getId() == id) { combo.setSelectedIndex(i); return; }
    }

    public static Integer idDeFila(JTable t, int col) {
        int f = t.getSelectedRow();
        return f < 0 ? null : Integer.valueOf(String.valueOf(t.getValueAt(f, col)));
    }

    public static void habilitar(boolean on, Component... cs) {
        for (Component c : cs) {
            c.setEnabled(on);
            if (c instanceof Container k) for (Component h : k.getComponents()) habilitar(on, h);
        }
    }

    public static void alCambiar(javax.swing.text.JTextComponent campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { accion.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { accion.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { accion.run(); }
        });
    }
    public static void alDobleClic(JTable t, Runnable accion) {
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() >= 0) accion.run();
            }
        });
    }
    public static void alSeleccionar(JTable t, Runnable accion) {
        t.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) accion.run(); });
    }

    public static boolean vacio(String s) { return s == null || s.trim().isEmpty(); }
    public static boolean correoValido(String s) { return s != null && s.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"); }
}
