package vista;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class vEditarDetalleCompra extends JDialog {

    // Variables de control
    private boolean aceptado = false;
    private DecimalFormat formateadorNumeros;

    // Datos del detalle
    private int idProducto;
    private String codBarra;
    private int cantidad;
    private double precio;

    public vEditarDetalleCompra(java.awt.Frame parent, String codBarra, String descripcion,
            int cantidad, double precio, int idProducto) {
        super(parent, "Editar Detalle de Compra", true);

        // Inicializar el formateador con puntos
        formateadorNumeros = new DecimalFormat("#,##0");
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        formateadorNumeros.setDecimalFormatSymbols(simbolos);

        this.codBarra = codBarra;
        this.cantidad = cantidad;
        this.precio = precio;
        this.idProducto = idProducto;

        initComponents();
        configurarEventos();

        // Llenar los datos del detalle - redondeando el precio
        txtCodBarra.setText(codBarra);
        txtDescripcion.setText(descripcion);
        txtCantidad.setText(String.valueOf(cantidad));

        // Redondear el precio a entero
        int precioRedondeado = (int) Math.round(precio);
        txtPrecio.setText(String.valueOf(precioRedondeado));

        // Calcular y mostrar subtotal
        calcularSubtotal();

        // Configuración final del diálogo
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void configurarEventos() {
        // Calcular subtotal al cambiar cantidad o precio
        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularSubtotal();
            }
        });

        txtPrecio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcularSubtotal();
            }
        });

        // Botón aceptar
        btnAceptar.addActionListener((ActionEvent e) -> {
            if (validarDatos()) {
                aceptado = true;
                dispose();
            }
        });

        // Botón cancelar
        btnCancelar.addActionListener((ActionEvent e) -> {
            aceptado = false;
            dispose();
        });

        // Tecla Enter en los campos numéricos
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (validarDatos()) {
                        aceptado = true;
                        dispose();
                    }
                }
            }
        };

        txtCantidad.addKeyListener(enterKeyAdapter);
        txtPrecio.addKeyListener(enterKeyAdapter);
    }

    private void calcularSubtotal() {
        try {
            // Parseamos el texto eliminando los puntos de miles para la conversión
            int cantidad = Integer.parseInt(txtCantidad.getText().trim().replace(".", "").replace(",", "."));

            if (cantidad <= 0) {
                txtSubtotal.setText("0");
                txtPrecio.setText("0");
                this.subtotalExacto = 0;
                return;
            }

            // Determinar si se está editando precio o subtotal
            if (ultimoCampoEditadoEsSubtotal) {
                // El usuario editó el subtotal directamente
                double subtotal = Double.parseDouble(txtSubtotal.getText().trim().replace(".", "").replace(",", "."));

                // Calcular impuesto usando método paraguayo
                double impuesto = subtotal / 11.0;
                int impuestoRedondeado = (int) Math.round(impuesto);

                // Calcular base imponible
                int baseImponible = (int) Math.round(subtotal) - impuestoRedondeado;

                // Calcular precio unitario para mostrar
                double precioUnitario = subtotal / cantidad;
                int precioUnitarioRedondeado = (int) Math.round(precioUnitario);

                // Actualizar campo
                txtPrecio.setText(formateadorNumeros.format(precioUnitarioRedondeado));

                // Guardar valores
                this.subtotalExacto = subtotal;
                this.baseImponibleCalculada = baseImponible;
                this.impuestoCalculado = impuestoRedondeado;
            } else {
                // El usuario editó el precio unitario
                double precioUnitario = Double.parseDouble(txtPrecio.getText().trim().replace(".", "").replace(",", "."));

                // Calcular subtotal
                double subtotal = cantidad * precioUnitario;
                int subtotalRedondeado = (int) Math.round(subtotal);

                // Calcular impuesto usando método paraguayo
                int impuesto = (int) Math.round(subtotalRedondeado / 11.0);

                // Calcular base imponible
                int baseImponible = subtotalRedondeado - impuesto;

                // Actualizar campo
                txtSubtotal.setText(formateadorNumeros.format(subtotalRedondeado));

                // Guardar valores
                this.subtotalExacto = subtotal;
                this.baseImponibleCalculada = baseImponible;
                this.impuestoCalculado = impuesto;
            }
        } catch (NumberFormatException e) {
            txtSubtotal.setText("0");
            this.subtotalExacto = 0;
            this.baseImponibleCalculada = 0;
            this.impuestoCalculado = 0;
        }
    }

    // Añadir estos campos a la clase vEditarDetalleCompra
    private double subtotalExacto;
    private int baseImponibleCalculada;
    private int impuestoCalculado;
    private boolean ultimoCampoEditadoEsSubtotal = false;

    // Añadir getters para estos campos
    public int getBaseImponibleCalculada() {
        return baseImponibleCalculada;
    }

    public int getImpuestoCalculado() {
        return impuestoCalculado;
    }

    private boolean validarDatos() {
        try {
            // Validar cantidad - quitar formateadores para la conversión
            String cantidadStr = txtCantidad.getText().trim().replace(".", "").replace(",", ".");
            this.cantidad = Integer.parseInt(cantidadStr);

            if (this.cantidad <= 0) {
                JOptionPane.showMessageDialog(this,
                        "La cantidad debe ser mayor a cero",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
                txtCantidad.requestFocus();
                return false;
            }

            // Validar precio - quitar formateadores para la conversión
            String precioStr = txtPrecio.getText().trim().replace(".", "").replace(",", ".");
            double precioDouble = Double.parseDouble(precioStr);

            // Redondear el precio a entero
            this.precio = (int) Math.round(precioDouble);

            if (this.precio <= 0) {
                JOptionPane.showMessageDialog(this,
                        "El precio debe ser mayor a cero",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
                txtPrecio.requestFocus();
                return false;
            }

            // Actualizar el campo con el valor redondeado y formateado
            txtPrecio.setText(formateadorNumeros.format(this.precio));

            return true;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese valores numéricos válidos para cantidad y precio",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Getters para obtener los datos modificados
    public boolean isAceptado() {
        return aceptado;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getCodBarra() {
        return codBarra;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return subtotalExacto; // Devolver el subtotal exacto en lugar del precio unitario
    }

    public boolean esSubtotal() {
        return true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        detailPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCodBarra = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        txtCantidad = new javax.swing.JTextField();
        txtPrecio = new javax.swing.JTextField();
        txtSubtotal = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        mainPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Codigo de barras:");

        jLabel2.setText("Descripcion:");

        jLabel3.setText("Cantidad:");

        jLabel4.setText("Precio Unitario:");

        jLabel5.setText("Subtotal:");

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(txtPrecio)
                    .addComponent(txtCantidad)
                    .addComponent(txtDescripcion)
                    .addComponent(txtCodBarra))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCodBarra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAceptar.setText("Aceptar");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAceptar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(detailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(detailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCodBarra;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtSubtotal;
    // End of variables declaration//GEN-END:variables
}
