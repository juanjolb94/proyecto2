package vista;

import interfaces.myInterface;
import modelo.Mesa;
import modelo.MesasDAO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

public class vSeleccionMesa extends javax.swing.JInternalFrame implements myInterface {

    private MesasDAO mesasDAO;
    private List<Mesa> mesas;
    private Image backgroundImage;
    private JPanel panelMesas;
    private Mesa mesaSeleccionada;
    private boolean modoEdicion = false;
    private Mesa mesaEnArrastre = null;
    private Point initialClick;

    public vSeleccionMesa() {
        initComponents();

        try {
            mesasDAO = new MesasDAO();
            mesasDAO.cargarMesasPorDefecto(); // Cargar mesas por defecto si no hay ninguna

            configurarUI();
            cargarMesas();
            cargarImagenFondo();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar la ventana: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarUI() {
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Selección de Mesa");

        // Panel principal con scroll
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Panel para dibujar las mesas
        panelMesas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dibujar imagen de fondo si existe
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };

        panelMesas.setLayout(null); // Layout absoluto para posicionar las mesas libremente
        panelMesas.setPreferredSize(new Dimension(800, 600)); // Tamaño inicial

        scrollPane.setViewportView(panelMesas);

        // Panel de controles
        JPanel panelControles = new JPanel();
        panelControles.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Controles",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Dialog", Font.BOLD, 12),
                Color.BLACK));

        // Botones para gestionar mesas
        JButton btnNuevaMesa = new JButton("Nueva Mesa");
        JButton btnEliminarMesa = new JButton("Eliminar Mesa");
        JButton btnEditarMesas = new JButton("Editar Mesas");
        JButton btnGuardarCambios = new JButton("Guardar Cambios");
        JButton btnCambiarFondo = new JButton("Cambiar Fondo");

        // Configurar panel de leyenda (estados de mesas)
        JPanel panelLeyenda = new JPanel();
        panelLeyenda.setBorder(BorderFactory.createTitledBorder("Leyenda"));
        panelLeyenda.setLayout(new GridLayout(0, 1, 5, 5));

        // Agregar leyenda para cada estado
        for (Mesa.EstadoMesa estado : Mesa.EstadoMesa.values()) {
            JPanel panelEstado = new JPanel(new BorderLayout());
            JPanel colorBox = new JPanel();
            colorBox.setBackground(estado.getColor());
            colorBox.setPreferredSize(new Dimension(20, 20));
            panelEstado.add(colorBox, BorderLayout.WEST);
            panelEstado.add(new JLabel(" " + estado.getDescripcion()), BorderLayout.CENTER);
            panelLeyenda.add(panelEstado);
        }

        // Configurar eventos de botones
        btnNuevaMesa.addActionListener(e -> agregarNuevaMesa());
        btnEliminarMesa.addActionListener(e -> eliminarMesaSeleccionada());
        btnEditarMesas.addActionListener(e -> toggleModoEdicion());
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnCambiarFondo.addActionListener(e -> cambiarImagenFondo());

        // Agregar botones al panel de controles
        panelControles.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelControles.add(btnNuevaMesa, gbc);

        gbc.gridx = 1;
        panelControles.add(btnEliminarMesa, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelControles.add(btnEditarMesas, gbc);

        gbc.gridx = 1;
        panelControles.add(btnGuardarCambios, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panelControles.add(btnCambiarFondo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panelControles.add(panelLeyenda, gbc);

        // Configurar el layout principal
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panelControles, BorderLayout.EAST);

        pack();
    }

    private void cargarMesas() throws SQLException {
        mesas = mesasDAO.listarTodas();
        panelMesas.removeAll();

        for (Mesa mesa : mesas) {
            JPanel mesaPanel = crearPanelMesa(mesa);
            panelMesas.add(mesaPanel);
        }

        panelMesas.revalidate();
        panelMesas.repaint();
    }

    private JPanel crearPanelMesa(Mesa mesa) {
        // Crear panel para representar la mesa
        JPanel mesaPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Establecer color según el estado
                g2d.setColor(mesa.getEstado().getColor());

                // Dibujar la forma de la mesa
                if ("CIRCULAR".equals(mesa.getForma())) {
                    g2d.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                } else {
                    g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                // Dibujar borde
                g2d.setColor(Color.BLACK);
                if ("CIRCULAR".equals(mesa.getForma())) {
                    g2d.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                } else {
                    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }

                // Dibujar número de mesa
                g2d.setColor(Color.WHITE);
                Font font = new Font("Dialog", Font.BOLD, 16);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = mesa.getNumero();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g2d.drawString(text, (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight / 2) / 2);
            }
        };

        // Configurar propiedades del panel
        mesaPanel.setBounds(mesa.getPosicion().x, mesa.getPosicion().y,
                mesa.getAncho(), mesa.getAlto());
        mesaPanel.setToolTipText("Mesa " + mesa.getNumero() + " - "
                + mesa.getEstado().getDescripcion()
                + " - Capacidad: " + mesa.getCapacidad());

        // Agregar eventos de mouse
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Deseleccionar mesa anterior si existe
                if (mesaSeleccionada != null) {
                    for (Component comp : panelMesas.getComponents()) {
                        if (comp instanceof JComponent
                                && comp.getBounds().x == mesaSeleccionada.getPosicion().x
                                && comp.getBounds().y == mesaSeleccionada.getPosicion().y) {
                            ((JComponent) comp).setBorder(null);
                            break;
                        }
                    }
                }

                // Seleccionar esta mesa
                mesaSeleccionada = mesa;
                if (mesaPanel instanceof JComponent) {
                    ((JComponent) mesaPanel).setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                }

                // Mostrar menú contextual si está en modo edición
                if (modoEdicion) {
                    JPopupMenu popup = crearMenuContextual(mesa, mesaPanel);
                    popup.show(mesaPanel, e.getX(), e.getY());
                } else if (e.getClickCount() == 2) {
                    // Doble clic para abrir la ventana de ventas
                    abrirVentanaVentas(mesa);
                }
            }
        });

        // Agregar soporte para arrastrar si está en modo edición
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modoEdicion) {
                    mesaEnArrastre = mesa;
                    initialClick = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mesaEnArrastre = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (modoEdicion && mesaEnArrastre == mesa) {
                    // Actualizar posición
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;

                    int newX = mesaPanel.getX() + deltaX;
                    int newY = mesaPanel.getY() + deltaY;

                    // Asegurar que no se salga del panel
                    newX = Math.max(0, Math.min(newX, panelMesas.getWidth() - mesaPanel.getWidth()));
                    newY = Math.max(0, Math.min(newY, panelMesas.getHeight() - mesaPanel.getHeight()));

                    mesaPanel.setLocation(newX, newY);
                    mesa.setPosicion(new Point(newX, newY));
                }
            }
        };

        mesaPanel.addMouseListener(dragAdapter);
        mesaPanel.addMouseMotionListener(dragAdapter);

        return mesaPanel;
    }

    private void cargarImagenFondo() {
        try {
            // Primero intentar cargar la imagen desde el directorio de recursos
            File fondoFile = new File("src/main/resources/imagenes/fondo_restaurante.jpg");
            if (!fondoFile.exists()) {
                // Si no existe, usar una imagen por defecto (crear un fondo simple)
                backgroundImage = createDefaultBackground();
                return;
            }

            // Cargar la imagen desde el archivo
            BufferedImage img = ImageIO.read(fondoFile);
            backgroundImage = img;

            // Repintar el panel
            panelMesas.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar la imagen de fondo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            // Crear fondo por defecto en caso de error
            backgroundImage = createDefaultBackground();
        }
    }

    private Image createDefaultBackground() {
        // Crear una imagen de fondo por defecto (cuadrícula)
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        // Fondo blanco
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, 800, 600);

        // Dibujar cuadrícula
        g2d.setColor(new Color(200, 200, 200));
        for (int x = 0; x < 800; x += 50) {
            g2d.drawLine(x, 0, x, 600);
        }
        for (int y = 0; y < 600; y += 50) {
            g2d.drawLine(0, y, 800, y);
        }

        g2d.dispose();
        return img;
    }

    private void cambiarImagenFondo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen de fondo");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Cargar la imagen seleccionada
                File selectedFile = fileChooser.getSelectedFile();
                BufferedImage img = ImageIO.read(selectedFile);
                backgroundImage = img;

                // Guardar la imagen en el directorio de recursos
                File destDir = new File("src/main/resources/imagenes");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                File destFile = new File(destDir, "fondo_restaurante.jpg");
                ImageIO.write(img, "jpg", destFile);

                // Repintar el panel
                panelMesas.repaint();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al cargar la imagen: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarNuevaMesa() {
        // Diálogo para ingresar datos de la nueva mesa
        JTextField numeroField = new JTextField();
        JSpinner capacidadSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        JComboBox<String> formaCombo = new JComboBox<>(new String[]{"CIRCULAR", "RECTANGULAR", "CUADRADA"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Número:"));
        panel.add(numeroField);
        panel.add(new JLabel("Capacidad:"));
        panel.add(capacidadSpinner);
        panel.add(new JLabel("Forma:"));
        panel.add(formaCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Nueva Mesa", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && !numeroField.getText().trim().isEmpty()) {
            try {
                // Crear nueva mesa
                String numero = numeroField.getText();
                int capacidad = (Integer) capacidadSpinner.getValue();
                String forma = (String) formaCombo.getSelectedItem();

                // Encontrar una posición disponible
                Point posicion = encontrarPosicionDisponible();

                Mesa nuevaMesa = new Mesa(0, numero, Mesa.EstadoMesa.DISPONIBLE,
                        posicion, capacidad, 60, 60, forma);

                // Guardar en la base de datos
                int id = mesasDAO.guardar(nuevaMesa);
                nuevaMesa.setId(id);

                // Agregar a la lista y panel
                mesas.add(nuevaMesa);
                JPanel mesaPanel = crearPanelMesa(nuevaMesa);
                panelMesas.add(mesaPanel);

                panelMesas.revalidate();
                panelMesas.repaint();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al crear nueva mesa: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Point encontrarPosicionDisponible() {
        // Recorrer el panel en busca de un espacio disponible
        boolean[][] ocupado = new boolean[panelMesas.getWidth() / 50][panelMesas.getHeight() / 50];

        // Marcar las posiciones ocupadas
        for (Mesa mesa : mesas) {
            int gridX = mesa.getPosicion().x / 50;
            int gridY = mesa.getPosicion().y / 50;

            if (gridX < ocupado.length && gridY < ocupado[0].length) {
                ocupado[gridX][gridY] = true;
            }
        }

        // Buscar la primera celda disponible
        for (int y = 1; y < ocupado[0].length - 1; y++) {
            for (int x = 1; x < ocupado.length - 1; x++) {
                if (!ocupado[x][y]) {
                    return new Point(x * 50, y * 50);
                }
            }
        }

        // Si no hay disponibles, usar una posición aleatoria
        int x = (int) (Math.random() * (panelMesas.getWidth() - 100) + 50);
        int y = (int) (Math.random() * (panelMesas.getHeight() - 100) + 50);
        return new Point(x, y);
    }

    private void eliminarMesaSeleccionada() {
        if (mesaSeleccionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una mesa para eliminar",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar la mesa " + mesaSeleccionada.getNumero() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Eliminar de la base de datos
                mesasDAO.eliminar(mesaSeleccionada.getId());

                // Eliminar de la interfaz gráfica
                for (Component comp : panelMesas.getComponents()) {
                    if (comp instanceof JPanel
                            && comp.getBounds().x == mesaSeleccionada.getPosicion().x
                            && comp.getBounds().y == mesaSeleccionada.getPosicion().y) {
                        panelMesas.remove(comp);
                        break;
                    }
                }

                // Eliminar de la lista en memoria
                mesas.remove(mesaSeleccionada);
                mesaSeleccionada = null;

                panelMesas.revalidate();
                panelMesas.repaint();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar mesa: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleModoEdicion() {
        modoEdicion = !modoEdicion;

        // Cambiar el cursor según el modo
        if (modoEdicion) {
            panelMesas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            JOptionPane.showMessageDialog(this,
                    "Modo edición activado. Puede arrastrar las mesas o hacer clic derecho para editar sus propiedades.",
                    "Modo Edición", JOptionPane.INFORMATION_MESSAGE);
        } else {
            panelMesas.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void guardarCambios() {
        try {
            // Guardar cambios de todas las mesas
            for (Mesa mesa : mesas) {
                mesasDAO.actualizar(mesa);
            }

            JOptionPane.showMessageDialog(this,
                    "Cambios guardados correctamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar cambios: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPopupMenu crearMenuContextual(Mesa mesa, JPanel mesaPanel) {
        JPopupMenu popup = new JPopupMenu();

        // Opción para cambiar el número
        JMenuItem itemNumero = new JMenuItem("Cambiar Número");
        itemNumero.addActionListener(e -> {
            String nuevoNumero = JOptionPane.showInputDialog(this,
                    "Ingrese el nuevo número:", mesa.getNumero());
            if (nuevoNumero != null && !nuevoNumero.trim().isEmpty()) {
                mesa.setNumero(nuevoNumero);
                mesaPanel.repaint();
            }
        });

        // Opción para cambiar el estado
        JMenu submenuEstado = new JMenu("Cambiar Estado");
        for (Mesa.EstadoMesa estado : Mesa.EstadoMesa.values()) {
            JMenuItem itemEstado = new JMenuItem(estado.getDescripcion());
            itemEstado.addActionListener(e -> {
                mesa.setEstado(estado);
                mesaPanel.repaint();
                mesaPanel.setToolTipText("Mesa " + mesa.getNumero() + " - "
                        + estado.getDescripcion()
                        + " - Capacidad: " + mesa.getCapacidad());
            });
            submenuEstado.add(itemEstado);
        }

        // Opción para cambiar la capacidad
        JMenuItem itemCapacidad = new JMenuItem("Cambiar Capacidad");
        itemCapacidad.addActionListener(e -> {
            SpinnerNumberModel model = new SpinnerNumberModel(
                    mesa.getCapacidad(), 1, 20, 1);
            JSpinner spinner = new JSpinner(model);

            int option = JOptionPane.showConfirmDialog(this, spinner,
                    "Capacidad de la mesa", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                mesa.setCapacidad((Integer) spinner.getValue());
                mesaPanel.setToolTipText("Mesa " + mesa.getNumero() + " - "
                        + mesa.getEstado().getDescripcion()
                        + " - Capacidad: " + mesa.getCapacidad());
            }
        });

        // Opción para cambiar el tamaño
        JMenuItem itemTamano = new JMenuItem("Cambiar Tamaño");
        itemTamano.addActionListener(e -> {
            JSpinner spinnerAncho = new JSpinner(new SpinnerNumberModel(
                    mesa.getAncho(), 40, 150, 5));
            JSpinner spinnerAlto = new JSpinner(new SpinnerNumberModel(
                    mesa.getAlto(), 40, 150, 5));

            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            panel.add(new JLabel("Ancho:"));
            panel.add(spinnerAncho);
            panel.add(new JLabel("Alto:"));
            panel.add(spinnerAlto);

            int option = JOptionPane.showConfirmDialog(this, panel,
                    "Tamaño de la mesa", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                mesa.setAncho((Integer) spinnerAncho.getValue());
                mesa.setAlto((Integer) spinnerAlto.getValue());
                mesaPanel.setSize(mesa.getAncho(), mesa.getAlto());
                mesaPanel.repaint();
            }
        });

        // Opción para cambiar la forma
        JMenu submenuForma = new JMenu("Cambiar Forma");
        String[] formas = {"CIRCULAR", "RECTANGULAR", "CUADRADA"};
        for (String forma : formas) {
            JMenuItem itemForma = new JMenuItem(forma);
            itemForma.addActionListener(e -> {
                mesa.setForma(forma);
                mesaPanel.repaint();
            });
            submenuForma.add(itemForma);
        }

        // Agregar items al popup
        popup.add(itemNumero);
        popup.add(submenuEstado);
        popup.add(itemCapacidad);
        popup.add(itemTamano);
        popup.add(submenuForma);

        return popup;
    }

    private void abrirVentanaVentas(Mesa mesa) {
        // Aquí se implementará la lógica para abrir la ventana de ventas
        // cuando esté lista la siguiente parte
        JOptionPane.showMessageDialog(this,
                "Se seleccionó la mesa " + mesa.getNumero()
                + " (" + mesa.getEstado().getDescripcion() + ")\n"
                + "Próximamente se abrirá el formulario de ventas.",
                "Mesa Seleccionada", JOptionPane.INFORMATION_MESSAGE);

        // Cambiar estado a OCUPADA si estaba disponible
        if (mesa.getEstado() == Mesa.EstadoMesa.DISPONIBLE) {
            try {
                mesa.setEstado(Mesa.EstadoMesa.OCUPADA);
                mesasDAO.actualizarEstado(mesa.getId(), Mesa.EstadoMesa.OCUPADA);

                // Actualizar la visualización
                for (Component comp : panelMesas.getComponents()) {
                    if (comp instanceof JPanel
                            && comp.getBounds().x == mesa.getPosicion().x
                            && comp.getBounds().y == mesa.getPosicion().y) {
                        comp.repaint();
                        break;
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar estado: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnNuevaMesa = new javax.swing.JButton();
        btnEliminarMesa = new javax.swing.JButton();
        btnEditarMesas = new javax.swing.JButton();
        btnGuardarCambios = new javax.swing.JButton();
        btnCambiarFondo = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Selección de Mesa");

        jPanel1.setLayout(null);

        btnNuevaMesa.setText("Nueva Mesa");

        btnEliminarMesa.setText("Eliminar Mesa");

        btnEditarMesas.setText("Editar Mesas");

        btnGuardarCambios.setText("Guardar Cambios");

        btnCambiarFondo.setText("Cambiar Fondo");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNuevaMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEliminarMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEditarMesas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGuardarCambios, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(btnCambiarFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnNuevaMesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminarMesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarMesas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGuardarCambios, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCambiarFondo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2);
        jPanel2.setBounds(610, 0, 140, 250);

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Implementación de los métodos de la interfaz myInterface
    @Override
    public void imGrabar() {
        guardarCambios();
    }

    @Override
    public void imFiltrar() {
        // No aplicable para esta ventana
    }

    @Override
    public void imActualizar() {
        try {
            cargarMesas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar mesas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void imBorrar() {
        eliminarMesaSeleccionada();
    }

    @Override
    public void imNuevo() {
        agregarNuevaMesa();
    }

    @Override
    public void imBuscar() {
        // No aplicable para esta ventana
    }

    @Override
    public void imPrimero() {
        // No aplicable para esta ventana
    }

    @Override
    public void imSiguiente() {
        // No aplicable para esta ventana
    }

    @Override
    public void imAnterior() {
        // No aplicable para esta ventana
    }

    @Override
    public void imUltimo() {
        // No aplicable para esta ventana
    }

    @Override
    public void imImprimir() {
        // No aplicable para esta ventana
    }

    @Override
    public void imInsDet() {
        // No aplicable para esta ventana
    }

    @Override
    public void imDelDet() {
        // No aplicable para esta ventana
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
        return "mesas";
    }

    @Override
    public String[] getCamposBusqueda() {
        return new String[]{"id", "numero"};
    }

    @Override
    public void setRegistroSeleccionado(int id) {
        // Buscar la mesa con el ID especificado
        for (Mesa mesa : mesas) {
            if (mesa.getId() == id) {
                // Seleccionar esta mesa
                mesaSeleccionada = mesa;

                // Actualizar UI
                for (Component comp : panelMesas.getComponents()) {
                    if (comp instanceof JComponent) {
                        ((JComponent) comp).setBorder(null);
                        if (comp instanceof JPanel) {
                            Rectangle bounds = comp.getBounds();
                            if (bounds.x == mesa.getPosicion().x && bounds.y == mesa.getPosicion().y) {
                                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCambiarFondo;
    private javax.swing.JButton btnEditarMesas;
    private javax.swing.JButton btnEliminarMesa;
    private javax.swing.JButton btnGuardarCambios;
    private javax.swing.JButton btnNuevaMesa;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
