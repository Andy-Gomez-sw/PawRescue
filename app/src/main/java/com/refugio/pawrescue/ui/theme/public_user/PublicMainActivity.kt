package com.refugio.pawrescue.ui.theme.public_user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.refugio.pawrescue.R
import com.refugio.pawrescue.data.model.repository.Animal
import com.refugio.pawrescue.databinding.ActivityPublicMainBinding
import com.refugio.pawrescue.ui.theme.auth.LoginActivity

class PublicMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublicMainBinding
    private val viewModel: PublicAnimalsViewModel by viewModels()
    private lateinit var adapter: PublicAnimalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadAnimals()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Adopta un Amigo"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.public_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_my_requests -> {
                startActivity(Intent(this, MyAdoptionRequestsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = PublicAnimalsAdapter { animal ->
            showAnimalDetails(animal)
        }

        binding.rvAnimals.apply {
            layoutManager = GridLayoutManager(this@PublicMainActivity, 2)
            adapter = this@PublicMainActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.animales.observe(this) { animales ->
            adapter.submitList(animales)
            updateEmptyState(animales.isEmpty())
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun loadAnimals() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAnimalesDisponibles()
        }
        viewModel.loadAnimalesDisponibles()
    }

    private fun showAnimalDetails(animal: Animal) {
        val intent = Intent(this, AnimalDetailsPublicActivity::class.java)
        intent.putExtra("animal", animal)
        startActivity(intent)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                rvAnimals.visibility = android.view.View.GONE
                llEmptyState.visibility = android.view.View.VISIBLE
            } else {
                rvAnimals.visibility = android.view.View.VISIBLE
                llEmptyState.visibility = android.view.View.GONE
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}