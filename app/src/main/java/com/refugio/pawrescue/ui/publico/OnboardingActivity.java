package com.refugio.pawrescue.ui.publico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.refugio.pawrescue.R;
import com.refugio.pawrescue.ui.publico.OnboardingItem;
import com.refugio.pawrescue.ui.adapter.OnboardingAdapter;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsIndicator;
    private MaterialButton btnVerAnimales;
    private MaterialButton btnCrearCuenta;
    private MaterialButton btnIniciarSesion;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerOnboarding);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        btnVerAnimales = findViewById(R.id.btnVerAnimales);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
    }

    private void setupViewPager() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(
                R.drawable.ic_heart,
                "Encuentra tu compañero ideal",
                "Miles de animales esperan por un hogar lleno de amor"
        ));
        items.add(new OnboardingItem(
                R.drawable.ic_home,
                "Proceso simple y seguro",
                "Te guiamos en cada paso de la adopción"
        ));
        items.add(new OnboardingItem(
                R.drawable.ic_phone,
                "Seguimiento completo",
                "Mantente conectado después de la adopción"
        ));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        setupDotsIndicator(items.size());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void setupDotsIndicator(int count) {
        dotsIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsIndicator.addView(dot);
        }

        if (count > 0) {
            dotsIndicator.getChildAt(0).setBackgroundResource(R.drawable.dot_active);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            View dot = dotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_active);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = 32;
                dot.setLayoutParams(params);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                params.width = 8;
                dot.setLayoutParams(params);
            }
        }
    }

    private void setupButtons() {
        btnVerAnimales.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, GalleryActivity.class);
            startActivity(intent);
            finish();
        });

        btnCrearCuenta.setOnClickListener(v -> {
            // Por ahora ir directo a la galería
            Intent intent = new Intent(OnboardingActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        btnIniciarSesion.setOnClickListener(v -> {
            // Por ahora ir directo a la galería
            Intent intent = new Intent(OnboardingActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }
}