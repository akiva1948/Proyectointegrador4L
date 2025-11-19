package tecnico;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import DAO.InspeccionDAO;

public class informesTecnicoAd extends JFrame {

    private Connection conn;
    private JFrame parentMenu;
    private InspeccionDAO inspeccionDAO;
    private List<Object[]> datosReporte;
    
    // --- COLORES ---
    private final Color COLOR_VERDE = new Color(40, 110, 45);
    private final Color COLOR_VERDE_CLARO = new Color(60, 140, 65);
    private final Color COLOR_ROJO = new Color(180, 60, 60);

    public informesTecnicoAd(JFrame parentMenu, Connection conn) {
        this.parentMenu = parentMenu;
        this.conn = conn;
        this.inspeccionDAO = new InspeccionDAO(conn);

        initUI();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                parentMenu.setVisible(true);
            }
        });
    }

    private void initUI() {
        setTitle("Reportes ICA - Sistema Forest");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        // 1. HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 250, 245));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_VERDE));
        
        JLabel title = new JLabel("  REPORTE FITOSANITARIO (ICA)");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(COLOR_VERDE);
        header.add(title, BorderLayout.WEST);
        
        add(header, BorderLayout.NORTH);
        
        // 2. TABLA BLINDADA
        String[] columnNames = {"Cultivo", "Plaga", "Alertas ROJAS", "Alertas MEDIAS", "Alertas BAJAS", "Incidencia"};
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 248, 240));
                }
                return c;
            }
        };
        
        table.setRowHeight(35);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setShowGrid(true);
        
        // --- AQUÍ ESTÁ LA MAGIA: FORZAR EL COLOR DEL ENCABEZADO ---
        JTableHeader headerTable = table.getTableHeader();
        headerTable.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(COLOR_VERDE); // Color de fondo FORZADO
                setForeground(Color.WHITE); // Texto Blanco
                setFont(new Font("SansSerif", Font.BOLD, 14));
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createLineBorder(Color.WHITE));
                return this;
            }
        });
        
        // Centrar celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=2; i<6; i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scroll, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Llenar datos
        datosReporte = inspeccionDAO.obtenerResumenICA();
        for(Object[] fila : datosReporte) model.addRow(fila);
        
        // 3. FOOTER (Botones Blindados)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setBackground(Color.WHITE);
        
        // Botón Descargar
        JButton btnDescargar = new JButton("DESCARGAR TXT");
        estilizarBoton(btnDescargar, COLOR_VERDE_CLARO);
        btnDescargar.addActionListener(e -> descargarInformeTxt());
        
        // Botón Volver
        JButton btnCerrar = new JButton("VOLVER");
        estilizarBoton(btnCerrar, COLOR_ROJO);
        btnCerrar.addActionListener(e -> this.dispose());

        footer.add(btnDescargar);
        footer.add(btnCerrar);
        
        add(footer, BorderLayout.SOUTH);
    }
    
    // Función para blindar los botones contra Windows
    private void estilizarBoton(JButton btn, Color colorFondo) {
        btn.setBackground(colorFondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setFocusPainted(false);       // Quita el recuadro de foco
        btn.setBorderPainted(false);      // <--- ESTO ES CLAVE: Quita el borde 3D de Windows
        btn.setOpaque(true);              // Fuerza la opacidad
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ========================================================================
    // LOGICA DE REPORTE
    // ========================================================================

    private String generarTextoInforme() {
        StringBuilder informe = new StringBuilder();
        
        informe.append("==================================================================================\n");
        informe.append("                       REPORTE OFICIAL FITOSANITARIO (ICA)                        \n");
        informe.append("==================================================================================\n");
        informe.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        
        informe.append(String.format("%-20s %-25s %15s %15s %15s %15s\n", 
                "CULTIVO", "PLAGA DETECTADA", "ALERTA ROJA", "MEDIA", "BAJA", "INCIDENCIA"));
        
        informe.append("----------------------------------------------------------------------------------------------------------\n");

        for (Object[] fila : datosReporte) {
            informe.append(String.format("%-20s %-25s %15d %15d %15d %15s\n",
                    fila[0], fila[1], fila[2], fila[3], fila[4], fila[5]));
        }
        return informe.toString();
    }

    private void descargarInformeTxt() {
        if (datosReporte == null || datosReporte.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos."); return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Informe_ICA.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                bw.write(generarTextoInforme());
                JOptionPane.showMessageDialog(this, "Guardado exitosamente.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}