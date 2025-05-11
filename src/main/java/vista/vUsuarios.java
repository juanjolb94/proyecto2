package vista;

import interfaces.myInterface;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import modelo.PersonasDAO;
import modelo.RolesDAO;

public class vUsuarios extends javax.swing.JInternalFrame implements myInterface{

    public vUsuarios() {
        initComponents();
        
        // Habilitar botones de cerrar, maximizar y minimizar
        setClosable(true);    // Botón de cerrar
        setMaximizable(true); // Botón de maximizar
        setIconifiable(true); // Botón de minimizar (iconificar)
        
        configurarComboBoxPersonas(); // Configurar el ComboBox de personas
        configurarComboBoxRoles();    // Configurar el ComboBox de roles
        
        // Establecer el valor inicial "0" en el campo txtPersonaId
        txtUsuarioId.setText("0");

        // Colocar el cursor en el campo txtPersonaId
        txtUsuarioId.requestFocusInWindow();
    }
    
    @Override
    public void imGrabar() {
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
        System.out.println("Navegando al primer registro");
    }

    @Override
    public void imSiguiente() {
        System.out.println("Navegando al siguiente registro");
    }

    @Override
    public void imAnterior() {
        System.out.println("Navegando al registro anterior");
    }

    @Override
    public void imUltimo() {
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
        this.setVisible(true); // Hace visible la ventana
    }
    
    // Método para llenar el JComboBox de personas cuando se despliegue
    private void configurarComboBoxPersonas() {
        usuariosComboBoxPersonas.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { // Corregido aquí
                llenarComboBoxPersonas(); // Llenar el ComboBox de personas
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // No es necesario hacer nada aquí
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // No es necesario hacer nada aquí
            }
        });
    }

    // Método para llenar el JComboBox de roles cuando se despliegue
    private void configurarComboBoxRoles() {
        usuariosComboBoxRoles.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { // Corregido aquí
                llenarComboBoxRoles(); // Llenar el ComboBox de roles
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // No es necesario hacer nada aquí
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // No es necesario hacer nada aquí
            }
        });
    }

    // Método para llenar el JComboBox de personas con nombre y apellido
    private void llenarComboBoxPersonas() {
        try {
            PersonasDAO personasDAO = new PersonasDAO();
            ResultSet resultSet = personasDAO.obtenerNombresYApellidos();
            usuariosComboBoxPersonas.removeAllItems(); // Limpiar el ComboBox
            while (resultSet.next()) {
                int id = resultSet.getInt("id_persona");
                String nombreCompleto = id + " - " + resultSet.getString("nombre") + " " + resultSet.getString("apellido");
                usuariosComboBoxPersonas.addItem(new ComboBoxItem(id, nombreCompleto));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para llenar el JComboBox de roles con nombres
    private void llenarComboBoxRoles() {
        try {
            RolesDAO rolesDAO = new RolesDAO();
            ResultSet resultSet = rolesDAO.obtenerNombresRoles();
            usuariosComboBoxRoles.removeAllItems(); // Limpiar el ComboBox
            while (resultSet.next()) {
                int id = resultSet.getInt("id_rol");
                String nombreRol = id + " - " + resultSet.getString("nombre");
                usuariosComboBoxRoles.addItem(new ComboBoxItem(id, nombreRol));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Clase auxiliar para manejar los elementos del ComboBox
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
    
    @Override
    public String getTablaActual() {
        return "roles"; // Nombre de la tabla en la base de datos
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id_rol", "nombre_rol"}; // Campos de búsqueda en la tabla
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtUsuarioId = new javax.swing.JTextField();
        usuariosComboBoxPersonas = new javax.swing.JComboBox<>();
        usuariosComboBoxRoles = new javax.swing.JComboBox<>();
        txtUsuarioNombre = new javax.swing.JTextField();
        usuariosContraseña = new javax.swing.JPasswordField();
        usuariosConfContraseña = new javax.swing.JPasswordField();
        usuariosCheckBoxActivo = new javax.swing.JCheckBox();

        jLabel2.setText("ID USUARIO:");

        jLabel3.setText("USUARIO:");

        jLabel4.setText("CONTRASEÑA:");

        jLabel5.setText("ROL:");

        jLabel6.setText("PERSONA:");

        jLabel7.setText("ACTIVO?");

        jLabel8.setText("CONFIRMAR CONTRASEÑA:");
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        usuariosContraseña.setText("jPasswordField1");

        usuariosConfContraseña.setText("jPasswordField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2))
                        .addGap(96, 96, 96)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtUsuarioId, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(usuariosComboBoxPersonas, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(usuariosComboBoxRoles, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtUsuarioNombre)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usuariosContraseña, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                            .addComponent(usuariosConfContraseña)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usuariosCheckBoxActivo)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(64, 64, 64))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUsuarioId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(usuariosComboBoxPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(usuariosComboBoxRoles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtUsuarioNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(usuariosContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(usuariosConfContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(usuariosCheckBoxActivo))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField txtUsuarioId;
    private javax.swing.JTextField txtUsuarioNombre;
    private javax.swing.JCheckBox usuariosCheckBoxActivo;
    private javax.swing.JComboBox<ComboBoxItem> usuariosComboBoxPersonas;
    private javax.swing.JComboBox<ComboBoxItem> usuariosComboBoxRoles;
    private javax.swing.JPasswordField usuariosConfContraseña;
    private javax.swing.JPasswordField usuariosContraseña;
    // End of variables declaration//GEN-END:variables

}
