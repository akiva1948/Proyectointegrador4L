package Modelos;

import javax.swing.JFrame;

public class Productor {
    private int idProductor;
    private String nombreProductor;
    private String cedulaCiudadania;
    private String telefono;
    private String correo;
    private String contrasena;
    private JFrame parentMenu;

    public Productor() {}

    public Productor(int idProductor, String nombreProductor, String cedulaCiudadania, String telefono, String correo, String contrasena) {
        this.idProductor = idProductor;
        this.nombreProductor = nombreProductor;
        this.cedulaCiudadania = cedulaCiudadania;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    // --- Getters y Setters ---

    public int getIdProductor() { return idProductor; }
    public void setIdProductor(int idProductor) { this.idProductor = idProductor; }

    public String getNombreProductor() { return nombreProductor; }
    public void setNombreProductor(String nombreProductor) { this.nombreProductor = nombreProductor; }

    public String getCedulaCiudadania() { return cedulaCiudadania; }
    public void setCedulaCiudadania(String cedulaCiudadania) { this.cedulaCiudadania = cedulaCiudadania; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    @Override
    public String toString() {
        // No incluir la contraseña en el toString por seguridad
        return idProductor + " - " + nombreProductor;
    }
}