package vista;

import controlador.cGestProd;
import javax.swing.*;
import interfaces.myInterface;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import modelo.service.ReporteService;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class vPrincipal extends javax.swing.JFrame {

    // Variable para almacenar las ventanas internas abiertas
    public JInternalFrame[] w_abiertos;

    public vPrincipal() {
        initComponents();
        // Limpieza de listeners duplicados
        ActionListener[] listeners = mBuscar.getActionListeners();
        for (ActionListener listener : listeners) {
            mBuscar.removeActionListener(listener);
        }
        // Asigna el listener correcto
        mBuscar.addActionListener(this::mBuscarActionPerformed);

        // Establecer en pantalla completa (maximizado)
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Adaptar tamaño a la resolución de pantalla
        ajustarTamanoVentana();

        setLocationRelativeTo(null);

        configurarMenusDetalle();
        configurarIconosMenu();
        configurarIconosMenusPrincipales();
    }

    private void ajustarTamanoVentana() {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.95);  // 95% del ancho de la pantalla
        int height = (int) (screenSize.height * 0.90); // 90% de la altura de la pantalla
        this.setPreferredSize(new java.awt.Dimension(width, height));
        this.setSize(width, height);
        this.pack();
    }

    private void configurarIconosMenu() {
        // Menú Archivo
        org.kordamp.ikonli.swing.FontIcon iconNuevo = new org.kordamp.ikonli.swing.FontIcon();
        iconNuevo.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_PLUS_OUTLINE);
        iconNuevo.setIconSize(16);
        iconNuevo.setIconColor(java.awt.Color.LIGHT_GRAY);
        mNuevo.setIcon(iconNuevo);

        org.kordamp.ikonli.swing.FontIcon iconGuardar = new org.kordamp.ikonli.swing.FontIcon();
        iconGuardar.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CONTENT_SAVE);
        iconGuardar.setIconSize(16);
        iconGuardar.setIconColor(java.awt.Color.LIGHT_GRAY);
        mGuardar.setIcon(iconGuardar);

        org.kordamp.ikonli.swing.FontIcon iconBorrar = new org.kordamp.ikonli.swing.FontIcon();
        iconBorrar.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignD.DELETE_OUTLINE);
        iconBorrar.setIconSize(16);
        iconBorrar.setIconColor(java.awt.Color.LIGHT_GRAY);
        mBorrar.setIcon(iconBorrar);

        org.kordamp.ikonli.swing.FontIcon iconBuscar = new org.kordamp.ikonli.swing.FontIcon();
        iconBuscar.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignM.MAGNIFY);
        iconBuscar.setIconSize(16);
        iconBuscar.setIconColor(java.awt.Color.LIGHT_GRAY);
        mBuscar.setIcon(iconBuscar);

        org.kordamp.ikonli.swing.FontIcon iconImprimir = new org.kordamp.ikonli.swing.FontIcon();
        iconImprimir.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PRINTER);
        iconImprimir.setIconSize(16);
        iconImprimir.setIconColor(java.awt.Color.LIGHT_GRAY);
        mImprimir.setIcon(iconImprimir);

        org.kordamp.ikonli.swing.FontIcon iconCerrarVentana = new org.kordamp.ikonli.swing.FontIcon();
        iconCerrarVentana.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignW.WINDOW_CLOSE);
        iconCerrarVentana.setIconSize(16);
        iconCerrarVentana.setIconColor(java.awt.Color.LIGHT_GRAY);
        mCerrarVentana.setIcon(iconCerrarVentana);

        org.kordamp.ikonli.swing.FontIcon iconSalir = new org.kordamp.ikonli.swing.FontIcon();
        iconSalir.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignE.EXIT_TO_APP);
        iconSalir.setIconSize(16);
        iconSalir.setIconColor(java.awt.Color.LIGHT_GRAY);
        mSalir.setIcon(iconSalir);

        // Menú Edición
        org.kordamp.ikonli.swing.FontIcon iconPrimero = new org.kordamp.ikonli.swing.FontIcon();
        iconPrimero.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PAGE_FIRST);
        iconPrimero.setIconSize(16);
        iconPrimero.setIconColor(java.awt.Color.LIGHT_GRAY);
        mPrimero.setIcon(iconPrimero);

        org.kordamp.ikonli.swing.FontIcon iconAnterior = new org.kordamp.ikonli.swing.FontIcon();
        iconAnterior.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHEVRON_LEFT);
        iconAnterior.setIconSize(16);
        iconAnterior.setIconColor(java.awt.Color.LIGHT_GRAY);
        mAnterior.setIcon(iconAnterior);

        org.kordamp.ikonli.swing.FontIcon iconSiguiente = new org.kordamp.ikonli.swing.FontIcon();
        iconSiguiente.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHEVRON_RIGHT);
        iconSiguiente.setIconSize(16);
        iconSiguiente.setIconColor(java.awt.Color.LIGHT_GRAY);
        mSiguiente.setIcon(iconSiguiente);

        org.kordamp.ikonli.swing.FontIcon iconUltimo = new org.kordamp.ikonli.swing.FontIcon();
        iconUltimo.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PAGE_LAST);
        iconUltimo.setIconSize(16);
        iconUltimo.setIconColor(java.awt.Color.LIGHT_GRAY);
        mUltimo.setIcon(iconUltimo);

        org.kordamp.ikonli.swing.FontIcon iconInsDetalle = new org.kordamp.ikonli.swing.FontIcon();
        iconInsDetalle.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_PLUS);
        iconInsDetalle.setIconSize(16);
        iconInsDetalle.setIconColor(java.awt.Color.LIGHT_GRAY);
        mInsDetalle.setIcon(iconInsDetalle);

        org.kordamp.ikonli.swing.FontIcon iconDelDetalle = new org.kordamp.ikonli.swing.FontIcon();
        iconDelDetalle.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_MINUS);
        iconDelDetalle.setIconSize(16);
        iconDelDetalle.setIconColor(java.awt.Color.LIGHT_GRAY);
        mDelDetalle.setIcon(iconDelDetalle);

        // Menú Compras
        org.kordamp.ikonli.swing.FontIcon iconProveedores = new org.kordamp.ikonli.swing.FontIcon();
        iconProveedores.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_GROUP);
        iconProveedores.setIconSize(16);
        iconProveedores.setIconColor(java.awt.Color.LIGHT_GRAY);
        mProveedores.setIcon(iconProveedores);

        org.kordamp.ikonli.swing.FontIcon iconRegCompras = new org.kordamp.ikonli.swing.FontIcon();
        iconRegCompras.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CART_PLUS);
        iconRegCompras.setIconSize(16);
        iconRegCompras.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRegCompras.setIcon(iconRegCompras);

        org.kordamp.ikonli.swing.FontIcon iconRepCompras = new org.kordamp.ikonli.swing.FontIcon();
        iconRepCompras.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CLIPBOARD_TEXT);
        iconRepCompras.setIconSize(16);
        iconRepCompras.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRepCompras.setIcon(iconRepCompras);

        // Menú Ventas
        org.kordamp.ikonli.swing.FontIcon iconClientes = new org.kordamp.ikonli.swing.FontIcon();
        iconClientes.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_MULTIPLE);
        iconClientes.setIconSize(16);
        iconClientes.setIconColor(java.awt.Color.LIGHT_GRAY);
        mClientes.setIcon(iconClientes);

        org.kordamp.ikonli.swing.FontIcon iconTalonarios = new org.kordamp.ikonli.swing.FontIcon();
        iconTalonarios.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignB.BOOK_OPEN_PAGE_VARIANT);
        iconTalonarios.setIconSize(16);
        iconTalonarios.setIconColor(java.awt.Color.LIGHT_GRAY);
        mTalonarios.setIcon(iconTalonarios);

        org.kordamp.ikonli.swing.FontIcon iconRegVentas = new org.kordamp.ikonli.swing.FontIcon();
        iconRegVentas.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_REGISTER);
        iconRegVentas.setIconSize(16);
        iconRegVentas.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRegVentas.setIcon(iconRegVentas);

        org.kordamp.ikonli.swing.FontIcon iconRepVentas = new org.kordamp.ikonli.swing.FontIcon();
        iconRepVentas.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHART_LINE);
        iconRepVentas.setIconSize(16);
        iconRepVentas.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRepVentas.setIcon(iconRepVentas);

        // Menú Stock
        org.kordamp.ikonli.swing.FontIcon iconProductos = new org.kordamp.ikonli.swing.FontIcon();
        iconProductos.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PACKAGE_VARIANT_CLOSED);
        iconProductos.setIconSize(16);
        iconProductos.setIconColor(java.awt.Color.LIGHT_GRAY);
        mProductos.setIcon(iconProductos);

        org.kordamp.ikonli.swing.FontIcon iconPromociones = new org.kordamp.ikonli.swing.FontIcon();
        iconPromociones.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignT.TAG_MULTIPLE);
        iconPromociones.setIconSize(16);
        iconPromociones.setIconColor(java.awt.Color.LIGHT_GRAY);
        mPromociones.setIcon(iconPromociones);

        org.kordamp.ikonli.swing.FontIcon iconAjustarStock = new org.kordamp.ikonli.swing.FontIcon();
        iconAjustarStock.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CLIPBOARD_CHECK);
        iconAjustarStock.setIconSize(16);
        iconAjustarStock.setIconColor(java.awt.Color.LIGHT_GRAY);
        mAjustarStock.setIcon(iconAjustarStock);

        org.kordamp.ikonli.swing.FontIcon iconRepInvent = new org.kordamp.ikonli.swing.FontIcon();
        iconRepInvent.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignB.BARCODE_SCAN);
        iconRepInvent.setIconSize(16);
        iconRepInvent.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRepInvent.setIcon(iconRepInvent);

        // Menú Tesorería
        org.kordamp.ikonli.swing.FontIcon iconTipoMov = new org.kordamp.ikonli.swing.FontIcon();
        iconTipoMov.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ARRANGE_SEND_BACKWARD);
        iconTipoMov.setIconSize(16);
        iconTipoMov.setIconColor(java.awt.Color.LIGHT_GRAY);
        mAperturaCierreCaja.setIcon(iconTipoMov);

        org.kordamp.ikonli.swing.FontIcon iconIngCaja = new org.kordamp.ikonli.swing.FontIcon();
        iconIngCaja.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_PLUS);
        iconIngCaja.setIconSize(16);
        iconIngCaja.setIconColor(java.awt.Color.LIGHT_GRAY);
        mIngCaja.setIcon(iconIngCaja);

        org.kordamp.ikonli.swing.FontIcon iconEgrCaja = new org.kordamp.ikonli.swing.FontIcon();
        iconEgrCaja.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_MINUS);
        iconEgrCaja.setIconSize(16);
        iconEgrCaja.setIconColor(java.awt.Color.LIGHT_GRAY);
        mEgrCaja.setIcon(iconEgrCaja);

        org.kordamp.ikonli.swing.FontIcon iconRepCaja = new org.kordamp.ikonli.swing.FontIcon();
        iconRepCaja.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHART_BAR);
        iconRepCaja.setIconSize(16);
        iconRepCaja.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRepCaja.setIcon(iconRepCaja);

        // Menú Seguridad
        org.kordamp.ikonli.swing.FontIcon iconPersonas = new org.kordamp.ikonli.swing.FontIcon();
        iconPersonas.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT);
        iconPersonas.setIconSize(16);
        iconPersonas.setIconColor(java.awt.Color.LIGHT_GRAY);
        mPersonas.setIcon(iconPersonas);

        org.kordamp.ikonli.swing.FontIcon iconUsuarios = new org.kordamp.ikonli.swing.FontIcon();
        iconUsuarios.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_KEY);
        iconUsuarios.setIconSize(16);
        iconUsuarios.setIconColor(java.awt.Color.LIGHT_GRAY);
        mUsuarios.setIcon(iconUsuarios);

        org.kordamp.ikonli.swing.FontIcon iconRoles = new org.kordamp.ikonli.swing.FontIcon();
        iconRoles.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.SHIELD_ACCOUNT);
        iconRoles.setIconSize(16);
        iconRoles.setIconColor(java.awt.Color.LIGHT_GRAY);
        mRoles.setIcon(iconRoles);

        org.kordamp.ikonli.swing.FontIcon iconPermisos = new org.kordamp.ikonli.swing.FontIcon();
        iconPermisos.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignK.KEY);
        iconPermisos.setIconSize(16);
        iconPermisos.setIconColor(java.awt.Color.LIGHT_GRAY);
        mPermisos.setIcon(iconPermisos);

        org.kordamp.ikonli.swing.FontIcon iconMenus = new org.kordamp.ikonli.swing.FontIcon();
        iconMenus.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignM.MENU);
        iconMenus.setIconSize(16);
        iconMenus.setIconColor(java.awt.Color.LIGHT_GRAY);
        mMenus.setIcon(iconMenus);
    }

    private void configurarIconosMenusPrincipales() {
        org.kordamp.ikonli.swing.FontIcon iconArchivo = new org.kordamp.ikonli.swing.FontIcon();
        iconArchivo.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_DOCUMENT_OUTLINE);
        iconArchivo.setIconSize(16);
        iconArchivo.setIconColor(java.awt.Color.LIGHT_GRAY);
        mArchivo.setIcon(iconArchivo);

        org.kordamp.ikonli.swing.FontIcon iconEdicion = new org.kordamp.ikonli.swing.FontIcon();
        iconEdicion.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PENCIL);
        iconEdicion.setIconSize(16);
        iconEdicion.setIconColor(java.awt.Color.LIGHT_GRAY);
        mEdicion.setIcon(iconEdicion);

        org.kordamp.ikonli.swing.FontIcon iconCompras = new org.kordamp.ikonli.swing.FontIcon();
        iconCompras.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CART);
        iconCompras.setIconSize(16);
        iconCompras.setIconColor(java.awt.Color.LIGHT_GRAY);
        mCompras.setIcon(iconCompras);

        org.kordamp.ikonli.swing.FontIcon iconVentas = new org.kordamp.ikonli.swing.FontIcon();
        iconVentas.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.STORE);
        iconVentas.setIconSize(16);
        iconVentas.setIconColor(java.awt.Color.LIGHT_GRAY);
        mVentas.setIcon(iconVentas);

        org.kordamp.ikonli.swing.FontIcon iconStock = new org.kordamp.ikonli.swing.FontIcon();
        iconStock.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PACKAGE);
        iconStock.setIconSize(16);
        iconStock.setIconColor(java.awt.Color.LIGHT_GRAY);
        mStock.setIcon(iconStock);

        org.kordamp.ikonli.swing.FontIcon iconTesoreria = new org.kordamp.ikonli.swing.FontIcon();
        iconTesoreria.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH);
        iconTesoreria.setIconSize(16);
        iconTesoreria.setIconColor(java.awt.Color.LIGHT_GRAY);
        mTesoreria.setIcon(iconTesoreria);

        org.kordamp.ikonli.swing.FontIcon iconSeguridad = new org.kordamp.ikonli.swing.FontIcon();
        iconSeguridad.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.SHIELD);
        iconSeguridad.setIconSize(16);
        iconSeguridad.setIconColor(java.awt.Color.LIGHT_GRAY);
        mSeguridad.setIcon(iconSeguridad);
    }

    private boolean isWindowOpen(JInternalFrame ventana) {
        w_abiertos = jDesktopPane2.getAllFrames(); // Actualiza el arreglo de ventanas abiertas
        for (JInternalFrame ventanaAbierta : w_abiertos) {
            if (ventanaAbierta.getTitle().equals(ventana.getTitle())) {
                return true; // La ventana ya está abierta
            }
        }
        return false; // La ventana no está abierta
    }

    // Método para obtener la ventana activa
    private myInterface getCurrentWindow() {
        JInternalFrame v = jDesktopPane2.getSelectedFrame();
        if (v == null || !(v instanceof myInterface)) {
            JOptionPane.showMessageDialog(this,
                    "Abra una ventana compatible primero",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (myInterface) v;
    }

    // Método para centrar una ventana interna
    public void centrar(JInternalFrame frame) {
        int x = (jDesktopPane2.getWidth() / 2) - frame.getWidth() / 2;
        int y = (jDesktopPane2.getHeight() / 2) - frame.getHeight() / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        frame.setLocation(x, y);
    }

    // Método para mostrar una ventana interna
    void showWindow(JInternalFrame frame) {
        if (isWindowOpen(frame)) {
            return; // Si la ventana ya está abierta, no hacer nada
        }
        jDesktopPane2.add(frame);
        centrar(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true); // Intentar seleccionar la ventana
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDesktopPane2 = new javax.swing.JDesktopPane();
        menuBar = new javax.swing.JMenuBar();
        mArchivo = new javax.swing.JMenu();
        mNuevo = new javax.swing.JMenuItem();
        mGuardar = new javax.swing.JMenuItem();
        mBorrar = new javax.swing.JMenuItem();
        mBuscar = new javax.swing.JMenuItem();
        mImprimir = new javax.swing.JMenuItem();
        mCerrarVentana = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        mSalir = new javax.swing.JMenuItem();
        mEdicion = new javax.swing.JMenu();
        mPrimero = new javax.swing.JMenuItem();
        mAnterior = new javax.swing.JMenuItem();
        mSiguiente = new javax.swing.JMenuItem();
        mUltimo = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        mInsDetalle = new javax.swing.JMenuItem();
        mDelDetalle = new javax.swing.JMenuItem();
        mCompras = new javax.swing.JMenu();
        mProveedores = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mRegCompras = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mRepCompras = new javax.swing.JMenuItem();
        mVentas = new javax.swing.JMenu();
        mClientes = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mTalonarios = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        mRegVentas = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mRepVentas = new javax.swing.JMenuItem();
        mStock = new javax.swing.JMenu();
        mProductos = new javax.swing.JMenuItem();
        mPromociones = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mAjustarStock = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mRepInvent = new javax.swing.JMenuItem();
        mTesoreria = new javax.swing.JMenu();
        mAperturaCierreCaja = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mIngCaja = new javax.swing.JMenuItem();
        mEgrCaja = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        mRepCaja = new javax.swing.JMenuItem();
        mSeguridad = new javax.swing.JMenu();
        mPersonas = new javax.swing.JMenuItem();
        mUsuarios = new javax.swing.JMenuItem();
        mRoles = new javax.swing.JMenuItem();
        mPermisos = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        mMenus = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jDesktopPane2.setBackground(new java.awt.Color(0, 102, 102));

        javax.swing.GroupLayout jDesktopPane2Layout = new javax.swing.GroupLayout(jDesktopPane2);
        jDesktopPane2.setLayout(jDesktopPane2Layout);
        jDesktopPane2Layout.setHorizontalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 397, Short.MAX_VALUE)
        );
        jDesktopPane2Layout.setVerticalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 327, Short.MAX_VALUE)
        );

        getContentPane().add(jDesktopPane2, java.awt.BorderLayout.CENTER);

        mArchivo.setMnemonic('a');
        mArchivo.setText("Archivo");

        mNuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mNuevo.setMnemonic('o');
        mNuevo.setText("Nuevo");
        mArchivo.add(mNuevo);

        mGuardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mGuardar.setMnemonic('s');
        mGuardar.setText("Guardar");
        mGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGuardarActionPerformed(evt);
            }
        });
        mArchivo.add(mGuardar);

        mBorrar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mBorrar.setText("Borrar");
        mBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mBorrarActionPerformed(evt);
            }
        });
        mArchivo.add(mBorrar);

        mBuscar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mBuscar.setText("Buscar");
        mBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mBuscarActionPerformed(evt);
            }
        });
        mBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mBuscarActionPerformed(evt);
            }
        });
        mArchivo.add(mBuscar);

        mImprimir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mImprimir.setText("Imprimir");
        mArchivo.add(mImprimir);

        mCerrarVentana.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mCerrarVentana.setText("Cerrar Ventana");
        mCerrarVentana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCerrarVentanaActionPerformed(evt);
            }
        });
        mArchivo.add(mCerrarVentana);
        mArchivo.add(jSeparator9);

        mSalir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        mSalir.setMnemonic('x');
        mSalir.setText("Salir");
        mSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSalirActionPerformed(evt);
            }
        });
        mArchivo.add(mSalir);

        menuBar.add(mArchivo);

        mEdicion.setMnemonic('e');
        mEdicion.setText("Edicion");

        mPrimero.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mPrimero.setMnemonic('t');
        mPrimero.setText("Primero");
        mPrimero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPrimeroActionPerformed(evt);
            }
        });
        mEdicion.add(mPrimero);

        mAnterior.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mAnterior.setMnemonic('y');
        mAnterior.setText("Anterior");
        mAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAnteriorActionPerformed(evt);
            }
        });
        mEdicion.add(mAnterior);

        mSiguiente.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mSiguiente.setMnemonic('p');
        mSiguiente.setText("Siguiente");
        mSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSiguienteActionPerformed(evt);
            }
        });
        mEdicion.add(mSiguiente);

        mUltimo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mUltimo.setMnemonic('d');
        mUltimo.setText("Ultimo");
        mUltimo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mUltimoActionPerformed(evt);
            }
        });
        mEdicion.add(mUltimo);
        mEdicion.add(jSeparator10);

        mInsDetalle.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mInsDetalle.setText("Ins. Detalle");
        mEdicion.add(mInsDetalle);

        mDelDetalle.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mDelDetalle.setText("Del. Detalle");
        mEdicion.add(mDelDetalle);

        menuBar.add(mEdicion);

        mCompras.setMnemonic('c');
        mCompras.setText("Compras");

        mProveedores.setText("Gestionar Proveedores");
        mProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mProveedoresActionPerformed(evt);
            }
        });
        mCompras.add(mProveedores);
        mCompras.add(jSeparator1);

        mRegCompras.setText("Registrar Compra");
        mRegCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRegComprasActionPerformed(evt);
            }
        });
        mCompras.add(mRegCompras);
        mCompras.add(jSeparator2);

        mRepCompras.setText("Reporte Compras");
        mCompras.add(mRepCompras);

        menuBar.add(mCompras);

        mVentas.setMnemonic('v');
        mVentas.setText("Ventas");

        mClientes.setText("Gestionar Clientes");
        mClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mClientesActionPerformed(evt);
            }
        });
        mVentas.add(mClientes);
        mVentas.add(jSeparator3);

        mTalonarios.setText("Talonarios de Factura");
        mTalonarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mTalonariosActionPerformed(evt);
            }
        });
        mVentas.add(mTalonarios);
        mVentas.add(jSeparator12);

        mRegVentas.setText("Registrar Ventas");
        mRegVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRegVentasActionPerformed(evt);
            }
        });
        mVentas.add(mRegVentas);
        mVentas.add(jSeparator4);

        mRepVentas.setText("Reporte Ventas");
        mVentas.add(mRepVentas);

        menuBar.add(mVentas);

        mStock.setText("Stock");

        mProductos.setText("Gestionar Productos");
        mProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mProductosActionPerformed(evt);
            }
        });
        mStock.add(mProductos);

        mPromociones.setText("Gestionar Promociones");
        mStock.add(mPromociones);
        mStock.add(jSeparator5);

        mAjustarStock.setText("Ajustar Stock");
        mStock.add(mAjustarStock);
        mStock.add(jSeparator6);

        mRepInvent.setText("Reporte de Inventario");
        mRepInvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRepInventActionPerformed(evt);
            }
        });
        mStock.add(mRepInvent);

        menuBar.add(mStock);

        mTesoreria.setText("Tesoreria");

        mAperturaCierreCaja.setText("Apertura / Cierre de caja");
        mAperturaCierreCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAperturaCierreCajaActionPerformed(evt);
            }
        });
        mTesoreria.add(mAperturaCierreCaja);
        mTesoreria.add(jSeparator7);

        mIngCaja.setText("Registrar Ingreso Caja");
        mTesoreria.add(mIngCaja);

        mEgrCaja.setText("Registrar Egreso Caja");
        mTesoreria.add(mEgrCaja);
        mTesoreria.add(jSeparator8);

        mRepCaja.setText("Reporte de Ingresos - Egresos");
        mTesoreria.add(mRepCaja);

        menuBar.add(mTesoreria);

        mSeguridad.setMnemonic('s');
        mSeguridad.setText("Seguridad");

        mPersonas.setMnemonic('p');
        mPersonas.setText("Personas");
        mPersonas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPersonasActionPerformed(evt);
            }
        });
        mSeguridad.add(mPersonas);

        mUsuarios.setMnemonic('a');
        mUsuarios.setText("Usuarios");
        mUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mUsuariosActionPerformed(evt);
            }
        });
        mSeguridad.add(mUsuarios);

        mRoles.setText("Roles");
        mRoles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRolesActionPerformed(evt);
            }
        });
        mSeguridad.add(mRoles);

        mPermisos.setText("Permisos");
        mPermisos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mPermisosActionPerformed(evt);
            }
        });
        mSeguridad.add(mPermisos);
        mSeguridad.add(jSeparator11);

        mMenus.setText("Menus");
        mSeguridad.add(mMenus);

        menuBar.add(mSeguridad);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Método para manejar el evento de salir
    private void mSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSalirActionPerformed
        int eleccion = JOptionPane.showConfirmDialog(null, "Desea salir del Sistema?");
        if (eleccion == 0) {
            JOptionPane.showMessageDialog(null, "El Sistema1 ha finalizado...");
            System.exit(0);
        }
    }//GEN-LAST:event_mSalirActionPerformed

    private void mPersonasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPersonasActionPerformed
        vPersonas personasForm = new vPersonas(); // Crear una instancia de vPersonas
        personasForm.setTitle("Personas"); // Asignar un título único a la ventana
        if (isWindowOpen(personasForm)) {
            // Si la ventana ya está abierta, mostrar un mensaje de advertencia
            JOptionPane.showMessageDialog(this, "La ventana ya está abierta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            // Si no está abierta, mostrarla
            showWindow(personasForm);
        }
    }//GEN-LAST:event_mPersonasActionPerformed

    private void mUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mUsuariosActionPerformed
        vUsuarios usuariosForm = new vUsuarios(); // Crear una instancia de vUsuarios
        usuariosForm.setTitle("Usuarios"); // Asignar un título único a la ventana
        if (isWindowOpen(usuariosForm)) {
            // Si la ventana ya está abierta, mostrar un mensaje de advertencia
            JOptionPane.showMessageDialog(this, "La ventana ya está abierta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            // Si no está abierta, mostrarla
            showWindow(usuariosForm);
        }
    }//GEN-LAST:event_mUsuariosActionPerformed

    private void mGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGuardarActionPerformed
        myInterface ve = getCurrentWindow();
        if (ve == null) {
            return;
        }
        ve.imGrabar(); // Llama al método imGrabar de la ventana activa
    }//GEN-LAST:event_mGuardarActionPerformed

    private void mRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRolesActionPerformed
        vRoles rolesForm = new vRoles(); // Crear una instancia de vRoles
        rolesForm.setTitle("Roles"); // Asignar un título único a la ventana
        if (isWindowOpen(rolesForm)) {
            // Si la ventana ya está abierta, mostrar un mensaje de advertencia
            JOptionPane.showMessageDialog(this, "La ventana ya está abierta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            // Si no está abierta, mostrarla
            showWindow(rolesForm);
        }
    }//GEN-LAST:event_mRolesActionPerformed

    private void mPermisosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPermisosActionPerformed
        vPermisos permisosForm = new vPermisos();
        permisosForm.setTitle("Permisos");
        if (isWindowOpen(permisosForm)) {
            // Si la ventana ya está abierta, mostrar un mensaje de advertencia
            JOptionPane.showMessageDialog(this, "La ventana ya está abierta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            // Si no está abierta, mostrarla
            showWindow(permisosForm);
        }
    }//GEN-LAST:event_mPermisosActionPerformed

    private void mPrimeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPrimeroActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imPrimero(); // Llama al método imPrimero de la ventana activa
        }
    }//GEN-LAST:event_mPrimeroActionPerformed

    private void mAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAnteriorActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imAnterior(); // Llama al método imAnterior de la ventana activa
        }
    }//GEN-LAST:event_mAnteriorActionPerformed

    private void mSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSiguienteActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imSiguiente(); // Llama al método imSiguiente de la ventana activa
        }
    }//GEN-LAST:event_mSiguienteActionPerformed

    private void mUltimoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mUltimoActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imUltimo(); // Llama al método imUltimo de la ventana activa
        }
    }//GEN-LAST:event_mUltimoActionPerformed

    private void mBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mBorrarActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imBorrar(); // Llama al método imBorrar de la ventana activa
        }
    }//GEN-LAST:event_mBorrarActionPerformed

    private void mCerrarVentanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCerrarVentanaActionPerformed
        // Obtener la ventana activa
        JInternalFrame ventanaActiva = jDesktopPane2.getSelectedFrame();

        if (ventanaActiva != null) {
            // Preguntar al usuario si está seguro de cerrar la ventana
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de que desea cerrar esta ventana?",
                    "Confirmar cierre",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Cerrar la ventana activa
                ventanaActiva.dispose();
            }
        } else {
            // Mostrar un mensaje si no hay ventanas abiertas
            JOptionPane.showMessageDialog(this, "No hay ventanas abiertas para cerrar.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_mCerrarVentanaActionPerformed

    private void mBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mBuscarActionPerformed
        JInternalFrame frameActivo = jDesktopPane2.getSelectedFrame();

        if (frameActivo == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay ventana activa para realizar la búsqueda",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si la ventana activa es un reporte
        if (frameActivo instanceof vReport) {
            vReport reporteActivo = (vReport) frameActivo;
            reporteActivo.abrirDialogoFiltro();
            return;
        }

        // Si no es un reporte, continuar con el proceso normal de búsqueda
        if (!(frameActivo instanceof myInterface)) {
            JOptionPane.showMessageDialog(this,
                    "La ventana activa no es compatible con la función de búsqueda",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            vBusqueda busqueda = new vBusqueda(this, false, (myInterface) frameActivo);
            busqueda.setTitle("Buscar en " + frameActivo.getTitle());
            jDesktopPane2.add(busqueda);
            busqueda.setVisible(true);
            busqueda.pack();
            centrar(busqueda);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir búsqueda: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mBuscarActionPerformed

    private void mProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mProductosActionPerformed
        try {
            vGestProd productosForm = buscarVentanaProductoAbierta();

            if (productosForm != null) {
                productosForm.toFront();
                productosForm.setSelected(true);
                try {
                    productosForm.setIcon(false);
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            try {
                productosForm = new vGestProd();
                productosForm.setTitle("Gestión de Productos");

                jDesktopPane2.add(productosForm);
                centrar(productosForm);
                productosForm.setVisible(true);

                configurarControladorProductos(productosForm);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos:\n" + ex.getMessage(),
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                if (productosForm != null) {
                    productosForm.dispose();
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir gestión de productos:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private vGestProd buscarVentanaProductoAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vGestProd) {
                return (vGestProd) frame;
            }
        }
        return null;
    }//GEN-LAST:event_mProductosActionPerformed

    private void configurarControladorProductos(vGestProd productosForm) throws SQLException {
        cGestProd controlador = new cGestProd(productosForm);

        productosForm.getTxtProdId().addActionListener(e -> {
            String idText = productosForm.getTxtProdId().getText().trim();

            try {
                if (!idText.isEmpty()) {
                    int id = Integer.parseInt(idText);
                    controlador.buscarProductoPorId(id);
                } else {
                    productosForm.mostrarError("El campo ID no puede estar vacío");
                    productosForm.getTxtProdId().requestFocus();
                }
            } catch (NumberFormatException ex) {
                productosForm.mostrarError("El ID debe ser un número entero válido");
                productosForm.getTxtProdId().requestFocus();
                productosForm.getTxtProdId().selectAll();
            }
        });

        productosForm.getTxtIva().setText("10.00");
        productosForm.getTxtProdId().requestFocus();
    }

    // Configurar los listeners para los menús de insertar y eliminar detalle
    private void configurarMenusDetalle() {
        mInsDetalle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mInsDetalleActionPerformed(evt);
            }
        });

        mDelDetalle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mDelDetalleActionPerformed(evt);
            }
        });
    }

    // Método para manejar el evento del menú "Ins. Detalle"
    private void mInsDetalleActionPerformed(ActionEvent evt) {
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imInsDet(); // Llama al método imInsDet de la ventana activa
        }
    }

    // Método para manejar el evento del menú "Del. Detalle"
    private void mDelDetalleActionPerformed(ActionEvent evt) {
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imDelDet(); // Llama al método imDelDet de la ventana activa
        }
    }

    private void mRepInventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRepInventActionPerformed
        // Buscar si ya existe una ventana con este reporte
        vReport reporteExistente = buscarVentanaReporteAbierta("inventario_productos");

        if (reporteExistente != null) {
            try {
                reporteExistente.setSelected(true);
                reporteExistente.toFront();
                if (reporteExistente.isIcon()) {
                    reporteExistente.setIcon(false);
                }
            } catch (Exception e) {
                System.err.println("Error al activar ventana de reporte: " + e.getMessage());
            }
        } else {
            // Si no existe, crear nueva instancia
            mostrarReporte("inventario_productos", "filtroInventario");
        }
    }

// Método auxiliar para buscar una ventana de reporte por nombre (agregar a vPrincipal.java)
    private vReport buscarVentanaReporteAbierta(String nombreReporte) {
        for (JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vReport && frame.getTitle().contains(nombreReporte)) {
                return (vReport) frame;
            }
        }
        return null;
    }//GEN-LAST:event_mRepInventActionPerformed

    // Método para mostrar un reporte en el JDesktopPane
    private void mostrarReporte(String nombreReporte, String filtro) {
        try {
            // Crear una nueva instancia del visor de reportes
            vReport reporteFrame = new vReport(nombreReporte, filtro);
            jDesktopPane2.add(reporteFrame);
            centrar(reporteFrame);
            reporteFrame.setVisible(true);
            reporteFrame.imFiltrar();

            // Seleccionar el reporte recién creado
            try {
                reporteFrame.setSelected(true);
            } catch (Exception e) {
                System.err.println("Error al seleccionar ventana: " + e.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el reporte: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mProveedoresActionPerformed
        try {
            vProveedores proveedoresForm = buscarVentanaProveedoresAbierta();

            if (proveedoresForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                proveedoresForm.toFront();
                proveedoresForm.setSelected(true);
                try {
                    proveedoresForm.setIcon(false); // Restaurarla si está minimizada
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            // Si no está abierta, crear una nueva instancia
            try {
                proveedoresForm = new vProveedores();
                proveedoresForm.setTitle("Gestión de Proveedores");

                jDesktopPane2.add(proveedoresForm);
                centrar(proveedoresForm);
                proveedoresForm.setVisible(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos:\n" + ex.getMessage(),
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                if (proveedoresForm != null) {
                    proveedoresForm.dispose();
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir gestión de proveedores:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mProveedoresActionPerformed

    private void mRegComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRegComprasActionPerformed
        try {
            vRegCompras comprasForm = buscarVentanaComprasAbierta();

            if (comprasForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                comprasForm.toFront();
                comprasForm.setSelected(true);
                try {
                    comprasForm.setIcon(false);  // Restaurar si está minimizada
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            // Si no está abierta, crear una nueva instancia
            comprasForm = new vRegCompras();
            comprasForm.setTitle("Registrar Compra");

            jDesktopPane2.add(comprasForm);
            centrar(comprasForm);
            comprasForm.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir Registro de Compras:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mRegComprasActionPerformed

    private void mClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mClientesActionPerformed
        try {
            vClientes clientesForm = buscarVentanaClientesAbierta();

            if (clientesForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                clientesForm.toFront();
                clientesForm.setSelected(true);
                try {
                    clientesForm.setIcon(false); // Restaurarla si está minimizada
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            // Si no está abierta, crear una nueva instancia
            try {
                clientesForm = new vClientes();
                clientesForm.setTitle("Gestión de Clientes");

                jDesktopPane2.add(clientesForm);
                centrar(clientesForm);
                clientesForm.setVisible(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos:\n" + ex.getMessage(),
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                if (clientesForm != null) {
                    clientesForm.dispose();
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir gestión de clientes:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private vClientes buscarVentanaClientesAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vClientes) {
                return (vClientes) frame;
            }
        }
        return null;
    }//GEN-LAST:event_mClientesActionPerformed

    private void mRegVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRegVentasActionPerformed
        try {
            vSeleccionMesa seleccionMesaForm = buscarVentanaSeleccionMesaAbierta();

            if (seleccionMesaForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                seleccionMesaForm.toFront();
                seleccionMesaForm.setSelected(true);
                try {
                    seleccionMesaForm.setIcon(false); // Restaurar si está minimizada
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            // Si no está abierta, crear una nueva instancia
            seleccionMesaForm = new vSeleccionMesa();
            jDesktopPane2.add(seleccionMesaForm);
            centrar(seleccionMesaForm);
            seleccionMesaForm.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir selección de mesas:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mRegVentasActionPerformed

    private void mAperturaCierreCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAperturaCierreCajaActionPerformed
        try {
            vAperturaCierreCaja aperturaCierreCajaForm = buscarVentanaAperturaCierreCajaAbierta();

            if (aperturaCierreCajaForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                aperturaCierreCajaForm.toFront();
                aperturaCierreCajaForm.setSelected(true);
                try {
                    aperturaCierreCajaForm.setIcon(false);
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            aperturaCierreCajaForm = new vAperturaCierreCaja(vLogin.getUsuarioAutenticado());
            aperturaCierreCajaForm.setTitle("Apertura / Cierre de Caja");

            jDesktopPane2.add(aperturaCierreCajaForm);
            centrar(aperturaCierreCajaForm);
            aperturaCierreCajaForm.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir Apertura / Cierre de Caja:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mAperturaCierreCajaActionPerformed

    private void mTalonariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mTalonariosActionPerformed
        try {
            vTalonarios talonariosForm = buscarVentanaTalonariosAbierta();

            if (talonariosForm != null) {
                // Si la ventana ya está abierta, traerla al frente
                talonariosForm.toFront();
                talonariosForm.setSelected(true);
                try {
                    talonariosForm.setIcon(false); // Restaurarla si está minimizada
                } catch (Exception e) {
                    System.err.println("Error al restaurar ventana: " + e.getMessage());
                }
                return;
            }

            // Si no está abierta, crear una nueva instancia
            talonariosForm = new vTalonarios();
            talonariosForm.setTitle("Gestión de Talonarios");

            jDesktopPane2.add(talonariosForm);
            centrar(talonariosForm);
            talonariosForm.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir gestión de talonarios:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mTalonariosActionPerformed

    // Agregar método para buscar si la ventana de talonarios ya está abierta
    private vTalonarios buscarVentanaTalonariosAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vTalonarios) {
                return (vTalonarios) frame;
            }
        }
        return null;
    }

    // Método auxiliar para buscar si la ventana ya está abierta
    private vAperturaCierreCaja buscarVentanaAperturaCierreCajaAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vAperturaCierreCaja) {
                return (vAperturaCierreCaja) frame;
            }
        }
        return null;
    }

    private vSeleccionMesa buscarVentanaSeleccionMesaAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vSeleccionMesa) {
                return (vSeleccionMesa) frame;
            }
        }
        return null;
    }

    // Método auxiliar para buscar si la ventana de Compras ya está abierta
    private vRegCompras buscarVentanaComprasAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vRegCompras) {
                return (vRegCompras) frame;
            }
        }
        return null;
    }

    private vProveedores buscarVentanaProveedoresAbierta() {
        for (javax.swing.JInternalFrame frame : jDesktopPane2.getAllFrames()) {
            if (frame instanceof vProveedores) {
                return (vProveedores) frame;
            }
        }
        return null;
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
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new vPrincipal().setVisible(true);
                }
            });
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(vPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane jDesktopPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JMenuItem mAjustarStock;
    private javax.swing.JMenuItem mAnterior;
    private javax.swing.JMenuItem mAperturaCierreCaja;
    private javax.swing.JMenu mArchivo;
    private javax.swing.JMenuItem mBorrar;
    private javax.swing.JMenuItem mBuscar;
    private javax.swing.JMenuItem mCerrarVentana;
    private javax.swing.JMenuItem mClientes;
    private javax.swing.JMenu mCompras;
    private javax.swing.JMenuItem mDelDetalle;
    private javax.swing.JMenu mEdicion;
    private javax.swing.JMenuItem mEgrCaja;
    private javax.swing.JMenuItem mGuardar;
    private javax.swing.JMenuItem mImprimir;
    private javax.swing.JMenuItem mIngCaja;
    private javax.swing.JMenuItem mInsDetalle;
    private javax.swing.JMenuItem mMenus;
    private javax.swing.JMenuItem mNuevo;
    private javax.swing.JMenuItem mPermisos;
    private javax.swing.JMenuItem mPersonas;
    private javax.swing.JMenuItem mPrimero;
    private javax.swing.JMenuItem mProductos;
    private javax.swing.JMenuItem mPromociones;
    private javax.swing.JMenuItem mProveedores;
    private javax.swing.JMenuItem mRegCompras;
    private javax.swing.JMenuItem mRegVentas;
    private javax.swing.JMenuItem mRepCaja;
    private javax.swing.JMenuItem mRepCompras;
    private javax.swing.JMenuItem mRepInvent;
    private javax.swing.JMenuItem mRepVentas;
    private javax.swing.JMenuItem mRoles;
    private javax.swing.JMenuItem mSalir;
    private javax.swing.JMenu mSeguridad;
    private javax.swing.JMenuItem mSiguiente;
    private javax.swing.JMenu mStock;
    private javax.swing.JMenuItem mTalonarios;
    private javax.swing.JMenu mTesoreria;
    private javax.swing.JMenuItem mUltimo;
    private javax.swing.JMenuItem mUsuarios;
    private javax.swing.JMenu mVentas;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
}
