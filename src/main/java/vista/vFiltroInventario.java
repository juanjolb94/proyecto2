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

        setLocationRelativeTo(parent);
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
                .addGap(43, 43, 43)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkMostrarInactivos)
                    .addGroup(panelFiltrosLayout.createSequentialGroup()
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategoria)
                            .addComponent(lblMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(95, 95, 95)
                        .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(comboCategorias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(comboMarcas, 0, 182, Short.MAX_VALUE)))
                    .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(panelFiltrosLayout.createSequentialGroup()
                            .addComponent(lblHasta)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spinnerStockHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelFiltrosLayout.createSequentialGroup()
                            .addComponent(lblDesde)
                            .addGap(18, 18, 18)
                            .addComponent(spinnerStockDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelFiltrosLayout.setVerticalGroup(
            panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltrosLayout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCategoria))
                .addGap(28, 28, 28)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMarca)
                    .addComponent(comboMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addComponent(chkMostrarInactivos)
                .addGap(37, 37, 37)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDesde)
                    .addComponent(spinnerStockDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(panelFiltrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHasta)
                    .addComponent(spinnerStockHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        btnAceptar.setText("Aceptar");

        btnCancelar.setText("Cancelar");

        btnLimpiar.setText("Limpiar");

        javax.swing.GroupLayout panelBotonesLayout = new javax.swing.GroupLayout(panelBotones);
        panelBotones.setLayout(panelBotonesLayout);
        panelBotonesLayout.setHorizontalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(btnAceptar)
                .addGap(87, 87, 87)
                .addComponent(btnCancelar)
                .addGap(87, 87, 87)
                .addComponent(btnLimpiar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesLayout.setVerticalGroup(
            panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(panelBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptar)
                    .addComponent(btnCancelar)
                    .addComponent(btnLimpiar))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelBotones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
