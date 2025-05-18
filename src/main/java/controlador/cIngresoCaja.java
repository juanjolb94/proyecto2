package controlador;

import interfaces.myInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.JOptionPane;
import modelo.DatabaseConnection;
import modelo.IngresoCajaDAO;
import modelo.mIngresoCaja;
import vista.vIngresoCaja;
import vista.vLogin;

public class cIngresoCaja implements myInterface {

    private vIngresoCaja vista;
    private IngresoCajaDAO modelo;
    private mIngresoCaja ingresoActual;

    public cIngresoCaja(vIngresoCaja vista) throws SQLException {
        this.vista = vista;
        this.modelo = new IngresoCajaDAO();
        this.ingresoActual = new mIngresoCaja();

        // Verificar si hay una caja abierta
        verificarCajaAbierta();
    }

    private void verificarCajaAbierta() throws SQLException {
        if (!modelo.existeCajaAbierta()) {
            vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar ingresos.");
            vista.deshabilitarComponentes();
        }
    }

    public boolean registrarIngreso() {
        try {
            // Verificar si hay una caja abierta
            if (!modelo.existeCajaAbierta()) {
                vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar ingresos.");
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
                // Verificar que el ingreso existe
                mIngresoCaja ingresoExistente = modelo.obtenerIngresoPorId(id);
                if (ingresoExistente == null) {
                    vista.mostrarMensajeError("El ingreso con ID " + id + " no existe");
                    return false;
                }

                // Verificar si está anulado
                if (ingresoExistente.isAnulado()) {
                    vista.mostrarMensajeError("No se puede modificar un ingreso anulado");
                    return false;
                }

                // Actualizar el ingreso
                ingresoExistente.setMonto(monto);
                ingresoExistente.setConcepto(concepto);
                ingresoExistente.setAnulado(!estadoActivo);

                boolean resultado = modelo.actualizarIngreso(ingresoExistente);

                if (resultado) {
                    vista.mostrarMensajeExito("Ingreso actualizado correctamente");
                    vista.limpiarCampos(); // Limpiar y volver a 0 el txtId
                    return true;
                } else {
                    vista.mostrarMensajeError("No se pudo actualizar el ingreso");
                    return false;
                }
            } else {
                // Es un nuevo ingreso (id = 0)
                // Crear objeto de ingreso
                ingresoActual = new mIngresoCaja();
                ingresoActual.setFecha(new Date());
                ingresoActual.setMonto(monto);
                ingresoActual.setConcepto(concepto);
                ingresoActual.setUsuario(vLogin.getUsuarioAutenticado());
                ingresoActual.setAnulado(!estadoActivo);

                // Insertar en la base de datos
                int nuevoId = modelo.insertarIngreso(ingresoActual);

                if (nuevoId > 0) {
                    ingresoActual.setId(nuevoId);
                    vista.mostrarMensajeExito("Ingreso registrado correctamente con ID: " + nuevoId);
                    vista.limpiarCampos(); // Limpiar y volver a 0 el txtId
                    return true;
                } else {
                    vista.mostrarMensajeError("No se pudo registrar el ingreso");
                    return false;
                }
            }

        } catch (SQLException e) {
            vista.mostrarMensajeError("Error de base de datos: " + e.getMessage());
            return false;
        } catch (Exception e) {
            vista.mostrarMensajeError("Error al registrar ingreso: " + e.getMessage());
            return false;
        }
    }

    public void cargarIngreso(int id) {
        try {
            mIngresoCaja ingreso = modelo.obtenerIngresoPorId(id);

            if (ingreso != null) {
                this.ingresoActual = ingreso;
                vista.mostrarIngreso(
                        ingreso.getId(),
                        ingreso.getFecha(),
                        ingreso.getMonto(),
                        ingreso.getConcepto(),
                        ingreso.getUsuario(),
                        ingreso.isAnulado()
                );
            } else {
                vista.mostrarMensajeError("No se encontró el ingreso con ID: " + id);
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al cargar ingreso: " + e.getMessage());
        }
    }

    public void buscarPrimerIngreso() {
        try {
            mIngresoCaja ingreso = modelo.obtenerPrimerIngreso();

            if (ingreso != null) {
                cargarIngreso(ingreso.getId());
            } else {
                vista.mostrarMensajeInfo("No hay ingresos registrados");
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar primer ingreso: " + e.getMessage());
        }
    }

    public void buscarIngresoAnterior() {
        try {
            if (ingresoActual != null && ingresoActual.getId() > 0) {
                mIngresoCaja ingreso = modelo.obtenerIngresoAnterior(ingresoActual.getId());

                if (ingreso != null) {
                    cargarIngreso(ingreso.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay ingresos anteriores");
                }
            } else {
                buscarPrimerIngreso();
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar ingreso anterior: " + e.getMessage());
        }
    }

    public void buscarIngresoSiguiente() {
        try {
            if (ingresoActual != null && ingresoActual.getId() > 0) {
                mIngresoCaja ingreso = modelo.obtenerIngresoSiguiente(ingresoActual.getId());

                if (ingreso != null) {
                    cargarIngreso(ingreso.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay ingresos siguientes");
                }
            } else {
                buscarPrimerIngreso();
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar ingreso siguiente: " + e.getMessage());
        }
    }

    public void buscarUltimoIngreso() {
        try {
            mIngresoCaja ingreso = modelo.obtenerUltimoIngreso();

            if (ingreso != null) {
                cargarIngreso(ingreso.getId());
            } else {
                vista.mostrarMensajeInfo("No hay ingresos registrados");
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar último ingreso: " + e.getMessage());
        }
    }

    public void anularIngreso() {
        try {
            int id = vista.getId();
            if (id <= 0) {
                vista.mostrarMensajeInfo("No hay un ingreso seleccionado para anular");
                return;
            }

            // Cargar ingreso actualizado de la base de datos
            mIngresoCaja ingreso = modelo.obtenerIngresoPorId(id);

            if (ingreso == null) {
                vista.mostrarMensajeError("No se encontró el ingreso con ID: " + id);
                return;
            }

            if (ingreso.isAnulado()) {
                vista.mostrarMensajeInfo("Este ingreso ya está anulado");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    vista,
                    "¿Está seguro de anular este ingreso?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean resultado = modelo.anularIngreso(id);

                if (resultado) {
                    vista.mostrarMensajeExito("Ingreso anulado correctamente");
                    // Actualizar el objeto actual
                    ingreso.setAnulado(true);
                    this.ingresoActual = ingreso;
                    // Actualizar la vista
                    vista.actualizarEstadoAnulado(true);
                    // Limpiar campos y volver a 0 el txtId después de un breve retraso
                    new javax.swing.Timer(1500, e -> {
                        ((javax.swing.Timer) e.getSource()).stop();
                        vista.limpiarCampos();
                    }).start();
                } else {
                    vista.mostrarMensajeError("Error al anular el ingreso");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al anular ingreso: " + e.getMessage());
        }
    }

    // Métodos de la interfaz
    @Override
    public void imGrabar() {
        registrarIngreso();
    }

    @Override
    public void imFiltrar() {
        // No aplica
    }

    @Override
    public void imActualizar() {
        // Recargar el ingreso actual si existe
        int id = vista.getId();
        if (id > 0) {
            cargarIngreso(id);
        }
    }

    @Override
    public void imBorrar() {
        anularIngreso();
    }

    @Override
    public void imNuevo() {
        vista.limpiarCampos();
        ingresoActual = new mIngresoCaja();
    }

    @Override
    public void imBuscar() {
        try {
            String idStr = JOptionPane.showInputDialog(vista, "Ingrese el ID del ingreso a buscar:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    cargarIngreso(id);
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
        buscarPrimerIngreso();
    }

    @Override
    public void imSiguiente() {
        buscarIngresoSiguiente();
    }

    @Override
    public void imAnterior() {
        buscarIngresoAnterior();
    }

    @Override
    public void imUltimo() {
        buscarUltimoIngreso();
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
        return "ingresos_caja";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "concepto"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        cargarIngreso(id);
    }
}
