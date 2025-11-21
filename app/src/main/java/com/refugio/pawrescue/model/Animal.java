package com.refugio.pawrescue.model;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Date; // <--- IMPORTANTE: Usar Date, no Timestamp
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
    private String fotoUrl;
    private String ubicacionRescate;

    // --- CORRECCIÓN DEL ERROR ---
    // Usamos java.util.Date porque es Serializable.
    // Firebase convierte automáticamente el Timestamp de la base de datos a Date.
    private Date fechaRegistro;

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

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getUbicacionRescate() {
        return ubicacionRescate;
    }

    public void setUbicacionRescate(String ubicacionRescate) {
        this.ubicacionRescate = ubicacionRescate;
    }

    // --- GETTER Y SETTER DE FECHA CORREGIDOS ---
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}