package Modelos;

import javax.swing.JFrame;

public class Tecnico {
    private int id;
    private String numDocumento;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String contrasena;
    private JFrame parentMenu;

    public Tecnico() {}

    // Constructor actualizado
    public Tecnico(int id, String numDocumento, String nombre, String apellido, String telefono, String correo, String contrasena) {
        this.id = id;
        this.numDocumento = numDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumDocumento() { return numDocumento; }
    public void setNumDocumento(String numDocumento) { this.numDocumento = numDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    @Override
    public String toString() {
        return id + " - " + nombre + " " + apellido;
    }
}