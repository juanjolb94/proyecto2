package vista;

import controlador.cTalonarios;
import interfaces.myInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import modelo.mTalonario;

public class vTalonarios extends javax.swing.JInternalFrame implements myInterface {

    private cTalonarios controlador;
    private boolean esNuevo = true;
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    public vTalonarios() {
        initComponents();
        try {
            controlador = new cTalonarios(this);
            configurarEventos();
            limpiarCampos();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al inicializar la ventana: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        // Evento de tecla para ID (buscar al presionar Enter)
        txtId.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(txtId.getText());
                    if (id == 0) {
                        // Si ID es 0, limpiar formulario para nuevo registro
                        limpiarCampos();
                    } else {
                        // Si ID > 0, buscar talonario
                        buscarPorId(id);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(vTalonarios.this,
                            "ID inválido. Ingrese un número entero.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Configurar evento de selección completa al hacer clic en txtId
        txtId.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtId.selectAll();
            }
        });

        // Agregar focus listener para seleccionar todo el texto al obtener foco
        txtId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtId.selectAll();
            }
        });
    }

    private void limpiarCampos() {
        txtId.setText("0");
        txtTimbrado.setText("");
        txtFechaVencimiento.setText("");
        txtFacturaDesde.setText("");
        txtFacturaHasta.setText("");
        txtFacturaActual.setText("");
        txtPuntoExpedicion.setText("");
        txtEstablecimiento.setText("");
        cmbTipoComprobante.setSelectedIndex(0);
        chkEstado.setSelected(true);

        // Limpiar campos de información
        txtNumeroCompleto.setText("");
        txtFacturasRestantes.setText("");
        txtEstadoTimbrado.setText("");

        esNuevo = true;
        txtId.setEnabled(true);
        habilitarCampos(true);

        txtId.requestFocusInWindow();
        txtId.selectAll();
    }

    private void habilitarCampos(boolean habilitar) {
        txtTimbrado.setEnabled(habilitar);
        txtFechaVencimiento.setEnabled(habilitar);
        txtFacturaDesde.setEnabled(habilitar);
        txtFacturaHasta.setEnabled(habilitar);
        txtFacturaActual.setEnabled(habilitar);
        txtPuntoExpedicion.setEnabled(habilitar);
        txtEstablecimiento.setEnabled(habilitar);
        cmbTipoComprobante.setEnabled(habilitar);
        chkEstado.setEnabled(habilitar);
    }

    private void cargarDatosTalonario(mTalonario talonario) {
        if (talonario != null) {
            txtId.setText(String.valueOf(talonario.getIdTalonario()));
            txtTimbrado.setText(talonario.getNumeroTimbrado());
            txtFechaVencimiento.setText(formatoFecha.format(talonario.getFechaVencimiento()));
            txtFacturaDesde.setText(String.valueOf(talonario.getFacturaDesde()));
            txtFacturaHasta.setText(String.valueOf(talonario.getFacturaHasta()));
            txtFacturaActual.setText(String.valueOf(talonario.getFacturaActual()));
            txtPuntoExpedicion.setText(talonario.getPuntoExpedicion());
            txtEstablecimiento.setText(talonario.getEstablecimiento());

            // Seleccionar el tipo de comprobante en el combobox
            String tipoComprobante = talonario.getTipoComprobante();
            for (int i = 0; i < cmbTipoComprobante.getItemCount(); i++) {
                if (cmbTipoComprobante.getItemAt(i).equals(tipoComprobante)) {
                    cmbTipoComprobante.setSelectedIndex(i);
                    break;
                }
            }

            chkEstado.setSelected(talonario.isEstado());

            esNuevo = false;
            //txtId.setEnabled(false);
            habilitarCampos(true);

            // Actualizar la información adicional
            actualizarInformacionAdicional();
        } else {
            limpiarCampos();
        }
    }

    private void buscarPorId(int id) {
        if (id == 0) {
            limpiarCampos();
            return;
        }

        mTalonario talonario = controlador.buscarTalonarioPorId(id);
        if (talonario != null) {
            cargarDatosTalonario(talonario);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se encontró ningún talonario con ID: " + id,
                    "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método para validar los datos ingresados
    private boolean validarDatos() {
        if (txtTimbrado.getText().trim().isEmpty()) {
            mostrarError("El número de timbrado es obligatorio.");
            txtTimbrado.requestFocus();
            return false;
        }

        if (txtFechaVencimiento.getText().trim().isEmpty()) {
            mostrarError("La fecha de vencimiento es obligatoria.");
            txtFechaVencimiento.requestFocus();
            return false;
        }

        // Validar formato de fecha
        try {
            formatoFecha.parse(txtFechaVencimiento.getText());
        } catch (ParseException e) {
            mostrarError("Formato de fecha inválido. Use el formato dd/mm/aaaa.");
            txtFechaVencimiento.requestFocus();
            return false;
        }

        // Validar que la fecha sea futura
        try {
            Date fechaVencimiento = formatoFecha.parse(txtFechaVencimiento.getText());
            Date hoy = new Date();
            if (fechaVencimiento.before(hoy)) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "La fecha de vencimiento es anterior a la fecha actual. ¿Desea continuar?",
                        "Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    txtFechaVencimiento.requestFocus();
                    return false;
                }
            }
        } catch (ParseException e) {
            // Ya manejado anteriormente
        }

        // Validar números de factura
        if (txtFacturaDesde.getText().trim().isEmpty()) {
            mostrarError("El número de factura inicial es obligatorio.");
            txtFacturaDesde.requestFocus();
            return false;
        }

        if (txtFacturaHasta.getText().trim().isEmpty()) {
            mostrarError("El número de factura final es obligatorio.");
            txtFacturaHasta.requestFocus();
            return false;
        }

        try {
            int facturaDesde = Integer.parseInt(txtFacturaDesde.getText());
            int facturaHasta = Integer.parseInt(txtFacturaHasta.getText());

            if (facturaDesde <= 0) {
                mostrarError("El número de factura inicial debe ser mayor a cero.");
                txtFacturaDesde.requestFocus();
                return false;
            }

            if (facturaHasta <= facturaDesde) {
                mostrarError("El número de factura final debe ser mayor al inicial.");
                txtFacturaHasta.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Los números de factura deben ser valores numéricos enteros.");
            return false;
        }

        // Validar factura actual (solo para edición)
        if (!esNuevo && !txtFacturaActual.getText().trim().isEmpty()) {
            try {
                int facturaActual = Integer.parseInt(txtFacturaActual.getText());
                int facturaDesde = Integer.parseInt(txtFacturaDesde.getText());
                int facturaHasta = Integer.parseInt(txtFacturaHasta.getText());

                if (facturaActual < facturaDesde || facturaActual > facturaHasta) {
                    mostrarError("El número de factura actual debe estar dentro del rango inicial y final.");
                    txtFacturaActual.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarError("El número de factura actual debe ser un valor numérico entero.");
                txtFacturaActual.requestFocus();
                return false;
            }
        }

        // Validar punto de expedición
        if (txtPuntoExpedicion.getText().trim().isEmpty()) {
            mostrarError("El punto de expedición es obligatorio.");
            txtPuntoExpedicion.requestFocus();
            return false;
        }

        // Validar establecimiento
        if (txtEstablecimiento.getText().trim().isEmpty()) {
            mostrarError("El establecimiento es obligatorio.");
            txtEstablecimiento.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Actualiza los campos de información adicional del talonario
     */
    private void actualizarInformacionAdicional() {
        try {
            // Solo actualizar si ya tenemos un talonario cargado
            if (!esNuevo) {
                // Formato: [Establecimiento]-[PuntoExpedicion]-[NumeroActual]
                String establecimiento = txtEstablecimiento.getText().trim();
                String puntoExpedicion = txtPuntoExpedicion.getText().trim();
                int facturaActual = Integer.parseInt(txtFacturaActual.getText().trim());

                // Formatear el número completo
                String numeroCompleto = String.format("%s-%s-%07d",
                        establecimiento, puntoExpedicion, facturaActual);
                txtNumeroCompleto.setText(numeroCompleto);

                // Calcular facturas restantes
                int facturaHasta = Integer.parseInt(txtFacturaHasta.getText().trim());
                int facturasRestantes = facturaHasta - facturaActual + 1;
                txtFacturasRestantes.setText(String.valueOf(facturasRestantes));

                // Verificar estado del timbrado
                try {
                    Date fechaVencimiento = formatoFecha.parse(txtFechaVencimiento.getText().trim());
                    Date hoy = new Date();

                    if (fechaVencimiento.before(hoy)) {
                        txtEstadoTimbrado.setText("VENCIDO");
                        txtEstadoTimbrado.setBackground(new java.awt.Color(255, 200, 200));
                        txtEstadoTimbrado.setForeground(new java.awt.Color(178, 34, 34));
                    } else {
                        txtEstadoTimbrado.setText("VIGENTE");
                        txtEstadoTimbrado.setBackground(new java.awt.Color(229, 247, 229));
                        txtEstadoTimbrado.setForeground(new java.awt.Color(34, 139, 34));
                    }
                } catch (ParseException e) {
                    txtEstadoTimbrado.setText("INDETERMINADO");
                    txtEstadoTimbrado.setBackground(new java.awt.Color(255, 255, 200));
                    txtEstadoTimbrado.setForeground(java.awt.Color.ORANGE.darker());
                }
            } else {
                // Limpiar campos si es nuevo
                txtNumeroCompleto.setText("");
                txtFacturasRestantes.setText("");
                txtEstadoTimbrado.setText("");
            }
        } catch (NumberFormatException e) {
            // En caso de error, no actualizar los campos
            System.err.println("Error al actualizar información adicional: " + e.getMessage());
        }
    }

    // Implementación de métodos de la interfaz
    @Override
    public void imGrabar() {
        grabar();
    }

    public void grabar() {
        if (!validarDatos()) {
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText());
            String numeroTimbrado = txtTimbrado.getText().trim();
            Date fechaVencimiento = formatoFecha.parse(txtFechaVencimiento.getText());
            int facturaDesde = Integer.parseInt(txtFacturaDesde.getText());
            int facturaHasta = Integer.parseInt(txtFacturaHasta.getText());
            boolean estado = chkEstado.isSelected();
            String tipoComprobante = cmbTipoComprobante.getSelectedItem().toString();
            String puntoExpedicion = txtPuntoExpedicion.getText().trim();
            String establecimiento = txtEstablecimiento.getText().trim();

            if (esNuevo) {
                int nuevoId = controlador.insertarTalonario(numeroTimbrado, fechaVencimiento,
                        facturaDesde, facturaHasta, estado, tipoComprobante,
                        puntoExpedicion, establecimiento);

                if (nuevoId > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Talonario guardado correctamente con ID: " + nuevoId,
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    txtId.setText(String.valueOf(nuevoId));
                    txtFacturaActual.setText(String.valueOf(facturaDesde));
                    esNuevo = false;
                    //txtId.setEnabled(false);
                }
            } else {
                int facturaActual = Integer.parseInt(txtFacturaActual.getText());

                boolean actualizado = controlador.actualizarTalonario(id, numeroTimbrado,
                        fechaVencimiento, facturaDesde, facturaHasta, estado,
                        tipoComprobante, puntoExpedicion, establecimiento, facturaActual);

                if (actualizado) {
                    JOptionPane.showMessageDialog(this,
                            "Talonario actualizado correctamente.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            // Actualizar la información adicional después de guardar
            actualizarInformacionAdicional();

        } catch (ParseException e) {
            mostrarError("Error en formato de fecha: " + e.getMessage());
        } catch (NumberFormatException e) {
            mostrarError("Error en valores numéricos: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @Override
    public void imFiltrar() {
        filtrar();
    }

    @Override
    public void imActualizar() {
        actualizar();
    }

    @Override
    public void imBorrar() {
        borrar();
    }

    @Override
    public void imNuevo() {
        nuevo();
    }

    @Override
    public void imBuscar() {
        buscar();
    }

    @Override
    public void imPrimero() {
        primero();
    }

    @Override
    public void imSiguiente() {
        siguiente();
    }

    @Override
    public void imAnterior() {
        anterior();
    }

    @Override
    public void imUltimo() {
        ultimo();
    }

    @Override
    public void imImprimir() {
        imprimir();
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
        return "talonarios";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_talonario", "numero_timbrado", "tipo_comprobante"};
    }

    public void nuevo() {
        limpiarCampos();
    }

    public void borrar() {
        if (esNuevo) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar este talonario?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                boolean eliminado = controlador.eliminarTalonario(id);

                if (eliminado) {
                    JOptionPane.showMessageDialog(this,
                            "Talonario eliminado correctamente.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    limpiarCampos();
                }
            } catch (Exception e) {
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    public void buscar() {
        try {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this,
                    "Ingrese el ID del talonario:", "Buscar Talonario",
                    JOptionPane.QUESTION_MESSAGE));

            buscarPorId(id);
        } catch (NumberFormatException e) {
            // Cancelado o entrada inválida
        }
    }

    public void primero() {
        mTalonario primerTalonario = controlador.obtenerPrimerTalonario();
        if (primerTalonario != null) {
            cargarDatosTalonario(primerTalonario);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay talonarios registrados.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void anterior() {
        try {
            int idActual = Integer.parseInt(txtId.getText());
            mTalonario anteriorTalonario = controlador.obtenerTalonarioAnterior(idActual);

            if (anteriorTalonario != null) {
                cargarDatosTalonario(anteriorTalonario);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No hay más talonarios anteriores.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            primero();
        }
    }

    public void siguiente() {
        try {
            int idActual = Integer.parseInt(txtId.getText());
            mTalonario siguienteTalonario = controlador.obtenerTalonarioSiguiente(idActual);

            if (siguienteTalonario != null) {
                cargarDatosTalonario(siguienteTalonario);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No hay más talonarios siguientes.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            ultimo();
        }
    }

    public void ultimo() {
        mTalonario ultimoTalonario = controlador.obtenerUltimoTalonario();
        if (ultimoTalonario != null) {
            cargarDatosTalonario(ultimoTalonario);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay talonarios registrados.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void imprimir() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de impresión no implementada.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void filtrar() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de filtrado no implementada.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizar() {
        try {
            int id = Integer.parseInt(txtId.getText());
            if (id > 0) {
                buscarPorId(id);
            } else {
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            limpiarCampos();
        }
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        buscarPorId(id);
    }

    // Método para mostrar mensajes de error
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtId = new javax.swing.JTextField();
        txtTimbrado = new javax.swing.JTextField();
        txtFechaVencimiento = new javax.swing.JTextField();
        txtFacturaDesde = new javax.swing.JTextField();
        txtFacturaHasta = new javax.swing.JTextField();
        cmbTipoComprobante = new javax.swing.JComboBox<>();
        txtPuntoExpedicion = new javax.swing.JTextField();
        txtEstablecimiento = new javax.swing.JTextField();
        txtFacturaActual = new javax.swing.JTextField();
        chkEstado = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        lblNumeroCompleto = new javax.swing.JLabel();
        txtNumeroCompleto = new javax.swing.JTextField();
        lblFacturasRestantes = new javax.swing.JLabel();
        txtFacturasRestantes = new javax.swing.JTextField();
        lblEstadoTimbrado = new javax.swing.JLabel();
        txtEstadoTimbrado = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestión de Talonarios");

        cmbTipoComprobante.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FACTURA", "NOTA DE CRÉDITO", "NOTA DE DÉBITO" }));

        chkEstado.setText("Activo");

        jLabel1.setText("ID:");

        jLabel2.setText("Nº Timbrado:");

        jLabel3.setText("Fecha Vencimiento:");

        jLabel4.setText("Factura Desde:");

        jLabel5.setText("Factura Hasta:");

        jLabel6.setText("Tipo Comprobante:");

        jLabel7.setText("Punto Expedición:");

        jLabel8.setText("Establecimiento:");

        jLabel9.setText("Factura Actual:");

        jLabel10.setText("Estado Factura:");

        lblNumeroCompleto.setText("Numero Completo de Factura:");

        lblFacturasRestantes.setText("Facturas Restantes:");

        lblEstadoTimbrado.setText("Estado Timbrado:");

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNumeroCompleto)
                    .addComponent(lblFacturasRestantes)
                    .addComponent(lblEstadoTimbrado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumeroCompleto, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFacturasRestantes, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEstadoTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumeroCompleto)
                    .addComponent(txtNumeroCompleto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFacturasRestantes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFacturasRestantes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEstadoTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstadoTimbrado))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtTimbrado)
                                .addComponent(txtFechaVencimiento)
                                .addComponent(txtFacturaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                            .addComponent(txtFacturaDesde, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbTipoComprobante, 0, 150, Short.MAX_VALUE)
                            .addComponent(chkEstado)
                            .addComponent(txtEstablecimiento, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(txtPuntoExpedicion)
                            .addComponent(txtFacturaActual))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTipoComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTimbrado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPuntoExpedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEstablecimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFacturaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFacturaActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFacturaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstado)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkEstado;
    private javax.swing.JComboBox<String> cmbTipoComprobante;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblEstadoTimbrado;
    private javax.swing.JLabel lblFacturasRestantes;
    private javax.swing.JLabel lblNumeroCompleto;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JTextField txtEstablecimiento;
    private javax.swing.JTextField txtEstadoTimbrado;
    private javax.swing.JTextField txtFacturaActual;
    private javax.swing.JTextField txtFacturaDesde;
    private javax.swing.JTextField txtFacturaHasta;
    private javax.swing.JTextField txtFacturasRestantes;
    private javax.swing.JTextField txtFechaVencimiento;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNumeroCompleto;
    private javax.swing.JTextField txtPuntoExpedicion;
    private javax.swing.JTextField txtTimbrado;
    // End of variables declaration//GEN-END:variables

}
