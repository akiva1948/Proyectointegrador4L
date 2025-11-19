package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.Connection;

public class MenuPrincipalAd extends JFrame {

    private Connection conn;

    public MenuPrincipalAd(Connection conn) {
        this.conn = conn; 
        
        setTitle("Sistema de Gestión Fitosanitaria - Menú Principal");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ====================================================================
        // 1. PANEL SUPERIOR (HEADER)
        // ====================================================================
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 248, 240));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setLayout(new BorderLayout()); 
        // Ajustamos márgenes: 20 a la izquierda, 20 a la derecha
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); 

        // --- A. BOTÓN IZQUIERDO (NUEVO) ---
        JButton leftBtn = new JButton("☰"); // Icono de Menú
        leftBtn.setToolTipText("Opciones / Configuración");
        
        // Estilo del botón izquierdo (Simétrico al derecho)
        leftBtn.setBackground(new Color(50, 130, 55));
        leftBtn.setForeground(Color.WHITE);
        leftBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        leftBtn.setPreferredSize(new Dimension(45, 45)); 
        leftBtn.setFocusPainted(false);
        leftBtn.setBorderPainted(false);
        leftBtn.setOpaque(true);
        leftBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        leftBtn.addActionListener(e -> {
            // Aquí puedes poner lo que quieras, por ejemplo, Información del Admin
            JOptionPane.showMessageDialog(this, "Panel de Administración V1.0\nConectado a Oracle DB.");
        });

        // Contenedor para alinear a la izquierda
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 27));
        leftContainer.setBackground(new Color(240, 248, 240));
        leftContainer.add(leftBtn);
        
        topPanel.add(leftContainer, BorderLayout.WEST); // <--- AQUÍ ESTÁ LA CLAVE

        // --- B. TÍTULO CENTRAL ---
        JLabel title = new JLabel("Panel de Administración");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(30, 100, 35));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(title, BorderLayout.CENTER);

        // --- C. BOTÓN DERECHO (+) ---
        JButton addAdminBtn = new JButton("+");
        addAdminBtn.setToolTipText("Crear nuevo Administrador");

        // Estilo del botón derecho
        addAdminBtn.setBackground(new Color(50, 130, 55));
        addAdminBtn.setForeground(Color.WHITE);
        addAdminBtn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        addAdminBtn.setPreferredSize(new Dimension(45, 45)); 
        addAdminBtn.setFocusPainted(false);
        addAdminBtn.setBorderPainted(false);
        addAdminBtn.setOpaque(true);
        addAdminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addAdminBtn.addActionListener(e -> {
            // new VistaCrearAdmin(this).setVisible(true); 
            JOptionPane.showMessageDialog(this, "Función: Crear Nuevo Admin");
        });

        // Contenedor para alinear a la derecha
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 27));
        rightContainer.setBackground(new Color(240, 248, 240));
        rightContainer.add(addAdminBtn);
        
        topPanel.add(rightContainer, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ====================================================================
        // 2. PANEL CENTRAL (GRID DE MODULOS)
        // ====================================================================
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 3, 25, 25)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        buttonPanel.setBackground(Color.WHITE);
        
        // --- AGREGAR BOTONES ---
        buttonPanel.add(crearBotonModulo("Gestionar Técnicos", "\uD83D\uDC68\u200D\uD83D\uDD2C"));
        buttonPanel.add(crearBotonModulo("Gestionar Productores", "\uD83D\uDC68\u200D\uD83C\uDF3E"));
        buttonPanel.add(crearBotonModulo("Autorizar Usuarios", "\uD83D\uDD11")); // Para aprobar registros

        buttonPanel.add(crearBotonModulo("Gestionar Municipios", "\uD83C\uDFD9\uFE0F"));
        buttonPanel.add(crearBotonModulo("Gestionar Lugares", "\uD83C\uDFE1"));
        buttonPanel.add(crearBotonModulo("Gestionar Predios", "\uD83C\uDF33"));
        
        buttonPanel.add(crearBotonModulo("Gestionar Lotes", "\uD83C\uDF43"));
        buttonPanel.add(crearBotonModulo("Gestionar Cultivos", "\uD83C\uDF3E"));
        buttonPanel.add(crearBotonModulo("Gestionar Plagas", "\uD83D\uDC1B"));
        
        buttonPanel.add(crearBotonModulo("Gestionar Inspecciones", "\uD83D\uDCCB"));
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // ====================================================================
        // 3. PANEL INFERIOR (FOOTER)
        // ====================================================================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(new Color(45, 55, 50));
        footerPanel.setPreferredSize(new Dimension(0, 60));
        
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setBackground(new Color(180, 60, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setOpaque(true);
        btnSalir.addActionListener(e -> System.exit(0));
        
        footerPanel.add(btnSalir);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------------
    // HELPER PARA BOTONES DEL GRID
    // ------------------------------------------------------------------------
    private JPanel crearBotonModulo(String texto, String icono) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(250, 250, 250)); 
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        Border bordeNormal = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        Border bordeHover = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 140, 65), 2), 
            BorderFactory.createEmptyBorder(19, 19, 19, 19)
        );
        panel.setBorder(bordeNormal);

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcono.setForeground(new Color(60, 140, 65));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTexto.setForeground(new Color(50, 50, 50));
        lblTexto.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblIcono, BorderLayout.CENTER);
        panel.add(lblTexto, BorderLayout.SOUTH);
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirVentana(texto);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(bordeHover);
                panel.setBackground(new Color(235, 250, 235));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(bordeNormal);
                panel.setBackground(new Color(250, 250, 250));
            }
        });
        
        return panel;
    }

    // ------------------------------------------------------------------------
    // NAVEGACIÓN
    // ------------------------------------------------------------------------
    private void abrirVentana(String nombreVentana) {
        try {
            this.setVisible(false);
            
            switch (nombreVentana) {
                case "Gestionar Técnicos":
                    new tecnicoAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Productores":
                    new productorAd(this, conn).setVisible(true);
                    break;
                case "Autorizar Usuarios":
                    new gestionSolicitudesAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Municipios":
                    new municipioAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Lugares":
                    new lugarProduccionAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Predios":
                    new predioAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Lotes":
                    new loteAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Cultivos":
                    new cultivoAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Plagas":
                    new plagaAd(this, conn).setVisible(true);
                    break;
                case "Gestionar Inspecciones":
                    new inspeccionAd(this, conn).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Módulo no implementado.");
                    this.setVisible(true);
            }
        
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, 
                "Error al conectar con la Base de Datos: " + ex.getMessage(), 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
            this.setVisible(true);
        
        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            this.setVisible(true);
            ex.printStackTrace();
        }
    }
}