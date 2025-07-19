
import modelo.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.Insets;
import javax.swing.UIManager;
import util.ReportCompiler;
import util.LogManager;
import vista.vBienvenida;
import vista.vLogin;
import vista.vPrincipal;

public class Main {

    public static void main(String[] args) {
        // AGREGAR SHUTDOWN HOOK PARA CAPTURAR CIERRE INESPERADO
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LogManager.getInstance().logLogin("LOGOUT",
                        "Aplicación cerrada - Usuario: " + vLogin.getUsuarioAutenticado());
                LogManager.getInstance().cerrarSesion();
            } catch (Exception e) {
                System.err.println("Error al registrar cierre: " + e.getMessage());
            }
        }));

        try {
            // Forzar inicialización completa de Material Design Icons
            try {
                // Detectar si estamos ejecutando desde JAR
                String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                boolean ejecutandoDesdeJar = jarPath.endsWith(".jar");

                if (ejecutandoDesdeJar) {
                    System.out.println("Inicializando iconos Material Design para JAR...");

                    // Forzar carga de todas las categorías de iconos
                    org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignB.BARCODE_SCAN.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignD.DELETE_OUTLINE.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignE.EXIT_TO_APP.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_DOCUMENT_OUTLINE.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignH.HELP_CIRCLE_OUTLINE.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignK.KEY.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignM.MAGNIFY.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignP.PACKAGE.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignS.SHIELD.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignT.TAG_MULTIPLE.getCode();
                    org.kordamp.ikonli.materialdesign2.MaterialDesignW.WINDOW_CLOSE.getCode();

                    System.out.println("✅ Material Design Icons inicializados para JAR");
                } else {
                    // Inicialización normal para desarrollo
                    org.kordamp.ikonli.materialdesign2.MaterialDesignC.CACHED.getCode();
                    System.out.println("✅ Material Design Icons inicializados para desarrollo");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al inicializar iconos Material Design: " + e.getMessage());
                e.printStackTrace();
            }

            // Configurar propiedades globales de UI
            UIManager.put("Button.arc", 10); // Bordes redondeados para botones
            UIManager.put("Component.arc", 5); // Bordes redondeados para componentes
            UIManager.put("TextComponent.arc", 5); // Bordes redondeados para campos de texto
            UIManager.put("ScrollBar.thumbArc", 999); // Barras de desplazamiento redondeadas
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            UIManager.put("TabbedPane.tabsOverlapBorder", true);

            // Configurar tema oscuro de IntelliJ
            com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.setup();

            // El siguiente código comentado es para el tema claro, por si quieres volver a él
            // com.formdev.flatlaf.FlatLightLaf.setup();
            System.out.println("Look and Feel aplicado: " + UIManager.getLookAndFeel().getName());
        } catch (Exception ex) {
            System.err.println("Error al configurar el Look and Feel: " + ex.getMessage());
            ex.printStackTrace();
        }

        //boolean reportesCompilados = ReportCompiler.compileAllReports();
        //if (!reportesCompilados) {
        //    System.err.println("Advertencia: Algunos reportes no se pudieron compilar.");
        //}
        // Usar una verificación para determinar si estamos en JAR:
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        boolean ejecutandoDesdeJar = jarPath.endsWith(".jar");

        if (!ejecutandoDesdeJar) {
            // Solo compilar reportes en desarrollo
            boolean reportesCompilados = ReportCompiler.compileAllReports();
            if (!reportesCompilados) {
                System.err.println("Advertencia: Algunos reportes no se pudieron compilar.");
            }
        } else {
            System.out.println("Ejecutando desde JAR - usando reportes pre-compilados");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Conexion exitosa a la base de datos!");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Crear e iniciar el diálogo de login (modal)
        vLogin login = new vLogin(null, true);
        login.setVisible(true); // Bloquea hasta que se cierre el login

        // Verificar si el login fue exitoso
        if (login.isLoginExitoso()) {
            // ✅ LOG LOGIN EXITOSO
            LogManager.getInstance().logLogin("LOGIN_EXITOSO",
                    "Usuario " + vLogin.getUsuarioAutenticado() + " inició sesión correctamente");

            // ✅ MOSTRAR VENTANA DE BIENVENIDA
            vBienvenida bienvenida = new vBienvenida(
                    null,
                    vLogin.getUsuarioAutenticado(),
                    vLogin.getRolAutenticado()
            );
            bienvenida.setVisible(true);

            // Verificar si el usuario continuó
            if (bienvenida.isContinuar()) {
                // Si continuó, abrir la ventana principal
                LogManager.getInstance().log(
                        LogManager.Modulo.SISTEMA,
                        "APLICACION_INICIADA",
                        LogManager.Nivel.INFO,
                        "Ventana principal abierta correctamente"
                );

                java.awt.EventQueue.invokeLater(() -> {
                    new vPrincipal().setVisible(true);
                });
            } else {
                // ✅ LOG CIERRE SIN CONTINUAR
                LogManager.getInstance().logLogin("LOGOUT",
                        "Usuario " + vLogin.getUsuarioAutenticado() + " cerró sin continuar a la aplicación");
                LogManager.getInstance().cerrarSesion();
                System.exit(0);
            }
        } else {
            // ✅ LOG LOGIN FALLIDO
            LogManager.getInstance().logLogin("LOGIN_FALLIDO",
                    "Intento de login fallido - Aplicación cerrada");
            System.exit(0);
        }
    }
}
