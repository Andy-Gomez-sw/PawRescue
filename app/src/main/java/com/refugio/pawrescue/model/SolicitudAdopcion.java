package com.refugio.pawrescue.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class SolicitudAdopcion implements Serializable {

    @DocumentId
    private String idSolicitud;

    // --- CAMPOS DEL ANIMAL ---
    @PropertyName("animalId")
    private String idAnimal;

    @PropertyName("animalNombre")
    private String nombreAnimal;

    private String animalRaza;
    private String animalFotoUrl;

    // --- CAMPOS DEL USUARIO ---
    @PropertyName("usuarioId")
    private String usuarioId;

    @PropertyName("usuarioEmail")
    private String usuarioEmail;

    // --- DATOS DEL FORMULARIO (Step 1) ---
    @PropertyName("nombreCompleto")
    private String nombreCompleto;

    @PropertyName("telefono")
    private String telefono;

    @PropertyName("email")
    private String email;

    @PropertyName("direccion")
    private String direccion;

    @PropertyName("tipoVivienda")
    private String tipoVivienda;

    @PropertyName("propiedadVivienda")
    private String propiedadVivienda;

    @PropertyName("fechaNacimiento")
    private String fechaNacimiento;

    // --- DATOS ADICIONALES DEL FORMULARIO ---
    private Map<String, Object> datosPersonales; // Para guardar todo Step1
    private Map<String, Object> datosFamilia;     // Step 2
    private Map<String, Object> datosExperiencia; // Step 3
    private Map<String, Object> datosCompromiso;  // Step 4

    // --- ESTADO Y FECHAS ---
    @PropertyName("estadoSolicitud")
    private String estadoSolicitud;

    private String folio;
    private Date fechaSolicitud;
    private Date fechaCita;
    private String citaId;
    private String reporteId;

    // Constructor vacío para Firestore
    public SolicitudAdopcion() {}

    // =========================================================
    // GETTERS Y SETTERS
    // =========================================================

    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getIdAnimal() { return idAnimal; }
    public void setIdAnimal(String idAnimal) { this.idAnimal = idAnimal; }

    public String getNombreAnimal() { return nombreAnimal; }
    public void setNombreAnimal(String nombreAnimal) { this.nombreAnimal = nombreAnimal; }

    public String getAnimalRaza() { return animalRaza; }
    public void setAnimalRaza(String animalRaza) { this.animalRaza = animalRaza; }

    public String getAnimalFotoUrl() { return animalFotoUrl; }
    public void setAnimalFotoUrl(String animalFotoUrl) { this.animalFotoUrl = animalFotoUrl; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTipoVivienda() { return tipoVivienda; }
    public void setTipoVivienda(String tipoVivienda) { this.tipoVivienda = tipoVivienda; }

    public String getPropiedadVivienda() { return propiedadVivienda; }
    public void setPropiedadVivienda(String propiedadVivienda) { this.propiedadVivienda = propiedadVivienda; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Map<String, Object> getDatosPersonales() { return datosPersonales; }
    public void setDatosPersonales(Map<String, Object> datosPersonales) { this.datosPersonales = datosPersonales; }

    public Map<String, Object> getDatosFamilia() { return datosFamilia; }
    public void setDatosFamilia(Map<String, Object> datosFamilia) { this.datosFamilia = datosFamilia; }

    public Map<String, Object> getDatosExperiencia() { return datosExperiencia; }
    public void setDatosExperiencia(Map<String, Object> datosExperiencia) { this.datosExperiencia = datosExperiencia; }

    public Map<String, Object> getDatosCompromiso() { return datosCompromiso; }
    public void setDatosCompromiso(Map<String, Object> datosCompromiso) { this.datosCompromiso = datosCompromiso; }

    public String getEstadoSolicitud() { return estadoSolicitud; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }

    // Alias para compatibilidad
    public String getEstado() { return estadoSolicitud; }
    public void setEstado(String estado) { this.estadoSolicitud = estado; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Date getFechaCita() { return fechaCita; }
    public void setFechaCita(Date fechaCita) { this.fechaCita = fechaCita; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public String getReporteId() { return reporteId; }
    public void setReporteId(String reporteId) { this.reporteId = reporteId; }

    // =========================================================
    // MÉTODOS HELPER PARA EL ADAPTER
    // =========================================================

    public String getNombreAdoptante() {
        if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
            return nombreCompleto;
        }
        return "Usuario desconocido";
    }

    public String getTelefonoAdoptante() {
        if (telefono != null && !telefono.isEmpty()) {
            return telefono;
        }
        return "Sin teléfono";
    }

    public String getEmailAdoptante() {
        if (email != null && !email.isEmpty()) {
            return email;
        }
        if (usuarioEmail != null && !usuarioEmail.isEmpty()) {
            return usuarioEmail;
        }
        return "Sin email";
    }

    public String getCorreoAdoptante() {
        return getEmailAdoptante();
    }
}