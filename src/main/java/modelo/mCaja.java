package modelo;

import java.io.Serializable;
import java.util.Date;

/**
 * Clase modelo para la entidad Caja
 */
public class mCaja implements Serializable {
    
    private int id;
    private Date fechaApertura;
    private long montoApertura;
    private String usuarioApertura;
    private Date fechaCierre;
    private long montoCierre;
    private long montoVentas;
    private long montoGastos;
    private long diferencia;
    private String usuarioCierre;
    private boolean estadoAbierto;
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public mCaja() {
        this.fechaApertura = new Date();
        this.estadoAbierto = true;
    }
    
    /**
     * Constructor con parámetros básicos para apertura de caja
     * @param montoApertura Monto inicial de la caja
     * @param usuarioApertura Usuario que abre la caja
     */
    public mCaja(long montoApertura, String usuarioApertura) {
        this.fechaApertura = new Date();
        this.montoApertura = montoApertura;
        this.usuarioApertura = usuarioApertura;
        this.estadoAbierto = true;
    }

    /**
     * @return El ID de la caja
     */
    public int getId() {
        return id;
    }

    /**
     * @param id El ID a establecer
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return La fecha de apertura
     */
    public Date getFechaApertura() {
        return fechaApertura;
    }

    /**
     * @param fechaApertura La fecha de apertura a establecer
     */
    public void setFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    /**
     * @return El monto de apertura
     */
    public long getMontoApertura() {
        return montoApertura;
    }

    /**
     * @param montoApertura El monto de apertura a establecer
     */
    public void setMontoApertura(long montoApertura) {
        this.montoApertura = montoApertura;
    }

    /**
     * @return El usuario que abrió la caja
     */
    public String getUsuarioApertura() {
        return usuarioApertura;
    }

    /**
     * @param usuarioApertura El usuario de apertura a establecer
     */
    public void setUsuarioApertura(String usuarioApertura) {
        this.usuarioApertura = usuarioApertura;
    }

    /**
     * @return La fecha de cierre
     */
    public Date getFechaCierre() {
        return fechaCierre;
    }

    /**
     * @param fechaCierre La fecha de cierre a establecer
     */
    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    /**
     * @return El monto de cierre
     */
    public long getMontoCierre() {
        return montoCierre;
    }

    /**
     * @param montoCierre El monto de cierre a establecer
     */
    public void setMontoCierre(long montoCierre) {
        this.montoCierre = montoCierre;
    }

    /**
     * @return El monto de ventas
     */
    public long getMontoVentas() {
        return montoVentas;
    }

    /**
     * @param montoVentas El monto de ventas a establecer
     */
    public void setMontoVentas(long montoVentas) {
        this.montoVentas = montoVentas;
    }

    /**
     * @return El monto de gastos
     */
    public long getMontoGastos() {
        return montoGastos;
    }

    /**
     * @param montoGastos El monto de gastos a establecer
     */
    public void setMontoGastos(long montoGastos) {
        this.montoGastos = montoGastos;
    }

    /**
     * @return La diferencia entre monto esperado y real
     */
    public long getDiferencia() {
        return diferencia;
    }

    /**
     * @param diferencia La diferencia a establecer
     */
    public void setDiferencia(long diferencia) {
        this.diferencia = diferencia;
    }

    /**
     * @return El usuario que cerró la caja
     */
    public String getUsuarioCierre() {
        return usuarioCierre;
    }

    /**
     * @param usuarioCierre El usuario de cierre a establecer
     */
    public void setUsuarioCierre(String usuarioCierre) {
        this.usuarioCierre = usuarioCierre;
    }

    /**
     * @return true si la caja está abierta, false si está cerrada
     */
    public boolean isEstadoAbierto() {
        return estadoAbierto;
    }

    /**
     * @param estadoAbierto El estado (abierto/cerrado) a establecer
     */
    public void setEstadoAbierto(boolean estadoAbierto) {
        this.estadoAbierto = estadoAbierto;
    }

    /**
     * @return Las observaciones
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * @param observaciones Las observaciones a establecer
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    /**
     * Calcula el monto total esperado en caja
     * @return El monto esperado (apertura + ventas - gastos)
     */
    public long calcularMontoEsperado() {
        return montoApertura + montoVentas - montoGastos;
    }
    
    @Override
    public String toString() {
        return "Caja #" + id + " - " + (estadoAbierto ? "ABIERTA" : "CERRADA");
    }
}