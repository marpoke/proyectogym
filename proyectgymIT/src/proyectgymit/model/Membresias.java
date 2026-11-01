/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

import java.time.LocalDate;

/**
 *
 * @author mario
 */
public class Membresias {
    private int id; 
    private int clienteID; 
    private String tipoPlan;
    private LocalDate fechaInicio; 
    private LocalDate fechaFinal; 
    private String estado;
    
    public Membresias (){}
    
    public Membresias(int id, int clienteID, String tipoPlan, LocalDate fechaInicio, LocalDate fechaFinal, String estado ){
    this.id = id;
    this.clienteID = clienteID;
    this.tipoPlan = tipoPlan;
    this.fechaInicio = fechaInicio;
    this.fechaFinal = fechaFinal;
    this.estado = estado;
    
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getClienteID() {return clienteID;}
    public void setClienteID(int clienteID) {this.clienteID = clienteID;}

    public String getTipoPlan() {return tipoPlan;}
    public void setTipoPlan(String tipoPlan) {this.tipoPlan = tipoPlan;}

    public LocalDate getFechaInicio() {return fechaInicio;}
    public void setFechaInicio(LocalDate fechaInicio) {this.fechaInicio = fechaInicio;}

    public LocalDate getFechaFinal() {return fechaFinal;}
    public void setFechaFinal(LocalDate fechaFinal) {this.fechaFinal = fechaFinal;}

    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}
    
    
    
}
