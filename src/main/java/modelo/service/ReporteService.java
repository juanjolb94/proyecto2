package modelo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.swing.JOptionPane;

import modelo.DatabaseConnection;
import modelo.ProductosDAO;
import modelo.mProducto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.swing.JRViewer;

public class ReporteService {

    private final ProductosDAO productosDAO;
    private static final String REPORTES_DIR = "src/main/resources/reportes/";

    public ReporteService() throws SQLException {
        this.productosDAO = new ProductosDAO();
    }

    /**
     * Genera un reporte y lo devuelve como un objeto JasperPrint
     *
     * @param reporteNombre Nombre del archivo de reporte sin extensión
     * @param parametros Parámetros para el reporte
     * @return Objeto JasperPrint generado
     * @throws Exception Si ocurre algún error durante la generación
     */
    public JasperPrint generarReporte(String reporteNombre, Map<String, Object> parametros) throws Exception {
        System.out.println("Generando reporte: " + reporteNombre);

        try {
            // Verificar existencia del directorio de reportes
            File outputDir = new File(REPORTES_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                System.out.println("Creado directorio de reportes: " + outputDir.getAbsolutePath());
            }

            // Compilar el reporte si es necesario
            compilarReporteSiEsNecesario(reporteNombre);

            // Obtener datos específicos según el reporte
            if (reporteNombre.equals("inventario_productos")) {
                return generarReporteInventario(parametros);
            } else {
                // Reporte genérico usando la conexión a base de datos
                JasperReport jasperReport = obtenerReporteCompilado(reporteNombre);
                return JasperFillManager.fillReport(jasperReport, parametros, DatabaseConnection.getConnection());
            }
        } catch (JRException | SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un visor de reportes para mostrar en la interfaz gráfica
     *
     * @param reporteNombre Nombre del reporte
     * @param parametros Parámetros del reporte
     * @return Un visor de JasperReports
     * @throws Exception Si ocurre algún error
     */
    public JRViewer generarVisorReporte(String reporteNombre, Map<String, Object> parametros) throws Exception {
        JasperPrint jasperPrint = generarReporte(reporteNombre, parametros);
        return new JRViewer(jasperPrint);
    }

    /**
     * Exporta un reporte a formato PDF
     *
     * @param reporteNombre Nombre del reporte
     * @param parametros Parámetros del reporte
     * @param rutaDestino Ruta donde se guardará el PDF
     * @throws Exception Si ocurre algún error
     */
    public void exportarReporteAPdf(String reporteNombre, Map<String, Object> parametros, String rutaDestino) throws Exception {
        JasperPrint jasperPrint = generarReporte(reporteNombre, parametros);

        // Crear directorio para el PDF si no existe
        File pdfFile = new File(rutaDestino);
        if (!pdfFile.getParentFile().exists()) {
            pdfFile.getParentFile().mkdirs();
        }

        // Exportar a PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaDestino);
        System.out.println("Reporte exportado a PDF: " + rutaDestino);
    }

    /**
     * Imprime directamente un reporte en la impresora predeterminada
     *
     * @param reporteNombre Nombre del reporte
     * @throws Exception Si ocurre algún error
     */
    public void imprimirReporte(String reporteNombre) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        JasperPrint jasperPrint = generarReporte(reporteNombre, parametros);

        PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
        printAttributes.add(new Copies(1)); // Número de copias

        JasperPrintManager.printReport(jasperPrint, false); // false para no mostrar diálogo de impresión
        System.out.println("Reporte enviado a la impresora");
    }

    /**
     * Genera específicamente el reporte de inventario
     *
     * @param parametros Parámetros del reporte
     * @return JasperPrint generado
     * @throws SQLException Si hay error en la base de datos
     * @throws JRException Si hay error en Jasper
     */
    private JasperPrint generarReporteInventario(Map<String, Object> parametros) throws SQLException, JRException {
        // Obtener datos para el reporte desde la base de datos
        List<mProducto> productos = productosDAO.listarTodos();

        // Aplicar filtros si existen
        if (parametros.containsKey("categoria_id")) {
            Integer categoriaId = (Integer) parametros.get("categoria_id");
            if (categoriaId > 0) {
                productos = productos.stream()
                        .filter(p -> p.getIdCategoria() == categoriaId)
                        .toList();
            }
        }

        if (parametros.containsKey("marca_id")) {
            Integer marcaId = (Integer) parametros.get("marca_id");
            if (marcaId > 0) {
                productos = productos.stream()
                        .filter(p -> p.getIdMarca() == marcaId)
                        .toList();
            }
        }

        if (!parametros.containsKey("mostrar_inactivos") || !(Boolean) parametros.get("mostrar_inactivos")) {
            productos = productos.stream()
                    .filter(mProducto::isEstado)
                    .toList();
        }

        // Convertir a datos para el reporte
        List<Map<String, Object>> datosReporte = prepararDatosProductos(productos);

        // Obtener el reporte compilado
        JasperReport jasperReport = obtenerReporteCompilado("inventario_productos");

        // Generar el reporte con la colección de datos
        return JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datosReporte)
        );
    }

    /**
     * Prepara los datos de productos para el reporte de inventario
     *
     * @param productos Lista de productos
     * @return Lista de mapas con datos formateados para el reporte
     * @throws SQLException Si hay error en la base de datos
     */
    private List<Map<String, Object>> prepararDatosProductos(List<mProducto> productos) throws SQLException {
        return productos.stream().map(producto -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("idProducto", producto.getIdProducto());
            fila.put("nombre", producto.getNombre());
            fila.put("categoria", obtenerNombreCategoria(producto.getIdCategoria()));
            fila.put("marca", obtenerNombreMarca(producto.getIdMarca()));
            fila.put("iva", producto.getIva());
            fila.put("estado", producto.isEstado() ? "Activo" : "Inactivo");
            return fila;
        }).toList();
    }

    // Método auxiliar para obtener nombre de categoría
    private String obtenerNombreCategoria(int idCategoria) {
        try {
            // Implementar la lógica para obtener el nombre real de la categoría
            return "Categoría " + idCategoria; // Temporalmente
        } catch (Exception e) {
            return "Categoría desconocida";
        }
    }

    // Método auxiliar para obtener nombre de marca
    private String obtenerNombreMarca(int idMarca) {
        try {
            // Implementar la lógica para obtener el nombre real de la marca
            return "Marca " + idMarca; // Temporalmente
        } catch (Exception e) {
            return "Marca desconocida";
        }
    }

    /**
     * Obtiene un reporte compilado desde el archivo .jasper
     *
     * @param reporteNombre Nombre del reporte sin extensión
     * @return JasperReport compilado
     * @throws JRException Si hay error cargando el reporte
     */
    private JasperReport obtenerReporteCompilado(String reporteNombre) throws JRException {
        String rutaJasper = REPORTES_DIR + reporteNombre + ".jasper";

        // Verificar si el archivo .jasper existe
        File jasperFile = new File(rutaJasper);
        if (jasperFile.exists()) {
            return (JasperReport) JRLoader.loadObject(jasperFile);
        } else {
            // Si no existe, compilar el reporte
            String rutaJrxml = REPORTES_DIR + reporteNombre + ".jrxml";
            return JasperCompileManager.compileReport(rutaJrxml);
        }
    }

    /**
     * Compila el reporte JRXML a JASPER si es necesario
     *
     * @param reporteNombre Nombre del reporte sin extensión
     * @throws JRException Si hay error compilando
     */
    private void compilarReporteSiEsNecesario(String reporteNombre) throws JRException {
        File jrxmlFile = new File(REPORTES_DIR + reporteNombre + ".jrxml");
        File jasperFile = new File(REPORTES_DIR + reporteNombre + ".jasper");

        if (!jasperFile.exists() || jrxmlFile.lastModified() > jasperFile.lastModified()) {
            System.out.println("Compilando reporte: " + reporteNombre);
            JasperCompileManager.compileReportToFile(
                    REPORTES_DIR + reporteNombre + ".jrxml",
                    REPORTES_DIR + reporteNombre + ".jasper"
            );
        }
    }
}
