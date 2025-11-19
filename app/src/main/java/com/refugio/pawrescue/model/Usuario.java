package com.refugio.pawrescue.model;

/**
 * Modelo de datos para la entidad Usuario.
 * Mapea la información de permisos y rol almacenada en la colección "usuarios".
 * Esencial para el flujo de autenticación y roles (RF-04).
 */
public class Usuario {
    private String uid;
    private String nombre;
    private String correo;
    private String rol; // Valores clave: "Admin", "Coordinador", "Voluntario" (RF-04)
    private boolean estadoActivo;

    // Constructor vacío requerido por Firebase Firestore
    public Usuario() {
    }

    // Constructor completo
    public Usuario(String uid, String nombre, String correo, String rol, boolean estadoActivo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }
}
