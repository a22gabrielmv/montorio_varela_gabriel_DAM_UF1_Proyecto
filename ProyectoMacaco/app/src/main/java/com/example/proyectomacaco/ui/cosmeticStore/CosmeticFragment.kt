package com.example.proyectomacaco.ui.cosmeticStore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.proyectomacaco.R
import com.example.proyectomacaco.databinding.FragmentCosmeticBinding
import com.example.proyectomacaco.ui.mainGame.GameViewModel

class CosmeticFragment : Fragment() {

    private lateinit var binding: FragmentCosmeticBinding

    private val gameViewModel: GameViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var sharedPreferences: SharedPreferences
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCosmeticBinding.inflate(inflater, container, false)
        val root = binding.root

        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        gameViewModel.loadCosmetics(sharedPreferences)

        gameViewModel.bananaCount.observe(viewLifecycleOwner) { count ->
            binding.bananaCounter.text = "Bananas: $count"
        }

        gameViewModel.cosmetics.observe(viewLifecycleOwner) { cosmetics ->
            updateCosmeticUI(cosmetics)
        }

        gameViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)

        Glide.with(this)
            .asGif()
            .load(R.drawable.banana_counter)
            .into(binding.bananaGif)

        return root
    }

    private fun updateCosmeticUI(cosmetics: List<GameViewModel.Monkey>) {
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

                    val colorRes = if (monkey.isEquipped) R.color.teal_200 else R.color.button_default
                    binding.chimpActionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

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

                    val colorRes = if (monkey.isEquipped) R.color.teal_200 else R.color.button_default
                    binding.gorillaActionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

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

                    val colorRes = if (monkey.isEquipped) R.color.teal_200 else R.color.button_default
                    binding.orangutanActionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

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

                    val colorRes = if (monkey.isEquipped) R.color.teal_200 else R.color.button_default
                    binding.macaqueActionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

                    binding.macaqueActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
                "albino" -> {
                    binding.albinoImage.setImageResource(R.drawable.albino_left)
                    binding.albinoName.text = monkey.name
                    binding.albinoCost.text = "The Miracle of the Genes"
                    binding.albinoActionButton.text = if (monkey.isPurchased) {
                        if (monkey.isEquipped) "Equipped" else "Equip"
                    } else {
                        "Buy (${monkey.cost} Bananas)"
                    }
                    binding.albinoActionButton.isEnabled = !monkey.isEquipped

                    val colorRes = if (monkey.isEquipped) R.color.teal_200 else R.color.button_default
                    binding.albinoActionButton.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

                    binding.albinoActionButton.setOnClickListener {
                        handleCosmeticAction(monkey)
                    }
                }
            }
        }
    }

    private fun handleCosmeticAction(monkey: GameViewModel.Monkey) {
        if (!monkey.isPurchased) {
            if (gameViewModel.buyCosmetic(monkey.id)) {
                gameViewModel.buyMediaPlayer(requireContext(), R.raw.buy)
                showToast("${monkey.name} purchased!")
            } else {
                showToast("Not enough bananas!")
            }
        } else {
            if (!monkey.isEquipped) {
                gameViewModel.equipCosmetic(monkey.id)
                showToast("${monkey.name} equipped!")
            }
        }
        gameViewModel.saveCosmetics(sharedPreferences)
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
        gameViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)
    }

    override fun onPause() {
        super.onPause()
        saveData()
        gameViewModel.stopBackgroundMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        gameViewModel.stopBackgroundMediaPlayer()
    }

    private fun saveData() {
        gameViewModel.saveCosmetics(sharedPreferences)

        sharedPreferences.edit().apply {
            putInt("banana_count", gameViewModel.bananaCount.value ?: 0)
            apply()
        }
    }
}