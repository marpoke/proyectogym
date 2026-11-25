/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Catalogos;
import proyectgymit.util.Item;
import proyectgymit.util.Ui;
import proyectgymit.controler.ClienteC;
import proyectgymit.controler.Transacciones;
import proyectgymit.model.Membresias;
import proyectgymit.model.Pagos;

/**
 *
 * @author mario
 */
public class panelMembresias extends javax.swing.JPanel {

    /**
     * Creates new form panelMembresias
     */
    private final Transacciones trx = new Transacciones();
    private final ClienteC clienteC = new ClienteC();
    private List<Item> clientes;
    private DefaultTableModel modelo;
    private Integer seleccionado = null;   // id de la membresía

    public panelMembresias() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        proyectgymit.util.Validacion.numero(jTextField6, false);
        trx.actualizarMembresiasVencidasAutomatico();
        modelo = Ui.modelo("ID", "ID Cliente", "Cliente", "Plan", "Inicio", "Fin", "Estado");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientes = Ui.llenarCombo(jComboBox2, clienteC.listarClientesCombo());
        jComboBox3.setModel(new DefaultComboBoxModel<>(Catalogos.PLANES.keySet().toArray(new String[0])));
        jComboBox4.setModel(new DefaultComboBoxModel<>(Catalogos.ESTADOS_MEMBRESIA));
        jComboBox5.setModel(new DefaultComboBoxModel<>(Catalogos.METODOS_PAGO));
        jTextField6.setToolTipText("Opcional al guardar (si lo llena se registra también el pago). Obligatorio al renovar.");
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        aplicarPermisos();
        limpiar();
        cargarTabla();
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.MEMBRESIAS);
        Ui.habilitar(escribe, jButton2, jButton3, jButton4, jButton6, jButton7, jComboBox2, jComboBox3, jComboBox4, jDateChooser1, jDateChooser2);
        boolean cobra = escribe && Permisos.puedeEscribir(Modulo.PAGOS);
        Ui.habilitar(cobra, jComboBox5, jTextField6);
        jButton2.setEnabled(cobra);   // renovar siempre cobra
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : trx.listarMembresiasTabla()) modelo.addRow(fila);
    }

    private void sugerirFin() {
        if (seleccionado != null || jDateChooser1.getDate() == null) return;
        Integer dias = Catalogos.PLANES.get(String.valueOf(jComboBox3.getSelectedItem()));
        if (dias != null) jDateChooser2.setDate(Ui.aDate(Ui.aLocalDate(jDateChooser1.getDate()).plusDays(dias)));
    }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        Ui.seleccionarPorId(jComboBox2, clientes, Integer.parseInt(String.valueOf(jTable1.getValueAt(f, 1))));
        jComboBox3.setSelectedItem(String.valueOf(jTable1.getValueAt(f, 3)));
        jDateChooser1.setDate(Ui.aDate((LocalDate) jTable1.getValueAt(f, 4)));
        jDateChooser2.setDate(Ui.aDate((LocalDate) jTable1.getValueAt(f, 5)));
        jComboBox4.setSelectedItem(String.valueOf(jTable1.getValueAt(f, 6)));
        jTextField6.setText("");
    }

    private void limpiar() {
        jTable1.clearSelection();
        seleccionado = null;
        jTextField6.setText("");
        jComboBox4.setSelectedItem("ACTIVA");
        jDateChooser1.setDate(new Date());
        sugerirFin();
    }

    /** Lee y valida el formulario. Devuelve null si hay un error (ya mostrado). */
    private Membresias leerFormulario() {
        int clienteId = Ui.idSeleccionado(jComboBox2, clientes);
        if (clienteId < 0) { Ui.error(this, "Seleccione un cliente."); return null; }
        LocalDate ini = Ui.aLocalDate(jDateChooser1.getDate());
        LocalDate fin = Ui.aLocalDate(jDateChooser2.getDate());
        if (ini == null || fin == null) { Ui.error(this, "Seleccione la fecha de inicio y la fecha final."); return null; }
        if (fin.isBefore(ini)) { Ui.error(this, "La fecha final no puede ser anterior a la de inicio."); return null; }
        Membresias m = new Membresias();
        m.setClienteID(clienteId);
        m.setTipoPlan(String.valueOf(jComboBox3.getSelectedItem()));
        m.setFechaInicio(ini);
        m.setFechaFinal(fin);
        m.setEstado(String.valueOf(jComboBox4.getSelectedItem()));
        return m;
    }

    private Double leerMonto(boolean obligatorio) {
        String t = jTextField6.getText().trim();
        if (t.isEmpty()) {
            if (obligatorio) Ui.error(this, "Escriba el monto del pago.");
            return null;
        }
        try {
            double v = Double.parseDouble(t);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            Ui.error(this, "El monto debe ser un número mayor a cero.");
            return -1.0;
        }
    }

    private void guardar() {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        Membresias m = leerFormulario();
        if (m == null) return;
        Double monto = leerMonto(false);
        if (monto != null && monto < 0) return;
        if ("ACTIVA".equals(m.getEstado())) {
            Membresias vigente = trx.buscarMembresiaActiva(m.getClienteID());
            if (vigente != null && !vigente.getFechaFinal().isBefore(LocalDate.now())
                    && !Ui.confirmar(this, "El cliente ya tiene una membresía activa hasta " + vigente.getFechaFinal()
                    + ".\nPara extenderla use «Renovar». ¿Crear otra de todos modos?")) return;
        }
        boolean ok;
        if (monto != null) {
            Pagos p = new Pagos();
            p.setClienteID(m.getClienteID());
            p.setMonto(monto);
            p.setMetodoPago(String.valueOf(jComboBox5.getSelectedItem()));
            p.setFechaPago(LocalDate.now());
            ok = trx.procesarPagoYMembresia(p, m);
        } else ok = trx.registrarMembresia(m);
        if (ok) { Ui.info(this, "Membresía guardada" + (monto != null ? " y pago registrado." : ".")); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo guardar la membresía.");
    }

    private void editar() {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        if (seleccionado == null) { Ui.error(this, "Seleccione una membresía de la tabla."); return; }
        Membresias m = leerFormulario();
        if (m == null) return;
        m.setId(seleccionado);
        if (trx.actualizarMembresia(m)) { Ui.info(this, "Membresía actualizada."); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo actualizar la membresía.");
    }

    private void renovar() {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        Permisos.exigirEscritura(Modulo.PAGOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione la membresía que desea renovar."); return; }
        Double monto = leerMonto(true);
        if (monto == null || monto < 0) return;
        int f = jTable1.getSelectedRow();
        int clienteId = Integer.parseInt(String.valueOf(jTable1.getValueAt(f, 1)));
        LocalDate finActual = (LocalDate) jTable1.getValueAt(f, 5);
        LocalDate inicio = finActual.isBefore(LocalDate.now()) ? LocalDate.now() : finActual.plusDays(1);
        String plan = String.valueOf(jComboBox3.getSelectedItem());
        Membresias m = new Membresias();
        m.setClienteID(clienteId);
        m.setTipoPlan(plan);
        m.setFechaInicio(inicio);
        m.setFechaFinal(inicio.plusDays(Catalogos.PLANES.get(plan)));
        m.setEstado("ACTIVA");
        Pagos p = new Pagos();
        p.setClienteID(clienteId);
        p.setMonto(monto);
        p.setMetodoPago(String.valueOf(jComboBox5.getSelectedItem()));
        p.setFechaPago(LocalDate.now());
        if (trx.procesarPagoYMembresia(p, m)) {
            Ui.info(this, "Renovada: " + plan + " del " + m.getFechaInicio() + " al " + m.getFechaFinal() + ".");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo renovar la membresía.");
    }

    private void cancelarPlan() {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        if (seleccionado == null) { Ui.error(this, "Seleccione una membresía de la tabla."); return; }
        if (!Ui.confirmar(this, "¿Cancelar este plan?")) return;
        if (trx.cambiarEstadoMembresiaManual(seleccionado, "CANCELADA")) { Ui.info(this, "Plan cancelado."); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo cancelar el plan.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.MEMBRESIAS);
        if (seleccionado == null) { Ui.error(this, "Seleccione una membresía de la tabla."); return; }
        if (!Ui.confirmar(this, "¿Eliminar esta membresía? (los pagos no se borran)")) return;
        if (trx.eliminarMembresia(seleccionado)) { Ui.info(this, "Membresía eliminada."); limpiar(); cargarTabla(); }
        else Ui.error(this, "No se pudo eliminar la membresía.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jComboBox4 = new javax.swing.JComboBox<>();
        jComboBox5 = new javax.swing.JComboBox<>();
        jTextField6 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(26, 32, 44));

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));

        jComboBox2.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox2.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jLabel1.setBackground(new java.awt.Color(247, 250, 252));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(247, 250, 252));
        jLabel1.setText("Cliente:");

        jComboBox3.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox3.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(this::jComboBox3ActionPerformed);

        jLabel2.setBackground(new java.awt.Color(247, 250, 252));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Tipo de plan:");

        jComboBox4.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox4.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox4.addActionListener(this::jComboBox4ActionPerformed);

        jComboBox5.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox5.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox5.addActionListener(this::jComboBox5ActionPerformed);

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextField6.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Fecha final:");

        jLabel4.setBackground(new java.awt.Color(247, 250, 252));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("Fecha de inicio:");

        jLabel5.setBackground(new java.awt.Color(247, 250, 252));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(247, 250, 252));
        jLabel5.setText("Metodo de pago");

        jLabel8.setBackground(new java.awt.Color(247, 250, 252));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(247, 250, 252));
        jLabel8.setText("Estado");

        jLabel9.setBackground(new java.awt.Color(247, 250, 252));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(247, 250, 252));
        jLabel9.setText("Monto $:");

        jButton2.setFocusPainted(false);
        jButton2.setBackground(new java.awt.Color(220, 38, 38));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(247, 250, 252));
        jButton2.setText("Renovar");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(55, 55, 55)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(91, 91, 91)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jLabel8)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("MEMBRESIAS Y RENOVACIONES:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Control de vigencias, renovaciones:");

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(220, 38, 38));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Guardar Membresia");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setBackground(new java.awt.Color(46, 56, 75));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setForeground(new java.awt.Color(247, 250, 252));
        jButton4.setText("Editar");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setFocusPainted(false);
        jButton5.setBackground(new java.awt.Color(46, 56, 75));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setForeground(new java.awt.Color(247, 250, 252));
        jButton5.setText("Limpiar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setFocusPainted(false);
        jButton6.setBackground(new java.awt.Color(90, 24, 30));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(247, 250, 252));
        jButton6.setText("Eliminar");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton7.setFocusPainted(false);
        jButton7.setBackground(new java.awt.Color(90, 24, 30));
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(247, 250, 252));
        jButton7.setText("Cancelar plan");
        jButton7.addActionListener(this::jButton7ActionPerformed);

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
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
                .addGap(3, 3, 3)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        renovar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        editar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        cancelarPlan();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        sugerirFin();
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
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
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
