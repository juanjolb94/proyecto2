package vista;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import controlador.cListaPrecios;
import interfaces.myInterface;

public class vListaPrecios extends javax.swing.JInternalFrame implements myInterface {

    // Componentes
    private JTextField txtNombre;
    private JComboBox<String> cboMoneda;
    private JDateChooser dcFechaCreacion;
    private JCheckBox chkActivo;
    private JTextArea txtObservaciones;
    private JTable tblDetalles;
    private DefaultTableModel modeloTabla;

    // Controlador
    private cListaPrecios controlador;

    public vListaPrecios() {
        super("Gestión de Listas de Precio",
                true, // resizable
                true, // closable
                true, // maximizable
                true); // iconifiable

        initComponents();
        this.controlador = new cListaPrecios(this);
    }

    @Override
    public void imGrabar() {
        controlador.imGrabar();
    }

    @Override
    public void imFiltrar() {
        // Implementar si se necesita filtrado especial
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
        // Implementar lógica de impresión si es necesario
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
        return "precio_cabecera";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"nombre", "moneda", "fecha_creacion"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        controlador.buscarPrecioPorId(id);
    }

    // ============ MÉTODOS PARA EL CONTROLADOR ============
    public void setIdCabecera(int id) {
        // Usamos el campo txtNombre para almacenar el ID en su propiedad "name"
        txtNombre.setName(String.valueOf(id));
    }

    public void setDatosCabecera(int id, String nombre, Date fecha, String moneda, boolean activo, String observaciones) {
        setIdCabecera(id);  // Primero establecemos el ID
        txtNombre.setText(nombre);
        dcFechaCreacion.setDate(fecha);
        cboMoneda.setSelectedItem(moneda);
        chkActivo.setSelected(activo);
        txtObservaciones.setText(observaciones);
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public void limpiarFormulario() {
        txtNombre.setText("");
        txtNombre.setName("0");
        dcFechaCreacion.setDate(new Date());
        cboMoneda.setSelectedIndex(0);
        chkActivo.setSelected(true);
        txtObservaciones.setText("");
        modeloTabla.setRowCount(0);
    }

    public int getIdCabecera() {
        try {
            return Integer.parseInt(txtNombre.getName());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getNombreCabecera() {
        return txtNombre.getText();
    }

    public Date getFechaCreacionCabecera() {
        return dcFechaCreacion.getDate();
    }

    public String getMonedaCabecera() {
        return (String) cboMoneda.getSelectedItem();
    }

    public boolean isActivoCabecera() {
        return chkActivo.isSelected();
    }

    public String getObservacionesCabecera() {
        return txtObservaciones.getText();
    }

    public int getFilaSeleccionada() {
        return tblDetalles.getSelectedRow();
    }

    // Método para mostrar mensajes al usuario
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 394, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
