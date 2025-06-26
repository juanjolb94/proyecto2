package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolesDAO {

    // Método para buscar un rol por ID
    public String[] buscarRolPorId(int id) throws SQLException {
        String sql = "SELECT id_rol, nombre, activo FROM roles WHERE id_rol = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String rolId = resultSet.getString("id_rol");
                    String nombre = resultSet.getString("nombre");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{rolId, nombre, activo};
                }
            }
        }
        return null; // Retorna null si no se encuentra el rol
    }

    // Método para obtener el primer registro
    public String[] obtenerPrimerRol() throws SQLException {
        String sql = "SELECT id_rol, nombre, activo FROM roles ORDER BY id_rol ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String id = resultSet.getString("id_rol");
                String nombre = resultSet.getString("nombre");
                String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                return new String[]{id, nombre, activo};
            }
        }
        return null; // Retorna null si no hay registros
    }

    // Método para obtener el registro anterior
    public String[] obtenerAnteriorRol(int idActual) throws SQLException {
        String sql = "SELECT id_rol, nombre, activo FROM roles WHERE id_rol < ? ORDER BY id_rol DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idActual);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String id = resultSet.getString("id_rol");
                    String nombre = resultSet.getString("nombre");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{id, nombre, activo};
                }
            }
        }
        return null; // Retorna null si no hay registros anteriores
    }

    // Método para obtener el siguiente registro
    public String[] obtenerSiguienteRol(int idActual) throws SQLException {
        String sql = "SELECT id_rol, nombre, activo FROM roles WHERE id_rol > ? ORDER BY id_rol ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idActual);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String id = resultSet.getString("id_rol");
                    String nombre = resultSet.getString("nombre");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{id, nombre, activo};
                }
            }
        }
        return null; // Retorna null si no hay registros siguientes
    }

    // Método para obtener el último registro
    public String[] obtenerUltimoRol() throws SQLException {
        String sql = "SELECT id_rol, nombre, activo FROM roles ORDER BY id_rol DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String id = resultSet.getString("id_rol");
                String nombre = resultSet.getString("nombre");
                String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                return new String[]{id, nombre, activo};
            }
        }
        return null; // Retorna null si no hay registros
    }

    public void eliminarRol(int id) throws SQLException {
        String sql = "DELETE FROM roles WHERE id_rol = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void insertarRol(String nombre, boolean activo) throws SQLException {
        String sql = "INSERT INTO roles (nombre, activo) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setBoolean(2, activo);
            statement.executeUpdate();
        }
    }

    public void actualizarRol(int id, String nombre, boolean activo) throws SQLException {
        String sql = "UPDATE roles SET nombre = ?, activo = ? WHERE id_rol = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setBoolean(2, activo);
            statement.setInt(3, id);
            statement.executeUpdate();
        }
    }

    public ResultSet obtenerNombresRoles() throws SQLException {
        String sql = "SELECT id_rol, nombre FROM roles WHERE activo = 1";
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.executeQuery();
    }
}
