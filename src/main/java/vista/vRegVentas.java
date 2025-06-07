package vista;

import controlador.cRegVentas;
import interfaces.myInterface;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.mVentas;

public class vRegVentas extends javax.swing.JInternalFrame implements myInterface {

    private cRegVentas controlador;
    private DefaultTableModel modeloTablaDetalles;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private DecimalFormat dfNumeros = new DecimalFormat("#,##0");

    // Constructor
    public vRegVentas() {
        try {
            initComponents(); // ← Generado por NetBeans

            // Configuraciones básicas de la ventana
            setClosable(true);
            setMaximizable(true);
            setResizable(true);
            setTitle("Registro de Ventas");

            // Inicializar controlador
            this.controlador = new cRegVentas(this);

            // Configuraciones iniciales
            configurarTabla();
            cargarClientes();
            configurarEventos();
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError("Error al inicializar ventana de ventas: " + e.getMessage());
        }
    }

    // Configurar la tabla de detalles
    private void configurarTabla() {
        // Configurar modelo de tabla
        modeloTablaDetalles = new DefaultTableModel(
                new Object[]{"#", "Código", "Descripción", "Cantidad", "Precio Unit.", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo cantidad es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 3:
                    case 4:
                    case 5:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        tblDetalles.setModel(modeloTablaDetalles);

        // Configurar renderizador para alineación
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tblDetalles.getColumnModel().getColumn(0).setCellRenderer(rightRenderer); // #
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
        tblDetalles.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Subtotal

        // Configurar anchos de columnas
        tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(30);  // #
        tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(100); // Código
        tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(250); // Descripción
        tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(80);  // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(100); // Precio
        tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(100); // Subtotal

        // Configurar selección
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // Configurar eventos de los componentes
    private void configurarEventos() {
        // Evento para código de barras (Enter para agregar)
        txtCodigoBarra.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    agregarProducto();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        // Evento para cambio de cliente
        comboCliente.addActionListener(e -> actualizarDatosCliente());

        // Eventos de botones (solo los que existen en la interfaz)
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnEliminarDetalle.addActionListener(e -> eliminarDetalle());
        btnAnular.addActionListener(e -> anularVenta());
    }

    // Cargar clientes en el combo
    private void cargarClientes() {
        try {
            comboCliente.removeAllItems();
            List<cRegVentas.ItemCombo> clientes = controlador.obtenerClientes();

            for (cRegVentas.ItemCombo cliente : clientes) {
                comboCliente.addItem(cliente);
            }

            if (comboCliente.getItemCount() > 0) {
                comboCliente.setSelectedIndex(0);
                actualizarDatosCliente();
            }

        } catch (Exception e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
        }
    }

    // Actualizar datos del cliente seleccionado
    private void actualizarDatosCliente() {
        if (comboCliente.getSelectedItem() != null) {
            cRegVentas.ItemCombo cliente = (cRegVentas.ItemCombo) comboCliente.getSelectedItem();
            int idCliente = (int) cliente.getValor();

            controlador.setCliente(idCliente);

            if (idCliente == 0) {
                // Cliente contado
                txtNombreCliente.setText("CLIENTE CONTADO");
                txtDocumentoCliente.setText("");
            } else {
                // Cliente específico
                Object[] datosCliente = controlador.obtenerDatosCliente(idCliente);
                if (datosCliente != null) {
                    txtNombreCliente.setText((String) datosCliente[0]);
                    txtDocumentoCliente.setText((String) datosCliente[1]);
                }
            }
        }
    }

    // Agregar producto a la venta
    private void agregarProducto() {
        String codBarra = txtCodigoBarra.getText().trim();
        if (codBarra.isEmpty()) {
            mostrarError("Ingrese un código de barras.");
            txtCodigoBarra.requestFocus();
            return;
        }

        int cantidad = (Integer) spinnerCantidad.getValue();
        controlador.agregarProducto(codBarra, cantidad);

        // Limpiar campos
        txtCodigoBarra.setText("");
        spinnerCantidad.setValue(1);
        txtCodigoBarra.requestFocus();
    }

    // Eliminar detalle seleccionado
    private void eliminarDetalle() {
        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada >= 0) {
            controlador.eliminarDetalle(filaSeleccionada);
        } else {
            mostrarError("Seleccione un producto para eliminar.");
        }
    }

    // Buscar venta por ID (llamado desde vPrincipal)
    private void buscarVenta() {
        String input = JOptionPane.showInputDialog(this, "Ingrese el ID de la venta:", "Buscar Venta", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(input.trim());
                controlador.buscarVentaPorId(id);
            } catch (NumberFormatException e) {
                mostrarError("El ID debe ser un número válido.");
            }
        }
    }

    // Guardar venta (llamado desde vPrincipal)
    private void guardarVenta() {
        // Establecer observaciones
        controlador.setObservaciones(txtObservaciones.getText().trim());

        // Guardar venta
        controlador.guardarVenta();
    }

    // Anular venta (único botón en la interfaz)
    private void anularVenta() {
        controlador.anularVenta();
    }

    // Imprimir factura (llamado desde vPrincipal)
    private void imprimirFactura() {
        // TODO: Implementar impresión de factura
        mostrarMensaje("Función de impresión en desarrollo.");
    }

    // MÉTODOS PÚBLICOS PARA EL CONTROLADOR
    // ====================================
    public void actualizarTablaDetalles() {
        DefaultTableModel modelo = controlador.getModeloTablaDetalles();
        tblDetalles.setModel(modelo);

        // Reconfigurar renderizadores después de cambiar el modelo
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tblDetalles.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
    }

    public void actualizarTotalVenta(int total) {
        txtTotal.setText(dfNumeros.format(total));
    }

    public void cargarDatosVenta(mVentas venta) {
        txtIdVenta.setText(String.valueOf(venta.getIdVenta()));
        txtFecha.setText(sdf.format(venta.getFecha()));
        chkAnulado.setSelected(venta.isAnulado());
        txtObservaciones.setText(venta.getObservaciones());

        // Seleccionar cliente
        for (int i = 0; i < comboCliente.getItemCount(); i++) {
            cRegVentas.ItemCombo item = (cRegVentas.ItemCombo) comboCliente.getItemAt(i);
            if ((int) item.getValor() == venta.getIdCliente()) {
                comboCliente.setSelectedIndex(i);
                break;
            }
        }
    }

    public void limpiarFormulario() {
        txtIdVenta.setText("");
        txtFecha.setText(sdf.format(new Date()));
        comboCliente.setSelectedIndex(0);
        txtCodigoBarra.setText("");
        spinnerCantidad.setValue(1);
        txtTotal.setText("0");
        txtObservaciones.setText("");
        chkAnulado.setSelected(false);

        // Limpiar tabla
        modeloTablaDetalles.setRowCount(0);

        // Establecer foco
        txtCodigoBarra.requestFocus();
    }

    public void marcarVentaComoAnulada() {
        chkAnulado.setSelected(true);
        btnAnular.setEnabled(false);  // Solo deshabilitar el botón Anular
        btnAgregarProducto.setEnabled(false);
        btnEliminarDetalle.setEnabled(false);
    }

    public boolean confirmarAnulacion() {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea anular esta venta?\n"
                + "Esta acción restaurará el stock de los productos.",
                "Confirmar Anulación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return opcion == JOptionPane.YES_OPTION;
    }

    public void preguntarImprimirFactura(int idVenta) {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "Venta guardada correctamente.\n¿Desea imprimir la factura?",
                "Imprimir Factura",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            imprimirFactura();
        }
    }

    public void mostrarDialogoFiltros() {
        // TODO: Implementar diálogo de filtros para consultar ventas
        mostrarMensaje("Diálogo de filtros en desarrollo.");
    }

    // Métodos de mensajes
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    // GETTERS PARA EL CONTROLADOR
    // ===========================
    public JTextField getTxtCodigoBarra() {
        return txtCodigoBarra;
    }

    public JSpinner getSpinnerCantidad() {
        return spinnerCantidad;
    }

    public JComboBox<cRegVentas.ItemCombo> getComboCliente() {
        return comboCliente;
    }

    public JTextArea getTxtObservaciones() {
        return txtObservaciones;
    }

    public JTable getTblDetalles() {
        return tblDetalles;
    }

    // IMPLEMENTACIÓN DE myInterface
    // ============================
    @Override
    public void imGrabar() {
        guardarVenta();
    }

    @Override
    public void imFiltrar() {
        mostrarDialogoFiltros();
    }

    @Override
    public void imActualizar() {
        actualizarTablaDetalles();
        actualizarTotalVenta(controlador.getVentaActual().getTotal());
    }

    @Override
    public void imBorrar() {
        anularVenta();
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        if (id > 0) {
            controlador.buscarVentaPorId(id);
        } else {
            limpiarFormulario();
        }
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{
            "ID Venta",
            "Número Factura",
            "Cliente",
            "Fecha",
            "Usuario"
        };
    }

    @Override
    public String getTablaActual() {
        return "ventas_detalle";
    }

    @Override
    public void imNuevo() {
        limpiarFormulario();
        txtCodigoBarra.requestFocus();
    }

    @Override
    public void imBuscar() {
        buscarVenta();
    }

    @Override
    public void imPrimero() {
        try {
            controlador.imPrimero();
        } catch (Exception e) {
            mostrarError("Error al navegar al primer registro: " + e.getMessage());
        }
    }

    @Override
    public void imSiguiente() {
        try {
            controlador.imSiguiente();
        } catch (Exception e) {
            mostrarError("Error al navegar al siguiente registro: " + e.getMessage());
        }
    }

    @Override
    public void imAnterior() {
        try {
            controlador.imAnterior();
        } catch (Exception e) {
            mostrarError("Error al navegar al registro anterior: " + e.getMessage());
        }
    }

    @Override
    public void imUltimo() {
        try {
            controlador.imUltimo();
        } catch (Exception e) {
            mostrarError("Error al navegar al último registro: " + e.getMessage());
        }
    }

    @Override
    public void imImprimir() {
        imprimirFactura();
    }

    @Override
    public void imInsDet() {
        txtCodigoBarra.requestFocus();
        txtCodigoBarra.selectAll();
    }

    @Override
    public void imDelDet() {
        eliminarDetalle();
    }

    @Override
    public void imCerrar() {
        this.dispose();
    }

    @Override
    public boolean imAbierto() {
        return this.isVisible() && !this.isClosed();
    }

    @Override
    public void imAbrir() {
        if (this.isClosed()) {
            mostrarMensaje("La ventana está cerrada. Crear nueva instancia desde el menú principal.");
        } else {
            this.setVisible(true);
            this.toFront();
            txtCodigoBarra.requestFocus();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtIdVenta = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        chkAnulado = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        comboCliente = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtDocumentoCliente = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtNombreCliente = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtCodigoBarra = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        spinnerCantidad = new javax.swing.JSpinner();
        btnAgregarProducto = new javax.swing.JButton();
        btnEliminarDetalle = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        btnAnular = new javax.swing.JButton();

        jLabel1.setText("ID Venta:");

        jLabel2.setText("Fecha:");

        chkAnulado.setText("Anulado");

        jLabel3.setText("Cliente:");

        jLabel4.setText("Documento:");

        jLabel5.setText("Nombre:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtIdVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(jLabel2)
                                .addGap(28, 28, 28)
                                .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(chkAnulado))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(comboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                                .addComponent(txtDocumentoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(183, 183, 183))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtIdVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAnulado))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtDocumentoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(39, 39, 39))
        );

        jLabel6.setText("Codigo de Barras:");

        jLabel7.setText("Cantidad:");

        btnAgregarProducto.setText("Agregar");

        btnEliminarDetalle.setText("Eliminar");

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jScrollPane1.setViewportView(tblDetalles);

        jLabel8.setText("TOTAL:");

        jLabel9.setText("OBSERVACIONES:");

        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(5);
        jScrollPane2.setViewportView(txtObservaciones);

        btnAnular.setText("Anular Venta");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(80, 80, 80)
                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnAnular)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(38, 38, 38)
                .addComponent(btnAnular)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(27, 27, 27)
                        .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel7)
                        .addGap(27, 27, 27)
                        .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(btnAgregarProducto)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminarDetalle)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarProducto)
                    .addComponent(btnEliminarDetalle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnAnular;
    private javax.swing.JButton btnEliminarDetalle;
    private javax.swing.JCheckBox chkAnulado;
    private javax.swing.JComboBox<cRegVentas.ItemCombo> comboCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner spinnerCantidad;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigoBarra;
    private javax.swing.JTextField txtDocumentoCliente;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIdVenta;
    private javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
