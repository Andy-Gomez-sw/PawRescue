package com.refugio.pawrescue.ui.theme.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.databinding.DialogGestionarSolicitudBinding
import java.util.Calendar
import java.util.Date
import androidx.fragment.app.viewModels


// Usamos el ViewModel compartido del Fragment que muestra la lista
// Asumimos que se llama SolicitudesAdopcionViewModel
class GestionarSolicitudDialog : BottomSheetDialogFragment() {

    private var _binding: DialogGestionarSolicitudBinding? = null
    private val binding get() = _binding!!

    // Usamos el ViewModel del fragmento padre (el que tiene la lista de solicitudes)
// LÍNEA CORREGIDA:
    private val viewModel: SolicitudesAdopcionViewModel by viewModels({ requireParentFragment() })
    private lateinit var solicitud: SolicitudAdopcion
    private val calendario = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogGestionarSolicitudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // El ViewModel debe tener un LiveData para la solicitud seleccionada
        viewModel.solicitudSeleccionada.observe(viewLifecycleOwner) { solicitudSeleccionada ->
            solicitud = solicitudSeleccionada
            setupUI()
        }
    }

    private fun setupUI() {
        binding.tvInfoSolicitud.text = "Solicitante: ${solicitud.solicitanteNombre}\nPara: ${solicitud.animalNombre}"
        binding.etNotasAdmin.setText(solicitud.notasEvaluacion)

        binding.btnRechazar.setOnClickListener {
            val notas = binding.etNotasAdmin.text.toString().trim()
            viewModel.rechazarSolicitud(solicitud.id, notas)
            Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnAgendar.setOnClickListener {
            abrirSelectorFecha()
        }
    }

    private fun abrirSelectorFecha() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendario.set(Calendar.YEAR, year)
            calendario.set(Calendar.MONTH, month)
            calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            abrirSelectorHora()
        }

        DatePickerDialog(
            requireContext(),
            dateListener,
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun abrirSelectorHora() {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendario.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendario.set(Calendar.MINUTE, minute)

            // ¡Tenemos la fecha y hora!
            val fechaCita = calendario.time
            val notas = binding.etNotasAdmin.text.toString().trim()

            // Llamamos al ViewModel para que guarde todo
            viewModel.agendarEntrevista(solicitud.id, fechaCita, notas)
            Toast.makeText(requireContext(), "Entrevista agendada", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        TimePickerDialog(
            requireContext(),
            timeListener,
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true // Formato 24hs
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // El Fragment que lista las solicitudes debe llamar a esto
    // ej: viewModel.seleccionar(solicitud)
    //    GestionarSolicitudDialog().show(childFragmentManager, "tag")
}