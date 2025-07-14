package controlador;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.ComprasDAO;
import modelo.mCompras;
import vista.vRegCompras;
import interfaces.myInterface;

public class cRegCompras implements myInterface {

    private vRegCompras vista;
    private ComprasDAO modelo;
    private mCompras compraActual;

    public cRegCompras(vRegCompras vista) throws SQLException {
        this.vista = vista;
        this.modelo = new ComprasDAO();
        this.compraActual = new mCompras();
        this.compraActual.setFechaCompra(new Date()); // Fecha actual por defecto
        this.compraActual.setEstado(true); // Activo por defecto
    }

    // Clase interna para manejar items en los combobox
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

    // Método para obtener la lista de proveedores
    public List<ItemCombo> obtenerProveedores() {
        List<ItemCombo> resultado = new ArrayList<>();
        try {
            // Obtener los proveedores desde la base de datos a través de otro DAO
            modelo.ProveedoresDAO proveedoresDAO = new modelo.ProveedoresDAO();
            List<Object[]> proveedores = proveedoresDAO.listarProveedores();

            for (Object[] proveedor : proveedores) {
                int id = (int) proveedor[0];
                String razonSocial = (String) proveedor[1];
                resultado.add(new ItemCombo(id, id + " - " + razonSocial));
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al obtener proveedores: " + e.getMessage());
        }
        return resultado;
    }

    // Método para obtener los datos de un proveedor por su ID
    public Object[] obtenerDatosProveedor(int idProveedor) {
        try {
            modelo.ProveedoresDAO proveedoresDAO = new modelo.ProveedoresDAO();
            Object[] proveedor = proveedoresDAO.buscarProveedorPorId(idProveedor);

            if (proveedor != null) {
                // Devolver RUC y Razón Social
                return new Object[]{
                    proveedor[2], // RUC
                    proveedor[1] // Razón Social
                };
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener datos del proveedor: " + e.getMessage());
        }
        return null;
    }

    // Método para guardar la compra completa
    public void guardarCompra() {
        try {
            System.out.println("DEBUG GUARDAR: Iniciando guardarCompra - Detalles: " + compraActual.getDetalles().size());

            // Validar que tenga detalles
            if (compraActual.getDetalles().isEmpty()) {
                System.out.println("DEBUG GUARDAR: ERROR - compraActual.getDetalles() está vacío!");
                vista.mostrarError("Debe agregar al menos un detalle a la compra.");
                return;
            }

            System.out.println("DEBUG GUARDAR: Validación de detalles pasó correctamente");

            // Validar proveedor
            if (compraActual.getIdProveedor() <= 0) {
                vista.mostrarError("Debe seleccionar un proveedor.");
                return;
            }

            // Validar número de factura
            if (compraActual.getNumeroFactura() == null || compraActual.getNumeroFactura().trim().isEmpty()) {
                vista.mostrarError("Debe ingresar un número de factura.");
                return;
            }

            // Verificar factura única
            if (!validarFacturaUnica(compraActual.getNumeroFactura())) {
                return;
            }

            // Guardar la compra
            int idCompra = modelo.insertarCompra(compraActual);

            if (idCompra > 0) {
                vista.mostrarMensaje("Compra registrada correctamente con ID: " + idCompra);
                compraActual = new mCompras();
                compraActual.setFechaCompra(new Date());
                compraActual.setEstado(true);
                vista.limpiarFormulario();
            } else {
                vista.mostrarError("Error al registrar la compra.");
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al guardar la compra: " + e.getMessage());
        }
    }

    // Método para agregar un detalle a la compra
    public void agregarDetalle(int idProducto, String codBarra, int cantidad, double precioUnitario) {
        try {
            // Validar datos
            if (idProducto <= 0) {
                vista.mostrarError("Debe seleccionar un producto válido.");
                return;
            }

            if (codBarra == null || codBarra.trim().isEmpty()) {
                vista.mostrarError("Código de barras inválido.");
                return;
            }

            if (cantidad <= 0) {
                vista.mostrarError("La cantidad debe ser mayor a cero.");
                return;
            }

            if (precioUnitario <= 0) {
                vista.mostrarError("El precio unitario debe ser mayor a cero.");
                return;
            }

            // Verificar si ya existe el producto en los detalles
            for (mCompras.DetalleCompra detalle : compraActual.getDetalles()) {
                if (detalle.getIdProducto() == idProducto && detalle.getCodBarra().equals(codBarra)) {
                    vista.mostrarError("Este producto ya está en la lista. Modifique la cantidad si es necesario.");
                    return;
                }
            }

            // Crear y agregar el detalle usando el constructor correcto
            mCompras.DetalleCompra detalle = new mCompras.DetalleCompra(
                    compraActual.getIdCompra(),
                    idProducto,
                    codBarra,
                    cantidad,
                    precioUnitario,
                    true // Indicar que es precio unitario, no subtotal
            );

            compraActual.agregarDetalle(detalle);

            // Actualizar la interfaz
            vista.actualizarTablaDetalles();
            vista.actualizarTotalCompra(compraActual.getTotalCompra());

        } catch (Exception e) {
            vista.mostrarError("Error al agregar detalle: " + e.getMessage());
        }
    }

    // Método para actualizar un detalle existente
    public void actualizarDetalle(int indice, int idProducto, String codBarra, int cantidad, double precio) {
        try {
            // Validar índice
            if (indice < 0 || indice >= compraActual.getDetalles().size()) {
                vista.mostrarError("Índice de detalle inválido.");
                return;
            }

            // Validar datos
            if (cantidad <= 0) {
                vista.mostrarError("La cantidad debe ser mayor a cero.");
                return;
            }

            if (precio <= 0) {
                vista.mostrarError("El precio unitario debe ser mayor a cero.");
                return;
            }

            // Obtener el detalle
            mCompras.DetalleCompra detalle = compraActual.getDetalles().get(indice);

            // Actualizar valores
            detalle.actualizar(cantidad, precio);

            // Recalcular el total de la compra
            double total = 0;
            for (mCompras.DetalleCompra d : compraActual.getDetalles()) {
                total += d.getSubtotal();
            }
            compraActual.setTotalCompra(Math.round(total)); // Redondear a entero

            // Actualizar la vista
            vista.actualizarTablaDetalles();
            vista.actualizarTotalCompra(compraActual.getTotalCompra());

        } catch (Exception e) {
            vista.mostrarError("Error al actualizar detalle: " + e.getMessage());
        }
    }

    // Método para eliminar un detalle de la compra
    public void eliminarDetalle(int indice) {
        try {
            if (indice >= 0 && indice < compraActual.getDetalles().size()) {
                compraActual.eliminarDetalle(indice);

                // Actualizar la interfaz
                vista.actualizarTablaDetalles();
                vista.actualizarTotalCompra(compraActual.getTotalCompra());
            }
        } catch (Exception e) {
            vista.mostrarError("Error al eliminar detalle: " + e.getMessage());
        }
    }

    // Método para buscar un producto por código de barras
    public Object[] buscarProductoPorCodBarra(String codBarra) {
        try {
            return modelo.buscarProductoPorCodBarra(codBarra);
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener la lista de productos para compra
    public List<Object[]> obtenerProductosParaCompra() {
        try {
            return modelo.obtenerProductosParaCompra();
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Método para buscar una compra por ID
    public void buscarCompraPorId(int id) {
        System.out.println("DEBUG: buscarCompraPorId(" + id + ") ejecutado");
        try {
            mCompras compra = modelo.buscarCompraPorId(id);
            if (compra != null) {
                System.out.println("DEBUG: Reemplazando compraActual con compra encontrada");
                compraActual = compra;

                // Actualizar la interfaz
                vista.cargarDatosCompra(compra);
                vista.actualizarTablaDetalles();
                vista.actualizarTotalCompra(compra.getTotalCompra());
            } else {
                vista.mostrarError("No se encontró la compra con ID: " + id);
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar compra: " + e.getMessage());
        }
    }

    // Método para establecer el proveedor de la compra
    public void setProveedor(int idProveedor) {
        compraActual.setIdProveedor(idProveedor);
    }

    // Método para establecer la fecha de la compra
    public void setFechaCompra(Date fecha) {
        compraActual.setFechaCompra(fecha);
    }

    // Método para establecer el número de factura
    public void setNumeroFactura(String numeroFactura) {
        compraActual.setNumeroFactura(numeroFactura);
    }

    // Método para establecer las observaciones
    public void setObservaciones(String observaciones) {
        compraActual.setObservaciones(observaciones);
    }

    // Método para obtener los detalles actuales como modelo para tabla
    public DefaultTableModel getModeloTablaDetalles() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID Producto", "Código Barras", "Descripción", "Cantidad", "Precio Unit.", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Solo permitir editar cantidad y precio
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // ID Producto
                        return Integer.class;
                    case 3: // Cantidad
                        return Integer.class;
                    case 4: // Precio Unit.
                    case 5: // Subtotal
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        for (mCompras.DetalleCompra detalle : compraActual.getDetalles()) {
            Object[] fila = {
                detalle.getIdProducto(),
                detalle.getCodBarra(),
                obtenerDescripcionProducto(detalle.getIdProducto(), detalle.getCodBarra()),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
            };
            modelo.addRow(fila);
        }

        return modelo;
    }

    // Método auxiliar para obtener la descripción de un producto
    private String obtenerDescripcionProducto(int idProducto, String codBarra) {
        try {
            Object[] producto = modelo.buscarProductoPorCodBarra(codBarra);
            if (producto != null) {
                return producto[1] + " - " + producto[3]; // Formato: nombre - descripción
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener descripción: " + e.getMessage());
        }
        return "Producto no encontrado";
    }

    // Obtener la compra actual
    public mCompras getCompraActual() {
        return compraActual;
    }

    // Método para agregar un detalle a la compra con subtotal
    public void agregarDetalleConSubtotal(int idProducto, String codBarra, int cantidad, double subtotal) {
        try {
            // Validar datos
            if (idProducto <= 0) {
                vista.mostrarError("Debe seleccionar un producto válido.");
                return;
            }

            if (codBarra == null || codBarra.trim().isEmpty()) {
                vista.mostrarError("Código de barras inválido.");
                return;
            }

            if (cantidad <= 0) {
                vista.mostrarError("La cantidad debe ser mayor a cero.");
                return;
            }

            if (subtotal <= 0) {
                vista.mostrarError("El subtotal debe ser mayor a cero.");
                return;
            }

            // Verificar si ya existe el producto en los detalles
            for (mCompras.DetalleCompra detalle : compraActual.getDetalles()) {
                if (detalle.getIdProducto() == idProducto && detalle.getCodBarra().equals(codBarra)) {
                    vista.mostrarError("Este producto ya está en la lista. Modifique la cantidad si es necesario.");
                    return;
                }
            }

            // Redondear subtotal
            int subtotalRedondeado = (int) Math.round(subtotal);

            // Calcular impuesto (método paraguayo)
            int impuesto = (int) Math.round(subtotalRedondeado / 11.0);

            // Calcular base imponible
            int baseImponible = subtotalRedondeado - impuesto;

            // Calcular precio unitario (para referencia)
            double precioUnitario = (cantidad > 0) ? subtotal / cantidad : 0;

            // Crear y agregar el detalle
            mCompras.DetalleCompra detalle = new mCompras.DetalleCompra(
                    compraActual.getIdCompra(),
                    idProducto,
                    codBarra,
                    cantidad,
                    precioUnitario
            );

            // Actualizar valores precalculados
            detalle.actualizarValoresPrecalculados(baseImponible, impuesto, subtotalRedondeado);

            compraActual.agregarDetalle(detalle);

            // Actualizar la interfaz
            vista.actualizarTablaDetalles();
            vista.actualizarTotalCompra(compraActual.getTotalCompra());

        } catch (Exception e) {
            vista.mostrarError("Error al agregar detalle: " + e.getMessage());
        }
    }

    private boolean validarFacturaUnica(String numeroFactura) {
        try {
            if (modelo.existeFactura(numeroFactura)) {
                vista.mostrarError("❌ FACTURA DUPLICADA\n\n"
                        + "Ya existe una compra registrada con el número de factura:\n"
                        + "'" + numeroFactura + "'\n\n"
                        + "Por favor, verifique el número de factura.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            vista.mostrarError("Error al validar factura: " + e.getMessage());
            return false;
        }
    }

    // Implementación de los métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        guardarCompra();
    }

    @Override
    public void imFiltrar() {
        // No implementado para compras
    }

    @Override
    public void imActualizar() {
        try {
            vista.actualizarTablaDetalles();
            vista.actualizarTotalCompra(compraActual.getTotalCompra());
        } catch (Exception e) {
            vista.mostrarError("Error al actualizar datos: " + e.getMessage());
        }
    }

    @Override
    public void imBorrar() {
        try {
            int id = compraActual.getIdCompra();
            if (id <= 0) {
                vista.mostrarError("No hay una compra seleccionada para anular.");
                return;
            }

            boolean exito = modelo.anularCompra(id);
            if (exito) {
                vista.mostrarMensaje("Compra anulada correctamente.");
                // Actualizar el estado en la compra actual
                compraActual.setEstado(false);
                vista.actualizarEstadoCompra(false);
            } else {
                vista.mostrarError("No se pudo anular la compra.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al anular compra: " + e.getMessage());
        }
    }

    @Override
    public void imNuevo() {
        System.out.println("DEBUG: imNuevo() ejecutado - limpiando compraActual");
        compraActual = new mCompras();
        compraActual.setFechaCompra(new Date());
        compraActual.setEstado(true);
    }

    @Override
    public void imBuscar() {
        try {
            String idStr = JOptionPane.showInputDialog(vista, "Ingrese el ID de la compra:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    buscarCompraPorId(id);
                } catch (NumberFormatException e) {
                    vista.mostrarError("El ID debe ser un número entero válido.");
                }
            }
        } catch (Exception e) {
            vista.mostrarError("Error al buscar: " + e.getMessage());
        }
    }

    @Override
    public void imPrimero() {
        try {
            Object[] compra = modelo.obtenerPrimeraCompra();
            if (compra != null) {
                int id = (int) compra[0];
                buscarCompraPorId(id);
            } else {
                vista.mostrarMensaje("No hay compras registradas.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener primera compra: " + e.getMessage());
        }
    }

    @Override
    public void imSiguiente() {
        try {
            int idActual = compraActual.getIdCompra();
            if (idActual <= 0) {
                vista.mostrarError("No hay una compra activa.");
                return;
            }

            Object[] compra = modelo.obtenerCompraSiguiente(idActual);
            if (compra != null) {
                int id = (int) compra[0];
                buscarCompraPorId(id);
            } else {
                vista.mostrarMensaje("No hay más compras siguientes.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener siguiente compra: " + e.getMessage());
        }
    }

    @Override
    public void imAnterior() {
        try {
            int idActual = compraActual.getIdCompra();
            if (idActual <= 0) {
                vista.mostrarError("No hay una compra activa.");
                return;
            }

            Object[] compra = modelo.obtenerCompraAnterior(idActual);
            if (compra != null) {
                int id = (int) compra[0];
                buscarCompraPorId(id);
            } else {
                vista.mostrarMensaje("No hay más compras anteriores.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener compra anterior: " + e.getMessage());
        }
    }

    @Override
    public void imUltimo() {
        try {
            Object[] compra = modelo.obtenerUltimaCompra();
            if (compra != null) {
                int id = (int) compra[0];
                buscarCompraPorId(id);
            } else {
                vista.mostrarMensaje("No hay compras registradas.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener última compra: " + e.getMessage());
        }
    }

    @Override
    public void imImprimir() {
        // La implementación de la impresión puede requerir un servicio adicional de reportes
        vista.mostrarMensaje("Funcionalidad de impresión no implementada.");
    }

    @Override
    public void imInsDet() {
        vista.mostrarDialogoAgregarDetalle();
    }

    @Override
    public void imDelDet() {
        int filaSeleccionada = vista.getFilaSeleccionada();
        if (filaSeleccionada >= 0) {
            eliminarDetalle(filaSeleccionada);
        } else {
            vista.mostrarError("Debe seleccionar un detalle para eliminar.");
        }
    }

    @Override
    public void imCerrar() {
        vista.dispose();
    }

    @Override
    public boolean imAbierto() {
        return vista.isVisible();
    }

    @Override
    public void imAbrir() {
        vista.setVisible(true);
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
        buscarCompraPorId(id);
    }

    public void setTipoDocumento(String tipoDocumento) {
        compraActual.setTipoDocumento(tipoDocumento);
    }

    public void setTimbrado(String timbrado) {
        compraActual.setTimbrado(timbrado);
    }

    public void setCondicion(String condicion) {
        compraActual.setCondicion(condicion);
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        compraActual.setFechaVencimiento(fechaVencimiento);
    }

    public void setSubtotal(double subtotal) {
        compraActual.setSubtotal(subtotal);
    }

    public void setTotalIva5(double totalIva5) {
        compraActual.setTotalIva5(totalIva5);
    }

    public void setTotalIva10(double totalIva10) {
        compraActual.setTotalIva10(totalIva10);
    }

    public void setTotalIva(double totalIva) {
        compraActual.setTotalIva(totalIva);
    }

    public void setNroPlanilla(String nroPlanilla) {
        compraActual.setNroPlanilla(nroPlanilla);
    }
}
