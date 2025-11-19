/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tecnico;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;

public class MenuPrincipalTecnico extends JFrame {

    private Connection conn;
    private String correoTecnico;
    
    // COLORES
    private final Color COLOR_FONDO_TARJETA = new Color(235, 250, 235); // Verde muy clarito
    private final Color COLOR_VERDE_OSCURO = new Color(40, 110, 45);
    private final Color COLOR_HOVER = new Color(60, 140, 65);

    public MenuPrincipalTecnico(Connection conn, String correoRecibido) { 
        this.conn = conn; 
        this.correoTecnico = correoRecibido;
        
        setTitle("Sistema de Gestión Fitosanitaria - Panel del Técnico");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Panel de Título
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 248, 240));
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setLayout(new GridBagLayout());
        
        JLabel title = new JLabel("Panel del Técnico Inspector");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(COLOR_VERDE_OSCURO);
        topPanel.add(title);
        add(topPanel, BorderLayout.NORTH);

        // 2. Panel de "Tarjetas"
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 3, 25, 25)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        buttonPanel.setBackground(Color.WHITE);
        
        // --- AGREGAR BOTONES ---
        buttonPanel.add(crearBotonModulo("Gestionar Inspecciones", "\uD83D\uDCCB")); 
        buttonPanel.add(crearBotonModulo("Generar Informes ICA", "\uD83D\uDCCA"));   
        buttonPanel.add(crearBotonModulo("Mi Perfil", "\uD83D\uDC64"));              
        
        buttonPanel.add(crearBotonModulo("Consultar Lotes", "\uD83C\uDF43"));        
        buttonPanel.add(crearBotonModulo("Consultar Cultivos", "\uD83C\uDF3E"));     
        buttonPanel.add(crearBotonModulo("Consultar Plagas", "\uD83D\uDC1B"));       
        
        buttonPanel.add(crearBotonModulo("Consultar Predios", "\uD83C\uDF33"));      
        buttonPanel.add(crearBotonModulo("Consultar Lugares", "\uD83C\uDFE1"));      
        buttonPanel.add(crearBotonModulo("Consultar Municipios", "\uD83C\uDFD9\uFE0F")); 
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // 3. Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(new Color(245, 248, 245));
        footerPanel.setPreferredSize(new Dimension(0, 60));
        
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setBackground(new Color(180, 60, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false); // Quita borde blanco de Windows
        btnSalir.setOpaque(true);         // Fuerza color
        btnSalir.addActionListener(e -> System.exit(0));
        footerPanel.add(btnSalir);
        
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel crearBotonModulo(String texto, String icono) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        
        // COLOR DE FONDO INICIAL (Ya no es blanco, es verde claro)
        panel.setBackground(COLOR_FONDO_TARJETA);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // BORDE SIEMPRE VISIBLE
        Border bordeNormal = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180), 1), // Borde verde sutil
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
        panel.setBorder(bordeNormal);

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40)); 
        lblIcono.setForeground(COLOR_VERDE_OSCURO);
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblTexto = new JLabel("<html><center>" + texto + "</center></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTexto.setForeground(COLOR_VERDE_OSCURO);
        lblTexto.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblIcono, BorderLayout.CENTER);
        panel.add(lblTexto, BorderLayout.SOUTH);
        
        // Efecto Hover (Cambio drástico de color para que se note)
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirVentana(texto);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                // AL PASAR EL MOUSE: Fondo Oscuro, Letra Blanca
                panel.setBackground(COLOR_HOVER);
                lblIcono.setForeground(Color.WHITE);
                lblTexto.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // AL SALIR: Vuelve al estado original
                panel.setBackground(COLOR_FONDO_TARJETA);
                lblIcono.setForeground(COLOR_VERDE_OSCURO);
                lblTexto.setForeground(COLOR_VERDE_OSCURO);
            }
        });
        
        return panel;
    }

    private void abrirVentana(String nombreVentana) {
        try {
            if (!nombreVentana.equals("Mi Perfil")) {
                this.setVisible(false);
            }
            
            switch (nombreVentana) {
                case "Gestionar Inspecciones":
                    new tecnico.inspeccionTecnicoAd(this, conn, correoTecnico).setVisible(true);
                    break;
                case "Generar Informes ICA":
                    new tecnico.informesTecnicoAd(this, conn).setVisible(true);
                    break;
                
                // Consultas
                case "Consultar Lotes":
                    new tecnico.loteTecnicoAd(this, conn).setVisible(true);
                    break;
                case "Consultar Cultivos":
                    new tecnico.cultivoTecnicoAd(this, conn).setVisible(true);
                    break;
                case "Consultar Plagas":
                    new tecnico.plagaTecnicoAd(this, conn).setVisible(true);
                    break;
                case "Consultar Predios":
                    new tecnico.predioTecnicoAd(this, conn).setVisible(true);
                    break;
                case "Consultar Lugares":
                    new tecnico.lugarTecnicoAd(this, conn).setVisible(true);
                    break;
                case "Consultar Municipios":
                    new tecnico.municipioTecnicoAd(this, conn).setVisible(true);
                    break;

                case "Mi Perfil":
                    JOptionPane.showMessageDialog(this, 
                        "Usuario Técnico Conectado:\n" + correoTecnico, 
                        "Información de Sesión", 
                        JOptionPane.INFORMATION_MESSAGE);
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Módulo no implementado.");
                    this.setVisible(true);
            }
        
        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.setVisible(true);
            ex.printStackTrace();
        }
    }
}