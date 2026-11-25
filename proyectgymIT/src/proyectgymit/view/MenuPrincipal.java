/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package proyectgymit.view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import proyectgymit.security.Permisos;
import proyectgymit.security.Permisos.Modulo;
import proyectgymit.security.Sesion;
import proyectgymit.util.Ui;

/**
 *
 * @author mario
 */
public class MenuPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MenuPrincipal.class.getName());

    /**
     * Creates new form MenuPrincipal
     */
    private boolean sinSesion = false;

    public MenuPrincipal() {
        initComponents();
        this.setLocationRelativeTo(null);
        if (!Sesion.activa()) {
            // Se ejecutó este formulario directamente (sin login): se va al login.
            sinSesion = true;
            java.awt.EventQueue.invokeLater(() -> new login().setVisible(true));
            return;
        }
        construirBarraSuperior();
        setTitle("GojoGym - " + Sesion.getUsuario().getNombre() + " (" + Sesion.getRol() + ")");
        aplicarPermisos();
        mostrarModulo(Modulo.DASHBOARD);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible && !sinSesion);
    }

    // ------------------------------------------------------------------ barra superior
    // Los componentes de la barra (barraSuperior, lblModulo, lblBd, btnUsuario) están en el diseñador.
    private javax.swing.Timer relojBd;

    private static String titulo(Modulo m) {
        switch (m) {
            case DASHBOARD: return "Inicio";
            case SOCIOS: return "Clientes";
            case ENTRENADORES: return "Entrenadores";
            case MEMBRESIAS: return "Membresías";
            case PAGOS: return "Pagos y caja";
            case ASISTENCIA: return "Asistencias";
            case EJERCICIOS: return "Ejercicios";
            case RUTINAS: return "Rutinas";
            case REPORTES: return "Reportes";
            case USUARIOS: return "Personal";
            default: return "";
        }
    }

    /** Datos del usuario, estado de la base de datos y modo claro/oscuro (los componentes los coloca el diseñador). */
    private void construirBarraSuperior() {
        barraSuperior.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, proyectgymit.util.Paleta.BORDE));
        btnUsuario.setText(Sesion.getUsuario().getNombre() + "  ·  " + Sesion.getRol() + "  ▾");
        relojBd = new javax.swing.Timer(30000, e -> revisarBd());
        relojBd.setInitialDelay(100);
        relojBd.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { relojBd.stop(); }
        });
    }

    private void revisarBd() {
        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try (java.sql.Connection c = proyectgymit.config.conexionbd.getConexion()) { return c.isValid(2); }
                catch (Exception ex) { return false; }
            }
            @Override protected void done() {
                boolean ok;
                try { ok = get(); } catch (Exception ex) { ok = false; }
                lblBd.setText(ok ? "● BD conectada" : "● Sin conexión a la BD");
                lblBd.setForeground(ok ? proyectgymit.util.Paleta.OK : proyectgymit.util.Paleta.MAL);
            }
        }.execute();
    }

    private void menuUsuario() {
        javax.swing.JPopupMenu m = new javax.swing.JPopupMenu();
        if (Permisos.puedeEscribir(Modulo.USUARIOS)) {
            m.add(opcion("Cambiar mi contraseña…", this::cambiarPassword));
            m.addSeparator();
        }
        m.add(opcion("Cerrar sesión", this::cerrarSesion));
        m.show(btnUsuario, 0, btnUsuario.getHeight());
    }

    private javax.swing.JMenuItem opcion(String texto, Runnable r) {
        javax.swing.JMenuItem i = new javax.swing.JMenuItem(texto);
        i.addActionListener(e -> r.run());
        return i;
    }

    /** Solo disponible para quien puede editar usuarios (ADMIN): usa UsuarioC.actualizarUsuario tal como está. */
    private void cambiarPassword() {
        javax.swing.JPasswordField nueva = new javax.swing.JPasswordField(18), repite = new javax.swing.JPasswordField(18);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        p.add(new javax.swing.JLabel("Nueva contraseña (mínimo 8 caracteres):"));
        p.add(nueva);
        p.add(new javax.swing.JLabel("Repita la nueva contraseña:"));
        p.add(repite);
        if (javax.swing.JOptionPane.showConfirmDialog(this, p, "Cambiar contraseña", javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE) != javax.swing.JOptionPane.OK_OPTION) return;
        String a = new String(nueva.getPassword()), b = new String(repite.getPassword());
        if (a.length() < 8) { Ui.error(this, "La contraseña debe tener al menos 8 caracteres."); return; }
        if (!a.equals(b)) { Ui.error(this, "Las contraseñas no coinciden."); return; }
        if (new proyectgymit.controler.UsuarioC().actualizarUsuario(Sesion.getUsuario(), a)) Ui.info(this, "Contraseña actualizada.");
        else Ui.error(this, "No se pudo actualizar la contraseña.");
    }

    private JButton[] botonesMenu() {
        return new JButton[]{jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9, jButton10};
    }

    private void marcarModulo(Modulo m) {
        JButton[] bs = botonesMenu();
        Modulo[] ms = {Modulo.DASHBOARD, Modulo.SOCIOS, Modulo.ENTRENADORES, Modulo.MEMBRESIAS, Modulo.PAGOS,
            Modulo.ASISTENCIA, Modulo.EJERCICIOS, Modulo.RUTINAS, Modulo.REPORTES, Modulo.USUARIOS};
        for (int i = 0; i < bs.length; i++) bs[i].setBackground(ms[i] == m ? proyectgymit.util.Paleta.TARJETA : proyectgymit.util.Paleta.FONDO);   // resalta el módulo abierto
    }

    /** Muestra solo los botones de los módulos que el rol puede ver (matriz de permisos). */
    private void aplicarPermisos() {
        ocultarSiNoPuede(jButton1, Modulo.DASHBOARD);
        ocultarSiNoPuede(jButton2, Modulo.SOCIOS);
        ocultarSiNoPuede(jButton3, Modulo.ENTRENADORES);
        ocultarSiNoPuede(jButton4, Modulo.MEMBRESIAS);
        ocultarSiNoPuede(jButton5, Modulo.PAGOS);
        ocultarSiNoPuede(jButton6, Modulo.ASISTENCIA);
        ocultarSiNoPuede(jButton7, Modulo.EJERCICIOS);
        ocultarSiNoPuede(jButton8, Modulo.RUTINAS);
        ocultarSiNoPuede(jButton9, Modulo.REPORTES);
        ocultarSiNoPuede(jButton10, Modulo.USUARIOS);
    }

    private void ocultarSiNoPuede(JButton boton, Modulo modulo) {
        boton.setVisible(Permisos.puedeVer(modulo));
    }

    private JPanel crearPanel(Modulo modulo) {
        switch (modulo) {
            case DASHBOARD:    return new PanelInicio();
            case SOCIOS:       return new panelClientes();
            case ENTRENADORES: return new panelEntrenadores();
            case MEMBRESIAS:   return new panelMembresias();
            case PAGOS:        return new panelPagos();
            case ASISTENCIA:   return new panelAsistencias();
            case EJERCICIOS:   return new panelEjercicios();
            case RUTINAS:      return new panelRutinas();
            case REPORTES:     return new panelReportes();
            case USUARIOS:     return new panelGestionU();
            default:           return null;
        }
    }

    /** Segunda barrera: aunque se invoque por código, no abre un módulo que el rol no puede ver. */
    private void mostrarModulo(Modulo modulo) {
        if (!Permisos.puedeVer(modulo)) {
            Ui.error(this, "Su rol no tiene acceso a este modulo.");
            return;
        }
        JPanel p = crearPanel(modulo);
        if (p != null) { mostrarPanel(p); marcarModulo(modulo); lblModulo.setText(titulo(modulo)); }
    }

    private void cerrarSesion() {
        Sesion.cerrar();
        new login().setVisible(true);
        dispose();
    }

    private void mostrarPanel(JPanel p) {
        panelModulo.removeAll();
        panelModulo.add(p, BorderLayout.CENTER);
        panelModulo.revalidate();
        panelModulo.repaint();
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
        panelMenu = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        panelContenido = new javax.swing.JPanel();
        barraSuperior = new javax.swing.JPanel();
        lblModulo = new javax.swing.JLabel();
        lblBd = new javax.swing.JLabel();
        btnUsuario = new javax.swing.JButton();
        panelModulo = new javax.swing.JPanel();

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(247, 250, 252));
        jLabel6.setText("MEMBRESIAS Y RENOVACIONES:");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));

        panelMenu.setBackground(new java.awt.Color(18, 22, 29));

        jButton1.setFocusPainted(false);
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.setBackground(new java.awt.Color(18, 22, 29));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(148, 163, 184));
        jButton1.setText("Inicio");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFocusPainted(false);
        jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton2.setBackground(new java.awt.Color(18, 22, 29));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(148, 163, 184));
        jButton2.setText("Clientes");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFocusPainted(false);
        jButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton3.setBackground(new java.awt.Color(18, 22, 29));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(148, 163, 184));
        jButton3.setText("Entrenadores");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setFocusPainted(false);
        jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton4.setBackground(new java.awt.Color(18, 22, 29));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(148, 163, 184));
        jButton4.setText("Membresias");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setFocusPainted(false);
        jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton5.setBackground(new java.awt.Color(18, 22, 29));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(148, 163, 184));
        jButton5.setText("Pagos");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setFocusPainted(false);
        jButton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton6.setBackground(new java.awt.Color(18, 22, 29));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(148, 163, 184));
        jButton6.setText("Asistencias");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton7.setFocusPainted(false);
        jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton7.setBackground(new java.awt.Color(18, 22, 29));
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton7.setForeground(new java.awt.Color(148, 163, 184));
        jButton7.setText("Ejercicios");
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton8.setFocusPainted(false);
        jButton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton8.setBackground(new java.awt.Color(18, 22, 29));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton8.setForeground(new java.awt.Color(148, 163, 184));
        jButton8.setText("Rutinas ");
        jButton8.addActionListener(this::jButton8ActionPerformed);

        jButton9.setFocusPainted(false);
        jButton9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton9.setBackground(new java.awt.Color(18, 22, 29));
        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton9.setForeground(new java.awt.Color(148, 163, 184));
        jButton9.setText("Reportes");
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jButton10.setFocusPainted(false);
        jButton10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton10.setBackground(new java.awt.Color(18, 22, 29));
        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton10.setForeground(new java.awt.Color(148, 163, 184));
        jButton10.setText("Personal");
        jButton10.addActionListener(this::jButton10ActionPerformed);

        jButton11.setFocusPainted(false);
        jButton11.setBackground(new java.awt.Color(90, 24, 30));
        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton11.setForeground(new java.awt.Color(247, 250, 252));
        jButton11.setText("Cerrar Sesion");
        jButton11.addActionListener(this::jButton11ActionPerformed);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(247, 250, 252));
        jLabel7.setText("GOJOGYM");

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenuLayout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(panelMenuLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(81, Short.MAX_VALUE))
        );
        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel7)
                .addGap(44, 44, 44)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addGap(18, 18, 18)
                .addComponent(jButton6)
                .addGap(18, 18, 18)
                .addComponent(jButton7)
                .addGap(18, 18, 18)
                .addComponent(jButton8)
                .addGap(18, 18, 18)
                .addComponent(jButton9)
                .addGap(18, 18, 18)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
                .addComponent(jButton11)
                .addGap(21, 21, 21))
        );

        panelContenido.setBackground(new java.awt.Color(26, 32, 44));
        panelContenido.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        panelContenido.setLayout(new java.awt.BorderLayout());

        barraSuperior.setBackground(new java.awt.Color(26, 32, 44));

        lblModulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblModulo.setForeground(new java.awt.Color(247, 250, 252));
        lblModulo.setText("Inicio");

        lblBd.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblBd.setForeground(new java.awt.Color(148, 163, 184));
        lblBd.setText("● Conectando…");
        lblBd.setToolTipText("Estado de la conexión con la base de datos");


        btnUsuario.setBackground(new java.awt.Color(46, 56, 75));
        btnUsuario.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnUsuario.setForeground(new java.awt.Color(247, 250, 252));
        btnUsuario.setText("Usuario  ▾");
        btnUsuario.addActionListener(this::btnUsuarioActionPerformed);

        javax.swing.GroupLayout barraSuperiorLayout = new javax.swing.GroupLayout(barraSuperior);
        barraSuperior.setLayout(barraSuperiorLayout);
        barraSuperiorLayout.setHorizontalGroup(
            barraSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraSuperiorLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblModulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblBd)
                .addGap(12, 12, 12)
                .addComponent(btnUsuario)
                .addGap(16, 16, 16))
        );
        barraSuperiorLayout.setVerticalGroup(
            barraSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraSuperiorLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(barraSuperiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblModulo)
                    .addComponent(lblBd)
                    .addComponent(btnUsuario))
                .addGap(9, 9, 9))
        );

        panelContenido.add(barraSuperior, java.awt.BorderLayout.NORTH);

        panelModulo.setBackground(new java.awt.Color(26, 32, 44));
        panelModulo.setLayout(new java.awt.BorderLayout());
        panelContenido.add(panelModulo, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btnUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuarioActionPerformed
        menuUsuario();
    }//GEN-LAST:event_btnUsuarioActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        cerrarSesion();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        mostrarModulo(Modulo.USUARIOS);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        mostrarModulo(Modulo.REPORTES);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        mostrarModulo(Modulo.RUTINAS);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        mostrarModulo(Modulo.EJERCICIOS);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        mostrarModulo(Modulo.ASISTENCIA);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        mostrarModulo(Modulo.ENTRENADORES);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        mostrarModulo(Modulo.DASHBOARD);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        mostrarModulo(Modulo.PAGOS);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        mostrarModulo(Modulo.SOCIOS);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        mostrarModulo(Modulo.MEMBRESIAS);
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel barraSuperior;
    private javax.swing.JButton btnUsuario;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblBd;
    private javax.swing.JLabel lblModulo;
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelModulo;
    // End of variables declaration//GEN-END:variables
}
