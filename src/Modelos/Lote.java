package Modelos;

import java.util.Date;
import javax.swing.JFrame;

public class Lote {
    private int idLote;
    private Date fechaSiembra;
    private String estado;
    private int idLugarProduccion; // Corregido (antes idPredio)
    private int idCultivo;
    private JFrame parentMenu;

    public Lote() {}

    // Constructor corregido
    public Lote(int idLote, Date fechaSiembra, String estado, int idLugarProduccion, int idCultivo) {
        this.idLote = idLote;
        this.fechaSiembra = fechaSiembra;
        this.estado = estado;
        this.idLugarProduccion = idLugarProduccion; // Corregido
        this.idCultivo = idCultivo;
    }

    // --- Getters y Setters ---

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public Date getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(Date fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Corregido
    public int getIdLugarProduccion() { return idLugarProduccion; }
    public void setIdLugarProduccion(int idLugarProduccion) { this.idLugarProduccion = idLugarProduccion; }

    public int getIdCultivo() { return idCultivo; }
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    @Override
    public String toString() {
        // Un toString() útil para el JComboBox (aunque no lo usamos, es buena práctica)
        return "Lote " + idLote + " (" + estado + ")";
    }
}