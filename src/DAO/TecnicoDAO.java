package DAO;

import Modelos.Tecnico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class TecnicoDAO {
    private Connection conexion;

    public TecnicoDAO(Connection conn_recibida) {
        this.conexion = conn_recibida; 
    }

    // -------------------------------------------------------------------------
    // CREATE - Agregar técnico (Llama al Robot: sp_crear_tecnico)
    // -------------------------------------------------------------------------
    public boolean agregarTecnico(Tecnico tecnico) {
        // Llama al procedimiento almacenado del dueño 'proyectons'
        String sql = "{CALL proyectons.sp_crear_tecnico(?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setString(1, tecnico.getNumDocumento());
            cs.setString(2, tecnico.getNombre());
            cs.setString(3, tecnico.getApellido());
            cs.setString(4, tecnico.getTelefono());
            cs.setString(5, tecnico.getCorreo());
            cs.setString(6, tecnico.getContrasena());
            
            cs.execute();
            return true;
            
        } catch (SQLException e) {
            // Manejo de errores específicos de Oracle
            if (e.getMessage().contains("ORA-00001")) { // Llave duplicada
                 JOptionPane.showMessageDialog(null, "Error: Ya existe un técnico con ese documento o correo.");
            } else if (e.getMessage().contains("ORA-01920")) { // Usuario ya existe
                 JOptionPane.showMessageDialog(null, "Error: El usuario de base de datos ya existe.");
            } else {
                 JOptionPane.showMessageDialog(null, "Error al crear técnico: " + e.getMessage());
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // READ - Obtener todos (SELECT directo a la tabla)
    // -------------------------------------------------------------------------
    public List<Tecnico> obtenerTodosTecnicos() {
        List<Tecnico> tecnicos = new ArrayList<>();
        String sql = "SELECT * FROM proyectons.TECNICO_INSPECTOR ORDER BY ID_TECNICO";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Tecnico tecnico = new Tecnico(
                    rs.getInt("ID_TECNICO"),
                    rs.getString("NUM_DOCUMENTO"),
                    rs.getString("NOMBRE"),
                    rs.getString("APELLIDO"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
                tecnicos.add(tecnico);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar técnicos: " + e.getMessage());
        }
        return tecnicos;
    }

    // -------------------------------------------------------------------------
    // READ - Obtener por ID
    // -------------------------------------------------------------------------
    public Tecnico obtenerTecnicoPorId(int id) {
        String sql = "SELECT * FROM proyectons.TECNICO_INSPECTOR WHERE ID_TECNICO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Tecnico(
                    rs.getInt("ID_TECNICO"),
                    rs.getString("NUM_DOCUMENTO"),
                    rs.getString("NOMBRE"),
                    rs.getString("APELLIDO"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar técnico: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // READ - Obtener por Documento
    // -------------------------------------------------------------------------
    public Tecnico obtenerTecnicoPorDocumento(String documento) {
        String sql = "SELECT * FROM proyectons.TECNICO_INSPECTOR WHERE NUM_DOCUMENTO = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, documento);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Tecnico(
                    rs.getInt("ID_TECNICO"),
                    rs.getString("NUM_DOCUMENTO"),
                    rs.getString("NOMBRE"),
                    rs.getString("APELLIDO"),
                    rs.getString("TELEFONO"),
                    rs.getString("CORREO"),
                    rs.getString("CONTRASENA")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar técnico: " + e.getMessage());
        }
        return null;
    }

    public boolean existeTecnicoPorDocumento(String documento) {
        return obtenerTecnicoPorDocumento(documento) != null;
    }

    // -------------------------------------------------------------------------
    // UPDATE - Actualizar CON contraseña (Llama al Robot: sp_actualizar_tecnico)
    // -------------------------------------------------------------------------
    public boolean actualizarTecnicoConContrasena(Tecnico tecnico) {
        String sql = "{CALL proyectons.sp_actualizar_tecnico(?, ?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, tecnico.getId());
            cs.setString(2, tecnico.getNumDocumento());
            cs.setString(3, tecnico.getNombre());
            cs.setString(4, tecnico.getApellido());
            cs.setString(5, tecnico.getTelefono());
            cs.setString(6, tecnico.getCorreo());
            cs.setString(7, tecnico.getContrasena()); // Se envía la NUEVA contraseña
            
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar técnico: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE - Actualizar SIN contraseña (Llama al Robot con NULL)
    // -------------------------------------------------------------------------
    public boolean actualizarTecnicoSinContrasena(Tecnico tecnico) {
        String sql = "{CALL proyectons.sp_actualizar_tecnico(?, ?, ?, ?, ?, ?, ?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, tecnico.getId());
            cs.setString(2, tecnico.getNumDocumento());
            cs.setString(3, tecnico.getNombre());
            cs.setString(4, tecnico.getApellido());
            cs.setString(5, tecnico.getTelefono());
            cs.setString(6, tecnico.getCorreo());
            cs.setNull(7, java.sql.Types.VARCHAR); // <-- Se envía NULL en la contraseña
            
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar técnico: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // DELETE - Eliminar técnico (Llama al Robot: sp_eliminar_tecnico)
    // -------------------------------------------------------------------------
    public boolean eliminarTecnico(int id) {
        String sql = "{CALL proyectons.sp_eliminar_tecnico(?)}";
        
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-02292")) { // Integridad referencial
                JOptionPane.showMessageDialog(null, "No se puede eliminar el técnico porque tiene inspecciones asociadas.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar técnico: " + e.getMessage());
            }
            return false;
        }
    }
    
    public int obtenerIdPorCorreo(String correo) {
        int id = -1;
        String sql = "SELECT ID_TECNICO FROM proyectons.TECNICO_INSPECTOR WHERE UPPER(CORREO) = UPPER(?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("ID_TECNICO");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscando ID Técnico: " + e.getMessage());
        }
        return id;
    }
}