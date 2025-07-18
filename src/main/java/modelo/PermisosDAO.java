package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class PermisosDAO {

    // Obtener todos los menús del sistema
    public List<mPermiso> obtenerMenusDelSistema() throws SQLException {
        List<mPermiso> menus = new ArrayList<>();
        String sql = "SELECT id_menu, nombre_menu, nombre_componente, menu_padre, orden "
                + "FROM menus WHERE activo = TRUE "
                + "ORDER BY COALESCE(menu_padre, id_menu), menu_padre IS NULL DESC, orden, id_menu";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mPermiso menu = new mPermiso();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNombreMenu(rs.getString("nombre_menu"));
                menu.setNombreComponente(rs.getString("nombre_componente"));
                menus.add(menu);
            }
        }
        return menus;
    }

    // Obtener permisos de un rol específico
    public List<mPermiso> obtenerPermisosPorRol(int idRol) throws SQLException {
        System.out.println("  → Consultando permisos en BD para rol: " + idRol);

        List<mPermiso> permisos = new ArrayList<>();
        String sql = "SELECT p.id_permiso, p.id_rol, p.id_menu, m.nombre_menu, "
                + "m.nombre_componente, p.ver, p.crear, p.leer, p.actualizar, p.eliminar "
                + "FROM permisos p "
                + "INNER JOIN menus m ON p.id_menu = m.id_menu "
                + "WHERE p.id_rol = ? "
                + "AND m.activo = TRUE "
                + "ORDER BY m.orden, m.id_menu";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mPermiso permiso = new mPermiso(
                            rs.getInt("id_permiso"),
                            rs.getInt("id_rol"),
                            rs.getInt("id_menu"),
                            rs.getString("nombre_menu"),
                            rs.getString("nombre_componente"),
                            rs.getBoolean("ver"),
                            rs.getBoolean("crear"),
                            rs.getBoolean("leer"),
                            rs.getBoolean("actualizar"),
                            rs.getBoolean("eliminar")
                    );
                    permisos.add(permiso);
                    System.out.println("  → Permiso cargado: ID=" + permiso.getIdMenu()
                            + ", '" + permiso.getNombreMenu() + "', VER=" + permiso.isVer());
                }
            }
        }

        System.out.println("  → Total permisos cargados de BD: " + permisos.size());
        return permisos;
    }

    // Guardar o actualizar permiso
    public boolean guardarPermiso(mPermiso permiso) throws SQLException {
        System.out.println("  → Ejecutando INSERT/UPDATE para rol=" + permiso.getIdRol()
                + ", menu=" + permiso.getIdMenu());

        String sql = "INSERT INTO permisos (id_rol, id_menu, ver, crear, leer, actualizar, eliminar) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "ver = VALUES(ver), crear = VALUES(crear), leer = VALUES(leer), "
                + "actualizar = VALUES(actualizar), eliminar = VALUES(eliminar)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, permiso.getIdRol());
            ps.setInt(2, permiso.getIdMenu());
            ps.setBoolean(3, permiso.isVer());
            ps.setBoolean(4, permiso.isCrear());
            ps.setBoolean(5, permiso.isLeer());
            ps.setBoolean(6, permiso.isActualizar());
            ps.setBoolean(7, permiso.isEliminar());

            int filasAfectadas = ps.executeUpdate();
            System.out.println("  → Filas afectadas: " + filasAfectadas);

            boolean exito = filasAfectadas > 0;
            System.out.println("  → Guardado exitoso: " + exito);

            return exito;
        } catch (SQLException e) {
            System.err.println("  → ERROR en guardarPermiso: " + e.getMessage());
            throw e;
        }
    }

    // Eliminar permisos de un rol
    public boolean eliminarPermisosPorRol(int idRol) throws SQLException {
        String sql = "DELETE FROM permisos WHERE id_rol = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            return ps.executeUpdate() >= 0;
        }
    }

    // Verificar si un rol tiene un permiso específico
    public boolean tienePermiso(int idRol, String nombreComponente, String tipoPermiso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM permisos p "
                + "INNER JOIN menus m ON p.id_menu = m.id_menu "
                + "WHERE p.id_rol = ? AND m.nombre_componente = ? AND p." + tipoPermiso + " = TRUE";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setString(2, nombreComponente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Buscar ID real de menú por nombre de componente
    public int buscarIdMenuPorComponente(String nombreComponente) throws SQLException {
        System.out.println("  → Buscando ID para componente: '" + nombreComponente + "'");

        String sql = "SELECT id_menu FROM menus WHERE nombre_componente = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreComponente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_menu");
                    System.out.println("  → ID encontrado: " + id);
                    return id;
                } else {
                    System.out.println("  → NO se encontró ID para: '" + nombreComponente + "'");
                }
            }
        }
        return -1; // No encontrado
    }
}
