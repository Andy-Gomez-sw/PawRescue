package com.refugio.pawrescue.ui.theme.stats

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.refugio.pawrescue.databinding.FragmentStatsBinding
import com.refugio.pawrescue.ui.theme.utils.Constants

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        setupUI()
        loadStats()
    }

    private fun setupUI() {
        binding.btnPeriodo.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar período", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStats() {
        // TODO: Cargar estadísticas reales desde Firestore
        // Por ahora mostramos datos de ejemplo
        binding.tvTotalRescates.text = "12"
        binding.tvTotalAdopciones.text = "8"
        binding.tvTotalAnimales.text = "48"
        binding.tvTotalVoluntarios.text = "15"

        binding.tvPerrosCount.text = "28 (70%)"
        binding.tvGatosCount.text = "10 (25%)"
        binding.tvOtrosCount.text = "2 (5%)"

        binding.pbPerros.progress = 70
        binding.pbGatos.progress = 25
        binding.pbOtros.progress = 5

        binding.tvTiempoAdopcion.text = "45 días"
        binding.tvTiempoRecuperacion.text = "12 días"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}