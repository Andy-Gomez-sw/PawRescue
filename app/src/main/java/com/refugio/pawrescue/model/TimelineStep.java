package com.refugio.pawrescue.model;

public class TimelineStep {
    private String title;
    private String date;
    private String status;      // Ej: "completed", "current", "pending"
    private String description;
    private String location;    // Puede ser null
    private boolean isLast;     // Para ocultar la línea final en el adaptador

    // Constructor que acepta los 5 parámetros que envía tu Activity
    public TimelineStep(String title, String date, String status, String description, String location) {
        this.title = title;
        this.date = date;
        this.status = status;
        this.description = description;
        this.location = location;
        this.isLast = false;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }

    public boolean isLast() { return isLast; }
    public void setLast(boolean last) { isLast = last; }

    // --- MÉTODO DE COMPATIBILIDAD ---
    // Tu adaptador TimelineAdapter busca este método booleano.
    // Lo calculamos basándonos en el texto "status".
    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }
}