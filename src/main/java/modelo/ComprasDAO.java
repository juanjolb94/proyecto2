package modelo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ComprasDAO {

    private Connection conexion;

    public ComprasDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Método para insertar una nueva compra con sus detalles
    public int insertarCompra(mCompras compra) throws SQLException {
        int idCompra = 0;
        // Iniciar transacción
        conexion.setAutoCommit(false);

        try {
            // Insertar cabecera de compra con TODOS los campos
            String sqlCabecera = "INSERT INTO compras_cabecera (id_proveedor, fecha_compra, tipo_documento, "
                    + "numero_factura, timbrado, fecha_vencimiento, condicion, subtotal, total_iva5, "
                    + "total_iva10, total_iva, total_compra, nro_planilla, observaciones, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psCabecera = conexion.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS);
            psCabecera.setInt(1, compra.getIdProveedor());
            psCabecera.setDate(2, new Date(compra.getFechaCompra().getTime()));
            psCabecera.setString(3, compra.getTipoDocumento());
            psCabecera.setString(4, compra.getNumeroFactura());
            psCabecera.setString(5, compra.getTimbrado());

            // Fecha de vencimiento (puede ser null)
            if (compra.getFechaVencimiento() != null) {
                psCabecera.setDate(6, new Date(compra.getFechaVencimiento().getTime()));
            } else {
                psCabecera.setNull(6, java.sql.Types.DATE);
            }

            psCabecera.setString(7, compra.getCondicion());
            psCabecera.setDouble(8, compra.getSubtotal());
            psCabecera.setDouble(9, compra.getTotalIva5());
            psCabecera.setDouble(10, compra.getTotalIva10());
            psCabecera.setDouble(11, compra.getTotalIva());
            psCabecera.setDouble(12, compra.getTotalCompra());
            psCabecera.setString(13, compra.getNroPlanilla());
            psCabecera.setString(14, compra.getObservaciones());
            psCabecera.setBoolean(15, compra.isEstado());

            psCabecera.executeUpdate();

            // Obtener el ID generado para la cabecera
            ResultSet rs = psCabecera.getGeneratedKeys();
            if (rs.next()) {
                idCompra = rs.getInt(1);

                // Insertar detalles de compra
                for (mCompras.DetalleCompra detalle : compra.getDetalles()) {
                    insertarDetalleCompra(idCompra, detalle);
                }
            }

            // Confirmar transacción
            conexion.commit();
            return idCompra;

        } catch (SQLException e) {
            // Revertir en caso de error
            conexion.rollback();
            throw e;
        } finally {
            // Restaurar autocommit
            conexion.setAutoCommit(true);
        }
    }

    // Método auxiliar para obtener datos completos del producto
    private Object[] obtenerDatosProductoCompletos(int idProducto, String codBarra) throws SQLException {
        String sql = "SELECT pd.descripcion, pd.unidad_medida_compra, pc.iva "
                + "FROM productos_detalle pd "
                + "INNER JOIN productos_cabecera pc ON pd.id_producto = pc.id_producto "
                + "WHERE pd.id_producto = ? AND pd.cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, codBarra);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("descripcion"), // [0]
                        rs.getString("unidad_medida_compra"), // [1]
                        1, // [2] - VALOR FIJO: unidades_por_empaque
                        rs.getDouble("iva") // [3]
                    };
                }
            }
        }

        // Valores por defecto si no encuentra el producto
        return new Object[]{"Producto", "UND", 1, 10.0};
    }

    // Método para calcular precio final (precio unitario - descuento)
    private double calcularPrecioFinal(double precioUnitario, double descuento) {
        return precioUnitario - descuento;
    }

    // Método para insertar un detalle de compra
    private void insertarDetalleCompra(int idCompra, mCompras.DetalleCompra detalle) throws SQLException {

        // Obtener datos adicionales del producto
        Object[] datosProducto = obtenerDatosProductoCompletos(detalle.getIdProducto(), detalle.getCodBarra());

        // Establecer valores en el detalle si no están definidos
        if (detalle.getDescripcion() == null) {
            detalle.setDescripcion((String) datosProducto[0]);
        }
        if (detalle.getUnidadMedida() == null) {
            detalle.setUnidadMedida((String) datosProducto[1]);
        }
        if (detalle.getUnidadesPorEmpaque() == 0) {
            detalle.setUnidadesPorEmpaque((Integer) datosProducto[2]);
        }
        if (detalle.getPorcentajeIva() == 0) {
            detalle.setPorcentajeIva((Double) datosProducto[3]);
        }

        // Calcular precio final (por ahora sin descuento)
        double precioFinal = calcularPrecioFinal(detalle.getPrecioUnitario(), detalle.getDescuento());
        detalle.setPrecioFinal(precioFinal);

        // SQL con TODOS los campos
        String sql = "INSERT INTO compras_detalle (id_compra, id_producto, cod_barra, descripcion, "
                + "cantidad, unidad_medida, unidades_por_empaque, precio_unitario, descuento, "
                + "precio_final, porcentaje_iva, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            ps.setInt(2, detalle.getIdProducto());
            ps.setString(3, detalle.getCodBarra());
            ps.setString(4, detalle.getDescripcion());
            ps.setInt(5, detalle.getCantidad());
            ps.setString(6, detalle.getUnidadMedida());
            ps.setInt(7, detalle.getUnidadesPorEmpaque());
            ps.setDouble(8, detalle.getPrecioUnitario());
            ps.setDouble(9, detalle.getDescuento());
            ps.setDouble(10, detalle.getPrecioFinal());
            ps.setDouble(11, detalle.getPorcentajeIva());
            ps.setDouble(12, detalle.getSubtotal());

            ps.executeUpdate();
        }
    }

    // Método para buscar una compra por ID
    public mCompras buscarCompraPorId(int id) throws SQLException {
        String sqlCabecera = "SELECT * FROM compras_cabecera WHERE id_compra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sqlCabecera)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mCompras compra = new mCompras(
                            rs.getInt("id_compra"),
                            rs.getInt("id_proveedor"),
                            rs.getDate("fecha_compra"),
                            rs.getString("numero_factura"),
                            rs.getDouble("total_compra"),
                            rs.getString("observaciones"),
                            rs.getBoolean("estado")
                    );

                    // Cargar los detalles de la compra
                    cargarDetallesCompra(compra);

                    return compra;
                }
            }
        }

        return null;
    }

    // Método para cargar los detalles de una compra
    private void cargarDetallesCompra(mCompras compra) throws SQLException {
        String sql = "SELECT cd.*, pd.descripcion as desc_producto FROM compras_detalle cd "
                + "LEFT JOIN productos_detalle pd ON cd.id_producto = pd.id_producto "
                + "AND cd.cod_barra = pd.cod_barra WHERE cd.id_compra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, compra.getIdCompra());

            try (ResultSet rs = ps.executeQuery()) {
                List<mCompras.DetalleCompra> detalles = new ArrayList<>();

                while (rs.next()) {
                    // Crear detalle usando el constructor con precio unitario
                    mCompras.DetalleCompra detalle = new mCompras.DetalleCompra(
                            rs.getInt("id_compra"),
                            rs.getInt("id_producto"),
                            rs.getString("cod_barra"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            true // Indicar que es precio unitario
                    );

                    // Establecer campos adicionales desde la BD
                    detalle.setDescripcion(rs.getString("descripcion"));
                    detalle.setUnidadMedida(rs.getString("unidad_medida"));
                    detalle.setUnidadesPorEmpaque(rs.getInt("unidades_por_empaque"));
                    detalle.setDescuento(rs.getDouble("descuento"));
                    detalle.setPrecioFinal(rs.getDouble("precio_final"));
                    detalle.setPorcentajeIva(rs.getDouble("porcentaje_iva"));

                    // El subtotal ya se calcula automáticamente en el constructor
                    detalles.add(detalle);
                }

                compra.setDetalles(detalles);
            }
        }
    }

    // Método para obtener todas las compras
    public List<Object[]> listarCompras() throws SQLException {
        List<Object[]> compras = new ArrayList<>();
        String sql = "SELECT c.id_compra, c.fecha_compra, c.numero_factura, p.razon_social, c.total_compra, c.estado "
                + "FROM compras_cabecera c "
                + "JOIN proveedores p ON c.id_proveedor = p.id_proveedor "
                + "ORDER BY c.fecha_compra DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] compra = {
                    rs.getInt("id_compra"),
                    rs.getDate("fecha_compra"),
                    rs.getString("numero_factura"),
                    rs.getString("razon_social"),
                    rs.getDouble("total_compra"),
                    rs.getBoolean("estado")
                };

                compras.add(compra);
            }
        }

        return compras;
    }

    // Método para anular una compra (cambiar estado)
    public boolean anularCompra(int idCompra) throws SQLException {
        String sql = "{CALL sp_anular_compra(?, ?, ?)}";

        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, idCompra);

            // Usar usuario dinámico del sistema de login
            int usuarioId = vista.vLogin.getIdUsuarioAutenticado();
            if (usuarioId <= 0) {
                usuarioId = 1; // Fallback por seguridad
            }
            cs.setInt(2, usuarioId);

            cs.setString(3, "Anulación manual desde sistema");

            boolean hasResultSet = cs.execute();

            if (hasResultSet) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        System.out.println("Resultado: " + rs.getString("resultado"));
                        System.out.println("Anulada por usuario ID: " + usuarioId);
                        return true;
                    }
                }
            }

            return false;

        } catch (SQLException e) {
            if (e.getSQLState().equals("45000")) {
                // Error controlado del stored procedure
                throw new SQLException("Error: " + e.getMessage());
            }
            throw e;
        }
    }

    // Método para obtener productos para seleccionar en compra
    public List<Object[]> obtenerProductosParaCompra() throws SQLException {
        List<Object[]> productos = new ArrayList<>();

        String sql = "SELECT p.id_producto, p.nombre, pd.cod_barra, pd.descripcion, pd.presentacion "
                + "FROM productos_cabecera p "
                + "JOIN productos_detalle pd ON p.id_producto = pd.id_producto "
                + "WHERE p.estado = true AND pd.estado = true "
                + "ORDER BY p.nombre, pd.descripcion";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] producto = {
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("cod_barra"),
                    rs.getString("descripcion"),
                    rs.getString("presentacion")
                };

                productos.add(producto);
            }
        }

        return productos;
    }

    // Método para buscar producto por código de barras
    public Object[] buscarProductoPorCodBarra(String codBarra) throws SQLException {
        String sql = "SELECT p.id_producto, p.nombre, pd.cod_barra, pd.descripcion, pd.presentacion "
                + "FROM productos_cabecera p "
                + "JOIN productos_detalle pd ON p.id_producto = pd.id_producto "
                + "WHERE pd.cod_barra = ? AND p.estado = true AND pd.estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codBarra);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("cod_barra"),
                        rs.getString("descripcion"),
                        rs.getString("presentacion")
                    };
                }
            }
        }

        return null;
    }

    // Método para obtener el primer registro
    public Object[] obtenerPrimeraCompra() throws SQLException {
        String sql = "SELECT id_compra, id_proveedor, fecha_compra, numero_factura, "
                + "total_compra, observaciones, estado FROM compras_cabecera "
                + "ORDER BY id_compra ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new Object[]{
                    rs.getInt("id_compra"),
                    rs.getInt("id_proveedor"),
                    rs.getDate("fecha_compra"),
                    rs.getString("numero_factura"),
                    rs.getDouble("total_compra"),
                    rs.getString("observaciones"),
                    rs.getBoolean("estado") ? "1" : "0"
                };
            }
        }

        return null;
    }

    // Método para obtener la compra anterior
    public Object[] obtenerCompraAnterior(int idActual) throws SQLException {
        String sql = "SELECT id_compra, id_proveedor, fecha_compra, numero_factura, "
                + "total_compra, observaciones, estado FROM compras_cabecera "
                + "WHERE id_compra < ? ORDER BY id_compra DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_compra"),
                        rs.getInt("id_proveedor"),
                        rs.getDate("fecha_compra"),
                        rs.getString("numero_factura"),
                        rs.getDouble("total_compra"),
                        rs.getString("observaciones"),
                        rs.getBoolean("estado") ? "1" : "0"
                    };
                }
            }
        }

        return null;
    }

    // Método para obtener la siguiente compra
    public Object[] obtenerCompraSiguiente(int idActual) throws SQLException {
        String sql = "SELECT id_compra, id_proveedor, fecha_compra, numero_factura, "
                + "total_compra, observaciones, estado FROM compras_cabecera "
                + "WHERE id_compra > ? ORDER BY id_compra ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_compra"),
                        rs.getInt("id_proveedor"),
                        rs.getDate("fecha_compra"),
                        rs.getString("numero_factura"),
                        rs.getDouble("total_compra"),
                        rs.getString("observaciones"),
                        rs.getBoolean("estado") ? "1" : "0"
                    };
                }
            }
        }

        return null;
    }

    // Método para obtener la última compra
    public Object[] obtenerUltimaCompra() throws SQLException {
        String sql = "SELECT id_compra, id_proveedor, fecha_compra, numero_factura, "
                + "total_compra, observaciones, estado FROM compras_cabecera "
                + "ORDER BY id_compra DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new Object[]{
                    rs.getInt("id_compra"),
                    rs.getInt("id_proveedor"),
                    rs.getDate("fecha_compra"),
                    rs.getString("numero_factura"),
                    rs.getDouble("total_compra"),
                    rs.getString("observaciones"),
                    rs.getBoolean("estado") ? "1" : "0"
                };
            }
        }

        return null;
    }

    // Método para obtener detalles de una compra
    public List<Object[]> obtenerDetallesCompra(int idCompra) throws SQLException {
        List<Object[]> detalles = new ArrayList<>();

        String sql = "SELECT cd.id_producto, cd.cod_barra, p.nombre, pd.descripcion, "
                + "cd.cantidad, cd.precio_unitario, cd.subtotal "
                + "FROM compras_detalle cd "
                + "JOIN productos_cabecera p ON cd.id_producto = p.id_producto "
                + "JOIN productos_detalle pd ON cd.id_producto = pd.id_producto AND cd.cod_barra = pd.cod_barra "
                + "WHERE cd.id_compra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCompra);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] detalle = {
                        rs.getInt("id_producto"),
                        rs.getString("cod_barra"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    };

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

}
