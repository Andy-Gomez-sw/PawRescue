package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.refugio.pawrescue.databinding.DialogNuevoVoluntarioBinding
import com.refugio.pawrescue.data.model.Usuario

// EditarVoluntarioDialog
class EditarVoluntarioDialog : DialogFragment() {

    private var _binding: DialogNuevoVoluntarioBinding? = null
    private val binding get() = _binding!!
    private var onVoluntarioEditedListener: (() -> Unit)? = null

    companion object {
        private const val ARG_VOLUNTARIO = "voluntario"

        fun newInstance(voluntario: Usuario): EditarVoluntarioDialog {
            val fragment = EditarVoluntarioDialog()
            val args = Bundle()
            args.putString(ARG_VOLUNTARIO, Gson().toJson(voluntario))
            fragment.arguments = args
            return fragment
        }
    }

    fun setOnVoluntarioEditedListener(listener: () -> Unit) {
        onVoluntarioEditedListener = listener
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

        val voluntarioJson = arguments?.getString(ARG_VOLUNTARIO)
        val voluntario = Gson().fromJson(voluntarioJson, Usuario::class.java)

        voluntario?.let {
            binding.etNombre.setText(it.nombre)
            binding.etEmail.setText(it.email)
            binding.etTelefono.setText(it.telefono)
        }

        binding.btnCancelar.setOnClickListener { dismiss() }
        binding.btnGuardar.setOnClickListener {
            // TODO: Implementar guardado
            onVoluntarioEditedListener?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// DetalleVoluntarioDialog
class DetalleVoluntarioDialog : DialogFragment() {

    private var _binding: DialogNuevoVoluntarioBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_VOLUNTARIO = "voluntario"

        fun newInstance(voluntario: Usuario): DetalleVoluntarioDialog {
            val fragment = DetalleVoluntarioDialog()
            val args = Bundle()
            args.putString(ARG_VOLUNTARIO, Gson().toJson(voluntario))
            fragment.arguments = args
            return fragment
        }
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

        val voluntarioJson = arguments?.getString(ARG_VOLUNTARIO)
        val voluntario = Gson().fromJson(voluntarioJson, Usuario::class.java)

        voluntario?.let {
            binding.etNombre.setText(it.nombre)
            binding.etEmail.setText(it.email)
            binding.etTelefono.setText(it.telefono)

            // Deshabilitar edición
            binding.etNombre.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etTelefono.isEnabled = false
            binding.btnGuardar.visibility = View.GONE
        }

        binding.btnCancelar.text = "Cerrar"
        binding.btnCancelar.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}