/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

/**
 *
 * @author mario
 */
public class RutinaEjercicio {
    private int id;
    private int rutinaID;
    private int ejercicioID;
    private int series;
    private int repeticiones;

    public RutinaEjercicio() {}
    public RutinaEjercicio(int id, int rutinaID, int ejercicioID, int series, int repeticiones) {
        this.id = id; 
        this.rutinaID = rutinaID; 
        this.ejercicioID = ejercicioID; 
        this.series = series; 
        this.repeticiones = repeticiones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRutinaID() {
        return rutinaID;
    }

    public void setRutinaID(int rutinaID) {
        this.rutinaID = rutinaID;
    }

    public int getEjercicioID() {
        return ejercicioID;
    }

    public void setEjercicioID(int ejercicioID) {
        this.ejercicioID = ejercicioID;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }
    
}
