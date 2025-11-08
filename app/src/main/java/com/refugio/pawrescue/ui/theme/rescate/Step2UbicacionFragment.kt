package com.refugio.pawrescue.ui.theme.rescate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.refugio.pawrescue.R
import com.refugio.pawrescue.databinding.Step2UbicacionBinding

class Step2UbicacionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: Step2UbicacionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NuevoRescateViewModel by activityViewModels()

    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Step2UbicacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí es donde busca el ID "mapFragment"
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupUI()
        observeViewModel()

        // Request location
        (requireActivity() as? NuevoRescateActivity)?.getCurrentLocation()
    }

    private fun setupUI() {
        binding.btnEditAddress.setOnClickListener {
            // TODO: Implement address editing
        }
    }

    private fun observeViewModel() {
        viewModel.ubicacion.observe(viewLifecycleOwner) { ubicacion ->
            ubicacion?.let {
                binding.tvAddress.text = it.direccion
                binding.tvCity.text = "${it.ciudad}, ${it.estado}"
                binding.tvCoordinates.text = "Lat: ${it.latitud}, Lng: ${it.longitud}"

                updateMap(it.latitud, it.longitud)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        viewModel.ubicacion.value?.let {
            updateMap(it.latitud, it.longitud)
        }
    }

    private fun updateMap(lat: Double, lng: Double) {
        googleMap?.let { map ->
            val location = LatLng(lat, lng)
            map.clear()
            map.addMarker(MarkerOptions().position(location).title("Ubicación del rescate"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            binding.llMapPlaceholder.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
