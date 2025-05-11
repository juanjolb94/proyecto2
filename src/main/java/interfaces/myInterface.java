package interfaces;

public interface myInterface {
    void imGrabar();
    void imFiltrar();
    void imActualizar();
    void imBorrar();
    void imNuevo();
    void imBuscar();
    
    void imPrimero();
    void imSiguiente();
    void imAnterior();
    void imUltimo();
    void imImprimir();
    
    void imInsDet();
    void imDelDet();
    
    void imCerrar();
    boolean imAbierto();
    void imAbrir();
    
    String getTablaActual();
    String[] getCamposBusqueda();
    void setRegistroSeleccionado(int id);
}
