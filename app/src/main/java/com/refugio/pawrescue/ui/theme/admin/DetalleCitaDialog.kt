package com.refugio.pawrescue.ui.theme.admin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.refugio.pawrescue.databinding.DialogDetalleCitaBinding
import com.refugio.pawrescue.data.model.SolicitudAdopcion

class DetalleCitaDialog : DialogFragment() {

    private var _binding: DialogDetalleCitaBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_CITA = "arg_cita"

        fun newInstance(cita: SolicitudAdopcion): DetalleCitaDialog {
            val fragment = DetalleCitaDialog()
            val args = Bundle()
            // Usamos Gson para serializar el objeto complejo, una solución rápida y efectiva
            args.putString(ARG_CITA, Gson().toJson(cita))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDetalleCitaBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val citaJson = arguments?.getString(ARG_CITA)
        val cita = Gson().fromJson(citaJson, SolicitudAdopcion::class.java)

        cita?.let {
            binding.apply {
                tvNombre.text = "Nombre: ${it.nombreSolicitante}"
                tvEmail.text = "Email: ${it.emailSolicitante}"
                tvTelefono.text = "Tel: ${it.telefonoSolicitante}"
                tvDireccion.text = "Dirección: ${it.direccionSolicitante}"

                tvVivienda.text = "Tipo de vivienda: ${it.tipoVivienda}"
                tvPatio.text = "Tiene patio: ${if (it.tienePatio) "Sí" else "No"}"
                tvOtrasMascotas.text = "Otras mascotas: ${if (it.tieneOtrasMascotas) "Sí (${it.detallesOtrasMascotas})" else "No"}"

                tvMotivo.text = it.motivoAdopcion

                btnCerrar.setOnClickListener { dismiss() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}