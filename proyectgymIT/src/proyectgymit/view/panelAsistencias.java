/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectgymit.controler.AsistenciasC;
import proyectgymit.controler.ClienteC;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Ui;

/**
 * Control de asistencias con búsqueda dinámica de clientes: al escribir (nombre, correo, teléfono o
 * número) la lista se filtra sola; se elige al cliente y «Registrar Entrada» usa la fecha y hora actuales.
 * El diseño está en panelAsistencias.form.
 */
public class panelAsistencias extends javax.swing.JPanel {

    private final AsistenciasC asistenciasC = new AsistenciasC();
    private final ClienteC clienteC = new ClienteC();
    private DefaultTableModel modelo;
    private DefaultTableModel modeloClientes;
    private final javax.swing.Timer retardo = new javax.swing.Timer(250, e -> buscar());
    private Integer seleccionado = null;      // asistencia elegida en el historial (para editar / eliminar)
    private Integer clienteId = null;         // cliente elegido en la búsqueda
    private String clienteNombre = null;
    private LocalDateTime fechaOriginal = null;
    private boolean ajustando = false;        // evita eventos en cascada al sincronizar las dos tablas

    public panelAsistencias() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        modelo = Ui.modelo("ID", "ID Cliente", "Cliente", "Fecha y hora de entrada");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modeloClientes = Ui.modelo("ID", "Cliente", "Correo", "Membresía");
        tablaClientes.setModel(modeloClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTextField6.setToolTipText("Escriba parte del nombre, correo, teléfono o el número (#) del cliente.");
        retardo.setRepeats(false);
        Ui.alCambiar(jTextField6, () -> { if (!ajustando) retardo.restart(); });
        Ui.alSeleccionar(tablaClientes, this::elegirCliente);
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        aplicarPermisos();
        cargarTabla();
        buscar();
    }

    private void aplicarPermisos() {
        // ENTRENADOR: solo lectura. ADMIN y RECEPCION: lectura y escritura.
        boolean escribe = Permisos.puedeEscribir(Modulo.ASISTENCIA);
        Ui.habilitar(escribe, jButton3, jButton4, jButton6, jDateChooser1);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : asistenciasC.listarAsistenciasTabla()) modelo.addRow(fila);
    }

    /** Búsqueda dinámica: se ejecuta al dejar de teclear. Un número exacto también localiza por id. */
    private void buscar() {
        String txt = jTextField6.getText().trim();
        Integer previo = clienteId;
        ajustando = true;
        try {
            modeloClientes.setRowCount(0);
            try {
                for (Object[] f : clienteC.listarClientesTabla(txt.replace("#", ""))) {
                    modeloClientes.addRow(new Object[]{f[0], f[1], f[2], f[5]});
                }
                if (txt.matches("#?\\d+")) {   // id exacto aunque no aparezca en nombre/correo/teléfono
                    int id = Integer.parseInt(txt.replace("#", ""));
                    int pos = -1;
                    for (int r = 0; r < modeloClientes.getRowCount(); r++) if (((Integer) modeloClientes.getValueAt(r, 0)) == id) pos = r;
                    if (pos >= 0) modeloClientes.moveRow(pos, pos, 0);     // el id exacto va primero
                    else {
                        String[] c = clienteC.obtenerClientePorId(id);
                        if (c != null) modeloClientes.insertRow(0, new Object[]{id, c[0], c[1], "—"});
                    }
                }
            } catch (SecurityException ex) {
                lblEstado.setText("Su rol no puede consultar la lista de clientes.");
            }
            // conserva la selección si el cliente sigue en los resultados
            tablaClientes.clearSelection();
            if (previo != null) {
                for (int r = 0; r < modeloClientes.getRowCount(); r++) {
                    if (((Integer) modeloClientes.getValueAt(r, 0)).equals(previo)) { int v = tablaClientes.convertRowIndexToView(r); tablaClientes.setRowSelectionInterval(v, v); break; }
                }
            }
        } finally { ajustando = false; }
        if (previo != null && tablaClientes.getSelectedRow() < 0) { clienteId = null; clienteNombre = null; }
        actualizarEstado();
    }

    private void elegirCliente() {
        if (ajustando) return;
        int f = tablaClientes.getSelectedRow();
        if (f < 0) return;
        clienteId = (Integer) tablaClientes.getValueAt(f, 0);
        clienteNombre = String.valueOf(tablaClientes.getValueAt(f, 1));
        actualizarEstado();
    }

    private void actualizarEstado() {
        if (clienteId == null) {
            lblEstado.setText(modeloClientes.getRowCount() == 0
                    ? "Sin resultados: revise el nombre o el número del cliente."
                    : "Seleccione un cliente de la lista (" + modeloClientes.getRowCount() + " resultado(s)).");
            return;
        }
        int f = tablaClientes.getSelectedRow();
        String mem = f < 0 ? "" : String.valueOf(tablaClientes.getValueAt(f, 3));
        String texto = (seleccionado != null ? "Asistencia #" + seleccionado + " → " : "Cliente: ") + clienteNombre + " (#" + clienteId + ")";
        if (!mem.isEmpty() && !mem.equals("—")) texto += "  ·  " + mem;
        if (mem.equals("Sin membresía") || (!mem.isEmpty() && !mem.contains("ACTIVA") && !mem.equals("—")))
            texto += "  ·  ⚠ sin membresía activa";
        lblEstado.setText(texto);
    }

    /** Al elegir una entrada del historial se carga su cliente y fecha para poder editarla o eliminarla. */
    private void cargarSeleccion() {
        if (ajustando) return;
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        clienteId = (Integer) jTable1.getValueAt(f, 1);
        clienteNombre = String.valueOf(jTable1.getValueAt(f, 2));
        fechaOriginal = (LocalDateTime) jTable1.getValueAt(f, 3);
        jDateChooser1.setDate(Ui.aDate(fechaOriginal.toLocalDate()));
        ajustando = true;
        try { tablaClientes.clearSelection(); } finally { ajustando = false; }
        lblEstado.setText("Asistencia #" + seleccionado + " de " + clienteNombre + " (#" + clienteId + "). "
                + "Para cambiar de cliente búsquelo y selecciónelo arriba; luego pulse Editar.");
    }

    private void limpiar() {
        ajustando = true;
        try {
            jTable1.clearSelection();
            tablaClientes.clearSelection();
            jTextField6.setText("");
            jDateChooser1.setDate(null);
        } finally { ajustando = false; }
        seleccionado = null;
        fechaOriginal = null;
        clienteId = null;
        clienteNombre = null;
        buscar();
    }

    private boolean exigirCliente() {
        if (clienteId != null) return true;
        Ui.error(this, "Busque y seleccione un cliente de la lista.");
        jTextField6.requestFocusInWindow();
        return false;
    }

    private void registrar() {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        if (!exigirCliente()) return;
        LocalDate hoy = LocalDate.now();
        for (int r = 0; r < modelo.getRowCount(); r++) {
            LocalDateTime t = (LocalDateTime) modelo.getValueAt(r, 3);
            if (((Integer) modelo.getValueAt(r, 1)).equals(clienteId) && t.toLocalDate().equals(hoy)) {
                if (!Ui.confirmar(this, clienteNombre + " ya registró entrada hoy. ¿Registrar otra?")) return;
                break;
            }
        }
        String error = asistenciasC.registrarAsistencia(clienteId);
        if (error != null) { Ui.error(this, error); return; }
        String quien = clienteNombre;
        limpiar();
        cargarTabla();
        lblEstado.setText("✔ Entrada registrada: " + quien + " · " + java.time.LocalTime.now().withNano(0));
    }

    private void editar() {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        if (seleccionado == null) { Ui.error(this, "Seleccione una asistencia del historial."); return; }
        if (!exigirCliente()) return;
        LocalDate dia = Ui.aLocalDate(jDateChooser1.getDate());
        if (dia == null) { Ui.error(this, "Seleccione la fecha."); return; }
        if (dia.isAfter(LocalDate.now())) { Ui.error(this, "La fecha de entrada no puede ser futura."); return; }
        if (asistenciasC.actualizarAsistencia(seleccionado, clienteId, LocalDateTime.of(dia, fechaOriginal.toLocalTime()))) {
            limpiar(); cargarTabla(); lblEstado.setText("✔ Asistencia actualizada.");
        } else Ui.error(this, "No se pudo actualizar (¿existe el cliente?).");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.ASISTENCIA);
        if (seleccionado == null) { Ui.error(this, "Seleccione una asistencia del historial."); return; }
        if (!Ui.confirmar(this, "¿Eliminar este registro de asistencia?")) return;
        if (asistenciasC.eliminarAsistencia(seleccionado)) { limpiar(); cargarTabla(); lblEstado.setText("✔ Asistencia eliminada."); }
        else Ui.error(this, "No se pudo eliminar la asistencia.");
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
        jTextField6 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        lblEstado = new javax.swing.JLabel();

        setBackground(new java.awt.Color(26, 32, 44));
        setPreferredSize(new java.awt.Dimension(1200, 846));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("CONTROL DE ASISTENCIAS");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Control de asistencias de clientes:");

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));

        jLabel1.setBackground(new java.awt.Color(247, 250, 252));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(247, 250, 252));
        jLabel1.setText("Buscar cliente (nombre, correo, teléfono o número):");

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(220, 38, 38));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Registrar Entrada");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setBackground(new java.awt.Color(46, 56, 75));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(247, 250, 252));
        jButton4.setText("Editar");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton6.setFocusPainted(false);
        jButton6.setBackground(new java.awt.Color(90, 24, 30));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(247, 250, 252));
        jButton6.setText("Eliminar");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton5.setFocusPainted(false);
        jButton5.setBackground(new java.awt.Color(46, 56, 75));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setForeground(new java.awt.Color(247, 250, 252));
        jButton5.setText("Limpiar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Fecha:");

        tablaClientes.setFillsViewportHeight(true);
        tablaClientes.setShowGrid(false);
        tablaClientes.setRowHeight(28);
        tablaClientes.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tablaClientes.setSelectionBackground(new java.awt.Color(127, 29, 29));
        tablaClientes.setBackground(new java.awt.Color(26, 32, 44));
        tablaClientes.setForeground(new java.awt.Color(247, 250, 252));
        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cliente", "Correo", "Membresía"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaClientes.getTableHeader().setBackground(new java.awt.Color(18, 22, 29));
        tablaClientes.getTableHeader().setForeground(new java.awt.Color(148, 163, 184));
        tablaClientes.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 12));
        jScrollPane2.setViewportView(tablaClientes);

        lblEstado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(148, 163, 184));
        lblEstado.setText("Escriba para buscar y seleccione un cliente de la lista.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1141, Short.MAX_VALUE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(8, 8, 8)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addGap(6, 6, 6)
                        .addComponent(jButton4)
                        .addGap(6, 6, 6)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 500, Short.MAX_VALUE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblEstado)
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(jButton6)
                    .addComponent(jButton5))
                .addGap(16, 16, 16))
        );

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1165, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        if (modeloClientes.getRowCount() > 0 && tablaClientes.getSelectedRow() < 0) tablaClientes.setRowSelectionInterval(0, 0);
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        registrar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        editar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JTable tablaClientes;
    // End of variables declaration//GEN-END:variables
}
