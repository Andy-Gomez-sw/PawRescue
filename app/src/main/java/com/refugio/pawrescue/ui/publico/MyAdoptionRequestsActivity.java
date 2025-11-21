package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.refugio.pawrescue.R;

public class MyAdoptionRequestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_adoption_requests);

        TextView tvPlaceholder = findViewById(R.id.tvPlaceholder);
        tvPlaceholder.setText("Módulo en desarrollo\n\nAquí verás tus solicitudes de adopción");
    }
}