package vista;

import java.awt.BorderLayout;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import interfaces.myInterface;
import java.awt.Component;
import modelo.DatabaseConnection;
import modelo.service.ReporteService;
import net.sf.jasperreports.swing.JRViewer;

public class vReport extends JInternalFrame implements myInterface {

    private String reporteNombre;
    private String filtroVista;
    private JPanel jpReporte;
    private ReporteService reporteService;

    /**
     * Constructor para el visor de reportes
     *
     * @param reporteNombre Nombre del archivo de reporte sin extensión
     * @param filtroVista Nombre del filtro a utilizar (opcional)
     */
    public vReport(String reporteNombre, String filtroVista) {
        super("Reporte: " + reporteNombre, true, true, true, true);
        this.reporteNombre = reporteNombre;
        this.filtroVista = filtroVista;

        initComponents();

        try {
            this.reporteService = new ReporteService();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar el servicio de reportes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera y muestra el reporte aplicando los filtros necesarios
     */
    public void generarReporte() {
        // Mapa para almacenar los parámetros del reporte
        Map<String, Object> parametros = new LinkedHashMap<>();

        // Añadir parámetros básicos por defecto
        parametros.put("REPORT_TITLE", "Reporte de " + reporteNombre);
        parametros.put("FECHA_GENERACION", new Date());

        // Si hay un filtro definido, mostrar diálogo para configurar parámetros
        if (filtroVista != null && !filtroVista.isEmpty()) {
            boolean parametrosConfigurados = configurarParametrosFiltro(parametros);
            if (!parametrosConfigurados) {
                // Si el usuario canceló la configuración de parámetros, no continuar
                return;
            }
        }

        try {
            // Utilizar el servicio de reportes para generar y mostrar el reporte
            JRViewer visorReporte = reporteService.generarVisorReporte(reporteNombre, parametros);

            if (visorReporte != null) {
                // Actualizar el panel con el visor de reportes
                jpReporte.removeAll();
                jpReporte.add(visorReporte, BorderLayout.CENTER);
                jpReporte.revalidate();
                jpReporte.repaint();

                // Opcional: Guardar automáticamente una copia en PDF
                // reporteService.exportarReporteAPdf(reporteNombre, parametros, "reportes/" + reporteNombre + "_" + System.currentTimeMillis() + ".pdf");
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo generar el reporte.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el reporte: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Configura los parámetros específicos según el filtro seleccionado
     *
     * @param parametros Mapa de parámetros a configurar
     * @return true si los parámetros fueron configurados, false si se canceló
     */
    private boolean configurarParametrosFiltro(Map<String, Object> parametros) {
        switch (filtroVista) {
            case "filtroInventario":
                // Filtro para inventario de productos
                vFiltroInventario filtroInv = new vFiltroInventario(null, true);
                filtroInv.setVisible(true);

                if (filtroInv.isAceptado()) {
                    // Agregar los parámetros específicos para el reporte de inventario
                    parametros.put("categoria_id", filtroInv.getCategoriaId());
                    parametros.put("marca_id", filtroInv.getMarcaId());
                    parametros.put("mostrar_inactivos", filtroInv.getMostrarInactivos());
                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroCompras":
            // Filtro para reporte de compras

            case "filtroVentas":
            // Implementar para ventas si es necesario
            // ...

            default:
                // Si no hay un filtro específico, solo usar los parámetros por defecto
                return true;
        }
    }

    // Variables y métodos para control de zoom
    private float factorZoom = 1.0f;

    private void aumentarZoom() {
        factorZoom += 0.25f;
        if (factorZoom > 3.0f) {
            factorZoom = 3.0f;
        }
        aplicarZoom();
    }

    private void reducirZoom() {
        factorZoom -= 0.25f;
        if (factorZoom < 0.5f) {
            factorZoom = 0.5f;
        }
        aplicarZoom();
    }

    private void cambiarZoom(String zoomStr) {
        try {
            // Convertir "100%" a 1.0f
            zoomStr = zoomStr.replace("%", "").trim();
            float zoom = Float.parseFloat(zoomStr) / 100f;
            factorZoom = zoom;
            aplicarZoom();
        } catch (NumberFormatException e) {
            // Ignorar errores de formato
        }
    }

    private void aplicarZoom() {
        // Este método depende de cómo estés implementando el visor
        // con JRViewer podrías necesitar regenerar el reporte con el nuevo zoom
        // o acceder a métodos internos del visor

        // Ejemplo simplificado (puede requerir ajustes):
        for (Component comp : jpReporte.getComponents()) {
            if (comp instanceof JRViewer) {
                JRViewer viewer = (JRViewer) comp;
                // Acceder al visor interno y aplicar zoom
                // viewer.setZoomRatio(factorZoom);

                // Alternativa: regenerar el reporte con el nuevo zoom
                generarReporte();
                break;
            }
        }
    }

    private void configurarComboZoom() {
        cboZoom.removeAllItems();
        String[] zoomOpciones = {"50%", "75%", "100%", "125%", "150%", "200%"};
        for (String zoom : zoomOpciones) {
            cboZoom.addItem(zoom);
        }
        cboZoom.setSelectedItem("100%");
        cboZoom.addActionListener(e -> cambiarZoom((String) cboZoom.getSelectedItem()));
    }

    // Implementación de los métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        // No aplicable para reportes
    }

    @Override
    public void imFiltrar() {
        // Este método se llama desde la interfaz para filtrar/regenerar el reporte
        generarReporte();
    }

    @Override
    public void imActualizar() {
        // Actualizar el reporte con los últimos datos
        generarReporte();
    }

    @Override
    public void imBorrar() {
        // No aplicable para reportes
    }

    @Override
    public void imNuevo() {
        // No aplicable para reportes
    }

    @Override
    public void imBuscar() {
        // No aplicable para reportes
    }

    @Override
    public void imPrimero() {
        // No aplicable para reportes
    }

    @Override
    public void imSiguiente() {
        // No aplicable para reportes
    }

    @Override
    public void imAnterior() {
        // No aplicable para reportes
    }

    @Override
    public void imUltimo() {
        // No aplicable para reportes
    }

    @Override
    public void imImprimir() {
        try {
            // Utilizar el servicio de reportes para imprimir directamente
            reporteService.imprimirReporte(reporteNombre);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void imInsDet() {
        // No aplicable para reportes
    }

    @Override
    public void imDelDet() {
        // No aplicable para reportes
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
        return "";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[0];
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // No aplicable para reportes
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpReporte1 = new javax.swing.JPanel();
        toolbarReporte = new javax.swing.JToolBar();
        btnActualizar = new javax.swing.JButton();
        btnExportar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        btnZoomOut = new javax.swing.JButton();
        btnZoomIn = new javax.swing.JButton();
        cboZoom = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        panelEstado = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Reporte");

        toolbarReporte.setFloatable(false);
        toolbarReporte.setRollover(true);

        btnActualizar.setText("Actualizar");
        btnActualizar.setFocusable(false);
        btnActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarReporte.add(btnActualizar);

        btnExportar.setText("Exportar a PDF");
        btnExportar.setFocusable(false);
        btnExportar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarReporte.add(btnExportar);

        btnImprimir.setText("Imprimir");
        btnImprimir.setFocusable(false);
        btnImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnImprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarReporte.add(btnImprimir);

        btnZoomOut.setText("-");
        btnZoomOut.setFocusable(false);
        btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarReporte.add(btnZoomOut);

        btnZoomIn.setText("+");
        btnZoomIn.setFocusable(false);
        btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarReporte.add(btnZoomIn);

        cboZoom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        toolbarReporte.add(cboZoom);
        toolbarReporte.add(jSeparator1);

        panelEstado.setLayout(new java.awt.BorderLayout());

        lblEstado.setText("Estado:");
        panelEstado.add(lblEstado, java.awt.BorderLayout.CENTER);
        panelEstado.add(progressBar, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout jpReporte1Layout = new javax.swing.GroupLayout(jpReporte1);
        jpReporte1.setLayout(jpReporte1Layout);
        jpReporte1Layout.setHorizontalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbarReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 799, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(panelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 799, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jpReporte1Layout.setVerticalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpReporte1Layout.createSequentialGroup()
                .addComponent(toolbarReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(525, 525, 525)
                .addComponent(panelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jpReporte1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnExportar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnZoomIn;
    private javax.swing.JButton btnZoomOut;
    private javax.swing.JComboBox<String> cboZoom;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPanel jpReporte1;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JPanel panelEstado;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JToolBar toolbarReporte;
    // End of variables declaration//GEN-END:variables
}
