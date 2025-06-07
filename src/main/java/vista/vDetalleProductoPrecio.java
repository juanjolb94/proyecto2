package vista;

import java.awt.Frame;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import modelo.PrecioDetalleDAO;
import modelo.mPrecioDetalle;

public class vDetalleProductoPrecio extends javax.swing.JDialog {

    private boolean aceptado = false;
    private mPrecioDetalle detalle;
    private PrecioDetalleDAO detalleDAO;
    private boolean modoEdicion = false;

    public vDetalleProductoPrecio(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);

        try {
            detalleDAO = new PrecioDetalleDAO();
            detalle = new mPrecioDetalle();

            // Configurar formato para el campo de precio
            configurarCampoPrecio();

            // Establecer fecha actual como predeterminada
            dcFechaVigencia.setDate(new Date());

            // Configurar botones
            btnGuardar.addActionListener(e -> guardar());
            btnCancelar.addActionListener(e -> cancelar());

            // Hacer que Enter ejecute guardar
            getRootPane().setDefaultButton(btnGuardar);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Añadir una variable para almacenar la moneda actual
    private String monedaActual = "PYG"; // Valor por defecto

    /**
     * Configura el formato del campo de precio
     */
    private void configurarCampoPrecio() {
        // Crear formato según moneda
        DecimalFormat formato;
        if ("PYG".equals(monedaActual)) {
            // Para PYG: formato entero
            formato = new DecimalFormat("#,###");
        } else {
            // Para USD, EUR, BRL: formato con decimales
            formato = new DecimalFormat("#,##0.00");
        }

        NumberFormatter formatter = new NumberFormatter(formato);
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setAllowsInvalid(false);

        txtPrecio.setFormatterFactory(new DefaultFormatterFactory(formatter));
        txtPrecio.setValue(0.0);
    }

    /**
     * Configura el diálogo para inserción de nuevo detalle
     *
     * @param idPrecioCabecera ID de la lista de precios
     */
    public void configurarParaInsercion(int idPrecioCabecera) {
        this.modoEdicion = false;
        this.setTitle("Agregar Detalle de Precio");

        // Crear nuevo detalle
        detalle = new mPrecioDetalle();
        detalle.setIdPrecioCabecera(idPrecioCabecera);
        detalle.setFechaVigencia(new Date());
        detalle.setActivo(true);

        // Cargar productos disponibles
        cargarProductos();

        // Habilitar todos los campos
        cboProducto.setEnabled(true);
        txtPrecio.setEnabled(true);
        dcFechaVigencia.setEnabled(true);
        chkActivo.setEnabled(true);

        // Limpiar campos
        txtPrecio.setValue(0.0);
        dcFechaVigencia.setDate(new Date());
        chkActivo.setSelected(true);
    }

    /**
     * Configura el diálogo para edición de un detalle existente
     *
     * @param detalle Detalle a editar
     */
    public void configurarParaEdicion(mPrecioDetalle detalle) {
        this.modoEdicion = true;
        this.detalle = detalle;
        this.setTitle("Editar Detalle de Precio");

        // Cargar productos disponibles
        cargarProductos();

        // Seleccionar el producto
        seleccionarProducto(detalle.getCodigoBarra());

        // Establecer valores
        txtPrecio.setValue(detalle.getPrecio());
        dcFechaVigencia.setDate(detalle.getFechaVigencia());
        chkActivo.setSelected(detalle.isActivo());

        // En modo edición, no permitir cambiar el producto
        cboProducto.setEnabled(false);
    }

    /**
     * Carga la lista de productos en el ComboBox
     */
    private void cargarProductos() {
        try {
            List<Object[]> productos = detalleDAO.obtenerProductosParaSelector();

            // Crear modelo para el combo
            DefaultComboBoxModel<ProductoItem> modelo = new DefaultComboBoxModel<>();

            // Agregar opción por defecto
            modelo.addElement(new ProductoItem("", "-- Seleccione un producto --", "", "", ""));

            // Agregar productos
            for (Object[] producto : productos) {
                String codigoBarra = (String) producto[0];
                String descripcion = (String) producto[1];
                String nombreProducto = (String) producto[2]; // Nuevo campo - nombre del producto cabecera
                String categoria = (String) producto[3];
                String marca = (String) producto[4];

                modelo.addElement(new ProductoItem(codigoBarra, descripcion, nombreProducto, categoria, marca));
            }

            // Establecer modelo
            cboProducto.setModel(modelo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar productos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Establece la moneda actual y actualiza el formato del campo de precio
     *
     * @param moneda Código de moneda (PYG, USD, EUR, BRL)
     */
    public void setMoneda(String moneda) {
        this.monedaActual = moneda;
        configurarCampoPrecio();
    }

    /**
     * Configura el diálogo para inserción de nuevo detalle
     *
     * @param idPrecioCabecera ID de la lista de precios
     * @param moneda Moneda seleccionada en la lista de precios
     */
    public void configurarParaInsercion(int idPrecioCabecera, String moneda) {
        this.modoEdicion = false;
        this.setTitle("Agregar Detalle de Precio");

        // Establecer moneda
        setMoneda(moneda);

        // Crear nuevo detalle
        detalle = new mPrecioDetalle();
        detalle.setIdPrecioCabecera(idPrecioCabecera);
        detalle.setFechaVigencia(new Date());
        detalle.setActivo(true);

        // Cargar productos disponibles
        cargarProductos();

        // Habilitar todos los campos
        cboProducto.setEnabled(true);
        txtPrecio.setEnabled(true);
        dcFechaVigencia.setEnabled(true);
        chkActivo.setEnabled(true);

        // Limpiar campos
        txtPrecio.setValue(0.0);
        dcFechaVigencia.setDate(new Date());
        chkActivo.setSelected(true);
    }

    /**
     * Configura el diálogo para edición de un detalle existente
     *
     * @param detalle Detalle a editar
     * @param moneda Moneda seleccionada en la lista de precios
     */
    public void configurarParaEdicion(mPrecioDetalle detalle, String moneda) {
        this.modoEdicion = true;
        this.detalle = detalle;
        this.setTitle("Editar Detalle de Precio");

        // Establecer moneda
        setMoneda(moneda);

        // Cargar productos disponibles
        cargarProductos();

        // Seleccionar el producto
        seleccionarProducto(detalle.getCodigoBarra());

        // Establecer valores
        txtPrecio.setValue(detalle.getPrecio());
        dcFechaVigencia.setDate(detalle.getFechaVigencia());
        chkActivo.setSelected(detalle.isActivo());

        // En modo edición, no permitir cambiar el producto
        cboProducto.setEnabled(false);
    }

    /**
     * Selecciona un producto en el ComboBox por su código de barras
     *
     * @param codigoBarra Código de barras a seleccionar
     */
    private void seleccionarProducto(String codigoBarra) {
        DefaultComboBoxModel<ProductoItem> modelo = (DefaultComboBoxModel<ProductoItem>) cboProducto.getModel();

        for (int i = 0; i < modelo.getSize(); i++) {
            ProductoItem item = modelo.getElementAt(i);
            if (item.getCodigoBarra().equals(codigoBarra)) {
                cboProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Guarda los cambios y cierra el diálogo
     */
    private void guardar() {
        try {
            // Validar campos
            ProductoItem productoSeleccionado = (ProductoItem) cboProducto.getSelectedItem();

            if (productoSeleccionado == null || productoSeleccionado.getCodigoBarra().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double precio = 0;
            try {
                precio = ((Number) txtPrecio.getValue()).doubleValue();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "El precio debe ser un número válido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (precio <= 0) {
                JOptionPane.showMessageDialog(this,
                        "El precio debe ser mayor a cero",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date fechaVigencia = dcFechaVigencia.getDate();
            if (fechaVigencia == null) {
                JOptionPane.showMessageDialog(this,
                        "La fecha de vigencia es obligatoria",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Actualizar objeto detalle
            detalle.setCodigoBarra(productoSeleccionado.getCodigoBarra());
            detalle.setPrecio(precio);
            detalle.setFechaVigencia(fechaVigencia);
            detalle.setActivo(chkActivo.isSelected());

            // Establecer nombre combinado para referencia (nombre producto + descripción detalle)
            String nombreCompleto = productoSeleccionado.getNombreProducto() + " - " + productoSeleccionado.getDescripcion();
            detalle.setNombreProducto(nombreCompleto);

            // Indicar que se aceptó
            aceptado = true;

            // Cerrar diálogo
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancela la operación y cierra el diálogo
     */
    private void cancelar() {
        aceptado = false;
        dispose();
    }

    /**
     * Indica si la operación fue aceptada
     *
     * @return true si se aceptó, false si se canceló
     */
    public boolean isAceptado() {
        return aceptado;
    }

    /**
     * Obtiene el detalle de precio
     *
     * @return Objeto con los datos del detalle
     */
    public mPrecioDetalle getDetalle() {
        return detalle;
    }

    /**
     * Clase interna para representar un item de producto en el ComboBox
     */
    class ProductoItem {

        private String codigoBarra;
        private String descripcion;
        private String nombreProducto;
        private String categoria;
        private String marca;

        public ProductoItem(String codigoBarra, String descripcion, String nombreProducto, String categoria, String marca) {
            this.codigoBarra = codigoBarra;
            this.descripcion = descripcion;
            this.nombreProducto = nombreProducto;
            this.categoria = categoria;
            this.marca = marca;
        }

        public String getCodigoBarra() {
            return codigoBarra;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getMarca() {
            return marca;
        }

        @Override
        public String toString() {
            if (codigoBarra.isEmpty()) {
                return descripcion;
            }
            // Incluir el nombre del producto en la visualización
            return nombreProducto + " - " + descripcion + " - " + marca + " [" + codigoBarra + "]";
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSlider1 = new javax.swing.JSlider();
        lblProducto = new javax.swing.JLabel();
        lblPrecio = new javax.swing.JLabel();
        lblFechaVigencia = new javax.swing.JLabel();
        chkActivo = new javax.swing.JCheckBox();
        cboProducto = new javax.swing.JComboBox<>();
        txtPrecio = new javax.swing.JFormattedTextField();
        dcFechaVigencia = new com.toedter.calendar.JDateChooser();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblProducto.setText("Producto:");

        lblPrecio.setText("Precio:");

        lblFechaVigencia.setText("Fecha Vigencia:");

        chkActivo.setText("Activo");

        btnGuardar.setText("Guardar");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(btnCancelar)
                        .addGap(54, 54, 54))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblFechaVigencia)
                                    .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblProducto))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dcFechaVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(chkActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(29, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProducto)
                    .addComponent(cboProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPrecio)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dcFechaVigencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaVigencia))
                .addGap(18, 18, 18)
                .addComponent(chkActivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnCancelar))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<ProductoItem> cboProducto;
    private javax.swing.JCheckBox chkActivo;
    private com.toedter.calendar.JDateChooser dcFechaVigencia;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JLabel lblFechaVigencia;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JFormattedTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
