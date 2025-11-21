package com.refugio.pawrescue.model;

public class Usuario {
    private String uid;
    private long idNumerico; // <--- Tu nuevo campo
    private String nombre;
    private String correo;
    private String rol;
    private boolean estadoActivo;

    public Usuario() {
    }

    public Usuario(String uid, String nombre, String correo, String rol, boolean estadoActivo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.estadoActivo = estadoActivo;
    }

    public long getIdNumerico() {
        return idNumerico;
    }

    public void setIdNumerico(long idNumerico) {
        this.idNumerico = idNumerico;
    }
    // ---------------------------------------

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