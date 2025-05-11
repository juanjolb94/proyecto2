package util;

import java.io.File;
import java.net.URL;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

public class ReportCompiler {
    public static void compileReport(String jrxmlPath, String jasperOutputPath) throws JRException {
        File jrxmlFile = new File(jrxmlPath);

        if (!jrxmlFile.exists()) {
            System.err.println("ARCHIVO NO ENCONTRADO: " + jrxmlFile.getAbsolutePath());

            // Intentar ubicar el archivo mediante ClassLoader (útil cuando está empaquetado como JAR)
            URL resourceUrl = ReportCompiler.class.getClassLoader().getResource(
                    jrxmlPath.replace("src/main/resources/", ""));

            if (resourceUrl != null) {
                System.out.println("Recurso encontrado vía ClassLoader: " + resourceUrl);
                // Aquí podrías manejar la compilación usando la URL
            } else {
                throw new JRException("No se pudo encontrar el archivo JRXML: " + jrxmlPath);
            }
        }
    }

    public static boolean compileAllReports() {
        boolean todosCompilados = true;
        try {
            // Crear directorio si no existe
            File reportDir = new File("src/main/resources/reportes/");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            // Compilar reportes
            compileReport(
                    "src/main/resources/reportes/inventario_productos.jrxml",
                    "src/main/resources/reportes/inventario_productos.jasper"
            );

        } catch (JRException e) {
            System.err.println("Error al compilar reportes: " + e.getMessage());
            e.printStackTrace();
            todosCompilados = false;
        }
        return todosCompilados;
    }
}
