package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.service.ReporteService;

/**
 * Clase para gestionar las operaciones de base de datos relacionadas con la caja
 */
public class CajaDAO {
    
    /**
     * Obtiene la caja actual (última caja abierta o en estado abierto)
     * @return Objeto mCaja con los datos de la caja actual o null si no hay ninguna abierta
     * @throws SQLException
     */
    public mCaja obtenerCajaActual() throws SQLException {
        String sql = "SELECT * FROM cajas WHERE estado_abierto = true ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                mCaja caja = new mCaja();
                caja.setId(rs.getInt("id"));
                caja.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                caja.setMontoApertura(rs.getLong("monto_apertura"));
                caja.setUsuarioApertura(rs.getString("usuario_apertura"));
                caja.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                caja.setMontoCierre(rs.getLong("monto_cierre"));
                caja.setMontoVentas(rs.getLong("monto_ventas"));
                caja.setMontoGastos(rs.getLong("monto_gastos"));
                caja.setDiferencia(rs.getLong("diferencia"));
                caja.setUsuarioCierre(rs.getString("usuario_cierre"));
                caja.setEstadoAbierto(rs.getBoolean("estado_abierto"));
                caja.setObservaciones(rs.getString("observaciones"));
                
                return caja;
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene la última caja cerrada
     * @return Objeto mCaja con los datos de la última caja cerrada o null si no hay ninguna
     * @throws SQLException
     */
    public mCaja obtenerUltimaCajaCerrada() throws SQLException {
        String sql = "SELECT * FROM cajas WHERE estado_abierto = false ORDER BY fecha_cierre DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                mCaja caja = new mCaja();
                caja.setId(rs.getInt("id"));
                caja.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                caja.setMontoApertura(rs.getLong("monto_apertura"));
                caja.setUsuarioApertura(rs.getString("usuario_apertura"));
                caja.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                caja.setMontoCierre(rs.getLong("monto_cierre"));
                caja.setMontoVentas(rs.getLong("monto_ventas"));
                caja.setMontoGastos(rs.getLong("monto_gastos"));
                caja.setDiferencia(rs.getLong("diferencia"));
                caja.setUsuarioCierre(rs.getString("usuario_cierre"));
                caja.setEstadoAbierto(rs.getBoolean("estado_abierto"));
                caja.setObservaciones(rs.getString("observaciones"));
                
                return caja;
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene el total de ventas registradas para la caja actual
     * @return Monto total de ventas
     * @throws SQLException
     */
    public long obtenerTotalVentasCajaActual() throws SQLException {
        mCaja cajaActual = obtenerCajaActual();
        
        if (cajaActual == null) {
            return 0;
        }
        
        // Obtener la fecha de apertura de la caja actual
        Date fechaApertura = cajaActual.getFechaApertura();
        
        // Consulta SQL para obtener el total de ventas desde la fecha de apertura
        String sql = "SELECT COALESCE(SUM(total), 0) AS total FROM ventas WHERE fecha >= ? AND anulado = false";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(fechaApertura.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Obtiene el total de gastos registrados para la caja actual
     * @return Monto total de gastos
     * @throws SQLException
     */
    public long obtenerTotalGastosCajaActual() throws SQLException {
        mCaja cajaActual = obtenerCajaActual();
        
        if (cajaActual == null) {
            return 0;
        }
        
        // Obtener la fecha de apertura de la caja actual
        Date fechaApertura = cajaActual.getFechaApertura();
        
        // Consulta SQL para obtener el total de gastos desde la fecha de apertura
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM gastos WHERE fecha >= ? AND anulado = false";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(fechaApertura.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Abre una nueva caja
     * @param caja Objeto con los datos de la nueva caja
     * @return ID de la caja creada
     * @throws SQLException
     */
    public int abrirCaja(mCaja caja) throws SQLException {
        // Verificar si ya hay una caja abierta
        mCaja cajaActual = obtenerCajaActual();
        
        if (cajaActual != null) {
            throw new SQLException("Ya existe una caja abierta. Debe cerrar la caja actual antes de abrir una nueva.");
        }
        
        // SQL para insertar una nueva caja
        String sql = "INSERT INTO cajas (fecha_apertura, monto_apertura, usuario_apertura, estado_abierto) "
                  + "VALUES (?, ?, ?, true)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setTimestamp(1, new Timestamp(caja.getFechaApertura().getTime()));
            ps.setLong(2, caja.getMontoApertura());
            ps.setString(3, caja.getUsuarioApertura());
            
            ps.executeUpdate();
            
            // Obtener el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Cierra una caja existente
     * @param caja Objeto con los datos de la caja a cerrar
     * @return true si se cerró correctamente, false en caso contrario
     * @throws SQLException
     */
    public boolean cerrarCaja(mCaja caja) throws SQLException {
        // Verificar que la caja esté abierta
        if (!caja.isEstadoAbierto()) {
            throw new SQLException("La caja ya está cerrada.");
        }
        
        // SQL para actualizar la caja
        String sql = "UPDATE cajas SET "
                  + "fecha_cierre = ?, "
                  + "monto_cierre = ?, "
                  + "monto_ventas = ?, "
                  + "monto_gastos = ?, "
                  + "diferencia = ?, "
                  + "usuario_cierre = ?, "
                  + "estado_abierto = false, "
                  + "observaciones = ? "
                  + "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(caja.getFechaCierre().getTime()));
            ps.setLong(2, caja.getMontoCierre());
            ps.setLong(3, caja.getMontoVentas());
            ps.setLong(4, caja.getMontoGastos());
            ps.setLong(5, caja.getDiferencia());
            ps.setString(6, caja.getUsuarioCierre());
            ps.setString(7, caja.getObservaciones());
            ps.setInt(8, caja.getId());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Obtiene un historial de cajas en un rango de fechas
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Lista de objetos mCaja
     * @throws SQLException
     */
    public List<mCaja> obtenerHistorialCajas(Date fechaInicio, Date fechaFin) throws SQLException {
        List<mCaja> cajas = new ArrayList<>();
        
        String sql = "SELECT * FROM cajas WHERE fecha_apertura BETWEEN ? AND ? ORDER BY fecha_apertura DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(fechaInicio.getTime()));
            ps.setTimestamp(2, new Timestamp(fechaFin.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mCaja caja = new mCaja();
                    caja.setId(rs.getInt("id"));
                    caja.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    caja.setMontoApertura(rs.getLong("monto_apertura"));
                    caja.setUsuarioApertura(rs.getString("usuario_apertura"));
                    caja.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                    caja.setMontoCierre(rs.getLong("monto_cierre"));
                    caja.setMontoVentas(rs.getLong("monto_ventas"));
                    caja.setMontoGastos(rs.getLong("monto_gastos"));
                    caja.setDiferencia(rs.getLong("diferencia"));
                    caja.setUsuarioCierre(rs.getString("usuario_cierre"));
                    caja.setEstadoAbierto(rs.getBoolean("estado_abierto"));
                    caja.setObservaciones(rs.getString("observaciones"));
                    
                    cajas.add(caja);
                }
            }
        }
        
        return cajas;
    }
    
    /**
     * Registra el arqueo detallado de billetes y monedas para una caja
     * @param idCaja ID de la caja
     * @param denominacion Valor de la denominación
     * @param cantidad Cantidad de billetes o monedas
     * @param esBillete true si es billete, false si es moneda
     * @return true si se registró correctamente, false en caso contrario
     * @throws SQLException
     */
    public boolean registrarArqueo(int idCaja, int denominacion, int cantidad, boolean esBillete) throws SQLException {
        // Verificar si ya existe un registro para esta denominación
        String sqlCheck = "SELECT id FROM caja_arqueo WHERE id_caja = ? AND denominacion = ? AND es_billete = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
            
            psCheck.setInt(1, idCaja);
            psCheck.setInt(2, denominacion);
            psCheck.setBoolean(3, esBillete);
            
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    // Si existe, actualizar
                    int idExistente = rs.getInt("id");
                    String sqlUpdate = "UPDATE caja_arqueo SET cantidad = ? WHERE id = ?";
                    
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                        psUpdate.setInt(1, cantidad);
                        psUpdate.setInt(2, idExistente);
                        
                        return psUpdate.executeUpdate() > 0;
                    }
                } else {
                    // Si no existe, insertar
                    String sqlInsert = "INSERT INTO caja_arqueo (id_caja, denominacion, cantidad, es_billete) VALUES (?, ?, ?, ?)";
                    
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setInt(1, idCaja);
                        psInsert.setInt(2, denominacion);
                        psInsert.setInt(3, cantidad);
                        psInsert.setBoolean(4, esBillete);
                        
                        return psInsert.executeUpdate() > 0;
                    }
                }
            }
        }
    }
    
    /**
     * Obtiene el detalle de arqueo para una caja específica
     * @param idCaja ID de la caja
     * @return Lista de objetos con el detalle del arqueo
     * @throws SQLException
     */
    public List<Object[]> obtenerDetalleArqueo(int idCaja) throws SQLException {
        List<Object[]> detalles = new ArrayList<>();
        
        String sql = "SELECT denominacion, cantidad, es_billete FROM caja_arqueo WHERE id_caja = ? ORDER BY es_billete DESC, denominacion DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idCaja);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] detalle = new Object[3];
                    detalle[0] = rs.getInt("denominacion");
                    detalle[1] = rs.getInt("cantidad");
                    detalle[2] = rs.getBoolean("es_billete");
                    
                    detalles.add(detalle);
                }
            }
        }
        
        return detalles;
    }
    
    /**
     * Imprime un reporte de apertura de caja
     * @param idCaja ID de la caja
     * @throws SQLException
     */
    public void imprimirReporteApertura(int idCaja) throws SQLException {
        try {
            // Crear un servicio de reportes
            ReporteService reporteService = new ReporteService();
            
            // Crear un mapa de parámetros para el reporte
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("REPORT_TITLE", "Comprobante de Apertura de Caja");
            parametros.put("FECHA_GENERACION", new Date());
            parametros.put("ID_CAJA", idCaja);
            
            // Generar el reporte
            reporteService.generarReporte("apertura_caja", parametros);
        } catch (Exception e) {
            throw new SQLException("Error al imprimir reporte de apertura: " + e.getMessage(), e);
        }
    }
    
    /**
     * Imprime un reporte de cierre de caja
     * @param idCaja ID de la caja
     * @throws SQLException
     */
    public void imprimirReporteCierre(int idCaja) throws SQLException {
        try {
            // Crear un servicio de reportes
            ReporteService reporteService = new ReporteService();
            
            // Crear un mapa de parámetros para el reporte
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("REPORT_TITLE", "Comprobante de Cierre de Caja");
            parametros.put("FECHA_GENERACION", new Date());
            parametros.put("ID_CAJA", idCaja);
            
            // Generar el reporte
            reporteService.generarReporte("cierre_caja", parametros);
        } catch (Exception e) {
            throw new SQLException("Error al imprimir reporte de cierre: " + e.getMessage(), e);
        }
    }
    
    /**
     * Guarda el detalle completo de arqueo (billetes y monedas)
     * @param idCaja ID de la caja
     * @param billetes Array con las cantidades de billetes por denominación
     * @param denominacionesBilletes Array con las denominaciones de billetes
     * @param monedas Array con las cantidades de monedas por denominación
     * @param denominacionesMonedas Array con las denominaciones de monedas
     * @throws SQLException
     */
    public void guardarArqueoCompleto(int idCaja, int[] billetes, int[] denominacionesBilletes, 
                                      int[] monedas, int[] denominacionesMonedas) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Eliminar registros previos
                String sqlDelete = "DELETE FROM caja_arqueo WHERE id_caja = ?";
                try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                    psDelete.setInt(1, idCaja);
                    psDelete.executeUpdate();
                }
                
                // Insertar billetes
                for (int i = 0; i < billetes.length; i++) {
                    if (billetes[i] > 0) {
                        registrarArqueo(idCaja, denominacionesBilletes[i], billetes[i], true);
                    }
                }
                
                // Insertar monedas
                for (int i = 0; i < monedas.length; i++) {
                    if (monedas[i] > 0) {
                        registrarArqueo(idCaja, denominacionesMonedas[i], monedas[i], false);
                    }
                }
                
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}