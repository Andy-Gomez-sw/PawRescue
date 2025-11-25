package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;

public class VolunteerHistorialItem {

    public static final String TIPO_OBSERVACION = "OBS";
    public static final String TIPO_FOTO = "FOTO";
    public static final String TIPO_ACCION = "ACCION";

    // Para todos los tipos
    private String tipo;          // OBS, FOTO o ACCION
    private String detalle;       // Notas o descripción
    private String fotoUrl;       // Solo si es FOTO
    private Timestamp fecha;      // Para ordenar cronológicamente

    // Solo para observaciones:
    private Boolean comioBien;
    private Boolean medicamentosOk;
    private Boolean comportamientoNormal;

    // Solo para acciones:
    // "ATENDIDO_HOY", "CITA_CONFIRMADA", "PROBLEMA"
    private String tipoAccion;

    public VolunteerHistorialItem() {
    }

    public VolunteerHistorialItem(String tipo, String detalle, String fotoUrl, Timestamp fecha) {
        this.tipo = tipo;
        this.detalle = detalle;
        this.fotoUrl = fotoUrl;
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public Boolean getComioBien() {
        return comioBien;
    }

    public void setComioBien(Boolean comioBien) {
        this.comioBien = comioBien;
    }

    public Boolean getMedicamentosOk() {
        return medicamentosOk;
    }

    public void setMedicamentosOk(Boolean medicamentosOk) {
        this.medicamentosOk = medicamentosOk;
    }

    public Boolean getComportamientoNormal() {
        return comportamientoNormal;
    }

    public void setComportamientoNormal(Boolean comportamientoNormal) {
        this.comportamientoNormal = comportamientoNormal;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }
}
