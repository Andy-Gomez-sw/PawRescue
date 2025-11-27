package com.refugio.pawrescue.model;

import java.util.Date;
import java.util.List;

public class ReporteCita {

    private String id;                // ID del documento en Firestore
    private String citaId;            // ID de la cita evaluada
    private String solicitudId;       // ID de la solicitud de adopción
    private String animalId;          // ID del animal relacionado
    private String usuarioId;         // ID del adoptante
    private String voluntarioId;      // ID del voluntario que evaluó
    private Date fechaReporte;        // Fecha en que se generó el reporte

    // Checklist de evaluación
    private boolean ineRecibida;
    private boolean comprobanteRecibido;
    private boolean domicilioAdecuado;
    private boolean actitudPositiva;
    private boolean experienciaPrevia;
    private boolean recomiendaAprobacion;

    // Comentarios del voluntario
    private String observaciones;
    private String recomendaciones;

    // Documentos (URLs en Firebase Storage)
    private List<String> documentos;

    // Constructor vacío requerido por Firestore
    public ReporteCita() {}

    // ----- GETTERS & SETTERS -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCitaId() {
        return citaId;
    }

    public void setCitaId(String citaId) {
        this.citaId = citaId;
    }

    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getVoluntarioId() {
        return voluntarioId;
    }

    public void setVoluntarioId(String voluntarioId) {
        this.voluntarioId = voluntarioId;
    }

    public Date getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(Date fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public boolean isIneRecibida() {
        return ineRecibida;
    }

    public void setIneRecibida(boolean ineRecibida) {
        this.ineRecibida = ineRecibida;
    }

    public boolean isComprobanteRecibido() {
        return comprobanteRecibido;
    }

    public void setComprobanteRecibido(boolean comprobanteRecibido) {
        this.comprobanteRecibido = comprobanteRecibido;
    }

    public boolean isDomicilioAdecuado() {
        return domicilioAdecuado;
    }

    public void setDomicilioAdecuado(boolean domicilioAdecuado) {
        this.domicilioAdecuado = domicilioAdecuado;
    }

    public boolean isActitudPositiva() {
        return actitudPositiva;
    }

    public void setActitudPositiva(boolean actitudPositiva) {
        this.actitudPositiva = actitudPositiva;
    }

    public boolean isExperienciaPrevia() {
        return experienciaPrevia;
    }

    public void setExperienciaPrevia(boolean experienciaPrevia) {
        this.experienciaPrevia = experienciaPrevia;
    }

    public boolean isRecomiendaAprobacion() {
        return recomiendaAprobacion;
    }

    public void setRecomiendaAprobacion(boolean recomiendaAprobacion) {
        this.recomiendaAprobacion = recomiendaAprobacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public List<String> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<String> documentos) {
        this.documentos = documentos;
    }
}
