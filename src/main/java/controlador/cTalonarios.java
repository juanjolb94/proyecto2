package controlador;

import modelo.TalonarioDAO;
import modelo.mTalonario;
import vista.vTalonarios;
import interfaces.myInterface;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

public class cTalonarios implements myInterface {
    private vTalonarios vista;
    private TalonarioDAO modelo;

    public cTalonarios(vTalonarios vista) throws SQLException {
        this.vista = vista;
        this.modelo = new TalonarioDAO();
    }

    // Método para buscar un talonario por ID
    public mTalonario buscarTalonarioPorId(int id) {
        try {
            return modelo.buscarTalonarioPorId(id);
        } catch (SQLException e) {
            mostrarError("Error al buscar el talonario: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el primer talonario
    public mTalonario obtenerPrimerTalonario() {
        try {
            return modelo.obtenerPrimerTalonario();
        } catch (SQLException e) {
            mostrarError("Error al obtener el primer talonario: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el talonario anterior
    public mTalonario obtenerTalonarioAnterior(int idActual) {
        try {
            return modelo.obtenerTalonarioAnterior(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el talonario anterior: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el siguiente talonario
    public mTalonario obtenerTalonarioSiguiente(int idActual) {
        try {
            return modelo.obtenerTalonarioSiguiente(idActual);
        } catch (SQLException e) {
            mostrarError("Error al obtener el siguiente talonario: " + e.getMessage());
            return null;
        }
    }

    // Método para obtener el último talonario
    public mTalonario obtenerUltimoTalonario() {
        try {
            return modelo.obtenerUltimoTalonario();
        } catch (SQLException e) {
            mostrarError("Error al obtener el último talonario: " + e.getMessage());
            return null;
        }
    }

    // Método para eliminar un talonario
    public boolean eliminarTalonario(int id) {
        try {
            return modelo.eliminarTalonario(id);
        } catch (SQLException e) {
            mostrarError("Error al eliminar el talonario: " + e.getMessage());
            return false;
        }
    }

    // Método para insertar un nuevo talonario
    public int insertarTalonario(String numeroTimbrado, Date fechaVencimiento, 
                               int facturaDesde, int facturaHasta, boolean estado,
                               String tipoComprobante, String puntoExpedicion, 
                               String establecimiento) {
        try {
            mTalonario talonario = new mTalonario();
            talonario.setNumeroTimbrado(numeroTimbrado);
            talonario.setFechaVencimiento(fechaVencimiento);
            talonario.setFacturaDesde(facturaDesde);
            talonario.setFacturaHasta(facturaHasta);
            talonario.setEstado(estado);
            talonario.setTipoComprobante(tipoComprobante);
            talonario.setPuntoExpedicion(puntoExpedicion);
            talonario.setEstablecimiento(establecimiento);
            talonario.setFacturaActual(facturaDesde); // Por defecto, empezamos con la primera factura
            
            return modelo.insertarTalonario(talonario);
        } catch (SQLException e) {
            mostrarError("Error al insertar el talonario: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            mostrarError("Datos inválidos: " + e.getMessage());
            return -1;
        }
    }

    // Método para actualizar un talonario existente
    public boolean actualizarTalonario(int id, String numeroTimbrado, Date fechaVencimiento, 
                                     int facturaDesde, int facturaHasta, boolean estado,
                                     String tipoComprobante, String puntoExpedicion, 
                                     String establecimiento, int facturaActual) {
        try {
            mTalonario talonario = new mTalonario();
            talonario.setIdTalonario(id);
            talonario.setNumeroTimbrado(numeroTimbrado);
            talonario.setFechaVencimiento(fechaVencimiento);
            talonario.setFacturaDesde(facturaDesde);
            talonario.setFacturaHasta(facturaHasta);
            talonario.setEstado(estado);
            talonario.setTipoComprobante(tipoComprobante);
            talonario.setPuntoExpedicion(puntoExpedicion);
            talonario.setEstablecimiento(establecimiento);
            talonario.setFacturaActual(facturaActual);
            
            return modelo.actualizarTalonario(talonario);
        } catch (SQLException e) {
            mostrarError("Error al actualizar el talonario: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            mostrarError("Datos inválidos: " + e.getMessage());
            return false;
        }
    }

    // Método para listar todos los talonarios
    public List<mTalonario> listarTalonarios() {
        try {
            return modelo.listarTalonarios();
        } catch (SQLException e) {
            mostrarError("Error al listar talonarios: " + e.getMessage());
            return null;
        }
    }
    
    // Método para listar talonarios activos
    public List<mTalonario> listarTalonariosActivos() {
        try {
            return modelo.obtenerTalonariosActivos();
        } catch (SQLException e) {
            mostrarError("Error al listar talonarios activos: " + e.getMessage());
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
        vista.grabar();
    }

    @Override
    public void imFiltrar() {
        vista.filtrar();
    }

    @Override
    public void imActualizar() {
        vista.actualizar();
    }

    @Override
    public void imBorrar() {
        vista.borrar();
    }

    @Override
    public void imNuevo() {
        vista.nuevo();
    }

    @Override
    public void imBuscar() {
        vista.buscar();
    }

    @Override
    public void imPrimero() {
        vista.primero();
    }

    @Override
    public void imSiguiente() {
        vista.siguiente();
    }

    @Override
    public void imAnterior() {
        vista.anterior();
    }

    @Override
    public void imUltimo() {
        vista.ultimo();
    }

    @Override
    public void imImprimir() {
        vista.imprimir();
    }

    @Override
    public void imInsDet() {
        // No aplica para talonarios
    }

    @Override
    public void imDelDet() {
        // No aplica para talonarios
    }

    @Override
    public void imCerrar() {
        vista.dispose();
    }

    @Override
    public boolean imAbierto() {
        return vista.isVisible();
    }

    @Override
    public void imAbrir() {
        vista.setVisible(true);
    }

    @Override
    public String getTablaActual() {
        return "talonarios";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_talonario", "numero_timbrado", "tipo_comprobante"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        vista.setRegistroSeleccionado(id);
    }
}
