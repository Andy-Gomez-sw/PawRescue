package com.refugio.pawrescue.model;

import java.io.Serializable;

public class AdoptionRequest implements Serializable {
    private String animalFotoUrl;
    private String animalNombre;
    private String animalRaza;
    private String folio;
    private String fecha;
    private String estado;      // Ejemplo: "cita_agendada"
    private String estadoTexto; // Ejemplo: "En Proceso"
    private int estadoIcon;     // R.drawable.ic_...
    private int estadoColor;    // R.color...
    private Cita citaAgendada;

    // Constructor vacío
    public AdoptionRequest() {}

    // Getters necesarios para tu Adapter
    public String getAnimalFotoUrl() { return animalFotoUrl; }
    public String getAnimalNombre() { return animalNombre; }
    public String getAnimalRaza() { return animalRaza; }
    public String getFolio() { return folio; }

    // Tu adaptador pide getFechaFormateada, así que retornamos la fecha tal cual o formateada
    public String getFechaFormateada() { return fecha; }

    public String getEstado() { return estado; }
    public String getEstadoTexto() { return estadoTexto; }
    public int getEstadoIcon() { return estadoIcon; }
    public int getEstadoColor() { return estadoColor; }
    public Cita getCitaAgendada() { return citaAgendada; }

    // Setters (para poder llenar los datos)
    public void setAnimalFotoUrl(String animalFotoUrl) { this.animalFotoUrl = animalFotoUrl; }
    public void setAnimalNombre(String animalNombre) { this.animalNombre = animalNombre; }
    public void setAnimalRaza(String animalRaza) { this.animalRaza = animalRaza; }
    public void setFolio(String folio) { this.folio = folio; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setEstadoTexto(String estadoTexto) { this.estadoTexto = estadoTexto; }
    public void setEstadoIcon(int estadoIcon) { this.estadoIcon = estadoIcon; }
    public void setEstadoColor(int estadoColor) { this.estadoColor = estadoColor; }
    public void setCitaAgendada(Cita citaAgendada) { this.citaAgendada = citaAgendada; }

    // Clase interna para la Cita (porque tu adaptador usa .getCitaAgendada().getFechaHoraFormateada())
    public static class Cita implements Serializable {
        private String fechaHora;

        public Cita(String fechaHora) {
            this.fechaHora = fechaHora;
        }

        public String getFechaHoraFormateada() {
            return fechaHora;
        }
    }
}