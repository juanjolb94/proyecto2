package modelo;

import java.util.Date;

public class mAprobacionAjuste {
    private int idAjuste;
    private Date fecha;
    private String observaciones;
    private boolean aprobado;
    private boolean estado;
    private int usuarioId;
    private int cantidadDetalles;

    public mAprobacionAjuste() {
        this.aprobado = false;
        this.estado = true;
    }

    public mAprobacionAjuste(int idAjuste, Date fecha, String observaciones, 
                           boolean aprobado, boolean estado, int usuarioId) {
        this.idAjuste = idAjuste;
        this.fecha = fecha;
        this.observaciones = observaciones;
        this.aprobado = aprobado;
        this.estado = estado;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getIdAjuste() { return idAjuste; }
    public void setIdAjuste(int idAjuste) { this.idAjuste = idAjuste; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getCantidadDetalles() { return cantidadDetalles; }
    public void setCantidadDetalles(int cantidadDetalles) { this.cantidadDetalles = cantidadDetalles; }

    public String getEstadoTexto() {
        return aprobado ? "Aprobado" : "Pendiente";
    }
}