package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import modelo.ProveedoresDAO;

public class vFiltroCompras extends javax.swing.JDialog {

    private boolean aceptado = false;

    public vFiltroCompras(java.awt.Frame parent, boolean modal) {
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
        comboProveedor.removeAllItems();
        comboTipo.removeAllItems();
        comboCondicion.removeAllItems();

        try {
            // Cargar proveedores
            comboProveedor.addItem(new ItemCombo(0, "TODOS LOS PROVEEDORES"));

            ProveedoresDAO dao = new ProveedoresDAO();
            List<Object[]> proveedores = dao.listarProveedores();
            for (Object[] proveedor : proveedores) {
                comboProveedor.addItem(new ItemCombo((int) proveedor[0], (String) proveedor[1]));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar proveedores: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Asegurar que al menos hay una opción
            if (comboProveedor.getItemCount() == 0) {
                comboProveedor.addItem(new ItemCombo(0, "TODOS LOS PROVEEDORES"));
            }
        }

        // Cargar tipos de documento (siempre, sin try-catch)
        comboTipo.addItem("TODOS");
        comboTipo.addItem("FACTURA");
        comboTipo.addItem("BOLETA");
        comboTipo.addItem("NOTA DE CREDITO");

        // Cargar condiciones (siempre, sin try-catch)
        comboCondicion.addItem("TODAS");
        comboCondicion.addItem("CONTADO");
        comboCondicion.addItem("CREDITO");
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
        comboProveedor.setSelectedIndex(0);
        comboTipo.setSelectedIndex(0);
        comboCondicion.setSelectedIndex(0);
        chkIncluirAnulados.setSelected(false);
    }

    private void limpiarCampos() {
        establecerValoresPorDefecto();
        txtNumeroDocumento.setText("");
        txtTimbrado.setText("");
    }

    private boolean validarDatos() {
        try {
            // Validar fechas
            Date fechaDesde = jDateChooser1.getDate();  // <- CAMBIO AQUÍ
            Date fechaHasta = jDateChooser2.getDate();  // <- CAMBIO AQUÍ

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

    public int getProveedorId() {
        if (comboProveedor.getSelectedItem() instanceof ItemCombo) {
            ItemCombo item = (ItemCombo) comboProveedor.getSelectedItem();
            return item != null ? item.getValor() : 0;
        }
        return 0;
    }

    // Método para obtener el texto del proveedor seleccionado
    public String getProveedorTexto() {
        ItemCombo item = (ItemCombo) comboProveedor.getSelectedItem();
        return item != null ? item.getTexto() : "TODOS LOS PROVEEDORES";
    }

    public String getTipoDocumento() {
        String tipo = (String) comboTipo.getSelectedItem();
        return "TODOS".equals(tipo) ? null : tipo;
    }

    public String getCondicion() {
        String condicion = (String) comboCondicion.getSelectedItem();
        return "TODAS".equals(condicion) ? null : condicion;
    }

    public String getNumeroDocumento() {
        String numero = txtNumeroDocumento.getText().trim();
        return numero.isEmpty() ? null : numero;
    }

    public String getTimbrado() {
        String timbrado = txtTimbrado.getText().trim();
        return timbrado.isEmpty() ? null : timbrado;
    }

    public boolean getIncluirAnulados() {
        return chkIncluirAnulados.isSelected();
    }

    public JComboBox<ItemCombo> getComboProveedor() {
        return comboProveedor;
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        comboProveedor = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        comboTipo = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        comboCondicion = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTimbrado = new javax.swing.JTextField();
        chkIncluirAnulados = new javax.swing.JCheckBox();
        btnFiltrar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Fecha Desde:");

        jLabel2.setText("Fecha Hasta:");

        jLabel3.setText("Proveedor:");

        jLabel4.setText("Tipo Documento:");

        comboTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Condición:");

        comboCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Nro Documento:");

        jLabel7.setText("Timbrado:");

        chkIncluirAnulados.setText("Incluir Compras Anuladas");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(comboProveedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(comboCondicion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(jLabel7))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtNumeroDocumento)
                                .addComponent(txtTimbrado))))
                    .addComponent(chkIncluirAnulados))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(comboTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(comboCondicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkIncluirAnulados)))
        );

        btnFiltrar.setText("Filtrar");

        btnLimpiar.setText("Limpiar");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(vFiltroCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(vFiltroCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(vFiltroCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(vFiltroCompras.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                vFiltroCompras dialog = new vFiltroCompras(new javax.swing.JFrame(), true);
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
    private javax.swing.JComboBox<String> comboCondicion;
    private javax.swing.JComboBox<ItemCombo> comboProveedor;
    private javax.swing.JComboBox<String> comboTipo;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtTimbrado;
    // End of variables declaration//GEN-END:variables
}
