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
            // Mostrar mensaje de error específico de la validación
            mostrarError(e.getMessage());
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
            boolean exito = modelo.actualizarCliente(id, nombre, ci, telefono, direccion, email, estado, idPersona);

            // Si la actualización fue exitosa, notificar al formulario de ventas
            if (exito) {
                notificarActualizacionCliente();
            }

            return exito;
        } catch (SQLException e) {
            mostrarError("Error al actualizar el cliente: " + e.getMessage());
            return false;
        }
    }

// Método para notificar al formulario de ventas que recargue la lista de clientes
    private void notificarActualizacionCliente() {
        try {
            // Buscar en todas las ventanas principales
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof javax.swing.JFrame) {
                    javax.swing.JFrame mainFrame = (javax.swing.JFrame) window;

                    // Buscar JDesktopPane en el frame principal
                    java.awt.Component[] components = mainFrame.getContentPane().getComponents();
                    for (java.awt.Component comp : components) {
                        if (comp instanceof javax.swing.JDesktopPane) {
                            javax.swing.JDesktopPane desktop = (javax.swing.JDesktopPane) comp;

                            // Buscar vRegVentas en el desktop
                            for (javax.swing.JInternalFrame frame : desktop.getAllFrames()) {
                                if (frame instanceof vista.vRegVentas && !frame.isClosed()) {
                                    ((vista.vRegVentas) frame).recargarClientes();
                                    System.out.println("Lista de clientes recargada en formulario de ventas");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al notificar actualización de cliente: " + e.getMessage());
            // No mostrar error al usuario, es una funcionalidad secundaria
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
