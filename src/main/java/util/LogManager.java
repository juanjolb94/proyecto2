package util;

import modelo.DatabaseConnection;
import vista.vLogin;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class LogManager {

    private static LogManager instance;
    private static String sessionId;
    private static final String LOG_FILE = "logs/gcsys_app.log";

    public enum Nivel {
        INFO, WARNING, ERROR, CRITICAL, AUDIT
    }

    public enum Modulo {
        LOGIN("LOGIN"),
        VENTAS("VENTAS"),
        COMPRAS("COMPRAS"),
        PRODUCTOS("PRODUCTOS"),
        USUARIOS("USUARIOS"),
        CAJA("CAJA"),
        REPORTES("REPORTES"),
        SISTEMA("SISTEMA");

        private final String nombre;

        Modulo(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private LogManager() {
        inicializarDirectorioLogs();
        sessionId = UUID.randomUUID().toString();
    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    // Método principal para registrar logs
    public void log(Modulo modulo, String accion, Nivel nivel, String descripcion) {
        log(modulo, accion, nivel, descripcion, null, null, null, null);
    }

    public void log(Modulo modulo, String accion, Nivel nivel, String descripcion,
            String datosAnteriores, String datosNuevos, String tablaAfectada, Integer registroId) {

        // Log en consola para desarrollo
        logConsola(modulo, accion, nivel, descripcion);

        // Log en archivo
        logArchivo(modulo, accion, nivel, descripcion);

        // Log en base de datos para auditoría
        logBaseDatos(modulo, accion, nivel, descripcion, datosAnteriores, datosNuevos, tablaAfectada, registroId);
    }

    // Métodos específicos para cada tipo de operación
    public void logVenta(String accion, String descripcion, String datosAnteriores, String datosNuevos, Integer ventaId) {
        log(Modulo.VENTAS, accion, Nivel.AUDIT, descripcion, datosAnteriores, datosNuevos, "ventas", ventaId);
    }

    public void logCompra(String accion, String descripcion, Integer compraId) {
        log(Modulo.COMPRAS, accion, Nivel.AUDIT, descripcion, null, null, "compras_cabecera", compraId);
    }

    public void logUsuario(String accion, String descripcion, Integer usuarioId) {
        log(Modulo.USUARIOS, accion, Nivel.AUDIT, descripcion, null, null, "usuarios", usuarioId);
    }

    public void logError(String clase, String metodo, Exception e) {
        logError(clase, metodo, e.getMessage(), getStackTrace(e), null);
    }

    public void logLogin(String accion, String descripcion) {
        log(Modulo.LOGIN, accion, Nivel.AUDIT, descripcion);

        if ("LOGIN_EXITOSO".equals(accion)) {
            registrarSesion();
        }
    }

    private void logConsola(Modulo modulo, String accion, Nivel nivel, String descripcion) {
        String timestamp = LocalDateTime.now().toString();
        String logMessage = String.format("[%s] [%s] [%s] %s: %s",
                timestamp, nivel, modulo.getNombre(), accion, descripcion);

        switch (nivel) {
            case ERROR:
            case CRITICAL:
                System.err.println(logMessage);
                break;
            default:
                System.out.println(logMessage);
        }
    }

    private void logArchivo(Modulo modulo, String accion, Nivel nivel, String descripcion) {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));

            String timestamp = LocalDateTime.now().toString();
            String logMessage = String.format("[%s] [%s] [%s] %s: %s%n",
                    timestamp, nivel, modulo.getNombre(), accion, descripcion);

            java.nio.file.Files.write(
                    java.nio.file.Paths.get(LOG_FILE),
                    logMessage.getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            System.err.println("Error escribiendo log a archivo: " + e.getMessage());
        }
    }

    private void logBaseDatos(Modulo modulo, String accion, Nivel nivel, String descripcion,
            String datosAnteriores, String datosNuevos, String tablaAfectada, Integer registroId) {
        String sql = """
        INSERT INTO sistema_logs 
        (usuario_id, usuario_nombre, modulo, accion, nivel, descripcion, 
         datos_anteriores, datos_nuevos, session_id, tabla_afectada, registro_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            Integer usuarioId = vLogin.getIdUsuarioAutenticado();
            String usuarioNombre = vLogin.getUsuarioAutenticado();

            ps.setObject(1, usuarioId);
            ps.setString(2, usuarioNombre);
            ps.setString(3, modulo.getNombre());
            ps.setString(4, accion);
            ps.setString(5, nivel.toString());
            ps.setString(6, descripcion);
            ps.setString(7, datosAnteriores);
            ps.setString(8, datosNuevos);
            ps.setString(9, sessionId);
            ps.setString(10, tablaAfectada);
            ps.setObject(11, registroId);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error registrando log en BD: " + e.getMessage());
        }
    }

    private void logError(String clase, String metodo, String mensaje, String stackTrace, String parametros) {
        String sql = """
            INSERT INTO sistema_errores 
            (usuario_id, clase_java, metodo, mensaje_error, stack_trace, parametros_entrada)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            Integer usuarioId = vLogin.getIdUsuarioAutenticado();

            ps.setObject(1, usuarioId);
            ps.setString(2, clase);
            ps.setString(3, metodo);
            ps.setString(4, mensaje);
            ps.setString(5, stackTrace);
            ps.setString(6, parametros);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error registrando error en BD: " + e.getMessage());
        }
    }

    private void registrarSesion() {
        String sql = """
            INSERT INTO sistema_sesiones (session_id, usuario_id, ip_address)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            Integer usuarioId = vLogin.getIdUsuarioAutenticado();

            ps.setString(1, sessionId);
            ps.setInt(2, usuarioId);
            ps.setString(3, "localhost"); // Puedes obtener la IP real si necesitas

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error registrando sesión: " + e.getMessage());
        }
    }

    public void cerrarSesion() {
        String sql = "UPDATE sistema_sesiones SET fecha_logout = NOW(), activa = FALSE WHERE session_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sessionId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error cerrando sesión: " + e.getMessage());
        }
    }

    private void inicializarDirectorioLogs() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));
        } catch (Exception e) {
            System.err.println("Error creando directorio de logs: " + e.getMessage());
        }
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
