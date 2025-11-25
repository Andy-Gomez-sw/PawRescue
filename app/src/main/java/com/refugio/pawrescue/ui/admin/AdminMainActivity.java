package com.refugio.pawrescue.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.refugio.pawrescue.R;

// Se asume la existencia de AnimalesListFragment para la navegación
import com.refugio.pawrescue.ui.admin.AnimalesListFragment;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_dashboard) {
            selectedFragment = new AdminDashboardFragment();
        } else if (itemId == R.id.nav_animals) {
            selectedFragment = new AnimalesListFragment();
        } else if (itemId == R.id.nav_volunteers) {
            selectedFragment = new VolunteerManagmentFragment();
        } else if (itemId == R.id.nav_finance) {
            selectedFragment = new FinanzasFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * MÉTODO AÑADIDO: Permite a los fragmentos hijos navegar a la lista de animales
     * aplicando un filtro específico (usado por el Dashboard).
     */
    public void navigateToAnimalListWithFilter(String filterKey) {
        // Selecciona visualmente la pestaña "Animales"
        bottomNavigationView.setSelectedItemId(R.id.nav_animals);

        // Navega al fragmento de lista de animales con el argumento del filtro
        AnimalesListFragment fragment = AnimalesListFragment.newInstance(filterKey);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Permite volver al Dashboard fácilmente
                .commit();
    }
}