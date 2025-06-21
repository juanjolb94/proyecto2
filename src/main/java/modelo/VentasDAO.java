package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasDAO {

    private Connection conexion;

    public VentasDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    /**
     * Inserta venta con datos del talonario
     */
    public int insertarVentaConTalonario(mVentas venta) throws SQLException {
        int idVenta = 0;
        conexion.setAutoCommit(false);

        try {
            // Insertar cabecera de venta CON datos del talonario
            String sqlCabecera = "INSERT INTO ventas (fecha, total, id_cliente, id_usuario, "
                    + "id_caja, anulado, observaciones, numero_factura, numero_timbrado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psCabecera = conexion.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS);
            psCabecera.setTimestamp(1, new Timestamp(venta.getFecha().getTime()));
            psCabecera.setInt(2, venta.getTotal());
            psCabecera.setInt(3, venta.getIdCliente());
            psCabecera.setInt(4, venta.getIdUsuario());
            psCabecera.setInt(5, venta.getIdCaja());
            psCabecera.setBoolean(6, venta.isAnulado());
            psCabecera.setString(7, venta.getObservaciones());
            psCabecera.setString(8, venta.getNumeroFactura());
            psCabecera.setString(9, venta.getNumeroTimbrado());  // ← NUEVO CAMPO

            psCabecera.executeUpdate();

            // Obtener ID y procesar detalles
            ResultSet rs = psCabecera.getGeneratedKeys();
            if (rs.next()) {
                idVenta = rs.getInt(1);
                venta.setIdVenta(idVenta);

                // Insertar detalles (código existente)
                for (mVentas.DetalleVenta detalle : venta.getDetalles()) {
                    insertarDetalleVenta(idVenta, detalle);
                    actualizarStockVenta(detalle.getIdProducto(), detalle.getCodigoBarra(), detalle.getCantidad());
                }
            }

            conexion.commit();
            return idVenta;

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    // Método para insertar una nueva venta con sus detalles
    public int insertarVenta(mVentas venta) throws SQLException {
        int idVenta = 0;
        // Iniciar transacción
        conexion.setAutoCommit(false);

        try {
            // Insertar cabecera de venta
            String sqlCabecera = "INSERT INTO ventas (fecha, total, id_cliente, id_usuario, "
                    + "id_caja, anulado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psCabecera = conexion.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS);
            psCabecera.setTimestamp(1, new Timestamp(venta.getFecha().getTime()));
            psCabecera.setInt(2, venta.getTotal());
            psCabecera.setInt(3, venta.getIdCliente());
            psCabecera.setInt(4, venta.getIdUsuario());
            psCabecera.setInt(5, venta.getIdCaja());
            psCabecera.setBoolean(6, venta.isAnulado());
            psCabecera.setString(7, venta.getObservaciones());

            psCabecera.executeUpdate();

            // Obtener el ID generado para la cabecera
            ResultSet rs = psCabecera.getGeneratedKeys();
            if (rs.next()) {
                idVenta = rs.getInt(1);
                venta.setIdVenta(idVenta);

                // Insertar detalles de venta
                for (mVentas.DetalleVenta detalle : venta.getDetalles()) {
                    insertarDetalleVenta(idVenta, detalle);
                    // Actualizar stock (reducir stock)
                    actualizarStockVenta(detalle.getIdProducto(), detalle.getCodigoBarra(), detalle.getCantidad());
                }
            }

            // Confirmar transacción
            conexion.commit();
            return idVenta;

        } catch (SQLException e) {
            // Revertir en caso de error
            conexion.rollback();
            throw e;
        } finally {
            // Restaurar autocommit
            conexion.setAutoCommit(true);
        }
    }

    // Método para insertar un detalle de venta
    private void insertarDetalleVenta(int idVenta, mVentas.DetalleVenta detalle) throws SQLException {
        String sql = "INSERT INTO ventas_detalle (id_venta, id_producto, codigo_barra, "
                + "cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, detalle.getIdProducto());
            ps.setString(3, detalle.getCodigoBarra());
            ps.setInt(4, detalle.getCantidad());
            ps.setInt(5, detalle.getPrecioUnitario());
            ps.setInt(6, detalle.getSubtotal());

            ps.executeUpdate();
        }
    }

    // Método para actualizar el stock al realizar una venta (adaptado a tu estructura)
    private void actualizarStockVenta(int idProducto, String codigoBarra, int cantidadVendida) throws SQLException {
        String sql = "UPDATE productos_detalle SET stock = stock - ? WHERE id_producto = ? AND cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidadVendida);
            ps.setInt(2, idProducto);
            ps.setString(3, codigoBarra);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo actualizar el stock del producto: " + codigoBarra);
            }
        }
    }

    // Método para buscar una venta por ID
    public mVentas buscarVentaPorId(int id) throws SQLException {
        String sql = "SELECT * FROM ventas WHERE id = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mVentas venta = new mVentas(
                            rs.getInt("id"),
                            rs.getTimestamp("fecha"),
                            rs.getInt("total"),
                            rs.getInt("id_cliente"),
                            rs.getInt("id_usuario"),
                            rs.getInt("id_caja"),
                            rs.getBoolean("anulado"),
                            rs.getString("observaciones")
                    );

                    // Establecer datos del talonario
                    venta.setNumeroFactura(rs.getString("numero_factura"));
                    venta.setNumeroTimbrado(rs.getString("numero_timbrado"));

                    // Cargar detalles
                    cargarDetallesVenta(venta);

                    return venta;
                }
            }
        }
        return null;
    }

    // Método para cargar los detalles de una venta
    private void cargarDetallesVenta(mVentas venta) throws SQLException {
        String sql = "SELECT * FROM ventas_detalle WHERE id_venta = ? ORDER BY id";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, venta.getIdVenta());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mVentas.DetalleVenta detalle = new mVentas.DetalleVenta(
                            rs.getInt("id_venta"),
                            rs.getInt("id_producto"),
                            rs.getString("codigo_barra"),
                            rs.getInt("cantidad"),
                            rs.getInt("precio_unitario")
                    );
                    detalle.setId(rs.getInt("id"));

                    venta.agregarDetalle(detalle);
                }
            }
        }
    }

    // Método para buscar producto por código de barras
    public Object[] buscarProductoPorCodBarra(String codBarra) throws SQLException {
        String sql = "SELECT pc.id_producto, pc.nombre, pd.cod_barra, pd.descripcion, "
                + "COALESCE(pr.precio, pd.precio_compra, 0) as precio_venta, "
                + "0 as stock, pc.estado "
                + "FROM productos_cabecera pc "
                + "INNER JOIN productos_detalle pd ON pc.id_producto = pd.id_producto "
                + "LEFT JOIN ("
                + "    SELECT prd.codigo_barra, prd.precio "
                + "    FROM precio_cabecera prc "
                + "    INNER JOIN precio_detalle prd ON prc.id = prd.id_precio_cabecera "
                + "    WHERE prc.activo = true AND prd.activo = true"
                + ") pr ON pd.cod_barra = pr.codigo_barra "
                + "WHERE pd.cod_barra = ? AND pc.estado = true AND pd.estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codBarra);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_producto"), // 0 - id producto
                        rs.getString("nombre"), // 1 - nombre
                        rs.getString("cod_barra"), // 2 - código de barras
                        rs.getString("descripcion"), // 3 - descripción
                        rs.getInt("precio_venta"), // 4 - precio de venta (de precio_detalle activo)
                        rs.getInt("stock"), // 5 - stock (0 por ahora)
                        rs.getBoolean("estado") // 6 - estado
                    };
                }
            }
        }
        return null;
    }

    // Método para buscar productos por nombre/descripción
    public List<Object[]> buscarProductosPorNombre(String nombre) throws SQLException {
        String sql = "SELECT pc.id_producto, pc.nombre, pd.cod_barra, pd.descripcion, "
                + "COALESCE(pr.precio, pd.precio_compra, 0) as precio_venta, "
                + "0 as stock, pc.estado "
                + "FROM productos_cabecera pc "
                + "INNER JOIN productos_detalle pd ON pc.id_producto = pd.id_producto "
                + "LEFT JOIN ("
                + "    SELECT prd.codigo_barra, prd.precio "
                + "    FROM precio_cabecera prc "
                + "    INNER JOIN precio_detalle prd ON prc.id = prd.id_precio_cabecera "
                + "    WHERE prc.activo = true AND prd.activo = true"
                + ") pr ON pd.cod_barra = pr.codigo_barra "
                + "WHERE (pc.nombre LIKE ? OR pd.descripcion LIKE ?) "
                + "AND pc.estado = true AND pd.estado = true "
                + "ORDER BY pc.nombre, pd.descripcion "
                + "LIMIT 20"; // Limitar resultados para evitar sobrecarga

        List<Object[]> productos = new ArrayList<>();
        String busqueda = "%" + nombre.trim() + "%";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, busqueda);
            ps.setString(2, busqueda);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new Object[]{
                        rs.getInt("id_producto"), // 0 - id producto
                        rs.getString("nombre"), // 1 - nombre
                        rs.getString("cod_barra"), // 2 - código de barras
                        rs.getString("descripcion"), // 3 - descripción
                        rs.getInt("precio_venta"), // 4 - precio de venta
                        rs.getInt("stock"), // 5 - stock
                        rs.getBoolean("estado") // 6 - estado
                    });
                }
            }
        }
        return productos;
    }    // Método de diagnóstico para verificar estructura de tablas

    public void diagnosticarEstructuraProductos() throws SQLException {
        System.out.println("=== DIAGNÓSTICO DE ESTRUCTURA DE PRODUCTOS ===");

        // Verificar productos_cabecera
        String sql = "SHOW COLUMNS FROM productos_cabecera";
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            System.out.println("Columnas en productos_cabecera:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar productos_cabecera: " + e.getMessage());
        }

        // Verificar productos_detalle
        sql = "SHOW COLUMNS FROM productos_detalle";
        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            System.out.println("\nColumnas en productos_detalle:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar productos_detalle: " + e.getMessage());
        }

        System.out.println("=== FIN DIAGNÓSTICO ===");
    }

    // Método para obtener lista de clientes
    public List<Object[]> obtenerClientes() throws SQLException {
        String sql = "SELECT id_cliente, nombre, ci_ruc, telefono FROM clientes "
                + "WHERE estado = 1 ORDER BY nombre";

        List<Object[]> clientes = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(new Object[]{
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("ci_ruc"),
                    rs.getString("telefono")
                });
            }
        }

        return clientes;
    }

    // Método para obtener datos de un cliente específico
    public Object[] obtenerDatosCliente(int idCliente) throws SQLException {
        String sql = "SELECT nombre, ci_ruc, telefono, direccion FROM clientes "
                + "WHERE id_cliente = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("nombre"),
                        rs.getString("ci_ruc"),
                        rs.getString("telefono"),
                        rs.getString("direccion")
                    };
                }
            }
        }
        return null;
    }

    // Método para anular una venta
    public boolean anularVenta(int idVenta) throws SQLException {
        conexion.setAutoCommit(false);

        try {
            // Primero verificar que la venta existe y no está anulada
            String sqlVerificar = "SELECT anulado FROM ventas WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlVerificar)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getBoolean("anulado")) {
                            throw new SQLException("La venta ya está anulada");
                        }
                    } else {
                        throw new SQLException("La venta no existe");
                    }
                }
            }

            // Restaurar stock de todos los productos de la venta
            String sqlDetalles = "SELECT id_producto, codigo_barra, cantidad FROM ventas_detalle WHERE id_venta = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlDetalles)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idProducto = rs.getInt("id_producto");
                        String codigoBarra = rs.getString("codigo_barra");
                        int cantidad = rs.getInt("cantidad");

                        // Restaurar stock (adaptado a tu estructura)
                        String sqlStock = "UPDATE productos_detalle SET stock = stock + ? WHERE id_producto = ? AND cod_barra = ?";
                        try (PreparedStatement psStock = conexion.prepareStatement(sqlStock)) {
                            psStock.setInt(1, cantidad);
                            psStock.setInt(2, idProducto);
                            psStock.setString(3, codigoBarra);
                            psStock.executeUpdate();
                        }
                    }
                }
            }

            // Marcar la venta como anulada
            String sqlAnular = "UPDATE ventas SET anulado = true WHERE id = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlAnular)) {
                ps.setInt(1, idVenta);
                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    conexion.commit();
                    return true;
                } else {
                    conexion.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    // Método para listar ventas con filtros (adaptado a tu estructura)
    public List<Object[]> listarVentas(Date fechaDesde, Date fechaHasta, boolean incluirAnuladas) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.id, v.fecha, v.total, c.nombre as cliente, ");
        sql.append("u.NombreUsuario as usuario, v.anulado ");  // Usando NombreUsuario
        sql.append("FROM ventas v ");
        sql.append("LEFT JOIN clientes c ON v.id_cliente = c.id_cliente ");
        sql.append("LEFT JOIN usuarios u ON v.id_usuario = u.UsuarioID ");  // Usando UsuarioID
        sql.append("WHERE 1=1 ");

        if (fechaDesde != null) {
            sql.append("AND DATE(v.fecha) >= ? ");
        }
        if (fechaHasta != null) {
            sql.append("AND DATE(v.fecha) <= ? ");
        }
        if (!incluirAnuladas) {
            sql.append("AND v.anulado = false ");
        }

        sql.append("ORDER BY v.fecha DESC, v.id DESC");

        List<Object[]> ventas = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (fechaDesde != null) {
                ps.setDate(paramIndex++, new Date(fechaDesde.getTime()));
            }
            if (fechaHasta != null) {
                ps.setDate(paramIndex++, new Date(fechaHasta.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp("fecha"),
                        rs.getInt("total"),
                        rs.getString("cliente"),
                        rs.getString("usuario"),
                        rs.getBoolean("anulado")
                    });
                }
            }
        }

        return ventas;
    }

    // Método para obtener el ID de caja actual activa
    public int obtenerIdCajaActiva() throws SQLException {
        String sql = "SELECT id FROM cajas WHERE estado_abierto = 1 LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("No hay ninguna caja abierta. Debe abrir una caja para realizar ventas.");
            }
        }
    }

    // Método público para obtener conexión (para uso del controlador)
    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
