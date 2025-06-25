package modelo;

public class mPermiso {

    private int idPermiso;
    private int idRol;
    private int idMenu;
    private String nombreMenu;
    private String nombreComponente;
    private boolean ver;
    private boolean crear;
    private boolean leer;
    private boolean actualizar;
    private boolean eliminar;

    // Constructor vacío
    public mPermiso() {
    }

    // Constructor completo
    public mPermiso(int idPermiso, int idRol, int idMenu, String nombreMenu,
            String nombreComponente, boolean ver, boolean crear,
            boolean leer, boolean actualizar, boolean eliminar) {
        this.idPermiso = idPermiso;
        this.idRol = idRol;
        this.idMenu = idMenu;
        this.nombreMenu = nombreMenu;
        this.nombreComponente = nombreComponente;
        this.ver = ver;
        this.crear = crear;
        this.leer = leer;
        this.actualizar = actualizar;
        this.eliminar = eliminar;
    }

    // Getters y Setters
    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public String getNombreMenu() {
        return nombreMenu;
    }

    public void setNombreMenu(String nombreMenu) {
        this.nombreMenu = nombreMenu;
    }

    public String getNombreComponente() {
        return nombreComponente;
    }

    public void setNombreComponente(String nombreComponente) {
        this.nombreComponente = nombreComponente;
    }

    public boolean isVer() {
        return ver;
    }

    public void setVer(boolean ver) {
        this.ver = ver;
    }

    public boolean isCrear() {
        return crear;
    }

    public void setCrear(boolean crear) {
        this.crear = crear;
    }

    public boolean isLeer() {
        return leer;
    }

    public void setLeer(boolean leer) {
        this.leer = leer;
    }

    public boolean isActualizar() {
        return actualizar;
    }

    public void setActualizar(boolean actualizar) {
        this.actualizar = actualizar;
    }

    public boolean isEliminar() {
        return eliminar;
    }

    public void setEliminar(boolean eliminar) {
        this.eliminar = eliminar;
    }
}
