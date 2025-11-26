package com.refugio.pawrescue.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Seguimiento {
    private String id;
    private String solicitudId;
    private String animalId;
    private String usuarioId; // ID del voluntario
    private String comentario;
    private String resultadoVisita; // 🟢 NUEVO CAMPO: Resultado final del voluntario
    // private String fotoUrl;

    @ServerTimestamp
    private Date fecha;

    public Seguimiento() {} // Vacío para Firebase

    public Seguimiento(String solicitudId, String animalId, String usuarioId, String comentario, String resultadoVisita) {
        this.solicitudId = solicitudId;
        this.animalId = animalId;
        this.usuarioId = usuarioId;
        this.comentario = comentario;
        this.resultadoVisita = resultadoVisita; // 🟢 Inicialización
    }

    // ==========================================================
    // GETTERS y SETTERS
    // ==========================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public String getAnimalId() { return animalId; }
    public void setAnimalId(String animalId) { this.animalId = animalId; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    // 🟢 GETTER y SETTER para resultadoVisita
    public String getResultadoVisita() { return resultadoVisita; }
    public void setResultadoVisita(String resultadoVisita) { this.resultadoVisita = resultadoVisita; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}