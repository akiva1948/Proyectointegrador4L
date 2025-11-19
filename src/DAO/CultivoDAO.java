package DAO;

import Modelos.Cultivo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CultivoDAO {
    private Connection conexion;

    public CultivoDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // CREATE - Agregar cultivo
    public boolean agregarCultivo(Cultivo cultivo) {
        
        // --- !! ARREGLO AQUÍ (Tabla y Secuencia) !! ---
        String sql = "INSERT INTO proyectons.CULTIVO (ID_CULTIVO, ESPECIE, NOMBRES, VARIEDAD, CICLO) VALUES (proyectons.SEQ_CULTIVO.NEXTVAL, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, cultivo.getEspecie());
            ps.setString(2, cultivo.getNombres());
            ps.setString(3, cultivo.getVariedad());
            ps.setString(4, cultivo.getCiclo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar cultivo: " + e.getMessage());
            return false;
        }
    }

    // READ - Obtener todos los cultivos
    public List<Cultivo> obtenerTodosCultivos() {
        List<Cultivo> cultivos = new ArrayList<>();
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.CULTIVO ORDER BY ID_CULTIVO";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Cultivo cultivo = new Cultivo(
                    rs.getInt("ID_CULTIVO"),
                    rs.getString("ESPECIE"),
                    rs.getString("NOMBRES"),
                    rs.getString("VARIEDAD"),
                    rs.getString("CICLO")
                );
                cultivos.add(cultivo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar cultivos: " + e.getMessage());
        }
        return cultivos;
    }

    // READ - Obtener cultivo por ID
    public Cultivo obtenerCultivoPorId(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.CULTIVO WHERE ID_CULTIVO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Cultivo(
                    rs.getInt("ID_CULTIVO"),
                    rs.getString("ESPECIE"),
                    rs.getString("NOMBRES"),
                    rs.getString("VARIEDAD"),
                    rs.getString("CICLO")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar cultivo: " + e.getMessage());
        }
        return null;
    }

    // READ - Obtener cultivo por Nombre (para validación)
    public Cultivo obtenerCultivoPorNombre(String nombre) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.CULTIVO WHERE NOMBRES = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Cultivo(
                    rs.getInt("ID_CULTIVO"),
                    rs.getString("ESPECIE"),
                    rs.getString("NOMBRES"),
                    rs.getString("VARIEDAD"),
                    rs.getString("CICLO")
                );
            }
        } catch (SQLException e) {
            // No se necesita popup
        }
        return null;
    }

    // READ - Verificar si existe por Nombre (No necesita cambios)
    public boolean existeCultivoPorNombre(String nombre) {
        return obtenerCultivoPorNombre(nombre) != null;
    }

    // UPDATE - Actualizar cultivo
    public boolean actualizarCultivo(Cultivo cultivo) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "UPDATE proyectons.CULTIVO SET ESPECIE=?, NOMBRES=?, VARIEDAD=?, CICLO=? WHERE ID_CULTIVO=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, cultivo.getEspecie());
            ps.setString(2, cultivo.getNombres());
            ps.setString(3, cultivo.getVariedad());
            ps.setString(4, cultivo.getCiclo());
            ps.setInt(5, cultivo.getIdCultivo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar cultivo: " + e.getMessage());
            return false;
        }
    }

    // DELETE - Eliminar cultivo
    public boolean eliminarCultivo(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "DELETE FROM proyectons.CULTIVO WHERE ID_CULTIVO=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar el cultivo porque tiene lotes o plagas asociadas.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar cultivo: " + e.getMessage());
            }
            return false;
        }
    }
}