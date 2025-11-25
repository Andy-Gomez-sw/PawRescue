package com.refugio.pawrescue.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment;
import com.refugio.pawrescue.ui.admin.tabs.HistoryFragment;
import com.refugio.pawrescue.ui.admin.tabs.InfoFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import com.refugio.pawrescue.ui.admin.AssignVolunteerActivity;


public class DetalleAnimalActivity extends AppCompatActivity {

    private static final String TAG = "DetalleAnimalActivity";
    private String animalId;
    private Animal currentAnimal;

    private Animal animal;
    // Componentes UI
    private ImageView ivAnimalHeader, icEdit, icShare;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fabAction;
    private FirebaseFirestore db;

    // NUEVO: Referencia al adaptador
    private ViewPagerAdapter pagerAdapter;

    // Estados del animal (RF-08)
    private final String[] estadosAnimal = {
            "Rescatado",
            "En Tratamiento",
            "Disponible Adopcion",
            "En Proceso Adopción",
            "Adoptado",
            "Caso Cerrado"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_animal);

        // Inicialización
        db = FirebaseFirestore.getInstance();
        animalId = getIntent().getStringExtra("animalId");
        animal = (Animal) getIntent().getSerializableExtra("animal");
        // Enlazar UI
        ivAnimalHeader = findViewById(R.id.iv_animal_header);
        toolbar = findViewById(R.id.toolbar_detail);
        icEdit = findViewById(R.id.ic_edit);
        icShare = findViewById(R.id.ic_share);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabAction = findViewById(R.id.fab_action);

        ImageView btnAsignarVoluntario = findViewById(R.id.ic_assign_volunteer);

        btnAsignarVoluntario.setOnClickListener(v -> {
            if (animalId == null || animalId.isEmpty()) {
                Toast.makeText(this, "Error: no se pudo obtener el ID del animal.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, AssignVolunteerActivity.class);
            intent.putExtra("idAnimal", animalId);
            intent.putExtra("nombreAnimal", currentAnimal != null ? currentAnimal.getNombre() : "Animal");
            startActivity(intent);
        });

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Listener de Edición (RF-07) -> Navega a RegistroAnimalActivity en modo edición
        icEdit.setOnClickListener(v -> {
            if (currentAnimal != null) {
                Intent editIntent = new Intent(DetalleAnimalActivity.this, RegistroAnimalActivity.class);
                editIntent.putExtra("animalId", animalId);
                editIntent.putExtra("isEditMode", true);
                startActivity(editIntent);
            } else {
                Toast.makeText(DetalleAnimalActivity.this, "Esperando datos del animal...", Toast.LENGTH_SHORT).show();
            }
        });

        // FAB también permite cambiar estado (función más rápida y controlada)
        fabAction.setOnClickListener(v -> mostrarDialogoEdicionEstado());

        // Listener de Exportar/Compartir
        icShare.setOnClickListener(v -> Toast.makeText(this,
                "Funcionalidad de Exportación (RF-23) - Implementación futura.",
                Toast.LENGTH_SHORT).show());

        if (animalId != null) {
            cargarDetalleAnimal(animalId);
        } else {
            Toast.makeText(this, "Error: ID de animal no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar el detalle cuando se regresa de la edición
        if (animalId != null) {
            cargarDetalleAnimal(animalId);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Muestra un diálogo para que el Administrador cambie el estado oficial del animal (RF-08).
     */
    private void mostrarDialogoEdicionEstado() {
        if (currentAnimal == null) {
            Toast.makeText(this, "Esperando datos del animal...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Estado del Animal");

        // Crear un layout personalizado con Spinner
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_estado, null);
        final Spinner spinnerEstado = dialogView.findViewById(R.id.spinner_estado);

        // Configurar el Spinner con los estados
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, estadosAnimal);
        spinnerEstado.setAdapter(adapter);

        // Seleccionar el estado actual
        String estadoActual = currentAnimal.getEstadoRefugio();
        for (int i = 0; i < estadosAnimal.length; i++) {
            if (estadosAnimal[i].equals(estadoActual)) {
                spinnerEstado.setSelection(i);
                break;
            }
        }

        builder.setView(dialogView);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String nuevoEstado = spinnerEstado.getSelectedItem().toString();
            if (!nuevoEstado.equals(estadoActual)) {
                actualizarEstado(nuevoEstado);
            } else {
                Toast.makeText(this, "El estado no ha cambiado.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Actualiza el estado del animal en Firestore (RF-08).
     */
    private void actualizarEstado(String nuevoEstado) {
        if (animalId == null || currentAnimal == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoRefugio", nuevoEstado);
        updates.put("fechaUltimaActualizacion", new Timestamp(new Date()));

        db.collection("animales").document(animalId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetalleAnimalActivity.this,
                            "✅ Estado actualizado a: " + nuevoEstado,
                            Toast.LENGTH_SHORT).show();

                    // Actualizar el objeto local
                    currentAnimal.setEstadoRefugio(nuevoEstado);

                    // Recargar datos para actualizar toda la UI
                    cargarDetalleAnimal(animalId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetalleAnimalActivity.this,
                            "❌ Error al actualizar estado: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error actualizando estado: ", e);
                });
    }

    /**
     * Carga el objeto Animal desde Firestore.
     */
    private void cargarDetalleAnimal(String id) {
        db.collection("animales").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            currentAnimal = task.getResult().toObject(Animal.class);
                            if (currentAnimal != null) {
                                // Asegurar que el ID del documento esté en el objeto
                                currentAnimal.setIdAnimal(task.getResult().getId());

                                // Formateo del ID Numérico
                                String idDisplay = String.format(Locale.US, "#%04d", currentAnimal.getIdNumerico());

                                // Establecer Título
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(currentAnimal.getNombre() + " " + idDisplay);
                                }

                                // Cargar foto con Glide
                                Glide.with(DetalleAnimalActivity.this)
                                        .load(currentAnimal.getFotoUrl())
                                        .placeholder(R.drawable.ic_pet_placeholder)
                                        .error(R.drawable.ic_pet_error)
                                        .into(ivAnimalHeader);

                                // CAMBIO IMPORTANTE: Si el adaptador ya existe, actualizar fragmentos
                                if (pagerAdapter != null) {
                                    actualizarFragmentos(currentAnimal);
                                } else {
                                    // Primera carga: Inicializar Pestañas
                                    setupViewPagerAndTabs(viewPager, currentAnimal);
                                    tabLayout.setupWithViewPager(viewPager);
                                }

                            } else {
                                Toast.makeText(DetalleAnimalActivity.this,
                                        "Error al mapear datos del animal.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(DetalleAnimalActivity.this,
                                    "Animal no encontrado.",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error al cargar el documento: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Desconocido"));
                        }
                    }
                });
    }

    /**
     * NUEVO: Actualiza los fragmentos existentes con los nuevos datos
     */
    private void actualizarFragmentos(Animal animal) {
        if (pagerAdapter == null) return;

        // Obtener los fragmentos actuales y actualizarlos
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            Fragment fragment = pagerAdapter.getItem(i);

            if (fragment instanceof InfoFragment) {
                // Recrear el fragmento de información con los nuevos datos
                Fragment nuevoFragment = InfoFragment.newInstance(animal);
                pagerAdapter.replaceFragment(i, nuevoFragment);
            }
            // Los otros fragmentos (Historial, Adopciones) no necesitan actualizarse
            // porque cargan sus propios datos desde Firestore
        }

        pagerAdapter.notifyDataSetChanged();

        // Forzar la recreación de la vista actual
        int currentItem = viewPager.getCurrentItem();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentItem);
    }

    /**
     * Configura el ViewPager con las pestañas.
     */
    private void setupViewPagerAndTabs(ViewPager viewPager, Animal animal) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Pestaña 1: Información General (RF-06)
        pagerAdapter.addFragment(InfoFragment.newInstance(animal), "Información");

        // Pestaña 2: Historial Médico/Cuidados (RF-09, RF-10)
        pagerAdapter.addFragment(HistoryFragment.newInstance(animal.getIdAnimal()), "Historial");

        // Pestaña 3: Adopciones (RF-14, RF-16)
        pagerAdapter.addFragment(AdoptionFragment.newInstance(animal.getIdAnimal()), "Adopciones");

        viewPager.setAdapter(pagerAdapter);
    }

    // Adaptador para el ViewPager - MODIFICADO
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        // NUEVO: Método para reemplazar un fragmento
        public void replaceFragment(int position, Fragment fragment) {
            if (position >= 0 && position < mFragmentList.size()) {
                mFragmentList.set(position, fragment);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        // IMPORTANTE: Forzar la actualización de fragmentos
        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}