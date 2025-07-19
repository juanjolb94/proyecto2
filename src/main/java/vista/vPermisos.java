package vista;

import interfaces.myInterface;
import java.awt.Window;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import modelo.RolesDAO;
import controlador.PermisosController;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import modelo.mPermiso;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import util.MenusCache;

public class vPermisos extends javax.swing.JInternalFrame implements myInterface {

    private boolean permisosCambiados = false;
    private boolean cargandoDatos = false;
    private PermisosController controlador;

    public vPermisos() {
        initComponents();

        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);

        // Inicializar controlador
        this.controlador = new PermisosController(this);

        configurarComboBoxRoles();
        configurarTablaPermisos();
        configurarEventoComboBox();
        configurarListenersTabla();
        habilitarOrdenamientoTabla();
        cargarMenusIniciales();
    }

    // Método para cargar menús iniciales
    private void cargarMenusIniciales() {
        // ✅ VERIFICAR si los menús están sincronizados
        if (MenusCache.getInstance().isCacheValido()) {
            // Usar menús ya sincronizados desde vMenus
            List<mPermiso> menusSincronizados = MenusCache.getInstance().getMenusDelSistema();
            cargarMenusEnTablaDesdeCache(menusSincronizados);
        } else {
            // Si no están sincronizados, mostrar mensaje y abrir vMenus
            mostrarMensajeSincronizacion();
        }
    }

    private void mostrarMensajeSincronizacion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "Los menús no están sincronizados con el sistema de permisos.\n\n"
                + "¿Desea abrir la ventana de Menús para sincronizar?",
                "Sincronización Requerida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            abrirVentanaMenus();
        } else {
            // Fallback: usar menús desde interfaz pero mostrar advertencia
            List<mPermiso> menus = obtenerMenusDesdaInterfaz();
            cargarMenusEnTablaDesdeDB(menus);

            JOptionPane.showMessageDialog(this,
                    "ADVERTENCIA: Usando menús no sincronizados.\n"
                    + "Pueden ocurrir errores al guardar permisos.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void abrirVentanaMenus() {
        // Buscar si ya existe una ventana vMenus abierta
        vPrincipal ventanaPrincipal = obtenerVentanaPrincipal();
        if (ventanaPrincipal != null) {
            JDesktopPane desktop = ventanaPrincipal.getDesktopPane();
            if (desktop != null) {
                for (JInternalFrame frame : desktop.getAllFrames()) {
                    if (frame instanceof vMenus) {
                        frame.toFront();
                        try {
                            frame.setSelected(true);
                        } catch (java.beans.PropertyVetoException e) {
                            // Ignorar la excepción - no es crítica
                            System.err.println("No se pudo seleccionar la ventana: " + e.getMessage());
                        }
                        return;
                    }
                }
            }

            // Si no existe, crear nueva
            vMenus ventanaMenus = new vMenus();
            desktop.add(ventanaMenus);
            ventanaMenus.setVisible(true);
            ventanaMenus.toFront();

            // ✅ CORRECCIÓN: También aquí si usas setSelected
            try {
                ventanaMenus.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                // Ignorar la excepción - no es crítica
                System.err.println("No se pudo seleccionar la ventana nueva: " + e.getMessage());
            }

            JOptionPane.showMessageDialog(this,
                    "Use el botón 'Sincronizar' en la ventana de Menús.\n"
                    + "Luego cierre y vuelva a abrir esta ventana de Permisos.",
                    "Instrucciones",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método para configurar listeners en la tabla
    private void configurarListenersTabla() {
        jTable1.getModel().addTableModelListener(e -> {
            // ✅ IGNORAR cambios durante la carga de datos
            if (cargandoDatos) {
                return;
            }

            if (e.getColumn() >= 2 && e.getColumn() <= 6) {
                jTable1.getTableHeader().repaint();
                permisosCambiados = true; // Marcar que hay cambios
                actualizarTitulo();
            }
        });
    }

    private void actualizarTitulo() {
        String titulo = "Permisos";
        if (permisosCambiados) {
            titulo += " *"; // Asterisco indica cambios no guardados
        }
        setTitle(titulo);
    }

    // Método para cargar menús desde base de datos
    private void cargarMenusEnTablaDesdeDB(List<mPermiso> menus) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0);

        for (mPermiso menu : menus) {
            modelo.addRow(new Object[]{
                menu.getIdMenu(),
                menu.getNombreMenu(),
                false, // VER
                false, // CREATE
                false, // READ
                false, // UPDATE
                false // DELETE
            });
        }
    }

    // Método para habilitar ordenamiento de tabla
    private void habilitarOrdenamientoTabla() {
        jTable1.setAutoCreateRowSorter(true);

        // Configurar ordenamiento personalizado por columna
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter
                = new javax.swing.table.TableRowSorter<>((DefaultTableModel) jTable1.getModel());

        // Configurar comparador para la columna ID (ordenamiento numérico)
        sorter.setComparator(0, (Integer o1, Integer o2) -> o1.compareTo(o2));

        // Deshabilitar ordenamiento para las columnas de checkboxes
        sorter.setSortable(2, false); // VER
        sorter.setSortable(3, false); // CREATE
        sorter.setSortable(4, false); // READ
        sorter.setSortable(5, false); // UPDATE
        sorter.setSortable(6, false); // DELETE

        jTable1.setRowSorter(sorter);
    }

    // Método actualizado para cargar permisos del rol
    private void cargarPermisosDelRol(int idRol) {
        // ✅ ACTIVAR modo carga para evitar que se disparen los listeners
        cargandoDatos = true;

        try {
            // ✅ MODIFICACIÓN: SIEMPRE usar BD como fuente de verdad
            List<mPermiso> menus = controlador.obtenerMenusDelSistemaCompleto();
            cargarMenusEnTablaDesdeDB(menus);

            // Cargar permisos existentes del rol
            List<mPermiso> permisos = controlador.obtenerPermisosPorRol(idRol);
            aplicarPermisosEnTabla(permisos);

            // ✅ RESETEAR estado después de cargar todo
            permisosCambiados = false;
            actualizarTitulo();

        } finally {
            // ✅ DESACTIVAR modo carga
            cargandoDatos = false;
        }
    }

    // Variable para mostrar advertencia solo una vez
    private boolean advertenciaMostrada = false;

    // Método para aplicar permisos en la tabla
    private void aplicarPermisosEnTabla(List<mPermiso> permisos) {
        System.out.println("\n=== APLICANDO PERMISOS EN TABLA ===");
        System.out.println("Permisos a aplicar: " + permisos.size());

        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        System.out.println("Filas en tabla: " + modelo.getRowCount());

        int aplicados = 0;

        for (mPermiso permiso : permisos) {
            System.out.println("\nBuscando fila para: ID=" + permiso.getIdMenu()
                    + ", Menú='" + permiso.getNombreMenu() + "'");

            // Buscar la fila correspondiente al menú por ID o por nombre de componente
            boolean encontrado = false;
            for (int i = 0; i < modelo.getRowCount(); i++) {
                int idMenuTabla = (Integer) modelo.getValueAt(i, 0);
                String nombreMenuTabla = (String) modelo.getValueAt(i, 1);

                // ✅ MEJORAR: buscar por ID o por nombre si coincide
                boolean esElMismo = (idMenuTabla == permiso.getIdMenu())
                        || (nombreMenuTabla != null && nombreMenuTabla.equals(permiso.getNombreMenu()));

                if (esElMismo) {
                    System.out.println("→ ENCONTRADO en fila " + i + ": '" + nombreMenuTabla + "'");
                    System.out.println("  Aplicando: VER=" + permiso.isVer() + ", CREAR=" + permiso.isCrear()
                            + ", LEER=" + permiso.isLeer() + ", ACTUALIZAR=" + permiso.isActualizar()
                            + ", ELIMINAR=" + permiso.isEliminar());

                    modelo.setValueAt(permiso.isVer(), i, 2);
                    modelo.setValueAt(permiso.isCrear(), i, 3);
                    modelo.setValueAt(permiso.isLeer(), i, 4);
                    modelo.setValueAt(permiso.isActualizar(), i, 5);
                    modelo.setValueAt(permiso.isEliminar(), i, 6);
                    aplicados++;
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                System.out.println("→ NO ENCONTRADO menú: ID=" + permiso.getIdMenu()
                        + ", '" + permiso.getNombreMenu() + "'");
            }
        }

        System.out.println("Permisos aplicados en tabla: " + aplicados + "/" + permisos.size());
        System.out.println("=== FIN APLICAR PERMISOS ===\n");
    }

    // Usar nombres EXACTOS de la interfaz
    private String generarNombreComponente(String nombreMenu) {
        if (nombreMenu == null || nombreMenu.trim().isEmpty()) {
            return "";
        }

        // ✅ AGREGAR LOGS PARA DEBUG
        System.out.println("=== generarNombreComponente DEBUG ===");
        System.out.println("Nombre recibido: '" + nombreMenu + "'");
        System.out.println("Longitud: " + nombreMenu.length());

        // Mostrar cada carácter para detectar caracteres invisibles
        for (int i = 0; i < nombreMenu.length(); i++) {
            char c = nombreMenu.charAt(i);
            System.out.print("'" + c + "'(" + (int) c + ") ");
        }
        System.out.println();

        // ✅ USAR NOMBRES EXACTOS que aparecen en la tabla
        Map<String, String> mapeoComponentes = new HashMap<>();

        // Menús de Compras
        mapeoComponentes.put("Gestionar Proveedores", "mProveedores");
        mapeoComponentes.put("Registrar Compra", "mRegCompras");
        mapeoComponentes.put("Reporte Compras", "mRepCompras");

        // Menús de Ventas
        mapeoComponentes.put("Gestionar Clientes", "mClientes");
        mapeoComponentes.put("Talonarios de Factura", "mTalonarios");
        mapeoComponentes.put("Registrar Venta Directa", "mRegVentaDirecta");
        mapeoComponentes.put("Registrar Ventas", "mRegVentas");
        mapeoComponentes.put("Reporte Ventas", "mRepVentas");
        mapeoComponentes.put("Reporte Productos Mas Vendidos", "mRepProductosMasVendidos");

        // Menús de Stock
        mapeoComponentes.put("Gestionar Productos", "mProductos");
        mapeoComponentes.put("Lista de Precios", "mListaPrecios");
        mapeoComponentes.put("Ajustar Stock", "mAjustarStock");
        mapeoComponentes.put("Aprobar Ajuste de Stock", "mAprobarStock");
        mapeoComponentes.put("Reporte de Inventario", "mRepInvent");

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

        // ✅ BUSCAR en el mapeo
        String componenteMapeado = mapeoComponentes.get(nombreMenu);

        System.out.println("Componente mapeado encontrado: '" + componenteMapeado + "'");

        if (componenteMapeado != null) {
            System.out.println("→ USANDO MAPEO: " + componenteMapeado);
            return componenteMapeado;
        }

        // ✅ FALLBACK: Generar automáticamente si no está mapeado
        String fallback = "m" + nombreMenu
                .replace(" ", "")
                .replace("/", "")
                .replace(".", "");

        System.out.println("→ USANDO FALLBACK: " + fallback);
        System.out.println("=== FIN DEBUG ===\n");

        return fallback;
    }

    @Override
    public void imGrabar() {
        ComboBoxItem rolSeleccionado = (ComboBoxItem) jComboBox1.getSelectedItem();
        if (rolSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un rol para guardar los permisos.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<mPermiso> permisosAGuardar = new ArrayList<>();
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        int permisosConfigurados = 0;

        // 1. PASO 1: Recolectar TODOS los permisos (con valores true o false)
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int idMenu = (Integer) modelo.getValueAt(i, 0);
            String nombreMenuTabla = (String) modelo.getValueAt(i, 1);
            boolean ver = (Boolean) modelo.getValueAt(i, 2);
            boolean crear = (Boolean) modelo.getValueAt(i, 3);
            boolean leer = (Boolean) modelo.getValueAt(i, 4);
            boolean actualizar = (Boolean) modelo.getValueAt(i, 5);
            boolean eliminar = (Boolean) modelo.getValueAt(i, 6);

            // ✅ GENERAR nombreComponente desde el nombre del menú
            String nombreComponente = generarNombreComponente(nombreMenuTabla);

            // ✅ SIEMPRE agregar el permiso
            mPermiso permiso = new mPermiso();
            permiso.setIdRol(rolSeleccionado.getId());
            permiso.setIdMenu(idMenu);
            permiso.setNombreComponente(nombreComponente);
            permiso.setVer(ver);
            permiso.setCrear(crear);
            permiso.setLeer(leer);
            permiso.setActualizar(actualizar);
            permiso.setEliminar(eliminar);

            permisosAGuardar.add(permiso);

            // Solo contar los configurados para el mensaje informativo
            if (ver || crear || leer || actualizar || eliminar) {
                permisosConfigurados++;
            }
        }

        // 2. PASO 2: Validar (opcional - puedes quitar esta validación)
        if (permisosConfigurados == 0) {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "No hay permisos configurados para este rol.\n¿Desea guardar sin permisos?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // 3. PASO 3: Guardar 
        if (controlador.guardarPermisos(permisosAGuardar)) {
            permisosCambiados = false;
            actualizarTitulo();
            JOptionPane.showMessageDialog(this,
                    String.format("Permisos guardados correctamente.\n%d menús con permisos activos para el rol %s",
                            permisosConfigurados, rolSeleccionado.toString()),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar los permisos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    // Método para configurar el JComboBox de roles
    private void configurarComboBoxRoles() {
        jComboBox1.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
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

    // Método para llenar el JComboBox de roles con nombres
    private void llenarComboBoxRoles() {
        try {
            RolesDAO rolesDAO = new RolesDAO();
            ResultSet resultSet = rolesDAO.obtenerNombresRoles();
            DefaultComboBoxModel<ComboBoxItem> model = new DefaultComboBoxModel<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id_rol");
                String nombreRol = id + " - " + resultSet.getString("nombre");
                model.addElement(new ComboBoxItem(id, nombreRol));
            }
            jComboBox1.setModel(model);
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
        return new String[]{"id_rol", "nombre"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {

    }

    private vPrincipal obtenerVentanaPrincipal() {
        // Buscar la ventana principal en las ventanas abiertas
        for (Window window : Window.getWindows()) {
            if (window instanceof vPrincipal) {
                return (vPrincipal) window;
            }
        }
        return null;
    }

    private void configurarEventoComboBox() {
        jComboBox1.addActionListener(e -> {
            ComboBoxItem item = (ComboBoxItem) jComboBox1.getSelectedItem();
            if (item != null) {
                cargarPermisosDelRol(item.getId());
            }
        });
    }

    private void configurarTablaPermisos() {
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.setRowHeight(25);

        // Configurar anchos de columnas
        jTable1.getColumnModel().getColumn(0).setMaxWidth(70);  // Menu ID
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(200); // Menu
        jTable1.getColumnModel().getColumn(2).setMaxWidth(60);  // VER
        jTable1.getColumnModel().getColumn(3).setMaxWidth(70);  // CREATE
        jTable1.getColumnModel().getColumn(4).setMaxWidth(60);  // READ
        jTable1.getColumnModel().getColumn(5).setMaxWidth(70);  // UPDATE
        jTable1.getColumnModel().getColumn(6).setMaxWidth(70);  // DELETE

        // Agregar checkboxes en headers de columnas de permisos
        configurarHeadersConCheckbox();
    }

    private void configurarHeadersConCheckbox() {
        // Agregar checkbox headers para las columnas de permisos (columnas 2-6)
        for (int i = 2; i <= 6; i++) {
            CheckBoxHeader checkBoxHeader = new CheckBoxHeader(jTable1, i);
            jTable1.getColumnModel().getColumn(i).setHeaderRenderer(checkBoxHeader);
        }

        // Hacer que el header sea más alto para acomodar los checkboxes
        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 35));

        // Agregar listener para manejar clicks en el header
        jTable1.getTableHeader().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = jTable1.getTableHeader().columnAtPoint(e.getPoint());

                // Solo procesar clicks en columnas de permisos (2-6)
                if (columnIndex >= 2 && columnIndex <= 6) {
                    toggleColumna(columnIndex);
                    jTable1.getTableHeader().repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    // Método para alternar selección de columna
    private void toggleColumna(int columna) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        // Verificar si todos están seleccionados
        boolean todosSeleccionados = true;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object value = model.getValueAt(i, columna);
            if (value == null || !(Boolean) value) {
                todosSeleccionados = false;
                break;
            }
        }

        // Aplicar el estado opuesto
        boolean nuevoEstado = !todosSeleccionados;
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(nuevoEstado, i, columna);
        }

        jTable1.repaint();
    }

    // Método para aplicar permisos a menús
    public void aplicarPermisosAMenus(int idRol) {
        vPrincipal ventanaPrincipal = obtenerVentanaPrincipal();
        if (ventanaPrincipal == null) {
            return;
        }

        JMenuBar menuBar = ventanaPrincipal.getJMenuBar();

        // Aplicar permisos a cada menú
        aplicarPermisosAMenu(menuBar.getMenu(0), idRol); // Archivo
        aplicarPermisosAMenu(menuBar.getMenu(1), idRol); // Edición
        aplicarPermisosAMenu(menuBar.getMenu(2), idRol); // Compras
        aplicarPermisosAMenu(menuBar.getMenu(3), idRol); // Ventas
        aplicarPermisosAMenu(menuBar.getMenu(4), idRol); // Stock
        aplicarPermisosAMenu(menuBar.getMenu(5), idRol); // Tesorería
        aplicarPermisosAMenu(menuBar.getMenu(6), idRol); // Seguridad
    }

    // Método auxiliar para aplicar permisos a un menú específico
    private void aplicarPermisosAMenu(JMenu menu, int idRol) {
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

        // Verificar permiso para el menú principal
        String nombreMenu = "m" + menu.getText().replace(" ", "");
        boolean tienePermiso = controlador.tienePermiso(idRol, nombreMenu, "ver");
        menu.setEnabled(tienePermiso);

        // Si el menú principal no tiene permisos, deshabilitar todos los submenús
        if (!tienePermiso) {
            for (int i = 0; i < menu.getItemCount(); i++) {
                JMenuItem item = menu.getItem(i);
                if (item != null && item.getText() != null && !item.getText().trim().isEmpty()) {
                    item.setEnabled(false);
                }
            }
            return;
        }

        // Mapeo de textos de menú a nombres de componentes en BD
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

        // Aplicar permisos a submenús
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null && item.getText() != null && !item.getText().trim().isEmpty()) {
                String textoItem = item.getText();
                String nombreItem = mapeoComponentes.get(textoItem);

                if (nombreItem != null) {
                    // Verificar permisos para TODOS los menús
                    boolean tienePermisoItem = controlador.tienePermiso(idRol, nombreItem, "ver");
                    item.setEnabled(tienePermisoItem);
                } else {
                    // Si no está en el mapeo, usar la lógica original como fallback
                    String nombreItemFallback = item.getActionCommand();
                    if (nombreItemFallback == null || nombreItemFallback.isEmpty()) {
                        nombreItemFallback = "m" + textoItem.replace(" ", "");
                    }
                    boolean tienePermisoItem = controlador.tienePermiso(idRol, nombreItemFallback, "ver");
                    item.setEnabled(tienePermisoItem);
                }
            }
        }
    }

    // Método para validar permisos antes de ejecutar acciones
    public boolean validarPermisoAccion(int idRol, String nombreComponente, String tipoAccion) {
        return controlador.tienePermiso(idRol, nombreComponente, tipoAccion);
    }

    // Clase para seleccionar toda la columna con checkbox en el header
    class CheckBoxHeader extends JCheckBox implements TableCellRenderer {

        private final int column;
        private final JTable table;

        public CheckBoxHeader(JTable table, int column) {
            this.table = table;
            this.column = column;
            setText(getColumnName());
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true);
            setBackground(jTable1.getTableHeader().getBackground());
        }

        private String getColumnName() {
            return table.getColumnModel().getColumn(column).getHeaderValue().toString();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Verificar estado actual de la columna
            boolean todosSeleccionados = verificarTodosSeleccionados();
            setSelected(todosSeleccionados);

            return this;
        }

        private boolean verificarTodosSeleccionados() {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            if (model.getRowCount() == 0) {
                return false;
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                Object cellValue = model.getValueAt(i, column);
                if (cellValue == null || !(Boolean) cellValue) {
                    return false;
                }
            }
            return true;
        }
    }

    public List<mPermiso> obtenerMenusDesdaInterfaz() {
        List<mPermiso> menus = new ArrayList<>();

        vPrincipal ventanaPrincipal = obtenerVentanaPrincipal();
        if (ventanaPrincipal == null) {
            return controlador.obtenerMenusDelSistema();
        }

        JMenuBar menuBar = ventanaPrincipal.getJMenuBar();

        // ✅ USAR IDs temporales negativos para distinguir de los reales
        int menuIdTemporal = -1;

        // Recorrer cada menú principal
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                // Agregar menú principal
                mPermiso menuPrincipal = new mPermiso();
                menuPrincipal.setIdMenu(menuIdTemporal--); // ✅ ID temporal negativo
                menuPrincipal.setNombreMenu(menu.getText());
                menuPrincipal.setNombreComponente("m" + menu.getText().replace(" ", ""));
                menus.add(menuPrincipal);

                // Recorrer submenús
                for (int j = 0; j < menu.getItemCount(); j++) {
                    JMenuItem item = menu.getItem(j);

                    if (item != null
                            && item.getText() != null
                            && !item.getText().trim().isEmpty()) {

                        mPermiso submenu = new mPermiso();
                        submenu.setIdMenu(menuIdTemporal--); // ✅ ID temporal negativo
                        submenu.setNombreMenu(item.getText());

                        String nombreComponente = item.getActionCommand();
                        if (nombreComponente == null || nombreComponente.isEmpty()) {
                            nombreComponente = "m" + item.getText()
                                    .replace(" ", "")
                                    .replace("/", "")
                                    .replace(".", "");
                        }
                        submenu.setNombreComponente(nombreComponente);
                        menus.add(submenu);
                    }
                }
            }
        }

        return menus;
    }

    /**
     * Método para actualizar menús desde vMenus (sincronización directa)
     */
    public void actualizarMenusDesdeVMenus(List<mPermiso> menusSincronizados) {
        // Guardar el rol actualmente seleccionado
        ComboBoxItem rolSeleccionado = (ComboBoxItem) jComboBox1.getSelectedItem();

        // Actualizar la tabla con los nuevos menús
        cargarMenusEnTablaDesdeCache(menusSincronizados);

        // Si hay un rol seleccionado, cargar sus permisos
        if (rolSeleccionado != null) {
            List<mPermiso> permisos = controlador.obtenerPermisosPorRol(rolSeleccionado.getId());
            aplicarPermisosEnTabla(permisos);
        }

        // Indicar que la ventana ha sido actualizada
        JOptionPane.showMessageDialog(this,
                "Tabla de menús actualizada con " + menusSincronizados.size() + " elementos.\n"
                + "Los permisos existentes se han mantenido.",
                "Actualización Completada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Cargar menús desde cache en lugar de base de datos
     */
    private void cargarMenusEnTablaDesdeCache(List<mPermiso> menus) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0); // Limpiar tabla existente

        for (mPermiso menu : menus) {
            modelo.addRow(new Object[]{
                menu.getIdMenu(),
                menu.getNombreMenu(),
                false, // VER
                false, // CREAR
                false, // LEER
                false, // ACTUALIZAR
                false // ELIMINAR
            });
        }

        jTable1.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        jLabel1.setText("ROL:");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Menu ID", "Menu", "VER", "CREATE", "READ", "UPDATE", "DELETE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(6).setResizable(false);
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<ComboBoxItem> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
