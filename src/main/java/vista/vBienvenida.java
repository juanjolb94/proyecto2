package vista;

import modelo.RolesDAO;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class vBienvenida extends javax.swing.JDialog {

    private String nombreUsuario;
    private String nombreRol;
    private boolean continuar = false;
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public vBienvenida(java.awt.Frame parent, String usuario, int idRol) {
        super(parent, "Inicio de Sesión Exitoso", true);
        this.nombreUsuario = usuario;

        // Obtener nombre del rol
        try {
            RolesDAO rolesDAO = new RolesDAO();
            String[] datosRol = rolesDAO.buscarRolPorId(idRol);
            this.nombreRol = (datosRol != null) ? datosRol[1] : "Rol no encontrado";
        } catch (Exception e) {
            this.nombreRol = "Error al obtener rol";
            System.err.println("Error al obtener rol: " + e.getMessage());
        }

        initComponents();
        configurarVentana();
        llenarDatos();
    }

    private void configurarVentana() {
        setLocationRelativeTo(getParent());
        pack();

        // Configurar cierre con X
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                continuar = true;
                dispose();
            }
        });

        // Configurar Enter para continuar
        getRootPane().setDefaultButton(btnContinuar);

        // ActionListener para el botón
        btnContinuar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continuar = true;
                dispose();
            }
        });

        // Efectos hover para el botón
        btnContinuar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnContinuar.setBackground(new Color(56, 135, 60));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnContinuar.setBackground(new Color(46, 125, 50));
            }
        });
    }

    private void llenarDatos() {
        lblUsuarioValor.setText(nombreUsuario);
        lblRolValor.setText(nombreRol);
        lblFechaValor.setText(formatoFecha.format(new Date()));
    }

    public boolean isContinuar() {
        return continuar;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        iconoUsuario = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        panelInfo = new javax.swing.JPanel();
        lblUsuarioTexto = new javax.swing.JLabel();
        lblUsuarioValor = new javax.swing.JLabel();
        lblRolTexto = new javax.swing.JLabel();
        lblRolValor = new javax.swing.JLabel();
        lblFechaTexto = new javax.swing.JLabel();
        lblFechaValor = new javax.swing.JLabel();
        panelBotones = new javax.swing.JPanel();
        btnContinuar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Inicio de Sesión Exitoso");
        setModal(true);
        setResizable(false);

        panelPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        panelHeader.setBackground(new java.awt.Color(46, 125, 50));
        panelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        iconoUsuario.setFont(new java.awt.Font("Dialog", 1, 48)); // NOI18N
        iconoUsuario.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        iconoUsuario.setText("👤");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("¡Bienvenido al Sistema!");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Inicio de sesión exitoso");

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(154, 154, 154)
                        .addComponent(iconoUsuario))
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(lblTitulo))
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(lblSubtitulo)))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(iconoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        panelInfo.setBackground(new java.awt.Color(255, 255, 255));
        panelInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Información de la Sesión"));

        lblUsuarioTexto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUsuarioTexto.setForeground(new java.awt.Color(70, 70, 70));
        lblUsuarioTexto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUsuarioTexto.setText("Usuario:");
        lblUsuarioTexto.setToolTipText("Usuario:");

        lblUsuarioValor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblUsuarioValor.setForeground(new java.awt.Color(30, 30, 30));

        lblRolTexto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblRolTexto.setForeground(new java.awt.Color(70, 70, 70));
        lblRolTexto.setText("Rol:");

        lblRolValor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRolValor.setForeground(new java.awt.Color(30, 30, 30));

        lblFechaTexto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblFechaTexto.setForeground(new java.awt.Color(70, 70, 70));
        lblFechaTexto.setText("Fecha y Hora:");

        lblFechaValor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblFechaValor.setForeground(new java.awt.Color(70, 70, 70));

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsuarioTexto)
                    .addComponent(lblRolTexto)
                    .addComponent(lblFechaTexto))
                .addGap(23, 23, 23)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaValor, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblUsuarioValor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addComponent(lblRolValor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsuarioTexto)
                    .addComponent(lblUsuarioValor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRolTexto)
                    .addComponent(lblRolValor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFechaTexto)
                    .addComponent(lblFechaValor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelBotones.setBackground(new java.awt.Color(255, 255, 255));

        btnContinuar.setBackground(new java.awt.Color(46, 125, 50));
        btnContinuar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnContinuar.setForeground(new java.awt.Color(255, 255, 255));
        btnContinuar.setText("Continuar al Sistema");
        btnContinuar.setFocusPainted(false);

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(btnContinuar, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnContinuar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContinuar;
    private javax.swing.JLabel iconoUsuario;
    private javax.swing.JLabel lblFechaTexto;
    private javax.swing.JLabel lblFechaValor;
    private javax.swing.JLabel lblRolTexto;
    private javax.swing.JLabel lblRolValor;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuarioTexto;
    private javax.swing.JLabel lblUsuarioValor;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelPrincipal;
    // End of variables declaration//GEN-END:variables
}
