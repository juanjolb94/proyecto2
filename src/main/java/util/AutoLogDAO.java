package util;

import java.sql.*;
import modelo.DatabaseConnection;

public class AutoLogDAO {

    public static Connection getConnectionWithLogging() throws SQLException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            LogManager.getInstance().log(
                    LogManager.Modulo.SISTEMA,
                    "DB_CONNECTION",
                    LogManager.Nivel.INFO,
                    "Conexión establecida correctamente"
            );
            return conn;
        } catch (SQLException e) {
            LogManager.getInstance().logError("AutoLogDAO", "getConnection", e);
            throw e;
        }
    }

    public static PreparedStatement prepareStatementWithLogging(Connection conn, String sql, String operacion) throws SQLException {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            LogManager.getInstance().log(
                    LogManager.Modulo.SISTEMA,
                    "SQL_PREPARED",
                    LogManager.Nivel.INFO,
                    "Operación: " + operacion
            );
            return ps;
        } catch (SQLException e) {
            LogManager.getInstance().logError("AutoLogDAO", "prepareStatement", e);
            throw e;
        }
    }
}
