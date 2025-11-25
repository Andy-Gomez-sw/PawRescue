package com.refugio.pawrescue.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Modelo de datos para la entidad Animal.
 * Implementa Serializable para poder pasar el objeto completo entre actividades con Intent.
 */
public class Animal implements Serializable {

    private String idAnimal;
    private long idNumerico;
    private String nombre;
    private String especie;
    private String raza;
    private String sexo;
    private String edadAprox;
    private String estadoSalud;
    private List<String> condicionesEspeciales;
    private String estadoRefugio;
    private String idVoluntario;
    private String nombreVoluntario;
    private String fotoUrl;
    private List<String> fotosUrls;
    private String ubicacionRescate;
    private Date fechaRegistro;

    // Campos adicionales para vista pública
    private String descripcion;
    private String personalidad;
    private String tamano;
    private boolean favorited;

    // Constructor vacío requerido por Firebase Firestore
    public Animal() {
    }

    // Getters y Setters

    public String getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(String idAnimal) {
        this.idAnimal = idAnimal;
    }

    // MÉTODOS CRÍTICOS AGREGADOS
    public String getId() {
        return idAnimal;
    }

    public void setId(String id) {
        this.idAnimal = id;
    }

    public long getIdNumerico() {
        return idNumerico;
    }

    public void setIdNumerico(long idNumerico) {
        this.idNumerico = idNumerico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEdadAprox() {
        return edadAprox;
    }

    public void setEdadAprox(String edadAprox) {
        this.edadAprox = edadAprox;
    }

    public String getEdadTexto() {
        if (edadAprox == null) return "Edad desconocida";
        return edadAprox;
    }

    public int getEdad() {
        return 0; // Placeholder si necesitas edad numérica
    }

    public String getEstadoSalud() {
        return estadoSalud;
    }

    public void setEstadoSalud(String estadoSalud) {
        this.estadoSalud = estadoSalud;
    }

    public List<String> getCondicionesEspeciales() {
        return condicionesEspeciales;
    }

    public void setCondicionesEspeciales(List<String> condicionesEspeciales) {
        this.condicionesEspeciales = condicionesEspeciales;
    }

    public String getEstadoRefugio() {
        return estadoRefugio;
    }

    public void setEstadoRefugio(String estadoRefugio) {
        this.estadoRefugio = estadoRefugio;
    }

    public String getIdVoluntario() {
        return idVoluntario;
    }

    public void setIdVoluntario(String idVoluntario) {
        this.idVoluntario = idVoluntario;
    }

    public String getNombreVoluntario() {
        return nombreVoluntario;
    }

    public void setNombreVoluntario(String nombreVoluntario) {
        this.nombreVoluntario = nombreVoluntario;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    public String getUbicacionRescate() {
        return ubicacionRescate;
    }

    public void setUbicacionRescate(String ubicacionRescate) {
        this.ubicacionRescate = ubicacionRescate;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getFechaRescate() {
        if (fechaRegistro == null) return "Fecha desconocida";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
        return sdf.format(fechaRegistro);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPersonalidad() {
        return personalidad;
    }

    public void setPersonalidad(String personalidad) {
        this.personalidad = personalidad;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }
}