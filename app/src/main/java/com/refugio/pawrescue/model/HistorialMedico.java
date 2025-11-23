package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;

/**
 * Modelo de datos para la entidad Historial Médico.
 * Esta entidad será una subcolección (ej: "animales/{idAnimal}/historialMedico").
 * Corresponde al Requisito Funcional RF-09.
 */
public class HistorialMedico {
    private String idRegistro;
    private String idAnimal;
    private String tipoEvento; // Ej: "Consulta", "Vacuna", "Tratamiento"
    private String diagnostico;
    private String tratamiento;
    private Timestamp fecha;
    private String veterinario;
    private String notas;

    // Constructor vacío requerido por Firebase Firestore
    public HistorialMedico() {
    }

    // Constructor completo
    public HistorialMedico(String idRegistro, String idAnimal, String tipoEvento, String diagnostico, String tratamiento, Timestamp fecha, String veterinario, String notas) {
        this.idRegistro = idRegistro;
        this.idAnimal = idAnimal;
        this.tipoEvento = tipoEvento;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fecha = fecha;
        this.veterinario = veterinario;
        this.notas = notas;
    }

    // Getters y Setters
    public String getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(String idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(String idAnimal) {
        this.idAnimal = idAnimal;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

}
