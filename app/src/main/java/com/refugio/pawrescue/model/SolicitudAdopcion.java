package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * Modelo de datos para gestionar las Solicitudes de Adopción (RF-14).
 * Esta entidad representa el interés de un adoptante por un animal.
 */
public class SolicitudAdopcion implements Serializable {
    private String idSolicitud;
    private String idAnimal; // ID del animal solicitado
    private String idUsuarioAdoptante; // UID del usuario que crea la solicitud (si es público)
    private String nombreAdoptante;
    private String telefonoAdoptante;
    private String correoAdoptante;
    private String domicilio;
    private String motivacion; // Ej: "Motivo por el que desea adoptar"
    private Timestamp fechaSolicitud;
    private String estadoSolicitud; // Ej: "Pendiente", "Rechazada", "Aprobada", "Cita Agendada"
    private Timestamp fechaCita; // Usado para agendar la cita (RF-15)

    // Constructor vacío requerido por Firebase Firestore
    public SolicitudAdopcion() {
    }

    // Getters y Setters
    public String getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(String idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(String idAnimal) {
        this.idAnimal = idAnimal;
    }

    public String getIdUsuarioAdoptante() {
        return idUsuarioAdoptante;
    }

    public void setIdUsuarioAdoptante(String idUsuarioAdoptante) {
        this.idUsuarioAdoptante = idUsuarioAdoptante;
    }

    public String getNombreAdoptante() {
        return nombreAdoptante;
    }

    public void setNombreAdoptante(String nombreAdoptante) {
        this.nombreAdoptante = nombreAdoptante;
    }

    public String getTelefonoAdoptante() {
        return telefonoAdoptante;
    }

    public void setTelefonoAdoptante(String telefonoAdoptante) {
        this.telefonoAdoptante = telefonoAdoptante;
    }

    public String getCorreoAdoptante() {
        return correoAdoptante;
    }

    public void setCorreoAdoptante(String correoAdoptante) {
        this.correoAdoptante = correoAdoptante;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getMotivacion() {
        return motivacion;
    }

    public void setMotivacion(String motivacion) {
        this.motivacion = motivacion;
    }

    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public Timestamp getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(Timestamp fechaCita) {
        this.fechaCita = fechaCita;
    }
}