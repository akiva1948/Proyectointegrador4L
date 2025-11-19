package DAO; // O en un paquete de utilidades si lo tienes

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String USER = "proyectons";
    private static final String PASSWORD = "proyectons";
    
    // --- !! 1. LA URL CORRECTA (con BARRA /) !! ---
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";

    // --- 2. Cargador del Driver (se ejecuta 1 vez) ---
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error FATAL al cargar el driver JDBC de Oracle: " + e.getMessage());
            throw new RuntimeException("Driver de Oracle no encontrado. Asegúrate de que el archivo ojdbc.jar esté en las librerías.", e);
        }
    }

    // Este es para el super-usuario (Admin creando usuarios, etc.)
    public static Connection getConnection() throws SQLException {
        // Usa la URL correcta
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // --- !! 3. EL MÉTODO DEL LOGIN (CORREGIDO) !! ---
    public static Connection getConnection(String username, String password) throws SQLException {
        // ¡YA NO RE-DEFINE la url! Usa la variable URL estática,
        // que SABEMOS que está bien (con la BARRA /).
        return DriverManager.getConnection(URL, username, password);    
    }
    
}