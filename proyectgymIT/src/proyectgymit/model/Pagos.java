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
public class Pagos {
    private int id; 
    private int clienteID;
    private double monto;
    private String metodoPago;
    private LocalDate fechaPago;
    
    public Pagos(){}
    
    public Pagos(int id, int clienteID, double monto, String metodoPago, LocalDate fechaPago){
    this.id = id ; 
    this.clienteID = clienteID;
    this.monto = monto;
    this.metodoPago = metodoPago;
    this.fechaPago= fechaPago;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getClienteID() {return clienteID;}
    public void setClienteID(int clienteID) {this.clienteID = clienteID;}

    public double getMonto() {return monto;}
    public void setMonto(double monto) {this.monto = monto;}

    public String getMetodoPago() {return metodoPago;}
    public void setMetodoPago(String metodoPago) {this.metodoPago = metodoPago;}

    public LocalDate getFechaPago() {return fechaPago;}
    public void setFechaPago(LocalDate fechaPago) {this.fechaPago = fechaPago;}

    
    
    
}
