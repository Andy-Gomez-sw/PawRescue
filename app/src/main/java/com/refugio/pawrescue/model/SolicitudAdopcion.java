package com.refugio.pawrescue.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;

public class SolicitudAdopcion implements Serializable {

    @DocumentId
    private String idSolicitud;

    // --- CAMPOS DEL FORMULARIO PÚBLICO (Firebase) ---
    @PropertyName("animalNombre")
    private String nombreAnimal;

    @PropertyName("animalId")
    private String idAnimal;

    @PropertyName("estado")
    private String estadoSolicitud;

    @PropertyName("nombreCompleto")
    private String nombreCompleto;

    @PropertyName("telefono")
    private String telefono;

    @PropertyName("email")
    private String email;

    private String usuarioId;
    private Date fechaSolicitud;

    // --- CAMPOS DEL ADMIN (Citas) ---
    private Date fechaCita;

    public SolicitudAdopcion() {}

    // =========================================================
    // MÉTODOS DE COMPATIBILIDAD (Alias para evitar errores)
    // =========================================================

    // 1. Para arreglar el error de VolunteerCitaDetailActivity
    public String getCorreoAdoptante() {
        return email != null ? email : "Sin correo";
    }

    // 2. El método estándar
    public String getEmailAdoptante() {
        return email != null ? email : "Sin email";
    }

    public String getNombreAdoptante() {
        return nombreCompleto != null ? nombreCompleto : "Usuario Desconocido";
    }

    public String getTelefonoAdoptante() {
        return telefono != null ? telefono : "Sin teléfono";
    }

    // =========================================================
    // SETTERS
    // =========================================================
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }

    // =========================================================
    // GETTERS Y SETTERS ESTÁNDAR
    // =========================================================
    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getNombreAnimal() { return nombreAnimal; }
    public void setNombreAnimal(String nombreAnimal) { this.nombreAnimal = nombreAnimal; }

    public String getIdAnimal() { return idAnimal; }
    public void setIdAnimal(String idAnimal) { this.idAnimal = idAnimal; }

    public String getEstadoSolicitud() { return estadoSolicitud; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Date getFechaCita() { return fechaCita; }
    public void setFechaCita(Date fechaCita) { this.fechaCita = fechaCita; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    // Alias extra
    public String getEstado() { return estadoSolicitud; }
    public void setEstado(String estado) { this.estadoSolicitud = estado; }
}