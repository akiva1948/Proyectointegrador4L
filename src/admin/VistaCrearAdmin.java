package admin;

import DAO.ConexionDB; // ¡Asegúrate de que esta sea tu clase de conexión!
import javax.swing.*;
import java.awt.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

// --- !! 1. USA JDIALOG !! ---
// Es mejor que un JFrame porque "flota" encima del Login
public class VistaCrearAdmin extends JDialog {

    private JTextField emailField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;

    // Recibe el JFrame del Login como "padre"
    public VistaCrearAdmin(JFrame parent) {
        super(parent, "Crear Nuevo Administrador", true); // 'true' lo hace "modal"
        
        setSize(400, 450);
        setLayout(null);
        setLocationRelativeTo(parent); // Se centra sobre el Login
        getContentPane().setBackground(new Color(240, 245, 240)); // Mismo fondo

        // --- !! 2. ESTILOS COPIADOS DE TU LOGIN !! ---
        
        // Título
        JLabel mainTitle = new JLabel("Nuevo Administrador");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitle.setBounds(70, 40, 300, 30);
        mainTitle.setForeground(new Color(40, 110, 45));
        add(mainTitle);

        // Correo
        JLabel emailLabel = new JLabel("Correo electrónico (será el usuario)");
        emailLabel.setBounds(70, 90, 260, 20);
        emailLabel.setForeground(new Color(60, 120, 65));
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(70, 115, 260, 40);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        emailField.setBackground(new Color(250, 252, 250));
        add(emailField);

        // Contraseña
        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setBounds(70, 170, 200, 20);
        passLabel.setForeground(new Color(60, 120, 65));
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(70, 195, 260, 40);
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        passField.setBackground(new Color(250, 252, 250));
        add(passField);

        // Confirmar Contraseña
        JLabel confirmPassLabel = new JLabel("Confirmar Contraseña");
        confirmPassLabel.setBounds(70, 250, 200, 20);
        confirmPassLabel.setForeground(new Color(60, 120, 65));
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(confirmPassLabel);

        confirmPassField = new JPasswordField();
        confirmPassField.setBounds(70, 275, 260, 40);
        confirmPassField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 180)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        confirmPassField.setBackground(new Color(250, 252, 250));
        add(confirmPassField);

        // --- !! 3. LOS BOTONES CON LA LÓGICA !! ---

        JButton crearBtn = new JButton("Crear");
        crearBtn.setBackground(new Color(50, 130, 55));
        crearBtn.setForeground(Color.WHITE);
        crearBtn.setBounds(70, 340, 120, 40);
        crearBtn.setFocusPainted(false);
        crearBtn.setBorderPainted(false);
        crearBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        crearBtn.addActionListener(e -> crearNuevoAdmin()); // Llama al método
        add(crearBtn);

        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.setBackground(new Color(200, 230, 201));
        cancelarBtn.setForeground(new Color(50, 130, 55));
        cancelarBtn.setBounds(210, 340, 120, 40);
        cancelarBtn.setFocusPainted(false);
        cancelarBtn.setBorderPainted(false);
        cancelarBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelarBtn.addActionListener(e -> dispose()); // Cierra esta ventana
        add(cancelarBtn);
    }

    // --- !! 4. EL MÉTODO QUE HACE LA MAGIA !! ---
    private void crearNuevoAdmin() {
        String correo = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        // Validaciones
        if (correo.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Correo y contraseña no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un correo válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            // --- !! 5. SE CONECTA COMO EL "DUEÑO" (EL ÚNICO CON PODERES) !! ---
            conn = ConexionDB.getConnection("proyectons", "proyectons"); 
            
            // Llama al "robot" que vamos a crear
            String sql = "{CALL proyectons.sp_crear_admin(?, ?)}";
            
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setString(1, correo); // p_correo
                cs.setString(2, pass);   // p_contrasena
                cs.execute();
                
                JOptionPane.showMessageDialog(this, "¡Administrador creado con éxito!");
                dispose(); // Cierra la ventana de creación
            }

        } catch (SQLException e) {
            // Muestra el error real de la base de datos (ej: ORA-01031)
            JOptionPane.showMessageDialog(this, "Error al crear admin: " + e.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) {}
            }
        }
    }
}