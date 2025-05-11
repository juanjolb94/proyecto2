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
import java.awt.Dimension;
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

        // Configuración inicial del frame
        setPreferredSize(new Dimension(800, 600));
        
        // Inicializar el panel de reporte con layout BorderLayout
        jpReporte = new JPanel(new BorderLayout());
        getContentPane().add(jpReporte, BorderLayout.CENTER);

        try {
            this.reporteService = new ReporteService();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar el servicio de reportes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        // Ajustar el tamaño
        pack();
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
                    parametros.put("stock_minimo", filtroInv.getStockMinimo());
                    parametros.put("stock_maximo", filtroInv.getStockMaximo());
                    return true;
                } else {
                    return false; // Usuario canceló
                }

            case "filtroCompras":
                // Filtro para reporte de compras
                // Implementar cuando se cree la vista de filtro de compras
                return true;

            case "filtroVentas":
                // Implementar para ventas si es necesario
                return true;

            default:
                // Si no hay un filtro específico, solo usar los parámetros por defecto
                return true;
        }
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

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Reporte");

        javax.swing.GroupLayout jpReporte1Layout = new javax.swing.GroupLayout(jpReporte1);
        jpReporte1.setLayout(jpReporte1Layout);
        jpReporte1Layout.setHorizontalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 799, Short.MAX_VALUE)
        );
        jpReporte1Layout.setVerticalGroup(
            jpReporte1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );

        getContentPane().add(jpReporte1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jpReporte1;
    // End of variables declaration//GEN-END:variables
}
