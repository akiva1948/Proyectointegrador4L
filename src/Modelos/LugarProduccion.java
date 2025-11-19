package Modelos;

import javax.swing.JFrame;

public class LugarProduccion {
    private int idLugarProduccion;
    private String nombreLugar;
    private String telefono;
    private String correo;
    private int idMunicipio;
    private int idProductor;
    private JFrame parentMenu;

    public LugarProduccion() {}

    // Constructor actualizado
    public LugarProduccion(int idLugarProduccion, String nombreLugar, String telefono, String correo, int idMunicipio, int idProductor) {
        this.idLugarProduccion = idLugarProduccion;
        this.nombreLugar = nombreLugar;
        this.telefono = telefono;
        this.correo = correo;
        this.idMunicipio = idMunicipio;
        this.idProductor = idProductor;
    }

    // --- Getters y Setters ---

    public int getIdLugarProduccion() { return idLugarProduccion; }
    public void setIdLugarProduccion(int idLugarProduccion) { this.idLugarProduccion = idLugarProduccion; }

    public String getNombreLugar() { return nombreLugar; }
    public void setNombreLugar(String nombreLugar) { this.nombreLugar = nombreLugar; }

    // Getter/Setter de numRegistroIca ELIMINADOS

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public int getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(int idMunicipio) { this.idMunicipio = idMunicipio; }

    public int getIdProductor() { return idProductor; }
    public void setIdProductor(int idProductor) { this.idProductor = idProductor; }

    @Override
    public String toString() {
        return idLugarProduccion + " - " + nombreLugar;
    }
}