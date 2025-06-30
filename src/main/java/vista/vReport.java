package vista;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import interfaces.myInterface;
import javax.swing.SwingUtilities;
import modelo.service.ReporteService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.view.save.JRPdfSaveContributor;
import net.sf.jasperreports.view.save.JRSingleSheetXlsSaveContributor;
import net.sf.jasperreports.view.save.JRDocxSaveContributor;
import net.sf.jasperreports.view.save.JRMultipleSheetsXlsSaveContributor;
import net.sf.jasperreports.view.save.JRRtfSaveContributor;
import net.sf.jasperreports.view.save.JRHtmlSaveContributor;
import net.sf.jasperreports.view.save.JRCsvSaveContributor;
import java.text.SimpleDateFormat;

public class vReport extends JInternalFrame implements myInterface {

    private String reporteNombre;
    private String filtroVista;
    private JPanel jpReporte;
    private ReporteService reporteService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor para el visor de reportes
     *
     * @param reporteNombre Nombre del archivo de reporte sin extensión
     * @param filtroVista Nombre del filtro a utilizar (opcional)
     */
    public vReport(String reporteNombre, String filtroVista) {
        super("Reporte: " + reporteNombre, true, true, true, true);
        this.reporteNombre = reporteNombre;
        this.filtroVista = filtroVista;

        // Inicializar el panel de reporte con layout BorderLayout
        jpReporte = new JPanel(new BorderLayout());
        getContentPane().add(jpReporte, BorderLayout.CENTER);

        // Intentar inicializar el servicio de reportes
        inicializarServicioReportes();

        // Ajustar el tamaño
        addNotify();
        ajustarTamano();
    }

    private void ajustarTamano() {
        try {
            // Verificar si hay un contenedor padre (Desktop Pane)
            if (getParent() != null) {
                // Obtener las dimensiones del contenedor padre
                java.awt.Dimension parentSize = getParent().getSize();

                // Calcular el 95% del ancho y alto del padre
                int width = (int) (parentSize.width * 0.95);
                int height = (int) (parentSize.height * 0.95);

                // Establecer el tamaño preferido
                setPreferredSize(new java.awt.Dimension(width, height));
                setSize(width, height);

                // Centrar la ventana en el contenedor padre
                setLocation((parentSize.width - width) / 2, (parentSize.height - height) / 2);

                System.out.println("Ventana de reporte ajustada a " + width + "x" + height);
            } else {
                // Si no hay padre, usar un tamaño por defecto
                setPreferredSize(new java.awt.Dimension(1024, 768));
                setSize(1024, 768);
                System.out.println("Usando tamaño por defecto para ventana de reporte: 1024x768");
            }
        } catch (Exception e) {
            // En caso de error, usar un tamaño por defecto
            setPreferredSize(new java.awt.Dimension(1024, 768));
            setSize(1024, 768);
            System.err.println("Error al ajustar tamaño de ventana: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para ajustar el tamaño después de que la ventana ha sido agregada
     * al contenedor padre y se ha hecho visible
     */
    public void ajustarDespuesDeAgregar() {
        SwingUtilities.invokeLater(() -> {
            ajustarTamano();
            revalidate();
            repaint();
        });
    }

    /**
     * Método para inicializar el servicio de reportes
     */
    private void inicializarServicioReportes() {
        try {
            this.reporteService = new ReporteService();
        } catch (SQLException ex) {
            ex.printStackTrace(); // Importante para depuración
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar el servicio de reportes: " + ex.getMessage()
                    + "\nIntente nuevamente o contacte al administrador.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera y muestra el reporte aplicando los filtros necesarios
     */
    public void generarReporte() {
        // Verificar si el servicio de reportes está inicializado
        if (reporteService == null) {
            // Intentar inicializar de nuevo
            inicializarServicioReportes();

            // Si sigue siendo null, mostrar error y salir
            if (reporteService == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo inicializar el servicio de reportes. Por favor, intente nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Mapa para almacenar los parámetros del reporte
        Map<String, Object> parametros = new LinkedHashMap<>();

        // Añadir parámetros básicos por defecto
        parametros.put("REPORT_TITLE", "Reporte de " + reporteNombre);
        parametros.put("FECHA_GENERACION", new Date());

        // Si hay un filtro definido, mostrar diálogo para configurar parámetros
        if (filtroVista != null && !filtroVista.isEmpty()) {
            boolean parametrosConfigurados = configurarParametrosFiltro(parametros);
            if (!parametrosConfigurados) {
                // Si el usuario canceló la configuración de parámetros, no continuar
                return;
            }
        }

        try {
            // Utilizar el servicio de reportes para generar el JasperPrint
            JasperPrint jasperPrint = reporteService.generarJasperPrint(reporteNombre, parametros);

            if (jasperPrint != null) {
                // Crear un visor mejorado con opciones de exportación
                JRViewer visorReporte = crearVisorMejorado(jasperPrint);

                // Actualizar el panel con el visor de reportes
                jpReporte.removeAll();
                jpReporte.add(visorReporte, BorderLayout.CENTER);
                jpReporte.revalidate();
                jpReporte.repaint();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo generar el reporte.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el reporte: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Crea un visor de JasperReports mejorado con opciones de exportación
     *
     * @param jasperPrint El reporte generado
     * @return JRViewer configurado con opciones de exportación
     */
    private JRViewer crearVisorMejorado(JasperPrint jasperPrint) {
        JRViewer viewer = new JRViewer(jasperPrint);

        // Agregar más formatos de exportación al visor
        try {
            // Obtener el objeto JRSaveContributor del visor
            java.lang.reflect.Field field = JRViewer.class.getDeclaredField("saveContributors");
            field.setAccessible(true);
            Object[] saveContributors = (Object[]) field.get(viewer);

            // Crear un nuevo array con más contribuidores
            Object[] newContributors = new Object[7];

            // El constructor correcto recibe un Locale, no un JasperPrint
            java.util.Locale locale = java.util.Locale.getDefault();

            // Agregar contribuidores para diferentes formatos
            newContributors[0] = new JRPdfSaveContributor(locale, null);           // PDF
            newContributors[1] = new JRSingleSheetXlsSaveContributor(locale, null); // Excel (hoja única)
            newContributors[2] = new JRMultipleSheetsXlsSaveContributor(locale, null); // Excel (múltiples hojas)
            newContributors[3] = new JRDocxSaveContributor(locale, null);          // Word
            newContributors[4] = new JRRtfSaveContributor(locale, null);           // RTF
            newContributors[5] = new JRHtmlSaveContributor(locale, null);          // HTML
            newContributors[6] = new JRCsvSaveContributor(locale, null);           // CSV

            // Establecer los nuevos contribuidores
            field.set(viewer, newContributors);
        } catch (Exception e) {
            System.err.println("No se pudieron agregar formatos de exportación: " + e.getMessage());
        }

        return viewer;
    }

    /**
     * Configura los parámetros específicos según el filtro seleccionado
     *
     * @param parametros Mapa de parámetros a configurar
     * @return true si los parámetros fueron configurados, false si se canceló
     */
    private boolean configurarParametrosFiltro(Map<String, Object> parametros) {
        switch (filtroVista) {
            case "filtroInventario":
                // Filtro para inventario de productos
                vFiltroInventario filtroInv = new vFiltroInventario(null, true);
                filtroInv.setVisible(true);

                if (filtroInv.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de inventario
                    parametros.put("categoria_id", filtroInv.getCategoriaId());
                    parametros.put("marca_id", filtroInv.getMarcaId());
                    parametros.put("mostrar_inactivos", filtroInv.getMostrarInactivos());
                    parametros.put("stock_minimo", filtroInv.getStockMinimo());
                    parametros.put("stock_maximo", filtroInv.getStockMaximo());
                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroCompras":
                // Filtro para reporte de compras
                vFiltroCompras filtroComp = new vFiltroCompras(null, true);
                filtroComp.setVisible(true);

                if (filtroComp.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de compras
                    parametros.put("fecha_desde", filtroComp.getFechaDesde());
                    parametros.put("fecha_hasta", filtroComp.getFechaHasta());
                    parametros.put("proveedor_id", filtroComp.getProveedorId());
                    parametros.put("tipo_documento", filtroComp.getTipoDocumento());
                    parametros.put("condicion", filtroComp.getCondicion());
                    parametros.put("numero_documento", filtroComp.getNumeroDocumento());
                    parametros.put("timbrado", filtroComp.getTimbrado());
                    parametros.put("incluir_anulados", filtroComp.getIncluirAnulados());

                    // Parámetros adicionales para mostrar en el reporte - CORREGIDO
                    parametros.put("PROVEEDOR_FILTRO", filtroComp.getProveedorTexto());

                    // Manejar fechas null - CORREGIDO
                    if (filtroComp.getFechaDesde() != null && filtroComp.getFechaHasta() != null) {
                        parametros.put("FECHA_FILTRO", sdf.format(filtroComp.getFechaDesde()) + " - " + sdf.format(filtroComp.getFechaHasta()));
                    } else {
                        parametros.put("FECHA_FILTRO", "Todas las fechas");
                    }

                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroVentas":
                // Filtro para reporte de ventas
                vFiltroVentas filtroVent = new vFiltroVentas(null, true);
                filtroVent.setVisible(true);

                if (filtroVent.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de ventas
                    parametros.put("fecha_desde", filtroVent.getFechaDesde());
                    parametros.put("fecha_hasta", filtroVent.getFechaHasta());
                    parametros.put("cliente_id", filtroVent.getClienteId());
                    parametros.put("usuario_id", filtroVent.getUsuarioId());
                    parametros.put("tipo_venta", filtroVent.getTipoVenta());
                    parametros.put("estado", filtroVent.getEstado());
                    parametros.put("numero_factura", filtroVent.getNumeroFactura());
                    parametros.put("incluir_anulados", filtroVent.getIncluirAnulados());

                    // Parámetros adicionales para mostrar en el reporte - CORREGIDO
                    parametros.put("CLIENTE_FILTRO", filtroVent.getClienteTexto());
                    parametros.put("USUARIO_FILTRO", filtroVent.getUsuarioTexto());

                    // Manejar fechas null - CORREGIDO
                    if (filtroVent.getFechaDesde() != null && filtroVent.getFechaHasta() != null) {
                        parametros.put("FECHA_FILTRO", sdf.format(filtroVent.getFechaDesde()) + " - " + sdf.format(filtroVent.getFechaHasta()));
                    } else {
                        parametros.put("FECHA_FILTRO", "Todas las fechas");
                    }

                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroIngresosEgresos":
                // Filtro para reporte de ingresos-egresos
                vFiltroIngresosEgresos filtroIE = new vFiltroIngresosEgresos(null, true);
                filtroIE.setVisible(true);

                if (filtroIE.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de ingresos-egresos
                    parametros.put("fecha_desde", filtroIE.getFechaDesde());
                    parametros.put("fecha_hasta", filtroIE.getFechaHasta());
                    parametros.put("usuario_id", filtroIE.getUsuarioId());
                    parametros.put("tipo_movimiento", filtroIE.getTipoMovimiento());
                    parametros.put("incluir_anulados", filtroIE.getIncluirAnulados());

                    // Parámetros adicionales para mostrar en el reporte
                    parametros.put("USUARIO_FILTRO", filtroIE.getUsuarioTexto());
                    parametros.put("TIPO_FILTRO", filtroIE.getTipoMovimiento());

                    // Manejar fechas null
                    if (filtroIE.getFechaDesde() != null && filtroIE.getFechaHasta() != null) {
                        parametros.put("FECHA_FILTRO", sdf.format(filtroIE.getFechaDesde()) + " - " + sdf.format(filtroIE.getFechaHasta()));
                    } else {
                        parametros.put("FECHA_FILTRO", "Todas las fechas");
                    }

                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroProductosMasVendidos":
                // Filtro para reporte de productos más vendidos
                vFiltroProductosMasVendidos filtroProds = new vFiltroProductosMasVendidos(null, true);
                filtroProds.setVisible(true);

                if (filtroProds.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de productos más vendidos
                    parametros.put("fecha_desde", filtroProds.getFechaDesde());
                    parametros.put("fecha_hasta", filtroProds.getFechaHasta());
                    parametros.put("limite_productos", filtroProds.getLimiteProductos());
                    parametros.put("tipo_ordenamiento", filtroProds.getTipoOrdenamiento());
                    parametros.put("incluir_anulados", filtroProds.getIncluirAnulados());

                    // Parámetros adicionales para mostrar en el reporte
                    parametros.put("FECHA_FILTRO", filtroProds.getFechaTexto());
                    parametros.put("ORDENAMIENTO_FILTRO", filtroProds.getTipoOrdenamiento());
                    parametros.put("LIMITE_FILTRO", String.valueOf(filtroProds.getLimiteProductos()));

                    return true;
                } else {
                    return false; // Usuario canceló
                }

            default:
                // Si no hay un filtro específico, solo usar los parámetros por defecto
                return true;
        }
    }

    // Implementación de los métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        // No aplicable para reportes
    }

    @Override
    public void imFiltrar() {
        // Este método se llama desde la interfaz para filtrar/regenerar el reporte
        generarReporte();
    }

    @Override
    public void imActualizar() {
        // Actualizar el reporte con los últimos datos
        generarReporte();
    }

    @Override
    public void imBorrar() {
        // No aplicable para reportes
    }

    @Override
    public void imNuevo() {
        // No aplicable para reportes
    }

    @Override
    public void imBuscar() {
        // No aplicable para reportes
    }

    @Override
    public void imPrimero() {
        // No aplicable para reportes
    }

    @Override
    public void imSiguiente() {
        // No aplicable para reportes
    }

    @Override
    public void imAnterior() {
        // No aplicable para reportes
    }

    @Override
    public void imUltimo() {
        // No aplicable para reportes
    }

    @Override
    public void imImprimir() {
        try {
            // Utilizar el servicio de reportes para imprimir directamente
            reporteService.imprimirReporte(reporteNombre);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el diálogo de filtro correspondiente al reporte actual y regenera el
     * reporte con los nuevos parámetros si el usuario acepta.
     */
    public void abrirDialogoFiltro() {
        // Verificar si el servicio de reportes está inicializado
        if (reporteService == null) {
            // Intentar inicializar de nuevo
            inicializarServicioReportes();

            // Si sigue siendo null, mostrar error y salir
            if (reporteService == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo inicializar el servicio de reportes. Por favor, intente nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Mapa para almacenar los parámetros del reporte
        Map<String, Object> parametros = new LinkedHashMap<>();

        // Añadir parámetros básicos por defecto
        parametros.put("REPORT_TITLE", "Reporte de " + reporteNombre);
        parametros.put("FECHA_GENERACION", new Date());

        // Si hay un filtro definido, mostrar diálogo para configurar parámetros
        if (filtroVista != null && !filtroVista.isEmpty()) {
            boolean parametrosConfigurados = configurarParametrosFiltro(parametros);
            if (!parametrosConfigurados) {
                // Si el usuario canceló la configuración de parámetros, no continuar
                return;
            }

            try {
                // Utilizar el servicio de reportes para generar el JasperPrint con los nuevos parámetros
                JasperPrint jasperPrint = reporteService.generarJasperPrint(reporteNombre, parametros);

                if (jasperPrint != null) {
                    // Crear un visor mejorado con opciones de exportación
                    JRViewer visorReporte = crearVisorMejorado(jasperPrint);

                    // Actualizar el panel con el visor de reportes
                    jpReporte.removeAll();
                    jpReporte.add(visorReporte, BorderLayout.CENTER);
                    jpReporte.revalidate();
                    jpReporte.repaint();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo generar el reporte.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al generar el reporte: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay filtros disponibles para este reporte",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Configura el reporte con parámetros específicos sin mostrar diálogo
     */
    public void configurarReporteConParametros(String nombreReporte, Map<String, Object> parametros) {
        this.reporteNombre = nombreReporte;

        // Verificar si el servicio de reportes está inicializado
        if (reporteService == null) {
            inicializarServicioReportes();
            if (reporteService == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo inicializar el servicio de reportes.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Añadir parámetros básicos por defecto
        parametros.put("REPORT_TITLE", "Ticket de Venta");
        parametros.put("FECHA_GENERACION", new Date());

        try {
            // Generar el reporte
            JasperPrint jasperPrint = reporteService.generarJasperPrint(nombreReporte, parametros);

            if (jasperPrint != null) {
                // Crear visor
                JRViewer visorReporte = crearVisorMejorado(jasperPrint);

                // Actualizar el panel
                jpReporte.removeAll();
                jpReporte.add(visorReporte, BorderLayout.CENTER);
                jpReporte.revalidate();
                jpReporte.repaint();

                // Actualizar título de la ventana
                setTitle("Ticket de Venta");
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo generar el ticket.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el ticket: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void imInsDet() {
        // No aplicable para reportes
    }

    @Override
    public void imDelDet() {
        // No aplicable para reportes
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
        return "";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[0];
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // No aplicable para reportes
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpReporte1 = new javax.swing.JPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Reporte");

        javax.swing.GroupLayout jpReporte1Layout = new javax.swing.GroupLayout(jpReporte1);
        jpReporte1.setLayout(jpReporte1Layout);
        jpReporte1Layout.setHorizontalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1067, Short.MAX_VALUE)
        );
        jpReporte1Layout.setVerticalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 757, Short.MAX_VALUE)
        );

        getContentPane().add(jpReporte1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jpReporte1;
    // End of variables declaration//GEN-END:variables
}
