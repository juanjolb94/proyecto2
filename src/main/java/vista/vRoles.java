package vista;

import controlador.RolesController;
import interfaces.myInterface;
import javax.swing.JOptionPane;

public class vRoles extends javax.swing.JInternalFrame implements myInterface{
    private RolesController controlador;
    
    public vRoles() {
        initComponents();
        
        // Habilitar botones de cerrar, maximizar y minimizar
        setClosable(true);    // Botón de cerrar
        setMaximizable(true); // Botón de maximizar
        setIconifiable(true); // Botón de minimizar (iconificar)
        
        // Inicializar el controlador
        controlador = new RolesController(this);
        
        // Asignar el valor inicial "0" al campo txtRolesId
        txtRolesId.setText("0");

        javax.swing.SwingUtilities.invokeLater(() -> {
            txtRolesNombre.requestFocusInWindow();
        });
        
        // Agregar un KeyListener para capturar el evento de presionar "Enter"
        txtRolesId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarRolPorId();
                }
            }
        });
    }    
    
    private void buscarRolPorId() {
        try {
            // Obtener el ID del rol desde el campo txtRolesId
            int id = Integer.parseInt(txtRolesId.getText());
            
            // Si el ID es 0, limpiar los campos del formulario
            if (id == 0) {
                limpiarCampos();
                return; // Salir del método
            }

            // Llamar al controlador para buscar el rol
            RolesController controlador = new RolesController(this);
            String[] rol = controlador.buscarRolPorId(id);

            // Si se encontró el rol, autocompletar los campos del formulario
            if (rol != null) {
                txtRolesId.setText(rol[0]);       // ID del rol
                txtRolesNombre.setText(rol[1]);    // Nombre del rol
                jCheckBox1.setSelected(rol[2].equals("Activo")); // Estado del rol
            } else {
                // Si no se encontró el rol, mostrar un mensaje
                JOptionPane.showMessageDialog(this, "No se encontró ningún rol con el ID especificado.");
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            // Si el ID no es un número válido, mostrar un mensaje de error
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            limpiarCampos();
        }
    }
    
    // Método para limpiar los campos del formulario
    private void limpiarCampos() {
        txtRolesId.setText("0"); 
        txtRolesNombre.setText(""); 
        jCheckBox1.setSelected(false); 
        txtRolesNombre.requestFocusInWindow(); 
    }
    
    @Override
    public void imGrabar() {
        // Obtener los datos del formulario
        int id = Integer.parseInt(txtRolesId.getText());
        String nombre = txtRolesNombre.getText();
    
        boolean activo = jCheckBox1.isSelected();

        // Llamar al controlador para guardar el registro
        RolesController controlador = new RolesController(this);
        controlador.guardarRol(id, nombre, activo);
        limpiarCampos();
        
        System.out.println("Grabando");
    }

    @Override
    public void imFiltrar() {
        System.out.println("Filtrando");
    }

    @Override
    public void imActualizar() {
        System.out.println("Actualizando");
    }

    @Override
    public void imBorrar() {
        // Obtener el ID del registro activo
        int id = Integer.parseInt(txtRolesId.getText());

        // Mostrar un mensaje de confirmación
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea eliminar este registro?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        // Si el usuario confirma, eliminar el registro
        if (confirmacion == JOptionPane.YES_OPTION) {
            RolesController controlador = new RolesController(this);
            controlador.eliminarRol(id);

        limpiarCampos();

        // Mostrar un mensaje de éxito
        JOptionPane.showMessageDialog(this, "Registro eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
        System.out.println("Borrando");
    }

    @Override
    public void imNuevo() {
        System.out.println("Creando nuevo registro");
    }

    @Override
    public void imBuscar() {
        System.out.println("Buscando datos");
    }

    @Override
    public void imPrimero() {
        RolesController controlador = new RolesController(this);
        String[] rol = controlador.obtenerPrimerRol();
        if (rol != null) {
            mostrarRol(rol);
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros en la base de datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        }
        System.out.println("Navegando al primer registro");
    }

    @Override
    public void imSiguiente() {
        int idActual = Integer.parseInt(txtRolesId.getText());
        RolesController controlador = new RolesController(this);
        String[] rol = controlador.obtenerSiguienteRol(idActual);
        if (rol != null) {
            mostrarRol(rol); // Mostrar el siguiente registro
        } else {
            // No hay registros siguientes, mostrar un mensaje informativo
            JOptionPane.showMessageDialog(this, "No hay más registros siguientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
        System.out.println("Navegando al siguiente registro");
    }

    @Override
    public void imAnterior() {
        int idActual = Integer.parseInt(txtRolesId.getText());
        RolesController controlador = new RolesController(this);
        String[] rol = controlador.obtenerAnteriorRol(idActual);
        if (rol != null) {
            mostrarRol(rol); // Mostrar el registro anterior
        } else {
            // No hay registros anteriores, mostrar un mensaje informativo
            JOptionPane.showMessageDialog(this, "No hay más registros anteriores.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        }
        System.out.println("Navegando al registro anterior");
    }

    @Override
    public void imUltimo() {
        RolesController controlador = new RolesController(this);
        String[] rol = controlador.obtenerUltimoRol();
        if (rol != null) {
            mostrarRol(rol);
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros en la base de datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        }
        System.out.println("Navegando al último registro");
    }

    @Override
    public void imImprimir() {
        System.out.println("Imprimiendo datos");
    }

    @Override
    public void imInsDet() {
        System.out.println("Insertando detalle");
    }

    @Override
    public void imDelDet() {
        System.out.println("Eliminando detalle");
    }

    @Override
    public void imCerrar() {
        System.out.println("Cerrando ventana");
        this.dispose(); // Cierra la ventana
    }

    @Override
    public boolean imAbierto() {
        return this.isVisible(); // Retorna true si la ventana está visible
    }

    @Override
    public void imAbrir() {
        System.out.println("Abriendo ventana");
        this.setVisible(true);
    }
    
    // Método para mostrar los datos de un rol en los campos del formulario
    private void mostrarRol(String[] rol) {
        txtRolesId.setText(rol[0]);       // ID del rol
        txtRolesNombre.setText(rol[1]);   // Nombre del rol
        jCheckBox1.setSelected(rol[2].equals("Activo")); // Estado del rol
    }
    
    // Implementación de los métodos de la interfaz myInterface

    @Override
    public String getTablaActual() {
        return "roles"; // Nombre de la tabla en la base de datos
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_rol", "nombre"}; // Campos de búsqueda en la tabla
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // Buscar el rol por ID y cargar los datos en el formulario
        RolesController controlador = new RolesController(this);
        String[] rol = controlador.buscarRolPorId(id);

        if (rol != null) {
            mostrarRol(rol); // Mostrar los datos del rol en el formulario
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró ningún rol con el ID especificado.");
            limpiarCampos();
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtRolesNombre = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtRolesId = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel2.setText("ID ROL:");

        jLabel3.setText("NOMBRE:");

        jLabel7.setText("ACTIVO?");

        txtRolesId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtRolesIdFocusGained(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtRolesNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel2))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(txtRolesId, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox1)))))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtRolesId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtRolesNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCheckBox1)
                    .addComponent(jLabel7))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtRolesIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRolesIdFocusGained
       txtRolesId.selectAll();
    }//GEN-LAST:event_txtRolesIdFocusGained
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField txtRolesId;
    private javax.swing.JTextField txtRolesNombre;
    // End of variables declaration//GEN-END:variables
}
