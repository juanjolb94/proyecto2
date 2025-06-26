package modelo.service;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

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
    private Map<Integer, String> cacheCategorias = new HashMap<>();
    private Map<Integer, String> cacheMarcas = new HashMap<>();

    public ReporteService() throws SQLException {
        this.productosDAO = new ProductosDAO();
        precargarDatos();
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
            // Preparar los parámetros para evitar errores de tipos
            prepararParametros(parametros);

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
                Connection connection = DatabaseConnection.getConnection();
                return JasperFillManager.fillReport(jasperReport, parametros, connection);
            }
        } catch (JRException | SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    /**
     * Genera el reporte de inventario de productos
     */
    public JasperPrint generarReporteInventarioProductos(Map<String, Object> parametros) throws JRException, SQLException {
        // Obtener productos desde el DAO con descripción incluida
        ProductosDAO productosDAO = new ProductosDAO();
        List<mProducto> productos = productosDAO.listarTodosConDescripcion();

        // Aplicar filtros si existen
        if (parametros.containsKey("categoria_id")) {
            Integer categoriaId = (Integer) parametros.get("categoria_id");
            if (categoriaId != null && categoriaId > 0) {
                productos = productos.stream()
                        .filter(p -> p.getIdCategoria() == categoriaId)
                        .toList();
            }
        }

        if (parametros.containsKey("marca_id")) {
            Integer marcaId = (Integer) parametros.get("marca_id");
            if (marcaId != null && marcaId > 0) {
                productos = productos.stream()
                        .filter(p -> p.getIdMarca() == marcaId)
                        .toList();
            }
        }

        if (parametros.containsKey("mostrar_inactivos")
                && !((Boolean) parametros.get("mostrar_inactivos"))) {
            productos = productos.stream()
                    .filter(mProducto::isEstado)
                    .toList();
        }

        // Aplicar filtros de stock si existen
        if (parametros.containsKey("stock_minimo")) {
            Integer stockMinimo = (Integer) parametros.get("stock_minimo");
            productos = productos.stream()
                    .filter(p -> p.getStock() >= stockMinimo)
                    .toList();
        }

        if (parametros.containsKey("stock_maximo")) {
            Integer stockMaximo = (Integer) parametros.get("stock_maximo");
            productos = productos.stream()
                    .filter(p -> p.getStock() <= stockMaximo)
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
     * Genera un objeto JasperPrint con el reporte procesado
     */
    public JasperPrint generarJasperPrint(String reporteNombre, Map<String, Object> parametros) throws Exception {
        System.out.println("Generando reporte: " + reporteNombre);

        try {
            // Preparar los parámetros para evitar errores de tipos
            prepararParametros(parametros);

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
                Connection connection = DatabaseConnection.getConnection();
                return JasperFillManager.fillReport(jasperReport, parametros, connection);
            }
        } catch (JRException | SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    /**
     * Prepara los parámetros para evitar errores de tipos
     *
     * @param parametros Mapa de parámetros a preparar
     */
    private void prepararParametros(Map<String, Object> parametros) {
        // Asegurar que FECHA_GENERACION sea un objeto java.util.Date
        if (parametros.containsKey("FECHA_GENERACION")) {
            Object fechaObj = parametros.get("FECHA_GENERACION");
            if (!(fechaObj instanceof java.util.Date)) {
                parametros.put("FECHA_GENERACION", new java.util.Date());
            }
        } else {
            parametros.put("FECHA_GENERACION", new java.util.Date());
        }

        // Añadir formato de fecha como parámetro adicional
        parametros.put("FECHA_FORMATO", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));

        // Convertir a tipos correctos los parámetros numéricos
        for (String key : parametros.keySet()) {
            Object valor = parametros.get(key);

            // Garantizar que los IDs sean Integer
            if (key.endsWith("_id") && valor instanceof String) {
                try {
                    parametros.put(key, Integer.parseInt((String) valor));
                } catch (NumberFormatException e) {
                    parametros.put(key, 0);
                }
            }

            // Garantizar que los booleanos sean Boolean
            if (valor instanceof String && ("true".equalsIgnoreCase((String) valor) || "false".equalsIgnoreCase((String) valor))) {
                parametros.put(key, Boolean.valueOf((String) valor));
            }
        }

        // Imprimir los parámetros para depuración
        System.out.println("Parámetros del reporte:");
        for (Map.Entry<String, Object> entry : parametros.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue()
                    + " (Tipo: " + (entry.getValue() != null ? entry.getValue().getClass().getName() : "null") + ")");
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

        // Aplicar filtros de stock si existen
        if (parametros.containsKey("stock_minimo")) {
            Integer stockMinimo = (Integer) parametros.get("stock_minimo");
            productos = productos.stream()
                    .filter(p -> p.getStock() >= stockMinimo)
                    .toList();
        }

        if (parametros.containsKey("stock_maximo")) {
            Integer stockMaximo = (Integer) parametros.get("stock_maximo");
            productos = productos.stream()
                    .filter(p -> p.getStock() <= stockMaximo)
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
     */
    private List<Map<String, Object>> prepararDatosProductos(List<mProducto> productos) throws SQLException {
        return productos.stream().map(producto -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("idProducto", producto.getIdProducto());
            fila.put("codigo", producto.getCodigo() != null ? producto.getCodigo() : "");
            fila.put("nombre", producto.getNombre());
            fila.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
            fila.put("stock", producto.getStock());  // Ya es int
            fila.put("precio", producto.getPrecio());  // Ya es int
            fila.put("categoria", obtenerNombreCategoria(producto.getIdCategoria()));
            fila.put("marca", obtenerNombreMarca(producto.getIdMarca()));
            fila.put("iva", producto.getIva());  // double
            fila.put("estado", producto.isEstado() ? "Activo" : "Inactivo");
            return fila;
        }).collect(Collectors.toList());
    }

    // Método para precargar los datos de categorías y marcas
    private void precargarDatos() throws SQLException {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Cargar categorías
            String sqlCategorias = "SELECT id_categoria, nombre FROM categoria_producto";
            try (java.sql.Statement stmt = conn.createStatement(); java.sql.ResultSet rs = stmt.executeQuery(sqlCategorias)) {
                while (rs.next()) {
                    cacheCategorias.put(rs.getInt("id_categoria"), rs.getString("nombre"));
                }
                System.out.println("Categorías cargadas: " + cacheCategorias.size());
            } catch (SQLException e) {
                System.err.println("Error al cargar categorías: " + e.getMessage());
                e.printStackTrace();
            }

            // Cargar marcas
            String sqlMarcas = "SELECT id_marca, nombre FROM marca_producto";
            try (java.sql.Statement stmt = conn.createStatement(); java.sql.ResultSet rs = stmt.executeQuery(sqlMarcas)) {
                while (rs.next()) {
                    cacheMarcas.put(rs.getInt("id_marca"), rs.getString("nombre"));
                }
                System.out.println("Marcas cargadas: " + cacheMarcas.size());
            } catch (SQLException e) {
                System.err.println("Error al cargar marcas: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error general al precargar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para obtener nombre de categoría
    private String obtenerNombreCategoria(int idCategoria) {
        if (cacheCategorias.isEmpty()) {
            // Si la caché está vacía, intentar obtener directamente de la base de datos
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "SELECT nombre FROM categoria_producto WHERE id_categoria = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idCategoria);
                    try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("nombre");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al buscar categoría " + idCategoria + ": " + e.getMessage());
            }
            return "Categoría " + idCategoria;
        }
        return cacheCategorias.getOrDefault(idCategoria, "Categoría " + idCategoria);
    }

    // Método auxiliar para obtener nombre de marca
    private String obtenerNombreMarca(int idMarca) {
        if (cacheMarcas.isEmpty()) {
            // Si la caché está vacía, intentar obtener directamente de la base de datos
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "SELECT nombre FROM marca_producto WHERE id_marca = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idMarca);
                    try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("nombre");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al buscar marca " + idMarca + ": " + e.getMessage());
            }
            return "Marca " + idMarca;
        }
        return cacheMarcas.getOrDefault(idMarca, "Marca " + idMarca);
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
