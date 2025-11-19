package Modelos;

import javax.swing.JFrame;

public class Municipio {
    private int idMunicipio;
    private String nombreMunicipio;
    private JFrame parentMenu;

    public Municipio() {}

    public Municipio(int idMunicipio, String nombreMunicipio) {
        this.idMunicipio = idMunicipio;
        this.nombreMunicipio = nombreMunicipio;
    }

    // --- Getters y Setters ---

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombreMunicipio() {
        return nombreMunicipio;
    }

    public void setNombreMunicipio(String nombreMunicipio) {
        this.nombreMunicipio = nombreMunicipio;
    }

    @Override
    public String toString() {
        return idMunicipio + " - " + nombreMunicipio;
    }
}