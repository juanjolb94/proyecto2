package vista;

import interfaces.myInterface;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.mPermiso;
import util.MenusCache;

public class vMenus extends javax.swing.JInternalFrame implements myInterface {

    private List<mPermiso> menusEncontrados;

    public vMenus() {
        initComponents();
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setTitle("Sincronización de Menús del Sistema");

        configurarTabla();
        leerMenusDesdeInterfaz();
    }

    private void configurarTabla() {
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // Bold, 14pt
        // Configurar modelo de tabla
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Tipo", "Nombre Menú", "Nombre Componente", "Nivel"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        jTable1.setModel(modelo);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.setRowHeight(25);

        // Configurar anchos de columnas
        jTable1.getColumnModel().getColumn(0).setMaxWidth(80);   // Tipo
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre Menú
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(200); // Nombre Componente
        jTable1.getColumnModel().getColumn(3).setMaxWidth(60);   // Nivel
    }

    private void leerMenusDesdeInterfaz() {
        menusEncontrados = new ArrayList<>();

        vPrincipal ventanaPrincipal = obtenerVentanaPrincipal();
        if (ventanaPrincipal == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo encontrar la ventana principal",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JMenuBar menuBar = ventanaPrincipal.getJMenuBar();
        int menuId = 1;

        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0);

        // Recorrer cada menú principal
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                // Agregar menú principal
                mPermiso menuPrincipal = new mPermiso();
                menuPrincipal.setIdMenu(menuId++);
                menuPrincipal.setNombreMenu(menu.getText());
                menuPrincipal.setNombreComponente("m" + menu.getText().replace(" ", ""));
                menusEncontrados.add(menuPrincipal);

                // Agregar a la tabla
                modelo.addRow(new Object[]{
                    "MENÚ",
                    menu.getText(),
                    "m" + menu.getText().replace(" ", ""),
                    "0"
                });

                // Recorrer submenús
                for (int j = 0; j < menu.getItemCount(); j++) {
                    JMenuItem item = menu.getItem(j);
                    // SOLUCIÓN: Misma lógica que vPermisos.java
                    if (item != null
                            && item.getText() != null
                            && !item.getText().trim().isEmpty()) {

                        mPermiso submenu = new mPermiso();
                        submenu.setIdMenu(menuId++);
                        submenu.setNombreMenu(item.getText());

                        // Usar ActionCommand si existe, sino generar nombre
                        String nombreComponente = item.getActionCommand();
                        if (nombreComponente == null || nombreComponente.isEmpty()) {
                            nombreComponente = "m" + item.getText()
                                    .replace(" ", "")
                                    .replace("/", "")
                                    .replace(".", "");
                        }
                        submenu.setNombreComponente(nombreComponente);
                        menusEncontrados.add(submenu);

                        // Agregar a la tabla
                        modelo.addRow(new Object[]{
                            "SUBMENÚ",
                            item.getText(),
                            nombreComponente,
                            "1"
                        });
                    }
                }
            }
        }

        // Actualizar labels informativos
        lblTotalMenus.setText("Total encontrados: " + menusEncontrados.size() + " elementos");
        btnSincronizar.setEnabled(menusEncontrados.size() > 0);
    }

    private vPrincipal obtenerVentanaPrincipal() {
        for (Window window : Window.getWindows()) {
            if (window instanceof vPrincipal) {
                return (vPrincipal) window;
            }
        }
        return null;
    }

    private void sincronizarConPermisos() {
        if (menusEncontrados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay menús para sincronizar",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de sincronizar estos " + menusEncontrados.size()
                + " elementos con el sistema de permisos?\n\n"
                + "Esto actualizará la estructura de menús disponibles para asignar permisos.",
                "Confirmar Sincronización",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // Buscar ventana vPermisos abierta
                vPermisos ventanaPermisos = buscarVentanaPermisos();

                if (ventanaPermisos != null) {
                    // Si está abierta, actualizar directamente
                    ventanaPermisos.actualizarMenusDesdeVMenus(menusEncontrados);

                    JOptionPane.showMessageDialog(this,
                            "Sincronización completada exitosamente.\n"
                            + menusEncontrados.size() + " elementos sincronizados.\n\n"
                            + "La ventana de Permisos ha sido actualizada.",
                            "Sincronización Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Si no está abierta, guardar en cache global para uso posterior
                    MenusCache.getInstance().setMenusDelSistema(menusEncontrados);

                    JOptionPane.showMessageDialog(this,
                            "Sincronización completada exitosamente.\n"
                            + menusEncontrados.size() + " elementos almacenados en cache.\n\n"
                            + "Los cambios estarán disponibles al abrir la ventana de Permisos.",
                            "Sincronización Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                // Cerrar la ventana
                this.dispose();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error durante la sincronización: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private vPermisos buscarVentanaPermisos() {
        // Buscar en el desktop pane si vPermisos está abierta
        vPrincipal ventanaPrincipal = obtenerVentanaPrincipal();
        if (ventanaPrincipal != null) {
            JDesktopPane desktop = ventanaPrincipal.getDesktopPane();
            if (desktop != null) {
                for (JInternalFrame frame : desktop.getAllFrames()) {
                    if (frame instanceof vPermisos) {
                        return (vPermisos) frame;
                    }
                }
            }
        }
        return null;
    }

// Método público para obtener los menús encontrados
    public List<mPermiso> getMenusEncontrados() {
        return new ArrayList<>(menusEncontrados);
    }

    // Implementación de myInterface (métodos requeridos)
    @Override
    public void imGrabar() {
        /* No aplica */ }

    @Override
    public void imFiltrar() {
        /* No aplica */ }

    @Override
    public void imActualizar() {
        leerMenusDesdeInterfaz();
    }

    @Override
    public void imBorrar() {
        /* No aplica */ }

    @Override
    public void imNuevo() {
        /* No aplica */ }

    @Override
    public void imBuscar() {
        /* No aplica */ }

    @Override
    public void imPrimero() {
        /* No aplica */ }

    @Override
    public void imSiguiente() {
        /* No aplica */ }

    @Override
    public void imAnterior() {
        /* No aplica */ }

    @Override
    public void imUltimo() {
        /* No aplica */ }

    @Override
    public void imImprimir() {
        /* No aplica */ }

    @Override
    public void imInsDet() {
        /* No aplica */ }

    @Override
    public void imDelDet() {
        /* No aplica */ }

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
        return "menus_sistema";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"nombre_menu", "nombre_componente"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        /* No aplica */ }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        lblTotalMenus = new javax.swing.JLabel();
        btnActualizar = new javax.swing.JButton();
        btnSincronizar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();

        jLabel1.setText("Menús y Submenús del Sistema");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        lblTotalMenus.setText("Total encontrados: 0 elementos");

        btnActualizar.setText("Actualizar Lista");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnSincronizar.setText("Sincronizar");
        btnSincronizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSincronizarActionPerformed(evt);
            }
        });

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTotalMenus)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnSincronizar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTotalMenus)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSincronizar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        leerMenusDesdeInterfaz();
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnSincronizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSincronizarActionPerformed
        sincronizarConPermisos();
    }//GEN-LAST:event_btnSincronizarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnSincronizar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTotalMenus;
    // End of variables declaration//GEN-END:variables
}
