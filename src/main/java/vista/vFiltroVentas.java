package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import modelo.ClientesDAO;
import modelo.UsuariosDAO;

public class vFiltroVentas extends javax.swing.JDialog {

    private boolean aceptado = false;

    public vFiltroVentas(java.awt.Frame parent, boolean modal) {
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
        comboCliente.removeAllItems();
        comboUsuario.removeAllItems();
        comboTipoVenta.removeAllItems();
        comboEstado.removeAllItems();

        try {
            // Cargar clientes
            comboCliente.addItem(new ItemCombo(0, "TODOS LOS CLIENTES"));

            ClientesDAO clienteDao = new ClientesDAO();
            List<Object[]> clientes = clienteDao.listarClientes();
            for (Object[] cliente : clientes) {
                comboCliente.addItem(new ItemCombo((int) cliente[0], (String) cliente[1]));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar clientes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Asegurar que al menos hay una opción
            if (comboCliente.getItemCount() == 0) {
                comboCliente.addItem(new ItemCombo(0, "TODOS LOS CLIENTES"));
            }
        }

        try {
            // Cargar usuarios
            comboUsuario.addItem(new ItemCombo(0, "TODOS LOS USUARIOS"));

            UsuariosDAO usuarioDao = new UsuariosDAO();
            List<Object[]> usuarios = usuarioDao.listarUsuarios();
            for (Object[] usuario : usuarios) {
                comboUsuario.addItem(new ItemCombo((int) usuario[0], (String) usuario[1]));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Asegurar que al menos hay una opción
            if (comboUsuario.getItemCount() == 0) {
                comboUsuario.addItem(new ItemCombo(0, "TODOS LOS USUARIOS"));
            }
        }

        // Cargar tipos de venta
        comboTipoVenta.addItem("TODOS");
        comboTipoVenta.addItem("CONTADO");
        comboTipoVenta.addItem("CREDITO");

        // Cargar estados
        comboEstado.addItem("TODOS");
        comboEstado.addItem("PENDIENTE");
        comboEstado.addItem("PAGADA");
        comboEstado.addItem("CREDITO");
        comboEstado.addItem("ANULADA");
    }

    private void establecerValoresPorDefecto() {
        // Establecer fechas por defecto (primer día del mes actual hasta hoy)
        Date hoy = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(hoy);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        Date primerDiaMes = cal.getTime();

        jDateChooser1.setDate(primerDiaMes);
        jDateChooser2.setDate(hoy);

        // Seleccionar opciones por defecto
        comboCliente.setSelectedIndex(0);
        comboUsuario.setSelectedIndex(0);
        comboTipoVenta.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
        chkIncluirAnulados.setSelected(false);
    }

    private void limpiarCampos() {
        establecerValoresPorDefecto();
        txtNumeroFactura.setText("");
    }

    private boolean validarDatos() {
        try {
            // Validar fechas
            Date fechaDesde = jDateChooser1.getDate();
            Date fechaHasta = jDateChooser2.getDate();

            if (fechaDesde == null || fechaHasta == null) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar ambas fechas",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (fechaDesde.after(fechaHasta)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha desde no puede ser mayor a la fecha hasta",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al validar fechas: " + e.getMessage(),
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Métodos getter para obtener los valores seleccionados
    public boolean isAceptado() {
        return aceptado;
    }

    public Date getFechaDesde() {
        return jDateChooser1.getDate();
    }

    public Date getFechaHasta() {
        return jDateChooser2.getDate();
    }

    public int getClienteId() {
        if (comboCliente.getSelectedItem() instanceof ItemCombo) {
            ItemCombo item = (ItemCombo) comboCliente.getSelectedItem();
            return item != null ? item.getValor() : 0;
        }
        return 0;
    }

    public JComboBox<ItemCombo> getComboCliente() {
        return comboCliente;
    }

    public JComboBox<ItemCombo> getComboUsuario() {
        return comboUsuario;
    }

    public int getUsuarioId() {
        if (comboUsuario.getSelectedItem() instanceof ItemCombo) {
            ItemCombo item = (ItemCombo) comboUsuario.getSelectedItem();
            return item != null ? item.getValor() : 0;
        }
        return 0;
    }

    public String getTipoVenta() {
        String tipo = (String) comboTipoVenta.getSelectedItem();
        return "TODOS".equals(tipo) ? null : tipo;
    }

    public String getEstado() {
        String estado = (String) comboEstado.getSelectedItem();
        return "TODOS".equals(estado) ? null : estado;
    }

    public String getNumeroFactura() {
        String numero = txtNumeroFactura.getText().trim();
        return numero.isEmpty() ? null : numero;
    }

    public boolean getIncluirAnulados() {
        return chkIncluirAnulados.isSelected();
    }

    // Métodos adicionales para obtener textos
    public String getClienteTexto() {
        ItemCombo item = (ItemCombo) comboCliente.getSelectedItem();
        return item != null ? item.getTexto() : "TODOS LOS CLIENTES";
    }

    public String getUsuarioTexto() {
        ItemCombo item = (ItemCombo) comboUsuario.getSelectedItem();
        return item != null ? item.getTexto() : "TODOS LOS USUARIOS";
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

        jLabel1 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        comboCliente = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        comboUsuario = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        comboTipoVenta = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        comboEstado = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtNumeroFactura = new javax.swing.JTextField();
        chkIncluirAnulados = new javax.swing.JCheckBox();
        btnFiltrar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Fecha Desde:");

        jLabel2.setText("Fecha Hasta:");

        jLabel3.setText("Cliente:");

        jLabel4.setText("Usuario/Vendedor:");

        jLabel5.setText("Tipo Venta:");

        comboTipoVenta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Estado:");

        comboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Nº Factura:");

        chkIncluirAnulados.setText("Incluir Ventas Anuladas");

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(comboCliente, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboUsuario, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addGap(26, 26, 26)
                        .addComponent(comboTipoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNumeroFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(comboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkIncluirAnulados)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(comboUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(comboTipoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtNumeroFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(comboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkIncluirAnulados))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(btnFiltrar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
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
            java.util.logging.Logger.getLogger(vFiltroVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vFiltroVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vFiltroVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vFiltroVentas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                vFiltroVentas dialog = new vFiltroVentas(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<ItemCombo> comboCliente;
    private javax.swing.JComboBox<String> comboEstado;
    private javax.swing.JComboBox<String> comboTipoVenta;
    private javax.swing.JComboBox<ItemCombo> comboUsuario;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField txtNumeroFactura;
    // End of variables declaration//GEN-END:variables
}
