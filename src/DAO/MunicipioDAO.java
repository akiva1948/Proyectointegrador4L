package DAO;

import Modelos.Municipio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class MunicipioDAO {
    private Connection conexion;

    public MunicipioDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // CREATE - Agregar municipio
   public boolean agregarMunicipio(Municipio municipio) {
        
        // --- !! ARREGLO AQUÍ (Tabla y Secuencia) !! ---
        String sql = "INSERT INTO proyectons.MUNICIPIO (ID_MUNICIPIO, NOMBRE_MUNICIPIO) VALUES (proyectons.SEQ_MUNICIPIO.NEXTVAL, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, municipio.getNombreMunicipio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar municipio: " + e.getMessage());
            return false;
        }
   }

    // READ - Obtener todos los municipios
    public List<Municipio> obtenerTodosMunicipios() {
        List<Municipio> municipios = new ArrayList<>();
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.MUNICIPIO ORDER BY ID_MUNICIPIO";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Municipio municipio = new Municipio(
                    rs.getInt("ID_MUNICIPIO"),
                    rs.getString("NOMBRE_MUNICIPIO")
                );
                municipios.add(municipio);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar municipios: " + e.getMessage());
        }
        return municipios;
    }

    // READ - Obtener municipio por ID
    public Municipio obtenerMunicipioPorId(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.MUNICIPIO WHERE ID_MUNICIPIO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Municipio(
                    rs.getInt("ID_MUNICIPIO"),
                    rs.getString("NOMBRE_MUNICIPIO")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar municipio: " + e.getMessage());
        }
        return null;
    }

    // READ - Obtener municipio por Nombre (para validación)
    public Municipio obtenerMunicipioPorNombre(String nombre) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.MUNICIPIO WHERE NOMBRE_MUNICIPIO = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Municipio(
                    rs.getInt("ID_MUNICIPIO"),
                    rs.getString("NOMBRE_MUNICIPIO")
                );
            }
        } catch (SQLException e) {
            // No es necesario un popup aquí
        }
        return null;
    }
    
    // READ - Verificar si existe por Nombre (No necesita cambios)
    public boolean existeMunicipioPorNombre(String nombre) {
        return obtenerMunicipioPorNombre(nombre) != null;
    }


    // UPDATE - Actualizar municipio
    public boolean actualizarMunicipio(Municipio municipio) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "UPDATE proyectons.MUNICIPIO SET NOMBRE_MUNICIPIO = ? WHERE ID_MUNICIPIO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, municipio.getNombreMunicipio());
            ps.setInt(2, municipio.getIdMunicipio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar municipio: " + e.getMessage());
            return false;
        }
    }

    // DELETE - Eliminar municipio
    public boolean eliminarMunicipio(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "DELETE FROM proyectons.MUNICIPIO WHERE ID_MUNICIPIO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar el municipio porque está siendo usado en un lugar de producción.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar municipio: " + e.getMessage());
            }
            return false;
        }
    }
}