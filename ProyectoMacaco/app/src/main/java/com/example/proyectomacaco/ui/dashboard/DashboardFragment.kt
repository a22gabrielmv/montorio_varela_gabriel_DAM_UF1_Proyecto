package com.example.proyectomacaco.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.proyectomacaco.R
import com.example.proyectomacaco.databinding.FragmentDashboardBinding
import kotlin.math.min
import kotlin.random.Random


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var monkeyMediaPlayer: MediaPlayer? = null
    private var isMonkeyFacingLeft = false
    private var isPlayingSound = false
    private var progressValue = 0
    private lateinit var sharedPreferences: SharedPreferences

    private val handler = Handler(Looper.getMainLooper())
    private var passiveRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        dashboardViewModel.loadImprovements(sharedPreferences)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val bananaCount = loadBananaCount()
        val experience = loadExperience()
        dashboardViewModel.setBananaCount(bananaCount)
        dashboardViewModel.setExperience(experience)

        dashboardViewModel.bananaCount.observe(viewLifecycleOwner) { count ->
            binding.bananaCounter.text = "Bananas: $count"
        }

        dashboardViewModel.experience.observe(viewLifecycleOwner) { exp ->
            binding.exp.text = "Exp: $exp"
        }

        dashboardViewModel.rank.observe(viewLifecycleOwner) { rank ->
            binding.rank.text = "Rank: $rank"
        }

        dashboardViewModel.expToNextRank.observe(viewLifecycleOwner) { expRemaining ->
            binding.expRemaining.text = "Next Rank in: $expRemaining Exp"
        }

        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_main_game)

        binding.monkey.setOnClickListener {
            playMonkeySoundIfNotPlaying()
            toggleMonkeyImage()
        }

        return root
    }

    private fun playMonkeySoundIfNotPlaying() {
        if (!isPlayingSound) {
            val randomSoundResId = when (Random.nextInt(6)) {
                0 -> R.raw.sound1
                1 -> R.raw.sound2
                2 -> R.raw.sound3
                3 -> R.raw.sound4
                4 -> R.raw.sound5
                else -> R.raw.sound6
            }

            monkeyMediaPlayer = MediaPlayer.create(requireContext(), randomSoundResId)
            isPlayingSound = true
            monkeyMediaPlayer?.start()

            monkeyMediaPlayer?.setOnCompletionListener {
                isPlayingSound = false
                monkeyMediaPlayer?.release()
                monkeyMediaPlayer = null
            }
        }
    }

    private fun toggleMonkeyImage() {
        if (isMonkeyFacingLeft) {
            binding.monkey.setImageResource(R.drawable.monkey_right)
        } else {
            binding.monkey.setImageResource(R.drawable.monkey_left)
        }
        isMonkeyFacingLeft = !isMonkeyFacingLeft

        val efficiencyLevel = dashboardViewModel.improvements.value?.get("efficiency") ?: 0
        val baseIncrement = 20 + (efficiencyLevel * 5)

        progressValue += baseIncrement

        while (progressValue >= 105) {
            dashboardViewModel.increaseBananaCount()
            progressValue -= 105
        }

        binding.progressBar.progress = progressValue
    }

    private fun startPassiveImprovement() {
        val passiveLevel = dashboardViewModel.improvements.value?.get("passive") ?: 0

        val interval = when (passiveLevel) {
            0 -> 5000L
            1 -> 2000L
            2 -> 1500L
            3 -> 1000L
            4 -> 800L
            5 -> 500L
            6 -> 300L
            7 -> 150L
            8 -> 50L
            else -> 5000L
        }

        passiveRunnable?.let { handler.removeCallbacks(it) }

        passiveRunnable = object : Runnable {
            override fun run() {
                playMonkeySoundIfNotPlaying()
                toggleMonkeyImage()
                handler.postDelayed(this, interval)
            }
        }

        handler.postDelayed(passiveRunnable!!, interval)
    }


    private fun loadBananaCount(): Int {
        return sharedPreferences.getInt("banana_count", 0)
    }

    private fun loadExperience(): Int {
        return sharedPreferences.getInt("experience", 0)
    }

    private fun saveData() {
        sharedPreferences.edit().apply {
            putInt("banana_count", dashboardViewModel.bananaCount.value ?: 0)
            putInt("experience", dashboardViewModel.experience.value ?: 0)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        monkeyMediaPlayer?.release()
        monkeyMediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        saveData()
        dashboardViewModel.stopBackgroundMediaPlayer()
        isPlayingSound = false
        passiveRunnable?.let { handler.removeCallbacks(it) }

        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong("last_closed_time", currentTime).apply()
    }


    override fun onStop() {
        super.onStop()
        dashboardViewModel.stopBackgroundMediaPlayer()
        passiveRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.initializeMediaPlayer(requireContext(), R.raw.background_main_game)

        startPassiveImprovement()
        handleAfkGeneration()
    }


    private fun resetGameData() {
        dashboardViewModel.setBananaCount(0)
        dashboardViewModel.setExperience(0)

        val defaultImprovements = mutableMapOf(
            "efficiency" to 0,
            "passive" to 0,
            "afk" to 0
        )
        dashboardViewModel.improvements.value = defaultImprovements

        val defaultCosts = mutableMapOf(
            "efficiency" to 50,
            "passive" to 100,
            "afk" to 200
        )
        dashboardViewModel.costs.value = defaultCosts

        sharedPreferences.edit().apply {
            putInt("banana_count", 0)
            putInt("experience", 0)
            putInt("bananas_spent", 0)
            putString("rank", "Recruit")
            putInt("exp_to_next_rank", 10)

            defaultImprovements.forEach { (type, level) ->
                putInt("improvement_${type}_level", level)
            }

            defaultCosts.forEach { (type, cost) ->
                putInt("improvement_${type}_cost", cost)
            }

            apply()
        }

        progressValue = 0
        binding.progressBar.progress = 0

        Toast.makeText(requireContext(), "Game reset successfully!", Toast.LENGTH_SHORT).show()
    }


    private fun handleAfkGeneration() {
        val lastClosedTime = sharedPreferences.getLong("last_closed_time", System.currentTimeMillis())
        val currentTime = System.currentTimeMillis()
        val timeElapsedInMillis = currentTime - lastClosedTime

        val afkLevel = dashboardViewModel.improvements.value?.get("afk") ?: 0
        val efficiencyLevel = dashboardViewModel.improvements.value?.get("efficiency") ?: 0
        val passiveLevel = dashboardViewModel.improvements.value?.get("passive") ?: 0

        val maxAfkMillis = (afkLevel * 3600000L)
        val effectiveAfkMillis = min(timeElapsedInMillis, maxAfkMillis)

        val interval = when (passiveLevel) {
            0 -> 5000L
            1 -> 2000L
            2 -> 1500L
            3 -> 1000L
            4 -> 800L
            5 -> 500L
            6 -> 300L
            7 -> 150L
            8 -> 50L
            else -> 5000L
        }

        val baseIncrement = 20 + (efficiencyLevel * 5)
        val passiveMovements = effectiveAfkMillis / interval
        val bananasGenerated = (passiveMovements * baseIncrement / 105).toInt()

        if (bananasGenerated > 0) {
            Toast.makeText(
                requireContext(),
                "Your monkey earned $bananasGenerated bananas while you were away!",
                Toast.LENGTH_LONG
            ).show()
            dashboardViewModel.setBananaCount(
                (dashboardViewModel.bananaCount.value ?: 0) + bananasGenerated
            )
        }
    }
}