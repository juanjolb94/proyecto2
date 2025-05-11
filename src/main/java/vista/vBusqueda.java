package vista;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import modelo.DatabaseConnection;
import interfaces.myInterface;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class vBusqueda extends javax.swing.JInternalFrame {

    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private myInterface ventanaOrigen;
    private String tabla;
    private String[] campos;
    private Connection conn;

    public vBusqueda(java.awt.Frame parent, boolean modal, myInterface ventanaOrigen) {
        initComponents();

        if (ventanaOrigen == null) {
            JOptionPane.showMessageDialog(parent, "Error: Ventana origen no válida", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        this.ventanaOrigen = ventanaOrigen;
        this.tabla = ventanaOrigen.getTablaActual();
        this.campos = ventanaOrigen.getCamposBusqueda();

        configurarTabla();
        // No cargar datos inicialmente
        // cargarDatos(); <-- Comentar o eliminar esta línea
        configurarFiltro();
        configurarEventos();

        if (ventanaOrigen instanceof JInternalFrame) {
            this.setTitle("Buscar en " + ((JInternalFrame) ventanaOrigen).getTitle());
        }
    }

    private void configurarTabla() {
        modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelo.addColumn("ID");
        modelo.addColumn("Descripción");

        // Inicializar la tabla vacía
        modelo.setRowCount(0);

        tblResultados.setModel(modelo);
        tblResultados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblResultados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblResultados.getTableHeader().setReorderingAllowed(false);
    }

    private void cargarDatos(String filtro) {
        try {
            conn = DatabaseConnection.getConnection();
            StringBuilder sqlBuilder = new StringBuilder();

            // Seleccionar las columnas sin alias que puedan interferir con el WHERE
            sqlBuilder.append("SELECT ").append(campos[0]).append(", ").append(campos[1]);
            sqlBuilder.append(" FROM ").append(tabla);

            // Si hay un filtro, aplicarlo a la consulta SQL correctamente
            if (filtro != null && !filtro.isEmpty()) {
                // Asegurarse de que el campo[1] no tenga alias que interfieran con el LIKE
                // Si campos[1] contiene un alias (como "campo AS alias"), extraer solo el nombre del campo
                String campoBusqueda = campos[1];
                if (campoBusqueda.toUpperCase().contains(" AS ")) {
                    campoBusqueda = campoBusqueda.substring(0, campoBusqueda.toUpperCase().indexOf(" AS "));
                }

                sqlBuilder.append(" WHERE ").append(campoBusqueda).append(" LIKE ?");
            }

            String sql = sqlBuilder.toString();
            System.out.println("SQL Query: " + sql); // Para debug

            PreparedStatement ps = conn.prepareStatement(sql);

            // Establecer el parámetro si hay filtro
            if (filtro != null && !filtro.isEmpty()) {
                ps.setString(1, "%" + filtro + "%");
            }

            ResultSet rs = ps.executeQuery();

            modelo.setRowCount(0);

            while (rs.next()) {
                modelo.addRow(new Object[]{rs.getObject(1), rs.getObject(2)});
            }

            ajustarAnchoColumnas();
            ajustarTamanoVentana();

            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Agregar esto para ver el error completo en la consola
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    private void configurarFiltro() {
        sorter = new TableRowSorter<>(modelo);
        tblResultados.setRowSorter(sorter);
    }

    private void filtrarTabla() {
        String texto = txtBusqueda.getText().trim();

        if (texto.length() < 3) {
            // Limpiar la tabla si hay menos de 3 caracteres
            modelo.setRowCount(0);

            // Si hay texto pero no suficiente, mostrar mensaje
            if (!texto.isEmpty()) {
                tblResultados.setModel(modelo);
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos 3 caracteres para iniciar la búsqueda",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Hay 3 o más caracteres, cargar datos con filtro
            cargarDatos(texto);
        }
    }

    private void configurarEventos() {
        tblResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarRegistro();
                }
            }
        });

        tblResultados.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    seleccionarRegistro();
                }
            }
        });

        // Modificar este KeyListener para el campo de búsqueda
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Realizar búsqueda solo cuando se presiona Enter
                    realizarBusqueda();
                }
            }
        });
    }

    private void realizarBusqueda() {
        String texto = txtBusqueda.getText().trim();

        if (texto.length() < 3) {
            // Limpiar la tabla si hay menos de 3 caracteres
            modelo.setRowCount(0);

            // Mostrar mensaje solo al presionar Enter con texto insuficiente
            JOptionPane.showMessageDialog(this,
                    "Ingrese al menos 3 caracteres para iniciar la búsqueda",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);

            SwingUtilities.invokeLater(() -> {
                txtBusqueda.requestFocusInWindow();
                txtBusqueda.selectAll();
            });
        } else {
            // Hay 3 o más caracteres, cargar datos con filtro
            cargarDatos(texto);

            // Si hay resultados, seleccionar el primero automáticamente
            if (tblResultados.getRowCount() > 0) {
                tblResultados.setRowSelectionInterval(0, 0);
                tblResultados.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron resultados para: " + texto,
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    txtBusqueda.requestFocusInWindow();
                    txtBusqueda.selectAll();
                });
            }
        }
    }

    private void seleccionarRegistro() {
        int filaSeleccionada = tblResultados.getSelectedRow();
        if (filaSeleccionada >= 0) {
            int filaModelo = tblResultados.convertRowIndexToModel(filaSeleccionada);
            int id = Integer.parseInt(modelo.getValueAt(filaModelo, 0).toString());

            if (ventanaOrigen != null) {
                ventanaOrigen.setRegistroSeleccionado(id);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un registro", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int ajustarAnchoColumnas() {
        int anchoTotal = 0;

        for (int column = 0; column < tblResultados.getColumnCount(); column++) {
            TableColumn tableColumn = tblResultados.getColumnModel().getColumn(column);
            int preferredWidth = 0;

            TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = tblResultados.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                    tblResultados,
                    tableColumn.getHeaderValue(),
                    false, false, 0, column
            );
            preferredWidth = headerComp.getPreferredSize().width;

            for (int row = 0; row < tblResultados.getRowCount(); row++) {
                Component cellComp = tblResultados.prepareRenderer(
                        tblResultados.getCellRenderer(row, column),
                        row, column
                );
                preferredWidth = Math.max(preferredWidth, cellComp.getPreferredSize().width);
            }

            tableColumn.setPreferredWidth(preferredWidth + 10);
            anchoTotal += preferredWidth + 10;
        }

        return anchoTotal;
    }

    private void ajustarTamanoVentana() {
        int anchoTabla = ajustarAnchoColumnas();
        int anchoVentana = anchoTabla + 2;

        // Establecer límites mínimo y máximo para el ancho
        anchoVentana = Math.max(anchoVentana, 300);
        anchoVentana = Math.min(anchoVentana, 1000);

        this.setSize(anchoVentana, this.getHeight());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtBusqueda = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblResultados = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        jLabel1.setText("BUSCAR:");
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tblResultados.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblResultados);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblResultados;
    private javax.swing.JTextField txtBusqueda;
    // End of variables declaration//GEN-END:variables
}
