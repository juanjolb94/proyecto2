package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EgresoCajaDAO {

    private Connection conexion;

    public EgresoCajaDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Obtener caja activa para validar que se pueda hacer un egreso
    public boolean existeCajaAbierta() throws SQLException {
        CajaDAO cajaDAO = new CajaDAO();
        return cajaDAO.obtenerCajaActual() != null;
    }

    // Obtener el ID de la caja activa
    public int obtenerIdCajaActual() throws SQLException {
        CajaDAO cajaDAO = new CajaDAO();
        mCaja cajaActual = cajaDAO.obtenerCajaActual();
        if (cajaActual != null) {
            return cajaActual.getId();
        }
        return -1; // No hay caja abierta
    }

    // Registrar un nuevo egreso de caja
    public int insertarEgreso(mEgresoCaja egreso) throws SQLException {
        // Validar que exista una caja abierta
        if (!existeCajaAbierta()) {
            throw new SQLException("No hay una caja abierta para registrar egresos");
        }

        // Obtener el ID de la caja activa
        int idCaja = obtenerIdCajaActual();
        egreso.setIdCaja(idCaja);

        String sql = "INSERT INTO gastos (fecha, monto, concepto, usuario, anulado, id_caja) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(egreso.getFecha().getTime()));
            ps.setDouble(2, egreso.getMonto());
            ps.setString(3, egreso.getConcepto());
            ps.setString(4, egreso.getUsuario());
            ps.setBoolean(5, egreso.isAnulado());
            ps.setInt(6, idCaja);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return -1; // Error al insertar
    }

    // Obtener un egreso por ID
    public mEgresoCaja obtenerEgresoPorId(int id) throws SQLException {
        String sql = "SELECT * FROM gastos WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEgreso(rs);
                }
            }
        }

        return null;
    }

    // Listar egresos por caja
    public List<mEgresoCaja> listarEgresosPorCaja(int idCaja) throws SQLException {
        List<mEgresoCaja> egresos = new ArrayList<>();
        String sql = "SELECT * FROM gastos WHERE id_caja = ? ORDER BY fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    egresos.add(mapearEgreso(rs));
                }
            }
        }

        return egresos;
    }

    public boolean actualizarEgreso(mEgresoCaja egreso) throws SQLException {
        String sql = "UPDATE gastos SET monto = ?, concepto = ?, anulado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, egreso.getMonto());
            ps.setString(2, egreso.getConcepto());
            ps.setBoolean(3, egreso.isAnulado());
            ps.setInt(4, egreso.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Anular un egreso
    public boolean anularEgreso(int id) throws SQLException {
        String sql = "UPDATE gastos SET anulado = true WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Método auxiliar para mapear un ResultSet a objeto mEgresoCaja
    private mEgresoCaja mapearEgreso(ResultSet rs) throws SQLException {
        return new mEgresoCaja(
                rs.getInt("id"),
                rs.getTimestamp("fecha"),
                rs.getDouble("monto"),
                rs.getString("concepto"),
                rs.getString("usuario"),
                rs.getBoolean("anulado"),
                rs.getInt("id_caja")
        );
    }

    // Obtener el primer egreso
    public mEgresoCaja obtenerPrimerEgreso() throws SQLException {
        String sql = "SELECT * FROM gastos ORDER BY id ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearEgreso(rs);
            }
        }

        return null;
    }

    // Obtener el egreso anterior
    public mEgresoCaja obtenerEgresoAnterior(int idActual) throws SQLException {
        String sql = "SELECT * FROM gastos WHERE id < ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEgreso(rs);
                }
            }
        }

        return null;
    }

    // Obtener el siguiente egreso
    public mEgresoCaja obtenerEgresoSiguiente(int idActual) throws SQLException {
        String sql = "SELECT * FROM gastos WHERE id > ? ORDER BY id ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEgreso(rs);
                }
            }
        }

        return null;
    }

    // Obtener el último egreso
    public mEgresoCaja obtenerUltimoEgreso() throws SQLException {
        String sql = "SELECT * FROM gastos ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearEgreso(rs);
            }
        }

        return null;
    }
}
