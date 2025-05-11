package controlador;

import modelo.PersonasDAO;
import vista.vPersonas;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class PersonasController {
    private vPersonas vista; // Referencia a la vista
    private PersonasDAO modelo; // Referencia al modelo

    public PersonasController(vPersonas vista) {
        this.vista = vista;
        this.modelo = new PersonasDAO(); // Inicializa el modelo
    }
    
    // Método para buscar una persona por ID
    public String[] buscarPersonaPorId(int id) {
        try {
            return modelo.buscarPersonaPorId(id); // Usa el modelo para buscar la persona
        } catch (SQLException e) {
            mostrarError("Error al buscar la persona: " + e.getMessage());
            return null;
        }
    }
    
    // Método para obtener el primer registro
    public String[] obtenerPrimerPersona() {
        try {
            return modelo.obtenerPrimerPersona();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer registro: " + e.getMessage());
            return null;
        }
    }
    
    // Método para obtener el registro anterior
    public String[] obtenerAnteriorPersona(int idActual) {
        try {
            return modelo.obtenerAnteriorPersona(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el registro anterior: " + e.getMessage());
            return null;
        }
    }
    
    // Método para obtener el siguiente registro
    public String[] obtenerSiguientePersona(int idActual) {
        try {
            return modelo.obtenerSiguientePersona(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente registro: " + e.getMessage());
            return null;
        }
    }
    
    // Método para obtener el último registro
    public String[] obtenerUltimaPersona() {
        try {
            return modelo.obtenerUltimaPersona();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último registro: " + e.getMessage());
            return null;
        }
    }
    
    // Método para eliminar una persona
    public void eliminarPersona(int id) {
        try {
            modelo.eliminarPersona(id); // Llama al método del modelo para eliminar el registro
        } catch (SQLException e) {
            mostrarError("Error al eliminar el registro: " + e.getMessage());
        }
    }
    
    // Método para guardar o actualizar una persona
    public void guardarPersona(int id, String nombre, String apellido, String ci, String telefono, String correo, String fechaNac, boolean activo) {
        try {
            if (id == 0) {
                // Insertar un nuevo registro
                modelo.insertarPersona(nombre, apellido, ci, telefono, correo, fechaNac, activo);
                JOptionPane.showMessageDialog(null, "Registro guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Actualizar un registro existente
                modelo.actualizarPersona(id, nombre, apellido, ci, telefono, correo, fechaNac, activo);
                JOptionPane.showMessageDialog(null, "Registro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar el registro: " + e.getMessage());
        }
    }
    
    // Método para mostrar un mensaje de error
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
