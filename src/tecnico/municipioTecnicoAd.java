package tecnico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Date;
import java.text.SimpleDateFormat;

import DAO.MunicipioDAO;
import Modelos.Municipio;

public class municipioTecnicoAd extends JFrame {

    private final Color PRIMARY_COLOR = new Color(40, 110, 45);
    private final Color HEADER_START = new Color(240, 248, 240);
    private final Color HEADER_END = new Color(230, 242, 230);

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel dateTimeLabel;
    private JTextField txtNombre;

    private Connection conn;
    private JFrame parentMenu;
    private MunicipioDAO municipioDAO;

    public municipioTecnicoAd(JFrame parentMenu, Connection conn) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.municipioDAO = new MunicipioDAO(conn);
        
        initializeUI();
        cargarDatos();
        new Timer(1000, e -> updateTime()).start();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { parentMenu.setVisible(true); }
        });
    }

    private void initializeUI() {
        setTitle("Consulta de Municipios");
        setSize(1366, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // HEADER
        JPanel top = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,HEADER_START,0,getHeight(),HEADER_END));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        top.setPreferredSize(new Dimension(0, 100));
        top.setLayout(null);
        
        JLabel title = new JLabel("Municipios Registrados");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(PRIMARY_COLOR);
        title.setBounds(40, 25, 500, 45);
        top.add(title);
        
        dateTimeLabel = new JLabel();
        dateTimeLabel.setBounds(1100, 30, 200, 30);
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateTimeLabel.setForeground(new Color(50, 110, 55));
        top.add(dateTimeLabel);
        add(top, BorderLayout.NORTH);

        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 248, 245));
        add(main, BorderLayout.CENTER);

        // LEFT PANEL
        JPanel left = new JPanel(null);
        left.setBounds(20, 20, 360, 620);
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        main.add(left);

        JLabel l1 = new JLabel("Detalle del Municipio");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l1.setForeground(PRIMARY_COLOR);
        l1.setBounds(20, 20, 300, 30);
        left.add(l1);

        crearLabel(left, "Nombre del Municipio:", 80);
        txtNombre = crearInput(left, 105);

        // --- BOTÓN VOLVER CORREGIDO ---
        JButton btnVolver = new JButton("VOLVER AL MENÚ");
        btnVolver.setBounds(20, 550, 320, 45);
        btnVolver.setBackground(new Color(180, 60, 60));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false); // <--- ESTO ARREGLA EL FONDO BLANCO
        btnVolver.setOpaque(true);         // <--- ESTO ASEGURA EL COLOR ROJO
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> this.dispose());
        left.add(btnVolver);

        // RIGHT PANEL
        JPanel right = new JPanel(new BorderLayout());
        right.setBounds(400, 20, 930, 620);
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        main.add(right);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre Municipio"}, 0){
             public boolean isCellEditable(int r, int c){return false;}
        };
        table = new JTable(tableModel);
        estilizarTabla(table);
        table.getSelectionModel().addListSelectionListener(e -> mostrarDetalle());
        
        right.add(new JScrollPane(table));
    }

    private void cargarDatos() {
        tableModel.setRowCount(0);
        for(Municipio m : municipioDAO.obtenerTodosMunicipios()) {
            tableModel.addRow(new Object[]{m.getIdMunicipio(), m.getNombreMunicipio()});
        }
    }

    private void mostrarDetalle() {
        int row = table.getSelectedRow();
        if(row != -1) {
            txtNombre.setText(table.getValueAt(row, 1).toString());
        }
    }

    // HELPERS
    private void crearLabel(JPanel p, String t, int y) {
        JLabel l = new JLabel(t);
        l.setBounds(20, y, 200, 20);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 120, 65));
        p.add(l);
    }
    private JTextField crearInput(JPanel p, int y) {
        JTextField t = new JTextField();
        t.setBounds(20, y, 320, 40);
        t.setEditable(false);
        t.setBackground(new Color(240, 240, 240));
        t.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        p.add(t);
        return t;
    }
    private void estilizarTabla(JTable t) {
        t.setRowHeight(35);
        t.getTableHeader().setBackground(new Color(240, 248, 240));
        t.getTableHeader().setForeground(PRIMARY_COLOR);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setShowVerticalLines(false);
    }
    private void updateTime(){
        dateTimeLabel.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
    }
}