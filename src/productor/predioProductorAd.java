package productor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

import DAO.PredioDAO;
import DAO.LugarProduccionDAO;
import DAO.ProductorDAO;

import Modelos.Predio;
import Modelos.LugarProduccion;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.sql.Connection;

public class predioProductorAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    
    // --- Campos del Formulario ---
    private JTextField ubicacionField, hectareasField;
    private JComboBox<LugarProduccion> lugarProduccionCombo;
    
    private JLabel dateTimeLabel, totalLabel;
    
    // DAOs
    private PredioDAO predioDAO;
    private LugarProduccionDAO lugarProduccionDAO;
    private ProductorDAO productorDAO;
    
    private int idActual;
    private JFrame parentMenu;
    private Connection conn;
    private String correoProductor;
    private int idProductorLogueado;

    // --- CONSTRUCTOR ---
    public predioProductorAd(JFrame parentMenu_recibido, Connection conn_recibida, String correo) {
        this.parentMenu = parentMenu_recibido;
        this.conn = conn_recibida;
        this.correoProductor = correo;
        
        this.predioDAO = new PredioDAO(this.conn);
        this.lugarProduccionDAO = new LugarProduccionDAO(this.conn);
        this.productorDAO = new ProductorDAO(this.conn);
        
        // Identificar al productor
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        idActual = 0;
        initializeUI();
        cargarCombos();
        cargarPredios(); // Solo los míos
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parentMenu.setVisible(true);
            }
        });
    }

    private void initializeUI() {
        setTitle("Gestión de Mis Predios (Tierras)");
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
                // Degradado verde elegante
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 240),
                        0, getHeight(), new Color(230, 242, 230));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setBounds(0, 0, 1366, 100);
        topPanel.setLayout(null);
        add(topPanel);
        
        JLabel title = new JLabel("Mis Predios y Terrenos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 500, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);
        
        JLabel subtitle = new JLabel("Administre la extensión y ubicación de sus tierras");
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

        JLabel formTitle = new JLabel("Datos del Predio");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        // --- Campos ---
        int yPos = 60;
        
        lugarProduccionCombo = crearComboFormulario(leftPanel, "Lugar de Producción (Finca)*", yPos);
        yPos += 70;
        
        ubicacionField = crearCampoFormulario(leftPanel, "Ubicación / Vereda*", yPos);
        yPos += 70;
        
        hectareasField = crearCampoFormulario(leftPanel, "Extensión (Hectáreas)*", yPos);
        yPos += 70;
        
        // --- Botones ---
        JButton addBtn = crearBotonEstilizado("Guardar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos, 140, 40); 
        
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos, 140, 40);
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos + 50, 140, 40);
        
        JButton exitBtn = crearBotonEstilizado("Volver", new Color(210, 80, 70)); // Rojo
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
        
        // --- BOTÓN REFRESCAR ---
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

        refreshBtn.addActionListener(e -> {
            cargarCombos(); 
            cargarPredios(); 
        });

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);
        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);

        rightPanel.add(tableHeader, BorderLayout.NORTH);
        
        // --- Definición de Columnas ---
        String[] columnNames = {"ID", "Ubicación", "Hectáreas", "Lugar Producción", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
            @Override public Class<?> getColumnClass(int column) {
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
        
        // ID oculto
        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        membersTable.getColumnModel().getColumn(4).setMaxWidth(180);
        
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

    // --- Renderers y Editores ---
    private class AccionesRenderer implements javax.swing.table.TableCellRenderer {
         @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (!(value instanceof Component)) { return new JPanel(); }
            Component c = (Component) value;
            if (isSelected) { c.setBackground(new Color(230, 245, 230)); } 
            else { c.setBackground(Color.WHITE); }
            return c;
        }
    }

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
        public Object getCellEditorValue() { return this.currentValue; }
    }

    // --- Helpers GUI ---
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
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(bgColor); }
        });
        return button;
    }

    private void updateDateTime() {
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String hora = new SimpleDateFormat("HH:mm").format(new Date()) + " h";
        dateTimeLabel.setText("<html><div style='text-align: right;'>" + fecha + "<br>" + hora + "</div></html>");
    }

    // --- LÓGICA DE DATOS (FILTRADA PARA PRODUCTOR) ---

    private void cargarCombos() {
        // SOLO cargamos lugares de este productor
        List<LugarProduccion> lugares = lugarProduccionDAO.obtenerLugaresPorProductor(idProductorLogueado);
        DefaultComboBoxModel<LugarProduccion> lugarModel = new DefaultComboBoxModel<>();
        for (LugarProduccion l : lugares) {
            lugarModel.addElement(l); 
        }
        lugarProduccionCombo.setModel(lugarModel);
    }

    private void cargarPredios() {
        tableModel.setRowCount(0);
        
        Map<Integer, String> mapaLugares = new HashMap<>();
        for (LugarProduccion l : lugarProduccionDAO.obtenerLugaresPorProductor(idProductorLogueado)) {
            mapaLugares.put(l.getIdLugarProduccion(), l.getNombreLugar());
        }

        // !!! IMPORTANTE: USAMOS EL DAO QUE FILTRA POR PRODUCTOR !!!
        List<Predio> predios = predioDAO.obtenerPrediosPorProductor(idProductorLogueado);

        for (Predio p : predios) {
            String nombreLugar = mapaLugares.getOrDefault(p.getIdLugarProduccion(), "ID: " + p.getIdLugarProduccion());
            
            JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelAcciones.setBackground(Color.WHITE);
            JButton fakeEdit = new JButton("Editar"); 
            JButton fakeDel = new JButton("X");
            panelAcciones.add(fakeEdit); panelAcciones.add(fakeDel);
            
            tableModel.addRow(new Object[]{
                p.getIdPredio(),
                p.getUbicacionMun(),
                p.getExtensionHectareas(),
                nombreLugar,
                panelAcciones
            });
        }
        totalLabel.setText("Total: " + predios.size());
    }

    private void editarPredioDesdeBoton(int id) {
        // Buscamos en la lista actual para no consultar BD uno por uno si no hay metodo 'obtenerPorId'
        // O usamos una lógica de búsqueda simple
        // Si tu PredioDAO no tiene 'obtenerPredioPorId', hay que añadirlo. 
        // Asumiremos que vamos a buscar en la tabla visualmente
        for (int i = 0; i < tableModel.getRowCount(); i++) {
             int rowId = (int) tableModel.getValueAt(i, 0);
             if(rowId == id) {
                 String ubic = (String) tableModel.getValueAt(i, 1);
                 BigDecimal hect = (BigDecimal) tableModel.getValueAt(i, 2);
                 String nomLugar = (String) tableModel.getValueAt(i, 3);
                 
                 idActual = id;
                 ubicacionField.setText(ubic);
                 hectareasField.setText(hect.toString());
                 
                 // Seleccionar combo por nombre (chapuza útil si no tienes el objeto exacto)
                 for(int k=0; k<lugarProduccionCombo.getItemCount(); k++) {
                     LugarProduccion l = lugarProduccionCombo.getItemAt(k);
                     if(l.getNombreLugar().equals(nomLugar) || ("ID: "+l.getIdLugarProduccion()).equals(nomLugar)) {
                         lugarProduccionCombo.setSelectedIndex(k);
                         break;
                     }
                 }
                 break;
             }
        }
    }

    private void eliminarPredioDesdeBoton(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar este predio?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (predioDAO.eliminarPredio(id)) {
                JOptionPane.showMessageDialog(this, "Predio eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarPredios();
                limpiarFormulario();
            }
        }
    }

    private void agregarPredio() {
        Predio p = obtenerPredioDesdeFormulario(false);
        if (p == null) return;
        
        if (predioDAO.agregarPredio(p)) {
            JOptionPane.showMessageDialog(this, "Predio guardado correctamente");
            cargarPredios();
            limpiarFormulario();
        }
    }

    private void actualizarPredio() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un predio para editar");
            return;
        }
        Predio p = obtenerPredioDesdeFormulario(true);
        if (p == null) return;
        p.setIdPredio(idActual);
        
        if (predioDAO.actualizarPredio(p)) {
            JOptionPane.showMessageDialog(this, "Predio actualizado");
            cargarPredios();
            limpiarFormulario();
        }
    }

    private Predio obtenerPredioDesdeFormulario(boolean esActualizacion) {
        String ubicacion = ubicacionField.getText().trim();
        String hecStr = hectareasField.getText().trim();
        
        if (ubicacion.isEmpty() || hecStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        LugarProduccion lugarSeleccionado = (LugarProduccion) lugarProduccionCombo.getSelectedItem();
        if (lugarSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Lugar de Producción");
            return null;
        }
        
        try {
            BigDecimal hectareas = new BigDecimal(hecStr.replace(",", "."));
            return new Predio(0, ubicacion, hectareas, lugarSeleccionado.getIdLugarProduccion());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Hectáreas inválidas");
            return null;
        }
    }

    private void limpiarFormulario() {
        idActual = 0;
        ubicacionField.setText("");
        hectareasField.setText("");
        if (lugarProduccionCombo.getItemCount() > 0) lugarProduccionCombo.setSelectedIndex(0);
    }
}