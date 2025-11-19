package DAO;

import Modelos.Productor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductorDAO {
    private Connection conexion;

    public ProductorDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // -------------------------------------------------------------------------
    // CREATE - Agregar productor (Llama al Robot: sp_crear_productor)
    // -------------------------------------------------------------------------
    public boolean agregarProductor(Productor productor) {
        // Llama al procedimiento almacenado del dueño 'proyectons'
        String sql = "{CALL proyectons.sp_crear_productor(?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            // Pasamos los parámetros en el orden del SP
            cs.setString(1, productor.getNombreProductor());
            cs.setString(2, productor.getCedulaCiudadania());
            cs.setString(3, productor.getTelefono());
            cs.setString(4, productor.getCorreo());
            cs.setString(5, productor.getContrasena());
            
            cs.execute();
            return true;
            
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-00001")) { 
                 JOptionPane.showMessageDialog(null, "Error: Ya existe un productor con esa cédula o correo.");
            } else if (e.getMessage().contains("ORA-01920")) { 
                 JOptionPane.showMessageDialog(null, "Error: El usuario de base de datos ya existe.");
            } else {
                 JOptionPane.showMessageDialog(null, "Error al crear productor: " + e.getMessage());
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // READ - Obtener todos (SELECT directo con prefijo 'proyectons.')
    // -------------------------------------------------------------------------
    public List<Productor> obtenerTodosProductores() {
        List<Productor> productores = new ArrayList<>();
        
        // ¡OJO! Prefijo 'proyectons.'
        String sql = "SELECT * FROM proyectons.PRODUCTOR ORDER BY ID_PRODUCTOR";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Productor productor = new Productor(
                    rs.getInt("ID_PRODUCTOR"),
                    rs.getString("NOMBRE_PRODUCTOR"),
                    rs.getString("CEDULA_CIUDADANIA"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
                productores.add(productor);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar productores: " + e.getMessage());
        }
        return productores;
    }

    // -------------------------------------------------------------------------
    // READ - Obtener por ID (con prefijo 'proyectons.')
    // -------------------------------------------------------------------------
    public Productor obtenerProductorPorId(int id) {
        String sql = "SELECT * FROM proyectons.PRODUCTOR WHERE ID_PRODUCTOR = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Productor(
                    rs.getInt("ID_PRODUCTOR"),
                    rs.getString("NOMBRE_PRODUCTOR"),
                    rs.getString("CEDULA_CIUDADANIA"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar productor: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // READ - Obtener por Cédula (con prefijo 'proyectons.')
    // -------------------------------------------------------------------------
    public Productor obtenerProductorPorDocumento(String cedula) {
        String sql = "SELECT * FROM proyectons.PRODUCTOR WHERE CEDULA_CIUDADANIA = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Productor(
                    rs.getInt("ID_PRODUCTOR"),
                    rs.getString("NOMBRE_PRODUCTOR"),
                    rs.getString("CEDULA_CIUDADANIA"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar productor: " + e.getMessage());
        }
        return null;
    }

    public boolean existeProductorPorDocumento(String cedula) {
        return obtenerProductorPorDocumento(cedula) != null;
    }

    // -------------------------------------------------------------------------
    // UPDATE - Actualizar CON contraseña (Llama al Robot: sp_actualizar_productor)
    // -------------------------------------------------------------------------
    public boolean actualizarProductorConContrasena(Productor productor) {
        // Llama al Robot de Actualizar del dueño 'proyectons'
        String sql = "{CALL proyectons.sp_actualizar_productor(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, productor.getIdProductor());
            cs.setString(2, productor.getNombreProductor());
            cs.setString(3, productor.getCedulaCiudadania());
            cs.setString(4, productor.getTelefono());
            cs.setString(5, productor.getCorreo());
            cs.setString(6, productor.getContrasena()); // Se envía la NUEVA contraseña
            
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar productor: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE - Actualizar SIN contraseña (Llama al Robot con NULL)
    // -------------------------------------------------------------------------
    public boolean actualizarProductorSinContrasena(Productor productor) {
        // Llama al mismo Robot
        String sql = "{CALL proyectons.sp_actualizar_productor(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, productor.getIdProductor());
            cs.setString(2, productor.getNombreProductor());
            cs.setString(3, productor.getCedulaCiudadania());
            cs.setString(4, productor.getTelefono());
            cs.setString(5, productor.getCorreo());
            cs.setNull(6, java.sql.Types.VARCHAR); // <-- Se envía NULL en la contraseña
            
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar productor: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // DELETE - Eliminar productor (Llama al Robot: sp_eliminar_productor)
    // -------------------------------------------------------------------------
    public boolean eliminarProductor(int id) {
        // Llama al Robot de Eliminar del dueño 'proyectons'
        String sql = "{CALL proyectons.sp_eliminar_productor(?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar el productor porque tiene lugares de producción asociados.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar productor: " + e.getMessage());
            }
            return false;
        }
    }

public int obtenerIdPorCorreo(String correo) {
        int id = -1;
        // Usamos UPPER para evitar problemas si escribieron Mayúsculas/Minúsculas diferente
        String sql = "SELECT ID_PRODUCTOR FROM proyectons.PRODUCTOR WHERE UPPER(CORREO) = UPPER(?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, correo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("ID_PRODUCTOR");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscando ID del productor: " + e.getMessage());
        }
        
        return id;
    }


}