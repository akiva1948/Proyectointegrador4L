package productor;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.event.*;
import java.sql.Connection;
import com.toedter.calendar.JDateChooser;
import DAO.LoteDAO;
import DAO.ProductorDAO;
import DTO.InformeProduccionDTO;
public class informeProductorAd extends JFrame {
    
    // --- COLORES ---
    private final Color PRIMARY_COLOR = new Color(40, 110, 45);    
    private final Color BTN_COLOR = new Color(50, 130, 55);     
    private final Color BG_COLOR = new Color(245, 250, 245); 
    private final Color RED_COLOR = new Color(180, 60, 60); // Color para el botón Salir
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalRegistrosLabel;
    
    // Formulario
    private JComboBox<String> loteSelectorCombo;
    private JTextField cantidadField;
    private JDateChooser fechaChooser;
    
    private Connection conn;
    private JFrame parentMenu;
    private String correoProductor;
    private int idProductorLogueado;
    private LoteDAO loteDAO;
    private ProductorDAO productorDAO;
    
    private List<InformeProduccionDTO> listaLotesCompleta;

    public informeProductorAd(JFrame parentMenu, Connection conn, String correoProductor) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.correoProductor = correoProductor;
        
        this.loteDAO = new LoteDAO(conn);
        this.productorDAO = new ProductorDAO(conn);
        this.idProductorLogueado = productorDAO.obtenerIdPorCorreo(correoProductor);
        
        setTitle("Registro de Producción");
        setSize(1150, 680); // Un poquito más alto para que quepa el botón
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        initializeUI();
        cargarDatos();
        
        // Al cerrar (por botón o X), mostramos el menú
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { parentMenu.setVisible(true); }
        });
    }

    private void initializeUI() {
        // =================================================================================
        // 1. ENCABEZADO (HEADER)
        // =================================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JLabel titleLabel = new JLabel("Mis Cosechas");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("Usuario: " + correoProductor);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // =================================================================================
        // 2. CONTENEDOR PRINCIPAL
        // =================================================================================
        JPanel mainContainer = new JPanel(new BorderLayout(20, 0));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- A. BARRA LATERAL (FORMULARIO) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("Nueva Cosecha");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(PRIMARY_COLOR);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(formTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(crearSeparador());
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // 1. LOTE
        sidebar.add(crearLabel("Lote a cosechar:"));
        loteSelectorCombo = new JComboBox<>();
        estilizarCombo(loteSelectorCombo);
        sidebar.add(loteSelectorCombo);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. CANTIDAD
        sidebar.add(crearLabel("Cantidad (Kg):"));
        cantidadField = new JTextField();
        estilizarCampo(cantidadField);
        sidebar.add(cantidadField);

        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. FECHA (JCALENDAR)
        sidebar.add(crearLabel("Fecha de Recolección:"));
        fechaChooser = new JDateChooser(new Date()); 
        estilizarDateChooser(fechaChooser);
        sidebar.add(fechaChooser);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTONES DE ACCIÓN ---
        JButton btnGuardar = crearBoton("GUARDAR DATOS", BTN_COLOR, Color.WHITE);
        btnGuardar.addActionListener(e -> guardarInforme());
        
        JButton btnLimpiar = crearBoton("LIMPIAR", new Color(220, 220, 220), Color.BLACK);
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        sidebar.add(btnGuardar);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnLimpiar);
        
        // --- BOTÓN SALIR (NUEVO) ---
        sidebar.add(Box.createVerticalGlue()); // Empuja el botón salir al final
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton btnSalir = crearBoton("VOLVER AL MENÚ", RED_COLOR, Color.WHITE);
        btnSalir.addActionListener(e -> this.dispose()); // Cierra y activa el windowClosed
        sidebar.add(btnSalir);

        // --- B. TABLA (CENTRO) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // Header Tabla
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(Color.WHITE);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel tableTitle = new JLabel("Historial Registrado");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(new Color(80, 80, 80));
        
        totalRegistrosLabel = new JLabel("0 Registros");
        totalRegistrosLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalRegistrosLabel.setForeground(BTN_COLOR);
        
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(totalRegistrosLabel, BorderLayout.EAST);
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);

        // JTable
        String[] cols = {"ID", "Cultivo", "Variedad", "Lugar", "Siembra", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        estilizarTabla(table);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);
        
        tablePanel.add(scroll, BorderLayout.CENTER);

        // Agregar al container
        mainContainer.add(sidebar, BorderLayout.WEST);
        mainContainer.add(tablePanel, BorderLayout.CENTER);
        
        add(mainContainer, BorderLayout.CENTER);
    }

    // =================================================================================
    // LÓGICA
    // =================================================================================
    
    private void cargarDatos() {
        tableModel.setRowCount(0);
        listaLotesCompleta = loteDAO.obtenerInformePorProductor(idProductorLogueado);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        int count = 0;
        loteSelectorCombo.removeAllItems();
        loteSelectorCombo.addItem("- Seleccionar -");
        
        for (InformeProduccionDTO item : listaLotesCompleta) {
            String fecha = (item.getFechaSiembra() != null) ? sdf.format(item.getFechaSiembra()) : "-";
            
            tableModel.addRow(new Object[]{
                item.getIdLote(),
                item.getNombreCultivo(),
                item.getVariedad(),
                item.getNombreLugar(),
                fecha,
                item.getReporteCosecha()
            });
            count++;
            
            if (!item.getReporteCosecha().contains("COSECHADO")) {
                loteSelectorCombo.addItem(item.getIdLote() + " - " + item.getNombreCultivo());
            }
        }
        totalRegistrosLabel.setText(count + " Registros");
    }
    
    private void guardarInforme() {
        if (loteSelectorCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un lote válido.");
            return;
        }
        String cant = cantidadField.getText().trim();
        if (cant.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe la cantidad obtenida.");
            return;
        }
        Date fechaDate = fechaChooser.getDate();
        if (fechaDate == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una fecha válida.");
            return;
        }
        
        String sel = (String) loteSelectorCombo.getSelectedItem();
        int idLote = Integer.parseInt(sel.split(" - ")[0]);
        
        String fechaStr = new SimpleDateFormat("dd/MM/yyyy").format(fechaDate);
        
        if(loteDAO.registrarProduccionSinTablas(idLote, cant, fechaStr)) {
            JOptionPane.showMessageDialog(this, "¡Producción registrada correctamente!");
            cargarDatos();
            limpiarFormulario();
        }
    }
    
    private void limpiarFormulario() {
        loteSelectorCombo.setSelectedIndex(0);
        cantidadField.setText("");
        fechaChooser.setDate(new Date());
    }

    // =================================================================================
    // ESTILOS
    // =================================================================================

    private JLabel crearLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void estilizarCampo(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    private void estilizarCombo(JComboBox box) {
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        box.setPreferredSize(new Dimension(200, 35));
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    private void estilizarDateChooser(JDateChooser chooser) {
        chooser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        chooser.setPreferredSize(new Dimension(200, 35));
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chooser.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton crearBoton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 220));
        return sep;
    }

    private void estilizarTabla(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(230, 245, 230)); 
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35));
        header.setBackground(Color.WHITE);
        header.setForeground(PRIMARY_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
    }
}