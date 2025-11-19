package productor; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import javax.swing.border.Border;

public class MenuPrincipalProductor extends JFrame {

    private Connection conn;
    private String correoUsuario; 

    public MenuPrincipalProductor(Connection conn, String correoRecibido) { 
        this.conn = conn; 
        this.correoUsuario = correoRecibido; 
        
        setTitle("Sistema de Gestión Fitosanitaria - Panel del Productor");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Panel de Título
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 248, 240));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Panel del Productor", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title, BorderLayout.CENTER);
        
        // Mostrar quien está conectado
        JLabel userLbl = new JLabel(" Usuario: " + correoUsuario + " ");
        userLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        topPanel.add(userLbl, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);

        // 2. Panel de Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 3, 25, 25)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buttonPanel.setBackground(Color.WHITE);
        
        // --- CREACIÓN DE BOTONES ---
        buttonPanel.add(crearBotonModulo("Mis Lugares", "\uD83C\uDFE1")); 
        buttonPanel.add(crearBotonModulo("Mis Predios", "\uD83C\uDF33")); 
        buttonPanel.add(crearBotonModulo("Mis Lotes", "\uD83C\uDF43")); 
        buttonPanel.add(crearBotonModulo("Consultar Cultivos/Plagas", "\uD83C\uDF3E")); 
        buttonPanel.add(crearBotonModulo("Ver Mis Inspecciones", "\uD83D\uDCCB")); 
        buttonPanel.add(crearBotonModulo("Registrar Producción", "\uD83D\uDCCA")); 
        buttonPanel.add(crearBotonModulo("Solicitar Inspección", "\uD83D\uDCEB")); 
        buttonPanel.add(crearBotonModulo("Mi Perfil", "\uD83D\uDC64")); 
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // 3. Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(45, 55, 50));
        footerPanel.setPreferredSize(new Dimension(0, 50));
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel crearBotonModulo(String texto, String icono) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
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
                panel.setBackground(new Color(248, 252, 248));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(bordeNormal);
                panel.setBackground(Color.WHITE);
            }
        });
        
        return panel;
    }

    // --- LOGICA DE NAVEGACIÓN ---
    private void abrirVentana(String nombreVentana) {
        try {
            
            switch (nombreVentana) {
                
                case "Mis Lugares":
                    this.setVisible(false);
                    new productor.lugarProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;
                    
                case "Mis Predios":
                    this.setVisible(false);
                    new productor.predioProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;

                case "Mis Lotes":
                    this.setVisible(false);
                    new productor.loteProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;
                
                case "Consultar Cultivos/Plagas":
                    this.setVisible(false);
                    new productor.catalogoProductorAd(this, conn).setVisible(true);
                    break;

                case "Ver Mis Inspecciones":
                    this.setVisible(false);
                    new productor.inspeccionProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;

                case "Registrar Producción":
                    this.setVisible(false);
                    // ¡ESTA ES LA VISTA NUEVA QUE CREAMOS CON LA VISTA SQL!
                    new productor.informeProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;

                case "Solicitar Inspección":
                    this.setVisible(false);
                    new productor.solicitudProductorAd(this, conn, correoUsuario).setVisible(true);
                    break;

                case "Mi Perfil":
                     JOptionPane.showMessageDialog(this, "Usuario conectado: " + correoUsuario + "\nRol: Productor");
                     break;

                default:
                    JOptionPane.showMessageDialog(this, "Opción no reconocida.");
            }
        
        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, 
                "Error al abrir módulo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.setVisible(true); // Asegurar que el menú vuelva si falla
            ex.printStackTrace();
        }
    }
}