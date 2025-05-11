package modelo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Información de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/proyecto2";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    // Método para obtener una conexión
    public static Connection getConnection() throws SQLException {
        try {
            // Registrar el driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Obtener y devolver la conexión
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error al cargar el driver MySQL: " + e.getMessage());
        }
    }
}
