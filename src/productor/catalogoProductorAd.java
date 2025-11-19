package productor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.sql.Connection;
import java.io.*; // Importante para guardar archivos
import java.time.LocalDateTime; // Para la fecha en el reporte
import java.time.format.DateTimeFormatter;

import DAO.CultivoDAO;
import DAO.PlagaDAO;
import Modelos.Cultivo;
import Modelos.Plaga;

public class catalogoProductorAd extends JFrame {
    
    private Connection conn;
    private JFrame parentMenu;
    
    // DAOs
    private CultivoDAO cultivoDAO;
    private PlagaDAO plagaDAO;
    
    // --- VARIABLES CULTIVOS ---
    private JTable tablaCultivos;
    private DefaultTableModel modeloCultivos;
    private JTextField txtEspecie, txtNombreC, txtVariedad, txtCiclo;
    private int idCultivoActual = 0;
    private JLabel totalCultivosLabel;

    // --- VARIABLES PLAGAS ---
    private JTable tablaPlagas;
    private DefaultTableModel modeloPlagas;
    private JTextField txtNombreP, txtEspecieP;
    private int idPlagaActual = 0;
    private JLabel totalPlagasLabel;

    public catalogoProductorAd(JFrame parentMenu, Connection conn) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        
        // Inicializamos DAOs
        cultivoDAO = new CultivoDAO(conn);
        plagaDAO = new PlagaDAO(conn);
        
        initializeUI();
        cargarCultivos();
        cargarPlagas();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parentMenu.setVisible(true);
            }
        });
    }

    private void initializeUI() {
        setTitle("Catálogos del Sistema (Cultivos y Plagas)");
        setSize(1366, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 245));

        // 1. HEADER SUPERIOR
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
        topPanel.setPreferredSize(new Dimension(0, 100));
        topPanel.setLayout(null);
        
        JLabel title = new JLabel("Catálogos de Producción");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(40, 15, 500, 45);
        title.setForeground(new Color(30, 100, 35));
        topPanel.add(title);
        
        JLabel subtitle = new JLabel("Gestione especies y genere informes oficiales");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setBounds(45, 60, 400, 25);
        subtitle.setForeground(new Color(70, 120, 75));
        topPanel.add(subtitle);
        
        // Botón Volver
        JButton btnVolver = new JButton("VOLVER AL MENÚ");
        btnVolver.setBounds(1150, 30, 160, 40);
        btnVolver.setBackground(new Color(180, 60, 60)); 
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setOpaque(true);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> this.dispose());
        topPanel.add(btnVolver);

        add(topPanel, BorderLayout.NORTH);

        // 2. PESTAÑAS (TABS)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFocusable(false);
        
        tabbedPane.addTab(" GESTIÓN DE CULTIVOS ", crearPanelCultivos());
        tabbedPane.addTab(" GESTIÓN DE PLAGAS ", crearPanelPlagas());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ========================================================================
    // PESTAÑA 1: CULTIVOS
    // ========================================================================
    private JPanel crearPanelCultivos() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 248, 245));

        // Formulario Izquierda
        JPanel leftPanel = crearPanelFormularioBase();
        JLabel lblTitle = new JLabel("Datos del Cultivo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 110, 45));
        lblTitle.setBounds(20, 20, 300, 30);
        leftPanel.add(lblTitle);

        int y = 70;
        txtEspecie = crearCampo(leftPanel, "Especie (Ej: Solanum)*", y); y+=70;
        txtNombreC = crearCampo(leftPanel, "Nombre Común*", y); y+=70;
        txtVariedad = crearCampo(leftPanel, "Variedad*", y); y+=70;
        txtCiclo = crearCampo(leftPanel, "Ciclo de Vida*", y); y+=80;

        JButton btnAdd = crearBoton("Guardar", new Color(60, 140, 65), 20, y);
        btnAdd.addActionListener(e -> guardarCultivo());
        leftPanel.add(btnAdd);

        JButton btnUpd = crearBoton("Actualizar", new Color(80, 160, 85), 170, y);
        btnUpd.addActionListener(e -> actualizarCultivo());
        leftPanel.add(btnUpd);
        
        JButton btnClear = crearBoton("Limpiar", new Color(100, 100, 100), 20, y+50);
        btnClear.addActionListener(e -> limpiarCultivo());
        leftPanel.add(btnClear);
        
        // --- NUEVO BOTÓN: GENERAR INFORME CULTIVOS ---
        JButton btnReporte = crearBoton("📄 Generar Informe", new Color(50, 100, 150), 170, y+50);
        btnReporte.addActionListener(e -> descargarInformeCultivos());
        leftPanel.add(btnReporte);

        panel.add(leftPanel);

        // Tabla Derecha
        JPanel rightPanel = crearPanelTablaBase("Listado de Cultivos");
        totalCultivosLabel = (JLabel) rightPanel.getClientProperty("totalLabel"); 
        
        JButton btnRef = (JButton) rightPanel.getClientProperty("refreshBtn");
        btnRef.addActionListener(e -> cargarCultivos());

        String[] cols = {"ID", "Especie", "Nombre", "Variedad", "Ciclo", "Acciones"};
        modeloCultivos = new DefaultTableModel(cols, 0) {
             public boolean isCellEditable(int r, int c) { return c == 5; }
             public Class<?> getColumnClass(int c) { return c == 5 ? JPanel.class : Object.class; }
        };
        tablaCultivos = new JTable(modeloCultivos);
        estilizarTabla(tablaCultivos);
        
        tablaCultivos.getColumnModel().getColumn(5).setCellRenderer(new AccionesRenderer());
        tablaCultivos.getColumnModel().getColumn(5).setCellEditor(new AccionesEditorCultivo());
        tablaCultivos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaCultivos.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scroll = new JScrollPane(tablaCultivos);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);
        rightPanel.add(scroll, BorderLayout.CENTER);

        panel.add(rightPanel);
        return panel;
    }

    // ========================================================================
    // PESTAÑA 2: PLAGAS
    // ========================================================================
    private JPanel crearPanelPlagas() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 248, 245));

        // Formulario
        JPanel leftPanel = crearPanelFormularioBase();
        JLabel lblTitle = new JLabel("Datos de la Plaga");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 110, 45));
        lblTitle.setBounds(20, 20, 300, 30);
        leftPanel.add(lblTitle);

        int y = 70;
        txtNombreP = crearCampo(leftPanel, "Nombre de la Plaga*", y); y+=70;
        txtEspecieP = crearCampo(leftPanel, "Especie Científica*", y); y+=80;

        JButton btnAdd = crearBoton("Guardar", new Color(60, 140, 65), 20, y);
        btnAdd.addActionListener(e -> guardarPlaga());
        leftPanel.add(btnAdd);

        JButton btnUpd = crearBoton("Actualizar", new Color(80, 160, 85), 170, y);
        btnUpd.addActionListener(e -> actualizarPlaga());
        leftPanel.add(btnUpd);
        
        JButton btnClear = crearBoton("Limpiar", new Color(100, 100, 100), 20, y+50);
        btnClear.addActionListener(e -> limpiarPlaga());
        leftPanel.add(btnClear);
        
        // --- NUEVO BOTÓN: GENERAR INFORME PLAGAS ---
        JButton btnReporte = crearBoton("📄 Generar Informe", new Color(50, 100, 150), 170, y+50);
        btnReporte.addActionListener(e -> descargarInformePlagas());
        leftPanel.add(btnReporte);

        panel.add(leftPanel);

        // Tabla
        JPanel rightPanel = crearPanelTablaBase("Listado de Plagas");
        totalPlagasLabel = (JLabel) rightPanel.getClientProperty("totalLabel");

        JButton btnRef = (JButton) rightPanel.getClientProperty("refreshBtn");
        btnRef.addActionListener(e -> cargarPlagas());

        String[] cols = {"ID", "Nombre Plaga", "Especie", "Acciones"};
        modeloPlagas = new DefaultTableModel(cols, 0) {
             public boolean isCellEditable(int r, int c) { return c == 3; }
             public Class<?> getColumnClass(int c) { return c == 3 ? JPanel.class : Object.class; }
        };
        tablaPlagas = new JTable(modeloPlagas);
        estilizarTabla(tablaPlagas);
        
        tablaPlagas.getColumnModel().getColumn(3).setCellRenderer(new AccionesRenderer());
        tablaPlagas.getColumnModel().getColumn(3).setCellEditor(new AccionesEditorPlaga());
        tablaPlagas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaPlagas.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(tablaPlagas);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);
        rightPanel.add(scroll, BorderLayout.CENTER);

        panel.add(rightPanel);
        return panel;
    }

    // ========================================================================
    // MÉTODOS CRUD (CULTIVOS)
    // ========================================================================
    private void cargarCultivos() {
        modeloCultivos.setRowCount(0);
        List<Cultivo> lista = cultivoDAO.obtenerTodosCultivos();
        for(Cultivo c : lista) {
            JPanel p = new JPanel(); p.setBackground(Color.WHITE);
            modeloCultivos.addRow(new Object[]{
                c.getIdCultivo(), c.getEspecie(), c.getNombres(), c.getVariedad(), c.getCiclo(), p
            });
        }
        totalCultivosLabel.setText("Total: " + lista.size());
    }
    private void guardarCultivo() {
        Cultivo c = obtenerCultivoForm();
        if(c != null && cultivoDAO.agregarCultivo(c)) {
            JOptionPane.showMessageDialog(this, "Cultivo Guardado"); cargarCultivos(); limpiarCultivo();
        }
    }
    private void actualizarCultivo() {
        if(idCultivoActual == 0) return;
        Cultivo c = obtenerCultivoForm();
        if(c != null) {
            c.setIdCultivo(idCultivoActual);
            if(cultivoDAO.actualizarCultivo(c)) {
                JOptionPane.showMessageDialog(this, "Actualizado"); cargarCultivos(); limpiarCultivo();
            }
        }
    }
    private void editarCultivoBtn(int id) {
        Cultivo c = cultivoDAO.obtenerCultivoPorId(id);
        if(c != null) {
            idCultivoActual = c.getIdCultivo();
            txtEspecie.setText(c.getEspecie());
            txtNombreC.setText(c.getNombres());
            txtVariedad.setText(c.getVariedad());
            txtCiclo.setText(c.getCiclo());
        }
    }
    private void eliminarCultivoBtn(int id) {
        if(JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION)==0) {
            if(cultivoDAO.eliminarCultivo(id)) {
                cargarCultivos(); limpiarCultivo();
            }
        }
    }
    private Cultivo obtenerCultivoForm() {
        String e = txtEspecie.getText().trim();
        String n = txtNombreC.getText().trim();
        String v = txtVariedad.getText().trim();
        String c = txtCiclo.getText().trim();
        if(n.isEmpty()) { JOptionPane.showMessageDialog(this, "Faltan datos"); return null; }
        return new Cultivo(0, e, n, v, c);
    }
    private void limpiarCultivo() {
        idCultivoActual = 0;
        txtEspecie.setText(""); txtNombreC.setText(""); txtVariedad.setText(""); txtCiclo.setText("");
    }

    // ========================================================================
    // NUEVO: GENERAR INFORME CULTIVOS
    // ========================================================================
    private void descargarInformeCultivos() {
        List<Cultivo> lista = cultivoDAO.obtenerTodosCultivos();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para descargar.");
            return;
        }

        // Construcción del texto del informe
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        sb.append("========================================================================\n");
        sb.append("                REPORTE OFICIAL DE CULTIVOS REGISTRADOS\n");
        sb.append("========================================================================\n");
        sb.append("Fecha de Generación: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("Total Registros: ").append(lista.size()).append("\n\n");
        sb.append(String.format("%-5s %-20s %-20s %-20s %-20s\n", "ID", "ESPECIE", "NOMBRE", "VARIEDAD", "CICLO"));
        sb.append("------------------------------------------------------------------------\n");

        for (Cultivo c : lista) {
            sb.append(String.format("%-5d %-20s %-20s %-20s %-20s\n", 
                c.getIdCultivo(), c.getEspecie(), c.getNombres(), c.getVariedad(), c.getCiclo()));
        }
        sb.append("------------------------------------------------------------------------\n");
        sb.append("Fin del reporte.\n");

        // Guardar archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe de Cultivos");
        fileChooser.setSelectedFile(new File("Informe_Cultivos.txt"));

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

    // ========================================================================
    // MÉTODOS CRUD (PLAGAS)
    // ========================================================================
    private void cargarPlagas() {
        modeloPlagas.setRowCount(0);
        List<Plaga> lista = plagaDAO.obtenerTodasPlagas();
        for(Plaga p : lista) {
            JPanel pa = new JPanel(); pa.setBackground(Color.WHITE);
            modeloPlagas.addRow(new Object[]{ p.getIdPlaga(), p.getNombrePlaga(), p.getEspecie(), pa });
        }
        totalPlagasLabel.setText("Total: " + lista.size());
    }
    private void guardarPlaga() {
        Plaga p = obtenerPlagaForm();
        if(p != null && plagaDAO.agregarPlaga(p)) {
            JOptionPane.showMessageDialog(this, "Guardado"); cargarPlagas(); limpiarPlaga();
        }
    }
    private void actualizarPlaga() {
        if(idPlagaActual == 0) return;
        Plaga p = obtenerPlagaForm();
        if(p != null) {
            p.setIdPlaga(idPlagaActual);
            if(plagaDAO.actualizarPlaga(p)) {
                JOptionPane.showMessageDialog(this, "Actualizado"); cargarPlagas(); limpiarPlaga();
            }
        }
    }
    private void editarPlagaBtn(int id) {
        Plaga p = plagaDAO.obtenerPlagaPorId(id);
        if(p != null) {
            idPlagaActual = p.getIdPlaga();
            txtNombreP.setText(p.getNombrePlaga());
            txtEspecieP.setText(p.getEspecie());
        }
    }
    private void eliminarPlagaBtn(int id) {
        if(JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION)==0) {
            if(plagaDAO.eliminarPlaga(id)) { cargarPlagas(); limpiarPlaga(); }
        }
    }
    private Plaga obtenerPlagaForm() {
        String n = txtNombreP.getText().trim();
        String e = txtEspecieP.getText().trim();
        if(n.isEmpty()) { JOptionPane.showMessageDialog(this, "Faltan datos"); return null; }
        return new Plaga(0, n, e);
    }
    private void limpiarPlaga() {
        idPlagaActual = 0; txtNombreP.setText(""); txtEspecieP.setText("");
    }

    // ========================================================================
    // NUEVO: GENERAR INFORME PLAGAS
    // ========================================================================
    private void descargarInformePlagas() {
        List<Plaga> lista = plagaDAO.obtenerTodasPlagas();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para descargar.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        sb.append("========================================================================\n");
        sb.append("                REPORTE OFICIAL DE PLAGAS REGISTRADAS\n");
        sb.append("========================================================================\n");
        sb.append("Fecha de Generación: ").append(dtf.format(LocalDateTime.now())).append("\n");
        sb.append("Total Registros: ").append(lista.size()).append("\n\n");
        sb.append(String.format("%-5s %-30s %-30s\n", "ID", "NOMBRE COMÚN", "NOMBRE CIENTÍFICO/ESPECIE"));
        sb.append("------------------------------------------------------------------------\n");

        for (Plaga p : lista) {
            sb.append(String.format("%-5d %-30s %-30s\n", p.getIdPlaga(), p.getNombrePlaga(), p.getEspecie()));
        }
        sb.append("------------------------------------------------------------------------\n");
        sb.append("Fin del reporte.\n");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe de Plagas");
        fileChooser.setSelectedFile(new File("Informe_Plagas.txt"));

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

    // ========================================================================
    // COMPONENTES BASE
    // ========================================================================
    
    private JPanel crearPanelFormularioBase() {
        JPanel p = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(250, 252, 250));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setBounds(20, 20, 340, 580);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return p;
    }
    
    private JPanel crearPanelTablaBase(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBounds(380, 20, 940, 580);
        p.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200), 1));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 248, 245));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblT.setForeground(new Color(40, 110, 45));
        
        JPanel rightH = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightH.setBackground(new Color(245, 248, 245));
        
        JButton refresh = new JButton("↻");
        refresh.setFont(new Font("SansSerif", Font.BOLD, 24));
        refresh.setForeground(new Color(60, 140, 65));
        refresh.setBorderPainted(false);
        refresh.setContentAreaFilled(false);
        refresh.setFocusPainted(false);
        refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.putClientProperty("refreshBtn", refresh);
        
        JLabel total = new JLabel("Total: 0");
        total.setFont(new Font("Segoe UI", Font.BOLD, 14));
        total.setForeground(new Color(80, 130, 85));
        p.putClientProperty("totalLabel", total);
        
        rightH.add(refresh);
        rightH.add(total);
        header.add(lblT, BorderLayout.WEST);
        header.add(rightH, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);
        return p;
    }

    // ========================================================================
    // ESTILOS Y RENDERERS
    // ========================================================================
    
    private JTextField crearCampo(JPanel p, String lbl, int y) {
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 120, 65));
        l.setBounds(20, y, 280, 20);
        p.add(l);
        JTextField t = new JTextField();
        t.setBounds(20, y + 25, 300, 40);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 180)),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(t);
        return t;
    }
    
    private JButton crearBoton(String t, Color c, int x, int y) {
        JButton b = new JButton(t);
        b.setBounds(x, y, 140, 40);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(c.darker()); }
            public void mouseExited(MouseEvent e) { b.setBackground(c); }
        });
        return b;
    }
    
    private void estilizarTabla(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(45);
        t.setSelectionBackground(new Color(230, 245, 230));
        t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(240, 240, 240));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(240, 248, 240));
        t.getTableHeader().setForeground(new Color(40, 110, 45));
        t.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
    }

    class AccionesRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            p.setBackground(s ? new Color(230, 245, 230) : Color.WHITE);
            JButton b1 = crearBtnTabla("Editar", new Color(74, 144, 226));
            JButton b2 = crearBtnTabla("X", new Color(231, 76, 60));
            p.add(b1); p.add(b2);
            return p;
        }
    }
    
    abstract class BaseEditor extends DefaultCellEditor {
        public BaseEditor() { super(new JCheckBox()); setClickCountToStart(1); }
        protected JPanel getPanel(JTable t, boolean s, int r, ActionListener editAct, ActionListener delAct) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            p.setBackground(s ? new Color(230, 245, 230) : Color.WHITE);
            JButton b1 = crearBtnTabla("Editar", new Color(74, 144, 226));
            b1.addActionListener(editAct);
            JButton b2 = crearBtnTabla("X", new Color(231, 76, 60));
            b2.addActionListener(delAct);
            p.add(b1); p.add(b2);
            return p;
        }
    }

    class AccionesEditorCultivo extends BaseEditor {
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            return getPanel(t, s, r, 
                e -> { fireEditingStopped(); editarCultivoBtn((int)modeloCultivos.getValueAt(r, 0)); },
                e -> { fireEditingStopped(); eliminarCultivoBtn((int)modeloCultivos.getValueAt(r, 0)); }
            );
        }
        public Object getCellEditorValue() { return ""; }
    }
    
    class AccionesEditorPlaga extends BaseEditor {
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            return getPanel(t, s, r, 
                e -> { fireEditingStopped(); editarPlagaBtn((int)modeloPlagas.getValueAt(r, 0)); },
                e -> { fireEditingStopped(); eliminarPlagaBtn((int)modeloPlagas.getValueAt(r, 0)); }
            );
        }
        public Object getCellEditorValue() { return ""; }
    }
    
    private JButton crearBtnTabla(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true); 
        b.setContentAreaFilled(true); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if(txt.equals("X")) b.setMargin(new Insets(2, 8, 2, 8));
        return b;
    }
}