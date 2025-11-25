package com.refugio.pawrescue.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Animal implements Serializable {

    // --- ID PRINCIPAL (Usado por Firebase) ---
    @DocumentId
    private String id;

    // --- CAMPOS ADMINISTRATIVOS (Para RegistroAnimalActivity) ---
    private long idNumerico;
    private String idVoluntario;
    private Date fechaRegistro;

    // --- CAMPOS DE DATOS ---
    private String nombre;
    private String especie;
    private String raza;
    private String edadAprox; // Texto: "2 años", "8 meses"
    private String estadoRefugio;
    private String estadoSalud;
    private String sexo;
    private String descripcion;
    private String personalidad;
    private String fotoUrl;

    // Campos para filtros de Galería
    private boolean urgente;

    @PropertyName("ubicacionRescate")
    private String ubicacionRescate;

    private Date fechaAdopcion;

    @PropertyName("fotos_estado")
    private List<String> fotosUrls;

    private List<String> condicionesEspeciales;

    // Constructor vacío
    public Animal() {}

    // =============================================================
    // MÉTODOS NUEVOS (Para arreglar el error de Galería)
    // =============================================================

    /**
     * Convierte el texto de edadAprox a un número para el filtro de "Cachorros".
     * Si dice "meses", devuelve 0. Si dice "años", devuelve el número.
     */
    public int getEdad() {
        if (edadAprox == null) return 0;

        String texto = edadAprox.toLowerCase();
        if (texto.contains("mes") || texto.contains("sem")) {
            return 0; // Es un bebé (< 1 año)
        }

        try {
            // Extrae solo los números del texto (ej: "3 años" -> 3)
            String numero = texto.replaceAll("\\D+", "");
            return numero.isEmpty() ? 0 : Integer.parseInt(numero);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isUrgente() {
        return urgente;
    }
    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    // =============================================================
    // MÉTODOS ADMINISTRATIVOS
    // =============================================================

    public long getIdNumerico() { return idNumerico; }
    public void setIdNumerico(long idNumerico) { this.idNumerico = idNumerico; }

    public String getIdVoluntario() { return idVoluntario; }
    public void setIdVoluntario(String idVoluntario) { this.idVoluntario = idVoluntario; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // =============================================================
    // RESTO DE GETTERS Y SETTERS (Compatibilidad total)
    // =============================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Alias para admin
    public void setIdAnimal(String id) { this.id = id; }
    public String getIdAnimal() { return id; }

    public String getNombre() { return nombre != null ? nombre : "Sin Nombre"; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza != null ? raza : "Raza única"; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getEdadTexto() { return edadAprox != null ? edadAprox : "Edad desconocida"; }
    public void setEdadAprox(String edadAprox) { this.edadAprox = edadAprox; }
    public String getEdadAprox() { return edadAprox; }

    public String getEstadoRefugio() { return estadoRefugio; }
    public void setEstadoRefugio(String estadoRefugio) { this.estadoRefugio = estadoRefugio; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPersonalidad() { return personalidad != null ? personalidad : ""; }
    public void setPersonalidad(String personalidad) { this.personalidad = personalidad; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getTamano() { return "Mediano"; }

    public String getUbicacionRescate() { return ubicacionRescate; }
    public void setUbicacionRescate(String ubicacionRescate) { this.ubicacionRescate = ubicacionRescate; }

    public String getFechaRescate() { return "Reciente"; }

    public List<String> getFotosUrls() {
        if (fotosUrls == null || fotosUrls.isEmpty()) {
            List<String> temp = new ArrayList<>();
            if (fotoUrl != null) temp.add(fotoUrl);
            return temp;
        }
        return fotosUrls;
    }
    public void setFotosUrls(List<String> fotosUrls) { this.fotosUrls = fotosUrls; }

    public List<String> getCondicionesEspeciales() { return condicionesEspeciales; }
    public void setCondicionesEspeciales(List<String> condicionesEspeciales) { this.condicionesEspeciales = condicionesEspeciales; }

    public String getEstadoSalud() { return estadoSalud; }
    public void setEstadoSalud(String estadoSalud) { this.estadoSalud = estadoSalud; }

    public Date getFechaAdopcion() { return fechaAdopcion; }
    public void setFechaAdopcion(Date fechaAdopcion) { this.fechaAdopcion = fechaAdopcion; }

    // Compatibilidad UI
    public boolean isFavorited() { return false; }
    public void setFavorited(boolean fav) {}
}