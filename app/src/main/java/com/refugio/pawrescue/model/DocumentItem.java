package com.refugio.pawrescue.model;

public class DocumentItem {
    private String nombre;
    private String estado;
    private boolean esObligatorio;
    private String url; // 🟢 NUEVO CAMPO

    // Constructor para documentos ya subidos (Visualización con URL)
    public DocumentItem(String nombre, String estado, String url) {
        this.nombre = nombre;
        this.estado = estado;
        this.esObligatorio = true; // Asumimos obligatorio si ya se subió
        this.url = url;
    }

    // Constructor COMPLETO (3 datos) - Retrocompatibilidad
    public DocumentItem(String nombre, String estado, boolean esObligatorio) {
        this.nombre = nombre;
        this.estado = estado;
        this.esObligatorio = esObligatorio;
        this.url = null;
    }

    // Constructor NUEVO (2 datos) - Valor por defecto "Pendiente"
    public DocumentItem(String nombre, boolean esObligatorio) {
        this.nombre = nombre;
        this.estado = "Pendiente"; // Valor por defecto
        this.esObligatorio = esObligatorio;
        this.url = null;
    }

    public String getNombre() { return nombre; }

    // getName por si tu código lo llama en inglés
    public String getName() { return nombre; }

    public String getEstado() { return estado; }

    public boolean esObligatorio() { return esObligatorio; }

    // 🟢 NUEVO GETTER PARA LA URL
    public String getUrl() { return url; }

    public void setEstado(String estado) { this.estado = estado; }
}