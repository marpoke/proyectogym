/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

/**
 *
 * @author mario
 */
public class Ejercicios {
    private int id;
    private String nombre;
    private String grupoMuscular;
    private String descripcion;

    public Ejercicios() {}
    
    
    public Ejercicios(int id, String nombre, String grupoMuscular, String descripcion) {
        this.id = id; 
        this.nombre = nombre; 
        this.grupoMuscular = grupoMuscular; 
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
}
