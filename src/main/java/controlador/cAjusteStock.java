package controlador;

import interfaces.myInterface;
import modelo.AjusteStockDAO;
import modelo.mAjusteStock;
import vista.vAjusteStock;
import vista.vBusqueda;
import vista.vSeleccionProductoAjuste;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class cAjusteStock implements myInterface {

    private vAjusteStock vista;
    private AjusteStockDAO modelo;
    private mAjusteStock ajusteActual;
    private DecimalFormat formatoNumero;
    private vSeleccionProductoAjuste ventanaSeleccionProducto;

    public cAjusteStock(vAjusteStock vista) throws SQLException {
        this.vista = vista;
        this.modelo = new AjusteStockDAO();
        this.ajusteActual = new mAjusteStock();
        this.formatoNumero = new DecimalFormat("#,##0");

        // Establecer usuario actual (aquí puedes obtenerlo del sistema de login)
        this.ajusteActual.setUsuarioId(1); // Temporal - obtener del login

        inicializarNuevoAjuste();
    }

    // Inicializar nuevo ajuste
    private void inicializarNuevoAjuste() {
        ajusteActual = new mAjusteStock();
        ajusteActual.setUsuarioId(1); // Temporal - obtener del login actual
        ajusteActual.setFecha(new Date());
        vista.limpiarFormulario();
        vista.actualizarTablaDetalles(); // Actualizar tabla vacía
    }

    // Buscar producto por código de barras y agregarlo al detalle
    public void agregarProductoPorCodigo(String codigoBarra) {
        if (codigoBarra == null || codigoBarra.trim().isEmpty()) {
            vista.mostrarError("Debe ingresar un código de barras válido.");
            return;
        }

        try {
            // Verificar si el producto ya está en el detalle
            for (mAjusteStock.DetalleAjuste detalle : ajusteActual.getDetalles()) {
                if (detalle.getCodBarra().equals(codigoBarra.trim())) {
                    vista.mostrarError("El producto ya está agregado al ajuste.");
                    return;
                }
            }

            // Buscar producto en la base de datos
            Object[] producto = modelo.buscarProductoConStock(codigoBarra.trim());

            if (producto != null) {
                // Crear detalle y agregarlo
                mAjusteStock.DetalleAjuste detalle = new mAjusteStock.DetalleAjuste();
                detalle.setIdProducto((Integer) producto[0]);
                detalle.setCodBarra((String) producto[2]);
                detalle.setNombreProducto((String) producto[1]);
                detalle.setDescripcionProducto((String) producto[3]);
                // Manejar tanto Integer como Double para el stock
                Object stockObj = producto[4];
                int stockValue = 0;
                if (stockObj instanceof Integer) {
                    stockValue = (Integer) stockObj;
                } else if (stockObj instanceof Double) {
                    stockValue = ((Double) stockObj).intValue();
                }
                detalle.setCantidadSistema(stockValue);
                detalle.setCantidadAjuste(0);

                ajusteActual.agregarDetalle(detalle);
                vista.actualizarTablaDetalles();
                vista.limpiarCodigoBarra();

                System.out.println("Producto agregado: " + detalle.getDescripcionCompleta());
            } else {
                vista.mostrarError("No se encontró el producto con código: " + codigoBarra);
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Agregar producto seleccionado desde ventana de selección
    public void agregarProductoSeleccionado(Object[] producto) {
        if (producto == null) {
            return;
        }

        try {
            String codBarra = (String) producto[2];

            // Verificar si ya está agregado
            for (mAjusteStock.DetalleAjuste detalle : ajusteActual.getDetalles()) {
                if (detalle.getCodBarra().equals(codBarra)) {
                    vista.mostrarError("El producto ya está agregado al ajuste.");
                    return;
                }
            }

            // Crear detalle
            mAjusteStock.DetalleAjuste detalle = new mAjusteStock.DetalleAjuste();
            detalle.setIdProducto((Integer) producto[0]);
            detalle.setCodBarra(codBarra);
            detalle.setNombreProducto((String) producto[1]);
            detalle.setDescripcionProducto((String) producto[3]);
            // Manejar tanto Integer como Double para el stock
            Object stockObj = producto[4];
            int stockValue = 0;
            if (stockObj instanceof Integer) {
                stockValue = (Integer) stockObj;
            } else if (stockObj instanceof Double) {
                stockValue = ((Double) stockObj).intValue();
            }
            detalle.setCantidadSistema(stockValue);
            detalle.setCantidadAjuste(0);

            ajusteActual.agregarDetalle(detalle);
            vista.actualizarTablaDetalles();

            System.out.println("Producto seleccionado agregado: " + detalle.getDescripcionCompleta());
        } catch (Exception e) {
            vista.mostrarError("Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Eliminar detalle seleccionado
    public void eliminarDetalleSeleccionado(int indiceSeleccionado) {
        if (indiceSeleccionado >= 0 && indiceSeleccionado < ajusteActual.getDetalles().size()) {
            String descripcion = ajusteActual.getDetalles().get(indiceSeleccionado).getDescripcionCompleta();

            int opcion = JOptionPane.showConfirmDialog(
                    vista,
                    "¿Está seguro que desea eliminar el producto?\n" + descripcion,
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion == JOptionPane.YES_OPTION) {
                ajusteActual.eliminarDetalle(indiceSeleccionado);
                vista.actualizarTablaDetalles();
                System.out.println("Detalle eliminado: " + descripcion);
            }
        } else {
            vista.mostrarError("Seleccione un producto para eliminar.");
        }
    }

    // Actualizar cantidad de ajuste desde la tabla
    public void actualizarCantidadAjuste(int indice, double nuevaCantidad) {
        if (indice >= 0 && indice < ajusteActual.getDetalles().size()) {
            if (nuevaCantidad >= 0) {
                // Validación adicional: verificar límites razonables
                if (nuevaCantidad > 999999) {
                    vista.mostrarError("La cantidad no puede ser mayor a 999,999.");
                    return;
                }

                ajusteActual.getDetalles().get(indice).setCantidadAjuste((int) nuevaCantidad);
                System.out.println("Cantidad actualizada en índice " + indice + ": " + nuevaCantidad);

                // Actualizar la vista para reflejar los cambios
                vista.actualizarTablaDetalles();
            } else {
                vista.mostrarError("La cantidad no puede ser negativa.");
            }
        }
    }

    // Actualizar observaciones de detalle desde la tabla
    public void actualizarObservacionesDetalle(int indice, String observaciones) {
        if (indice >= 0 && indice < ajusteActual.getDetalles().size()) {
            ajusteActual.getDetalles().get(indice).setObservaciones(observaciones);
        }
    }

    // Cargar ajuste en la vista
    private void cargarAjusteEnVista(mAjusteStock ajuste) {
        if (ajuste != null) {
            this.ajusteActual = ajuste;
            vista.cargarDatosAjuste(ajuste);
            vista.actualizarTablaDetalles();
            System.out.println("Ajuste cargado: ID " + ajuste.getIdAjuste());
        } else {
            vista.mostrarError("No se encontró el ajuste especificado.");

            // IMPORTANTE: Limpiar formulario y tabla cuando no se encuentra el ajuste
            inicializarNuevoAjuste();

            // Enfocar el campo ID para facilitar nueva búsqueda
            SwingUtilities.invokeLater(() -> {
                vista.enfocarIdAjuste();
            });
        }
    }

    // Guardar ajuste
    private void guardarAjuste() {
        try {
            // Validaciones
            if (!ajusteActual.tieneDetalles()) {
                vista.mostrarError("Debe agregar al menos un producto al ajuste.");
                return;
            }

            // Obtener datos de la vista
            ajusteActual.setObservaciones(vista.getObservaciones());

            boolean esNuevo = ajusteActual.getIdAjuste() <= 0;

            if (esNuevo) {
                // Insertar nuevo ajuste
                int idGenerado = modelo.insertarAjusteCabecera(ajusteActual);

                if (idGenerado > 0) {
                    ajusteActual.setIdAjuste(idGenerado);

                    // Insertar detalles
                    for (mAjusteStock.DetalleAjuste detalle : ajusteActual.getDetalles()) {
                        modelo.insertarAjusteDetalle(idGenerado, detalle);
                    }

                    vista.mostrarMensaje("Ajuste guardado exitosamente. ID: " + idGenerado);

                    // IMPORTANTE: Limpiar después de guardar exitosamente
                    inicializarNuevoAjuste();

                } else {
                    vista.mostrarError("Error al generar ID del ajuste.");
                }
            } else {
                // Actualizar ajuste existente
                modelo.actualizarAjusteCabecera(ajusteActual);

                // Eliminar detalles anteriores y insertar nuevos
                modelo.eliminarDetallesAjuste(ajusteActual.getIdAjuste());
                for (mAjusteStock.DetalleAjuste detalle : ajusteActual.getDetalles()) {
                    modelo.insertarAjusteDetalle(ajusteActual.getIdAjuste(), detalle);
                }

                vista.mostrarMensaje("Ajuste actualizado exitosamente.");

                // Limpiar después de actualizar exitosamente
                inicializarNuevoAjuste();
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al guardar ajuste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Buscar ajuste por ID
    public void buscarAjustePorId(int idAjuste) {
        if (idAjuste <= 0) {
            vista.mostrarError("Ingrese un ID válido.");
            return;
        }

        try {
            mAjusteStock ajuste = modelo.buscarAjustePorId(idAjuste);
            cargarAjusteEnVista(ajuste);
        } catch (SQLException e) {
            vista.mostrarError("Error al buscar ajuste: " + e.getMessage());

            // También limpiar en caso de error de base de datos
            inicializarNuevoAjuste();
            SwingUtilities.invokeLater(() -> {
                vista.enfocarIdAjuste();
            });

            e.printStackTrace();
        }
    }

    // Obtener modelo de tabla para los detalles
    public DefaultTableModel getModeloTablaDetalles() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Código Barras", "Descripción", "Cant. Sistema", "Cant. Ajuste", "Diferencia", "Observaciones"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 5; // Solo cantidad ajuste y observaciones son editables
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: // Cant. Sistema
                    case 3: // Cant. Ajuste
                    case 4: // Diferencia
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        // Llenar modelo con detalles actuales
        for (mAjusteStock.DetalleAjuste detalle : ajusteActual.getDetalles()) {
            modelo.addRow(new Object[]{
                detalle.getCodBarra(),
                detalle.getDescripcionCompleta(),
                (int) detalle.getCantidadSistema(),
                (int) detalle.getCantidadAjuste(),
                (int) detalle.getDiferencia(),
                detalle.getObservaciones() != null ? detalle.getObservaciones() : ""
            });
        }

        return modelo;
    }

    // Obtener ajuste actual
    public mAjusteStock getAjusteActual() {
        return ajusteActual;
    }

    // Formatear número
    public String formatearNumero(double numero) {
        return formatoNumero.format(numero);
    }

    // Limpiar completamente después de operaciones exitosas
    private void limpiarDespuesDeOperacion() {
        // Crear nuevo ajuste vacío
        ajusteActual = new mAjusteStock();
        ajusteActual.setUsuarioId(1); // Temporal - obtener del login
        ajusteActual.setFecha(new Date());

        // Limpiar vista
        vista.limpiarFormulario();
        vista.actualizarTablaDetalles();
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        guardarAjuste();
    }

    @Override
    public void imFiltrar() {
        // Implementar filtros si es necesario
        vista.mostrarMensaje("Función de filtros no implementada aún.");
    }

    @Override
    public void imActualizar() {
        vista.actualizarTablaDetalles();
    }

    @Override
    public void imBorrar() {
        if (ajusteActual.getIdAjuste() > 0) {
            try {
                // Validar que el ajuste existe
                if (!modelo.existeAjuste(ajusteActual.getIdAjuste())) {
                    vista.mostrarError("El ajuste no existe en la base de datos.");
                    return;
                }

                // Validar que el ajuste no esté aprobado
                if (modelo.estaAprobado(ajusteActual.getIdAjuste())) {
                    vista.mostrarError("No se puede eliminar un ajuste aprobado.");
                    return;
                }

                int opcion = JOptionPane.showConfirmDialog(
                        vista,
                        "¿Está seguro que desea eliminar este ajuste?\n"
                        + "ID: " + ajusteActual.getIdAjuste() + "\n"
                        + "Esta acción no se puede deshacer.",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    boolean eliminado = modelo.eliminarAjuste(ajusteActual.getIdAjuste());
                    if (eliminado) {
                        vista.mostrarMensaje("Ajuste eliminado exitosamente.");
                        // Limpiar después de eliminar
                        inicializarNuevoAjuste();
                    } else {
                        vista.mostrarError("Error al eliminar el ajuste.");
                    }
                }
            } catch (SQLException e) {
                vista.mostrarError("Error al eliminar ajuste: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            vista.mostrarError("No hay ajuste seleccionado para eliminar.");
        }
    }

    @Override
    public void imNuevo() {
        inicializarNuevoAjuste();
    }

    @Override
    public void imBuscar() {
        try {
            // Buscar el Frame padre de la vista
            java.awt.Window window = SwingUtilities.getWindowAncestor(vista);
            java.awt.Frame parentFrame = null;

            if (window instanceof java.awt.Frame) {
                parentFrame = (java.awt.Frame) window;
            } else {
                // Si no encuentra Frame, usar null (permitido en algunos casos)
                parentFrame = null;
            }

            vBusqueda ventanaBusqueda = new vBusqueda(parentFrame, true, this);
            ventanaBusqueda.setVisible(true);

        } catch (Exception e) {
            vista.mostrarError("Error al abrir ventana de búsqueda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void imPrimero() {
        try {
            mAjusteStock ajuste = modelo.obtenerPrimerAjuste();
            cargarAjusteEnVista(ajuste);
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener primer ajuste: " + e.getMessage());
        }
    }

    @Override
    public void imSiguiente() {
        try {
            if (ajusteActual.getIdAjuste() > 0) {
                mAjusteStock ajuste = modelo.obtenerSiguienteAjuste(ajusteActual.getIdAjuste());
                cargarAjusteEnVista(ajuste);
            } else {
                vista.mostrarError("No hay ajuste actual para navegar.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener siguiente ajuste: " + e.getMessage());
        }
    }

    @Override
    public void imAnterior() {
        try {
            if (ajusteActual.getIdAjuste() > 0) {
                mAjusteStock ajuste = modelo.obtenerAnteriorAjuste(ajusteActual.getIdAjuste());
                cargarAjusteEnVista(ajuste);
            } else {
                vista.mostrarError("No hay ajuste actual para navegar.");
            }
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener anterior ajuste: " + e.getMessage());
        }
    }

    @Override
    public void imUltimo() {
        try {
            mAjusteStock ajuste = modelo.obtenerUltimoAjuste();
            cargarAjusteEnVista(ajuste);
        } catch (SQLException e) {
            vista.mostrarError("Error al obtener último ajuste: " + e.getMessage());
        }
    }

    @Override
    public void imImprimir() {
        vista.mostrarMensaje("Función de impresión no implementada aún.");
    }

    @Override
    public void imInsDet() {
        try {
            if (ventanaSeleccionProducto == null || !ventanaSeleccionProducto.isVisible()) {
                // Buscar el Frame contenedor
                java.awt.Window window = SwingUtilities.getWindowAncestor(vista);
                java.awt.Frame parentFrame = null;

                if (window instanceof java.awt.Frame) {
                    parentFrame = (java.awt.Frame) window;
                }

                ventanaSeleccionProducto = new vSeleccionProductoAjuste(parentFrame, this);
            }
            ventanaSeleccionProducto.setVisible(true);
            ventanaSeleccionProducto.toFront();
        } catch (Exception e) {
            vista.mostrarError("Error al abrir selección de productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void imDelDet() {
        vista.eliminarDetalleSeleccionado();
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
        return "ajustes_stock_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_ajuste", "fecha", "observaciones"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        buscarAjustePorId(id);
    }
}
