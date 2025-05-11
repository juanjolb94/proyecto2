package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class vDetalleProducto extends javax.swing.JDialog {

    private boolean aceptado = false;

    public vDetalleProducto(Frame parent, String title) {
        super(parent, title, true);
        initComponents();
        setLocationRelativeTo(parent);
        
        setupPresentacionComboBox();

        // Configurar eventos para los botones
        btnGuardar1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validarCampos()) {
                    aceptado = true;
                    dispose();
                }
            }
        });

        btnCancelar1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aceptado = false;
                dispose();
            }
        });

        // Establecer el botón por defecto (Enter)
        getRootPane().setDefaultButton(btnGuardar1);
    }

    private boolean validarCampos() {
        // Validar que los campos obligatorios no estén vacíos
        if (txtCodigoBarras.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El código de barras es obligatorio.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            txtCodigoBarras.requestFocus();
            return false;
        }

        if (txtDescripcion1.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "La descripción es obligatoria.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            txtDescripcion1.requestFocus();
            return false;
        }

        return true;
    }

    // Métodos para obtener los valores ingresados
    public String getCodBarras() {
        return txtCodigoBarras.getText().trim();
    }

    public String getDescripcion() {
        return txtDescripcion1.getText().trim();
    }

    public String getPresentacion() {
        if (comboPresentacion != null) {
            return comboPresentacion.getSelectedItem().toString();
        }
        return "";
    }

    public boolean getEstado() {
        return chkEstado1.isSelected();
    }

    public boolean isAceptado() {
        return aceptado;
    }

    // Métodos para precargar valores (útil para edición)
    public void setCodBarras(String codBarras) {
        txtCodigoBarras.setText(codBarras);
    }

    public void setCodBarrasEditable(boolean editable) {
        txtCodigoBarras.setEditable(editable);
        // Cambiar el color de fondo para indicar visualmente que no es editable
        if (!editable) {
            txtCodigoBarras.setBackground(new Color(240, 240, 240));
        } else {
            txtCodigoBarras.setBackground(Color.WHITE);
        }
    }

    public void setDescripcion(String descripcion) {
        txtDescripcion1.setText(descripcion);
    }

    public void setPresentacion(String presentacion) {
        if (comboPresentacion != null) {
            // Try to find and select the matching item
            for (int i = 0; i < comboPresentacion.getItemCount(); i++) {
                if (comboPresentacion.getItemAt(i).equals(presentacion)) {
                    comboPresentacion.setSelectedIndex(i);
                    return;
                }
            }
            // If no match is found, add the value as a new item and select it
            comboPresentacion.addItem(presentacion);
            comboPresentacion.setSelectedItem(presentacion);
        }
    }

    private int getComponentIndex(Container container, Component component) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == component) {
                return i;
            }
        }
        return -1;
    }
    
    private void setupPresentacionComboBox() {
    
    // Add standard presentation options
    comboPresentacion.addItem("UNIDAD");
    comboPresentacion.addItem("CAJA");
    comboPresentacion.addItem("PAQUETE");
    comboPresentacion.addItem("BOLSA");
    comboPresentacion.addItem("BOTELLA");
    comboPresentacion.addItem("LATA");
    comboPresentacion.addItem("FRASCO");
    comboPresentacion.addItem("METRO");
    comboPresentacion.addItem("KILO");
    comboPresentacion.addItem("LITRO");
    
}

    public void setEstado(boolean estado) {
        chkEstado1.setSelected(estado);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtCodigoBarras = new javax.swing.JTextField();
        txtDescripcion1 = new javax.swing.JTextField();
        chkEstado1 = new javax.swing.JCheckBox();
        btnGuardar1 = new javax.swing.JButton();
        btnCancelar1 = new javax.swing.JButton();
        comboPresentacion = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        jLabel1.setText("Codigo de baras:");

        jLabel2.setText("Descripcion:");

        jLabel3.setText("Presentación:");

        jLabel4.setText("Estado:");

        chkEstado1.setText("Activo? Si / No");

        btnGuardar1.setText("Guardar");

        btnCancelar1.setText("Cancelar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnGuardar1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCodigoBarras, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(txtDescripcion1)
                            .addComponent(chkEstado1)
                            .addComponent(comboPresentacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtDescripcion1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboPresentacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(chkEstado1))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar1;
    private javax.swing.JButton btnGuardar1;
    private javax.swing.JCheckBox chkEstado1;
    private javax.swing.JComboBox<String> comboPresentacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JTextField txtDescripcion1;
    // End of variables declaration//GEN-END:variables
}
