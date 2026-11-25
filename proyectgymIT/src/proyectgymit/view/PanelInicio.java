/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package proyectgymit.view;

import proyectgymit.controler.AsistenciasC;
import proyectgymit.controler.Transacciones;

/**
 *
 * @author mario
 */
public class PanelInicio extends javax.swing.JPanel {

    /**
     * Creates new form PanelInicio
     */
    private final Transacciones transacciones = new Transacciones();
    private final AsistenciasC asistencias = new AsistenciasC();

    private final javax.swing.table.DefaultTableModel modeloVencer = proyectgymit.util.Ui.modelo("Cliente", "Plan", "Vence", "Días");
    private final javax.swing.table.DefaultTableModel modeloAsist = proyectgymit.util.Ui.modelo("Cliente", "Fecha y hora");

    public PanelInicio() {
        initComponents();
        configurarTablas();
        ocultarSegunPermisos();
        cargarEstadisticas();
    }

    /** Las tablas pequeñas del dashboard no llevan filtro ni exportación. */
    private void configurarTablas() {
        tablaVencer.setModel(modeloVencer);
        tablaAsistencias.setModel(modeloAsist);
        for (javax.swing.JTable t : new javax.swing.JTable[]{tablaVencer, tablaAsistencias}) {
            t.putClientProperty("tabla.simple", Boolean.TRUE);
            t.setRowSelectionAllowed(false);
        }
    }

    /** Solo se muestra lo que el rol puede ver según la matriz de permisos. */
    private void ocultarSegunPermisos() {
        boolean asist = proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.ASISTENCIA);
        pnlGrafAsist.setVisible(asist);
        pnlUltimas.setVisible(asist);
        pnlGrafIngresos.setVisible(proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.PAGOS));
        pnlVencer.setVisible(proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.MEMBRESIAS));
    }

    /** CORREGIDO: nunca se llamaba, usaba Transacciones.contarAsistenciasHoy (no existía) y lblActivos jamás se llenaba. */
    public void cargarEstadisticas() {
        transacciones.actualizarMembresiasVencidasAutomatico();
        lblAsistencias.setText(String.valueOf(asistencias.contarAsistenciasHoy()));
        lblActivos.setText(String.valueOf(transacciones.contarMembresiasActivas()));
        lblVencer.setText(String.valueOf(transacciones.contarMembresiasPorVencer()));
        cargarGraficas();
    }

    /** Las gráficas y listas reutilizan las consultas que ya existen en los controladores. */
    private void cargarGraficas() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.util.Locale es = new java.util.Locale("es", "MX");
        if (proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.ASISTENCIA)) {
            java.util.List<Object[]> filas = asistencias.listarAsistenciasTabla();   // id, id cliente, nombre, fecha/hora
            String[] et = new String[7]; double[] val = new double[7];
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate d = hoy.minusDays(6 - i);
                et[i] = d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, es) + " " + d.getDayOfMonth();
                for (Object[] f : filas) if (((java.time.LocalDateTime) f[3]).toLocalDate().equals(d)) val[i]++;
            }
            graficaAsistencias.setDatos(et, val);
            modeloAsist.setRowCount(0);
            for (int i = 0; i < Math.min(8, filas.size()); i++)
                modeloAsist.addRow(new Object[]{filas.get(i)[2], ((java.time.LocalDateTime) filas.get(i)[3]).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))});
        }
        if (proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.PAGOS)) {
            java.util.List<Object[]> pagos = transacciones.listarPagosTabla(null, null, null);   // ..., monto, método, fecha
            String[] et = new String[6]; double[] val = new double[6];
            for (int i = 0; i < 6; i++) {
                java.time.LocalDate m = hoy.minusMonths(5 - i).withDayOfMonth(1);
                et[i] = m.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, es);
                for (Object[] f : pagos) {
                    java.time.LocalDate d = (java.time.LocalDate) f[5];
                    if (d != null && d.getYear() == m.getYear() && d.getMonth() == m.getMonth()) val[i] += (Double) f[3];
                }
            }
            graficaIngresos.setDatos(et, val);
        }
        if (proyectgymit.security.Permisos.puedeVer(proyectgymit.security.Permisos.Modulo.MEMBRESIAS)) {
            modeloVencer.setRowCount(0);
            java.util.List<Object[]> mem = transacciones.listarMembresiasTabla();   // id, id cliente, cliente, plan, inicio, fin, estado
            mem.sort(java.util.Comparator.comparing(f -> (java.time.LocalDate) f[5]));
            for (Object[] f : mem) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, (java.time.LocalDate) f[5]);
                if ("ACTIVA".equals(f[6]) && dias >= 0 && dias <= 7) modeloVencer.addRow(new Object[]{f[2], f[3], f[5], dias == 0 ? "hoy" : dias + " d"});
            }
        }
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
        lblAsistencias = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblActivos = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblVencer = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        pnlGrafAsist = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        graficaAsistencias = new proyectgymit.util.Graficos.Barras();
        pnlGrafIngresos = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        graficaIngresos = new proyectgymit.util.Graficos.Barras();
        pnlVencer = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaVencer = new javax.swing.JTable();
        pnlUltimas = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaAsistencias = new javax.swing.JTable();

        setBackground(new java.awt.Color(26, 32, 44));

        jPanel1.setBackground(new java.awt.Color(38, 46, 61));
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(160, 174, 192));
        jLabel1.setText("ASISTENCIAS DE CLIENTES");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(160, 174, 192));
        jLabel2.setText("DIA DE HOY ");

        lblAsistencias.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        lblAsistencias.setForeground(new java.awt.Color(247, 250, 252));
        lblAsistencias.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAsistencias)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAsistencias, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(38, 46, 61));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(160, 174, 192));
        jLabel3.setText("CLIENTES ACTIVOS HOY");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(160, 174, 192));
        jLabel4.setText("MEMBRESIAS VIGENTES AL DIA");

        lblActivos.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        lblActivos.setForeground(new java.awt.Color(247, 250, 252));
        lblActivos.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblActivos)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblActivos, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(38, 46, 61));
        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(160, 174, 192));
        jLabel5.setText("MEMBRESIAS POR VENCER");

        lblVencer.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        lblVencer.setForeground(new java.awt.Color(247, 250, 252));
        lblVencer.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVencer)
                    .addComponent(jLabel5))
                .addContainerGap(113, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblVencer, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addGap(28, 28, 28))
        );


        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jLabel6.setForeground(new java.awt.Color(148, 163, 184));
        jLabel6.setText("PANEL OPERATIVO     GOJOGYM");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(247, 250, 252));
        jLabel8.setText("PANEL INFORMATIVO DIARIO");


        pnlGrafAsist.setBackground(new java.awt.Color(38, 46, 61));
        pnlGrafAsist.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(63, 74, 96)));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(148, 163, 184));
        jLabel9.setText("ASISTENCIAS - ÚLTIMOS 7 DÍAS");

        graficaAsistencias.setDinero(false);

        javax.swing.GroupLayout pnlGrafAsistLayout = new javax.swing.GroupLayout(pnlGrafAsist);
        pnlGrafAsist.setLayout(pnlGrafAsistLayout);
        pnlGrafAsistLayout.setHorizontalGroup(
            pnlGrafAsistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGrafAsistLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlGrafAsistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(graficaAsistencias, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        pnlGrafAsistLayout.setVerticalGroup(
            pnlGrafAsistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGrafAsistLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel9)
                .addGap(8, 8, 8)
                .addComponent(graficaAsistencias, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );


        pnlGrafIngresos.setBackground(new java.awt.Color(38, 46, 61));
        pnlGrafIngresos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(63, 74, 96)));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(148, 163, 184));
        jLabel10.setText("INGRESOS - ÚLTIMOS 6 MESES");

        graficaIngresos.setDinero(true);

        javax.swing.GroupLayout pnlGrafIngresosLayout = new javax.swing.GroupLayout(pnlGrafIngresos);
        pnlGrafIngresos.setLayout(pnlGrafIngresosLayout);
        pnlGrafIngresosLayout.setHorizontalGroup(
            pnlGrafIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGrafIngresosLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlGrafIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(graficaIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        pnlGrafIngresosLayout.setVerticalGroup(
            pnlGrafIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGrafIngresosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel10)
                .addGap(8, 8, 8)
                .addComponent(graficaIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );


        pnlVencer.setBackground(new java.awt.Color(38, 46, 61));
        pnlVencer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(63, 74, 96)));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(148, 163, 184));
        jLabel11.setText("MEMBRESÍAS POR VENCER (7 DÍAS)");

        tablaVencer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaVencer.getTableHeader().setBackground(new java.awt.Color(18, 22, 29));
        tablaVencer.getTableHeader().setForeground(new java.awt.Color(148, 163, 184));
        tablaVencer.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 12));
        jScrollPane1.setViewportView(tablaVencer);

        javax.swing.GroupLayout pnlVencerLayout = new javax.swing.GroupLayout(pnlVencer);
        pnlVencer.setLayout(pnlVencerLayout);
        pnlVencerLayout.setHorizontalGroup(
            pnlVencerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVencerLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlVencerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        pnlVencerLayout.setVerticalGroup(
            pnlVencerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVencerLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel11)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );


        pnlUltimas.setBackground(new java.awt.Color(38, 46, 61));
        pnlUltimas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(63, 74, 96)));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(148, 163, 184));
        jLabel12.setText("ÚLTIMAS ASISTENCIAS");

        tablaAsistencias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaAsistencias.getTableHeader().setBackground(new java.awt.Color(18, 22, 29));
        tablaAsistencias.getTableHeader().setForeground(new java.awt.Color(148, 163, 184));
        tablaAsistencias.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 12));
        jScrollPane2.setViewportView(tablaAsistencias);

        javax.swing.GroupLayout pnlUltimasLayout = new javax.swing.GroupLayout(pnlUltimas);
        pnlUltimas.setLayout(pnlUltimasLayout);
        pnlUltimasLayout.setHorizontalGroup(
            pnlUltimasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimasLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlUltimasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        pnlUltimasLayout.setVerticalGroup(
            pnlUltimasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimasLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel12)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlGrafAsist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlGrafIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlVencer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlUltimas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel6)
                .addGap(4, 4, 4)
                .addComponent(jLabel8)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlGrafAsist, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(pnlGrafIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlVencer, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(pnlUltimas, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private proyectgymit.util.Graficos.Barras graficaAsistencias;
    private proyectgymit.util.Graficos.Barras graficaIngresos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblActivos;
    private javax.swing.JLabel lblAsistencias;
    private javax.swing.JLabel lblVencer;
    private javax.swing.JPanel pnlGrafAsist;
    private javax.swing.JPanel pnlGrafIngresos;
    private javax.swing.JPanel pnlUltimas;
    private javax.swing.JPanel pnlVencer;
    private javax.swing.JTable tablaAsistencias;
    private javax.swing.JTable tablaVencer;
    // End of variables declaration//GEN-END:variables
}
