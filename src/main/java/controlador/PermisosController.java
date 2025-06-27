package controlador;

import modelo.PermisosDAO;
import modelo.mPermiso;
import vista.vPermisos;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import util.MenusCache;

public class PermisosController {

    private vPermisos vista;
    private PermisosDAO dao;

    public PermisosController(vPermisos vista) {
        this.vista = vista;
        this.dao = new PermisosDAO();
    }

    /**
     * Obtener menús prioritizando cache, luego interfaz, luego BD
     */
    public List<mPermiso> obtenerMenusDelSistema() {
        // 1. Verificar cache de vMenus
        if (MenusCache.getInstance().isCacheValido()) {
            return MenusCache.getInstance().getMenusDelSistema();
        }

        // 2. Fallback: leer desde interfaz
        try {
            return vista.obtenerMenusDesdaInterfaz();
        } catch (Exception e) {
            // 3. Último recurso: base de datos
            try {
                return dao.obtenerMenusDelSistema();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(vista,
                        "Error al obtener menús: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return new ArrayList<>();
            }
        }
    }

    public List<mPermiso> obtenerPermisosPorRol(int idRol) {
        try {
            return dao.obtenerPermisosPorRol(idRol);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(vista,
                    "Error al obtener permisos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    public boolean guardarPermisos(List<mPermiso> permisos) {
        try {
            for (mPermiso permiso : permisos) {
                if (!dao.guardarPermiso(permiso)) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(vista,
                    "Error al guardar permisos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean tienePermiso(int idRol, String nombreComponente, String tipoPermiso) {
        try {
            return dao.tienePermiso(idRol, nombreComponente, tipoPermiso);
        } catch (SQLException e) {
            return false;
        }
    }
}
