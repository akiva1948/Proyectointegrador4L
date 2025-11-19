package admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import DAO.MunicipioDAO;
import Modelos.Municipio;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.sql.Connection;

public class municipioAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField nombreField;
    private JLabel dateTimeLabel, totalLabel;
    private MunicipioDAO municipioDAO;
    private int idActual;
    private JFrame parentMenu;
    private Connection conn;

    public municipioAd(JFrame parentMenu_recibido, Connection conn_recibida) throws SQLException {
        this.parentMenu = parentMenu_recibido;
        this.conn = conn_recibida;
        
        this.municipioDAO = new MunicipioDAO(this.conn);
        
        idActual = 0;
        initializeUI();
        cargarMunicipios();
        
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
        setTitle("Gestión de Municipios");
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

        JLabel title = new JLabel("Municipios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 400, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);

        JLabel subtitle = new JLabel("Gestión de municipios de producción");
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

        JLabel formTitle = new JLabel("Gestión de Municipio");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        int yPos = 60;
        nombreField = crearCampoFormulario(leftPanel, "Nombre del Municipio*", yPos);
        yPos += 70; 
        
        JButton addBtn = crearBotonEstilizado("Agregar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos + 20, 140, 40); 
        
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos + 20, 140, 40); 
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos + 70, 140, 40); 
        
        JButton exitBtn = crearBotonEstilizado("Cerrar", new Color(210, 80, 70));
        exitBtn.setBounds(160, yPos + 70, 140, 40); 

        addBtn.addActionListener(e -> agregarMunicipio());
        updateBtn.addActionListener(e -> actualizarMunicipio());
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

        JLabel tableTitle = new JLabel("Lista de Municipios");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(40, 110, 45));
        tableHeader.add(tableTitle, BorderLayout.WEST);

        // --- !! AQUI ESTÁ EL BOTÓN DE REFRESCAR !! ---
        
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

        // Acción: Recargar datos de MUNICIPIOS
        refreshBtn.addActionListener(e -> cargarMunicipios());

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);

        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);
        
        // --- FIN DEL CAMBIO ---

        rightPanel.add(tableHeader, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; 
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return JPanel.class;
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
        
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        membersTable.getColumnModel().getColumn(2).setMaxWidth(200);

        membersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        membersTable.getTableHeader().setBackground(new Color(240, 248, 240));
        membersTable.getTableHeader().setForeground(new Color(40, 110, 45));
        membersTable.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));

        membersTable.getColumnModel().getColumn(2).setCellRenderer(new AccionesRenderer());
        membersTable.getColumnModel().getColumn(2).setCellEditor(new AccionesEditor());

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

            editarBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(row, 0); 
                editarMunicipioDesdeBoton(id);
            });

            eliminarBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(row, 0);
                eliminarMunicipioDesdeBoton(id);
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

    private void cargarMunicipios() {
        tableModel.setRowCount(0);
        List<Municipio> municipios = municipioDAO.obtenerTodosMunicipios();

        for (Municipio municipio : municipios) {
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
                municipio.getIdMunicipio(), // Col 0 (Oculta)
                municipio.getNombreMunicipio(), // Col 1
                panelAcciones // Col 2
            });
        }
        totalLabel.setText("Total: " + municipios.size());
    }

    private void editarMunicipioDesdeBoton(int id) {
        Municipio municipio = municipioDAO.obtenerMunicipioPorId(id);
        if (municipio != null) {
            cargarMunicipioEnFormulario(municipio);
        }
    }

    private void eliminarMunicipioDesdeBoton(int id) {
        Municipio municipio = municipioDAO.obtenerMunicipioPorId(id);
        if (municipio != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar el municipio: " + municipio.getNombreMunicipio() + "?",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (municipioDAO.eliminarMunicipio(id)) {
                    JOptionPane.showMessageDialog(this, "Municipio eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    SwingUtilities.invokeLater(() -> {
                        cargarMunicipios();
                        limpiarFormulario();
                    });
                }
            }
        }
    }

    private void cargarMunicipioEnFormulario(Municipio municipio) {
        idActual = municipio.getIdMunicipio();
        nombreField.setText(municipio.getNombreMunicipio());
    }

    private void agregarMunicipio() {
        Municipio municipio = obtenerMunicipioDesdeFormulario(false);
        if (municipio == null) return;
        
        if (municipioDAO.existeMunicipioPorNombre(municipio.getNombreMunicipio())) {
            JOptionPane.showMessageDialog(this, "Ya existe un municipio con ese nombre", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (municipioDAO.agregarMunicipio(municipio)) {
            JOptionPane.showMessageDialog(this, "Municipio agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarMunicipios();
            limpiarFormulario();
        }
    }

    private void actualizarMunicipio() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un municipio para editar primero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Municipio municipio = obtenerMunicipioDesdeFormulario(true);
        if (municipio == null) return;
        
        municipio.setIdMunicipio(idActual);
        
        Municipio existente = municipioDAO.obtenerMunicipioPorNombre(municipio.getNombreMunicipio());
        if (existente != null && existente.getIdMunicipio() != municipio.getIdMunicipio()) {
            JOptionPane.showMessageDialog(this, "Ya existe otro municipio con ese nombre", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (municipioDAO.actualizarMunicipio(municipio)) {
            JOptionPane.showMessageDialog(this, "Municipio actualizado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarMunicipios();
            limpiarFormulario();
        }
    }

    private Municipio obtenerMunicipioDesdeFormulario(boolean esActualizacion) {
        String nombre = nombreField.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new Municipio(0, nombre);
    }

    private void limpiarFormulario() {
        nombreField.setText("");
        idActual = 0;
    }
}