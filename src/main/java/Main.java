import modelo.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.Insets;
import javax.swing.UIManager;
import util.ReportCompiler;
import vista.vLogin;
import vista.vPrincipal;

public class Main {

    public static void main(String[] args) {
        try {
            // Inicializar el registro de iconos de Material Design
            org.kordamp.ikonli.materialdesign2.MaterialDesignC.CACHED.getCode();
            
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

        boolean reportesCompilados = ReportCompiler.compileAllReports();
        if (!reportesCompilados) {
            System.err.println("Advertencia: Algunos reportes no se pudieron compilar.");
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
            // Si el login fue exitoso, abrir la ventana principal
            java.awt.EventQueue.invokeLater(() -> {
                new vPrincipal().setVisible(true);
            });
        } else {
            // Si el login no fue exitoso, cerrar la aplicación
            System.exit(0);
        }
    }
}
