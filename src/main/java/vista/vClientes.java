package vista;

import controlador.cClientes;
import interfaces.myInterface;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import modelo.PersonasDAO;

public class vClientes extends javax.swing.JInternalFrame implements myInterface {
    private cClientes controlador;
    private DefaultTableModel modeloTabla;
    private boolean editando = false;

    public vClientes() throws SQLException {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setTitle("Gestión de Clientes");
        
        configurarSeleccionAutomatica();
        configurarComboBoxPersonas();
        
        controlador = new cClientes(this);
        
        txtId.setText("0");
        limpiarFormulario();
        configurarFocoInicial();
        
        // Configurar listener para la tecla Enter en el campo ID
        txtId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarClientePorId();
                }
            }
        });    
    }
    
    private void configurarSeleccionAutomatica() {
        txtId.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtId.selectAll(); // Seleccionar todo el texto al hacer clic
            }
        });

        // También seleccionar todo al obtener el foco
        txtId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtId.selectAll();
            }
        });
    }
    
    // Método para configurar el ComboBox de personas
    private void configurarComboBoxPersonas() {
        // Agregar listener para cargar datos cuando se despliega el combo
        comboPersonas.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                llenarComboBoxPersonas();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // Si se selecciona una persona, actualizar los campos relacionados
                ComboBoxItem selectedItem = (ComboBoxItem) comboPersonas.getSelectedItem();
                if (selectedItem != null) {
                    autocompletarDatosPersona(selectedItem.getId());
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // No es necesario implementar
            }
        });
    }
    
    // Método para llenar el ComboBox con las personas disponibles
    private void llenarComboBoxPersonas() {
        try {
            PersonasDAO personasDAO = new PersonasDAO();
            ResultSet resultSet = personasDAO.obtenerNombresYApellidos();
            comboPersonas.removeAllItems(); // Limpiar el ComboBox
            
            // Agregar un elemento vacío como opción predeterminada
            comboPersonas.addItem(new ComboBoxItem(0, "-- Seleccione una persona --"));
            
            while (resultSet.next()) {
                int id = resultSet.getInt("id_persona");
                String nombreCompleto = id + " - " + resultSet.getString("nombre") + " " + resultSet.getString("apellido");
                comboPersonas.addItem(new ComboBoxItem(id, nombreCompleto));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error al cargar personas: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Método para autocompletar datos del cliente basado en la persona seleccionada
    private void autocompletarDatosPersona(int idPersona) {
        if (idPersona <= 0) {
            return; // No hacer nada si no hay selección válida
        }
        
        try {
            // Buscar los datos de la persona seleccionada
            PersonasDAO personasDAO = new PersonasDAO();
            String[] datosPersona = personasDAO.buscarPersonaPorId(idPersona);
            
            if (datosPersona != null) {
                // Autocompletar campos del cliente con datos de la persona
                txtNombre.setText(datosPersona[1] + " " + datosPersona[2]); // Nombre + Apellido
                txtCI.setText(datosPersona[3]); // CI/RUC
                txtTelefono.setText(datosPersona[4]); // Teléfono
                txtEmail.setText(datosPersona[5]); // Correo
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error al cargar datos de la persona: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configurarFocoInicial() {
        comboPersonas.requestFocusInWindow(); // Cambiar el foco inicial al ComboBox de personas
    }
    
    public void limpiarFormulario() {
        txtId.setText("0");
        txtNombre.setText("");
        txtCI.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtEmail.setText("");
        chkEstado.setSelected(true);
        
        // Limpiar el ComboBox de personas
        if (comboPersonas.getItemCount() > 0) {
            comboPersonas.setSelectedIndex(0);
        }
        
        editando = false;
        
        // Habilitar campos
        txtNombre.setEnabled(true);
        txtCI.setEnabled(true);
        comboPersonas.setEnabled(true);
    }
    
    private void buscarClientePorId() {
        try {
            int id = Integer.parseInt(txtId.getText());
            
            if (id == 0) {
                limpiarFormulario();
                return;
            }
            
            // Llamar al controlador para buscar el cliente
            Object[] cliente = controlador.buscarClientePorId(id);
            
            if (cliente != null) {
                // Autocompletar campos del formulario
                cargarDatosEnFormulario(cliente);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "No se encontró ningún cliente con el ID especificado.", 
                        "Advertencia", 
                        JOptionPane.WARNING_MESSAGE);
                limpiarFormulario();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "El ID debe ser un número válido.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            limpiarFormulario();
        }
    }
    
    private void cargarClienteEnFormulario(int id) {
        try {
            Object[] cliente = controlador.buscarClientePorId(id);
            if (cliente != null) {
                cargarDatosEnFormulario(cliente);
            }
        } catch (Exception e) {
            mostrarError("Error al cargar el cliente: " + e.getMessage());
        }
    }
    
    private void cargarDatosEnFormulario(Object[] cliente) {
        txtId.setText(cliente[0].toString());
        txtNombre.setText(cliente[1].toString());
        txtCI.setText(cliente[2].toString());
        txtTelefono.setText(cliente[3].toString());
        txtDireccion.setText(cliente[4].toString());
        txtEmail.setText(cliente[5].toString());
        chkEstado.setSelected(cliente[6].toString().equals("1"));
        
        // También actualizar el ComboBox de personas si está disponible el ID de persona
        if (cliente.length > 7 && cliente[7] != null) {
            int idPersona = Integer.parseInt(cliente[7].toString());
            seleccionarPersonaEnComboBox(idPersona);
        }
        
        editando = true;
        
        // Deshabilitar campos que no deberían cambiar una vez creados
        txtCI.setEnabled(false);
        // En modo de edición, también podríamos decidir si queremos permitir cambiar la persona asociada
        comboPersonas.setEnabled(false); 
    }
    
    // Método para seleccionar una persona en el ComboBox por su ID
    private void seleccionarPersonaEnComboBox(int idPersona) {
        for (int i = 0; i < comboPersonas.getItemCount(); i++) {
            ComboBoxItem item = comboPersonas.getItemAt(i);
            if (item.getId() == idPersona) {
                comboPersonas.setSelectedIndex(i);
                return;
            }
        }
        
        // Si no se encuentra en el ComboBox, intentar cargar las personas y buscar de nuevo
        llenarComboBoxPersonas();
        for (int i = 0; i < comboPersonas.getItemCount(); i++) {
            ComboBoxItem item = comboPersonas.getItemAt(i);
            if (item.getId() == idPersona) {
                comboPersonas.setSelectedIndex(i);
                return;
            }
        }
    }
    
    // Clase para manejar los elementos del ComboBox
    class ComboBoxItem {
        private int id;
        private String descripcion;

        public ComboBoxItem(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }   
    
    // Métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty()) {
                mostrarError("El Nombre es obligatorio");
                txtNombre.requestFocus();
                return;
            }
            
            if (txtCI.getText().trim().isEmpty()) {
                mostrarError("El CI/RUC es obligatorio");
                txtCI.requestFocus();
                return;
            }
            
            // Validar que se haya seleccionado una persona
            ComboBoxItem personaSeleccionada = (ComboBoxItem) comboPersonas.getSelectedItem();
            if (personaSeleccionada == null || personaSeleccionada.getId() <= 0) {
                mostrarError("Debe seleccionar una persona");
                comboPersonas.requestFocus();
                return;
            }
            
            // Obtener valores de los campos
            int id = Integer.parseInt(txtId.getText());
            int idPersona = personaSeleccionada.getId();
            String nombre = txtNombre.getText().trim();
            String ci = txtCI.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String email = txtEmail.getText().trim();
            boolean estado = chkEstado.isSelected();
            
            // Llamar al controlador para guardar/actualizar
            boolean exito;
            if (id > 0) { // Si el ID es mayor que 0, actualizamos
                exito = controlador.actualizarCliente(id, nombre, ci, telefono, direccion, email, estado, idPersona);
                if (exito) {
                    mostrarMensaje("Cliente actualizado correctamente");
                }
            } else { // Si el ID es 0, insertamos un nuevo registro
                exito = controlador.insertarCliente(nombre, ci, telefono, direccion, email, estado, idPersona);
                if (exito) {
                    mostrarMensaje("Cliente guardado correctamente");
                }
            }
            
            if (exito) {
                limpiarFormulario();
            }
            
        } catch (Exception e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @Override
    public void imBorrar() {
        try {
            int id = Integer.parseInt(txtId.getText());
            if (id <= 0) {
                mostrarError("Seleccione un cliente para eliminar");
                return;
            }
            
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea eliminar este cliente?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean exito = controlador.eliminarCliente(id);
                if (exito) {
                    mostrarMensaje("Cliente eliminado correctamente");
                    limpiarFormulario();
                } else {
                    mostrarError("No se pudo eliminar el cliente");
                }
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }
    
    @Override
    public void imNuevo() {
        limpiarFormulario();
    }
    
    @Override
    public void imBuscar() {
        buscarClientePorId();
    }
    
    @Override
    public void imPrimero() {
        Object[] cliente = controlador.obtenerPrimerCliente();
        if (cliente != null) {
            cargarDatosEnFormulario(cliente);
        } else {
            mostrarMensaje("No hay clientes registrados");
        }
    }
    
    @Override
    public void imSiguiente() {
        int idActual = Integer.parseInt(txtId.getText());
        Object[] cliente = controlador.obtenerSiguienteCliente(idActual);
        if (cliente != null) {
            cargarDatosEnFormulario(cliente);
        } else {
            mostrarMensaje("No hay más clientes siguientes");
        }
    }
    
    @Override
    public void imAnterior() {
        int idActual = Integer.parseInt(txtId.getText());
        Object[] cliente = controlador.obtenerAnteriorCliente(idActual);
        if (cliente != null) {
            cargarDatosEnFormulario(cliente);
        } else {
            mostrarMensaje("No hay más clientes anteriores");
        }
    }
    
    @Override
    public void imUltimo() {
        Object[] cliente = controlador.obtenerUltimoCliente();
        if (cliente != null) {
            cargarDatosEnFormulario(cliente);
        } else {
            mostrarMensaje("No hay clientes registrados");
        }
    }
    
    // Métodos de utilidad
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void imFiltrar() {
        // Se implementará si es necesario
    }
    
    @Override
    public void imActualizar() {
        // Se implementará si es necesario
    }
    
    @Override
    public void imImprimir() {
        // Se implementará si es necesario
    }
    
    @Override
    public void imInsDet() {
        // No aplicable para clientes simples
    }
    
    @Override
    public void imDelDet() {
        // No aplicable para clientes simples
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
        return "clientes";
    }
    
    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_cliente", "nombre", "ci_ruc"};
    }
    
    @Override
    public void setRegistroSeleccionado(int id) {
        txtId.setText(String.valueOf(id));
        buscarClientePorId();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlDatos = new javax.swing.JPanel();
        pnlDatos1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtCI = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        chkEstado = new javax.swing.JCheckBox();
        comboPersonas = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();

        jLabel1.setText("id Cliente:");

        jLabel2.setText("Nombre/Razón Social:");

        jLabel3.setText("Cedula/RUC:");

        jLabel6.setText("Email:");

        jLabel7.setText("Activo?:");

        jLabel4.setText("Telefono:");

        jLabel5.setText("Dirección:");

        jLabel8.setText("Persona:");

        javax.swing.GroupLayout pnlDatos1Layout = new javax.swing.GroupLayout(pnlDatos1);
        pnlDatos1.setLayout(pnlDatos1Layout);
        pnlDatos1Layout.setHorizontalGroup(
            pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatos1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatos1Layout.createSequentialGroup()
                            .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtTelefono, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                .addComponent(txtCI))
                            .addGap(51, 51, 51))
                        .addGroup(pnlDatos1Layout.createSequentialGroup()
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(16, 16, 16)))
                    .addGroup(pnlDatos1Layout.createSequentialGroup()
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(151, 151, 151)))
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comboPersonas, 0, 170, Short.MAX_VALUE)
                    .addGroup(pnlDatos1Layout.createSequentialGroup()
                        .addComponent(chkEstado)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtDireccion, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pnlDatos1Layout.setVerticalGroup(
            pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatos1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatos1Layout.createSequentialGroup()
                        .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtCI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlDatos1Layout.createSequentialGroup()
                        .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatos1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(chkEstado))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDatosLayout = new javax.swing.GroupLayout(pnlDatos);
        pnlDatos.setLayout(pnlDatosLayout);
        pnlDatosLayout.setHorizontalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDatos1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlDatosLayout.setVerticalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlDatos1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDatos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkEstado;
    private javax.swing.JComboBox<ComboBoxItem> comboPersonas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlDatos1;
    private javax.swing.JTextField txtCI;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
