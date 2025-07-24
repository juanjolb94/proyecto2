package vista;

import controlador.cRegVentas;
import interfaces.myInterface;
import java.awt.Frame;
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
import java.util.Vector;
import modelo.VentaTemporal;

public class vRegVentas extends javax.swing.JInternalFrame implements myInterface {

    private cRegVentas controlador;
    private DefaultTableModel modeloTablaDetalles;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private DecimalFormat dfNumeros = new DecimalFormat("#,##0");
    private int idMesaAsociada = 0;
    private String numeroMesaAsociada = "";
    private vSeleccionMesa ventanaSeleccionMesa = null;

    // Constructor
    public vRegVentas() {
        try {
            initComponents();

            txtIdVenta.setText("0");

            setClosable(true);
            setMaximizable(true);
            setResizable(true);
            setTitle("Registro de Ventas");

            // Inicializar controlador
            this.controlador = new cRegVentas(this);

            // Configuraciones iniciales
            configurarTabla();
            configurarSpinners();
            cargarClientes();
            configurarEventos();
            limpiarFormulario();
            txtFecha.setEditable(false);
            txtTimbrado.setEditable(false);
            txtNumeroFactura.setEditable(false);
            //txtTimbrado.setBackground(this.getBackground());
            //txtNumeroFactura.setBackground(this.getBackground());

        } catch (SQLException e) {
            mostrarError("Error al inicializar ventana de ventas: " + e.getMessage());
        }
    }

    public VentaTemporal obtenerDatosTemporal() {
        System.out.println("DEBUG: obtenerDatosTemporal para Mesa " + numeroMesaAsociada);

        if (idMesaAsociada <= 0) {
            return null;
        }

        VentaTemporal ventaTemporal = new VentaTemporal(idMesaAsociada, numeroMesaAsociada);

        // Guardar cliente
        if (comboCliente.getSelectedItem() != null) {
            cRegVentas.ItemCombo cliente = (cRegVentas.ItemCombo) comboCliente.getSelectedItem();
            ventaTemporal.setIdCliente((int) cliente.getValor());
        }

        // Guardar observaciones
        ventaTemporal.setObservaciones(txtObservaciones.getText().trim());

        //Usar la tabla visual en lugar del modelo de clase**
        DefaultTableModel modeloVisual = (DefaultTableModel) tblDetalles.getModel();
        System.out.println("DEBUG: Filas en tabla visual: " + modeloVisual.getRowCount());

        // Clonar el modelo visual
        DefaultTableModel modeloClonado = new DefaultTableModel();

        // Copiar columnas
        for (int i = 0; i < modeloVisual.getColumnCount(); i++) {
            modeloClonado.addColumn(modeloVisual.getColumnName(i));
        }

        // Copiar filas del modelo visual
        for (int i = 0; i < modeloVisual.getRowCount(); i++) {
            Vector<Object> fila = new Vector<>();
            for (int j = 0; j < modeloVisual.getColumnCount(); j++) {
                Object valor = modeloVisual.getValueAt(i, j);
                fila.add(valor);
                System.out.println("DEBUG: Fila " + i + ", Col " + j + ": " + valor);
            }
            modeloClonado.addRow(fila);
        }

        ventaTemporal.setDatosTabla(modeloClonado);

        // Guardar totales
        try {
            if (!txtTotal.getText().isEmpty()) {
                String totalText = txtTotal.getText().replaceAll("[^0-9]", "");
                if (!totalText.isEmpty()) {
                    ventaTemporal.setTotal(Integer.parseInt(totalText));
                }
            }
            // Subtotal e IVA si existen...
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear totales: " + e.getMessage());
        }

        System.out.println("DEBUG: VentaTemporal creada - Tiene productos: " + ventaTemporal.tieneProductos());
        return ventaTemporal;
    }

    //Método para restaurar datos temporales
    public void restaurarDatosTemporal(VentaTemporal ventaTemporal) {
        System.out.println("DEBUG RESTAURAR: Iniciando restauración para Mesa "
                + (ventaTemporal != null ? ventaTemporal.getNumeroMesa() : "null"));

        if (ventaTemporal == null) {
            System.out.println("DEBUG RESTAURAR: ventaTemporal es null - SALIENDO");
            return;
        }

        System.out.println("DEBUG RESTAURAR: ventaTemporal válido, datos tabla: "
                + (ventaTemporal.getDatosTabla() != null
                ? ventaTemporal.getDatosTabla().getRowCount() + " filas" : "null"));

        try {
            // Restaurar cliente
            seleccionarClientePorId(ventaTemporal.getIdCliente());
            System.out.println("DEBUG RESTAURAR: Cliente restaurado: " + ventaTemporal.getIdCliente());

            // Restaurar observaciones
            if (ventaTemporal.getObservaciones() != null) {
                txtObservaciones.setText(ventaTemporal.getObservaciones());
                System.out.println("DEBUG RESTAURAR: Observaciones restauradas");
            }

            //Restaurar productos a través del controlador**
            DefaultTableModel datosRestaurados = ventaTemporal.getDatosTabla();
            if (datosRestaurados != null) {
                System.out.println("DEBUG RESTAURAR: Restaurando " + datosRestaurados.getRowCount() + " productos a través del controlador");

                //Limpiar controlador primero**
                try {
                    // Crear nueva venta temporal en el controlador
                    controlador.imNuevo();
                    System.out.println("DEBUG RESTAURAR: Controlador limpiado");
                } catch (Exception e) {
                    System.err.println("ERROR al limpiar controlador: " + e.getMessage());
                }

                //Restaurar productos uno por uno a través del controlador**
                for (int i = 0; i < datosRestaurados.getRowCount(); i++) {
                    try {
                        String codigoBarra = datosRestaurados.getValueAt(i, 1).toString();
                        int cantidad = Integer.parseInt(datosRestaurados.getValueAt(i, 3).toString());

                        System.out.println("DEBUG RESTAURAR: Agregando producto " + i + " - " + codigoBarra + " x" + cantidad);

                        // **RESTAURAR A TRAVÉS DEL CONTROLADOR**
                        controlador.agregarProducto(codigoBarra, cantidad);

                    } catch (Exception e) {
                        System.err.println("ERROR al restaurar producto " + i + ": " + e.getMessage());
                    }
                }

                System.out.println("DEBUG RESTAURAR: Productos restaurados a través del controlador");
            }

            // Restaurar totales (opcional, el controlador debería calcularlos)
            if (txtSubtotal != null) {
                txtSubtotal.setText(dfNumeros.format(ventaTemporal.getSubtotal()));
            }
            if (txtIVA10 != null) {
                txtIVA10.setText(dfNumeros.format(ventaTemporal.getIva10()));
            }
            txtTotal.setText(dfNumeros.format(ventaTemporal.getTotal()));

            System.out.println("DEBUG RESTAURAR: Totales restaurados - Total: " + ventaTemporal.getTotal());

            // Reconfigurar tabla
            configurarColumnasTabla();

            // Actualizar vista
            tblDetalles.revalidate();
            tblDetalles.repaint();

            // Mostrar mensaje de restauración
            mostrarMensaje("Datos de la Mesa " + ventaTemporal.getNumeroMesa() + " restaurados correctamente.\n"
                    + "Productos: " + datosRestaurados.getRowCount()
                    + " | Total: " + dfNumeros.format(ventaTemporal.getTotal()));

            DefaultTableModel modeloFinal = (DefaultTableModel) tblDetalles.getModel();
            System.out.println("DEBUG RESTAURAR FINAL: Tabla tiene " + modeloFinal.getRowCount() + " filas al terminar restauración");

            // Esperar un poco y verificar otra vez
            SwingUtilities.invokeLater(() -> {
                DefaultTableModel modeloDespues = (DefaultTableModel) tblDetalles.getModel();
                System.out.println("DEBUG RESTAURAR DELAYED: Tabla tiene " + modeloDespues.getRowCount() + " filas después de SwingUtilities.invokeLater");
            });

            System.out.println("DEBUG RESTAURAR: Restauración completada exitosamente");

        } catch (Exception e) {
            System.err.println("ERROR RESTAURAR: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al restaurar datos temporales: " + e.getMessage());
        }
    }

    //Método para configurar la venta por mesa (llamado desde vSeleccionMesa)
    public void configurarVentaPorMesa(int idMesa, String numeroMesa) {
        System.out.println("DEBUG CONFIG: Configurando venta para Mesa " + numeroMesa + " (ID: " + idMesa + ")");

        this.idMesaAsociada = idMesa;
        this.numeroMesaAsociada = numeroMesa;

        // Actualizar el título de la ventana para mostrar la mesa
        setTitle("Registro de Ventas - Mesa " + numeroMesa);

        // Buscar la ventana de selección de mesas para comunicación posterior
        buscarVentanaSeleccionMesa();

        // Enfocar en el campo de código de barras para empezar a agregar productos
        txtCodigoBarra.requestFocus();

        System.out.println("DEBUG CONFIG: Configuración completada para Mesa " + numeroMesa);
    }

    //Metodo para limpiar el formulario solo cuando NO hay datos temporales
    public void limpiarFormularioSiEsNecesario() {
        System.out.println("DEBUG: Limpiando formulario para nueva venta");
        limpiarFormulario();
    }

    //Método para buscar la ventana de selección de mesas
    private void buscarVentanaSeleccionMesa() {
        try {
            JDesktopPane desktop = getDesktopPane();
            if (desktop != null) {
                for (JInternalFrame frame : desktop.getAllFrames()) {
                    if (frame instanceof vSeleccionMesa) {
                        ventanaSeleccionMesa = (vSeleccionMesa) frame;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar ventana de selección de mesa: " + e.getMessage());
        }
    }

    // Método para verificar si hay productos en la venta
    public boolean tieneProductosEnVenta() {
        return modeloTablaDetalles != null && modeloTablaDetalles.getRowCount() > 0;
    }

    // Método guardarVenta para notificar a la mesa
    private void guardarVenta() {
        try {
            // ... código de validación existente ...

            // Establecer observaciones incluyendo información de la mesa
            String observaciones = txtObservaciones.getText().trim();
            if (idMesaAsociada > 0) {
                observaciones = "Mesa " + numeroMesaAsociada
                        + (observaciones.isEmpty() ? "" : " - " + observaciones);
            }
            controlador.setObservaciones(observaciones);

            // Guardar venta definitivamente
            controlador.guardarVenta();
            // ⚠️ NOTA: La impresión ahora se maneja en el controlador

            // Liberar mesa definitivamente (eliminar datos temporales)
            if (ventanaSeleccionMesa != null && idMesaAsociada > 0) {
                ventanaSeleccionMesa.liberarMesaDefinitivamente(idMesaAsociada);
            }

            // Mostrar mensaje de éxito
            mostrarMensaje("Venta guardada correctamente.\nMesa " + numeroMesaAsociada + " liberada.");

            // Limpiar asociación con mesa y cerrar ventana
            idMesaAsociada = 0;
            numeroMesaAsociada = "";
            setTitle("Registro de Ventas");

            // Cerrar ventana después del guardado exitoso
            this.dispose();

        } catch (Exception e) {
            mostrarError("Error al guardar venta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Método para guardar temporal (sin finalizar venta)
    public void guardarTemporal() {
        if (!tieneProductosEnVenta()) {
            mostrarError("No hay productos para guardar temporalmente.");
            return;
        }

        mostrarMensaje("Datos guardados temporalmente para Mesa " + numeroMesaAsociada + ".\n"
                + "Para finalizar la venta, use Archivo → Guardar.");

        // Los datos se guardarán automáticamente al cerrar la ventana
        this.dispose();
    }

    // Método para actualizar el número de factura
    public void actualizarNumeroFactura(String numeroFactura) {
        txtNumeroFactura.setText(numeroFactura);
    }

    public void actualizarTimbrado(String numeroTimbrado) {
        txtTimbrado.setText(numeroTimbrado);
    }

    // Método para limpiar datos de facturación solo en casos de error o falta de talonario
    public void limpiarDatosFacturacion() {
        System.out.println("DEBUG: Limpiando datos de facturación por error/falta de talonario");
        txtNumeroFactura.setText("");
        txtTimbrado.setText("");
    }

    // Método adicional para mostrar mensajes de error en campos de facturación
    public void mostrarErrorEnFacturacion(String mensajeFactura, String mensajeTimbrado) {
        System.out.println("DEBUG: Mostrando error en campos de facturación");
        txtNumeroFactura.setText(mensajeFactura != null ? mensajeFactura : "ERROR");
        txtTimbrado.setText(mensajeTimbrado != null ? mensajeTimbrado : "SIN TIMBRADO");
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

        configurarColumnasTabla();

        // Configurar selección
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        configurarMenuContextual();

        //Listener para recalcular al editar cantidad**
        tblDetalles.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                actualizarFilaEditada(e.getFirstRow());
            }
        });

        // **LISTENER ADICIONAL: Para detectar cuando termina la edición de celda**
        tblDetalles.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Listener para detectar cuando se termina de editar una celda
        tblDetalles.getDefaultEditor(Integer.class).addCellEditorListener(new javax.swing.event.CellEditorListener() {
            @Override
            public void editingStopped(javax.swing.event.ChangeEvent e) {
                // Se ejecuta cuando termina la edición (Enter, Tab, click fuera, etc.)
                int fila = tblDetalles.getEditingRow();
                int columna = tblDetalles.getEditingColumn();

                // Solo procesar si se editó la columna de cantidad (columna 3)
                if (columna == 3) {
                    // Usar SwingUtilities.invokeLater para asegurar que la tabla se actualice primero
                    SwingUtilities.invokeLater(() -> {
                        actualizarFilaEditada(fila);
                    });
                }
            }

            @Override
            public void editingCanceled(javax.swing.event.ChangeEvent e) {
                // No hacer nada si se cancela la edición
            }
        });

        configurarMenuContextual();
    }

    // Método público para recargar clientes (llamar después de actualizar)
    public void recargarClientes() {
        // Guardar el cliente seleccionado actualmente
        int clienteSeleccionado = 0;
        if (comboCliente.getSelectedItem() != null) {
            clienteSeleccionado = (int) ((cRegVentas.ItemCombo) comboCliente.getSelectedItem()).getValor();
        }

        // Recargar lista de clientes
        cargarClientes();

        // Restaurar selección del cliente
        seleccionarClientePorId(clienteSeleccionado);
        actualizarDatosCliente();
    }

    // Configurar eventos de los componentes
    private void configurarEventos() {
        // Evento para ID de venta (Enter para buscar o limpiar si es 0)
        txtIdVenta.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    procesarIdVenta();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

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

        // Eventos de botones
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnAnular.addActionListener(e -> anularVenta());

        // Seleccionar todo el texto al hacer focus en txtIdVenta
        txtIdVenta.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtIdVenta.selectAll();
            }
        });

        //MouseListener para el click
        txtIdVenta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtIdVenta.selectAll();
            }
        });
    }

    private void configurarSpinners() {
        // Configurar spinner de cantidad - no permitir negativos, iniciar en 1
        SpinnerNumberModel modeloCantidad = new SpinnerNumberModel(1, 1, 999, 1);
        spinnerCantidad.setModel(modeloCantidad);
    }

    // Método para procesar el ID de venta ingresado
    private void procesarIdVenta() {
        // DEBUG
        System.out.println("🔧 DEBUG procesarIdVenta: txtIdVenta.getText() = '" + txtIdVenta.getText() + "'");
        System.out.println("🔧 Llamado desde:");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i <= Math.min(5, stackTrace.length - 1); i++) {
            System.out.println("  " + i + ": " + stackTrace[i].getClassName() + "."
                    + stackTrace[i].getMethodName() + ":" + stackTrace[i].getLineNumber());
        }

        try {
            String idTexto = txtIdVenta.getText().trim();

            if (idTexto.isEmpty()) {
                mostrarError("Ingrese un ID de venta.");
                txtIdVenta.requestFocus();
                return;
            }

            int id = Integer.parseInt(idTexto);
            System.out.println("🔧 ID parseado: " + id);

            if (id == 0) {
                // Llamar al controlador, no a la vista
                controlador.limpiarFormulario();
                mostrarMensaje("Formulario limpiado para nueva venta.");
            } else if (id > 0) {
                // Buscar venta
                System.out.println("🔧 Buscando venta ID: " + id);
                controlador.buscarVentaPorId(id);
            } else {
                mostrarError("El ID debe ser un número positivo o 0 para limpiar.");
                txtIdVenta.selectAll();
                txtIdVenta.requestFocus();
            }

        } catch (NumberFormatException e) {
            mostrarError("El ID debe ser un número válido.");
            txtIdVenta.selectAll();
            txtIdVenta.requestFocus();
        }
    }

    // Método para abrir búsqueda por nombre
    private void abrirBusquedaPorNombre() {
        vSeleccionProductoVentas ventanaBusqueda = new vSeleccionProductoVentas(
                (Frame) SwingUtilities.getWindowAncestor(this),
                controlador
        );
        ventanaBusqueda.setVisible(true);

        // Si se seleccionó un producto, enfocar en código de barras para continuar
        if (ventanaBusqueda.isProductoElegido()) {
            txtCodigoBarra.requestFocus();
        }
    }

    // Método para configurar el menú contextual
    private void configurarMenuContextual() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemEliminar = new JMenuItem("Eliminar Producto");

        // Configurar icono y acción del menú
        menuItemEliminar.addActionListener(e -> eliminarProductoSeleccionado());

        // Agregar el item al menú
        popupMenu.add(menuItemEliminar);

        // Asignar el menú popup a la tabla
        tblDetalles.setComponentPopupMenu(popupMenu);

        // LISTENER PARA ENTER: Detectar Enter después de editar**
        tblDetalles.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    int fila = tblDetalles.getSelectedRow();
                    int columna = tblDetalles.getSelectedColumn();

                    // Si estamos en la columna de cantidad (columna 3)
                    if (columna == 3 && fila >= 0) {
                        // Esperar un poco para que la edición termine
                        SwingUtilities.invokeLater(() -> {
                            actualizarFilaEditada(fila);
                        });
                    }
                }
            }
        });

        // Opcional: Agregar listener para habilitar/deshabilitar según selección
        tblDetalles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    int filaSeleccionada = tblDetalles.rowAtPoint(evt.getPoint());
                    if (filaSeleccionada >= 0) {
                        tblDetalles.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
                        menuItemEliminar.setEnabled(true);
                    } else {
                        menuItemEliminar.setEnabled(false);
                    }
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    int filaSeleccionada = tblDetalles.rowAtPoint(evt.getPoint());
                    if (filaSeleccionada >= 0) {
                        tblDetalles.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
                        menuItemEliminar.setEnabled(true);
                    } else {
                        menuItemEliminar.setEnabled(false);
                    }
                }
            }
        });
    }

    //Método para eliminar producto seleccionado desde menú contextual
    private void eliminarProductoSeleccionado() {
        int filaSeleccionada = tblDetalles.getSelectedRow();

        if (filaSeleccionada < 0) {
            mostrarError("No hay ningún producto seleccionado.");
            return;
        }

        try {
            // Obtener descripción del producto para mostrar en confirmación
            String descripcion = tblDetalles.getValueAt(filaSeleccionada, 2).toString();

            // Confirmar eliminación
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar el producto: " + descripcion + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (opcion == JOptionPane.YES_OPTION) {
                // Eliminar a través del controlador
                controlador.eliminarDetalle(filaSeleccionada);

                // Actualizar título
                actualizarTituloConEstado();

                // Recalcular totales si el método existe
                try {
                    recalcularTotales();
                } catch (Exception e) {
                    // Método puede no estar implementado aún
                }

                mostrarMensaje("Producto eliminado correctamente");
            }

        } catch (Exception e) {
            mostrarError("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Método para actualizar fila editada (recalcular subtotal al cambiar cantidad)
    private void actualizarFilaEditada(int fila) {
        try {
            // Verificar que la fila sea válida
            if (fila < 0 || fila >= tblDetalles.getRowCount()) {
                return;
            }

            // Debug: imprimir información
            System.out.println("Actualizando fila: " + fila);

            // Obtener valores de la fila editada
            Object cantidadObj = tblDetalles.getValueAt(fila, 3);
            Object precioObj = tblDetalles.getValueAt(fila, 4);

            if (cantidadObj == null || precioObj == null) {
                System.out.println("Valores nulos en fila " + fila);
                return;
            }

            int cantidad = Integer.parseInt(cantidadObj.toString());
            int precioUnitario = Integer.parseInt(precioObj.toString());

            // Debug: imprimir valores
            System.out.println("Cantidad: " + cantidad + ", Precio: " + precioUnitario);

            // Calcular nuevo subtotal
            int nuevoSubtotal = cantidad * precioUnitario;

            // Actualizar subtotal en la tabla
            tblDetalles.setValueAt(nuevoSubtotal, fila, 5);

            System.out.println("Nuevo subtotal: " + nuevoSubtotal);

            // Recalcular totales generales
            recalcularTotales();

        } catch (NumberFormatException e) {
            mostrarError("Error: La cantidad debe ser un número válido");
            // Recargar tabla para restaurar valor anterior
            actualizarTablaDetalles();
        } catch (Exception e) {
            mostrarError("Error al actualizar fila: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cargar clientes en el combo
    private void cargarClientes() {
        try {
            comboCliente.removeAllItems();
            List<cRegVentas.ItemCombo> clientes = controlador.obtenerClientes();

            for (cRegVentas.ItemCombo cliente : clientes) {
                int idCliente = (int) cliente.getValor();
                if (idCliente != 0) {
                    comboCliente.addItem(cliente);
                }
            }

            if (comboCliente.getItemCount() > 0) {
                seleccionarClientePorId(2);  // Seleccionar cliente con ID 2
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

            Object[] datosCliente = controlador.obtenerDatosCliente(idCliente);
            if (datosCliente != null) {
                txtNombreCliente.setText((String) datosCliente[0]);
                txtDocumentoCliente.setText((String) datosCliente[1]);
            }
        }
    }

    // Método auxiliar para seleccionar un cliente por su ID
    private void seleccionarClientePorId(int idCliente) {
        try {
            // Buscar el cliente con el ID especificado en el combo
            for (int i = 0; i < comboCliente.getItemCount(); i++) {
                cRegVentas.ItemCombo item = (cRegVentas.ItemCombo) comboCliente.getItemAt(i);
                if ((int) item.getValor() == idCliente) {
                    comboCliente.setSelectedIndex(i);
                    return; // Cliente encontrado y seleccionado
                }
            }

            // Si no encuentra el cliente ID 2, seleccionar el primero como fallback
            if (comboCliente.getItemCount() > 0) {
                comboCliente.setSelectedIndex(0);
            }

        } catch (Exception e) {
            // En caso de error, seleccionar el primero
            if (comboCliente.getItemCount() > 0) {
                comboCliente.setSelectedIndex(0);
            }
            System.err.println("Error al seleccionar cliente por ID: " + e.getMessage());
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

        recalcularTotales();
    }

    // Eliminar detalle seleccionado
    private void eliminarDetalle() {
        eliminarProductoSeleccionado();
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

    // Anular venta (único botón en la interfaz)
    private void anularVenta() {
        controlador.anularVenta();
    }

    // Imprimir factura (llamado desde vPrincipal)
    private void imprimirFactura() {
        try {
            // Llamar al método de impresión del controlador
            controlador.imImprimir();
        } catch (Exception e) {
            mostrarError("Error al imprimir: " + e.getMessage());
        }
    }

    // MÉTODOS PÚBLICOS PARA EL CONTROLADOR
    public void actualizarTablaDetalles() {
        modeloTablaDetalles = controlador.getModeloTablaDetalles();
        tblDetalles.setModel(modeloTablaDetalles);

        configurarColumnasTabla();
        recalcularTotales();
    }

    private void configurarColumnasTabla() {
        // Configurar renderizador para alineación
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tblDetalles.getColumnModel().getColumn(0).setCellRenderer(rightRenderer); // #
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
        tblDetalles.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Subtotal

        // Configurar anchos de columnas
        tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(15);  // #
        tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(100); // Código
        tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(30);  // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(50);  // Precio
        tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(50);  // Subtotal
    }

    private void recalcularTotales() {
        int subtotalTotal = 0;
        int totalIVA = 0;

        // Obtener número de filas en la tabla
        int numFilas = tblDetalles.getRowCount();

        // Recorrer todas las filas de la tabla
        for (int fila = 0; fila < numFilas; fila++) {
            try {
                // Obtener cantidad y precio unitario
                int cantidad = Integer.parseInt(tblDetalles.getValueAt(fila, 3).toString());
                int precioUnitario = Integer.parseInt(tblDetalles.getValueAt(fila, 4).toString());

                // Calcular subtotal de esta fila (cantidad * precio)
                int subtotalFila = cantidad * precioUnitario;

                // Para IVA 10%, usar el método paraguayo: dividir por 11
                int ivaFila = (int) Math.round(subtotalFila / 11.0);

                // Acumular totales
                subtotalTotal += subtotalFila;
                totalIVA += ivaFila;

            } catch (Exception e) {
                // Manejar posibles errores de conversión
                System.err.println("Error al procesar fila " + fila + ": " + e.getMessage());
            }
        }

        // Calcular total final
        int totalFinal = subtotalTotal;

        // Actualizar campos de totales
        if (txtSubtotal != null) {
            txtSubtotal.setText(dfNumeros.format(subtotalTotal));
        }
        if (txtIVA10 != null) {
            txtIVA10.setText(dfNumeros.format(totalIVA));
        }

        // Actualizar el campo total existente
        txtTotal.setText(dfNumeros.format(totalFinal));

        // Actualizar también el total en el controlador si es necesario
        if (controlador != null && controlador.getVentaActual() != null) {
            controlador.getVentaActual().setTotal(totalFinal);
        }
    }

    public void actualizarTotalVenta(int total) {
        recalcularTotales();
    }

    public void cargarDatosVenta(mVentas venta) {
        // DEBUG PARA RASTREAR LLAMADAS AUTOMÁTICAS
        System.out.println("🔍 DEBUG cargarDatosVenta: Llamado desde:");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i <= Math.min(7, stackTrace.length - 1); i++) {
            System.out.println("  " + i + ": " + stackTrace[i].getClassName() + "."
                    + stackTrace[i].getMethodName() + ":" + stackTrace[i].getLineNumber());
        }
        System.out.println("🔍 Venta ID: " + venta.getIdVenta() + ", Factura: " + venta.getNumeroFactura());
        System.out.println("=====================================");

        // Cargar datos básicos
        txtIdVenta.setText(String.valueOf(venta.getIdVenta()));
        txtFecha.setText(sdf.format(venta.getFecha()));
        chkAnulado.setSelected(venta.isAnulado());
        txtObservaciones.setText(venta.getObservaciones() != null ? venta.getObservaciones() : "");

        // Seleccionar cliente
        seleccionarClientePorId(venta.getIdCliente());

        // Actualizar datos del talonario si existen
        if (venta.getNumeroFactura() != null) {
            actualizarNumeroFactura(venta.getNumeroFactura());
            System.out.println("Factura cargada: " + venta.getNumeroFactura());
        }

        // Actualizar número de timbrado si existe
        if (venta.getNumeroTimbrado() != null) {
            actualizarTimbrado(venta.getNumeroTimbrado());
            System.out.println("Timbrado cargado: " + venta.getNumeroTimbrado());
        }

        // Actualizar totales
        txtTotal.setText(dfNumeros.format(venta.getTotal()));

        // Recalcular totales basado en detalles
        recalcularTotales();
    }

    public void limpiarFormulario() {
        System.out.println("DEBUG LIMPIAR: limpiarFormulario() llamado desde:");

        // Debug para ver desde dónde se llama
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i <= Math.min(5, stackTrace.length - 1); i++) {
            System.out.println("  " + i + ": " + stackTrace[i].getClassName() + "."
                    + stackTrace[i].getMethodName() + ":" + stackTrace[i].getLineNumber());
        }

        txtIdVenta.setText("0");
        txtFecha.setText(sdf.format(new Date()));

        // Mantener cliente ocasional seleccionado por defecto
        seleccionarClientePorId(2);

        // Los datos del talonario se cargarán desde el controlador
        txtCodigoBarra.setText("");
        spinnerCantidad.setValue(1);

        // Limpiar campos de totales si existen
        if (txtSubtotal != null) {
            txtSubtotal.setText("0");
        }
        if (txtIVA10 != null) {
            txtIVA10.setText("0");
        }
        txtTotal.setText("0");

        // Limpiar observaciones pero mantener referencia a la mesa si existe
        if (idMesaAsociada > 0) {
            txtObservaciones.setText("Mesa " + numeroMesaAsociada);
        } else {
            txtObservaciones.setText("");
        }

        chkAnulado.setSelected(false);

        // Debug cuando se limpia la tabla
        System.out.println("DEBUG LIMPIAR: Limpiando tabla con " + modeloTablaDetalles.getRowCount() + " filas");
        modeloTablaDetalles.setRowCount(0);
        System.out.println("DEBUG LIMPIAR: Tabla limpiada");

        // Establecer foco
        txtCodigoBarra.requestFocus();
    }

    public void marcarVentaComoAnulada() {
        chkAnulado.setSelected(true);
        btnAnular.setEnabled(false);
        btnAgregarProducto.setEnabled(false);
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
        // AGREGAR LOGS DE DIAGNÓSTICO
        System.out.println("=== DEBUG TICKET ===");
        System.out.println("ID Venta recibido: " + idVenta);
        System.out.println("==================");

        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea ver el ticket de la venta?\nID Venta: " + idVenta, // Mostrar ID en el diálogo
                "Visualizar Ticket",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // VERIFICAR QUE LA VENTA EXISTE EN BD
                if (verificarVentaExiste(idVenta)) {
                    System.out.println("✓ Venta existe en BD");

                    // Crear nueva ventana de reporte para el ticket
                    vista.vReport ventanaReporte = new vista.vReport("ticket_venta", null);

                    // Agregar parámetro del ID de venta
                    java.util.Map<String, Object> parametros = new java.util.HashMap<>();
                    parametros.put("ID_VENTA", idVenta);

                    // DEBUG: Mostrar parámetros
                    System.out.println("Parámetros enviados al reporte:");
                    parametros.forEach((k, v) -> System.out.println("  " + k + " = " + v));

                    // Configurar y mostrar el reporte
                    ventanaReporte.configurarReporteConParametros("ticket_venta", parametros);

                    // Agregar la ventana al desktop
                    if (getDesktopPane() != null) {
                        getDesktopPane().add(ventanaReporte);
                        ventanaReporte.setVisible(true);
                        ventanaReporte.toFront();
                    }
                } else {
                    System.err.println("✗ La venta ID " + idVenta + " no existe en la base de datos");
                    mostrarError("Error: La venta no se encuentra en la base de datos.");
                }

            } catch (Exception e) {
                System.err.println("✗ Error al mostrar ticket: " + e.getMessage());
                e.printStackTrace();
                mostrarError("Error al mostrar ticket: " + e.getMessage());
            }
        }
    }

    // Método auxiliar para verificar si la venta existe
    private boolean verificarVentaExiste(int idVenta) {
        try {
            // Usar el controlador o crear consulta directa
            java.sql.Connection conn = modelo.DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM ventas WHERE id = ?";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idVenta);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Venta " + idVenta + " - registros encontrados: " + count);
                        return count > 0;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar venta: " + e.getMessage());
        }
        return false;
    }

    //Método para finalizar atención de mesa (liberar mesa explícitamente)
    public void finalizarAtencionMesa() {
        if (idMesaAsociada > 0 && ventanaSeleccionMesa != null) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro que desea finalizar la atención de la Mesa " + numeroMesaAsociada
                    + "?\nEsto liberará la mesa para nuevos clientes.",
                    "Finalizar Atención",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.YES_OPTION) {
                ventanaSeleccionMesa.liberarMesa(idMesaAsociada);

                // Limpiar asociación con mesa
                idMesaAsociada = 0;
                numeroMesaAsociada = "";
                setTitle("Registro de Ventas");

                // Cerrar ventana
                this.dispose();
            }
        } else {
            this.dispose();
        }
    }

    //Método auxiliar para obtener información de la mesa asociada
    public String getInfoMesaAsociada() {
        if (idMesaAsociada > 0) {
            return "Mesa " + numeroMesaAsociada + " (ID: " + idMesaAsociada + ")";
        }
        return "Sin mesa asociada";
    }

    //Método para verificar si la mesa actual tiene datos temporales
    public boolean mesaTieneDatosTemporal() {
        return ventanaSeleccionMesa != null
                && idMesaAsociada > 0
                && ventanaSeleccionMesa.mesaTieneDatosTemporal(idMesaAsociada);
    }

    //Actualizar el título con indicador temporal
    private void actualizarTituloConEstado() {
        String titulo = "Registro de Ventas";
        if (idMesaAsociada > 0) {
            titulo += " - Mesa " + numeroMesaAsociada;
            if (tieneProductosEnVenta()) {
                titulo += " (CON PRODUCTOS)";
            }
        }
        setTitle(titulo);
    }

    //Getter para el ID de mesa (útil para otras funcionalidades)
    public int getIdMesaAsociada() {
        return idMesaAsociada;
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
    public JTextField getTxtNumeroFactura() {
        return txtNumeroFactura;
    }

    public JTextField getTxtTimbrado() {
        return txtTimbrado;
    }

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
    @Override
    public void imGrabar() {
        // Validar caja abierta antes de guardar
        if (!controlador.isCajaAbierta()) {
            mostrarError("No se puede guardar la venta. No hay ninguna caja abierta.");
            return;
        }
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
        abrirBusquedaPorNombre();
    }

    @Override
    public void imDelDet() {
        eliminarDetalle();
    }

    @Override
    public void imCerrar() {
        if (tieneProductosEnVenta() && idMesaAsociada > 0) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "La Mesa " + numeroMesaAsociada + " tiene productos agregados.\n\n"
                    + "¿Qué desea hacer?\n\n"
                    + "SÍ: Guardar temporalmente (puede continuar después)\n"
                    + "NO: Descartar cambios y liberar mesa\n"
                    + "CANCELAR: Continuar trabajando",
                    "Mesa con Productos",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            switch (opcion) {
                case JOptionPane.YES_OPTION:
                    // Guardar temporalmente - se hace automático al cerrar
                    this.dispose();
                    break;
                case JOptionPane.NO_OPTION:
                    // Descartar cambios
                    if (JOptionPane.showConfirmDialog(
                            this,
                            "¿Está seguro que desea descartar todos los productos?",
                            "Confirmar Descarte",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                        // Limpiar datos y liberar mesa
                        limpiarFormulario();
                        if (ventanaSeleccionMesa != null && idMesaAsociada > 0) {
                            ventanaSeleccionMesa.liberarMesaDefinitivamente(idMesaAsociada);
                        }
                        this.dispose();
                    }
                    break;
                case JOptionPane.CANCEL_OPTION:
                    // No hacer nada, continuar trabajando
                    break;
            }
        } else {
            // No hay productos, cerrar normalmente
            this.dispose();
        }
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
        lblNumeroFactura = new javax.swing.JLabel();
        txtNumeroFactura = new javax.swing.JTextField();
        lblTimbrado = new javax.swing.JLabel();
        txtTimbrado = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtCodigoBarra = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        spinnerCantidad = new javax.swing.JSpinner();
        btnAgregarProducto = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        btnAnular = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtIVA10 = new javax.swing.JTextField();

        jLabel1.setText("ID Venta:");

        jLabel2.setText("Fecha:");

        chkAnulado.setText("Anulado");

        jLabel3.setText("Cliente:");

        jLabel4.setText("Nro Doc:");

        jLabel5.setText("Nombre:");

        lblNumeroFactura.setText("Factura Nº:");

        lblTimbrado.setText("Timbrado:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDocumentoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboCliente, 0, 220, Short.MAX_VALUE)
                            .addComponent(txtNombreCliente))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(chkAnulado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtIdVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFecha)
                                .addGap(109, 109, 109)))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTimbrado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTimbrado))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblNumeroFactura)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumeroFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(21, 21, 21))
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
                    .addComponent(lblTimbrado)
                    .addComponent(txtTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNumeroFactura)
                            .addComponent(txtNumeroFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkAnulado)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtDocumentoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel6.setText("Codigo de Barras:");

        jLabel7.setText("Cantidad:");

        btnAgregarProducto.setText("Agregar");

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblDetalles);

        jLabel8.setText("TOTAL:");

        jLabel9.setText("OBSERVACIONES:");

        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(5);
        jScrollPane2.setViewportView(txtObservaciones);

        btnAnular.setText("Anular Venta");

        jLabel10.setText("Subtotal:");

        txtSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSubtotalActionPerformed(evt);
            }
        });

        jLabel11.setText("IVA 10%:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(txtIVA10, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAnular)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIVA10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAnular)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 635, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAgregarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAgregarProducto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7)
                        .addComponent(spinnerCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubtotalActionPerformed

    }//GEN-LAST:event_txtSubtotalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarProducto;
    private javax.swing.JButton btnAnular;
    private javax.swing.JCheckBox chkAnulado;
    private javax.swing.JComboBox<cRegVentas.ItemCombo> comboCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JLabel lblNumeroFactura;
    private javax.swing.JLabel lblTimbrado;
    private javax.swing.JSpinner spinnerCantidad;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigoBarra;
    private javax.swing.JTextField txtDocumentoCliente;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIVA10;
    private javax.swing.JTextField txtIdVenta;
    private javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextField txtNumeroFactura;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTimbrado;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
