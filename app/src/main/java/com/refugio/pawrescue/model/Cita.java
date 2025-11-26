package com.refugio.pawrescue.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Modelo de Cita para el sistema de adopción
 * Estados: pendiente_asignacion, asignada, completada, cancelada
 */
public class Cita implements Serializable {
    private String id;
    private String solicitudId;
    private String animalId;
    private String animalNombre;
    private String usuarioId;
    private String usuarioEmail;
    private String fecha; // "2025-01-15"
    private String hora; // "10:00"
    private String estado; // pendiente_asignacion, asignada, completada, cancelada
    private String voluntarioAsignado; // UID del voluntario
    private String voluntarioNombre;
    private boolean reporteCompleto;
    private String reporteId;
    private Date fechaCreacion;

    public Cita() {}

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAnimalNombre() {
        return animalNombre;
    }

    public void setAnimalNombre(String animalNombre) {
        this.animalNombre = animalNombre;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getVoluntarioAsignado() {
        return voluntarioAsignado;
    }

    public void setVoluntarioAsignado(String voluntarioAsignado) {
        this.voluntarioAsignado = voluntarioAsignado;
    }

    public String getVoluntarioNombre() {
        return voluntarioNombre;
    }

    public void setVoluntarioNombre(String voluntarioNombre) {
        this.voluntarioNombre = voluntarioNombre;
    }

    public boolean isReporteCompleto() {
        return reporteCompleto;
    }

    public void setReporteCompleto(boolean reporteCompleto) {
        this.reporteCompleto = reporteCompleto;
    }

    public String getReporteId() {
        return reporteId;
    }

    public void setReporteId(String reporteId) {
        this.reporteId = reporteId;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // Métodos helper
    public String getFechaHoraCompleta() {
        return fecha + " a las " + hora;
    }

    public String getEstadoTexto() {
        switch (estado) {
            case "pendiente_asignacion":
                return "Pendiente de Asignar";
            case "asignada":
                return "Asignada a Voluntario";
            case "completada":
                return "Completada";
            case "cancelada":
                return "Cancelada";
            default:
                return "Desconocido";
        }
    }
}