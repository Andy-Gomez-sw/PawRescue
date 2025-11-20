package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.List;

/**
 * Modelo de datos para la entidad Animal.
 * Mapea la información almacenada en la colección "animales" de Firestore.
 * Corresponde principalmente a los Requisitos Funcionales RF-05 y RF-08.
 */
public class Animal implements Serializable {
    private String idAnimal;
    private long idNumerico;
    private String nombre;
    private String especie;
    private String raza;
    private String sexo;
    private String edadAprox; // Ej: "Cachorro", "Adulto"
    private String estadoSalud;
    private String estadoRefugio; // Ej: "Rescatado", "Disponible Adopcion", "Adoptado" (RF-08)
    private GeoPoint ubicacionRescate; // Coordenadas GPS del rescate (RF-11)
    private Timestamp fechaRegistro;
    private String fotoUrl; // URL de la imagen en Firebase Storage (RF-05)
    private String idVoluntario;
    private List<String> condicionesEspeciales; // Ej: "Heridas", "Desnutrido"

    // Constructor vacío requerido por Firebase Firestore
    public Animal() {
    }

    // Constructor completo
    public Animal(String idAnimal, String nombre, String especie, String raza, String sexo, String edadAprox, String estadoSalud, String estadoRefugio, GeoPoint ubicacionRescate, Timestamp fechaRegistro, String fotoUrl, String idVoluntario, List<String> condicionesEspeciales) {
        this.idAnimal = idAnimal;
        this.idNumerico = idNumerico;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.sexo = sexo;
        this.edadAprox = edadAprox;
        this.estadoSalud = estadoSalud;
        this.estadoRefugio = estadoRefugio;
        this.ubicacionRescate = ubicacionRescate;
        this.fechaRegistro = fechaRegistro;
        this.fotoUrl = fotoUrl;
        this.idVoluntario = idVoluntario;
        this.condicionesEspeciales = condicionesEspeciales;
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

    public String getEstadoRefugio() {
        return estadoRefugio;
    }

    public void setEstadoRefugio(String estadoRefugio) {
        this.estadoRefugio = estadoRefugio;
    }

    public GeoPoint getUbicacionRescate() {
        return ubicacionRescate;
    }

    public void setUbicacionRescate(GeoPoint ubicacionRescate) {
        this.ubicacionRescate = ubicacionRescate;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getIdVoluntario() {
        return idVoluntario;
    }

    public void setIdVoluntario(String idVoluntario) {
        this.idVoluntario = idVoluntario;
    }

    public List<String> getCondicionesEspeciales() {
        return condicionesEspeciales;
    }

    public void setCondicionesEspeciales(List<String> condicionesEspeciales) {
        this.condicionesEspeciales = condicionesEspeciales;
    }
}