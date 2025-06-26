package vista;

import interfaces.myInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import modelo.PersonasDAO;
import modelo.RolesDAO;
import modelo.UsuariosDAO;
import modelo.PasswordUtils;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;
import modelo.DatabaseConnection;

public class vUsuarios extends javax.swing.JInternalFrame implements myInterface {

    // Variables de clase
    private UsuariosDAO usuariosDAO;
    private int currentIndex = 0;
    private List<String[]> listaUsuarios;

    public vUsuarios() {
        initComponents();

        // Configuraciones de ventana
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);

        // Inicializar DAO y lista
        usuariosDAO = new UsuariosDAO();
        listaUsuarios = new ArrayList<>();

        // Verificar datos requeridos
        verificarDatosRequeridos();

        // Configurar componentes
        configurarComboBoxPersonas();
        configurarComboBoxRoles();
        configurarEventosTeclado();

        // Estado inicial
        txtUsuarioId.setText("0");
        usuariosCheckBoxActivo.setSelected(true);

        // Cargar datos
        cargarTodosLosUsuarios();
        if (!listaUsuarios.isEmpty()) {
            mostrarUsuario(0);
        }

        // Focus inicial
        javax.swing.SwingUtilities.invokeLater(() -> {
            txtUsuarioNombre.requestFocusInWindow();
        });

        actualizarIndicadorPosicion();
    }

    // Método para validar todos los datos del formulario
    private boolean validarDatos() {
        // Validar nombre de usuario
        String nombreUsuario = txtUsuarioNombre.getText().trim();
        if (nombreUsuario.isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio", txtUsuarioNombre);
            return false;
        }

        if (nombreUsuario.length() < 3) {
            mostrarError("El nombre de usuario debe tener al menos 3 caracteres", txtUsuarioNombre);
            return false;
        }

        if (!nombreUsuario.matches("^[a-zA-Z0-9._-]+$")) {
            mostrarError("El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos", txtUsuarioNombre);
            return false;
        }

        // Validar contraseña solo si no está vacía (para edición)
        String contraseña = new String(usuariosContraseña.getPassword());
        int usuarioId = Integer.parseInt(txtUsuarioId.getText());

        if (usuarioId == 0 && contraseña.isEmpty()) {
            mostrarError("La contraseña es obligatoria para usuarios nuevos", usuariosContraseña);
            return false;
        }

        if (!contraseña.isEmpty() && contraseña.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres", usuariosContraseña);
            return false;
        }

        // Validar confirmación de contraseña
        if (!contraseña.isEmpty()) {
            String confirmacion = new String(usuariosConfContraseña.getPassword());
            if (!contraseña.equals(confirmacion)) {
                mostrarError("Las contraseñas no coinciden", usuariosConfContraseña);
                return false;
            }
        }

        // Validar selección de persona
        ComboBoxItem personaSeleccionada = (ComboBoxItem) usuariosComboBoxPersonas.getSelectedItem();
        if (personaSeleccionada == null || personaSeleccionada.getId() <= 0) {
            mostrarError("Debe seleccionar una persona", usuariosComboBoxPersonas);
            return false;
        }

        // Validar que la persona no tenga ya un usuario
        int personaId = personaSeleccionada.getId();
        if (usuariosDAO.personaTieneUsuario(personaId, usuarioId)) {
            mostrarError("Esta persona ya tiene un usuario asignado", usuariosComboBoxPersonas);
            return false;
        }

        // Validar selección de rol
        ComboBoxItem rolSeleccionado = (ComboBoxItem) usuariosComboBoxRoles.getSelectedItem();
        if (rolSeleccionado == null || rolSeleccionado.getId() <= 0) {
            mostrarError("Debe seleccionar un rol", usuariosComboBoxRoles);
            return false;
        }

        return true;
    }

    // Método auxiliar para mostrar errores con focus
    private void mostrarError(String mensaje, javax.swing.JComponent componente) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
        if (componente != null) {
            componente.requestFocus();
        }
    }

    private void mostrarError(String mensaje) {
        mostrarError(mensaje, null);
    }

    // Configurar eventos de teclado
    private void configurarEventosTeclado() {
        // Evento Enter en campo ID para buscar
        txtUsuarioId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    buscarUsuarioPorId();
                }
            }
        });

        // Focus automático en campos
        txtUsuarioId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUsuarioId.selectAll();
            }
        });

        txtUsuarioNombre.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUsuarioNombre.selectAll();
            }
        });
    }

    // Método para buscar usuario por ID al presionar Enter
    private void buscarUsuarioPorId() {
        try {
            int id = Integer.parseInt(txtUsuarioId.getText().trim());

            if (id == 0) {
                limpiarCampos();
                return;
            }

            String[] usuario = usuariosDAO.buscarPorId(id);
            if (usuario != null) {
                mostrarDatosUsuario(usuario);
                // Actualizar posición en la lista
                for (int i = 0; i < listaUsuarios.size(); i++) {
                    if (Integer.parseInt(listaUsuarios.get(i)[0]) == id) {
                        currentIndex = i;
                        break;
                    }
                }
                actualizarIndicadorPosicion();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró ningún usuario con el ID especificado.");
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido.");
            txtUsuarioId.selectAll();
        }
    }

    // Método para configurar el ComboBox de personas
    private void configurarComboBoxPersonas() {
        usuariosComboBoxPersonas.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                llenarComboBoxPersonas();
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

    // Método para configurar el ComboBox de roles
    private void configurarComboBoxRoles() {
        usuariosComboBoxRoles.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                llenarComboBoxRoles();
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

    @Override
    public void imGrabar() {
        try {
            if (!validarDatos()) {
                return;
            }

            String nombreUsuario = txtUsuarioNombre.getText().trim();
            String contraseñaPlana = new String(usuariosContraseña.getPassword());

            // HASHEAR LA CONTRASEÑA Y IMPRIMIR EN LOG
            String contraseñaHasheada = "";
            if (!contraseñaPlana.isEmpty()) {
                contraseñaHasheada = PasswordUtils.hashPassword(contraseñaPlana);
            }

            ComboBoxItem personaSeleccionada = (ComboBoxItem) usuariosComboBoxPersonas.getSelectedItem();
            ComboBoxItem rolSeleccionado = (ComboBoxItem) usuariosComboBoxRoles.getSelectedItem();

            int personaId = personaSeleccionada.getId();
            int rolId = rolSeleccionado.getId();
            boolean activo = usuariosCheckBoxActivo.isSelected();
            int usuarioId = Integer.parseInt(txtUsuarioId.getText());

            // Verificar si el nombre de usuario ya existe
            if (usuariosDAO.existeNombreUsuario(nombreUsuario, usuarioId)) {
                mostrarError("El nombre de usuario ya existe", txtUsuarioNombre);
                return;
            }

            // AGREGAR LOG DE ASIGNACIÓN DE ROL
            System.out.println("=== ASIGNACIÓN DE ROL ===");
            System.out.println("Rol seleccionado: " + rolSeleccionado.toString());
            System.out.println("ID del rol: " + rolId);
            System.out.println("========================");

            boolean resultado;
            if (usuarioId == 0) {
                // CREAR NUEVO USUARIO - IMPRIMIR LOG
                System.out.println("=== CREANDO NUEVO USUARIO ===");
                System.out.println("Usuario: " + nombreUsuario);
                System.out.println("Contraseña original: " + contraseñaPlana);
                System.out.println("Contraseña hasheada: " + contraseñaHasheada);
                System.out.println("Persona ID: " + personaId);
                System.out.println("Rol ID: " + rolId);
                System.out.println("Activo: " + activo);
                System.out.println("=============================");

                resultado = usuariosDAO.insertar(nombreUsuario, contraseñaHasheada, personaId, rolId, activo);
            } else {
                // ACTUALIZAR USUARIO EXISTENTE
                String contraseñaParaGuardar;
                if (contraseñaPlana.isEmpty()) {
                    // Si el campo está vacío, mantener la contraseña actual
                    String[] usuarioActual = usuariosDAO.buscarPorId(usuarioId);
                    contraseñaParaGuardar = usuarioActual[2]; // Contraseña actual hasheada

                    System.out.println("=== ACTUALIZANDO USUARIO (SIN CAMBIAR CONTRASEÑA) ===");
                    System.out.println("Usuario ID: " + usuarioId);
                    System.out.println("Usuario: " + nombreUsuario);
                    System.out.println("Contraseña actual (hasheada): " + contraseñaParaGuardar);
                    System.out.println("Persona ID: " + personaId);
                    System.out.println("Rol ID: " + rolId);
                    System.out.println("Activo: " + activo);
                    System.out.println("=============================================");
                } else {
                    // Si hay nueva contraseña, hashearla
                    contraseñaParaGuardar = contraseñaHasheada;

                    System.out.println("=== ACTUALIZANDO USUARIO (CON NUEVA CONTRASEÑA) ===");
                    System.out.println("Usuario ID: " + usuarioId);
                    System.out.println("Usuario: " + nombreUsuario);
                    System.out.println("Nueva contraseña original: " + contraseñaPlana);
                    System.out.println("Nueva contraseña hasheada: " + contraseñaParaGuardar);
                    System.out.println("Persona ID: " + personaId);
                    System.out.println("Rol ID: " + rolId);
                    System.out.println("Activo: " + activo);
                    System.out.println("===============================================");
                }
                resultado = usuariosDAO.actualizar(usuarioId, nombreUsuario, contraseñaParaGuardar, personaId, rolId, activo);
            }

            if (resultado) {
                System.out.println("✓ Usuario guardado exitosamente en la base de datos");
                JOptionPane.showMessageDialog(this, "Usuario guardado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTodosLosUsuarios();
                // Limpiar campos de contraseña por seguridad
                usuariosContraseña.setText("");
                usuariosConfContraseña.setText("");
                if (usuarioId == 0) {
                    imUltimo();
                }
                actualizarIndicadorPosicion();
            } else {
                System.out.println("✗ Error al guardar el usuario en la base de datos");
                mostrarError("Error al guardar el usuario");
            }

        } catch (Exception e) {
            System.err.println("✗ Excepción al guardar usuario: " + e.getMessage());
            mostrarError("Error: " + e.getMessage());
        }
    }

    @Override
    public void imNuevo() {
        limpiarCampos();
        System.out.println("Preparando nuevo usuario");
    }

    @Override
    public void imBorrar() {
        try {
            int usuarioId = Integer.parseInt(txtUsuarioId.getText());
            if (usuarioId <= 0) {
                mostrarError("Seleccione un usuario para eliminar");
                return;
            }

            String nombreUsuario = txtUsuarioNombre.getText();

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar el usuario '" + nombreUsuario + "'?\n"
                    + "Esta acción no se puede deshacer.",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                if (usuariosDAO.eliminar(usuarioId)) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarTodosLosUsuarios();
                    if (!listaUsuarios.isEmpty()) {
                        if (currentIndex >= listaUsuarios.size()) {
                            currentIndex = listaUsuarios.size() - 1;
                        }
                        mostrarUsuario(currentIndex);
                    } else {
                        limpiarCampos();
                    }
                    actualizarIndicadorPosicion();
                } else {
                    mostrarError("Error al eliminar el usuario");
                }
            }
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    @Override
    public void imBuscar() {
        try {
            String input = JOptionPane.showInputDialog(this, "Ingrese el ID del usuario a buscar:");
            if (input != null && !input.trim().isEmpty()) {
                int id = Integer.parseInt(input.trim());
                String[] usuario = usuariosDAO.buscarPorId(id);
                if (usuario != null) {
                    mostrarDatosUsuario(usuario);
                    // Actualizar posición en la lista
                    for (int i = 0; i < listaUsuarios.size(); i++) {
                        if (Integer.parseInt(listaUsuarios.get(i)[0]) == id) {
                            currentIndex = i;
                            break;
                        }
                    }
                    actualizarIndicadorPosicion();
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario no encontrado");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID válido");
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    @Override
    public void imPrimero() {
        if (!listaUsuarios.isEmpty()) {
            currentIndex = 0;
            mostrarUsuario(currentIndex);
            actualizarIndicadorPosicion();
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros en la base de datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void imSiguiente() {
        if (!listaUsuarios.isEmpty() && currentIndex < listaUsuarios.size() - 1) {
            currentIndex++;
            mostrarUsuario(currentIndex);
            actualizarIndicadorPosicion();
        } else {
            JOptionPane.showMessageDialog(this, "Ya está en el último registro", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void imAnterior() {
        if (!listaUsuarios.isEmpty() && currentIndex > 0) {
            currentIndex--;
            mostrarUsuario(currentIndex);
            actualizarIndicadorPosicion();
        } else {
            JOptionPane.showMessageDialog(this, "Ya está en el primer registro", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void imUltimo() {
        if (!listaUsuarios.isEmpty()) {
            currentIndex = listaUsuarios.size() - 1;
            mostrarUsuario(currentIndex);
            actualizarIndicadorPosicion();
        } else {
            JOptionPane.showMessageDialog(this, "No hay registros en la base de datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void imActualizar() {
        cargarTodosLosUsuarios();
        if (!listaUsuarios.isEmpty()) {
            if (currentIndex >= listaUsuarios.size()) {
                currentIndex = listaUsuarios.size() - 1;
            }
            mostrarUsuario(currentIndex);
        } else {
            limpiarCampos();
        }
        actualizarIndicadorPosicion();
        JOptionPane.showMessageDialog(this, "Datos actualizados", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void imFiltrar() {
        // No implementado específicamente para usuarios
        JOptionPane.showMessageDialog(this, "Funcionalidad de filtro no implementada para usuarios", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void imImprimir() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de impresión no implementada", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void imInsDet() {
        // No aplicable para usuarios
    }

    @Override
    public void imDelDet() {
        // No aplicable para usuarios
    }

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
        return "usuarios";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"UsuarioID", "NombreUsuario"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        try {
            String[] usuario = usuariosDAO.buscarPorId(id);
            if (usuario != null) {
                mostrarDatosUsuario(usuario);
                // Actualizar posición en la lista
                for (int i = 0; i < listaUsuarios.size(); i++) {
                    if (Integer.parseInt(listaUsuarios.get(i)[0]) == id) {
                        currentIndex = i;
                        break;
                    }
                }
                actualizarIndicadorPosicion();
            }
        } catch (Exception e) {
            System.err.println("Error al seleccionar registro: " + e.getMessage());
        }
    }

    // Método para llenar el JComboBox de personas con nombre y apellido
    private void llenarComboBoxPersonas() {
        try {
            PersonasDAO personasDAO = new PersonasDAO();
            ResultSet resultSet = personasDAO.obtenerNombresYApellidos();
            usuariosComboBoxPersonas.removeAllItems();

            // Agregar elemento vacío como primera opción
            usuariosComboBoxPersonas.addItem(new ComboBoxItem(0, "-- Seleccione una persona --"));

            while (resultSet.next()) {
                int id = resultSet.getInt("id_persona");
                String nombreCompleto = id + " - " + resultSet.getString("nombre") + " " + resultSet.getString("apellido");
                usuariosComboBoxPersonas.addItem(new ComboBoxItem(id, nombreCompleto));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar personas: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al cargar personas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para llenar el JComboBox de roles con nombres
    private void llenarComboBoxRoles() {
        try {
            RolesDAO rolesDAO = new RolesDAO();
            ResultSet resultSet = rolesDAO.obtenerNombresRoles();
            usuariosComboBoxRoles.removeAllItems();

            // Agregar elemento vacío como primera opción
            usuariosComboBoxRoles.addItem(new ComboBoxItem(0, "-- Seleccione un rol --"));

            while (resultSet.next()) {
                int id = resultSet.getInt("id_rol");
                String nombreRol = id + " - " + resultSet.getString("nombre");
                usuariosComboBoxRoles.addItem(new ComboBoxItem(id, nombreRol));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar todos los usuarios en la lista
    private void cargarTodosLosUsuarios() {
        try {
            listaUsuarios.clear();

            // Usar una consulta más simple para evitar errores de JOIN
            String sql = "SELECT UsuarioID, NombreUsuario, PersonaID, RolID, Activo FROM usuarios ORDER BY UsuarioID";

            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String[] usuario = {
                        String.valueOf(rs.getInt("UsuarioID")),
                        rs.getString("NombreUsuario"),
                        "", // Contraseña no se carga por seguridad
                        String.valueOf(rs.getInt("PersonaID")),
                        String.valueOf(rs.getInt("RolID")),
                        String.valueOf(rs.getBoolean("Activo"))
                    };
                    listaUsuarios.add(usuario);
                }
            }

            currentIndex = 0;
            System.out.println("Usuarios cargados exitosamente: " + listaUsuarios.size());

        } catch (SQLException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar un usuario específico de la lista
    private void mostrarUsuario(int index) {
        if (index >= 0 && index < listaUsuarios.size()) {
            String[] usuario = listaUsuarios.get(index);
            mostrarDatosUsuario(usuario);
        }
    }

    // Método para mostrar los datos de un usuario en los campos
    private void mostrarDatosUsuario(String[] usuario) {
        txtUsuarioId.setText(usuario[0]);
        txtUsuarioNombre.setText(usuario[1]);
        usuariosContraseña.setText(""); // Por seguridad
        usuariosConfContraseña.setText("");

        // Seleccionar persona en combo
        int personaId = Integer.parseInt(usuario[3]);
        seleccionarEnCombo(usuariosComboBoxPersonas, personaId);

        // Seleccionar rol en combo
        int rolId = Integer.parseInt(usuario[4]);
        seleccionarEnCombo(usuariosComboBoxRoles, rolId);

        usuariosCheckBoxActivo.setSelected(Boolean.parseBoolean(usuario[5]));
    }

    // Método para seleccionar un item en un ComboBox por ID
    private void seleccionarEnCombo(javax.swing.JComboBox<ComboBoxItem> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            ComboBoxItem item = combo.getItemAt(i);
            if (item != null && item.getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        // Si no se encuentra, seleccionar el primer elemento (vacío)
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void verificarDatosRequeridos() {
        try {
            // Verificar que existan personas
            PersonasDAO personasDAO = new PersonasDAO();
            ResultSet rsPersonas = personasDAO.obtenerNombresYApellidos();
            if (!rsPersonas.next()) {
                JOptionPane.showMessageDialog(this,
                        "No hay personas registradas. Debe crear personas antes de crear usuarios.",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
            }

            // Verificar que existan roles
            RolesDAO rolesDAO = new RolesDAO();
            ResultSet rsRoles = rolesDAO.obtenerNombresRoles();
            if (!rsRoles.next()) {
                JOptionPane.showMessageDialog(this,
                        "No hay roles registrados. Debe crear roles antes de crear usuarios.",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar datos requeridos: " + e.getMessage());
        }
    }

    // Método para limpiar todos los campos
    private void limpiarCampos() {
        txtUsuarioId.setText("0");
        txtUsuarioNombre.setText("");
        usuariosContraseña.setText("");
        usuariosConfContraseña.setText("");

        // Limpiar selecciones de combo
        if (usuariosComboBoxPersonas.getItemCount() > 0) {
            usuariosComboBoxPersonas.setSelectedIndex(0);
        }
        if (usuariosComboBoxRoles.getItemCount() > 0) {
            usuariosComboBoxRoles.setSelectedIndex(0);
        }

        usuariosCheckBoxActivo.setSelected(true);

        // Focus en el primer campo
        javax.swing.SwingUtilities.invokeLater(() -> {
            txtUsuarioNombre.requestFocusInWindow();
        });

        actualizarIndicadorPosicion();
    }

    // Método para actualizar indicador de posición
    private void actualizarIndicadorPosicion() {
        if (listaUsuarios != null && !listaUsuarios.isEmpty()) {
            String texto = String.format("Usuario %d de %d", currentIndex + 1, listaUsuarios.size());
            this.setTitle("Gestión de Usuarios - " + texto);
        } else {
            this.setTitle("Gestión de Usuarios - Sin registros");
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
                            .addComponent(jLabel4)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usuariosCheckBoxActivo)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(usuariosConfContraseña, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                                    .addComponent(usuariosContraseña))))))
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
