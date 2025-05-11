package util;

import java.io.File;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

public class CompiladorReportes {
    public static void main(String[] args) {
        compileAllReports();
    }
    
    public static void compileAllReports() {
        try {
            // Definir rutas
            String baseDir = "src/main/resources/reportes/";
            String jrxmlPath = baseDir + "inventario_productos.jrxml";
            String jasperPath = baseDir + "inventario_productos.jasper";
            
            // Verificar directorio
            File dir = new File(baseDir);
            if (!dir.exists()) {
                System.out.println("Creando directorio: " + dir.getAbsolutePath());
                dir.mkdirs();
            }
            
            // Verificar archivo JRXML
            File jrxmlFile = new File(jrxmlPath);
            if (!jrxmlFile.exists()) {
                System.err.println("ERROR: No se encuentra el archivo JRXML en: " + jrxmlFile.getAbsolutePath());
                System.err.println("Directorio actual: " + new File(".").getAbsolutePath());
                return;
            }
            
            // Compilar el reporte
            System.out.println("Compilando reporte: " + jrxmlPath);
            JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
            
            // Verificar resultado
            File jasperFile = new File(jasperPath);
            if (jasperFile.exists()) {
                System.out.println("Reporte compilado exitosamente en: " + jasperFile.getAbsolutePath());
                System.out.println("Tamaño del archivo: " + jasperFile.length() + " bytes");
            } else {
                System.err.println("ERROR: No se generó el archivo compilado en: " + jasperFile.getAbsolutePath());
            }
            
        } catch (JRException e) {
            System.err.println("Error al compilar reportes: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
