package vista;

import java.awt.Insets;
import javax.swing.*;
import java.awt.event.*;
import modelo.PasswordUtils;
import modelo.UsuariosDAO;

public class vLogin extends javax.swing.JDialog {

    private boolean loginExitoso = false; // Variable para almacenar el estado del login

    public vLogin(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null); // Centrar la ventana

        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Cambiar el foco al campo de contraseña
                    txtContraseña.requestFocus();
                }
            }
        });

        txtContraseña.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Verificar si se presionó Enter
                    btnAccederActionPerformed(null); // Ejecutar la validación de inicio de sesión
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtContraseña = new javax.swing.JPasswordField();
        btnAcceder = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        jPasswordField1.setText("jPasswordField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        jLabel1.setText("USUARIO");

        jLabel2.setText("CONTRASEÑA");

        btnAcceder.setText("ACCEDER");
        btnAcceder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAccederActionPerformed(evt);
            }
        });

        btnSalir.setText("SALIR");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        jLabel3.setText("INGRESE SUS DATOS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSalir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnAcceder, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtContraseña, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtUsuario, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(113, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnAcceder, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    private static String usuarioAutenticado = "";
    private static int rolAutenticado;
    private static int idUsuarioAutenticado;

    private void btnAccederActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccederActionPerformed
        String usuario = txtUsuario.getText();
        String contraseña = new String(txtContraseña.getPassword());

        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Usuario ingresado: " + usuario);
        System.out.println("Contraseña ingresada: " + contraseña);

        // Primero verificar el usuario hardcodeado admin (temporal)
        if (usuario.equals("admin") && contraseña.equals("1234")) {
            System.out.println("Login exitoso con usuario ADMIN hardcodeado");
            System.out.println("=======================");
            usuarioAutenticado = usuario;
            rolAutenticado = 1;
            idUsuarioAutenticado = 1;
            loginExitoso = true;
            this.dispose();
            return;
        }

        // Verificar usuarios de la base de datos
        try {
            UsuariosDAO usuariosDAO = new UsuariosDAO();
            String[] datosUsuario = usuariosDAO.buscarPorNombreUsuario(usuario);

            if (datosUsuario != null) {
                System.out.println("Usuario encontrado en la base de datos:");
                System.out.println("ID: " + datosUsuario[0]);
                System.out.println("Nombre: " + datosUsuario[1]);
                System.out.println("Contraseña hasheada en BD: " + datosUsuario[2]);
                System.out.println("Persona ID: " + datosUsuario[3]);
                System.out.println("Rol ID: " + datosUsuario[4]);
                System.out.println("Activo: " + datosUsuario[5]);

                if (Boolean.parseBoolean(datosUsuario[5])) { // Si está activo
                    String contraseñaHasheada = datosUsuario[2]; // Contraseña hasheada de la BD

                    System.out.println("Verificando contraseña...");
                    System.out.println("Contraseña ingresada: " + contraseña);
                    System.out.println("Contraseña hasheada en BD: " + contraseñaHasheada);

                    boolean passwordMatch = PasswordUtils.checkPassword(contraseña, contraseñaHasheada);
                    System.out.println("¿Contraseña coincide?: " + passwordMatch);

                    if (passwordMatch) {
                        System.out.println("✓ LOGIN EXITOSO");
                        System.out.println("=======================");
                        usuarioAutenticado = usuario;
                        rolAutenticado = Integer.parseInt(datosUsuario[4]);
                        idUsuarioAutenticado = Integer.parseInt(datosUsuario[0]);
                        loginExitoso = true;
                        this.dispose();
                        return;
                    } else {
                        System.out.println("✗ Contraseña incorrecta");
                    }
                } else {
                    System.out.println("✗ Usuario inactivo");
                }
            } else {
                System.out.println("✗ Usuario no encontrado en la base de datos");
            }

            System.out.println("✗ LOGIN FALLIDO");
            System.out.println("=======================");
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            System.err.println("✗ Error durante el login: " + e.getMessage());
            System.out.println("=======================");
            JOptionPane.showMessageDialog(this, "Error al verificar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAccederActionPerformed

    // Método para obtener el usuario autenticado
    public static String getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public boolean isLoginExitoso() {
        return loginExitoso;
    }

    public static int getRolAutenticado() {
        return rolAutenticado;
    }

    public static int getIdUsuarioAutenticado() {
        return idUsuarioAutenticado;
    }

    public static void main(String args[]) {
        try {
            // Inicializar el registro de iconos de Material Design
            org.kordamp.ikonli.materialdesign2.MaterialDesignC.CACHED.getCode();

            // Configurar propiedades globales de UI
            UIManager.put("Button.arc", 10); // Bordes redondeados para botones
            UIManager.put("Component.arc", 5); // Bordes redondeados para componentes
            UIManager.put("TextComponent.arc", 5); // Bordes redondeados para campos de texto
            UIManager.put("ScrollBar.thumbArc", 999); // Barras de desplazamiento redondeadas
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            UIManager.put("TabbedPane.tabsOverlapBorder", true);

            // Configurar tema oscuro de IntelliJ
            com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.setup();

            // El siguiente código comentado es para el tema claro, por si quieres volver a él
            // FlatLightLaf.setup();
            /* Create and display the dialog */
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    vLogin dialog = new vLogin(null, true); // Sin padre
                    dialog.setVisible(true);
                }
            });
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(vLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcceder;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField txtContraseña;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
