/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectgymit.model;

/**
 *
 * @author mario
 */
public class Clientes {
    private int id;
    private int usuarioID;
    private String telefono;
    private String contactoEmergencia;
    
    public Clientes(){}
    
    public Clientes(int id, int usuarioID, String telefono, String contactoEmergencia){
    this.id = id; 
    this.usuarioID = usuarioID;
    this.telefono = telefono;
    this.contactoEmergencia = contactoEmergencia;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getUsuarioID() {return usuarioID;}
    public void setUsuarioID(int usuarioID) {this.usuarioID = usuarioID;}

    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}

    public String getContactoEmergencia() {return contactoEmergencia;}
    public void setContactoEmergencia(String contactoEmergencia) {this.contactoEmergencia = contactoEmergencia;}
    
   
}
