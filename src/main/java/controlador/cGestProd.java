package controlador;

import modelo.ProductosDAO;
import vista.vGestProd;
import interfaces.myInterface;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import vista.vGestionMarcas;

public class cGestProd implements myInterface {

    private vGestProd vista;
    private ProductosDAO modelo;

    public cGestProd(vGestProd vista) throws SQLException {
        this.vista = vista;
        this.modelo = new ProductosDAO();
        configurarEventos();
        cargarComboboxIniciales();
    }

    public void configurarEventos() {
        vista.getTxtProdId().addActionListener(e -> {
            String idText = vista.getTxtProdId().getText().trim();
            if (idText.matches("\\d+")) { // Verifica que sean solo dígitos
                buscarProductoPorId(Integer.parseInt(idText));
            } else {
                vista.mostrarError("El ID debe ser un número entero positivo");
            }
        });
    }

    private void cargarComboboxIniciales() {
        cargarCategorias(vista.getComboCat());
        cargarMarcas(vista.getComboMarca());
    }

    // Método para buscar un producto por ID
    public Object[] buscarProductoPorId(int id) {
        try {
            return modelo.buscarProductoPorId(id);
        } catch (SQLException e) {
            mostrarError("Error al buscar el producto: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el primer producto
    public Object[] obtenerPrimerProducto() {
        try {
            return modelo.obtenerPrimerProducto();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer producto: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el producto anterior
    public Object[] obtenerAnteriorProducto(int idActual) {
        try {
            return modelo.obtenerAnteriorProducto(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el producto anterior: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el siguiente producto
    public Object[] obtenerSiguienteProducto(int idActual) {
        try {
            return modelo.obtenerSiguienteProducto(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente producto: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el último producto
    public Object[] obtenerUltimoProducto() {
        try {
            return modelo.obtenerUltimoProducto();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último producto: " + e.getMessage());
            return null;
        }
    }

    // Método para eliminar un producto
    public boolean eliminarProducto(int id) {
        try {
            return modelo.eliminarProducto(id);
        } catch (SQLException e) {
            // Mostrar mensaje de error específico de la validación
            mostrarError(e.getMessage());
            return false;
        }
    }

    // Método para guardar/actualizar un producto
    public void guardarProducto(int id, String nombre, int idCategoria, int idMarca, double iva, boolean activo) {
        try {
            if (id == 0) {
                // Insertar un nuevo producto
                modelo.insertarProducto(nombre, idCategoria, idMarca, iva, activo);
                mostrarMensaje("Producto guardado correctamente");
            } else {
                // Actualizar un producto existente
                modelo.actualizarProducto(id, nombre, idCategoria, idMarca, iva, activo);
                mostrarMensaje("Producto actualizado correctamente");
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar el producto: " + e.getMessage());
        }
    }

    // Método para obtener los detalles de un producto
    public List<Object[]> obtenerDetallesProducto(int idProducto) {
        try {
            return modelo.obtenerDetallesProducto(idProducto);
        } catch (SQLException e) {
            mostrarError("Error al obtener los detalles del producto: " + e.getMessage());
            return null;
        }
    }

    // Método para cargar categorías en un combobox
    public void cargarCategorias(JComboBox<ItemCombo<Integer>> combo) {
        try {
            combo.removeAllItems();

            List<ItemCombo<Integer>> categorias = modelo.obtenerCategorias();
            for (ItemCombo<Integer> categoria : categorias) {
                combo.addItem(categoria);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar categorías: " + e.getMessage());
        }
    }

    // Método para cargar marcas en un combobox
    public void cargarMarcas(JComboBox<ItemCombo<Integer>> combo) {
        try {
            combo.removeAllItems();

            List<ItemCombo<Integer>> marcas = modelo.obtenerMarcas();
            for (ItemCombo<Integer> marca : marcas) {
                combo.addItem(marca);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar marcas: " + e.getMessage());
        }
    }

    // Método para verificar si ya existe una marca con el mismo nombre
    public boolean existeMarca(String nombre) throws SQLException {
        return modelo.existeMarca(nombre);
    }

    // Método para agregar una nueva marca
    public boolean agregarNuevaMarca(String nombre) throws SQLException {
        boolean resultado = modelo.insertarMarca(nombre);

        // Si se agregó correctamente, actualizar el combo de marcas
        if (resultado) {
            cargarMarcas(vista.getComboMarca());
        }

        return resultado;
    }

    // Métodos auxiliares para mostrar mensajes
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void imGrabar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imFiltrar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imActualizar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imBorrar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imNuevo() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imBuscar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imPrimero() {
        Object[] producto = obtenerPrimerProducto();
        if (producto != null) {
            cargarProductoEnVista(producto);
        }
    }

    @Override
    public void imSiguiente() {
        int idActual = Integer.parseInt(vista.getTxtProdId().getText());
        Object[] producto = obtenerSiguienteProducto(idActual);
        if (producto != null) {
            cargarProductoEnVista(producto);
        }
    }

    @Override
    public void imAnterior() {
        int idActual = Integer.parseInt(vista.getTxtProdId().getText());
        Object[] producto = obtenerAnteriorProducto(idActual);
        if (producto != null) {
            cargarProductoEnVista(producto);
        }
    }

    @Override
    public void imUltimo() {
        Object[] producto = obtenerUltimoProducto();
        if (producto != null) {
            cargarProductoEnVista(producto);
        }
    }

    @Override
    public void imImprimir() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imInsDet() {
        vista.imInsDet();
    }

    @Override
    public void imDelDet() {
        vista.imDelDet();
    }

    @Override
    public void imCerrar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean imAbierto() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void imAbrir() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getTablaActual() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String[] getCamposBusqueda() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void cargarProductoEnVista(Object[] producto) {
        try {
            // Cargar datos básicos
            vista.getTxtProdId().setText(producto[0].toString());
            vista.getTxtProdNombre().setText(producto[1].toString());
            vista.getComboCat().setSelectedItem(new ItemCombo<>((Integer) producto[2], producto[3].toString()));
            vista.getComboMarca().setSelectedItem(new ItemCombo<>((Integer) producto[4], producto[5].toString()));
            vista.getTxtIva().setText(String.format("%.2f", Double.parseDouble(producto[6].toString())));

            // Cargar estado activo
            vista.getChkActivo().setSelected("1".equals(producto[7].toString()));

            // Cargar detalles en la tabla
            List<Object[]> detalles = obtenerDetallesProducto((Integer) producto[0]);
            DefaultTableModel modelo = (DefaultTableModel) vista.getTblProdDetalle().getModel();
            modelo.setRowCount(0); // Limpiar tabla

            if (detalles != null) {
                for (Object[] detalle : detalles) {
                    modelo.addRow(new Object[]{
                        detalle[0], // Código de barras
                        detalle[1], // Descripción
                        detalle[2], // Presentación
                        "1".equals(detalle[3]) // Estado (convertido a boolean)
                    });
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al cargar datos del producto: " + e.getMessage());
        }
    }

    // Clase interna para manejar items en los combobox
    public static class ItemCombo<T> {

        private T valor;
        private String texto;

        public ItemCombo(T valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }

        public T getValor() {
            return valor;
        }

        @Override
        public String toString() {
            return texto;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ItemCombo) {
                ItemCombo<?> otro = (ItemCombo<?>) obj;
                return this.valor.equals(otro.valor);
            }
            return false;
        }
    }

    // Método para agregar detalle de producto
    public boolean agregarDetalleProducto(int idProducto, String codBarra, String descripcion,
            String presentacion, boolean estado) {
        try {
            return modelo.insertarDetalleProducto(idProducto, codBarra, descripcion, presentacion, estado);
        } catch (SQLException e) {
            mostrarError("Error al agregar detalle de producto: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar detalle de producto
    public boolean eliminarDetalleProducto(int idProducto, String codBarra) {
        try {
            return modelo.eliminarDetalleProducto(idProducto, codBarra);
        } catch (SQLException e) {
            mostrarError("Error al eliminar detalle de producto: " + e.getMessage());
            return false;
        }
    }

    // Método para verificar si existe un código de barras
    public boolean existeCodBarra(String codBarra) {
        try {
            return modelo.existeCodBarra(codBarra);
        } catch (SQLException e) {
            mostrarError("Error al verificar código de barras: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar detalle de producto
    public boolean actualizarDetalleProducto(int idProducto, String codBarra, String descripcion,
            String presentacion, boolean estado) {
        try {
            return modelo.actualizarDetalleProducto(idProducto, codBarra, descripcion, presentacion, estado);
        } catch (SQLException e) {
            mostrarError("Error al actualizar detalle de producto: " + e.getMessage());
            return false;
        }
    }

    // Método para buscar un detalle por su código de barras
    public Object[] buscarDetallePorCodBarra(int idProducto, String codBarra) {
        try {
            return modelo.buscarDetallePorCodBarra(idProducto, codBarra);
        } catch (SQLException e) {
            mostrarError("Error al buscar detalle: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener todas las marcas con sus detalles completos
    public List<Object[]> obtenerMarcasCompletas() throws SQLException {
        return modelo.obtenerMarcasCompletas();
    }

// Método para actualizar el estado de una marca
    public boolean actualizarEstadoMarca(int idMarca, boolean estado) throws SQLException {
        return modelo.actualizarEstadoMarca(idMarca, estado);
    }

// Método para actualizar el nombre de una marca
    public boolean actualizarNombreMarca(int idMarca, String nuevoNombre) throws SQLException {
        return modelo.actualizarNombreMarca(idMarca, nuevoNombre);
    }

// Método para verificar si una marca está siendo utilizada por algún producto
    public boolean marcaEstaEnUso(int idMarca) throws SQLException {
        return modelo.marcaEstaEnUso(idMarca);
    }

// Método para eliminar una marca
    public boolean eliminarMarca(int idMarca) throws SQLException {
        return modelo.eliminarMarca(idMarca);
    }

// Método para mostrar la ventana de gestión de marcas
    public void mostrarVentanaGestionMarcas() {
        try {
            // Crear una instancia del formulario modal para gestión de marcas
            vGestionMarcas formGestionMarcas = new vGestionMarcas(
                    (Frame) javax.swing.SwingUtilities.getWindowAncestor(vista),
                    true, // Modal
                    this);

            // Mostrar el formulario
            formGestionMarcas.setVisible(true);

            // Si se realizaron cambios, recargar el combobox de marcas
            if (formGestionMarcas.seCambiaronMarcas()) {
                cargarMarcas(vista.getComboMarca());
            }
        } catch (Exception e) {
            mostrarError("Error al abrir ventana de gestión de marcas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
