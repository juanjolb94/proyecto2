package controlador;

import interfaces.myInterface;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.table.DefaultTableModel;
import modelo.VentasDAO;
import modelo.mVentas;
import modelo.service.ReporteService;
import vista.vRegVentas;
import servicio.sTalonarios;
import vista.vTalonarios;
import servicio.sTalonarios;
import vista.vLogin;

public class cRegVentas implements myInterface {

    private vRegVentas vista;
    private VentasDAO modelo;
    private mVentas ventaActual;
    private DecimalFormat formatoNumero;
    private sTalonarios servicioTalonarios;
    private String numeroFacturaActual;
    private sTalonarios.DatosTalonario datosTalonarioActual;

    public cRegVentas(vRegVentas vista) throws SQLException {
        this.vista = vista;
        this.modelo = new VentasDAO();
        this.ventaActual = new mVentas();
        this.formatoNumero = new DecimalFormat("#,##0");
        this.servicioTalonarios = new sTalonarios();

        // Configurar venta actual con valores por defecto
        inicializarVentaNueva();
        cargarDatosTalonarioActivo();
    }

    /**
     * Carga los datos del talonario activo para mostrar en pantalla
     */
    public void cargarDatosTalonarioActivo() {
        try {
            datosTalonarioActual = servicioTalonarios.obtenerDatosTalonarioActivo();

            // Actualizar vista con los datos
            vista.actualizarNumeroFactura(datosTalonarioActual.getNumeroFactura());
            vista.actualizarTimbrado(datosTalonarioActual.getNumeroTimbrado());

            // Verificar si está vencido y mostrar advertencia
            if (servicioTalonarios.isTimbradoVencido(datosTalonarioActual.getFechaVencimiento())) {
                vista.mostrarAdvertencia("¡ATENCIÓN! El timbrado está VENCIDO. "
                        + "Contacte con la administración antes de continuar.");
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al cargar datos del talonario: " + e.getMessage());
            vista.actualizarNumeroFactura("ERROR-NO-TALONARIO");
            vista.actualizarTimbrado("SIN TIMBRADO");
        }
    }

    // Clase interna para manejar items en combobox
    public static class ItemCombo {

        private Object valor;
        private String texto;

        public ItemCombo(Object valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }

        public Object getValor() {
            return valor;
        }

        @Override
        public String toString() {
            return texto;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ItemCombo) {
                ItemCombo otro = (ItemCombo) obj;
                return this.valor.equals(otro.valor);
            }
            return false;
        }
    }

    // Método para inicializar una nueva venta
    private void inicializarVentaNueva() {
        try {
            this.ventaActual = new mVentas();
            this.ventaActual.setFecha(new Date());
            this.ventaActual.setAnulado(false);

            // Obtener ID de caja activa
            int idCaja = modelo.obtenerIdCajaActiva();
            this.ventaActual.setIdCaja(idCaja);

            // Establecer el usuario actual desde la sesión de login
            this.ventaActual.setIdUsuario(vLogin.getIdUsuarioAutenticado());

        } catch (SQLException e) {
            vista.mostrarError("Error al inicializar venta: " + e.getMessage());
        }
    }

    // Método para obtener lista de clientes para el combo
    public List<ItemCombo> obtenerClientes() {
        List<ItemCombo> resultado = new ArrayList<>();
        try {
            List<Object[]> clientes = modelo.obtenerClientes();

            // Agregar opción por defecto
            resultado.add(new ItemCombo(0, "CLIENTE CONTADO"));

            for (Object[] cliente : clientes) {
                int id = (int) cliente[0];
                String nombre = (String) cliente[1];
                String documento = (String) cliente[2];
                resultado.add(new ItemCombo(id, id + " - " + nombre + " (" + documento + ")"));
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al cargar clientes: " + e.getMessage());
        }
        return resultado;
    }

    // Método para obtener datos de un cliente específico
    public Object[] obtenerDatosCliente(int idCliente) {
        try {
            return modelo.obtenerDatosCliente(idCliente);
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener datos del cliente: " + e.getMessage());
            return null;
        }
    }

    // Método para buscar producto por código de barras
    public Object[] buscarProductoPorCodBarra(String codBarra) {
        try {
            Object[] producto = modelo.buscarProductoPorCodBarra(codBarra);
            if (producto == null) {
                vista.mostrarError("Producto no encontrado con código: " + codBarra);
            }
            return producto;
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    // Método para buscar productos por nombre
    public List<Object[]> buscarProductosPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().length() < 2) {
                vista.mostrarError("Ingrese al menos 2 caracteres para buscar.");
                return new ArrayList<>();
            }

            List<Object[]> productos = modelo.buscarProductosPorNombre(nombre);
            if (productos.isEmpty()) {
                vista.mostrarMensaje("No se encontraron productos con el nombre: " + nombre);
            }
            return productos;

        } catch (SQLException e) {
            vista.mostrarError("Error al buscar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Método para agregar producto desde selección por nombre
    public void agregarProductoSeleccionado(Object[] producto, int cantidad) {
        try {
            if (producto == null) {
                vista.mostrarError("Producto no válido.");
                return;
            }

            int idProducto = (int) producto[0];
            String codBarra = (String) producto[2];
            int precioVenta = (int) producto[4];

            // Verificar si el producto ya está en la venta
            boolean productoExistente = false;
            for (mVentas.DetalleVenta detalle : ventaActual.getDetalles()) {
                if (detalle.getIdProducto() == idProducto
                        && detalle.getCodigoBarra().equals(codBarra)) {

                    // Actualizar cantidad existente
                    int nuevaCantidad = detalle.getCantidad() + cantidad;
                    detalle.actualizar(nuevaCantidad, precioVenta);
                    productoExistente = true;
                    break;
                }
            }

            // Si el producto no existe, agregarlo como nuevo detalle
            if (!productoExistente) {
                mVentas.DetalleVenta detalle = new mVentas.DetalleVenta(
                        ventaActual.getIdVenta(),
                        idProducto,
                        codBarra,
                        cantidad,
                        precioVenta
                );

                ventaActual.agregarDetalle(detalle);
            }

            // Actualizar la vista
            vista.actualizarTablaDetalles();
            vista.actualizarTotalVenta(ventaActual.getTotal());

        } catch (Exception e) {
            vista.mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }

    // Método para agregar un producto a la venta
    public void agregarProducto(String codBarra, int cantidad) {
        try {
            // Buscar el producto
            Object[] producto = buscarProductoPorCodBarra(codBarra);
            if (producto == null) {
                return;
            }

            int idProducto = (int) producto[0];
            String nombre = (String) producto[1];
            int precioVenta = (int) producto[4];

            // NUEVA VALIDACIÓN DE STOCK
            int stockDisponible = obtenerStockDisponible(idProducto, codBarra);
            if (stockDisponible < cantidad) {
                vista.mostrarError("Stock insuficiente.\nDisponible: " + stockDisponible
                        + "\nSolicitado: " + cantidad);
                return;
            }

            // Verificar stock total si ya existe el producto en la venta
            int cantidadYaEnVenta = 0;
            for (mVentas.DetalleVenta detalle : ventaActual.getDetalles()) {
                if (detalle.getIdProducto() == idProducto && detalle.getCodigoBarra().equals(codBarra)) {
                    cantidadYaEnVenta = detalle.getCantidad();
                    break;
                }
            }

            int cantidadTotal = cantidadYaEnVenta + cantidad;
            if (stockDisponible < cantidadTotal) {
                vista.mostrarError("Stock insuficiente para cantidad total.\nDisponible: " + stockDisponible
                        + "\nYa en venta: " + cantidadYaEnVenta
                        + "\nTotal solicitado: " + cantidadTotal);
                return;
            }

            // Resto del código existente...
            boolean productoExistente = false;
            for (mVentas.DetalleVenta detalle : ventaActual.getDetalles()) {
                if (detalle.getIdProducto() == idProducto && detalle.getCodigoBarra().equals(codBarra)) {
                    int nuevaCantidad = detalle.getCantidad() + cantidad;
                    detalle.actualizar(nuevaCantidad, precioVenta);
                    productoExistente = true;
                    break;
                }
            }

            if (!productoExistente) {
                mVentas.DetalleVenta detalle = new mVentas.DetalleVenta(
                        ventaActual.getIdVenta(), idProducto, codBarra, cantidad, precioVenta);
                ventaActual.agregarDetalle(detalle);
            }

            vista.actualizarTablaDetalles();
            vista.actualizarTotalVenta(ventaActual.getTotal());

        } catch (Exception e) {
            vista.mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }

    // Método para obtener stock disponible
    private int obtenerStockDisponible(int idProducto, String codBarra) {
        try {
            return modelo.obtenerStockDisponible(idProducto, codBarra);
        } catch (SQLException e) {
            System.err.println("Error al obtener stock: " + e.getMessage());
            return 0; // Si hay error, asumir que no hay stock
        }
    }

    // Método para eliminar un detalle de la venta
    public void eliminarDetalle(int indice) {
        try {
            if (indice >= 0 && indice < ventaActual.getDetalles().size()) {
                ventaActual.eliminarDetalle(indice);

                // Actualizar la vista
                vista.actualizarTablaDetalles();
                vista.actualizarTotalVenta(ventaActual.getTotal());
            }
        } catch (Exception e) {
            vista.mostrarError("Error al eliminar detalle: " + e.getMessage());
        }
    }

    // Método para modificar cantidad de un detalle
    public void modificarCantidadDetalle(int indice, int nuevaCantidad) {
        try {
            if (indice >= 0 && indice < ventaActual.getDetalles().size()) {
                mVentas.DetalleVenta detalle = ventaActual.getDetalles().get(indice);

                // Validar cantidad
                if (nuevaCantidad <= 0) {
                    vista.mostrarError("La cantidad debe ser mayor a cero.");
                    return;
                }

                // TODO: Cuando tengas stock en productos_detalle, descomenta esta validación:
                /*
                // Verificar stock disponible
                Object[] producto = buscarProductoPorCodBarra(detalle.getCodigoBarra());
                if (producto != null) {
                    int stockDisponible = (int) producto[5];
                    if (stockDisponible < nuevaCantidad) {
                        vista.mostrarError("Stock insuficiente. Disponible: " + stockDisponible);
                        return;
                    }
                }
                 */
                // Actualizar cantidad
                detalle.setCantidad(nuevaCantidad);

                // Actualizar la vista
                vista.actualizarTablaDetalles();
                vista.actualizarTotalVenta(ventaActual.getTotal());
            }
        } catch (Exception e) {
            vista.mostrarError("Error al modificar cantidad: " + e.getMessage());
        }
    }

    // Método para guardar la venta
    public void guardarVenta() {
        try {
            // Validaciones existentes...
            if (ventaActual.getDetalles().isEmpty()) {
                vista.mostrarError("No hay productos en la venta.");
                return;
            }

            if (ventaActual.getIdCliente() == 0) {
                vista.mostrarError("Debe seleccionar un cliente.");
                return;
            }

            if (ventaActual.getIdUsuario() == 0) {
                ventaActual.setIdUsuario(vLogin.getIdUsuarioAutenticado());

                if (ventaActual.getIdUsuario() == 0) {
                    vista.mostrarError("Error del sistema: Usuario no identificado. Reinicie sesión.");
                    return;
                }
            }

            // Consumir número de factura del talonario
            sTalonarios.DatosTalonario datosVenta = servicioTalonarios.consumirSiguienteFactura();

            // Establecer datos del talonario en la venta
            ventaActual.setNumeroFactura(datosVenta.getNumeroFactura());
            ventaActual.setNumeroTimbrado(datosVenta.getNumeroTimbrado());

            // Insertar venta con datos del talonario
            int idVenta = modelo.insertarVentaConTalonario(ventaActual);

            if (idVenta > 0) {
                // ✅ VERIFICAR que el ID se estableció correctamente
                System.out.println("=== DESPUÉS DE GUARDAR ===");
                System.out.println("ID retornado: " + idVenta);
                System.out.println("ID en ventaActual: " + ventaActual.getIdVenta());
                System.out.println("==========================");

                vista.mostrarMensaje("Venta guardada correctamente.\nFactura: "
                        + datosVenta.getNumeroFactura()
                        + "\nTimbrado: " + datosVenta.getNumeroTimbrado());

                try {
                    ReporteService reporteService = new ReporteService();
                    reporteService.generarYGuardarTicket(ventaActual.getIdVenta());
                } catch (Exception e) {
                    System.err.println("Error al guardar PDF del ticket: " + e.getMessage());
                    // No interrumpir el flujo si falla el guardado del PDF
                }

                // Actualizar para la siguiente venta
                cargarDatosTalonarioActivo();

                // Notificar cambio a ventana de talonarios
                notificarVentanaTalonarios();

                // ✅ PREGUNTAR IMPRESIÓN ANTES DE LIMPIAR
                vista.preguntarImprimirFactura(ventaActual.getIdVenta());

                // Limpiar formulario para nueva venta (DESPUÉS de la impresión)
                limpiarFormulario();

            } else {
                vista.mostrarError("Error al guardar la venta.");
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al guardar venta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Busca y actualiza la ventana de talonarios si está abierta
     */
    private void notificarVentanaTalonarios() {
        try {
            JDesktopPane desktop = vista.getDesktopPane();
            if (desktop != null) {
                for (JInternalFrame frame : desktop.getAllFrames()) {
                    if (frame instanceof vTalonarios && !frame.isClosed()) {
                        // Actualizar la ventana de talonarios
                        ((vTalonarios) frame).actualizar();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar ventana de talonarios: " + e.getMessage());
        }
    }

    // Método para buscar una venta por ID
    public void buscarVentaPorId(int id) {
        try {
            mVentas venta = modelo.buscarVentaPorId(id);

            if (venta != null) {
                this.ventaActual = venta;

                // Actualizar la vista con los datos de la venta
                vista.cargarDatosVenta(venta);
                vista.actualizarTablaDetalles();
                vista.actualizarTotalVenta(venta.getTotal());

            } else {
                vista.mostrarError("No se encontró la venta con ID: " + id);
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar venta: " + e.getMessage());
        }
    }

    // Método para anular una venta
    public void anularVenta() {
        try {
            int idVenta = ventaActual.getIdVenta();
            if (idVenta <= 0) {
                vista.mostrarError("No hay una venta seleccionada para anular.");
                return;
            }

            if (ventaActual.isAnulado()) {
                vista.mostrarError("La venta ya está anulada.");
                return;
            }

            // Confirmar anulación
            boolean confirmar = vista.confirmarAnulacion();
            if (!confirmar) {
                return;
            }

            // Anular la venta
            boolean exito = modelo.anularVenta(idVenta);
            if (exito) {
                vista.mostrarMensaje("Venta anulada correctamente.");
                ventaActual.setAnulado(true);
                vista.marcarVentaComoAnulada();
            } else {
                vista.mostrarError("Error al anular la venta.");
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al anular venta: " + e.getMessage());
        }
    }

    // Método para limpiar el formulario
    public void limpiarFormulario() {
        inicializarVentaNueva();
        vista.limpiarFormulario();
        vista.actualizarTablaDetalles();
        vista.actualizarTotalVenta(0);
    }

    // Método para establecer el cliente de la venta
    public void setCliente(int idCliente) {
        ventaActual.setIdCliente(idCliente);
    }

    // Método para establecer el usuario de la venta
    public void setUsuario(int idUsuario) {
        ventaActual.setIdUsuario(idUsuario);
    }

    // Método para establecer las observaciones
    public void setObservaciones(String observaciones) {
        ventaActual.setObservaciones(observaciones);
    }

    // Método para obtener el modelo de tabla de detalles
    public DefaultTableModel getModeloTablaDetalles() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"#", "Código", "Descripción", "Cantidad", "Precio Unit.", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo permitir editar cantidad
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // Número
                    case 3: // Cantidad
                    case 4: // Precio
                    case 5: // Subtotal
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        // Llenar el modelo con los detalles actuales
        for (int i = 0; i < ventaActual.getDetalles().size(); i++) {
            mVentas.DetalleVenta detalle = ventaActual.getDetalles().get(i);

            // Obtener descripción del producto
            String descripcion = obtenerDescripcionProducto(detalle.getIdProducto(), detalle.getCodigoBarra());

            modelo.addRow(new Object[]{
                i + 1, // Número de ítem
                detalle.getCodigoBarra(),
                descripcion,
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
            });
        }

        return modelo;
    }

    // Método auxiliar para obtener descripción del producto
    private String obtenerDescripcionProducto(int idProducto, String codBarra) {
        try {
            Object[] producto = modelo.buscarProductoPorCodBarra(codBarra);
            if (producto != null) {
                return producto[1] + " - " + producto[3]; // nombre - descripción
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener descripción: " + e.getMessage());
        }
        return "Producto no encontrado";
    }

    // Método para obtener la venta actual
    public mVentas getVentaActual() {
        return ventaActual;
    }

    // Método para formatear números
    public String formatearNumero(int numero) {
        return formatoNumero.format(numero);
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        guardarVenta();
    }

    @Override
    public void imFiltrar() {
        // Implementar filtrado de ventas si es necesario
        vista.mostrarDialogoFiltros();
    }

    @Override
    public void imActualizar() {
        try {
            vista.actualizarTablaDetalles();
            vista.actualizarTotalVenta(ventaActual.getTotal());
        } catch (Exception e) {
            vista.mostrarError("Error al actualizar datos: " + e.getMessage());
        }
    }

    @Override
    public void imBorrar() {
        anularVenta();
    }

    // Métodos adicionales de myInterface
    @Override
    public void setRegistroSeleccionado(int id) {
        buscarVentaPorId(id);
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
        return "ventas";
    }

    @Override
    public void imNuevo() {
        limpiarFormulario();
    }

    @Override
    public void imBuscar() {
        // Solicitar ID de venta para buscar
        try {
            String input = javax.swing.JOptionPane.showInputDialog(
                    vista,
                    "Ingrese el ID de la venta:",
                    "Buscar Venta",
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

            if (input != null && !input.trim().isEmpty()) {
                int id = Integer.parseInt(input.trim());
                buscarVentaPorId(id);
            }
        } catch (NumberFormatException e) {
            vista.mostrarError("El ID debe ser un número válido.");
        }
    }

    @Override
    public void imPrimero() {
        // Navegar al primer registro de ventas
        try {
            java.sql.Connection conn = modelo.getConnection();
            String sql = "SELECT MIN(id) FROM ventas WHERE anulado = false";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int primerID = rs.getInt(1);
                    if (primerID > 0) {
                        buscarVentaPorId(primerID);
                    }
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al navegar al primer registro: " + e.getMessage());
        }
    }

    @Override
    public void imSiguiente() {
        // Navegar al siguiente registro
        try {
            int idActual = ventaActual.getIdVenta();
            if (idActual > 0) {
                java.sql.Connection conn = modelo.getConnection();
                String sql = "SELECT MIN(id) FROM ventas WHERE id > ? AND anulado = false";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idActual);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int siguienteID = rs.getInt(1);
                            if (siguienteID > 0) {
                                buscarVentaPorId(siguienteID);
                            } else {
                                vista.mostrarMensaje("No hay más registros.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al navegar al siguiente registro: " + e.getMessage());
        }
    }

    @Override
    public void imAnterior() {
        // Navegar al registro anterior
        try {
            int idActual = ventaActual.getIdVenta();
            if (idActual > 0) {
                java.sql.Connection conn = modelo.getConnection();
                String sql = "SELECT MAX(id) FROM ventas WHERE id < ? AND anulado = false";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idActual);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int anteriorID = rs.getInt(1);
                            if (anteriorID > 0) {
                                buscarVentaPorId(anteriorID);
                            } else {
                                vista.mostrarMensaje("No hay registros anteriores.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al navegar al registro anterior: " + e.getMessage());
        }
    }

    @Override
    public void imUltimo() {
        // Navegar al último registro de ventas
        try {
            java.sql.Connection conn = modelo.getConnection();
            String sql = "SELECT MAX(id) FROM ventas WHERE anulado = false";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ultimoID = rs.getInt(1);
                    if (ultimoID > 0) {
                        buscarVentaPorId(ultimoID);
                    }
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al navegar al último registro: " + e.getMessage());
        }
    }

    @Override
    public void imImprimir() {
        // Implementar impresión de factura
        try {
            if (ventaActual.getIdVenta() > 0) {
                vista.mostrarMensaje("Imprimiendo factura de venta #" + ventaActual.getIdVenta());
                // TODO: Implementar lógica de impresión real
            } else {
                vista.mostrarError("No hay una venta seleccionada para imprimir.");
            }
        } catch (Exception e) {
            vista.mostrarError("Error al imprimir: " + e.getMessage());
        }
    }

    @Override
    public void imInsDet() {
        // Insertar detalle - enfocar en código de barras para agregar producto
        vista.getTxtCodigoBarra().requestFocus();
        vista.getTxtCodigoBarra().selectAll();
    }

    @Override
    public void imDelDet() {
        // Eliminar detalle seleccionado
        javax.swing.JTable tabla = vista.getTblDetalles();
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada >= 0) {
            eliminarDetalle(filaSeleccionada);
        } else {
            vista.mostrarError("Seleccione un producto para eliminar.");
        }
    }

    @Override
    public void imCerrar() {
        // Cerrar la ventana
        vista.dispose();
    }

    @Override
    public boolean imAbierto() {
        // Verificar si la ventana está abierta y visible
        return vista.isVisible() && !vista.isClosed();
    }

    @Override
    public void imAbrir() {
        // Abrir/mostrar la ventana
        if (vista.isClosed()) {
            vista.mostrarMensaje("La ventana está cerrada. Crear nueva instancia desde el menú principal.");
        } else {
            vista.setVisible(true);
            vista.toFront();
            vista.getTxtCodigoBarra().requestFocus();
        }
    }
}
