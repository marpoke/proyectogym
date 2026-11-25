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
import proyectgymit.model.Pagos;
import java.util.ArrayList;

/**
 *
 * @author mario
 */
public class panelPagos extends javax.swing.JPanel {

    /**
     * Creates new form panelPagos
     */
    private final Transacciones trx = new Transacciones();
    private final ClienteC clienteC = new ClienteC();
    private List<Item> clientes;          // combo del formulario
    private List<Item> clientesFiltro;    // combo del filtro (con "Todos")
    private DefaultTableModel modelo;
    private Integer seleccionado = null;  // id del pago

    public panelPagos() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        proyectgymit.util.Validacion.numero(jTextField6, true);
        proyectgymit.util.Tabla.agregarAccion(jTable1, "Imprimir recibo…", this::imprimirRecibo);
        modelo = Ui.modelo("ID", "ID Cliente", "Cliente", "Monto $", "Método", "Fecha");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientes = Ui.llenarCombo(jComboBox3, clienteC.listarClientesCombo());
        clientesFiltro = new ArrayList<>();
        clientesFiltro.add(new Item(0, "Todos"));
        clientesFiltro.addAll(clientes);
        Ui.llenarCombo(jComboBox2, clientesFiltro);
        jComboBox4.setModel(new DefaultComboBoxModel<>(Catalogos.METODOS_PAGO));
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        aplicarPermisos();
        limpiar();
        cargarTabla(null, null, null);
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.PAGOS);
        Ui.habilitar(escribe, jButton7, jButton8, jButton9, jComboBox3, jComboBox4, jTextField6, jDateChooser3);
    }

    private void cargarTabla(Integer clienteId, LocalDate desde, LocalDate hasta) {
        modelo.setRowCount(0);
        for (Object[] fila : trx.listarPagosTabla(clienteId, desde, hasta)) modelo.addRow(fila);
    }

    private void filtrar() {
        int id = Ui.idSeleccionado(jComboBox2, clientesFiltro);
        LocalDate d = Ui.aLocalDate(jDateChooser1.getDate());
        LocalDate h = Ui.aLocalDate(jDateChooser2.getDate());
        if (d != null && h != null && h.isBefore(d)) { Ui.error(this, "«Hasta» no puede ser anterior a «Desde»."); return; }
        cargarTabla(id > 0 ? id : null, d, h);
    }

    private void todos() {
        jComboBox2.setSelectedIndex(0);
        jDateChooser1.setDate(null);
        jDateChooser2.setDate(null);
        cargarTabla(null, null, null);
    }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        Ui.seleccionarPorId(jComboBox3, clientes, Integer.parseInt(String.valueOf(jTable1.getValueAt(f, 1))));
        jTextField6.setText(String.valueOf(jTable1.getValueAt(f, 3)));
        jComboBox4.setSelectedItem(String.valueOf(jTable1.getValueAt(f, 4)));
        jDateChooser3.setDate(Ui.aDate((LocalDate) jTable1.getValueAt(f, 5)));
    }

    private void limpiar() {
        jTable1.clearSelection();
        seleccionado = null;
        jTextField6.setText("");
        jDateChooser3.setDate(new Date());
    }

    /** Recibo imprimible de la fila seleccionada (opción del menú contextual de la tabla). */
    private void imprimirRecibo() {
        int f = jTable1.getSelectedRow();
        if (f < 0) { Ui.info(this, "Seleccione un pago de la tabla."); return; }
        new DialogRecibo(javax.swing.SwingUtilities.getWindowAncestor(this), Integer.parseInt(String.valueOf(jTable1.getValueAt(f, 0))),
                String.valueOf(jTable1.getValueAt(f, 2)), Double.parseDouble(String.valueOf(jTable1.getValueAt(f, 3))),
                String.valueOf(jTable1.getValueAt(f, 4)), (LocalDate) jTable1.getValueAt(f, 5)).setVisible(true);
    }

    private Pagos leerFormulario() {
        int clienteId = Ui.idSeleccionado(jComboBox3, clientes);
        if (clienteId < 0) { Ui.error(this, "Seleccione un cliente."); return null; }
        double monto;
        try {
            monto = Double.parseDouble(jTextField6.getText().trim());
            if (monto <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { Ui.error(this, "El monto debe ser un número mayor a cero."); return null; }
        LocalDate fecha = Ui.aLocalDate(jDateChooser3.getDate());
        if (fecha == null) { Ui.error(this, "Seleccione la fecha del pago."); return null; }
        Pagos p = new Pagos();
        p.setClienteID(clienteId);
        p.setMonto(monto);
        p.setMetodoPago(String.valueOf(jComboBox4.getSelectedItem()));
        p.setFechaPago(fecha);
        return p;
    }

    private void registrar() {
        Permisos.exigirEscritura(Modulo.PAGOS);
        Pagos p = leerFormulario();
        if (p == null) return;
        if (trx.registrarPago(p)) { Ui.info(this, "Pago registrado."); limpiar(); filtrar(); }
        else Ui.error(this, "No se pudo registrar el pago.");
    }

    private void editar() {
        Permisos.exigirEscritura(Modulo.PAGOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un pago de la tabla."); return; }
        Pagos p = leerFormulario();
        if (p == null) return;
        p.setId(seleccionado);
        if (trx.actualizarPago(p)) { Ui.info(this, "Pago actualizado."); limpiar(); filtrar(); }
        else Ui.error(this, "No se pudo actualizar el pago.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.PAGOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un pago de la tabla."); return; }
        if (!Ui.confirmar(this, "¿Eliminar este pago? Se descontará de la caja.")) return;
        if (trx.eliminarPago(seleccionado)) { Ui.info(this, "Pago eliminado."); limpiar(); filtrar(); }
        else Ui.error(this, "No se pudo eliminar el pago.");
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
        jLabel2 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jTextField6 = new javax.swing.JTextField();
        jComboBox4 = new javax.swing.JComboBox<>();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
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

        jLabel2.setBackground(new java.awt.Color(247, 250, 252));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Desde:");

        jLabel3.setBackground(new java.awt.Color(247, 250, 252));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Hasta:");

        jButton5.setFocusPainted(false);
        jButton5.setBackground(new java.awt.Color(46, 56, 75));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setForeground(new java.awt.Color(247, 250, 252));
        jButton5.setText("Filtrar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setFocusPainted(false);
        jButton6.setBackground(new java.awt.Color(46, 56, 75));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(247, 250, 252));
        jButton6.setText("Todos");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton5)
                        .addComponent(jButton6))
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("PAGOS Y CAJA:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Caja registradora:");

        jPanel2.setBackground(new java.awt.Color(38, 46, 61));

        jLabel4.setBackground(new java.awt.Color(247, 250, 252));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("Cliente:");

        jComboBox3.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox3.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(this::jComboBox3ActionPerformed);

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextField6.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jComboBox4.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox4.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox4.addActionListener(this::jComboBox4ActionPerformed);

        jLabel5.setBackground(new java.awt.Color(247, 250, 252));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(247, 250, 252));
        jLabel5.setText("Monto $:");

        jLabel8.setBackground(new java.awt.Color(247, 250, 252));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(247, 250, 252));
        jLabel8.setText("Metodo de pago:");

        jLabel9.setBackground(new java.awt.Color(247, 250, 252));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(247, 250, 252));
        jLabel9.setText("Fecha:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(84, 84, 84)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(76, 76, 76)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(93, 93, 93)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jButton7.setFocusPainted(false);
        jButton7.setBackground(new java.awt.Color(90, 24, 30));
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(247, 250, 252));
        jButton7.setText("Eliminar");
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton8.setFocusPainted(false);
        jButton8.setBackground(new java.awt.Color(220, 38, 38));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton8.setForeground(new java.awt.Color(247, 250, 252));
        jButton8.setText("Registra pago");
        jButton8.addActionListener(this::jButton8ActionPerformed);

        jButton9.setFocusPainted(false);
        jButton9.setBackground(new java.awt.Color(46, 56, 75));
        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton9.setForeground(new java.awt.Color(247, 250, 252));
        jButton9.setText("Editar");
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jButton10.setFocusPainted(false);
        jButton10.setBackground(new java.awt.Color(46, 56, 75));
        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton10.setForeground(new java.awt.Color(247, 250, 252));
        jButton10.setText("Limpiar");
        jButton10.addActionListener(this::jButton10ActionPerformed);

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
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8)
                    .addComponent(jButton9)
                    .addComponent(jButton7)
                    .addComponent(jButton10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        filtrar();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        todos();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        registrar();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        editar();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton10ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
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
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
