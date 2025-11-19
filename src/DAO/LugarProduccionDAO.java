package DAO;

import Modelos.LugarProduccion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class LugarProduccionDAO {
    private Connection conexion;

    public LugarProduccionDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // 1. CREATE - Agregar lugar
    public boolean agregarLugarProduccion(LugarProduccion lugar) {
        String sql = "INSERT INTO proyectons.LUGAR_PRODUCCION (ID_LUGAR_PRODUCCION, NOMBRE_LUGAR, TELEFONO, CORREO, ID_MUNICIPIO, ID_PRODUCTOR) VALUES (proyectons.SEQ_LUGAR_PROD.NEXTVAL, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, lugar.getNombreLugar());
            ps.setString(2, lugar.getTelefono());
            ps.setString(3, lugar.getCorreo());
            ps.setInt(4, lugar.getIdMunicipio());
            ps.setInt(5, lugar.getIdProductor());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar lugar: " + e.getMessage());
            return false;
        }
    }

    // 2. READ - Obtener TODOS (Para admin si lo necesitas)
    public List<LugarProduccion> obtenerTodosLugares() {
        List<LugarProduccion> lugares = new ArrayList<>();
        String sql = "SELECT * FROM proyectons.LUGAR_PRODUCCION ORDER BY ID_LUGAR_PRODUCCION";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lugares.add(new LugarProduccion(
                    rs.getInt("ID_LUGAR_PRODUCCION"),
                    rs.getString("NOMBRE_LUGAR"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getInt("ID_MUNICIPIO"),
                    rs.getInt("ID_PRODUCTOR")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener lugares: " + e.getMessage());
        }
        return lugares;
    }
    
    public List<LugarProduccion> obtenerLugaresPorProductor(int idProductor) {
            List<LugarProduccion> lista = new ArrayList<>();

            // FILTRAMOS POR TU ID
            String sql = "SELECT * FROM proyectons.LUGAR_PRODUCCION WHERE ID_PRODUCTOR = ? ORDER BY ID_LUGAR_PRODUCCION";

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idProductor); // Le pasamos tu ID

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new LugarProduccion(
                            rs.getInt("ID_LUGAR_PRODUCCION"),
                            rs.getString("NOMBRE_LUGAR"),
                            rs.getString("TELEFONO"),
                            rs.getString("CORREO"),
                            rs.getInt("ID_MUNICIPIO"),
                            rs.getInt("ID_PRODUCTOR")
                        ));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error listando mis lugares: " + e.getMessage());
            }
            return lista;
        }

    // 4. READ - Obtener UN lugar por ID (Para editar)
    public LugarProduccion obtenerLugarPorId(int idLugar) {
        String sql = "SELECT * FROM proyectons.LUGAR_PRODUCCION WHERE ID_LUGAR_PRODUCCION = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idLugar);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new LugarProduccion(
                    rs.getInt("ID_LUGAR_PRODUCCION"),
                    rs.getString("NOMBRE_LUGAR"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getInt("ID_MUNICIPIO"),
                    rs.getInt("ID_PRODUCTOR")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 5. UPDATE - Actualizar lugar
    public boolean actualizarLugarProduccion(LugarProduccion lugar) {
        String sql = "UPDATE proyectons.LUGAR_PRODUCCION SET NOMBRE_LUGAR=?, TELEFONO=?, CORREO=?, ID_MUNICIPIO=?, ID_PRODUCTOR=? WHERE ID_LUGAR_PRODUCCION=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, lugar.getNombreLugar());
            ps.setString(2, lugar.getTelefono());
            ps.setString(3, lugar.getCorreo());
            ps.setInt(4, lugar.getIdMunicipio());
            ps.setInt(5, lugar.getIdProductor());
            ps.setInt(6, lugar.getIdLugarProduccion());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar lugar: " + e.getMessage());
            return false;
        }
    }

    // 6. DELETE - Eliminar lugar
    public boolean eliminarLugarProduccion(int id) {
        String sql = "DELETE FROM proyectons.LUGAR_PRODUCCION WHERE ID_LUGAR_PRODUCCION=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar el lugar porque tiene predios asociados.", "Error de integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar lugar: " + e.getMessage());
            }
            return false;
        }
    }
}