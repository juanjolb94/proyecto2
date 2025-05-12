package vista;

import interfaces.myInterface;
import modelo.Mesa;
import modelo.MesasDAO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class vSeleccionMesa extends javax.swing.JInternalFrame implements myInterface {

    private MesasDAO mesasDAO;
    private List<Mesa> mesas;
    private Image backgroundImage;
    private JPanel panelMesas;
    private Mesa mesaSeleccionada;
    private boolean modoEdicion = false;
    private Mesa mesaEnArrastre = null;
    private Point initialClick;

    // Constantes para la cuadrícula
    private final int COLUMNAS_POR_FILA = 5;
    private final int ESPACIO_ENTRE_MESAS = 10;
    private final int TAMANO_MESA_DEFAULT = 100;

    // Para controlar el estado de los botones
    private JButton btnEditarMesas;
    private Color colorOriginalBoton;

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
                // Dibujar imagen de fondo con transparencia si existe
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    // Guardar la configuración original
                    Composite originalComposite = g2d.getComposite();

                    // Aplicar transparencia (0.3f = 30% de opacidad)
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);

                    // Restaurar la configuración original
                    g2d.setComposite(originalComposite);
                }
            }
        };

        panelMesas.setLayout(null); // Layout absoluto para posicionar las mesas libremente

        // Establecer un tamaño preferido para tener suficiente espacio
        panelMesas.setPreferredSize(new Dimension(
                COLUMNAS_POR_FILA * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS) + ESPACIO_ENTRE_MESAS,
                400)); // Altura inicial, se ajustará según las mesas

        scrollPane.setViewportView(panelMesas);

        // Panel de controles con botones más grandes y uno debajo del otro
        JPanel panelControles = new JPanel();
        panelControles.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Controles",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Dialog", Font.BOLD, 12),
                Color.WHITE));
        panelControles.setBackground(new Color(60, 63, 65)); // Color oscuro para el panel de controles

        // Botones para gestionar mesas
        JButton btnNuevaMesa = crearBotonControl("Nueva Mesa");
        JButton btnEliminarMesa = crearBotonControl("Eliminar Mesa");
        btnEditarMesas = crearBotonControl("Editar Mesas");
        colorOriginalBoton = btnEditarMesas.getBackground();
        JButton btnGuardarCambios = crearBotonControl("Guardar Cambios");
        JButton btnCambiarFondo = crearBotonControl("Cambiar Fondo");

        // Configurar panel de leyenda (estados de mesas)
        JPanel panelLeyenda = new JPanel();
        panelLeyenda.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Leyenda",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Dialog", Font.BOLD, 12),
                Color.WHITE));
        panelLeyenda.setBackground(new Color(60, 63, 65)); // Color oscuro para el panel de leyenda
        panelLeyenda.setLayout(new GridLayout(0, 1, 5, 5));

        // Agregar leyenda para cada estado
        for (Mesa.EstadoMesa estado : Mesa.EstadoMesa.values()) {
            JPanel panelEstado = new JPanel(new BorderLayout());
            panelEstado.setBackground(new Color(60, 63, 65)); // Para mantener consistencia

            JPanel colorBox = new JPanel();
            colorBox.setBackground(estado.getColor());
            colorBox.setPreferredSize(new Dimension(20, 20));

            JLabel labelEstado = new JLabel(" " + estado.getDescripcion());
            labelEstado.setForeground(Color.WHITE); // Texto en blanco para mejor legibilidad

            panelEstado.add(colorBox, BorderLayout.WEST);
            panelEstado.add(labelEstado, BorderLayout.CENTER);
            panelLeyenda.add(panelEstado);
        }

        // Configurar eventos de botones
        btnNuevaMesa.addActionListener(e -> agregarNuevaMesa());
        btnEliminarMesa.addActionListener(e -> eliminarMesaSeleccionada());
        btnEditarMesas.addActionListener(e -> toggleModoEdicion());
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnCambiarFondo.addActionListener(e -> cambiarImagenFondo());

        // Layout de botones verticalmente (uno debajo del otro)
        panelControles.setLayout(new BoxLayout(panelControles, BoxLayout.Y_AXIS));
        panelControles.add(Box.createVerticalStrut(10)); // Espacio
        panelControles.add(btnNuevaMesa);
        panelControles.add(Box.createVerticalStrut(10)); // Espacio
        panelControles.add(btnEliminarMesa);
        panelControles.add(Box.createVerticalStrut(10)); // Espacio
        panelControles.add(btnEditarMesas);
        panelControles.add(Box.createVerticalStrut(10)); // Espacio
        panelControles.add(btnGuardarCambios);
        panelControles.add(Box.createVerticalStrut(10)); // Espacio
        panelControles.add(btnCambiarFondo);
        panelControles.add(Box.createVerticalStrut(20)); // Espacio mayor antes de la leyenda
        panelControles.add(panelLeyenda);

        // Configurar el layout principal
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panelControles, BorderLayout.EAST);

        pack();
    }

    // Método para crear botones de control con estilo unificado
    private JButton crearBotonControl(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Dialog", Font.BOLD, 14));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar horizontalmente
        boton.setMaximumSize(new Dimension(200, 50)); // Ancho máximo y altura fija
        boton.setPreferredSize(new Dimension(180, 50)); // Tamaño preferido
        boton.setBackground(new Color(59, 89, 152)); // Azul tipo Facebook
        boton.setForeground(Color.WHITE); // Texto blanco
        boton.setFocusPainted(false); // Quitar borde de foco
        boton.setBorderPainted(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano al pasar sobre el botón
        return boton;
    }

    private void cargarMesas() throws SQLException {
        mesas = mesasDAO.listarTodas();
        panelMesas.removeAll();

        // Ordenar mesas por número para asegurar que se muestren en orden
        mesas.sort((m1, m2) -> {
            try {
                int num1 = Integer.parseInt(m1.getNumero());
                int num2 = Integer.parseInt(m2.getNumero());
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                return m1.getNumero().compareTo(m2.getNumero());
            }
        });

        // Calcular posiciones en cuadrícula
        calcularPosicionesCuadricula();

        // Crear y agregar los paneles de mesa
        for (Mesa mesa : mesas) {
            JPanel mesaPanel = crearPanelMesa(mesa);
            panelMesas.add(mesaPanel);
        }

        // Ajustar el tamaño del panel según las filas necesarias
        ajustarTamanoPanelMesas();

        panelMesas.revalidate();
        panelMesas.repaint();
    }

    private void calcularPosicionesCuadricula() {
        int totalMesas = mesas.size();
        int filas = (totalMesas + COLUMNAS_POR_FILA - 1) / COLUMNAS_POR_FILA; // Redondear hacia arriba

        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);

            // Calcular fila y columna
            int fila = i / COLUMNAS_POR_FILA;
            int columna = i % COLUMNAS_POR_FILA;

            // Calcular posición X,Y
            int x = ESPACIO_ENTRE_MESAS + columna * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS);
            int y = ESPACIO_ENTRE_MESAS + fila * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS);

            // Actualizar la posición de la mesa
            mesa.setPosicion(new Point(x, y));

            // Asegurar que el tamaño es el estándar
            mesa.setAncho(TAMANO_MESA_DEFAULT);
            mesa.setAlto(TAMANO_MESA_DEFAULT);
        }
    }

    private void ajustarTamanoPanelMesas() {
        int totalMesas = mesas.size();
        int filas = (totalMesas + COLUMNAS_POR_FILA - 1) / COLUMNAS_POR_FILA; // Redondear hacia arriba

        int altura = (filas * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS)) + ESPACIO_ENTRE_MESAS;
        int anchura = (COLUMNAS_POR_FILA * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS)) + ESPACIO_ENTRE_MESAS;

        panelMesas.setPreferredSize(new Dimension(anchura, altura));
        panelMesas.revalidate();
    }

    private JPanel crearPanelMesa(Mesa mesa) {
        // Crear panel para representar la mesa como un botón cuadrado
        JPanel mesaPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Antialiasing para bordes más suaves
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Establecer color según el estado
                g2d.setColor(mesa.getEstado().getColor());

                // Dibujar la forma de la mesa (cuadrado redondeado)
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);

                // Dibujar borde
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);

                // Dibujar número de mesa
                g2d.setColor(Color.WHITE);
                Font font = new Font("Dialog", Font.BOLD, 32); // Fuente más grande
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = mesa.getNumero();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g2d.drawString(text, (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight / 2) / 2 - 10); // Ajuste para centrar mejor

                // Dibujar capacidad
                Font smallFont = new Font("Dialog", Font.PLAIN, 14);
                g2d.setFont(smallFont);
                FontMetrics fmSmall = g2d.getFontMetrics();
                String capacidadText = mesa.getCapacidad() + " personas";
                int smallTextWidth = fmSmall.stringWidth(capacidadText);
                g2d.drawString(capacidadText,
                        (getWidth() - smallTextWidth) / 2,
                        getHeight() - 15);
            }
        };

        // Configurar propiedades del panel
        mesaPanel.setBounds(mesa.getPosicion().x, mesa.getPosicion().y,
                mesa.getAncho(), mesa.getAlto());
        mesaPanel.setToolTipText("Mesa " + mesa.getNumero() + " - "
                + mesa.getEstado().getDescripcion()
                + " - Capacidad: " + mesa.getCapacidad());

        // Agregar efecto de hover
        mesaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!modoEdicion) {
                    mesaPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    mesaPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!modoEdicion && mesaSeleccionada != mesa) {
                    mesaPanel.setBorder(null);
                }
            }
        });

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
                    mesaPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (modoEdicion) {
                    mesaEnArrastre = null;
                    mesaPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
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
        // Crear una imagen de fondo por defecto (cuadrícula sutil)
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

        // Obtener el siguiente número de mesa disponible
        int siguienteNumero = obtenerSiguienteNumeroMesa();
        numeroField.setText(String.valueOf(siguienteNumero));

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Número:"));
        panel.add(numeroField);
        panel.add(new JLabel("Capacidad:"));
        panel.add(capacidadSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Nueva Mesa", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && !numeroField.getText().trim().isEmpty()) {
            try {
                // Crear nueva mesa
                String numero = numeroField.getText();
                int capacidad = (Integer) capacidadSpinner.getValue();

                // Calcular la posición para la nueva mesa
                Point posicion = calcularPosicionParaNuevaMesa();

                Mesa nuevaMesa = new Mesa(0, numero, Mesa.EstadoMesa.DISPONIBLE,
                        posicion, capacidad, TAMANO_MESA_DEFAULT, TAMANO_MESA_DEFAULT, "RECTANGULAR");

                // Guardar en la base de datos
                int id = mesasDAO.guardar(nuevaMesa);
                nuevaMesa.setId(id);

                // Agregar a la lista
                mesas.add(nuevaMesa);

                // Recalcular todas las posiciones
                calcularPosicionesCuadricula();

                // Actualizar el panel con todas las mesas
                panelMesas.removeAll();
                for (Mesa mesa : mesas) {
                    JPanel mesaPanel = crearPanelMesa(mesa);
                    panelMesas.add(mesaPanel);
                }

                // Ajustar el tamaño del panel según las filas necesarias
                ajustarTamanoPanelMesas();

                panelMesas.revalidate();
                panelMesas.repaint();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al crear nueva mesa: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int obtenerSiguienteNumeroMesa() {
        int mayor = 0;
        for (Mesa mesa : mesas) {
            try {
                int numeroMesa = Integer.parseInt(mesa.getNumero());
                if (numeroMesa > mayor) {
                    mayor = numeroMesa;
                }
            } catch (NumberFormatException e) {
                // Ignorar si no es un número
            }
        }
        return mayor + 1;
    }

    private Point calcularPosicionParaNuevaMesa() {
        int totalMesas = mesas.size();

        // Calcular fila y columna para la próxima mesa
        int fila = totalMesas / COLUMNAS_POR_FILA;
        int columna = totalMesas % COLUMNAS_POR_FILA;

        // Calcular posición X,Y
        int x = ESPACIO_ENTRE_MESAS + columna * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS);
        int y = ESPACIO_ENTRE_MESAS + fila * (TAMANO_MESA_DEFAULT + ESPACIO_ENTRE_MESAS);

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

                // Eliminar de la lista en memoria
                mesas.remove(mesaSeleccionada);
                mesaSeleccionada = null;

                // Recalcular posiciones
                calcularPosicionesCuadricula();

                // Actualizar la vista
                panelMesas.removeAll();
                for (Mesa mesa : mesas) {
                    JPanel mesaPanel = crearPanelMesa(mesa);
                    panelMesas.add(mesaPanel);
                }

                // Ajustar el tamaño del panel
                ajustarTamanoPanelMesas();

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

        // Cambiar el cursor y el color del botón según el modo
        if (modoEdicion) {
            panelMesas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            // Cambiar el color del botón a un rojo intenso para indicar que está activo
            btnEditarMesas.setBackground(new Color(204, 0, 0));
            btnEditarMesas.setText("Finalizar Edición");
            JOptionPane.showMessageDialog(this,
                    "Modo edición activado. Puede arrastrar las mesas o hacer clic en ellas para editar sus propiedades.",
                    "Modo Edición", JOptionPane.INFORMATION_MESSAGE);
        } else {
            panelMesas.setCursor(Cursor.getDefaultCursor());
            // Restaurar el color original del botón
            btnEditarMesas.setBackground(colorOriginalBoton);
            btnEditarMesas.setText("Editar Mesas");

            // Al salir del modo edición, preguntar si se quiere reorganizar
            if (JOptionPane.showConfirmDialog(this,
                    "¿Desea reorganizar las mesas en una cuadrícula ordenada?",
                    "Reorganizar mesas",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                calcularPosicionesCuadricula();

                // Actualizar la vista
                panelMesas.removeAll();
                for (Mesa mesa : mesas) {
                    JPanel mesaPanel = crearPanelMesa(mesa);
                    panelMesas.add(mesaPanel);
                }

                // Ajustar el tamaño del panel
                ajustarTamanoPanelMesas();

                panelMesas.revalidate();
                panelMesas.repaint();
            }
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
        popup.setBackground(new Color(60, 63, 65)); // Fondo oscuro

        // Opción para cambiar el número
        JMenuItem itemNumero = crearItemMenu("Cambiar Número");
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
        submenuEstado.setForeground(Color.WHITE);
        for (Mesa.EstadoMesa estado : Mesa.EstadoMesa.values()) {
            JMenuItem itemEstado = crearItemMenu(estado.getDescripcion());
            itemEstado.setIcon(createColorIcon(estado.getColor(), 12, 12));
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
        JMenuItem itemCapacidad = crearItemMenu("Cambiar Capacidad");
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
                mesaPanel.repaint();
            }
        });

        // Agregar items al popup
        popup.add(itemNumero);
        popup.add(submenuEstado);
        popup.add(itemCapacidad);

        return popup;
    }

    // Método para crear ítem de menú con estilo unificado
    private JMenuItem crearItemMenu(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setForeground(Color.WHITE);
        item.setBackground(new Color(60, 63, 65)); // Fondo oscuro
        return item;
    }

    // Método para crear íconos de color
    private ImageIcon createColorIcon(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);
        g2d.dispose();
        return new ImageIcon(image);
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnNuevaMesa = new javax.swing.JButton();
        btnEliminarMesa = new javax.swing.JButton();
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
                    .addComponent(btnGuardarCambios, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(btnCambiarFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnNuevaMesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminarMesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCambiarFondo;
    private javax.swing.JButton btnEliminarMesa;
    private javax.swing.JButton btnGuardarCambios;
    private javax.swing.JButton btnNuevaMesa;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
