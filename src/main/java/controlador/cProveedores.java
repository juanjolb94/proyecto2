package controlador;

import modelo.ProveedoresDAO;
import vista.vProveedores;
import interfaces.myInterface;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;

public class cProveedores implements myInterface {

    private vProveedores vista;
    private ProveedoresDAO modelo;

    public cProveedores(vProveedores vista) throws SQLException {
        this.vista = vista;
        this.modelo = new ProveedoresDAO();
    }

    // Método para buscar un proveedor por ID
    public Object[] buscarProveedorPorId(int id) {
        try {
            return modelo.buscarProveedorPorId(id);
        } catch (SQLException e) {
            mostrarError("Error al buscar el proveedor: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el primer proveedor
    public Object[] obtenerPrimerProveedor() {
        try {
            return modelo.obtenerPrimerProveedor();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer proveedor: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el proveedor anterior
    public Object[] obtenerAnteriorProveedor(int idActual) {
        try {
            return modelo.obtenerAnteriorProveedor(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el proveedor anterior: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el siguiente proveedor
    public Object[] obtenerSiguienteProveedor(int idActual) {
        try {
            return modelo.obtenerSiguienteProveedor(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente proveedor: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el último proveedor
    public Object[] obtenerUltimoProveedor() {
        try {
            return modelo.obtenerUltimoProveedor();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último proveedor: " + e.getMessage());
            return null;
        }
    }

    // Método para eliminar un proveedor
    public boolean eliminarProveedor(int id) {
        try {
            return modelo.eliminarProveedor(id);
        } catch (SQLException e) {
            // Mostrar mensaje de error específico de la validación
            mostrarError(e.getMessage());
            return false;
        }
    }

    // Método para insertar un nuevo proveedor
    public boolean insertarProveedor(String razonSocial, String ruc, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) {
        try {
            return modelo.insertarProveedor(razonSocial, ruc, telefono, direccion, email, estado, idPersona);
        } catch (SQLException e) {
            mostrarError("Error al insertar el proveedor: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar un proveedor existente
    public boolean actualizarProveedor(int id, String razonSocial, String ruc, String telefono,
            String direccion, String email, boolean estado,
            int idPersona) {
        try {
            return modelo.actualizarProveedor(id, razonSocial, ruc, telefono, direccion, email, estado, idPersona);
        } catch (SQLException e) {
            mostrarError("Error al actualizar el proveedor: " + e.getMessage());
            return false;
        }
    }

    // Método para listar todos los proveedores
    public List<Object[]> listarProveedores() {
        try {
            return modelo.listarProveedores();
        } catch (SQLException e) {
            mostrarError("Error al listar proveedores: " + e.getMessage());
            return null;
        }
    }

    // Método para mostrar un mensaje de error
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        vista.imGrabar();
    }

    @Override
    public void imFiltrar() {
        vista.imFiltrar();
    }

    @Override
    public void imActualizar() {
        vista.imActualizar();
    }

    @Override
    public void imBorrar() {
        vista.imBorrar();
    }

    @Override
    public void imNuevo() {
        vista.imNuevo();
    }

    @Override
    public void imBuscar() {
        vista.imBuscar();
    }

    @Override
    public void imPrimero() {
        vista.imPrimero();
    }

    @Override
    public void imSiguiente() {
        vista.imSiguiente();
    }

    @Override
    public void imAnterior() {
        vista.imAnterior();
    }

    @Override
    public void imUltimo() {
        vista.imUltimo();
    }

    @Override
    public void imImprimir() {
        vista.imImprimir();
    }

    @Override
    public void imInsDet() {
        vista.imInsDet();
    }

    @Override
    public void imDelDet() {
        vista.imDelDet();
    }

    @Override
    public void imCerrar() {
        vista.imCerrar();
    }

    @Override
    public boolean imAbierto() {
        return vista.imAbierto();
    }

    @Override
    public void imAbrir() {
        vista.imAbrir();
    }

    @Override
    public String getTablaActual() {
        return vista.getTablaActual();
    }

    @Override
    public String[] getCamposBusqueda() {
        return vista.getCamposBusqueda();
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        vista.setRegistroSeleccionado(id);
    }
}
