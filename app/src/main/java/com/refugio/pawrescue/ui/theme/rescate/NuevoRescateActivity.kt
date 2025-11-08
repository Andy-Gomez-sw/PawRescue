package com.refugio.pawrescue.ui.theme.rescate

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.ActivityNuevoRescateBinding
import com.refugio.pawrescue.ui.theme.utils.Constants
import com.refugio.pawrescue.ui.theme.utils.NetworkUtils
import java.util.*

class NuevoRescateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoRescateBinding
    private val viewModel: NuevoRescateViewModel by viewModels()
    private lateinit var prefs: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentStep = 0
    private val totalSteps = 4

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido
            Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoRescateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupViewPager()
        setupButtons()
        checkNetworkStatus()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        updateStepIndicator()
    }

    private fun setupViewPager() {
        val adapter = RescateStepsAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // Disable swipe

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentStep = position
                updateStepIndicator()
                updateButtons()
            }
        })
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            if (validateCurrentStep()) {
                if (currentStep < totalSteps - 1) {
                    binding.viewPager.currentItem = currentStep + 1
                } else {
                    saveRescate()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentStep > 0) {
                binding.viewPager.currentItem = currentStep - 1
            }
        }
    }

    private fun updateStepIndicator() {
        binding.tvStepIndicator.text = "${currentStep + 1}/$totalSteps"

        // Update step progress bars
        val steps = listOf(binding.step1, binding.step2, binding.step3, binding.step4)
        steps.forEachIndexed { index, view ->
            view.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    if (index <= currentStep) R.color.primary_green else R.color.border_color
                )
            )
        }
    }

    private fun updateButtons() {
        binding.btnBack.visibility = if (currentStep > 0) View.VISIBLE else View.GONE
        binding.btnNext.text = if (currentStep == totalSteps - 1) {
            "💾 GUARDAR TODO"
        } else {
            "Siguiente →"
        }
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> validateStep1() // Foto
            1 -> validateStep2() // Ubicación
            2 -> validateStep3() // Datos
            3 -> validateStep4() // Notas
            else -> false
        }
    }

    private fun validateStep1(): Boolean {
        if (viewModel.fotoPrincipal.value == null) {
            Toast.makeText(this, "Debes capturar al menos una foto", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateStep2(): Boolean {
        if (viewModel.ubicacion.value == null) {
            Toast.makeText(this, "Debes obtener la ubicación", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateStep3(): Boolean {
        if (viewModel.tipoAnimal.value.isNullOrEmpty()) {
            Toast.makeText(this, "Debes seleccionar el tipo de animal", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateStep4(): Boolean {
        // Opcional, siempre válido
        return true
    }

    private fun saveRescate() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Rescate")
            .setMessage("¿Estás seguro de guardar este rescate?")
            .setPositiveButton("Guardar") { _, _ ->
                val userId = prefs.getString(Constants.KEY_USER_ID, "") ?: ""
                val refugioId = prefs.getString(Constants.KEY_REFUGIO_ID, "") ?: ""

                viewModel.guardarRescate(userId, refugioId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.saveState.observe(this) { state ->
            when (state) {
                is RescateState.Loading -> {
                    // Mostrar loading
                    binding.btnNext.isEnabled = false
                }
                is RescateState.Success -> {
                    Toast.makeText(this, "Rescate guardado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                }
                is RescateState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    binding.btnNext.isEnabled = true
                }
                else -> {
                    binding.btnNext.isEnabled = true
                }
            }
        }
    }

    private fun checkNetworkStatus() {
        val isConnected = NetworkUtils.isNetworkAvailable(this)
        binding.tvOfflineIndicator.visibility = if (isConnected) View.GONE else View.VISIBLE
    }

    fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        viewModel.setUbicacion(
                            location.latitude,
                            location.longitude,
                            address.getAddressLine(0) ?: "",
                            address.locality ?: "",
                            address.adminArea ?: ""
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (currentStep > 0) {
            binding.viewPager.currentItem = currentStep - 1
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar Rescate")
                .setMessage("¿Estás seguro de cancelar? Se perderán todos los datos.")
                .setPositiveButton("Sí, cancelar") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}