package vista;

import controlador.cAjusteStock;
import interfaces.myInterface;
import modelo.mAjusteStock;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingUtilities;

public class vAjusteStock extends javax.swing.JInternalFrame implements myInterface {

    private cAjusteStock controlador;
    private DefaultTableModel modeloTablaDetalles;
    private SimpleDateFormat formatoFecha;

    public vAjusteStock() {
        initComponents();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try {
            this.controlador = new cAjusteStock(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar el controlador: " + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            this.controlador = null;

            configurarComponentes();
            configurarTabla();
            configurarEventos();
            limpiarFormulario(); // CAMBIO: Esto ahora establece ID en 0
            mostrarError("Ventana abierta con funcionalidad limitada. Verifique la conexión a la base de datos.");
            return;
        }

        if (this.controlador != null) {
            configurarComponentes();
            configurarTabla();
            configurarEventos();
            actualizarTablaDetalles();
            limpiarFormulario(); // CAMBIO: Esto ahora establece ID en 0
        }
    }

    // Configuración inicial de componentes
    private void configurarComponentes() {
        // Configurar campos de solo lectura
        txtFecha.setEditable(false);
        chkAprobado.setEnabled(false);

        // Configurar texto de observaciones
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setLineWrap(true);

        // Establecer fecha actual
        txtFecha.setText(formatoFecha.format(new Date()));
    }

    // Configuración de la tabla de detalles
    private void configurarTabla() {
        // VALIDACIÓN: Solo proceder si el controlador existe
        if (controlador == null) {
            // Crear modelo vacío por defecto
            modeloTablaDetalles = new DefaultTableModel(
                    new Object[]{"Código Barras", "Descripción", "Cant. Sistema", "Cant. Ajuste", "Diferencia", "Observaciones"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Deshabilitar edición si no hay controlador
                }
            };
            tblDetalles.setModel(modeloTablaDetalles);
            return;
        }

        modeloTablaDetalles = controlador.getModeloTablaDetalles();
        tblDetalles.setModel(modeloTablaDetalles);

        // Configurar ancho de columnas
        tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(120); // Código Barras
        tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(250); // Descripción
        tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(100); // Cant. Sistema
        tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(100); // Cant. Ajuste
        tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(100); // Diferencia
        tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(150); // Observaciones

        // Configurar alineación para columnas numéricas
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblDetalles.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        // Configurar selección
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDetalles.getTableHeader().setReorderingAllowed(false);
    }

    // Configuración de eventos
    private void configurarEventos() {
        // Evento para agregar producto por código de barras
        txtCodigoBarra.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    agregarProductoPorCodigo();
                }
            }
        });

        // Evento para buscar por ID al presionar Enter
        txtIdAjuste.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarPorId();
                }
            }
        });

        // Evento para seleccionar todo el contenido al hacer click en txtIdAjuste
        txtIdAjuste.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtIdAjuste.selectAll();
            }
        });

        // También seleccionar todo al recibir foco
        txtIdAjuste.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    txtIdAjuste.selectAll();
                });
            }
        });

        // Solo configurar editores si el controlador existe
        if (controlador != null) {
            // Resto del código de editores igual...
            JTextField editorCantidad = new JTextField();
            editorCantidad.setHorizontalAlignment(JTextField.RIGHT);

            DefaultCellEditor cellEditorCantidad = new DefaultCellEditor(editorCantidad) {
                @Override
                public boolean stopCellEditing() {
                    try {
                        String valor = (String) getCellEditorValue();
                        int cantidad = Integer.parseInt(valor);
                        int fila = tblDetalles.getEditingRow();

                        if (fila >= 0 && cantidad >= 0) {
                            if (controlador != null) {
                                controlador.actualizarCantidadAjuste(fila, cantidad);
                            }
                            return super.stopCellEditing();
                        } else if (cantidad < 0) {
                            mostrarError("La cantidad no puede ser negativa.");
                            return false;
                        }

                        return super.stopCellEditing();
                    } catch (NumberFormatException e) {
                        mostrarError("Ingrese un número entero válido para la cantidad.");
                        return false;
                    }
                }
            };

            tblDetalles.getColumnModel().getColumn(3).setCellEditor(cellEditorCantidad);

            // Editor para observaciones
            JTextField editorObservaciones = new JTextField();
            DefaultCellEditor cellEditorObservaciones = new DefaultCellEditor(editorObservaciones) {
                @Override
                public boolean stopCellEditing() {
                    String valor = (String) getCellEditorValue();
                    int fila = tblDetalles.getEditingRow();
                    if (fila >= 0 && controlador != null) {
                        controlador.actualizarObservacionesDetalle(fila, valor);
                    }
                    return super.stopCellEditing();
                }
            };

            tblDetalles.getColumnModel().getColumn(5).setCellEditor(cellEditorObservaciones);
        }
    }

    // Limpiar formulario para nuevo ajuste
    public void limpiarFormulario() {
        txtIdAjuste.setText("0");
        txtFecha.setText(formatoFecha.format(new Date()));
        txtObservaciones.setText("");
        chkAprobado.setSelected(false);
        txtCodigoBarra.setText("");

        // Validación de seguridad
        if (modeloTablaDetalles != null) {
            while (modeloTablaDetalles.getRowCount() > 0) {
                modeloTablaDetalles.removeRow(0);
            }
        }

        txtCodigoBarra.requestFocus();
    }

    // Método para resetear ID y seleccionar contenido
    private void resetearIdYSeleccionar() {
        txtIdAjuste.setText("0");
        txtIdAjuste.selectAll();
        txtIdAjuste.requestFocus();
    }

    // Cargar datos de ajuste en el formulario
    public void cargarDatosAjuste(mAjusteStock ajuste) {
        if (ajuste != null) {
            txtIdAjuste.setText(String.valueOf(ajuste.getIdAjuste()));
            txtFecha.setText(formatoFecha.format(ajuste.getFecha()));
            txtObservaciones.setText(ajuste.getObservaciones() != null ? ajuste.getObservaciones() : "");
            chkAprobado.setSelected(ajuste.isAprobado());
        }
    }

    // Actualizar tabla de detalles
    public void actualizarTablaDetalles() {
        // VALIDACIÓN: Solo proceder si el controlador existe
        if (controlador != null) {
            modeloTablaDetalles = controlador.getModeloTablaDetalles();
            tblDetalles.setModel(modeloTablaDetalles);
            configurarTabla(); // Reconfigurar después de cambiar modelo
        }
    }

    // Agregar producto por código de barras
    private void agregarProductoPorCodigo() {
        if (controlador == null) {
            mostrarError("Error: Sistema no disponible. Verifique la conexión a la base de datos.");
            return;
        }

        String codigo = txtCodigoBarra.getText().trim();
        if (!codigo.isEmpty()) {
            controlador.agregarProductoPorCodigo(codigo);
        }
    }

    // Buscar ajuste por ID
    private void buscarPorId() {
        if (controlador == null) {
            mostrarError("Error: Sistema no disponible. Verifique la conexión a la base de datos.");
            return;
        }

        String idTexto = txtIdAjuste.getText().trim();
        if (!idTexto.isEmpty()) {
            try {
                int id = Integer.parseInt(idTexto);

                if (id == 0) {
                    limpiarFormulario();
                    mostrarMensaje("Formulario limpiado para nuevo ajuste.");
                    return;
                }

                controlador.buscarAjustePorId(id);
            } catch (NumberFormatException e) {
                mostrarError("ID debe ser un número válido.");
            }
        }
    }

    // Eliminar detalle seleccionado
    public void eliminarDetalleSeleccionado() {
        if (controlador == null) {
            mostrarError("Error: Sistema no disponible. Verifique la conexión a la base de datos.");
            return;
        }

        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada >= 0) {
            controlador.eliminarDetalleSeleccionado(filaSeleccionada);
        } else {
            mostrarError("Seleccione un producto para eliminar.");
        }
    }

    // Limpiar campo código de barras
    public void limpiarCodigoBarra() {
        txtCodigoBarra.setText("");
        txtCodigoBarra.requestFocus();
    }

    // Obtener observaciones
    public String getObservaciones() {
        return txtObservaciones.getText().trim();
    }

    // Establecer ID de ajuste
    public void setIdAjuste(int id) {
        txtIdAjuste.setText(String.valueOf(id));
    }

    // Métodos para mostrar mensajes
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Método para enfocar y seleccionar ID (útil para llamadas externas)
    public void enfocarId() {
        txtIdAjuste.requestFocus();
        txtIdAjuste.selectAll();
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        if (controlador != null) {
            controlador.imGrabar();

            SwingUtilities.invokeLater(() -> {
                resetearIdYSeleccionar();
            });
        } else {
            mostrarError("Error: Sistema no disponible para guardar.");
        }
    }

    @Override
    public void imFiltrar() {
        if (controlador != null) {
            controlador.imFiltrar();
        } else {
            mostrarError("Error: Sistema no disponible para filtrar.");
        }
    }

    @Override
    public void imActualizar() {
        if (controlador != null) {
            controlador.imActualizar();

            SwingUtilities.invokeLater(() -> {
                resetearIdYSeleccionar();
            });
        } else {
            mostrarError("Error: Sistema no disponible para actualizar.");
        }
    }

    @Override
    public void imBorrar() {
        if (controlador != null) {
            controlador.imBorrar();

            SwingUtilities.invokeLater(() -> {
                resetearIdYSeleccionar();
            });
        } else {
            mostrarError("Error: Sistema no disponible para borrar.");
        }
    }

    @Override
    public void imNuevo() {
        if (controlador != null) {
            controlador.imNuevo();
        } else {
            // Permitir nuevo ajuste incluso sin controlador
            limpiarFormulario();
        }

        SwingUtilities.invokeLater(() -> {
            txtIdAjuste.selectAll();
            txtIdAjuste.requestFocus();
        });
    }

    @Override
    public void imBuscar() {
        if (controlador != null) {
            controlador.imBuscar();
        } else {
            mostrarError("Error: Sistema no disponible para buscar.");
        }
    }

    @Override
    public void imPrimero() {
        if (controlador != null) {
            controlador.imPrimero();
        } else {
            mostrarError("Error: Sistema no disponible para navegación.");
        }
    }

    @Override
    public void imSiguiente() {
        if (controlador != null) {
            controlador.imSiguiente();
        } else {
            mostrarError("Error: Sistema no disponible para navegación.");
        }
    }

    @Override
    public void imAnterior() {
        if (controlador != null) {
            controlador.imAnterior();
        } else {
            mostrarError("Error: Sistema no disponible para navegación.");
        }
    }

    @Override
    public void imUltimo() {
        if (controlador != null) {
            controlador.imUltimo();
        } else {
            mostrarError("Error: Sistema no disponible para navegación.");
        }
    }

    @Override
    public void imImprimir() {
        if (controlador != null) {
            controlador.imImprimir();
        } else {
            mostrarError("Error: Sistema no disponible para imprimir.");
        }
    }

    @Override
    public void imInsDet() {
        if (controlador != null) {
            controlador.imInsDet();
        } else {
            mostrarError("Error: Sistema no disponible para insertar detalles.");
        }
    }

    @Override
    public void imDelDet() {
        if (controlador != null) {
            controlador.imDelDet();
        } else {
            mostrarError("Error: Sistema no disponible para eliminar detalles.");
        }
    }

    @Override
    public void imCerrar() {
        if (controlador != null) {
            controlador.imCerrar();
        } else {
            dispose();
        }
    }

    @Override
    public boolean imAbierto() {
        if (controlador != null) {
            return controlador.imAbierto();
        }
        return this.isVisible();
    }

    @Override
    public void imAbrir() {
        if (controlador != null) {
            controlador.imAbrir();
        } else {
            this.setVisible(true);
        }
    }

    @Override
    public String getTablaActual() {
        if (controlador != null) {
            return controlador.getTablaActual();
        }
        return "ajustes_stock_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        if (controlador != null) {
            return controlador.getCamposBusqueda();
        }
        return new String[]{"id_ajuste", "fecha", "observaciones"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        if (controlador != null) {
            controlador.setRegistroSeleccionado(id);
        } else {
            mostrarError("Error: Sistema no disponible para seleccionar registro.");
        }
    }

    // Métodos para integración con ventana principal
    public void enfocarCodigoBarra() {
        txtCodigoBarra.requestFocus();
        txtCodigoBarra.selectAll();
    }

    public boolean tieneDetalles() {
        return modeloTablaDetalles != null && modeloTablaDetalles.getRowCount() > 0;
    }

    public void mostrarResumenAjuste() {
        if (tieneDetalles()) {
            int productos = modeloTablaDetalles.getRowCount();
            int totalDiferencias = 0;

            for (int i = 0; i < productos; i++) {
                Object valorDif = modeloTablaDetalles.getValueAt(i, 4);
                if (valorDif instanceof Integer) {
                    totalDiferencias += (Integer) valorDif;
                }
            }

            String resumen = String.format(
                    "Productos en ajuste: %d\nTotal diferencias: %d",
                    productos, totalDiferencias
            );

            mostrarMensaje(resumen);
        } else {
            mostrarMensaje("No hay productos en el ajuste actual.");
        }
    }

    // Método para validar antes de cerrar
    public boolean puedeCarriar() {
        if (tieneDetalles()) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "Hay un ajuste en proceso. ¿Desea guardarlo antes de cerrar?",
                    "Confirmar Cierre",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            switch (opcion) {
                case JOptionPane.YES_OPTION:
                    if (controlador != null) {
                        controlador.imGrabar();
                    } else {
                        mostrarError("No se puede guardar: Sistema no disponible.");
                    }
                    return true;
                case JOptionPane.NO_OPTION:
                    return true;
                case JOptionPane.CANCEL_OPTION:
                default:
                    return false;
            }
        }
        return true;
    }

    // Sobrescribir método de cierre
    @Override
    public void dispose() {
        if (puedeCarriar()) {
            super.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtIdAjuste = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        chkAprobado = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        txtCodigoBarra = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();

        jLabel1.setText("ID Ajuste:");

        jLabel2.setText("Fecha:");

        jLabel3.setText("Observaciones:");

        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(5);
        jScrollPane1.setViewportView(txtObservaciones);

        chkAprobado.setText("Aprobado");

        jLabel4.setText("Codigo de Barra:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtIdAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(jLabel2)
                                .addGap(28, 28, 28)
                                .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(chkAprobado))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkAprobado)
                    .addComponent(txtIdAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtCodigoBarra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56))
        );

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblDetalles);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkAprobado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtCodigoBarra;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtIdAjuste;
    private javax.swing.JTextArea txtObservaciones;
    // End of variables declaration//GEN-END:variables
}
