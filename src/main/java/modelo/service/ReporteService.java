package modelo.service;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
            } else if (reporteNombre.equals("productos_mas_vendidos")) {
                // Reporte de productos más vendidos
                return generarReporteProductosMasVendidos(parametros);
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

    // Genera el reporte de inventario de productos
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

    // Genera un objeto JasperPrint con el reporte procesado
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
            switch (reporteNombre) {
                case "inventario_productos":
                    return generarReporteInventario(parametros);
                case "reporte_compras":
                    return generarReporteCompras(parametros);
                case "reporte_ventas":
                    return generarReporteVentas(parametros);
                case "reporte_ingresos_egresos":
                    return generarReporteIngresosEgresos(parametros);
                case "ticket_venta":
                    return generarReporteTicketVenta(parametros);
                default:
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
     */
    public JRViewer generarVisorReporte(String reporteNombre, Map<String, Object> parametros) throws Exception {
        JasperPrint jasperPrint = generarReporte(reporteNombre, parametros);
        return new JRViewer(jasperPrint);
    }

    /**
     * Exporta un reporte a formato PDF
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
     */
    public void imprimirReporte(String reporteNombre) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        JasperPrint jasperPrint = generarReporte(reporteNombre, parametros);

        PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
        printAttributes.add(new Copies(1)); // Número de copias

        JasperPrintManager.printReport(jasperPrint, false); // false para no mostrar diálogo de impresión
        System.out.println("Reporte enviado a la impresora");
    }

    // Genera específicamente el reporte de inventario
    private JasperPrint generarReporteInventario(Map<String, Object> parametros) throws SQLException, JRException {

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
            System.out.println("Aplicando filtro de estados. Productos antes del filtro: " + productos.size());
            productos = productos.stream()
                    .filter(mProducto::isEstado)
                    .toList();
            System.out.println("Productos después del filtro: " + productos.size());
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

    // Prepara los datos de productos para el reporte de inventario
    private List<Map<String, Object>> prepararDatosProductos(List<mProducto> productos) throws SQLException {
        return productos.stream().map(producto -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("idProducto", producto.getIdProducto());
            fila.put("codigo", producto.getCodigo() != null ? producto.getCodigo() : "");
            fila.put("nombre", producto.getNombre());
            fila.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
            fila.put("stock", producto.getStock());
            fila.put("precio", producto.getPrecio());

            // Usar los nombres ya obtenidos de la consulta SQL
            fila.put("categoria", producto.getNombreCategoria() != null ? producto.getNombreCategoria() : "Sin categoría");
            fila.put("marca", producto.getNombreMarca() != null ? producto.getNombreMarca() : "Sin marca");

            fila.put("iva", producto.getIva());
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

    /**
     * Genera específicamente el reporte de compras con filtros
     */
    private JasperPrint generarReporteCompras(Map<String, Object> parametros) throws SQLException, JRException {
        System.out.println("=== GENERANDO REPORTE DE COMPRAS ===");

        // Crear la consulta SQL base
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    cc.id_compra, ");
        sql.append("    cc.fecha_compra, ");
        sql.append("    COALESCE(p.razon_social, 'Sin proveedor') as proveedor, ");
        sql.append("    cc.tipo_documento, ");
        sql.append("    cc.nro_documento, ");
        sql.append("    cc.timbrado, ");
        sql.append("    cc.condicion, ");
        sql.append("    cc.subtotal, ");
        sql.append("    cc.total_iva, ");
        sql.append("    cc.total, ");
        sql.append("    CASE WHEN cc.estado = 1 THEN 'Activo' ELSE 'Anulado' END as estado ");
        sql.append("FROM compras_cabecera cc ");
        sql.append("LEFT JOIN proveedores p ON cc.id_proveedor = p.id_proveedor ");
        sql.append("WHERE 1=1 ");

        List<Object> parametrosSql = new ArrayList<>();

        // Aplicar filtros de fecha
        if (parametros.containsKey("fecha_desde") && parametros.get("fecha_desde") != null) {
            sql.append("AND cc.fecha_compra >= ? ");
            parametrosSql.add(parametros.get("fecha_desde"));
        }

        if (parametros.containsKey("fecha_hasta") && parametros.get("fecha_hasta") != null) {
            sql.append("AND cc.fecha_compra <= ? ");
            parametrosSql.add(parametros.get("fecha_hasta"));
        }

        // Aplicar filtro de proveedor
        if (parametros.containsKey("proveedor_id")) {
            Integer proveedorId = (Integer) parametros.get("proveedor_id");
            if (proveedorId != null && proveedorId > 0) {
                sql.append("AND cc.id_proveedor = ? ");
                parametrosSql.add(proveedorId);
            }
        }

        // Aplicar filtro de tipo documento
        if (parametros.containsKey("tipo_documento") && parametros.get("tipo_documento") != null) {
            sql.append("AND cc.tipo_documento = ? ");
            parametrosSql.add(parametros.get("tipo_documento"));
        }

        // Aplicar filtro de condición
        if (parametros.containsKey("condicion") && parametros.get("condicion") != null) {
            sql.append("AND cc.condicion = ? ");
            parametrosSql.add(parametros.get("condicion"));
        }

        // Aplicar filtro de número de documento
        if (parametros.containsKey("numero_documento") && parametros.get("numero_documento") != null) {
            sql.append("AND cc.nro_documento LIKE ? ");
            parametrosSql.add("%" + parametros.get("numero_documento") + "%");
        }

        // Aplicar filtro de timbrado
        if (parametros.containsKey("timbrado") && parametros.get("timbrado") != null) {
            sql.append("AND cc.timbrado LIKE ? ");
            parametrosSql.add("%" + parametros.get("timbrado") + "%");
        }

        // Aplicar filtro de incluir anulados
        if (parametros.containsKey("incluir_anulados") && parametros.get("incluir_anulados") != null) {
            Boolean incluirAnulados = (Boolean) parametros.get("incluir_anulados");
            if (!incluirAnulados) {
                sql.append("AND cc.estado = 1 ");
            }
        }

        sql.append("ORDER BY cc.fecha_compra DESC, cc.id_compra DESC");

        System.out.println("SQL Compras: " + sql.toString());
        System.out.println("Parámetros: " + parametrosSql);

        // Ejecutar consulta y crear lista de datos
        List<Map<String, Object>> datosReporte = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            // Establecer parámetros
            for (int i = 0; i < parametrosSql.size(); i++) {
                ps.setObject(i + 1, parametrosSql.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id_compra", rs.getInt("id_compra"));
                    fila.put("fecha_compra", rs.getDate("fecha_compra"));
                    fila.put("proveedor", rs.getString("proveedor"));
                    fila.put("tipo_documento", rs.getString("tipo_documento"));
                    fila.put("nro_documento", rs.getString("nro_documento"));
                    fila.put("timbrado", rs.getString("timbrado"));
                    fila.put("condicion", rs.getString("condicion"));
                    fila.put("subtotal", rs.getBigDecimal("subtotal"));
                    fila.put("total_iva", rs.getBigDecimal("total_iva"));
                    fila.put("total", rs.getBigDecimal("total"));
                    fila.put("estado", rs.getString("estado"));

                    datosReporte.add(fila);
                }
            }
        }

        System.out.println("Registros encontrados: " + datosReporte.size());

        // IMPORTANTE: Si no hay datos, crear al menos un registro vacío para que se muestre noData
        if (datosReporte.isEmpty()) {
            System.out.println("No hay datos - agregando registro vacío para noData");
            // No agregamos datos, dejamos la lista vacía para que se active noData
        }

        // Obtener el reporte compilado
        JasperReport jasperReport = obtenerReporteCompilado("reporte_compras");

        // Generar el reporte con la colección de datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datosReporte)
        );

        System.out.println("JasperPrint generado - Páginas: " + jasperPrint.getPages().size());
        return jasperPrint;
    }

    /**
     * Genera específicamente el reporte de ventas con filtros
     */
    private JasperPrint generarReporteVentas(Map<String, Object> parametros) throws SQLException, JRException {
        System.out.println("=== GENERANDO REPORTE DE VENTAS ===");

        // Crear la consulta SQL base
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    v.id, ");
        sql.append("    v.fecha, ");
        sql.append("    v.numero_factura, ");
        sql.append("    COALESCE(c.nombre, 'Cliente ocasional') as cliente, ");
        sql.append("    COALESCE(u.NombreUsuario, 'Usuario') as usuario, ");
        sql.append("    COALESCE(v.tipo_venta, 'CONTADO') as tipo_venta, ");
        sql.append("    COALESCE(v.estado, 'PENDIENTE') as estado, ");
        sql.append("    COALESCE(v.subtotal, 0) as subtotal, ");
        sql.append("    COALESCE(v.impuesto_total, 0) as impuesto_total, ");
        sql.append("    COALESCE(v.total, 0) as total, ");
        sql.append("    CASE WHEN v.anulado = 1 THEN 'Sí' ELSE 'No' END as anulado ");
        sql.append("FROM ventas v ");
        sql.append("LEFT JOIN clientes c ON v.id_cliente = c.id_cliente ");
        sql.append("LEFT JOIN usuarios u ON v.id_usuario = u.UsuarioID ");
        sql.append("WHERE 1=1 ");

        List<Object> parametrosSql = new ArrayList<>();

        // Aplicar filtros de fecha
        if (parametros.containsKey("fecha_desde") && parametros.get("fecha_desde") != null) {
            sql.append("AND DATE(v.fecha) >= ? ");
            parametrosSql.add(parametros.get("fecha_desde"));
        }

        if (parametros.containsKey("fecha_hasta") && parametros.get("fecha_hasta") != null) {
            sql.append("AND DATE(v.fecha) <= ? ");
            parametrosSql.add(parametros.get("fecha_hasta"));
        }

        // Aplicar filtro de cliente
        if (parametros.containsKey("cliente_id")) {
            Integer clienteId = (Integer) parametros.get("cliente_id");
            if (clienteId != null && clienteId > 0) {
                sql.append("AND v.id_cliente = ? ");
                parametrosSql.add(clienteId);
            }
        }

        // Aplicar filtro de usuario
        if (parametros.containsKey("usuario_id")) {
            Integer usuarioId = (Integer) parametros.get("usuario_id");
            if (usuarioId != null && usuarioId > 0) {
                sql.append("AND v.id_usuario = ? ");
                parametrosSql.add(usuarioId);
            }
        }

        // Aplicar filtro de tipo de venta
        if (parametros.containsKey("tipo_venta") && parametros.get("tipo_venta") != null) {
            sql.append("AND v.tipo_venta = ? ");
            parametrosSql.add(parametros.get("tipo_venta"));
        }

        // Aplicar filtro de estado
        if (parametros.containsKey("estado") && parametros.get("estado") != null) {
            sql.append("AND v.estado = ? ");
            parametrosSql.add(parametros.get("estado"));
        }

        // Aplicar filtro de número de factura
        if (parametros.containsKey("numero_factura") && parametros.get("numero_factura") != null) {
            sql.append("AND v.numero_factura LIKE ? ");
            parametrosSql.add("%" + parametros.get("numero_factura") + "%");
        }

        // Aplicar filtro de incluir anulados
        if (parametros.containsKey("incluir_anulados") && parametros.get("incluir_anulados") != null) {
            Boolean incluirAnulados = (Boolean) parametros.get("incluir_anulados");
            if (!incluirAnulados) {
                sql.append("AND v.anulado = 0 ");
            }
        }

        sql.append("ORDER BY v.fecha DESC, v.id DESC");

        System.out.println("SQL Ventas: " + sql.toString());
        System.out.println("Parámetros: " + parametrosSql);

        // Ejecutar consulta y crear lista de datos
        List<Map<String, Object>> datosReporte = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            // Establecer parámetros
            for (int i = 0; i < parametrosSql.size(); i++) {
                ps.setObject(i + 1, parametrosSql.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id", rs.getInt("id"));
                    fila.put("fecha", rs.getTimestamp("fecha"));
                    fila.put("numero_factura", rs.getString("numero_factura"));
                    fila.put("cliente", rs.getString("cliente"));
                    fila.put("usuario", rs.getString("usuario"));
                    fila.put("tipo_venta", rs.getString("tipo_venta"));
                    fila.put("estado", rs.getString("estado"));
                    fila.put("subtotal", rs.getLong("subtotal"));
                    fila.put("impuesto_total", rs.getLong("impuesto_total"));
                    fila.put("total", rs.getLong("total"));
                    fila.put("anulado", rs.getString("anulado"));

                    datosReporte.add(fila);
                }
            }
        }

        System.out.println("Registros encontrados: " + datosReporte.size());

        // Obtener el reporte compilado
        JasperReport jasperReport = obtenerReporteCompilado("reporte_ventas");

        // Generar el reporte con la colección de datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datosReporte)
        );

        System.out.println("JasperPrint generado - Páginas: " + jasperPrint.getPages().size());
        return jasperPrint;
    }

    /**
     * Genera específicamente el reporte de ingresos-egresos con filtros
     */
    private JasperPrint generarReporteIngresosEgresos(Map<String, Object> parametros) throws SQLException, JRException {
        System.out.println("=== GENERANDO REPORTE DE INGRESOS-EGRESOS ===");

        List<Map<String, Object>> datosReporte = new ArrayList<>();

        // Consultar INGRESOS
        StringBuilder sqlIngresos = new StringBuilder();
        sqlIngresos.append("SELECT ");
        sqlIngresos.append("    ic.id, ");
        sqlIngresos.append("    ic.fecha, ");
        sqlIngresos.append("    ic.monto, ");
        sqlIngresos.append("    ic.concepto, ");
        sqlIngresos.append("    ic.usuario, ");
        sqlIngresos.append("    'INGRESO' as tipo_movimiento, ");
        sqlIngresos.append("    CASE WHEN ic.anulado = 1 THEN 'SI' ELSE 'NO' END as anulado, ");
        sqlIngresos.append("    ic.id_caja ");
        sqlIngresos.append("FROM ingresos_caja ic ");
        sqlIngresos.append("WHERE 1=1 ");

        List<Object> parametrosIngresos = new ArrayList<>();

        // Aplicar filtros para ingresos
        aplicarFiltrosMovimientos(sqlIngresos, parametrosIngresos, parametros, "ic", true);

        // Consultar EGRESOS
        StringBuilder sqlEgresos = new StringBuilder();
        sqlEgresos.append("SELECT ");
        sqlEgresos.append("    g.id, ");
        sqlEgresos.append("    g.fecha, ");
        sqlEgresos.append("    g.monto, ");
        sqlEgresos.append("    g.concepto, ");
        sqlEgresos.append("    g.usuario, ");
        sqlEgresos.append("    'EGRESO' as tipo_movimiento, ");
        sqlEgresos.append("    CASE WHEN g.anulado = 1 THEN 'SI' ELSE 'NO' END as anulado, ");
        sqlEgresos.append("    g.id_caja ");
        sqlEgresos.append("FROM gastos g ");
        sqlEgresos.append("WHERE 1=1 ");

        List<Object> parametrosEgresos = new ArrayList<>();

        // Aplicar filtros para egresos
        aplicarFiltrosMovimientos(sqlEgresos, parametrosEgresos, parametros, "g", false);

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Ejecutar consulta de INGRESOS
            if (debeIncluirTipo(parametros, "INGRESO")) {
                try (PreparedStatement ps = connection.prepareStatement(sqlIngresos.toString())) {
                    for (int i = 0; i < parametrosIngresos.size(); i++) {
                        ps.setObject(i + 1, parametrosIngresos.get(i));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            datosReporte.add(crearFilaMovimiento(rs));
                        }
                    }
                }
            }

            // Ejecutar consulta de EGRESOS
            if (debeIncluirTipo(parametros, "EGRESO")) {
                try (PreparedStatement ps = connection.prepareStatement(sqlEgresos.toString())) {
                    for (int i = 0; i < parametrosEgresos.size(); i++) {
                        ps.setObject(i + 1, parametrosEgresos.get(i));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            datosReporte.add(crearFilaMovimiento(rs));
                        }
                    }
                }
            }
        }

        // Ordenar por fecha descendente
        datosReporte.sort((a, b) -> {
            java.sql.Timestamp fechaA = (java.sql.Timestamp) a.get("fecha");
            java.sql.Timestamp fechaB = (java.sql.Timestamp) b.get("fecha");
            return fechaB.compareTo(fechaA);
        });

        System.out.println("Registros encontrados: " + datosReporte.size());

        // Obtener el reporte compilado
        JasperReport jasperReport = obtenerReporteCompilado("reporte_ingresos_egresos");

        // Generar el reporte con la colección de datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datosReporte)
        );

        System.out.println("JasperPrint generado - Páginas: " + jasperPrint.getPages().size());
        return jasperPrint;
    }

    /**
     * Aplica filtros comunes para movimientos de caja
     */
    private void aplicarFiltrosMovimientos(StringBuilder sql, List<Object> parametrosSql,
            Map<String, Object> parametros, String alias, boolean esIngreso) {

        // Filtros de fecha
        if (parametros.containsKey("fecha_desde") && parametros.get("fecha_desde") != null) {
            sql.append("AND DATE(").append(alias).append(".fecha) >= ? ");
            parametrosSql.add(parametros.get("fecha_desde"));
        }

        if (parametros.containsKey("fecha_hasta") && parametros.get("fecha_hasta") != null) {
            sql.append("AND DATE(").append(alias).append(".fecha) <= ? ");
            parametrosSql.add(parametros.get("fecha_hasta"));
        }

        // Filtro de usuario
        if (parametros.containsKey("usuario_id")) {
            Integer usuarioId = (Integer) parametros.get("usuario_id");
            if (usuarioId != null && usuarioId > 0) {
                sql.append("AND ").append(alias).append(".usuario = (SELECT NombreUsuario FROM usuarios WHERE UsuarioID = ?) ");
                parametrosSql.add(usuarioId);
            }
        }

        // Filtro de incluir anulados
        if (parametros.containsKey("incluir_anulados") && parametros.get("incluir_anulados") != null) {
            Boolean incluirAnulados = (Boolean) parametros.get("incluir_anulados");
            if (!incluirAnulados) {
                sql.append("AND ").append(alias).append(".anulado = 0 ");
            }
        }

        sql.append("ORDER BY ").append(alias).append(".fecha DESC");
    }

    /**
     * Determina si debe incluir un tipo de movimiento según los filtros
     */
    private boolean debeIncluirTipo(Map<String, Object> parametros, String tipo) {
        if (!parametros.containsKey("tipo_movimiento")) {
            return true; // Incluir todos por defecto
        }

        String tipoFiltro = (String) parametros.get("tipo_movimiento");

        switch (tipoFiltro) {
            case "TODOS LOS MOVIMIENTOS":
                return true;
            case "SOLO INGRESOS":
                return "INGRESO".equals(tipo);
            case "SOLO EGRESOS":
                return "EGRESO".equals(tipo);
            default:
                return true;
        }
    }

    /**
     * Crea una fila de datos para el reporte desde un ResultSet
     */
    private Map<String, Object> crearFilaMovimiento(ResultSet rs) throws SQLException {
        Map<String, Object> fila = new HashMap<>();
        fila.put("id", rs.getInt("id"));
        fila.put("fecha", rs.getTimestamp("fecha"));
        fila.put("monto", rs.getBigDecimal("monto"));
        fila.put("concepto", rs.getString("concepto"));
        fila.put("usuario", rs.getString("usuario"));
        fila.put("tipo_movimiento", rs.getString("tipo_movimiento"));
        fila.put("anulado", rs.getString("anulado"));
        fila.put("id_caja", rs.getInt("id_caja"));
        return fila;
    }

    /**
     * Genera el reporte de productos más vendidos
     */
    public JasperPrint generarReporteProductosMasVendidos(Map<String, Object> parametros) throws JRException, SQLException {
        // Obtener datos de productos más vendidos
        List<Map<String, Object>> datosReporte = obtenerProductosMasVendidos(parametros);

        // Obtener el reporte compilado
        JasperReport jasperReport = obtenerReporteCompilado("productos_mas_vendidos");

        // Generar el reporte con la colección de datos
        return JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datosReporte)
        );
    }

    /**
     * Obtiene los datos de productos más vendidos según los filtros
     */
    private List<Map<String, Object>> obtenerProductosMasVendidos(Map<String, Object> parametros) throws SQLException {
        List<Map<String, Object>> productos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    pv.id_producto, ");
        sql.append("    pv.codigo_barra, ");
        sql.append("    pv.descripcion_producto, ");
        sql.append("    pv.cantidad_total_vendida, ");
        sql.append("    pv.monto_total_vendido, ");
        sql.append("    pv.numero_ventas, ");
        sql.append("    pv.precio_promedio, ");
        sql.append("    pc.nombre as nombre_producto, ");
        sql.append("    cp.nombre as categoria, ");
        sql.append("    mp.nombre as marca ");
        sql.append("FROM v_productos_mas_vendidos pv ");
        sql.append("INNER JOIN productos_detalle pd ON pv.codigo_barra = pd.cod_barra ");
        sql.append("INNER JOIN productos_cabecera pc ON pd.id_producto = pc.id_producto ");
        sql.append("LEFT JOIN categoria_producto cp ON pc.id_categoria = cp.id_categoria ");
        sql.append("LEFT JOIN marca_producto mp ON pc.id_marca = mp.id_marca ");
        sql.append("WHERE 1=1 ");

        List<Object> parametrosSql = new ArrayList<>();

        // Aplicar filtros de fecha a través de subconsulta
        if (parametros.containsKey("fecha_desde") && parametros.get("fecha_desde") != null) {
            sql.append("AND pv.codigo_barra IN (");
            sql.append("    SELECT DISTINCT vd.codigo_barra ");
            sql.append("    FROM ventas_detalle vd ");
            sql.append("    INNER JOIN ventas_cabecera vc ON vd.id_venta = vc.id ");
            sql.append("    WHERE vc.fecha >= ? ");

            if (parametros.containsKey("fecha_hasta") && parametros.get("fecha_hasta") != null) {
                sql.append("    AND vc.fecha <= ? ");
            }

            if (parametros.containsKey("incluir_anulados") && parametros.get("incluir_anulados") != null) {
                Boolean incluirAnulados = (Boolean) parametros.get("incluir_anulados");
                if (!incluirAnulados) {
                    sql.append("    AND vc.anulado = 0 ");
                }
            }

            sql.append(") ");

            parametrosSql.add(parametros.get("fecha_desde"));
            if (parametros.containsKey("fecha_hasta") && parametros.get("fecha_hasta") != null) {
                parametrosSql.add(parametros.get("fecha_hasta"));
            }
        }

        // Ordenamiento según la selección
        String tipoOrden = (String) parametros.get("tipo_ordenamiento");
        if ("Por monto vendido".equals(tipoOrden)) {
            sql.append("ORDER BY pv.monto_total_vendido DESC ");
        } else if ("Por número de ventas".equals(tipoOrden)) {
            sql.append("ORDER BY pv.numero_ventas DESC ");
        } else {
            // Por defecto: Por cantidad vendida
            sql.append("ORDER BY pv.cantidad_total_vendida DESC ");
        }

        // Limitar resultados
        if (parametros.containsKey("limite_productos")) {
            Integer limite = (Integer) parametros.get("limite_productos");
            sql.append("LIMIT ? ");
            parametrosSql.add(limite);
        }

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            // Establecer parámetros
            for (int i = 0; i < parametrosSql.size(); i++) {
                ps.setObject(i + 1, parametrosSql.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int ranking = 1;
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("ranking", ranking++);
                    fila.put("id_producto", rs.getInt("id_producto"));
                    fila.put("codigo_barra", rs.getString("codigo_barra"));
                    fila.put("nombre_producto", rs.getString("nombre_producto"));
                    fila.put("descripcion_producto", rs.getString("descripcion_producto"));
                    fila.put("categoria", rs.getString("categoria"));
                    fila.put("marca", rs.getString("marca"));
                    fila.put("cantidad_total_vendida", rs.getInt("cantidad_total_vendida"));
                    fila.put("monto_total_vendido", rs.getBigDecimal("monto_total_vendido"));
                    fila.put("numero_ventas", rs.getInt("numero_ventas"));
                    fila.put("precio_promedio", rs.getBigDecimal("precio_promedio"));

                    productos.add(fila);
                }
            }
        }

        return productos;
    }

    /**
     * Genera específicamente el reporte de ticket de venta
     */
    private JasperPrint generarReporteTicketVenta(Map<String, Object> parametros) throws SQLException, JRException {
        // Obtener conexión a la base de datos
        Connection conexion = DatabaseConnection.getConnection();

        // Compilar y llenar el reporte
        String rutaReporte = REPORTES_DIR + "ticket_venta.jrxml";
        JasperReport jasperReport = JasperCompileManager.compileReport(rutaReporte);

        // Llenar el reporte con la conexión y parámetros
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, conexion);

        return jasperPrint;
    }

    public void generarYGuardarTicket(int idVenta) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("ID_VENTA", idVenta);
            parametros.put("REPORT_TITLE", "Ticket de Venta");
            parametros.put("FECHA_GENERACION", new Date());

            JasperPrint jasperPrint = generarJasperPrint("ticket_venta", parametros);

            if (jasperPrint != null) {
                // Crear estructura de fechas
                Date ahora = new Date();
                String año = new SimpleDateFormat("yyyy").format(ahora);
                String mes = new SimpleDateFormat("MM").format(ahora);
                String dia = new SimpleDateFormat("dd").format(ahora);
                String horaMinSeg = new SimpleDateFormat("HHmmss").format(ahora);

                // Construir ruta paso a paso para mejor control
                String rutaBase = "tickets_respaldo";
                String rutaAño = rutaBase + File.separator + año;
                String rutaMes = rutaAño + File.separator + mes;
                String rutaDia = rutaMes + File.separator + dia;

                // Crear cada nivel de carpeta
                crearCarpetaSiNoExiste(rutaBase);
                crearCarpetaSiNoExiste(rutaAño);
                crearCarpetaSiNoExiste(rutaMes);
                crearCarpetaSiNoExiste(rutaDia);

                // Nombre del archivo
                String nombreArchivo = String.format("ticket_venta_%d_%s%s%s_%s.pdf",
                        idVenta, año, mes, dia, horaMinSeg);

                String rutaCompleta = rutaDia + File.separator + nombreArchivo;

                // Guardar PDF
                JasperExportManager.exportReportToPdfFile(jasperPrint, rutaCompleta);

                System.out.println("✅ Ticket guardado exitosamente:");
                System.out.println("   Ruta: " + rutaCompleta);
                System.out.println("   Tamaño: " + new File(rutaCompleta).length() + " bytes");

            } else {
                System.err.println("❌ Error: No se pudo generar el JasperPrint");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al guardar ticket: " + e.getMessage());
            e.printStackTrace();
            throw e; // Propagar el error si es crítico
        }
    }

    // Método auxiliar para crear carpetas
    private void crearCarpetaSiNoExiste(String ruta) {
        File carpeta = new File(ruta);
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdir();
            if (creada) {
                System.out.println("📁 Carpeta creada: " + ruta);
            } else {
                System.err.println("❌ No se pudo crear carpeta: " + ruta);
            }
        }
    }

    public void generarYGuardarTicketReimpresion(int idVenta) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("ID_VENTA", idVenta);
            parametros.put("ES_REIMPRESION", true);
            parametros.put("MARCA_REIMPRESION", "*** REIMPRESIÓN ***");
            parametros.put("REPORT_TITLE", "Ticket de Venta - REIMPRESIÓN");
            parametros.put("FECHA_GENERACION", new Date());

            // Generar ticket con marca de reimpresión
            JasperPrint jasperPrint = generarJasperPrint("ticket_venta", parametros);

            if (jasperPrint != null) {
                // Crear estructura de fechas
                Date ahora = new Date();
                String año = new SimpleDateFormat("yyyy").format(ahora);
                String mes = new SimpleDateFormat("MM").format(ahora);
                String dia = new SimpleDateFormat("dd").format(ahora);
                String horaMinSeg = new SimpleDateFormat("HHmmss").format(ahora);

                // Construir ruta (igual que el método original)
                String rutaBase = "tickets_respaldo";
                String rutaAño = rutaBase + File.separator + año;
                String rutaMes = rutaAño + File.separator + mes;
                String rutaDia = rutaMes + File.separator + dia;

                // Crear carpetas si no existen
                crearCarpetaSiNoExiste(rutaBase);
                crearCarpetaSiNoExiste(rutaAño);
                crearCarpetaSiNoExiste(rutaMes);
                crearCarpetaSiNoExiste(rutaDia);

                // Nombre del archivo CON MARCA DE REIMPRESIÓN
                String nombreArchivo = String.format("REIMPRESION_ticket_venta_%d_%s%s%s_%s.pdf",
                        idVenta, año, mes, dia, horaMinSeg);

                String rutaCompleta = rutaDia + File.separator + nombreArchivo;

                // AQUÍ SE USAN LAS VARIABLES: Guardar PDF
                JasperExportManager.exportReportToPdfFile(jasperPrint, rutaCompleta);

                System.out.println("✅ Ticket REIMPRESIÓN guardado exitosamente:");
                System.out.println("   Ruta: " + rutaCompleta);
                System.out.println("   Tamaño: " + new File(rutaCompleta).length() + " bytes");

            } else {
                System.err.println("❌ Error: No se pudo generar el JasperPrint para reimpresión");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al guardar ticket de reimpresión: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
