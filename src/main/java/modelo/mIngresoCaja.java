package modelo;

import java.io.Serializable;
import java.util.Date;

public class mIngresoCaja implements Serializable {
    
    private int id;
    private Date fecha;
    private double monto;
    private String concepto;
    private String usuario;
    private boolean anulado;
    private int idCaja;
    
    public mIngresoCaja() {
        this.fecha = new Date();
        this.anulado = false;
    }
    
    public mIngresoCaja(int id, Date fecha, double monto, String concepto, 
                       String usuario, boolean anulado, int idCaja) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.concepto = concepto;
        this.usuario = usuario;
        this.anulado = anulado;
        this.idCaja = idCaja;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        if (concepto == null || concepto.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto no puede estar vacío");
        }
        this.concepto = concepto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public boolean isAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }
    
    @Override
    public String toString() {
        return "Ingreso [ID: " + id + ", Fecha: " + fecha + 
               ", Monto: " + monto + ", Concepto: " + concepto + "]";
    }
}
