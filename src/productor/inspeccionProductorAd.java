package productor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.*;
import java.sql.Connection;

import DAO.InspeccionDAO;
import DAO.ProductorDAO;

public class inspeccionProductorAd extends JFrame {
    
    // --- Colores del Tema ---
    private final Color PRIMARY_COLOR = new Color(40, 110, 45);
    private final Color HEADER_GRADIENT_START = new Color(240, 248, 240);
    private final Color HEADER_GRADIENT_END = new Color(230, 242, 230);
    
    // Componentes
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalRegistrosLabel, dateTimeLabel;
    
    // Campos de Detalle (Solo Lectura)
    private JTextField txtFecha, txtLote, txtCultivo, txtPlaga, txtTecnico;
    private JTextArea txtObservaciones;
    
    private Connection conn;
    private JFrame parentMenu;
    private String correoProductor;
    private int idProductorLogueado;
    
    private InspeccionDAO inspeccionDAO;
    private ProductorDAO productorDAO;

    public inspeccionProductorAd(JFrame parentMenu, Connection conn, String correoProductor) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.correoProductor = correoProductor;
        
        // Inicializar DAOs
        this.inspeccionDAO = new InspeccionDAO(conn);
        this.productorDAO = new ProductorDAO(conn);
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        initializeUI();
        cargarInspecciones();
        
        // Timer Reloj
        new Timer(1000, e -> updateDateTime()).start();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { parentMenu.setVisible(true); }
        });
    }

    private void initializeUI() {
        setTitle("Historial de Inspecciones Fitosanitarias");
        setSize(1366, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 245));

        // 1. PANEL SUPERIOR (HEADER)
        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, HEADER_GRADIENT_START,
                        0, getHeight(), HEADER_GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setLayout(null);
        
        JLabel title = new JLabel("Mis Inspecciones");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(PRIMARY_COLOR);
        title.setBounds(40, 15, 500, 45);
        topPanel.add(title);
        
        JLabel subtitle = new JLabel("Reportes técnicos y hallazgos de plagas en sus cultivos");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(70, 120, 75));
        subtitle.setBounds(45, 60, 500, 25);
        topPanel.add(subtitle);
        
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateTimeLabel.setForeground(new Color(50, 110, 55));
        dateTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateTimeLabel.setBounds(1120, 25, 220, 50);
        updateDateTime();
        topPanel.add(dateTimeLabel);
        
        add(topPanel, BorderLayout.NORTH);

        // 2. CONTENEDOR PRINCIPAL
        JPanel mainContainer = new JPanel(null);
        mainContainer.setBackground(new Color(245, 248, 245));
        add(mainContainer, BorderLayout.CENTER);

        // --- A. PANEL IZQUIERDO (DETALLES / LECTURA) ---
        JPanel leftPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(250, 252, 250));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setBounds(20, 20, 360, 620);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        mainContainer.add(leftPanel);

        JLabel lblInfo = new JLabel("Detalle de Inspección");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblInfo.setForeground(PRIMARY_COLOR);
        lblInfo.setBounds(0, 0, 300, 30);
        leftPanel.add(lblInfo);
        
        JLabel lblSub = new JLabel("Seleccione una fila para ver detalles");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSub.setForeground(Color.GRAY);
        lblSub.setBounds(0, 35, 300, 20);
        leftPanel.add(lblSub);

        int y = 70;
        txtFecha = crearCampoLectura(leftPanel, "Fecha de Visita:", y); y+=70;
        txtLote = crearCampoLectura(leftPanel, "Lote Inspeccionado:", y); y+=70;
        txtCultivo = crearCampoLectura(leftPanel, "Cultivo:", y); y+=70;
        txtPlaga = crearCampoLectura(leftPanel, "Plaga Reportada:", y); y+=70;
        txtTecnico = crearCampoLectura(leftPanel, "Técnico Inspector:", y); y+=70;
        
        // Área de Observaciones (Más grande)
        JLabel lblObs = new JLabel("Observaciones / Recomendaciones:");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblObs.setForeground(new Color(60, 120, 65));
        lblObs.setBounds(0, y, 300, 20);
        leftPanel.add(lblObs);
        
        txtObservaciones = new JTextArea();
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setEditable(false); // Solo lectura
        txtObservaciones.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtObservaciones.setBackground(new Color(240, 240, 240));
        
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setBounds(0, y+25, 320, 100);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        leftPanel.add(scrollObs);
        
        // Botones Inferiores
        JButton btnVolver = new JButton("VOLVER AL MENÚ");
        btnVolver.setBounds(0, y + 140, 320, 45);
        btnVolver.setBackground(new Color(180, 60, 60));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> this.dispose());
        leftPanel.add(btnVolver);

        // --- B. PANEL DERECHO (TABLA) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBounds(400, 20, 930, 620);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        mainContainer.add(rightPanel);
        
        // Header Tabla
        JPanel headerT = new JPanel(new BorderLayout());
        headerT.setBackground(new Color(245, 248, 245));
        headerT.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel tTitle = new JLabel("Registros Encontrados");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tTitle.setForeground(PRIMARY_COLOR);
        
        totalRegistrosLabel = new JLabel("Total: 0");
        totalRegistrosLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalRegistrosLabel.setForeground(new Color(80, 130, 85));
        
        // Botón Refrescar
        JButton btnRefresh = new JButton("\u27F3"); // Símbolo refresh
        btnRefresh.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        btnRefresh.setForeground(PRIMARY_COLOR);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> cargarInspecciones());
        
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setBackground(new Color(245, 248, 245));
        rightHeader.add(btnRefresh);
        rightHeader.add(totalRegistrosLabel);
        
        headerT.add(tTitle, BorderLayout.WEST);
        headerT.add(rightHeader, BorderLayout.EAST);
        rightPanel.add(headerT, BorderLayout.NORTH);
        
        // Tabla Config
        String[] cols = {"ID", "Fecha", "Lote", "Cultivo", "Plaga", "Técnico", "Observaciones (Oculto)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Tabla de solo lectura
        };
        
        table = new JTable(tableModel);
        estilizarTabla(table);
        
        // Evento Click en Tabla (Para ver detalles)
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                mostrarDetalles(table.getSelectedRow());
            }
        });
        
        // Ocultar columna Observaciones (se ve en el panel izquierdo)
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setPreferredWidth(0);
        
        // Anchos
        table.getColumnModel().getColumn(0).setMaxWidth(60); // ID pequeño
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Fecha

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        rightPanel.add(scroll, BorderLayout.CENTER);
    }

    // ========================================================================
    // LÓGICA DE DATOS
    // ========================================================================
    
    private void cargarInspecciones() {
        tableModel.setRowCount(0);
        
        // Usamos el método del DAO que devuelve datos "humanos" (nombres, no IDs)
        List<Object[]> lista = inspeccionDAO.obtenerReportePorProductor(idProductorLogueado);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Object[] row : lista) {
            tableModel.addRow(row);
        }
        
        totalRegistrosLabel.setText("Total: " + lista.size());
        limpiarDetalles();
    }
    
    private void mostrarDetalles(int row) {
        // Sacamos los datos directamente de la tabla
        // [0]ID, [1]Fecha, [2]Lote, [3]Cultivo, [4]Plaga, [5]Tecnico, [6]Observaciones
        
        try {
            Object fechaObj = table.getValueAt(row, 1);
            String fechaStr = (fechaObj != null) ? fechaObj.toString() : "---";
            
            txtFecha.setText(fechaStr);
            txtLote.setText(table.getValueAt(row, 2).toString());
            txtCultivo.setText(table.getValueAt(row, 3).toString());
            txtPlaga.setText(table.getValueAt(row, 4).toString());
            txtTecnico.setText(table.getValueAt(row, 5).toString());
            txtObservaciones.setText(table.getValueAt(row, 6).toString());
            
        } catch(Exception e) {
            System.out.println("Error mostrando detalles: " + e.getMessage());
        }
    }
    
    private void limpiarDetalles() {
        txtFecha.setText("");
        txtLote.setText("");
        txtCultivo.setText("");
        txtPlaga.setText("");
        txtTecnico.setText("");
        txtObservaciones.setText("Seleccione una inspección para ver los detalles completos.");
    }
    
    // ========================================================================
    // HELPERS DE DISEÑO
    // ========================================================================
    
    private JTextField crearCampoLectura(JPanel p, String lbl, int y) {
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 120, 65));
        l.setBounds(0, y, 280, 20);
        p.add(l);
        
        JTextField t = new JTextField();
        t.setBounds(0, y + 25, 320, 40);
        t.setEditable(false); // Importante: Solo lectura
        t.setBackground(new Color(240, 240, 240)); // Fondo grisáceo para indicar readonly
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        p.add(t);
        return t;
    }
    
    private void estilizarTabla(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(45);
        t.setSelectionBackground(new Color(230, 245, 230));
        t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(240, 240, 240));
        t.setShowVerticalLines(false);
        
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(240, 248, 240));
        t.getTableHeader().setForeground(PRIMARY_COLOR);
        t.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
    }
    
    private void updateDateTime() {
        String f = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String h = new SimpleDateFormat("HH:mm").format(new Date()) + " h";
        dateTimeLabel.setText("<html><div style='text-align: right;'>" + f + "<br>" + h + "</div></html>");
    }
}