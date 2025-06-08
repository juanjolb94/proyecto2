package vista;

import controlador.cRegVentas;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class vSeleccionProductoVentas extends javax.swing.JDialog {

    private cRegVentas controlador;
    private DefaultTableModel modeloTabla;
    private DecimalFormat dfNumeros = new DecimalFormat("#,##0");
    private Object[] productoSeleccionado;
    private boolean productoElegido = false;

    public vSeleccionProductoVentas(Frame parent, cRegVentas controlador) {
        super(parent, "Seleccionar Producto", true);
        this.controlador = controlador;

        initComponents();
        configurarSpinners();
        configurarTabla();
        configurarEventos();

        setLocationRelativeTo(parent);
        txtBusqueda.requestFocus();
    }

    private void configurarSpinners() {
        // Configurar spinner de cantidad - no permitir negativos, iniciar en 1
        SpinnerNumberModel modeloCantidad = new SpinnerNumberModel(1, 1, 999, 1);
        spinnerCantidad.setModel(modeloCantidad);
    }

    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"Código", "Nombre", "Descripción", "Precio"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Integer.class : String.class;
            }
        };

        tblProductos.setModel(modeloTabla);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar renderizador para precio
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblProductos.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Configurar anchos de columna
        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(150); // Código
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(100); // Nombre
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(250); // Descripción
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(50); // Precio
    }

    private void configurarEventos() {
        // Evento para búsqueda con Enter
        txtBusqueda.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarProductos();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        //Seleccionar todo el texto al hacer focus en txtBusqueda**
        txtBusqueda.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Usar SwingUtilities.invokeLater para asegurar que la selección se ejecute correctamente
                SwingUtilities.invokeLater(() -> txtBusqueda.selectAll());
            }
        });

        //Seleccionar todo al hacer click en txtBusqueda**
        txtBusqueda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Usar SwingUtilities.invokeLater para que se ejecute después del click
                SwingUtilities.invokeLater(() -> txtBusqueda.selectAll());
            }
        });

        // Eventos de botones
        btnBuscar.addActionListener(e -> buscarProductos());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnCancelar.addActionListener(e -> dispose());

        // Evento de selección en tabla
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tblProductos.getSelectedRow();
                btnAgregar.setEnabled(filaSeleccionada >= 0);
            }
        });

        // Doble clic para agregar directamente
        tblProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarProducto();
                }
            }
        });
    }

    private void buscarProductos() {
        String termino = txtBusqueda.getText().trim();

        if (termino.length() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese al menos 2 caracteres para buscar.",
                    "Búsqueda",
                    JOptionPane.WARNING_MESSAGE);
            txtBusqueda.requestFocus();
            return;
        }

        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Buscar productos
        List<Object[]> productos = controlador.buscarProductosPorNombre(termino);

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron productos con: " + termino,
                    "Sin Resultados",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Llenar tabla con resultados
        for (Object[] producto : productos) {
            modeloTabla.addRow(new Object[]{
                producto[2], // código de barras
                producto[1], // nombre
                producto[3], // descripción
                dfNumeros.format((Integer) producto[4]) // precio formateado
            });
        }

        // Seleccionar primer resultado
        if (tblProductos.getRowCount() > 0) {
            tblProductos.setRowSelectionInterval(0, 0);
        }
    }

    private void agregarProducto() {
        int filaSeleccionada = tblProductos.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un producto de la tabla.",
                    "Selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener producto de la búsqueda original
        String codigoBarra = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        int cantidad = (Integer) spinnerCantidad.getValue();

        // Buscar el producto completo por código de barras
        Object[] producto = controlador.buscarProductoPorCodBarra(codigoBarra);

        if (producto != null) {
            // Agregar producto a la venta
            controlador.agregarProductoSeleccionado(producto, cantidad);

            // Cerrar ventana
            productoElegido = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener datos del producto.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isProductoElegido() {
        return productoElegido;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBusqueda = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBusqueda = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        spinnerCantidad = new javax.swing.JSpinner();
        panelTabla = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        panelBotones = new javax.swing.JPanel();
        btnAgregar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelBusqueda.setToolTipText("Busqueda de Productos");

        jLabel1.setText("Buscar por nombre:");

        btnBuscar.setText("Buscar");

        jLabel2.setText("Cantidad:");

        javax.swing.GroupLayout panelBusquedaLayout = new javax.swing.GroupLayout(panelBusqueda);
        panelBusqueda.setLayout(panelBusquedaLayout);
        panelBusquedaLayout.setHorizontalGroup(
            panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBusquedaLayout.setVerticalGroup(
            panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar)
                    .addComponent(jLabel2)
                    .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTabla.setToolTipText("Productos Encontrados");

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

        javax.swing.GroupLayout panelTablaLayout = new javax.swing.GroupLayout(panelTabla);
        panelTabla.setLayout(panelTablaLayout);
        panelTablaLayout.setHorizontalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTablaLayout.setVerticalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        btnAgregar.setText("Agregar Producto");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAgregar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTabla, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTabla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelBusqueda;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JSpinner spinnerCantidad;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBusqueda;
    // End of variables declaration//GEN-END:variables
}
