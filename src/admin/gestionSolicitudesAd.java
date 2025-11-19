package admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.List;
import DAO.SolicitudDAO;

public class gestionSolicitudesAd extends JFrame {
    
    private Connection conn;
    private JFrame parentMenu;
    private SolicitudDAO solicitudDAO;
    private JTable table;
    private DefaultTableModel model;
    
    public gestionSolicitudesAd(JFrame parentMenu, Connection conn) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.solicitudDAO = new SolicitudDAO(conn);
        
        initUI();
        cargarTabla();
        
        // Esto asegura que al cerrar con la X, se muestre el menú principal
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { 
                parentMenu.setVisible(true); 
            }
        });
    }
    
    private void initUI() {
        setTitle("Gestión de Accesos - Aprobación de Usuarios");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Importante: DISPOSE, no EXIT
        
        // --- HEADER ---
        JPanel header = new JPanel();
        header.setBackground(new Color(40, 110, 45));
        header.setPreferredSize(new Dimension(0, 70));
        header.setLayout(new GridBagLayout()); // Para centrar bien
        
        JLabel title = new JLabel("Solicitudes de Registro Pendientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);
        
        // --- TABLA ---
        String[] cols = {"ID", "Rol Solicitado", "Documento", "Nombre", "Correo"};
        model = new DefaultTableModel(cols, 0) {
             public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 245, 230));
        table.setSelectionBackground(new Color(200, 230, 200));
        table.setSelectionForeground(Color.BLACK);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);
        
        // --- FOOTER (BOTONES) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        
        // Botón APROBAR
        JButton btnAprobar = crearBoton("APROBAR USUARIO", new Color(60, 160, 70));
        btnAprobar.addActionListener(e -> gestionar(true));
        
        // Botón RECHAZAR
        JButton btnRechazar = crearBoton("RECHAZAR", new Color(180, 60, 60));
        btnRechazar.addActionListener(e -> gestionar(false));
        
        // Botón VOLVER (¡Nuevo!)
        JButton btnVolver = crearBoton("VOLVER AL MENÚ", Color.GRAY);
        btnVolver.addActionListener(e -> this.dispose()); // Cierra esta ventana y dispara windowClosed
        
        footer.add(btnAprobar);
        footer.add(btnRechazar);
        footer.add(Box.createHorizontalStrut(40)); // Separador
        footer.add(btnVolver);
        
        add(footer, BorderLayout.SOUTH);
    }
    
    // Helper para crear botones idénticos y "blindados"
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); 
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void cargarTabla() {
        model.setRowCount(0);
        List<Object[]> lista = solicitudDAO.obtenerPendientes();
        
        if (lista.isEmpty()) {
            // Opcional: Mostrar fila vacía o mensaje
        }
        
        for(Object[] row : lista) model.addRow(row);
    }
    
    private void gestionar(boolean aprobar) {
        int row = table.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud de la tabla."); return;
        }
        
        int id = (int) table.getValueAt(row, 0);
        String rol = (String) table.getValueAt(row, 1);
        String nombre = (String) table.getValueAt(row, 3);
        
        if(aprobar) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Seguro que desea crear el usuario para:\n" + nombre + " (" + rol + ")?",
                "Confirmar Aprobación", JOptionPane.YES_NO_OPTION);
                
            if(confirm == JOptionPane.YES_OPTION) {
                if(solicitudDAO.aprobarSolicitud(id)) {
                    JOptionPane.showMessageDialog(this, "¡Usuario creado exitosamente!\nYa puede iniciar sesión.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error creando usuario.\nVerifique logs o permisos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            if(JOptionPane.showConfirmDialog(this, "¿Rechazar esta solicitud?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                solicitudDAO.rechazarSolicitud(id);
                JOptionPane.showMessageDialog(this, "Solicitud rechazada.");
            }
        }
        cargarTabla();
    }
}