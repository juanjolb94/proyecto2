package vista;

import controlador.cGestProd.ItemCombo;
import controlador.cGestProd;
import interfaces.myInterface;
import java.awt.Point;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class vGestProd extends javax.swing.JInternalFrame implements myInterface {

    private cGestProd controlador;

    public vGestProd() throws SQLException {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setTitle("Gestión de Productos");
        
        configurarSeleccionAutomatica();

        controlador = new cGestProd(this);

        controlador.cargarCategorias(comboCat);
        controlador.cargarMarcas(comboMarca);

        txtProdId.setText("0");

        configurarTablaDetalle();
        configurarEventosTabla();
        configurarMenuContextual();
        configurarFocoInicial();

        txtProdId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarProductoPorId();
                }
            }
        });
    }

    private void configurarFocoInicial() {
        txtProdId.setText("0");
        txtProdNombre.requestFocusInWindow();
        limpiarFormulario();
    }

    private void configurarSeleccionAutomatica() {
        txtProdId.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtProdId.selectAll(); // Seleccionar todo el texto al hacer clic
            }
        });

        // También seleccionar todo al obtener el foco
        txtProdId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtProdId.selectAll();
            }
        });
    }

    private void buscarProductoPorId() {
        try {
            int id = Integer.parseInt(txtProdId.getText());

            if (id == 0) {
                limpiarFormulario();
                return;
            }

            // Llamar al controlador para buscar el producto
            Object[] producto = controlador.buscarProductoPorId(id);

            if (producto != null) {
                // Autocompletar campos del formulario
                txtProdId.setText(producto[0].toString());
                txtProdNombre.setText(producto[1].toString());
                comboCat.setSelectedItem(new ItemCombo<Integer>((Integer) producto[2], producto[3].toString()));
                comboMarca.setSelectedItem(new ItemCombo<Integer>((Integer) producto[4], producto[5].toString()));
                txtIva.setText(producto[6].toString());
                chkActivo.setSelected(producto[7].equals("1"));

                // Cargar detalles en la tabla
                cargarDetallesProducto(id);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró ningún producto con el ID especificado.");
                limpiarFormulario();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            limpiarFormulario();
        }
    }

    private void cargarDetallesProducto(int idProducto) {
        DefaultTableModel modelo = (DefaultTableModel) tblProdDetalle.getModel();
        modelo.setRowCount(0); // Limpiar tabla

        // Obtener detalles del controlador y agregarlos a la tabla
        java.util.List<Object[]> detalles = controlador.obtenerDetallesProducto(idProducto);
        for (Object[] detalle : detalles) {
            modelo.addRow(new Object[]{
                detalle[0], // Código de barras
                detalle[1], // Descripción
                detalle[2], // Presentación
                detalle[3].equals("1") // Estado (convertir a boolean)
            });
        }
    }

    public void limpiarFormulario() {
        txtProdId.setText("0");
        txtProdNombre.setText("");
        comboCat.setSelectedIndex(0);
        comboMarca.setSelectedIndex(0);
        txtIva.setText("10.00");
        chkActivo.setSelected(true);
        ((DefaultTableModel) tblProdDetalle.getModel()).setRowCount(0);
        txtProdNombre.requestFocusInWindow();
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        try {
            int id = Integer.parseInt(txtProdId.getText());
            String nombre = txtProdNombre.getText();
            ItemCombo<Integer> categoria = (ItemCombo<Integer>) comboCat.getSelectedItem();
            ItemCombo<Integer> marca = (ItemCombo<Integer>) comboMarca.getSelectedItem();
            double iva = Double.parseDouble(txtIva.getText());
            boolean activo = chkActivo.isSelected();

            controlador.guardarProducto(id, nombre, categoria.getValor(), marca.getValor(), iva, activo);
            limpiarFormulario();
        } catch (NumberFormatException e) {
            mostrarError("El valor del IVA debe ser numérico");
        }
    }

    @Override
    public void imFiltrar() {
        // Implementar filtrado si es necesario
    }

    @Override
    public void imActualizar() {
        buscarProductoPorId();
    }

    @Override
    public void imBorrar() {
        int id = Integer.parseInt(txtProdId.getText());

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea eliminar este producto?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (controlador.eliminarProducto(id)) {
                mostrarMensaje("Producto eliminado correctamente");
                limpiarFormulario();
            } else {
                mostrarError("No se pudo eliminar el producto");
            }
        }
    }

    @Override
    public void imNuevo() {
        limpiarFormulario();
    }

    @Override
    public void imBuscar() {
        buscarProductoPorId();
    }

    @Override
    public void imPrimero() {
        Object[] producto = controlador.obtenerPrimerProducto();
        if (producto != null) {
            cargarProductoEnFormulario(producto);
        } else {
            mostrarMensaje("No hay productos registrados");
            limpiarFormulario();
        }
    }

    @Override
    public void imSiguiente() {
        int idActual = Integer.parseInt(txtProdId.getText());
        Object[] producto = controlador.obtenerSiguienteProducto(idActual);
        if (producto != null) {
            cargarProductoEnFormulario(producto);
        } else {
            mostrarMensaje("No hay más productos siguientes");
        }
    }

    @Override
    public void imAnterior() {
        int idActual = Integer.parseInt(txtProdId.getText());
        Object[] producto = controlador.obtenerAnteriorProducto(idActual);
        if (producto != null) {
            cargarProductoEnFormulario(producto);
        } else {
            mostrarMensaje("No hay más productos anteriores");
        }
    }

    @Override
    public void imUltimo() {
        Object[] producto = controlador.obtenerUltimoProducto();
        if (producto != null) {
            cargarProductoEnFormulario(producto);
        } else {
            mostrarMensaje("No hay productos registrados");
            limpiarFormulario();
        }
    }

    private void cargarProductoEnFormulario(Object[] producto) {
        txtProdId.setText(producto[0].toString());
        txtProdNombre.setText(producto[1].toString());
        comboCat.setSelectedItem(new ItemCombo<Integer>((Integer) producto[2], producto[3].toString()));
        comboMarca.setSelectedItem(new ItemCombo<Integer>((Integer) producto[4], producto[5].toString()));
        txtIva.setText(producto[6].toString());
        chkActivo.setSelected(producto[7].equals("1"));

        cargarDetallesProducto(Integer.parseInt(producto[0].toString()));
    }

    @Override
    public void imImprimir() {
        // Implementar si es necesario
    }

    @Override
    public void imInsDet() {
        // Verificar que exista un producto seleccionado
        int idProducto = Integer.parseInt(txtProdId.getText());
        if (idProducto <= 0) {
            mostrarError("Debe seleccionar o guardar un producto antes de agregar detalles.");
            return;
        }

        // Crear y mostrar el diálogo de detalle
        vDetalleProducto dialogo = new vDetalleProducto(null, "Agregar Detalle de Producto");
        dialogo.setVisible(true);

        // Verificar si el usuario aceptó el diálogo
        if (dialogo.isAceptado()) {
            try {
                // Obtener datos del diálogo
                String codBarra = dialogo.getCodBarras();
                String descripcion = dialogo.getDescripcion();
                String presentacion = dialogo.getPresentacion();
                boolean estado = dialogo.getEstado();

                // Verificar si ya existe el código de barras
                if (controlador.existeCodBarra(codBarra)) {
                    mostrarError("Ya existe un producto con ese código de barras.");
                    return;
                }

                // Agregar el detalle
                boolean exito = controlador.agregarDetalleProducto(idProducto, codBarra, descripcion, presentacion, estado);

                if (exito) {
                    mostrarMensaje("Detalle agregado correctamente.");
                    // Recargar la tabla de detalles
                    cargarDetallesProducto(idProducto);
                } else {
                    mostrarError("No se pudo agregar el detalle del producto.");
                }
            } catch (Exception e) {
                mostrarError("Error al agregar detalle: " + e.getMessage());
            }
        }
    }

    @Override
    public void imDelDet() {
        // Verificar que haya una fila seleccionada en la tabla
        int filaSeleccionada = tblProdDetalle.getSelectedRow();
        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar un detalle para eliminar.");
            return;
        }

        // Obtener el código de barras de la fila seleccionada
        String codBarra = tblProdDetalle.getValueAt(filaSeleccionada, 0).toString();

        // Pedir confirmación
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea eliminar el detalle con código " + codBarra + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                int idProducto = Integer.parseInt(txtProdId.getText());
                boolean exito = controlador.eliminarDetalleProducto(idProducto, codBarra);

                if (exito) {
                    mostrarMensaje("Detalle eliminado correctamente.");
                    // Actualizar la tabla
                    cargarDetallesProducto(idProducto);
                } else {
                    mostrarError("No se pudo eliminar el detalle del producto.");
                }
            } catch (Exception e) {
                mostrarError("Error al eliminar detalle: " + e.getMessage());
            }
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
        return "productos_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_producto", "nombre"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        txtProdId.setText(String.valueOf(id));
        buscarProductoPorId();
    }

    // Métodos auxiliares
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configurarTablaDetalle() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Código Barras", "Descripción", "Presentación", "Estado"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitimos editar la columna de Estado (columna 3)
                return column == 3;
            }
        };

        tblProdDetalle.setModel(modelo);
        tblProdDetalle.getTableHeader().setReorderingAllowed(false);

        // Agregar listener para detectar cambios en la celda de estado
        tblProdDetalle.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 3) {
                actualizarEstadoDetalle(e.getFirstRow());
            }
        });
    }

    // Método para actualizar el estado de un detalle al modificarlo en la tabla
    private void actualizarEstadoDetalle(int fila) {
        try {
            int idProducto = Integer.parseInt(txtProdId.getText());
            if (idProducto <= 0) {
                return;
            }

            String codBarra = tblProdDetalle.getValueAt(fila, 0).toString();
            boolean nuevoEstado = (boolean) tblProdDetalle.getValueAt(fila, 3);

            // Obtener los valores existentes del detalle
            Object[] detalle = controlador.buscarDetallePorCodBarra(idProducto, codBarra);
            if (detalle == null) {
                return;
            }

            String descripcion = detalle[1].toString();
            String presentacion = detalle[2].toString();

            // Actualizar el detalle con el nuevo estado
            boolean exito = controlador.actualizarDetalleProducto(
                    idProducto, codBarra, descripcion, presentacion, nuevoEstado);

            if (!exito) {
                mostrarError("No se pudo actualizar el estado del detalle");
                // Recargar la tabla para que muestre el estado original
                cargarDetallesProducto(idProducto);
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar el estado: " + e.getMessage());
        }
    }

    public void editarDetalle() {
        // Verificar que haya una fila seleccionada en la tabla
        int filaSeleccionada = tblProdDetalle.getSelectedRow();
        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar un detalle para editar.");
            return;
        }

        try {
            int idProducto = Integer.parseInt(txtProdId.getText());
            if (idProducto <= 0) {
                mostrarError("Debe seleccionar un producto válido.");
                return;
            }

            // Obtener datos de la fila seleccionada
            String codBarra = tblProdDetalle.getValueAt(filaSeleccionada, 0).toString();
            String descripcion = tblProdDetalle.getValueAt(filaSeleccionada, 1).toString();
            String presentacion = tblProdDetalle.getValueAt(filaSeleccionada, 2).toString();
            boolean estado = (boolean) tblProdDetalle.getValueAt(filaSeleccionada, 3);

            // Crear y configurar el diálogo con los datos actuales
            vDetalleProducto dialogo = new vDetalleProducto(null, "Editar Detalle de Producto");
            dialogo.setCodBarras(codBarra);
            dialogo.setDescripcion(descripcion);
            dialogo.setPresentacion(presentacion);
            dialogo.setEstado(estado);

            // Deshabilitar la edición del código de barras porque es la clave primaria
            dialogo.setCodBarrasEditable(false);

            // Mostrar el diálogo
            dialogo.setVisible(true);

            // Si el usuario aceptó los cambios
            if (dialogo.isAceptado()) {
                // Obtener los nuevos valores
                String nuevaDescripcion = dialogo.getDescripcion();
                String nuevaPresentacion = dialogo.getPresentacion();
                boolean nuevoEstado = dialogo.getEstado();

                // Actualizar el detalle
                boolean exito = controlador.actualizarDetalleProducto(
                        idProducto, codBarra, nuevaDescripcion, nuevaPresentacion, nuevoEstado);

                if (exito) {
                    mostrarMensaje("Detalle actualizado correctamente.");
                    // Recargar la tabla
                    cargarDetallesProducto(idProducto);
                } else {
                    mostrarError("No se pudo actualizar el detalle.");
                }
            }
        } catch (Exception e) {
            mostrarError("Error al editar detalle: " + e.getMessage());
        }
    }

    // Método para manejar doble clic en la tabla para editar
    private void configurarEventosTabla() {
        tblProdDetalle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarDetalle();
                }
            }
        });
    }

    private void configurarMenuContextual() {
        JPopupMenu menuContextual = new JPopupMenu();

        JMenuItem menuAgregar = new JMenuItem("Agregar Detalle");
        menuAgregar.addActionListener(e -> imInsDet());

        JMenuItem menuEditar = new JMenuItem("Editar Detalle");
        menuEditar.addActionListener(e -> editarDetalle());

        JMenuItem menuEliminar = new JMenuItem("Eliminar Detalle");
        menuEliminar.addActionListener(e -> imDelDet());

        menuContextual.add(menuAgregar);
        menuContextual.add(menuEditar);
        menuContextual.add(menuEliminar);

        // Asignar el menú contextual a la tabla
        tblProdDetalle.setComponentPopupMenu(menuContextual);

        // Agregar listener para habilitar/deshabilitar opciones según contexto
        menuContextual.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    int fila = tblProdDetalle.rowAtPoint(SwingUtilities.convertPoint(
                            menuContextual, new Point(0, 0), tblProdDetalle));
                    if (fila >= 0) {
                        tblProdDetalle.setRowSelectionInterval(fila, fila);
                        menuEditar.setEnabled(true);
                        menuEliminar.setEnabled(true);
                    } else {
                        // Si no hay fila seleccionada, deshabilitar opciones que requieren selección
                        menuEditar.setEnabled(false);
                        menuEliminar.setEnabled(false);
                    }

                    // Verificar si hay un producto válido seleccionado
                    int idProducto = 0;
                    try {
                        idProducto = Integer.parseInt(txtProdId.getText());
                    } catch (NumberFormatException e) {
                        // Error al parsear, dejar en 0
                    }
                    menuAgregar.setEnabled(idProducto > 0);
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
    }

    // Métodos getter para los componentes
    public JComboBox<ItemCombo<Integer>> getComboCat() {
        return comboCat;
    }

    public JComboBox<ItemCombo<Integer>> getComboMarca() {
        return comboMarca;
    }

    public JTextField getTxtIva() {
        return txtIva;
    }

    public JTextField getTxtProdId() {
        return txtProdId;
    }

    public JTextField getTxtProdNombre() {
        return txtProdNombre;
    }

    public JCheckBox getChkActivo() {
        return chkActivo;
    }

    public JTable getTblProdDetalle() {
        return tblProdDetalle;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProdDetalle = new javax.swing.JTable();
        txtProdId = new javax.swing.JTextField();
        txtProdNombre = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtIva = new javax.swing.JTextField();
        chkActivo = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        comboCat = new javax.swing.JComboBox<>();
        comboMarca = new javax.swing.JComboBox<>();
        btnNuevaMarca = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        tblProdDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "codigo_barra", "descripcion", "presentacion", "estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblProdDetalle);

        jLabel1.setText("id Producto:");

        jLabel2.setText("Nombre:");

        jLabel3.setText("Categoria:");

        jLabel4.setText("Marca:");

        jLabel5.setText("iva:");

        chkActivo.setText("Activo");

        jLabel6.setText("Estado:");

        btnNuevaMarca.setText("+");
        btnNuevaMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaMarcaActionPerformed(evt);
            }
        });

        jLabel8.setText("%");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboCat, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtIva, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8))
                            .addComponent(txtProdId, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkActivo))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addGap(18, 18, 18)
                                    .addComponent(comboMarca, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnNuevaMarca))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtProdNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 34, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtProdId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtProdNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(comboMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevaMarca))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(txtIva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6)
                        .addComponent(jLabel8))
                    .addComponent(chkActivo))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevaMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaMarcaActionPerformed
        controlador.mostrarVentanaGestionMarcas();
    }//GEN-LAST:event_btnNuevaMarcaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNuevaMarca;
    private javax.swing.JCheckBox chkActivo;
    private javax.swing.JComboBox<ItemCombo<Integer>> comboCat;
    private javax.swing.JComboBox<ItemCombo<Integer>> comboMarca;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProdDetalle;
    private javax.swing.JTextField txtIva;
    private javax.swing.JTextField txtProdId;
    private javax.swing.JTextField txtProdNombre;
    // End of variables declaration//GEN-END:variables
}
