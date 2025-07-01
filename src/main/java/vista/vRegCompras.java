package vista;

import controlador.cRegCompras;
import interfaces.myInterface;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.mCompras;

public class vRegCompras extends javax.swing.JInternalFrame implements myInterface {

    private cRegCompras controlador;
    private DefaultTableModel modeloTablaDetalles;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private DecimalFormat dfNumeros;
    private JTextField txtObservaciones;

    public vRegCompras() {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setTitle("Registrar Compra");

        // Inicializar el formateador de números con punto como separador de miles
        dfNumeros = new DecimalFormat("#,##0");
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        dfNumeros.setDecimalFormatSymbols(simbolos);

        // Configurar componentes iniciales
        configurarSeleccionAutomatica();
        configurarTablaDetalles();
        configurarFormateadores();

        // Crear y agregar campo de observaciones
        txtObservaciones = new JTextField();

        try {
            // Inicializar controlador
            controlador = new cRegCompras(this);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar el controlador: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Inicializar valores por defecto
        txtIDCompra.setText("0");
        txtSubtotal.setText("0");
        txtIVA5.setText("0");
        txtIVA10.setText("0");
        txtTotalIVA.setText("0");
        txtTotal.setText("0");

        // Establecer fecha actual
        txtFecha.setText(sdf.format(new Date()));
        txtVencimiento.setText(sdf.format(new Date()));

        // Activar checkbox activo por defecto
        chkActivo.setSelected(true);

        // Cargar proveedores en combobox
        cargarProveedores();

        // Cargar tipos de documentos
        cargarTiposDocumento();

        // Cargar condiciones de compra
        cargarCondicionesCompra();

        // Configurar listeners
        configurarEventos();
    }

    private void configurarSeleccionAutomatica() {
        txtIDCompra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtIDCompra.selectAll();
            }
        });

        txtIDCompra.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtIDCompra.selectAll();
            }
        });
    }

    private void configurarTablaDetalles() {
        // Crear modelo de tabla con columnas modificadas
        modeloTablaDetalles = new DefaultTableModel(
                new Object[]{"#", "Código Barras", "Descripción", "Cantidad", "Precio Unit.", "Gravada", "Impuesto", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar cantidad y precio
                return column == 3 || column == 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // # (Número de ítem)
                    case 3: // Cantidad
                        return Integer.class;
                    case 4: // Precio
                    case 5: // Gravada
                    case 6: // Impuesto
                    case 7: // Subtotal
                        return Integer.class; // Cambiado a Integer en lugar de Double para redondear a 0
                    default:
                        return String.class;
                }
            }
        };

        tblDetalles.setModel(modeloTablaDetalles);
        tblDetalles.getTableHeader().setReorderingAllowed(false);

        // Configurar ancho de columnas
        tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(30);  // # (Número de ítem)
        tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(100); // Código Barras
        tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(70);  // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(80);  // Precio Unit.
        tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(80);  // Gravada
        tblDetalles.getColumnModel().getColumn(6).setPreferredWidth(80);  // Impuesto
        tblDetalles.getColumnModel().getColumn(7).setPreferredWidth(100); // Subtotal

        // Configurar alineación a la derecha para la columna codBarra
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
        tblDetalles.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        // También alinear a la derecha las columnas numéricas
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cantidad
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
        tblDetalles.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Gravada
        tblDetalles.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Impuesto
        tblDetalles.getColumnModel().getColumn(7).setCellRenderer(rightRenderer); // Subtotal

        // Agregar listener para actualizar totales cuando se edita la tabla
        tblDetalles.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                actualizarFilaEditada(e.getFirstRow());
            }
        });
    }

    private void configurarFormateadores() {
        // Configurar formatos para campos numéricos
    }

    private void configurarEventos() {
        // Configurar evento para buscar compra por ID
        txtIDCompra.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarCompraPorId();
                }
            }
        });

        // Agregar listener para manejar doble clic en la tabla para editar un detalle
        tblDetalles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarDetalle();
                }
            }
        });

        // Listener para el cambio de proveedor
        comboProveedor.addActionListener(e -> {
            actualizarDatosProveedor();
        });

        // Listener para el cambio de condición de compra
        comboCondicion.addActionListener(e -> {
            if ("CRÉDITO".equals(comboCondicion.getSelectedItem())) {
                // Establecer vencimiento por defecto a 30 días después de la fecha actual
                try {
                    Date fechaActual = sdf.parse(txtFecha.getText());
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(fechaActual);
                    cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
                    txtVencimiento.setText(sdf.format(cal.getTime()));
                } catch (ParseException ex) {
                    // Si hay error, mantener la fecha actual
                }
            } else {
                // Para contado, establecer vencimiento igual a la fecha de compra
                txtVencimiento.setText(txtFecha.getText());
            }
        });

        // Agregar listener para manejar doble clic en la tabla para editar un detalle
        tblDetalles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarDetalle();
                }
            }
        });

        // Agregar un menú contextual (opcional)
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemEditar = new JMenuItem("Editar");
        JMenuItem menuItemEliminar = new JMenuItem("Eliminar");

        menuItemEditar.addActionListener(e -> editarDetalle());
        menuItemEliminar.addActionListener(e -> imDelDet());

        popupMenu.add(menuItemEditar);
        popupMenu.add(menuItemEliminar);

        tblDetalles.setComponentPopupMenu(popupMenu);
    }

    private void cargarProveedores() {
        try {
            comboProveedor.removeAllItems();

            // Obtener proveedores desde el controlador
            List<cRegCompras.ItemCombo> proveedores = controlador.obtenerProveedores();
            for (cRegCompras.ItemCombo proveedor : proveedores) {
                comboProveedor.addItem(proveedor);
            }

            if (comboProveedor.getItemCount() > 0) {
                comboProveedor.setSelectedIndex(0);
                actualizarDatosProveedor();
            }

        } catch (Exception e) {
            mostrarError("Error al cargar proveedores: " + e.getMessage());
        }
    }

    private void actualizarDatosProveedor() {
        if (comboProveedor.getSelectedItem() != null) {
            cRegCompras.ItemCombo proveedor = (cRegCompras.ItemCombo) comboProveedor.getSelectedItem();
            // Obtener datos del proveedor desde el controlador
            try {
                // Aquí hacemos cast explícito a int
                Object[] datosProveedor = controlador.obtenerDatosProveedor((int) proveedor.getValor());
                if (datosProveedor != null) {
                    txtRUC.setText(datosProveedor[0].toString());
                    txtRazonSocial.setText(datosProveedor[1].toString());
                }
            } catch (Exception e) {
                mostrarError("Error al obtener datos del proveedor: " + e.getMessage());
            }
        }
    }

    private void cargarTiposDocumento() {
        comboTipo.removeAllItems();
        comboTipo.addItem("FACTURA");
        comboTipo.addItem("NOTA DE CRÉDITO");
        comboTipo.addItem("NOTA DE DÉBITO");
        comboTipo.addItem("OTRO");
    }

    private void cargarCondicionesCompra() {
        comboCondicion.removeAllItems();
        comboCondicion.addItem("CONTADO");
        comboCondicion.addItem("CRÉDITO");
    }

    private void buscarCompraPorId() {
        try {
            int id = Integer.parseInt(txtIDCompra.getText());

            if (id == 0) {
                limpiarFormulario();
                return;
            }

            controlador.buscarCompraPorId(id);

        } catch (NumberFormatException e) {
            mostrarError("El ID debe ser un número válido");
            limpiarFormulario();
        }
    }

    private void editarDetalle() {
        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada < 0) {
            mostrarError("Seleccione un detalle para editar");
            return;
        }

        try {
            // Obtener datos del detalle seleccionado
            int idProducto = Integer.parseInt(tblDetalles.getValueAt(filaSeleccionada, 0).toString());
            String codBarra = tblDetalles.getValueAt(filaSeleccionada, 1).toString();
            String descripcion = tblDetalles.getValueAt(filaSeleccionada, 2).toString();
            int cantidad = Integer.parseInt(tblDetalles.getValueAt(filaSeleccionada, 3).toString());
            double precio = Double.parseDouble(tblDetalles.getValueAt(filaSeleccionada, 4).toString());

            // Crear y mostrar el diálogo de edición
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            vEditarDetalleCompra dialogo = new vEditarDetalleCompra(
                    parentFrame, codBarra, descripcion, cantidad, precio, idProducto);
            dialogo.setVisible(true);

            // Si el usuario aceptó los cambios
            if (dialogo.isAceptado()) {
                // Actualizar el detalle en el controlador
                controlador.actualizarDetalle(
                        filaSeleccionada,
                        dialogo.getIdProducto(),
                        dialogo.getCodBarra(),
                        dialogo.getCantidad(),
                        dialogo.getPrecio());

                // Actualizar la vista
                actualizarTablaDetalles();
                recalcularTotales();
            }

        } catch (Exception e) {
            mostrarError("Error al editar detalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarFilaEditada(int fila) {
        try {
            if (fila >= 0 && fila < tblDetalles.getRowCount()) {
                // Obtener valores editados
                int cantidad = Integer.parseInt(tblDetalles.getValueAt(fila, 3).toString());
                double precioUnitario = Double.parseDouble(tblDetalles.getValueAt(fila, 4).toString());

                // Calcular el subtotal
                double subtotalExacto = precioUnitario * cantidad;
                int subtotal = (int) Math.round(subtotalExacto);

                // Para IVA 10%, el método paraguayo: dividir por 11
                int impuesto = (int) Math.round(subtotal / 11.0);

                // Base imponible = subtotal - impuesto
                int baseImponible = subtotal - impuesto;

                // Actualizar en la tabla
                tblDetalles.setValueAt(baseImponible, fila, 5); // Base Imponible
                tblDetalles.setValueAt(impuesto, fila, 6);     // Impuesto
                tblDetalles.setValueAt(subtotal, fila, 7);     // Subtotal

                // Guardar los valores en el controlador
                String codBarra = tblDetalles.getValueAt(fila, 1).toString();
                int idProducto = obtenerIdProductoParaFila(fila);

                mCompras.DetalleCompra detalle = controlador.getCompraActual().getDetalles().get(fila);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setBaseImponible(baseImponible);
                detalle.setImpuesto(impuesto);
                detalle.setSubtotal(subtotal);

                // Recalcular totales
                recalcularTotales();
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar detalle: " + e.getMessage());
        }
    }

    private int obtenerIdProductoParaFila(int fila) {
        return controlador.getCompraActual().getDetalles().get(fila).getIdProducto();
    }

    private void recalcularTotales() {
        int subtotal = 0;
        int totalBaseImponible = 0;
        int totalImpuesto = 0;

        // Obtener número de filas en la tabla
        int numFilas = tblDetalles.getRowCount();

        // Recorrer todas las filas de la tabla
        for (int fila = 0; fila < numFilas; fila++) {
            try {
                // Columna 5 = Base Imponible, Columna 6 = Impuesto, Columna 7 = Subtotal
                int baseImponible = Integer.parseInt(tblDetalles.getValueAt(fila, 5).toString());
                int impuesto = Integer.parseInt(tblDetalles.getValueAt(fila, 6).toString());
                int subtotalFila = Integer.parseInt(tblDetalles.getValueAt(fila, 7).toString());

                // Acumular totales
                totalBaseImponible += baseImponible;
                totalImpuesto += impuesto;
                subtotal += subtotalFila;
            } catch (Exception e) {
                // Manejar posibles errores de conversión
                System.err.println("Error al procesar fila " + fila + ": " + e.getMessage());
            }
        }

        // Actualizar campos de totales
        txtSubtotal.setText(dfNumeros.format(subtotal));
        txtIVA5.setText(dfNumeros.format(0)); // Asumiendo que no hay IVA al 5%
        txtIVA10.setText(dfNumeros.format(totalImpuesto));
        txtTotalIVA.setText(dfNumeros.format(totalImpuesto));
        txtTotal.setText(dfNumeros.format(subtotal));

        // Actualizar también el total en el objeto compraActual
        if (controlador != null && controlador.getCompraActual() != null) {
            controlador.getCompraActual().setTotalCompra(subtotal);
        }
    }

    public void cargarDatosCompra(mCompras compra) {
        txtIDCompra.setText(String.valueOf(compra.getIdCompra()));

        // Fecha
        txtFecha.setText(sdf.format(compra.getFechaCompra()));

        // Estado
        chkActivo.setSelected(compra.isEstado());

        // Proveedor
        for (int i = 0; i < comboProveedor.getItemCount(); i++) {
            cRegCompras.ItemCombo item = (cRegCompras.ItemCombo) comboProveedor.getItemAt(i);
            if ((int) item.getValor() == compra.getIdProveedor()) {
                comboProveedor.setSelectedIndex(i);
                break;
            }
        }

        // Número factura, timbrado, etc.
        if (compra.getNumeroFactura() != null) {
            String[] partes = compra.getNumeroFactura().split("-");
            if (partes.length >= 2) {
                txtNumero.setText(partes[1]);
                txtTimbrado.setText(partes[0]);
            } else {
                txtNumero.setText(compra.getNumeroFactura());
            }
        }

        // Información adicional
        txtObservaciones.setText(compra.getObservaciones());

        // Actualizar tabla de detalles
        actualizarTablaDetalles();

        // Actualizar totales
        actualizarTotalCompra(compra.getTotalCompra());
    }

    public void actualizarTablaDetalles() {
        System.out.println("DEBUG TABLA: Iniciando actualizarTablaDetalles()");

        try {
            // Obtener los detalles desde el controlador y mostrarlos en la tabla
            DefaultTableModel modelo = new DefaultTableModel(
                    new Object[]{"#", "Código Barras", "Descripción", "Cantidad", "Precio Unit.", "Base Imp.", "Impuesto", "Subtotal"}, 0);

            List<mCompras.DetalleCompra> detalles = controlador.getCompraActual().getDetalles();
            System.out.println("DEBUG TABLA: Detalles obtenidos del controlador: " + detalles.size());

            for (int i = 0; i < detalles.size(); i++) {
                mCompras.DetalleCompra detalle = detalles.get(i);

                // Obtener valores calculados del detalle
                int cantidad = detalle.getCantidad();
                int precioUnitarioRedondeado = detalle.getPrecioUnitarioRedondeado();
                int baseImponible = detalle.getBaseImponible();
                int impuesto = detalle.getImpuesto();
                int subtotal = detalle.getSubtotalRedondeado();

                // Agregamos los valores a la tabla, usando i+1 como número de ítem
                modelo.addRow(new Object[]{
                    i + 1, // Número de ítem (enumeración)
                    detalle.getCodBarra(),
                    obtenerDescripcionProducto(detalle.getIdProducto(), detalle.getCodBarra()),
                    cantidad,
                    precioUnitarioRedondeado,
                    baseImponible,
                    impuesto,
                    subtotal
                });
            }

            modeloTablaDetalles = modelo;
            tblDetalles.setModel(modelo);

            // Configurar alineación y ancho de columnas
            javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

            // Alineación a la derecha para columnas numéricas
            tblDetalles.getColumnModel().getColumn(1).setCellRenderer(rightRenderer); // Código Barras
            tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cantidad
            tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
            tblDetalles.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Base Imponible
            tblDetalles.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Impuesto
            tblDetalles.getColumnModel().getColumn(7).setCellRenderer(rightRenderer); // Subtotal

            // Ajustar ancho de columnas
            tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(30);  // # (Número de ítem)
            tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(100); // Código Barras
            tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
            tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(70);  // Cantidad
            tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(80);  // Precio Unit.
            tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(80);  // Base Imponible
            tblDetalles.getColumnModel().getColumn(6).setPreferredWidth(80);  // Impuesto
            tblDetalles.getColumnModel().getColumn(7).setPreferredWidth(100); // Subtotal

            recalcularTotales();
        } catch (Exception e) {
            System.out.println("DEBUG TABLA: Error en actualizarTablaDetalles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para obtener la descripción de un producto
    private String obtenerDescripcionProducto(int idProducto, String codBarra) {
        System.out.println("DEBUG DESC: Obteniendo descripción para ID: " + idProducto + ", CodBarra: " + codBarra);
        try {
            Object[] producto = controlador.buscarProductoPorCodBarra(codBarra);
            if (producto != null) {
                String descripcion = producto[1] + " - " + producto[3];
                System.out.println("DEBUG DESC: Descripción encontrada: " + descripcion);
                return descripcion;
            } else {
                System.out.println("DEBUG DESC: Producto no encontrado");
            }
        } catch (Exception e) {
            System.out.println("DEBUG DESC: Error al obtener descripción: " + e.getMessage());
        }
        return "Producto no encontrado";
    }

    public void actualizarTotalCompra(double total) {
        // Actualizar campos de totales
        txtSubtotal.setText(dfNumeros.format(total));

        // Calcular los IVA (ejemplo simplificado)
        double iva10 = total * 0.10;
        double iva5 = 0; // Para este ejemplo, asumimos que todo es IVA 10%

        txtIVA10.setText(dfNumeros.format(iva10));
        txtIVA5.setText(dfNumeros.format(iva5));
        txtTotalIVA.setText(dfNumeros.format(iva10 + iva5));
        txtTotal.setText(dfNumeros.format(total));
    }

    // Método para calcular base imponible e impuesto según el subtotal
    private void calcularBaseImponibleEImpuesto(int fila) {
        try {
            if (fila >= 0 && fila < tblDetalles.getRowCount()) {
                // Obtener valores
                int cantidad = Integer.parseInt(tblDetalles.getValueAt(fila, 3).toString());
                double precioUnitario = Double.parseDouble(tblDetalles.getValueAt(fila, 4).toString());

                // Calcular el subtotal
                double subtotalExacto = precioUnitario * cantidad;
                int subtotal = (int) Math.round(subtotalExacto);

                // Para IVA 10%, el método paraguayo: dividir por 11
                int impuesto = (int) Math.round(subtotal / 11.0);

                // Base imponible = subtotal - impuesto
                int baseImponible = subtotal - impuesto;

                // Actualizar en la tabla
                tblDetalles.setValueAt(baseImponible, fila, 5); // Base Imponible
                tblDetalles.setValueAt(impuesto, fila, 6);     // Impuesto
                tblDetalles.setValueAt(subtotal, fila, 7);     // Subtotal
            }
        } catch (Exception e) {
            mostrarError("Error al calcular base imponible e impuesto: " + e.getMessage());
        }
    }

    public void mostrarDialogoAgregarDetalle() {
        try {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            vSeleccionProductoCompras dialogo = new vSeleccionProductoCompras(parentFrame, controlador);
            dialogo.setVisible(true);

            if (dialogo.isAceptado()) {
                int idProducto = dialogo.getIdProductoSeleccionado();
                String codBarra = dialogo.getCodBarraSeleccionado();
                int cantidad = dialogo.getCantidad();
                double valorMonetario = dialogo.getPrecio();

                // Decidir qué método usar según el modo de entrada utilizado
                if (dialogo.isSubtotalDirectamenteIngresado()) {
                    controlador.agregarDetalleConSubtotal(idProducto, codBarra, cantidad, valorMonetario);
                } else {
                    controlador.agregarDetalle(idProducto, codBarra, cantidad, valorMonetario);
                }

                actualizarTablaDetalles();
                recalcularTotales();
            } else {
                System.out.println("DEBUG: Diálogo cancelado - no se agregó nada");
            }

            dialogo.dispose();
        } catch (Exception e) {
            mostrarError("Error al mostrar diálogo de selección: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getFilaSeleccionada() {
        return tblDetalles.getSelectedRow();
    }

    public void actualizarEstadoCompra(boolean estado) {
        chkActivo.setSelected(estado);
    }

    public void limpiarFormulario() {
        txtIDCompra.setText("0");
        txtFecha.setText(sdf.format(new Date()));
        chkActivo.setSelected(true);

        if (comboProveedor.getItemCount() > 0) {
            comboProveedor.setSelectedIndex(0);
        }
        txtRUC.setText("");
        txtRazonSocial.setText("");

        comboTipo.setSelectedIndex(0);
        txtNumero.setText("");
        txtTimbrado.setText("");

        comboCondicion.setSelectedIndex(0);
        txtVencimiento.setText(sdf.format(new Date()));

        // Limpiar observaciones
        txtObservaciones.setText("");

        // Limpiar tabla de detalles
        modeloTablaDetalles.setRowCount(0);

        // Limpiar totales
        txtSubtotal.setText("0");
        txtIVA5.setText("0");
        txtIVA10.setText("0");
        txtTotalIVA.setText("0");
        txtTotal.setText("0");

        // Notificar al controlador
        controlador.imNuevo();
    }

    private boolean validarDatos() {
        // Validar campos obligatorios
        if (comboProveedor.getSelectedIndex() == -1) {
            System.out.println("DEBUG VALIDAR: FALLO - No hay proveedor seleccionado");
            mostrarError("Debe seleccionar un proveedor");
            comboProveedor.requestFocus();
            return false;
        }
        System.out.println("DEBUG VALIDAR: Proveedor OK");

        if (txtNumero.getText().trim().isEmpty()) {
            System.out.println("DEBUG VALIDAR: FALLO - Número de comprobante vacío");
            mostrarError("Debe ingresar un número de comprobante");
            txtNumero.requestFocus();
            return false;
        }
        System.out.println("DEBUG VALIDAR: Número de comprobante OK");

        // Validar fecha
        try {
            sdf.parse(txtFecha.getText());
            System.out.println("DEBUG VALIDAR: Fecha OK");
        } catch (ParseException e) {
            System.out.println("DEBUG VALIDAR: FALLO - Formato de fecha inválido: " + txtFecha.getText());
            mostrarError("Formato de fecha inválido. Use dd/mm/yyyy");
            txtFecha.requestFocus();
            return false;
        }

        // Validar que haya detalles EN LA TABLA
        if (modeloTablaDetalles.getRowCount() == 0) {
            System.out.println("DEBUG VALIDAR: FALLO - No hay detalles en la tabla. Filas: " + modeloTablaDetalles.getRowCount());
            mostrarError("Debe agregar al menos un detalle a la compra");
            return false;
        }
        System.out.println("DEBUG VALIDAR: Detalles en tabla OK. Filas: " + modeloTablaDetalles.getRowCount());

        System.out.println("DEBUG VALIDAR: Todas las validaciones pasaron correctamente");
        return true;
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        try {
            // Validar datos antes de guardar
            if (!validarDatos()) {
                return;
            }

            // Obtener datos del formulario
            Date fecha = null;
            try {
                fecha = sdf.parse(txtFecha.getText());
            } catch (Exception e) {
                System.out.println("DEBUG IMGRABAR: Error al parsear fecha: " + e.getMessage());
                mostrarError("Formato de fecha inválido. Use dd/mm/yyyy");
                return;
            }

            // Establecer fecha en controlador
            controlador.setFechaCompra(fecha);

            // Establecer proveedor
            cRegCompras.ItemCombo proveedor = (cRegCompras.ItemCombo) comboProveedor.getSelectedItem();
            if (proveedor != null) {
                // Hacemos cast explícito a int
                controlador.setProveedor((int) proveedor.getValor());
            } else {
                System.out.println("DEBUG IMGRABAR: WARNING - No hay proveedor seleccionado");
            }

            // Establecer tipo de documento
            String tipoDoc = comboTipo.getSelectedItem().toString();
            controlador.setTipoDocumento(tipoDoc);

            // Establecer timbrado
            String timbrado = txtTimbrado.getText().trim();
            controlador.setTimbrado(timbrado);
            // ============================================

            // Establecer número de factura
            String numero = txtNumero.getText().trim();
            // Formatear número de factura (timbrado-numero)
            String numeroFactura = timbrado + "-" + numero;
            controlador.setNumeroFactura(numeroFactura);

            // Establecer observaciones
            controlador.setObservaciones(txtObservaciones.getText());

            // Guardar la compra
            controlador.guardarCompra();

        } catch (Exception e) {
            System.out.println("DEBUG IMGRABAR: Excepción capturada en imGrabar(): " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @Override
    public void imFiltrar() {
        // No implementado para esta pantalla
    }

    @Override
    public void imActualizar() {
        buscarCompraPorId();
    }

    @Override
    public void imBorrar() {
        try {
            int id = Integer.parseInt(txtIDCompra.getText());
            if (id <= 0) {
                mostrarError("Seleccione una compra para anular");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea anular esta compra?",
                    "Confirmar anulación",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                controlador.imBorrar();
            }
        } catch (Exception e) {
            mostrarError("Error al anular: " + e.getMessage());
        }
    }

    @Override
    public void imNuevo() {
        limpiarFormulario();
    }

    @Override
    public void imBuscar() {
        buscarCompraPorId();
    }

    @Override
    public void imPrimero() {
        controlador.imPrimero();
    }

    @Override
    public void imSiguiente() {
        controlador.imSiguiente();
    }

    @Override
    public void imAnterior() {
        controlador.imAnterior();
    }

    @Override
    public void imUltimo() {
        controlador.imUltimo();
    }

    @Override
    public void imImprimir() {
        controlador.imImprimir();
    }

    @Override
    public void imInsDet() {
        mostrarDialogoAgregarDetalle();
    }

    @Override
    public void imDelDet() {
        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada < 0) {
            mostrarError("Debe seleccionar un detalle para eliminar");
            return;
        }

        try {
            // Obtener datos del detalle para mostrar en el mensaje de confirmación
            String descripcion = tblDetalles.getValueAt(filaSeleccionada, 2).toString();

            // Confirmar eliminación
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar el detalle: " + descripcion + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (opcion == JOptionPane.YES_OPTION) {
                // Eliminar el detalle a través del controlador
                controlador.eliminarDetalle(filaSeleccionada);

                // Actualizar la vista
                actualizarTablaDetalles();
                recalcularTotales();

                mostrarMensaje("Detalle eliminado correctamente");
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar detalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void imCerrar() {
        this.dispose();
    }

    @Override
    public boolean imAbierto() {
        return this.isVisible();
    }

    @Override
    public void imAbrir() {
        this.setVisible(true);
    }

    @Override
    public String getTablaActual() {
        return "compras_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_compra", "numero_factura"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        txtIDCompra.setText(String.valueOf(id));
        buscarCompraPorId();
    }

    // Métodos auxiliares para mostrar mensajes
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtIDCompra = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        chkActivo = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        comboProveedor = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtRUC = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtRazonSocial = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        comboTipo = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtTimbrado = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        comboCondicion = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        txtVencimiento = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtIVA10 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtIVA5 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtTotalIVA = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();

        txtIDCompra.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel1.setText("id Compra:");

        jLabel2.setText("Fecha:");

        jLabel3.setText("Estado:");

        chkActivo.setText("Activo si/no");

        jLabel5.setText("Proveedor:");

        jLabel6.setText("RUC:");

        txtRUC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel7.setText("Razón Social:");

        jLabel8.setText("Tipo:");

        comboTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setText("Nro:");

        jLabel10.setText("Timbrado:");

        jLabel11.setText("Condicion:");

        comboCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel12.setText("Vencimiento:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(15, 15, 15)
                            .addComponent(txtIDCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(chkActivo)
                            .addGap(77, 77, 77))
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel11))
                                .addGap(17, 17, 17)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(comboCondicion, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(comboTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtRazonSocial, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(comboProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtRUC, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(86, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkActivo, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtIDCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(txtRUC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRazonSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(comboTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(txtTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboCondicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(txtVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel13.setText("Subtotal:");

        txtSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel14.setText("IVA 5%:");

        txtIVA10.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel15.setText("IVA 10%:");

        txtIVA5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel16.setText("Total IVA:");

        txtTotalIVA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("TOTAL:");

        txtTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtIVA5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtTotalIVA, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel17)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIVA10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtIVA10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(txtIVA5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(txtTotalIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Método para seleccionar un proveedor por ID en el combobox
    private void seleccionarProveedorPorId(int id) {
        try {
            // Recorrer todos los items del combobox
            for (int i = 0; i < comboProveedor.getItemCount(); i++) {
                cRegCompras.ItemCombo item = (cRegCompras.ItemCombo) comboProveedor.getItemAt(i);
                if ((int) item.getValor() == id) {
                    // Seleccionar el proveedor en el combobox
                    comboProveedor.setSelectedIndex(i);
                    // Cargar los datos del proveedor
                    actualizarDatosProveedor();
                    return;
                }
            }

            // Si no se encontró el proveedor en el combo (puede que no esté cargado)
            // obtener los datos directamente de la base de datos
            Object[] datosProveedor = controlador.obtenerDatosProveedor(id);
            if (datosProveedor != null) {
                // Actualizar campos
                txtRUC.setText(datosProveedor[0].toString());
                txtRazonSocial.setText(datosProveedor[1].toString());

                // También podríamos actualizar el combo cargando de nuevo los proveedores
                // y seleccionando el correcto, pero dejamos que se actualice en la siguiente carga
                // Guardar el ID del proveedor en el controlador
                controlador.setProveedor(id);
            }

        } catch (Exception e) {
            mostrarError("Error al seleccionar proveedor: " + e.getMessage());
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkActivo;
    private javax.swing.JComboBox<String> comboCondicion;
    private javax.swing.JComboBox<cRegCompras.ItemCombo> comboProveedor;
    private javax.swing.JComboBox<String> comboTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIDCompra;
    private javax.swing.JTextField txtIVA10;
    private javax.swing.JTextField txtIVA5;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtRUC;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTimbrado;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotalIVA;
    private javax.swing.JTextField txtVencimiento;
    // End of variables declaration//GEN-END:variables
}
