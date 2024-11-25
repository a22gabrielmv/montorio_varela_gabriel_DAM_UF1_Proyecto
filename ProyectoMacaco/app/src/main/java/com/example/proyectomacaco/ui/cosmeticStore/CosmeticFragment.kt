package com.example.proyectomacaco.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.proyectomacaco.R
import com.example.proyectomacaco.databinding.FragmentHomeBinding
import com.example.proyectomacaco.databinding.FragmentNotificationsBinding
import com.example.proyectomacaco.ui.dashboard.DashboardViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val dashboardViewModel: DashboardViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var sharedPreferences: SharedPreferences
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        dashboardViewModel.loadCosmetics(sharedPreferences)

        dashboardViewModel.bananaCount.observe(viewLifecycleOwner) { count ->
            binding.bananaCounter.text = "Bananas: $count"
        }

        dashboardViewModel.cosmetics.observe(viewLifecycleOwner) { cosmetics ->
            updateCosmeticUI(cosmetics)
        }

        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)

        Glide.with(this)
            .asGif()
            .load(R.drawable.banana_counter)
            .into(binding.bananaGif)

        return root
    }

    private fun updateCosmeticUI(cosmetics: List<DashboardViewModel.Monkey>) {
        cosmetics.forEach { monkey ->
            when (monkey.id) {
                "chimp" -> {
                    binding.chimpImage.setImageResource(R.drawable.monkey_left)
                    binding.chimpName.text = monkey.name
                    binding.chimpCost.text = if (monkey.cost == 0) "The Primate Strategist" else "Cost: ${monkey.cost} Bananas"
                    binding.chimpActionButton.text = when {
                        monkey.isEquipped -> "Equipped"
                        monkey.isPurchased -> "Equip"
                        else -> "Buy (${monkey.cost} Bananas)"
                    }
                    binding.chimpActionButton.isEnabled = !monkey.isEquipped
                    binding.chimpActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
                "gorilla" -> {
                    binding.gorillaImage.setImageResource(R.drawable.gorilla_left)
                    binding.gorillaName.text = monkey.name
                    binding.gorillaCost.text = "The Titan of the Jungle"
                    binding.gorillaActionButton.text = if (monkey.isPurchased) {
                        if (monkey.isEquipped) "Equipped" else "Equip"
                    } else {
                        "Buy (${monkey.cost} Bananas)"
                    }
                    binding.gorillaActionButton.isEnabled = !monkey.isEquipped
                    binding.gorillaActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
                "orangutan" -> {
                    binding.orangutanImage.setImageResource(R.drawable.orangutan_left)
                    binding.orangutanName.text = monkey.name
                    binding.orangutanCost.text = "The Sage of the Canopy"
                    binding.orangutanActionButton.text = if (monkey.isPurchased) {
                        if (monkey.isEquipped) "Equipped" else "Equip"
                    } else {
                        "Buy (${monkey.cost} Bananas)"
                    }
                    binding.orangutanActionButton.isEnabled = !monkey.isEquipped
                    binding.orangutanActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
                "macaque" -> {
                    binding.macaqueImage.setImageResource(R.drawable.macaque_left)
                    binding.macaqueName.text = monkey.name
                    binding.macaqueCost.text = "The Trickster of the Tropics"
                    binding.macaqueActionButton.text = if (monkey.isPurchased) {
                        if (monkey.isEquipped) "Equipped" else "Equip"
                    } else {
                        "Buy (${monkey.cost} Bananas)"
                    }
                    binding.macaqueActionButton.isEnabled = !monkey.isEquipped
                    binding.macaqueActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
            }
        }
    }

    private fun handleCosmeticAction(monkey: DashboardViewModel.Monkey) {
        if (!monkey.isPurchased) {
            if (dashboardViewModel.buyCosmetic(monkey.id)) {
                dashboardViewModel.buyMediaPlayer(requireContext(), R.raw.buy)
                showToast("${monkey.name} purchased!")
            } else {
                showToast("Not enough bananas!")
            }
        } else {
            if (!monkey.isEquipped) {
                dashboardViewModel.equipCosmetic(monkey.id)
                showToast("${monkey.name} equipped!")
            }
        }
        dashboardViewModel.saveCosmetics(sharedPreferences)
    }


    private fun showToast(message: String) {
        toast?.cancel()

        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message

        toast = Toast(requireContext()).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)
    }

    override fun onPause() {
        super.onPause()
        saveData()
        dashboardViewModel.stopBackgroundMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        dashboardViewModel.stopBackgroundMediaPlayer()
    }

    private fun saveData() {
        dashboardViewModel.saveCosmetics(sharedPreferences)

        sharedPreferences.edit().apply {
            putInt("banana_count", dashboardViewModel.bananaCount.value ?: 0)
            apply()
        }
    }
}