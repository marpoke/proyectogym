/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectgymit.controler.UsuarioC;
import proyectgymit.model.Usuario;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.security.Sesion;
import proyectgymit.util.Catalogos;
import proyectgymit.util.Ui;

/**
 *
 * @author mario
 */
public class panelGestionU extends javax.swing.JPanel {

    /**
     * Creates new form panelGestionU
     */
    private final UsuarioC usuarioC = new UsuarioC();
    private DefaultTableModel modelo;
    private Integer seleccionado = null;     // id de usuarios
    private String rolOriginal = null;

    public panelGestionU() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        proyectgymit.util.Validacion.obligatorio(jTextField7);
        proyectgymit.util.Validacion.correo(jTextField8, true);
        modelo = Ui.modelo("ID", "Nombre", "Correo", "Rol");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jComboBox1.setModel(new DefaultComboBoxModel<>(Catalogos.ROLES));
        jTextField9.setToolTipText("En un usuario existente, déjela vacía para no cambiarla (mínimo 8 caracteres al crearla).");
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        Ui.alCambiar(jTextField6, this::cargarTabla);
        aplicarPermisos();
        cargarTabla();
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.USUARIOS);
        Ui.habilitar(escribe, jButton1, jButton2, jButton3, jTextField7, jTextField8, jTextField9, jComboBox1);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Usuario u : usuarioC.busquedaUsuarioDinamica(jTextField6.getText())) {
            modelo.addRow(new Object[]{u.getId(), u.getNombre(), u.getCorreo(), u.getRol()});
        }
    }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        jTextField7.setText(String.valueOf(jTable1.getValueAt(f, 1)));
        jTextField8.setText(String.valueOf(jTable1.getValueAt(f, 2)));
        jTextField9.setText("");
        rolOriginal = String.valueOf(jTable1.getValueAt(f, 3));
        jComboBox1.setSelectedItem(rolOriginal);
    }

    private void limpiar() {
        jTextField7.setText(""); jTextField8.setText(""); jTextField9.setText("");
        jComboBox1.setSelectedIndex(0);
        jTable1.clearSelection();
        seleccionado = null;
        rolOriginal = null;
    }

    private static boolean esPersonal(String rol) { return Permisos.ADMIN.equals(rol) || Permisos.RECEPCION.equals(rol); }

    private void guardar() {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        String nombre = jTextField7.getText().trim();
        String correo = jTextField8.getText().trim();
        String pass = new String(jTextField9.getPassword());
        String rol = String.valueOf(jComboBox1.getSelectedItem());
        if (Ui.vacio(nombre)) { Ui.error(this, "El nombre es obligatorio."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (pass.length() < 8) { Ui.error(this, "La contraseña debe tener al menos 8 caracteres."); return; }
        if (!esPersonal(rol)) { Ui.error(this, "Aquí solo se registra personal ADMIN o RECEPCION.\nLos entrenadores y clientes se registran en sus módulos."); return; }
        if (usuarioC.existeCorreo(correo, 0)) { Ui.error(this, "Ya existe un usuario con ese correo."); return; }
        Usuario u = new Usuario();
        u.setNombre(nombre); u.setCorreo(correo); u.setPassword(pass); u.setRol(rol);
        if (usuarioC.registrarPersonal(u)) {
            Ui.info(this, "Personal registrado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo registrar el personal.");
    }

    private void actualizar() {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un usuario de la tabla."); return; }
        String nombre = jTextField7.getText().trim();
        String correo = jTextField8.getText().trim();
        String pass = new String(jTextField9.getPassword());
        String rol = String.valueOf(jComboBox1.getSelectedItem());
        if (Ui.vacio(nombre)) { Ui.error(this, "El nombre es obligatorio."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (!pass.isEmpty() && pass.length() < 8) { Ui.error(this, "La nueva contraseña debe tener al menos 8 caracteres."); return; }
        if (usuarioC.existeCorreo(correo, seleccionado)) { Ui.error(this, "Ya existe otro usuario con ese correo."); return; }
        if (!rol.equals(rolOriginal)) {
            if (!esPersonal(rolOriginal) || !esPersonal(rol)) {
                Ui.error(this, "El rol de un entrenador o cliente no se cambia aquí (tienen datos propios en su módulo).");
                return;
            }
            if (Permisos.ADMIN.equals(rolOriginal) && usuarioC.contarPorRol(Permisos.ADMIN) <= 1) {
                Ui.error(this, "No se puede quitar el rol al único administrador.");
                return;
            }
        }
        Usuario u = new Usuario();
        u.setId(seleccionado); u.setNombre(nombre); u.setCorreo(correo); u.setRol(rol);
        if (usuarioC.actualizarUsuario(u, pass)) {
            Ui.info(this, "Usuario actualizado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo actualizar el usuario.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.USUARIOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un usuario de la tabla."); return; }
        if (Sesion.getUsuario() != null && Sesion.getUsuario().getId() == seleccionado) { Ui.error(this, "No puede eliminar su propia cuenta."); return; }
        if (!esPersonal(rolOriginal)) { Ui.error(this, "Los entrenadores y clientes se eliminan desde su propio módulo."); return; }
        if (Permisos.ADMIN.equals(rolOriginal) && usuarioC.contarPorRol(Permisos.ADMIN) <= 1) { Ui.error(this, "No se puede eliminar al único administrador."); return; }
        if (!Ui.confirmar(this, "¿Eliminar este usuario?")) return;
        if (usuarioC.eliminarPersonal(seleccionado)) {
            Ui.info(this, "Usuario eliminado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo eliminar el usuario.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JPasswordField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(26, 32, 44));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("GESTION Y REGISTRO DE PERSONAL:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Control de personal:");

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));

        jLabel1.setBackground(new java.awt.Color(247, 250, 252));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(247, 250, 252));
        jLabel1.setText("Nombre completo:");

        jTextField7.setForeground(new java.awt.Color(247, 250, 252));
        jTextField7.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField7.setBackground(new java.awt.Color(20, 25, 35));
        jTextField7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField7.addActionListener(this::jTextField7ActionPerformed);

        jLabel2.setBackground(new java.awt.Color(247, 250, 252));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Correo electronico:");

        jTextField8.setForeground(new java.awt.Color(247, 250, 252));
        jTextField8.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField8.setBackground(new java.awt.Color(20, 25, 35));
        jTextField8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField8.addActionListener(this::jTextField8ActionPerformed);

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Contraseña:");

        jTextField9.setForeground(new java.awt.Color(247, 250, 252));
        jTextField9.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField9.setBackground(new java.awt.Color(20, 25, 35));
        jTextField9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField9.addActionListener(this::jTextField9ActionPerformed);

        jComboBox1.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox1.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setBackground(new java.awt.Color(247, 250, 252));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("Rol:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(59, Short.MAX_VALUE))
        );

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jButton1.setFocusPainted(false);
        jButton1.setBackground(new java.awt.Color(220, 38, 38));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(247, 250, 252));
        jButton1.setText("Guardar personal");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFocusPainted(false);
        jButton2.setBackground(new java.awt.Color(46, 56, 75));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(247, 250, 252));
        jButton2.setText("Actualizar");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(90, 24, 30));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Eliminar");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setBackground(new java.awt.Color(46, 56, 75));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(247, 250, 252));
        jButton4.setText("Limpiar");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(247, 250, 252));
        jLabel12.setText("BUSCAR:");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(26, 26, 26)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        actualizar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JPasswordField jTextField9;
    // End of variables declaration//GEN-END:variables
}
