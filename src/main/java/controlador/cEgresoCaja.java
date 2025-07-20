package controlador;

import interfaces.myInterface;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
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
                // CORRECCIÓN: Usar procesamiento correcto de montos
                monto = procesarMonto(montoTexto);
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
                    vista.limpiarCampos();
                    this.egresoActual = egresoExistente;
                    return true;
                } else {
                    vista.mostrarMensajeError("Error al actualizar el egreso");
                    return false;
                }
            } else {
                // Insertar nuevo egreso
                String usuario = vLogin.getUsuarioAutenticado();
                Date fechaActual = new Date();

                mEgresoCaja nuevoEgreso = new mEgresoCaja();
                nuevoEgreso.setFecha(fechaActual);
                nuevoEgreso.setMonto(monto);
                nuevoEgreso.setConcepto(concepto);
                nuevoEgreso.setUsuario(usuario);
                nuevoEgreso.setAnulado(false);

                int idGenerado = modelo.insertarEgreso(nuevoEgreso);

                if (idGenerado > 0) {
                    vista.mostrarMensajeExito("Egreso registrado correctamente con ID: " + idGenerado);
                    nuevoEgreso.setId(idGenerado);
                    this.egresoActual = nuevoEgreso;

                    // Actualizar vista con el egreso registrado
                    vista.mostrarEgreso(idGenerado, fechaActual, monto, concepto, usuario, false);
                    return true;
                } else {
                    vista.mostrarMensajeError("Error al registrar el egreso");
                    return false;
                }
            }

        } catch (SQLException e) {
            vista.mostrarMensajeError("Error al registrar egreso: " + e.getMessage());
            return false;
        }
    }

    /**
     * Procesa correctamente el monto ingresado considerando el formato
     * paraguayo donde el punto (.) se usa como separador de miles y la coma (,)
     * como decimal
     *
     * @param montoTexto El texto del monto a procesar
     * @return El valor numérico del monto
     * @throws NumberFormatException si el formato es inválido
     */
    private double procesarMonto(String montoTexto) throws NumberFormatException {
        if (montoTexto == null || montoTexto.trim().isEmpty()) {
            return 0.0;
        }

        // Procesamiento del texto del monto
        String montoLimpio = montoTexto.trim();
        System.out.println("Texto original: '" + montoTexto + "'");

        // Remover espacios y caracteres especiales excepto números, puntos y comas
        montoLimpio = montoLimpio.replaceAll("[^0-9,.]", "");
        System.out.println("Después de limpiar: '" + montoLimpio + "'");

        // Si está vacío después de limpiar, devolver 0
        if (montoLimpio.isEmpty()) {
            return 0.0;
        }

        // Determinar si usa formato paraguayo (punto como separador de miles)
        if (montoLimpio.matches("\\d{1,3}(\\.\\d{3})+$")) {
            // Formato con separadores de miles: 20.000, 1.500.000, etc.
            montoLimpio = montoLimpio.replace(".", "");
            System.out.println("Formato paraguayo detectado, resultado: " + montoLimpio);
        } else if (montoLimpio.matches("\\d{1,3}(\\.\\d{3})+,\\d{1,2}$")) {
            // Formato con separadores de miles y decimales: 20.000,50
            String[] partes = montoLimpio.split(",");
            String parteEntera = partes[0].replace(".", "");
            String parteDecimal = partes[1];
            montoLimpio = parteEntera + "." + parteDecimal;
            System.out.println("Formato paraguayo con decimales, resultado: " + montoLimpio);
        } else if (montoLimpio.contains(",")) {
            // Solo coma decimal sin separadores de miles: 20000,50
            montoLimpio = montoLimpio.replace(",", ".");
            System.out.println("Solo coma decimal, resultado: " + montoLimpio);
        }

        double resultado = Double.parseDouble(montoLimpio);
        System.out.println("Valor final procesado: " + resultado);
        return resultado;
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
