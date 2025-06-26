package modelo;

import java.sql.*;

public class UsuariosDAO {

    // Método para insertar un nuevo usuario
    public boolean insertar(String nombreUsuario, String contraseña, int personaId, int rolId, boolean activo) {
        String sql = "INSERT INTO usuarios (NombreUsuario, Contraseña, PersonaID, RolID, Activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            System.out.println("=== UsuariosDAO.insertar() ===");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros:");
            System.out.println("  1. NombreUsuario: " + nombreUsuario);
            System.out.println("  2. Contraseña: " + contraseña);
            System.out.println("  3. PersonaID: " + personaId);
            System.out.println("  4. RolID: " + rolId);
            System.out.println("  5. Activo: " + activo);

            ps.setString(1, nombreUsuario);
            ps.setString(2, contraseña);
            ps.setInt(3, personaId);
            ps.setInt(4, rolId);
            ps.setBoolean(5, activo);

            int resultado = ps.executeUpdate();
            System.out.println("Filas afectadas: " + resultado);
            System.out.println("=============================");

            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar un usuario
    public boolean actualizar(int id, String nombreUsuario, String contraseña, int personaId, int rolId, boolean activo) {
        String sql = "UPDATE usuarios SET NombreUsuario = ?, Contraseña = ?, PersonaID = ?, RolID = ?, Activo = ? WHERE UsuarioID = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            System.out.println("=== UsuariosDAO.actualizar() ===");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros:");
            System.out.println("  1. NombreUsuario: " + nombreUsuario);
            System.out.println("  2. Contraseña: " + contraseña);
            System.out.println("  3. PersonaID: " + personaId);
            System.out.println("  4. RolID: " + rolId);
            System.out.println("  5. Activo: " + activo);
            System.out.println("  6. UsuarioID: " + id);

            ps.setString(1, nombreUsuario);
            ps.setString(2, contraseña);
            ps.setInt(3, personaId);
            ps.setInt(4, rolId);
            ps.setBoolean(5, activo);
            ps.setInt(6, id);

            int resultado = ps.executeUpdate();
            System.out.println("Filas afectadas: " + resultado);
            System.out.println("==============================");

            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar un usuario
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE UsuarioID = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // Método para buscar usuario por ID
    public String[] buscarPorId(int id) {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo, "
                + "p.nombre, p.apellido, r.nombre "
                + // CAMBIO: r.nombre en lugar de r.nombre_rol
                "FROM usuarios u "
                + "LEFT JOIN personas p ON u.PersonaID = p.id_persona "
                + "LEFT JOIN roles r ON u.RolID = r.id_rol "
                + "WHERE u.UsuarioID = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("UsuarioID")),
                        rs.getString("NombreUsuario"),
                        rs.getString("Contraseña"),
                        String.valueOf(rs.getInt("PersonaID")),
                        String.valueOf(rs.getInt("RolID")),
                        String.valueOf(rs.getBoolean("Activo"))
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    // Método para buscar usuario por nombre de usuario
    public String[] buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo "
                + "FROM usuarios u WHERE u.NombreUsuario = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("UsuarioID")),
                        rs.getString("NombreUsuario"),
                        rs.getString("Contraseña"),
                        String.valueOf(rs.getInt("PersonaID")),
                        String.valueOf(rs.getInt("RolID")),
                        String.valueOf(rs.getBoolean("Activo"))
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por nombre: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener todos los usuarios
    public ResultSet obtenerTodos() throws SQLException {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.PersonaID, u.RolID, u.Activo, "
                + "CONCAT(p.nombre, ' ', p.apellido) as persona_completa, r.nombre as nombre_rol "
                + "FROM usuarios u "
                + "LEFT JOIN personas p ON u.PersonaID = p.id_persona "
                + "LEFT JOIN roles r ON u.RolID = r.id_rol "
                + "ORDER BY u.UsuarioID";

        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeQuery();
    }

    // Método para verificar si existe un nombre de usuario
    public boolean existeNombreUsuario(String nombreUsuario, int excludeId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE NombreUsuario = ? AND UsuarioID != ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de usuario: " + e.getMessage());
        }
        return false;
    }

    // Método para verificar si una persona ya tiene un usuario
    public boolean personaTieneUsuario(int idPersona, int excludeUsuarioId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE PersonaID = ? AND UsuarioID != ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPersona);
            ps.setInt(2, excludeUsuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar persona: " + e.getMessage());
        }
        return false;
    }

    // Método para obtener el primer usuario
    public String[] obtenerPrimerUsuario() {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo "
                + "FROM usuarios u ORDER BY u.UsuarioID ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new String[]{
                    String.valueOf(rs.getInt("UsuarioID")),
                    rs.getString("NombreUsuario"),
                    rs.getString("Contraseña"),
                    String.valueOf(rs.getInt("PersonaID")),
                    String.valueOf(rs.getInt("RolID")),
                    String.valueOf(rs.getBoolean("Activo"))
                };
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener primer usuario: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener el usuario anterior
    public String[] obtenerAnteriorUsuario(int idActual) {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo "
                + "FROM usuarios u WHERE u.UsuarioID < ? ORDER BY u.UsuarioID DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("UsuarioID")),
                        rs.getString("NombreUsuario"),
                        rs.getString("Contraseña"),
                        String.valueOf(rs.getInt("PersonaID")),
                        String.valueOf(rs.getInt("RolID")),
                        String.valueOf(rs.getBoolean("Activo"))
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario anterior: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener el siguiente usuario
    public String[] obtenerSiguienteUsuario(int idActual) {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo "
                + "FROM usuarios u WHERE u.UsuarioID > ? ORDER BY u.UsuarioID ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("UsuarioID")),
                        rs.getString("NombreUsuario"),
                        rs.getString("Contraseña"),
                        String.valueOf(rs.getInt("PersonaID")),
                        String.valueOf(rs.getInt("RolID")),
                        String.valueOf(rs.getBoolean("Activo"))
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener siguiente usuario: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener el último usuario
    public String[] obtenerUltimoUsuario() {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.Contraseña, u.PersonaID, u.RolID, u.Activo "
                + "FROM usuarios u ORDER BY u.UsuarioID DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new String[]{
                    String.valueOf(rs.getInt("UsuarioID")),
                    rs.getString("NombreUsuario"),
                    rs.getString("Contraseña"),
                    String.valueOf(rs.getInt("PersonaID")),
                    String.valueOf(rs.getInt("RolID")),
                    String.valueOf(rs.getBoolean("Activo"))
                };
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último usuario: " + e.getMessage());
        }
        return null;
    }

    // Método para contar total de usuarios
    public int contarUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar usuarios: " + e.getMessage());
        }
        return 0;
    }

    public Object[] obtenerDatosCompletos(int usuarioId) {
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.PersonaID, u.RolID, u.Activo, "
                + "CONCAT(p.nombre, ' ', p.apellido) as persona_nombre, "
                + "r.nombre as rol_nombre "
                + "FROM usuarios u "
                + "LEFT JOIN personas p ON u.PersonaID = p.id_persona "
                + "LEFT JOIN roles r ON u.RolID = r.id_rol "
                + "WHERE u.UsuarioID = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("UsuarioID"),
                        rs.getString("NombreUsuario"),
                        rs.getInt("PersonaID"),
                        rs.getInt("RolID"),
                        rs.getBoolean("Activo"),
                        rs.getString("persona_nombre"),
                        rs.getString("rol_nombre")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos completos: " + e.getMessage());
        }
        return null;
    }
}
