package util;

import modelo.mPermiso;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache singleton para almacenar menús del sistema leídos desde la interfaz
 */
public class MenusCache {
    private static MenusCache instance;
    private List<mPermiso> menusDelSistema;
    private boolean cacheValido = false;
    
    private MenusCache() {
        menusDelSistema = new ArrayList<>();
    }
    
    public static MenusCache getInstance() {
        if (instance == null) {
            instance = new MenusCache();
        }
        return instance;
    }
    
    public void setMenusDelSistema(List<mPermiso> menus) {
        this.menusDelSistema = new ArrayList<>(menus);
        this.cacheValido = true;
    }
    
    public List<mPermiso> getMenusDelSistema() {
        return new ArrayList<>(menusDelSistema);
    }
    
    public boolean isCacheValido() {
        return cacheValido;
    }
    
    public void invalidarCache() {
        this.cacheValido = false;
        this.menusDelSistema.clear();
    }
}