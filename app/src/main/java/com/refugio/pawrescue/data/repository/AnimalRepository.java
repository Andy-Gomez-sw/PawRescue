package com.refugio.pawrescue.data.repository;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.refugio.pawrescue.model.Animal;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de datos para la entidad Animal.
 * Se encarga de la comunicación con Firebase Firestore (Lectura, CRUD futuro).
 * Utiliza ListenerRegistration para obtener datos en tiempo real (onSnapshot, RF-06).
 */
public class AnimalRepository {

    private static final String TAG = "AnimalRepository";
    private final CollectionReference animalCollection;

    public AnimalRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.animalCollection = db.collection("animales");
    }

    /**
     * Obtiene una lista completa de animales y escucha los cambios en tiempo real (RF-06).
     * @param listener Callback para entregar la lista de animales o errores.
     * @return Objeto ListenerRegistration para poder detener la escucha.
     */
    public ListenerRegistration getAnimalesEnTiempoReal(final AnimalesListener listener) {
        // Ordenamos por fecha de registro descendente para mostrar los más recientes primero.
        Query query = animalCollection.orderBy("fechaRegistro", Query.Direction.DESCENDING);

        return query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error al escuchar cambios en animales: " + error.getMessage());
                listener.onError("Error al cargar la lista de animales.");
                return;
            }

            if (value != null) {
                List<Animal> animales = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        // Mapeo directo del documento de Firestore a nuestro POJO Animal
                        Animal animal = doc.toObject(Animal.class);
                        // CRUCIAL: Asignar el ID del documento (lo que en el modelo es idAnimal)
                        animal.setIdAnimal(doc.getId());
                        animales.add(animal);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear documento a Animal: " + e.getMessage());
                    }
                }
                listener.onAnimalesLoaded(animales);
            }
        });
    }

    /**
     * NUEVO MÉTODO: Obtiene la lista de animales que requieren atención veterinaria urgente (RF-09, RF-10).
     * Filtra animales en estado "En Tratamiento".
     * @param listener Callback para entregar la lista de animales o errores.
     * @return Objeto ListenerRegistration para poder detener la escucha.
     */
    public ListenerRegistration getAnimalesConAtencionUrgente(final AnimalesListener listener) {
        // Filtramos por estado "En Tratamiento" (campo correcto: estadoRefugio)
        Query query = animalCollection
                .whereEqualTo("estadoRefugio", "En Tratamiento")
                .orderBy("fechaRegistro", Query.Direction.DESCENDING);

        return query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error al escuchar animales urgentes: " + error.getMessage());
                listener.onError("Error al cargar la lista de animales con atención urgente: " + error.getMessage());
                return;
            }

            if (value != null) {
                List<Animal> animales = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Animal animal = doc.toObject(Animal.class);
                        // Asignar el ID del documento
                        animal.setIdAnimal(doc.getId());
                        animales.add(animal);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear documento a Animal: " + e.getMessage());
                    }
                }
                listener.onAnimalesLoaded(animales);
            }
        });
    }

    /**
     * NUEVO MÉTODO: Obtiene la lista de animales con medicación activa (RF-10).
     * Filtra animales que tienen condiciones especiales registradas.
     * @param listener Callback para entregar la lista de animales o errores.
     * @return Objeto ListenerRegistration para poder detener la escucha.
     */
    public ListenerRegistration getAnimalesConMedicacion(final AnimalesListener listener) {
        // Obtenemos todos los animales y filtramos en el cliente
        // (Firestore no permite filtrar por arrays no vacíos directamente)
        Query query = animalCollection.orderBy("fechaRegistro", Query.Direction.DESCENDING);

        return query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error al escuchar animales con medicación: " + error.getMessage());
                listener.onError("Error al cargar animales con medicación: " + error.getMessage());
                return;
            }

            if (value != null) {
                List<Animal> animales = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Animal animal = doc.toObject(Animal.class);
                        animal.setIdAnimal(doc.getId());

                        // Filtrar solo los que tienen condiciones especiales
                        if (animal.getCondicionesEspeciales() != null &&
                                !animal.getCondicionesEspeciales().isEmpty()) {
                            animales.add(animal);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear documento a Animal: " + e.getMessage());
                    }
                }
                listener.onAnimalesLoaded(animales);
            }
        });
    }

    /**
     * Interfaz de callback para notificar los resultados de la consulta.
     */
    public interface AnimalesListener {
        void onAnimalesLoaded(List<Animal> animales);
        void onError(String message);
    }

    // --- Métodos de CRUD futuros (Update/Delete) irán aquí en el Módulo 3.4 ---
}