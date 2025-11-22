package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo de datos para el seguimiento post-adopción (RF-17).
 * Esta entidad será una subcolección de SolicitudAdopcion o Animal.
 */
public class SeguimientoPostAdopcion implements Serializable {
    private String idSeguimiento;
    private String idAnimal;
    private Timestamp fechaVisita;
    private String comentarios;
    private String fotoUrl; // Opcional, evidencia multimedia
    private String coordinadorId;

    // Constructor vacío requerido por Firebase Firestore
    public SeguimientoPostAdopcion() {
    }

    // Getters y Setters
    public String getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(String idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public String getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(String idAnimal) {
        this.idAnimal = idAnimal;
    }

    public Timestamp getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(Timestamp fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getCoordinadorId() {
        return coordinadorId;
    }

    public void setCoordinadorId(String coordinadorId) {
        this.coordinadorId = coordinadorId;
    }
}