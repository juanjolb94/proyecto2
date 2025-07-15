package controlador;

import modelo.RolesDAO;
import vista.vRoles;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class RolesController {

    private vRoles vista; // Referencia a la vista
    private RolesDAO modelo; // Referencia al modelo

    public RolesController(vRoles vista) {
        this.vista = vista;
        this.modelo = new RolesDAO(); // Inicializa el modelo
    }

    // Método para buscar un rol por ID
    public String[] buscarRolPorId(int id) {
        try {
            return modelo.buscarRolPorId(id);
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al buscar el rol: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Método para obtener el primer registro
    public String[] obtenerPrimerRol() {
        try {
            return modelo.obtenerPrimerRol();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer registro: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el registro anterior
    public String[] obtenerAnteriorRol(int idActual) {
        try {
            return modelo.obtenerAnteriorRol(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el registro anterior: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el siguiente registro
    public String[] obtenerSiguienteRol(int idActual) {
        try {
            return modelo.obtenerSiguienteRol(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente registro: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el último registro
    public String[] obtenerUltimoRol() {
        try {
            return modelo.obtenerUltimoRol();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último registro: " + e.getMessage());
            return null;
        }
    }

    // Método para mostrar un mensaje de error
    private void mostrarError(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(null, mensaje, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public void eliminarRol(int id) {
        try {
            modelo.eliminarRol(id); // Llama al método del modelo con validaciones
            JOptionPane.showMessageDialog(null, "Rol eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            // Mostrar mensaje de error específico de la validación
            mostrarError(e.getMessage());
        }
    }

    public void guardarRol(int id, String nombre, boolean activo) {
        try {
            if (id == 0) {
                // Insertar un nuevo registro
                modelo.insertarRol(nombre, activo);
                JOptionPane.showMessageDialog(null, "Registro guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Actualizar un registro existente
                modelo.actualizarRol(id, nombre, activo);
                JOptionPane.showMessageDialog(null, "Registro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar el registro: " + e.getMessage());
        }
    }
}
