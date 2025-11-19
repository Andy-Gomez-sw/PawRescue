package com.refugio.pawrescue.data.helper;

import android.graphics.Bitmap;
import android.net.Uri;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data = baos.toByteArray();

        final String imagePath = "animal_photos/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference(imagePath);

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Log.e(TAG, "Fallo al subir foto a Storage: " + exception.getMessage());
            callback.onFailure("Error al subir la fotografía.");
        }).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                animal.setFotoUrl(uri.toString());
                // Llamar a la función que gestiona la transacción del ID numérico
                guardarAnimalEnFirestoreConContador(animal, callback);
            });
        });
    }

    /**
     * Guarda el objeto Animal en la colección "animales" usando una transacción
     * para obtener y asignar el ID numérico incremental.
     */
    private void guardarAnimalEnFirestoreConContador(final Animal animal, final GuardadoAnimalCallback callback) {
        final DocumentReference counterRef = db.collection("counters").document("animales_id");
        final DocumentReference newAnimalRef = db.collection("animales").document();

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
            Log.d(TAG, "Registro de animal exitoso: ID Numérico " + animal.getIdNumerico());
            callback.onSuccess("Animal registrado con ID: #" + animal.getIdNumerico());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Fallo en la transacción de guardado: " + e.getMessage());
            callback.onFailure("Error al guardar el expediente en la base de datos y generar ID numérico.");
        });
    }

    /**
     * Clase interna para mapear el contador de ID en Firestore.
     */
    private static class AnimalCounter {
        public long currentId;
        public AnimalCounter() {}
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