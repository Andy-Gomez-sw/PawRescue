package com.refugio.pawrescue.ui.publico;

import java.io.Serializable;
import java.util.Date;

public class SolicitudAdopcion implements Serializable {
    private String idSolicitud;
    private long idNumerico;
    private String idUsuario;
    private String emailUsuario;
    private String idAnimal;
    private String nombreAnimal;
    private String fotoAnimalUrl;
    private Date fechaSolicitud;
    private String estado; 

    public SolicitudAdopcion() {
        // Constructor vacío para Firebase
    }

    // Getters y Setters
    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public long getIdNumerico() { return idNumerico; }
    public void setIdNumerico(long idNumerico) { this.idNumerico = idNumerico; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getIdAnimal() { return idAnimal; }
    public void setIdAnimal(String idAnimal) { this.idAnimal = idAnimal; }

    public String getNombreAnimal() { return nombreAnimal; }
    public void setNombreAnimal(String nombreAnimal) { this.nombreAnimal = nombreAnimal; }

    public String getFotoAnimalUrl() { return fotoAnimalUrl; }
    public void setFotoAnimalUrl(String fotoAnimalUrl) { this.fotoAnimalUrl = fotoAnimalUrl; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}