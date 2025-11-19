package admin; 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import DAO.PredioDAO; 
import DAO.LugarProduccionDAO; 
import Modelos.Predio; 
import Modelos.LugarProduccion; 
import java.sql.SQLException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;

public class predioAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    
    // Campos del formulario
    private JTextField ubicacionField, hectareasField;
    private JComboBox<LugarProduccion> lugarProduccionCombo; 
    
    private JLabel dateTimeLabel, totalLabel;
    
    // DAOs
    private PredioDAO predioDAO;
    private LugarProduccionDAO lugarProduccionDAO;
    
    private int idActual;
    private JFrame parentMenu;
    private Connection conn;

    public predioAd(JFrame parentMenu_recibido, Connection conn_recibida) throws SQLException {
        this.parentMenu = parentMenu_recibido;
        this.conn = conn_recibida;
        this.predioDAO = new PredioDAO(this.conn);
        this.lugarProduccionDAO = new LugarProduccionDAO(this.conn);
        idActual = 0;
        initializeUI();
        cargarComboLugarProduccion();
        cargarPredios();
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
        setTitle("Gestión de Predios");
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

        JLabel title = new JLabel("Predios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 500, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);

        JLabel subtitle = new JLabel("Gestión de predios de producción");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setBounds(45, 60, 400, 25);
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

        JLabel formTitle = new JLabel("Gestión de Predio");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        // --- Campos del formulario ---
        int yPos = 60;
        ubicacionField = crearCampoFormulario(leftPanel, "Ubicación (Municipio)*", yPos);
        yPos += 70;
        hectareasField = crearCampoFormulario(leftPanel, "Extensión (Hectáreas)*", yPos);
        yPos += 70;
        
        // --- JComboBox de Lugar de Producción ---
        lugarProduccionCombo = crearComboFormulario(leftPanel, "Lugar de Producción*", yPos);
        yPos += 70;
        
        // --- Botones ---
        JButton addBtn = crearBotonEstilizado("Agregar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos, 140, 40); 
        
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos, 140, 40);
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos + 50, 140, 40);
        
        JButton exitBtn = crearBotonEstilizado("Cerrar", new Color(210, 80, 70));
        exitBtn.setBounds(160, yPos + 50, 140, 40); 

        addBtn.addActionListener(e -> agregarPredio());
        updateBtn.addActionListener(e -> actualizarPredio());
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

        JLabel tableTitle = new JLabel("Lista de Predios");
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
        refreshBtn.setToolTipText("Actualizar tabla y lista de lugares"); 

        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshBtn.setForeground(new Color(40, 100, 45)); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshBtn.setForeground(new Color(60, 140, 65)); 
            }
        });

        // Acción: Recargar datos de PREDIOS y LUGARES (Combo)
        refreshBtn.addActionListener(e -> {
            cargarComboLugarProduccion(); // Recarga el combo por si hubo cambios
            cargarPredios(); // Recarga la tabla
        });

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);

        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);
        // --- FIN BOTÓN REFRESCAR ---

        rightPanel.add(tableHeader, BorderLayout.NORTH);

        // --- Columnas de la tabla ---
        String[] columnNames = {"ID", "Ubicación", "Hectáreas", "Lugar de Producción", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Columna de acciones
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return JPanel.class;
                return Object.class;
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersTable.setRowHeight(45);
        membersTable.setSelectionBackground(new Color(230, 245, 230));
        membersTable.setSelectionForeground(Color.BLACK);
        membersTable.setGridColor(new Color(240, 240, 240));
        
        // --- Ocultar Columna ID (Col 0) ---
        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        // Ancho de columnas visibles
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(200); 
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(80);  
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(200); 
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(130); 
        membersTable.getColumnModel().getColumn(4).setMaxWidth(150);

        membersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        membersTable.getTableHeader().setBackground(new Color(240, 248, 240));
        membersTable.getTableHeader().setForeground(new Color(40, 110, 45));
        membersTable.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));

        membersTable.getColumnModel().getColumn(4).setCellRenderer(new AccionesRenderer());
        membersTable.getColumnModel().getColumn(4).setCellEditor(new AccionesEditor());

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // --- Renderer ---
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

    // --- Editor ---
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
                editarPredioDesdeBoton(id);
            });

            eliminarBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(row, 0);
                eliminarPredioDesdeBoton(id);
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

    // --- Métodos de ayuda para la GUI ---
    
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
    
    // Método genérico para crear JComboBox
    private <T> JComboBox<T> crearComboFormulario(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBounds(0, y, 280, 20);
        label.setForeground(new Color(60, 120, 65));
        panel.add(label);
        
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setBounds(0, y + 25, 300, 40);
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 180)));
        comboBox.setBackground(Color.WHITE);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(comboBox);
        return comboBox;
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

    // --- MÉTODOS CRUD ADAPTADOS ---

    private void cargarComboLugarProduccion() {
        List<LugarProduccion> lugares = lugarProduccionDAO.obtenerTodosLugares();
        DefaultComboBoxModel<LugarProduccion> lugarModel = new DefaultComboBoxModel<>();
        for (LugarProduccion l : lugares) {
            lugarModel.addElement(l);
        }
        lugarProduccionCombo.setModel(lugarModel);
    }

    private void cargarPredios() {
        tableModel.setRowCount(0);
        
        Map<Integer, String> mapaLugares = new HashMap<>();
        for (LugarProduccion lugar : lugarProduccionDAO.obtenerTodosLugares()) {
            mapaLugares.put(lugar.getIdLugarProduccion(), lugar.getNombreLugar());
        }

        List<Predio> predios = predioDAO.obtenerTodosPredios();

        for (Predio predio : predios) {
            
            String nombreLugar = mapaLugares.getOrDefault(predio.getIdLugarProduccion(), "ID: " + predio.getIdLugarProduccion());
            
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
                predio.getIdPredio(),          // Col 0 (Oculta)
                predio.getUbicacionMun(),      // Col 1
                predio.getExtensionHectareas(),// Col 2
                nombreLugar,                   // Col 3 (Nombre traducido)
                panelAcciones                  // Col 4 (Panel de Acciones)
            });
        }
        totalLabel.setText("Total: " + predios.size());
    }

    private void editarPredioDesdeBoton(int id) {
        Predio predio = predioDAO.obtenerPredioPorId(id);
        if (predio != null) {
            cargarPredioEnFormulario(predio);
        }
    }

    private void eliminarPredioDesdeBoton(int id) {
        Predio predio = predioDAO.obtenerPredioPorId(id);
        if (predio != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar el predio: " + predio.getUbicacionMun() + "?",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (predioDAO.eliminarPredio(id)) {
                    JOptionPane.showMessageDialog(this, "Predio eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
                    SwingUtilities.invokeLater(() -> {
                        cargarPredios();
                        limpiarFormulario();
                    });
                }
            }
        }
    }

    private void cargarPredioEnFormulario(Predio predio) {
        idActual = predio.getIdPredio();
        ubicacionField.setText(predio.getUbicacionMun());
        hectareasField.setText(predio.getExtensionHectareas().toString());
        
        for (int i = 0; i < lugarProduccionCombo.getModel().getSize(); i++) {
            LugarProduccion lugar = (LugarProduccion) lugarProduccionCombo.getModel().getElementAt(i);
            if (lugar.getIdLugarProduccion() == predio.getIdLugarProduccion()) {
                lugarProduccionCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void agregarPredio() {
        Predio predio = obtenerPredioDesdeFormulario(false);
        if (predio == null) return;
        
        if (predioDAO.agregarPredio(predio)) {
            JOptionPane.showMessageDialog(this, "Predio agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPredios();
            limpiarFormulario();
        }
    }

    private void actualizarPredio() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un predio para editar (clic en 'Editar' en la tabla)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Predio predio = obtenerPredioDesdeFormulario(true);
        if (predio == null) return;
        
        predio.setIdPredio(idActual);
        
        if (predioDAO.actualizarPredio(predio)) {
            JOptionPane.showMessageDialog(this, "Predio actualizado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPredios();
            limpiarFormulario();
        }
    }

    private Predio obtenerPredioDesdeFormulario(boolean esActualizacion) {
        String ubicacion = ubicacionField.getText().trim();
        String hectareasStr = hectareasField.getText().trim();
        
        if (ubicacion.isEmpty() || hectareasStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        BigDecimal hectareas;
        try {
            hectareas = new BigDecimal(hectareasStr.replace(",", ".")); // Acepta coma o punto
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La extensión (hectáreas) debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        LugarProduccion lugarSeleccionado = (LugarProduccion) lugarProduccionCombo.getSelectedItem();
        
        if (lugarSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Lugar de Producción", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        int idLugar = lugarSeleccionado.getIdLugarProduccion();
        
        return new Predio(0, ubicacion, hectareas, idLugar);
    }

    private void limpiarFormulario() {
        idActual = 0;
        ubicacionField.setText("");
        hectareasField.setText("");
        
        if (lugarProduccionCombo.getItemCount() > 0) {
            lugarProduccionCombo.setSelectedIndex(0);
        }
    }
}