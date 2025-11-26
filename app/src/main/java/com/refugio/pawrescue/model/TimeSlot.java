package com.refugio.pawrescue.model;

/**
 * Modelo para representar un horario disponible o ocupado
 */
public class TimeSlot {
    private String time; // "09:00", "10:00", etc.
    private boolean available;
    private boolean selected;

    public TimeSlot() {}

    public TimeSlot(String time, boolean available) {
        this.time = time;
        this.available = available;
        this.selected = false;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDisplayTime() {
        // Convertir "09:00" a "9:00 AM"
        if (time == null) return "";

        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        String amPm = hour < 12 ? "AM" : "PM";

        if (hour > 12) hour -= 12;
        if (hour == 0) hour = 12;

        return hour + ":" + parts[1] + " " + amPm;
    }
}