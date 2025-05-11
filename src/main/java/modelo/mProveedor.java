package modelo;

public class mProveedor {
    private int idProveedor;
    private String razonSocial;
    private String ruc;
    private String telefono;
    private String direccion;
    private String email;
    private boolean estado;
    
    public mProveedor() {
        // Constructor vacío
    }
    
    public mProveedor(int idProveedor, String razonSocial, String ruc, String telefono, 
                     String direccion, String email, boolean estado) {
        this.idProveedor = idProveedor;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.telefono = telefono;
        this.direccion = direccion;
        this.email = email;
        this.estado = estado;
    }
    
    // Getters y setters
    public int getIdProveedor() {
        return idProveedor;
    }
    
    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }
    
    public String getRazonSocial() {
        return razonSocial;
    }
    
    public void setRazonSocial(String razonSocial) {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new IllegalArgumentException("La razón social no puede estar vacía");
        }
        this.razonSocial = razonSocial;
    }
    
    public String getRuc() {
        return ruc;
    }
    
    public void setRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC no puede estar vacío");
        }
        this.ruc = ruc;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return String.format("Proveedor [ID: %d, Razón Social: %s, RUC: %s, Estado: %s]",
                idProveedor, razonSocial, ruc, estado ? "Activo" : "Inactivo");
    }
    
    public String toShortString() {
        return String.format("%d - %s", idProveedor, razonSocial);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        mProveedor proveedor = (mProveedor) obj;
        return idProveedor == proveedor.idProveedor;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idProveedor);
    }
    
    // Método para validar los datos del proveedor
    public boolean validar() {
        return razonSocial != null && !razonSocial.trim().isEmpty() &&
               ruc != null && !ruc.trim().isEmpty();
    }
    
    // Validar formato de email (si se requiere)
    public boolean validarEmail() {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email opcional
        }
        // Expresión regular simple para validar email
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
