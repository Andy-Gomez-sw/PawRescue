package com.refugio.pawrescue.ui.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.model.Animal;
import com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment;
import com.refugio.pawrescue.ui.admin.tabs.HistoryFragment;
import com.refugio.pawrescue.ui.admin.tabs.InfoFragment;
import com.refugio.pawrescue.ui.volunteer.VolunteerCitasFragment;


import java.util.ArrayList;
import java.util.List;

public class VolunteerAnimalDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ANIMAL = "extra_animal";

    private ImageView ivAnimalHeader;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Animal animal;
    private ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Usamos EXACTAMENTE el mismo layout que el admin
        setContentView(R.layout.activity_detalle_animal);

        // -------- Toolbar (igual que admin) --------
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // -------- Referencias UI principales --------
        ivAnimalHeader = findViewById(R.id.iv_animal_header);
        tabLayout = findViewById(R.id.tab_layout);   // mismos ids que en el layout de admin
        viewPager = findViewById(R.id.view_pager);   // mismos ids que en el layout de admin

        // 🔹 Ocultar acciones solo de administrador (iconos de la barra superior)
        ImageView icShare = findViewById(R.id.ic_share);
        ImageView icAssign = findViewById(R.id.ic_assign_volunteer);
        if (icShare != null) icShare.setVisibility(ImageView.GONE);
        if (icAssign != null) icAssign.setVisibility(ImageView.GONE);

        // 🔹 Ocultar FAB de acciones rápidas (editar, etc.) para el voluntario
        FloatingActionButton fab = findViewById(R.id.fab_action);
        if (fab != null) {
            fab.hide();
        }

        // -------- Recuperar Animal del intent --------
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_ANIMAL)) {
            Toast.makeText(this, "No se encontró la información del animal.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        animal = (Animal) intent.getSerializableExtra(EXTRA_ANIMAL);
        if (animal == null) {
            Toast.makeText(this, "Error al cargar el animal.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Título con el nombre del animal (como admin)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animal.getNombre());
        }

        // -------- Imagen de cabecera --------
        if (ivAnimalHeader != null) {
            if (animal.getFotoUrl() != null && !animal.getFotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(animal.getFotoUrl())
                        .placeholder(R.mipmap.ic_launcher)  // icono de la app como placeholder
                        .error(R.mipmap.ic_launcher)
                        .into(ivAnimalHeader);
            } else {
                ivAnimalHeader.setImageResource(R.mipmap.ic_launcher);
            }
        }

        // -------- Tabs + ViewPager (misma lógica que admin, pero solo lectura) --------
        if (tabLayout != null && viewPager != null) {
            setupViewPagerAndTabs(viewPager, animal);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            Toast.makeText(this,
                    "Error al inicializar las pestañas de información.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupViewPagerAndTabs(ViewPager viewPager, Animal animal) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Pestaña 1: Información general
        pagerAdapter.addFragment(InfoFragment.newInstance(animal), "Información");

        // Pestaña 2: Historial médico/cuidados
        pagerAdapter.addFragment(HistoryFragment.newInstance(animal.getIdAnimal()), "Historial");

        // Pestaña 3: Adopciones
        pagerAdapter.addFragment(VolunteerCitasFragment.newInstance(animal.getIdAnimal()), "Citas");

        viewPager.setAdapter(pagerAdapter);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    // Flecha de regreso del toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
