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

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * Clase auxiliar para encapsular operaciones complejas de Firebase.
 * Maneja la subida de imágenes a Storage y el guardado de datos en Firestore.
 * VERSIÓN CORREGIDA con mejor manejo de errores y compresión optimizada.
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth mAuth;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
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
     * Interfaz de callback para manejar el resultado del registro.
     */
    public interface GuardadoAnimalCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}