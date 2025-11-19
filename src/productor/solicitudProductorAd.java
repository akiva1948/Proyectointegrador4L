package productor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.event.*;
import java.sql.Connection;
import com.toedter.calendar.JDateChooser; 

import DAO.LoteDAO;
import DAO.ProductorDAO;
import DAO.LugarProduccionDAO;
import DAO.CultivoDAO;

import Modelos.Lote;
import Modelos.LugarProduccion;
import Modelos.Cultivo;

public class solicitudProductorAd extends JFrame {
    
    private final Color PRIMARY_COLOR = new Color(40, 110, 45);
    private final Color HEADER_START = new Color(240, 248, 240);
    private final Color HEADER_END = new Color(230, 242, 230);
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel dateTimeLabel;
    
    // Formulario
    private JComboBox<ComboItem> loteCombo; 
    private JDateChooser fechaVisitaChooser;
    
    private Connection conn;
    private JFrame parentMenu;
    private String correoProductor;
    private int idProductorLogueado;
    
    private LoteDAO loteDAO;
    private ProductorDAO productorDAO;
    private LugarProduccionDAO lugarDAO;
    private CultivoDAO cultivoDAO;

    public solicitudProductorAd(JFrame parentMenu, Connection conn, String correoProductor) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.correoProductor = correoProductor;
        
        this.loteDAO = new LoteDAO(conn);
        this.productorDAO = new ProductorDAO(conn);
        this.lugarDAO = new LugarProduccionDAO(conn);
        this.cultivoDAO = new CultivoDAO(conn);
        
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        initializeUI();
        cargarDatosCombo(); 
        cargarTablaSolicitudes();
        
        new Timer(1000, e -> updateTime()).start();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { parentMenu.setVisible(true); }
        });
    }

    private void initializeUI() {
        setTitle("Solicitar Visita Técnica");
        setSize(1100, 650);
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
        top.setPreferredSize(new Dimension(0, 90));
        top.setLayout(null);
        
        JLabel t = new JLabel("Solicitud de Inspección");
        t.setFont(new Font("Segoe UI", Font.BOLD, 28));
        t.setForeground(PRIMARY_COLOR);
        t.setBounds(30, 20, 400, 40);
        top.add(t);
        
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateTimeLabel.setForeground(new Color(50, 110, 55));
        dateTimeLabel.setBounds(850, 30, 200, 20);
        top.add(dateTimeLabel);
        
        add(top, BorderLayout.NORTH);

        // MAIN
        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 250, 245));
        add(main, BorderLayout.CENTER);

        // --- PANEL IZQUIERDO (FORMULARIO) ---
        JPanel left = new JPanel(null);
        left.setBounds(20, 20, 350, 480);
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
        main.add(left);
        
        JLabel l1 = new JLabel("Nueva Solicitud");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l1.setForeground(PRIMARY_COLOR);
        l1.setBounds(20, 20, 200, 30);
        left.add(l1);
        
        int y = 70;
        left.add(crearLabel("Seleccione Lote:", y));
        
        loteCombo = new JComboBox<>();
        loteCombo.setBounds(20, y+25, 310, 35);
        loteCombo.setBackground(Color.WHITE);
        left.add(loteCombo);
        
        y += 80;
        left.add(crearLabel("Fecha Sugerida:", y));
        fechaVisitaChooser = new JDateChooser();
        fechaVisitaChooser.setBounds(20, y+25, 310, 35);
        fechaVisitaChooser.setDate(new Date());
        left.add(fechaVisitaChooser);
        
        y += 100;
        
        // --- BOTÓN ENVIAR (CORREGIDO) ---
        JButton btnEnviar = new JButton("ENVIAR SOLICITUD");
        btnEnviar.setBounds(20, y, 310, 45);
        estilizarBoton(btnEnviar, new Color(60, 140, 65)); // Verde
        btnEnviar.addActionListener(e -> guardarSolicitud());
        left.add(btnEnviar);
        
        // --- BOTÓN VOLVER (CORREGIDO) ---
        JButton btnVolver = new JButton("VOLVER");
        btnVolver.setBounds(20, y+60, 310, 45);
        estilizarBoton(btnVolver, new Color(180, 60, 60)); // Rojo
        btnVolver.addActionListener(e -> this.dispose());
        left.add(btnVolver);

        // --- PANEL DERECHO (TABLA) ---
        JPanel right = new JPanel(new BorderLayout());
        right.setBounds(390, 20, 670, 480);
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));
        main.add(right);
        
        JLabel l2 = new JLabel("  Historial de Solicitudes");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l2.setForeground(PRIMARY_COLOR);
        l2.setPreferredSize(new Dimension(0, 50));
        right.add(l2, BorderLayout.NORTH);
        
        tableModel = new DefaultTableModel(new String[]{"ID Lote", "Cultivo", "Finca", "Estado Actual"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        estilizarTabla(table);
        
        right.add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    // --- HELPER PARA BOTONES (LA SOLUCIÓN AL COLOR BLANCO) ---
    private void estilizarBoton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // <--- ESTO QUITA EL BORDE BLANCO
        btn.setOpaque(true);         // <--- ESTO FUERZA EL COLOR
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void estilizarTabla(JTable t) {
        t.setRowHeight(30);
        t.getTableHeader().setBackground(new Color(240, 248, 240));
        t.getTableHeader().setForeground(PRIMARY_COLOR);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(230, 230, 230));
        t.setSelectionBackground(new Color(220, 240, 220));
        t.setSelectionForeground(Color.BLACK);
    }

    // --- LÓGICA DE DATOS ---
    private void cargarDatosCombo() {
        loteCombo.removeAllItems();
        
        List<LugarProduccion> misLugares = new ArrayList<>();
        for(LugarProduccion l : lugarDAO.obtenerTodosLugares()) {
            if(l.getIdProductor() == idProductorLogueado) {
                misLugares.add(l);
            }
        }
        
        if(misLugares.isEmpty()) return;

        for(Lote lote : loteDAO.obtenerTodosLotes()) {
            for(LugarProduccion miLugar : misLugares) {
                if(lote.getIdLugarProduccion() == miLugar.getIdLugarProduccion()) {
                    
                    String nombreCultivo = "Desconocido";
                    for(Cultivo c : cultivoDAO.obtenerTodosCultivos()) {
                        if(c.getIdCultivo() == lote.getIdCultivo()) {
                            nombreCultivo = c.getNombres();
                            break;
                        }
                    }
                    String etiqueta = "Lote #" + lote.getIdLote() + " - " + nombreCultivo + " (" + miLugar.getNombreLugar() + ")";
                    loteCombo.addItem(new ComboItem(lote.getIdLote(), etiqueta));
                    break; 
                }
            }
        }
    }
    
    private void cargarTablaSolicitudes() {
        tableModel.setRowCount(0);
        List<String[]> solicitudes = loteDAO.obtenerMisSolicitudes(idProductorLogueado);
        for(String[] fila : solicitudes) {
            tableModel.addRow(fila);
        }
    }
    
    private void guardarSolicitud() {
        if (loteCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No tienes lotes registrados para solicitar visita.");
            return;
        }

        ComboItem item = (ComboItem) loteCombo.getSelectedItem();
        
        if (item == null || item.getId() == 0) {
             JOptionPane.showMessageDialog(this, "Selección inválida.");
             return;
        }
        
        Date fecha = fechaVisitaChooser.getDate();
        if (fecha == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una fecha.");
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fechaStr = sdf.format(fecha);
        
        if (loteDAO.crearSolicitudVisita(item.getId(), fechaStr)) {
            JOptionPane.showMessageDialog(this, "Solicitud enviada correctamente.");
            cargarTablaSolicitudes();
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar solicitud.");
        }
    }
    
    private JLabel crearLabel(String t, int y) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 120, 65));
        l.setBounds(20, y, 200, 20);
        return l;
    }
    
    private void updateTime() {
        dateTimeLabel.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
    }
    
    class ComboItem {
        private int id;
        private String label;
        
        public ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }
        
        public int getId() { return id; }
        
        @Override
        public String toString() {
            return label;
        }
    }
}