package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AprobacionAjusteDAO {

    private Connection conexion;

    public AprobacionAjusteDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Buscar ajustes por filtros
    public List<mAprobacionAjuste> buscarAjustesPorFiltros(Date fechaDesde, Date fechaHasta, Integer idAjuste) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cab.id_ajuste, cab.fecha, cab.observaciones, cab.aprobado, cab.estado, cab.usuario_id, ");
        sql.append("COUNT(asd.id_detalle) as cantidad_detalles ");
        sql.append("FROM ajustes_stock_cabecera cab ");
        sql.append("LEFT JOIN ajustes_stock_detalle asd ON cab.id_ajuste = asd.id_ajuste ");
        sql.append("WHERE cab.estado = 1 ");

        List<Object> parametros = new ArrayList<>();

        // Filtro por fecha desde
        if (fechaDesde != null) {
            sql.append("AND DATE(cab.fecha) >= ? ");
            parametros.add(new java.sql.Date(fechaDesde.getTime()));
        }

        // Filtro por fecha hasta
        if (fechaHasta != null) {
            sql.append("AND DATE(cab.fecha) <= ? ");
            parametros.add(new java.sql.Date(fechaHasta.getTime()));
        }

        // Filtro por ID específico
        if (idAjuste != null && idAjuste > 0) {
            sql.append("AND cab.id_ajuste = ? ");
            parametros.add(idAjuste);
        }

        sql.append("GROUP BY cab.id_ajuste, cab.fecha, cab.observaciones, cab.aprobado, cab.estado, cab.usuario_id ");
        sql.append("ORDER BY cab.fecha DESC, cab.id_ajuste DESC");

        List<mAprobacionAjuste> ajustes = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            // Establecer parámetros
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mAprobacionAjuste ajuste = new mAprobacionAjuste();
                    ajuste.setIdAjuste(rs.getInt("id_ajuste"));
                    ajuste.setFecha(rs.getTimestamp("fecha"));
                    ajuste.setObservaciones(rs.getString("observaciones"));
                    ajuste.setAprobado(rs.getBoolean("aprobado"));
                    ajuste.setEstado(rs.getBoolean("estado"));
                    ajuste.setUsuarioId(rs.getInt("usuario_id"));
                    ajuste.setCantidadDetalles(rs.getInt("cantidad_detalles"));

                    ajustes.add(ajuste);
                }
            }
        }

        return ajustes;
    }

    // Cambiar estado de aprobación de un ajuste
    public boolean cambiarAprobacionAjuste(int idAjuste, boolean aprobar) throws SQLException {
        String sql = "UPDATE ajustes_stock_cabecera SET aprobado = ? WHERE id_ajuste = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBoolean(1, aprobar);
            ps.setInt(2, idAjuste);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    // Obtener ajuste por ID
    public mAprobacionAjuste obtenerAjustePorId(int idAjuste) throws SQLException {
        String sql = "SELECT cab.id_ajuste, cab.fecha, cab.observaciones, cab.aprobado, cab.estado, cab.usuario_id, "
                + "COUNT(asd.id_detalle) as cantidad_detalles "
                + "FROM ajustes_stock_cabecera cab "
                + "LEFT JOIN ajustes_stock_detalle asd ON cab.id_ajuste = asd.id_ajuste "
                + "WHERE cab.id_ajuste = ? "
                + "GROUP BY cab.id_ajuste, cab.fecha, cab.observaciones, cab.aprobado, cab.estado, cab.usuario_id";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mAprobacionAjuste ajuste = new mAprobacionAjuste();
                    ajuste.setIdAjuste(rs.getInt("id_ajuste"));
                    ajuste.setFecha(rs.getTimestamp("fecha"));
                    ajuste.setObservaciones(rs.getString("observaciones"));
                    ajuste.setAprobado(rs.getBoolean("aprobado"));
                    ajuste.setEstado(rs.getBoolean("estado"));
                    ajuste.setUsuarioId(rs.getInt("usuario_id"));
                    ajuste.setCantidadDetalles(rs.getInt("cantidad_detalles"));

                    return ajuste;
                }
            }
        }
        return null;
    }

    // Obtener detalles de un ajuste para información
    public List<Object[]> obtenerDetallesAjuste(int idAjuste) throws SQLException {
        String sql = "SELECT asd.cod_barra, pc.nombre, pd.descripcion, "
                + "asd.cantidad_sistema, asd.cantidad_ajuste, "
                + "(asd.cantidad_ajuste - asd.cantidad_sistema) as diferencia, "
                + "asd.observaciones "
                + "FROM ajustes_stock_detalle asd "
                + "INNER JOIN productos_cabecera pc ON asd.id_producto = pc.id_producto "
                + "INNER JOIN productos_detalle pd ON asd.id_producto = pd.id_producto AND asd.cod_barra = pd.cod_barra "
                + "WHERE asd.id_ajuste = ? "
                + "ORDER BY pc.nombre, pd.descripcion";

        List<Object[]> detalles = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new Object[]{
                        rs.getString("cod_barra"), // 0
                        rs.getString("nombre"), // 1
                        rs.getString("descripcion"), // 2
                        rs.getInt("cantidad_sistema"), // 3
                        rs.getInt("cantidad_ajuste"), // 4
                        rs.getInt("diferencia"), // 5
                        rs.getString("observaciones") // 6
                    });
                }
            }
        }

        return detalles;
    }

    // Verificar si un ajuste tiene detalles
    public boolean tieneDetalles(int idAjuste) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ajustes_stock_detalle WHERE id_ajuste = ?";

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

    // Obtener estadísticas de ajustes
    public Object[] obtenerEstadisticasAjustes(Date fechaDesde, Date fechaHasta) throws SQLException {
        String sql = "SELECT "
                + "COUNT(*) as total_ajustes, "
                + "SUM(CASE WHEN aprobado = 1 THEN 1 ELSE 0 END) as aprobados, "
                + "SUM(CASE WHEN aprobado = 0 THEN 1 ELSE 0 END) as pendientes "
                + "FROM ajustes_stock_cabecera "
                + "WHERE estado = 1 ";

        List<Object> parametros = new ArrayList<>();

        if (fechaDesde != null) {
            sql += "AND DATE(fecha) >= ? ";
            parametros.add(new java.sql.Date(fechaDesde.getTime()));
        }

        if (fechaHasta != null) {
            sql += "AND DATE(fecha) <= ? ";
            parametros.add(new java.sql.Date(fechaHasta.getTime()));
        }

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("total_ajustes"), // 0
                        rs.getInt("aprobados"), // 1
                        rs.getInt("pendientes") // 2
                    };
                }
            }
        }

        return new Object[]{0, 0, 0};
    }
}
