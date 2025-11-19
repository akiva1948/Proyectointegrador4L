package Modelos;

import java.math.BigDecimal;
import javax.swing.JFrame;

public class Predio {
    private int idPredio;
    private String ubicacionMun;
    private BigDecimal extensionHectareas; // Usamos BigDecimal para DECIMAL(10,2)
    private int idLugarProduccion;
    private JFrame parentMenu;

    public Predio() {}

    // Constructor actualizado
    public Predio(int idPredio, String ubicacionMun, BigDecimal extensionHectareas, int idLugarProduccion) {
        this.idPredio = idPredio;
        this.ubicacionMun = ubicacionMun;
        this.extensionHectareas = extensionHectareas;
        this.idLugarProduccion = idLugarProduccion;
    }

    // --- Getters y Setters ---

    public int getIdPredio() { return idPredio; }
    public void setIdPredio(int idPredio) { this.idPredio = idPredio; }

    public String getUbicacionMun() { return ubicacionMun; }
    public void setUbicacionMun(String ubicacionMun) { this.ubicacionMun = ubicacionMun; }

    public BigDecimal getExtensionHectareas() { return extensionHectareas; }
    public void setExtensionHectareas(BigDecimal extensionHectareas) { this.extensionHectareas = extensionHectareas; }

    public int getIdLugarProduccion() { return idLugarProduccion; }
    public void setIdLugarProduccion(int idLugarProduccion) { this.idLugarProduccion = idLugarProduccion; }

    @Override
    public String toString() {
        return idPredio + " - " + ubicacionMun;
    }
}