package vista;

import controlador.PersonasController;
import interfaces.myInterface;
import javax.swing.JOptionPane;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class vPersonas extends javax.swing.JInternalFrame implements myInterface{
    private PersonasController controlador; // Controlador para personas

    public vPersonas() {
        initComponents();
        
        // Habilitar botones de cerrar, maximizar y minimizar
        setClosable(true);    // Botón de cerrar
        setMaximizable(true); // Botón de maximizar
        setIconifiable(true); // Botón de minimizar (iconificar)
        
        // Inicializar el controlador
        controlador = new PersonasController(this);
        
        // Establecer el valor inicial "0" en el campo txtPersonaId
        txtPersonaId.setText("0");
        
        // Colocar el foco en txtPersonaNombre después de inicializar los componentes
        javax.swing.SwingUtilities.invokeLater(() -> {
            txtPersonaNombre.requestFocusInWindow();
        });
        
        txtPersonaFNac = new javax.swing.JTextField();
        
        txtPersonaId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarPersonaPorId();
                }
            }
        });
    }
    
    private void buscarPersonaPorId(){
        try {
            // Obtener el ID de la persona desde el campo txtPersonaId
            int id = Integer.parseInt(txtPersonaId.getText());

            // Si el ID es 0, limpiar los campos del formulario
            if (id == 0) {
                limpiarCampos();
                return; // Salir del método
            }

            // Llamar al controlador para buscar la persona
            PersonasController controlador = new PersonasController(this);
            String[] persona = controlador.buscarPersonaPorId(id);

            // Si se encontró la persona, autocompletar los campos del formulario
            if (persona != null) {
                txtPersonaId.setText(persona[0]);       // ID de la persona
                txtPersonaNombre.setText(persona[1]);   // Nombre
                txtPersonaApellido.setText(persona[2]); // Apellido
                txtPersonaCi.setText(persona[3]);       // CI
                txtPersonaTelef.setText(persona[4]);   // Teléfono
                txtPersonaCorreo.setText(persona[5]);  // Correo

                try {
                    String fechaSQL = persona[6];
                    DateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
                    Date fechaDate = formatoEntrada.parse(fechaSQL);
                    jDateChooser1.setDate(fechaDate);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(this, "Error al formatear la fecha.", "Error", JOptionPane.ERROR_MESSAGE);
                    jDateChooser1.setDate(null);
                }

                jCheckBox1.setSelected(persona[7].equals("Activo")); // Estado
            } else {
                // Si no se encontró la persona, mostrar un mensaje
                JOptionPane.showMessageDialog(this, "No se encontró ninguna persona con el ID especificado.");
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            // Si el ID no es un número válido, mostrar un mensaje de error
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            limpiarCampos();
        }
    }
    
    @Override
    public void imGrabar() {
        // Obtener los datos del formulario
        int id = Integer.parseInt(txtPersonaId.getText());
        String nombre = txtPersonaNombre.getText();
        String apellido = txtPersonaApellido.getText();
        String ci = txtPersonaCi.getText();
        String telefono = txtPersonaTelef.getText();
        String correo = txtPersonaCorreo.getText();
        String fechaNac = "";
        if (jDateChooser1.getDate() != null) {
            DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            fechaNac = formatoFecha.format(jDateChooser1.getDate());
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una fecha de nacimiento.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean activo = jCheckBox1.isSelected();

        // Convertir la fecha al formato SQL
        fechaNac = convertirFechaFormatoSQL(fechaNac);
        
        // Verificar que la fecha sea válida
        if (fechaNac == null) {
            return; // No continuar si la fecha es inválida
        }

        // Llamar al controlador para guardar el registro
        controlador.guardarPersona(id, nombre, apellido, ci, telefono, correo, fechaNac, activo);
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
        int id = Integer.parseInt(txtPersonaId.getText());

        // Mostrar un mensaje de confirmación
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea eliminar este registro?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        // Si el usuario confirma, eliminar el registro
        if (confirmacion == JOptionPane.YES_OPTION) {
            controlador.eliminarPersona(id);

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
        String[] persona = controlador.obtenerPrimerPersona();
        if (persona != null) {
            mostrarPersona(persona); // Mostrar el primer registro
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros en la base de datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        }
        System.out.println("Navegando al primer registro");
    }

    @Override
    public void imSiguiente() {
        int idActual = Integer.parseInt(txtPersonaId.getText());
        String[] persona = controlador.obtenerSiguientePersona(idActual);
        if (persona != null) {
            mostrarPersona(persona); // Mostrar el siguiente registro
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros siguientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            
        }
        System.out.println("Navegando al siguiente registro");
    }

    @Override
    public void imAnterior() {
        int idActual = Integer.parseInt(txtPersonaId.getText());
        String[] persona = controlador.obtenerAnteriorPersona(idActual);
        if (persona != null) {
            mostrarPersona(persona); // Mostrar el registro anterior
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros anteriores.", "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        }
        System.out.println("Navegando al registro anterior");
    }

    @Override
    public void imUltimo() {
        String[] persona = controlador.obtenerUltimaPersona();
        if (persona != null) {
            mostrarPersona(persona); // Mostrar el último registro
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
    
    // Método para mostrar los datos de una persona en los campos del formulario
    private void mostrarPersona(String[] persona) {
        txtPersonaId.setText(persona[0]);       // ID de la persona
        txtPersonaNombre.setText(persona[1]);   // Nombre
        txtPersonaApellido.setText(persona[2]); // Apellido
        txtPersonaCi.setText(persona[3]);      // CI
        txtPersonaTelef.setText(persona[4]);   // Teléfono
        txtPersonaCorreo.setText(persona[5]);  // Correo

        try {
            String fechaSQL = persona[6];
            DateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaDate = formatoEntrada.parse(fechaSQL);
            jDateChooser1.setDate(fechaDate);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Error al formatear la fecha.", "Error", JOptionPane.ERROR_MESSAGE);
            jDateChooser1.setDate(null);
        }

        jCheckBox1.setSelected(persona[7].equals("Activo")); // Estado
    }
    
    // Método para limpiar los campos del formulario
    private void limpiarCampos() {
        txtPersonaId.setText("0");
        txtPersonaNombre.setText("");
        txtPersonaApellido.setText("");
        txtPersonaCi.setText("");
        txtPersonaTelef.setText("");
        txtPersonaCorreo.setText("");
        jDateChooser1.setDate(null);
        jCheckBox1.setSelected(false);
        txtPersonaNombre.requestFocusInWindow();
    }
    
    private String convertirFechaFormatoSQL(String fecha) {
        try {
            // Crear un DateFormat para el formato de entrada (dd/MM/yyyy)
            DateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");
            formatoEntrada.setLenient(false); // No permitir fechas inválidas

            // Parsear la fecha desde el formato de entrada
            Date fechaDate = formatoEntrada.parse(fecha);

            // Crear un DateFormat para el formato de salida (YYYY-MM-DD)
            DateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd");

            // Convertir la fecha al formato de salida
            return formatoSalida.format(fechaDate);
        } catch (ParseException e) {
            // Si el formato es incorrecto, mostrar un mensaje de error
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    private String convertirFechaFormatoVisual(String fechaSQL) {
        try {
            // Crear un DateFormat para el formato de entrada (YYYY-MM-DD)
            DateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
            formatoEntrada.setLenient(false); // No permitir fechas inválidas

            // Parsear la fecha desde el formato de entrada
            Date fechaDate = formatoEntrada.parse(fechaSQL);

            // Crear un DateFormat para el formato de salida (dd/MM/yyyy)
            DateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy");

            // Convertir la fecha al formato de salida
            return formatoSalida.format(fechaDate);
        } catch (ParseException e) {
            // Si el formato es incorrecto, mostrar un mensaje de error
            JOptionPane.showMessageDialog(this, "Error al formatear la fecha.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    private boolean validarFormatoFecha(String fecha) {
        // Expresión regular para validar el formato dd/MM/yyyy
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
        return fecha.matches(regex);
    }
    
    @Override
    public String getTablaActual() {
        return "personas";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_persona", "CONCAT(nombre, ' ', apellido) AS descripcion"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // Buscar la persona por ID y cargar los datos en el formulario
        PersonasController controlador = new PersonasController(this);
        String[] persona = controlador.buscarPersonaPorId(id);

        if (persona != null) {
            mostrarPersona(persona); // Mostrar los datos de la persona en el formulario
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró ninguna persona con el ID especificado.");
            limpiarCampos();
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtPersonaNombre = new javax.swing.JTextField();
        txtPersonaApellido = new javax.swing.JTextField();
        txtPersonaCi = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtPersonaId = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        txtPersonaTelef = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPersonaCorreo = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();

        jLabel2.setText("ID PERSONA:");

        jLabel3.setText("NOMBRE:");

        jLabel4.setText("APELLIDO:");

        jLabel5.setText("NRO DOC.:");

        jLabel7.setText("ACTIVO?");

        txtPersonaId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPersonaIdFocusGained(evt);
            }
        });

        jLabel6.setText("FECHA NAC.:");

        jLabel8.setText("TELEFONO:");

        jLabel9.setText("CORREO:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox1)
                    .addComponent(txtPersonaId, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPersonaCi, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPersonaNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(txtPersonaApellido)
                    .addComponent(txtPersonaTelef, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(txtPersonaCorreo)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPersonaId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPersonaNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtPersonaApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtPersonaCi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtPersonaTelef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtPersonaCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jCheckBox1))
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPersonaIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPersonaIdFocusGained
        txtPersonaId.selectAll();
    }//GEN-LAST:event_txtPersonaIdFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField txtPersonaApellido;
    private javax.swing.JTextField txtPersonaCi;
    private javax.swing.JTextField txtPersonaCorreo;
    private javax.swing.JTextField txtPersonaId;
    private javax.swing.JTextField txtPersonaNombre;
    private javax.swing.JTextField txtPersonaTelef;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTextField txtPersonaFNac;
}