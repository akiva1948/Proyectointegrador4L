package DAO;

import Modelos.Lote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import DTO.InformeProduccionDTO;

public class LoteDAO {
    private Connection conexion;

    public LoteDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // CREATE - Corregido
    public boolean agregarLote(Lote lote) {
        
        // --- !! ARREGLO AQUÍ (Tabla y Secuencia) !! ---
        String sql = "INSERT INTO proyectons.LOTE (ID_LOTE, FECHA_SIEMBRA, ESTADO, ID_LUGAR_PRODUCCION, ID_CULTIVO) VALUES (proyectons.SEQ_LOTE.NEXTVAL, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(lote.getFechaSiembra().getTime()));
            ps.setString(2, lote.getEstado());
            ps.setInt(3, lote.getIdLugarProduccion()); 
            ps.setInt(4, lote.getIdCultivo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar lote: " + e.getMessage());
            return false;
        }
    }

    // READ - Corregido
    public List<Lote> obtenerTodosLotes() {
        List<Lote> lotes = new ArrayList<>();
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.LOTE ORDER BY ID_LOTE";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Lote lote = new Lote(
                    rs.getInt("ID_LOTE"),
                    rs.getDate("FECHA_SIEMBRA"), 
                    rs.getString("ESTADO"),
                    rs.getInt("ID_LUGAR_PRODUCCION"), 
                    rs.getInt("ID_CULTIVO")
                );
                lotes.add(lote);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar lotes: " + e.getMessage());
        }
        return lotes;
    }
    
    // READ - Corregido
    public Lote obtenerLotePorId(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "SELECT * FROM proyectons.LOTE WHERE ID_LOTE = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Lote(
                    rs.getInt("ID_LOTE"),
                    rs.getDate("FECHA_SIEMBRA"),
                    rs.getString("ESTADO"),
                    rs.getInt("ID_LUGAR_PRODUCCION"), 
                    rs.getInt("ID_CULTIVO")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar lote: " + e.getMessage());
        }
        return null;
    }

    // UPDATE - Corregido
    public boolean actualizarLote(Lote lote) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "UPDATE proyectons.LOTE SET FECHA_SIEMBRA=?, ESTADO=?, ID_LUGAR_PRODUCCION=?, ID_CULTIVO=? WHERE ID_LOTE=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(lote.getFechaSiembra().getTime()));
            ps.setString(2, lote.getEstado());
            ps.setInt(3, lote.getIdLugarProduccion()); 
            ps.setInt(4, lote.getIdCultivo());
            ps.setInt(5, lote.getIdLote());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar lote: " + e.getMessage());
            return false;
        }
    }

    // DELETE - (No cambia)
    public boolean eliminarLote(int id) {
        
        // --- !! ARREGLO AQUÍ !! ---
        String sql = "DELETE FROM proyectons.LOTE WHERE ID_LOTE=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar lote: " + e.getMessage());
            return false;
        }
    }
    public List<DTO.InformeProduccionDTO> obtenerInformePorProductor(int idProductor) {
        List<DTO.InformeProduccionDTO> lista = new ArrayList<>();
        
        // ¡Mira qué limpia queda la consulta gracias a la VISTA!
        String sql = "SELECT * FROM proyectons.V_INFORME_PRODUCCION WHERE ID_PRODUCTOR = ? ORDER BY ID_LOTE DESC";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DTO.InformeProduccionDTO(
                        rs.getInt("ID_LOTE"),
                        rs.getString("NOMBRE_CULTIVO"),
                        rs.getString("VARIEDAD"),
                        rs.getString("NOMBRE_LUGAR"),
                        rs.getDate("FECHA_SIEMBRA"),
                        rs.getString("REPORTE_COSECHA")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error leyendo vista de informe: " + e.getMessage());
        }
        return lista;
    }
    
    public boolean registrarProduccionSinTablas(int idLote, String cantidad, String fecha) {
        // Guardamos todo en la columna ESTADO para no modificar tablas
        String info = "COSECHADO (Cant: " + cantidad + " - Fecha: " + fecha + ")";
        String sql = "UPDATE proyectons.LOTE SET ESTADO = ? WHERE ID_LOTE = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, info);
            ps.setInt(2, idLote);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return false;
        }
    }
    
    public List<Lote> obtenerLotesPorProductor(int idProductor) {
        List<Lote> lista = new ArrayList<>();
        
        // JOIN para filtrar lotes que pertenecen a lugares de ESTE productor
        String sql = "SELECT lt.* FROM proyectons.LOTE lt " +
                     "JOIN proyectons.LUGAR_PRODUCCION lp ON lt.ID_LUGAR_PRODUCCION = lp.ID_LUGAR_PRODUCCION " +
                     "WHERE lp.ID_PRODUCTOR = ? ORDER BY lt.ID_LOTE";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Lote(
                        rs.getInt("ID_LOTE"),
                        rs.getDate("FECHA_SIEMBRA"),
                        rs.getString("ESTADO"),
                        rs.getInt("ID_LUGAR_PRODUCCION"),
                        rs.getInt("ID_CULTIVO")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listando mis lotes: " + e.getMessage());
        }
        return lista;
    }
    
    public List<String[]> obtenerLotesParaSolicitud(int idProductor) {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT ID_LOTE, CULTIVO, VARIEDAD, NOMBRE_LUGAR, ESTADO " +
                     "FROM proyectons.V_GESTION_SOLICITUDES WHERE ID_PRODUCTOR = ? ORDER BY ID_LOTE";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("ID_LOTE")),
                        rs.getString("CULTIVO") + " - " + rs.getString("VARIEDAD"),
                        rs.getString("NOMBRE_LUGAR"),
                        rs.getString("ESTADO")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error vista solicitudes: " + e.getMessage());
        }
        return lista;
    }

    // 2. REALIZAR LA SOLICITUD (Cambia el estado del lote)
    public boolean crearSolicitudVisita(int idLote, String fechaDeseada) {
        // Truco: Guardamos la fecha en el estado para que el técnico la vea
        String nuevoEstado = "SOLICITUD_PENDIENTE [" + fechaDeseada + "]";
        String sql = "UPDATE proyectons.LOTE SET ESTADO = ? WHERE ID_LOTE = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idLote);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al solicitar: " + e.getMessage());
            return false;
        }
    }
    
    public List<String[]> obtenerMisSolicitudes(int idProductor) {
        List<String[]> lista = new ArrayList<>();
        
        // Hacemos JOIN con Lugar y Cultivo para mostrar nombres, no números
        String sql = "SELECT l.ID_LOTE, c.NOMBRES AS CULTIVO, lp.NOMBRE_LUGAR, l.ESTADO " +
                     "FROM proyectons.LOTE l " +
                     "JOIN proyectons.LUGAR_PRODUCCION lp ON l.ID_LUGAR_PRODUCCION = lp.ID_LUGAR_PRODUCCION " +
                     "JOIN proyectons.CULTIVO c ON l.ID_CULTIVO = c.ID_CULTIVO " +
                     "WHERE lp.ID_PRODUCTOR = ? " +
                     "ORDER BY l.ID_LOTE DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("ID_LOTE")),
                        rs.getString("CULTIVO"),
                        rs.getString("NOMBRE_LUGAR"),
                        rs.getString("ESTADO")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listando solicitudes: " + e.getMessage());
        }
        return lista;
    }
    
    
}