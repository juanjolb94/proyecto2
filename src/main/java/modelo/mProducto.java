package modelo;

/**
 * Modelo de datos para Productos Representa la información completa de un
 * producto incluyendo datos de cabecera y detalle
 */
public class mProducto {

    // Campos de productos_cabecera
    private int idProducto;
    private String nombre;
    private int idCategoria;
    private int idMarca;
    private double iva;
    private boolean estado;

    // Campos de productos_detalle
    private String descripcion;
    private String codigo;           // cod_barra de productos_detalle 
    private String presentacion;

    // Campos adicionales para reportes y manejo de inventario
    private int stock;
    private int precio;              // Precio en guaraníes (entero)
    private int precioCompra;        // Precio de compra en guaraníes
    private String unidadMedidaCompra;
    private String unidadMedidaStock;

    // Campos auxiliares para nombres de categoria y marca
    private String nombreCategoria;
    private String nombreMarca;

    /**
     * Constructor vacío
     */
    public mProducto() {
        this.stock = 0;
        this.precio = 0;
        this.precioCompra = 0;
        this.iva = 10.0;
        this.estado = true;
        this.unidadMedidaCompra = "UND";
        this.unidadMedidaStock = "UND";
    }

    /**
     * Constructor con parámetros básicos
     */
    public mProducto(int idProducto, String nombre, int idCategoria, int idMarca) {
        this();
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.idMarca = idMarca;
    }

    /**
     * Constructor completo
     */
    public mProducto(int idProducto, String nombre, String descripcion, String codigo,
            int idCategoria, int idMarca, double iva, boolean estado,
            int stock, int precio) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.codigo = codigo;
        this.idCategoria = idCategoria;
        this.idMarca = idMarca;
        this.iva = iva;
        this.estado = estado;
        this.stock = stock;
        this.precio = precio;
        this.precioCompra = 0;
        this.unidadMedidaCompra = "UND";
        this.unidadMedidaStock = "UND";
    }

    // ======================== GETTERS Y SETTERS ========================
    /**
     * ID único del producto
     */
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Nombre del producto
     */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Descripción detallada del producto (viene de productos_detalle)
     */
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Código de barras del producto
     */
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Presentación del producto (LATA, BOTELLA, etc.)
     */
    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    /**
     * ID de la categoría del producto
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * ID de la marca del producto
     */
    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    /**
     * Porcentaje de IVA aplicable al producto
     */
    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    /**
     * Estado del producto (activo/inactivo)
     */
    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    /**
     * Cantidad en stock
     */
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Precio de venta en guaraníes
     */
    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    /**
     * Precio de compra en guaraníes
     */
    public int getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(int precioCompra) {
        this.precioCompra = precioCompra;
    }

    /**
     * Unidad de medida para compras
     */
    public String getUnidadMedidaCompra() {
        return unidadMedidaCompra;
    }

    public void setUnidadMedidaCompra(String unidadMedidaCompra) {
        this.unidadMedidaCompra = unidadMedidaCompra;
    }

    /**
     * Unidad de medida para stock
     */
    public String getUnidadMedidaStock() {
        return unidadMedidaStock;
    }

    public void setUnidadMedidaStock(String unidadMedidaStock) {
        this.unidadMedidaStock = unidadMedidaStock;
    }

    /**
     * Nombre de la categoría (campo auxiliar para reportes)
     */
    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    /**
     * Nombre de la marca (campo auxiliar para reportes)
     */
    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    // ======================== MÉTODOS AUXILIARES ========================
    /**
     * Calcula el valor total del stock (stock * precio)
     *
     * @return Valor total en guaraníes
     */
    public long getValorTotalStock() {
        return (long) stock * precio;
    }

    /**
     * Calcula el precio con IVA incluido
     *
     * @return Precio con IVA en guaraníes
     */
    public int getPrecioConIva() {
        return (int) Math.round(precio * (1 + iva / 100));
    }

    /**
     * Verifica si el producto tiene stock disponible
     *
     * @return true si tiene stock > 0
     */
    public boolean tieneStock() {
        return stock > 0;
    }

    /**
     * Verifica si el producto tiene stock bajo (menor a una cantidad mínima)
     *
     * @param stockMinimo Cantidad mínima considerada
     * @return true si el stock actual es menor al mínimo
     */
    public boolean tieneStockBajo(int stockMinimo) {
        return stock < stockMinimo;
    }

    /**
     * Obtiene una representación completa del producto para mostrar
     *
     * @return String con nombre - descripción
     */
    public String getNombreCompleto() {
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            return nombre + " - " + descripcion;
        }
        return nombre;
    }

    /**
     * Obtiene el estado como texto
     *
     * @return "Activo" o "Inactivo"
     */
    public String getEstadoTexto() {
        return estado ? "Activo" : "Inactivo";
    }

    // ======================== MÉTODOS OBJECT ========================
    @Override
    public String toString() {
        return "mProducto{"
                + "idProducto=" + idProducto
                + ", nombre='" + nombre + '\''
                + ", descripcion='" + descripcion + '\''
                + ", codigo='" + codigo + '\''
                + ", stock=" + stock
                + ", precio=" + precio
                + ", estado=" + estado
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        mProducto mProducto = (mProducto) o;

        return idProducto == mProducto.idProducto;
    }

    @Override
    public int hashCode() {
        return idProducto;
    }

    /**
     * Crea una copia del producto
     *
     * @return Nueva instancia con los mismos datos
     */
    public mProducto clone() {
        mProducto copia = new mProducto();
        copia.idProducto = this.idProducto;
        copia.nombre = this.nombre;
        copia.descripcion = this.descripcion;
        copia.codigo = this.codigo;
        copia.presentacion = this.presentacion;
        copia.idCategoria = this.idCategoria;
        copia.idMarca = this.idMarca;
        copia.iva = this.iva;
        copia.estado = this.estado;
        copia.stock = this.stock;
        copia.precio = this.precio;
        copia.precioCompra = this.precioCompra;
        copia.unidadMedidaCompra = this.unidadMedidaCompra;
        copia.unidadMedidaStock = this.unidadMedidaStock;
        copia.nombreCategoria = this.nombreCategoria;
        copia.nombreMarca = this.nombreMarca;
        return copia;
    }
}
