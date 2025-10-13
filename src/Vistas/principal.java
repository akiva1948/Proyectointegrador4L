package Vistas;

import javax.swing.*;
import java.awt.*;

public class principal extends JFrame {

    private JPanel panelActual;

    public principal() {
        setTitle("Sistema de Inspección Fitosanitaria");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(430, 400); // tamaño fijo
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); // 👈 vuelve a esto
        mostrarPanel(new Inicio(this));
    }

    public void mostrarPanel(JPanel nuevo) {
        if (panelActual != null) remove(panelActual);
        panelActual = nuevo;
        add(panelActual, BorderLayout.CENTER);
        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new principal().setVisible(true));
    }
}

