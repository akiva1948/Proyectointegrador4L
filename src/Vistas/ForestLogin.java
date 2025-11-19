package Vistas;
import DAO.ConexionDB;
import admin.MenuPrincipalAd;
import productor.MenuPrincipalProductor; 
import tecnico.MenuPrincipalTecnico;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class ForestLogin extends JFrame {

    public ForestLogin() {
        setTitle("Login");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // ====== PANEL IZQUIERDO ======
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(240, 245, 240));
        leftPanel.setBounds(0, 0, 500, 600);
        leftPanel.setLayout(null);
        add(leftPanel);

        // Título principal
        JLabel mainTitle = new JLabel("Inicio sesión aquí");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainTitle.setBounds(120, 80, 300, 40);
        mainTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(mainTitle);

        // Subtítulo
        JLabel subtitle = new JLabel("Comienza tu vida ahora");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setBounds(120, 120, 300, 25);
        subtitle.setForeground(new Color(80, 130, 85));
        leftPanel.add(subtitle);

        // Panel para fecha y hora con estilo mejorado
        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setBounds(120, 160, 260, 80);
        dateTimePanel.setBackground(new Color(220, 235, 220));
        dateTimePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        dateTimePanel.setLayout(new BorderLayout());
        leftPanel.add(dateTimePanel);

        // Fecha y hora actual con estilo mejorado
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        JLabel fechaLabel = new JLabel(fecha, JLabel.CENTER);
        fechaLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fechaLabel.setForeground(new Color(40, 110, 45));
        dateTimePanel.add(fechaLabel, BorderLayout.NORTH);

        JLabel horaLabel = new JLabel(hora, JLabel.CENTER);
        horaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        horaLabel.setForeground(new Color(80, 130, 85));
        dateTimePanel.add(horaLabel, BorderLayout.CENTER);

        JLabel fechaText = new JLabel("Fecha y hora actual", JLabel.CENTER);
        fechaText.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        fechaText.setForeground(new Color(120, 150, 120));
        dateTimePanel.add(fechaText, BorderLayout.SOUTH);

        // Actualizar la hora cada segundo
        Timer timer = new Timer(1000, e -> {
            String nuevaHora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            horaLabel.setText(nuevaHora);
        });
        timer.start();

        // Campos de formulario
        JLabel emailLabel = new JLabel("Correo electrónico");
        emailLabel.setBounds(120, 260, 200, 20);
        emailLabel.setForeground(new Color(60, 120, 65));
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        leftPanel.add(emailLabel);

        JTextField emailField = new JTextField();
        emailField.setBounds(120, 285, 260, 40);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        emailField.setBackground(new Color(250, 252, 250));
        leftPanel.add(emailField);

        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setBounds(120, 340, 200, 20);
        passLabel.setForeground(new Color(60, 120, 65));
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        leftPanel.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(120, 365, 260, 40);
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        passField.setBackground(new Color(250, 252, 250));
        leftPanel.add(passField);

        JButton loginBtn = new JButton("Iniciar sesión");
        loginBtn.addActionListener(e -> login(emailField.getText(), new String(passField.getPassword())));
        loginBtn.setBackground(new Color(50, 130, 55));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBounds(120, 470, 120, 40);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        leftPanel.add(loginBtn);

        // Botón de registrarse
        JButton registerBtn = new JButton("Registrarse");
        registerBtn.setBackground(new Color(200, 230, 201));
        registerBtn.setForeground(new Color(50, 130, 55));
        registerBtn.setBounds(260, 470, 120, 40);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerBtn.addActionListener(e -> {
            new ForestRegister().setVisible(true);
            this.dispose(); // Cierra el Login temporalmente
        });
        
        leftPanel.add(registerBtn);
        

        // ====== PANEL DERECHO CON IMAGEN ======
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                
                // Gradiente de fondo
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 80, 25),
                        400, getHeight(), new Color(35, 105, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                try {
                    ImageIcon originalIcon = new ImageIcon(getClass().getResource("/Vistas/img1.jpg"));
                    Image originalImage = originalIcon.getImage();
                    
                    Composite originalComposite = g2.getComposite();
                    
                    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
                    g2.setComposite(alpha);
                    
                    if (originalImage != null) {
                        g2.drawImage(originalImage, 0, 0, getWidth(), getHeight(), this);
                    }
                    
                    g2.setComposite(originalComposite);
                    
                } catch (Exception e) {
                    System.out.println("Error cargando la imagen: " + e.getMessage());                  
                }
                
                g2.setColor(new Color(0, 0, 0, 100)); 
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setBounds(500, 0, 400, 600);
        rightPanel.setLayout(null);
        add(rightPanel);

        // Texto decorativo en el panel derecho (sobre la imagen)
        JLabel rightText1 = new JLabel("CONÉCTATE");
        rightText1.setForeground(new Color(200, 230, 200));
        rightText1.setFont(new Font("Segoe UI", Font.BOLD, 32));
        rightText1.setBounds(80, 200, 250, 40);
        rightPanel.add(rightText1);

        JLabel rightText2 = new JLabel("CON LA");
        rightText2.setForeground(new Color(200, 230, 200));
        rightText2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        rightText2.setBounds(80, 240, 250, 40);
        rightPanel.add(rightText2);

        JLabel rightText3 = new JLabel("NATURALEZA");
        rightText3.setForeground(new Color(200, 230, 200));
        rightText3.setFont(new Font("Segoe UI", Font.BOLD, 32));
        rightText3.setBounds(80, 280, 250, 40);
        rightPanel.add(rightText3);

        // Línea decorativa
        JSeparator separator = new JSeparator();
        separator.setBounds(80, 330, 120, 2);
        separator.setForeground(new Color(150, 200, 150));
        rightPanel.add(separator);

        // Texto descriptivo
        JLabel description = new JLabel("<html><div style='text-align:left;width:250px;'>Explora nuevos caminos y descubre experiencia, alarma con el entorno.</div></html>");
        description.setForeground(new Color(180, 220, 180));
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setBounds(80, 350, 250, 80);
        rightPanel.add(description);

        setVisible(true);
    }
    
private void login(String correo, String contrasena) {    
    
    if (correo.isEmpty() || contrasena.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor ingresa correo y contraseña.");
        return;
    }

    // --- !! 1. PASA A MAYÚSCULAS !! ---
    correo = correo.trim().toUpperCase(); 
    
    // --- !! 2. LA TRANSFORMACIÓN (La clave del éxito) !! ---
    String username_transformado = correo.replace('@', '_').replace('.', '_');
    
    // --- Log de Debug (Para confirmar) ---
    System.out.println("--- INTENTO DE LOGIN (TRANSFORMADO) ---");
    System.out.println("Usuario enviado: [" + username_transformado + "]");
    System.out.println("Password enviado: [" + contrasena + "]");
    
    Connection conn = null;
    try {
        // --- !! 3. USA EL USUARIO TRANSFORMADO !! ---
        conn = ConexionDB.getConnection(username_transformado, contrasena);
        
        System.out.println("¡CONEXIÓN EXITOSA!");
        
        // --- 4. OBTENER ROL (Tu código original) ---
        String rol = obtenerRol(conn); 
        System.out.println("Rol encontrado: " + rol);
        
        if (rol.equals("rol_administrador")) {
            JOptionPane.showMessageDialog(this, "✅ Bienvenido Administrador");
            MenuPrincipalAd adminMenu = new MenuPrincipalAd(conn); 
            adminMenu.setVisible(true);
            this.dispose();

        } else if (rol.equals("rol_productor")) {
        JOptionPane.showMessageDialog(this, "✅ Bienvenido Productor");

        // --- !! ASÍ DEBE QUEDAR LA LLAMADA !! ---
        // Pasamos 'conn' Y 'correo' (el texto del campo de usuario)
        MenuPrincipalProductor prodMenu = new MenuPrincipalProductor(conn, correo); 

        prodMenu.setVisible(true);
        this.dispose();


        } else if (rol.equals("rol_tecnico")) {
            JOptionPane.showMessageDialog(this, "✅ Bienvenido Técnico");

            // --- !! ESTE ES EL ARREGLO !! ---
            MenuPrincipalTecnico tecMenu = new MenuPrincipalTecnico(conn , correo);
            tecMenu.setVisible(true);
            this.dispose();
            
        } else {
            // Esto puede pasar si el GRANT al rol falló
            JOptionPane.showMessageDialog(this, "Usuario válido, pero sin un rol conocido.");
        }

    // --- El "Catch Honesto" ---
    } catch (SQLException e) {
        System.err.println("¡ERROR DE SQL! " + e.getMessage());
        e.printStackTrace(); 
        
        // --- !! ESTE ES EL ERROR QUE TE SALÍA !! ---
        // 'ORA-01045: el usuario ... no tiene privilegio CREATE SESSION'
        if (e.getMessage().contains("ORA-01045")) {
             JOptionPane.showMessageDialog(this, 
                "Error: El usuario existe, pero no tiene permisos para iniciar sesión.", 
                "Error de Permisos", 
                JOptionPane.ERROR_MESSAGE);
        // 'ORA-01017: invalid username/password'
        } else if (e.getMessage().contains("ORA-01017")) {
            JOptionPane.showMessageDialog(this, 
                "Error: Usuario o contraseña incorrectos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        } else {
             JOptionPane.showMessageDialog(this, 
                "ERROR REAL: " + e.getMessage(), 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    } 
}

private String obtenerRol(Connection conn) throws SQLException {
    
    // --- !! ESTE ES EL CAMBIO !! ---
    // ¡BIEN! Esta vista (USER_ROLE_PRIVS) le muestra a CADA 
    // usuario los roles que tiene. ¡No necesita permisos!
    String sql = "SELECT GRANTED_ROLE FROM USER_ROLE_PRIVS"; 
    
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            String rol = rs.getString("GRANTED_ROLE");
            // ¡Importante! Lo pasamos a minúscula para comparar
            rol = rol.toLowerCase(); 
            
            if (rol.equals("rol_administrador")) {
                return "rol_administrador";
            }
            if (rol.equals("rol_productor")) {
                return "rol_productor";
            }
            if (rol.equals("rol_tecnico")) {
                return "rol_tecnico";
            }
        }
    }
    return "DESCONOCIDO"; // Si no es ninguno de los 3
}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(ForestLogin::new);
    }
}
