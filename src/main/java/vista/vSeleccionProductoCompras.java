package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import controlador.cRegCompras;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class vSeleccionProductoCompras extends JDialog {

    // Variables de control
    private cRegCompras controlador;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean aceptado = false;
    private DecimalFormat formateadorNumeros;

    // Datos del producto seleccionado
    private int idProductoSeleccionado;
    private String codBarraSeleccionado;
    private String descripcionSeleccionada;
    private int cantidad;
    private double precio;

    public vSeleccionProductoCompras(java.awt.Frame parent, cRegCompras controlador) {
        super(parent, "Seleccionar Producto", true);
        this.controlador = controlador;

        // Inicializar el formateador con puntos
        formateadorNumeros = new DecimalFormat("#,##0");
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        formateadorNumeros.setDecimalFormatSymbols(simbolos);

        initComponents();

        modeloTabla = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Código Barras", "Nombre", "Descripción", "Presentación"}
        );
        tblProductos.setModel(modeloTabla);

        sorter = new TableRowSorter<>(modeloTabla);
        tblProductos.setRowSorter(sorter);

        txtCantidad.setText("1");
        txtPrecio.setText("0");
        txtSubtotal.setText("0");

        cargarProductos();
        configurarEventos();

        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(200);
        tblProductos.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Configuración final del diálogo
        pack();
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void cargarProductos() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);

            // Obtener productos desde el controlador
            List<Object[]> productos = controlador.obtenerProductosParaCompra();

            for (Object[] producto : productos) {
                modeloTabla.addRow(new Object[]{
                    producto[0], // ID
                    producto[2], // Código de Barras
                    producto[1], // Nombre
                    producto[3], // Descripción
                    producto[4] // Presentación
                });
            }

            // Ajustar anchos de columnas
            tblProductos.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblProductos.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblProductos.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblProductos.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblProductos.getColumnModel().getColumn(4).setPreferredWidth(100);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar productos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        // Filtrar productos al escribir en el campo de búsqueda
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarProductos();
            }
        });

        // Al seleccionar un producto, mostrar sus datos
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblProductos.getSelectedRow() >= 0) {
                int row = tblProductos.getSelectedRow();
                int modelRow = tblProductos.convertRowIndexToModel(row);

                // Mostrar datos en campos
                txtCodBarra.setText(modeloTabla.getValueAt(modelRow, 1).toString());

                // Concatenar nombre y descripción
                String nombre = modeloTabla.getValueAt(modelRow, 2).toString();
                String descripcion = modeloTabla.getValueAt(modelRow, 3).toString();
                txtDescripcion.setText(nombre + " - " + descripcion);

                // Calcular subtotal
                calcularSubtotal();
            }
        });

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
            if (validarSeleccion()) {
                aceptado = true;
                dispose();
            }
        });

        // Botón cancelar
        btnCancelar.addActionListener((ActionEvent e) -> {
            aceptado = false;
            dispose();
        });

        // Doble clic en la tabla selecciona el producto
        tblProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    if (validarSeleccion()) {
                        aceptado = true;
                        dispose();
                    }
                }
            }
        });

        // Tecla Enter en los campos numéricos
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (validarSeleccion()) {
                        aceptado = true;
                        dispose();
                    }
                }
            }
        };

        txtCantidad.addKeyListener(enterKeyAdapter);
        txtPrecio.addKeyListener(enterKeyAdapter);

        // Para el campo de precio
        txtPrecio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                ultimoCampoEditadoEsSubtotal = false;
                calcularSubtotal();
            }
        });

        // Para el campo de subtotal
        txtSubtotal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                ultimoCampoEditadoEsSubtotal = true;
                calcularSubtotal();
            }
        });
    }

    private void filtrarProductos() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();

        if (textoBusqueda.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Filtrar en todas las columnas
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoBusqueda));
        }
    }

    private boolean ultimoCampoEditadoEsSubtotal = false;

    private void calcularSubtotal() {
        try {
            // Obtener cantidad
            int cantidad = Integer.parseInt(txtCantidad.getText().trim().replace(".", "").replace(",", "."));

            if (cantidad <= 0) {
                txtSubtotal.setText("0");
                txtPrecio.setText("0");
                this.subtotalExacto = 0;
                return;
            }

            // Lógica diferente dependiendo de qué campo fue editado por último
            if (ultimoCampoEditadoEsSubtotal) {
                // El usuario editó el subtotal, calcular precio unitario e impuesto
                double subtotal = Double.parseDouble(txtSubtotal.getText().trim().replace(".", "").replace(",", "."));

                // Calcular impuesto usando método paraguayo (dividir por 11 para IVA 10%)
                double impuesto = subtotal / 11.0;

                // Calcular base imponible
                double baseImponible = subtotal - impuesto;

                // Calcular precio unitario para mostrar
                double precioUnitario = subtotal / cantidad;
                int precioUnitarioRedondeado = (int) Math.round(precioUnitario);

                // Actualizar campo de precio usando el formateador
                txtPrecio.setText(formateadorNumeros.format(precioUnitarioRedondeado));

                // Guardar el subtotal exacto ingresado
                this.subtotalExacto = subtotal;
                this.baseImponibleCalculada = (int) Math.round(baseImponible);
                this.impuestoCalculado = (int) Math.round(impuesto);
            } else {
                // El usuario editó el precio, calcular subtotal e impuesto
                double precioUnitario = Double.parseDouble(txtPrecio.getText().trim().replace(".", "").replace(",", "."));

                // Calcular subtotal
                double subtotal = cantidad * precioUnitario;
                int subtotalRedondeado = (int) Math.round(subtotal);

                // Calcular impuesto diviendo el subtotal por 11
                int impuesto = (int) Math.round(subtotalRedondeado / 11.0);

                // Calcular base imponible
                int baseImponible = subtotalRedondeado - impuesto;

                // Actualizar campo de subtotal usando el formateador
                txtSubtotal.setText(formateadorNumeros.format(subtotalRedondeado));

                // Guardar valores calculados
                this.subtotalExacto = subtotal;
                this.baseImponibleCalculada = baseImponible;
                this.impuestoCalculado = impuesto;
            }
        } catch (NumberFormatException e) {
            txtSubtotal.setText("0");
            txtPrecio.setText("0");
            this.subtotalExacto = 0;
            this.baseImponibleCalculada = 0;
            this.impuestoCalculado = 0;
        }
    }
    
    // Campo para almacenar el subtotal exacto
    private double subtotalExacto;
    
    private int baseImponibleCalculada;
    private int impuestoCalculado;

    // Getters
    public int getBaseImponibleCalculada() {
        return baseImponibleCalculada;
    }

    public int getImpuestoCalculado() {
        return impuestoCalculado;
    }

    private boolean validarSeleccion() {
        // Verificar que haya una fila seleccionada
        int filaSeleccionada = tblProductos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un producto",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            // Obtener cantidad
            String cantidadStr = txtCantidad.getText().trim().replace(",", ".");
            cantidad = Integer.parseInt(cantidadStr);

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this,
                        "La cantidad debe ser mayor a cero",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
                txtCantidad.requestFocus();
                return false;
            }

            // Obtener y validar subtotal (independientemente de cómo se calculó)
            String subtotalStr = txtSubtotal.getText().trim().replace(",", ".");
            double subtotalDouble = Double.parseDouble(subtotalStr);

            if (subtotalDouble <= 0) {
                JOptionPane.showMessageDialog(this,
                        "El subtotal debe ser mayor a cero",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);

                // Enfocar el campo apropiado según el último modo usado
                if (ultimoCampoEditadoEsSubtotal) {
                    txtSubtotal.requestFocus();
                } else {
                    txtPrecio.requestFocus();
                }
                return false;
            }

            // Guardar datos del producto seleccionado
            int modelRow = tblProductos.convertRowIndexToModel(filaSeleccionada);
            idProductoSeleccionado = Integer.parseInt(modeloTabla.getValueAt(modelRow, 0).toString());
            codBarraSeleccionado = modeloTabla.getValueAt(modelRow, 1).toString();
            descripcionSeleccionada = modeloTabla.getValueAt(modelRow, 2).toString() + " - "
                    + modeloTabla.getValueAt(modelRow, 3).toString();

            // El subtotal ya se guardó en this.subtotalExacto
            return true;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese valores numéricos válidos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isSubtotalDirectamenteIngresado() {
        return ultimoCampoEditadoEsSubtotal;
    }

    // Getters para obtener los datos seleccionados
    public boolean isAceptado() {
        return aceptado;
    }

    public int getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    public String getCodBarraSeleccionado() {
        return codBarraSeleccionado;
    }

    public String getDescripcionSeleccionada() {
        return descripcionSeleccionada;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return subtotalExacto; // Devolver el subtotal exacto en lugar del precio unitario
    }

    // Añadir un getter para indicar que el valor es subtotal
    public boolean esSubtotal() {
        return true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        searchPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        productPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        detailPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtCodBarra = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        mainPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Buscar:");

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblProductos);

        javax.swing.GroupLayout productPanelLayout = new javax.swing.GroupLayout(productPanel);
        productPanel.setLayout(productPanelLayout);
        productPanelLayout.setHorizontalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, productPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addContainerGap())
        );
        productPanelLayout.setVerticalGroup(
            productPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel2.setText("Codigo de barras:");

        jLabel3.setText("Descripción:");

        jLabel4.setText("Cantidad:");

        jLabel5.setText("Precio Unitario:");

        jLabel6.setText("Subtotal:");

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCodBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(detailPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPrecio)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCodBarra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAceptar.setText("Aceptar");
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(328, Short.MAX_VALUE)
                .addComponent(btnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAceptar, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(productPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(detailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAceptarActionPerformed


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
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel productPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCodBarra;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtSubtotal;
    // End of variables declaration//GEN-END:variables
}
