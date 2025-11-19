package Modelos;

import javax.swing.JFrame;

public class Plaga {
    private int idPlaga;
    private String nombrePlaga;
    private String especie;
    private JFrame parentMenu;

    public Plaga() {}

    public Plaga(int idPlaga, String nombrePlaga, String especie) {
        this.idPlaga = idPlaga;
        this.nombrePlaga = nombrePlaga;
        this.especie = especie;
    }

    // --- Getters y Setters ---

    public int getIdPlaga() { return idPlaga; }
    public void setIdPlaga(int idPlaga) { this.idPlaga = idPlaga; }

    public String getNombrePlaga() { return nombrePlaga; }
    public void setNombrePlaga(String nombrePlaga) { this.nombrePlaga = nombrePlaga; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    @Override
    public String toString() {
        return idPlaga + " - " + nombrePlaga;
    }
}