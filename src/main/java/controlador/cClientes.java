package controlador;

import modelo.ClientesDAO;
import vista.vClientes;
import interfaces.myInterface;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;

public class cClientes implements myInterface {
    private vClientes vista;
    private ClientesDAO modelo;

    public cClientes(vClientes vista) throws SQLException {
        this.vista = vista;
        this.modelo = new ClientesDAO();
    }

    // Método para buscar un cliente por ID
    public Object[] buscarClientePorId(int id) {
        try {
            return modelo.buscarClientePorId(id);
        } catch (SQLException e) {
            mostrarError("Error al buscar el cliente: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el primer cliente
    public Object[] obtenerPrimerCliente() {
        try {
            return modelo.obtenerPrimerCliente();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer cliente: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el cliente anterior
    public Object[] obtenerAnteriorCliente(int idActual) {
        try {
            return modelo.obtenerAnteriorCliente(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el cliente anterior: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el siguiente cliente
    public Object[] obtenerSiguienteCliente(int idActual) {
        try {
            return modelo.obtenerSiguienteCliente(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente cliente: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el último cliente
    public Object[] obtenerUltimoCliente() {
        try {
            return modelo.obtenerUltimoCliente();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último cliente: " + e.getMessage());
            return null;
        }
    }

    // Método para eliminar un cliente
    public boolean eliminarCliente(int id) {
        try {
            return modelo.eliminarCliente(id);
        } catch (SQLException e) {
            mostrarError("Error al eliminar el cliente: " + e.getMessage());
            return false;
        }
    }

    // Método para insertar un nuevo cliente
    public boolean insertarCliente(String nombre, String ci, String telefono, 
                                   String direccion, String email, boolean estado,
                                   int idPersona) {
        try {
            return modelo.insertarCliente(nombre, ci, telefono, direccion, email, estado, idPersona);
        } catch (SQLException e) {
            mostrarError("Error al insertar el cliente: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar un cliente existente
    public boolean actualizarCliente(int id, String nombre, String ci, String telefono, 
                                     String direccion, String email, boolean estado,
                                     int idPersona) {
        try {
            return modelo.actualizarCliente(id, nombre, ci, telefono, direccion, email, estado, idPersona);
        } catch (SQLException e) {
            mostrarError("Error al actualizar el cliente: " + e.getMessage());
            return false;
        }
    }

    // Método para listar todos los clientes
    public List<Object[]> listarClientes() {
        try {
            return modelo.listarClientes();
        } catch (SQLException e) {
            mostrarError("Error al listar clientes: " + e.getMessage());
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
