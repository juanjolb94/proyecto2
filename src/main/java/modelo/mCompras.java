package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class mCompras {

    private int idCompra;
    private int idProveedor;
    private Date fechaCompra;
    private String numeroFactura;
    private String tipoDocumento;
    private String timbrado;
    private String condicion;
    private Date fechaVencimiento;        // CAMPO AGREGADO
    private double subtotal;
    private double totalIva5;             // CAMPO AGREGADO
    private double totalIva10;            // CAMPO AGREGADO
    private double totalIva;
    private String nroPlanilla;           // CAMPO AGREGADO
    private double totalCompra;
    private String observaciones;
    private boolean estado;

    // Lista para manejar los detalles de compra
    private List<DetalleCompra> detalles;

    public mCompras() {
        this.detalles = new ArrayList<>();
    }

    public mCompras(int idCompra, int idProveedor, Date fechaCompra, String numeroFactura,
            double totalCompra, String observaciones, boolean estado) {
        this.idCompra = idCompra;
        this.idProveedor = idProveedor;
        this.fechaCompra = fechaCompra;
        this.numeroFactura = numeroFactura;
        this.totalCompra = totalCompra;
        this.observaciones = observaciones;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    // ======================== GETTERS Y SETTERS ========================
    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(String timbrado) {
        this.timbrado = timbrado;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    // ========== GETTERS Y SETTERS DE CAMPOS AGREGADOS ==========
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalIva5() {
        return totalIva5;
    }

    public void setTotalIva5(double totalIva5) {
        this.totalIva5 = totalIva5;
    }

    public double getTotalIva10() {
        return totalIva10;
    }

    public void setTotalIva10(double totalIva10) {
        this.totalIva10 = totalIva10;
    }

    public double getTotalIva() {
        return totalIva;
    }

    public void setTotalIva(double totalIva) {
        this.totalIva = totalIva;
    }

    public String getNroPlanilla() {
        return nroPlanilla;
    }

    public void setNroPlanilla(String nroPlanilla) {
        this.nroPlanilla = nroPlanilla;
    }

    // ========== GETTERS Y SETTERS EXISTENTES ==========
    public double getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(double totalCompra) {
        this.totalCompra = totalCompra;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }

    // ======================== MÉTODOS DE NEGOCIO ========================
    // Método para agregar un detalle a la compra
    public void agregarDetalle(DetalleCompra detalle) {
        this.detalles.add(detalle);
        calcularTotal();
    }

    // Método para eliminar un detalle
    public void eliminarDetalle(int indice) {
        if (indice >= 0 && indice < detalles.size()) {
            detalles.remove(indice);
            calcularTotal();
        }
    }

    // Método para calcular el total de la compra a partir de los detalles
    private void calcularTotal() {
        this.totalCompra = 0.0;
        this.subtotal = 0.0;
        this.totalIva5 = 0.0;
        this.totalIva10 = 0.0;
        this.totalIva = 0.0;

        for (DetalleCompra detalle : detalles) {
            this.totalCompra += detalle.getSubtotal();
            this.subtotal += detalle.getSubtotal() - detalle.getImpuesto();
            this.totalIva10 += detalle.getImpuesto(); // Asumiendo que todo es IVA 10%
        }

        this.totalIva = this.totalIva5 + this.totalIva10;
    }

    // ======================== CLASE INTERNA DETALLECOMPRA ========================
    // Clase interna para manejar los detalles de compra
    public static class DetalleCompra {

        private int idCompra;
        private int idProducto;
        private String codBarra;
        private int cantidad;
        private double precioUnitario; // Mantenemos como double para cálculos precisos
        private double subtotal;       // Mantenemos como double para cálculos precisos
        private int baseImponible;     // Nueva propiedad
        private int impuesto;          // Nueva propiedad

        public DetalleCompra() {
        }

        // Constructor para cuando se conoce el subtotal directamente
        public DetalleCompra(int idCompra, int idProducto, String codBarra,
                int cantidad, double subtotal) {
            this.idCompra = idCompra;
            this.idProducto = idProducto;
            this.codBarra = codBarra;
            this.cantidad = cantidad;
            this.subtotal = subtotal;
            this.calcularPrecioUnitario();
            this.calcularBaseImponibleEImpuesto();
        }

        // Constructor alternativo cuando se conoce el precio unitario
        public DetalleCompra(int idCompra, int idProducto, String codBarra,
                int cantidad, double precioUnitario, boolean esPrecioUnitario) {
            this.idCompra = idCompra;
            this.idProducto = idProducto;
            this.codBarra = codBarra;
            this.cantidad = cantidad;
            if (esPrecioUnitario) {
                this.precioUnitario = precioUnitario;
                this.calcularSubtotal();
            } else {
                this.subtotal = precioUnitario; // En realidad es el subtotal
                this.calcularPrecioUnitario();
            }
            this.calcularBaseImponibleEImpuesto();
        }

        // Método para calcular el precio unitario a partir del subtotal
        private void calcularPrecioUnitario() {
            if (this.cantidad > 0) {
                this.precioUnitario = this.subtotal / this.cantidad;
            } else {
                this.precioUnitario = 0;
            }
        }

        // ========== GETTERS Y SETTERS DE DETALLECOMPRA ==========
        public int getIdCompra() {
            return idCompra;
        }

        public void setIdCompra(int idCompra) {
            this.idCompra = idCompra;
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

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
            calcularSubtotal();
        }

        public double getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(double precioUnitario) {
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
        }

        public double getSubtotal() {
            return subtotal;
        }

        public int getBaseImponible() {
            return baseImponible;
        }

        public void setBaseImponible(int baseImponible) {
            this.baseImponible = baseImponible;
        }

        public int getImpuesto() {
            return impuesto;
        }

        public void setImpuesto(int impuesto) {
            this.impuesto = impuesto;
        }

        // ========== MÉTODOS DE CÁLCULO ==========
        // Método para calcular el subtotal
        private void calcularSubtotal() {
            this.subtotal = this.cantidad * this.precioUnitario;
        }

        // Método para establecer el subtotal directamente
        public void setSubtotal(int subtotal) {
            this.subtotal = subtotal;
        }

        private void calcularBaseImponibleEImpuesto() {
            // Llamamos al método con parámetro usando un valor por defecto
            calcularBaseImponibleEImpuesto(10.0); // 10% IVA por defecto
        }

        // Nuevo método para calcular base imponible e impuesto
        private void calcularBaseImponibleEImpuesto(double tipoIVA) {
            // Calcular el subtotal total
            double subtotalTotal = this.cantidad * this.precioUnitario;
            int subtotalRedondeado = (int) Math.round(subtotalTotal);

            double divisor;
            if (tipoIVA == 10.0) {
                divisor = 11.0; // Para IVA 10%
            } else if (tipoIVA == 5.0) {
                divisor = 21.0; // Para IVA 5%
            } else {
                divisor = 100.0 / tipoIVA + 1; // Fórmula general
            }

            // Calcular impuesto
            int impuestoTotal = (int) Math.round(subtotalRedondeado / divisor);

            // Calcular base imponible
            int baseImponibleTotal = subtotalRedondeado - impuestoTotal;

            // Actualizar los valores
            this.baseImponible = baseImponibleTotal;
            this.impuesto = impuestoTotal;
            this.subtotal = subtotalRedondeado;
        }

        // ========== MÉTODOS AUXILIARES ==========
        // Método para actualizar valores y recalcular
        public void actualizar(int cantidad, double precioUnitario) {
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            calcularSubtotal();
            calcularBaseImponibleEImpuesto();
        }

        public void actualizarValoresPrecalculados(int baseImponible, int impuesto, int subtotal) {
            this.baseImponible = baseImponible;
            this.impuesto = impuesto;
            this.subtotal = subtotal;

            // Recalcular precio unitario si es necesario
            if (this.cantidad > 0) {
                this.precioUnitario = (double) subtotal / this.cantidad;
            }
        }

        // Método para obtener precio unitario redondeado (solo para visualización)
        public int getPrecioUnitarioRedondeado() {
            if (this.cantidad > 0) {
                return (int) Math.round(this.subtotal / this.cantidad);
            }
            return 0;
        }

        // Método para obtener subtotal redondeado
        public int getSubtotalRedondeado() {
            return (int) Math.round(this.subtotal);
        }

        // Método para actualizar valores y recalcular
        public void actualizarCantidad(int cantidad) {
            double precioUnitarioExacto = this.subtotal / this.cantidad;
            this.cantidad = cantidad;
            this.subtotal = precioUnitarioExacto * this.cantidad;
            this.calcularBaseImponibleEImpuesto();
        }

        // Método para actualizar el subtotal directamente
        public void actualizarSubtotal(double subtotal) {
            this.subtotal = subtotal;
            this.calcularPrecioUnitario();
            this.calcularBaseImponibleEImpuesto();
        }
    }
}
