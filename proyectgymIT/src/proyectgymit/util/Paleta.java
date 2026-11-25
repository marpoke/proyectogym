package proyectgymit.util;

import java.awt.Color;
import java.awt.Font;

/** Colores y fuente de la aplicación (los mismos que se usan en los formularios del diseñador). */
public final class Paleta {

    private Paleta() {}

    public static final Color FONDO = new Color(18, 22, 29);
    public static final Color SUPERFICIE = new Color(26, 32, 44);
    public static final Color TARJETA = new Color(38, 46, 61);
    public static final Color BORDE = new Color(63, 74, 96);
    public static final Color TEXTO = new Color(247, 250, 252);
    public static final Color SUAVE = new Color(148, 163, 184);
    public static final Color ACENTO = new Color(220, 38, 38);
    public static final Color ACENTO_OSCURO = new Color(127, 29, 29);
    public static final Color OK = new Color(74, 222, 128);
    public static final Color MAL = new Color(248, 113, 113);

    public static Font fuente(int estilo, int tam) { return new Font("Segoe UI", estilo, tam); }
}
