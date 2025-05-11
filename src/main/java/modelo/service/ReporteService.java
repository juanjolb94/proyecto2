package modelo.service;

import modelo.ProductosDAO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.text.SimpleDateFormat;
import net.sf.jasperreports.engine.util.JRLoader;
import java.io.File;
import modelo.mProducto;

public class ReporteService {

    private final ProductosDAO productosDAO;
    private static final String REPORTES_DIR = "src/main/resources/reportes/";
    private static final String REPORTE_NOMBRE = "inventario_productos";

    public ReporteService() throws SQLException {
        this.productosDAO = new ProductosDAO();
    }

    public void generarReporteInventario(String outputPath) throws Exception {
        try {
            System.out.println("============================================");
            System.out.println("Iniciando generación de reporte de inventario");
            System.out.println("Directorio actual: " + new File(".").getAbsolutePath());

            // 1. Asegurar que existen los directorios
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
                System.out.println("Creado directorio de salida: " + outputFile.getParentFile().getAbsolutePath());
            }

            // 2. Intentar compilar el reporte forzosamente
            try {
                System.out.println("Forzando compilación del reporte JRXML...");
                forzarCompilacionJRXML();
            } catch (Exception e) {
                System.err.println("Error durante la compilación forzada: " + e.getMessage());
                System.err.println("Intentando continuar con el proceso...");
            }

            // 3. Obtener datos para el reporte
            System.out.println("Obteniendo datos para el reporte...");
            List<mProducto> productos = productosDAO.listarTodos();
            List<Map<String, Object>> datosReporte = new ArrayList<>();

            for (mProducto producto : productos) {
                datosReporte.add(crearFilaReporte(producto));
            }
            System.out.println("Datos obtenidos: " + datosReporte.size() + " productos");

            // 4. Preparar parámetros del reporte
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("REPORT_TITLE", "INVENTARIO DE PRODUCTOS");
            parametros.put("FECHA_GENERACION", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

            // 5. Generar el reporte
            System.out.println("Generando JasperPrint...");
            JasperPrint jasperPrint = generarJasperPrint(parametros, datosReporte);

            // 6. Exportar a PDF
            System.out.println("Exportando a PDF: " + outputPath);
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            // 7. Verificar el archivo generado
            if (new File(outputPath).exists()) {
                System.out.println("Reporte generado exitosamente: " + new File(outputPath).getAbsolutePath());
                System.out.println("Tamaño del archivo: " + new File(outputPath).length() + " bytes");
            } else {
                throw new Exception("El archivo PDF no se generó correctamente en: " + outputPath);
            }

            System.out.println("Proceso de generación de reporte completado");
            System.out.println("============================================");

        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al acceder a los datos: " + e.getMessage(), e);
        } catch (JRException e) {
            System.err.println("Error de JasperReports: " + e.getMessage());
            if (e.getCause() != null) {
                e.printStackTrace();
            }
            throw new Exception("Error al generar el reporte: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Método para compilar el reporte si es necesario
    private void compilarReporteSiEsNecesario() throws JRException {
        File jrxmlFile = new File(REPORTES_DIR + REPORTE_NOMBRE + ".jrxml");
        File jasperFile = new File(REPORTES_DIR + REPORTE_NOMBRE + ".jasper");

        if (!jasperFile.exists()
                || jrxmlFile.lastModified() > jasperFile.lastModified()) {

            System.out.println("Compilando reporte...");
            JasperCompileManager.compileReportToFile(
                    REPORTES_DIR + REPORTE_NOMBRE + ".jrxml",
                    REPORTES_DIR + REPORTE_NOMBRE + ".jasper"
            );
        }
    }

    // Método para crear una fila del reporte
    private Map<String, Object> crearFilaReporte(mProducto producto) throws SQLException {
        Map<String, Object> fila = new HashMap<>();
        fila.put("idProducto", producto.getIdProducto());
        fila.put("nombre", producto.getNombre());
        fila.put("categoria", obtenerNombreCategoria(producto.getIdCategoria()));
        fila.put("marca", obtenerNombreMarca(producto.getIdMarca()));
        fila.put("iva", producto.getIva());
        fila.put("estado", producto.isEstado() ? "Activo" : "Inactivo");
        return fila;
    }

    // Método para generar el JasperPrint
    private JasperPrint generarJasperPrint(Map<String, Object> parametros,
            List<Map<String, Object>> datos) throws JRException {
        // Intentar diferentes ubicaciones para el archivo jasper
        JasperReport jasperReport = null;
        Exception lastException = null;

        // Lista de posibles ubicaciones
        String[] posiblesUbicaciones = {
            REPORTES_DIR + REPORTE_NOMBRE + ".jasper",
            "reportes/" + REPORTE_NOMBRE + ".jasper",
            "src/main/resources/reportes/" + REPORTE_NOMBRE + ".jasper",
            "./reportes/" + REPORTE_NOMBRE + ".jasper"
        };

        // Intentar cargar desde archivo
        for (String path : posiblesUbicaciones) {
            File jasperFile = new File(path);
            if (jasperFile.exists()) {
                try {
                    System.out.println("Intentando cargar archivo jasper desde: " + jasperFile.getAbsolutePath());
                    jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);
                    System.out.println("Archivo jasper cargado exitosamente.");
                    break;
                } catch (JRException e) {
                    lastException = e;
                    System.err.println("Error al cargar desde " + path + ": " + e.getMessage());
                }
            } else {
                System.out.println("Archivo no encontrado en: " + jasperFile.getAbsolutePath());
            }
        }

        // Intentar cargar desde recursos si falló la carga desde archivo
        if (jasperReport == null) {
            try {
                System.out.println("Intentando cargar jasper desde recursos del classpath...");
                // Intenta con varias rutas de recursos
                String[] resourcePaths = {
                    "/reportes/" + REPORTE_NOMBRE + ".jasper",
                    "/resources/reportes/" + REPORTE_NOMBRE + ".jasper",
                    "/" + REPORTE_NOMBRE + ".jasper"
                };

                for (String resourcePath : resourcePaths) {
                    try {
                        System.out.println("Intentando cargar recurso: " + resourcePath);
                        InputStream jasperStream = getClass().getResourceAsStream(resourcePath);

                        if (jasperStream != null) {
                            jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
                            System.out.println("Archivo jasper cargado exitosamente desde recurso: " + resourcePath);
                            break;
                        } else {
                            System.out.println("Recurso no encontrado: " + resourcePath);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al cargar recurso " + resourcePath + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("Error al cargar desde recursos: " + e.getMessage());
            }
        }

        // Último recurso: compilar el JRXML durante la ejecución
        if (jasperReport == null) {
            try {
                System.out.println("Intentando compilar JRXML en tiempo de ejecución...");
                // Intentar varias ubicaciones para el JRXML
                String[] jrxmlPaths = {
                    REPORTES_DIR + REPORTE_NOMBRE + ".jrxml",
                    "reportes/" + REPORTE_NOMBRE + ".jrxml",
                    "src/main/resources/reportes/" + REPORTE_NOMBRE + ".jrxml",
                    "./reportes/" + REPORTE_NOMBRE + ".jrxml"
                };

                for (String jrxmlPath : jrxmlPaths) {
                    File jrxmlFile = new File(jrxmlPath);
                    if (jrxmlFile.exists()) {
                        System.out.println("Compilando JRXML encontrado en: " + jrxmlFile.getAbsolutePath());
                        jasperReport = JasperCompileManager.compileReport(jrxmlFile.getAbsolutePath());
                        System.out.println("JRXML compilado exitosamente en memoria.");
                        break;
                    }
                }

                // Intentar desde recursos si no se encontró el archivo
                if (jasperReport == null) {
                    InputStream jrxmlStream = getClass().getResourceAsStream("/reportes/" + REPORTE_NOMBRE + ".jrxml");
                    if (jrxmlStream != null) {
                        System.out.println("Compilando JRXML desde recurso...");
                        jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                        System.out.println("JRXML compilado exitosamente desde recurso.");
                    }
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("Error al compilar JRXML en tiempo de ejecución: " + e.getMessage());
            }
        }

        // Si todavía no tenemos el reporte, fallar con mensaje detallado
        if (jasperReport == null) {
            StringBuilder errorMsg = new StringBuilder("No se encontró el archivo de reporte compilado. ");
            errorMsg.append("Directorio de trabajo: ").append(new File(".").getAbsolutePath());
            errorMsg.append("\nSe intentó buscar en las siguientes ubicaciones:");
            for (String path : posiblesUbicaciones) {
                errorMsg.append("\n - ").append(path);
                errorMsg.append(" (").append(new File(path).exists() ? "Existe" : "No existe").append(")");
            }

            if (lastException != null) {
                errorMsg.append("\nÚltimo error: ").append(lastException.getMessage());
            }

            throw new JRException(errorMsg.toString());
        }

        // Llenar el reporte con los datos
        return JasperFillManager.fillReport(
                jasperReport,
                parametros,
                new JRBeanCollectionDataSource(datos)
        );
    }

    private void forzarCompilacionJRXML() throws JRException {
        String jrxmlPath = null;
        Exception lastException = null;
        boolean compiled = false;

        // Lista de posibles ubicaciones del JRXML
        String[] posiblesJrxml = {
            REPORTES_DIR + REPORTE_NOMBRE + ".jrxml",
            "reportes/" + REPORTE_NOMBRE + ".jrxml",
            "src/main/resources/reportes/" + REPORTE_NOMBRE + ".jrxml",
            "./reportes/" + REPORTE_NOMBRE + ".jrxml"
        };

        // Probar cada ubicación
        for (String path : posiblesJrxml) {
            File jrxmlFile = new File(path);
            if (jrxmlFile.exists()) {
                jrxmlPath = path;
                System.out.println("JRXML encontrado en: " + jrxmlFile.getAbsolutePath());

                // Intentar compilar a varias ubicaciones
                String[] posiblesSalidas = {
                    REPORTES_DIR + REPORTE_NOMBRE + ".jasper",
                    "reportes/" + REPORTE_NOMBRE + ".jasper",
                    "src/main/resources/reportes/" + REPORTE_NOMBRE + ".jasper",
                    "./reportes/" + REPORTE_NOMBRE + ".jasper"
                };

                for (String outputPath : posiblesSalidas) {
                    try {
                        // Asegurar que el directorio de salida existe
                        File outputFile = new File(outputPath);
                        File outputDir = outputFile.getParentFile();
                        if (!outputDir.exists()) {
                            outputDir.mkdirs();
                        }

                        // Compilar el reporte
                        System.out.println("Compilando de " + jrxmlPath + " a " + outputPath);
                        JasperCompileManager.compileReportToFile(jrxmlPath, outputPath);

                        // Verificar que se haya creado el archivo
                        if (new File(outputPath).exists()) {
                            System.out.println("Compilación exitosa en: " + outputPath);
                            compiled = true;
                            break;
                        }
                    } catch (Exception e) {
                        lastException = e;
                        System.err.println("Error compilando a " + outputPath + ": " + e.getMessage());
                    }
                }

                // Si se compiló exitosamente, salir
                if (compiled) {
                    break;
                }
            }
        }

        // Si no se encontró o compiló el archivo, lanzar excepción
        if (!compiled) {
            StringBuilder errorMsg = new StringBuilder("No se pudo compilar el reporte JRXML. ");
            if (jrxmlPath == null) {
                errorMsg.append("No se encontró el archivo JRXML en ninguna ubicación conocida.");
            } else {
                errorMsg.append("El archivo JRXML fue encontrado en ").append(jrxmlPath);
                errorMsg.append(" pero falló la compilación.");
            }

            if (lastException != null) {
                errorMsg.append("\nÚltimo error: ").append(lastException.getMessage());
            }

            throw new JRException(errorMsg.toString());
        }
    }

    // Métodos auxiliares para obtener nombres de categoría y marca
    private String obtenerNombreCategoria(int idCategoria) throws SQLException {
        // Implementación real debería consultar la base de datos
        return "Categoría " + idCategoria;
    }

    private String obtenerNombreMarca(int idMarca) throws SQLException {
        // Implementación real debería consultar la base de datos
        return "Marca " + idMarca;
    }

    // Clase wrapper alternativa para el reporte
    public static class ProductoReporte {

        private final mProducto producto;

        public ProductoReporte(mProducto producto) {
            this.producto = producto;
        }

        public Integer getIdProducto() {
            return producto.getIdProducto();
        }

        public String getNombre() {
            return producto.getNombre();
        }

        public String getCategoria() {
            return "Categoría " + producto.getIdCategoria();
        }

        public String getMarca() {
            return "Marca " + producto.getIdMarca();
        }

        public Double getIva() {
            return producto.getIva();
        }

        public String getEstado() {
            return producto.isEstado() ? "Activo" : "Inactivo";
        }
    }

    // Método alternativo usando la clase wrapper
    public void generarReporteInventarioAlternativo(String outputPath) throws Exception {
        try {
            compilarReporteSiEsNecesario();

            List<mProducto> productos = productosDAO.listarTodos();
            List<ProductoReporte> productosReporte = new ArrayList<>();

            for (mProducto p : productos) {
                productosReporte.add(new ProductoReporte(p));
            }

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("REPORT_TITLE", "INVENTARIO - VERSIÓN ALTERNATIVA");
            parametros.put("FECHA_GENERACION", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

            JasperPrint jasperPrint = generarJasperPrint(
                    parametros,
                    productosReporte.stream()
                            .map(p -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("idProducto", p.getIdProducto());
                                map.put("nombre", p.getNombre());
                                map.put("categoria", p.getCategoria());
                                map.put("marca", p.getMarca());
                                map.put("iva", p.getIva());
                                map.put("estado", p.getEstado());
                                return map;
                            })
                            .toList()
            );

            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        } catch (Exception e) {
            throw new Exception("Error generando reporte alternativo: " + e.getMessage(), e);
        }
    }

    // Método estático para compilación manual desde otras clases
    public static void compilarReporte() throws JRException {
        String jrxmlPath = "src/main/resources/reportes/inventario_productos.jrxml";
        String jasperPath = "src/main/resources/reportes/inventario_productos.jasper";

        System.out.println("Compilando reporte: " + jrxmlPath);
        JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
        System.out.println("Reporte compilado en: " + jasperPath);
    }
}
