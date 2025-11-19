package DAO;

import Modelos.Predio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.math.BigDecimal;

public class PredioDAO {
    private Connection conexion;

    public PredioDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // CREATE - Agregar predio
    public boolean agregarPredio(Predio predio) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "INSERT INTO proyectons.PREDIO (ID_PREDIO, UBICACION_MUN, EXTENSION_HECTAREAS, ID_LUGAR_PRODUCCION) VALUES (proyectons.SEQ_PREDIO.NEXTVAL, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, predio.getUbicacionMun());
            ps.setBigDecimal(2, predio.getExtensionHectareas());
            ps.setInt(3, predio.getIdLugarProduccion());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar predio: " + e.getMessage());
            return false;
        }
    }

    // READ - Obtener todos los predios
    public List<Predio> obtenerTodosPredios() {
        List<Predio> predios = new ArrayList<>();
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.PREDIO ORDER BY ID_PREDIO";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Predio predio = new Predio(
                    rs.getInt("ID_PREDIO"),
                    rs.getString("UBICACION_MUN"),
                    rs.getBigDecimal("EXTENSION_HECTAREAS"),
                    rs.getInt("ID_LUGAR_PRODUCCION")
                );
                predios.add(predio);
            }
        } catch (SQLException e) {
            // ¡Este es el error que te va a salir si no lo arreglas!
            JOptionPane.showMessageDialog(null, "Error al cargar predios: " + e.getMessage());
        }
        return predios;
    }

    // READ - Obtener predio por ID
    public Predio obtenerPredioPorId(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.PREDIO WHERE ID_PREDIO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Predio(
                    rs.getInt("ID_PREDIO"),
                    rs.getString("UBICACION_MUN"),
                    rs.getBigDecimal("EXTENSION_HECTAREAS"),
                    rs.getInt("ID_LUGAR_PRODUCCION")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar predio: " + e.getMessage());
        }
        return null;
    }
    
        public List<Predio> obtenerPrediosPorProductor(int idProductor) {
        List<Predio> lista = new ArrayList<>();
        
        // Hacemos JOIN con LUGAR_PRODUCCION para poder filtrar por ID_PRODUCTOR
        String sql = "SELECT p.* FROM proyectons.PREDIO p " +
                     "JOIN proyectons.LUGAR_PRODUCCION l ON p.ID_LUGAR_PRODUCCION = l.ID_LUGAR_PRODUCCION " +
                     "WHERE l.ID_PRODUCTOR = ? ORDER BY p.ID_PREDIO";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Predio(
                        rs.getInt("ID_PREDIO"),
                        rs.getString("UBICACION_MUN"),
                        rs.getBigDecimal("EXTENSION_HECTAREAS"),
                        rs.getInt("ID_LUGAR_PRODUCCION")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listando mis predios: " + e.getMessage());
        }
        return lista;
    }
    // UPDATE - Actualizar predio
    public boolean actualizarPredio(Predio predio) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "UPDATE proyectons.PREDIO SET UBICACION_MUN=?, EXTENSION_HECTAREAS=?, ID_LUGAR_PRODUCCION=? WHERE ID_PREDIO=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, predio.getUbicacionMun());
            ps.setBigDecimal(2, predio.getExtensionHectareas());
            ps.setInt(3, predio.getIdLugarProduccion());
            ps.setInt(4, predio.getIdPredio());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar predio: " + e.getMessage());
            return false;
        }
    }

    
    // DELETE - Eliminar predio
    public boolean eliminarPredio(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "DELETE FROM proyectons.PREDIO WHERE ID_PREDIO=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar el predio porque tiene lotes asociados.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar predio: " + e.getMessage());
            }
            return false;
        }
    }
    
    
}