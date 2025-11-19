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
import java.io.*; // IMPORTANTE PARA TXT

// --- !! IMPORTACIÓN DEL CALENDARIO !! ---
import com.toedter.calendar.JDateChooser; 

import DAO.LoteDAO;
import DAO.LugarProduccionDAO;
import DAO.CultivoDAO;
import DAO.ProductorDAO;

import Modelos.Lote;
import Modelos.LugarProduccion;
import Modelos.Cultivo;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.sql.Connection;

public class loteProductorAd extends JFrame {
    private JTable membersTable;
    private DefaultTableModel tableModel;
    
    // --- Componentes del Formulario ---
    private JDateChooser fechaChooser; 
    private JTextField estadoField;
    private JComboBox<LugarProduccion> lugarProduccionCombo;
    private JComboBox<Cultivo> cultivoCombo;
    
    private JLabel dateTimeLabel, totalLabel;
    
    // DAOs
    private LoteDAO loteDAO;
    private LugarProduccionDAO lugarProduccionDAO;
    private CultivoDAO cultivoDAO;
    private ProductorDAO productorDAO;
    
    private int idActual;
    private JFrame parentMenu;
    private Connection conn;
    private String correoProductor;
    private int idProductorLogueado;

    // --- CONSTRUCTOR ADAPTADO PARA PRODUCTOR ---
    public loteProductorAd(JFrame parentMenu_recibido, Connection conn_recibida, String correo) {
        this.parentMenu = parentMenu_recibido;
        this.conn = conn_recibida;
        this.correoProductor = correo;
        
        // Inicializar DAOs
        this.loteDAO = new LoteDAO(this.conn);
        this.lugarProduccionDAO = new LugarProduccionDAO(this.conn);
        this.cultivoDAO = new CultivoDAO(this.conn);
        this.productorDAO = new ProductorDAO(this.conn);
        
        // Obtener ID del productor
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        idActual = 0;
        initializeUI();
        cargarCombos();
        cargarLotes(); // Carga solo los lotes de este productor
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parentMenu.setVisible(true);
            }
        });
    }

    private void initializeUI() {
        setTitle("Gestión de Mis Lotes");
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
        
        JLabel title = new JLabel("Mis Lotes de Cultivo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 500, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);
        
        JLabel subtitle = new JLabel("Administre sus unidades de siembra");
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

    // --- PANEL FORMULARIO MODIFICADO CON LOS BOTONES NUEVOS ---
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

        JLabel formTitle = new JLabel("Datos del Lote");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setBounds(0, 0, 300, 30);
        formTitle.setForeground(new Color(40, 110, 45));
        leftPanel.add(formTitle);

        // --- Campos del formulario ---
        int yPos = 60;
        
        fechaChooser = crearDateChooserFormulario(leftPanel, "Fecha Siembra*", yPos);
        yPos += 70;
        
        estadoField = crearCampoFormulario(leftPanel, "Estado (Ej: Sembrado)*", yPos);
        yPos += 70;
        
        lugarProduccionCombo = crearComboFormulario(leftPanel, "Lugar de Producción*", yPos);
        yPos += 70;
        
        cultivoCombo = crearComboFormulario(leftPanel, "Cultivo Asociado*", yPos);
        yPos += 80; // Espacio extra
        
        // --- Botones CRUD ---
        JButton addBtn = crearBotonEstilizado("Guardar", new Color(60, 140, 65));
        addBtn.setBounds(0, yPos, 140, 40); 
        JButton updateBtn = crearBotonEstilizado("Actualizar", new Color(80, 160, 85));
        updateBtn.setBounds(160, yPos, 140, 40);
        
        yPos += 50;
        
        JButton clearBtn = crearBotonEstilizado("Limpiar", new Color(100, 100, 100));
        clearBtn.setBounds(0, yPos, 140, 40);
        JButton exitBtn = crearBotonEstilizado("Volver", new Color(210, 80, 70)); 
        exitBtn.setBounds(160, yPos, 140, 40); 

        // --- BOTONES DE INFORME (ESTILO TU CÓDIGO DE REFERENCIA) ---
        yPos += 60; // Separación
        
        // Botón Naranja: Ver Informe
        JButton btnVisualizar = crearBotonEstilizado("Ver Informe", new Color(255, 165, 0));
        btnVisualizar.setBounds(0, yPos, 140, 40);
        
        // Botón Azul: Descargar
        JButton btnDescargar = crearBotonEstilizado("Descargar .txt", new Color(0, 123, 255));
        btnDescargar.setBounds(160, yPos, 140, 40);
        
        addBtn.addActionListener(e -> agregarLote());
        updateBtn.addActionListener(e -> actualizarLote());
        clearBtn.addActionListener(e -> limpiarFormulario());
        exitBtn.addActionListener(e -> this.dispose());
        
        // ACCIONES NUEVAS
        btnVisualizar.addActionListener(e -> visualizarInforme());
        btnDescargar.addActionListener(e -> descargarInforme());
        
        leftPanel.add(addBtn);
        leftPanel.add(updateBtn);
        leftPanel.add(clearBtn);
        leftPanel.add(exitBtn);
        leftPanel.add(btnVisualizar);
        leftPanel.add(btnDescargar);
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
        
        JLabel tableTitle = new JLabel("Lotes Registrados");
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
            cargarLotes(); 
        });

        totalLabel = new JLabel("Total: 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(80, 130, 85));

        rightHeaderPanel.add(refreshBtn);
        rightHeaderPanel.add(totalLabel);
        tableHeader.add(rightHeaderPanel, BorderLayout.EAST);

        rightPanel.add(tableHeader, BorderLayout.NORTH);
        
        // --- Definición de Columnas ---
        String[] columnNames = {"ID", "Fecha Siembra", "Estado", "Lugar", "Cultivo", "Acciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
            @Override public Class<?> getColumnClass(int column) {
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
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        
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

    // --- Renderers y Editores (IGUAL QUE ADMIN) ---
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
                editarLoteDesdeBoton(id);
            });
            eliminarBtn.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(row, 0);
                eliminarLoteDesdeBoton(id);
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
    
    private JDateChooser crearDateChooserFormulario(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBounds(0, y, 280, 20);
        label.setForeground(new Color(60, 120, 65));
        panel.add(label);
        
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setBounds(0, y + 25, 300, 40);
        dateChooser.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 180)));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateChooser.setDateFormatString("yyyy-MM-dd"); 
        panel.add(dateChooser);
        return dateChooser;
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

    // --- LÓGICA DE DATOS (FILTRADA PARA PRODUCTOR) ---

    private void cargarCombos() {
        // 1. Lugares de Producción: Carga SOLO los del productor conectado
        List<LugarProduccion> lugares = lugarProduccionDAO.obtenerLugaresPorProductor(idProductorLogueado);
        DefaultComboBoxModel<LugarProduccion> lugarModel = new DefaultComboBoxModel<>();
        for (LugarProduccion l : lugares) {
            lugarModel.addElement(l); 
        }
        lugarProduccionCombo.setModel(lugarModel);
        
        // 2. Cultivos: Carga todos (Catálogo general)
        List<Cultivo> cultivos = cultivoDAO.obtenerTodosCultivos();
        DefaultComboBoxModel<Cultivo> cultivoModel = new DefaultComboBoxModel<>();
        for (Cultivo c : cultivos) {
            cultivoModel.addElement(c);
        }
        cultivoCombo.setModel(cultivoModel);
    }

    private void cargarLotes() {
        tableModel.setRowCount(0);
        
        Map<Integer, String> mapaLugares = new HashMap<>();
        // Map solo con mis lugares para ponerles nombre
        for (LugarProduccion l : lugarProduccionDAO.obtenerLugaresPorProductor(idProductorLogueado)) {
            mapaLugares.put(l.getIdLugarProduccion(), l.getNombreLugar());
        }
        Map<Integer, String> mapaCultivos = new HashMap<>();
        for (Cultivo c : cultivoDAO.obtenerTodosCultivos()) {
            mapaCultivos.put(c.getIdCultivo(), c.getNombres());
        }

        // !!! IMPORTANTE: USAMOS EL DAO QUE FILTRA POR PRODUCTOR !!!
        List<Lote> lotes = loteDAO.obtenerLotesPorProductor(idProductorLogueado);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Lote lote : lotes) {
            String nombreLugar = mapaLugares.getOrDefault(lote.getIdLugarProduccion(), "ID: " + lote.getIdLugarProduccion());
            String nombreCultivo = mapaCultivos.getOrDefault(lote.getIdCultivo(), "ID: " + lote.getIdCultivo());
            String fechaFormateada = (lote.getFechaSiembra() != null) ? sdf.format(lote.getFechaSiembra()) : "N/A";
            
            JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelAcciones.setBackground(Color.WHITE);
            // Botones visuales para el renderer
            JButton fakeEdit = new JButton("Editar"); 
            JButton fakeDel = new JButton("X");
            panelAcciones.add(fakeEdit); panelAcciones.add(fakeDel);
            
            tableModel.addRow(new Object[]{
                lote.getIdLote(),
                fechaFormateada,
                lote.getEstado(),
                nombreLugar,
                nombreCultivo,
                panelAcciones
            });
        }
        totalLabel.setText("Total: " + lotes.size());
    }

    private void editarLoteDesdeBoton(int id) {
        Lote lote = loteDAO.obtenerLotePorId(id);
        if (lote != null) {
            cargarLoteEnFormulario(lote);
        }
    }

    private void eliminarLoteDesdeBoton(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar este lote?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (loteDAO.eliminarLote(id)) {
                JOptionPane.showMessageDialog(this, "Lote eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.invokeLater(() -> {
                    cargarLotes();
                    limpiarFormulario();
                });
            }
        }
    }

    private void cargarLoteEnFormulario(Lote lote) {
        idActual = lote.getIdLote();
        
        if(lote.getFechaSiembra() != null) {
            fechaChooser.setDate(lote.getFechaSiembra());
        }
        
        estadoField.setText(lote.getEstado());
        
        // Seleccionar en combos
        for (int i = 0; i < lugarProduccionCombo.getModel().getSize(); i++) {
            LugarProduccion l = lugarProduccionCombo.getModel().getElementAt(i);
            if (l.getIdLugarProduccion() == lote.getIdLugarProduccion()) {
                lugarProduccionCombo.setSelectedIndex(i);
                break;
            }
        }
        
        for (int i = 0; i < cultivoCombo.getModel().getSize(); i++) {
            Cultivo c = cultivoCombo.getModel().getElementAt(i);
            if (c.getIdCultivo() == lote.getIdCultivo()) {
                cultivoCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void agregarLote() {
        Lote lote = obtenerLoteDesdeFormulario(false);
        if (lote == null) return;
        
        if (loteDAO.agregarLote(lote)) {
            JOptionPane.showMessageDialog(this, "Lote guardado correctamente");
            cargarLotes();
            limpiarFormulario();
        }
    }

    private void actualizarLote() {
        if (idActual == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un lote para editar");
            return;
        }
        Lote lote = obtenerLoteDesdeFormulario(true);
        if (lote == null) return;
        lote.setIdLote(idActual);
        
        if (loteDAO.actualizarLote(lote)) {
            JOptionPane.showMessageDialog(this, "Lote actualizado");
            cargarLotes();
            limpiarFormulario();
        }
    }

    private Lote obtenerLoteDesdeFormulario(boolean esActualizacion) {
        Date fechaSiembra = fechaChooser.getDate();
        String estado = estadoField.getText().trim();
        
        if (fechaSiembra == null || estado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fecha y Estado son obligatorios");
            return null;
        }
        
        LugarProduccion lugarSeleccionado = (LugarProduccion) lugarProduccionCombo.getSelectedItem();
        Cultivo cultivoSeleccionado = (Cultivo) cultivoCombo.getSelectedItem();
        
        if (lugarSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Lugar de Producción");
            return null;
        }
        
        if (cultivoSeleccionado == null) {
             JOptionPane.showMessageDialog(this, "Debe seleccionar un Cultivo");
            return null;
        }
        
        return new Lote(0, fechaSiembra, estado, lugarSeleccionado.getIdLugarProduccion(), cultivoSeleccionado.getIdCultivo());
    }

    private void limpiarFormulario() {
        idActual = 0;
        fechaChooser.setDate(null);
        estadoField.setText("");
        if (lugarProduccionCombo.getItemCount() > 0) lugarProduccionCombo.setSelectedIndex(0);
        if (cultivoCombo.getItemCount() > 0) cultivoCombo.setSelectedIndex(0);
    }

    // ============================================================
    //    AQUÍ ESTÁ LA LÓGICA QUE ME PEDISTE (ESTILO FITOSANITARIO)
    // ============================================================

    private String generarTextoInforme() {
        // 1. Obtenemos los datos frescos de la BD
        List<Lote> listaLotes = loteDAO.obtenerLotesPorProductor(idProductorLogueado);
        
        // 2. Preparamos mapas para tener los nombres
        Map<Integer, String> mapaLugares = new HashMap<>();
        for (LugarProduccion l : lugarProduccionDAO.obtenerLugaresPorProductor(idProductorLogueado)) {
            mapaLugares.put(l.getIdLugarProduccion(), l.getNombreLugar());
        }
        Map<Integer, String> mapaCultivos = new HashMap<>();
        for (Cultivo c : cultivoDAO.obtenerTodosCultivos()) {
            mapaCultivos.put(c.getIdCultivo(), c.getNombres());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 3. Construimos el String LITERAL como en el ejemplo
        StringBuilder informe = new StringBuilder();
        informe.append("========== INFORME DE LOTES DE PRODUCCIÓN ==========\n");
        informe.append("Productor: ").append(correoProductor).append("\n");
        informe.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy").format(new Date())).append("\n\n");
        
        // Encabezados alineados (igual que tu ejemplo de Fitosanitarios)
        informe.append(String.format("%-15s %-15s %-20s %-20s\n", "Fecha", "Estado", "Lugar", "Cultivo"));
        informe.append("---------------------------------------------------------------------------\n");

        for (Lote l : listaLotes) {
            String fechaStr = (l.getFechaSiembra() != null) ? sdf.format(l.getFechaSiembra()) : "N/A";
            String nombreLugar = mapaLugares.getOrDefault(l.getIdLugarProduccion(), "Desc.");
            String nombreCultivo = mapaCultivos.getOrDefault(l.getIdCultivo(), "Desc.");

            informe.append(String.format("%-15s %-15s %-20s %-20s\n",
                    fechaStr, 
                    l.getEstado(), 
                    cortar(nombreLugar, 19), 
                    cortar(nombreCultivo, 19)));
        }

        informe.append("===========================================================================\n");
        return informe.toString();
    }

    // Auxiliar para que no se descuadre la tabla de texto
    private String cortar(String texto, int max) {
        if(texto == null) return "";
        if(texto.length() > max) return texto.substring(0, max-3) + "...";
        return texto;
    }

    private void visualizarInforme() {
        List<Lote> listaCheck = loteDAO.obtenerLotesPorProductor(idProductorLogueado);
        if (listaCheck.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para generar el informe.");
            return;
        }

        JTextArea areaInforme = new JTextArea(generarTextoInforme());
        areaInforme.setEditable(false);
        areaInforme.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Fuente monoespaciada para alinear
        JScrollPane scrollPane = new JScrollPane(areaInforme);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Informe de Lotes", JOptionPane.INFORMATION_MESSAGE);
    }

    private void descargarInforme() {
        List<Lote> listaCheck = loteDAO.obtenerLotesPorProductor(idProductorLogueado);
        if (listaCheck.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para descargar.");
            return;
        }

        String informe = generarTextoInforme();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe");
        fileChooser.setSelectedFile(new File("informe_lotes.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            // Asegurar extensión .txt
            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".txt");
            }
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
                bw.write(informe);
                JOptionPane.showMessageDialog(this, "Informe guardado correctamente en:\n" + archivo.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el informe: " + e.getMessage());
            }
        }
    }
}