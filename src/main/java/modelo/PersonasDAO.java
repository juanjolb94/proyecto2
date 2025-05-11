package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonasDAO {
    // Método para buscar una persona por ID
    public String[] buscarPersonaPorId(int id) throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido, ci, telefono, correo, fecha_nac, activo FROM personas WHERE id_persona = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String idPersona = resultSet.getString("id_persona");
                    String nombre = resultSet.getString("nombre");
                    String apellido = resultSet.getString("apellido");
                    String ci = resultSet.getString("ci");
                    String telefono = resultSet.getString("telefono");
                    String correo = resultSet.getString("correo");
                    String fechaNac = resultSet.getString("fecha_nac");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{idPersona, nombre, apellido, ci, telefono, correo, fechaNac, activo};
                }
            }
        }
        return null; // Retorna null si no se encuentra la persona
    }
    
    // Método para obtener el primer registro
    public String[] obtenerPrimerPersona() throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido, ci, telefono, correo, fecha_nac, activo FROM personas ORDER BY id_persona ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String id = resultSet.getString("id_persona");
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");
                String ci = resultSet.getString("ci");
                String telefono = resultSet.getString("telefono");
                String correo = resultSet.getString("correo");
                String fechaNac = resultSet.getString("fecha_nac");
                String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                return new String[]{id, nombre, apellido, ci, telefono, correo, fechaNac, activo};
            }
        }
        return null; // Retorna null si no hay registros
    }
    
    // Método para obtener el registro anterior
    public String[] obtenerAnteriorPersona(int idActual) throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido, ci, telefono, correo, fecha_nac, activo FROM personas WHERE id_persona < ? ORDER BY id_persona DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idActual);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String id = resultSet.getString("id_persona");
                    String nombre = resultSet.getString("nombre");
                    String apellido = resultSet.getString("apellido");
                    String ci = resultSet.getString("ci");
                    String telefono = resultSet.getString("telefono");
                    String correo = resultSet.getString("correo");
                    String fechaNac = resultSet.getString("fecha_nac");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{id, nombre, apellido, ci, telefono, correo, fechaNac, activo};
                }
            }
        }
        return null; // Retorna null si no hay registros anteriores
    }
    
    // Método para obtener el siguiente registro
    public String[] obtenerSiguientePersona(int idActual) throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido, ci, telefono, correo, fecha_nac, activo FROM personas WHERE id_persona > ? ORDER BY id_persona ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idActual);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String id = resultSet.getString("id_persona");
                    String nombre = resultSet.getString("nombre");
                    String apellido = resultSet.getString("apellido");
                    String ci = resultSet.getString("ci");
                    String telefono = resultSet.getString("telefono");
                    String correo = resultSet.getString("correo");
                    String fechaNac = resultSet.getString("fecha_nac");
                    String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                    return new String[]{id, nombre, apellido, ci, telefono, correo, fechaNac, activo};
                }
            }
        }
        return null; // Retorna null si no hay registros siguientes
    }
    
    // Método para obtener el último registro
    public String[] obtenerUltimaPersona() throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido, ci, telefono, correo, fecha_nac, activo FROM personas ORDER BY id_persona DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String id = resultSet.getString("id_persona");
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");
                String ci = resultSet.getString("ci");
                String telefono = resultSet.getString("telefono");
                String correo = resultSet.getString("correo");
                String fechaNac = resultSet.getString("fecha_nac");
                String activo = resultSet.getBoolean("activo") ? "Activo" : "Inactivo";
                return new String[]{id, nombre, apellido, ci, telefono, correo, fechaNac, activo};
            }
        }
        return null; // Retorna null si no hay registros
    }
    
    // Método para eliminar una persona
    public void eliminarPersona(int id) throws SQLException {
        String sql = "DELETE FROM personas WHERE id_persona = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Método para insertar una persona
    public void insertarPersona(String nombre, String apellido, String ci, String telefono, String correo, String fechaNac, boolean activo) throws SQLException {
        String sql = "INSERT INTO personas (nombre, apellido, ci, telefono, correo, fecha_nac, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, ci);
            statement.setString(4, telefono);
            statement.setString(5, correo);
            statement.setString(6, fechaNac);
            statement.setBoolean(7, activo);
            statement.executeUpdate();
        }
    }
    
    // Método para actualizar una persona
    public void actualizarPersona(int id, String nombre, String apellido, String ci, String telefono, String correo, String fechaNac, boolean activo) throws SQLException {
        String sql = "UPDATE personas SET nombre = ?, apellido = ?, ci = ?, telefono = ?, correo = ?, fecha_nac = ?, activo = ? WHERE id_persona = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, ci);
            statement.setString(4, telefono);
            statement.setString(5, correo);
            statement.setString(6, fechaNac);
            statement.setBoolean(7, activo);
            statement.setInt(8, id);
            statement.executeUpdate();
        }
    }
    
    public ResultSet obtenerNombresYApellidos() throws SQLException {
        String sql = "SELECT id_persona, nombre, apellido FROM personas";
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement.executeQuery();
    }
}
