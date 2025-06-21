package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class mVentas {

    // Propiedades de la venta
    private int idVenta;
    private Date fecha;
    private int total;
    private int idCliente;
    private int idUsuario;
    private int idCaja;
    private boolean anulado;
    private String observaciones;
    private List<DetalleVenta> detalles;
    private String numeroFactura;
    private String numeroTimbrado;

    // Constructor
    public mVentas() {
        this.detalles = new ArrayList<>();
        this.fecha = new Date();
        this.anulado = false;
        this.total = 0;
    }

    // Constructor con parámetros
    public mVentas(int idVenta, Date fecha, int total, int idCliente,
            int idUsuario, int idCaja, boolean anulado, String observaciones) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.total = total;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.idCaja = idCaja;
        this.anulado = anulado;
        this.observaciones = observaciones;
        this.detalles = new ArrayList<>();
    }

    // Métodos para manejar detalles
    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
        calcularTotal();
    }

    public void eliminarDetalle(int indice) {
        if (indice >= 0 && indice < detalles.size()) {
            detalles.remove(indice);
            calcularTotal();
        }
    }

    public void limpiarDetalles() {
        this.detalles.clear();
        this.total = 0;
    }

    private void calcularTotal() {
        this.total = 0;
        for (DetalleVenta detalle : detalles) {
            this.total += detalle.getSubtotal();
        }
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getNumeroTimbrado() {
        return numeroTimbrado;
    }

    public void setNumeroTimbrado(String numeroTimbrado) {
        this.numeroTimbrado = numeroTimbrado;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public boolean isAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
        calcularTotal();
    }

    // Clase interna para el detalle de venta
    public static class DetalleVenta {

        private int id;
        private int idVenta;
        private int idProducto;
        private String codigoBarra;
        private int cantidad;
        private int precioUnitario;
        private int subtotal;

        // Campos adicionales para cálculos de impuestos
        private int baseImponible;
        private int impuesto;
        private double tasaImpuesto;

        // Constructor
        public DetalleVenta() {
            this.tasaImpuesto = 10.0; // IVA del 10% por defecto
        }

        public DetalleVenta(int idVenta, int idProducto, String codigoBarra,
                int cantidad, int precioUnitario) {
            this();
            this.idVenta = idVenta;
            this.idProducto = idProducto;
            this.codigoBarra = codigoBarra;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }

        // Método para calcular subtotal e impuestos
        private void calcularSubtotal() {
            this.subtotal = this.cantidad * this.precioUnitario;

            // Calcular impuesto (método paraguayo: IVA incluido)
            this.impuesto = (int) Math.round(this.subtotal / 11.0);
            this.baseImponible = this.subtotal - this.impuesto;
        }

        // Método para actualizar cantidad y precio
        public void actualizar(int cantidad, int precioUnitario) {
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIdVenta() {
            return idVenta;
        }

        public void setIdVenta(int idVenta) {
            this.idVenta = idVenta;
        }

        public int getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(int idProducto) {
            this.idProducto = idProducto;
        }

        public String getCodigoBarra() {
            return codigoBarra;
        }

        public void setCodigoBarra(String codigoBarra) {
            this.codigoBarra = codigoBarra;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
            calcularSubtotal();
        }

        public int getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(int precioUnitario) {
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }

        public int getSubtotal() {
            return subtotal;
        }

        public int getBaseImponible() {
            return baseImponible;
        }

        public int getImpuesto() {
            return impuesto;
        }

        public double getTasaImpuesto() {
            return tasaImpuesto;
        }

        public void setTasaImpuesto(double tasaImpuesto) {
            this.tasaImpuesto = tasaImpuesto;
            calcularSubtotal();
        }
    }
}
