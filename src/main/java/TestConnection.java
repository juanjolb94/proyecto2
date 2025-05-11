import modelo.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Si la conexión es exitosa, mostrar este mensaje
            System.out.println("Conexion exitosa a la base de datos!");
        } catch (SQLException e) {
            // Si hay un error, mostrar el mensaje de error
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}
