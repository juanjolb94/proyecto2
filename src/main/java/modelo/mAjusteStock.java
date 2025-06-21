package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class mAjusteStock {

    private int idAjuste;
    private Date fecha;
    private String observaciones;
    private int usuarioId;
    private boolean aprobado;
    private boolean estado;
    private List<DetalleAjuste> detalles;

    public mAjusteStock() {
        this.detalles = new ArrayList<>();
        this.fecha = new Date();
        this.estado = true;
        this.aprobado = false;
    }

    // Clase interna para detalle de ajuste
    public static class DetalleAjuste {

        private int idDetalle;
        private int idAjuste;
        private int idProducto;
        private String codBarra;
        private int cantidadSistema;
        private int cantidadAjuste;
        private String observaciones;
        private String nombreProducto;
        private String descripcionProducto;

        public DetalleAjuste() {
            this.cantidadAjuste = 0;
        }

        public DetalleAjuste(int idProducto, String codBarra, int cantidadSistema) {
            this.idProducto = idProducto;
            this.codBarra = codBarra;
            this.cantidadSistema = cantidadSistema;
            this.cantidadAjuste = 0;
        }

        // Getters y Setters modificados
        public int getCantidadSistema() {
            return cantidadSistema;
        }

        public void setCantidadSistema(int cantidadSistema) {
            this.cantidadSistema = cantidadSistema;
        }

        public int getCantidadAjuste() {
            return cantidadAjuste;
        }                 // CAMBIO: double a int

        public void setCantidadAjuste(int cantidadAjuste) {
            this.cantidadAjuste = cantidadAjuste;
        }

        public int getDiferencia() {
            return cantidadAjuste - cantidadSistema;
        }

        public int getIdDetalle() {
            return idDetalle;
        }

        public void setIdDetalle(int idDetalle) {
            this.idDetalle = idDetalle;
        }

        public int getIdAjuste() {
            return idAjuste;
        }

        public void setIdAjuste(int idAjuste) {
            this.idAjuste = idAjuste;
        }

        public int getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(int idProducto) {
            this.idProducto = idProducto;
        }

        public String getCodBarra() {
            return codBarra;
        }

        public void setCodBarra(String codBarra) {
            this.codBarra = codBarra;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public void setNombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
        }

        public String getDescripcionProducto() {
            return descripcionProducto;
        }

        public void setDescripcionProducto(String descripcionProducto) {
            this.descripcionProducto = descripcionProducto;
        }

        public String getDescripcionCompleta() {
            return (nombreProducto != null ? nombreProducto : "")
                    + (descripcionProducto != null ? " - " + descripcionProducto : "");
        }
    }

    // Métodos para manejar detalles
    public void agregarDetalle(DetalleAjuste detalle) {
        if (detalle != null) {
            this.detalles.add(detalle);
        }
    }

    public void eliminarDetalle(int indice) {
        if (indice >= 0 && indice < detalles.size()) {
            detalles.remove(indice);
        }
    }

    public void limpiarDetalles() {
        this.detalles.clear();
    }

    public boolean tieneDetalles() {
        return detalles != null && !detalles.isEmpty();
    }

    // Getters y Setters principales
    public int getIdAjuste() {
        return idAjuste;
    }

    public void setIdAjuste(int idAjuste) {
        this.idAjuste = idAjuste;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public List<DetalleAjuste> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleAjuste> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
