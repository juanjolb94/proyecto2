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

public class TalonarioDAO {
    private Connection conexion;
    
    public TalonarioDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }
    
    // Método para insertar un nuevo talonario
    public int insertarTalonario(mTalonario talonario) throws SQLException {
        String sql = "INSERT INTO talonarios (numero_timbrado, fecha_vencimiento, factura_desde, "
                + "factura_hasta, estado, tipo_comprobante, punto_expedicion, establecimiento, factura_actual) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, talonario.getNumeroTimbrado());
            ps.setTimestamp(2, new Timestamp(talonario.getFechaVencimiento().getTime()));
            ps.setInt(3, talonario.getFacturaDesde());
            ps.setInt(4, talonario.getFacturaHasta());
            ps.setBoolean(5, talonario.isEstado());
            ps.setString(6, talonario.getTipoComprobante());
            ps.setString(7, talonario.getPuntoExpedicion());
            ps.setString(8, talonario.getEstablecimiento());
            ps.setInt(9, talonario.getFacturaActual());
            
            ps.executeUpdate();
            
            // Obtener el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return -1; // Error al insertar
    }
    
    // Método para actualizar un talonario existente
    public boolean actualizarTalonario(mTalonario talonario) throws SQLException {
        String sql = "UPDATE talonarios SET numero_timbrado = ?, fecha_vencimiento = ?, "
                + "factura_desde = ?, factura_hasta = ?, estado = ?, tipo_comprobante = ?, "
                + "punto_expedicion = ?, establecimiento = ?, factura_actual = ? "
                + "WHERE id_talonario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, talonario.getNumeroTimbrado());
            ps.setTimestamp(2, new Timestamp(talonario.getFechaVencimiento().getTime()));
            ps.setInt(3, talonario.getFacturaDesde());
            ps.setInt(4, talonario.getFacturaHasta());
            ps.setBoolean(5, talonario.isEstado());
            ps.setString(6, talonario.getTipoComprobante());
            ps.setString(7, talonario.getPuntoExpedicion());
            ps.setString(8, talonario.getEstablecimiento());
            ps.setInt(9, talonario.getFacturaActual());
            ps.setInt(10, talonario.getIdTalonario());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // Método para eliminar un talonario
    public boolean eliminarTalonario(int idTalonario) throws SQLException {
        String sql = "DELETE FROM talonarios WHERE id_talonario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idTalonario);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // Método para buscar un talonario por ID
    public mTalonario buscarTalonarioPorId(int id) throws SQLException {
        String sql = "SELECT * FROM talonarios WHERE id_talonario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTalonario(rs);
                }
            }
        }
        
        return null;
    }
    
    // Método para listar todos los talonarios
    public List<mTalonario> listarTalonarios() throws SQLException {
        List<mTalonario> talonarios = new ArrayList<>();
        String sql = "SELECT * FROM talonarios ORDER BY estado DESC, fecha_vencimiento DESC";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                talonarios.add(mapearTalonario(rs));
            }
        }
        
        return talonarios;
    }
    
    // Método para obtener talonarios activos y vigentes
    public List<mTalonario> obtenerTalonariosActivos() throws SQLException {
        List<mTalonario> talonarios = new ArrayList<>();
        String sql = "SELECT * FROM talonarios WHERE estado = true AND fecha_vencimiento > NOW() "
                + "AND factura_actual <= factura_hasta ORDER BY fecha_vencimiento";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                talonarios.add(mapearTalonario(rs));
            }
        }
        
        return talonarios;
    }
    
    // Método para obtener el primer talonario
    public mTalonario obtenerPrimerTalonario() throws SQLException {
        String sql = "SELECT * FROM talonarios ORDER BY id_talonario ASC LIMIT 1";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return mapearTalonario(rs);
            }
        }
        
        return null;
    }
    
    // Método para obtener el talonario anterior
    public mTalonario obtenerTalonarioAnterior(int idActual) throws SQLException {
        String sql = "SELECT * FROM talonarios WHERE id_talonario < ? ORDER BY id_talonario DESC LIMIT 1";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTalonario(rs);
                }
            }
        }
        
        return null;
    }
    
    // Método para obtener el siguiente talonario
    public mTalonario obtenerTalonarioSiguiente(int idActual) throws SQLException {
        String sql = "SELECT * FROM talonarios WHERE id_talonario > ? ORDER BY id_talonario ASC LIMIT 1";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTalonario(rs);
                }
            }
        }
        
        return null;
    }
    
    // Método para obtener el último talonario
    public mTalonario obtenerUltimoTalonario() throws SQLException {
        String sql = "SELECT * FROM talonarios ORDER BY id_talonario DESC LIMIT 1";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return mapearTalonario(rs);
            }
        }
        
        return null;
    }
    
    // Método para incrementar el número de factura actual
    public boolean incrementarFacturaActual(int idTalonario) throws SQLException {
        String sql = "UPDATE talonarios SET factura_actual = factura_actual + 1 WHERE id_talonario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idTalonario);
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // Método auxiliar para mapear un ResultSet a un objeto mTalonario
    private mTalonario mapearTalonario(ResultSet rs) throws SQLException {
        mTalonario talonario = new mTalonario();
        
        talonario.setIdTalonario(rs.getInt("id_talonario"));
        talonario.setNumeroTimbrado(rs.getString("numero_timbrado"));
        talonario.setFechaVencimiento(rs.getTimestamp("fecha_vencimiento"));
        talonario.setFacturaDesde(rs.getInt("factura_desde"));
        talonario.setFacturaHasta(rs.getInt("factura_hasta"));
        talonario.setEstado(rs.getBoolean("estado"));
        talonario.setTipoComprobante(rs.getString("tipo_comprobante"));
        talonario.setPuntoExpedicion(rs.getString("punto_expedicion"));
        talonario.setEstablecimiento(rs.getString("establecimiento"));
        talonario.setFacturaActual(rs.getInt("factura_actual"));
        
        return talonario;
    }
}
