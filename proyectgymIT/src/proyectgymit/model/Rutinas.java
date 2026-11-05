/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

/**
 *
 * @author mario
 */
public class Rutinas {
    private int id ;
    private int clienteID;
    private Integer entrenadorID;
    private String nombreRutina;
    
    public Rutinas(){}
    
    public Rutinas(int id, int clienteID, Integer entrenadorID, String nombreRutina){
    this.id = id; 
    this.clienteID = clienteID;
    this.entrenadorID = entrenadorID;
    this.nombreRutina = nombreRutina;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getClienteID() {return clienteID;}
    public void setClienteID(int clienteID) {this.clienteID = clienteID;}

    public Integer getEntrenadorID() {return entrenadorID;}
    public void setEntrenadorID(Integer entrenadorID) {this.entrenadorID = entrenadorID;}
   
    public String getNombreRutina() {return nombreRutina;}
    public void setNombreRutina(String nombreRutina) {this.nombreRutina = nombreRutina;}
    
    
}
