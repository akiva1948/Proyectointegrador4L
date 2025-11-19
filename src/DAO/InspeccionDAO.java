package DAO;

import Modelos.Inspeccion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class InspeccionDAO {
    private Connection conexion;

    public InspeccionDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // --------------------------------------------------------------------------------
    // CREATE - Agregar inspección
    // --------------------------------------------------------------------------------
    public boolean agregarInspeccion(Inspeccion inspeccion) {
        // NOTA: No insertamos NIVEL_INCIDENCIA ni NIVEL_ALERTA en el SQL.
        // El TRIGGER 'TRG_CALCULAR_ALERTA' en Oracle se activará automáticamente al insertar
        // y hará los cálculos matemáticos.
        
        String sql = "INSERT INTO proyectons.INSPECCION_FITOSANITARIA " + 
                     "(ID_INSPECCION, FECHA_INSPECCION, OBSERVACIONES, ID_LOTE, ID_TECNICO, ID_PLAGA, CANTIDAD_PLANTAS, PLANTAS_AFECTADAS) " + 
                     "VALUES (proyectons.SEQ_INSPECCION.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inspeccion.getFechaInspeccion().getTime()));
            ps.setString(2, inspeccion.getObservaciones());
            ps.setInt(3, inspeccion.getIdLote());
            ps.setInt(4, inspeccion.getIdTecnico());
            ps.setInt(5, inspeccion.getIdPlaga());
            // Enviamos los datos crudos para que el Trigger trabaje
            ps.setInt(6, inspeccion.getCantidadPlantas());
            ps.setInt(7, inspeccion.getPlantasAfectadas());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al agregar inspección: " + e.getMessage());
            return false;
        }
    }

    // --------------------------------------------------------------------------------
    // READ - Obtener todas (Para el Admin o Técnico)
    // --------------------------------------------------------------------------------
    public List<Inspeccion> obtenerTodasInspecciones() {
        List<Inspeccion> inspecciones = new ArrayList<>();
        String sql = "SELECT * FROM proyectons.INSPECCION_FITOSANITARIA ORDER BY ID_INSPECCION DESC";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Al leer, TRAEMOS TODO. Aquí ya vendrán los datos calculados por el Trigger.
                Inspeccion inspeccion = new Inspeccion(
                    rs.getInt("ID_INSPECCION"),
                    rs.getDate("FECHA_INSPECCION"),
                    rs.getString("OBSERVACIONES"),
                    rs.getInt("ID_LOTE"),
                    rs.getInt("ID_TECNICO"),
                    rs.getInt("ID_PLAGA"),
                    rs.getInt("CANTIDAD_PLANTAS"),
                    rs.getInt("PLANTAS_AFECTADAS"),
                    rs.getDouble("NIVEL_INCIDENCIA"), // Dato llenado por el Trigger
                    rs.getString("NIVEL_ALERTA")      // Dato llenado por el Trigger
                );
                inspecciones.add(inspeccion);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar inspecciones: " + e.getMessage());
        }
        return inspecciones;
    }
    
    // --------------------------------------------------------------------------------
    // READ - Por ID (Para cargar en el formulario de edición)
    // --------------------------------------------------------------------------------
    public Inspeccion obtenerInspeccionPorId(int id) {
        String sql = "SELECT * FROM proyectons.INSPECCION_FITOSANITARIA WHERE ID_INSPECCION = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Inspeccion(
                    rs.getInt("ID_INSPECCION"),
                    rs.getDate("FECHA_INSPECCION"),
                    rs.getString("OBSERVACIONES"),
                    rs.getInt("ID_LOTE"),
                    rs.getInt("ID_TECNICO"),
                    rs.getInt("ID_PLAGA"),
                    rs.getInt("CANTIDAD_PLANTAS"),
                    rs.getInt("PLANTAS_AFECTADAS"),
                    rs.getDouble("NIVEL_INCIDENCIA"),
                    rs.getString("NIVEL_ALERTA")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar inspección: " + e.getMessage());
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // UPDATE - Actualizar
    // --------------------------------------------------------------------------------
    public boolean actualizarInspeccion(Inspeccion inspeccion) {
        // Al actualizar las cantidades, el Trigger se disparará de nuevo y actualizará
        // automáticamente la Incidencia y la Alerta en la BD. No hace falta enviarlas.
        String sql = "UPDATE proyectons.INSPECCION_FITOSANITARIA SET FECHA_INSPECCION=?, OBSERVACIONES=?, ID_LOTE=?, ID_TECNICO=?, ID_PLAGA=?, CANTIDAD_PLANTAS=?, PLANTAS_AFECTADAS=? WHERE ID_INSPECCION=?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(inspeccion.getFechaInspeccion().getTime()));
            ps.setString(2, inspeccion.getObservaciones());
            ps.setInt(3, inspeccion.getIdLote());
            ps.setInt(4, inspeccion.getIdTecnico());
            ps.setInt(5, inspeccion.getIdPlaga());
            ps.setInt(6, inspeccion.getCantidadPlantas());
            ps.setInt(7, inspeccion.getPlantasAfectadas());
            ps.setInt(8, inspeccion.getIdInspeccion());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar inspección: " + e.getMessage());
            return false;
        }
    }

    // --------------------------------------------------------------------------------
    // DELETE - Eliminar
    // --------------------------------------------------------------------------------
    public boolean eliminarInspeccion(int id) {
        String sql = "DELETE FROM proyectons.INSPECCION_FITOSANITARIA WHERE ID_INSPECCION=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
            return false;
        }
    }
    
    // --------------------------------------------------------------------------------
    // REPORTE PARA EL PRODUCTOR (Vista Bonita)
    // --------------------------------------------------------------------------------
    public List<Object[]> obtenerReportePorProductor(int idProductor) {
        List<Object[]> lista = new ArrayList<>();
        // Hacemos JOINS para traer nombres en lugar de números
        // Y traemos INCIDENCIA y ALERTA para que el productor vea el semáforo
        String sql = "SELECT i.ID_INSPECCION, i.FECHA_INSPECCION, i.OBSERVACIONES, " +
                     "l.ID_LOTE, c.NOMBRES AS CULTIVO, pl.NOMBRE_PLAGA, " +
                     "t.NOMBRE || ' ' || t.APELLIDO AS TECNICO, " +
                     "i.NIVEL_INCIDENCIA, i.NIVEL_ALERTA " +
                     "FROM proyectons.INSPECCION_FITOSANITARIA i " +
                     "JOIN proyectons.LOTE l ON i.ID_LOTE = l.ID_LOTE " +
                     "JOIN proyectons.LUGAR_PRODUCCION lp ON l.ID_LUGAR_PRODUCCION = lp.ID_LUGAR_PRODUCCION " +
                     "JOIN proyectons.CULTIVO c ON l.ID_CULTIVO = c.ID_CULTIVO " +
                     "JOIN proyectons.PLAGA pl ON i.ID_PLAGA = pl.ID_PLAGA " +
                     "JOIN proyectons.TECNICO_INSPECTOR t ON i.ID_TECNICO = t.ID_TECNICO " +
                     "WHERE lp.ID_PRODUCTOR = ? " +
                     "ORDER BY i.FECHA_INSPECCION DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProductor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Guardamos en un Array de Objetos listo para la JTable
                    lista.add(new Object[]{
                        rs.getInt("ID_INSPECCION"),
                        rs.getDate("FECHA_INSPECCION"),
                        "Lote #" + rs.getInt("ID_LOTE"),
                        rs.getString("CULTIVO"),
                        rs.getString("NOMBRE_PLAGA"),
                        rs.getString("TECNICO"),
                        rs.getString("OBSERVACIONES"),
                        rs.getDouble("NIVEL_INCIDENCIA") + "%", // Dato del Trigger
                        rs.getString("NIVEL_ALERTA")            // Dato del Trigger
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listando inspecciones: " + e.getMessage());
        }
        return lista;
    }
    
    public List<Object[]> obtenerResumenICA() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT c.NOMBRES AS CULTIVO, pl.NOMBRE_PLAGA, " +
                     "COUNT(CASE WHEN i.NIVEL_ALERTA = 'ALTA' THEN 1 END) AS ALERTA_ROJA, " +
                     "COUNT(CASE WHEN i.NIVEL_ALERTA = 'MEDIA' THEN 1 END) AS ALERTA_NARANJA, " +
                     "COUNT(CASE WHEN i.NIVEL_ALERTA = 'BAJA' THEN 1 END) AS ALERTA_VERDE, " +
                     "AVG(i.NIVEL_INCIDENCIA) AS INCIDENCIA_PROMEDIO " +
                     "FROM proyectons.INSPECCION_FITOSANITARIA i " +
                     "JOIN proyectons.LOTE l ON i.ID_LOTE = l.ID_LOTE " +
                     "JOIN proyectons.CULTIVO c ON l.ID_CULTIVO = c.ID_CULTIVO " +
                     "JOIN proyectons.PLAGA pl ON i.ID_PLAGA = pl.ID_PLAGA " +
                     "GROUP BY c.NOMBRES, pl.NOMBRE_PLAGA " +
                     "ORDER BY ALERTA_ROJA DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("CULTIVO"),
                    rs.getString("NOMBRE_PLAGA"),
                    rs.getInt("ALERTA_ROJA"),     // Cantidad Alertas Altas
                    rs.getInt("ALERTA_NARANJA"),  // Cantidad Alertas Medias
                    rs.getInt("ALERTA_VERDE"),    // Cantidad Alertas Bajas
                    String.format("%.2f", rs.getDouble("INCIDENCIA_PROMEDIO")) + "%"
                });
            }
        } catch (SQLException e) {
            System.err.println("Error reporte ICA: " + e.getMessage());
        }
        return lista;
    }
    
    public List<Object[]> obtenerInspeccionesDetalladas() {
        List<Object[]> lista = new ArrayList<>();
        
        String sql = "SELECT i.ID_INSPECCION, i.FECHA_INSPECCION, " +
                     "l.ID_LOTE, c.NOMBRES AS CULTIVO, pl.NOMBRE_PLAGA, " +
                     "i.NIVEL_INCIDENCIA, i.NIVEL_ALERTA " +
                     "FROM proyectons.INSPECCION_FITOSANITARIA i " +
                     "JOIN proyectons.LOTE l ON i.ID_LOTE = l.ID_LOTE " +
                     "JOIN proyectons.CULTIVO c ON l.ID_CULTIVO = c.ID_CULTIVO " +
                     "JOIN proyectons.PLAGA pl ON i.ID_PLAGA = pl.ID_PLAGA " +
                     "ORDER BY i.FECHA_INSPECCION DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("ID_INSPECCION"),
                    rs.getDate("FECHA_INSPECCION"),
                    "Lote #" + rs.getInt("ID_LOTE"), // O podrías traer el nombre del lugar también
                    rs.getString("CULTIVO"),
                    rs.getString("NOMBRE_PLAGA"),
                    rs.getDouble("NIVEL_INCIDENCIA") + "%",
                    rs.getString("NIVEL_ALERTA")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error cargando lista detallada: " + e.getMessage());
        }
        return lista;
    }
}