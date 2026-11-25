package proyectgymit.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Mejoras genéricas para cualquier JTable: ordenar por columna, filtro rápido, contador de registros,
 * mensaje de tabla vacía, menú contextual (clic derecho) y exportación a CSV.
 * Solo trabaja sobre los datos que ya muestra la tabla: no toca controladores ni base de datos.
 */
public final class Tabla {

    private Tabla() {}

    private static final String MARCA = "tabla.instalada";

    /** Ordenar por columna (clic en el encabezado) y menú contextual. No cambia el aspecto de la tabla. Es idempotente. */
    public static void instalar(JTable t) {
        if (t.getClientProperty(MARCA) != null || t.getClientProperty("tabla.simple") != null) return;
        t.putClientProperty(MARCA, Boolean.TRUE);
        t.setAutoCreateRowSorter(true);          // se vuelve a crear solo cuando la tabla recibe otro modelo
        menuContextual(t);
    }

    /** Instala lo anterior en todas las tablas que contenga el contenedor. */
    public static void instalarTodas(Container c) {
        for (Component h : c.getComponents()) {
            if (h instanceof JTable t) instalar(t);
            else if (h instanceof Container k) instalarTodas(k);
        }
    }

    // ------------------------------------------------------------------ menú contextual
    private static final String ACCIONES = "tabla.acciones";

    /** Agrega una opción propia al menú contextual de la tabla (por ejemplo "Imprimir recibo"). */
    @SuppressWarnings("unchecked")
    public static void agregarAccion(JTable t, String texto, Runnable r) {
        java.util.Map<String, Runnable> m = (java.util.Map<String, Runnable>) t.getClientProperty(ACCIONES);
        if (m == null) { m = new java.util.LinkedHashMap<>(); t.putClientProperty(ACCIONES, m); }
        m.put(texto, r);
    }

    @SuppressWarnings("unchecked")
    private static void menuContextual(JTable t) {
        t.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { mostrar(e); }
            @Override public void mouseReleased(MouseEvent e) { mostrar(e); }

            private void mostrar(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int fila = t.rowAtPoint(e.getPoint());
                int col = t.columnAtPoint(e.getPoint());
                if (fila >= 0) t.setRowSelectionInterval(fila, fila);
                JPopupMenu m = new JPopupMenu();
                if (fila >= 0) {
                    // Acciones de la pantalla: reutiliza los botones que ya existen (respetando permisos y validaciones)
                    for (JButton b : botonesAccion(t)) {
                        if (!b.isVisible() || !b.isEnabled()) continue;
                        m.add(item(b.getText(), () -> b.doClick()));
                    }
                    java.util.Map<String, Runnable> propias = (java.util.Map<String, Runnable>) t.getClientProperty(ACCIONES);
                    if (propias != null) propias.forEach((k, r) -> m.add(item(k, r)));
                    if (m.getComponentCount() > 0) m.addSeparator();
                    final int c = col;
                    m.add(item("Copiar valor", () -> copiar(String.valueOf(t.getValueAt(fila, Math.max(c, 0))))));
                    m.add(item("Copiar fila", () -> copiar(filaComoTexto(t, fila))));
                    m.addSeparator();
                }
                m.add(item("Exportar CSV…", () -> exportarCsv(t)));
                m.show(t, e.getX(), e.getY());
            }
        });
    }

    private static JMenuItem item(String texto, Runnable r) {
        JMenuItem i = new JMenuItem(texto);
        i.addActionListener(e -> r.run());
        return i;
    }

    /** Botones de la pantalla que actúan sobre la fila seleccionada (editar / eliminar / abrir / asignar). */
    private static List<JButton> botonesAccion(JTable t) {
        List<JButton> res = new ArrayList<>();
        Container raiz = t.getTopLevelAncestor() != null ? (Container) t.getTopLevelAncestor() : null;
        if (raiz == null) {
            Container c = t;
            while (c.getParent() != null) c = c.getParent();
            raiz = c;
        }
        buscar(raiz, res);
        return res;
    }

    private static void buscar(Container c, List<JButton> res) {
        for (Component h : c.getComponents()) {
            if (h instanceof JButton b && b.getText() != null) {
                String x = b.getText().toLowerCase();
                if (x.contains("editar") || x.contains("actualizar") || x.contains("eliminar") || x.contains("renovar")
                        || x.contains("ejercicios") || x.contains("asignar")) res.add(b);
            }
            if (h instanceof Container k) buscar(k, res);
        }
    }

    private static void copiar(String s) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
    }

    private static String filaComoTexto(JTable t, int fila) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < t.getColumnCount(); c++) {
            if (c > 0) sb.append('\t');
            sb.append(texto(t.getValueAt(fila, c)));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ CSV
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static String texto(Object v) {
        if (v == null) return "";
        if (v instanceof LocalDateTime d) return d.format(FECHA_HORA);
        if (v instanceof LocalDate d) return d.toString();
        return String.valueOf(v);
    }

    private static String csv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains(";")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    /** Exporta las filas visibles (con el filtro y el orden actuales) a un CSV con BOM UTF-8 para que Excel lo abra bien. */
    public static void exportarCsv(JTable t) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar tabla a CSV");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        fc.setSelectedFile(new File("exportacion_" + java.time.LocalDate.now() + ".csv"));
        if (fc.showSaveDialog(t) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        if (f.exists() && !Ui.confirmar(t, "El archivo ya existe. ¿Desea reemplazarlo?")) return;
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < t.getColumnCount(); c++) { if (c > 0) sb.append(','); sb.append(csv(t.getColumnName(c))); }
            w.write(sb.append("\r\n").toString());
            for (int r = 0; r < t.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < t.getColumnCount(); c++) { if (c > 0) sb.append(','); sb.append(csv(texto(t.getValueAt(r, c)))); }
                w.write(sb.append("\r\n").toString());
            }
            Ui.info(t, "Se exportaron " + t.getRowCount() + " registros a:\n" + f.getAbsolutePath());
        } catch (IOException e) {
            Ui.error(t, "No se pudo guardar el archivo: " + e.getMessage());
        }
    }
}
