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

public class IngresoCajaDAO {

    private Connection conexion;

    public IngresoCajaDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Obtener caja activa para validar que se pueda hacer un ingreso
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

    // Registrar un nuevo ingreso a caja
    public int insertarIngreso(mIngresoCaja ingreso) throws SQLException {
        // Validar que exista una caja abierta
        if (!existeCajaAbierta()) {
            throw new SQLException("No hay una caja abierta para registrar ingresos");
        }

        // Obtener el ID de la caja activa
        int idCaja = obtenerIdCajaActual();
        ingreso.setIdCaja(idCaja);

        String sql = "INSERT INTO ingresos_caja (fecha, monto, concepto, usuario, anulado, id_caja) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(ingreso.getFecha().getTime()));
            ps.setDouble(2, ingreso.getMonto());
            ps.setString(3, ingreso.getConcepto());
            ps.setString(4, ingreso.getUsuario());
            ps.setBoolean(5, ingreso.isAnulado());
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

    // Obtener un ingreso por ID
    public mIngresoCaja obtenerIngresoPorId(int id) throws SQLException {
        String sql = "SELECT * FROM ingresos_caja WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearIngreso(rs);
                }
            }
        }

        return null;
    }

    // Listar ingresos por caja
    public List<mIngresoCaja> listarIngresosPorCaja(int idCaja) throws SQLException {
        List<mIngresoCaja> ingresos = new ArrayList<>();
        String sql = "SELECT * FROM ingresos_caja WHERE id_caja = ? ORDER BY fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCaja);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingresos.add(mapearIngreso(rs));
                }
            }
        }

        return ingresos;
    }

    public boolean actualizarIngreso(mIngresoCaja ingreso) throws SQLException {
        String sql = "UPDATE ingresos_caja SET monto = ?, concepto = ?, anulado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ingreso.getMonto());
            ps.setString(2, ingreso.getConcepto());
            ps.setBoolean(3, ingreso.isAnulado());
            ps.setInt(4, ingreso.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Anular un ingreso
    public boolean anularIngreso(int id) throws SQLException {
        String sql = "UPDATE ingresos_caja SET anulado = true WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Método auxiliar para mapear un ResultSet a objeto mIngresoCaja
    private mIngresoCaja mapearIngreso(ResultSet rs) throws SQLException {
        return new mIngresoCaja(
                rs.getInt("id"),
                rs.getTimestamp("fecha"),
                rs.getDouble("monto"),
                rs.getString("concepto"),
                rs.getString("usuario"),
                rs.getBoolean("anulado"),
                rs.getInt("id_caja")
        );
    }

    // Obtener el primer ingreso
    public mIngresoCaja obtenerPrimerIngreso() throws SQLException {
        String sql = "SELECT * FROM ingresos_caja ORDER BY id ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearIngreso(rs);
            }
        }

        return null;
    }

    // Obtener el ingreso anterior
    public mIngresoCaja obtenerIngresoAnterior(int idActual) throws SQLException {
        String sql = "SELECT * FROM ingresos_caja WHERE id < ? ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearIngreso(rs);
                }
            }
        }

        return null;
    }

    // Obtener el siguiente ingreso
    public mIngresoCaja obtenerIngresoSiguiente(int idActual) throws SQLException {
        String sql = "SELECT * FROM ingresos_caja WHERE id > ? ORDER BY id ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearIngreso(rs);
                }
            }
        }

        return null;
    }

    // Obtener el último ingreso
    public mIngresoCaja obtenerUltimoIngreso() throws SQLException {
        String sql = "SELECT * FROM ingresos_caja ORDER BY id DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearIngreso(rs);
            }
        }

        return null;
    }
}
