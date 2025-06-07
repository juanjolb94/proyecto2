package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class PrecioDetalleDAO {

    // Método para obtener todos los detalles de una lista de precios
    public List<mPrecioDetalle> obtenerDetallesPorPrecioId(int idPrecioCabecera) throws SQLException {
        List<mPrecioDetalle> detalles = new ArrayList<>();

        // Consulta modificada para incluir el nombre del producto cabecera
        String sql = "SELECT pd.*, p.descripcion AS detalle_descripcion, pc.nombre AS producto_nombre "
                + "FROM precio_detalle pd "
                + "LEFT JOIN productos_detalle p ON pd.codigo_barra = p.cod_barra "
                + "LEFT JOIN productos_cabecera pc ON p.id_producto = pc.id_producto "
                + "WHERE pd.id_precio_cabecera = ? "
                + "ORDER BY pd.codigo_barra";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrecioCabecera);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mPrecioDetalle detalle = new mPrecioDetalle();
                    detalle.setId(rs.getInt("id"));
                    detalle.setIdPrecioCabecera(rs.getInt("id_precio_cabecera"));
                    detalle.setCodigoBarra(rs.getString("codigo_barra"));
                    detalle.setPrecio(rs.getDouble("precio"));
                    detalle.setFechaVigencia(rs.getDate("fecha_vigencia"));
                    detalle.setActivo(rs.getBoolean("activo"));

                    // Combinar nombre del producto cabecera con la descripción del detalle
                    String nombreProducto = rs.getString("producto_nombre");
                    String detalleDescripcion = rs.getString("detalle_descripcion");

                    if (nombreProducto != null && detalleDescripcion != null) {
                        detalle.setNombreProducto(nombreProducto + " - " + detalleDescripcion);
                    } else if (detalleDescripcion != null) {
                        detalle.setNombreProducto(detalleDescripcion);
                    } else if (nombreProducto != null) {
                        detalle.setNombreProducto(nombreProducto);
                    } else {
                        detalle.setNombreProducto("Desconocido");
                    }

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    // Método para obtener un detalle específico
    public mPrecioDetalle obtenerDetallePorId(int id) throws SQLException {
        String sql = "SELECT pd.*, p.descripcion AS detalle_descripcion, pc.nombre AS producto_nombre "
                + "FROM precio_detalle pd "
                + "LEFT JOIN productos_detalle p ON pd.codigo_barra = p.cod_barra "
                + "LEFT JOIN productos_cabecera pc ON p.id_producto = pc.id_producto "
                + "WHERE pd.id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioDetalle detalle = new mPrecioDetalle();
                    detalle.setId(rs.getInt("id"));
                    detalle.setIdPrecioCabecera(rs.getInt("id_precio_cabecera"));
                    detalle.setCodigoBarra(rs.getString("codigo_barra"));
                    detalle.setPrecio(rs.getDouble("precio"));
                    detalle.setFechaVigencia(rs.getDate("fecha_vigencia"));
                    detalle.setActivo(rs.getBoolean("activo"));

                    // Combinar nombre del producto cabecera con la descripción del detalle
                    String nombreProducto = rs.getString("producto_nombre");
                    String detalleDescripcion = rs.getString("detalle_descripcion");

                    if (nombreProducto != null && detalleDescripcion != null) {
                        detalle.setNombreProducto(nombreProducto + " - " + detalleDescripcion);
                    } else if (detalleDescripcion != null) {
                        detalle.setNombreProducto(detalleDescripcion);
                    } else if (nombreProducto != null) {
                        detalle.setNombreProducto(nombreProducto);
                    } else {
                        detalle.setNombreProducto("Desconocido");
                    }

                    return detalle;
                }
            }
        }

        return null; // No encontrado
    }

    // Método para obtener un detalle por código de barras y ID de lista de precios
    public mPrecioDetalle obtenerDetallePorCodigoBarra(int idPrecioCabecera, String codigoBarra) throws SQLException {
        String sql = "SELECT pd.*, p.descripcion AS detalle_descripcion, pc.nombre AS producto_nombre "
                + "FROM precio_detalle pd "
                + "LEFT JOIN productos_detalle p ON pd.codigo_barra = p.cod_barra "
                + "LEFT JOIN productos_cabecera pc ON p.id_producto = pc.id_producto "
                + "WHERE pd.id_precio_cabecera = ? AND pd.codigo_barra = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrecioCabecera);
            stmt.setString(2, codigoBarra);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioDetalle detalle = new mPrecioDetalle();
                    detalle.setId(rs.getInt("id"));
                    detalle.setIdPrecioCabecera(rs.getInt("id_precio_cabecera"));
                    detalle.setCodigoBarra(rs.getString("codigo_barra"));
                    detalle.setPrecio(rs.getDouble("precio"));
                    detalle.setFechaVigencia(rs.getDate("fecha_vigencia"));
                    detalle.setActivo(rs.getBoolean("activo"));

                    // Combinar nombre del producto cabecera con la descripción del detalle
                    String nombreProducto = rs.getString("producto_nombre");
                    String detalleDescripcion = rs.getString("detalle_descripcion");

                    if (nombreProducto != null && detalleDescripcion != null) {
                        detalle.setNombreProducto(nombreProducto + " - " + detalleDescripcion);
                    } else if (detalleDescripcion != null) {
                        detalle.setNombreProducto(detalleDescripcion);
                    } else if (nombreProducto != null) {
                        detalle.setNombreProducto(nombreProducto);
                    } else {
                        detalle.setNombreProducto("Desconocido");
                    }

                    return detalle;
                }
            }
        }

        return null; // No encontrado
    }

    // Método para insertar un nuevo detalle de precio
    public int insertarDetalle(mPrecioDetalle detalle) throws SQLException {
        String sql = "INSERT INTO precio_detalle (id_precio_cabecera, codigo_barra, precio, fecha_vigencia, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, detalle.getIdPrecioCabecera());
            stmt.setString(2, detalle.getCodigoBarra());
            stmt.setDouble(3, detalle.getPrecio());
            stmt.setDate(4, new Date(detalle.getFechaVigencia().getTime()));
            stmt.setBoolean(5, detalle.isActivo());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return -1; // Falló la inserción
    }

    // Método para actualizar un detalle de precio existente
    public boolean actualizarDetalle(mPrecioDetalle detalle) throws SQLException {
        String sql = "UPDATE precio_detalle SET codigo_barra = ?, precio = ?, fecha_vigencia = ?, activo = ? "
                + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, detalle.getCodigoBarra());
            stmt.setDouble(2, detalle.getPrecio());
            stmt.setDate(3, new Date(detalle.getFechaVigencia().getTime()));
            stmt.setBoolean(4, detalle.isActivo());
            stmt.setInt(5, detalle.getId());

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    // Método para eliminar un detalle de precio
    public boolean eliminarDetalle(int id) throws SQLException {
        String sql = "DELETE FROM precio_detalle WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    // Método para desactivar un detalle de precio
    public boolean desactivarDetalle(int id) throws SQLException {
        String sql = "UPDATE precio_detalle SET activo = false WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    // Método para obtener detalles de productos para el selector
    public List<Object[]> obtenerProductosParaSelector() throws SQLException {
        List<Object[]> productos = new ArrayList<>();

        String sql = "SELECT p.cod_barra, p.descripcion AS detalle_descripcion, "
                + "pc.nombre AS producto_nombre, "
                + "c.nombre AS categoria, m.nombre AS marca "
                + "FROM productos_detalle p "
                + "JOIN productos_cabecera pc ON p.id_producto = pc.id_producto "
                + "JOIN categoria_producto c ON pc.id_categoria = c.id_categoria "
                + "JOIN marca_producto m ON pc.id_marca = m.id_marca "
                + "WHERE p.estado = true "
                + "ORDER BY pc.nombre, p.descripcion";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] producto = new Object[5]; // Aumentamos a 5 para incluir el nombre del producto cabecera
                producto[0] = rs.getString("cod_barra");
                producto[1] = rs.getString("detalle_descripcion");
                producto[2] = rs.getString("producto_nombre"); // Nuevo campo: nombre del producto cabecera
                producto[3] = rs.getString("categoria");
                producto[4] = rs.getString("marca");

                productos.add(producto);
            }
        }

        return productos;
    }

    // Método para verificar si un producto ya existe en una lista de precios
    public boolean existeProductoEnPrecio(int idPrecioCabecera, String codigoBarra) throws SQLException {
        String sql = "SELECT COUNT(*) FROM precio_detalle "
                + "WHERE id_precio_cabecera = ? AND codigo_barra = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrecioCabecera);
            stmt.setString(2, codigoBarra);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }
}
