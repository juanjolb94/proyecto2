package controlador;

import interfaces.myInterface;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.JOptionPane;
import modelo.DatabaseConnection;
import modelo.IngresoCajaDAO;
import modelo.EgresoCajaDAO;
import modelo.mIngresoCaja;
import modelo.mEgresoCaja;
import vista.vMovimientoCaja;
import vista.vLogin;

public class cMovimientoCaja implements myInterface {

    private vMovimientoCaja vista;
    private IngresoCajaDAO modeloIngreso;
    private EgresoCajaDAO modeloEgreso;
    private mIngresoCaja ingresoActual;
    private mEgresoCaja egresoActual;
    private String tipoActual;

    public cMovimientoCaja(vMovimientoCaja vista) throws SQLException {
        this.vista = vista;
        this.modeloIngreso = new IngresoCajaDAO();
        this.modeloEgreso = new EgresoCajaDAO();
        this.ingresoActual = new mIngresoCaja();
        this.egresoActual = new mEgresoCaja();
        this.tipoActual = vista.getTipoMovimiento();

        // Verificar si hay una caja abierta
        verificarCajaAbierta();
    }

    private void verificarCajaAbierta() throws SQLException {
        boolean cajaAbierta = modeloIngreso.existeCajaAbierta(); // Ambos DAOs usan la misma validación
        if (!cajaAbierta) {
            vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar movimientos.");
            vista.deshabilitarComponentes();
        }
    }
    
    public void cambiarTipo(String nuevoTipo) {
        this.tipoActual = nuevoTipo;
        // Crear nuevos objetos según el tipo
        if ("INGRESO".equals(tipoActual)) {
            this.ingresoActual = new mIngresoCaja();
        } else {
            this.egresoActual = new mEgresoCaja();
        }
    }

    public boolean registrarMovimiento() {
        try {
            // Verificar si hay una caja abierta
            boolean cajaAbierta = "INGRESO".equals(tipoActual) ? 
                modeloIngreso.existeCajaAbierta() : modeloEgreso.existeCajaAbierta();
                
            if (!cajaAbierta) {
                vista.mostrarMensajeError("No hay una caja abierta. Debe abrir una caja para registrar movimientos.");
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
                montoTexto = montoTexto.replaceAll("[^0-9,.]", "").replace(",", "");
                monto = Double.parseDouble(montoTexto);
            } catch (NumberFormatException e) {
                vista.mostrarMensajeError("Monto inválido");
                vista.enfocarMonto();
                return false;
            }

            if (monto <= 0) {
                vista.mostrarMensajeError("El monto debe ser mayor a 0");
                vista.enfocarMonto();
                return false;
            }

            // Validar concepto
            if (concepto == null || concepto.trim().isEmpty()) {
                vista.mostrarMensajeError("Debe ingresar un concepto");
                vista.enfocarConcepto();
                return false;
            }

            if (id == 0) {
                // Insertar nuevo movimiento
                return insertarNuevoMovimiento(monto, concepto.trim());
            } else {
                // Actualizar movimiento existente
                return actualizarMovimiento(id, monto, concepto.trim(), !estadoActivo);
            }

        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al registrar movimiento: " + e.getMessage());
            return false;
        }
    }

    private boolean insertarNuevoMovimiento(double monto, String concepto) throws SQLException {
        String usuario = vLogin.getUsuarioAutenticado();
        Date fechaActual = new Date();

        if ("INGRESO".equals(tipoActual)) {
            // Crear nuevo ingreso
            mIngresoCaja nuevoIngreso = new mIngresoCaja();
            nuevoIngreso.setFecha(fechaActual);
            nuevoIngreso.setMonto(monto);
            nuevoIngreso.setConcepto(concepto);
            nuevoIngreso.setUsuario(usuario);
            nuevoIngreso.setAnulado(false);

            int idGenerado = modeloIngreso.insertarIngreso(nuevoIngreso);
            if (idGenerado > 0) {
                vista.mostrarMensajeExito("Ingreso registrado correctamente con ID: " + idGenerado);
                nuevoIngreso.setId(idGenerado);
                this.ingresoActual = nuevoIngreso;
                
                // Actualizar vista con el ID generado
                vista.mostrarMovimiento(idGenerado, fechaActual, monto, concepto, usuario, false, "INGRESO");
                return true;
            }
        } else {
            // Crear nuevo egreso
            mEgresoCaja nuevoEgreso = new mEgresoCaja();
            nuevoEgreso.setFecha(fechaActual);
            nuevoEgreso.setMonto(monto);
            nuevoEgreso.setConcepto(concepto);
            nuevoEgreso.setUsuario(usuario);
            nuevoEgreso.setAnulado(false);

            int idGenerado = modeloEgreso.insertarEgreso(nuevoEgreso);
            if (idGenerado > 0) {
                vista.mostrarMensajeExito("Egreso registrado correctamente con ID: " + idGenerado);
                nuevoEgreso.setId(idGenerado);
                this.egresoActual = nuevoEgreso;
                
                // Actualizar vista con el ID generado
                vista.mostrarMovimiento(idGenerado, fechaActual, monto, concepto, usuario, false, "EGRESO");
                return true;
            }
        }

        vista.mostrarMensajeError("Error al registrar el movimiento");
        return false;
    }

    private boolean actualizarMovimiento(int id, double monto, String concepto, boolean anulado) throws SQLException {
        boolean resultado = false;
        
        if ("INGRESO".equals(tipoActual)) {
            if (ingresoActual != null && ingresoActual.getId() == id) {
                ingresoActual.setMonto(monto);
                ingresoActual.setConcepto(concepto);
                ingresoActual.setAnulado(anulado);
                resultado = modeloIngreso.actualizarIngreso(ingresoActual);
            }
        } else {
            if (egresoActual != null && egresoActual.getId() == id) {
                egresoActual.setMonto(monto);
                egresoActual.setConcepto(concepto);
                egresoActual.setAnulado(anulado);
                resultado = modeloEgreso.actualizarEgreso(egresoActual);
            }
        }

        if (resultado) {
            vista.mostrarMensajeExito("Movimiento actualizado correctamente");
        } else {
            vista.mostrarMensajeError("Error al actualizar el movimiento");
        }

        return resultado;
    }

    public void cargarMovimiento(int id) {
        try {
            String tipoMovimiento = vista.getTipoMovimiento();
            
            if ("INGRESO".equals(tipoMovimiento)) {
                mIngresoCaja ingreso = modeloIngreso.obtenerIngresoPorId(id);
                if (ingreso != null) {
                    this.ingresoActual = ingreso;
                    vista.mostrarMovimiento(
                        ingreso.getId(),
                        ingreso.getFecha(),
                        ingreso.getMonto(),
                        ingreso.getConcepto(),
                        ingreso.getUsuario(),
                        ingreso.isAnulado(),
                        "INGRESO"
                    );
                } else {
                    vista.mostrarMensajeInfo("No se encontró un ingreso con ID: " + id);
                    vista.limpiarCampos();
                }
            } else {
                mEgresoCaja egreso = modeloEgreso.obtenerEgresoPorId(id);
                if (egreso != null) {
                    this.egresoActual = egreso;
                    vista.mostrarMovimiento(
                        egreso.getId(),
                        egreso.getFecha(),
                        egreso.getMonto(),
                        egreso.getConcepto(),
                        egreso.getUsuario(),
                        egreso.isAnulado(),
                        "EGRESO"
                    );
                } else {
                    vista.mostrarMensajeInfo("No se encontró un egreso con ID: " + id);
                    vista.limpiarCampos();
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al cargar movimiento: " + e.getMessage());
        }
    }

    public void anularMovimiento() {
        try {
            int id = vista.getId();
            if (id <= 0) {
                vista.mostrarMensajeError("No hay un movimiento seleccionado para anular");
                return;
            }

            String tipoMovimiento = vista.getTipoMovimiento();
            Object movimiento = null;
            
            if ("INGRESO".equals(tipoMovimiento)) {
                movimiento = ingresoActual;
            } else {
                movimiento = egresoActual;
            }

            if (movimiento == null) {
                vista.mostrarMensajeError("Debe cargar un movimiento antes de anularlo");
                return;
            }

            // Verificar si ya está anulado
            boolean yaAnulado = false;
            if ("INGRESO".equals(tipoMovimiento)) {
                yaAnulado = ((mIngresoCaja) movimiento).isAnulado();
            } else {
                yaAnulado = ((mEgresoCaja) movimiento).isAnulado();
            }

            if (yaAnulado) {
                vista.mostrarMensajeInfo("Este movimiento ya está anulado");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                vista,
                "¿Está seguro que desea anular este " + tipoMovimiento.toLowerCase() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean resultado = false;
                
                if ("INGRESO".equals(tipoMovimiento)) {
                    resultado = modeloIngreso.anularIngreso(id);
                    if (resultado) {
                        ((mIngresoCaja) movimiento).setAnulado(true);
                        this.ingresoActual = (mIngresoCaja) movimiento;
                    }
                } else {
                    resultado = modeloEgreso.anularEgreso(id);
                    if (resultado) {
                        ((mEgresoCaja) movimiento).setAnulado(true);
                        this.egresoActual = (mEgresoCaja) movimiento;
                    }
                }

                if (resultado) {
                    vista.mostrarMensajeExito(tipoMovimiento + " anulado correctamente");
                    vista.actualizarEstadoAnulado(true);
                    
                    // Limpiar campos después de un breve retraso
                    new javax.swing.Timer(1500, e -> {
                        ((javax.swing.Timer) e.getSource()).stop();
                        vista.limpiarCampos();
                    }).start();
                } else {
                    vista.mostrarMensajeError("Error al anular el " + tipoMovimiento.toLowerCase());
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al anular movimiento: " + e.getMessage());
        }
    }

    // Métodos de navegación
    private void buscarPrimerMovimiento() {
        try {
            String tipoMovimiento = vista.getTipoMovimiento();
            
            if ("INGRESO".equals(tipoMovimiento)) {
                mIngresoCaja primer = modeloIngreso.obtenerPrimerIngreso();
                if (primer != null) {
                    cargarMovimiento(primer.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay ingresos registrados");
                }
            } else {
                mEgresoCaja primer = modeloEgreso.obtenerPrimerEgreso();
                if (primer != null) {
                    cargarMovimiento(primer.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay egresos registrados");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar primer movimiento: " + e.getMessage());
        }
    }

    private void buscarUltimoMovimiento() {
        try {
            String tipoMovimiento = vista.getTipoMovimiento();
            
            if ("INGRESO".equals(tipoMovimiento)) {
                mIngresoCaja ultimo = modeloIngreso.obtenerUltimoIngreso();
                if (ultimo != null) {
                    cargarMovimiento(ultimo.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay ingresos registrados");
                }
            } else {
                mEgresoCaja ultimo = modeloEgreso.obtenerUltimoEgreso();
                if (ultimo != null) {
                    cargarMovimiento(ultimo.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay egresos registrados");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar último movimiento: " + e.getMessage());
        }
    }

    private void buscarMovimientoSiguiente() {
        try {
            int idActual = vista.getId();
            if (idActual <= 0) {
                vista.mostrarMensajeInfo("No hay un movimiento actual para buscar el siguiente");
                return;
            }

            String tipoMovimiento = vista.getTipoMovimiento();
            
            if ("INGRESO".equals(tipoMovimiento)) {
                mIngresoCaja siguiente = modeloIngreso.obtenerIngresoSiguiente(idActual);
                if (siguiente != null) {
                    cargarMovimiento(siguiente.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay más ingresos");
                }
            } else {
                mEgresoCaja siguiente = modeloEgreso.obtenerEgresoSiguiente(idActual);
                if (siguiente != null) {
                    cargarMovimiento(siguiente.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay más egresos");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar siguiente movimiento: " + e.getMessage());
        }
    }

    private void buscarMovimientoAnterior() {
        try {
            int idActual = vista.getId();
            if (idActual <= 0) {
                vista.mostrarMensajeInfo("No hay un movimiento actual para buscar el anterior");
                return;
            }

            String tipoMovimiento = vista.getTipoMovimiento();
            
            if ("INGRESO".equals(tipoMovimiento)) {
                mIngresoCaja anterior = modeloIngreso.obtenerIngresoAnterior(idActual);
                if (anterior != null) {
                    cargarMovimiento(anterior.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay ingresos anteriores");
                }
            } else {
                mEgresoCaja anterior = modeloEgreso.obtenerEgresoAnterior(idActual);
                if (anterior != null) {
                    cargarMovimiento(anterior.getId());
                } else {
                    vista.mostrarMensajeInfo("No hay egresos anteriores");
                }
            }
        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al buscar movimiento anterior: " + e.getMessage());
        }
    }

    // Implementación de métodos de la interfaz
    @Override
    public void imGrabar() {
        registrarMovimiento();
    }

    @Override
    public void imFiltrar() {
        // No aplica
    }

    @Override
    public void imActualizar() {
        // Recargar el movimiento actual si existe
        int id = vista.getId();
        if (id > 0) {
            cargarMovimiento(id);
        }
    }

    @Override
    public void imBorrar() {
        anularMovimiento();
    }

    @Override
    public void imNuevo() {
        vista.limpiarCampos();
        if ("INGRESO".equals(vista.getTipoMovimiento())) {
            ingresoActual = new mIngresoCaja();
        } else {
            egresoActual = new mEgresoCaja();
        }
    }

    @Override
    public void imBuscar() {
        try {
            String idStr = JOptionPane.showInputDialog(vista, 
                "Ingrese el ID del " + vista.getTipoMovimiento().toLowerCase() + " a buscar:");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr);
                    cargarMovimiento(id);
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
        buscarPrimerMovimiento();
    }

    @Override
    public void imSiguiente() {
        buscarMovimientoSiguiente();
    }

    @Override
    public void imAnterior() {
        buscarMovimientoAnterior();
    }

    @Override
    public void imUltimo() {
        buscarUltimoMovimiento();
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
        return "INGRESO".equals(vista.getTipoMovimiento()) ? "ingresos_caja" : "gastos";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "concepto"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        cargarMovimiento(id);
    }
}