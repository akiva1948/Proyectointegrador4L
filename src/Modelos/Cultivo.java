package Modelos;

import javax.swing.JFrame;

public class Cultivo {
    private int idCultivo;
    private String especie;
    private String nombres;
    private String variedad;
    private String ciclo;
    private JFrame parentMenu;

    public Cultivo() {}

    public Cultivo(int idCultivo, String especie, String nombres, String variedad, String ciclo) {
        this.idCultivo = idCultivo;
        this.especie = especie;
        this.nombres = nombres;
        this.variedad = variedad;
        this.ciclo = ciclo;
    }

    // --- Getters y Setters ---

    public int getIdCultivo() { return idCultivo; }
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public String getCiclo() { return ciclo; }
    public void setCiclo(String ciclo) { this.ciclo = ciclo; }

    @Override
    public String toString() {
        return idCultivo + " - " + nombres + " (" + especie + ")";
    }
}