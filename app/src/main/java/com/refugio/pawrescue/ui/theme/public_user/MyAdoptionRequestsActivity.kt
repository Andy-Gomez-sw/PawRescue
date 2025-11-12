package com.refugio.pawrescue.ui.theme.public_user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refugio.pawrescue.data.model.SolicitudAdopcion
import com.refugio.pawrescue.databinding.ActivityMyAdoptionRequestsBinding

class MyAdoptionRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAdoptionRequestsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: MyAdoptionRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAdoptionRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadSolicitudes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = MyAdoptionRequestsAdapter()
        binding.rvSolicitudes.layoutManager = LinearLayoutManager(this)
        binding.rvSolicitudes.adapter = adapter
    }

    private fun loadSolicitudes() {
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = android.view.View.VISIBLE

        firestore.collection("solicitudes_adopcion")
            .whereEqualTo("solicitanteEmail", auth.currentUser?.email)
            .get()
            .addOnSuccessListener { snapshot ->
                val solicitudes = snapshot.documents.mapNotNull {
                    it.toObject(SolicitudAdopcion::class.java)
                }

                adapter.submitList(solicitudes)
                updateStats(solicitudes)
                binding.progressBar.visibility = android.view.View.GONE

                if (solicitudes.isEmpty()) {
                    binding.llEmptyState.visibility = android.view.View.VISIBLE
                    binding.rvSolicitudes.visibility = android.view.View.GONE
                } else {
                    binding.llEmptyState.visibility = android.view.View.GONE
                    binding.rvSolicitudes.visibility = android.view.View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = android.view.View.GONE
            }
    }

    private fun updateStats(solicitudes: List<SolicitudAdopcion>) {
        binding.tvTotalSolicitudes.text = solicitudes.size.toString()
        binding.tvPendientes.text = solicitudes.count { it.estadoString == "pendiente" }.toString()
        binding.tvAprobadas.text = solicitudes.count { it.estadoString == "aprobada" }.toString()
    }
}