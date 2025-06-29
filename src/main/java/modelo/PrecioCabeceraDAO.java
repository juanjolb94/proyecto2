package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class PrecioCabeceraDAO {

    // Método para obtener todas las listas de precios
    public List<mPrecioCabecera> obtenerTodosPrecios() throws SQLException {
        List<mPrecioCabecera> precios = new ArrayList<>();
        String sql = "SELECT * FROM precio_cabecera ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                mPrecioCabecera precio = new mPrecioCabecera();
                precio.setId(rs.getInt("id"));
                precio.setNombre(rs.getString("nombre"));
                precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                precio.setMoneda(rs.getString("moneda"));
                precio.setActivo(rs.getBoolean("activo"));
                precio.setObservaciones(rs.getString("observaciones"));
                precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                precios.add(precio);
            }
        }

        return precios;
    }

    // Método para obtener listas de precios activas
    public List<mPrecioCabecera> obtenerPreciosActivos() throws SQLException {
        List<mPrecioCabecera> precios = new ArrayList<>();
        String sql = "SELECT * FROM precio_cabecera WHERE activo = true ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                mPrecioCabecera precio = new mPrecioCabecera();
                precio.setId(rs.getInt("id"));
                precio.setNombre(rs.getString("nombre"));
                precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                precio.setMoneda(rs.getString("moneda"));
                precio.setActivo(rs.getBoolean("activo"));
                precio.setObservaciones(rs.getString("observaciones"));
                precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                precios.add(precio);
            }
        }

        return precios;
    }

    // Método para obtener una lista de precios por ID
    public mPrecioCabecera obtenerPrecioPorId(int id) throws SQLException {
        String sql = "SELECT * FROM precio_cabecera WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioCabecera precio = new mPrecioCabecera();
                    precio.setId(rs.getInt("id"));
                    precio.setNombre(rs.getString("nombre"));
                    precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                    precio.setMoneda(rs.getString("moneda"));
                    precio.setActivo(rs.getBoolean("activo"));
                    precio.setObservaciones(rs.getString("observaciones"));
                    precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                    return precio;
                }
            }
        }

        return null; // No encontrado
    }

    // Método para insertar una nueva lista de precios
    public int insertarPrecio(mPrecioCabecera precio) throws SQLException {
        String sql = "INSERT INTO precio_cabecera (nombre, fecha_creacion, moneda, activo, observaciones, es_defecto_venta) "
                + "VALUES (?, ?, ?, ?, ?, ?)"; // MODIFICADO

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, precio.getNombre());
            stmt.setDate(2, new Date(precio.getFechaCreacion().getTime()));
            stmt.setString(3, precio.getMoneda());
            stmt.setBoolean(4, precio.isActivo());
            stmt.setString(5, precio.getObservaciones());
            stmt.setBoolean(6, precio.isEsDefectoVenta()); // AGREGADO

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

    // Método para actualizar una lista de precios existente
    public boolean actualizarPrecio(mPrecioCabecera precio) throws SQLException {
        String sql = "UPDATE precio_cabecera SET nombre = ?, moneda = ?, activo = ?, observaciones = ?, es_defecto_venta = ? "
                + "WHERE id = ?"; // MODIFICADO

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, precio.getNombre());
            stmt.setString(2, precio.getMoneda());
            stmt.setBoolean(3, precio.isActivo());
            stmt.setString(4, precio.getObservaciones());
            stmt.setBoolean(5, precio.isEsDefectoVenta()); // AGREGADO
            stmt.setInt(6, precio.getId()); // MOVIDO A POSICIÓN 6

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    // Método para eliminar una lista de precios (desactivarla)
    public boolean eliminarPrecio(int id) throws SQLException {
        String sql = "UPDATE precio_cabecera SET activo = false WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        }
    }

    // Métodos de navegación para la interfaz
    public mPrecioCabecera obtenerPrimerPrecio() throws SQLException {
        String sql = "SELECT * FROM precio_cabecera ORDER BY id ASC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                mPrecioCabecera precio = new mPrecioCabecera();
                precio.setId(rs.getInt("id"));
                precio.setNombre(rs.getString("nombre"));
                precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                precio.setMoneda(rs.getString("moneda"));
                precio.setActivo(rs.getBoolean("activo"));
                precio.setObservaciones(rs.getString("observaciones"));
                precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                return precio;
            }
        }

        return null; // No hay registros
    }

    public mPrecioCabecera obtenerSiguientePrecio(int idActual) throws SQLException {
        String sql = "SELECT * FROM precio_cabecera WHERE id > ? ORDER BY id ASC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActual);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioCabecera precio = new mPrecioCabecera();
                    precio.setId(rs.getInt("id"));
                    precio.setNombre(rs.getString("nombre"));
                    precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                    precio.setMoneda(rs.getString("moneda"));
                    precio.setActivo(rs.getBoolean("activo"));
                    precio.setObservaciones(rs.getString("observaciones"));
                    precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                    return precio;
                }
            }
        }

        return null; // No hay siguiente registro
    }

    public mPrecioCabecera obtenerAnteriorPrecio(int idActual) throws SQLException {
        String sql = "SELECT * FROM precio_cabecera WHERE id < ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActual);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioCabecera precio = new mPrecioCabecera();
                    precio.setId(rs.getInt("id"));
                    precio.setNombre(rs.getString("nombre"));
                    precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                    precio.setMoneda(rs.getString("moneda"));
                    precio.setActivo(rs.getBoolean("activo"));
                    precio.setObservaciones(rs.getString("observaciones"));
                    precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                    return precio;
                }
            }
        }

        return null; // No hay registro anterior
    }

    public mPrecioCabecera obtenerUltimoPrecio() throws SQLException {
        String sql = "SELECT * FROM precio_cabecera ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                mPrecioCabecera precio = new mPrecioCabecera();
                precio.setId(rs.getInt("id"));
                precio.setNombre(rs.getString("nombre"));
                precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                precio.setMoneda(rs.getString("moneda"));
                precio.setActivo(rs.getBoolean("activo"));
                precio.setObservaciones(rs.getString("observaciones"));
                precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta")); // AGREGADO

                return precio;
            }
        }

        return null; // No hay registros
    }

    // Limpiar todas las listas como defecto
    public boolean limpiarDefectoVenta() throws SQLException {
        String sql = "UPDATE precio_cabecera SET es_defecto_venta = false";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int affectedRows = stmt.executeUpdate();
            return affectedRows >= 0;
        }
    }

    // Establecer una lista como defecto
    public boolean establecerDefectoVenta(int idPrecio) throws SQLException {
        String sql = "UPDATE precio_cabecera SET es_defecto_venta = true WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrecio);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Obtener la lista de precios por defecto para ventas
    public mPrecioCabecera obtenerPrecioDefectoVenta() throws SQLException {
        String sql = "SELECT * FROM precio_cabecera WHERE es_defecto_venta = true AND activo = true LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mPrecioCabecera precio = new mPrecioCabecera();
                    precio.setId(rs.getInt("id"));
                    precio.setNombre(rs.getString("nombre"));
                    precio.setFechaCreacion(rs.getDate("fecha_creacion"));
                    precio.setMoneda(rs.getString("moneda"));
                    precio.setActivo(rs.getBoolean("activo"));
                    precio.setObservaciones(rs.getString("observaciones"));
                    precio.setEsDefectoVenta(rs.getBoolean("es_defecto_venta"));

                    return precio;
                }
            }
        }

        return null; // No hay lista por defecto
    }
}