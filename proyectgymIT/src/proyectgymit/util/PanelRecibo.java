package proyectgymit.util;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.time.LocalDate;

/**
 * Hoja del recibo de pago (componente propio para el diseñador, también imprimible).
 * Se coloca en DialogRecibo.form; los datos se asignan con {@link #setDatos}.
 */
public class PanelRecibo extends javax.swing.JPanel implements Printable {

    private int id;
    private String cliente = "", metodo = "";
    private double monto;
    private LocalDate fecha;

    public PanelRecibo() {
        setPreferredSize(new Dimension(420, 460));
        setBackground(Color.WHITE);
    }

    public void setDatos(int id, String cliente, double monto, String metodo, LocalDate fecha) {
        this.id = id; this.cliente = cliente; this.monto = monto; this.metodo = metodo; this.fecha = fecha;
        repaint();
    }

    private void dibujar(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(220, 38, 38));
        g2.fillRect(0, 0, w, 10);
        Color tinta = new Color(15, 23, 42), suave = new Color(100, 116, 139);
        g2.setColor(tinta);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g2.drawString("GOJOGYM", 32, 62);
        g2.setColor(suave);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.drawString("Recibo de pago No. " + String.format("%05d", id), 32, 86);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(226, 232, 240));
        g2.drawLine(32, 104, w - 32, 104);
        int y = 144;
        String[][] filas = {{"Cliente", cliente}, {"Fecha", fecha == null ? "" : fecha.toString()}, {"Método de pago", metodo}};
        for (String[] f : filas) {
            g2.setColor(suave);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString(f[0], 32, y);
            g2.setColor(tinta);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(f[1], 32, y + 24);
            y += 62;
        }
        g2.setColor(new Color(254, 242, 242));
        g2.fillRoundRect(32, y + 4, w - 64, 70, 12, 12);
        g2.setColor(suave);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.drawString("Total pagado", 48, y + 30);
        g2.setColor(new Color(185, 28, 28));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g2.drawString(String.format("$%,.2f", monto), 48, y + 62);
        g2.setColor(suave);
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        g2.drawString("Gracias por entrenar con nosotros.", 32, h - 24);
    }

    @Override
    protected void paintComponent(Graphics g) {
        dibujar((Graphics2D) g.create(), getWidth(), getHeight());
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pagina) {
        if (pagina > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        double esc = Math.min(1.0, pf.getImageableWidth() / 420.0);
        g2.scale(esc, esc);
        dibujar(g2, 420, 460);
        return PAGE_EXISTS;
    }
}
