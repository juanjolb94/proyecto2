 package controlador;

import interfaces.myInterface;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.PrecioCabeceraDAO;
import modelo.PrecioDetalleDAO;
import modelo.mPrecioCabecera;
import modelo.mPrecioDetalle;
import vista.vDetalleProductoPrecio;
import vista.vListaPrecios;

public class cListaPrecios implements myInterface {

    private vListaPrecios vista;
    private PrecioCabeceraDAO cabeceraDAO;
    private PrecioDetalleDAO detalleDAO;
    private mPrecioCabecera cabeceraActual;

    public cListaPrecios(vListaPrecios vista) {
        this.vista = vista;
        try {
            this.cabeceraDAO = new PrecioCabeceraDAO();
            this.detalleDAO = new PrecioDetalleDAO();
            this.cabeceraActual = new mPrecioCabecera();
        } catch (Exception e) {
            mostrarError("Error al inicializar el controlador: " + e.getMessage());
        }
    }

    /**
     * Busca una lista de precios por su ID
     */
    public void buscarPrecioPorId(int id) {
        try {
            if (id <= 0) {
                limpiarFormulario();
                return;
            }

            cabeceraActual = cabeceraDAO.obtenerPrecioPorId(id);

            if (cabeceraActual != null) {
                // Cargar datos en la vista
                vista.setDatosCabecera(
                        cabeceraActual.getId(),
                        cabeceraActual.getNombre(),
                        cabeceraActual.getFechaCreacion(),
                        cabeceraActual.getMoneda(),
                        cabeceraActual.isActivo(),
                        cabeceraActual.getObservaciones()
                );

                // Cargar detalles en la tabla
                cargarDetallesEnTabla(id);
            } else {
                mostrarError("No se encontró la lista de precios con el ID: " + id);
                limpiarFormulario();
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar la lista de precios: " + e.getMessage());
        }
    }

    /**
     * Guarda o actualiza una lista de precios
     */
    public void guardarPrecio(int id, String nombre, Date fechaCreacion, String moneda, boolean activo, String observaciones) {
        try {
            // Validar datos
            if (nombre == null || nombre.trim().isEmpty()) {
                mostrarError("El nombre de la lista de precios es obligatorio");
                return;
            }

            if (moneda == null || moneda.trim().isEmpty()) {
                mostrarError("La moneda es obligatoria");
                return;
            }

            // Crear o actualizar el objeto de cabecera
            if (id <= 0) {
                // Nuevo registro
                cabeceraActual = new mPrecioCabecera();
                cabeceraActual.setFechaCreacion(fechaCreacion != null ? fechaCreacion : new Date());
            } else {
                // Verificar que exista
                cabeceraActual = cabeceraDAO.obtenerPrecioPorId(id);
                if (cabeceraActual == null) {
                    mostrarError("No se encontró la lista de precios con ID: " + id);
                    return;
                }
            }

            // Establecer los datos
            cabeceraActual.setNombre(nombre);
            cabeceraActual.setMoneda(moneda);
            cabeceraActual.setActivo(activo);
            cabeceraActual.setObservaciones(observaciones);

            // Guardar en la base de datos
            if (id <= 0) {
                // Insertar nuevo
                int nuevoId = cabeceraDAO.insertarPrecio(cabeceraActual);
                if (nuevoId > 0) {
                    cabeceraActual.setId(nuevoId);
                    mostrarMensaje("Lista de precios creada correctamente con ID: " + nuevoId);
                    vista.setIdCabecera(nuevoId);
                } else {
                    mostrarError("No se pudo crear la lista de precios");
                }
            } else {
                // Actualizar existente
                boolean exito = cabeceraDAO.actualizarPrecio(cabeceraActual);
                if (exito) {
                    mostrarMensaje("Lista de precios actualizada correctamente");
                } else {
                    mostrarError("No se pudo actualizar la lista de precios");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar la lista de precios: " + e.getMessage());
        }
    }

    /**
     * Elimina una lista de precios (la desactiva)
     */
    public void eliminarPrecio(int id) {
        try {
            if (id <= 0) {
                mostrarError("Debe seleccionar una lista de precios para eliminar");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    vista,
                    "¿Está seguro de eliminar esta lista de precios? Se desactivará la lista y todos sus detalles.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean exito = cabeceraDAO.eliminarPrecio(id);
                if (exito) {
                    mostrarMensaje("Lista de precios eliminada correctamente");
                    limpiarFormulario();
                } else {
                    mostrarError("No se pudo eliminar la lista de precios");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al eliminar la lista de precios: " + e.getMessage());
        }
    }

    /**
     * Carga los detalles de una lista de precios en la tabla
     */
    public void cargarDetallesEnTabla(int idPrecioCabecera) {
        try {
            List<mPrecioDetalle> detalles = detalleDAO.obtenerDetallesPorPrecioId(idPrecioCabecera);
            DefaultTableModel modelo = vista.getModeloTabla();
            modelo.setRowCount(0);

            for (mPrecioDetalle detalle : detalles) {
                Object[] fila = {
                    detalle.getId(),
                    detalle.getCodigoBarra(),
                    detalle.getNombreProducto(),
                    detalle.getPrecio(),
                    detalle.getFechaVigencia(),
                    detalle.isActivo()
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar detalles: " + e.getMessage());
        }
    }

    /**
     * Abre el diálogo para agregar un nuevo detalle de precio
     */
    public void agregarDetalle() {
        if (cabeceraActual == null || cabeceraActual.getId() <= 0) {
            mostrarError("Debe guardar la lista de precios antes de agregar detalles");
            return;
        }

        try {
            // Abrir diálogo para selección de producto y precio
            vDetalleProductoPrecio dialogo = new vDetalleProductoPrecio(null, true);
            dialogo.configurarParaInsercion(cabeceraActual.getId());
            dialogo.setVisible(true);

            if (dialogo.isAceptado()) {
                mPrecioDetalle detalle = dialogo.getDetalle();

                // Verificar si ya existe el producto en la lista
                if (detalleDAO.existeProductoEnPrecio(cabeceraActual.getId(), detalle.getCodigoBarra())) {
                    int confirmacion = JOptionPane.showConfirmDialog(
                            vista,
                            "Este producto ya existe en la lista de precios. ¿Desea actualizarlo?",
                            "Producto existente",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirmacion != JOptionPane.YES_OPTION) {
                        return;
                    }

                    // Obtener el detalle existente y actualizarlo
                    mPrecioDetalle detalleExistente = detalleDAO.obtenerDetallePorCodigoBarra(
                            cabeceraActual.getId(), detalle.getCodigoBarra());

                    if (detalleExistente != null) {
                        detalleExistente.setPrecio(detalle.getPrecio());
                        detalleExistente.setFechaVigencia(detalle.getFechaVigencia());
                        detalleExistente.setActivo(detalle.isActivo());

                        boolean exito = detalleDAO.actualizarDetalle(detalleExistente);
                        if (exito) {
                            mostrarMensaje("Detalle de precio actualizado correctamente");
                            cargarDetallesEnTabla(cabeceraActual.getId());
                        } else {
                            mostrarError("No se pudo actualizar el detalle de precio");
                        }

                        return;
                    }
                }

                // Insertar nuevo detalle
                detalle.setIdPrecioCabecera(cabeceraActual.getId());
                int nuevoId = detalleDAO.insertarDetalle(detalle);

                if (nuevoId > 0) {
                    mostrarMensaje("Detalle de precio agregado correctamente");
                    cargarDetallesEnTabla(cabeceraActual.getId());
                } else {
                    mostrarError("No se pudo agregar el detalle de precio");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al agregar detalle: " + e.getMessage());
        }
    }

    /**
     * Abre el diálogo para editar un detalle de precio existente
     */
    public void editarDetalle(int filaSeleccionada) {
        if (cabeceraActual == null || cabeceraActual.getId() <= 0) {
            mostrarError("No hay una lista de precios activa");
            return;
        }

        if (filaSeleccionada < 0) {
            mostrarError("Seleccione un detalle para editar");
            return;
        }

        try {
            DefaultTableModel modelo = vista.getModeloTabla();
            int idDetalle = (int) modelo.getValueAt(filaSeleccionada, 0);
            mPrecioDetalle detalle = detalleDAO.obtenerDetallePorId(idDetalle);

            if (detalle != null) {
                vDetalleProductoPrecio dialogo = new vDetalleProductoPrecio(null, true);
                dialogo.configurarParaEdicion(detalle);
                dialogo.setVisible(true);

                if (dialogo.isAceptado()) {
                    detalle = dialogo.getDetalle();
                    boolean exito = detalleDAO.actualizarDetalle(detalle);

                    if (exito) {
                        mostrarMensaje("Detalle de precio actualizado correctamente");
                        cargarDetallesEnTabla(cabeceraActual.getId());
                    } else {
                        mostrarError("No se pudo actualizar el detalle de precio");
                    }
                }
            } else {
                mostrarError("No se encontró el detalle de precio");
            }
        } catch (SQLException e) {
            mostrarError("Error al editar detalle: " + e.getMessage());
        }
    }

    /**
     * Elimina un detalle de precio
     */
    public void eliminarDetalle(int filaSeleccionada) {
        if (cabeceraActual == null || cabeceraActual.getId() <= 0) {
            mostrarError("No hay una lista de precios activa");
            return;
        }

        if (filaSeleccionada < 0) {
            mostrarError("Seleccione un detalle para eliminar");
            return;
        }

        try {
            DefaultTableModel modelo = vista.getModeloTabla();
            int idDetalle = (int) modelo.getValueAt(filaSeleccionada, 0);

            int confirmacion = JOptionPane.showConfirmDialog(
                    vista,
                    "¿Está seguro de eliminar este detalle de precio?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean exito = detalleDAO.eliminarDetalle(idDetalle);

                if (exito) {
                    mostrarMensaje("Detalle de precio eliminado correctamente");
                    cargarDetallesEnTabla(cabeceraActual.getId());
                } else {
                    mostrarError("No se pudo eliminar el detalle de precio");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al eliminar detalle: " + e.getMessage());
        }
    }

    /**
     * Desactiva un detalle de precio
     */
    public void desactivarDetalle(int filaSeleccionada) {
        if (cabeceraActual == null || cabeceraActual.getId() <= 0) {
            mostrarError("No hay una lista de precios activa");
            return;
        }

        if (filaSeleccionada < 0) {
            mostrarError("Seleccione un detalle para desactivar");
            return;
        }

        try {
            DefaultTableModel modelo = vista.getModeloTabla();
            int idDetalle = (int) modelo.getValueAt(filaSeleccionada, 0);

            boolean exito = detalleDAO.desactivarDetalle(idDetalle);

            if (exito) {
                mostrarMensaje("Detalle de precio desactivado correctamente");
                cargarDetallesEnTabla(cabeceraActual.getId());
            } else {
                mostrarError("No se pudo desactivar el detalle de precio");
            }
        } catch (SQLException e) {
            mostrarError("Error al desactivar detalle: " + e.getMessage());
        }
    }

    /**
     * Limpia el formulario
     */
    public void limpiarFormulario() {
        vista.limpiarFormulario();
        DefaultTableModel modelo = vista.getModeloTabla();
        if (modelo != null) {
            modelo.setRowCount(0);
        }
        cabeceraActual = new mPrecioCabecera();
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje informativo
     */
    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Implementación de métodos de navegación requeridos por la interfaz myInterface
    @Override
    public void imGrabar() {
        int id = vista.getIdCabecera();
        String nombre = vista.getNombreCabecera();
        Date fechaCreacion = vista.getFechaCreacionCabecera();
        String moneda = vista.getMonedaCabecera();
        boolean activo = vista.isActivoCabecera();
        String observaciones = vista.getObservacionesCabecera();

        guardarPrecio(id, nombre, fechaCreacion, moneda, activo, observaciones);
    }

    @Override
    public void imBorrar() {
        int id = vista.getIdCabecera();
        eliminarPrecio(id);
    }

    @Override
    public void imNuevo() {
        limpiarFormulario();
    }

    @Override
    public void imBuscar() {
        String idStr = JOptionPane.showInputDialog(vista, "Ingrese el ID de la lista de precios:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                buscarPrecioPorId(id);
            } catch (NumberFormatException e) {
                mostrarError("El ID debe ser un número entero válido");
            }
        }
    }

    @Override
    public void imPrimero() {
        try {
            mPrecioCabecera primera = cabeceraDAO.obtenerPrimerPrecio();
            if (primera != null) {
                buscarPrecioPorId(primera.getId());
            } else {
                mostrarMensaje("No hay listas de precios registradas");
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar la primera lista de precios: " + e.getMessage());
        }
    }

    @Override
    public void imSiguiente() {
        try {
            int idActual = vista.getIdCabecera();
            if (idActual > 0) {
                mPrecioCabecera siguiente = cabeceraDAO.obtenerSiguientePrecio(idActual);
                if (siguiente != null) {
                    buscarPrecioPorId(siguiente.getId());
                } else {
                    mostrarMensaje("No hay más listas de precios siguientes");
                }
            } else {
                imPrimero();
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar la siguiente lista de precios: " + e.getMessage());
        }
    }

    @Override
    public void imAnterior() {
        try {
            int idActual = vista.getIdCabecera();
            if (idActual > 0) {
                mPrecioCabecera anterior = cabeceraDAO.obtenerAnteriorPrecio(idActual);
                if (anterior != null) {
                    buscarPrecioPorId(anterior.getId());
                } else {
                    mostrarMensaje("No hay más listas de precios anteriores");
                }
            } else {
                imPrimero();
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar la anterior lista de precios: " + e.getMessage());
        }
    }

    @Override
    public void imUltimo() {
        try {
            mPrecioCabecera ultima = cabeceraDAO.obtenerUltimoPrecio();
            if (ultima != null) {
                buscarPrecioPorId(ultima.getId());
            } else {
                mostrarMensaje("No hay listas de precios registradas");
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar la última lista de precios: " + e.getMessage());
        }
    }

    @Override
    public void imInsDet() {
        agregarDetalle();
    }

    @Override
    public void imDelDet() {
        int filaSeleccionada = vista.getFilaSeleccionada();
        eliminarDetalle(filaSeleccionada);
    }

    @Override
    public void imFiltrar() {
        // No implementado en este controlador
    }

    @Override
    public void imActualizar() {
        int id = vista.getIdCabecera();
        if (id > 0) {
            buscarPrecioPorId(id);
        }
    }

    @Override
    public void imImprimir() {
        // No implementado en este controlador
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
        return "precio_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "nombre", "moneda"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        buscarPrecioPorId(id);
    }
}
