package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClientesDAO {

    // Método para buscar un cliente por ID
    public Object[] buscarClientePorId(int id) throws SQLException {
        String sql = "SELECT c.id_cliente, c.nombre, c.ci_ruc, c.telefono, c.direccion, c.email, c.estado, c.id_persona "
                + "FROM clientes c WHERE c.id_cliente = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Object[]{
                        resultSet.getInt("id_cliente"),
                        resultSet.getString("nombre"),
                        resultSet.getString("ci_ruc"),
                        resultSet.getString("telefono"),
                        resultSet.getString("direccion"),
                        resultSet.getString("email"),
                        resultSet.getBoolean("estado") ? "1" : "0",
                        resultSet.getInt("id_persona")
                    };
                }
            }
        }
        return null; // Retorna null si no se encuentra el cliente
    }

    // Método para listar todos los clientes
    public List<Object[]> listarClientes() throws SQLException {
        List<Object[]> clientes = new ArrayList<>();
        String sql = "SELECT c.id_cliente, c.nombre, c.ci_ruc, c.telefono, c.direccion, c.email, c.estado, c.id_persona "
                + "FROM clientes c ORDER BY c.nombre";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Object[] cliente = {
                    resultSet.getInt("id_cliente"),
                    resultSet.getString("nombre"),
                    resultSet.getString("ci_ruc"),
                    resultSet.getString("telefono"),
                    resultSet.getString("direccion"),
                    resultSet.getString("email"),
                    resultSet.getBoolean("estado") ? "1" : "0",
                    resultSet.getInt("id_persona")
                };
                clientes.add(cliente);
            }
        }

        return clientes;
    }

    // Método para insertar un nuevo cliente
    public boolean insertarCliente(String nombre, String ci, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, ci_ruc, telefono, direccion, email, estado, id_persona) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombre);
            statement.setString(2, ci);
            statement.setString(3, telefono);
            statement.setString(4, direccion);
            statement.setString(5, email);
            statement.setBoolean(6, estado);
            statement.setInt(7, idPersona);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Método para actualizar un cliente existente
    public boolean actualizarCliente(int id, String nombre, String ci, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, ci_ruc = ?, telefono = ?, "
                + "direccion = ?, email = ?, estado = ?, id_persona = ? "
                + "WHERE id_cliente = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombre);
            statement.setString(2, ci);
            statement.setString(3, telefono);
            statement.setString(4, direccion);
            statement.setString(5, email);
            statement.setBoolean(6, estado);
            statement.setInt(7, idPersona);
            statement.setInt(8, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Método para verificar si un cliente tiene ventas asociadas
    public boolean tieneVentasAsociadas(int idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE id_cliente = ? AND anulado = false";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCliente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para obtener el conteo de ventas de un cliente
    public int contarVentasCliente(int idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE id_cliente = ? AND anulado = false";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCliente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    // Método para verificar si es cliente ocasional (no se puede eliminar)
    public boolean esClienteOcasional(int idCliente) throws SQLException {
        String sql = "SELECT ci_ruc FROM clientes WHERE id_cliente = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCliente);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String ciRuc = resultSet.getString("ci_ruc");
                    return "9999999-9".equals(ciRuc); // Cliente ocasional
                }
            }
        }
        return false;
    }

    // Método para eliminar un cliente
    public boolean eliminarCliente(int id) throws SQLException {
        // Validar si es cliente ocasional
        if (esClienteOcasional(id)) {
            throw new SQLException("No se puede eliminar el cliente ocasional del sistema.");
        }

        // Validar si tiene ventas asociadas
        if (tieneVentasAsociadas(id)) {
            int cantidadVentas = contarVentasCliente(id);
            throw new SQLException("No se puede eliminar el cliente: tiene " + cantidadVentas + " venta(s) registrada(s). "
                    + "Considere desactivar el cliente en lugar de eliminarlo.");
        }

        String sql = "DELETE FROM clientes WHERE id_cliente = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Método para obtener el primer registro
    public Object[] obtenerPrimerCliente() throws SQLException {
        String sql = "SELECT id_cliente, nombre, ci_ruc, telefono, direccion, email, estado, id_persona "
                + "FROM clientes ORDER BY id_cliente ASC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new Object[]{
                    resultSet.getInt("id_cliente"),
                    resultSet.getString("nombre"),
                    resultSet.getString("ci_ruc"),
                    resultSet.getString("telefono"),
                    resultSet.getString("direccion"),
                    resultSet.getString("email"),
                    resultSet.getBoolean("estado") ? "1" : "0",
                    resultSet.getInt("id_persona")
                };
            }
        }

        return null; // Retorna null si no hay registros
    }

    // Método para obtener el registro anterior
    public Object[] obtenerAnteriorCliente(int idActual) throws SQLException {
        String sql = "SELECT id_cliente, nombre, ci_ruc, telefono, direccion, email, estado, id_persona "
                + "FROM clientes WHERE id_cliente < ? ORDER BY id_cliente DESC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActual);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Object[]{
                        resultSet.getInt("id_cliente"),
                        resultSet.getString("nombre"),
                        resultSet.getString("ci_ruc"),
                        resultSet.getString("telefono"),
                        resultSet.getString("direccion"),
                        resultSet.getString("email"),
                        resultSet.getBoolean("estado") ? "1" : "0",
                        resultSet.getInt("id_persona")
                    };
                }
            }
        }

        return null; // Retorna null si no hay registros anteriores
    }

    // Método para obtener el siguiente registro
    public Object[] obtenerSiguienteCliente(int idActual) throws SQLException {
        String sql = "SELECT id_cliente, nombre, ci_ruc, telefono, direccion, email, estado, id_persona "
                + "FROM clientes WHERE id_cliente > ? ORDER BY id_cliente ASC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActual);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Object[]{
                        resultSet.getInt("id_cliente"),
                        resultSet.getString("nombre"),
                        resultSet.getString("ci_ruc"),
                        resultSet.getString("telefono"),
                        resultSet.getString("direccion"),
                        resultSet.getString("email"),
                        resultSet.getBoolean("estado") ? "1" : "0",
                        resultSet.getInt("id_persona")
                    };
                }
            }
        }

        return null; // Retorna null si no hay registros siguientes
    }

    // Método para obtener el último registro
    public Object[] obtenerUltimoCliente() throws SQLException {
        String sql = "SELECT id_cliente, nombre, ci_ruc, telefono, direccion, email, estado, id_persona "
                + "FROM clientes ORDER BY id_cliente DESC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new Object[]{
                    resultSet.getInt("id_cliente"),
                    resultSet.getString("nombre"),
                    resultSet.getString("ci_ruc"),
                    resultSet.getString("telefono"),
                    resultSet.getString("direccion"),
                    resultSet.getString("email"),
                    resultSet.getBoolean("estado") ? "1" : "0",
                    resultSet.getInt("id_persona")
                };
            }
        }

        return null; // Retorna null si no hay registros
    }

    // Método para verificar si existe un cliente con el mismo CI/RUC
    public boolean existeClienteConCI(String ci) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE ci_ruc = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ci);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // Método para verificar si existe un cliente asociado a una persona
    public boolean existeClienteConPersona(int idPersona) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE id_persona = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPersona);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }
}
