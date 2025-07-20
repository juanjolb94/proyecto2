package vista;

import interfaces.myInterface;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;
import modelo.CajaDAO;
import modelo.mCaja;

public class vAperturaCierreCaja extends javax.swing.JInternalFrame implements myInterface {

    private CajaDAO cajaDAO;
    private mCaja cajaActual;
    private boolean cajaAbierta = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // Variables para las diferentes denominaciones de moneda
    private final int[] BILLETES = {100000, 50000, 20000, 10000, 5000, 2000};
    private final int[] MONEDAS = {1000, 500, 100, 50};
    private JFormattedTextField[] txtBilletes;
    private JFormattedTextField[] txtMonedas;
    private String nombreUsuarioLogin;

    /**
     * Constructor de la ventana de apertura y cierre de caja
     *
     * @param nombreUsuario El nombre del usuario que inició sesión
     */
    public vAperturaCierreCaja(String nombreUsuario) {
        initComponents();
        try {
            cajaDAO = new CajaDAO();

            // Usar el nombre de usuario proporcionado en lugar del nombre del sistema
            this.nombreUsuarioLogin = nombreUsuario;

            configurarComponentes();
            verificarEstadoCaja();
            configurarBotones();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Configura los componentes visuales de la ventana
     */
    private void configurarComponentes() {
        // Configurar formateadores para campos numéricos
        configurarFormateadorMoneda((JFormattedTextField) txtMontoApertura);
        configurarFormateadorMoneda((JFormattedTextField) txtMontoCierre);
        configurarFormateadorMoneda((JFormattedTextField) txtMontoVentas);
        configurarFormateadorMoneda((JFormattedTextField) txtMontoGastos);
        configurarFormateadorMoneda((JFormattedTextField) txtMontoTotal);

        // Configurar campos de billetes y monedas
        txtBilletes = new JFormattedTextField[BILLETES.length];
        txtMonedas = new JFormattedTextField[MONEDAS.length];

        for (int i = 0; i < BILLETES.length; i++) {
            JFormattedTextField txt = new JFormattedTextField();
            configurarFormateadorEntero(txt);
            txt.setHorizontalAlignment(SwingConstants.RIGHT);
            txt.setValue(0);
            txt.setColumns(5);
            panelDetalles.add(new javax.swing.JLabel("₲" + formatearValor(BILLETES[i]) + ":"));
            panelDetalles.add(txt);
            txtBilletes[i] = txt;

            txt.addPropertyChangeListener("value", evt -> {
                actualizarTotalCalculo();

                // Si la caja está cerrada, actualizar también el monto de apertura
                if (!cajaAbierta) {
                    actualizarMontoApertura();
                }
            });
        }

        for (int i = 0; i < MONEDAS.length; i++) {
            JFormattedTextField txt = new JFormattedTextField();
            configurarFormateadorEntero(txt);
            txt.setHorizontalAlignment(SwingConstants.RIGHT);
            txt.setValue(0);
            txt.setColumns(5);
            panelMonedas.add(new javax.swing.JLabel("₲" + formatearValor(MONEDAS[i]) + ":"));
            panelMonedas.add(txt);
            txtMonedas[i] = txt;

            txt.addPropertyChangeListener("value", evt -> {
                actualizarTotalCalculo();

                // Si la caja está cerrada, actualizar también el monto de apertura
                if (!cajaAbierta) {
                    actualizarMontoApertura();
                }
            });
        }

        // Configurar bordes con título       
        panelDetalles.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Detalles y Cálculo",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Dialog", Font.BOLD, 12),
                Color.WHITE));
    }

    /**
     * Actualiza el monto de apertura a partir de los billetes y monedas
     * ingresados
     */
    private void actualizarMontoApertura() {
        long total = 0;

        // Sumar billetes
        for (int i = 0; i < BILLETES.length; i++) {
            int cantidad = ((Number) txtBilletes[i].getValue()).intValue();
            total += (long) BILLETES[i] * cantidad;
        }

        // Sumar monedas
        for (int i = 0; i < MONEDAS.length; i++) {
            int cantidad = ((Number) txtMonedas[i].getValue()).intValue();
            total += (long) MONEDAS[i] * cantidad;
        }

        // Actualizar monto de apertura directamente
        txtMontoApertura.setText(String.valueOf(total));

        // Forzar la actualización del valor formateado
        try {
            txtMontoApertura.commitEdit();
        } catch (java.text.ParseException e) {
            System.err.println("Error al actualizar monto de apertura: " + e.getMessage());
        }

        System.out.println("Monto actualizado: " + total);
        System.out.println("Valor en txtMontoApertura después de actualizar: " + txtMontoApertura.getValue());
    }

    /**
     * Configura los botones con iconos y funcionalidades
     */
    private void configurarBotones() {
        // Configurar acciones para los botones
        btnAbrirCaja.addActionListener(e -> abrirCaja());
        btnCerrarCaja.addActionListener(e -> cerrarCaja());

        // Botón Calcular: siempre calcula el total desde billetes y monedas
        btnCalcular.addActionListener(e -> calcularTotalDesdeTexto());

        btnSalir.addActionListener(e -> dispose());
    }

    /**
     * Calcula el total basado en el texto de los campos
     */
    private void calcularTotalDesdeTexto() {
        try {
            long total = 0;
            StringBuilder detalle = new StringBuilder("Cálculo detallado:\n\n");

            // Estructura de datos para organizar el cálculo
            JFormattedTextField[] camposBilletes = {txt100000, txt50000, txt20000, txt10000, txt5000, txt2000};
            long[] valoresBilletes = {100000L, 50000L, 20000L, 10000L, 5000L, 2000L};
            String[] nombresBilletes = {"100.000", "50.000", "20.000", "10.000", "5.000", "2.000"};

            JFormattedTextField[] camposMonedas = {txt1000, txt500, txt100, txt50};
            long[] valoresMonedas = {1000L, 500L, 100L, 50L};
            String[] nombresMonedas = {"1000", "500", "100", "50"};

            // Procesar billetes
            for (int i = 0; i < camposBilletes.length; i++) {
                try {
                    String texto = camposBilletes[i].getText().trim();
                    int cantidad = texto.isEmpty() ? 0 : Integer.parseInt(texto);
                    long subtotal = valoresBilletes[i] * cantidad;
                    total += subtotal;

                    detalle.append(String.format("Billetes de %s: %d x %s = %s\n",
                            nombresBilletes[i], cantidad, formatearValor(valoresBilletes[i]), formatearValor(subtotal)));

                    System.out.println("Billetes de " + nombresBilletes[i] + ": " + cantidad
                            + " - Subtotal: " + subtotal);
                } catch (Exception e) {
                    System.err.println("Error al procesar billetes de " + nombresBilletes[i] + ": " + e.getMessage());
                    detalle.append("Error al procesar billetes de " + nombresBilletes[i] + "\n");
                }
            }

            // Procesar monedas
            for (int i = 0; i < camposMonedas.length; i++) {
                try {
                    String texto = camposMonedas[i].getText().trim();
                    int cantidad = texto.isEmpty() ? 0 : Integer.parseInt(texto);
                    long subtotal = valoresMonedas[i] * cantidad;
                    total += subtotal;

                    detalle.append(String.format("Monedas de %s: %d x %s = %s\n",
                            nombresMonedas[i], cantidad, formatearValor(valoresMonedas[i]), formatearValor(subtotal)));

                    System.out.println("Monedas de " + nombresMonedas[i] + ": " + cantidad
                            + " - Subtotal: " + subtotal);
                } catch (Exception e) {
                    System.err.println("Error al procesar monedas de " + nombresMonedas[i] + ": " + e.getMessage());
                    detalle.append("Error al procesar monedas de " + nombresMonedas[i] + "\n");
                }
            }

            detalle.append("\nTOTAL: ₲" + formatearValor(total));
            System.out.println("Total calculado desde texto: " + total);

            // Actualizar campo correspondiente
            if (cajaAbierta) {
                txtMontoCierre.setValue(total);

                // Actualizar diferencia
                long esperado = ((Number) txtMontoTotal.getValue()).longValue();
                long diferencia = total - esperado;

                // Mostrar diferencia
                if (diferencia > 0) {
                    lblDiferencia.setText("Sobrante: ₲" + formatearValor(diferencia));
                    lblDiferencia.setForeground(new Color(0, 153, 0)); // Verde
                } else if (diferencia < 0) {
                    lblDiferencia.setText("Faltante: ₲" + formatearValor(Math.abs(diferencia)));
                    lblDiferencia.setForeground(new Color(204, 0, 0)); // Rojo
                } else {
                    lblDiferencia.setText("Sin diferencia");
                    lblDiferencia.setForeground(Color.BLACK);
                }
            } else {
                // Importante: Usar setValue para actualizar el campo
                txtMontoApertura.setValue(total);
                System.out.println("Monto de apertura actualizado: " + total);
            }

            // Mostrar el detalle del cálculo
            JOptionPane.showMessageDialog(this,
                    detalle.toString(),
                    "Total calculado: ₲" + formatearValor(total),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error en el cálculo: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Configura un formateador de moneda para un JFormattedTextField
     */
    private void configurarFormateadorMoneda(JFormattedTextField textField) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);

        NumberFormatter formatter = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text.isEmpty()) {
                    return 0;
                }
                return super.stringToValue(text);
            }
        };

        formatter.setValueClass(Long.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0L);

        textField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(formatter));
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setValue(0L);
    }

    /**
     * Configura un formateador de enteros para un JFormattedTextField
     */
    private void configurarFormateadorEntero(JFormattedTextField textField) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        format.setParseIntegerOnly(true);

        NumberFormatter formatter = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text.isEmpty()) {
                    return 0;
                }
                return super.stringToValue(text);
            }
        };

        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);

        textField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(formatter));
        textField.setValue(0);
    }

    /**
     * Verifica el estado actual de la caja
     */
    private void verificarEstadoCaja() throws SQLException {
        cajaActual = cajaDAO.obtenerCajaActual();

        if (cajaActual != null && cajaActual.isEstadoAbierto()) {
            // La caja está abierta
            cajaAbierta = true;
            lblEstadoCaja.setText("CAJA ABIERTA");
            lblEstadoCaja.setForeground(new Color(0, 153, 0)); // Verde

            txtFechaApertura.setText(dateFormat.format(cajaActual.getFechaApertura()));
            txtMontoApertura.setValue(cajaActual.getMontoApertura());
            txtUsuarioApertura.setText(cajaActual.getUsuarioApertura());

            // Habilitar campos de cierre
            panelDatosCierre.setEnabled(true);
            txtFechaCierre.setText(dateFormat.format(new Date()));
            txtMontoCierre.setEnabled(true);
            txtMontoVentas.setEnabled(true);
            txtMontoGastos.setEnabled(true);

            // Cargar montos actuales de ventas y gastos
            try {
                long totalVentas = cajaDAO.obtenerTotalVentasCajaActual();
                long totalGastos = cajaDAO.obtenerTotalGastosCajaActual();

                txtMontoVentas.setValue(totalVentas);
                txtMontoGastos.setValue(totalGastos);

                // Calcular total esperado
                long totalEsperado = cajaActual.getMontoApertura() + totalVentas - totalGastos;
                txtMontoTotal.setValue(totalEsperado);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al obtener datos de ventas y gastos: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            // Configurar botones
            btnAbrirCaja.setEnabled(false);
            btnCerrarCaja.setEnabled(true);
            btnCalcular.setEnabled(true);
            panelDetalles.setEnabled(true);
            habilitarPanelCalculo(true);
        } else {
            // La caja está cerrada
            cajaAbierta = false;
            lblEstadoCaja.setText("CAJA CERRADA");
            lblEstadoCaja.setForeground(new Color(204, 0, 0)); // Rojo

            // Limpiar y habilitar campos de apertura
            txtFechaApertura.setText(dateFormat.format(new Date()));
            txtMontoApertura.setValue(0L); // Iniciar en cero para que se calcule
            txtMontoApertura.setEnabled(false); // Deshabilitar para evitar edición directa - se calculará

            // Usar el usuario que hizo login
            txtUsuarioApertura.setText(nombreUsuarioLogin);

            // Deshabilitar campos de cierre
            panelDatosCierre.setEnabled(false);
            txtFechaCierre.setText("");
            txtMontoCierre.setEnabled(false);
            txtMontoVentas.setEnabled(false);
            txtMontoGastos.setEnabled(false);
            txtMontoTotal.setValue(0L);

            // Configurar botones
            btnAbrirCaja.setEnabled(true);
            btnCerrarCaja.setEnabled(false);
            btnCalcular.setEnabled(true); // Habilitar el botón calcular
            panelDetalles.setEnabled(true); // Habilitar panel de detalles
            habilitarPanelCalculo(true); // Habilitar campos de cálculo para apertura
            limpiarCamposBilletesYMonedas();

            // Mostrar mensaje instructivo
            JOptionPane.showMessageDialog(this,
                    "Ingrese la cantidad de billetes y monedas para la apertura, luego presione 'Calcular Total'.",
                    "Instrucciones",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Habilita o deshabilita el panel de cálculo de billetes y monedas
     */
    private void habilitarPanelCalculo(boolean habilitar) {
        for (JFormattedTextField txt : txtBilletes) {
            txt.setEnabled(habilitar);
        }

        for (JFormattedTextField txt : txtMonedas) {
            txt.setEnabled(habilitar);
        }
    }

    /**
     * Actualiza el total calculado a partir de billetes y monedas
     */
    private void actualizarTotalCalculo() {
        long total = 0;

        // Sumar billetes
        for (int i = 0; i < BILLETES.length; i++) {
            int cantidad = ((Number) txtBilletes[i].getValue()).intValue();
            total += (long) BILLETES[i] * cantidad;
        }

        // Sumar monedas
        for (int i = 0; i < MONEDAS.length; i++) {
            int cantidad = ((Number) txtMonedas[i].getValue()).intValue();
            total += (long) MONEDAS[i] * cantidad;
        }

        // Actualizar monto según el estado de la caja
        if (cajaAbierta) {
            // Si la caja está abierta, actualizar monto de cierre
            txtMontoCierre.setValue(total);

            // Actualizar diferencia
            long esperado = ((Number) txtMontoTotal.getValue()).longValue();
            long diferencia = total - esperado;

            // Mostrar diferencia
            if (diferencia > 0) {
                lblDiferencia.setText("Sobrante: ₲" + formatearValor(diferencia));
                lblDiferencia.setForeground(new Color(0, 153, 0)); // Verde
            } else if (diferencia < 0) {
                lblDiferencia.setText("Faltante: ₲" + formatearValor(Math.abs(diferencia)));
                lblDiferencia.setForeground(new Color(204, 0, 0)); // Rojo
            } else {
                lblDiferencia.setText("Sin diferencia");
                lblDiferencia.setForeground(Color.BLACK);
            }
        } else {
            // Si la caja está cerrada, actualizar monto de apertura
            txtMontoApertura.setValue(total);
        }
    }

    /**
     * Limpia todos los campos de billetes y monedas
     */
    private void limpiarCamposBilletesYMonedas() {
        // Limpiar campos de billetes
        for (JFormattedTextField txt : txtBilletes) {
            txt.setValue(0);
        }

        // Limpiar campos de monedas  
        for (JFormattedTextField txt : txtMonedas) {
            txt.setValue(0);
        }

        // Limpiar también la etiqueta de diferencia
        lblDiferencia.setText("");

        System.out.println("DEBUG: Campos de billetes y monedas limpiados para nueva apertura");
    }

    /**
     * Formatea un valor numérico para mostrar
     */
    private String formatearValor(long valor) {
        return DecimalFormat.getNumberInstance().format(valor);
    }

    /**
     * Abre la caja con los datos ingresados
     */
    private void abrirCaja() {
        if (cajaAbierta) {
            JOptionPane.showMessageDialog(this,
                    "La caja ya está abierta",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Validar monto de apertura
        long montoApertura = ((Number) txtMontoApertura.getValue()).longValue();
        if (montoApertura <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese cantidades de billetes y monedas y presione 'Calcular Total' antes de abrir la caja",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmar apertura
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de abrir la caja con un monto de ₲" + formatearValor(montoApertura) + "?",
                "Confirmar Apertura",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Crear objeto de caja
            mCaja nuevaCaja = new mCaja();
            nuevaCaja.setFechaApertura(new Date());
            nuevaCaja.setMontoApertura(montoApertura);
            nuevaCaja.setUsuarioApertura(txtUsuarioApertura.getText());
            nuevaCaja.setEstadoAbierto(true);

            // Guardar en la base de datos
            int idCaja = cajaDAO.abrirCaja(nuevaCaja);

            // Guardar también el arqueo detallado
            if (idCaja > 0) {
                // Preparar arrays de cantidades
                int[] billetes = new int[BILLETES.length];
                int[] monedas = new int[MONEDAS.length];

                // Obtener cantidades de billetes y monedas
                for (int i = 0; i < BILLETES.length; i++) {
                    billetes[i] = ((Number) txtBilletes[i].getValue()).intValue();
                }

                for (int i = 0; i < MONEDAS.length; i++) {
                    monedas[i] = ((Number) txtMonedas[i].getValue()).intValue();
                }

                // Guardar arqueo de apertura
                cajaDAO.guardarArqueoCompleto(idCaja, billetes, BILLETES, monedas, MONEDAS);
            }

            // Actualizar estado
            verificarEstadoCaja();

            JOptionPane.showMessageDialog(this,
                    "Caja abierta correctamente con un monto de ₲" + formatearValor(montoApertura),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la caja: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cierra la caja con los datos ingresados
     */
    private void cerrarCaja() {
        if (!cajaAbierta) {
            JOptionPane.showMessageDialog(this,
                    "La caja ya está cerrada",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Validar monto de cierre
        long montoCierre = ((Number) txtMontoCierre.getValue()).longValue();
        if (montoCierre <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El monto de cierre debe ser mayor a cero",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            txtMontoCierre.requestFocus();
            return;
        }

        try {
            // Verificar que la caja siga abierta en la base de datos
            mCaja cajaActualizada = cajaDAO.obtenerCajaActual();

            if (cajaActualizada == null || !cajaActualizada.isEstadoAbierto() || cajaActualizada.getId() != cajaActual.getId()) {
                JOptionPane.showMessageDialog(this,
                        "La caja actual ya no está disponible o fue modificada por otro usuario.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                verificarEstadoCaja(); // Actualizar la interfaz
                return;
            }

            // Obtener montos
            long montoApertura = ((Number) txtMontoApertura.getValue()).longValue();
            long montoVentas = ((Number) txtMontoVentas.getValue()).longValue();
            long montoGastos = ((Number) txtMontoGastos.getValue()).longValue();
            long montoEsperado = montoApertura + montoVentas - montoGastos;
            long diferencia = montoCierre - montoEsperado;

            String mensajeDiferencia = "";
            if (diferencia > 0) {
                mensajeDiferencia = "Hay un sobrante de ₲" + formatearValor(diferencia);
            } else if (diferencia < 0) {
                mensajeDiferencia = "Hay un faltante de ₲" + formatearValor(Math.abs(diferencia));
            } else {
                mensajeDiferencia = "No hay diferencia entre el monto esperado y el monto real";
            }

            // Confirmar cierre
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de cerrar la caja?\n\n"
                    + "Monto Apertura: ₲" + formatearValor(montoApertura) + "\n"
                    + "Ventas: ₲" + formatearValor(montoVentas) + "\n"
                    + "Gastos: ₲" + formatearValor(montoGastos) + "\n"
                    + "Monto Esperado: ₲" + formatearValor(montoEsperado) + "\n"
                    + "Monto Real: ₲" + formatearValor(montoCierre) + "\n\n"
                    + mensajeDiferencia,
                    "Confirmar Cierre",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            // Verificar nuevamente que la caja siga abierta
            cajaActualizada = cajaDAO.obtenerCajaActual();

            if (cajaActualizada == null || !cajaActualizada.isEstadoAbierto() || cajaActualizada.getId() != cajaActual.getId()) {
                JOptionPane.showMessageDialog(this,
                        "La caja actual ya no está disponible o fue modificada por otro usuario mientras confirmaba el cierre.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                verificarEstadoCaja(); // Actualizar la interfaz
                return;
            }

            // Actualizar datos de la caja
            cajaActual.setFechaCierre(new Date());
            cajaActual.setMontoCierre(montoCierre);
            cajaActual.setMontoVentas(montoVentas);
            cajaActual.setMontoGastos(montoGastos);
            cajaActual.setDiferencia(diferencia);
            cajaActual.setUsuarioCierre(System.getProperty("user.name")); // O el usuario actual del sistema
            cajaActual.setEstadoAbierto(false);

            // Guardar en la base de datos
            boolean resultado = cajaDAO.cerrarCaja(cajaActual);

            if (resultado) {
                JOptionPane.showMessageDialog(this,
                        "Caja cerrada correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar estado
                verificarEstadoCaja();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo cerrar la caja. Intente nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cerrar la caja: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Imprime el reporte de apertura/cierre de caja
     */
    private void imprimirReporte() {
        try {
            if (cajaAbierta) {
                cajaDAO.imprimirReporteApertura(cajaActual.getId());
            } else {
                // Imprimir último reporte de cierre
                mCaja ultimaCajaCerrada = cajaDAO.obtenerUltimaCajaCerrada();
                if (ultimaCajaCerrada != null) {
                    cajaDAO.imprimirReporteCierre(ultimaCajaCerrada.getId());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No hay registros de caja cerrada para imprimir",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir reporte: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        if (cajaAbierta) {
            cerrarCaja();
        } else {
            abrirCaja();
        }
    }

    @Override
    public void imFiltrar() {
        // No aplicable
    }

    @Override
    public void imActualizar() {
        try {
            verificarEstadoCaja();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar estado: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void imBorrar() {
        // No aplicable
    }

    @Override
    public void imNuevo() {
        // No aplicable
    }

    @Override
    public void imBuscar() {
        // No aplicable
    }

    @Override
    public void imPrimero() {
        // No aplicable
    }

    @Override
    public void imSiguiente() {
        // No aplicable
    }

    @Override
    public void imAnterior() {
        // No aplicable
    }

    @Override
    public void imUltimo() {
        // No aplicable
    }

    @Override
    public void imImprimir() {
        imprimirReporte();
    }

    @Override
    public void imInsDet() {
        // No aplicable
    }

    @Override
    public void imDelDet() {
        // No aplicable
    }

    @Override
    public void imCerrar() {
        this.dispose();
    }

    @Override
    public boolean imAbierto() {
        return this.isVisible();
    }

    @Override
    public void imAbrir() {
        this.setVisible(true);
    }

    @Override
    public String getTablaActual() {
        return "cajas";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "fecha_apertura"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // No aplicable
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblEstadoCaja = new javax.swing.JLabel();
        panelDatosApertura = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtFechaApertura = new javax.swing.JTextField();
        txtUsuarioApertura = new javax.swing.JTextField();
        btnAbrirCaja = new javax.swing.JButton();
        txtMontoApertura = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        panelDatosCierre = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtFechaCierre = new javax.swing.JTextField();
        btnCerrarCaja = new javax.swing.JButton();
        lblDiferencia = new javax.swing.JLabel();
        txtMontoCierre = new javax.swing.JFormattedTextField();
        txtMontoVentas = new javax.swing.JFormattedTextField();
        txtMontoGastos = new javax.swing.JFormattedTextField();
        txtMontoTotal = new javax.swing.JFormattedTextField();
        panelDetalles = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        panelCalculo = new javax.swing.JPanel();
        panelBilletes = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txt100000 = new javax.swing.JFormattedTextField();
        txt50000 = new javax.swing.JFormattedTextField();
        txt20000 = new javax.swing.JFormattedTextField();
        txt10000 = new javax.swing.JFormattedTextField();
        txt5000 = new javax.swing.JFormattedTextField();
        txt2000 = new javax.swing.JFormattedTextField();
        panelMonedas = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txt500 = new javax.swing.JFormattedTextField();
        txt100 = new javax.swing.JFormattedTextField();
        txt50 = new javax.swing.JFormattedTextField();
        txt1000 = new javax.swing.JFormattedTextField();
        btnCalcular = new javax.swing.JButton();
        panelBotones = new javax.swing.JPanel();
        btnSalir = new javax.swing.JButton();

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblEstadoCaja.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel2.setText("Monto Apertura:");

        jLabel3.setText("Usuario:");

        btnAbrirCaja.setText("Abrir Caja");

        jLabel1.setText("Fecha Apertura:");

        javax.swing.GroupLayout panelDatosAperturaLayout = new javax.swing.GroupLayout(panelDatosApertura);
        panelDatosApertura.setLayout(panelDatosAperturaLayout);
        panelDatosAperturaLayout.setHorizontalGroup(
            panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosAperturaLayout.createSequentialGroup()
                .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2))
                    .addComponent(jLabel3))
                .addGap(12, 12, 12)
                .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAbrirCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtMontoApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtUsuarioApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtFechaApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelDatosAperturaLayout.setVerticalGroup(
            panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosAperturaLayout.createSequentialGroup()
                .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtFechaApertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtMontoApertura, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosAperturaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsuarioApertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAbrirCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Fecha Cierre:");

        jLabel5.setText("Monto Cierre:");

        jLabel6.setText("Ventas del día:");

        jLabel7.setText("Gastos del día:");

        jLabel8.setText("Total Esperado:");

        btnCerrarCaja.setText("Cerrar Caja");

        lblDiferencia.setForeground(new java.awt.Color(60, 63, 65));

        javax.swing.GroupLayout panelDatosCierreLayout = new javax.swing.GroupLayout(panelDatosCierre);
        panelDatosCierre.setLayout(panelDatosCierreLayout);
        panelDatosCierreLayout.setHorizontalGroup(
            panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosCierreLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8)
                    .addComponent(jLabel4))
                .addGap(10, 10, 10)
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtFechaCierre, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelDatosCierreLayout.createSequentialGroup()
                        .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnCerrarCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtMontoGastos, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtMontoVentas, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtMontoCierre, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtMontoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelDatosCierreLayout.setVerticalGroup(
            panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosCierreLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaCierre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMontoCierre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMontoVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMontoGastos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelDatosCierreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtMontoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8))
                    .addComponent(lblDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCerrarCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jLabel9.setText("Cálculo de Billetes y Monedas:");

        panelBilletes.setToolTipText("");

        jLabel10.setText("100.000:");

        jLabel11.setText("50.000:");

        jLabel12.setText("20.000:");

        jLabel13.setText("10.000:");

        jLabel14.setText("5.000:");

        jLabel15.setText("2.000:");

        javax.swing.GroupLayout panelBilletesLayout = new javax.swing.GroupLayout(panelBilletes);
        panelBilletes.setLayout(panelBilletesLayout);
        panelBilletesLayout.setHorizontalGroup(
            panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBilletesLayout.createSequentialGroup()
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBilletesLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txt5000, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBilletesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel10)
                        .addGap(12, 12, 12)
                        .addComponent(txt100000, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBilletesLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txt2000, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBilletesLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txt10000, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBilletesLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txt20000, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBilletesLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txt50000, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 6, Short.MAX_VALUE))
        );
        panelBilletesLayout.setVerticalGroup(
            panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBilletesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txt100000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txt50000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txt20000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txt10000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txt5000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBilletesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txt2000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jLabel16.setText("500:");

        jLabel17.setText("100:");

        jLabel18.setText("50:");

        jLabel19.setText("1000:");

        javax.swing.GroupLayout panelMonedasLayout = new javax.swing.GroupLayout(panelMonedas);
        panelMonedas.setLayout(panelMonedasLayout);
        panelMonedasLayout.setHorizontalGroup(
            panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMonedasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelMonedasLayout.createSequentialGroup()
                        .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel17))
                        .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMonedasLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(txt50, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelMonedasLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(txt100, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelMonedasLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(txt500, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMonedasLayout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt1000)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        panelMonedasLayout.setVerticalGroup(
            panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMonedasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txt1000, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(txt500, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txt100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMonedasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txt50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btnCalcular.setText("Calcular");

        javax.swing.GroupLayout panelCalculoLayout = new javax.swing.GroupLayout(panelCalculo);
        panelCalculo.setLayout(panelCalculoLayout);
        panelCalculoLayout.setHorizontalGroup(
            panelCalculoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCalculoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelBilletes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCalculoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMonedas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCalculoLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        panelCalculoLayout.setVerticalGroup(
            panelCalculoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCalculoLayout.createSequentialGroup()
                .addComponent(panelBilletes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelCalculoLayout.createSequentialGroup()
                .addComponent(panelMonedas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout panelDetallesLayout = new javax.swing.GroupLayout(panelDetalles);
        panelDetalles.setLayout(panelDetallesLayout);
        panelDetallesLayout.setHorizontalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDetallesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(panelCalculo, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );
        panelDetallesLayout.setVerticalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetallesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCalculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 35, Short.MAX_VALUE)
        );

        btnSalir.setText("Salir");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEstadoCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelDatosCierre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDatosApertura, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(84, 84, 84))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(panelDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelDatosApertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelDatosCierre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 12, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrirCaja;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnCerrarCaja;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblDiferencia;
    private javax.swing.JLabel lblEstadoCaja;
    private javax.swing.JPanel panelBilletes;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCalculo;
    private javax.swing.JPanel panelDatosApertura;
    private javax.swing.JPanel panelDatosCierre;
    private javax.swing.JPanel panelDetalles;
    private javax.swing.JPanel panelMonedas;
    private javax.swing.JFormattedTextField txt100;
    private javax.swing.JFormattedTextField txt1000;
    private javax.swing.JFormattedTextField txt10000;
    private javax.swing.JFormattedTextField txt100000;
    private javax.swing.JFormattedTextField txt2000;
    private javax.swing.JFormattedTextField txt20000;
    private javax.swing.JFormattedTextField txt50;
    private javax.swing.JFormattedTextField txt500;
    private javax.swing.JFormattedTextField txt5000;
    private javax.swing.JFormattedTextField txt50000;
    private javax.swing.JTextField txtFechaApertura;
    private javax.swing.JTextField txtFechaCierre;
    private javax.swing.JFormattedTextField txtMontoApertura;
    private javax.swing.JFormattedTextField txtMontoCierre;
    private javax.swing.JFormattedTextField txtMontoGastos;
    private javax.swing.JFormattedTextField txtMontoTotal;
    private javax.swing.JFormattedTextField txtMontoVentas;
    private javax.swing.JTextField txtUsuarioApertura;
    // End of variables declaration//GEN-END:variables
}
