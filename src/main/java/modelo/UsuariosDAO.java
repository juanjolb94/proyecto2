package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    // Método para verificar si un usuario tiene ventas asociadas
    public boolean tieneVentasAsociadas(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE id_usuario = ? AND anulado = false";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para verificar si un usuario tiene movimientos de caja asociados
    public boolean tieneMovimientosCaja(int usuarioId) throws SQLException {
        // Verificar en gastos por usuario (nombre)
        String sqlUsuario = "SELECT NombreUsuario FROM usuarios WHERE UsuarioID = ?";
        String nombreUsuario = null;

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlUsuario)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombreUsuario = rs.getString("NombreUsuario");
                }
            }
        }

        if (nombreUsuario == null) {
            return false;
        }

        // Verificar gastos
        String sqlGastos = "SELECT COUNT(*) FROM gastos WHERE usuario = ? AND anulado = false";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlGastos)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        // Verificar ingresos de caja
        String sqlIngresos = "SELECT COUNT(*) FROM ingresos_caja WHERE usuario = ? AND anulado = false";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlIngresos)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        // Verificar apertura/cierre de cajas
        String sqlCajas = "SELECT COUNT(*) FROM cajas WHERE usuario_apertura = ? OR usuario_cierre = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlCajas)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // Método para verificar si un usuario tiene reimpresiones asociadas
    public boolean tieneReimpresiones(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reimpresiones WHERE usuario_id = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para verificar si un usuario tiene historial de movimientos
    public boolean tieneHistorialMovimientos(int usuarioId) throws SQLException {
        // Verificar compras_historico
        String sqlCompras = "SELECT COUNT(*) FROM compras_historico WHERE usuario_id = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlCompras)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        // Verificar ventas_historico (si existe la tabla)
        try {
            String sqlVentas = "SELECT COUNT(*) FROM ventas_historico WHERE usuario_id = ?";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlVentas)) {

                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // La tabla ventas_historico puede no existir, ignorar
            System.out.println("Tabla ventas_historico no encontrada: " + e.getMessage());
        }

        return false;
    }

    // Método para obtener resumen completo de dependencias del usuario
    public String obtenerResumenDependenciasUsuario(int usuarioId) throws SQLException {
        StringBuilder resumen = new StringBuilder();

        // Contar ventas
        String sqlVentas = "SELECT COUNT(*) FROM ventas WHERE id_usuario = ? AND anulado = false";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlVentas)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ventas = rs.getInt(1);
                    if (ventas > 0) {
                        resumen.append("Ventas: ").append(ventas).append(" | ");
                    }
                }
            }
        }

        // Obtener nombre de usuario para verificar movimientos de caja
        String nombreUsuario = null;
        String sqlUsuario = "SELECT NombreUsuario FROM usuarios WHERE UsuarioID = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlUsuario)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombreUsuario = rs.getString("NombreUsuario");
                }
            }
        }

        if (nombreUsuario != null) {
            // Contar gastos
            String sqlGastos = "SELECT COUNT(*) FROM gastos WHERE usuario = ? AND anulado = false";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlGastos)) {

                ps.setString(1, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int gastos = rs.getInt(1);
                        if (gastos > 0) {
                            resumen.append("Gastos: ").append(gastos).append(" | ");
                        }
                    }
                }
            }

            // Contar ingresos
            String sqlIngresos = "SELECT COUNT(*) FROM ingresos_caja WHERE usuario = ? AND anulado = false";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlIngresos)) {

                ps.setString(1, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int ingresos = rs.getInt(1);
                        if (ingresos > 0) {
                            resumen.append("Ingresos: ").append(ingresos).append(" | ");
                        }
                    }
                }
            }

            // Contar cajas
            String sqlCajas = "SELECT COUNT(*) FROM cajas WHERE usuario_apertura = ? OR usuario_cierre = ?";
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlCajas)) {

                ps.setString(1, nombreUsuario);
                ps.setString(2, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int cajas = rs.getInt(1);
                        if (cajas > 0) {
                            resumen.append("Cajas: ").append(cajas).append(" | ");
                        }
                    }
                }
            }
        }

        // Contar reimpresiones
        String sqlReimpresiones = "SELECT COUNT(*) FROM reimpresiones WHERE usuario_id = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sqlReimpresiones)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int reimpresiones = rs.getInt(1);
                    if (reimpresiones > 0) {
                        resumen.append("Reimpresiones: ").append(reimpresiones);
                    }
                }
            }
        }

        return resumen.toString();
    }

    // Método para verificar si es un usuario crítico (administrador principal)
    public boolean esUsuarioAdministrador(int usuarioId) throws SQLException {
        String sql = "SELECT u.RolID, r.nombre FROM usuarios u "
                + "INNER JOIN roles r ON u.RolID = r.id_rol "
                + "WHERE u.UsuarioID = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombreRol = rs.getString("nombre");
                    return "Administrador".equalsIgnoreCase(nombreRol);
                }
            }
        }
        return false;
    }

    // Método para eliminar un usuario
    public boolean eliminar(int id) throws SQLException {
        // Validar si es un usuario administrador (advertencia especial)
        if (esUsuarioAdministrador(id)) {
            throw new SQLException("ADVERTENCIA: Está intentando eliminar un usuario Administrador. "
                    + "Esto puede afectar la administración del sistema. "
                    + "Considere desactivar el usuario en lugar de eliminarlo.");
        }

        // Verificar si tiene ventas asociadas
        if (tieneVentasAsociadas(id)) {
            String resumen = obtenerResumenDependenciasUsuario(id);
            throw new SQLException("No se puede eliminar el usuario: tiene operaciones registradas. "
                    + resumen + " Considere desactivar el usuario en lugar de eliminarlo.");
        }

        // Verificar si tiene movimientos de caja
        if (tieneMovimientosCaja(id)) {
            String resumen = obtenerResumenDependenciasUsuario(id);
            throw new SQLException("No se puede eliminar el usuario: tiene movimientos de caja registrados. "
                    + resumen + " Considere desactivar el usuario en lugar de eliminarlo.");
        }

        // Verificar si tiene reimpresiones
        if (tieneReimpresiones(id)) {
            String resumen = obtenerResumenDependenciasUsuario(id);
            throw new SQLException("No se puede eliminar el usuario: tiene reimpresiones autorizadas. "
                    + resumen + " Considere desactivar el usuario en lugar de eliminarlo.");
        }

        // Verificar historial de movimientos
        if (tieneHistorialMovimientos(id)) {
            throw new SQLException("No se puede eliminar el usuario: tiene historial de movimientos registrados. "
                    + "Eliminar este usuario afectaría la trazabilidad del sistema. "
                    + "Considere desactivar el usuario en lugar de eliminarlo.");
        }

        String sql = "DELETE FROM usuarios WHERE UsuarioID = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
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

    // Método para listar todos los usuarios (para combobox)
    public List<Object[]> listarUsuarios() throws SQLException {
        List<Object[]> usuarios = new ArrayList<>();
        String sql = "SELECT u.UsuarioID, u.NombreUsuario, u.PersonaID, u.RolID, u.Activo, "
                + "CONCAT(p.nombre, ' ', p.apellido) as persona_completa "
                + "FROM usuarios u "
                + "LEFT JOIN personas p ON u.PersonaID = p.id_persona "
                + "WHERE u.Activo = 1 " // Solo usuarios activos
                + "ORDER BY u.NombreUsuario";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Object[]{
                    rs.getInt("UsuarioID"),
                    rs.getString("NombreUsuario"),
                    rs.getInt("PersonaID"),
                    rs.getInt("RolID"),
                    rs.getBoolean("Activo"),
                    rs.getString("persona_completa")
                });
            }
        }
        return usuarios;
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
