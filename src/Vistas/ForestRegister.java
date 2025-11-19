package Vistas;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import DAO.ConexionDB;
import DAO.SolicitudDAO;

public class ForestRegister extends JFrame {

    // Colores del tema
    private final Color COLOR_VERDE = new Color(40, 110, 45);
    private final Color COLOR_FONDO = new Color(245, 250, 245);

    private JTextField txtNombre, txtApellido, txtDoc, txtTel, txtCorreo;
    private JPasswordField txtPass;
    private JComboBox<String> comboTipo;

    public ForestRegister() {
        setTitle("Registro de Usuario - Forest System");
        setSize(900, 600); // Tamaño ancho como el Login
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // ====================================================================
        // PANEL IZQUIERDO (DECORATIVO)
        // ====================================================================
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Gradiente verde elegante
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 80, 25), 0, getHeight(), new Color(40, 110, 45));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.setLayout(null);
        
        JLabel lblWelcome = new JLabel("<html><div style='text-align: center;'>Únete a<br>Forest System</div></html>");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblWelcome.setForeground(new Color(220, 240, 220));
        lblWelcome.setBounds(25, 150, 300, 100);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(lblWelcome);
        
        JLabel lblDesc = new JLabel("<html><div style='text-align: center;'>Gestiona tus cultivos y<br>protege tu producción.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDesc.setForeground(new Color(180, 220, 180));
        lblDesc.setBounds(25, 260, 300, 60);
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(lblDesc);
        
        add(leftPanel, BorderLayout.WEST);
        
        // ====================================================================
        // PANEL DERECHO (FORMULARIO)
        // ====================================================================
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.WHITE);
        add(rightPanel, BorderLayout.CENTER);
        
        JLabel lblTitle = new JLabel("Crear Nueva Cuenta");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_VERDE);
        lblTitle.setBounds(40, 30, 300, 30);
        rightPanel.add(lblTitle);
        
        JLabel lblSub = new JLabel("Su cuenta será verificada por un administrador.");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setBounds(40, 60, 400, 20);
        rightPanel.add(lblSub);
        
        int y = 100;
        
        rightPanel.add(crearLabel("Tipo de Usuario:", 40, y));
        comboTipo = new JComboBox<>(new String[]{"PRODUCTOR", "TECNICO"});
        estilizarCombo(comboTipo, 40, y+25);
        rightPanel.add(comboTipo);
        
        // Columna Izquierda del Formulario
        rightPanel.add(crearLabel("Documento:", 40, y+70));
        txtDoc = crearInput(40, y+95, 200); rightPanel.add(txtDoc);
        
        rightPanel.add(crearLabel("Nombres:", 40, y+140));
        txtNombre = crearInput(40, y+165, 200); rightPanel.add(txtNombre);
        
        rightPanel.add(crearLabel("Teléfono:", 40, y+210));
        txtTel = crearInput(40, y+235, 200); rightPanel.add(txtTel);
        
        // Columna Derecha del Formulario
        int x2 = 260;
        rightPanel.add(crearLabel("Correo Electrónico:", x2, y+70));
        txtCorreo = crearInput(x2, y+95, 240); rightPanel.add(txtCorreo);
        
        rightPanel.add(crearLabel("Apellidos:", x2, y+140));
        txtApellido = crearInput(x2, y+165, 240); rightPanel.add(txtApellido);
        
        rightPanel.add(crearLabel("Contraseña:", x2, y+210));
        txtPass = new JPasswordField();
        txtPass.setBounds(x2, y+235, 240, 35);
        txtPass.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        rightPanel.add(txtPass);
        
        // --- BOTONES BLINDADOS (CORREGIDOS) ---
        JButton btnReg = new JButton("SOLICITAR REGISTRO");
        btnReg.setBounds(40, 450, 460, 45);
        estilizarBoton(btnReg, COLOR_VERDE);
        btnReg.addActionListener(e -> registrarse());
        rightPanel.add(btnReg);
        
        JButton btnVolver = new JButton("Volver al Login");
        btnVolver.setBounds(40, 510, 460, 30);
        btnVolver.setForeground(COLOR_VERDE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            new ForestLogin().setVisible(true);
            this.dispose();
        });
        rightPanel.add(btnVolver);
    }
    
    // --- LÓGICA ---
    private void registrarse() {
        if(txtDoc.getText().isEmpty() || txtCorreo.getText().isEmpty() || new String(txtPass.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios."); return;
        }
        
        try {
            Connection conn = ConexionDB.getConnection(); 
            SolicitudDAO dao = new SolicitudDAO(conn);
            
            boolean ok = dao.registrarSolicitud(
                comboTipo.getSelectedItem().toString(),
                txtDoc.getText(), txtNombre.getText(), txtApellido.getText(),
                txtTel.getText(), txtCorreo.getText(), new String(txtPass.getPassword())
            );
            
            if(ok) {
                JOptionPane.showMessageDialog(this, "¡Solicitud enviada con éxito!\nEspere aprobación del administrador.");
                new ForestLogin().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al enviar solicitud.");
            }
            conn.close(); 
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // --- HELPERS DE DISEÑO ---
    private JLabel crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, 200, 20);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(80, 80, 80));
        return l;
    }
    
    private JTextField crearInput(int x, int y, int width) {
        JTextField t = new JTextField();
        t.setBounds(x, y, width, 35);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        return t;
    }
    
    private void estilizarCombo(JComboBox box, int x, int y) {
        box.setBounds(x, y, 460, 35);
        box.setBackground(Color.WHITE);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }
    
    private void estilizarBoton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // <--- BLINDAJE CONTRA WINDOWS
        btn.setOpaque(true);         // <--- BLINDAJE CONTRA WINDOWS
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}