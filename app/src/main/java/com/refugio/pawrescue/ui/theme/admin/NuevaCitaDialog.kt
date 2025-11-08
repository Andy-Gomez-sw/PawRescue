package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.DialogNuevaCitaBinding
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import java.util.*

class NuevaCitaDialog : BottomSheetDialogFragment() {

    private var _binding: DialogNuevaCitaBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private var onCitaCreatedListener: (() -> Unit)? = null
    private val animalesList = mutableListOf<String>()

    fun setOnCitaCreatedListener(listener: () -> Unit) {
        onCitaCreatedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNuevaCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAnimales()
        setupViviendaDropdown()
        setupButtons()
    }

    private fun loadAnimales() {
        firestore.collection("animales")
            .whereEqualTo("estadoAdopcion", "disponible")
            .get()
            .addOnSuccessListener { documents ->
                animalesList.clear()
                documents.forEach { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    if (nombre.isNotEmpty()) {
                        animalesList.add(nombre)
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    animalesList
                )
                binding.actvAnimal.setAdapter(adapter)
            }
    }

    private fun setupViviendaDropdown() {
        val tiposVivienda = arrayOf("Casa propia", "Casa rentada", "Departamento", "Otro")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            tiposVivienda
        )
        binding.actvTipoVivienda.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnGuardar.setOnClickListener {
            if (validarFormulario()) {
                guardarCita()
            }
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true

        if (binding.etNombreSolicitante.text.isNullOrEmpty()) {
            binding.tilNombreSolicitante.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilNombreSolicitante.error = null
        }

        if (binding.etEmail.text.isNullOrEmpty()) {
            binding.tilEmail.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (binding.etTelefono.text.isNullOrEmpty()) {
            binding.tilTelefono.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilTelefono.error = null
        }

        if (binding.actvAnimal.text.isNullOrEmpty()) {
            binding.tilAnimal.error = "Selecciona un animal"
            isValid = false
        } else {
            binding.tilAnimal.error = null
        }

        if (binding.etEdad.text.isNullOrEmpty()) {
            binding.tilEdad.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilEdad.error = null
        }

        return isValid
    }

    private fun guardarCita() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        val citaId = firestore.collection("solicitudes_adopcion").document().id

        val solicitud = SolicitudAdopcion(
            id = citaId,
            animalNombre = binding.actvAnimal.text.toString(),
            solicitanteNombre = binding.etNombreSolicitante.text.toString(),
            solicitanteEmail = binding.etEmail.text.toString(),
            solicitanteTelefono = binding.etTelefono.text.toString(),
            solicitanteEdad = binding.etEdad.text.toString().toIntOrNull() ?: 0,
            solicitanteDireccion = binding.etDireccion.text.toString(),
            tipoVivienda = binding.actvTipoVivienda.text.toString(),
            tieneJardin = binding.cbTieneJardin.isChecked,
            otrosAnimales = binding.etOtrosAnimales.text.toString(),
            numeroPersonas = binding.etNumeroPersonas.text.toString().toIntOrNull() ?: 1,
            hayNinos = binding.cbHayNinos.isChecked,
            experienciaMascotas = binding.etExperiencia.text.toString(),
            motivoAdopcion = binding.etMotivo.text.toString(),
            estado = "pendiente",
            fechaSolicitud = Date(),
            puntuacionAutomatica = calcularPuntuacion()
        )

        firestore.collection("solicitudes_adopcion")
            .document(citaId)
            .set(solicitud)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                onCitaCreatedListener?.invoke()
                dismiss()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calcularPuntuacion(): Int {
        var puntuacion = 50 // Base

        // Puntos por vivienda
        when (binding.actvTipoVivienda.text.toString()) {
            "Casa propia" -> puntuacion += 20
            "Casa rentada" -> puntuacion += 15
            "Departamento" -> puntuacion += 10
        }

        // Puntos por jardín
        if (binding.cbTieneJardin.isChecked) puntuacion += 10

        // Puntos por experiencia
        val experiencia = binding.etExperiencia.text.toString()
        if (experiencia.length > 50) puntuacion += 10

        // Puntos por motivación
        val motivo = binding.etMotivo.text.toString()
        if (motivo.length > 50) puntuacion += 10

        return puntuacion.coerceIn(0, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}