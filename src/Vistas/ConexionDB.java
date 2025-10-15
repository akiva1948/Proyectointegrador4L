package Vistas; // O en un paquete de utilidades si lo tienes

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String USER = "proyectoNSC";
    private static final String PASSWORD = "proyectoNSC";
    private static final String URL = "jdbc:oracle:thin:@192.168.254.215:1521:orcl";

    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver JDBC de Oracle: " + e.getMessage());
            // Lanza la excepción para que el llamador sepa que algo falló
            throw new SQLException("Driver de Oracle no encontrado. Asegúrate de que el archivo ojdbc.jar esté en las librerías.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    

}
