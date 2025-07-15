package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vista.vProveedores;

public class ProveedoresDAO {

    private Connection conexion;

    public ProveedoresDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Método para buscar un proveedor por ID
    public Object[] buscarProveedorPorId(int id) throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p WHERE p.id_proveedor = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_proveedor"),
                        rs.getString("razon_social"),
                        rs.getString("ruc"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("email"),
                        rs.getBoolean("estado") ? "1" : "0",
                        rs.getInt("id_persona")
                    };
                }
            }
        }
        return null;
    }

    // Método para obtener el primer proveedor
    public Object[] obtenerPrimerProveedor() throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p ORDER BY p.id_proveedor ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("id_proveedor"),
                    rs.getString("razon_social"),
                    rs.getString("ruc"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("email"),
                    rs.getBoolean("estado") ? "1" : "0",
                    rs.getInt("id_persona")
                };
            }
        }
        return null;
    }

    // Método para obtener el proveedor anterior
    public Object[] obtenerAnteriorProveedor(int idActual) throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p WHERE p.id_proveedor < ? "
                + "ORDER BY p.id_proveedor DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_proveedor"),
                        rs.getString("razon_social"),
                        rs.getString("ruc"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("email"),
                        rs.getBoolean("estado") ? "1" : "0",
                        rs.getInt("id_persona")
                    };
                }
            }
        }
        return null;
    }

    // Método para obtener el siguiente proveedor
    public Object[] obtenerSiguienteProveedor(int idActual) throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p WHERE p.id_proveedor > ? "
                + "ORDER BY p.id_proveedor ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_proveedor"),
                        rs.getString("razon_social"),
                        rs.getString("ruc"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("email"),
                        rs.getBoolean("estado") ? "1" : "0",
                        rs.getInt("id_persona")
                    };
                }
            }
        }
        return null;
    }

    // Método para obtener el último proveedor
    public Object[] obtenerUltimoProveedor() throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p ORDER BY p.id_proveedor DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getInt("id_proveedor"),
                    rs.getString("razon_social"),
                    rs.getString("ruc"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("email"),
                    rs.getBoolean("estado") ? "1" : "0",
                    rs.getInt("id_persona")
                };
            }
        }
        return null;
    }

    // Método para listar todos los proveedores
    public List<Object[]> listarProveedores() throws SQLException {
        List<Object[]> proveedores = new ArrayList<>();
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p ORDER BY p.id_proveedor ASC";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(new Object[]{
                    rs.getInt("id_proveedor"),
                    rs.getString("razon_social"),
                    rs.getString("ruc"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("email"),
                    rs.getBoolean("estado") ? "1" : "0",
                    rs.getInt("id_persona")
                });
            }
        }
        return proveedores;
    }

    // Método para insertar un nuevo proveedor
    public boolean insertarProveedor(String razonSocial, String ruc, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) throws SQLException {
        String sql = "INSERT INTO proveedores (razon_social, ruc, telefono, direccion, email, estado, id_persona) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, razonSocial);
            ps.setString(2, ruc);
            ps.setString(3, telefono);
            ps.setString(4, direccion);
            ps.setString(5, email);
            ps.setBoolean(6, estado);
            ps.setInt(7, idPersona);

            return ps.executeUpdate() > 0;
        }
    }

    // Método para actualizar un proveedor existente
    public boolean actualizarProveedor(int id, String razonSocial, String ruc, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) throws SQLException {
        String sql = "UPDATE proveedores SET razon_social = ?, ruc = ?, telefono = ?, "
                + "direccion = ?, email = ?, estado = ?, id_persona = ? "
                + "WHERE id_proveedor = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, razonSocial);
            ps.setString(2, ruc);
            ps.setString(3, telefono);
            ps.setString(4, direccion);
            ps.setString(5, email);
            ps.setBoolean(6, estado);
            ps.setInt(7, idPersona);
            ps.setInt(8, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Método para verificar si un proveedor tiene compras asociadas
    public boolean tieneComprasAsociadas(int idProveedor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM compras_cabecera WHERE id_proveedor = ? AND estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para obtener el conteo de compras de un proveedor
    public int contarComprasProveedor(int idProveedor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM compras_cabecera WHERE id_proveedor = ? AND estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Método para obtener información detallada de dependencias
    public String obtenerDetalleDependencias(int idProveedor) throws SQLException {
        StringBuilder detalle = new StringBuilder();

        // Verificar compras activas
        String sqlCompras = "SELECT COUNT(*) as total, MIN(fecha_compra) as primera, MAX(fecha_compra) as ultima "
                + "FROM compras_cabecera WHERE id_proveedor = ? AND estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sqlCompras)) {
            ps.setInt(1, idProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    if (total > 0) {
                        detalle.append("Compras activas: ").append(total);
                        detalle.append(" (desde ").append(rs.getDate("primera"));
                        detalle.append(" hasta ").append(rs.getDate("ultima")).append(")");
                    }
                }
            }
        }

        return detalle.toString();
    }

    // Método para eliminar un proveedor
    public boolean eliminarProveedor(int id) throws SQLException {
        // Validar si tiene compras asociadas
        if (tieneComprasAsociadas(id)) {
            int cantidadCompras = contarComprasProveedor(id);
            String detalle = obtenerDetalleDependencias(id);

            throw new SQLException("No se puede eliminar el proveedor: tiene " + cantidadCompras + " compra(s) registrada(s). "
                    + detalle + ". Considere desactivar el proveedor en lugar de eliminarlo.");
        }

        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Método para verificar si un proveedor ya existe por RUC
    public boolean existeProveedorPorRuc(String ruc) throws SQLException {
        String sql = "SELECT COUNT(*) FROM proveedores WHERE ruc = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, ruc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para verificar si una persona ya está asociada a un proveedor
    public boolean personaEsProveedor(int idPersona) throws SQLException {
        String sql = "SELECT COUNT(*) FROM proveedores WHERE id_persona = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para buscar un proveedor por id_persona
    public Object[] buscarProveedorPorIdPersona(int idPersona) throws SQLException {
        String sql = "SELECT p.id_proveedor, p.razon_social, p.ruc, p.telefono, p.direccion, "
                + "p.email, p.estado, p.id_persona "
                + "FROM proveedores p WHERE p.id_persona = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_proveedor"),
                        rs.getString("razon_social"),
                        rs.getString("ruc"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("email"),
                        rs.getBoolean("estado") ? "1" : "0",
                        rs.getInt("id_persona")
                    };
                }
            }
        }
        return null;
    }
}
