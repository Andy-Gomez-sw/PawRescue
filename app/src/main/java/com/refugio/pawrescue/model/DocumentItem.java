package com.refugio.pawrescue.model;

public class DocumentItem {
    private String nombre;
    private String estado;
    private boolean esObligatorio;

    // Constructor COMPLETO (3 datos)
    public DocumentItem(String nombre, String estado, boolean esObligatorio) {
        this.nombre = nombre;
        this.estado = estado;
        this.esObligatorio = esObligatorio;
    }

    // Constructor NUEVO (2 datos) - Este es el que necesita tu Activity
    // Automáticamente pone el estado como "Pendiente"
    public DocumentItem(String nombre, boolean esObligatorio) {
        this.nombre = nombre;
        this.estado = "Pendiente"; // Valor por defecto
        this.esObligatorio = esObligatorio;
    }

    public String getNombre() { return nombre; }

    // Agrego getName por si tu código lo llama en inglés en algún lado
    public String getName() { return nombre; }

    public String getEstado() { return estado; }
    public boolean esObligatorio() { return esObligatorio; }

    public void setEstado(String estado) { this.estado = estado; }
}