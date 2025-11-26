package com.refugio.pawrescue.data.helper;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.model.Cita;
import com.refugio.pawrescue.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * Clase auxiliar para encapsular operaciones complejas de Firebase.
 * Implementa el patrón Singleton para asegurar una única instancia global.
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    // 1. Instancia estática de la clase
    private static FirebaseHelper instance;

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth mAuth;

    /**
     * Constructor privado. Solo se llama una vez desde getInstance().
     */
    public FirebaseHelper() { // 🟢 CORREGIDO: Debe ser privado
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Método estático para obtener la instancia del Singleton.
     */
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * Getter para obtener la instancia de Firestore, usado por Fragments/Activities.
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Sube la imagen del animal a Firebase Storage y luego guarda el registro del Animal en Firestore,
     * gestionando el ID numérico incremental en una transacción.
     * @param animal El objeto Animal a guardar.
     * @param bitmap La imagen capturada como Bitmap.
     * @param callback Interfaz para manejar el resultado.
     */
    public void registrarAnimalConFoto(final Animal animal, Bitmap bitmap, final GuardadoAnimalCallback callback) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap es null, no se puede subir");
            callback.onFailure("Error: La imagen no está disponible.");
            return;
        }

        // Comprimir imagen de forma optimizada
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Calcular calidad basada en el tamaño de la imagen
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int totalPixels = width * height;

            // Calidad de compresión: menor para imágenes más grandes
            int quality;
            if (totalPixels > 4000000) { // Imágenes muy grandes (>4MP)
                quality = 60;
            } else if (totalPixels > 2000000) { // Imágenes grandes (>2MP)
                quality = 70;
            } else {
                quality = 80; // Imágenes normales
            }

            Log.d(TAG, "Comprimiendo imagen: " + width + "x" + height + " pixels, calidad: " + quality);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] data = baos.toByteArray();

            // Verificar tamaño del archivo
            float sizeMB = data.length / (1024f * 1024f);
            Log.d(TAG, "Tamaño de imagen comprimida: " + String.format("%.2f MB", sizeMB));

            if (sizeMB > 10) {
                callback.onFailure("Error: La imagen es demasiado grande (máx 10MB). Intente con otra foto.");
                return;
            }

            // Generar nombre único para la imagen
            final String imagePath = "animal_photos/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference(imagePath);

            Log.d(TAG, "Iniciando subida a Storage: " + imagePath);

            // Subir imagen a Firebase Storage
            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                Log.d(TAG, "Progreso de subida: " + String.format("%.1f%%", progress));
            });

            uploadTask.addOnFailureListener(exception -> {
                Log.e(TAG, "Fallo al subir foto a Storage: " + exception.getMessage());
                exception.printStackTrace();
                callback.onFailure("Error al subir la fotografía: " + exception.getMessage());
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Foto subida exitosamente a Storage");

                // Obtener URL de descarga de la imagen
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "URL de descarga obtenida: " + downloadUrl);

                    animal.setFotoUrl(downloadUrl);

                    // Ahora guardar el animal en Firestore con el contador
                    guardarAnimalEnFirestoreConContador(animal, callback);

                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener URL de descarga: " + e.getMessage());
                    callback.onFailure("Error al obtener URL de la foto: " + e.getMessage());
                });
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Error al procesar la imagen: " + e.getMessage());
        }
    }

    /**
     * Guarda el objeto Animal en la colección "animales" usando una transacción
     * para obtener y asignar el ID numérico incremental.
     */
    private void guardarAnimalEnFirestoreConContador(final Animal animal, final GuardadoAnimalCallback callback) {
        final DocumentReference counterRef = db.collection("counters").document("animales_id");
        final DocumentReference newAnimalRef = db.collection("animales").document();

        Log.d(TAG, "Iniciando transacción para guardar animal");

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. Leer el contador actual
            DocumentSnapshot snapshot = transaction.get(counterRef);
            long newId = 1;

            if (snapshot.exists()) {
                Long currentId = snapshot.getLong("currentId");
                if (currentId != null) {
                    newId = currentId + 1;
                }
            }

            Log.d(TAG, "Nuevo ID numérico asignado: " + newId);

            // 2. Actualizar el contador (Incrementar en 1)
            transaction.set(counterRef, new AnimalCounter(newId));

            // 3. Asignar IDs al objeto Animal
            animal.setIdNumerico(newId);
            animal.setIdAnimal(newAnimalRef.getId());

            if (mAuth.getCurrentUser() != null) {
                animal.setIdVoluntario(mAuth.getCurrentUser().getUid());
            }

            // 4. Guardar el objeto Animal con el nuevo ID numérico
            transaction.set(newAnimalRef, animal);
            return null;

        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "✅ Registro de animal exitoso: ID Numérico #" + animal.getIdNumerico());
            callback.onSuccess("¡Animal registrado exitosamente! ID: #" + String.format("%04d", animal.getIdNumerico()));

        }).addOnFailureListener(e -> {
            Log.e(TAG, "❌ Fallo en la transacción de guardado: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Error al guardar en la base de datos: " + e.getMessage());
        });
    }

    /**
     * Actualiza los datos de un animal existente en Firestore (RF-07).
     * @param animalId ID del documento del animal.
     * @param updates Mapa con los campos a actualizar.
     * @param callback Interfaz para manejar el resultado.
     */
    public void actualizarAnimal(String animalId, Map<String, Object> updates, final GuardadoAnimalCallback callback) {
        db.collection("animales").document(animalId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("✅ Expediente actualizado exitosamente.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar animal: ", e);
                    callback.onFailure("❌ Error al actualizar expediente: " + e.getMessage());
                });
    }


    /**
     * Clase interna para mapear el contador de ID en Firestore.
     */
    private static class AnimalCounter {
        public long currentId;

        public AnimalCounter() {
            // Constructor vacío requerido por Firestore
        }

        public AnimalCounter(long currentId) {
            this.currentId = currentId;
        }
    }

    /**
     * Interfaz de callback para manejar el resultado del registro de ANIMALES.
     */
    public interface GuardadoAnimalCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    // ============================================================================================
    //                                  NUEVOS MÉTODOS PARA USUARIOS
    // ============================================================================================

    /**
     * Registra un usuario asignándole un ID numérico autoincremental.
     * Usa la colección "counters" -> documento "usuarios_id"
     */
    public void registrarUsuarioConContador(final Usuario usuario, final RegistroUsuarioCallback callback) {
        final DocumentReference counterRef = db.collection("counters").document("usuarios_id");
        final DocumentReference userRef = db.collection("usuarios").document(usuario.getUid());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. Leer el contador actual
            DocumentSnapshot snapshot = transaction.get(counterRef);
            long newId = 1;

            if (snapshot.exists()) {
                Long currentId = snapshot.getLong("currentId");
                if (currentId != null) {
                    newId = currentId + 1;
                }
            }

            // 2. Actualizar el contador en Firestore
            Map<String, Object> counterData = new HashMap<>();
            counterData.put("currentId", newId);
            transaction.set(counterRef, counterData);

            // 3. Asignar el nuevo ID al objeto usuario
            usuario.setIdNumerico(newId);

            // 4. Guardar el usuario con el ID ya asignado
            transaction.set(userRef, usuario);

            return null;
        }).addOnSuccessListener(aVoid -> {
            callback.onSuccess(usuario.getIdNumerico());
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
        });
    }

    /**
     * Interfaz de callback para manejar el resultado del registro de USUARIOS.
     */
    public interface RegistroUsuarioCallback {
        void onSuccess(long idGenerado);
        void onFailure(String error);
    }

    public void actualizarUsuario(String uid, Map<String, Object> updates, final OperacionUsuarioCallback callback) {
        db.collection("usuarios").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("✅ Datos de usuario actualizados exitosamente.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar usuario: ", e);
                    callback.onFailure("❌ Error al actualizar usuario: " + e.getMessage());
                });
    }

    /**
     * Interfaz de callback para operaciones de usuario (Update/Delete).
     */
    public interface OperacionUsuarioCallback {
        void onSuccess(String message);

        void onFailure(String error);
    }

    // ============================================================================================
    //                                  MÉTODOS PARA ADOPCIÓN/CITAS
    // ============================================================================================

    /**
     * Guarda un nuevo documento de Cita en la colección "citas".
     */
    public void addCita(final Cita cita, final CitaAddListener listener) {
        if (cita.getFechaCreacion() == null) {
            cita.setFechaCreacion(new Date());
        }

        db.collection("citas")
                .add(cita)
                .addOnSuccessListener(documentReference -> {
                    listener.onCitaAdded(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear la cita: " + e.getMessage());
                    listener.onError(e);
                });
    }

    /**
     * Interfaz de callback para manejar el resultado de la adición de CITAS.
     */
    public interface CitaAddListener {
        void onCitaAdded(String citaId);
        void onError(Exception e);
    }

    /**
     * 🟢 NUEVO: Actualiza campos de un documento de Cita existente (usado para asignar voluntario o actualizar reporte).
     * @param citaId ID del documento de Cita.
     * @param updates Mapa con los campos a actualizar (voluntarioId, estado, reporteId, etc.).
     * @param listener Interfaz para manejar el resultado.
     */
    public void updateCita(String citaId, Map<String, Object> updates, final CitaUpdateListener listener) {
        db.collection("citas").document(citaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onCitaUpdated();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar la cita: ", e);
                    listener.onError(e);
                });
    }

    /**
     * Interfaz de callback para manejar el resultado de la actualización de CITAS.
     */
    public interface CitaUpdateListener {
        void onCitaUpdated();
        void onError(Exception e);
    }
}