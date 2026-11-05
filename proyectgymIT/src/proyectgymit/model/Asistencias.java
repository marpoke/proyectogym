/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

import java.time.LocalDateTime;

/**
 *
 * @author mario
 */
public class Asistencias {
    private int id;
    private int clienteID;
    private LocalDateTime fechaEntrada;
    
    public Asistencias(){}
    
    public Asistencias(int id, int clienteID, LocalDateTime fechaEntrada){
    this.id = id;
    this.clienteID = clienteID;
    this.fechaEntrada = fechaEntrada;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getClienteID() { return clienteID;}
    public void setClienteID(int clienteID) {this.clienteID = clienteID;}

    public LocalDateTime getFechaEntrada() {return fechaEntrada;}
    public void setFechaEntrada(LocalDateTime fechaEntrada) {this.fechaEntrada = fechaEntrada;}
    
    
    
}
