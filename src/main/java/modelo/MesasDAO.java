package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class MesasDAO {

    // Método para obtener todas las mesas de la base de datos
    public List<Mesa> listarTodas() throws SQLException {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT id, numero, estado, posicion_x, posicion_y, capacidad, ancho, alto, forma FROM mesas ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String numero = rs.getString("numero");
                String estadoStr = rs.getString("estado");
                int posX = rs.getInt("posicion_x");
                int posY = rs.getInt("posicion_y");
                int capacidad = rs.getInt("capacidad");
                int ancho = rs.getInt("ancho");
                int alto = rs.getInt("alto");
                String forma = rs.getString("forma");

                // Convertir el string de estado a enum
                Mesa.EstadoMesa estado = Mesa.EstadoMesa.DISPONIBLE; // Valor por defecto
                for (Mesa.EstadoMesa e : Mesa.EstadoMesa.values()) {
                    if (e.getDescripcion().equalsIgnoreCase(estadoStr)) {
                        estado = e;
                        break;
                    }
                }

                mesas.add(new Mesa(id, numero, estado, new Point(posX, posY), capacidad, ancho, alto, forma));
            }
        } catch (SQLException e) {
            throw e;
        }

        return mesas;
    }

    // Método para guardar una mesa nueva
    public int guardar(Mesa mesa) throws SQLException {
        String sql = "INSERT INTO mesas (numero, estado, posicion_x, posicion_y, capacidad, ancho, alto, forma) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, mesa.getNumero());
            ps.setString(2, mesa.getEstado().getDescripcion());
            ps.setInt(3, mesa.getPosicion().x);
            ps.setInt(4, mesa.getPosicion().y);
            ps.setInt(5, mesa.getCapacidad());
            ps.setInt(6, mesa.getAncho());
            ps.setInt(7, mesa.getAlto());
            ps.setString(8, mesa.getForma());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw e;
        }

        return -1;
    }

    // Método para actualizar una mesa existente
    public boolean actualizar(Mesa mesa) throws SQLException {
        String sql = "UPDATE mesas SET numero = ?, estado = ?, posicion_x = ?, posicion_y = ?, "
                + "capacidad = ?, ancho = ?, alto = ?, forma = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mesa.getNumero());
            ps.setString(2, mesa.getEstado().getDescripcion());
            ps.setInt(3, mesa.getPosicion().x);
            ps.setInt(4, mesa.getPosicion().y);
            ps.setInt(5, mesa.getCapacidad());
            ps.setInt(6, mesa.getAncho());
            ps.setInt(7, mesa.getAlto());
            ps.setString(8, mesa.getForma());
            ps.setInt(9, mesa.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    // Método para eliminar una mesa
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM mesas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    // Método para actualizar el estado de una mesa
    public boolean actualizarEstado(int idMesa, Mesa.EstadoMesa nuevoEstado) throws SQLException {
        String sql = "UPDATE mesas SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.getDescripcion());
            ps.setInt(2, idMesa);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    // Método para cargar mesas por defecto si no hay ninguna
    public void cargarMesasPorDefecto() throws SQLException {
        // Verificar si ya existen mesas
        String sqlCount = "SELECT COUNT(*) FROM mesas";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlCount)) {

            rs.next();
            int count = rs.getInt(1);

            // Si no hay mesas, crear las 10 por defecto
            if (count == 0) {
                // Crear 10 mesas distribuidas en una cuadrícula
                for (int i = 0; i < 10; i++) {
                    int row = i / 5;  // 2 filas
                    int col = i % 5;  // 5 columnas

                    // Calcular la posición en una cuadrícula
                    int x = 100 + (col * 100);
                    int y = 100 + (row * 100);

                    Mesa mesa = new Mesa(0, String.valueOf(i + 1), Mesa.EstadoMesa.DISPONIBLE,
                            new Point(x, y), 4, 60, 60, "CIRCULAR");
                    guardar(mesa);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}
