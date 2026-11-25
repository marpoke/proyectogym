package proyectgymit.util;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/** Gráficas sencillas dibujadas con Java2D (sin librerías) con los colores del tema. */
public final class Graficos {

    private Graficos() {}

    /** Gráfica de barras verticales. */
    public static class Barras extends JComponent {
        private String[] etiquetas = new String[0];
        private double[] valores = new double[0];
        private boolean dinero;

        /** Constructor sin argumentos: lo usa el diseñador de NetBeans (la propiedad "dinero" se fija en el diseñador). */
        public Barras() { this(false); }

        public boolean isDinero() { return dinero; }

        public void setDinero(boolean dinero) { this.dinero = dinero; repaint(); }

        public Barras(boolean dinero) {
            this.dinero = dinero;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 200));
        }

        public void setDatos(String[] etiquetas, double[] valores) {
            this.etiquetas = etiquetas;
            this.valores = valores;
            repaint();
        }

        private String formato(double v) {
            return dinero ? "$" + (v >= 1000 ? String.format("%.1fk", v / 1000) : String.format("%.0f", v)) : String.valueOf((long) v);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int izq = 34, abajo = 24, arriba = 20, der = 8;
            double max = 0;
            for (double v : valores) max = Math.max(max, v);
            g2.setFont(Paleta.fuente(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            if (valores.length == 0 || max <= 0) {
                g2.setColor(Paleta.SUAVE);
                String m = "Sin datos para mostrar";
                g2.drawString(m, (w - fm.stringWidth(m)) / 2, h / 2);
                g2.dispose();
                return;
            }
            max = escala(max);
            int alto = h - abajo - arriba, ancho = w - izq - der;
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int y = arriba + alto - alto * i / 4;
                g2.setColor(Paleta.BORDE);
                g2.drawLine(izq, y, w - der, y);
                g2.setColor(Paleta.SUAVE);
                String t = formato(max * i / 4);
                g2.drawString(t, izq - 6 - fm.stringWidth(t), y + 4);
            }
            int n = valores.length;
            double paso = (double) ancho / n;
            int bw = (int) Math.min(46, paso * 0.55);
            for (int i = 0; i < n; i++) {
                int bh = (int) Math.round(alto * valores[i] / max);
                int x = (int) (izq + paso * i + (paso - bw) / 2);
                int y = arriba + alto - bh;
                if (bh > 0) {
                    g2.setColor(i == n - 1 ? Paleta.ACENTO : Paleta.ACENTO_OSCURO);
                    g2.fill(new RoundRectangle2D.Float(x, y, bw, bh + 6, 8, 8));
                    g2.setColor(Paleta.TARJETA);
                    g2.fillRect(x - 1, arriba + alto, bw + 2, 8);   // recorta la parte redondeada de abajo
                    g2.setColor(Paleta.BORDE);
                    g2.drawLine(izq, arriba + alto, w - der, arriba + alto);
                    g2.setColor(Paleta.TEXTO);
                    String t = formato(valores[i]);
                    g2.drawString(t, x + (bw - fm.stringWidth(t)) / 2, y - 5);
                }
                g2.setColor(Paleta.SUAVE);
                g2.drawString(etiquetas[i], x + (bw - fm.stringWidth(etiquetas[i])) / 2, h - 7);
            }
            g2.dispose();
        }

        /** Redondea el máximo a un valor "bonito" para que las líneas guía sean legibles. */
        private static double escala(double max) {
            double p = Math.pow(10, Math.floor(Math.log10(max)));
            double[] pasos = {1, 2, 4, 5, 8, 10};
            for (double s : pasos) if (max <= s * p) return s * p;
            return 10 * p;
        }
    }
}
