package productor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer; 
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// --- IMPORTS NUEVOS PARA ARCHIVOS ---
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import DAO.LugarProduccionDAO;
import DAO.MunicipioDAO;
import DAO.ProductorDAO;

import Modelos.LugarProduccion;
import Modelos.Municipio;
import Modelos.Productor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.sql.SQLException;
import java.sql.Connection;

public class lugarProductorAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    
    // Campos del formulario
    private JTextField nombreField, telefonoField, correoField;
    private JComboBox<Municipio> municipioCombo; 
    
    private JLabel dateTimeLabel, totalLabel;
    
    private LugarProduccionDAO lugarDAO;
    private MunicipioDAO municipioDAO;
    private ProductorDAO productorDAO;
    
    private int idActual;
    private JFrame parentMenu;
    private Connection conn;
    private String correoProductor;
    private int idProductorLogueado;

    public lugarProductorAd(JFrame parentMenu, Connection conn, String correoProductor) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.correoProductor = correoProductor;
        
        this.lugarDAO = new LugarProduccionDAO(this.conn);
        this.municipioDAO = new MunicipioDAO(this.conn);
        this.productorDAO = new ProductorDAO(this.conn);
        
        // Obtener ID del productor conectado
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        idActual = 0;
        initializeUI();
        cargarCombos();
        cargarMisLugares(); // Carga solo los tuyos
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parentMenu.setVisible(true);
            }
        });
    }
    
    private void initializeUI() {
        setTitle("Gestión de Mis Lugares de Producción");
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
                // Degradado verde profesional
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 240),
                        0, getHeight(), new Color(230, 242, 230));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setBounds(0, 0, 1366, 100);
        topPanel.setLayout(null);
        add(topPanel);

        JLabel title = new JLabel("Mis Lugares de Producción");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 600, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);

        JLabel subtitle = new JLabel("Administre sus fincas y terrenos registrados");
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

        JLabel formTitle = new JLabel("Datos del Lugar");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        // --- Campos del formulario ---
        int yPos = 60;
        nombreField = crearCampoFormulario(leftPanel, "Nombre del Lugar*", yPos);
        yPos += 70;
        telefonoField = crearCampoFormulario(leftPanel, "Teléfono*", yPos);
        yPos += 70;
        correoField = crearCampoFormulario(leftPanel, "Correo*", yPos);
        yPos += 70;
        
        // --- JComboBox de Municipio ---
        municipioCombo = crearComboFormulario(leftPanel, "Municipio*", yPos);
        yPos += 80; // Espacio extra
        
        // --- Botones CRUD ---
        JButton addBtn = crearBotonEstilizado("Guardar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos, 140, 40); 
        
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos, 140, 40);
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos + 50, 140, 40);

        // --- NUEVO BOTÓN DE INFORME ---
        JButton reportBtn = crearBotonEstilizado("📄 Generar Informe", new Color(50, 100, 150)); // Azul profesional
        reportBtn.setBounds(160, yPos + 50, 140, 40);
        
        // --- Botón Salir ---
        JButton exitBtn = crearBotonEstilizado("Volver al Menú", new Color(180, 60, 60)); // Rojo
        exitBtn.setBounds(0, yPos + 100, 300, 40); // Ancho completo

        addBtn.addActionListener(e -> agregarLugar());
        updateBtn.addActionListener(e -> actualizarLugar());
        clearBtn.addActionListener(e -> limpiarFormulario());
        reportBtn.addActionListener(e -> descargarInformeLugares()); // <--- ACCIÓN NUEVA
        exitBtn.addActionListener(e -> this.dispose());
        
        leftPanel.add(addBtn);
        leftPanel.add(updateBtn);
        leftPanel.add(clearBtn);
        leftPanel.add(reportBtn); // Agregar botón al panel
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

        JLabel tableTitle = new JLabel("Lugares Registrados");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(40, 110, 45));
        tableHeader.add(tableTitle, BorderLayout.WEST);

        // --- BOTÓN REFRESCAR ---
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeaderPanel.setBackground(new Color(245, 248, 245));

        JButton refreshBtn = new JButton("↻"); 
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 24)); 
        refreshBtn.setForeground(new Color(60, 140, 65)); 
        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false); 
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setToolTipText("Actualizar tabla"); 

        refreshBtn.addActionListener(e -> {
            cargarCombos();
            cargarMisLugares();
        });

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);

        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);
        rightPanel.add(tableHeader, BorderLayout.NORTH);

        // --- Columnas (Sin columna Productor, porque es redundante) ---
        String[] columnNames = {"ID", "Nombre", "Teléfono", "Correo", "Municipio", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // La columna de acciones es la 5
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) return JPanel.class;
                return Object.class;
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        membersTable.setRowHeight(45);
        membersTable.setSelectionBackground(new Color(230, 245, 230));
        membersTable.setSelectionForeground(Color.BLACK);
        membersTable.setGridColor(new Color(240, 240, 240));
        
        // Ocultar ID
        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        // Anchos
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(150);

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

    // --- Renderer para Botones en Tabla ---
    private class AccionesRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (!(value instanceof Component)) return new JPanel();
            Component c = (Component) value;
            c.setBackground(isSelected ? new Color(230, 245, 230) : Color.WHITE);
            return c;
        }
    }

    // --- Editor para Botones en Tabla ---
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
            editarBtn.setOpaque(true); // Corrección visual
            editarBtn.setContentAreaFilled(true);

            JButton eliminarBtn = new JButton("X");
            eliminarBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            eliminarBtn.setBackground(new Color(231, 76, 60));
            eliminarBtn.setForeground(Color.WHITE);
            eliminarBtn.setBorderPainted(false);
            eliminarBtn.setFocusPainted(false);
            eliminarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eliminarBtn.setMargin(new Insets(2, 8, 2, 8));
            eliminarBtn.setOpaque(true); // Corrección visual
            eliminarBtn.setContentAreaFilled(true);

            editarBtn.addActionListener(e -> {
                fireEditingStopped();
                // Obtenemos datos directamente de la tabla para llenar el formulario
                int id = (int) tableModel.getValueAt(row, 0);
                String nom = (String) tableModel.getValueAt(row, 1);
                String tel = (String) tableModel.getValueAt(row, 2);
                String cor = (String) tableModel.getValueAt(row, 3);
                String munNombre = (String) tableModel.getValueAt(row, 4);
                
                cargarLugarEnFormularioManual(id, nom, tel, cor, munNombre);
            });

            eliminarBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(row, 0);
                String nom = (String) tableModel.getValueAt(row, 1);
                eliminarLugarDesdeBoton(id, nom);
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
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        
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

    // --- LÓGICA CRUD ---

    private void cargarCombos() {
        // Municipios
        List<Municipio> municipios = municipioDAO.obtenerTodosMunicipios();
        DefaultComboBoxModel<Municipio> municipioModel = new DefaultComboBoxModel<>();
        for (Municipio m : municipios) municipioModel.addElement(m);
        municipioCombo.setModel(municipioModel);
    }

    private void cargarMisLugares() {
        tableModel.setRowCount(0);
        
        Map<Integer, String> mapaMunicipios = new HashMap<>();
        for (Municipio mun : municipioDAO.obtenerTodosMunicipios()) {
            mapaMunicipios.put(mun.getIdMunicipio(), mun.getNombreMunicipio());
        }
        
        // USAMOS EL MÉTODO FILTRADO POR PRODUCTOR
        List<LugarProduccion> lugares = lugarDAO.obtenerLugaresPorProductor(idProductorLogueado);

        for (LugarProduccion lugar : lugares) {
            String nombreMun = mapaMunicipios.getOrDefault(lugar.getIdMunicipio(), "ID: " + lugar.getIdMunicipio());
            
            JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelAcciones.setBackground(Color.WHITE);
            // (Los botones se agregan en el Editor, esto es visual para el Renderer)
            JButton btnFake1 = new JButton("Editar"); 
            JButton btnFake2 = new JButton("X");
            panelAcciones.add(btnFake1); panelAcciones.add(btnFake2);
            
            tableModel.addRow(new Object[]{
                lugar.getIdLugarProduccion(),
                lugar.getNombreLugar(),
                lugar.getTelefono(),
                lugar.getCorreo(),
                nombreMun,
                panelAcciones // Objeto visual
            });
        }
        totalLabel.setText("Total: " + lugares.size());
    }

    private void cargarLugarEnFormularioManual(int id, String nom, String tel, String cor, String nombreMun) {
        idActual = id;
        nombreField.setText(nom);
        telefonoField.setText(tel);
        correoField.setText(cor);
        
        // Seleccionar el municipio en el combo comparando Strings
        for (int i = 0; i < municipioCombo.getItemCount(); i++) {
            Municipio m = municipioCombo.getItemAt(i);
            if (m.getNombreMunicipio().equals(nombreMun) || m.toString().equals(nombreMun)) {
                municipioCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void eliminarLugarDesdeBoton(int id, String nombre) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar lugar: " + nombre + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (lugarDAO.eliminarLugarProduccion(id)) {
                JOptionPane.showMessageDialog(this, "Eliminado correctamente");
                cargarMisLugares();
                limpiarFormulario();
            }
        }
    }

    private void agregarLugar() {
        LugarProduccion lugar = obtenerLugarDesdeFormulario(false);
        if (lugar == null) return;
        
        if (lugarDAO.agregarLugarProduccion(lugar)) {
            JOptionPane.showMessageDialog(this, "¡Lugar guardado!");
            cargarMisLugares();
            limpiarFormulario();
        }
    }

    private void actualizarLugar() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un lugar en la tabla (Botón Editar)", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LugarProduccion lugar = obtenerLugarDesdeFormulario(true);
        if (lugar == null) return;
        
        lugar.setIdLugarProduccion(idActual);
        
        if (lugarDAO.actualizarLugarProduccion(lugar)) {
            JOptionPane.showMessageDialog(this, "Actualizado correctamente");
            cargarMisLugares();
            limpiarFormulario();
        }
    }

    private LugarProduccion obtenerLugarDesdeFormulario(boolean esActualizacion) {
        String nombre = nombreField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String correo = correoField.getText().trim();
        
        if (nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        Municipio munSeleccionado = (Municipio) municipioCombo.getSelectedItem();
        if (munSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un Municipio", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        return new LugarProduccion(0, nombre, telefono, correo, munSeleccionado.getIdMunicipio(), idProductorLogueado);
    }

    private void limpiarFormulario() {
        idActual = 0;
        nombreField.setText("");
        telefonoField.setText("");
        correoField.setText("");
        if (municipioCombo.getItemCount() > 0) municipioCombo.setSelectedIndex(0);
    }
    
    // ========================================================================
    // NUEVO MÉTODO: GENERAR INFORME DE LUGARES
    // ========================================================================
    private void descargarInformeLugares() {
        List<LugarProduccion> lista = lugarDAO.obtenerLugaresPorProductor(idProductorLogueado);
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay lugares registrados para descargar.");
            return;
        }

        // Mapa auxiliar para tener los nombres de municipios (ya que el objeto Lugar solo tiene ID)
        Map<Integer, String> mapaMunicipios = new HashMap<>();
        for (Municipio mun : municipioDAO.obtenerTodosMunicipios()) {
            mapaMunicipios.put(mun.getIdMunicipio(), mun.getNombreMunicipio());
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        
        sb.append("==============================================================================\n");
        sb.append("                REPORTE OFICIAL DE LUGARES DE PRODUCCIÓN\n");
        sb.append("==============================================================================\n");
        sb.append("Fecha: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("Productor ID: ").append(idProductorLogueado).append("\n");
        sb.append("Total Registros: ").append(lista.size()).append("\n\n");
        
        // Encabezados de columna alineados
        sb.append(String.format("%-5s %-25s %-15s %-25s %-15s\n", "ID", "NOMBRE LUGAR", "TELÉFONO", "CORREO", "MUNICIPIO"));
        sb.append("------------------------------------------------------------------------------\n");

        for (LugarProduccion l : lista) {
            String nomMuni = mapaMunicipios.getOrDefault(l.getIdMunicipio(), "ID:" + l.getIdMunicipio());
            sb.append(String.format("%-5d %-25s %-15s %-25s %-15s\n", 
                l.getIdLugarProduccion(), 
                l.getNombreLugar(), 
                l.getTelefono(), 
                l.getCorreo(), 
                nomMuni));
        }
        sb.append("------------------------------------------------------------------------------\n");
        sb.append("Fin del reporte.\n");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe de Lugares");
        fileChooser.setSelectedFile(new File("Informe_MisLugares.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write(sb.toString());
                JOptionPane.showMessageDialog(this, "Informe guardado en: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar archivo: " + ex.getMessage());
            }
        }
    }
}