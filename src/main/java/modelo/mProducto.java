package modelo;

public class mProducto {
    private int idProducto;
    private String nombre;
    private int idCategoria;
    private int idMarca;
    private double iva;
    private boolean estado;
    
    public mProducto(){
        
    }
    
    public mProducto(int idProducto, String nombre, int idCategoria, int idMarca, double iva, boolean estado) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.idMarca = idMarca;
        this.iva = iva;
        this.estado = estado;
    }
    
    public int getIdProducto() { 
        return idProducto; 
    }
    
    public void setIdProducto(int idProducto) { 
        this.idProducto = idProducto; 
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
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return String.format("Producto [ID: %d, Nombre: %s, Categoría: %d, Marca: %d, IVA: %.2f, Estado: %s]",
                idProducto, nombre, idCategoria, idMarca, iva, estado ? "Activo" : "Inactivo");
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
               iva >= 0;
    }
}
