package modelo;

import java.io.Serializable;
import java.util.Date;

public class mPrecioCabecera implements Serializable {
    
    private int id;
    private String nombre;
    private Date fechaCreacion;
    private String moneda;
    private boolean activo;
    private String observaciones;
    
    // Constructor por defecto
    public mPrecioCabecera() {
        this.fechaCreacion = new Date();
        this.moneda = "PYG";
        this.activo = true;
    }
    
    // Constructor con parámetros
    public mPrecioCabecera(int id, String nombre, Date fechaCreacion, String moneda, boolean activo, String observaciones) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.moneda = moneda;
        this.activo = activo;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getMoneda() {
        return moneda;
    }
    
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public String toString() {
        return nombre + " (" + moneda + ")";
    }
}
