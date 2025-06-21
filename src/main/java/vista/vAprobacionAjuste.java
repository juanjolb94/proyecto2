package vista;

import controlador.cAprobacionAjuste;
import interfaces.myInterface;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

public class vAprobacionAjuste extends javax.swing.JInternalFrame implements myInterface {

    private cAprobacionAjuste controlador;
    private DefaultTableModel modeloTabla;
    private SimpleDateFormat formatoFecha;

    public vAprobacionAjuste() {
        initComponents();
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        try {
            this.controlador = new cAprobacionAjuste(this);
            configurarComponentes();
            configurarTabla();
            configurarEventos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar ventana: " + e.getMessage(),
                    "Error de Inicialización",
                    JOptionPane.ERROR_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        }
    }

    // Configuración inicial de componentes
    private void configurarComponentes() {
        setTitle("Aprobar/Desaprobar Ajustes de Stock");

        // Configurar date choosers
        dcFechaDesde.setDateFormatString("dd/MM/yyyy");
        dcFechaHasta.setDateFormatString("dd/MM/yyyy");

        // Establecer fecha por defecto (últimos 30 días)
        Date fechaHasta = new Date();
        Date fechaDesde = new Date(fechaHasta.getTime() - (30L * 24 * 60 * 60 * 1000));

        dcFechaDesde.setDate(fechaDesde);
        dcFechaHasta.setDate(fechaHasta);

        // Configurar etiquetas de estadísticas
        actualizarEstadisticas(0, 0, 0);
    }

    // Configuración de la tabla
    private void configurarTabla() {
        modeloTabla = controlador.getModeloTabla();
        tblAjustes.setModel(modeloTabla);

        // Configurar ancho de columnas
        tblAjustes.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        tblAjustes.getColumnModel().getColumn(1).setPreferredWidth(140); // Fecha
        tblAjustes.getColumnModel().getColumn(2).setPreferredWidth(200); // Observaciones
        tblAjustes.getColumnModel().getColumn(3).setPreferredWidth(80);  // Cant. Items
        tblAjustes.getColumnModel().getColumn(4).setPreferredWidth(80);  // Estado
        tblAjustes.getColumnModel().getColumn(5).setPreferredWidth(80);  // Aprobado

        // Configurar alineación para columnas numéricas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblAjustes.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        tblAjustes.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Cant. Items
        tblAjustes.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Estado

        // Renderizador personalizado para la columna Estado
        DefaultTableCellRenderer estadoRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value != null && value.toString().equals("Aprobado")) {
                    if (!isSelected) {
                        setBackground(new java.awt.Color(230, 255, 230)); // Verde claro
                    }
                } else if (value != null && value.toString().equals("Pendiente")) {
                    if (!isSelected) {
                        setBackground(new java.awt.Color(255, 245, 230)); // Amarillo claro
                    }
                } else {
                    if (!isSelected) {
                        setBackground(table.getBackground());
                    }
                }

                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        };
        tblAjustes.getColumnModel().getColumn(4).setCellRenderer(estadoRenderer);

        // Configurar selección
        tblAjustes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAjustes.getTableHeader().setReorderingAllowed(false);
    }

    // Configuración de eventos
    private void configurarEventos() {
        // Evento del botón buscar
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.buscarAjustes();
            }
        });

        // Evento del botón limpiar
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.limpiarFiltros();
            }
        });

        // Evento para buscar por ID al presionar Enter
        txtIdAjuste.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    controlador.buscarAjustes();
                }
            }
        });

        // Evento para buscar al cambiar fechas
        dcFechaDesde.addPropertyChangeListener("date", evt -> {
            if (dcFechaDesde.getDate() != null && dcFechaHasta.getDate() != null) {
                // Auto-buscar cuando se cambian las fechas (opcional)
                // controlador.buscarAjustes();
            }
        });

        dcFechaHasta.addPropertyChangeListener("date", evt -> {
            if (dcFechaDesde.getDate() != null && dcFechaHasta.getDate() != null) {
                // Auto-buscar cuando se cambian las fechas (opcional)
                // controlador.buscarAjustes();
            }
        });

        // Evento para manejar cambios en checkbox de aprobación
        tblAjustes.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 5) { // Columna "Aprobado"
                int fila = e.getFirstRow();
                if (fila >= 0) {
                    Boolean aprobado = (Boolean) modeloTabla.getValueAt(fila, 5);
                    if (aprobado != null) {
                        controlador.cambiarAprobacionAjuste(fila, aprobado);
                    }
                }
            }
        });

        // Doble clic para ver detalles
        tblAjustes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int filaSeleccionada = tblAjustes.getSelectedRow();
                    if (filaSeleccionada >= 0) {
                        controlador.mostrarDetallesAjuste(filaSeleccionada);
                    }
                }
            }
        });

        // Teclas de acceso rápido
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "actualizar");
        actionMap.put("actualizar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.imActualizar();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "buscar");
        actionMap.put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.buscarAjustes();
            }
        });
    }

    // Métodos para interactuar con el controlador
    // Obtener fecha desde
    public Date getFechaDesde() {
        return dcFechaDesde.getDate();
    }

    // Establecer fecha desde
    public void setFechaDesde(Date fecha) {
        dcFechaDesde.setDate(fecha);
    }

    // Obtener fecha hasta
    public Date getFechaHasta() {
        return dcFechaHasta.getDate();
    }

    // Establecer fecha hasta
    public void setFechaHasta(Date fecha) {
        dcFechaHasta.setDate(fecha);
    }

    // Obtener ID de ajuste
    public Integer getIdAjuste() {
        String texto = txtIdAjuste.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Establecer ID de ajuste
    public void setIdAjuste(Integer id) {
        txtIdAjuste.setText(id != null ? id.toString() : "");
    }

    // Limpiar filtros
    public void limpiarFiltros() {
        txtIdAjuste.setText("");

        // Establecer últimos 30 días
        Date fechaHasta = new Date();
        Date fechaDesde = new Date(fechaHasta.getTime() - (30L * 24 * 60 * 60 * 1000));

        dcFechaDesde.setDate(fechaDesde);
        dcFechaHasta.setDate(fechaHasta);
    }

    // Actualizar tabla
    public void actualizarTabla() {
        tblAjustes.revalidate();
        tblAjustes.repaint();
    }

    // Actualizar estadísticas
    public void actualizarEstadisticas(int total, int aprobados, int pendientes) {
        lblTotalAjustes.setText("Total: " + total);
        lblAprobados.setText("Aprobados: " + aprobados);
        lblPendientes.setText("Pendientes: " + pendientes);
    }

    // Seleccionar fila específica
    public void seleccionarFila(int fila) {
        if (fila >= 0 && fila < tblAjustes.getRowCount()) {
            tblAjustes.setRowSelectionInterval(fila, fila);
            tblAjustes.scrollRectToVisible(tblAjustes.getCellRect(fila, 0, true));
        }
    }

    // Obtener fila seleccionada
    public int getFilaSeleccionada() {
        return tblAjustes.getSelectedRow();
    }

    // Métodos para mostrar mensajes
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Implementación de métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        controlador.imGrabar();
    }

    @Override
    public void imFiltrar() {
        controlador.imFiltrar();
    }

    @Override
    public void imActualizar() {
        controlador.imActualizar();
    }

    @Override
    public void imBorrar() {
        controlador.imBorrar();
    }

    @Override
    public void imNuevo() {
        controlador.imNuevo();
    }

    @Override
    public void imBuscar() {
        controlador.imBuscar();
    }

    @Override
    public void imPrimero() {
        controlador.imPrimero();
    }

    @Override
    public void imSiguiente() {
        controlador.imSiguiente();
    }

    @Override
    public void imAnterior() {
        controlador.imAnterior();
    }

    @Override
    public void imUltimo() {
        controlador.imUltimo();
    }

    @Override
    public void imImprimir() {
        controlador.imImprimir();
    }

    @Override
    public void imInsDet() {
        controlador.imInsDet();
    }

    @Override
    public void imDelDet() {
        controlador.imDelDet();
    }

    @Override
    public void imCerrar() {
        controlador.imCerrar();
    }

    @Override
    public boolean imAbierto() {
        return controlador.imAbierto();
    }

    @Override
    public void imAbrir() {
        controlador.imAbrir();
    }

    @Override
    public String getTablaActual() {
        return controlador.getTablaActual();
    }

    @Override
    public String[] getCamposBusqueda() {
        return controlador.getCamposBusqueda();
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        controlador.setRegistroSeleccionado(id);
    }

    // Métodos adicionales para funcionalidades específicas
    public void enfocarFiltros() {
        txtIdAjuste.requestFocus();
        txtIdAjuste.selectAll();
    }

    public void mostrarResumenBusqueda() {
        int total = modeloTabla.getRowCount();
        if (total > 0) {
            int aprobados = 0;
            int pendientes = 0;

            for (int i = 0; i < total; i++) {
                Boolean esAprobado = (Boolean) modeloTabla.getValueAt(i, 5);
                if (esAprobado != null && esAprobado) {
                    aprobados++;
                } else {
                    pendientes++;
                }
            }

            String resumen = String.format(
                    "Resultados de búsqueda:\n"
                    + "Total de ajustes: %d\n"
                    + "Aprobados: %d\n"
                    + "Pendientes: %d",
                    total, aprobados, pendientes
            );

            mostrarMensaje(resumen);
        } else {
            mostrarMensaje("No hay ajustes para mostrar.");
        }
    }

    // Validar antes de cerrar
    public boolean puedeCarrar() {
        // Esta ventana no mantiene estado, siempre puede cerrarse
        return true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFiltros = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dcFechaDesde = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        dcFechaHasta = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        txtIdAjuste = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        pnlTabla = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAjustes = new javax.swing.JTable();
        pnlEstadisticas = new javax.swing.JPanel();
        lblTotalAjustes = new javax.swing.JLabel();
        lblAprobados = new javax.swing.JLabel();
        lblPendientes = new javax.swing.JLabel();

        jLabel1.setText("Fecha Desde:");

        jLabel2.setText("Hasta:");

        jLabel3.setText("ID Ajuste:");

        btnBuscar.setText("Buscar");

        btnLimpiar.setText("Limpiar");

        javax.swing.GroupLayout pnlFiltrosLayout = new javax.swing.GroupLayout(pnlFiltros);
        pnlFiltros.setLayout(pnlFiltrosLayout);
        pnlFiltrosLayout.setHorizontalGroup(
            pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltrosLayout.createSequentialGroup()
                        .addComponent(dcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFiltrosLayout.createSequentialGroup()
                        .addComponent(txtIdAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlFiltrosLayout.setVerticalGroup(
            pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlFiltrosLayout.createSequentialGroup()
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dcFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(dcFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdAjuste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        tblAjustes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblAjustes);

        lblTotalAjustes.setText("Total: 0");

        lblAprobados.setText("Aprobados: 0");

        lblPendientes.setText("Pendientes: 0");

        javax.swing.GroupLayout pnlEstadisticasLayout = new javax.swing.GroupLayout(pnlEstadisticas);
        pnlEstadisticas.setLayout(pnlEstadisticasLayout);
        pnlEstadisticasLayout.setHorizontalGroup(
            pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                .addGap(164, 164, 164)
                .addComponent(lblTotalAjustes)
                .addGap(64, 64, 64)
                .addComponent(lblAprobados)
                .addGap(62, 62, 62)
                .addComponent(lblPendientes)
                .addContainerGap(80, Short.MAX_VALUE))
        );
        pnlEstadisticasLayout.setVerticalGroup(
            pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadisticasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEstadisticasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalAjustes)
                    .addComponent(lblAprobados)
                    .addComponent(lblPendientes))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlTablaLayout = new javax.swing.GroupLayout(pnlTabla);
        pnlTabla.setLayout(pnlTablaLayout);
        pnlTablaLayout.setHorizontalGroup(
            pnlTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addComponent(pnlEstadisticas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlTablaLayout.setVerticalGroup(
            pnlTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTablaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlEstadisticas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlTabla, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTabla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnLimpiar;
    private com.toedter.calendar.JDateChooser dcFechaDesde;
    private com.toedter.calendar.JDateChooser dcFechaHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAprobados;
    private javax.swing.JLabel lblPendientes;
    private javax.swing.JLabel lblTotalAjustes;
    private javax.swing.JPanel pnlEstadisticas;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlTabla;
    private javax.swing.JTable tblAjustes;
    private javax.swing.JTextField txtIdAjuste;
    // End of variables declaration//GEN-END:variables
}
