package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.databinding.DialogNuevoVoluntarioBinding
import com.refugio.pawrescue.data.model.Usuario
import com.refugio.pawrescue.ui.theme.utils.Constants
import java.util.*

class NuevoVoluntarioDialog : BottomSheetDialogFragment() {

    private var _binding: DialogNuevoVoluntarioBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var onVoluntarioCreatedListener: (() -> Unit)? = null

    fun setOnVoluntarioCreatedListener(listener: () -> Unit) {
        onVoluntarioCreatedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNuevoVoluntarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupButtons()
    }

    private fun setupDropdowns() {
        // Roles
        val roles = arrayOf("Voluntario", "Coordinador", "Admin")
        val rolesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roles
        )
        binding.actvRol.setAdapter(rolesAdapter)
        binding.actvRol.setText("Voluntario", false)

        // Turnos
        val turnos = arrayOf("Mañana", "Tarde", "Noche", "Fines de semana")
        val turnosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            turnos
        )
        binding.actvTurno.setAdapter(turnosAdapter)
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnGuardar.setOnClickListener {
            if (validarFormulario()) {
                crearVoluntario()
            }
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true

        if (binding.etNombre.text.isNullOrEmpty()) {
            binding.tilNombre.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (binding.etEmail.text.isNullOrEmpty()) {
            binding.tilEmail.error = "Campo requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
            binding.tilEmail.error = "Email inválido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (binding.etPassword.text.isNullOrEmpty()) {
            binding.tilPassword.error = "Campo requerido"
            isValid = false
        } else if (binding.etPassword.text.toString().length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (binding.etTelefono.text.isNullOrEmpty()) {
            binding.tilTelefono.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilTelefono.error = null
        }

        return isValid
    }

    private fun crearVoluntario() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                // Crear documento en Firestore
                val rolSeleccionado = binding.actvRol.text.toString().lowercase()

                val usuario = Usuario(
                    id = userId,
                    nombre = binding.etNombre.text.toString(),
                    email = email,
                    telefono = binding.etTelefono.text.toString(),
                    rol = rolSeleccionado,
                    refugioId = "default_refugio", // TODO: Obtener refugio del usuario actual
                    turno = binding.actvTurno.text.toString(),
                    activo = true,
                    animalesAsignados = emptyList(),
                    fechaRegistro = Date(),
                    ultimaConexion = Date()
                )

                firestore.collection(Constants.COLLECTION_USUARIOS)
                    .document(userId)
                    .set(usuario)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Voluntario creado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        onVoluntarioCreatedListener?.invoke()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnGuardar.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Error al crear usuario: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}