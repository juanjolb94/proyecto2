package vista;

import controlador.cAjusteStock;
import modelo.AjusteStockDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class vSeleccionProductoAjuste extends javax.swing.JDialog {

    private cAjusteStock controladorAjuste;
    private AjusteStockDAO modelo;
    private DefaultTableModel modeloTabla;
    private DecimalFormat formatoDecimal;
    private boolean productoSeleccionado = false;

    public vSeleccionProductoAjuste(java.awt.Frame parent, cAjusteStock controlador) {
        super(parent, true);
        this.controladorAjuste = controlador;
        this.formatoDecimal = new DecimalFormat("#,##0");

        // Manejar posible SQLException del constructor de AjusteStockDAO
        try {
            this.modelo = new AjusteStockDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al conectar con la base de datos: " + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        configurarComponentes();
        configurarEventos();
        configurarTabla();

        // Cargar productos iniciales
        cargarTodosLosProductos();

        // Centrar ventana
        setLocationRelativeTo(parent);
    }

    private void configurarComponentes() {
        setTitle("Seleccionar Producto para Ajuste");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Configurar campo de búsqueda
        txtBusqueda.setToolTipText("Ingrese código de barras, nombre o descripción del producto");

        // Configurar botones
        btnAgregar.setEnabled(false);

        // Configurar etiqueta de resultados
        lblResultados.setText("Productos disponibles");
    }

    private void configurarTabla() {
        // Configurar modelo de tabla
        modeloTabla = new DefaultTableModel(
                new Object[]{"Código Barras", "Nombre", "Descripción", "Stock", "Presentación"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // Columna Stock
                    return Integer.class; // Cambiar a Integer
                }
                return String.class;
            }
        };

        tblProductos.setModel(modeloTabla);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProductos.getTableHeader().setReorderingAllowed(false);

        // Configurar ancho de columnas
        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(120); // Código Barras
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(80);  // Stock
        tblProductos.getColumnModel().getColumn(4).setPreferredWidth(100); // Presentación

        // Renderizador personalizado para la columna Stock
        DefaultTableCellRenderer stockRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 3) { // Columna Stock
                    int stock = 0;

                    // Manejar tanto Integer como Double por compatibilidad
                    if (value instanceof Integer) {
                        stock = (Integer) value;
                    } else if (value instanceof Double) {
                        stock = ((Double) value).intValue();
                    } else if (value != null) {
                        try {
                            stock = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            stock = 0;
                        }
                    }

                    // Establecer texto y alineación
                    setText(String.valueOf(stock));
                    setHorizontalAlignment(JLabel.RIGHT);

                    // Configurar colores - fondo igual, solo cambiar texto si stock < 11
                    if (isSelected) {
                        setBackground(table.getSelectionBackground());
                        setForeground(table.getSelectionForeground());
                    } else {
                        setBackground(table.getBackground()); // Fondo siempre igual

                        // Color del texto: rojo si stock < 11, normal en otros casos
                        if (stock < 11) {
                            setForeground(java.awt.Color.RED);
                            setToolTipText("Stock bajo: " + stock + " unidades");
                        } else {
                            setForeground(table.getForeground());
                            setToolTipText("Stock: " + stock + " unidades");
                        }
                    }
                }
                return this;
            }
        };

// Aplicar renderizador solo a la columna Stock
        tblProductos.getColumnModel().getColumn(3).setCellRenderer(stockRenderer);
    }

    private void configurarEventos() {
        // Evento de búsqueda al escribir
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarProductos();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        // Evento de selección en tabla
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tblProductos.getSelectedRow();
                btnAgregar.setEnabled(filaSeleccionada >= 0);

                if (filaSeleccionada >= 0) {
                    // Mostrar información del producto seleccionado
                    String codigo = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
                    String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
                    String descripcion = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
                    // Manejar tanto Integer como Double
                    Object stockObj = modeloTabla.getValueAt(filaSeleccionada, 3);
                    int stock = 0;
                    if (stockObj instanceof Integer) {
                        stock = (Integer) stockObj;
                    } else if (stockObj instanceof Double) {
                        stock = ((Double) stockObj).intValue();
                    }

                    String info = String.format("%s - %s (Stock: %d unidades)",
                            nombre, descripcion, stock);
                    lblResultados.setText(info);
                }
            }
        });

        // Doble clic para agregar producto
        tblProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblProductos.getSelectedRow() >= 0) {
                    agregarProductoSeleccionado();
                }
            }
        });

        // Eventos de botones
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProductoSeleccionado();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Teclas de acceso rápido
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelar");
        actionMap.put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "agregar");
        actionMap.put("agregar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnAgregar.isEnabled()) {
                    agregarProductoSeleccionado();
                }
            }
        });
    }

    private void cargarTodosLosProductos() {
        try {
            // Cargar productos con una búsqueda vacía (todos los productos)
            List<Object[]> productos = modelo.buscarProductos("");
            actualizarTablaProductos(productos);

            lblResultados.setText(productos.size() + " productos encontrados");

        } catch (SQLException e) {
            mostrarError("Error al cargar productos: " + e.getMessage());
        }
    }

    private void buscarProductos() {
        String termino = txtBusqueda.getText().trim();

        if (termino.length() < 2 && !termino.isEmpty()) {
            lblResultados.setText("Ingrese al menos 2 caracteres para buscar");
            return;
        }

        try {
            List<Object[]> productos = modelo.buscarProductos(termino);
            actualizarTablaProductos(productos);

            if (productos.isEmpty()) {
                lblResultados.setText("No se encontraron productos con: '" + termino + "'");
            } else {
                lblResultados.setText(productos.size() + " producto(s) encontrado(s)");

                // Seleccionar primer resultado si hay búsqueda específica
                if (!termino.isEmpty() && tblProductos.getRowCount() > 0) {
                    tblProductos.setRowSelectionInterval(0, 0);
                }
            }

        } catch (SQLException e) {
            mostrarError("Error en la búsqueda: " + e.getMessage());
        }
    }

    private void actualizarTablaProductos(List<Object[]> productos) {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Agregar productos
        for (Object[] producto : productos) {
            modeloTabla.addRow(new Object[]{
                producto[2], // cod_barra
                producto[1], // nombre
                producto[3], // descripcion
                ((Double) producto[4]).intValue(), // stock como entero
                obtenerPresentacion(producto) // presentación (si está disponible)
            });
        }
    }

    private String obtenerPresentacion(Object[] producto) {
        // Si tienes información de presentación en la consulta, úsala
        // Por ahora retornamos un valor por defecto
        return "UND";
    }

    private void agregarProductoSeleccionado() {
        int filaSeleccionada = tblProductos.getSelectedRow();

        if (filaSeleccionada < 0) {
            mostrarError("Seleccione un producto de la tabla.");
            return;
        }

        try {
            // Obtener datos del producto seleccionado
            String codigoBarra = (String) modeloTabla.getValueAt(filaSeleccionada, 0);

            // Buscar el producto completo con stock actualizado
            Object[] productoCompleto = modelo.buscarProductoConStock(codigoBarra);

            if (productoCompleto != null) {
                // Confirmar selección
                String nombre = (String) productoCompleto[1];
                String descripcion = (String) productoCompleto[3];
                // Manejar tanto Integer como Double
                Object stockObj = productoCompleto[4];
                int stock = 0;
                if (stockObj instanceof Integer) {
                    stock = (Integer) stockObj;
                } else if (stockObj instanceof Double) {
                    stock = ((Double) stockObj).intValue();
                }

                String mensaje = String.format(
                        "¿Agregar este producto al ajuste?\n\n"
                        + "Código: %s\n"
                        + "Producto: %s - %s\n"
                        + "Stock actual: %d unidades",
                        codigoBarra, nombre, descripcion, stock
                );

                int opcion = JOptionPane.showConfirmDialog(
                        this,
                        mensaje,
                        "Confirmar Selección",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    // Enviar producto al controlador
                    controladorAjuste.agregarProductoSeleccionado(productoCompleto);

                    productoSeleccionado = true;
                    dispose();
                }

            } else {
                mostrarError("Error al obtener datos completos del producto.");
            }

        } catch (SQLException e) {
            mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para verificar si se seleccionó un producto
    public boolean isProductoSeleccionado() {
        return productoSeleccionado;
    }

    // Sobrescribir setVisible para enfocar el campo de búsqueda
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(() -> {
                txtBusqueda.requestFocus();
                txtBusqueda.selectAll();
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtBusqueda = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        btnAgregar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        lblResultados = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Seleccionar Producto para Ajuste");
        setModal(true);

        jLabel1.setText("Buscar:");

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblProductos);

        btnAgregar.setText("Agregar");

        btnCancelar.setText("Cancelar");

        lblResultados.setText("Productos Disponibles:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblResultados))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblResultados;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBusqueda;
    // End of variables declaration//GEN-END:variables
}
