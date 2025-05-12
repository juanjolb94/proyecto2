package vista;

import controlador.cGestProd;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class vGestionMarcas extends javax.swing.JDialog {
    private cGestProd controlador;
    private DefaultTableModel modeloTabla;
    private boolean cambiosRealizados = false;

    public vGestionMarcas(java.awt.Frame parent, boolean modal, cGestProd controlador) {
        super(parent, modal);
        initComponents();
        this.controlador = controlador;
        
        // Configurar la tabla
        configurarTabla();
        
        // Cargar datos iniciales
        cargarMarcas();
        
        // Centrar la ventana
        setLocationRelativeTo(parent);
        
        // Título de la ventana
        setTitle("Gestión de Marcas");
        
        // Configurar los ActionListeners para los botones
        configurarBotones();
    }

    private void configurarBotones() {
        // Botón Agregar
        btnAgregar.addActionListener(e -> agregarNuevaMarca());
        
        // Botón Editar
        btnEditar.addActionListener(e -> editarMarcaSeleccionada());
        
        // Botón Eliminar
        btnEliminar.addActionListener(e -> eliminarMarcaSeleccionada());
        
        // Botón Cerrar
        btnCerrar.addActionListener(e -> dispose());
        
        // Configurar teclas de acceso rápido
        btnAgregar.setMnemonic(KeyEvent.VK_A);
        btnEditar.setMnemonic(KeyEvent.VK_E);
        btnEliminar.setMnemonic(KeyEvent.VK_L);
        btnCerrar.setMnemonic(KeyEvent.VK_C);
    }
    
    private void configurarTabla() {
        // Crear el modelo de tabla
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Estado"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : Object.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar la columna de estado
                return column == 2;
            }
        };
        
        tblMarcas.setModel(modeloTabla);
        
        // Configurar ancho de columnas
        tblMarcas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblMarcas.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblMarcas.getColumnModel().getColumn(2).setPreferredWidth(80);
        
        // Agregar listener para detectar cambios en la columna de estado
        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 2) {
                actualizarEstadoMarca(e.getFirstRow());
            }
        });
        
        // Configurar doble clic para editar
        tblMarcas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarMarcaSeleccionada();
                }
            }
        });
    }
    
    private void cargarMarcas() {
        try {
            // Limpiar la tabla
            modeloTabla.setRowCount(0);
            
            // Obtener las marcas y agregarlas a la tabla
            List<Object[]> marcas = controlador.obtenerMarcasCompletas();
            for (Object[] marca : marcas) {
                modeloTabla.addRow(new Object[]{
                    marca[0], // ID
                    marca[1], // Nombre
                    "1".equals(marca[2]) // Estado (convertir a boolean)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar marcas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarEstadoMarca(int fila) {
        try {
            int idMarca = (int) modeloTabla.getValueAt(fila, 0);
            boolean nuevoEstado = (boolean) modeloTabla.getValueAt(fila, 2);
            
            // Actualizar en la base de datos
            boolean exito = controlador.actualizarEstadoMarca(idMarca, nuevoEstado);
            
            if (exito) {
                cambiosRealizados = true;
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo actualizar el estado de la marca",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                // Recargar la tabla para restaurar el valor original
                cargarMarcas();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar estado: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void agregarNuevaMarca() {
        // Crear un panel con un campo de texto para el nombre
        JTextField txtNombre = new JTextField();
        Object[] mensaje = {
            "Nombre de la marca:", txtNombre
        };
        
        // Mostrar el diálogo
        int opcion = JOptionPane.showConfirmDialog(this,
                mensaje,
                "Agregar Nueva Marca",
                JOptionPane.OK_CANCEL_OPTION);
        
        // Procesar la respuesta
        if (opcion == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            
            // Validar que no esté vacío
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre de la marca no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verificar si ya existe
                if (controlador.existeMarca(nombre)) {
                    JOptionPane.showMessageDialog(this,
                            "Ya existe una marca con este nombre",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Agregar la marca
                if (controlador.agregarNuevaMarca(nombre)) {
                    JOptionPane.showMessageDialog(this,
                            "Marca agregada correctamente",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    cambiosRealizados = true;
                    // Recargar la tabla
                    cargarMarcas();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al agregar la marca",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error de base de datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editarMarcaSeleccionada() {
        // Verificar si hay una fila seleccionada
        int filaSeleccionada = tblMarcas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una marca para editar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener datos de la fila seleccionada
        int idMarca = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombreActual = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        
        // Crear un panel con un campo de texto para el nombre
        JTextField txtNombre = new JTextField(nombreActual);
        Object[] mensaje = {
            "Nombre de la marca:", txtNombre
        };
        
        // Mostrar el diálogo
        int opcion = JOptionPane.showConfirmDialog(this,
                mensaje,
                "Editar Marca",
                JOptionPane.OK_CANCEL_OPTION);
        
        // Procesar la respuesta
        if (opcion == JOptionPane.OK_OPTION) {
            String nuevoNombre = txtNombre.getText().trim();
            
            // Validar que no esté vacío
            if (nuevoNombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre de la marca no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verificar si ya existe (y no es la misma marca)
                if (!nuevoNombre.equalsIgnoreCase(nombreActual) && controlador.existeMarca(nuevoNombre)) {
                    JOptionPane.showMessageDialog(this,
                            "Ya existe una marca con este nombre",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Actualizar la marca
                if (controlador.actualizarNombreMarca(idMarca, nuevoNombre)) {
                    JOptionPane.showMessageDialog(this,
                            "Marca actualizada correctamente",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    cambiosRealizados = true;
                    // Recargar la tabla
                    cargarMarcas();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al actualizar la marca",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error de base de datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void eliminarMarcaSeleccionada() {
        // Verificar si hay una fila seleccionada
        int filaSeleccionada = tblMarcas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una marca para eliminar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener datos de la fila seleccionada
        int idMarca = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        
        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar la marca \"" + nombre + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Verificar si la marca está en uso por algún producto
                if (controlador.marcaEstaEnUso(idMarca)) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede eliminar la marca porque está siendo utilizada por uno o más productos",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Eliminar la marca
                if (controlador.eliminarMarca(idMarca)) {
                    JOptionPane.showMessageDialog(this,
                            "Marca eliminada correctamente",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                    cambiosRealizados = true;
                    // Recargar la tabla
                    cargarMarcas();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar la marca",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error de base de datos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public boolean seCambiaronMarcas() {
        return cambiosRealizados;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblMarcas = new javax.swing.JTable();
        btnAgregar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblMarcas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        jScrollPane1.setViewportView(tblMarcas);

        btnAgregar.setText("AGREGAR");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnEditar.setText("EDITAR");

        btnEliminar.setText("ELIMINAR");

        btnCerrar.setText("CERRAR");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCerrar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        
    }//GEN-LAST:event_btnAgregarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblMarcas;
    // End of variables declaration//GEN-END:variables
}
