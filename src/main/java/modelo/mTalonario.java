package modelo;

import java.util.Date;

public class mTalonario {
    private int idTalonario;
    private String numeroTimbrado;
    private Date fechaVencimiento;
    private int facturaDesde;
    private int facturaHasta;
    private boolean estado;
    private String tipoComprobante;
    private String puntoExpedicion;
    private String establecimiento;
    private int facturaActual;
    
    public mTalonario() {
        // Constructor vacío
        this.estado = true; // Por defecto activo
    }
    
    public mTalonario(int idTalonario, String numeroTimbrado, Date fechaVencimiento, 
                     int facturaDesde, int facturaHasta, boolean estado, 
                     String tipoComprobante, String puntoExpedicion, 
                     String establecimiento, int facturaActual) {
        this.idTalonario = idTalonario;
        this.numeroTimbrado = numeroTimbrado;
        this.fechaVencimiento = fechaVencimiento;
        this.facturaDesde = facturaDesde;
        this.facturaHasta = facturaHasta;
        this.estado = estado;
        this.tipoComprobante = tipoComprobante;
        this.puntoExpedicion = puntoExpedicion;
        this.establecimiento = establecimiento;
        this.facturaActual = facturaActual;
    }
    
    // Getters y setters
    public int getIdTalonario() {
        return idTalonario;
    }
    
    public void setIdTalonario(int idTalonario) {
        this.idTalonario = idTalonario;
    }
    
    public String getNumeroTimbrado() {
        return numeroTimbrado;
    }
    
    public void setNumeroTimbrado(String numeroTimbrado) {
        if (numeroTimbrado == null || numeroTimbrado.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de timbrado no puede estar vacío");
        }
        this.numeroTimbrado = numeroTimbrado;
    }
    
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public void setFechaVencimiento(Date fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula");
        }
        this.fechaVencimiento = fechaVencimiento;
    }
    
    public int getFacturaDesde() {
        return facturaDesde;
    }
    
    public void setFacturaDesde(int facturaDesde) {
        if (facturaDesde <= 0) {
            throw new IllegalArgumentException("El número de factura inicial debe ser mayor a cero");
        }
        this.facturaDesde = facturaDesde;
    }
    
    public int getFacturaHasta() {
        return facturaHasta;
    }
    
    public void setFacturaHasta(int facturaHasta) {
        if (facturaHasta <= facturaDesde) {
            throw new IllegalArgumentException("El número de factura final debe ser mayor al inicial");
        }
        this.facturaHasta = facturaHasta;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    public String getTipoComprobante() {
        return tipoComprobante;
    }
    
    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }
    
    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }
    
    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }
    
    public String getEstablecimiento() {
        return establecimiento;
    }
    
    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }
    
    public int getFacturaActual() {
        return facturaActual;
    }
    
    public void setFacturaActual(int facturaActual) {
        if (facturaActual < facturaDesde || facturaActual > facturaHasta) {
            throw new IllegalArgumentException("El número de factura actual debe estar entre el rango inicial y final");
        }
        this.facturaActual = facturaActual;
    }
    
    // Métodos adicionales
    public boolean estaVigente() {
        Date hoy = new Date();
        return this.fechaVencimiento.after(hoy);
    }
    
    public boolean disponible() {
        return this.estado && estaVigente() && (this.facturaActual <= this.facturaHasta);
    }
    
    public String getNumeroCompletoFactura() {
        return String.format("%s-%s-%07d", 
                this.establecimiento, 
                this.puntoExpedicion, 
                this.facturaActual);
    }
    
    public int getFacturasRestantes() {
        return this.facturaHasta - this.facturaActual + 1;
    }
    
    @Override
    public String toString() {
        return String.format("Talonario [ID: %d, Timbrado: %s, Vence: %s, Rango: %d-%d, Estado: %s]",
                idTalonario, numeroTimbrado, fechaVencimiento.toString(), 
                facturaDesde, facturaHasta, estado ? "Activo" : "Inactivo");
    }
}
