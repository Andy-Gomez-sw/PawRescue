package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.databinding.DialogNuevaTransaccionBinding
import java.util.*

class NuevaTransaccionDialog : BottomSheetDialogFragment() {

    private var _binding: DialogNuevaTransaccionBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private var onTransaccionCreatedListener: (() -> Unit)? = null
    // CORRECCIÓN: Usar el enum TipoTransaccion en lugar de String
    private var tipoTransaccion: TipoTransaccion = TipoTransaccion.DONACION

    companion object {
        private const val ARG_TIPO = "tipo"

        fun newInstance(tipo: String): NuevaTransaccionDialog {
            val fragment = NuevaTransaccionDialog()
            val args = Bundle()
            args.putString(ARG_TIPO, tipo)
            fragment.arguments = args
            return fragment
        }
    }

    fun setOnTransaccionCreatedListener(listener: () -> Unit) {
        onTransaccionCreatedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNuevaTransaccionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CORRECCIÓN: Convertir el String recibido en argumentos al enum TipoTransaccion
        val tipoString = arguments?.getString(ARG_TIPO) ?: "ingreso"
        tipoTransaccion = if (tipoString == "ingreso") TipoTransaccion.DONACION else TipoTransaccion.GASTO

        setupUI()
        setupCategoriasDropdown()
        setupButtons()
    }

    private fun setupUI() {
        // CORRECCIÓN: Comparar con el enum
        binding.tvTitulo.text = if (tipoTransaccion == TipoTransaccion.DONACION) {
            "💰 Nueva Donación"
        } else {
            "💸 Nuevo Gasto"
        }
    }

    private fun setupCategoriasDropdown() {
        // CORRECCIÓN: Comparar con el enum
        val categorias = if (tipoTransaccion == TipoTransaccion.DONACION) {
            arrayOf("Donación individual", "Donación empresarial", "Evento", "Otros ingresos")
        } else {
            arrayOf("Alimentación", "Veterinaria", "Medicamentos", "Limpieza", "Mantenimiento", "Otros gastos")
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categorias
        )
        binding.actvCategoria.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnGuardar.setOnClickListener {
            if (validarFormulario()) {
                guardarTransaccion()
            }
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true

        if (binding.etConcepto.text.isNullOrEmpty()) {
            binding.tilConcepto.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilConcepto.error = null
        }

        if (binding.etMonto.text.isNullOrEmpty()) {
            binding.tilMonto.error = "Campo requerido"
            isValid = false
        } else {
            binding.tilMonto.error = null
        }

        if (binding.actvCategoria.text.isNullOrEmpty()) {
            binding.tilCategoria.error = "Selecciona una categoría"
            isValid = false
        } else {
            binding.tilCategoria.error = null
        }

        return isValid
    }

    private fun guardarTransaccion() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        val transaccionId = firestore.collection("transacciones").document().id

        val transaccion = Transaccion(
            id = transaccionId,
            tipo = tipoTransaccion, // Ahora 'tipoTransaccion' es del tipo correcto (enum)
            concepto = binding.etConcepto.text.toString(),
            monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0,
            fecha = Date(),
            categoria = binding.actvCategoria.text.toString(),
            descripcion = binding.etDescripcion.text.toString()
        )

        firestore.collection("transacciones")
            .document(transaccionId)
            .set(transaccion)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                onTransaccionCreatedListener?.invoke()
                dismiss()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnGuardar.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}