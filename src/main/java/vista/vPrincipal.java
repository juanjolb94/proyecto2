package vista;

import controlador.cGestProd;
import javax.swing.*;
import interfaces.myInterface;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import modelo.PermisosDAO;
import util.LogManager;

public class vPrincipal extends javax.swing.JFrame {

    // Variable para almacenar las ventanas internas abiertas
    public JInternalFrame[] w_abiertos;

    private Map<String, org.kordamp.ikonli.swing.FontIcon> iconCache = new HashMap<>();
    private vMovimientoCaja ventanaMovimientoCaja;

    // Variables para barra de estado
    private javax.swing.JPanel panelEstado;
    private javax.swing.JLabel lblUsuarioActivo;
    private javax.swing.JLabel lblFechaHora;

    public vPrincipal() {
        initComponents();
        configurarActionCommands();
        configurarBarraEstado();

        // Establecer en pantalla completa (maximizado)
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Adaptar tamaño a la resolución de pantalla
        ajustarTamanoVentana();

        setLocationRelativeTo(null);

        configurarListeners();
        configurarTodosLosIconos();
        validarIconos();
        aplicarPermisosSegunRol();
        iniciarRelojBarraEstado();
    }

    private void configurarBarraEstado() {
        // Crear panel de estado
        panelEstado = new javax.swing.JPanel(new java.awt.BorderLayout());
        panelEstado.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelEstado.setPreferredSize(new java.awt.Dimension(0, 25));

        // Crear label usuario
        lblUsuarioActivo = new javax.swing.JLabel();
        lblUsuarioActivo.setText("Usuario activo: " + vLogin.getUsuarioAutenticado());
        lblUsuarioActivo.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // Crear label fecha/hora
        lblFechaHora = new javax.swing.JLabel();
        lblFechaHora.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFechaHora.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // Actualizar fecha/hora inicial
        actualizarFechaHora();

        // Agregar labels al panel
        panelEstado.add(lblUsuarioActivo, java.awt.BorderLayout.WEST);
        panelEstado.add(lblFechaHora, java.awt.BorderLayout.EAST);

        // Agregar panel al frame (parte inferior)
        getContentPane().add(panelEstado, java.awt.BorderLayout.SOUTH);

        // Revalidar para mostrar los cambios
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void actualizarFechaHora() {
        java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lblFechaHora.setText(formato.format(new java.util.Date()));
    }

    private void iniciarRelojBarraEstado() {
        // Timer para actualizar la hora cada segundo
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> actualizarFechaHora());
        timer.start();
    }

    // Método para actualizar el usuario cuando cambie (si es necesario)
    public void actualizarUsuarioActivo() {
        if (lblUsuarioActivo != null) {
            lblUsuarioActivo.setText("Usuario activo: " + vLogin.getUsuarioAutenticado());
        }
    }

    private void configurarActionCommands() {
        // ==============================================
        // MENÚS PRINCIPALES
        // ==============================================
        mArchivo.setActionCommand("mArchivo");
        mEdicion.setActionCommand("mEdicion");
        mCompras.setActionCommand("mCompras");
        mVentas.setActionCommand("mVentas");
        mStock.setActionCommand("mStock");
        mTesoreria.setActionCommand("mTesoreria");
        mSeguridad.setActionCommand("mSeguridad");

        // ==============================================
        // MENÚ ARCHIVO
        // ==============================================
        mNuevo.setActionCommand("mNuevo");
        mGuardar.setActionCommand("mGuardar");
        mBorrar.setActionCommand("mBorrar");
        mBuscar.setActionCommand("mBuscar");
        mImprimir.setActionCommand("mImprimir");
        mCerrarVentana.setActionCommand("mCerrarVentana");
        mSalir.setActionCommand("mSalir");

        // ==============================================
        // MENÚ EDICIÓN
        // ==============================================
        mPrimero.setActionCommand("mPrimero");
        mAnterior.setActionCommand("mAnterior");
        mSiguiente.setActionCommand("mSiguiente");
        mUltimo.setActionCommand("mUltimo");
        mInsDetalle.setActionCommand("mInsDetalle");
        mDelDetalle.setActionCommand("mDelDetalle");

        // ==============================================
        // MENÚ COMPRAS
        // ==============================================
        mProveedores.setActionCommand("mProveedores");
        mRegCompras.setActionCommand("mRegCompras");
        mRepCompras.setActionCommand("mRepCompras");

        // ==============================================
        // MENÚ VENTAS
        // ==============================================
        mClientes.setActionCommand("mClientes");
        mTalonarios.setActionCommand("mTalonarios");
        mRegVentaDirecta.setActionCommand("mRegVentaDirecta");
        mRegVentas.setActionCommand("mRegVentas");
        mRepVentas.setActionCommand("mRepVentas");

        // ==============================================
        // MENÚ STOCK
        // ==============================================
        mProductos.setActionCommand("mProductos");
        mListaPrecios.setActionCommand("mListaPrecios");
        mAjustarStock.setActionCommand("mAjustarStock");
        mAprobarStock.setActionCommand("mAprobarStock");
        mRepInvent.setActionCommand("mRepInvent");

        // ==============================================
        // MENÚ TESORERÍA (LOS MÁS IMPORTANTES PARA EL PROBLEMA)
        // ==============================================
        mAperturaCierreCaja.setActionCommand("mAperturaCierreCaja");
        mIngCaja.setActionCommand("mIngCaja");
        mRepCaja.setActionCommand("mRepCaja");

        // ==============================================
        // MENÚ SEGURIDAD
        // ==============================================
        mPersonas.setActionCommand("mPersonas");
        mUsuarios.setActionCommand("mUsuarios");
        mRoles.setActionCommand("mRoles");
        mPermisos.setActionCommand("mPermisos");
        mMenus.setActionCommand("mMenus");
    }

    private void aplicarPermisosSegunRol() {
        int rolUsuario = vLogin.getRolAutenticado();
        if (rolUsuario > 0) {
            // Aplicar permisos directamente en esta ventana
            aplicarPermisosDirectamente(rolUsuario);
        }
    }

    private void aplicarPermisosDirectamente(int idRol) {
        JMenuBar menuBar = this.getJMenuBar();
        if (menuBar == null) {
            return;
        }

        // Usar PermisosDAO directamente
        PermisosDAO permisosDAO = new PermisosDAO();

        // Aplicar permisos a cada menú
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(0), idRol, permisosDAO); // Archivo
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(1), idRol, permisosDAO); // Edición
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(2), idRol, permisosDAO); // Compras
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(3), idRol, permisosDAO); // Ventas
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(4), idRol, permisosDAO); // Stock
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(5), idRol, permisosDAO); // Tesorería
        aplicarPermisosAMenuEspecifico(menuBar.getMenu(6), idRol, permisosDAO); // Seguridad
    }

    private void aplicarPermisosAMenuEspecifico(JMenu menu, int idRol, PermisosDAO permisosDAO) {
        if (menu == null) {
            return;
        }

        // Si es Administrador (rol ID = 1), habilitar todo
        if (idRol == 1) {
            menu.setEnabled(true);
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null && item.getText() != null && !item.getText().trim().isEmpty()) {
                    item.setEnabled(true);
                }
            }
            return;
        }

        try {
            // Crear el mapeo completo de textos a nombres de componentes
            Map<String, String> mapeoComponentes = crearMapeoComponentes();

            // Verificar permiso para el menú principal
            String nombreMenu = "m" + menu.getText().replace(" ", "");
            boolean tienePermiso = permisosDAO.tienePermiso(idRol, nombreMenu, "ver");
            menu.setEnabled(tienePermiso);

            // Si el menú principal no tiene permisos, deshabilitar todos los submenús
            if (!tienePermiso) {
                for (int i = 0; i < menu.getItemCount(); i++) {
                    JMenuItem item = menu.getItem(i);
                    if (item != null) {
                        item.setEnabled(false);
                    }
                }
                return;
            }

            // Aplicar permisos a submenús usando el mapeo
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null && item.getText() != null && !item.getText().trim().isEmpty()) {
                    String textoItem = item.getText();
                    String nombreComponente = mapeoComponentes.get(textoItem);

                    if (nombreComponente != null) {
                        boolean tienePermisoItem = permisosDAO.tienePermiso(idRol, nombreComponente, "ver");
                        item.setEnabled(tienePermisoItem);
                    } else {
                        // Fallback para menús no mapeados
                        item.setEnabled(false);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error aplicando permisos a menú " + menu.getText() + ": " + e.getMessage());
        }
    }

    private Map<String, String> crearMapeoComponentes() {
        Map<String, String> mapeoComponentes = new HashMap<>();

        // Menús de Compras
        mapeoComponentes.put("Proveedores", "mProveedores");
        mapeoComponentes.put("Registrar Compra", "mRegCompras");
        mapeoComponentes.put("Reporte Compras", "mRepCompras");

        // Menús de Ventas
        mapeoComponentes.put("Clientes", "mClientes");
        mapeoComponentes.put("Talonarios", "mTalonarios");
        mapeoComponentes.put("Registrar Venta Directa", "mRegVentaDirecta");
        mapeoComponentes.put("Registrar Ventas", "mRegVentas");
        mapeoComponentes.put("Reporte Ventas", "mRepVentas");
        mapeoComponentes.put("Reporte Productos Más Vendidos", "mRepProductosMasVendidos");

        // Menús de Stock
        mapeoComponentes.put("Productos", "mProductos");
        mapeoComponentes.put("Lista Precios", "mListaPrecios");
        mapeoComponentes.put("Ajustar Stock", "mAjustarStock");
        mapeoComponentes.put("Aprobar Stock", "mAprobarStock");
        mapeoComponentes.put("Reporte Inventario", "mRepInvent");

        // Menús de Tesorería
        mapeoComponentes.put("Tesoreria", "mTesoreria");
        mapeoComponentes.put("Apertura / Cierre de caja", "mAperturaCierreCaja");
        mapeoComponentes.put("Registrar Movimiento de Caja", "mIngCaja");
        mapeoComponentes.put("Reporte de Ingresos - Egresos", "mRepCaja");

        // Menús de Seguridad
        mapeoComponentes.put("Personas", "mPersonas");
        mapeoComponentes.put("Usuarios", "mUsuarios");
        mapeoComponentes.put("Roles", "mRoles");
        mapeoComponentes.put("Permisos", "mPermisos");
        mapeoComponentes.put("Menús", "mMenus");

        // Menús de Archivo
        mapeoComponentes.put("Nuevo", "mNuevo");
        mapeoComponentes.put("Guardar", "mGuardar");
        mapeoComponentes.put("Borrar", "mBorrar");
        mapeoComponentes.put("Buscar", "mBuscar");
        mapeoComponentes.put("Imprimir", "mImprimir");
        mapeoComponentes.put("Cerrar Ventana", "mCerrarVentana");
        mapeoComponentes.put("Salir", "mSalir");

        // Menús de Edición
        mapeoComponentes.put("Primero", "mPrimero");
        mapeoComponentes.put("Anterior", "mAnterior");
        mapeoComponentes.put("Siguiente", "mSiguiente");
        mapeoComponentes.put("Último", "mUltimo");
        mapeoComponentes.put("Ins. Detalle", "mInsDetalle");
        mapeoComponentes.put("Del. Detalle", "mDelDetalle");

        return mapeoComponentes;
    }

    private void ajustarTamanoVentana() {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.95);  // 95% del ancho de la pantalla
        int height = (int) (screenSize.height * 0.90); // 90% de la altura de la pantalla
        this.setPreferredSize(new java.awt.Dimension(width, height));
        this.setSize(width, height);
        this.pack();
    }

    //Método para obtener un icono del caché o crear uno nuevo si no existe
    private org.kordamp.ikonli.swing.FontIcon getOrCreateIcon(String ikonName, int size, Color color) {
        // Verificar si ya existe en el caché
        if (iconCache.containsKey(ikonName)) {
            return iconCache.get(ikonName);
        }

        org.kordamp.ikonli.swing.FontIcon icon = new org.kordamp.ikonli.swing.FontIcon();

        // Asignar el ikon basado en el nombre
        boolean ikonAsignado = true;

        try {
            switch (ikonName) {
                // Menú Archivo
                case "FILE_DOCUMENT_OUTLINE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_DOCUMENT_OUTLINE);
                    break;
                case "FILE_PLUS_OUTLINE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignF.FILE_PLUS_OUTLINE);
                    break;
                case "CONTENT_SAVE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CONTENT_SAVE);
                    break;
                case "DELETE_OUTLINE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignD.DELETE_OUTLINE);
                    break;
                case "MAGNIFY":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignM.MAGNIFY);
                    break;
                case "PRINTER":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PRINTER);
                    break;
                case "WINDOW_CLOSE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignW.WINDOW_CLOSE);
                    break;
                case "EXIT_TO_APP":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignE.EXIT_TO_APP);
                    break;

                // Menú Edición
                case "PENCIL":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PENCIL);
                    break;
                case "PAGE_FIRST":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PAGE_FIRST);
                    break;
                case "CHEVRON_LEFT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHEVRON_LEFT);
                    break;
                case "CHEVRON_RIGHT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHEVRON_RIGHT);
                    break;
                case "PAGE_LAST":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PAGE_LAST);
                    break;
                case "PLAYLIST_PLUS":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_PLUS);
                    break;
                case "PLAYLIST_MINUS":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PLAYLIST_MINUS);
                    break;

                // Menú Compras
                case "CART":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CART);
                    break;
                case "ACCOUNT_GROUP":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_GROUP);
                    break;
                case "CART_PLUS":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CART_PLUS);
                    break;
                case "CLIPBOARD_TEXT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CLIPBOARD_TEXT);
                    break;

                // Menú Ventas
                case "STORE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.STORE);
                    break;
                case "ACCOUNT_MULTIPLE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_MULTIPLE);
                    break;
                case "BOOK_OPEN_PAGE_VARIANT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignB.BOOK_OPEN_PAGE_VARIANT);
                    break;
                case "CASH_REGISTER":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_REGISTER);
                    break;
                case "CHART_LINE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHART_LINE);
                    break;
                case "STOREFRONT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.STOREFRONT);
                    break;
                case "STAR":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.STAR);
                    break;
                case "FLASH":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignF.FLASH);
                    break;
                case "RECEIPT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignR.RECEIPT);
                    break;

                // Menú Stock
                case "PACKAGE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PACKAGE);
                    break;
                case "PACKAGE_VARIANT_CLOSED":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignP.PACKAGE_VARIANT_CLOSED);
                    break;
                case "TAG_MULTIPLE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignT.TAG_MULTIPLE);
                    break;
                case "CLIPBOARD_CHECK":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CLIPBOARD_CHECK);
                case "CLIPBOARD_CHECK_OUTLINE":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CLIPBOARD_CHECK_OUTLINE);
                    break;
                case "BARCODE_SCAN":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignB.BARCODE_SCAN);
                    break;

                // Menú Tesorería
                case "CASH":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH);
                    break;
                case "ARRANGE_SEND_BACKWARD":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ARRANGE_SEND_BACKWARD);
                    break;
                case "CASH_PLUS":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_PLUS);
                    break;
                case "CASH_MINUS":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CASH_MINUS);
                    break;
                case "CHART_BAR":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignC.CHART_BAR);
                    break;

                // Menú Seguridad
                case "SHIELD":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.SHIELD);
                    break;
                case "ACCOUNT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT);
                    break;
                case "ACCOUNT_KEY":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignA.ACCOUNT_KEY);
                    break;
                case "SHIELD_ACCOUNT":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignS.SHIELD_ACCOUNT);
                    break;
                case "KEY":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignK.KEY);
                    break;
                case "MENU":
                    icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignM.MENU);
                    break;

                default:
                    // Ikon por defecto para nombres no reconocidos
                    try {
                        icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignH.HELP_CIRCLE_OUTLINE);
                        System.out.println("Advertencia: Ikon no reconocido: " + ikonName + ". Usando ikon por defecto.");
                    } catch (Exception ex) {
                        // Si incluso el ikon por defecto falla, no usar iconos
                        System.err.println("Error crítico con iconos: " + ex.getMessage());
                        return null; // Retorna null en lugar de un icono roto
                    }
                    ikonAsignado = false;
                    break;
            }
        } catch (Exception e) {
            // En caso de error al asignar un ikon, usar uno por defecto
            System.err.println("Error al asignar ikon '" + ikonName + "': " + e.getMessage());
            try {
                icon.setIkon(org.kordamp.ikonli.materialdesign2.MaterialDesignH.HELP_CIRCLE_OUTLINE);
            } catch (Exception ex) {
                // Si incluso el ikon por defecto falla, no asignar ninguno
                System.err.println("Error crítico: No se pudo asignar el ikon por defecto. " + ex.getMessage());
                return null; // No cachear iconos inválidos
            }
        }

        // Establecer tamaño y color solo si se asignó correctamente un ikon
        icon.setIconSize(size);
        icon.setIconColor(color);

        // Cachear el icono solo si se asignó correctamente
        if (ikonAsignado) {
            iconCache.put(ikonName, icon);
        }

        return icon;
    }

    // Configura todos los iconos de menú de manera eficiente usando iconos cacheados
    private void configurarTodosLosIconos() {
        // Solo configurar iconos si no han sido configurados todavía
        if (iconCache == null) {
            iconCache = new HashMap<>();
        }

        Color menuIconColor = java.awt.Color.LIGHT_GRAY;
        int iconSize = 16;

        // Menús principales
        setIconSafely(mArchivo, "FILE_DOCUMENT_OUTLINE", iconSize, menuIconColor);
        setIconSafely(mEdicion, "PENCIL", iconSize, menuIconColor);
        setIconSafely(mCompras, "CART", iconSize, menuIconColor);
        setIconSafely(mVentas, "STORE", iconSize, menuIconColor);
        setIconSafely(mStock, "PACKAGE", iconSize, menuIconColor);
        setIconSafely(mTesoreria, "CASH", iconSize, menuIconColor);
        setIconSafely(mSeguridad, "SHIELD", iconSize, menuIconColor);

        // Menú Archivo
        setIconSafely(mNuevo, "FILE_PLUS_OUTLINE", iconSize, menuIconColor);
        setIconSafely(mGuardar, "CONTENT_SAVE", iconSize, menuIconColor);
        setIconSafely(mBorrar, "DELETE_OUTLINE", iconSize, menuIconColor);
        setIconSafely(mBuscar, "MAGNIFY", iconSize, menuIconColor);
        setIconSafely(mImprimir, "PRINTER", iconSize, menuIconColor);
        setIconSafely(mCerrarVentana, "WINDOW_CLOSE", iconSize, menuIconColor);
        setIconSafely(mSalir, "EXIT_TO_APP", iconSize, menuIconColor);

        // Menú Edición
        setIconSafely(mPrimero, "PAGE_FIRST", iconSize, menuIconColor);
        setIconSafely(mAnterior, "CHEVRON_LEFT", iconSize, menuIconColor);
        setIconSafely(mSiguiente, "CHEVRON_RIGHT", iconSize, menuIconColor);
        setIconSafely(mUltimo, "PAGE_LAST", iconSize, menuIconColor);
        setIconSafely(mInsDetalle, "PLAYLIST_PLUS", iconSize, menuIconColor);
        setIconSafely(mDelDetalle, "PLAYLIST_MINUS", iconSize, menuIconColor);

        // Menú Compras
        setIconSafely(mProveedores, "ACCOUNT_GROUP", iconSize, menuIconColor);
        setIconSafely(mRegCompras, "CART_PLUS", iconSize, menuIconColor);
        setIconSafely(mRepCompras, "CLIPBOARD_TEXT", iconSize, menuIconColor);

        // Menú Ventas
        setIconSafely(mClientes, "ACCOUNT_MULTIPLE", iconSize, menuIconColor);
        setIconSafely(mTalonarios, "BOOK_OPEN_PAGE_VARIANT", iconSize, menuIconColor);
        setIconSafely(mRegVentaDirecta, "STOREFRONT", iconSize, menuIconColor);
        setIconSafely(mRegVentas, "CASH_REGISTER", iconSize, menuIconColor);
        setIconSafely(mRepVentas, "CHART_LINE", iconSize, menuIconColor);
        setIconSafely(mRepProductosMasVendidos, "STAR", iconSize, menuIconColor);

        // Menú Stock
        setIconSafely(mProductos, "PACKAGE_VARIANT_CLOSED", iconSize, menuIconColor);
        setIconSafely(mListaPrecios, "TAG_MULTIPLE", iconSize, menuIconColor);
        setIconSafely(mAjustarStock, "CLIPBOARD_CHECK", iconSize, menuIconColor);
        setIconSafely(mAprobarStock, "CLIPBOARD_CHECK_OUTLINE", iconSize, menuIconColor);
        setIconSafely(mRepInvent, "BARCODE_SCAN", iconSize, menuIconColor);

        // Menú Tesorería
        setIconSafely(mAperturaCierreCaja, "ARRANGE_SEND_BACKWARD", iconSize, menuIconColor);
        setIconSafely(mIngCaja, "CASH_PLUS", iconSize, menuIconColor);
        setIconSafely(mRepCaja, "CHART_BAR", iconSize, menuIconColor);

        // Menú Seguridad
        setIconSafely(mPersonas, "ACCOUNT", iconSize, menuIconColor);
        setIconSafely(mUsuarios, "ACCOUNT_KEY", iconSize, menuIconColor);
        setIconSafely(mRoles, "SHIELD_ACCOUNT", iconSize, menuIconColor);
        setIconSafely(mPermisos, "KEY", iconSize, menuIconColor);
        setIconSafely(mMenus, "MENU", iconSize, menuIconColor);

        // Validar que todos los iconos se hayan asignado correctamente
        validarIconos();
    }

    /**
     * Método auxiliar para asignar un icono de manera segura a un componente
     */
    private void setIconSafely(JMenuItem menuItem, String ikonName, int size, java.awt.Color color) {
        if (menuItem == null) {
            return;
        }

        try {
            org.kordamp.ikonli.swing.FontIcon icon = getOrCreateIcon(ikonName, size, color);
            if (icon != null) {
                menuItem.setIcon(icon);
            } else {
                // Si el icono es null, no establecer ningún icono (mejor sin icono que con error)
                System.out.println("No se asignó icono a " + menuItem.getText() + " - funcionará sin icono");
            }
        } catch (Exception e) {
            System.err.println("Error al asignar icono a " + menuItem.getText() + ": " + e.getMessage());
            // No asignar ningún icono si hay error
        }
    }

    /**
     * Valida que todos los iconos asignados sean válidos Útil para ejecutar
     * después de la inicialización para verificar que todo está correcto
     */
    private void validarIconos() {
        JMenuItem[] items = {
            // Menús principales
            mArchivo, mEdicion, mCompras, mVentas, mStock, mTesoreria, mSeguridad,
            // Menú Archivo
            mNuevo, mGuardar, mBorrar, mBuscar, mImprimir, mCerrarVentana, mSalir,
            // Menú Edición
            mPrimero, mAnterior, mSiguiente, mUltimo, mInsDetalle, mDelDetalle,
            // Menú Compras
            mProveedores, mRegCompras, mRepCompras,
            // Menú Ventas
            mClientes, mTalonarios, mRegVentas, mRepVentas, mRepProductosMasVendidos,
            // Menú Stock
            mProductos, mAjustarStock, mListaPrecios, mRepInvent,
            // Menú Tesorería
            mAperturaCierreCaja, mIngCaja, mRepCaja,
            // Menú Seguridad
            mPersonas, mUsuarios, mRoles, mPermisos, mMenus
        };

        System.out.println("Verificando " + items.length + " iconos de menú...");

        for (JMenuItem item : items) {
            if (item == null) {
                System.err.println("ADVERTENCIA: Elemento de menú nulo encontrado en la validación.");
                continue;
            }

            Icon icon = item.getIcon();
            if (icon == null) {
                System.out.println("ADVERTENCIA: " + item.getText() + " no tiene icono asignado.");
            } else if (icon instanceof org.kordamp.ikonli.swing.FontIcon) {
                org.kordamp.ikonli.swing.FontIcon fontIcon = (org.kordamp.ikonli.swing.FontIcon) icon;
                if (fontIcon.getIkon() == null) {
                    System.err.println("ERROR: " + item.getText() + " tiene un FontIcon con ikon nulo.");
                } else {
                    // Todo está correcto
                    // System.out.println("OK: " + item.getText() + " tiene icono válido.");
                }
            } else {
                System.out.println("INFORMACIÓN: " + item.getText() + " usa un icono que no es FontIcon.");
            }
        }

        System.out.println("Validación de iconos completada.");
    }

    /**
     * Configura los listeners de los componentes de la interfaz de usuario.
     * Este método asegura que cada componente tenga exactamente un listener,
     * evitando la duplicación de acciones.
     */
    private void configurarListeners() {
        // Almacena referencias a los listeners para facilitar su gestión
        Map<JMenuItem, ActionListener> menuListeners = new HashMap<>();

        // Crear y almacenar los listeners para cada elemento del menú
        menuListeners.put(mBuscar, e -> mBuscarActionPerformed(e));
        menuListeners.put(mGuardar, e -> mGuardarActionPerformed(e));
        menuListeners.put(mBorrar, e -> mBorrarActionPerformed(e));
        menuListeners.put(mCerrarVentana, e -> mCerrarVentanaActionPerformed(e));
        menuListeners.put(mSalir, e -> mSalirActionPerformed(e));
        menuListeners.put(mPrimero, e -> mPrimeroActionPerformed(e));
        menuListeners.put(mAnterior, e -> mAnteriorActionPerformed(e));
        menuListeners.put(mSiguiente, e -> mSiguienteActionPerformed(e));
        menuListeners.put(mUltimo, e -> mUltimoActionPerformed(e));

        menuListeners.put(mInsDetalle, e -> mInsDetalleActionPerformed(e));
        menuListeners.put(mDelDetalle, e -> mDelDetalleActionPerformed(e));

        // Y así sucesivamente para todos los elementos del menú que necesiten listeners
        // Aplicar los listeners, eliminando primero cualquier listener existente
        for (Map.Entry<JMenuItem, ActionListener> entry : menuListeners.entrySet()) {
            JMenuItem menuItem = entry.getKey();
            ActionListener listener = entry.getValue();

            // Eliminar listeners existentes
            for (ActionListener existingListener : menuItem.getActionListeners()) {
                menuItem.removeActionListener(existingListener);
            }

            // Agregar el nuevo listener
            menuItem.addActionListener(listener);
        }
    }

    /**
     * Busca una ventana abierta del tipo especificado
     *
     * @param <T> Tipo de ventana a buscar (debe extender JInternalFrame)
     * @param windowClass Clase de la ventana a buscar
     * @return La ventana abierta del tipo especificado o null si no existe
     */
    private <T extends JInternalFrame> T findOpenWindow(Class<T> windowClass) {
        JInternalFrame[] frames = jDesktopPane2.getAllFrames();
        for (JInternalFrame frame : frames) {
            if (windowClass.isInstance(frame)) {
                return windowClass.cast(frame);
            }
        }
        return null;
    }

    /**
     * Busca una ventana de reporte por nombre de reporte
     *
     * @param nombreReporte Nombre del reporte a buscar
     * @return La ventana de reporte o null si no existe
     */
    private vReport findOpenReport(String nombreReporte) {
        JInternalFrame[] frames = jDesktopPane2.getAllFrames();
        for (JInternalFrame frame : frames) {
            if (frame instanceof vReport && frame.getTitle().contains(nombreReporte)) {
                return (vReport) frame;
            }
        }
        return null;
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

    /**
     * Método genérico para abrir cualquier tipo de ventana interna
     *
     * @param <T> Tipo de ventana a abrir
     * @param windowSupplier Proveedor de la ventana (función que crea la
     * instancia)
     * @param windowClass Clase de la ventana
     * @param title Título de la ventana
     * @param setupFunction Función opcional para configurar la ventana (puede
     * ser null)
     * @return La ventana abierta
     */
    private <T extends JInternalFrame & myInterface> T abrirVentanaGenerica(
            Supplier<T> windowSupplier,
            Class<T> windowClass,
            String title,
            Consumer<T> setupFunction) {

        try {
            // Buscar ventana existente
            T existingWindow = findOpenWindow(windowClass);

            if (existingWindow != null) {
                // Si ya existe, activarla
                existingWindow.toFront();
                existingWindow.setSelected(true);
                if (existingWindow.isIcon()) {
                    existingWindow.setIcon(false);
                }
                return existingWindow;
            } else {
                // Si no existe, crear una nueva
                T newWindow = windowSupplier.get();
                newWindow.setTitle(title);

                // Configurar la ventana si se proporciona una función de configuración
                if (setupFunction != null) {
                    setupFunction.accept(newWindow);
                }

                // Mostrar la ventana
                jDesktopPane2.add(newWindow);
                centrar(newWindow);
                newWindow.setVisible(true);
                newWindow.setSelected(true);

                // Agregar un listener para limpiar recursos cuando se cierra la ventana
                newWindow.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosed(InternalFrameEvent e) {
                        // Limpiar cualquier recurso asociado con esta ventana
                        // Por ejemplo, podría almacenar referencias a controladores en un mapa
                        // y eliminarlos cuando se cierra la ventana
                        limpiarRecursos(newWindow);
                    }
                });

                return newWindow;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir ventana: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }

    // Mapa para almacenar referencias a los controladores por ventana
    private Map<JInternalFrame, Object> controladores = new HashMap<>();

    /**
     * Registra un controlador asociado a una ventana
     */
    private void registrarControlador(JInternalFrame ventana, Object controlador) {
        controladores.put(ventana, controlador);
    }

    /**
     * Método para limpiar recursos asociados con una ventana cerrada
     */
    private void limpiarRecursos(JInternalFrame ventana) {
        Object controlador = controladores.remove(ventana);
        if (controlador != null) {
            // Si el controlador implementa alguna interfaz de "disposable", llamar a su método de limpieza
            if (controlador instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) controlador).close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar controlador: " + e.getMessage());
                }
            }
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
        mRegVentaDirecta = new javax.swing.JMenuItem();
        mRegVentas = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mRepVentas = new javax.swing.JMenuItem();
        mRepProductosMasVendidos = new javax.swing.JMenuItem();
        mStock = new javax.swing.JMenu();
        mProductos = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mListaPrecios = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mAjustarStock = new javax.swing.JMenuItem();
        mAprobarStock = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        mRepInvent = new javax.swing.JMenuItem();
        mTesoreria = new javax.swing.JMenu();
        mAperturaCierreCaja = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mIngCaja = new javax.swing.JMenuItem();
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
        mNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mNuevoActionPerformed(evt);
            }
        });
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
        mImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mImprimirActionPerformed(evt);
            }
        });
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

        mAnterior.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        mAnterior.setMnemonic('y');
        mAnterior.setText("Anterior");
        mAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAnteriorActionPerformed(evt);
            }
        });
        mEdicion.add(mAnterior);

        mSiguiente.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
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
        mRepCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRepComprasActionPerformed(evt);
            }
        });
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

        mRegVentaDirecta.setText("Registrar Venta Directa");
        mRegVentaDirecta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRegVentaDirectaActionPerformed(evt);
            }
        });
        mVentas.add(mRegVentaDirecta);

        mRegVentas.setText("Registrar Ventas");
        mRegVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRegVentasActionPerformed(evt);
            }
        });
        mVentas.add(mRegVentas);
        mVentas.add(jSeparator4);

        mRepVentas.setText("Reporte Ventas");
        mRepVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRepVentasActionPerformed(evt);
            }
        });
        mVentas.add(mRepVentas);

        mRepProductosMasVendidos.setText("Reporte Productos Mas Vendidos");
        mRepProductosMasVendidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRepProductosMasVendidosActionPerformed(evt);
            }
        });
        mVentas.add(mRepProductosMasVendidos);

        menuBar.add(mVentas);

        mStock.setText("Stock");

        mProductos.setText("Gestionar Productos");
        mProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mProductosActionPerformed(evt);
            }
        });
        mStock.add(mProductos);
        mStock.add(jSeparator5);

        mListaPrecios.setText("Lista de Precios");
        mListaPrecios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mListaPreciosActionPerformed(evt);
            }
        });
        mStock.add(mListaPrecios);
        mStock.add(jSeparator6);

        mAjustarStock.setText("Ajustar Stock");
        mAjustarStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAjustarStockActionPerformed(evt);
            }
        });
        mStock.add(mAjustarStock);

        mAprobarStock.setText("Aprobar Ajuste de Stock");
        mAprobarStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAprobarStockActionPerformed(evt);
            }
        });
        mStock.add(mAprobarStock);
        mStock.add(jSeparator13);

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

        mIngCaja.setText("Registrar Movimiento de Caja");
        mIngCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mIngCajaActionPerformed(evt);
            }
        });
        mTesoreria.add(mIngCaja);
        mTesoreria.add(jSeparator8);

        mRepCaja.setText("Reporte de Ingresos - Egresos");
        mRepCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRepCajaActionPerformed(evt);
            }
        });
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
        mMenus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mMenusActionPerformed(evt);
            }
        });
        mSeguridad.add(mMenus);

        menuBar.add(mSeguridad);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Método para manejar el evento de salir
    private void mSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSalirActionPerformed
        int eleccion = JOptionPane.showConfirmDialog(null, "¿Desea salir del Sistema?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (eleccion == JOptionPane.YES_OPTION) {
            // ✅ AGREGAR LOG ANTES DE SALIR
            LogManager.getInstance().logLogin("LOGOUT",
                    "Usuario " + vLogin.getUsuarioAutenticado() + " cerró la aplicación desde menú");
            LogManager.getInstance().cerrarSesion();

            System.exit(0);
        }
    }//GEN-LAST:event_mSalirActionPerformed

    private void mPersonasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPersonasActionPerformed
        abrirVentanaGenerica(
                vPersonas::new,
                vPersonas.class,
                "Personas",
                null
        );
    }//GEN-LAST:event_mPersonasActionPerformed

    private void mUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mUsuariosActionPerformed
        abrirVentanaGenerica(
                vUsuarios::new,
                vUsuarios.class,
                "Usuarios",
                null
        );
    }//GEN-LAST:event_mUsuariosActionPerformed

    private void mGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGuardarActionPerformed
        System.out.println("DEBUG: mGuardarActionPerformed ejecutado");

        JInternalFrame v = jDesktopPane2.getSelectedFrame();
        System.out.println("DEBUG: getSelectedFrame() = " + v);

        if (v != null) {
            System.out.println("DEBUG: Ventana activa: " + v.getClass().getSimpleName());
            System.out.println("DEBUG: Es instancia de myInterface: " + (v instanceof myInterface));
            System.out.println("DEBUG: Título ventana: " + v.getTitle());
        }

        myInterface ve = getCurrentWindow();
        System.out.println("DEBUG: getCurrentWindow() devolvió: " + ve);

        if (ve == null) {
            System.out.println("DEBUG: ve es null, retornando");
            return;
        }

        System.out.println("DEBUG: Llamando ve.imGrabar()");
        ve.imGrabar();
    }//GEN-LAST:event_mGuardarActionPerformed

    private void mRolesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRolesActionPerformed
        abrirVentanaGenerica(
                vRoles::new,
                vRoles.class,
                "Roles",
                null
        );
    }//GEN-LAST:event_mRolesActionPerformed

    private void mPermisosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mPermisosActionPerformed
        abrirVentanaGenerica(
                vPermisos::new,
                vPermisos.class,
                "Permisos",
                null
        );
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
        abrirVentanaGenerica(
                () -> {
                    try {
                        return new vGestProd();
                    } catch (SQLException ex) {
                        throw new RuntimeException("Error al crear ventana de productos: " + ex.getMessage(), ex);
                    }
                },
                vGestProd.class,
                "Gestión de Productos",
                window -> {
                    try {
                        // Configurar el controlador
                        cGestProd controlador = new cGestProd(window);

                        // Configurar eventos específicos
                        window.getTxtProdId().addActionListener(e -> {
                            String idText = window.getTxtProdId().getText().trim();
                            try {
                                if (!idText.isEmpty()) {
                                    int id = Integer.parseInt(idText);
                                    controlador.buscarProductoPorId(id);
                                } else {
                                    window.mostrarError("El campo ID no puede estar vacío");
                                    window.getTxtProdId().requestFocus();
                                }
                            } catch (NumberFormatException ex) {
                                window.mostrarError("El ID debe ser un número entero válido");
                                window.getTxtProdId().requestFocus();
                                window.getTxtProdId().selectAll();
                            }
                        });

                        window.getTxtIva().setText("10.00");
                        window.getTxtProdId().requestFocus();

                        // Podríamos almacenar el controlador en un mapa para limpiarlo después
                        // controladores.put(window, controlador);
                    } catch (SQLException ex) {
                        throw new RuntimeException("Error al configurar controlador: " + ex.getMessage(), ex);
                    }
                }
        );
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
        abrirVentanaGenerica(
                () -> {
                    vReport reporte = new vReport("inventario_productos", "filtroInventario");
                    // Configurar tamaño
                    int maxWidth = (int) (jDesktopPane2.getWidth() * 0.9);
                    int maxHeight = (int) (jDesktopPane2.getHeight() * 0.9);
                    double aspectRatio = 1.53;
                    int width, height;

                    if (maxWidth * aspectRatio <= maxHeight) {
                        width = maxWidth;
                        height = (int) (width * aspectRatio);
                    } else {
                        height = maxHeight;
                        width = (int) (height / aspectRatio);
                    }

                    reporte.setSize(width, height);
                    reporte.setPreferredSize(new Dimension(width, height));
                    return reporte;
                },
                vReport.class,
                "Reporte de Inventario",
                vReport::imFiltrar
        );
    }//GEN-LAST:event_mRepInventActionPerformed

    /**
     * Método refactorizado para mostrar un reporte en el JDesktopPane
     *
     * @param nombreReporte Nombre del reporte a mostrar
     * @param filtro Filtro a aplicar al reporte
     */
    private void mostrarReporteRefactorizado(String nombreReporte, String filtro) {
        try {
            abrirVentanaGenerica(
                    () -> {
                        vReport reporte = new vReport(nombreReporte, filtro);

                        // Configurar tamaño
                        int maxWidth = (int) (jDesktopPane2.getWidth() * 0.9);
                        int maxHeight = (int) (jDesktopPane2.getHeight() * 0.9);
                        double aspectRatio = 1.53;
                        int width, height;

                        if (maxWidth * aspectRatio <= maxHeight) {
                            width = maxWidth;
                            height = (int) (width * aspectRatio);
                        } else {
                            height = maxHeight;
                            width = (int) (height / aspectRatio);
                        }

                        reporte.setSize(width, height);
                        reporte.setPreferredSize(new Dimension(width, height));

                        return reporte;
                    },
                    vReport.class,
                    "Reporte: " + nombreReporte,
                    vReport::imFiltrar
            );
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
            abrirVentanaGenerica(
                    () -> {
                        try {
                            return new vProveedores();
                        } catch (SQLException ex) {
                            throw new RuntimeException("Error al crear ventana de proveedores", ex);
                        }
                    },
                    vProveedores.class,
                    "Gestión de Proveedores",
                    null
            );
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof SQLException) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos:\n" + ex.getCause().getMessage(),
                        "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error inesperado al abrir gestión de proveedores:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_mProveedoresActionPerformed

    private void mRegComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRegComprasActionPerformed
        abrirVentanaGenerica(
                vRegCompras::new,
                vRegCompras.class,
                "Registrar Compra",
                null
        );
    }//GEN-LAST:event_mRegComprasActionPerformed

    private void mClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mClientesActionPerformed
        try {
            abrirVentanaGenerica(
                    () -> {
                        try {
                            return new vClientes();
                        } catch (SQLException ex) {
                            throw new RuntimeException("Error al crear ventana de clientes", ex);
                        }
                    },
                    vClientes.class,
                    "Gestión de Clientes",
                    null
            );
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof SQLException) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos:\n" + ex.getCause().getMessage(),
                        "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error inesperado al abrir gestión de clientes:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_mClientesActionPerformed

    private void mRegVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRegVentasActionPerformed
        abrirVentanaGenerica(
                vSeleccionMesa::new,
                vSeleccionMesa.class,
                "Selección de Mesas",
                null
        );
    }//GEN-LAST:event_mRegVentasActionPerformed

    private void mAperturaCierreCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAperturaCierreCajaActionPerformed
        abrirVentanaGenerica(
                () -> new vAperturaCierreCaja(vLogin.getUsuarioAutenticado()),
                vAperturaCierreCaja.class,
                "Apertura / Cierre de Caja",
                null
        );
    }//GEN-LAST:event_mAperturaCierreCajaActionPerformed

    private void mTalonariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mTalonariosActionPerformed
        abrirVentanaGenerica(
                vTalonarios::new,
                vTalonarios.class,
                "Gestión de Talonarios",
                null
        );
    }//GEN-LAST:event_mTalonariosActionPerformed

    private void mIngCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mIngCajaActionPerformed
        try {
            if (ventanaMovimientoCaja == null || ventanaMovimientoCaja.isClosed()) {
                ventanaMovimientoCaja = new vMovimientoCaja("INGRESO");
                jDesktopPane2.add(ventanaMovimientoCaja);
                centrar(ventanaMovimientoCaja);
            } else {
                // Si ya existe, cambiar el tipo a INGRESO
                ventanaMovimientoCaja.getCmbTipo().setSelectedItem("INGRESO");
            }
            ventanaMovimientoCaja.setVisible(true);
            ventanaMovimientoCaja.toFront();
            ventanaMovimientoCaja.setSelected(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir ventana de ingreso: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mIngCajaActionPerformed

    private void mListaPreciosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mListaPreciosActionPerformed
        abrirVentanaGenerica(
                vListaPrecios::new,
                vListaPrecios.class,
                "Gestión de Listas de Precio",
                null
        );
    }//GEN-LAST:event_mListaPreciosActionPerformed

    private void mRegVentaDirectaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRegVentaDirectaActionPerformed
        try {
            abrirVentanaGenerica(
                    () -> {
                        return new vRegVentas(); // Sin try-catch aquí
                    },
                    vRegVentas.class,
                    "Registro de Ventas",
                    (window) -> {
                        // Configuraciones adicionales si son necesarias
                        window.getTxtCodigoBarra().requestFocus();
                    }
            );
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al abrir registro de ventas:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mRegVentaDirectaActionPerformed

    private void mAjustarStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAjustarStockActionPerformed
        try {
            abrirVentanaGenerica(
                    () -> {
                        return new vAjusteStock(); // Sin try-catch aquí
                    },
                    vAjusteStock.class,
                    "Ajuste de Stock",
                    (window) -> {
                        // Configuraciones adicionales al abrir la ventana
                        window.enfocarCodigoBarra(); // Enfocar en el campo de código de barras
                    }
            );
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir ventana de ajuste de stock:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mAjustarStockActionPerformed

    private void mAprobarStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAprobarStockActionPerformed
        try {
            abrirVentanaGenerica(
                    () -> {
                        try {
                            return new vAprobacionAjuste();
                        } catch (Exception ex) {
                            throw new RuntimeException("Error al inicializar ventana de aprobación de ajustes: " + ex.getMessage(), ex);
                        }
                    },
                    vAprobacionAjuste.class,
                    "Aprobar/Desaprobar Ajustes de Stock",
                    (window) -> {
                        // Configuraciones adicionales al abrir la ventana
                        window.enfocarFiltros(); // Enfocar en los campos de filtro

                        // Mostrar mensaje informativo
                        SwingUtilities.invokeLater(() -> {
                            window.mostrarMensaje("Ventana de aprobación iniciada.\n"
                                    + "• Use los filtros para buscar ajustes\n"
                                    + "• Marque/desmarque para aprobar/desaprobar\n"
                                    + "• Doble clic para ver detalles del ajuste");
                        });
                    }
            );
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir ventana de aprobación de ajustes:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_mAprobarStockActionPerformed

    private void mMenusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mMenusActionPerformed
        abrirVentanaGenerica(
                vMenus::new,
                vMenus.class,
                "Sincronización de Menús",
                null
        );
    }//GEN-LAST:event_mMenusActionPerformed

    private void mRepComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRepComprasActionPerformed
        abrirVentanaGenerica(
                () -> {
                    vReport reporte = new vReport("reporte_compras", "filtroCompras");
                    // Configurar tamaño
                    int maxWidth = (int) (jDesktopPane2.getWidth() * 0.9);
                    int maxHeight = (int) (jDesktopPane2.getHeight() * 0.9);
                    double aspectRatio = 1.53;
                    int width, height;

                    if (maxWidth * aspectRatio <= maxHeight) {
                        width = maxWidth;
                        height = (int) (width * aspectRatio);
                    } else {
                        height = maxHeight;
                        width = (int) (height / aspectRatio);
                    }

                    reporte.setSize(width, height);
                    reporte.setPreferredSize(new Dimension(width, height));
                    return reporte;
                },
                vReport.class,
                "Reporte de Compras",
                vReport::imFiltrar
        );
    }//GEN-LAST:event_mRepComprasActionPerformed

    private void mRepVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRepVentasActionPerformed
        abrirVentanaGenerica(
                () -> {
                    vReport reporte = new vReport("reporte_ventas", "filtroVentas");
                    // Configurar tamaño
                    int maxWidth = (int) (jDesktopPane2.getWidth() * 0.9);
                    int maxHeight = (int) (jDesktopPane2.getHeight() * 0.9);
                    double aspectRatio = 1.53;
                    int width, height;

                    if (maxWidth * aspectRatio <= maxHeight) {
                        width = maxWidth;
                        height = (int) (width * aspectRatio);
                    } else {
                        height = maxHeight;
                        width = (int) (height / aspectRatio);
                    }

                    reporte.setSize(width, height);
                    reporte.setPreferredSize(new Dimension(width, height));
                    return reporte;
                },
                vReport.class,
                "Reporte de Ventas",
                vReport::imFiltrar
        );
    }//GEN-LAST:event_mRepVentasActionPerformed

    private void mRepCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRepCajaActionPerformed
        mostrarReporteRefactorizado("ingresos_egresos", "filtroIngresosEgresos");
    }//GEN-LAST:event_mRepCajaActionPerformed

    private void mRepProductosMasVendidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRepProductosMasVendidosActionPerformed
        abrirVentanaGenerica(
                () -> {
                    vReport reporte = new vReport("productos_mas_vendidos", "filtroProductosMasVendidos");
                    // Configurar tamaño
                    int maxWidth = (int) (jDesktopPane2.getWidth() * 0.9);
                    int maxHeight = (int) (jDesktopPane2.getHeight() * 0.9);
                    double aspectRatio = 1.53;
                    int width, height;

                    if (maxWidth * aspectRatio <= maxHeight) {
                        width = maxWidth;
                        height = (int) (width * aspectRatio);
                    } else {
                        height = maxHeight;
                        width = (int) (height / aspectRatio);
                    }

                    reporte.setSize(width, height);
                    reporte.setPreferredSize(new Dimension(width, height));
                    return reporte;
                },
                vReport.class,
                "Reporte de Productos Más Vendidos",
                vReport::imFiltrar
        );
    }//GEN-LAST:event_mRepProductosMasVendidosActionPerformed

    private void mImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mImprimirActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imImprimir(); // Llama al método imImprimir de la ventana activa
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay ninguna ventana activa para imprimir.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_mImprimirActionPerformed

    private void mNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mNuevoActionPerformed
        myInterface ventanaActiva = getCurrentWindow();
        if (ventanaActiva != null) {
            ventanaActiva.imNuevo(); // Llama al método imNuevo de la ventana activa
        }
    }//GEN-LAST:event_mNuevoActionPerformed

    public JDesktopPane getDesktopPane() {
        return jDesktopPane2;
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
    private javax.swing.JPopupMenu.Separator jSeparator13;
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
    private javax.swing.JMenuItem mAprobarStock;
    private javax.swing.JMenu mArchivo;
    private javax.swing.JMenuItem mBorrar;
    private javax.swing.JMenuItem mBuscar;
    private javax.swing.JMenuItem mCerrarVentana;
    private javax.swing.JMenuItem mClientes;
    private javax.swing.JMenu mCompras;
    private javax.swing.JMenuItem mDelDetalle;
    private javax.swing.JMenu mEdicion;
    private javax.swing.JMenuItem mGuardar;
    private javax.swing.JMenuItem mImprimir;
    private javax.swing.JMenuItem mIngCaja;
    private javax.swing.JMenuItem mInsDetalle;
    private javax.swing.JMenuItem mListaPrecios;
    private javax.swing.JMenuItem mMenus;
    private javax.swing.JMenuItem mNuevo;
    private javax.swing.JMenuItem mPermisos;
    private javax.swing.JMenuItem mPersonas;
    private javax.swing.JMenuItem mPrimero;
    private javax.swing.JMenuItem mProductos;
    private javax.swing.JMenuItem mProveedores;
    private javax.swing.JMenuItem mRegCompras;
    private javax.swing.JMenuItem mRegVentaDirecta;
    private javax.swing.JMenuItem mRegVentas;
    private javax.swing.JMenuItem mRepCaja;
    private javax.swing.JMenuItem mRepCompras;
    private javax.swing.JMenuItem mRepInvent;
    private javax.swing.JMenuItem mRepProductosMasVendidos;
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
