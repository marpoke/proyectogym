/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectgymit.controler.ClienteC;
import proyectgymit.controler.EntrenadoresC;
import proyectgymit.controler.RutinasC;
import proyectgymit.model.Entrenadores;
import proyectgymit.model.Rutinas;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.security.Sesion;
import proyectgymit.util.Item;
import proyectgymit.util.Ui;

/**
 *
 * @author mario
 */
public class panelRutinas extends javax.swing.JPanel {

    /**
     * Creates new form panelRutinas
     */
    private final RutinasC rutinasC = new RutinasC();
    private List<Item> clientes;
    private List<Item> entrenadores;       // el primero es "Sin asignar" (id 0)
    private int miEntrenadorId = 0;        // si quien inició sesión es ENTRENADOR
    private DefaultTableModel modelo;
    private Integer seleccionado = null;

    public panelRutinas() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        modelo = Ui.modelo("ID", "ID Cliente", "Cliente", "ID Entrenador", "Entrenador", "Rutina", "Ejercicios");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        EntrenadoresC entC = new EntrenadoresC();
        clientes = Ui.llenarCombo(jComboBox2, new ClienteC().listarClientesCombo());
        entrenadores = new ArrayList<>();
        entrenadores.add(new Item(0, "Sin asignar"));
        entrenadores.addAll(entC.listarEntrenadoresCombo());
        Ui.llenarCombo(jComboBox3, entrenadores);
        if (Permisos.ENTRENADOR.equals(Sesion.getRol())) {
            Entrenadores yo = entC.buscarPorUsuarioId(Sesion.getUsuario().getId());
            if (yo != null) miEntrenadorId = yo.getId();
        }
        jTable1.setToolTipText("Doble clic: agregar/quitar los ejercicios de la rutina");
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        Ui.alDobleClic(jTable1, this::abrirDetalle);
        aplicarPermisos();
        limpiar();
        cargarTabla();
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.RUTINAS);
        Ui.habilitar(escribe, jButton3, jButton4, jButton6, jComboBox2, jComboBox3, jTextField6);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : rutinasC.listarRutinasTabla()) modelo.addRow(fila);
    }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        Ui.seleccionarPorId(jComboBox2, clientes, Integer.parseInt(String.valueOf(jTable1.getValueAt(f, 1))));
        Object ent = jTable1.getValueAt(f, 3);
        Ui.seleccionarPorId(jComboBox3, entrenadores, ent == null ? 0 : Integer.parseInt(ent.toString()));
        jTextField6.setText(String.valueOf(jTable1.getValueAt(f, 5)));
    }

    private void limpiar() {
        jTable1.clearSelection();
        seleccionado = null;
        jTextField6.setText("");
        Ui.seleccionarPorId(jComboBox3, entrenadores, miEntrenadorId);
    }

    private Rutinas leerFormulario() {
        int clienteId = Ui.idSeleccionado(jComboBox2, clientes);
        if (clienteId < 0) { Ui.error(this, "Seleccione un cliente."); return null; }
        if (Ui.vacio(jTextField6.getText())) { Ui.error(this, "Escriba el nombre de la rutina."); return null; }
        int ent = Ui.idSeleccionado(jComboBox3, entrenadores);
        Rutinas r = new Rutinas();
        r.setClienteID(clienteId);
        r.setEntrenadorID(ent > 0 ? ent : null);
        r.setNombreRutina(jTextField6.getText().trim());
        return r;
    }

    private void guardar() {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        Rutinas r = leerFormulario();
        if (r == null) return;
        int id = rutinasC.crearRutina(r);
        if (id > 0) {
            Ui.info(this, "Rutina guardada. Haga doble clic sobre ella para agregarle ejercicios.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo guardar la rutina.");
    }

    private void editar() {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        if (seleccionado == null) { Ui.error(this, "Seleccione una rutina de la tabla."); return; }
        Rutinas r = leerFormulario();
        if (r == null) return;
        r.setId(seleccionado);
        if (rutinasC.actualizarRutina(r)) { Ui.info(this, "Rutina actualizada."); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo actualizar la rutina.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.RUTINAS);
        if (seleccionado == null) { Ui.error(this, "Seleccione una rutina de la tabla."); return; }
        if (!Ui.confirmar(this, "Se eliminará la rutina con todos sus ejercicios.\n¿Desea continuar?")) return;
        if (rutinasC.eliminarRutina(seleccionado)) { Ui.info(this, "Rutina eliminada."); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo eliminar la rutina.");
    }

    private void abrirDetalle() {
        if (seleccionado == null) return;
        String nombre = String.valueOf(jTable1.getValueAt(jTable1.getSelectedRow(), 5));
        new DialogRutinaEjercicios(javax.swing.SwingUtilities.getWindowAncestor(this), seleccionado, nombre).setVisible(true);
        int fila = jTable1.getSelectedRow();
        cargarTabla();
        if (fila >= 0 && fila < jTable1.getRowCount()) jTable1.setRowSelectionInterval(fila, fila);
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
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jTextField6 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(26, 32, 44));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("REGISTRO DE RUTINAS:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Control de registro de rutinas:");

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));

        jComboBox2.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox2.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jLabel2.setBackground(new java.awt.Color(247, 250, 252));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Cliente:");

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Entrenador:");

        jComboBox3.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox3.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(this::jComboBox3ActionPerformed);

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jLabel4.setBackground(new java.awt.Color(247, 250, 252));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("Nombre de la rutina:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(46, 56, 75));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Editar Rutina");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton6.setFocusPainted(false);
        jButton6.setBackground(new java.awt.Color(90, 24, 30));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(247, 250, 252));
        jButton6.setText("Eliminar");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setBackground(new java.awt.Color(220, 38, 38));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(247, 250, 252));
        jButton4.setText("Guardar Rutina");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setFocusPainted(false);
        jButton5.setBackground(new java.awt.Color(46, 56, 75));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setForeground(new java.awt.Color(247, 250, 252));
        jButton5.setText("Limpiar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

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
                    .addComponent(jLabel6)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton6)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        editar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
