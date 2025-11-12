package com.refugio.pawrescue.ui.theme.public_user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.EstadoSolicitud
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.databinding.DialogSolicitudAdopcionBinding
import java.util.*

class SolicitudAdopcionDialog : BottomSheetDialogFragment() {

    private var _binding: DialogSolicitudAdopcionBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var animalId: String = ""

    companion object {
        private const val ARG_ANIMAL_ID = "animal_id"

        fun newInstance(animalId: String): SolicitudAdopcionDialog {
            val fragment = SolicitudAdopcionDialog()
            val args = Bundle()
            args.putString(ARG_ANIMAL_ID, animalId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animalId = arguments?.getString(ARG_ANIMAL_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSolicitudAdopcionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupButtons()
        cargarDatosUsuario()
    }

    private fun setupDropdowns() {
        val tiposVivienda = arrayOf("Casa propia", "Casa rentada", "Departamento", "Otro")
        val adapterVivienda = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposVivienda)
        binding.actvTipoVivienda.setAdapter(adapterVivienda)
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener { dismiss() }
        binding.btnEnviar.setOnClickListener { enviarSolicitud() }
    }

    private fun cargarDatosUsuario() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: ""
                val email = doc.getString("email") ?: ""
                val telefono = doc.getString("telefono") ?: ""

                binding.etNombre.setText(nombre)
                binding.etEmail.setText(email)
                binding.etTelefono.setText(telefono)
            }
    }

    private fun enviarSolicitud() {
        if (!validarFormulario()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnEnviar.isEnabled = false

        val solicitud = SolicitudAdopcion(
            id = firestore.collection("solicitudes_adopcion").document().id,
            animalId = animalId,
            solicitanteNombre = binding.etNombre.text.toString(),
            solicitanteEmail = binding.etEmail.text.toString(),
            solicitanteTelefono = binding.etTelefono.text.toString(),
            solicitanteEdad = binding.etEdad.text.toString().toIntOrNull() ?: 0,
            solicitanteDireccion = binding.etDireccion.text.toString(),
            tipoVivienda = binding.actvTipoVivienda.text.toString(),
            tieneJardin = binding.cbTieneJardin.isChecked,
            otrosAnimales = binding.etOtrosAnimales.text.toString(),
            experienciaMascotas = binding.etExperiencia.text.toString(),
            motivoAdopcion = binding.etMotivo.text.toString(),
            estado = EstadoSolicitud.PENDIENTE,
            fechaSolicitud = Date()
        )

        firestore.collection("solicitudes_adopcion")
            .document(solicitud.id)
            .set(solicitud)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "✅ Solicitud enviada exitosamente", Toast.LENGTH_LONG).show()
                dismiss()
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnEnviar.isEnabled = true
            }
    }

    private fun validarFormulario(): Boolean {
        // Validaciones básicas
        if (binding.etNombre.text.isNullOrEmpty()) {
            binding.tilNombre.error = "Campo requerido"
            return false
        }
        if (binding.etEmail.text.isNullOrEmpty()) {
            binding.tilEmail.error = "Campo requerido"
            return false
        }
        if (binding.etTelefono.text.isNullOrEmpty()) {
            binding.tilTelefono.error = "Campo requerido"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}