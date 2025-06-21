package servicio;

import modelo.TalonarioDAO;
import modelo.mTalonario;
import java.sql.SQLException;
import java.util.Date;

public class sTalonarios {
    private TalonarioDAO talonarioDAO;
    
    public sTalonarios() throws SQLException {
        this.talonarioDAO = new TalonarioDAO();
    }
    
    /**
     * Clase para transportar datos del talonario para ventas
     */
    public static class DatosTalonario {
        private String numeroFactura;
        private String numeroTimbrado;
        private Date fechaVencimiento;
        private int idTalonario;
        
        public DatosTalonario(String numeroFactura, String numeroTimbrado, 
                             Date fechaVencimiento, int idTalonario) {
            this.numeroFactura = numeroFactura;
            this.numeroTimbrado = numeroTimbrado;
            this.fechaVencimiento = fechaVencimiento;
            this.idTalonario = idTalonario;
        }
        
        // Getters
        public String getNumeroFactura() { return numeroFactura; }
        public String getNumeroTimbrado() { return numeroTimbrado; }
        public Date getFechaVencimiento() { return fechaVencimiento; }
        public int getIdTalonario() { return idTalonario; }
    }
    
    
    
    /**
     * Obtiene datos del talonario activo para mostrar en pantalla
     */
    public DatosTalonario obtenerDatosTalonarioActivo() throws SQLException {
        mTalonario talonario = talonarioDAO.obtenerTalonarioActivoVigente();
        if (talonario == null) {
            throw new SQLException("No hay talonarios activos y vigentes disponibles");
        }
        
        String numeroFactura = String.format("%s-%s-%07d", 
            talonario.getEstablecimiento(),
            talonario.getPuntoExpedicion(),
            talonario.getFacturaActual());
            
        return new DatosTalonario(
            numeroFactura,
            talonario.getNumeroTimbrado(),
            talonario.getFechaVencimiento(),
            talonario.getIdTalonario()
        );
    }
    
    /**
     * Consume el siguiente número de factura y retorna datos para la venta
     */
    public DatosTalonario consumirSiguienteFactura() throws SQLException {
        mTalonario talonario = talonarioDAO.obtenerTalonarioActivoVigente();
        if (talonario == null) {
            throw new SQLException("No hay talonarios activos disponibles");
        }
        
        // Verificar límite
        if (talonario.getFacturaActual() >= talonario.getFacturaHasta()) {
            throw new SQLException("El talonario ha alcanzado su límite de facturas");
        }
        
        // Generar número actual
        String numeroFactura = String.format("%s-%s-%07d", 
            talonario.getEstablecimiento(),
            talonario.getPuntoExpedicion(),
            talonario.getFacturaActual());
        
        // Crear datos para la venta
        DatosTalonario datos = new DatosTalonario(
            numeroFactura,
            talonario.getNumeroTimbrado(),
            talonario.getFechaVencimiento(),
            talonario.getIdTalonario()
        );
        
        // Incrementar para la siguiente
        talonarioDAO.incrementarFacturaActual(talonario.getIdTalonario());
        
        return datos;
    }
    
    /**
     * Verifica si un talonario está vencido
     */
    public boolean isTimbradoVencido(Date fechaVencimiento) {
        return fechaVencimiento.before(new Date());
    }
}