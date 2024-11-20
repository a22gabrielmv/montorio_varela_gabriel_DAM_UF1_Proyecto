package com.example.proyectomacaco.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.proyectomacaco.R
import com.example.proyectomacaco.databinding.FragmentNotificationsBinding
import com.example.proyectomacaco.ui.dashboard.DashboardViewModel

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val dashboardViewModel: DashboardViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root = binding.root

        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        dashboardViewModel.loadImprovements(sharedPreferences)

        dashboardViewModel.bananaCount.observe(viewLifecycleOwner) { count ->
            binding.bananaCounter.text = "Bananas: $count"
        }

        dashboardViewModel.improvements.observe(viewLifecycleOwner) { levels ->
            binding.improvementEfficiencyDescription.text =
                "Efficiency Improvement (Level ${levels["efficiency"] ?: 0}): ${if (dashboardViewModel.isImprovementMaxed("efficiency")) "MAX Level Reached" else "Makes the bar easier to fill"}"
            binding.improvementPassiveDescription.text =
                "Passive Improvement (Level ${levels["passive"] ?: 0}): ${if (dashboardViewModel.isImprovementMaxed("passive")) "MAX Level Reached" else "The monkey moves on its own periodically"}"
            binding.improvementAfkDescription.text =
                "AFK Improvement (Level ${levels["afk"] ?: 0}): ${if (dashboardViewModel.isImprovementMaxed("afk")) "MAX Level Reached" else "Automatically generates bananas while away"}"
        }

        dashboardViewModel.improvements.observe(viewLifecycleOwner) { levels ->
            val maxedEfficiency = dashboardViewModel.isImprovementMaxed("efficiency")
            val maxedPassive = dashboardViewModel.isImprovementMaxed("passive")
            val maxedAfk = dashboardViewModel.isImprovementMaxed("afk")

            binding.buyEfficiency.text = if (maxedEfficiency) "MAX" else "Buy Efficiency Improvement (${dashboardViewModel.costs.value?.get("efficiency")} Bananas)"
            binding.buyPassive.text = if (maxedPassive) "MAX" else "Buy Passive Improvement (${dashboardViewModel.costs.value?.get("passive")} Bananas)"
            binding.buyAfk.text = if (maxedAfk) "MAX" else "Buy AFK Improvement (${dashboardViewModel.costs.value?.get("afk")} Bananas)"

            binding.buyEfficiency.isEnabled = !maxedEfficiency
            binding.buyPassive.isEnabled = !maxedPassive
            binding.buyAfk.isEnabled = !maxedAfk
        }

        binding.buyEfficiency.setOnClickListener {
            dashboardViewModel.buyImprovement("efficiency")
            dashboardViewModel.buyMediaPlayer(requireContext(), R.raw.buy)
        }
        binding.buyPassive.setOnClickListener {
            dashboardViewModel.buyImprovement("passive")
            dashboardViewModel.buyMediaPlayer(requireContext(), R.raw.buy)
        }
        binding.buyAfk.setOnClickListener {
            dashboardViewModel.buyImprovement("afk")
            dashboardViewModel.buyMediaPlayer(requireContext(), R.raw.buy)
        }

        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)

        return root
    }

    private fun saveData() {
        dashboardViewModel.saveImprovements(sharedPreferences)
        sharedPreferences.edit().apply {
            putInt("banana_count", dashboardViewModel.bananaCount.value ?: 0)
            apply()
        }
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

    override fun onResume() {
        super.onResume()
        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_store)
    }
}

