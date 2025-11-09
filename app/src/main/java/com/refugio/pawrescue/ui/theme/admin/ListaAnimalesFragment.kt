package com.refugio.pawrescue.ui.theme.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.refugio.pawrescue.databinding.FragmentListaAnimalesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListaAnimalesFragment : Fragment() {

    private var _binding: FragmentListaAnimalesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaAnimalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}