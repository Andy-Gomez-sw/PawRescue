package com.refugio.pawrescue.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.data.repository.AnimalRepository;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment;
import com.refugio.pawrescue.ui.admin.tabs.HistoryFragment;
import com.refugio.pawrescue.ui.admin.tabs.InfoFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity para mostrar el detalle, historial y opciones de edición de un Animal (RF-06, RF-07).
 * Utiliza pestañas para organizar la información (similar al mockup "Max #A001").
 */
public class DetalleAnimalActivity extends AppCompatActivity {

    private static final String TAG = "DetalleAnimalActivity";
    private String animalId;
    private Animal currentAnimal;

    // Componentes UI
    private ImageView ivAnimalHeader, icEdit, icShare;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseFirestore db;
    private AnimalRepository animalRepository; // Añadimos el repositorio para operaciones de CRUD

    // Estados del animal (RF-08)
    private final String[] estadosAnimal = {"Rescatado", "En Tratamiento", "Disponible Adopcion", "Adoptado", "Caso Cerrado"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_animal);

        // Inicialización
        db = FirebaseFirestore.getInstance();
        animalRepository = new AnimalRepository();
        animalId = getIntent().getStringExtra("animalId");

        // Enlazar UI
        ivAnimalHeader = findViewById(R.id.iv_animal_header);
        toolbar = findViewById(R.id.toolbar_detail);
        icEdit = findViewById(R.id.ic_edit);
        icShare = findViewById(R.id.ic_share);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // Configurar Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Título se establecerá al cargar el animal
        }

        // Listener de Edición (RF-07, RF-08) -> Muestra diálogo para cambiar estado
        //icEdit.setOnClickListener(v -> mostrarDialogoEdicionEstado());

        // Listener de Exportar/Compartir
        icShare.setOnClickListener(v -> Toast.makeText(this, "Funcionalidad de Exportación (RF-23) - Implementación futura.", Toast.LENGTH_SHORT).show());

        if (animalId != null) {
            cargarDetalleAnimal(animalId);
        } else {
            Toast.makeText(this, "Error: ID de animal no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
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
   /* private void mostrarDialogoEdicionEstado() {
        if (currentAnimal == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Estado del Animal (RF-08)");

        final Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, estadosAnimal);
        spinner.setAdapter(adapter);

        // Seleccionar el estado actual
        int currentPosition = adapter.getPosition(currentAnimal.getEstadoRefugio());
        if (currentPosition >= 0) {
            spinner.setSelection(currentPosition);
        }

        builder.setView(spinner);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String nuevoEstado = spinner.getSelectedItem().toString();
            actualizarEstado(nuevoEstado);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }*/

    /**
     * Llama al repositorio para actualizar el estado del animal en Firestore (RF-08).
     * @param nuevoEstado El nuevo estado seleccionado.
     */
    /*private void actualizarEstado(String nuevoEstado) {
        if (animalId == null || nuevoEstado.equals(currentAnimal.getEstadoRefugio())) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoRefugio", nuevoEstado);

        animalRepository.actualizarAnimal(animalId, updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DetalleAnimalActivity.this, "Estado actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
                        // Recargar datos para actualizar la UI (ya que no estamos escuchando en tiempo real en esta Activity)
                        cargarDetalleAnimal(animalId);
                    } else {
                        Toast.makeText(DetalleAnimalActivity.this, "Error al actualizar estado.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error actualizando estado: ", task.getException());
                    }
                });
    }*/


    /**
     * Carga el objeto Animal desde Firestore usando el ID.
     */
    private void cargarDetalleAnimal(String id) {
        db.collection("animales").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            currentAnimal = task.getResult().toObject(Animal.class);
                            if (currentAnimal != null) {

                                // CORRECCIÓN 1: Formateo del ID Numérico para el título
                                String idDisplay = String.format("#%04d", currentAnimal.getIdNumerico());

                                // Establecer Título de la Toolbar con el nombre del animal
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(currentAnimal.getNombre() + " " + idDisplay);
                                }

                                // Cargar foto con Glide (Asumiendo que la dependencia ya fue agregada)
                                Glide.with(DetalleAnimalActivity.this)
                                        .load(currentAnimal.getFotoUrl())
                                        .placeholder(R.drawable.ic_pet_placeholder)
                                        .error(R.drawable.ic_pet_error)
                                        .into(ivAnimalHeader);

                                // Inicializar Pestañas con el objeto Animal cargado
                                setupViewPagerAndTabs(viewPager, currentAnimal);
                                tabLayout.setupWithViewPager(viewPager);

                            } else {
                                Toast.makeText(DetalleAnimalActivity.this, "Error al mapear datos del animal.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(DetalleAnimalActivity.this, "Animal no encontrado.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error al cargar el documento del animal: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"));
                            finish();
                        }
                    }
                });
    }

    /**
     * Configura el ViewPager con las pestañas de navegación (Info, Historial, Adopciones).
     */
    private void setupViewPagerAndTabs(ViewPager viewPager, Animal animal) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Pestaña 1: Información General (RF-06, Base para Edición RF-07)
        adapter.addFragment(InfoFragment.newInstance(animal), "Información");

        // Pestaña 2: Historial Médico/Cuidados (RF-09, RF-10)
        adapter.addFragment(HistoryFragment.newInstance(animal.getIdAnimal()), "Historial");

        // Pestaña 3: Adopciones (RF-14, RF-16)
        adapter.addFragment(AdoptionFragment.newInstance(animal.getIdAnimal()), "Adopciones");

        viewPager.setAdapter(adapter);
    }

    // Adaptador para el ViewPager (FragmentPagerAdapter)
    static class ViewPagerAdapter extends FragmentPagerAdapter {
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

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}