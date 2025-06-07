package vista;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import controlador.cListaPrecios;
import interfaces.myInterface;

public class vListaPrecios extends javax.swing.JInternalFrame implements myInterface {

    private DefaultTableModel modeloTabla;

    // Controlador
    private cListaPrecios controlador;

    public vListaPrecios() {
        super("Gestión de Listas de Precio",
                true, // resizable
                true, // closable
                true, // maximizable
                true); // iconifiable

        initComponents();

        // Añadir borde con título al panel de cabecera
        jPanel1.setBorder(BorderFactory.createTitledBorder("Datos de la Lista de Precios"));

        // Añadir opciones al combo de monedas
        cboMoneda.removeAllItems();
        cboMoneda.addItem("PYG");
        cboMoneda.addItem("USD");
        cboMoneda.addItem("EUR");
        cboMoneda.addItem("BRL");

        // Configurar la tabla de detalles
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) { // Columna "Activo"
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        // Añadir columnas al modelo
        modeloTabla.addColumn("ID");
        modeloTabla.addColumn("Código");
        modeloTabla.addColumn("Producto");
        modeloTabla.addColumn("Precio");
        modeloTabla.addColumn("Vigencia");
        modeloTabla.addColumn("Activo");

        // Asignar el modelo a la tabla
        tblDetalles.setModel(modeloTabla);

        // Configurar anchos de columnas
        tblDetalles.getTableHeader().setReorderingAllowed(false);
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ocultar la columna ID
        if (tblDetalles.getColumnCount() > 0) {
            tblDetalles.getColumnModel().getColumn(0).setMinWidth(0);
            tblDetalles.getColumnModel().getColumn(0).setMaxWidth(0);
            tblDetalles.getColumnModel().getColumn(0).setWidth(0);
        }

        // Añadir borde con título al panel de detalles
        panelDetalles.setBorder(BorderFactory.createTitledBorder("Detalle de Precios"));

        // Añadir evento de doble clic para editar detalles
        tblDetalles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarDetalleSeleccionado();
                }
            }
        });

        // Configurar selección de texto en ID
        txtId.setText("0");
        txtId.setName("0");

        txtId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtId.selectAll();
            }
        });

        txtId.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtId.selectAll();
            }
        });

        // Agregar evento para buscar cuando se presiona Enter
        txtId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    int id;
                    try {
                        id = Integer.parseInt(txtId.getText());
                        controlador.buscarPrecioPorId(id);
                    } catch (NumberFormatException e) {
                        mostrarError("El ID debe ser un número entero válido");
                        txtId.setText("0");
                        txtId.selectAll();
                    }
                }
            }
        });

        // Inicializar controlador
        this.controlador = new cListaPrecios(this);

        // Configurar foco inicial en ID
        SwingUtilities.invokeLater(() -> {
            txtId.requestFocusInWindow();
            txtId.selectAll();
        });
    }

    // Método para editar detalle seleccionado (invocado desde doble clic)
    private void editarDetalleSeleccionado() {
        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada >= 0) {
            controlador.editarDetalle(filaSeleccionada);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un detalle para editar",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método para eliminar detalle seleccionado (invocado desde menú)
    private void eliminarDetalleSeleccionado() {
        int filaSeleccionada = tblDetalles.getSelectedRow();
        if (filaSeleccionada >= 0) {
            controlador.eliminarDetalle(filaSeleccionada);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un detalle para eliminar",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void imGrabar() {
        controlador.imGrabar();
    }

    @Override
    public void imFiltrar() {
        // Implementar si se necesita filtrado especial
    }

    @Override
    public void imActualizar() {
        controlador.imActualizar();
    }

    @Override
    public void imBorrar() {
        controlador.imBorrar();
    }

    @Override
    public void imNuevo() {
        controlador.imNuevo();
    }

    @Override
    public void imBuscar() {
        controlador.imBuscar();
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
        // Implementar lógica de impresión si es necesario
    }

    @Override
    public void imInsDet() {
        controlador.imInsDet();
    }

    @Override
    public void imDelDet() {
        controlador.imDelDet();
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
        return "precio_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "nombre", "moneda"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        controlador.buscarPrecioPorId(id);
    }

    // ============ MÉTODOS PARA EL CONTROLADOR ============
    public void setIdCabecera(int id) {
        txtId.setText(String.valueOf(id));
        txtId.setName(String.valueOf(id));

        SwingUtilities.invokeLater(() -> {
            if (txtId.hasFocus()) {
                txtId.selectAll();
            }
        });
    }

    public void setDatosCabecera(int id, String nombre, Date fecha, String moneda, boolean activo, String observaciones) {
        setIdCabecera(id);
        txtNombre.setText(nombre);
        dcFechaCreacion.setDate(fecha);
        cboMoneda.setSelectedItem(moneda);
        chkActivo.setSelected(activo);
        txtObservaciones.setText(observaciones);
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public void limpiarFormulario() {
        txtId.setText("0");
        txtId.setName("0");
        txtNombre.setText("");
        dcFechaCreacion.setDate(new Date());
        cboMoneda.setSelectedIndex(0);
        chkActivo.setSelected(true);
        txtObservaciones.setText("");
        modeloTabla.setRowCount(0);
    }

    public int getIdCabecera() {
        try {
            return Integer.parseInt(txtId.getName());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getNombreCabecera() {
        return txtNombre.getText();
    }

    public Date getFechaCreacionCabecera() {
        return dcFechaCreacion.getDate();
    }

    public String getMonedaCabecera() {
        return (String) cboMoneda.getSelectedItem();
    }

    public boolean isActivoCabecera() {
        return chkActivo.isSelected();
    }

    public String getObservacionesCabecera() {
        return txtObservaciones.getText();
    }

    public int getFilaSeleccionada() {
        return tblDetalles.getSelectedRow();
    }

    // Método para mostrar mensajes al usuario
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        dcFechaCreacion = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cboMoneda = new javax.swing.JComboBox<>();
        chkActivo = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        panelDetalles = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();

        jLabel1.setText("ID:");

        jLabel2.setText("Fecha:");

        jLabel3.setText("Nombre:");

        jLabel4.setText("Moneda:");

        chkActivo.setText("Activo?");

        jLabel5.setText("Observaciones:");

        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(5);
        jScrollPane1.setViewportView(txtObservaciones);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4))
                        .addGap(54, 54, 54)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtId)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dcFechaCreacion, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cboMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkActivo))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dcFechaCreacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(chkActivo))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
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
        jScrollPane2.setViewportView(tblDetalles);

        javax.swing.GroupLayout panelDetallesLayout = new javax.swing.GroupLayout(panelDetalles);
        panelDetalles.setLayout(panelDetallesLayout);
        panelDetallesLayout.setHorizontalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        panelDetallesLayout.setVerticalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelDetalles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cboMoneda;
    private javax.swing.JCheckBox chkActivo;
    private com.toedter.calendar.JDateChooser dcFechaCreacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelDetalles;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextArea txtObservaciones;
    // End of variables declaration//GEN-END:variables
}
