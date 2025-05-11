package vista;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import controlador.cGestProd.ItemCombo;
import modelo.ProductosDAO;

public class vFiltroInventario extends JDialog {

    // Variables para el estado
    private boolean aceptado = false;

    /**
     * Constructor para el diálogo de filtro de inventario
     *
     * @param parent Ventana padre
     * @param modal True para hacer el diálogo modal
     */
    public vFiltroInventario(Frame parent, boolean modal) {
        super(parent, "Filtro de Inventario", modal);
        initComponents();

        try {
            cargarCategorias();
            cargarMarcas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar datos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Inicializar los spinners con valores predeterminados
        spinnerStockDesde.setValue(0);
        spinnerStockHasta.setValue(999999);

        // Configurar los manejadores de eventos para los botones
        configurarBotones();

        setLocationRelativeTo(parent);
    }

    /**
     * Configura los eventos de los botones
     */
    private void configurarBotones() {
        // Botón Aceptar
        btnAceptar.addActionListener(e -> {
            if (validarFiltros()) {
                aceptado = true;
                dispose(); // Cierra el diálogo
            }
        });

        // Botón Cancelar
        btnCancelar.addActionListener(e -> {
            aceptado = false;
            dispose(); // Cierra el diálogo sin aplicar filtros
        });

        // Botón Limpiar
        btnLimpiar.addActionListener(e -> {
            limpiarFiltros();
        });

        // También cerrar con ESC (cancelar)
        getRootPane().registerKeyboardAction(
                e -> {
                    aceptado = false;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Configurar Enter para aceptar
        getRootPane().setDefaultButton(btnAceptar);
    }

    /**
     * Validar que los filtros sean coherentes
     *
     * @return true si los filtros son válidos
     */
    private boolean validarFiltros() {
        int stockMin = getStockMinimo();
        int stockMax = getStockMaximo();

        if (stockMin > stockMax) {
            JOptionPane.showMessageDialog(this,
                    "El stock mínimo no puede ser mayor que el stock máximo",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            spinnerStockDesde.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Carga las categorías disponibles en el ComboBox
     *
     * @throws SQLException si ocurre un error en la base de datos
     */
    private void cargarCategorias() throws SQLException {
        // Limpiar el combo
        comboCategorias.removeAllItems();

        // Agregar elemento para "Todas las categorías"
        comboCategorias.addItem(new ItemCombo<>(0, "Todas las categorías"));

        try {
            // Obtener categorías desde la base de datos
            ProductosDAO productosDAO = new ProductosDAO();
            List<ItemCombo<Integer>> categorias = productosDAO.obtenerCategorias();

            for (ItemCombo<Integer> categoria : categorias) {
                comboCategorias.addItem(categoria);
            }

        } catch (SQLException e) {
            throw e;
        }

        // Seleccionar el primer elemento por defecto
        if (comboCategorias.getItemCount() > 0) {
            comboCategorias.setSelectedIndex(0);
        }
    }

    /**
     * Carga las marcas disponibles en el ComboBox
     *
     * @throws SQLException si ocurre un error en la base de datos
     */
    private void cargarMarcas() throws SQLException {
        // Limpiar el combo
        comboMarcas.removeAllItems();

        // Agregar elemento para "Todas las marcas"
        comboMarcas.addItem(new ItemCombo<>(0, "Todas las marcas"));

        try {
            // Obtener marcas desde la base de datos
            ProductosDAO productosDAO = new ProductosDAO();
            List<ItemCombo<Integer>> marcas = productosDAO.obtenerMarcas();

            for (ItemCombo<Integer> marca : marcas) {
                comboMarcas.addItem(marca);
            }

        } catch (SQLException e) {
            throw e;
        }

        // Seleccionar el primer elemento por defecto
        if (comboMarcas.getItemCount() > 0) {
            comboMarcas.setSelectedIndex(0);
        }
    }

    /**
     * Limpia todos los filtros establecidos
     */
    private void limpiarFiltros() {
        // Seleccionar la primera opción en los combos (normalmente "Todos")
        if (comboCategorias.getItemCount() > 0) {
            comboCategorias.setSelectedIndex(0);
        }

        if (comboMarcas.getItemCount() > 0) {
            comboMarcas.setSelectedIndex(0);
        }

        // Desmarcar mostrar inactivos
        chkMostrarInactivos.setSelected(false);

        // Resetear valores de stock
        spinnerStockDesde.setValue(0);
        spinnerStockHasta.setValue(999999);
    }

    /**
     * Obtener el ID de categoría seleccionada
     *
     * @return ID de categoría (0 para todas)
     */
    public int getCategoriaId() {
        ItemCombo<Integer> item = (ItemCombo<Integer>) comboCategorias.getSelectedItem();
        return item != null ? item.getValor() : 0;
    }

    /**
     * Obtener el ID de marca seleccionada
     *
     * @return ID de marca (0 para todas)
     */
    public int getMarcaId() {
        ItemCombo<Integer> item = (ItemCombo<Integer>) comboMarcas.getSelectedItem();
        return item != null ? item.getValor() : 0;
    }

    /**
     * Verificar si se deben mostrar productos inactivos
     *
     * @return true si se deben mostrar inactivos
     */
    public boolean getMostrarInactivos() {
        return chkMostrarInactivos.isSelected();
    }

    /**
     * Obtener el valor mínimo de stock para filtrar
     *
     * @return valor mínimo de stock
     */
    public int getStockMinimo() {
        return (Integer) spinnerStockDesde.getValue();
    }

    /**
     * Obtener el valor máximo de stock para filtrar
     *
     * @return valor máximo de stock
     */
    public int getStockMaximo() {
        return (Integer) spinnerStockHasta.getValue();
    }

    /**
     * Verificar si el usuario aceptó el filtro
     *
     * @return true si se aceptó, false si se canceló
     */
    public boolean isAceptado() {
        return aceptado;
    }

    /**
     * Método principal para probar el diálogo independientemente
     */
    public static void main(String args[]) {
        try {
            // Establecer Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ejecutar en el Event Dispatch Thread
        java.awt.EventQueue.invokeLater(() -> {
            vFiltroInventario dialog = new vFiltroInventario(new JFrame(), true);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);

            // Para probar, mostrar los resultados después de cerrar el diálogo
            if (dialog.isAceptado()) {
                System.out.println("Filtros aceptados:");
                System.out.println("Categoría ID: " + dialog.getCategoriaId());
                System.out.println("Marca ID: " + dialog.getMarcaId());
                System.out.println("Mostrar inactivos: " + dialog.getMostrarInactivos());
                System.out.println("Stock mínimo: " + dialog.getStockMinimo());
                System.out.println("Stock máximo: " + dialog.getStockMaximo());
            } else {
                System.out.println("Filtros cancelados");
            }

            System.exit(0);
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        panelFiltros = new javax.swing.JPanel();
        lblCategoria = new javax.swing.JLabel();
        comboCategorias = new javax.swing.JComboBox<>();
        lblMarca = new javax.swing.JLabel();
        comboMarcas = new javax.swing.JComboBox<>();
        chkMostrarInactivos = new javax.swing.JCheckBox();
        lblDesde = new javax.swing.JLabel();
        spinnerStockDesde = new javax.swing.JSpinner();
        lblHasta = new javax.swing.JLabel();
        spinnerStockHasta = new javax.swing.JSpinner();
        panelBotones = new javax.swing.JPanel();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelPrincipal.setLayout(new java.awt.BorderLayout());

        lblCategoria.setText("Categoria:");

        lblMarca.setText("Marca:");

        chkMostrarInactivos.setText("Mostrar productos inactivos");

        lblDesde.setText("Stock Desde:");

        lblHasta.setText("Stock Hasta:");

        javax.swing.GroupLayout panelFiltrosLayout = new javax.swing.GroupLayout(panelFiltros);
        panelFiltros.setLayout(panelFiltrosLayout);
        panelFiltrosLayout.setHorizontalGroup(
            panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFiltrosLayout.createSequentialGroup()
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategoria)
                            .addComponent(lblMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(chkMostrarInactivos)
                    .addGroup(panelFiltrosLayout.createSequentialGroup()
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDesde)
                            .addComponent(lblHasta))
                        .addGap(28, 28, 28)
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerStockHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinnerStockDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelFiltrosLayout.setVerticalGroup(
            panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltrosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategoria)
                    .addComponent(comboCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMarca)
                    .addComponent(comboMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkMostrarInactivos)
                .addGap(18, 18, 18)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDesde)
                    .addComponent(spinnerStockDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHasta)
                    .addComponent(spinnerStockHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAceptar.setText("Aceptar");

        btnCancelar.setText("Cancelar");

        btnLimpiar.setText("Limpiar");

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnAceptar, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addComponent(btnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JCheckBox chkMostrarInactivos;
    private javax.swing.JComboBox<ItemCombo<Integer>> comboCategorias;
    private javax.swing.JComboBox<ItemCombo<Integer>> comboMarcas;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JSpinner spinnerStockDesde;
    private javax.swing.JSpinner spinnerStockHasta;
    // End of variables declaration//GEN-END:variables
}
