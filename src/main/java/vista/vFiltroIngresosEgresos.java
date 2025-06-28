package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import modelo.UsuariosDAO;

public class vFiltroIngresosEgresos extends javax.swing.JDialog {

    private boolean aceptado = false;

    public vFiltroIngresosEgresos(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarVentana();
        cargarDatos();
        establecerValoresPorDefecto();
        configurarEventos();
    }

    private void configurarVentana() {
        pack();
        setLocationRelativeTo(getParent());
    }

    private void configurarEventos() {
        btnFiltrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validarDatos()) {
                    aceptado = true;
                    dispose();
                }
            }
        });

        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aceptado = false;
                dispose();
            }
        });
    }

    private void cargarDatos() {
        // Limpiar combos
        comboUsuario.removeAllItems();
        comboTipoMovimiento.removeAllItems();

        try {
            // Cargar usuarios
            comboUsuario.addItem(new ItemCombo(0, "TODOS LOS USUARIOS"));

            UsuariosDAO usuarioDao = new UsuariosDAO();
            List<Object[]> usuarios = usuarioDao.listarUsuarios();
            for (Object[] usuario : usuarios) {
                // Usar el nombre completo de la persona (índice 5) combinado con nombre de usuario (índice 1)
                String nombreCompleto = (String) usuario[5];
                String nombreUsuario = (String) usuario[1];

                // Combinar nombre completo y nombre de usuario para mejor identificación
                String textoMostrar = nombreCompleto != null
                        ? nombreCompleto + " (" + nombreUsuario + ")" : nombreUsuario;

                comboUsuario.addItem(new ItemCombo((int) usuario[0], textoMostrar));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Asegurar que al menos hay una opción si hay error
            if (comboUsuario.getItemCount() == 0) {
                comboUsuario.addItem(new ItemCombo(0, "TODOS LOS USUARIOS"));
            }
        }

        // Cargar tipos de movimiento
        comboTipoMovimiento.addItem("TODOS LOS MOVIMIENTOS");
        comboTipoMovimiento.addItem("SOLO INGRESOS");
        comboTipoMovimiento.addItem("SOLO EGRESOS");
    }

    private void establecerValoresPorDefecto() {
        // Establecer fechas por defecto (último mes)
        Date fechaActual = new Date();
        Date fechaInicio = new Date(fechaActual.getTime() - (30L * 24 * 60 * 60 * 1000)); // 30 días atrás

        jDateChooser1.setDate(fechaInicio);
        jDateChooser2.setDate(fechaActual);

        // Seleccionar valores por defecto
        comboUsuario.setSelectedIndex(0);
        comboTipoMovimiento.setSelectedIndex(0);
        chkIncluirAnulados.setSelected(false);
    }

    private void limpiarCampos() {
        jDateChooser1.setDate(null);
        jDateChooser2.setDate(null);
        comboUsuario.setSelectedIndex(0);
        comboTipoMovimiento.setSelectedIndex(0);
        chkIncluirAnulados.setSelected(false);
    }

    private boolean validarDatos() {
        Date fechaDesde = jDateChooser1.getDate();
        Date fechaHasta = jDateChooser2.getDate();

        if (fechaDesde != null && fechaHasta != null) {
            if (fechaDesde.after(fechaHasta)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha desde no puede ser mayor que la fecha hasta",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    // Métodos getter para obtener los valores del filtro
    public Date getFechaDesde() {
        return jDateChooser1.getDate();
    }

    public Date getFechaHasta() {
        return jDateChooser2.getDate();
    }

    public int getUsuarioId() {
        ItemCombo item = (ItemCombo) comboUsuario.getSelectedItem();
        return item != null ? item.getValor() : 0;
    }

    public String getUsuarioTexto() {
        ItemCombo item = (ItemCombo) comboUsuario.getSelectedItem();
        if (item != null) {
            return item.getValor() == 0 ? "Todos los usuarios" : item.getTexto();
        }
        return "Todos los usuarios";
    }

    public String getTipoMovimiento() {
        return (String) comboTipoMovimiento.getSelectedItem();
    }

    public boolean getIncluirAnulados() {
        return chkIncluirAnulados.isSelected();
    }

    public boolean isAceptado() {
        return aceptado;
    }

    // Clase interna para elementos del combo
    public static class ItemCombo {

        private int valor;
        private String texto;

        public ItemCombo(int valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }

        public int getValor() {
            return valor;
        }

        public String getTexto() {
            return texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        comboUsuario = new javax.swing.JComboBox<>();
        comboTipoMovimiento = new javax.swing.JComboBox<>();
        chkIncluirAnulados = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnFiltrar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Filtro Ingresos - Egresos");

        chkIncluirAnulados.setText("Incluir Anulados");

        jLabel1.setText("Fecha Desde:");

        jLabel2.setText("Fecha Hasta:");

        jLabel3.setText("Usuario:");

        jLabel4.setText("Tipo Movimiento:");

        btnFiltrar.setText("Filtrar");

        btnLimpiar.setText("Limpiar");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(86, 86, 86)
                            .addComponent(chkIncluirAnulados, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(152, 152, 152))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(comboTipoMovimiento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboTipoMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkIncluirAnulados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(vFiltroIngresosEgresos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vFiltroIngresosEgresos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vFiltroIngresosEgresos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vFiltroIngresosEgresos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                vFiltroIngresosEgresos dialog = new vFiltroIngresosEgresos(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JCheckBox chkIncluirAnulados;
    private javax.swing.JComboBox<String> comboTipoMovimiento;
    private javax.swing.JComboBox<ItemCombo> comboUsuario;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
