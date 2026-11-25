package proyectgymit.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Valores de los combos. Deben coincidir con los ENUM / valores de la base de datos (ver schema.sql). */
public final class Catalogos {
    private Catalogos() {}
    /** Plan -> duración sugerida en días. */
    public static final Map<String, Integer> PLANES = new LinkedHashMap<>();
    static {
        PLANES.put("MENSUAL", 30);
        PLANES.put("TRIMESTRAL", 90);
        PLANES.put("SEMESTRAL", 180);
        PLANES.put("ANUAL", 365);
    }
    public static final String[] METODOS_PAGO = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"};
    public static final String[] ESTADOS_MEMBRESIA = {"ACTIVA", "VENCIDA", "CANCELADA"};
    public static final String[] ROLES = {"ADMIN", "RECEPCION", "ENTRENADOR", "CLIENTE"};
    public static final String[] GRUPOS_MUSCULARES = {"Pecho", "Espalda", "Piernas", "Hombros", "Brazos", "Core", "Cardio", "Cuerpo completo"};
}
