package admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import DAO.ProductorDAO;
import Modelos.Productor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.sql.Connection;

public class productorAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField cedulaField, nombreField, telefonoField, correoField;
    private JPasswordField contrasenaField;
    private JCheckBox showPassCheckbox;
    private JLabel dateTimeLabel, totalLabel; 
    private ProductorDAO productorDAO;
    private int idActual; 
    private char defaultEchoChar;
    private JFrame parentMenu;
    private Connection conn;
    
    public productorAd(JFrame parentMenu_recibido, Connection conn_recibida) throws SQLException { 
        this.parentMenu = parentMenu_recibido;
        this.conn = conn_recibida; 
        
        this.productorDAO = new ProductorDAO(this.conn);
        
        idActual = 0;
        initializeUI();
        cargarProductores();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentMenu != null) {
                    parentMenu.setVisible(true);
                }
            }
        });
    }

    private void initializeUI() {
        setTitle("Gestión de Productores");
        setSize(1366, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 248, 245));

        crearPanelSuperior();
        crearPanelFormulario();
        crearPanelTabla();
        setVisible(true);
    }

    private void crearPanelSuperior() {
        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 240),
                        0, getHeight(), new Color(230, 242, 230));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setBounds(0, 0, 1366, 100);
        topPanel.setLayout(null);
        add(topPanel);

        JLabel title = new JLabel("Productores");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 400, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);

        JLabel subtitle = new JLabel("Gestión de productores agrícolas");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setBounds(45, 60, 300, 25);
        subtitle.setForeground(new Color(70, 120, 75));
        topPanel.add(subtitle);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateTimeLabel.setBounds(1120, 25, 220, 50);
        dateTimeLabel.setForeground(new Color(50, 110, 55));
        dateTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        updateDateTime();
        topPanel.add(dateTimeLabel);

        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
    }

    private void crearPanelFormulario() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(250, 252, 250));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setBounds(20, 120, 340, 640);
        leftPanel.setLayout(null);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        add(leftPanel);

        JLabel formTitle = new JLabel("Gestión de Productor");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        int yPos = 60; 
        cedulaField = crearCampoFormulario(leftPanel, "Cédula de Ciudadanía*", yPos);
        yPos += 70;
        nombreField = crearCampoFormulario(leftPanel, "Nombre Completo*", yPos);
        yPos += 70;
        telefonoField = crearCampoFormulario(leftPanel, "Teléfono*", yPos);
        yPos += 70;
        correoField = crearCampoFormulario(leftPanel, "Correo*", yPos);
        yPos += 70;
        
        contrasenaField = crearCampoContrasena(leftPanel, "Contraseña*", yPos);
        defaultEchoChar = contrasenaField.getEchoChar();
        
        showPassCheckbox = new JCheckBox("Mostrar contraseña");
        showPassCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassCheckbox.setForeground(new Color(80, 80, 80));
        showPassCheckbox.setOpaque(false);
        showPassCheckbox.setFocusPainted(false);
        showPassCheckbox.setBounds(0, yPos + 65, 200, 25);
        
        showPassCheckbox.addActionListener(e -> {
            if (showPassCheckbox.isSelected()) {
                contrasenaField.setEchoChar((char) 0);
            } else {
                contrasenaField.setEchoChar(defaultEchoChar);
            }
        });
        leftPanel.add(showPassCheckbox);
        
        yPos += 100; 
        
        JButton addBtn = crearBotonEstilizado("Agregar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos, 140, 40); 
        
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos, 140, 40);
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos + 50, 140, 40);
        
        JButton exitBtn = crearBotonEstilizado("Cerrar", new Color(210, 80, 70));
        exitBtn.setBounds(160, yPos + 50, 140, 40); 

        addBtn.addActionListener(e -> agregarProductor());
        updateBtn.addActionListener(e -> actualizarProductor());
        clearBtn.addActionListener(e -> limpiarFormulario());
        exitBtn.addActionListener(e -> this.dispose());

        leftPanel.add(addBtn);
        leftPanel.add(updateBtn);
        leftPanel.add(clearBtn);
        leftPanel.add(exitBtn);
    }

    private void crearPanelTabla() {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBounds(380, 120, 966, 640);
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        add(rightPanel);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(new Color(245, 248, 245));
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel tableTitle = new JLabel("Lista de Productores");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(40, 110, 45));
        tableHeader.add(tableTitle, BorderLayout.WEST);

        // --- !! BOTÓN REFRESCAR !! ---
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeaderPanel.setBackground(new Color(245, 248, 245));

        JButton refreshBtn = new JButton("\u27F3"); 
        refreshBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24)); 
        refreshBtn.setForeground(new Color(60, 140, 65)); 
        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false); 
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setToolTipText("Actualizar tabla"); 

        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshBtn.setForeground(new Color(40, 100, 45)); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshBtn.setForeground(new Color(60, 140, 65)); 
            }
        });

        // Acción: Recargar datos de PRODUCTORES
        refreshBtn.addActionListener(e -> cargarProductores());

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);

        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);
        // --- FIN BOTÓN REFRESCAR ---

        rightPanel.add(tableHeader, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Cédula", "Nombre", "Teléfono", "Correo", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; 
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) return JPanel.class;
                return Object.class;
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersTable.setRowHeight(45);
        membersTable.setSelectionBackground(new Color(230, 245, 230));
        membersTable.setSelectionForeground(Color.BLACK);
        membersTable.setGridColor(new Color(240, 240, 240));

        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(100); 
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(150); 
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(80);  
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(150); 
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(130); 
        membersTable.getColumnModel().getColumn(5).setMaxWidth(150);

        membersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        membersTable.getTableHeader().setBackground(new Color(240, 248, 240));
        membersTable.getTableHeader().setForeground(new Color(40, 110, 45));
        membersTable.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));

        membersTable.getColumnModel().getColumn(5).setCellRenderer(new AccionesRenderer());
        membersTable.getColumnModel().getColumn(5).setCellEditor(new AccionesEditor());

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // --- RENDERER ---
    private class AccionesRenderer implements javax.swing.table.TableCellRenderer {
         @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            
            if (!(value instanceof Component)) {
                return new JPanel();
            }
            
            Component c = (Component) value;
            if (isSelected) {
                c.setBackground(new Color(230, 245, 230));
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }

    // --- EDITOR ---
    private class AccionesEditor extends DefaultCellEditor {
        
        private Object currentValue; 

        public AccionesEditor() {
            super(new JCheckBox()); 
            setClickCountToStart(1); 
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            
            this.currentValue = value; 
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(isSelected ? new Color(230, 245, 230) : Color.WHITE);
            
            JButton editarBtn = new JButton("Editar");
            editarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editarBtn.setBackground(new Color(74, 144, 226));
            editarBtn.setForeground(Color.WHITE);
            editarBtn.setBorderPainted(false);
            editarBtn.setFocusPainted(false);
            editarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton eliminarBtn = new JButton("X");
            eliminarBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            eliminarBtn.setBackground(new Color(231, 76, 60));
            eliminarBtn.setForeground(Color.WHITE);
            eliminarBtn.setBorderPainted(false);
            eliminarBtn.setFocusPainted(false);
            eliminarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eliminarBtn.setMargin(new Insets(2, 8, 2, 8));

            // Acción EDITAR
            editarBtn.addActionListener(e -> {
                fireEditingStopped(); 
                String cedula = (String) tableModel.getValueAt(row, 1); 
                Productor p = productorDAO.obtenerProductorPorDocumento(cedula);
                if (p != null) {
                    editarProductorDesdeBoton(p.getIdProductor());
                }
            });

            // Acción ELIMINAR
            eliminarBtn.addActionListener(e -> {
                fireEditingStopped(); 
                String cedula = (String) tableModel.getValueAt(row, 1);
                Productor p = productorDAO.obtenerProductorPorDocumento(cedula);
                if (p != null) {
                    eliminarProductorDesdeBoton(p.getIdProductor());
                }
            });

            panel.add(editarBtn);
            panel.add(eliminarBtn);
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return this.currentValue;
        }
    }

    // --- Métodos de ayuda ---
    
    private JTextField crearCampoFormulario(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBounds(0, y, 280, 20);
        label.setForeground(new Color(60, 120, 65));
        panel.add(label);
        JTextField field = new JTextField();
        field.setBounds(0, y + 25, 300, 40);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 180)),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field);
        return field;
    }
    
    private JPasswordField crearCampoContrasena(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBounds(0, y, 280, 20);
        label.setForeground(new Color(60, 120, 65));
        panel.add(label);

        JPasswordField field = new JPasswordField();
        field.setBounds(0, y + 25, 300, 40);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 180)),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field);
        return field;
    }

    private JButton crearBotonEstilizado(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void updateDateTime() {
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date()) + " h";
        dateTimeLabel.setText("<html><div style='text-align: right;'>" + fecha + "<br>" + hora + "</div></html>");
    }

    // --- MÉTODOS CRUD ---

    private void cargarProductores() {
        tableModel.setRowCount(0);
        List<Productor> productores = productorDAO.obtenerTodosProductores();

        for (Productor productor : productores) {
            JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelAcciones.setBackground(Color.WHITE);
            
            JButton editarBtn = new JButton("Editar");
            editarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editarBtn.setBackground(new Color(74, 144, 226));
            editarBtn.setForeground(Color.WHITE);
            editarBtn.setBorderPainted(false);
            editarBtn.setFocusPainted(false);
            editarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton eliminarBtn = new JButton("X");
            eliminarBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            eliminarBtn.setBackground(new Color(231, 76, 60));
            eliminarBtn.setForeground(Color.WHITE);
            eliminarBtn.setBorderPainted(false);
            eliminarBtn.setFocusPainted(false);
            eliminarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eliminarBtn.setMargin(new Insets(2, 8, 2, 8));
            
            panelAcciones.add(editarBtn);
            panelAcciones.add(eliminarBtn);

            tableModel.addRow(new Object[]{
                productor.getIdProductor(), 
                productor.getCedulaCiudadania(), 
                productor.getNombreProductor(), 
                productor.getTelefono(), 
                productor.getCorreo(), 
                panelAcciones 
            });
        }
        totalLabel.setText("Total: " + productores.size());
    }

    private void editarProductorDesdeBoton(int id) {
        Productor productor = productorDAO.obtenerProductorPorId(id);
        if (productor != null) {
            cargarProductorEnFormulario(productor);
        }
    }

    private void eliminarProductorDesdeBoton(int id) {
        Productor productor = productorDAO.obtenerProductorPorId(id);
        if (productor != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar al productor: " + productor.getNombreProductor() + "?",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (productorDAO.eliminarProductor(id)) {
                    JOptionPane.showMessageDialog(this, "Productor eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    SwingUtilities.invokeLater(() -> {
                        cargarProductores();
                        limpiarFormulario();
                    });
                }
            }
        }
    }

    private void cargarProductorEnFormulario(Productor productor) {
        idActual = productor.getIdProductor(); 
        cedulaField.setText(productor.getCedulaCiudadania());
        nombreField.setText(productor.getNombreProductor());
        telefonoField.setText(productor.getTelefono());
        correoField.setText(productor.getCorreo());
        
        contrasenaField.setText("");
        
        showPassCheckbox.setSelected(false);
        contrasenaField.setEchoChar(defaultEchoChar);
    }

    private void agregarProductor() {
        Productor productor = obtenerProductorDesdeFormulario(false);
        if (productor == null) return;
        
        if (!productor.getContrasena().matches("^[a-zA-Z0-9_#$]+$")) {
             JOptionPane.showMessageDialog(this, 
                "La contraseña contiene caracteres inválidos.\n" +
                "Solo se permiten letras (sin ñ/tildes), números y los símbolos _ # $", 
                "Error de Contraseña", 
                JOptionPane.ERROR_MESSAGE);
            return; 
        }
        
        if (productorDAO.existeProductorPorDocumento(productor.getCedulaCiudadania())) {
            JOptionPane.showMessageDialog(this, "Ya existe un productor con esa cédula", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (productorDAO.agregarProductor(productor)) {
            JOptionPane.showMessageDialog(this, "Productor agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarProductores();
            limpiarFormulario();
        }
    }

    private void actualizarProductor() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un productor para editar (clic en 'Editar' en la tabla)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Productor productor = obtenerProductorDesdeFormulario(true);
        if (productor == null) return;
        
        productor.setIdProductor(idActual); 
        
        if (productor.getContrasena() != null && !productor.getContrasena().isEmpty()) {
             if (!productor.getContrasena().matches("^[a-zA-Z0-9_#$]+$")) {
                 JOptionPane.showMessageDialog(this, 
                    "La nueva contraseña contiene caracteres inválidos.\n" +
                    "Solo se permiten letras (sin ñ/tildes), números y los símbolos _ # $", 
                    "Error de Contraseña", 
                    JOptionPane.ERROR_MESSAGE);
                return; 
            }
        }
        
        Productor existente = productorDAO.obtenerProductorPorDocumento(productor.getCedulaCiudadania());
        if (existente != null && existente.getIdProductor() != productor.getIdProductor()) {
            JOptionPane.showMessageDialog(this, "Ya existe otro productor con esa cédula", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean exito;
        if (productor.getContrasena() == null || productor.getContrasena().isEmpty()) {
            exito = productorDAO.actualizarProductorSinContrasena(productor);
        } else {
            exito = productorDAO.actualizarProductorConContrasena(productor);
        }
        
        if (exito) {
            JOptionPane.showMessageDialog(this, "Productor actualizado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarProductores();
            limpiarFormulario();
        }
    }

    private Productor obtenerProductorDesdeFormulario(boolean esActualizacion) {
        String cedula = cedulaField.getText().trim();
        String nombre = nombreField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String correo = correoField.getText().trim();
        String contrasena = new String(contrasenaField.getPassword()).trim();

        if (cedula.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos (excepto contraseña al actualizar) son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!esActualizacion && contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria al agregar un nuevo productor", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        if (esActualizacion && contrasena.isEmpty()) {
            contrasena = null;
        }

        return new Productor(0, nombre, cedula, telefono, correo, contrasena);
    }

    private void limpiarFormulario() {
        cedulaField.setText("");
        nombreField.setText("");
        telefonoField.setText("");
        correoField.setText("");
        contrasenaField.setText("");
        
        showPassCheckbox.setSelected(false);
        contrasenaField.setEchoChar(defaultEchoChar);
        
        idActual = 0; 
    }
}