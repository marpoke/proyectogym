package proyectgymit.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Panel decorativo con la marca (se coloca desde el diseñador en la pantalla de inicio de sesión). */
public class PanelMarca extends JPanel {

    private String titulo = "GOJOGYM";
    private String linea1 = "Gestión de socios, membresías,";
    private String linea2 = "pagos, asistencias y rutinas.";

    public PanelMarca() {
        setOpaque(false);
        setPreferredSize(new Dimension(470, 560));
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; repaint(); }
    public String getLinea1() { return linea1; }
    public void setLinea1(String linea1) { this.linea1 = linea1; repaint(); }
    public String getLinea2() { return linea2; }
    public void setLinea2(String linea2) { this.linea2 = linea2; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, Paleta.FONDO, w, h, Paleta.SUPERFICIE));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new java.awt.Color(220, 38, 38, 38));
        g2.fillOval(-w / 3, h - w / 2, w, w);
        g2.setColor(new java.awt.Color(220, 38, 38, 24));
        g2.fillOval(w / 2, -w / 4, w, w);
        g2.setColor(Paleta.ACENTO);
        g2.fillRoundRect(48, h / 2 - 96, 56, 6, 6, 6);
        g2.setColor(Paleta.TEXTO);
        g2.setFont(Paleta.fuente(Font.BOLD, 54));
        g2.drawString(titulo, 46, h / 2 - 28);
        g2.setColor(Paleta.SUAVE);
        g2.setFont(Paleta.fuente(Font.PLAIN, 17));
        g2.drawString(linea1, 48, h / 2 + 14);
        g2.drawString(linea2, 48, h / 2 + 40);
        g2.dispose();
    }
}
