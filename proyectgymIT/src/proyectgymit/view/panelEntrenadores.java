/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectgymit.controler.EntrenadoresC;
import proyectgymit.controler.UsuarioC;
import proyectgymit.model.Entrenadores;
import proyectgymit.model.Usuario;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Ui;

/**
 *
 * @author mario
 */
public class panelEntrenadores extends javax.swing.JPanel {

    /**
     * Creates new form panelEntrenadores
     */
    private final EntrenadoresC entrenadorC = new EntrenadoresC();
    private final UsuarioC usuarioC = new UsuarioC();
    private DefaultTableModel modelo;
    private Integer seleccionado = null;   // id de la tabla entrenadores

    public panelEntrenadores() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        proyectgymit.util.Validacion.obligatorio(jTextField1);
        proyectgymit.util.Validacion.correo(jTextField3, true);
        modelo = Ui.modelo("ID", "Nombre", "Correo", "Especialidad", "Teléfono");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTextField2.setToolTipText("En un entrenador existente, déjela vacía para no cambiarla (mínimo 8 caracteres al crearla).");
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        Ui.alCambiar(jTextField6, this::cargarTabla);
        aplicarPermisos();
        cargarTabla();
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.ENTRENADORES);
        Ui.habilitar(escribe, jButton1, jButton2, jButton3, jTextField1, jTextField2, jTextField3, jTextField4, jTextField5);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : entrenadorC.listarEntrenadoresTabla(jTextField6.getText())) modelo.addRow(fila);
    }

    private String txt(Object o) { return o == null ? "" : o.toString(); }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        jTextField1.setText(txt(jTable1.getValueAt(f, 1)));
        jTextField3.setText(txt(jTable1.getValueAt(f, 2)));
        jTextField2.setText("");
        jTextField4.setText(txt(jTable1.getValueAt(f, 3)));
        jTextField5.setText(txt(jTable1.getValueAt(f, 4)));
    }

    private void limpiar() {
        jTextField1.setText(""); jTextField2.setText(""); jTextField3.setText("");
        jTextField4.setText(""); jTextField5.setText("");
        jTable1.clearSelection();
        seleccionado = null;
    }

    private boolean existeCorreoDeOtro(String correo) {
        for (Entrenadores e : entrenadorC.listarEntrenadores()) {
            if (e.getId() == seleccionado) return usuarioC.existeCorreo(correo, e.getUsuarioID());
        }
        return usuarioC.existeCorreo(correo, 0);
    }

    private void guardar() {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        String nombre = jTextField1.getText().trim();
        String correo = jTextField3.getText().trim();
        String pass = new String(jTextField2.getPassword());
        String esp = jTextField4.getText().trim();
        if (Ui.vacio(nombre) || Ui.vacio(esp)) { Ui.error(this, "Nombre y especialidad son obligatorios."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (pass.length() < 8) { Ui.error(this, "La contraseña debe tener al menos 8 caracteres."); return; }
        if (usuarioC.existeCorreo(correo, 0)) { Ui.error(this, "Ya existe un usuario con ese correo."); return; }
        Usuario u = new Usuario();
        u.setNombre(nombre); u.setCorreo(correo); u.setPassword(pass);
        Entrenadores e = new Entrenadores();
        e.setEspecialidad(esp); e.setTelefono(jTextField5.getText().trim());
        if (entrenadorC.registrarEntrenadorCompleto(u, e)) {
            Ui.info(this, "Entrenador registrado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo registrar el entrenador.");
    }

    private void actualizar() {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        if (seleccionado == null) { Ui.error(this, "Seleccione un entrenador de la tabla."); return; }
        String nombre = jTextField1.getText().trim();
        String correo = jTextField3.getText().trim();
        String pass = new String(jTextField2.getPassword());
        if (Ui.vacio(nombre) || Ui.vacio(jTextField4.getText())) { Ui.error(this, "Nombre y especialidad son obligatorios."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (!pass.isEmpty() && pass.length() < 8) { Ui.error(this, "La nueva contraseña debe tener al menos 8 caracteres."); return; }
        if (existeCorreoDeOtro(correo)) { Ui.error(this, "Ya existe otro usuario con ese correo."); return; }
        if (entrenadorC.actualizarEntrenador(seleccionado, nombre, correo, jTextField4.getText().trim(), jTextField5.getText().trim(), pass)) {
            Ui.info(this, "Entrenador actualizado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo actualizar el entrenador.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.ENTRENADORES);
        if (seleccionado == null) { Ui.error(this, "Seleccione un entrenador de la tabla."); return; }
        if (!Ui.confirmar(this, "Se eliminará al entrenador (sus rutinas quedarán sin entrenador asignado).\n¿Desea continuar?")) return;
        if (entrenadorC.eliminarEntrenador(seleccionado)) {
            Ui.info(this, "Entrenador eliminado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo eliminar el entrenador.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JPasswordField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField6 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        jLabel5.setBackground(new java.awt.Color(247, 250, 252));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(247, 250, 252));
        jLabel5.setText("Nombre completo:");

        setBackground(new java.awt.Color(26, 32, 44));
        setForeground(new java.awt.Color(26, 32, 44));

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));

        jTextField1.setForeground(new java.awt.Color(247, 250, 252));
        jTextField1.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField1.setBackground(new java.awt.Color(20, 25, 35));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jTextField2.setForeground(new java.awt.Color(247, 250, 252));
        jTextField2.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField2.setBackground(new java.awt.Color(20, 25, 35));
        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jTextField3.setForeground(new java.awt.Color(247, 250, 252));
        jTextField3.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField3.setBackground(new java.awt.Color(20, 25, 35));
        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField3.addActionListener(this::jTextField3ActionPerformed);

        jTextField4.setForeground(new java.awt.Color(247, 250, 252));
        jTextField4.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField4.setBackground(new java.awt.Color(20, 25, 35));
        jTextField4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField4.addActionListener(this::jTextField4ActionPerformed);

        jTextField5.setForeground(new java.awt.Color(247, 250, 252));
        jTextField5.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField5.setBackground(new java.awt.Color(20, 25, 35));
        jTextField5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField5.addActionListener(this::jTextField5ActionPerformed);

        jLabel1.setBackground(new java.awt.Color(247, 250, 252));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(247, 250, 252));
        jLabel1.setText("Nombre completo:");

        jLabel2.setBackground(new java.awt.Color(247, 250, 252));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Correo electronico:");

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Contraseña:");

        jLabel8.setBackground(new java.awt.Color(247, 250, 252));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(247, 250, 252));
        jLabel8.setText("Especialidad:");

        jLabel9.setBackground(new java.awt.Color(247, 250, 252));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(247, 250, 252));
        jLabel9.setText("Telefono:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(153, 153, 153))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(139, 139, 139)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(440, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("GESTION DE ENTRENADORES:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Control de entrenadores");

        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setRowHeight(32);
        jTable1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jTable1.setSelectionBackground(new java.awt.Color(127, 29, 29));
        jTable1.setBackground(new java.awt.Color(26, 32, 44));
        jTable1.setForeground(new java.awt.Color(247, 250, 252));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.getTableHeader().setBackground(new java.awt.Color(18, 22, 29));
        jTable1.getTableHeader().setForeground(new java.awt.Color(148, 163, 184));
        jTable1.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 12));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setFocusPainted(false);
        jButton1.setBackground(new java.awt.Color(46, 56, 75));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(247, 250, 252));
        jButton1.setText("Actualizar");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFocusPainted(false);
        jButton2.setBackground(new java.awt.Color(220, 38, 38));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(247, 250, 252));
        jButton2.setText("Guardar Entrenador");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(90, 24, 30));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Eliminar entrenador");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setBackground(new java.awt.Color(46, 56, 75));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(247, 250, 252));
        jButton4.setText("Limpiar");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("BUSCAR:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1132, Short.MAX_VALUE))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jButton2)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        actualizar();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPasswordField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
