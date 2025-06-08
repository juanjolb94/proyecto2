package modelo;

import java.util.*;
import javax.swing.table.DefaultTableModel;

public class VentaTemporal {

    private int idMesa;
    private String numeroMesa;
    private int idCliente;
    private String observaciones;
    private DefaultTableModel datosTabla;
    private Date fechaCreacion;
    private int subtotal;
    private int iva10;
    private int total;

    // Constructor
    public VentaTemporal(int idMesa, String numeroMesa) {
        this.idMesa = idMesa;
        this.numeroMesa = numeroMesa;
        this.fechaCreacion = new Date();
        this.datosTabla = new DefaultTableModel();
    }

    // Getters y setters
    public int getIdMesa() {
        return idMesa;
    }

    public String getNumeroMesa() {
        return numeroMesa;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public DefaultTableModel getDatosTabla() {
        return datosTabla;
    }

    public void setDatosTabla(DefaultTableModel datosTabla) {
        this.datosTabla = datosTabla;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public int getIva10() {
        return iva10;
    }

    public void setIva10(int iva10) {
        this.iva10 = iva10;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // Método de utilidad
    public boolean tieneProductos() {
        boolean resultado = datosTabla != null && datosTabla.getRowCount() > 0;
        System.out.println("DEBUG VentaTemporal.tieneProductos(): datosTabla="
                + (datosTabla != null ? "existe" : "null")
                + ", rowCount=" + (datosTabla != null ? datosTabla.getRowCount() : "N/A")
                + ", resultado=" + resultado);
        return resultado;
    }
}
