package com.refugio.pawrescue.ui.theme.rescate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RescateStepsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Step1FotoFragment()
            1 -> Step2UbicacionFragment()
            2 -> Step3DatosFragment()
            3 -> Step4NotasFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}