package com.refugio.pawrescue.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.admin.AnimalesListFragment;
import com.refugio.pawrescue.ui.admin.AdminDashboardFragment;
import com.refugio.pawrescue.ui.admin.ProfileFragment; // NUEVO IMPORT
import com.refugio.pawrescue.ui.admin.tabs.AdoptionFragment;
import com.refugio.pawrescue.ui.volunteer.VolunteerMainActivity;

/**
 * Activity Principal para el rol de Administrador.
 * Contiene la navegación principal a los módulos del Administrador (RF-04).
 */
public class AdminMainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar el Fragmento de Dashboard por defecto
        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_dashboard) {
                    selectedFragment = new AdminDashboardFragment(); // Panel de Estadísticas
                } else if (item.getItemId() == R.id.nav_animals) {
                    selectedFragment = new AnimalesListFragment(); // CRUD de Animales
                } else if (item.getItemId() == R.id.nav_volunteers) {
                    // Módulo 5: Gestión de Adopciones
                    selectedFragment = new VolunteerManagmentFragment();
                } else if (item.getItemId() == R.id.nav_finance) {
                    // Módulo 4.4: Gestión de Donaciones
                    selectedFragment = new FinanzasFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    // NUEVO: Perfil de Usuario/Mi Cuenta
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}