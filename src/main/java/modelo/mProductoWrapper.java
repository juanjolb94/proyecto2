package modelo;

public class mProductoWrapper {
    private final mProducto producto;

    public mProductoWrapper(mProducto producto) {
        this.producto = producto;
    }
    
    // Métodos que JasperReports usará para acceder a los datos
    public Integer getIdProducto() {
        return producto.getIdProducto();
    }
    
    public String getNombre() {
        return producto.getNombre();
    }
    
    public String getCategoria() {
        // Aquí deberías obtener el nombre de la categoría según id_categoria
        return "Categoría " + producto.getIdCategoria();
    }
    
    public String getMarca() {
        // Aquí deberías obtener el nombre de la marca según id_marca
        return "Marca " + producto.getIdMarca();
    }
    
    public String getEstado() {
        return producto.isEstado() ? "Activo" : "Inactivo";
    }
    
    public Double getIva() {
        return producto.getIva();
    }
}
