package com.refugio.pawrescue.model;

import java.util.Date;

public class Message {
    private String contenido;
    private String remitenteId; // ID del usuario o "ADMIN"
    private Date fecha;
    private boolean esMio; // Para saber si lo alineamos a la derecha (mío) o izquierda (admin)

    // Constructor vacío para Firebase
    public Message() {}

    public Message(String contenido, String remitenteId, Date fecha, boolean esMio) {
        this.contenido = contenido;
        this.remitenteId = remitenteId;
        this.fecha = fecha;
        this.esMio = esMio;
    }

    public String getContenido() { return contenido; }
    public String getRemitenteId() { return remitenteId; }
    public Date getFecha() { return fecha; }
    public boolean isEsMio() { return esMio; }

    public void setEsMio(boolean esMio) { this.esMio = esMio; }
}
