package controlador;

import interfaces.myInterface;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.JOptionPane;
import modelo.DatabaseConnection;
import modelo.EgresoCajaDAO;
import modelo.mEgresoCaja;
import vista.vEgresoCaja;
import vista.vLogin;

public class cEgresoCaja implements myInterface {

    private vEgresoCaja vista;
    private EgresoCajaDAO modelo;
    private mEgresoCaja egresoActual;

    public cEgresoCaja(vEgresoCaja vista) throws SQLException {
        this.vista = vista;
        this.modelo = new EgresoCajaDAO();
        this.egresoActual = new mEgresoCaja();

        // Verificar si hay una caja abierta
        verificarCajaAbierta();
    }

    private void verificarCajaAbierta() throws SQLException {
        if (!modelo.existeCajaAbierta()) {
            vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar egresos.");
            vista.deshabilitarComponentes();
        }
    }

    public boolean registrarEgreso() {
        try {
            // Verificar si hay una caja abierta
            if (!modelo.existeCajaAbierta()) {
                vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar egresos.");
                return false;
            }

            // Obtener ID para determinar si es inserción o actualización
            int id = vista.getId();

            // Validar datos
            String montoTexto = vista.getMonto();
            String concepto = vista.getConcepto();
            boolean estadoActivo = vista.getEstado();

            // Validar monto
            if (montoTexto == null || montoTexto.trim().isEmpty()) {
                vista.mostrarMensajeError("Debe ingresar un monto");
                vista.enfocarMonto();
                return false;
            }

            double monto = 0;
            try {
                // Eliminar caracteres no numéricos y convertir a double
                montoTexto = montoTexto.replaceAll("[^0-9,.]", "").replace(",", ".");
                monto = Double.parseDouble(montoTexto);
            } catch (NumberFormatException e) {
                vista.mostrarMensajeError("El monto debe ser un valor numérico válido");
                vista.enfocarMonto();
                return false;
            }

            if (monto <= 0) {
                vista.mostrarMensajeError("El monto debe ser mayor a cero");
                vista.enfocarMonto();
                return false;
            }

            // Validar concepto
            if (concepto == null || concepto.trim().isEmpty()) {
                vista.mostrarMensajeError("Debe ingresar un concepto");
                vista.enfocarConcepto();
                return false;
            }

            // Si es una actualización (id > 0)
            if (id > 0) {
                // Verificar que el egreso existe
                mEgresoCaja egresoExistente = modelo.obtenerEgresoPorId(id);
                if (egresoExistente == null) {
                    vista.mostrarMensajeError("El egreso con ID " + id + " no existe");
                    return false;
                }

                // Verificar si está anulado
                if (egresoExistente.isAnulado()) {
                    vista.mostrarMensajeError("No se puede modificar un egreso anulado");
                    return false;
                }

                // Actualizar el egreso
                egresoExistente.setMonto(monto);
                egresoExistente.setConcepto(concepto);
                egresoExistente.setAnulado(!estadoActivo);

                boolean resultado = modelo.actualizarEgreso(egresoExistente);

                if (resultado) {
                    vista.mostrarMensajeExito("Egreso actualizado correctamente");
                    vista.limpiarCampos(); // Limpiar y volver a 0 el txtId
                    return true;
                } else {
                    vista.mostrarMensajeError("No se pudo actualizar el egreso");
                    return false;
                }
            } else {
                // Es un nuevo egreso (id = 0)
                // Crear objeto de egreso
                egresoActual = new mEgresoCaja();
                egresoActual.setFecha(new Date());
                egresoActual.setMonto(monto);
                egresoActual.setConcepto(concepto);
                egresoActual.setUsuario(vLogin.getUsuarioAutenticado());
                egresoActual.setAnulado(!estadoActivo);

                // Insertar en la base de datos
                int nuevoId = modelo.insertarEgreso(egresoActual);

                if (nuevoId > 0) {
                    egresoActual.setId(nuevoId);
                    vista.mostrarMensajeExito("Egreso registrado correctamente con ID: " + nuevoId);
                    vista.limpiarCampos(); // Limpiar y volver a 0 el txtId
                    return true;
                } else {
                    vista.mostrarMensajeError("No se pudo registrar el egreso");
                    return false;
                }
            }

        } catch (SQLException e) {
            vista.mostrarMensajeError("Error de base de datos: " + e.getMessage());
            return false;
        } catch (Exception e) {
            vista.mostrarMensajeError("Error al registrar egreso: " + e.getMessage());
            return false;
        }
    }

    public void cargarEgreso(int id) {
        try {
            mEgresoCaja egreso = modelo.obtenerEgresoPorId(id);

            if (egreso != null) {
                this.egresoActual = egreso;
                vista.mostrarEgreso(
                        egreso.getId(),
                        egreso.getFecha(),
                        egreso.getMonto(),
                        egreso.getConcepto(),
                        egreso.getUsuario(),
                        egreso.isAnulado()
                );
            } else {
                vista.mostrarMensajeError("No se encontró el egreso con ID: " + id);
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al cargar egreso: " + e.getMessage());
        }
    }

    public void buscarPrimerEgreso() {
        try {
            mEgresoCaja egreso = modelo.obtenerPrimerEgreso();

            if (egreso != null) {
                cargarEgreso(egreso.getId());
            } else {
                vista.mostrarMensajeInfo("No hay egresos registrados");
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar primer egreso: " + e.getMessage());
        }
    }

    public void buscarEgresoAnterior() {
        try {
            if (egresoActual != null && egresoActual.getId() > 0) {
                mEgresoCaja egreso = modelo.obtenerEgresoAnterior(egresoActual.getId());

                if (egreso != null) {
                    cargarEgreso(egreso.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay egresos anteriores");
                }
            } else {
                buscarPrimerEgreso();
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar egreso anterior: " + e.getMessage());
        }
    }

    public void buscarEgresoSiguiente() {
        try {
            if (egresoActual != null && egresoActual.getId() > 0) {
                mEgresoCaja egreso = modelo.obtenerEgresoSiguiente(egresoActual.getId());

                if (egreso != null) {
                    cargarEgreso(egreso.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay egresos siguientes");
                }
            } else {
                buscarPrimerEgreso();
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar egreso siguiente: " + e.getMessage());
        }
    }

    public void buscarUltimoEgreso() {
        try {
            mEgresoCaja egreso = modelo.obtenerUltimoEgreso();

            if (egreso != null) {
                cargarEgreso(egreso.getId());
            } else {
                vista.mostrarMensajeInfo("No hay egresos registrados");
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar último egreso: " + e.getMessage());
        }
    }

    public void anularEgreso() {
        try {
            int id = vista.getId();
            if (id <= 0) {
                vista.mostrarMensajeInfo("No hay un egreso seleccionado para anular");
                return;
            }

            // Cargar egreso actualizado de la base de datos
            mEgresoCaja egreso = modelo.obtenerEgresoPorId(id);

            if (egreso == null) {
                vista.mostrarMensajeError("No se encontró el egreso con ID: " + id);
                return;
            }

            if (egreso.isAnulado()) {
                vista.mostrarMensajeInfo("Este egreso ya está anulado");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    vista,
                    "¿Está seguro de anular este egreso?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean resultado = modelo.anularEgreso(id);

                if (resultado) {
                    vista.mostrarMensajeExito("Egreso anulado correctamente");
                    // Actualizar el objeto actual
                    egreso.setAnulado(true);
                    this.egresoActual = egreso;
                    // Actualizar la vista
                    vista.actualizarEstadoAnulado(true);
                    // Limpiar campos y volver a 0 el txtId después de un breve retraso
                    new javax.swing.Timer(1500, e -> {
                        ((javax.swing.Timer) e.getSource()).stop();
                        vista.limpiarCampos();
                    }).start();
                } else {
                    vista.mostrarMensajeError("Error al anular el egreso");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al anular egreso: " + e.getMessage());
        }
    }

    // Métodos de la interfaz
    @Override
    public void imGrabar() {
        registrarEgreso();
    }

    @Override
    public void imFiltrar() {
        // No aplica
    }

    @Override
    public void imActualizar() {
        // Recargar el egreso actual si existe
        int id = vista.getId();
        if (id > 0) {
            cargarEgreso(id);
        }
    }

    @Override
    public void imBorrar() {
        anularEgreso();
    }

    @Override
    public void imNuevo() {
        vista.limpiarCampos();
        egresoActual = new mEgresoCaja();
    }

    @Override
    public void imBuscar() {
        try {
            String idStr = JOptionPane.showInputDialog(vista, "Ingrese el ID del egreso a buscar:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    cargarEgreso(id);
                } catch (NumberFormatException e) {
                    vista.mostrarMensajeError("El ID debe ser un número entero");
                }
            }
        } catch (Exception e) {
            vista.mostrarMensajeError("Error al buscar: " + e.getMessage());
        }
    }

    @Override
    public void imPrimero() {
        buscarPrimerEgreso();
    }

    @Override
    public void imSiguiente() {
        buscarEgresoSiguiente();
    }

    @Override
    public void imAnterior() {
        buscarEgresoAnterior();
    }

    @Override
    public void imUltimo() {
        buscarUltimoEgreso();
    }

    @Override
    public void imImprimir() {
        // No implementado
    }

    @Override
    public void imInsDet() {
        // No aplica
    }

    @Override
    public void imDelDet() {
        // No aplica
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
        return "gastos";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "concepto"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        cargarEgreso(id);
    }
}
