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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class vPermisos extends javax.swing.JInternalFrame implements myInterface {

    private boolean permisosCambiados = false;
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
        List<mPermiso> menus = controlador.obtenerMenusDelSistema();
        cargarMenusEnTablaDesdeDB(menus);
    }

    // Método para configurar listeners en la tabla
    private void configurarListenersTabla() {
        jTable1.getModel().addTableModelListener(e -> {
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
        // Cargar menús del sistema
        List<mPermiso> menus = controlador.obtenerMenusDelSistema();
        cargarMenusEnTablaDesdeDB(menus);

        // Cargar permisos existentes del rol
        List<mPermiso> permisos = controlador.obtenerPermisosPorRol(idRol);
        aplicarPermisosEnTabla(permisos);
    }

    // Método para aplicar permisos en la tabla
    private void aplicarPermisosEnTabla(List<mPermiso> permisos) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();

        for (mPermiso permiso : permisos) {
            // Buscar la fila correspondiente al menú
            for (int i = 0; i < modelo.getRowCount(); i++) {
                int idMenuTabla = (Integer) modelo.getValueAt(i, 0);
                if (idMenuTabla == permiso.getIdMenu()) {
                    modelo.setValueAt(permiso.isVer(), i, 2);
                    modelo.setValueAt(permiso.isCrear(), i, 3);
                    modelo.setValueAt(permiso.isLeer(), i, 4);
                    modelo.setValueAt(permiso.isActualizar(), i, 5);
                    modelo.setValueAt(permiso.isEliminar(), i, 6);
                    break;
                }
            }
        }
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

        // 1. PASO 1: Recolectar todos los permisos (SIN guardar)
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int idMenu = (Integer) modelo.getValueAt(i, 0);
            boolean ver = (Boolean) modelo.getValueAt(i, 2);
            boolean crear = (Boolean) modelo.getValueAt(i, 3);
            boolean leer = (Boolean) modelo.getValueAt(i, 4);
            boolean actualizar = (Boolean) modelo.getValueAt(i, 5);
            boolean eliminar = (Boolean) modelo.getValueAt(i, 6);

            // Solo agregar si al menos un permiso está marcado
            if (ver || crear || leer || actualizar || eliminar) {
                mPermiso permiso = new mPermiso();
                permiso.setIdRol(rolSeleccionado.getId());
                permiso.setIdMenu(idMenu);
                permiso.setVer(ver);
                permiso.setCrear(crear);
                permiso.setLeer(leer);
                permiso.setActualizar(actualizar);
                permiso.setEliminar(eliminar);

                permisosAGuardar.add(permiso);
                permisosConfigurados++;
            }
        }

        // 2. PASO 2: Validar 
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
            permisosCambiados = false; // Resetear indicador
            actualizarTitulo();
            JOptionPane.showMessageDialog(this,
                    String.format("Permisos guardados correctamente.\n%d menús configurados para el rol %s",
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
        mapeoComponentes.put("Gestionar Proveedores", "mProveedores");
        mapeoComponentes.put("Registrar Compra", "mRegCompras");
        mapeoComponentes.put("Reporte Compras", "mRepCompras");

        // Menús de Ventas
        mapeoComponentes.put("Gestionar Clientes", "mClientes");
        mapeoComponentes.put("Talonarios de Factura", "mTalonarios");
        mapeoComponentes.put("Registrar Venta Directa", "mRegVentaDirecta");
        mapeoComponentes.put("Registrar Ventas", "mRegVentas");
        mapeoComponentes.put("Reporte Ventas", "mRepVentas");

        // Menús de Stock
        mapeoComponentes.put("Productos", "mProductos");
        mapeoComponentes.put("Lista de Precios", "mListaPrecios");
        mapeoComponentes.put("Ajustar Stock", "mAjustarStock");
        mapeoComponentes.put("Aprobar Stock", "mAprobarStock");
        mapeoComponentes.put("Reporte Inventario", "mRepInvent");

        // Menús de Tesorería
        mapeoComponentes.put("Apertura/Cierre Caja", "mAperturaCierreCaja");
        mapeoComponentes.put("Ingresar Caja", "mIngCaja");
        mapeoComponentes.put("Reporte Caja", "mRepCaja");

        // Menús de Seguridad
        mapeoComponentes.put("Personas", "mPersonas");
        mapeoComponentes.put("Usuarios", "mUsuarios");
        mapeoComponentes.put("Roles", "mRoles");
        mapeoComponentes.put("Permisos", "mPermisos");
        mapeoComponentes.put("Menus", "mMenus");

        // Menús de Archivo - TAMBIÉN verificar permisos
        mapeoComponentes.put("Nuevo", "mNuevo");
        mapeoComponentes.put("Guardar", "mGuardar");
        mapeoComponentes.put("Borrar", "mBorrar");
        mapeoComponentes.put("Buscar", "mBuscar");
        mapeoComponentes.put("Imprimir", "mImprimir");
        mapeoComponentes.put("Cerrar Ventana", "mCerrarVentana");
        mapeoComponentes.put("Salir", "mSalir");

        // Menús de Edición - TAMBIÉN verificar permisos
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
