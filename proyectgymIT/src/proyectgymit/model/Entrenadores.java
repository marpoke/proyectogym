/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

/**
 *
 * @author mario
 */
public class Entrenadores {
    private int id; 
    private int usuarioID; 
    private String especialidad;
    private String telefono; 
    
    public Entrenadores (){}
    
    public Entrenadores(int id, int usuarioID, String especialidad, String telefono){
    this.id = id;
    this.usuarioID = usuarioID;
    this.especialidad = especialidad;
    this.telefono = telefono;
    
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getUsuarioID() {return usuarioID;}
    public void setUsuarioID(int usuarioID) {this.usuarioID = usuarioID;}

    public String getEspecialidad() {return especialidad;}
    public void setEspecialidad(String especialidad) {this.especialidad = especialidad;}

    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}
    
    
    
}
