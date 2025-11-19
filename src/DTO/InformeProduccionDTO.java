package DTO;

import java.util.Date;

public class InformeProduccionDTO {
    private int idLote;
    private String nombreCultivo;
    private String variedad;
    private String nombreLugar;
    private Date fechaSiembra;
    private String reporteCosecha;

    public InformeProduccionDTO(int idLote, String nombreCultivo, String variedad, String nombreLugar, Date fechaSiembra, String reporteCosecha) {
        this.idLote = idLote;
        this.nombreCultivo = nombreCultivo;
        this.variedad = variedad;
        this.nombreLugar = nombreLugar;
        this.fechaSiembra = fechaSiembra;
        this.reporteCosecha = reporteCosecha;
    }

    // Getters
    public int getIdLote() { return idLote; }
    public String getNombreCultivo() { return nombreCultivo; }
    public String getVariedad() { return variedad; }
    public String getNombreLugar() { return nombreLugar; }
    public Date getFechaSiembra() { return fechaSiembra; }
    public String getReporteCosecha() { return reporteCosecha; }
    
    @Override
    public String toString() {
        return nombreCultivo + " - " + variedad + " (" + nombreLugar + ")";
    }
}