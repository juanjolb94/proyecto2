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

    public List<mPermiso> obtenerMenusDelSistemaCompleto() {
        try {
            // Usar SIEMPRE la BD como fuente de verdad
            return dao.obtenerMenusDelSistema();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(vista,
                    "Error al obtener menús completos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    public List<mPermiso> obtenerPermisosPorRol(int idRol) {
        System.out.println("\n=== CARGANDO PERMISOS PARA ROL " + idRol + " ===");
        try {
            List<mPermiso> permisos = dao.obtenerPermisosPorRol(idRol);
            System.out.println("Permisos encontrados: " + permisos.size());

            for (int i = 0; i < permisos.size(); i++) {
                mPermiso p = permisos.get(i);
                System.out.println("Permiso #" + (i + 1) + ": ID=" + p.getIdMenu()
                        + ", Menú='" + p.getNombreMenu()
                        + "', Componente='" + p.getNombreComponente()
                        + "', VER=" + p.isVer() + ", CREAR=" + p.isCrear()
                        + ", LEER=" + p.isLeer() + ", ACTUALIZAR=" + p.isActualizar()
                        + ", ELIMINAR=" + p.isEliminar());
            }
            System.out.println("=== FIN CARGA PERMISOS ===\n");

            return permisos;
        } catch (SQLException e) {
            System.err.println("ERROR al obtener permisos para rol " + idRol + ": " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(vista,
                    "Error al obtener permisos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    public boolean guardarPermisos(List<mPermiso> permisos) {
        System.out.println("=== INICIANDO GUARDADO DE PERMISOS ===");
        System.out.println("Cantidad de permisos a procesar: " + permisos.size());

        try {
            int procesados = 0;
            int exitosos = 0;

            for (mPermiso permiso : permisos) {
                procesados++;
                System.out.println("\n--- Procesando permiso #" + procesados + " ---");
                System.out.println("Rol ID: " + permiso.getIdRol());
                System.out.println("Menu ID temporal: " + permiso.getIdMenu());
                System.out.println("Nombre componente: '" + permiso.getNombreComponente() + "'");
                System.out.println("Permisos: VER=" + permiso.isVer() + ", CREAR=" + permiso.isCrear()
                        + ", LEER=" + permiso.isLeer() + ", ACTUALIZAR=" + permiso.isActualizar()
                        + ", ELIMINAR=" + permiso.isEliminar());

                // ✅ BUSCAR ID REAL basándose en nombre_componente
                int idReal = dao.buscarIdMenuPorComponente(permiso.getNombreComponente());
                System.out.println("ID real encontrado: " + idReal);

                if (idReal > 0) {
                    // Asignar ID real antes de guardar
                    permiso.setIdMenu(idReal);
                    System.out.println("Intentando guardar permiso con ID real: " + idReal);

                    boolean guardado = dao.guardarPermiso(permiso);
                    System.out.println("Resultado del guardado: " + guardado);

                    if (guardado) {
                        exitosos++;
                    } else {
                        System.err.println("FALLO al guardar permiso para componente: " + permiso.getNombreComponente());
                        return false;
                    }
                } else {
                    System.err.println("COMPONENTE NO ENCONTRADO: '" + permiso.getNombreComponente() + "'");
                    // Continuar con los demás (no fallar por uno)
                }
            }

            System.out.println("\n=== RESUMEN GUARDADO ===");
            System.out.println("Procesados: " + procesados);
            System.out.println("Exitosos: " + exitosos);
            System.out.println("=== FIN GUARDADO DE PERMISOS ===\n");

            return true;
        } catch (SQLException e) {
            System.err.println("ERROR SQL en guardarPermisos: " + e.getMessage());
            e.printStackTrace();
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
