package modelo;

import java.awt.Point;
import java.io.Serializable;

public class Mesa implements Serializable {

    private int id;
    private String numero;
    private EstadoMesa estado;
    private Point posicion;
    private int capacidad;
    private int ancho;
    private int alto;
    private String forma; // "CIRCULAR", "RECTANGULAR", "CUADRADA"

    // Enumeración de posibles estados de la mesa
    public enum EstadoMesa {
        DISPONIBLE("Disponible", new java.awt.Color(0, 153, 51)), // Verde
        OCUPADA("Ocupada", new java.awt.Color(204, 0, 0)), // Rojo
        RESERVADA("Reservada", new java.awt.Color(255, 153, 0)), // Naranja
        MANTENIMIENTO("Mantenimiento", new java.awt.Color(102, 102, 102)), // Gris
        PENDIENTE_PAGO("Pendiente de pago", new java.awt.Color(0, 102, 204)); // Azul

        private final String descripcion;
        private final java.awt.Color color;

        EstadoMesa(String descripcion, java.awt.Color color) {
            this.descripcion = descripcion;
            this.color = color;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public java.awt.Color getColor() {
            return color;
        }
    }

    // Constructor
    public Mesa(int id, String numero, EstadoMesa estado, Point posicion, int capacidad, int ancho, int alto, String forma) {
        this.id = id;
        this.numero = numero;
        this.estado = estado;
        this.posicion = posicion;
        this.capacidad = capacidad;
        this.ancho = ancho;
        this.alto = alto;
        this.forma = forma;
    }

    // Constructor simplificado para crear mesas rápidamente
    public Mesa(int id, String numero, Point posicion) {
        this.id = id;
        this.numero = numero;
        this.estado = EstadoMesa.DISPONIBLE;
        this.posicion = posicion;
        this.capacidad = 4;  // Valor por defecto
        this.ancho = 60;     // Tamaño por defecto
        this.alto = 60;      // Tamaño por defecto
        this.forma = "CIRCULAR";  // Forma por defecto
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    public Point getPosicion() {
        return posicion;
    }

    public void setPosicion(Point posicion) {
        this.posicion = posicion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getAncho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public int getAlto() {
        return alto;
    }

    public void setAlto(int alto) {
        this.alto = alto;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }

    @Override
    public String toString() {
        return "Mesa " + numero + " (" + estado.getDescripcion() + ")";
    }
}
