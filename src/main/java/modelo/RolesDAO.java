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

    // Método para verificar si un rol tiene usuarios asignados
    public boolean tieneUsuariosAsignados(int idRol) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE RolID = ? AND Activo = true";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para contar usuarios asignados a un rol
    public int contarUsuariosAsignados(int idRol) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE RolID = ? AND Activo = true";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    // Método para obtener información detallada de usuarios asignados
    public String obtenerDetalleUsuariosAsignados(int idRol) throws SQLException {
        StringBuilder detalle = new StringBuilder();

        String sql = "SELECT u.NombreUsuario, p.nombre, p.apellido "
                + "FROM usuarios u "
                + "LEFT JOIN personas p ON u.PersonaID = p.id_persona "
                + "WHERE u.RolID = ? AND u.Activo = true "
                + "ORDER BY u.NombreUsuario";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {
                int contador = 0;
                while (resultSet.next() && contador < 5) { // Mostrar máximo 5 usuarios
                    if (contador > 0) {
                        detalle.append(", ");
                    }

                    String nombreUsuario = resultSet.getString("NombreUsuario");
                    String nombre = resultSet.getString("nombre");
                    String apellido = resultSet.getString("apellido");

                    detalle.append(nombreUsuario);
                    if (nombre != null && apellido != null) {
                        detalle.append(" (").append(nombre).append(" ").append(apellido).append(")");
                    }

                    contador++;
                }

                // Si hay más usuarios, indicarlo
                if (resultSet.next()) {
                    detalle.append("...");
                }
            }
        }

        return detalle.toString();
    }

    // Método para verificar si es un rol crítico del sistema
    public boolean esRolCritico(int idRol) throws SQLException {
        String sql = "SELECT nombre FROM roles WHERE id_rol = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String nombreRol = resultSet.getString("nombre");
                    return "Administrador".equalsIgnoreCase(nombreRol)
                            || "Cajero".equalsIgnoreCase(nombreRol);
                }
            }
        }
        return false;
    }

    // Método para verificar permisos asociados al rol
    public boolean tienePermisosAsignados(int idRol) throws SQLException {
        String sql = "SELECT COUNT(*) FROM permisos WHERE id_rol = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void eliminarRol(int id) throws SQLException {
        // Validar si es un rol crítico del sistema
        if (esRolCritico(id)) {
            throw new SQLException("No se puede eliminar un rol crítico del sistema (Administrador/Cajero). "
                    + "Estos roles son necesarios para el funcionamiento del sistema.");
        }

        // Validar si tiene usuarios asignados
        if (tieneUsuariosAsignados(id)) {
            int cantidadUsuarios = contarUsuariosAsignados(id);
            String detalleUsuarios = obtenerDetalleUsuariosAsignados(id);

            throw new SQLException("No se puede eliminar el rol: tiene " + cantidadUsuarios + " usuario(s) asignado(s). "
                    + "Usuarios: " + detalleUsuarios + ". "
                    + "Reasigne los usuarios a otro rol antes de eliminar este rol.");
        }

        // Validar si tiene permisos asignados
        if (tienePermisosAsignados(id)) {
            throw new SQLException("No se puede eliminar el rol: tiene permisos configurados. "
                    + "Los permisos se eliminarán automáticamente al eliminar el rol.");
        }

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
