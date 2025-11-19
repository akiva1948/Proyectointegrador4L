package DAO;

import Modelos.Plaga;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class PlagaDAO {
    private Connection conexion;

    public PlagaDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // CREATE - Agregar plaga
    public boolean agregarPlaga(Plaga plaga) {
        
        // --- !! ARREGLO AQUÍ (Tabla y Secuencia) !! ---
        String sql = "INSERT INTO proyectons.PLAGA (ID_PLAGA, NOMBRE_PLAGA, ESPECIE) VALUES (proyectons.SEQ_PLAGA.NEXTVAL, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, plaga.getNombrePlaga());
            ps.setString(2, plaga.getEspecie());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar plaga: " + e.getMessage());
            return false;
        }
    }

    // READ - Obtener todas las plagas
    public List<Plaga> obtenerTodasPlagas() {
        List<Plaga> plagas = new ArrayList<>();
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.PLAGA ORDER BY ID_PLAGA";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Plaga plaga = new Plaga(
                    rs.getInt("ID_PLAGA"),
                    rs.getString("NOMBRE_PLAGA"),
                    rs.getString("ESPECIE")
                );
                plagas.add(plaga);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar plagas: " + e.getMessage());
        }
        return plagas;
    }

    // READ - Obtener plaga por ID
    public Plaga obtenerPlagaPorId(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.PLAGA WHERE ID_PLAGA = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Plaga(
                    rs.getInt("ID_PLAGA"),
                    rs.getString("NOMBRE_PLAGA"),
                    rs.getString("ESPECIE")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar plaga: " + e.getMessage());
        }
        return null;
    }

    // READ - Obtener plaga por Nombre (para validación)
    public Plaga obtenerPlagaPorNombre(String nombre) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.PLAGA WHERE NOMBRE_PLAGA = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Plaga(
                    rs.getInt("ID_PLAGA"),
                    rs.getString("NOMBRE_PLAGA"),
                    rs.getString("ESPECIE")
                );
            }
        } catch (SQLException e) {
            // No se necesita popup
        }
        return null;
    }

    // READ - Verificar si existe por Nombre (Este no necesita cambios)
    public boolean existePlagaPorNombre(String nombre) {
        return obtenerPlagaPorNombre(nombre) != null;
    }

    // UPDATE - Actualizar plaga
    public boolean actualizarPlaga(Plaga plaga) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "UPDATE proyectons.PLAGA SET NOMBRE_PLAGA=?, ESPECIE=? WHERE ID_PLAGA=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, plaga.getNombrePlaga());
            ps.setString(2, plaga.getEspecie());
            ps.setInt(3, plaga.getIdPlaga());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar plaga: " + e.getMessage());
            return false;
        }
    }

    // DELETE - Eliminar plaga
    public boolean eliminarPlaga(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "DELETE FROM proyectons.PLAGA WHERE ID_PLAGA=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar la plaga porque está asociada a un cultivo.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar plaga: " + e.getMessage());
            }
            return false;
        }
    }
}