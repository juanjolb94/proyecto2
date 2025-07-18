package modelo;

import controlador.cGestProd.ItemCombo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductosDAO {

    private final Connection conexion;

    public ProductosDAO() throws SQLException {
        this.conexion = DatabaseConnection.getConnection();
    }

    // Método para buscar un producto por ID (devuelve Object[] para compatibilidad con el controlador)
    public Object[] buscarProductoPorId(int id) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, m.nombre as marca_nombre "
                + "FROM productos_cabecera p "
                + "LEFT JOIN categoria_producto c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON p.id_marca = m.id_marca "
                + "WHERE p.id_producto = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getInt("id_categoria"),
                        rs.getString("categoria_nombre"),
                        rs.getInt("id_marca"),
                        rs.getString("marca_nombre"),
                        rs.getDouble("iva"),
                        rs.getBoolean("estado") ? "1" : "0"
                    };
                }
            }
        }
        return null;
    }

    // Método para obtener el primer producto
    public Object[] obtenerPrimerProducto() throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, m.nombre as marca_nombre "
                + "FROM productos_cabecera p "
                + "LEFT JOIN categoria_producto c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON p.id_marca = m.id_marca "
                + "ORDER BY p.id_producto ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearProductoCompleto(rs);
            }
        }
        return null;
    }

    // Método para obtener el producto anterior
    public Object[] obtenerAnteriorProducto(int idActual) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, m.nombre as marca_nombre "
                + "FROM productos_cabecera p "
                + "LEFT JOIN categoria_producto c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON p.id_marca = m.id_marca "
                + "WHERE p.id_producto < ? ORDER BY p.id_producto DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProductoCompleto(rs);
                }
            }
        }
        return null;
    }

    // Método para obtener el siguiente producto
    public Object[] obtenerSiguienteProducto(int idActual) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, m.nombre as marca_nombre "
                + "FROM productos_cabecera p "
                + "LEFT JOIN categoria_producto c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON p.id_marca = m.id_marca "
                + "WHERE p.id_producto > ? ORDER BY p.id_producto ASC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProductoCompleto(rs);
                }
            }
        }
        return null;
    }

    // Método para obtener el último producto
    public Object[] obtenerUltimoProducto() throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, m.nombre as marca_nombre "
                + "FROM productos_cabecera p "
                + "LEFT JOIN categoria_producto c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON p.id_marca = m.id_marca "
                + "ORDER BY p.id_producto DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearProductoCompleto(rs);
            }
        }
        return null;
    }

    // Método para insertar un nuevo producto
    public void insertarProducto(String nombre, int idCategoria, int idMarca, double iva, boolean estado) throws SQLException {
        String sql = "INSERT INTO productos_cabecera (nombre, id_categoria, id_marca, iva, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idCategoria);
            ps.setInt(3, idMarca);
            ps.setDouble(4, iva);
            ps.setBoolean(5, estado);

            ps.executeUpdate();
        }
    }

    // Método para actualizar un producto existente
    public void actualizarProducto(int id, String nombre, int idCategoria, int idMarca, double iva, boolean estado) throws SQLException {
        String sql = "UPDATE productos_cabecera SET nombre = ?, id_categoria = ?, "
                + "id_marca = ?, iva = ?, estado = ? WHERE id_producto = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idCategoria);
            ps.setInt(3, idMarca);
            ps.setDouble(4, iva);
            ps.setBoolean(5, estado);
            ps.setInt(6, id);

            ps.executeUpdate();
        }
    }

    // Método para verificar si un producto tiene movimientos asociados
    public boolean tieneMovimientosAsociados(int idProducto) throws SQLException {
        return tieneVentas(idProducto) || tieneCompras(idProducto) || tieneStock(idProducto);
    }

    // Método para verificar si un producto tiene ventas
    public boolean tieneVentas(int idProducto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ventas_detalle vd "
                + "INNER JOIN ventas v ON vd.id_venta = v.id "
                + "WHERE vd.id_producto = ? AND v.anulado = false";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para verificar si un producto tiene compras
    public boolean tieneCompras(int idProducto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM compras_detalle cd "
                + "INNER JOIN compras_cabecera cc ON cd.id_compra = cc.id_compra "
                + "WHERE cd.id_producto = ? AND cc.estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para verificar si un producto tiene stock
    public boolean tieneStock(int idProducto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE id_producto = ? AND cantidad_disponible > 0";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para obtener resumen de dependencias del producto
    public String obtenerResumenDependencias(int idProducto) throws SQLException {
        StringBuilder resumen = new StringBuilder();

        // Contar ventas
        String sqlVentas = "SELECT COUNT(*) FROM ventas_detalle vd "
                + "INNER JOIN ventas v ON vd.id_venta = v.id "
                + "WHERE vd.id_producto = ? AND v.anulado = false";

        try (PreparedStatement ps = conexion.prepareStatement(sqlVentas)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ventas = rs.getInt(1);
                    if (ventas > 0) {
                        resumen.append("Ventas: ").append(ventas).append(" | ");
                    }
                }
            }
        }

        // Contar compras
        String sqlCompras = "SELECT COUNT(*) FROM compras_detalle cd "
                + "INNER JOIN compras_cabecera cc ON cd.id_compra = cc.id_compra "
                + "WHERE cd.id_producto = ? AND cc.estado = true";

        try (PreparedStatement ps = conexion.prepareStatement(sqlCompras)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int compras = rs.getInt(1);
                    if (compras > 0) {
                        resumen.append("Compras: ").append(compras).append(" | ");
                    }
                }
            }
        }

        // Verificar stock
        String sqlStock = "SELECT SUM(cantidad_disponible) FROM stock WHERE id_producto = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sqlStock)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt(1);
                    if (stock > 0) {
                        resumen.append("Stock: ").append(stock).append(" unidades");
                    }
                }
            }
        }

        return resumen.toString();
    }

    // Método para eliminar un producto
    public boolean eliminarProducto(int id) throws SQLException {
        // Validar si tiene movimientos asociados
        if (tieneMovimientosAsociados(id)) {
            String resumen = obtenerResumenDependencias(id);

            throw new SQLException("No se puede eliminar el producto: tiene movimientos asociados. "
                    + resumen + ". Considere desactivar el producto en lugar de eliminarlo.");
        }

        // Primero eliminar detalles asociados
        String sqlDetalles = "DELETE FROM productos_detalle WHERE id_producto = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlDetalles)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        // Luego eliminar cabecera
        String sqlCabecera = "DELETE FROM productos_cabecera WHERE id_producto = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlCabecera)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Método para obtener los detalles de un producto
    public List<Object[]> obtenerDetallesProducto(int idProducto) throws SQLException {
        List<Object[]> detalles = new ArrayList<>();
        String sql = "SELECT cod_barra, descripcion, presentacion, estado "
                + "FROM productos_detalle WHERE id_producto = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new Object[]{
                        rs.getString("cod_barra"),
                        rs.getString("descripcion"),
                        rs.getString("presentacion"),
                        rs.getBoolean("estado") ? "1" : "0"
                    });
                }
            }
        }
        return detalles;
    }

    // Método para obtener todas las categorías
    public List<ItemCombo<Integer>> obtenerCategorias() throws SQLException {
        List<ItemCombo<Integer>> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM categoria_producto ORDER BY nombre";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(new ItemCombo<>(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre")
                ));
            }
        }
        return categorias;
    }

    // Método para obtener todas las marcas
    public List<ItemCombo<Integer>> obtenerMarcas() throws SQLException {
        List<ItemCombo<Integer>> marcas = new ArrayList<>();
        String sql = "SELECT id_marca, nombre FROM marca_producto ORDER BY nombre";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                marcas.add(new ItemCombo<>(
                        rs.getInt("id_marca"),
                        rs.getString("nombre")
                ));
            }
        }
        return marcas;
    }

    // Método auxiliar para mapear un ResultSet a Object[] (producto completo)
    private Object[] mapearProductoCompleto(ResultSet rs) throws SQLException {
        return new Object[]{
            rs.getInt("id_producto"),
            rs.getString("nombre"),
            rs.getInt("id_categoria"),
            rs.getString("categoria_nombre"),
            rs.getInt("id_marca"),
            rs.getString("marca_nombre"),
            rs.getDouble("iva"),
            rs.getBoolean("estado") ? "1" : "0"
        };
    }

    // Método para obtener todos los productos (si es necesario)
    public List<mProducto> listarTodos() throws SQLException {
        List<mProducto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos_cabecera";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        }
        return productos;
    }

    // Método específico para obtener productos con descripción para reportes
    public List<mProducto> listarTodosConDescripcion() throws SQLException {
        List<mProducto> productos = new ArrayList<>();
        String sql = "SELECT pc.id_producto, pc.nombre, pc.id_categoria, pc.id_marca, "
                + "pc.iva, pc.estado, pd.descripcion, pd.cod_barra, "
                + "COALESCE(pd.precio_compra, 0) as precio_compra, "
                + "COALESCE(pr.precio, 0) as precio_venta, "
                + "COALESCE(s.cantidad_disponible, 0) as stock_real, "
                + "c.nombre as categoria_nombre, "
                + "m.nombre as marca_nombre "
                + "FROM productos_cabecera pc "
                + "LEFT JOIN productos_detalle pd ON pc.id_producto = pd.id_producto "
                + "LEFT JOIN categoria_producto c ON pc.id_categoria = c.id_categoria "
                + "LEFT JOIN marca_producto m ON pc.id_marca = m.id_marca "
                + "LEFT JOIN stock s ON pd.id_producto = s.id_producto AND pd.cod_barra = s.cod_barra "
                + "LEFT JOIN precio_detalle pr ON pd.cod_barra = pr.codigo_barra "
                + "    AND pr.id_precio_cabecera = (SELECT id FROM precio_cabecera WHERE es_defecto_venta = 1 LIMIT 1) "
                + "    AND pr.activo = 1 "
                + "WHERE pc.estado = true AND (pd.estado IS NULL OR pd.estado = true) "
                + "ORDER BY m.nombre, pc.nombre, pd.descripcion";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mProducto producto = new mProducto();
                producto.setIdProducto(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setIdCategoria(rs.getInt("id_categoria"));
                producto.setIdMarca(rs.getInt("id_marca"));
                producto.setIva(rs.getDouble("iva"));
                producto.setEstado(rs.getBoolean("estado"));

                // Datos del detalle
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setCodigo(rs.getString("cod_barra"));

                // PRECIOS CORRECTOS
                producto.setPrecioCompra(rs.getInt("precio_compra"));  // Precio de compra
                producto.setPrecio(rs.getInt("precio_venta"));         // Precio de venta

                // STOCK
                producto.setStock(rs.getInt("stock_real"));

                // Establecer nombres de categoría y marca
                producto.setNombreCategoria(rs.getString("categoria_nombre"));
                producto.setNombreMarca(rs.getString("marca_nombre"));

                productos.add(producto);
            }
        }
        return productos;
    }

    // Método auxiliar para mapear un ResultSet a mProducto
    private mProducto mapearProducto(ResultSet rs) throws SQLException {
        mProducto producto = new mProducto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setIdCategoria(rs.getInt("id_categoria"));
        producto.setIdMarca(rs.getInt("id_marca"));
        producto.setIva(rs.getDouble("iva"));
        producto.setEstado(rs.getBoolean("estado"));
        return producto;
    }

    public boolean existeMarca(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM marca_producto WHERE UPPER(nombre) = UPPER(?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean insertarMarca(String nombre) throws SQLException {
        String sql = "INSERT INTO marca_producto (nombre) VALUES (?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);

            return ps.executeUpdate() > 0;
        }
    }

    // Método para insertar un detalle de producto
    public boolean insertarDetalleProducto(int idProducto, String codBarra, String descripcion,
            String presentacion, boolean estado) throws SQLException {
        String sql = "INSERT INTO productos_detalle (id_producto, cod_barra, descripcion, presentacion, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, codBarra);
            ps.setString(3, descripcion);
            ps.setString(4, presentacion);
            ps.setBoolean(5, estado);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    // Método para eliminar un detalle de producto por código de barras
    public boolean eliminarDetalleProducto(int idProducto, String codBarra) throws SQLException {
        String sql = "DELETE FROM productos_detalle WHERE id_producto = ? AND cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, codBarra);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    // Método para verificar si ya existe un código de barras
    public boolean existeCodBarra(String codBarra) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos_detalle WHERE cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codBarra);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para buscar un detalle de producto por código de barras
    public Object[] buscarDetallePorCodBarra(int idProducto, String codBarra) throws SQLException {
        String sql = "SELECT cod_barra, descripcion, presentacion, estado "
                + "FROM productos_detalle WHERE id_producto = ? AND cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, codBarra);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("cod_barra"),
                        rs.getString("descripcion"),
                        rs.getString("presentacion"),
                        rs.getBoolean("estado") ? "1" : "0"
                    };
                }
            }
        }
        return null;
    }

    public boolean actualizarDetalleProducto(int idProducto, String codBarra, String descripcion,
            String presentacion, boolean estado) throws SQLException {
        String sql = "UPDATE productos_detalle SET descripcion = ?, presentacion = ?, estado = ? "
                + "WHERE id_producto = ? AND cod_barra = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ps.setString(2, presentacion);
            ps.setBoolean(3, estado);
            ps.setInt(4, idProducto);
            ps.setString(5, codBarra);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    // Método para obtener todas las marcas con sus detalles completos
    public List<Object[]> obtenerMarcasCompletas() throws SQLException {
        List<Object[]> marcas = new ArrayList<>();
        String sql = "SELECT id_marca, nombre, estado FROM marca_producto ORDER BY nombre";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                marcas.add(new Object[]{
                    rs.getInt("id_marca"),
                    rs.getString("nombre"),
                    rs.getBoolean("estado") ? "1" : "0"
                });
            }
        }
        return marcas;
    }

// Método para actualizar el estado de una marca
    public boolean actualizarEstadoMarca(int idMarca, boolean estado) throws SQLException {
        String sql = "UPDATE marca_producto SET estado = ? WHERE id_marca = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBoolean(1, estado);
            ps.setInt(2, idMarca);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

// Método para actualizar el nombre de una marca
    public boolean actualizarNombreMarca(int idMarca, String nuevoNombre) throws SQLException {
        String sql = "UPDATE marca_producto SET nombre = ? WHERE id_marca = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, idMarca);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }

// Método para verificar si una marca está siendo utilizada por algún producto
    public boolean marcaEstaEnUso(int idMarca) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos_cabecera WHERE id_marca = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMarca);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

// Método para eliminar una marca
    public boolean eliminarMarca(int idMarca) throws SQLException {
        String sql = "DELETE FROM marca_producto WHERE id_marca = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMarca); // Corregido: cambié el índice de 2 a 1

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}
