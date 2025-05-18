package vista;

import controlador.cEgresoCaja;
import interfaces.myInterface;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class vEgresoCaja extends javax.swing.JInternalFrame implements myInterface{

    private cEgresoCaja controlador;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0");

    public vEgresoCaja() {
        initComponents();
        // Habilitar botones de cerrar, maximizar y minimizar
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setTitle("Registro de Egreso de Caja");

        // Configurar formateo para el campo de monto
        configurarCampoMonto();

        // Inicializar fecha actual y usuario
        txtFecha.setText(dateFormat.format(new Date()));
        txtUsuario.setText(vLogin.getUsuarioAutenticado());

        // Establecer estado activo por defecto
        chkEstado.setSelected(true);

        // Inicializar ID en 0
        txtId.setText("0");

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

        // Configurar evento de Enter en txtId para buscar registro
        txtId.addActionListener(e -> {
            buscarPorId();
        });

        try {
            controlador = new cEgresoCaja(this);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar el controlador: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarPorId() {
        try {
            String idText = txtId.getText().trim();
            if (!idText.isEmpty()) {
                int id = Integer.parseInt(idText);
                if (id > 0) {
                    controlador.cargarEgreso(id);
                } else {
                    limpiarCampos();
                }
            } else {
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("ID inválido. Debe ser un número entero.");
            txtId.requestFocus();
            txtId.selectAll();
        }
    }

    private void configurarCampoMonto() {
        try {
            // Crear formateador para moneda
            NumberFormatter formatter = new NumberFormatter(moneyFormat);
            formatter.setValueClass(Double.class);
            formatter.setMinimum(0.0);
            formatter.setAllowsInvalid(false);

            // Asegúrate de que txtMonto sea un JFormattedTextField en lugar de JTextField
            if (txtMonto instanceof javax.swing.JFormattedTextField) {
                ((javax.swing.JFormattedTextField) txtMonto).setFormatterFactory(
                        new DefaultFormatterFactory(formatter));
                ((javax.swing.JFormattedTextField) txtMonto).setValue(0.0);
            }
        } catch (Exception e) {
            System.err.println("Error al configurar campo de monto: " + e.getMessage());
        }
    }

    // Métodos para el controlador
    public String getMonto() {
        return txtMonto.getText();
    }

    public String getConcepto() {
        return txtConcepto.getText();
    }

    public boolean getEstado() {
        return chkEstado.isSelected();
    }

    public int getId() {
        try {
            return Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void enfocarMonto() {
        txtMonto.requestFocus();
    }

    public void enfocarConcepto() {
        txtConcepto.requestFocus();
    }

    public void limpiarCampos() {
        txtId.setText("0");
        txtMonto.setText("0");
        txtConcepto.setText("");
        txtFecha.setText(dateFormat.format(new Date()));
        // Mantener el usuario actual
        chkEstado.setSelected(true);
        habilitarComponentes();
    }

    public void deshabilitarComponentes() {
        txtMonto.setEnabled(false);
        txtConcepto.setEnabled(false);
        chkEstado.setEnabled(false);
    }

    public void habilitarComponentes() {
        txtMonto.setEnabled(true);
        txtConcepto.setEnabled(true);
        chkEstado.setEnabled(true);
    }

    public void mostrarEgreso(int id, Date fecha, double monto, String concepto,
            String usuario, boolean anulado) {
        txtId.setText(String.valueOf(id));
        txtFecha.setText(dateFormat.format(fecha));
        txtMonto.setText(moneyFormat.format(monto));
        txtConcepto.setText(concepto);
        txtUsuario.setText(usuario);
        chkEstado.setSelected(!anulado);

        // Deshabilitar edición al mostrar un registro existente
        txtMonto.setEnabled(!anulado);
        txtConcepto.setEnabled(!anulado);
        chkEstado.setEnabled(!anulado);
    }

    public void actualizarEstadoAnulado(boolean anulado) {
        chkEstado.setSelected(!anulado);
        txtMonto.setEnabled(!anulado);
        txtConcepto.setEnabled(!anulado);
        chkEstado.setEnabled(!anulado);
    }

    public void mostrarMensajeError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarMensajeInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
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
    
    


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtConcepto = new javax.swing.JTextArea();
        txtFecha = new javax.swing.JTextField();
        txtUsuario = new javax.swing.JTextField();
        txtId = new javax.swing.JTextField();
        chkEstado = new javax.swing.JCheckBox();
        txtMonto = new javax.swing.JFormattedTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Registrar Egreso de Caja");

        jLabel1.setText("ID:");

        jLabel2.setText("Fecha y Hora:");

        jLabel3.setText("Monto:");

        jLabel4.setText("Concepto:");

        jLabel5.setText("Usuario:");

        jLabel6.setText("Estado:");

        txtConcepto.setColumns(20);
        txtConcepto.setRows(5);
        jScrollPane1.setViewportView(txtConcepto);

        chkEstado.setText("Activo?");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(chkEstado))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(74, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(14, 14, 14))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstado))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkEstado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtConcepto;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtId;
    private javax.swing.JFormattedTextField txtMonto;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
