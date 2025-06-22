package modelo;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AjusteStockDAO {

    private Connection conexion;

    public AjusteStockDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Buscar producto por código de barras con stock actual
    public Object[] buscarProductoConStock(String codBarra) throws SQLException {
        String sql = "SELECT pc.id_producto, pc.nombre, pd.cod_barra, pd.descripcion, "
                + "COALESCE(s.cantidad_disponible, 0) as stock "
                + "FROM productos_cabecera pc "
                + "INNER JOIN productos_detalle pd ON pc.id_producto = pd.id_producto "
                + "LEFT JOIN stock s ON pd.id_producto = s.id_producto AND pd.cod_barra = s.cod_barra "
                + "WHERE pd.cod_barra = ? AND pc.estado = 1 AND pd.estado = 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codBarra);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_producto"), // 0
                        rs.getString("nombre"), // 1
                        rs.getString("cod_barra"), // 2
                        rs.getString("descripcion"), // 3
                        rs.getInt("stock")
                    };
                }
            }
        }
        return null;
    }

    // Insertar cabecera de ajuste
    public int insertarAjusteCabecera(mAjusteStock ajuste) throws SQLException {
        String sql = "INSERT INTO ajustes_stock_cabecera (fecha, observaciones, usuario_id, aprobado, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(ajuste.getFecha().getTime()));
            ps.setString(2, ajuste.getObservaciones());
            ps.setInt(3, ajuste.getUsuarioId());
            ps.setBoolean(4, ajuste.isAprobado());
            ps.setBoolean(5, ajuste.isEstado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Insertar detalle de ajuste
    public void insertarAjusteDetalle(int idAjuste, mAjusteStock.DetalleAjuste detalle) throws SQLException {
        String sql = "INSERT INTO ajustes_stock_detalle (id_ajuste, id_producto, cod_barra, "
                + "cantidad_sistema, cantidad_ajuste, observaciones) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            ps.setInt(2, detalle.getIdProducto());
            ps.setString(3, detalle.getCodBarra());
            ps.setInt(4, detalle.getCantidadSistema());
            ps.setInt(5, detalle.getCantidadAjuste());
            ps.setString(6, detalle.getObservaciones());

            ps.executeUpdate();
        }
    }

    // Buscar ajuste por ID
    public mAjusteStock buscarAjustePorId(int idAjuste) throws SQLException {
        String sqlCabecera = "SELECT id_ajuste, fecha, observaciones, usuario_id, aprobado, estado "
                + "FROM ajustes_stock_cabecera WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sqlCabecera)) {
            ps.setInt(1, idAjuste);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mAjusteStock ajuste = new mAjusteStock();
                    ajuste.setIdAjuste(rs.getInt("id_ajuste"));
                    ajuste.setFecha(rs.getTimestamp("fecha"));
                    ajuste.setObservaciones(rs.getString("observaciones"));
                    ajuste.setUsuarioId(rs.getInt("usuario_id"));
                    ajuste.setAprobado(rs.getBoolean("aprobado"));
                    ajuste.setEstado(rs.getBoolean("estado"));

                    // Cargar detalles
                    ajuste.setDetalles(cargarDetallesAjuste(idAjuste));
                    return ajuste;
                }
            }
        }
        return null;
    }

    // Cargar detalles de un ajuste
    private List<mAjusteStock.DetalleAjuste> cargarDetallesAjuste(int idAjuste) throws SQLException {
        List<mAjusteStock.DetalleAjuste> detalles = new ArrayList<>();

        String sql = "SELECT asd.id_detalle, asd.id_producto, asd.cod_barra, asd.cantidad_sistema, "
                + "asd.cantidad_ajuste, asd.observaciones, pc.nombre, pd.descripcion "
                + "FROM ajustes_stock_detalle asd "
                + "INNER JOIN productos_cabecera pc ON asd.id_producto = pc.id_producto "
                + "INNER JOIN productos_detalle pd ON asd.id_producto = pd.id_producto AND asd.cod_barra = pd.cod_barra "
                + "WHERE asd.id_ajuste = ? ORDER BY asd.id_detalle";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mAjusteStock.DetalleAjuste detalle = new mAjusteStock.DetalleAjuste();
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    detalle.setIdAjuste(idAjuste);
                    detalle.setIdProducto(rs.getInt("id_producto"));
                    detalle.setCodBarra(rs.getString("cod_barra"));
                    detalle.setCantidadSistema(rs.getInt("cantidad_sistema"));
                    detalle.setCantidadAjuste(rs.getInt("cantidad_ajuste"));
                    detalle.setObservaciones(rs.getString("observaciones"));
                    detalle.setNombreProducto(rs.getString("nombre"));
                    detalle.setDescripcionProducto(rs.getString("descripcion"));

                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    // Obtener primer ajuste
    public mAjusteStock obtenerPrimerAjuste() throws SQLException {
        String sql = "SELECT id_ajuste FROM ajustes_stock_cabecera ORDER BY id_ajuste ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return buscarAjustePorId(rs.getInt("id_ajuste"));
            }
        }
        return null;
    }

    // Obtener último ajuste
    public mAjusteStock obtenerUltimoAjuste() throws SQLException {
        String sql = "SELECT id_ajuste FROM ajustes_stock_cabecera ORDER BY id_ajuste DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return buscarAjustePorId(rs.getInt("id_ajuste"));
            }
        }
        return null;
    }

    // Obtener siguiente ajuste
    public mAjusteStock obtenerSiguienteAjuste(int idActual) throws SQLException {
        String sql = "SELECT id_ajuste FROM ajustes_stock_cabecera WHERE id_ajuste > ? ORDER BY id_ajuste ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buscarAjustePorId(rs.getInt("id_ajuste"));
                }
            }
        }
        return null;
    }

    // Obtener anterior ajuste
    public mAjusteStock obtenerAnteriorAjuste(int idActual) throws SQLException {
        String sql = "SELECT id_ajuste FROM ajustes_stock_cabecera WHERE id_ajuste < ? ORDER BY id_ajuste DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buscarAjustePorId(rs.getInt("id_ajuste"));
                }
            }
        }
        return null;
    }

    // Actualizar cabecera de ajuste
    public void actualizarAjusteCabecera(mAjusteStock ajuste) throws SQLException {
        String sql = "UPDATE ajustes_stock_cabecera SET observaciones = ?, aprobado = ?, estado = ? WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, ajuste.getObservaciones());
            ps.setBoolean(2, ajuste.isAprobado());
            ps.setBoolean(3, ajuste.isEstado());
            ps.setInt(4, ajuste.getIdAjuste());

            ps.executeUpdate();
        }
    }

    // Eliminar todos los detalles de un ajuste
    public void eliminarDetallesAjuste(int idAjuste) throws SQLException {
        String sql = "DELETE FROM ajustes_stock_detalle WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            ps.executeUpdate();
        }
    }

    // Eliminar ajuste completo (cabecera y detalles)
    public boolean eliminarAjuste(int idAjuste) throws SQLException {
        // Como la tabla ajustes_stock_detalle tiene CASCADE en la FK,
        // al eliminar la cabecera se eliminan automáticamente los detalles
        String sql = "DELETE FROM ajustes_stock_cabecera WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

// Método alternativo si quieres eliminar explícitamente los detalles primero
    public boolean eliminarAjusteCompleto(int idAjuste) throws SQLException {
        // Iniciar transacción
        conexion.setAutoCommit(false);

        try {
            // Primero eliminar los detalles
            eliminarDetallesAjuste(idAjuste);

            // Luego eliminar la cabecera
            String sqlCabecera = "DELETE FROM ajustes_stock_cabecera WHERE id_ajuste = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlCabecera)) {
                ps.setInt(1, idAjuste);
                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    conexion.commit();
                    return true;
                } else {
                    conexion.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

// Verificar si un ajuste existe
    public boolean existeAjuste(int idAjuste) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ajustes_stock_cabecera WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

// Verificar si un ajuste está aprobado (no se puede eliminar si está aprobado)
    public boolean estaAprobado(int idAjuste) throws SQLException {
        String sql = "SELECT aprobado FROM ajustes_stock_cabecera WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("aprobado");
                }
            }
        }
        return false;
    }

    // Buscar productos por nombre o código de barras
    public List<Object[]> buscarProductos(String busqueda) throws SQLException {
        String sql = "SELECT pc.id_producto, pc.nombre, pd.cod_barra, pd.descripcion, "
                + "COALESCE(s.cantidad_disponible, 0) as stock "
                + "FROM productos_cabecera pc "
                + "INNER JOIN productos_detalle pd ON pc.id_producto = pd.id_producto "
                + "LEFT JOIN stock s ON pd.id_producto = s.id_producto AND pd.cod_barra = s.cod_barra "
                + "WHERE (pc.nombre LIKE ? OR pd.descripcion LIKE ? OR pd.cod_barra LIKE ?) "
                + "AND pc.estado = 1 AND pd.estado = 1 "
                + "ORDER BY pc.nombre, pd.descripcion LIMIT 50";

        List<Object[]> productos = new ArrayList<>();
        String patron = "%" + busqueda + "%";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new Object[]{
                        rs.getInt("id_producto"), // 0
                        rs.getString("nombre"), // 1
                        rs.getString("cod_barra"), // 2
                        rs.getString("descripcion"), // 3
                        rs.getDouble("stock") // 4
                    });
                }
            }
        }
        return productos;
    }
}
