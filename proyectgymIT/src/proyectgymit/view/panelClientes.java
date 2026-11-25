/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import java.time.LocalDate;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import proyectgymit.controler.ClienteC;
import proyectgymit.controler.UsuarioC;
import proyectgymit.model.Clientes;
import proyectgymit.model.Membresias;
import proyectgymit.model.Pagos;
import proyectgymit.model.Usuario;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.util.Catalogos;
import proyectgymit.util.Ui;

/**
 *
 * @author mario
 */
public class panelClientes extends javax.swing.JPanel {

    /**
     * Creates new form panelClientes
     */
    private final ClienteC clienteC = new ClienteC();
    private final UsuarioC usuarioC = new UsuarioC();
    private DefaultTableModel modelo;
    private Integer seleccionado = null;   // id de la tabla clientes

    public panelClientes() {
        initComponents();
        proyectgymit.util.Tabla.instalarTodas(this);
        proyectgymit.util.Atajos.formulario(this);
        proyectgymit.util.Validacion.obligatorio(jTextField1);
        proyectgymit.util.Validacion.correo(jTextField2, true);
        proyectgymit.util.Validacion.numero(jTextField6, false);
        proyectgymit.util.Validacion.entero(jTextField7, false);
        modelo = Ui.modelo("ID", "Nombre", "Correo", "Teléfono", "Contacto de emergencia", "Membresía");
        jTable1.setModel(modelo);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jComboBox1.setModel(new DefaultComboBoxModel<>(Catalogos.METODOS_PAGO));
        jComboBox2.setModel(new DefaultComboBoxModel<>(Catalogos.PLANES.keySet().toArray(new String[0])));
        sugerirDias();
        jTextField3.setToolTipText("En un cliente existente, déjela vacía para no cambiarla (mínimo 8 caracteres al crearla).");
        jTable1.setToolTipText("Doble clic (solo ADMIN): abrir el portal del socio");
        Ui.alSeleccionar(jTable1, this::cargarSeleccion);
        Ui.alCambiar(jTextField8, this::cargarTabla);
        Ui.alDobleClic(jTable1, this::abrirPortalSocio);
        aplicarPermisos();
        cargarTabla();
    }

    private void aplicarPermisos() {
        boolean escribe = Permisos.puedeEscribir(Modulo.SOCIOS);
        Ui.habilitar(escribe, jButton1, jButton2, jButton3, jTextField1, jTextField2, jTextField3, jTextField4, jTextField5);
        // Plan y pago inicial solo si el rol también escribe en Membresías y Pagos (el ENTRENADOR no).
        boolean cobra = escribe && Permisos.puedeEscribir(Modulo.MEMBRESIAS) && Permisos.puedeEscribir(Modulo.PAGOS);
        Ui.habilitar(cobra, jComboBox1, jComboBox2, jTextField6, jTextField7);
    }

    private void sugerirDias() {
        Integer dias = Catalogos.PLANES.get(String.valueOf(jComboBox2.getSelectedItem()));
        jTextField7.setText(dias == null ? "" : String.valueOf(dias));
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : clienteC.listarClientesTabla(jTextField8.getText())) modelo.addRow(fila);
    }

    private String txt(Object o) { return o == null ? "" : o.toString(); }

    private void cargarSeleccion() {
        seleccionado = Ui.idDeFila(jTable1, 0);
        if (seleccionado == null) return;
        int f = jTable1.getSelectedRow();
        jTextField1.setText(txt(jTable1.getValueAt(f, 1)));
        jTextField2.setText(txt(jTable1.getValueAt(f, 2)));
        jTextField3.setText("");
        jTextField4.setText(txt(jTable1.getValueAt(f, 3)));
        jTextField5.setText(txt(jTable1.getValueAt(f, 4)));
    }

    private boolean existeCorreoDeOtro(String correo) {
        for (Clientes c : clienteC.listarClientes()) {
            if (c.getId() == seleccionado) return usuarioC.existeCorreo(correo, c.getUsuarioID());
        }
        return usuarioC.existeCorreo(correo, 0);
    }

    private void limpiar() {
        jTextField1.setText(""); jTextField2.setText(""); jTextField3.setText("");
        jTextField4.setText(""); jTextField5.setText(""); jTextField6.setText("");
        sugerirDias();
        jTable1.clearSelection();
        seleccionado = null;
    }

    /** Modo admin: abre el portal del socio seleccionado (matriz: Portal Socio = Modo Admin). */
    private void abrirPortalSocio() {
        if (!Permisos.puedeEscribir(Modulo.PORTAL_SOCIO) || seleccionado == null) return;
        new MenuCliente(seleccionado).setVisible(true);
    }

    private void guardar() {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        String nombre = jTextField1.getText().trim();
        String correo = jTextField2.getText().trim();
        String pass = new String(jTextField3.getPassword());
        String tel = jTextField4.getText().trim();
        String emerg = jTextField5.getText().trim();
        if (Ui.vacio(nombre) || Ui.vacio(tel)) { Ui.error(this, "Nombre y teléfono son obligatorios."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (pass.length() < 8) { Ui.error(this, "La contraseña debe tener al menos 8 caracteres."); return; }
        if (usuarioC.existeCorreo(correo, 0)) { Ui.error(this, "Ya existe un usuario con ese correo."); return; }
        Membresias m = null;
        Pagos p = null;
        if (jTextField6.isEnabled() && !Ui.vacio(jTextField6.getText())) {
            double monto;
            int dias;
            try {
                monto = Double.parseDouble(jTextField6.getText().trim());
                dias = Integer.parseInt(jTextField7.getText().trim());
            } catch (NumberFormatException ex) { Ui.error(this, "Monto y días deben ser números."); return; }
            if (monto <= 0 || dias <= 0) { Ui.error(this, "Monto y días deben ser mayores a cero."); return; }
            LocalDate hoy = LocalDate.now();
            m = new Membresias();
            m.setTipoPlan(String.valueOf(jComboBox2.getSelectedItem()));
            m.setFechaInicio(hoy);
            m.setFechaFinal(hoy.plusDays(dias));
            m.setEstado("ACTIVA");
            p = new Pagos();
            p.setMonto(monto);
            p.setMetodoPago(String.valueOf(jComboBox1.getSelectedItem()));
        }
        Usuario u = new Usuario();
        u.setNombre(nombre); u.setCorreo(correo); u.setPassword(pass);
        Clientes c = new Clientes();
        c.setTelefono(tel); c.setContactoEmergencia(emerg);
        if (clienteC.registrarClienteCompleto(u, c, m, p)) {
            Ui.info(this, "Cliente registrado" + (m != null ? " con su membresía y pago inicial." : "."));
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo registrar el cliente.");
    }

    private void actualizar() {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un cliente de la tabla."); return; }
        String nombre = jTextField1.getText().trim();
        String correo = jTextField2.getText().trim();
        String pass = new String(jTextField3.getPassword());
        if (Ui.vacio(nombre)) { Ui.error(this, "El nombre es obligatorio."); return; }
        if (!Ui.correoValido(correo)) { Ui.error(this, "Escriba un correo válido."); return; }
        if (!pass.isEmpty() && pass.length() < 8) { Ui.error(this, "La nueva contraseña debe tener al menos 8 caracteres."); return; }
        if (existeCorreoDeOtro(correo)) { Ui.error(this, "Ya existe otro usuario con ese correo."); return; }
        if (clienteC.actualizarCliente(seleccionado, nombre, correo, jTextField4.getText().trim(), jTextField5.getText().trim(), pass)) {
            Ui.info(this, "Cliente actualizado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo actualizar el cliente.");
    }

    private void eliminar() {
        Permisos.exigirEscritura(Modulo.SOCIOS);
        if (seleccionado == null) { Ui.error(this, "Seleccione un cliente de la tabla."); return; }
        if (!Ui.confirmar(this, "Se eliminará el cliente y TODO su historial (membresías, pagos, asistencias y rutinas).\n¿Desea continuar?")) return;
        if (clienteC.eliminarCliente(seleccionado)) {
            Ui.info(this, "Cliente eliminado.");
            limpiar(); cargarTabla();
        } else Ui.error(this, "No se pudo eliminar el cliente.");
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JPasswordField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();

        setBackground(new java.awt.Color(26, 32, 44));
        setForeground(new java.awt.Color(26, 32, 44));

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setBackground(new java.awt.Color(247, 250, 252));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(247, 250, 252));
        jLabel1.setText("Nombre completo:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(247, 250, 252));
        jLabel2.setText("Correo Electronico:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(247, 250, 252));
        jLabel3.setText("Contraseña:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(247, 250, 252));
        jLabel4.setText("Telefono:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(247, 250, 252));
        jLabel5.setText("Telefono de emergencia:");

        jTextField1.setForeground(new java.awt.Color(247, 250, 252));
        jTextField1.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField1.setBackground(new java.awt.Color(20, 25, 35));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jTextField2.setForeground(new java.awt.Color(247, 250, 252));
        jTextField2.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField2.setBackground(new java.awt.Color(20, 25, 35));
        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jTextField3.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField3.setBackground(new java.awt.Color(20, 25, 35));
        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField3.setForeground(new java.awt.Color(247, 250, 252));
        jTextField3.addActionListener(this::jTextField3ActionPerformed);

        jTextField4.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField4.setBackground(new java.awt.Color(20, 25, 35));
        jTextField4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField4.setForeground(new java.awt.Color(247, 250, 252));
        jTextField4.addActionListener(this::jTextField4ActionPerformed);

        jTextField5.setForeground(new java.awt.Color(247, 250, 252));
        jTextField5.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField5.setBackground(new java.awt.Color(20, 25, 35));
        jTextField5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextField5.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextField5.addActionListener(this::jTextField5ActionPerformed);

        jComboBox1.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox1.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(247, 250, 252));
        jLabel8.setText("Metodo de pago:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(247, 250, 252));
        jLabel9.setText("Dias:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(247, 250, 252));
        jLabel10.setText("Plan inicial");

        jTextField6.setForeground(new java.awt.Color(247, 250, 252));
        jTextField6.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField6.setBackground(new java.awt.Color(20, 25, 35));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextField6.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextField6.addActionListener(this::jTextField6ActionPerformed);

        jComboBox2.setForeground(new java.awt.Color(247, 250, 252));
        jComboBox2.setBackground(new java.awt.Color(20, 25, 35));
        jComboBox2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(247, 250, 252));
        jLabel11.setText("Monto $:");

        jTextField7.setForeground(new java.awt.Color(247, 250, 252));
        jTextField7.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField7.setBackground(new java.awt.Color(20, 25, 35));
        jTextField7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextField7.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextField7.addActionListener(this::jTextField7ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                                .addComponent(jTextField2)))
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel3))
                .addGap(143, 143, 143)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jLabel11))
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)))
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(124, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(97, 97, 97)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel2))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("GESTION DE CLIENTES:");

        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("Registro de clientes, membresias y pagos.");

        jTable1.setFillsViewportHeight(true);
        jTable1.setShowGrid(false);
        jTable1.setRowHeight(32);
        jTable1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jTable1.setSelectionBackground(new java.awt.Color(127, 29, 29));
        jTable1.setBackground(new java.awt.Color(26, 32, 44));
        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(247, 250, 252));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jTable1.getTableHeader().setBackground(new java.awt.Color(18, 22, 29));
        jTable1.getTableHeader().setForeground(new java.awt.Color(148, 163, 184));
        jTable1.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 12));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setFocusPainted(false);
        jButton1.setBackground(new java.awt.Color(220, 38, 38));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(247, 250, 252));
        jButton1.setText("Guardar nuevo cliente");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFocusPainted(false);
        jButton2.setBackground(new java.awt.Color(46, 56, 75));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(247, 250, 252));
        jButton2.setText("Actualizar cliente");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFocusPainted(false);
        jButton3.setBackground(new java.awt.Color(90, 24, 30));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(247, 250, 252));
        jButton3.setText("Eliminar cliente");
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

        jTextField8.setForeground(new java.awt.Color(247, 250, 252));
        jTextField8.setCaretColor(new java.awt.Color(247, 250, 252));
        jTextField8.setBackground(new java.awt.Color(20, 25, 35));
        jTextField8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField8.addActionListener(this::jTextField8ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGap(20, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        limpiar();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        sugerirDias();
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        actualizar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        eliminar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JTextField jTextField2;
    private javax.swing.JPasswordField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    // End of variables declaration//GEN-END:variables
}
