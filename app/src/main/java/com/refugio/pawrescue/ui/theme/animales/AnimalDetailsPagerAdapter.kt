package com.refugio.pawrescue.ui.theme.animales

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.refugio.pawrescue.ui.theme.animales.tabs.AnimalInfoFragment
import com.refugio.pawrescue.ui.theme.animales.tabs.AnimalCuidadosFragment
import com.refugio.pawrescue.ui.theme.animales.tabs.AnimalHistorialFragment

class AnimalDetailsPagerAdapter(
    fragment: Fragment,
    private val animalId: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnimalInfoFragment.newInstance(animalId)
            1 -> AnimalCuidadosFragment.newInstance(animalId)
            2 -> AnimalHistorialFragment.newInstance(animalId)
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}

