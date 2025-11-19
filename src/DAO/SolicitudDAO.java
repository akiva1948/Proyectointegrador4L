package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class SolicitudDAO {
    private Connection conn;

    public SolicitudDAO(Connection conn) {
        this.conn = conn;
    }

    // 1. REGISTRAR SOLICITUD (Usado por la pantalla de Registro)
    public boolean registrarSolicitud(String tipo, String doc, String nom, String ape, String tel, String mail, String pass) {
        String sql = "INSERT INTO proyectons.SOLICITUD_REGISTRO (ID_SOLICITUD, TIPO_USUARIO, DOCUMENTO, NOMBRES, APELLIDOS, TELEFONO, CORREO, CONTRASENA, ESTADO) " +
                     "VALUES (proyectons.SEQ_SOLICITUD.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, doc);
            ps.setString(3, nom);
            ps.setString(4, ape);
            ps.setString(5, tel);
            ps.setString(6, mail);
            ps.setString(7, pass);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registro: " + e.getMessage());
            return false;
        }
    }

    // 2. LISTAR PENDIENTES (Usado por el Admin)
    public List<Object[]> obtenerPendientes() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT * FROM proyectons.SOLICITUD_REGISTRO WHERE ESTADO = 'PENDIENTE' ORDER BY ID_SOLICITUD DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("ID_SOLICITUD"),
                    rs.getString("TIPO_USUARIO"),
                    rs.getString("DOCUMENTO"),
                    rs.getString("NOMBRES") + " " + (rs.getString("APELLIDOS") != null ? rs.getString("APELLIDOS") : ""),
                    rs.getString("CORREO")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // 3. APROBAR SOLICITUD (La Magia: Llama a los SP que ya tienes)
    public boolean aprobarSolicitud(int idSolicitud) {
        // Primero recuperamos los datos de la solicitud
        String sqlGet = "SELECT * FROM proyectons.SOLICITUD_REGISTRO WHERE ID_SOLICITUD = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String tipo = rs.getString("TIPO_USUARIO");
                String doc = rs.getString("DOCUMENTO");
                String nom = rs.getString("NOMBRES");
                String ape = rs.getString("APELLIDOS");
                String tel = rs.getString("TELEFONO");
                String mail = rs.getString("CORREO");
                String pass = rs.getString("CONTRASENA");
                
                // LLAMAMOS A LOS ROBOTS DE ORACLE QUE YA CREASTE
                if (tipo.equals("PRODUCTOR")) {
                    // Tu SP de productor pide: NombreCompleto, Cedula, Telefono, Correo, Pass
                    String nombreCompleto = nom + (ape != null ? " " + ape : "");
                    ProductorDAO pDao = new ProductorDAO(conn);
                    // Creamos un objeto dummy para pasar los datos
                    Modelos.Productor p = new Modelos.Productor(0, nombreCompleto, doc, tel, mail, pass);
                    if (!pDao.agregarProductor(p)) return false; // Si falla el SP, cancelamos
                    
                } else if (tipo.equals("TECNICO")) {
                    // Tu SP de tecnico pide: Doc, Nom, Ape, Tel, Correo, Pass
                    TecnicoDAO tDao = new TecnicoDAO(conn);
                    Modelos.Tecnico t = new Modelos.Tecnico(0, doc, nom, ape, tel, mail, pass);
                    if (!tDao.agregarTecnico(t)) return false;
                }
                
                // Si todo salió bien, marcamos como APROBADO
                String sqlUpdate = "UPDATE proyectons.SOLICITUD_REGISTRO SET ESTADO = 'APROBADO' WHERE ID_SOLICITUD = ?";
                try(PreparedStatement psUp = conn.prepareStatement(sqlUpdate)) {
                    psUp.setInt(1, idSolicitud);
                    psUp.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al aprobar: " + e.getMessage());
        }
        return false;
    }

    // 4. RECHAZAR
    public boolean rechazarSolicitud(int id) {
        String sql = "UPDATE proyectons.SOLICITUD_REGISTRO SET ESTADO = 'RECHAZADO' WHERE ID_SOLICITUD = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}