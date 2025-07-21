package controlador;

import interfaces.myInterface;
import modelo.AprobacionAjusteDAO;
import modelo.mAprobacionAjuste;
import vista.vAprobacionAjuste;
import vista.vBusqueda;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class cAprobacionAjuste implements myInterface {

    private vAprobacionAjuste vista;
    private AprobacionAjusteDAO modelo;
    private DefaultTableModel modeloTabla;
    private SimpleDateFormat formatoFecha;
    private SimpleDateFormat formatoFechaCorta;

    public cAprobacionAjuste(vAprobacionAjuste vista) throws SQLException {
        this.vista = vista;
        this.modelo = new AprobacionAjusteDAO();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.formatoFechaCorta = new SimpleDateFormat("dd/MM/yyyy");

        inicializarModelo();
        inicializarEstadisticas();
    }

    // Inicializar modelo de tabla
    private void inicializarModelo() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Fecha", "Observaciones", "Cant. Items", "Estado", "Aprobado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la columna "Aprobado" es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // ID
                    case 3: // Cantidad Items
                        return Integer.class;
                    case 1: // Fecha
                        return String.class;
                    case 2: // Observaciones
                    case 4: // Estado
                        return String.class;
                    case 5: // Aprobado (checkbox)
                        return Boolean.class;
                    default:
                        return String.class;
                }
            }
        };
    }

    // Inicializar estadísticas vacías
    private void inicializarEstadisticas() {
        vista.actualizarEstadisticas(0, 0, 0);
    }

    // Cargar ajustes iniciales (últimos 30 días) 
    private void cargarAjustesIniciales() {
        try {
            // Cargar ajustes de los últimos 30 días por defecto
            Date fechaHasta = new Date();
            Date fechaDesde = new Date(fechaHasta.getTime() - (30L * 24 * 60 * 60 * 1000)); // 30 días atrás

            vista.setFechaDesde(fechaDesde);
            vista.setFechaHasta(fechaHasta);

            buscarAjustes();
            actualizarEstadisticas();

        } catch (Exception e) {
            vista.mostrarError("Error al cargar ajustes iniciales: " + e.getMessage());
        }
    }

    // Buscar ajustes con filtros modificado
    public void buscarAjustes() {
        try {
            Date fechaDesde = vista.getFechaDesde();
            Date fechaHasta = vista.getFechaHasta();
            Integer idAjuste = vista.getIdAjuste();

            // Si ID = 0, convertir a null para buscar todos
            if (idAjuste != null && idAjuste == 0) {
                idAjuste = null;
            }

            // Validar fechas
            if (fechaDesde != null && fechaHasta != null && fechaDesde.after(fechaHasta)) {
                vista.mostrarError("La fecha desde no puede ser mayor que la fecha hasta.");
                return;
            }

            List<mAprobacionAjuste> ajustes = modelo.buscarAjustesPorFiltros(fechaDesde, fechaHasta, idAjuste);
            actualizarTablaAjustes(ajustes);
            actualizarEstadisticas();

            if (ajustes.isEmpty()) {
                vista.mostrarMensaje("No se encontraron ajustes con los filtros especificados.");
            } else {
                // Obtener ID del filtro para personalizar mensaje
                Integer idBusqueda = vista.getIdAjuste();

                if (idBusqueda != null && idBusqueda > 0) {
                    // Búsqueda específica por ID - mensaje más informativo
                    mAprobacionAjuste ajuste = ajustes.get(0);
                    vista.mostrarMensaje(String.format("Ajuste ID %d cargado - Estado: %s",
                            ajuste.getIdAjuste(),
                            ajuste.isAprobado() ? "APROBADO" : "PENDIENTE"));
                } else {
                    // Búsqueda general - mostrar cantidad total
                    vista.mostrarMensaje(ajustes.size() + " ajuste(s) encontrado(s).");
                }
            }

        } catch (SQLException e) {
            vista.mostrarError("Error al buscar ajustes: " + e.getMessage());
        }
    }

    // Actualizar tabla con lista de ajustes
    private void actualizarTablaAjustes(List<mAprobacionAjuste> ajustes) {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Agregar ajustes a la tabla
        for (mAprobacionAjuste ajuste : ajustes) {
            modeloTabla.addRow(new Object[]{
                ajuste.getIdAjuste(),
                formatoFecha.format(ajuste.getFecha()),
                ajuste.getObservaciones() != null ? ajuste.getObservaciones() : "",
                ajuste.getCantidadDetalles(),
                ajuste.getEstadoTexto(),
                ajuste.isAprobado()
            });
        }

        vista.actualizarTabla();
    }

    // Cambiar estado de aprobación de un ajuste
    public void cambiarAprobacionAjuste(int fila, boolean aprobar) {
        try {
            if (fila < 0 || fila >= modeloTabla.getRowCount()) {
                vista.mostrarError("Fila no válida seleccionada.");
                return;
            }

            int idAjuste = (Integer) modeloTabla.getValueAt(fila, 0);
            String accion = aprobar ? "aprobar" : "desaprobar";

            // Confirmación del usuario
            int confirmacion = JOptionPane.showConfirmDialog(
                    vista,
                    String.format("¿Está seguro que desea %s el ajuste ID %d?\n\n"
                            + "%s el ajuste %s las cantidades en el stock.",
                            accion, idAjuste,
                            aprobar ? "Aprobar" : "Desaprobar",
                            aprobar ? "aplicará" : "revertirá"),
                    "Confirmar " + (aprobar ? "Aprobación" : "Desaprobación"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirmacion != JOptionPane.YES_OPTION) {
                // Revertir el checkbox si el usuario cancela
                modeloTabla.setValueAt(!aprobar, fila, 5);
                vista.actualizarTabla();
                return;
            }

            // Verificar que el ajuste tenga detalles
            if (!modelo.tieneDetalles(idAjuste)) {
                vista.mostrarError("El ajuste no tiene productos para procesar.");
                modeloTabla.setValueAt(!aprobar, fila, 5);
                vista.actualizarTabla();
                return;
            }

            // NUEVO: Obtener detalles del ajuste ANTES del cambio para el log
            List<Object[]> detallesAjuste = modelo.obtenerDetallesAjuste(idAjuste);

            // Ejecutar cambio en base de datos (esto dispara el trigger)
            boolean exito = modelo.cambiarAprobacionAjuste(idAjuste, aprobar);

            if (exito) {
                // Actualizar estado en la tabla
                modeloTabla.setValueAt(aprobar ? "Aprobado" : "Pendiente", fila, 4);

                vista.mostrarMensaje(String.format("Ajuste ID %d %s exitosamente.",
                        idAjuste,
                        aprobar ? "aprobado" : "desaprobado"));

                // NUEVO: Imprimir log detallado de los cambios realizados
                imprimirLogCambiosAjuste(idAjuste, aprobar, detallesAjuste);

                // Actualizar estadísticas
                actualizarEstadisticas();

            } else {
                vista.mostrarError("Error al cambiar el estado del ajuste.");
                // Revertir el checkbox
                modeloTabla.setValueAt(!aprobar, fila, 5);
            }

            vista.actualizarTabla();

        } catch (SQLException e) {
            vista.mostrarError("Error en base de datos: " + e.getMessage());
            // Revertir el checkbox en caso de error
            modeloTabla.setValueAt(!aprobar, fila, 5);
            vista.actualizarTabla();
        }
    }

    // Imprimir log detallado de cambios en el ajuste
    private void imprimirLogCambiosAjuste(int idAjuste, boolean aprobar, List<Object[]> detalles) {
        System.out.println("═".repeat(80));
        System.out.println(String.format("AJUSTE DE STOCK %s - ID: %d",
                aprobar ? "APROBADO" : "REVERTIDO", idAjuste));
        System.out.println("Fecha: " + new java.util.Date());
        System.out.println("─".repeat(80));

        if (detalles.isEmpty()) {
            System.out.println("No hay productos en este ajuste.");
            System.out.println("═".repeat(80));
            return;
        }

        System.out.println(String.format("%-15s %-25s %-12s %-12s %-12s",
                "Código", "Producto", "Cant.Sist.", "Cant.Ajust.", "Diferencia"));
        System.out.println("─".repeat(80));

        int totalProductos = 0;
        int totalDiferencia = 0;

        for (Object[] detalle : detalles) {
            String codigo = (String) detalle[0];
            String nombre = (String) detalle[1];
            String descripcion = (String) detalle[2];
            int cantidadSistema = (Integer) detalle[3];
            int cantidadAjuste = (Integer) detalle[4];
            int diferencia = (Integer) detalle[5];

            // Truncar nombre del producto si es muy largo
            String productoCompleto = nombre + " - " + descripcion;
            String productoCorto = productoCompleto.length() > 25
                    ? productoCompleto.substring(0, 22) + "..." : productoCompleto;

            System.out.println(String.format("%-15s %-25s %12d %12d %12d%s",
                    codigo,
                    productoCorto,
                    cantidadSistema,
                    cantidadAjuste,
                    diferencia,
                    diferencia != 0 ? " *" : ""));

            totalProductos++;
            totalDiferencia += Math.abs(diferencia);
        }

        System.out.println("─".repeat(80));
        System.out.println(String.format("RESUMEN: %d producto(s) procesado(s)", totalProductos));

        if (aprobar) {
            System.out.println("ACCIÓN: Stock actualizado a cantidades de ajuste");
            System.out.println("EFECTO: Las cantidades en stock ahora reflejan los valores ajustados");
        } else {
            System.out.println("ACCIÓN: Stock revertido a cantidades del sistema");
            System.out.println("EFECTO: Las cantidades en stock volvieron a los valores originales");
        }

        if (totalDiferencia > 0) {
            System.out.println(String.format("Total de unidades con diferencias: %d", totalDiferencia));
        }

        System.out.println("ESTADO: Cambios aplicados por trigger de base de datos");
        System.out.println("═".repeat(80));
    }

    // Actualizar estadísticas
    private void actualizarEstadisticas() {
        try {
            Date fechaDesde = vista.getFechaDesde();
            Date fechaHasta = vista.getFechaHasta();

            Object[] estadisticas = modelo.obtenerEstadisticasAjustes(fechaDesde, fechaHasta);

            int total = (Integer) estadisticas[0];
            int aprobados = (Integer) estadisticas[1];
            int pendientes = (Integer) estadisticas[2];

            vista.actualizarEstadisticas(total, aprobados, pendientes);

        } catch (SQLException e) {
            System.err.println("Error al actualizar estadísticas: " + e.getMessage());
        }
    }

    // CAMBIO: Limpiar filtros
    public void limpiarFiltros() {
        vista.limpiarFiltros();
        modeloTabla.setRowCount(0);
        vista.actualizarTabla();
        vista.actualizarEstadisticas(0, 0, 0);
    }

    // Mostrar detalles de un ajuste seleccionado
    public void mostrarDetallesAjuste(int filaSeleccionada) {
        try {
            if (filaSeleccionada < 0) {
                vista.mostrarError("Seleccione un ajuste para ver sus detalles.");
                return;
            }

            int idAjuste = (Integer) modeloTabla.getValueAt(filaSeleccionada, 0);
            List<Object[]> detalles = modelo.obtenerDetallesAjuste(idAjuste);

            if (detalles.isEmpty()) {
                vista.mostrarMensaje("El ajuste seleccionado no tiene productos.");
                return;
            }

            // Crear ventana de detalles
            StringBuilder sb = new StringBuilder();
            sb.append("DETALLES DEL AJUSTE ID: ").append(idAjuste).append("\n\n");
            sb.append(String.format("%-15s %-20s %-15s %-15s %-15s %-20s\n",
                    "Código", "Producto", "Cant. Sistema", "Cant. Ajuste", "Diferencia", "Observaciones"));
            sb.append("─".repeat(120)).append("\n");

            for (Object[] detalle : detalles) {
                sb.append(String.format("%-15s %-20s %15d %15d %15d %-20s\n",
                        detalle[0], // código
                        (detalle[1] + " - " + detalle[2]).substring(0, Math.min(20, (detalle[1] + " - " + detalle[2]).length())), // producto
                        detalle[3], // cantidad sistema
                        detalle[4], // cantidad ajuste
                        detalle[5], // diferencia
                        detalle[6] != null ? detalle[6].toString() : "" // observaciones
                ));
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, 400));

            JOptionPane.showMessageDialog(vista, scrollPane, "Detalles del Ajuste " + idAjuste, JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            vista.mostrarError("Error al obtener detalles: " + e.getMessage());
        }
    }

    // Exportar resultados a CSV (funcionalidad adicional)
    public void exportarCSV() {
        vista.mostrarMensaje("Funcionalidad de exportación no implementada aún.");
    }

    // Obtener modelo de tabla
    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        vista.mostrarMensaje("No hay datos para guardar en esta ventana.");
    }

    @Override
    public void imFiltrar() {
        buscarAjustes();
    }

    @Override
    public void imActualizar() {
        buscarAjustes();
        actualizarEstadisticas();
    }

    @Override
    public void imBorrar() {
        vista.mostrarMensaje("No se permite eliminar ajustes desde esta ventana.");
    }

    @Override
    public void imNuevo() {
        vista.mostrarMensaje("Use la ventana de Ajuste de Stock para crear nuevos ajustes.");
    }

    @Override
    public void imBuscar() {
        // Abrir ventana de búsqueda si fuera necesario
        vista.mostrarMensaje("Use los filtros superiores para buscar ajustes.");
    }

    @Override
    public void imPrimero() {
        if (modeloTabla.getRowCount() > 0) {
            vista.seleccionarFila(0);
        }
    }

    @Override
    public void imSiguiente() {
        int filaActual = vista.getFilaSeleccionada();
        if (filaActual >= 0 && filaActual < modeloTabla.getRowCount() - 1) {
            vista.seleccionarFila(filaActual + 1);
        }
    }

    @Override
    public void imAnterior() {
        int filaActual = vista.getFilaSeleccionada();
        if (filaActual > 0) {
            vista.seleccionarFila(filaActual - 1);
        }
    }

    @Override
    public void imUltimo() {
        if (modeloTabla.getRowCount() > 0) {
            vista.seleccionarFila(modeloTabla.getRowCount() - 1);
        }
    }

    @Override
    public void imImprimir() {
        vista.mostrarMensaje("Funcionalidad de impresión no implementada aún.");
    }

    @Override
    public void imInsDet() {
        vista.mostrarMensaje("No aplica para esta ventana.");
    }

    @Override
    public void imDelDet() {
        vista.mostrarMensaje("No aplica para esta ventana.");
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
        vista.setIdAjuste(id);
    }
}
