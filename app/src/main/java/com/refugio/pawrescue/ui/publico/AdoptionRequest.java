package com.refugio.pawrescue.ui.publico;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import com.refugio.pawrescue.R;

public class AdoptionRequest implements Serializable {
    private String id;
    private String folio;
    private String animalId;
    private String animalNombre;
    private String animalRaza;
    private String animalFotoUrl;
    private String usuarioId;
    private Map<String, Object> datosPersonales;
    private String estado; // pendiente, en_revision, cita_agendada, aprobada, rechazada
    private String estadoActual;
    private Date fechaSolicitud;
    private CitaAgendada citaAgendada;
    private Date fechaEntrega;
    private String voluntarioId;

    // 🚨 CAMPO AÑADIDO PARA SOLUCIONAR EL ERROR DE COMPILACIÓN
    private String citaId;

    // 🟢 NUEVOS CAMPOS PARA DOCUMENTOS
    private String urlIneFrente;
    private String urlIneReverso;
    private String urlComprobante;

    // Constructor vacío requerido por Firestore
    public AdoptionRequest() {}

    public AdoptionRequest(String id, String folio, String animalNombre, String estado, Date fechaSolicitud) {
        this.id = id;
        this.folio = folio;
        this.animalNombre = animalNombre;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    // 🟢 GETTERS Y SETTERS PARA DOCUMENTOS
    public String getUrlIneFrente() {
        return urlIneFrente;
    }

    public void setUrlIneFrente(String urlIneFrente) {
        this.urlIneFrente = urlIneFrente;
    }

    public String getUrlIneReverso() {
        return urlIneReverso;
    }

    public void setUrlIneReverso(String urlIneReverso) {
        this.urlIneReverso = urlIneReverso;
    }

    public String getUrlComprobante() {
        return urlComprobante;
    }

    public void setUrlComprobante(String urlComprobante) {
        this.urlComprobante = urlComprobante;
    }
    // FIN GETTERS Y SETTERS DOCUMENTOS

    // Getters y Setters existentes
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
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

    public String getAnimalRaza() {
        return animalRaza;
    }

    public void setAnimalRaza(String animalRaza) {
        this.animalRaza = animalRaza;
    }

    public String getAnimalFotoUrl() {
        return animalFotoUrl;
    }

    public void setAnimalFotoUrl(String animalFotoUrl) {
        this.animalFotoUrl = animalFotoUrl;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Map<String, Object> getDatosPersonales() {
        return datosPersonales;
    }

    public void setDatosPersonales(Map<String, Object> datosPersonales) {
        this.datosPersonales = datosPersonales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public CitaAgendada getCitaAgendada() {
        return citaAgendada;
    }

    public void setCitaAgendada(CitaAgendada citaAgendada) {
        this.citaAgendada = citaAgendada;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getVoluntarioId() {
        return voluntarioId;
    }

    public void setVoluntarioId(String voluntarioId) {
        this.voluntarioId = voluntarioId;
    }

    // 🚨 GETTER Y SETTER AÑADIDOS PARA EL NUEVO CAMPO
    public String getCitaId() {
        return citaId;
    }

    public void setCitaId(String citaId) {
        this.citaId = citaId;
    }
    // FIN GETTER Y SETTER AÑADIDOS


    // Métodos helper
    public String getEstadoTexto() {
        if (estado == null) return "Desconocido";
        switch (estado) {
            case "pendiente":
                return "Pendiente";
            case "en_revision":
                return "En Revisión";
            case "cita_agendada":
                return "Cita Agendada";
            case "aprobada":
                return "Aprobada";
            case "rechazada":
                return "No Aprobada";
            default:
                return "Desconocido";
        }
    }

    public int getEstadoColor() {
        if (estado == null) return R.color.gray_light;
        switch (estado) {
            case "pendiente":
                return R.color.status_pending_bg;
            case "en_revision":
                return R.color.status_review_bg;
            case "cita_agendada":
                return R.color.status_scheduled_bg;
            case "aprobada":
                return R.color.status_approved_bg;
            case "rechazada":
                return R.color.status_rejected_bg;
            default:
                return R.color.gray_light;
        }
    }

    public int getEstadoIcon() {
        if (estado == null) return R.drawable.ic_help;
        switch (estado) {
            case "pendiente":
                return R.drawable.ic_clock;
            case "en_revision":
                return R.drawable.ic_document;
            case "cita_agendada":
                return R.drawable.ic_calendar_check;
            case "aprobada":
                return R.drawable.ic_check_circle;
            case "rechazada":
                return R.drawable.ic_close_circle;
            default:
                return R.drawable.ic_help;
        }
    }

    public String getFechaFormateada() {
        if (fechaSolicitud == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy",
                java.util.Locale.getDefault());
        return sdf.format(fechaSolicitud);
    }


    // Clase interna para citas
    public static class CitaAgendada implements Serializable {
        private Date fecha;
        private String hora;
        private String lugar;
        private String coordinador;

        public CitaAgendada() {}

        public CitaAgendada(Date fecha, String hora, String lugar, String coordinador) {
            this.fecha = fecha;
            this.hora = hora;
            this.lugar = lugar;
            this.coordinador = coordinador;
        }

        public Date getFecha() {
            return fecha;
        }

        public void setFecha(Date fecha) {
            this.fecha = fecha;
        }

        public String getHora() {
            return hora;
        }

        public void setHora(String hora) {
            this.hora = hora;
        }

        public String getLugar() {
            return lugar;
        }

        public void setLugar(String lugar) {
            this.lugar = lugar;
        }

        public String getCoordinador() {
            return coordinador;
        }

        public void setCoordinador(String coordinador) {
            this.coordinador = coordinador;
        }

        public String getFechaHoraFormateada() {
            if (fecha == null) return hora;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM, HH:mm",
                    java.util.Locale.getDefault());
            return sdf.format(fecha);
        }
    }
}