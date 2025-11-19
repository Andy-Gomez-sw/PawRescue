package com.refugio.pawrescue.ui.admin;

import static com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment.newInstance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment;
import com.refugio.pawrescue.ui.admin.tabs.HistoryFragment;
import com.refugio.pawrescue.ui.admin.tabs.InfoFragment;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_animal);

        // Inicialización
        db = FirebaseFirestore.getInstance();
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

        // Listener de Edición (RF-07)
        icEdit.setOnClickListener(v -> Toast.makeText(this, "Iniciando Edición (RF-07) - Implementación futura.", Toast.LENGTH_SHORT).show());

        // Listener de Exportar/Compartir
        icShare.setOnClickListener(v -> Toast.makeText(this, "Funcionalidad de Exportación (RF-23) - Implementación futura.", Toast.LENGTH_SHORT).show());

        if (animalId != null) {
            cargarDetalleAnimal(animalId);
        } else {
            Toast.makeText(this, "Error: ID de animal no proporcionado.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

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
                                // Establecer Título de la Toolbar con el nombre del animal
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(currentAnimal.getNombre() + " #" + currentAnimal.getIdAnimal());
                                }

                                // Cargar foto con Glide
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
        adapter.addFragment(newInstance(animal.getIdAnimal()), "Adopciones");

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