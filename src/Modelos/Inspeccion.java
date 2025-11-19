package Modelos;

import java.util.Date;

public class Inspeccion {
    private int idInspeccion;
    private Date fechaInspeccion;
    private String observaciones;
    private int idLote;
    private int idTecnico;
    private int idPlaga;
    
    // --- NUEVOS CAMPOS ---
    private int cantidadPlantas;
    private int plantasAfectadas;
    private double nivelIncidencia; // Calculado por Oracle
    private String nivelAlerta;     // Calculado por Oracle

    public Inspeccion() {}

    // Constructor COMPLETO
    public Inspeccion(int id, Date fecha, String obs, int idLote, int idTec, int idPlaga, 
                      int cant, int afect, double inc, String alerta) {
        this.idInspeccion = id;
        this.fechaInspeccion = fecha;
        this.observaciones = obs;
        this.idLote = idLote;
        this.idTecnico = idTec;
        this.idPlaga = idPlaga;
        this.cantidadPlantas = cant;
        this.plantasAfectadas = afect;
        this.nivelIncidencia = inc;
        this.nivelAlerta = alerta;
    }

    // Constructor para GUARDAR (Sin calculados, porque Oracle lo hace)
    public Inspeccion(int id, Date fecha, String obs, int idLote, int idTec, int idPlaga, 
                      int cant, int afect) {
        this(id, fecha, obs, idLote, idTec, idPlaga, cant, afect, 0.0, "");
    }

    // Getters y Setters
    public int getIdInspeccion() { return idInspeccion; }
    public void setIdInspeccion(int idInspeccion) { this.idInspeccion = idInspeccion; }
    public Date getFechaInspeccion() { return fechaInspeccion; }
    public void setFechaInspeccion(Date fechaInspeccion) { this.fechaInspeccion = fechaInspeccion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }
    public int getIdTecnico() { return idTecnico; }
    public void setIdTecnico(int idTecnico) { this.idTecnico = idTecnico; }
    public int getIdPlaga() { return idPlaga; }
    public void setIdPlaga(int idPlaga) { this.idPlaga = idPlaga; }
    
    // Nuevos Getters/Setters
    public int getCantidadPlantas() { return cantidadPlantas; }
    public void setCantidadPlantas(int cantidadPlantas) { this.cantidadPlantas = cantidadPlantas; }
    public int getPlantasAfectadas() { return plantasAfectadas; }
    public void setPlantasAfectadas(int plantasAfectadas) { this.plantasAfectadas = plantasAfectadas; }
    public double getNivelIncidencia() { return nivelIncidencia; }
    public void setNivelIncidencia(double nivelIncidencia) { this.nivelIncidencia = nivelIncidencia; }
    public String getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }
}