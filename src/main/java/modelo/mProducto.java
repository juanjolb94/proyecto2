package modelo;

public class mProducto {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int idCategoria;
    private int idMarca;
    private double iva;
    private int stock;
    private double precio;
    private boolean estado;
    
    public mProducto(){
        
    }
    
    public mProducto(int idProducto, String codigo, String nombre, String descripcion, 
                     int idCategoria, int idMarca, double iva, int stock, 
                     double precio, boolean estado) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idCategoria = idCategoria;
        this.idMarca = idMarca;
        this.iva = iva;
        this.stock = stock;
        this.precio = precio;
        this.estado = estado;
    }
    
    public int getIdProducto() { 
        return idProducto; 
    }
    
    public void setIdProducto(int idProducto) { 
        this.idProducto = idProducto; 
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public int getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(int idCategoria) {
        if (idCategoria <= 0) {
            throw new IllegalArgumentException("ID de categoría inválido");
        }
        this.idCategoria = idCategoria;
    }
    
    public int getIdMarca() {
        return idMarca;
    }
    
    public void setIdMarca(int idMarca) {
        if (idMarca <= 0) {
            throw new IllegalArgumentException("ID de marca inválido");
        }
        this.idMarca = idMarca;
    }
    
    public double getIva() {
        return iva;
    }
    
    public void setIva(double iva) {
        if (iva < 0) {
            throw new IllegalArgumentException("El IVA no puede ser negativo");
        }
        this.iva = iva;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return String.format("Producto [ID: %d, Código: %s, Nombre: %s, Categoría: %d, Marca: %d, " +
                "Stock: %d, Precio: %.2f, IVA: %.2f, Estado: %s]",
                idProducto, codigo, nombre, idCategoria, idMarca, stock, precio, iva, 
                estado ? "Activo" : "Inactivo");
    }
    
    public String toShortString() {
        return String.format("%d - %s", idProducto, nombre);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        mProducto producto = (mProducto) obj;
        return idProducto == producto.idProducto;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idProducto);
    }
    
    // Método para validar los datos del producto
    public boolean validar() {
        return nombre != null && !nombre.trim().isEmpty() &&
               idCategoria > 0 && idMarca > 0 &&
               precio >= 0 && stock >= 0 && iva >= 0;
    }
}
