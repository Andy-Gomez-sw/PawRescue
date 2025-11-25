package com.refugio.pawrescue.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo de datos para registrar transacciones financieras (Donaciones/Gastos).
 * Corresponde a los Requisitos Funcionales RF-21 y RF-22.
 */
public class Transaccion implements Serializable {
    private String idTransaccion;
    private String tipo; // "Donacion" o "Gasto"
    private double monto;
    private String categoria; // Ej: "Alimento", "Veterinaria", "Efectivo"
    private String descripcion;
    private Timestamp fecha;
    private String idUsuarioRegistro; // UID del administrador/coordinador que registra

    // Constructor vacío requerido por Firebase Firestore
    public Transaccion() {
    }

    // Getters y Setters
    public String getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(String idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getIdUsuarioRegistro() {
        return idUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(String idUsuarioRegistro) {
        this.idUsuarioRegistro = idUsuarioRegistro;
    }
}