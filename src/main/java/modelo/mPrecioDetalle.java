package modelo;

import java.io.Serializable;
import java.util.Date;

public class mPrecioDetalle implements Serializable {
    
    private int id;
    private int idPrecioCabecera;
    private String codigoBarra;
    private double precio;
    private Date fechaVigencia;
    private boolean activo;
    
    // Campo adicional para mostrar (no almacenado en base de datos)
    private String nombreProducto;
    
    // Constructor por defecto
    public mPrecioDetalle() {
        this.fechaVigencia = new Date();
        this.activo = true;
    }
    
    // Constructor con parámetros
    public mPrecioDetalle(int id, int idPrecioCabecera, String codigoBarra, double precio, 
                         Date fechaVigencia, boolean activo) {
        this.id = id;
        this.idPrecioCabecera = idPrecioCabecera;
        this.codigoBarra = codigoBarra;
        this.precio = precio;
        this.fechaVigencia = fechaVigencia;
        this.activo = activo;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdPrecioCabecera() {
        return idPrecioCabecera;
    }
    
    public void setIdPrecioCabecera(int idPrecioCabecera) {
        this.idPrecioCabecera = idPrecioCabecera;
    }
    
    public String getCodigoBarra() {
        return codigoBarra;
    }
    
    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public Date getFechaVigencia() {
        return fechaVigencia;
    }
    
    public void setFechaVigencia(Date fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    @Override
    public String toString() {
        return codigoBarra + " - " + precio;
    }
}
