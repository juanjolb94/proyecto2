package servicio;

import modelo.VentasDAO;
import modelo.mVentas;
import java.awt.print.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class sImpresionTicket implements Printable {
    
    private mVentas venta;
    private VentasDAO ventasDAO;
    private final DecimalFormat formatoNumero = new DecimalFormat("#,##0");
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public sImpresionTicket(int idVenta) throws SQLException {
        this.ventasDAO = new VentasDAO();
        this.venta = ventasDAO.buscarVentaPorId(idVenta);
    }
    
    public void imprimirTicket() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            
            // Configurar papel para ticket (58mm)
            PageFormat pf = job.defaultPage();
            Paper paper = new Paper();
            paper.setSize(58 * 2.83, 200 * 2.83); // 58mm ancho
            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pf.setPaper(paper);
            
            // Mostrar diálogo de impresión
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException e) {
            System.err.println("Error de impresión: " + e.getMessage());
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Configurar fuente pequeña para ticket
        Font fontTitulo = new Font("Monospaced", Font.BOLD, 10);
        Font fontNormal = new Font("Monospaced", Font.PLAIN, 8);
        
        int y = 10;
        int lineHeight = 12;
        
        // Encabezado
        g2d.setFont(fontTitulo);
        g2d.drawString("NOMBRE DE TU EMPRESA", 10, y);
        y += lineHeight;
        
        g2d.setFont(fontNormal);
        g2d.drawString("RUC: 80000000-0", 10, y);
        y += lineHeight;
        g2d.drawString("Dir: Tu dirección", 10, y);
        y += lineHeight;
        g2d.drawString("Tel: (021) 000-000", 10, y);
        y += lineHeight * 2;
        
        // Datos de la venta
        g2d.drawString("Factura: " + venta.getNumeroFactura(), 10, y);
        y += lineHeight;
        g2d.drawString("Fecha: " + formatoFecha.format(venta.getFecha()), 10, y);
        y += lineHeight;
        g2d.drawString("Timbrado: " + venta.getNumeroTimbrado(), 10, y);
        y += lineHeight * 2;
        
        // Línea separadora
        g2d.drawString("--------------------------------", 10, y);
        y += lineHeight;
        
        // Detalles de productos
        for (mVentas.DetalleVenta detalle : venta.getDetalles()) {
            // Obtener descripción del producto
            String descripcion = obtenerDescripcionProducto(detalle.getIdProducto());
            
            g2d.drawString(descripcion, 10, y);
            y += lineHeight;
            
            String lineaDetalle = String.format("%d x %s = %s", 
                detalle.getCantidad(),
                formatoNumero.format(detalle.getPrecioUnitario()),
                formatoNumero.format(detalle.getSubtotal())
            );
            g2d.drawString(lineaDetalle, 10, y);
            y += lineHeight * 2;
        }
        
        // Línea separadora
        g2d.drawString("--------------------------------", 10, y);
        y += lineHeight;
        
        // Total
        g2d.setFont(fontTitulo);
        g2d.drawString("TOTAL: Gs. " + formatoNumero.format(venta.getTotal()), 10, y);
        y += lineHeight * 2;
        
        g2d.setFont(fontNormal);
        g2d.drawString("Gracias por su compra", 10, y);
        
        return PAGE_EXISTS;
    }
    
    private String obtenerDescripcionProducto(int idProducto) {
        try {
            // Implementar búsqueda de descripción del producto
            return "Producto ID: " + idProducto;
        } catch (Exception e) {
            return "Producto";
        }
    }
}